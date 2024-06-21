@echo off

REM Créer le répertoire de sortie temporaire
mkdir "out"

REM Copier tous les fichiers .java dans le répertoire de sortie temporaire
for /r "src" %%f in (*.java) do copy "%%f" "out"

REM Compiler toutes les classes en spécifiant le classpath
javac -cp "lib\*" -d "out" "out\*.java"

REM Créer le fichier JAR en spécifiant le point d'entrée et en incluant les fichiers compilés
jar cfe "lib\front-controller.jar" mg.itu.prom16.controllers.FrontController -C out .

REM Supprimer le répertoire de sortie temporaire
if exist "out" (
    rmdir /s /q "out"
)

pause
