@echo off
color 0A
title L2jOrion - Auth Server

set LIBS_DIR=..\libs
set JAVA_OPTS=-Xms128m -Xmx256m
set LOG_OPTS=-Djava.util.logging.manager=l2jorion.util.L2LogManager
set CLASSPATH=%LIBS_DIR%\*;%LIBS_DIR%\l2jserver.jar

where java >nul 2>&1
if ERRORLEVEL 1 (
    echo [ERRO] Java nao encontrado no PATH. Instale o JDK e tente novamente.
    pause
    exit /b 1
)

echo ============================================
echo   L2jOrion - Auth Server
echo ============================================
:start
echo [%date% %time%] Iniciando Auth Server...
java %JAVA_OPTS% %LOG_OPTS% -Dfile.encoding=UTF-8 -cp %CLASSPATH% l2jorion.login.L2LoginServer

set EXIT_CODE=%ERRORLEVEL%

if %EXIT_CODE%==2 goto restart
if %EXIT_CODE%==1 goto error
goto end

:restart
echo.
echo [%date% %time%] Admin Restart solicitado. Reiniciando em 5 segundos...
ping -n 5 localhost >nul
goto start

:error
echo.
echo [%date% %time%] Auth Server encerrado com erro (code: %EXIT_CODE%).
ping -n 5 localhost >nul
goto start

:end
echo.
echo [%date% %time%] Auth Server encerrado normalmente.
echo.
set /p "choix=Reiniciar(r) ou Sair(q)? [q]: "
if /i "%choix%"=="r" goto start
exit
