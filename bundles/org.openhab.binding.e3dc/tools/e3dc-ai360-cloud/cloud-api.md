# Cloud-API: AI 360 und preisbasiertes Laden – Feld- und Payload-Spezifikation

Extrahiert aus dem ausgelieferten Frontend von `my.e3dc.com` (Oktober 2026). Das ist die Grundlage für eine Cloud-Anbindung im Binding: welche Requests die App/das Portal schickt, mit welchen Feldern, Einheiten und Wertebereichen.

Alle Aufrufe: `Authorization: Bearer <JWT>`, bei Bodies zusätzlich `content-type: application/json`.

| Dienst | Host |
| --- | --- |
| Auth | `https://e3dc.e3dc.com` |
| Anlagen, Speicher, Tarife | `https://installations.e3dc.com` |

## 1. Auth

```http
POST https://e3dc.e3dc.com/auth-saml/re-auth
Content-Type: multipart/form-data

reAuthToken=<JWT>
```

Antwort `200`:

```json
{ "token": "<JWT>", "reAuthToken": "<JWT>" }
```

Beide Tokens werden bei jedem Aufruf rotiert; `token` hat ein `exp`-Claim, das Portal erneuert 60 s vor Ablauf. `401` auf einem API-Call ⇒ einmal `re-auth`, dann wiederholen.

## 2. AI 360 – zwei getrennte Objekte

### 2.1 Installationsebene: `installations/{installationId}/dynamic-ems`

`GET` → `200`:

```json
{ "battery": true, "sgReady": false, "feedInTariffType": "FIXED", "isActive": true }
```

`PUT` → **204** (kein Body in der Antwort). **Immer das komplette Objekt**, es ist kein PATCH:

```json
{ "battery": true, "sgReady": false, "feedInTariffType": "ZERO_WHEN_NEGATIVE_PRICES" }
```

| Feld | Typ | Bedeutung |
| --- | --- | --- |
| `battery` | boolean | Batterie nimmt am dynamischen Tarif teil – das ist der Schalter „Hauskraftwerksbatterie preisbasiert laden?" |
| `sgReady` | boolean | SG-Ready-/§14a-Steuerung aktiv |
| `feedInTariffType` | string \| null | `"FIXED"`, `"ZERO_WHEN_NEGATIVE_PRICES"`, sonst `null` = keine Einspeise-Vergütung hinterlegt |
| `isActive` | boolean | **read-only**, AI 360 aktiv (Spiegel des Speicher-Flags) |

### 2.2 Speicherebene: `storages/{storageId}/dynamic-power-price`

`GET` → `200`, oder `204` wenn nichts konfiguriert ist:

```json
{
  "priceLimitWb": 25.0,
  "gridfee": 12.3,
  "surcharge": 2.5,
  "furthertaxes": 1.9,
  "dppEnabledWb": true,
  "dppEnabledBattery": true,
  "enableDynamicEms": true,
  "priceLimitBattery": 22.5,
  "activeMonthsBattery": 4095,
  "maxSocBattery": 100
}
```

`PUT` → **204**. Das Portal schickt zwei Varianten, jeweils **vollständig**:

```json
{ "gridfee": 12.3, "surcharge": 2.5, "furthertaxes": 1.9,
  "dppEnabledWb": true, "priceLimitWb": 25.0 }
```

```json
{ "gridfee": 12.3, "surcharge": 2.5, "furthertaxes": 1.9,
  "dppEnabledWb": true, "priceLimitWb": 25.0,
  "dppEnabledBattery": true, "enableDynamicEms": true,
  "priceLimitBattery": 22.5, "activeMonthsBattery": 4095, "maxSocBattery": 100 }
```

Die zweite Variante wird genommen, sobald `priceLimitBattery` gesetzt ist; sonst die erste (nur Wallbox). Wer im Binding schreibt, muss also **read-modify-write** machen, sonst löscht ein Batterie-Update die Wallbox-Felder.

| Feld | Typ | Einheit | Bedeutung |
| --- | --- | --- | --- |
| `priceLimitBattery` | number | ct/kWh, 3 Dezimalen | Preisschwelle Batterie: darunter wird aus dem Netz geladen |
| `priceLimitWb` | number | ct/kWh, 3 Dezimalen | Preisschwelle Wallbox |
| `gridfee` | number | ct/kWh, 3 Dezimalen | Netzentgelt |
| `surcharge` | number | ct/kWh, 2 Dezimalen | Energiepreis-Aufschlag (Prozent oder Wert, siehe Tarifsetting) |
| `furthertaxes` | number | ct/kWh, 3 Dezimalen | Steuern/Abgaben |
| `maxSocBattery` | number | % (ganzzahlig) | Bis zu welchem SOC preisbasiert geladen wird |
| `activeMonthsBattery` | number | Bitmaske 0…4095 | Monate, in denen preisbasiert geladen wird: **Bit 0 = Januar** … **Bit 11 = Dezember**; `4095` = alle |
| `dppEnabledBattery` | boolean | – | Batterie-Preisladen aktiv |
| `dppEnabledWb` | boolean | – | Wallbox-Preisladen aktiv |
| `enableDynamicEms` | boolean | – | AI 360 für diesen Speicher aktiv |

Die Bitmaske entsteht im Portal als `[jan…dec].map(→"1"/"0").reverse().join("")` und `Number("0b"+…)`, daher Januar auf dem LSB.

## 3. Fahrplan (read-only)

