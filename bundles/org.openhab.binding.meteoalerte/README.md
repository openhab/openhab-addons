# Meteo Alerte Binding

The Meteo Alerte binding gives alert level regarding major weather related risk factors.
This binding provides its own icon set and provides appropriate static and dynamic SVG icons (see items examples below).

## Supported Things

There is exactly one supported thing type, which represents the weather alerts for a given department.
It has the `department` id.
Of course, you can add multiple things, e.g. for getting alerts for different locations.

## Binding Configuration

The binding has no configuration options, all configuration is done at Thing level.

## Thing Configuration

The thing has a few configuration parameters:

| Parameter     | Description                                                                            |
|---------------|----------------------------------------------------------------------------------------|
| `department` | Name of the department.                                                               |
| refresh       | Refresh interval in minutes. Optional, the default value is 1440 minutes (once a day). |

## Channels

The Météo Alerte information that are retrieved is available as these channels:

| Channel ID            | Item Type | Description                                   |
|-----------------------|-----------|-----------------------------------------------|
| observation-time      | DateTime  | Date and time of report validity start        |
| end-time              | DateTime  | Date and time of report validity end          |
| comment               | String    | General comments on alerts for the department |
| vent                  | Number    | Wind alert level (*)                          |
| pluie-inondation      | Number    | Rain alert level (*)                          |
| orage                 | Number    | Storm alert level (*)                         |
| inondation            | Number    | Flood alert level (*)                         |
| neige                 | Number    | Snow alert level (*)                          |
| canicule              | Number    | Heat alert level (*)                          |
| grand-froid           | Number    | Cold alert level (*)                          |
| avalanches            | Number    | Avalanche alert level (*)                     |
| vague-submersion      | Number    | Wave submersion alert level (*)               |
| pluie-inondation-icon | Image     | Pictogram of the Rain alert level             |
| vent-icon             | Image     | Pictogram of the Wind alert level             |
| orage-icon            | Image     | Pictogram of Storm alert level                |
| inondation-icon       | Image     | Pictogram of Flood alert level                |
| neige-icon            | Image     | Pictogram of Snow alert level                 |
| canicule-icon         | Image     | Pictogram of Heat alert level                 |
| grand-froid-icon      | Image     | Pictogram of Cold alert level                 |
| avalanches-icon       | Image     | Pictogram of Avalanche alert level            |
| vague-submersion-icon | Image     | Pictogram of Wave Submersion alert level      |

(*) Each alert level is described by a color :

| Code | Color  | Description                               |
|------|--------|-------------------------------------------|
| 0    | Green  | No particular vigilance                   |
| 1    | Yellow | Be attentive to the weather situation     |
| 2    | Orange | Be "very vigilant" in the concerned areas |
| 3    | Red    | Absolute vigilance required               |

## Full Example

meteoalert.things:

```java
Thing meteoalerte:department:yvelines @ "MyCity" [department="YVELINES", refresh=12]
```

meteoalert.items:

```java
Group gMeteoAlert "Alertes Météo" <meteo_france> 
String     MA_Dept78                  "Département 78 [%s]"       <text>               (gMeteoAlert)   {channel="meteoalerte:department:yvelines:comment"}
Number     MA_etat_canicule           "Canicule [%s]"             <canicule>           (gMeteoAlert)   {channel="meteoalerte:department:yvelines:canicule"}
Number     MA_etat_grand_froid        "Grand Froid [%s]"          <grand-froid>        (gMeteoAlert)   {channel="meteoalerte:department:yvelines:grand-froid"}
Number     MA_etat_pluie_inondation   "Pluie-Inondation [%s]"     <pluie-inondation>   (gMeteoAlert)   {channel="meteoalerte:department:yvelines:pluie-inondation"}
Number     MA_etat_neige              "Neige [%s]"                <neige>              (gMeteoAlert)   {channel="meteoalerte:department:yvelines:neige"}
Number     MA_etat_vent               "Vent [%s]"                 <vent>               (gMeteoAlert)   {channel="meteoalerte:department:yvelines:vent"}
Number     MA_etat_inondation         "Inondation [%s]"           <inondation>         (gMeteoAlert)   {channel="meteoalerte:department:yvelines:inondation"}
Number     MA_etat_orage              "Orage [%s]"                <orage>              (gMeteoAlert)   {channel="meteoalerte:department:yvelines:orage"}
Number     MA_etat_avalanche          "Avalanches [%s]"           <avalanches>         (gMeteoAlert)   {channel="meteoalerte:department:yvelines:avalanches"}
    
Image      MA_icon_canicule           "Canicule"                  <canicule>           (gMeteoAlert)   {channel="meteoalerte:department:yvelines:canicule-icon"}
Image      MA_icon_grand_froid        "Grand Froid"               <grand-froid>        (gMeteoAlert)   {channel="meteoalerte:department:yvelines:grand-froid-icon"}
Image      MA_icon_pluie_inondation   "Pluie-Inondation"          <pluie-inondation>   (gMeteoAlert)   {channel="meteoalerte:department:yvelines:pluie-inondation-icon"}
Image      MA_icon_neige              "Neige"                     <neige>              (gMeteoAlert)   {channel="meteoalerte:department:yvelines:neige-icon"}
Image      MA_icon_vent               "Vent"                      <vent>               (gMeteoAlert)   {channel="meteoalerte:department:yvelines:vent-icon"}
Image      MA_icon_inondation         "Inondation"                <inondation>         (gMeteoAlert)   {channel="meteoalerte:department:yvelines:inondation-icon"}
Image      MA_icon_orage              "Orage"                     <orage>              (gMeteoAlert)   {channel="meteoalerte:department:yvelines:orage-icon"}
Image      MA_icon_avalanche          "Avalanche"                 <avalanches>         (gMeteoAlert)   {channel="meteoalerte:department:yvelines:avalanches-icon"}
    
DateTime   MA_ObservationTS           "Timestamp [%1$tH:%1$tM]"   <time>               (gMeteoAlert)   {channel="meteoalerte:department:yvelines:observation-time"}

```
