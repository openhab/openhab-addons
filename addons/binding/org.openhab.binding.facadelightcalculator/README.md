# Facade Light Calculator Binding

The Facade Light Calculator binding is used for calculating lightning status of facades depending upon sun position.
Sun position is can be provided by Astro Binding.

## Supported Things

This binding supports one Thing: Facade

## Discovery

This binding does not offer auto-discovery.

## Binding Configuration

No binding configuration required.

## Thing Configuration

Facade requires a bridge reference to the Sun and the orientation of the facade toward geographic north. By default, the facade is considered enlighted when the sun azimuth reaches orientation - 90° (negative offset) until orientation + 90° (positive offset). The two parameters (negative and positive offset) are set by default to 90. These can be modified to take in account natural obstacles (wall, trees) that reduces the lighting angle.

## Channels

* **thing** `facade`
        * **channel**: 
            * `facingsun` (Switch) : ON if orientation - negative offset <= sun azimut <= orientation + negative offset
            * `bearing` (Number:Dimensionless) : percentage of direct lighting. 100% when the sun is in front, 0% when not facing sun 
            * `side` (String) : indicates if the sun is in the left, in front or on the right of the facade
            
### Trigger Channels

* **thing** `face`
        * **facadeEvent**:: `SUN_ENTER, SUN_LEAVE

## Full Example

facade.things
```
facadelightcalculator:facade:cuisine "Facade Cuisine" @ "Outside" [orientation=115, poffset=90, margin=2, noffset=90]
facadelightcalculator:facade:sejour "Facade Sejour" @ "Outside" [orientation=205, poffset=90, margin=2, noffset=90]
facadelightcalculator:facade:jardin "Facade Jardin" @ "Outside" [orientation=295, poffset=90, margin=2, noffset=90]
```

facade.items
```
Group gFacade "Facades" (gBindings, gOutside)
    Group gFacadeCuisine "Facade Cuisine" (gFacade)
        Switch                  Facade_Cuisine_FacingSun    "Cuisine Ensolleillée"  (gFacadeCuisine)                {channel="facadelightcalculator:facade:cuisine:facingSun" }
        Number:Dimensionless    Facade_Cuisine_Bearing      "Cuisine Bearing"       (gFacadeCuisine,gSensorCounter) {channel="facadelightcalculator:facade:cuisine:bearing"}
        String                  Facade_Cuisine_Side         "Côté du soleil"        (gFacadeCuisine)                {channel="facadelightcalculator:facade:cuisine:side"}
    Group gFacadeSejour  "Facade Séjour" (gFacade)
        Switch                  Facade_Sejour_FacingSun     "Séjour Ensolleillée"   (gFacadeSejour)                 {channel="facadelightcalculator:facade:sejour:facingSun" }
        Number:Dimensionless    Facade_Sejour_Bearing       "Séjour Bearing"        (gFacadeSejour,gSensorCounter)  {channel="facadelightcalculator:facade:sejour:bearing"}
        String                  Facade_Sejour_Side          "Côté du soleil"        (gFacadeSejour)                 {channel="facadelightcalculator:facade:sejour:side"}
    Group gFacadeJardin  "Facade Jardin" (gFacade)
        Switch                  Facade_Jardin_FacingSun     "Jardin Ensolleillée"   (gFacadeJardin)                 {channel="facadelightcalculator:facade:jardin:facingSun" }
        Number:Dimensionless    Facade_Jardin_Bearing       "Jardin Bearing"        (gFacadeJardin,gSensorCounter)  {channel="facadelightcalculator:facade:jardin:bearing"}
        String                  Facade_Jardin_Side          "Côté du soleil"        (gFacadeJardin)                 {channel="facadelightcalculator:facade:jardin:side"}
``` 

Here, we will use follow profile in order to furnish sun azimuth to the facades :

astro.items
```
// Sun
Number:Angle        Astro_Sun_Azimuth       "Azimuth [%d %unit%]"                               (gAstro, gRecordLastUpdate, gSensorCounter) 
                                                                                                                            {channel="astro:sun:home:position#azimuth",
                                                                                                                             channel="facadelightcalculator:facade:cuisine:sunAzimuth" [profile="follow"],
                                                                                                                             channel="facadelightcalculator:facade:jardin:sunAzimuth" [profile="follow"],
                                                                                                                             channel="facadelightcalculator:facade:sejour:sunAzimuth" [profile="follow"]
                                                                                                                            }
```
