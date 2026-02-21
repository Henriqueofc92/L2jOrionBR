@echo off
color 0B
title L2jOrion - GameServer Register

set LIBS_DIR=..\libs
set CLASSPATH=%LIBS_DIR%\*;%LIBS_DIR%\l2jserver.jar

where java >nul 2>&1
if ERRORLEVEL 1 (
    echo [ERRO] Java nao encontrado no PATH. Instale o JDK e tente novamente.
    pause
    exit /b 1
)

echo ============================================
echo   L2jOrion - GameServer Register
echo ============================================
echo.

java -Dfile.encoding=UTF-8 -cp %CLASSPATH% l2jorion.gsregistering.GameServerRegister

echo.
echo Registro finalizado.
pause
