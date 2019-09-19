# DwdUnwetter Binding

Binding to retrieve the Weather Warnings of the "Deutscher Wetterdienstes" from the [DWD Geoserver](https://maps.dwd.de/geoserver/web/).
The DWD provides weather warnings for the German area. 
Regions outside of Germany are not supported.

## Supported Things

This binding supports exactly one thing - Weather Warning.
One Thing provides one or multiple warnings for a city.



## Thing Configuration

| Property     | Default | Required | Description                                                                                                                                                                                                                                                                                                 |
|--------------|---------|----------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| cellId       | -       | Yes      | Id of the area to retrieve weather warnings. For a list of valid IDs look at https://www.dwd.de/DE/leistungen/opendata/help/warnungen/cap_warncellids_csv.csv. With the % sign at the end it is possible to query multiple cells at once. For example with 8111% are cells retrieved that starts with 8111. |
| refresh      | 30      | No       | Time between to API requests in minutes. Minimum 15 minutes.                                                                                                                                                                                                                                                |
| warningCount | 1       | No       | Number of warnings to provide. For each warning there will multiple channels.                                                                                                                                                                                                                               |

The plugin will deliver no warnings if the number of retrieved warnings for one thing is to big. 
This can only happen if you select to many Cells with the %-Operator. 
It will happen if the area contains about 300+ warnings.

Example:

```
dwdunwetter:dwdwarnings:cologne "Warnings Cologne" [ cellId="105315000", refresh=15, warningCount=1 ]
```


## Channels

The are multiple channels for every weather warning.
The channels are numbered, so channels ending in 1 are for the first warning, ending with 2 are for the second warning and so on.
The warnings will be sorted by severity and for warnings with same severity by the valid from date.
This ensures that in the channels for the first warning will always be the highest severity.
If the API returns more warnings than the configured number in the thing, the warnings with the lowest severity will be dropped.  
 
| Channel      | Type            | Description                                                                      |
|--------------|-----------------|----------------------------------------------------------------------------------|
| warningN     | Switch          | ON if a warning is present, OFF else                                             |
| UpdatedN     | Trigger Channel | Triggers NEW if a warning is send the first time                                 |
| severityN    | String          | Severity of the warning. Possible values are Minor, Moderate, Severe and Extreme |
| headlineN    | String          | Headline of the warning like "Amtliche Warnung vor FROST"                        |
| descriptionN | String          | Textual description of the warning                                               |
| eventN       | String          | Type of the warning, e.g. FROST                                                  |
| effectiveN   | DateTime        | Issued Date and Time                                                             |
| onsetN       | DateTime        | Start Date and Time for which the warning is valid                               |
| expiresN     | DateTime        | End Date and Time for which the warning is valid                                 |
| altitudeN    | Number:Length   | Lower Height above sea level for which the warning is valid                      |
| ceilingN     | Number:Length   | Upper Height above sea level for which the warning is valid                      |
| urgencyN     | String          | Urgency of the warning Possible values are Future and Immediate                  |
| instructionN | String          | Additional instructions and safety informations                                  |

All channels are readonly!

The main purpose of the channel _warning_ is to be used for controlling visibility in sitemaps.
The channel can also be used in rules to check if there is a warning present. 
It should not be used for rules that need to fire if a new warning shows up. 
If a warning is replaced by another warning, that channel stays at ON, there will be no state change. 
For rules the need to fire if a new warning show up there is the trigger channel _updatedN_. 
That trigger channel fires an event whenever a warning is send the first time to that thing.
That channel triggers if a warning is replaced by another.

More explanations about the specific values of the channels can be found in the documentation of the DWD at: [CAP DWD Profile 1.2](https://www.dwd.de/DE/leistungen/opendata/help/warnungen/cap_dwd_profile_de_pdf.pdf?__blob=publicationFile&v=7)   

## Full Example

demo.things:

```
dwdunwetter:dwdwarnings:cologne "Warnings Cologne" [ cellId="105315000", refresh=15, warningCount=1 ]
```

demo.items:

```
Switch WarningCologne "Weather warning" { channel="dwdunwetter:dwdwarnings:cologne:warning1" }
String WarningCologneServerity "Severity[%s]" { channel="dwdunwetter:dwdwarnings:cologne:severity1" }
String WarningCologneBeschreibung "[%s]" { channel="dwdunwetter:dwdwarnings:cologne:description1" }
String WarningCologneAusgabedatum "Issued at [%s]" { channel="dwdunwetter:dwdwarnings:cologne:effective1" }
String WarningCologneGueltigAb "Valid from [%s]" { channel="dwdunwetter:dwdwarnings:cologne:onset1" }
String WarningCologneGueltigBis "Valid to [%s]" { channel="dwdunwetter:dwdwarnings:cologne:expires1" }
String WarningCologneTyp "Type [%s]" { channel="dwdunwetter:dwdwarnings:cologne:event1" }
String WarningCologneTitel "[%s]" { channel="dwdunwetter:dwdwarnings:cologne:headline1" }
String WarningCologneHoeheAb "Height from [%d m]" { channel="dwdunwetter:dwdwarnings:cologne:altitude1" }
String WarningCologneHoeheBis "Height to [%d m]" { channel="dwdunwetter:dwdwarnings:cologne:ceiling1" }
String WarningCologneUrgency "[%s]" { channel="dwdunwetter:dwdwarnings:cologne:urgency1" }
String WarningCologneInstruction "Additional information: [%s]" { channel="dwdunwetter:dwdwarnings:cologne:instruction1" }
```

demo.sitemap:

```
sitemap demo label="Main Menu"
{
    Frame {
        Text item=WarningCologneTitel visibility=[WarningCologne==ON]
        Text item=WarningCologneBeschreibung visibility=[WarningCologne==ON]
    }
}
```

demo.rules

```
rule "New Warnung"
when
     Channel 'dwdunwetter:dwdwarnings:cologne:updated1' triggered NEW
then
   // New Warning send a push notification to everyone
end 

```
