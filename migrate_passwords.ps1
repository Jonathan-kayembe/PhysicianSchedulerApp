# Script PowerShell pour migrer les mots de passe
# Exécutez ce script depuis PowerShell dans le répertoire du projet

Write-Host "Migration des mots de passe en cours..." -ForegroundColor Yellow

$uri = "http://localhost:8080/auth/migrate-all-passwords"

try {
    $response = Invoke-RestMethod -Uri $uri -Method Post -ContentType "application/json"
    
    Write-Host "`n=== Résultat de la Migration ===" -ForegroundColor Green
    Write-Host "Succès: $($response.success)" -ForegroundColor $(if ($response.success) { "Green" } else { "Red" })
    Write-Host "Message: $($response.message)" -ForegroundColor Cyan
    
    if ($response.migrated) {
        Write-Host "Mots de passe migrés: $($response.migrated)" -ForegroundColor Green
    }
    if ($response.skipped) {
        Write-Host "Mots de passe ignorés (déjà hashés): $($response.skipped)" -ForegroundColor Yellow
    }
    if ($response.errors) {
        Write-Host "Erreurs: $($response.errors)" -ForegroundColor Red
    }
    if ($response.totalUsers) {
        Write-Host "Total d'utilisateurs: $($response.totalUsers)" -ForegroundColor Cyan
    }
    
    if ($response.errorMessages) {
        Write-Host "`nMessages d'erreur:" -ForegroundColor Red
        foreach ($error in $response.errorMessages) {
            Write-Host "  - $error" -ForegroundColor Red
        }
    }
    
    Write-Host "`nMigration terminée!" -ForegroundColor Green
    
} catch {
    Write-Host "`nERREUR lors de la migration:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Réponse du serveur: $responseBody" -ForegroundColor Yellow
    }
    
    Write-Host "`nAssurez-vous que:" -ForegroundColor Yellow
    Write-Host "  1. L'application Spring Boot est démarrée (port 8080)" -ForegroundColor Yellow
    Write-Host "  2. La base de données MySQL est accessible" -ForegroundColor Yellow
    Write-Host "  3. Les utilisateurs existent dans la base de données" -ForegroundColor Yellow
}