```http
GET installations/{installationId}/dynamic-ems/periods?start=<ISO-8601>&end=<ISO-8601>
```

`200`:

```json
[ { "start": "2026-10-18T00:00:00Z", "end": "2026-10-18T04:00:00Z",
    "emsBatteryMode": "GRID_CHARGE", "emsSgReadyMode": "FORCED" } ]
```

| Enum | Werte |
| --- | --- |
| `emsBatteryMode` | `NORMAL_OPERATION`, `PV_CHARGING_DELAYED`, `PV_CHARGING_REDUCED`, `BLOCK_DISCHARGING`, `GRID_CHARGE` |
| `emsSgReadyMode` | `NORMAL`, `RECOMMENDED`, `FORCED`, `BLOCKED` |

Das ist der einzige Weg, den Cloud-Fahrplan zu lesen – es gibt ihn nur hier, nicht per RSCP. Das genaue Datumsformat von `start`/`end` beim ersten echten Aufruf prüfen (Portal nutzt ISO-8601-Strings).

## 4. Tarif-Kontext (read, teils write)

| Methode | Pfad | Antwort |
| --- | --- | --- |
| `GET` | `tariffs/{installationId}/mode` | `{ "mode": "STATIC" \| "DYNAMIC", "vendorName": "…", "setting": … }` |
| `GET` | `tariffs/{installationId}/gross-prices?filter=validFrom>=…,validFrom<=…&timePeriod=day\|month` | `[{ "price": 28.4, "validFrom": …, "validTo": … }]` (ct/kWh) |
| `GET` | `installations/{installationId}/power-buy-price` | Bezugspreis-Konfiguration |
| `POST` | `tariffs/{installationId}/setting` | → 204, Tarif-/Aufschlags-Konfiguration |
| `POST` | `storages/{storageId}/prices/{charge\|discharge}` | → 201, eigene Preistabelle `{ "prices": [{ "from": null, "price": "0" }] }` |

## 5. Verhalten der App, das man nachbauen muss

- **AI 360 ein:** erst `PUT storages/{id}/dynamic-power-price` mit `enableDynamicEms: true`, dann `PUT installations/{id}/dynamic-ems` mit `battery`/`sgReady`/`feedInTariffType`.
- **AI 360 aus:** `enableDynamicEms: false` **und** `dynamic-ems` auf `{ "battery": false, "sgReady": false, "feedInTariffType": null }`. Nur eines von beidem zu setzen lässt die Anlage in einem Zwischenzustand.
- **`enableDynamicEms` ist ein UND** aus `dppEnabledBattery` und dem Installationsflag: `enableDynamicEms = dppEnabledBattery && isDynamicEmsEnabledForBattery`. Ein `enableDynamicEms: true` ohne `dppEnabledBattery: true` bewirkt nichts.
- **Fehler kommen als `errno`** im Body, z. B. `12003` (kein Wallbox-Preis gesetzt), `12004` (keine Wallbox vorhanden), `3010` bei `400`. Erfolg ist `204` – ein `200` ist bei den PUTs unerwartet.

## 6. Vorschlag für Kanäle im Binding

| Kanal | Typ | Zugriff | Abbildung |
| --- | --- | --- | --- |
| `ai360#active` | Switch | RW | `enableDynamicEms` (+ `dppEnabledBattery` mitziehen) |
| `ai360#battery-price-charging` | Switch | RW | `dynamic-ems.battery` |
| `ai360#sg-ready` | Switch | RW | `dynamic-ems.sgReady` |
| `ai360#feed-in-tariff-type` | String | RW | `FIXED` / `ZERO_WHEN_NEGATIVE_PRICES` / leer |
| `ai360#price-limit-battery` | Number:Dimensionless (ct/kWh) | RW | `priceLimitBattery` |
| `ai360#price-limit-wallbox` | Number:Dimensionless (ct/kWh) | RW | `priceLimitWb` |
| `ai360#charge-until-soc` | Number:Dimensionless (%) | RW | `maxSocBattery` |
| `ai360#active-months` | String (12 Zeichen `J…D`) | RW | `activeMonthsBattery` als Bitmaske |
| `ai360#current-mode` | String | RO | `emsBatteryMode` des laufenden Zeitfensters aus `periods` |
| `plan#next` | String | RO | nächstes Zeitfenster aus `periods` |
| `tariff#mode` | String | RO | `tariffs/{id}/mode` |
| `tariff#price` | Number:Dimensionless (ct/kWh) | RO | `tariffs/{id}/gross-prices` |

Praktische Randbedingungen: Die App pollt im Sekundenbereich, ein Binding sollte ≥ 60 s nehmen; `storageId` und `installationId` gehören in die Thing-Konfiguration (sie stehen in der Portal-URL `/overview/installations/<installationId>/<storageId>`), eine Discovery-API dafür ist nicht bekannt – `GET installations` liefert die Liste.

## 7. Offene Punkte vor der Implementierung

1. Datumsformat von `periods` (`start`/`end`) einmal real verifizieren.
1. Ob `PUT dynamic-ems` fehlende Felder toleriert oder `null` erzwingt (Portal schickt immer alle drei).
1. Ob `dppEnabledBattery` und `battery` serverseitig synchron gehalten werden oder das Binding beide schreiben muss.
1. Ob `surcharge` als Wert oder Prozent gemeint ist – das Portal kennt beides (`surchargePercent` / `surchargeValue`) und wandelt beim Lesen in `energyPriceSurcharge`.
