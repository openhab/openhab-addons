# E3/DC Cloud – AI 360 und preisbasiertes Batterieladen steuern

Kleines CLI, das genau den Schalter aus der App umlegt: **„Hauskraftwerksbatterie preisbasiert laden?"** inklusive **AI 360° aktiv/inaktiv** — über die E3/DC-Cloud (`my.e3dc.com`), nicht über RSCP.

> **Status:** Die Endpunkte sind aus dem öffentlichen Frontend des Portals gelesen (Stand Oktober 2026). Sie sind nicht dokumentiert, können sich jederzeit ändern, und dieses Tool ist kein E3/DC-Produkt. Nutzung auf eigenes Risiko, nur mit dem eigenen Account.

Wichtig: Das **alte Portal `s10.e3dc.com` ist abgeschaltet**. Alles läuft über `my.e3dc.com`; die Services dahinter sind:

| Zweck | Host |
| --- | --- |
| Login / Token-Erneuerung | `https://e3dc.e3dc.com` |
| Anlagen- und Speicherdaten | `https://installations.e3dc.com` |

## Endpunkte

Alle Aufrufe brauchen `Authorization: Bearer <JWT>`.

| Methode | Pfad | Zweck |
| --- | --- | --- |
| `POST` | `e3dc.e3dc.com/auth-saml/re-auth` | Tauscht `reAuthToken` (Formularfeld) gegen neues `token` + `reAuthToken` |
| `GET` | `installations/{installationId}/dynamic-ems` | Liest `{battery, sgReady, feedInTariffType, isActive}` |
| `PUT` | `installations/{installationId}/dynamic-ems` | Setzt `{battery, sgReady, feedInTariffType}` → **204** |
| `GET` | `storages/{storageId}/dynamic-power-price` | Liest `{enableDynamicEms, dppEnabledBattery, priceLimitBattery, …}` |
| `PUT` | `storages/{storageId}/dynamic-power-price` | Setzt z. B. `{enableDynamicEms: true}` → **204** |
| `GET` | `installations/{installationId}/dynamic-ems/periods?start=&end=` | Liest den Fahrplan (`emsBatteryMode`: `GRID_CHARGE`, `BLOCK_DISCHARGING`, normal) |
| `GET` | `tariffs/{installationId}/mode` | Tarifmodus, u. a. `STATIC` oder `AI` |
| `GET` | `tariffs/{installationId}/gross-prices` | Preisfahrplan |

`battery` in `dynamic-ems` ist der Schalter „Batterie nimmt am dynamischen Tarif teil"; `enableDynamicEms` in `dynamic-power-price` ist AI 360 für diesen Speicher.

## Tokens holen

1. `https://my.e3dc.com` öffnen und einloggen.
1. DevTools-Konsole (`F12`) und dort ausführen:

   ```js
   copy(JSON.stringify({ authToken: localStorage.authToken, reAuthToken: localStorage.reAuthToken }))
   ```

1. Inhalt in `.tokens.json` neben diesem Skript legen:

   ```json
   { "authToken": "eyJ…", "reAuthToken": "eyJ…" }
   ```

Das Skript erneuert die Tokens bei jedem Lauf und schreibt das neue Paar zurück — der `reAuthToken` wird dabei rotiert, die Kette bleibt also gültig, solange sie benutzt wird. Alternativ per Umgebungsvariable `E3DC_AUTH_TOKEN` / `E3DC_REAUTH_TOKEN`.

## Benutzung

```bash
node e3dc-cloud.mjs login                      # Tokens prüfen/erneuern
node e3dc-cloud.mjs discover                   # Anlagen auflisten (liefert installationId)
node e3dc-cloud.mjs raw installations/123456   # beliebigen Pfad lesen (z. B. für die storageId)
node e3dc-cloud.mjs status --installation 123456 --storage 789
node e3dc-cloud.mjs ai360    on  --storage 789
node e3dc-cloud.mjs ai360    off --storage 789
node e3dc-cloud.mjs battery  on  --installation 123456 --storage 789
node e3dc-cloud.mjs battery  off --installation 123456
```

`ai360` schaltet AI 360 für den Speicher, `battery` den preisbasierten Batteriebetrieb. Beide Kommandos lesen den aktuellen Stand vorher ein und schreiben nur die geänderten Felder zurück, damit Schwelle, Monate und SOC-Limit unangetastet bleiben.

Die IDs stehen im Portal in der URL, wenn eine Anlage geöffnet ist: `/overview/installations/<installationId>/<storageId>`. Falls `discover` die Speicher-ID nicht mitliefert, hilft `raw installations/<installationId>`.

## Einbindung in openHAB

Variante ohne Binding, direkt in einer Regel:

```java
rule "E3DC AI 360"
when
    Item E3DC_AI360 changed
then
    val cmd = if (E3DC_AI360.state == ON) "on" else "off"
    executeCommandLine(Duration.ofSeconds(30), "node",
        "/opt/e3dc/e3dc-cloud.mjs", "ai360", cmd, "--storage", "789")
end
```

Oder mit dem Exec-Binding als zwei Things (`ai360 on` / `ai360 off`) und einem Switch-Item, das das passende `run`-Channel anspricht. Das Skript braucht Node ≥ 18 (nutzt `fetch` und `FormData`) und Schreibrecht im eigenen Ordner für `.tokens.json`.

## Grenzen

- **Undokumentiert:** E3/DC kann Pfade, Felder oder den Auth-Flow jederzeit ändern; dann bricht das Tool, bis die Werte nachgezogen sind.
- **Rate Limits:** sparsam aufrufen (Minutentakt statt Sekundentakt), es ist derselbe API-Zugang wie die App.
- **Zwei Schalter:** „AI 360 aktiv" und „Batterie preisbasiert" sind getrennte Felder. Wer nur `battery` setzt, hat AI 360 eventuell weiterhin aktiv.
- **Token-Ablauf:** ohne regelmäßige Nutzung läuft der `reAuthToken` irgendwann ab; dann einmal neu aus dem Browser kopieren.
