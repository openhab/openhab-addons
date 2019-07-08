# iAquaLink Binding
 
This binding supports:

* Any iAquaLink based pool system
* Reading auxiliary, temperature, pump, chemistry and system values  
* Controlling system, auxiliary, lighting, and temperature settings

## Binding Configuration

The binding requires the iAquaLink user name and password.  If you have more then one pool system registered to an account, you may optionally specify the pool serial ID/Number to use, otherwise the first pool controller will be used. 


## Manual Thing Configuration

```
Thing iaqualink:controller:pool [ userName="user@domain.com", password="somepassword"]
```

## Channels


The following is a list of supported channels.  Auxiliary and OneTouch channels will be dynamically added depending on what a system reports as being supported.

Auxiliary channels that are of a number type represent lighting modes (typically 0-15), Auxiliary channels that are dimmer types can set the light value in increments of 25 (0,25,50,750,100).  The Auxiliary channel type will be dynamically assigned based on the controller configuration. 

Heater status can be OFF (0), Enabled/ON (3), or Heating (1).  

| Channel Type ID     | Item Type                  |
|---------------------|----------------------------|
| status              | String                     |
| system_type         | Number                     |
| temp_scale          | String                     |
| spa_temp            | Number                     |
| pool_temp           | Number                     |
| air_temp            | Number                     |
| spa_set_point       | Number                     |
| pool_set_point      | Number                     |
| cover_pool          | Switch                     |
| freeze_protection   | Switch                     |
| spa_pump            | Switch                     |
| pool_pump           | Switch                     |
| spa_heater          | Switch                     |
| pool_heater         | Switch                     |
| solar_heater        | Switch                     |
| spa_heater_status   | Number                     |
| pool_heater_status  | Number                     |
| solar_heater_status | Number                     |
| spa_salinity        | Number                     |
| pool_salinity       | Number                     |
| orp                 | Number                     |
| ph                  | Number                     |
| onetouch_1          | Switch                     |
| onetouch_n+1        | Switch                     |
| aux_1               | Switch or Number or Dimmer |
| aux_n+1             | Switch or Number or Dimmer |

## Sample Items

```
Group Group_AquaLink
String AquaLinkStatus "Status [%s]" (Group_AquaLink) {channel="iaqualink:controller:pool:status"}
Switch AquaLinkBoosterPump "Booster Pump" (Group_AquaLink) {channel="iaqualink:controller:pool:aux_1"}
Switch AquaLinkPoolLight "Pool Light" (Group_AquaLink) {channel="iaqualink:controller:pool:aux_2"}
Switch AquaLinkSpaLight "Spa Light" (Group_AquaLink) {channel="iaqualink:controller:pool:aux_3"}
Switch AquaLinkVanishingEdge "Vanishing Edge" (Group_AquaLink) {channel="iaqualink:controller:pool:aux_4"}

Switch AquaLinkAllOffOneTouch "All Off" (Group_AquaLink) {channel="iaqualink:controller:pool:onetouch_1"}
Switch AquaLinkSpaOneTouch "Spa Mode" (Group_AquaLink) {channel="iaqualink:controller:pool:onetouch_2"}
Switch AquaLinkCleanOneTouch "Clean Mode" (Group_AquaLink) {channel="iaqualink:controller:pool:onetouch_3"}
Switch AquaLinkPoolOneTouch "Pool Mode" (Group_AquaLink) {channel="iaqualink:controller:pool:onetouch_4"}

Number AquaLinkSpaTemp "Spa Temperature [%d]" (Group_AquaLink) {channel="iaqualink:controller:pool:spa_temp"}
Number AquaLinkPoolTemp "Pool Temperature [%d]" (Group_AquaLink) {channel="iaqualink:controller:pool:pool_temp"}
Number AquaLinkAirTemp "Air Temperature [%d]" (Group_AquaLink) {channel="iaqualink:controller:pool:air_temp"}

Number AquaLinkSpaSetpoint "Spa Setpoint [%d]" (Group_AquaLink) {channel="iaqualink:controller:pool:spa_set_point"}
Number AquaLinkPoolSetpoint "Pool Setpoint [%d]" (Group_AquaLink) {channel="iaqualink:controller:pool:pool_set_point"}

Switch AquaLinkSpaPump "Spa Pump" (Group_AquaLink) {channel="iaqualink:controller:pool:spa_pump"}
Switch AquaLinkPoolPump"Pool Pump" (Group_AquaLink) {channel="iaqualink:controller:pool:pool_pump"}

Number AquaLinkSpaHeaterStatus "Spa Heater [%s]" (Group_AquaLink) {channel="iaqualink:controller:pool:spa_heater_status"}
Number AquaLinkPoolHeaterStatus  "Pool Heater [%s]" (Group_AquaLink) {channel="iaqualink:controller:pool:pool_heater_status"}

Switch AquaLinkSpaHeater "Spa Heater" (Group_AquaLink) {channel="iaqualink:controller:pool:spa_heater"}
Switch AquaLinkPoolHeater  "Pool Heater" (Group_AquaLink) {channel="iaqualink:controller:pool:pool_heater"}
```





