# DWD Unwetter Binding

Binding zur Abfrage von aktuellen Unwetterwarnungen des Deutschen Wetterdienstes via [DWD Geoserver](https://maps.dwd.de/geoserver/web/).

## Unterstütztes Thing

Das Binding unterstützt genau ein Thing - Unwetterwarnungen.
Ein Thing stellt dabei eine oder mehrere Warnungen für eine Gemeinde bereit.


## Thing Konfiguration

| Property     | Standard | Erforderlich | Beschreibung                                                                                                                                                                                                                                                                                                                                                      |
|--------------|----------|--------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| cellId       | -        | Ja           | ID der abzufragenden Zelle. Siehe [cap_warncellids_csv.csv](https://www.dwd.de/DE/leistungen/opendata/help/warnungen/cap_warncellids_csv.csv "cap_warncellids_csv.csv"), nur IDs die mit einer 8 (Ausnahme für Berlin: 7) beginnen werden unterstützt. Es kann auch mittels % eine Gesamtmenge abgefragt werden, z.B. 8111% alle Gemeinden die mit 8111 anfangen. |
| refresh      | 30       | Nein         | Abfrageintervall in Minuten. Minimum 15 Minuten.                                                                                                                                                                                                                                                                                                                  |
| warningCount | 1        | Nein         | Anzahl der Warnungen, die als Channels bereitgestellt werden sollen                                                                                                                                                                                                                                                                                               |

### Cell ID
<!-- See page 10-13 (in German) of https://www.dwd.de/DE/wetter/warnungen_aktuell/objekt_einbindung/einbindung_karten_geodienste.pdf?__blob=publicationFile&v=14 for Cell ID documentation. -->
Verwende [diese Liste](https://www.dwd.de/DE/leistungen/opendata/help/warnungen/cap_warncellids_csv.csv) für gültige IDs, bitte bedenke dass **nur IDs die mit einer "8" beginnen und neun Ziffern haben unterstützt werden**.
Ausnahme für Berlin, wo die ID der Stadtbezirke genutzt wird. Diese beginnt mit "7".

Unter Verwendung des Prozent-Zeichens (%) als wildcard, kann man mehrere Zellen abfragen.
Zum Beispiel werden mit dem Wert `8111%` alle Zellen abgefragt, die mit `8111` beginnen.

Weitere Erläuterungen der CellID können auf Seite 10-13 von [PDF: DWD-Geoserver: Nutzung von WMS-Diensten für eigene Websites](https://www.dwd.de/DE/wetter/warnungen_aktuell/objekt_einbindung/einbindung_karten_geodienste.pdf?__blob=publicationFile&v=14) gefunden werden.

Wählt man die Cell-ID mittels des %-Operators zu groß, so kann es passieren, das gar keine Warnungen kommen.
Das ist immer Fall, wenn die zurückgelieferte XML-Datei des DWD zu groß ist, dass sie nicht intern gebuffered werden kann.
Dies ist bei ca. 300+ Warnungen der Fall.

Beispiel:

```
dwdunwetter:dwdwarnings:koeln "Warnungen Köln" [ cellId="805315000", refresh=15, warningCount=1 ]
```

## Channels

Für jede bereitgestellte Warnung werden mehrere Channels bereitgestellt. 
Die Channels sind jeweils durchnummeriert, Channels die mit 1 enden sind für die erste Warnung, Channels die mit 2 enden für die zweite Warnung usw. 
Die vom DWD gelieferten Warnungen werden dabei nach Severity (Warnstufe) sortiert und innerhalb der Warnstufe nach Beginndatum. 
Dadurch ist sichergestellt, dass in den Channels für die erste Warnung (...1) immer die Warnung mit der höchsten Warnstufe steht. 
Werden mehr Warnungen vom DWD geliefert, als an Channels konfiguriert ist, werden dadurch die Warnungen mit der niedrigsten Warnstufe verworfen.

| Channel      | Type            | Beschreibung                                                                                           |
|--------------|-----------------|--------------------------------------------------------------------------------------------------------|
| warningN     | Switch          | Schalter, der auf ON steht, wenn eine Warnung vorliegt, OFF sonst.                                     |
| UpdatedN     | Trigger Channel | Sendet das Event "NEW", wenn diese Warnung das erste mal gesendet wird.                                |
| severityN    | String          | Warnstufe, von niedrig nach hoch: Minor, Moderate, Severe, Extreme.                                    |
| headlineN    | String          | Überschrift der Warnung, z.B. Amtliche WARNUNG vor STURMBÖEN                                           |
| descriptionN | String          | Klartext Beschreibung der Warnung.                                                                     |
| eventN       | String          | Art der Warnung, z.B. STURMBÖEN                                                                        |
| effectiveN   | DateTime        | Zeitpunkt, an dem die Warnung ausgegeben wurde.                                                        |
| onsetN       | DateTime        | Zeitpunkt, von dem an die Warnung gilt.                                                                |
| effectiveN   | DateTime        | Zeitpunkt, bis zu dem die Warnung gilt.                                                                |
| altitudeN    | Number:Length   | Höhe über dem Meerespiegel, ab dem die Warnung gilt.                                                   |
| ceilingN     | Number:Length   | Höhe über dem Meerespiegel, bis zu dem die Warnung gilt.                                               |
| urgencyN     | String          | Zeitrahmen der Meldung, Mögliche Werte sind Future (Vorabinformation) und Immediate (Konkrete Warnung) |
| instructionN | String          | Zusatztext zur Warnung (Instruktionen und Sicherheitshinweise)                                         |

Sämtliche Channels sind ReadOnly!  

Der Channel _warningN_ dient hauptsächlich dazu, um z.B. in Sitemaps dynamisch Warnungen ein- oder auszublenden, bzw. um in Regeln zu prüfen, ob überhaupt eine Warnung vorliegt. 
Er ist nicht geeignet um auf das Erscheinen einer Warnung zu prüfen. 
Denn wenn eine Warnung durch eine neue Warnung ersetzt wird, bleibt der Zustand ON, es gibt keinen Zustandswechsel. 
Um auf das erscheinen einer Warnung zu prüfen, sollte der Trigger-Channel _updatedN_ genutzt werden. 
Der feuert immer dann, wenn eine Warnung das erste mal gesendet wird. 
Das heißt, der feuert auch dann, wenn eine Warnung durch eine neue Warnung ersetzt wird. 

Weitere Erläuterungen der Bedeutungen finden sich in der Dokumentation des DWDs unter [CAP DWD Profile 1.2](https://www.dwd.de/DE/leistungen/opendata/help/warnungen/cap_dwd_profile_de_pdf.pdf?__blob=publicationFile&v=7).
Bitte bedenke, dass dieses Binding nur *Gemeinden* unterstützt.

## Vollständiges Beispiel

demo.things:

```
dwdunwetter:dwdwarnings:koeln "Warnungen Köln" [ cellId="805315000", refresh=15, warningCount=1 ]
```

demo.items:

```
Switch WarnungKoeln "Warnung vorhanden" { channel="dwdunwetter:dwdwarnings:koeln:warning1" }
String WarnungKoelnServerity "Warnstufe [%s]" { channel="dwdunwetter:dwdwarnings:koeln:severity1" }
String WarnungKoelnBeschreibung "[%s]" { channel="dwdunwetter:dwdwarnings:koeln:description1" }
String WarnungKoelnAusgabedatum "Ausgeben am [%s]" { channel="dwdunwetter:dwdwarnings:koeln:effective1" }
String WarnungKoelnGueltigAb "Warnung gültig ab [%s]" { channel="dwdunwetter:dwdwarnings:koeln:onset1" }
String WarnungKoelnGueltigBis "Warnung gültig bis [%s]" { channel="dwdunwetter:dwdwarnings:koeln:expires1" }
String WarnungKoelnTyp "Warnungstyp [%s]" { channel="dwdunwetter:dwdwarnings:koeln:event1" }
String WarnungKoelnTitel "[%s]" { channel="dwdunwetter:dwdwarnings:koeln:headline1" }
String WarnungKoelnHoeheAb "Höhe ab [%d m]" { channel="dwdunwetter:dwdwarnings:koeln:altitude1" }
String WarnungKoelnHoeheBis "Höhe bis [%d m]" { channel="dwdunwetter:dwdwarnings:koeln:ceiling1" }
String WarningCologneUrgency "[%s]" { channel="dwdunwetter:dwdwarnings:cologne:urgency1" }
String WarningCologneInstruction "Zusatzinformationen: [%s]" { channel="dwdunwetter:dwdwarnings:cologne:instruction1" }
```

demo.sitemap:

```
sitemap demo label="Main Menu" {
    Frame {
        Text item=WarnungKoelnTitel visibility=[WarnungKoeln==ON]
        Text item=WarnungKoelnBeschreibung visibility=[WarnungKoeln==ON]
    }
}
```

demo.rules

```
rule "Neue Warnung"
when
     Channel 'dwdunwetter:dwdwarnings:koeln:updated1' triggered NEW
then
   // Neue Warnung, Informiere alle Bewohner
end 

```
dwdunwetter_de.map

```
ON=aktiv
OFF=inaktiv
NULL=undefiniert
UNDEF=undefiniert
```

dwdunwetter_severity_de.map

```
Minor=Wetterwarnung
Moderate=Markante Wetterwarnung
Severe=Unwetterwarnung
Extreme=Extreme Unwetterwarnung
NULL=undefiniert
UNDEF=undefiniert
```

dwdunwetter_urgency_de.map

```
Immediate=Warnung
Future=Vorabinformation
NULL=undefiniert
UNDEF=undefiniert
```

Wenn du unsicher bist, ob das Binding korrekt funktioniert, kannst du die Wetterdaten direkt mit deinem Browser abrufen, indem du https://maps.dwd.de/geoserver/dwd/ows?service=WFS&version=2.0.0&request=GetFeature&typeName=dwd:Warnungen_Gemeinden&CQL_FILTER=WARNCELLID%20LIKE%20%27CELL_ID%27 (ersetze `CELL_ID` mit deiner Cell ID) besuchst, den Datei Download zulässt und die heruntergeladene `.xml` Datei öffnest.
