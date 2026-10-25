<#
    Testrunde for måleverdi-tjenesten.

    Kjør fra prosjektroten mens applikasjonen og databasen kjører:
        .\test-api.ps1

    Registreringen av døgnverdier legger inn data for datoen i $Date.
    Kjører du skriptet flere ganger, bruk en ny dato eller nullstill databasen
    med "docker compose down -v", ellers gir den andre runden duplikater.
        .\test-api.ps1 -Date 2026-10-22
#>

param(
    [string]$BaseUrl = "http://localhost:8080/metering-points",
    [string]$Gsrn = "707057500000000001",
    [string]$Date = "2026-10-23"
)

function Test-Api {
    param(
        [string]$Title,
        [string]$Expected,
        [string]$Method,
        [string]$Uri,
        [string]$Body
    )

    Write-Host ""
    Write-Host "=== $Title (forventet: $Expected) ===" -ForegroundColor Cyan

    try {
        $params = @{ Method = $Method; Uri = $Uri; ContentType = "application/json" }
        if ($Body) { $params.Body = $Body }
        $result = Invoke-RestMethod @params
        Write-Host "Status: OK" -ForegroundColor Green
        $result | ConvertTo-Json -Depth 5
    } catch {
        $status = $_.Exception.Response.StatusCode.value__
        Write-Host "Status: $status" -ForegroundColor Yellow
        $_.ErrorDetails.Message
    }
}

$nextDate = ([datetime]::ParseExact($Date, "yyyy-MM-dd", $null)).AddDays(1).ToString("yyyy-MM-dd")
$from = "${Date}T00:00:00Z"
$to = "${nextDate}T00:00:00Z"

# ---------------------------------------------------------------
# 1. Målepunkter
# ---------------------------------------------------------------

Test-Api -Title "Opprett målepunkt" -Expected "201, eller 409 hvis det finnes" `
    -Method Post -Uri $BaseUrl `
    -Body "{`"gsrn`":`"$Gsrn`",`"name`":`"Testpunkt`"}"

Test-Api -Title "Samme GSRN igjen" -Expected "409" `
    -Method Post -Uri $BaseUrl `
    -Body "{`"gsrn`":`"$Gsrn`",`"name`":`"Testpunkt`"}"

Test-Api -Title "Ugyldig GSRN" -Expected "400" `
    -Method Post -Uri $BaseUrl `
    -Body '{"gsrn":"123","name":"Feil"}'

Test-Api -Title "Hent målepunkt som ikke finnes" -Expected "404" `
    -Method Get -Uri "$BaseUrl/999"

# ---------------------------------------------------------------
# 2. Registrer et helt døgn
# ---------------------------------------------------------------

$readings = foreach ($h in 0..23) {
    [pscustomobject]@{
        periodStart = "{0}T{1:D2}:00:00Z" -f $Date, $h
        quantityKwh = [math]::Round((Get-Random -Minimum 0.3 -Maximum 2.5), 3)
    }
}
$dayBody = @{ readings = @($readings) } | ConvertTo-Json -Depth 3

Test-Api -Title "Registrer 24 timesverdier for $Date" -Expected "201" `
    -Method Post -Uri "$BaseUrl/$Gsrn/readings" -Body $dayBody

# ---------------------------------------------------------------
# 3. Hent verdier og forbruk
# ---------------------------------------------------------------

Test-Api -Title "Hent verdier for døgnet" -Expected "24 verdier" `
    -Method Get -Uri "$BaseUrl/$Gsrn/readings?from=$from&to=$to"

Test-Api -Title "Forbruk for døgnet" -Expected "summen av verdiene" `
    -Method Get -Uri "$BaseUrl/$Gsrn/consumption?from=$from&to=$to"

Test-Api -Title "Forbruk for periode uten verdier" -Expected "0" `
    -Method Get -Uri "$BaseUrl/$Gsrn/consumption?from=2025-01-01T00:00:00Z&to=2025-01-02T00:00:00Z"

# ---------------------------------------------------------------
# 4. Feilsituasjoner for måleverdier
# ---------------------------------------------------------------

Test-Api -Title "GSRN som ikke finnes" -Expected "404" `
    -Method Get -Uri "$BaseUrl/707057599999999999/readings?from=$from&to=$to"

Test-Api -Title "Negativ verdi" -Expected "400" `
    -Method Post -Uri "$BaseUrl/$Gsrn/readings" `
    -Body "{`"readings`":[{`"periodStart`":`"${Date}T10:00:00Z`",`"quantityKwh`":-1.0}]}"

Test-Api -Title "Tom liste" -Expected "400" `
    -Method Post -Uri "$BaseUrl/$Gsrn/readings" `
    -Body '{"readings":[]}'

Test-Api -Title "Duplikat time" -Expected "409 (gir 500 før valideringen er på plass)" `
    -Method Post -Uri "$BaseUrl/$Gsrn/readings" `
    -Body "{`"readings`":[{`"periodStart`":`"${Date}T10:00:00Z`",`"quantityKwh`":1.0}]}"

Test-Api -Title "Tidspunkt som ikke er på hel time" -Expected "400 (lagres før valideringen er på plass)" `
    -Method Post -Uri "$BaseUrl/$Gsrn/readings" `
    -Body "{`"readings`":[{`"periodStart`":`"${Date}T10:17:00Z`",`"quantityKwh`":1.0}]}"

Test-Api -Title "Samme time to ganger i forespørselen" -Expected "400" `
    -Method Post -Uri "$BaseUrl/$Gsrn/readings" `
    -Body "{`"readings`":[{`"periodStart`":`"2026-10-21T10:00:00Z`",`"quantityKwh`":1.0},{`"periodStart`":`"2026-10-21T10:00:00Z`",`"quantityKwh`":2.0}]}"

Write-Host ""
Write-Host "Ferdig." -ForegroundColor Cyan
