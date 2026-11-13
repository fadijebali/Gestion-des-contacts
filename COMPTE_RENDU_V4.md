# Compte Rendu - TP Android Gestion des Contacts V4

## 1. Schéma de la base et choix des types
La base de données `contacts.db` contient une table unique `contacts` structurée comme suit :

| Colonne | Type SQLite | Type Kotlin | Contrainte |
| :--- | :--- | :--- | :--- |
| `_id` | INTEGER | Long | PRIMARY KEY AUTOINCREMENT |
| `nom` | TEXT | String | NOT NULL |
| `prenom` | TEXT | String | NOT NULL |
| `telephone` | TEXT | String | NOT NULL |
| `email` | TEXT | String? | - |
| `photo_uri` | TEXT | String? | - |
| `latitude` | REAL | Double? | - |
| `longitude` | REAL | Double? | - |
| `favori` | INTEGER | Boolean | DEFAULT 0 (0: false, 1: true) |

**Choix des types** :
- `REAL` pour les coordonnées GPS afin de conserver la précision décimale.
- `TEXT` pour le téléphone pour supporter les préfixes `+` et les espaces.
- `INTEGER` (0/1) pour le statut favori, SQLite ne possédant pas de type Boolean natif.

## 2. Rôle du Helper et du DAO
- **ContactDbHelper** : Gère le cycle de vie de la base. Il contient le script de création (`onCreate`) et la logique de migration (`onUpgrade`). Pour la V4, il gère le passage de la version 1 à 2 en ajoutant la colonne `favori` via un `ALTER TABLE`.
- **ContactDao** : C'est l'unique point d'entrée pour les requêtes SQL. Il transforme les objets `Contact` en `ContentValues` pour l'écriture et les `Cursor` en objets `Contact` pour la lecture.

## 3. Stratégie de threads
Tous les accès à la base de données sont effectués hors du thread principal pour garantir la fluidité de l'interface :
- Utilisation de `viewModelScope` dans le `ContactViewModel`.
- Utilisation de `withContext(Dispatchers.IO)` pour les appels bloquants au DAO.
- Les données sont renvoyées à l'UI via des `LiveData`.

## 4. Choix de filtrage (SQL ou mémoire)
**Choix : SQL**.
Le filtrage est réalisé directement en base de données via la clause `LIKE` dans la méthode `search(query)`.
**Justification** : Cela permet de trier les résultats directement via le moteur SQLite (`ORDER BY`) et de ne charger en mémoire que les contacts correspondants, ce qui est plus efficace si la liste devient très longue.

## 5. Gestion des photos
Pour éviter de perdre l'accès aux photos (les URI de la galerie étant temporaires), chaque image sélectionnée est :
1. Copiée dans le dossier interne de l'application (`context.filesDir`).
2. Renommée de façon unique.
3. Son chemin local est enregistré en base.
Ainsi, la photo reste disponible même après un redémarrage de l'appareil ou une mise à jour système.

## 6. Préférences utilisateur
- **Stockage** : `SharedPreferences` via la bibliothèque `androidx.preference`.
- **Lecture** : Centralisée dans la classe `PreferencesManager`.
- **Application immédiate** : Dans `ListeContactsFragment`, la méthode `onResume()` relit les préférences et met à jour l'Adapter, forçant un rafraîchissement visuel sans redémarrer l'activité.

## 7. Tests réalisés
- **Persistence** : Ajout d'un contact, fermeture "kill" de l'app, vérification de la présence au redémarrage.
- **SQL Injection** : Recherche d'un nom contenant une apostrophe (ex: `L'Ariana`) -> succès grâce aux `selectionArgs`.
- **Migration** : Passage de la V1 à la V2 sans perte de données.
- **Validation** : Tentative d'enregistrement sans nom -> message d'erreur bloquant.

## 8. Difficultés rencontrées
- La gestion des menus avec le nouveau `MenuHost` API pour afficher/masquer la corbeille dynamiquement.
- L'alignement du design avec le modèle fourni, notamment pour le bouton "Enregistrer" en bas et la Toolbar.

---

## 9. Questions de compréhension

**1. Quand les méthodes onCreate et onUpgrade sont-elles appelées ?**
- `onCreate` : Uniquement lors de la toute première création du fichier de base de données.
- `onUpgrade` : Lorsque la constante `DATABASE_VERSION` dans le code est supérieure à la version enregistrée dans le fichier `.db` existant.
*Si on modifie onCreate sans changer la version, les changements ne s'appliqueront jamais sur les téléphones où l'app est déjà installée.*

**2. Pourquoi utiliser des arguments ? plutôt que concaténer ?**
Pour éviter les **attaques par injection SQL**. Si l'utilisateur saisit `' OR '1'='1`, il pourrait bypasser une condition ou corrompre la base. Les `?` traitent l'entrée comme une simple donnée texte.

**3. Que renvoient insert(), update() et delete() ?**
- `insert` : Le `rowId` (Long) de la nouvelle ligne, ou -1 en cas d'échec.
- `update` / `delete` : Le nombre de lignes affectées (Int).
*On vérifie si ces valeurs sont > 0 pour confirmer le succès à l'utilisateur via un Toast.*

**4. Pourquoi l'accès à la base ne doit-il pas se faire sur le thread principal ?**
L'accès disque est lent. S'il dure plus de 5 secondes, Android affiche un message "L'application ne répond pas" (ANR) et tue le processus. Nous utilisons les **Coroutines** pour l'éviter.

**5. Pourquoi le helper doit-il être unique et construit avec applicationContext ?**
Pour éviter les fuites de mémoire (un `Context` d'Activity resterait bloqué en mémoire) et pour éviter les conflits d'ouverture/fermeture de base simultanés.

**6. SharedPreferences ou SQLite : quels critères ?**
- **SharedPreferences** : Données simples (clés/valeurs), réglages UI, flags. Utilisé ici pour les préférences.
- **SQLite** : Données complexes, structurées, volumineuses ou liées entre elles. Utilisé ici pour les contacts.

**7. Comment les préférences modifiées sont-elles appliquées sans redémarrer ?**
En utilisant le cycle de vie du fragment : `onResume()` est appelé à chaque retour sur la liste. On y force la relecture de `PreferencesManager` et on appelle `adapter.notifyDataSetChanged()`.

**8. Différence entre ACTION_DIAL et ACTION_CALL ?**
- `ACTION_DIAL` : Ouvre le clavier téléphonique avec le numéro pré-rempli. **Aucune permission** requise.
- `ACTION_CALL` : Lance l'appel immédiatement. Requiert la permission dangereuse `CALL_PHONE`.

**9. Que deviennent la base et les préférences lors de la désinstallation ?**
Tout est **supprimé** par le système Android. De même, si l'utilisateur clique sur "Effacer les données" dans les paramètres Android, la base et les préférences sont réinitialisées.
