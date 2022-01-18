@ECHO OFF
CALL :RESOLVEPATH "..\..\Tools\apktool.bat"
SET APKTOOL=%RP%

"%APKTOOL%" d wayin.apk -o wayin
pause

:RESOLVEPATH
  SET RP=%~f1
  EXIT /B