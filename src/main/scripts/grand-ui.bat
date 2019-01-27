@set BASEDIR=%~dp0

@set ARCH=x86_64
@java -version 2>&1 | findstr 64 >nul
@if ERRORLEVEL 1 set ARCH=x86

@for /f tokens^=2^ delims^=.-+_^" %%j in ('java -fullversion 2^>^&1') do set "java.major=%%j"
if %java.major%==1 (
    start javaw -Djava.ext.dirs=%BASEDIR%lib;%BASEDIR%lib\win32\%ARCH% -jar %BASEDIR%lib\grand-ui.jar
) else (
    start javaw -p %BASEDIR%lib;%BASEDIR%lib\win32\%ARCH% -m grand.ui
)
@exit
