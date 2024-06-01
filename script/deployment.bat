@echo off
setlocal

rem Déclaration des variables
set projet=Sprint1
set temp=.\..\temp
rem set web=.\views
set conf=.\..\..\test\conf
set lib=.\..\lib
set src=.\..\src
set bin=.\..\bin
@REM set package=mg\itu
set destination=C:\Program Files\Apache Software Foundation\Tomcat 10.1\webapps

rem Vérifier si le dossier temp existe
if exist "%temp%\" (
    rd /S /Q "%temp%"
)

rem Création d'un dossier temp avec les contenu de base si le dossier temp n'existe pas
mkdir "%temp%"
mkdir "%temp%\views"
mkdir "%temp%\WEB-INF"
mkdir "%temp%\WEB-INF\lib"
mkdir "%temp%\WEB-INF\classes"  

rem Copie des élements indispensables pour tomcat vers temp
rem copy /Y  ".\index.jsp" "%temp%"
rem xcopy /E /I /Y "%web%\" "%temp%\views"
xcopy /E /I /Y "%conf%\" "%temp%\WEB-INF\"

rem Compilation des codes java vers le dossier bin
call compilateur.bat


rem création de fichier .jar
jar cvf "%lib%\%projet%.jar" -C "%bin%" . 

@REM if exist "%bin%\%package%\prom16\"  (
@REM     rd /S /Q "%bin%\%package%\prom16\"
@REM )

@REM if exist "%bin%\%package%\annotation\" (
@REM     rd /S /Q "%bin%\%package%\annotation\"
@REM )

rem Copie des élements nécessaires vers classes de tomcat
xcopy /E /I /Y "%lib%\" "%temp%\WEB-INF\lib"
xcopy /E /I /Y "%bin%\" "%temp%\WEB-INF\classes"

rem Déplacement du répertoire actuel vers temp
cd /D "%temp%"

rem Compresser dans un fichier war
jar -cvf "..\%projet%".war *

rem Déplacement du répertoire actuel vers le projet
cd /D ..\

rem Copie des élements indispensables pour tomcat vers temp
copy /Y  ".\%projet%.war" "%destination%"

endlocal
