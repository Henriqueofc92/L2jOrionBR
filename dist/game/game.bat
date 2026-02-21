@echo off
color 0A
title L2jOrion - Game Server

set LIBS_DIR=..\libs
set JAVA_OPTS=-Xms1g -Xmx2g
set LOG_OPTS=-Djava.util.logging.manager=l2jorion.util.L2LogManager
set CLASSPATH=%LIBS_DIR%\*;%LIBS_DIR%\l2jserver.jar

where java >nul 2>&1
if ERRORLEVEL 1 (
    echo [ERRO] Java nao encontrado no PATH. Instale o JDK e tente novamente.
    pause
    exit /b 1
)

echo ============================================
echo   L2jOrion - Game Server
echo ============================================
:start
echo [%date% %time%] Iniciando Game Server...
java %JAVA_OPTS% %LOG_OPTS% -Dfile.encoding=UTF-8 -cp %CLASSPATH% l2jorion.game.GameServer

set EXIT_CODE=%ERRORLEVEL%

if %EXIT_CODE%==7 goto telldown
if %EXIT_CODE%==6 goto tellrestart
if %EXIT_CODE%==5 goto taskrestart
if %EXIT_CODE%==4 goto taskdown
if %EXIT_CODE%==2 goto restart
if %EXIT_CODE%==1 goto error
goto end

:tellrestart
echo.
echo [%date% %time%] Telnet Restart solicitado. Reiniciando em 5 segundos...
ping -n 5 localhost >nul
goto start

:taskrestart
echo.
echo [%date% %time%] Auto Task Restart. Reiniciando em 5 segundos...
ping -n 5 localhost >nul
goto start

:restart
echo.
echo [%date% %time%] Admin Restart solicitado. Reiniciando em 5 segundos...
ping -n 5 localhost >nul
goto start

:taskdown
echo.
echo [%date% %time%] Server encerrado por Auto Task.
goto end

:telldown
echo.
echo [%date% %time%] Server encerrado via Telnet.
goto end

:error
echo.
echo [%date% %time%] Server encerrado com erro (code: %EXIT_CODE%).
ping -n 5 localhost >nul
goto start

:end
echo.
echo [%date% %time%] Game Server encerrado.
echo.
set /p "choix=Reiniciar(r) ou Sair(q)? [q]: "
if /i "%choix%"=="r" goto start
exit
