# MagicBlocks
***
### Ce plugin vous permet de faire des choses comme ça :</br>
![example_1](https://github.com/tytraman/magicblocks/blob/master/example_1.gif?raw=true)</br>
![example_2](https://github.com/tytraman/magicblocks/blob/master/example_2.gif?raw=true)</br>
![example_3](https://github.com/tytraman/magicblocks/blob/master/example_3.gif?raw=true)</br>
### Fonctionnalités :
* Créer des groupes de blocs, ajouter / supprimer des blocs sans limitation
* Choisir deux matériaux sur lesquels les blocs d'un groupe changeront selon un laps de temps
* Changer le laps de temps de chaque groupe *(temps en millisecondes compris entre 50 et la valeur maximale d'une variable long en Java)*
* Resynchronisation automatique des timers *(c'est comme les aiguilles d'une montre, au bout d'un moment y a un retard qui apparaît)*

### Liste des commandes :
* `/magic add <nom du groupe>` - Ajoute le bloc visé dans le groupe spécifié
* `/magic remove <nom du groupe>` - Retire le bloc visé du groupe spécifié
* `/magic settimer <nom du groupe>` - Change le laps de temps entre chaque changement de bloc du groupe spécifié *(temps en millisecondes)*
* `/magic showtimer <nom du groupe>` - Affiche le laps de temps du groupe spécifié
* `/magic replace <nom du groupe> <matériel 1> <matériel 2>` - Détermine les blocs voulus dans le groupe spécifié, liste des items existants dans Minecraft : https://minecraft-ids.grahamedgecombe.com
* `/magic showmaterials <nom du groupe>` - Affiche les matériaux du groupe spécifié
* `/magic deletegroup <nom du groupe>` - Supprime un groupe tout entier *(cela aura pour effet de totalement arrêter le changement de blocs du groupe puisqu'il n'existe plus)*
* `/magic number` - Affiche le nombre de groupes ainsi que de blocs ajoutés
* `/magic checkerror` - Affiche tous les blocs de chaque groupe *(commande utilisée pendant le developpement pour justement voir s'il y avait des problèmes)*
* `/magic reset yes` - Supprimer l'intégralité des groupes, **à utiliser avec précautions !**

### Quelques trucs à savoir :
Le plugin en tant que tel n'utilise pas beaucoup de RAM ni trop de performance, mais chaque bloc ajouté augmentera la charge de travail du CPU ainsi que l'utilisation de la RAM.
