rem @echo off
setlocal

rem Déclaration des variables
set projet=Morvenger
set temp=.\temp
set conf=..\Morvenger_Web\conf
set lib=.\lib
set src=.\src
set bin=.\bin
set destination=C:\Program Files\Apache Software Foundation\Tomcat 10.1\webapps

rem Vérifier si le dossier temp existe
if exist "%temp%\" (
    rd /S /Q "%temp%"
)

rem Création d'un dossier bin
mkdir "%bin%"

rem Création d'un dossier temp avec les contenu de base si le dossier temp n'existe pas
mkdir "%temp%"
mkdir "%temp%\WEB-INF"
mkdir "%temp%\WEB-INF\lib"
mkdir "%temp%\WEB-INF\classes"

rem Copie des élements indispensables pour tomcat vers temp
xcopy /E /I /Y "%conf%\" "%temp%\WEB-INF\"
xcopy /E /I /Y "%lib%\" "%temp%\WEB-INF\lib"

rem Compilation des codes java vers le dossier bin
call compress.bat

xcopy /E /I /Y "%bin%\" "%temp%\WEB-INF\classes"

rem Déplacement du répertoire actuel vers temp
cd /D "%temp%"

rem Compresser dans un fichier jar
jar -cvf "..\%projet%".war *

rem Déplacement du répertoire actuel vers le projet
cd /D ..\

rem Copie des élements indispensables pour tomcat vers temp
copy /Y  ".\%projet%.war" "%destination%"

rem Supprimer Toutes les inutiles 
rd /S /Q "%bin%"
rd /S /Q "%temp%"
del %projet%.war

endlocal

