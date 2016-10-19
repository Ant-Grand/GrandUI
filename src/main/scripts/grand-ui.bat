@set BASEDIR=%~dp0

@set ARCH=x86_64
@java -version 2>&1 | findstr 64 >nul
@if ERRORLEVEL 1 set ARCH=x86

start javaw -Djava.ext.dirs=%BASEDIR%lib;%BASEDIR%lib\win32\%ARCH% -jar %BASEDIR%lib\grand-ui.jar
@exit
