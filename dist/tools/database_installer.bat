@Echo off
SETLOCAL EnableDelayedExpansion
:: Configurações Visuais e de Identidade
COLOR 0B
TITLE L2jOrion: Professional MariaDB Suite
SET "LOG_FILE=l2jorion_setup.log"

:: Inicializa o Log com cabeçalho limpo
echo ========================================================== > %LOG_FILE%
echo   L2jOrion Database Log - %date% %time% >> %LOG_FILE%
echo ========================================================== >> %LOG_FILE%

:header
cls
echo.
echo  ######################################################################
echo  #                                                                    #
echo  #          L 2 J O R I O N   -   M A R I A D B   M A S T E R         #
echo  #                                                                    #
echo  ######################################################################
echo.
echo [!] Verificando requisitos do sistema...

:: 1. Verificação de Binários (Engine e Backup)
mariadb --help >nul 2>nul
if errorlevel 1 (
    color 0C
    echo [ERROR] MariaDB (mariadb.exe) nao encontrado no seu PATH.
    echo [LOG] Falha critica: Binarios MariaDB ausentes. >> %LOG_FILE%
    pause
    exit
)
echo [OK] Engine MariaDB pronta.

:config
echo [+] Configurando credenciais de acesso:
set DB_HOST=localhost
set DB_USER=root
set DB_PASS=
set DB_NAME=l2_database

set /P DB_HOST="  - Host do Banco [%DB_HOST%]: "
set /P DB_USER="  - Usuario [%DB_USER%]: "
set /P DB_PASS="  - Senha (vazio para none): "
set /P DB_NAME="  - Nome da DB [%DB_NAME%]: "

:: Define strings de autenticação
SET AUTH=-u %DB_USER% -h %DB_HOST%
if NOT "%DB_PASS%"=="" SET AUTH=%AUTH% --password=%DB_PASS%

:: Teste de Conexão Profissional
echo exit | mariadb %AUTH% >nul 2>nul
if errorlevel 1 (
    color 0C
    echo [ERROR] Conexao negada! Verifique Host/User/Senha.
    echo [LOG] Tentativa de login falhou para %DB_USER%@%DB_HOST% >> %LOG_FILE%
    pause
    color 0B
    goto header
)

:: Garantia de existência da Database
echo exit | mariadb %AUTH% %DB_NAME% >nul 2>nul
if errorlevel 1 (
    echo [!] Database "%DB_NAME%" nao detectada.
    set /P CREATE="[?] Criar database agora? (Y/N): "
    if /I "!CREATE!" EQU "Y" (
        echo CREATE DATABASE IF NOT EXISTS %DB_NAME% DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci; | mariadb %AUTH%
        echo [LOG] Database %DB_NAME% criada manualmente. >> %LOG_FILE%
    ) else ( goto config )
)

:menu
cls
echo ======================================================================
echo   DB: %DB_NAME%  ^|  USER: %DB_USER%  ^|  STATUS: Conectado
echo ======================================================================
echo.
echo   [1] FULL INSTALL (Backup + Login + Game + Community + Custom)
echo   [2] APENAS LOGIN SERVER
echo   [3] APENAS GAME SERVER
echo   [4] APENAS CUSTOM MODS
echo   [5] SAIR
echo.
set /P OPT="Escolha uma acao [1-5]: "

if "%OPT%"=="1" goto full_proc
if "%OPT%"=="2" set "TARGET=sql\login" & goto single_proc
if "%OPT%"=="3" set "TARGET=sql\server" & goto single_proc
if "%OPT%"=="4" set "TARGET=sql\server\custom" & goto single_proc
if "%OPT%"=="5" exit
goto menu

:full_proc
set "FOLDERS=sql\login sql\server sql\server\custom cb_sql"
goto run_backup

:single_proc
set "FOLDERS=%TARGET%"
goto run_backup

:run_backup
echo.
echo [!] Iniciando Backup de seguranca...
set "BK_NAME=backup_%DB_NAME%_%date:~-4%%date:~3,2%%date:~0,2%.sql"
:: mariadb-dump realiza a exportação completa
mariadb-dump %AUTH% %DB_NAME% > %BK_NAME% 2>nul
if errorlevel 1 (
    echo [ALERTA] Nao foi possivel criar o backup (DB vazia?). Continuando...
    echo [LOG] Tentativa de backup sem sucesso ou DB nova. >> %LOG_FILE%
) else (
    echo [OK] Backup salvo como: %BK_NAME%
    echo [LOG] Backup realizado com sucesso: %BK_NAME% >> %LOG_FILE%
)
goto execute_sql

:execute_sql
echo.
echo [+] Processando arquivos SQL...
echo ----------------------------------------------------------------------
for %%d in (%FOLDERS%) do (
    if exist "%%d" (
        echo [PASTA] %%d
        for /R "%%d" %%f in (*.sql) do (
            echo   ^> Aplicando: %%~nxf...
            :: O '2>>' envia apenas as mensagens de erro do banco para o arquivo .txt
            mariadb %AUTH% %DB_NAME% < "%%f" 2>> %LOG_FILE%
            
            if errorlevel 1 (
                echo     [!] ERRO: Verifique o log.
                echo [ERRO SQL] Falha no arquivo: %%f >> %LOG_FILE%
            ) else (
                echo [OK] %%~nxf >> %LOG_FILE%
            )
        )
    )
)

:end
echo.
echo ######################################################################
echo # INSTALACAO CONCLUIDA!                                              #
echo # Log gerado: %LOG_FILE%                                             #
echo # Backup gerado: %BK_NAME% (se aplicavel)                            #
echo ######################################################################
echo.
pause
goto menu