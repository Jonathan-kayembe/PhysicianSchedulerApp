@echo off
REM Script batch pour migrer les mots de passe avec curl
REM Nécessite curl installé (généralement inclus dans Windows 10+)

echo Migration des mots de passe en cours...
echo.

curl -X POST http://localhost:8080/auth/migrate-all-passwords ^
  -H "Content-Type: application/json" ^
  -w "\n\nCode HTTP: %%{http_code}\n"

echo.
echo Migration terminée!
echo.
pause

