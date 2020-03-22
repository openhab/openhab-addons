# Meteo Alerte Binding


## Supported Things

There is exactly one supported thing type, which represents the weather alerts for a given department.
It has the `department` id.
Of course, you can add multiple Things, e.g. for getting alerts for different locations.

## Discovery

This binding does not handle auto-discovery.

## Binding Configuration

The binding has no configuration options, all configuration is done at Thing level.

## Thing Configuration

The thing has a few configuration parameters:

| Parameter | Description                                                          |
|-----------|----------------------------------------------------------------------|
| department | Name of the department.                                             |
| refresh    | Refresh interval in hours. Optional, the default value is 24 hours. |

## Channels

The Météo Alerte information that are retrieved is available as these channels:

| Channel ID      | Item Type     | Description                                   |
|-----------------|---------------|-----------------------------------------------|
| observationTime | DateTime      | Observation date and time                     |
| comment         | String        | General comments on alerts for the department |

## Full Example

meteoalert.things:

```
Thing meteoalerte:department:yvelines @ "MyCity" [department="YVELINES", refresh=12]
```

meteoalert.items:

```
Group gMeteoAlert "Alertes Météo" <weather> 
    String  MA_Dept78                    "Département 78 [%s]"   <aqi>       (gMeteoAlert)   {channel="meteoalerte:department:yvelines:comment"}
    String  MA_etat_canicule             "Canicule [%s]"         <aqi>       (gMeteoAlert)   {channel="meteoalerte:department:yvelines:canicule"}
    String  MA_etat_grand_froid          "Grand Froid [%s]"      <aqi>       (gMeteoAlert)   {channel="meteoalerte:department:yvelines:grand-froid"}
    String  MA_etat_pluie_inondation     "Pluie-Inondation [%s]" <aqi>       (gMeteoAlert)   {channel="meteoalerte:department:yvelines:pluie-inondation"}
    String  MA_etat_neige                "Neige [%s]"            <aqi>       (gMeteoAlert)   {channel="meteoalerte:department:yvelines:neige"}
    String  MA_etat_vent                 "Vent [%s]"             <aqi>       (gMeteoAlert)   {channel="meteoalerte:department:yvelines:vent"}
    String  MA_etat_inondation           "Inondation [%s]"       <aqi>       (gMeteoAlert)   {channel="meteoalerte:department:yvelines:inondation"}
    String  MA_etat_orage                "Orage [%s]"            <aqi>       (gMeteoAlert)   {channel="meteoalerte:department:yvelines:orage"}
    
    Image       MA_icon_canicule         "Canicule"              <aqi>       (gMeteoAlert)   {channel="meteoalerte:department:yvelines:canicule-icon"}
    Image       MA_icon_grand_froid      "Grand Froid"           <aqi>       (gMeteoAlert)   {channel="meteoalerte:department:yvelines:grand-froid-icon"}
    Image       MA_icon_pluie_inondation "Pluie-Inondation"      <aqi>       (gMeteoAlert)   {channel="meteoalerte:department:yvelines:pluie-inondation-icon"}
    Image       MA_icon_neige            "Neige"                 <aqi>       (gMeteoAlert)   {channel="meteoalerte:department:yvelines:neige-icon"}
    Image       MA_icon_vent             "Vent"                  <aqi>       (gMeteoAlert)   {channel="meteoalerte:department:yvelines:vent-icon"}
    Image       MA_icon_inondation       "Inondation"            <aqi>       (gMeteoAlert)   {channel="meteoalerte:department:yvelines:inondation-icon"}
    Image       MA_icon_orage            "Orage"                 <aqi>       (gMeteoAlert)   {channel="meteoalerte:department:yvelines:orage-icon"}
    
    DateTime    MA_ObservationTS         "Timestamp [%1$tH:%1$tM]"   <time>  (gMeteoAlert)   {channel="meteoalerte:department:yvelines:observation-time"}

```
