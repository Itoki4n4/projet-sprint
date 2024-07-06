@echo off
setlocal enabledelayedexpansion

rem Extraire le nom du dossier root à partir du chemin du dossier source
for %%I in (".") do set "projet_name=%%~nxI"

rem Déclaration des variables
set "temp=temp"
set "lib=lib"
set "src=src"
set "bin=bin"
set "test=Test"
set "views=Test\views"
set "webappFolder=C:\apache-tomcat-10.1.30\webapps"

rem Création du dossier temp
if exist "%temp%" (
    rd /S /Q "%temp%"
)
mkdir "%temp%"

rem Création des dossiers nécessaires
mkdir "%temp%\WEB-INF"
mkdir "%temp%\views"
mkdir "%temp%\WEB-INF\lib"
mkdir "%temp%\WEB-INF\classes"

rem Récupérer la liste de tous les fichiers Java dans les sous-dossiers de %src%
set "javaFiles="
for /r "%src%" %%G in (*.java) do (
    set "javaFiles=!javaFiles! %%G"
)

rem Afficher les fichiers Java trouvés
echo Fichiers Java trouvés: !javaFiles!

rem Vérifier si des fichiers Java existent à compiler
if "!javaFiles!"=="" (
    echo Aucun fichier Java à compiler.
    exit /b 1
)

rem Construire le chemin de classe pour toutes les bibliothèques dans le dossier "lib"
set "classpath="
for %%I in ("%lib%\*.jar") do (
    set "classpath=!classpath!;%%I"
)

rem Compiler tous les fichiers Java en une seule commande avec les bibliothèques nécessaires
javac -cp "%classpath%" -parameters -d "%bin%" !javaFiles!

rem Vérification si la compilation a réussi
if not exist "%bin%\*" (
    echo La compilation a échoué. Aucun fichier dans %bin%.
    exit /b 1
)

rem Déplacement vers bin
cd "%bin%"

rem Compresser bin en un fichier jar
jar -cvf "..\lib\%projet_name%.jar" *

rem Déplacement vers le projet
cd /D ".."

rem Copie des éléments indispensables pour Tomcat vers temp
echo Copie des éléments indispensables pour Tomcat vers temp...
xcopy "%test%\" "%temp%\WEB-INF\" /E /I /Y
xcopy /E /I /Y "%views%\" "%temp%\views"
xcopy /E /I /Y "%lib%\" "%temp%\WEB-INF\lib"
xcopy /E /I /Y "%bin%\" "%temp%\WEB-INF\classes"

rem Déplacement vers temp
cd /D "%temp%"

rem Compresser le projet en un fichier war
jar -cvf "..\%projet_name%.war" *

rem Déplacement vers le projet
cd /D ".."

rem Effacer temp
rd /S /Q "%temp%"

rem Copie du fichier war vers webapps de Tomcat
copy /Y ".\%projet_name%.war" "%webappFolder%"

rem Effacer le fichier war
del ".\%projet_name%.war"

echo Deployment success !!

rem Pause pour éviter de quitter immédiatement
echo Appuyez sur une touche pour continuer...
pause >nul

endlocal
