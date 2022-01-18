@ECHO OFF
CALL :RESOLVEPATH "..\..\Tools\apktool.bat"
SET APKTOOL=%RP%

"%APKTOOL%" --use-aapt2 b wayin -o wayin_mod.apk
pause

:RESOLVEPATH
  SET RP=%~f1
  EXIT /B