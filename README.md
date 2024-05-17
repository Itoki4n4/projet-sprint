## Les classes Java

Contenus dans le dossier src, voici les classes et leur fonctionnalités:
- `FrontController`: cette classe modifier les fonctions suivantes:
    -`getClassesInSpecificPackage`: prend les classes dans le chemin specifier par init param du fichier xml
    -`scanner`: appel la fonction précédente et change la liste des controller ainsi que le boolean de vérification
- `AnnotationController`: l'anotation des controllers
- `TestController`: une classe qui à l'annotation des controllers

## Les scripts

Voici les scripts(.bat):

-`compilateur`: ceci compile tout les fichiers java dans src et les met dans le repertoire bin
-`deployement`: ceci deploi le projet framework vers le répertoire webapps de tomcat 