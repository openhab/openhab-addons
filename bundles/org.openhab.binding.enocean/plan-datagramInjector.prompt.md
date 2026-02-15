## Plan: Symmetrischer Datagram Injector (FTK Start, ohne Transform-Service)

Ziel ist ein send-only `datagramInjector`-Pfad, der symmetrisch zur bestehenden EEP-Typisierung aufgebaut ist: statt externer MAP/Transformationen wird eine interne Profil-/Codec-Struktur genutzt. Einstieg erfolgt mit einem konkreten Profil für FTK (`D5-00-01`) auf Basis der Spezifikation in `EEP_FTK.txt`.

Zusätzlich muss die bereits als PR vorliegende Alternative zu `senderIdOffset` umgesetzt werden: `senderAddress` (vollständige 32-bit Senderadresse in Hex), mit Vorrang vor `senderIdOffset` und RS485-Validierung.

**Architekturentscheidungen**
- Eigener Handler (`EnOceanDatagramInjectorHandler`) statt Vererbung vom Sensor-Handler.
- Interne Mapping-/Codec-Registry (profilbasiert), kein openHAB Transform-Service.
- Start mit genau einem Profil: `FTK_D5_00_01`.
- Erweiterbar auf weitere Geräteprofile analog zu `EEPType`.
- Senderidentität dual:
  - Standard: `senderIdOffset` (bestehendes Verhalten)
  - Alternative: `senderAddress` (PR-Lösung), hat Vorrang.

**Schritt A: PR-Lösung für Senderadresse (Pflichtteil)**
1. Parameter `senderAddress` ergänzen (wie in PR):
   - `EnOceanBindingConstants`: `PARAMETER_SENDERADDRESS`
   - `EnOceanActuatorConfig`: `@Nullable String senderAddress`
2. Validierungs-/Initialisierungslogik im TX-Handler übernehmen:
   - Wenn `senderAddress` gesetzt: hex-dekodieren, Länge = 4 Byte prüfen, als `senderId` verwenden.
   - `senderAddress` hat Vorrang vor `senderIdOffset`.
   - Bei `senderAddress` keine Offset-Reservierung am Bridge-Allocator.
3. RS485-Guard übernehmen:
   - `EnOceanBridgeHandler#isRS485Enabled()` bereitstellen.
   - `senderAddress` nur zulassen, wenn Bridge im RS485-Modus läuft.
   - Sonst `CONFIGURATION_ERROR` mit klarer Fehlermeldung.
4. XML/i18n für relevante sendende Thing-Typen ergänzen:
   - Label/Description für `senderAddress`
   - Beschreibung: „32-bit hex, 8 Stellen, Vorrang vor senderIdOffset“.

**Schritt B: DatagramInjector V1 (FTK) auf dieser Basis**
1. Neues Thing-Modell `datagramInjector` in `OH-INF/thing/DatagramInjector.xml` definieren.
   - Konfiguration: `sendingProfileId`, `senderIdOffset`, `senderAddress`, `broadcastMessages`, `suppressRepeating`, optional `enoceanId`.
   - Identitätsauflösung wie oben: `senderAddress` vor `senderIdOffset`.
2. Neue Thing-Type-Konstante `THING_TYPE_DATAGRAMINJECTOR` in `EnOceanBindingConstants` ergänzen und in `SUPPORTED_DEVICE_THING_TYPES_UIDS` aufnehmen.
3. Neues Profil-Enum/Registry einführen (z. B. `DatagramProfileType`), initial mit `FTK_D5_00_01`.
4. Codec-Schnittstelle für Outbound definieren (z. B. `DatagramCommandEncoder`).
   - Methode: `encode(channelId, channelTypeId, command, channelConfig, currentStateProvider)` → ERP1 payload bytes.
5. FTK-Encoder implementieren (`FtkD5_00_01Encoder`).
   - Mapping gemäß `EEP_FTK.txt`: `CLOSED -> 0x09`, `OPEN -> 0x08` (DB3), Teach-In optional `0x00`.
6. `EnOceanDatagramInjectorHandler` implementieren auf Basis von `EnOceanBaseThingHandler`.
   - `validateConfig`: Profil + Senderidentität (`senderAddress`/`senderIdOffset`) + Zieladresse.
   - `handleCommand`: Command an Encoder, ERP1 bauen, via Bridge senden.
   - Keine PacketListener-Registrierung, keine RX-EEP-Pflicht.
7. Handler-Factory (`EnOceanHandlerFactory`) erweitern, damit `datagramInjector` den neuen Handler erhält.
8. i18n ergänzen (`enocean.properties`) für Thing/Config/Channel inkl. Profiloption `FTK_D5_00_01` und `senderAddress`.
9. README erweitern:
   - Neues Kapitel „Datagram Injector (Profile-based, send-only)“.
   - FTK-Beispiel inkl. `senderAddress`-Variante und `senderIdOffset`-Variante.

**FTK V1 Mapping**
- Profil: `FTK_D5_00_01`
- EEP: `D5-00-01` (1BS, ORG/RORG `0x06`)
- Nutzdaten:
  - Kontakt geschlossen → `0x09`
  - Kontakt offen → `0x08`
  - Teach-In Telegramm DB3..DB0 → `0x00,0x00,0x00,0x00` (optional)

**Verification**
- Build: `mvn clean install -DskipTests` im Binding-Verzeichnis.
- Formatierung: `mvn spotless:apply` im Binding-Verzeichnis.
- Funktionstest Senderidentität:
  - Fall 1: `senderIdOffset` gesetzt, `senderAddress` leer → bestehendes Verhalten.
  - Fall 2: `senderAddress` gesetzt, RS485 aktiv → direkte 32-bit Senderadresse wird verwendet.
  - Fall 3: `senderAddress` gesetzt, RS485 inaktiv → erwarteter `CONFIGURATION_ERROR`.
- Funktionstest FTK:
  - Injector-Thing mit Profil `FTK_D5_00_01` anlegen.
  - `OPEN`/`CLOSED` Command senden, Bus auf `0x08/0x09` prüfen.
- Negativtests:
  - Ungültiges Profil/Command oder ungültiges `senderAddress` (nicht-hex / !=8 Zeichen) → keine Sendung + klare Fehlermeldung.

**Abgrenzung V1**
- Kein RX-Pfad für den Injector.
- Kein generischer Hex-Injector.
- Keine zusätzlichen Geräteprofile außer FTK im ersten Schnitt.
