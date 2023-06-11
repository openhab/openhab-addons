# DWD Unwetter Binding

Binding to retrieve the Weather Warnings of the "Deutscher Wetterdienstes" from the [DWD Geoserver](https://maps.dwd.de/geoserver/web/).
The DWD provides weather warnings for Germany.
Regions outside of Germany are not supported.

## Supported Things

This binding supports one thing, the Weather Warning.
Each Thing provides one or more warnings for a city.

## Thing Configuration

| Property     | Default | Required | Description                                                                                                                                |
|--------------|---------|----------|--------------------------------------------------------------------------------------------------------------------------------------------|
| cellId       | -       | Yes      | ID of the area to retrieve weather warnings, only IDs starting with an 8 (exception for Berlin: 7) are supported. See [Cell ID](#cell-id). |
| refresh      | 30      | No       | Time between API requests in minutes. Minimum 15 minutes.                                                                                  |
| warningCount | 1       | No       | Number of warnings to provide.                                                                                                             |

### Cell ID

<!-- See page 10-13 (in German) of https://www.dwd.de/DE/wetter/warnungen_aktuell/objekt_einbindung/einbindung_karten_geodienste.pdf?__blob=publicationFile&v=14 for Cell ID documentation. -->
Use [this list](https://www.dwd.de/DE/leistungen/opendata/help/warnungen/cap_warncellids_csv.csv) of valid IDs, please notice that **only IDs starting with an eight (8) and nine digits are supported** by this binding.
Exeception for Berlin, where the ID of the city's districts are used. Those start with a seven (7).

Using the percent sign (%) as a wildcard, it is possible to query multiple cells.
For example, the value `8111%` retrieves all cell IDs that start with `8111`.

More explanation (in German) for CellID can be found on page 10-13 of [PDF: DWD-Geoserver: Nutzung von WMS-Diensten für eigene Websites](https://www.dwd.de/DE/wetter/warnungen_aktuell/objekt_einbindung/einbindung_karten_geodienste.pdf?__blob=publicationFile&v=14).

The binding will deliver no warnings if the number of retrieved warnings for one Thing is too big.
This can only happen if you select too many cell IDs (more than about 300) with the percent wildcard.

Example:

```java
dwdunwetter:dwdwarnings:cologne "Warnings Cologne" [ cellId="805315000", refresh=15, warningCount=1 ]
```

## Channels

The are multiple channels for every weather warning.
The channels are numbered, with channel names ending in 1 for the first warning, 2 for the second warning, and so on.
The warnings will be sorted first by `Severity` and then by the `Valid From` date.
This ensures that the channels for the first warning will always be the highest Severity.
If the API returns more warnings than the configured number in the Thing, the warnings with the lowest Severity will be dropped.

| Channel      | Type            | Description                                                                       |
|--------------|-----------------|-----------------------------------------------------------------------------------|
| warningN     | Switch          | ON if a warning is present                                                        |
| UpdatedN     | Trigger Channel | Triggers NEW when a warning is sent the first time                                |
| severityN    | String          | Severity of the warning. Possible values are Minor, Moderate, Severe, and Extreme |
| headlineN    | String          | Headline of the warning (e.g. "Amtliche Warnung vor FROST")                       |
| descriptionN | String          | Textual description of the warning                                                |
| eventN       | String          | Type of the warning (e.g. FROST)                                                  |
| effectiveN   | DateTime        | Issued Date and Time                                                              |
| onsetN       | DateTime        | Start Date and Time for which the warning is valid                                |
| expiresN     | DateTime        | End Date and Time for which the warning is valid                                  |
| altitudeN    | Number:Length   | Lower Height above sea level for which the warning is valid                       |
| ceilingN     | Number:Length   | Upper Height above sea level for which the warning is valid                       |
| urgencyN     | String          | Urgency of the warning. Possible values are Future and Immediate                  |
| instructionN | String          | Additional instructions and safety information                                    |

All channels are readonly.

The main purpose of the channel `warningN` is to be used for controlling visibility in sitemaps.
The channel can also be used in rules to check if there is a warning present.
It should not be used for rules that need to fire if a new warning shows up.
If a warning is replaced by another warning, that channel stays at ON, and there will be no state change.
For rules that need to fire if a new warning occurs, there is the trigger channel `updatedN`.
That trigger channel fires an event whenever a warning is sent for the first time.
It also triggers if a warning is replaced by another.

More explanations about the specific values of the channels can be found in the PDF documentation of the DWD at: [CAP DWD Profile 1.2](https://www.dwd.de/DE/leistungen/opendata/help/warnungen/cap_dwd_profile_en_pdf_1_12.html).
Please note that this binding only supports _Gemeinden_ (COMMUNE) for _WarncellID_.

## Full Example

dwdunwetter.things:

```java
dwdunwetter:dwdwarnings:cologne "Warnings Cologne" [ cellId="805315000", refresh=15, warningCount=1 ]
```

e.g.

to get two warnings like in the item example, set `warningCount=2` in things file

```java
dwdunwetter:dwdwarnings:cologne "Warnings Cologne" [ cellId="805315000", refresh=15, warningCount=2 ]
```

dwdunwetter.items:

```java
Switch        WarningCologne_1             "Weather warning [MAP(dwdunwetter_de.map):%s]"   { channel="dwdunwetter:dwdwarnings:cologne:warning1" }
String        WarningCologneServerity_1    "Severity [MAP(dwdunwetter_severity_de.map):%s]" { channel="dwdunwetter:dwdwarnings:cologne:severity1" }
String        WarningCologneBeschreibung_1 "[%s]"                                           { channel="dwdunwetter:dwdwarnings:cologne:description1" }
DateTime      WarningCologneAusgabedatum_1 "Issued at [%s]"                                 { channel="dwdunwetter:dwdwarnings:cologne:effective1" }
DateTime      WarningCologneGueltigAb_1    "Valid from [%s]"                                { channel="dwdunwetter:dwdwarnings:cologne:onset1" }
DateTime      WarningCologneGueltigBis_1   "Valid to [%s]"                                  { channel="dwdunwetter:dwdwarnings:cologne:expires1" }
String        WarningCologneTyp_1          "Event [%s]"                                     { channel="dwdunwetter:dwdwarnings:cologne:event1" }
String        WarningCologneTitel_1        "[%s]"                                           { channel="dwdunwetter:dwdwarnings:cologne:headline1" }
Number:Length WarningCologneHoeheAb_1      "Height from [%d m]"                             { channel="dwdunwetter:dwdwarnings:cologne:altitude1" }
Number:Length WarningCologneHoeheBis_1     "Height to [%d m]"                               { channel="dwdunwetter:dwdwarnings:cologne:ceiling1" }
String        WarningCologneUrgency_1      "[MAP(dwdunwetter_urgency_de.map):%s]"           { channel="dwdunwetter:dwdwarnings:cologne:urgency1" }
String        WarningCologneInstruction_1  "Additional information: [%s]"                   { channel="dwdunwetter:dwdwarnings:cologne:instruction1" }

Switch        WarningCologne_2             "Weather warning [MAP(dwdunwetter_de.map):%s]"   { channel="dwdunwetter:dwdwarnings:cologne:warning2" }
String        WarningCologneServerity_2    "Severity [MAP(dwdunwetter_severity_de.map):%s]" { channel="dwdunwetter:dwdwarnings:cologne:severity2" }
String        WarningCologneBeschreibung_2 "[%s]"                                           { channel="dwdunwetter:dwdwarnings:cologne:description2" }
DateTime      WarningCologneAusgabedatum_2 "Issued at [%s]"                                 { channel="dwdunwetter:dwdwarnings:cologne:effective2" }
...
```

demo.sitemap:

```perl
sitemap demo label="Main Menu" {
    Frame {
        Text item=WarningCologneTitel visibility=[WarningCologne==ON]
        Text item=WarningCologneBeschreibung visibility=[WarningCologne==ON]
    }
}
```

dwdunwetter.rules

```java
rule "New Warnung"
when
     Channel 'dwdunwetter:dwdwarnings:cologne:updated1' triggered NEW
then
   // New Warning send a push notification to everyone
end 

```

dwdunwetter_de.map

```text
ON=aktiv
OFF=inaktiv
NULL=undefiniert
UNDEF=undefiniert
```

dwdunwetter_severity_de.map

```text
Minor=Wetterwarnung
Moderate=Markante Wetterwarnung
Severe=Unwetterwarnung
Extreme=Extreme Unwetterwarnung
NULL=undefiniert
UNDEF=undefiniert
```

dwdunwetter_urgency_de.map

```text
Immediate=Warnung
Future=Vorabinformation
NULL=undefiniert
UNDEF=undefiniert
```

If you're unsure if the binding is working correctly, you can access the data directly by visiting `https://maps.dwd.de/geoserver/dwd/ows?service=WFS&version=2.0.0&request=GetFeature&typeName=dwd:Warnungen_Gemeinden&CQL_FILTER=WARNCELLID%20LIKE%20%27CELL_ID%27` (replace `CELL_ID` with your Cell ID), allowing the download and opening the downloaded `.xml` file.
