@echo off
title Auth Console
:start

java -Djava.util.logging.manager=l2jorion.util.L2LogManager -Xms128m -Xmx128m -cp ./../libs/*;l2jserver.jar l2jorion.login.L2LoginServer

if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end
:restart
echo.
echo Admin Restarted ...
ping -n 5 localhost > nul
echo.
goto start
:error
echo.
echo Login terminated abnormaly
ping -n 5 localhost > nul
echo.
goto start
:end
echo.
echo Login terminated
echo.
:question
set choix=q
set /p choix=Restart(r) or Quit(q)
if /i %choix%==r goto start
if /i %choix%==q goto exit
:exit
exit
pause
