@set BASEDIR=%~dp0

@java -version 2>&1 | findstr 64 >nul
@if ERRORLEVEL 1 (echo "SWT 4.10+ requires 64-bit Java" & exit /b 1)

@for /f tokens^=2^ delims^=.-+_^" %%j in ('java -fullversion 2^>^&1') do set "java.major=%%j"
if %java.major%==1 (
    start javaw -Djava.ext.dirs=%BASEDIR%lib;%BASEDIR%lib\win32 -jar %BASEDIR%lib\grand-ui.jar
) else (
    start javaw -p %BASEDIR%lib;%BASEDIR%lib\win32 -m grand.ui
)
@exit
