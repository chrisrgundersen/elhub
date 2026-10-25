#!/usr/bin/env bash
#
# Testrunde for måleverdi-tjenesten (bash-variant av test-api.ps1, for macOS/Linux).
#
# Kjør fra prosjektroten mens applikasjonen og databasen kjører:
#     ./test-api.sh
#
# Registreringen av døgnverdier legger inn data for datoen i -d.
# Kjører du skriptet flere ganger, bruk en ny dato eller nullstill databasen
# med "docker compose down -v", ellers gir den andre runden duplikater.
#     ./test-api.sh -d 2026-10-22
#
# Valg:
#     -u BASE_URL   (standard: http://localhost:8080/metering-points)
#     -g GSRN       (standard: 707057500000000001)
#     -d DATE       (standard: 2026-10-23)

BASE_URL="http://localhost:8080/metering-points"
GSRN="707057500000000001"
DATE="2026-10-23"

while getopts "u:g:d:" opt; do
    case $opt in
        u) BASE_URL="$OPTARG" ;;
        g) GSRN="$OPTARG" ;;
        d) DATE="$OPTARG" ;;
        *) echo "Bruk: $0 [-u BASE_URL] [-g GSRN] [-d YYYY-MM-DD]" >&2; exit 1 ;;
    esac
done

CYAN=$'\033[36m'
GREEN=$'\033[32m'
YELLOW=$'\033[33m'
RESET=$'\033[0m'

pretty_json() {
    if command -v jq >/dev/null 2>&1; then
        jq . 2>/dev/null || cat
    elif command -v python3 >/dev/null 2>&1; then
        python3 -m json.tool 2>/dev/null || cat
    else
        cat
    fi
}

test_api() {
    local title="$1" expected="$2" method="$3" uri="$4" body="$5"

    echo ""
    echo "${CYAN}=== $title (forventet: $expected) ===${RESET}"

    local args=(-s -w $'\n%{http_code}' -X "$method" -H "Content-Type: application/json")
    if [ -n "$body" ]; then
        args+=(-d "$body")
    fi

    local response status content
    response=$(curl "${args[@]}" "$uri")
    status="${response##*$'\n'}"
    content="${response%$'\n'*}"

    if [ "$status" -ge 200 ] && [ "$status" -lt 300 ]; then
        echo "${GREEN}Status: OK ($status)${RESET}"
    else
        echo "${YELLOW}Status: $status${RESET}"
    fi
    if [ -n "$content" ]; then
        echo "$content" | pretty_json
    fi
}

# Neste dag: BSD date (macOS) og GNU date (Linux) har ulik syntaks
if NEXT_DATE=$(date -j -v+1d -f "%Y-%m-%d" "$DATE" "+%Y-%m-%d" 2>/dev/null); then
    :
else
    NEXT_DATE=$(date -d "$DATE + 1 day" "+%Y-%m-%d")
fi
FROM="${DATE}T00:00:00Z"
TO="${NEXT_DATE}T00:00:00Z"

# ---------------------------------------------------------------
# 1. Målepunkter
# ---------------------------------------------------------------

test_api "Opprett maalepunkt" "201, eller 409 hvis det finnes" \
    POST "$BASE_URL" \
    "{\"gsrn\":\"$GSRN\",\"name\":\"Testpunkt\"}"

test_api "Samme GSRN igjen" "409" \
    POST "$BASE_URL" \
    "{\"gsrn\":\"$GSRN\",\"name\":\"Testpunkt\"}"

test_api "Ugyldig GSRN" "400" \
    POST "$BASE_URL" \
    '{"gsrn":"123","name":"Feil"}'

test_api "Hent alle maalepunkt" "200, liste med maalepunkt" \
    GET "$BASE_URL"

# ---------------------------------------------------------------
# 2. Registrer et helt døgn
# ---------------------------------------------------------------

READINGS=$(awk -v date="$DATE" 'BEGIN {
    srand()
    for (h = 0; h < 24; h++) {
        sep = (h < 23) ? "," : ""
        printf "{\"periodStart\":\"%sT%02d:00:00Z\",\"quantityKwh\":%.3f}%s", date, h, 0.3 + rand() * 2.2, sep
    }
}')
DAY_BODY="{\"readings\":[$READINGS]}"

test_api "Registrer 24 timesverdier for $DATE" "201" \
    POST "$BASE_URL/$GSRN/readings" "$DAY_BODY"

# ---------------------------------------------------------------
# 3. Hent verdier og forbruk
# ---------------------------------------------------------------

test_api "Hent verdier for dognet" "24 verdier" \
    GET "$BASE_URL/$GSRN/readings?from=$FROM&to=$TO"

test_api "Forbruk for dognet" "summen av verdiene" \
    GET "$BASE_URL/$GSRN/consumption?from=$FROM&to=$TO"

test_api "Forbruk for periode uten verdier" "0" \
    GET "$BASE_URL/$GSRN/consumption?from=2025-01-01T00:00:00Z&to=2025-01-02T00:00:00Z"

# ---------------------------------------------------------------
# 4. Feilsituasjoner for måleverdier
# ---------------------------------------------------------------

test_api "GSRN som ikke finnes" "404" \
    GET "$BASE_URL/707057599999999999/readings?from=$FROM&to=$TO"

test_api "Negativ verdi" "400" \
    POST "$BASE_URL/$GSRN/readings" \
    "{\"readings\":[{\"periodStart\":\"${DATE}T10:00:00Z\",\"quantityKwh\":-1.0}]}"

test_api "Tom liste" "400" \
    POST "$BASE_URL/$GSRN/readings" \
    '{"readings":[]}'

test_api "Duplikat time" "409" \
    POST "$BASE_URL/$GSRN/readings" \
    "{\"readings\":[{\"periodStart\":\"${DATE}T10:00:00Z\",\"quantityKwh\":1.0}]}"

test_api "Tidspunkt som ikke er paa hel time" "400" \
    POST "$BASE_URL/$GSRN/readings" \
    "{\"readings\":[{\"periodStart\":\"${DATE}T10:17:00Z\",\"quantityKwh\":1.0}]}"

test_api "Samme time to ganger i foresporselen" "400" \
    POST "$BASE_URL/$GSRN/readings" \
    '{"readings":[{"periodStart":"2026-10-21T10:00:00Z","quantityKwh":1.0},{"periodStart":"2026-10-21T10:00:00Z","quantityKwh":2.0}]}'

echo ""
echo "${CYAN}Ferdig.${RESET}"
