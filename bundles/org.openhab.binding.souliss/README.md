For updates go to official repository at [https://github.com/souliss/bindingopenhab2](https://github.com/souliss/bindingopenhab2)

# Souliss Binding

[Souliss](http://www.souliss.net/) is a networking framework for Arduino and compatibles boards, and is designed to let you easily build a smart home that is distributed over multiple boards via Ethernet, WiFi, wireless point-to-point and RS485 bus. 

Souliss is an open-source and community driven project, you can use the [wiki](https://github.com/souliss/souliss/wiki) and [Community](https://github.com/souliss/souliss/wiki/Community) to get help and share you results.  

## Prerequisites

The binding requires a deployed network.  As a minimum, you need one Souliss node with Ethernet or WiFi access configured as a [Gateway](https://github.com/souliss/souliss/wiki/Gateway). A Gateway is a special node that is able to communicate with the user interfaces. The binding interacts as a user interface for Souliss.

A starting point is the [Souliss wiki](https://github.com/souliss/souliss/wiki). The best is to start with a single node and connect with SoulissApp. The code for networking activities of this binding is based on [SoulissApp](https://github.com/souliss/souliss/wiki/SoulissApp) code, so once connected with SoulissApp, you can move to openHAB directly.

You can use SoulissApp and the Souliss binding at the same time, and generally up to five (by default, but can be increased) user interfaces simultaneously.

### Sketches

The easiest way is start with a simple example to control an ON/OFF light (though a relay). 
You can go to project [Souliss[(https://github.com/souliss/souliss), see a lot of examples sketches: [Souliss examples](https://github.com/souliss/souliss/tree/friariello/examples)

## Binding Configuration
This binding does not require any special configuration.

## Discovery
This binding can automatically discover devices. First Gateway Node, then Peer Nodes. 

## Supported Things
In Souliss Framework a Typical is one of predefined logic dedicated to smart home devices like lights, heating or antitheft. 

Typical can be one of T11, T12, T13, T14, etc... 

It are defined [here](https://github.com/souliss/souliss/wiki/Typicals).

Typicals match directly with openHAB Thing type.

|Device type |Typical Code | Thing type | 
|------------|---------|-------------------------------|
|ON/OFF Digital Output with Timer Option|T11|souliss:t11|
|ON/OFF Digital Output with AUTO mode|T12|souliss:t12|
|Digital Input Value|T13|souliss:t13|
|Pulse Digital Output|T14|souliss:t14|
|RGB LED Strip|T16|souliss:t16|
|ON/OFF Digital Output|T18|souliss:t18|
|Single Color LED Strip|T19|souliss:t19|
|Digital Input Pass Through|T1A|souliss:t1A|
|Motorized devices with limit switches|T21|souliss:t21|
|Motorized devices with limit switches and middle position|T22|souliss:t22|
|Temperature control|T31|souliss:t31|
|Anti-theft integration (Main)|T41|souliss:t41|
|Anti-theft integration (Peer)|T42|souliss:t42|
|Analog input, half-precision floating point|T51|souliss:t51|
|Temperature measure (-20, +50) °C|T52|souliss:t52|
|Humidity measure (0, 100) %|T53|souliss:t53|
|Light Sensor (0, 40) kLux|T54|souliss:t54|
|Voltage (0, 400) V|T55|souliss:t55|
|Current (0, 25)  A|T56|souliss:t56|
|Power (0, 6500)  W|T57|souliss:t57|
|Pressure measure (0, 1500) hPa|T58|souliss:t58|
|Analog Setpoint|T61|souliss:t61|
|Analog Setpoint-Temperature measure (-20, +50) °C|T62|souliss:t62|
|Analog Setpoint-Humidity measure (0, 100) %|T63|souliss:t63|
|Analog Setpoint-Light Sensor (0, 40) kLux|T64|souliss:t64|
|Analog Setpoint-Voltage (0, 400) V|T65|souliss:t65|
|Analog Setpoint-Current (0, 25)  A|T66|souliss:t66|
|Analog Setpoint-Power (0, 6500)  W|T67|souliss:t67|
|Analog Setpoint-Pressure measure (0, 1500) hPa|T68|souliss:t68|
|Broadcast messages|Action Message|souliss:topic|

### Channels
The following matrix lists the capabilities (channels) for each type:

|Thing type / Channel |Switch / onoff | Switch / sleep | DateTime / lastStatusStored | Number / healty |Switch / automode|Contact / stateOnOff|Contact / stateOpenClose|Switch / pulse|Switch / whitemode|Rollershutter / roller_brightness|Dimmer / dimmer_brightness|Color / ledcolor|Switch / one|Switch / two|Switch / three|Switch / four|Switch / five|Switch / six|Switch / seven|Switch / eight|
|-- |-- | -- | -- | -- |--|--|--|--|--|--|--|--|--|--|--|--|--|--|--|--|
|souliss:t11|x|x|x|x||||
|souliss:t12|x| |x|x|x||||
|souliss:t13|||x|x||x|x||
|souliss:t14|||x|x||||x|
|souliss:t16|x|x|x|x|||||x|x|x|x||
|souliss:t18|x||x|x|||||||||
|souliss:t19|x|x|x|x||||||x|x|
|souliss:t1A|||x|x|||||||||x|x|x|x|x|x|x|x|

|Thing type / Channel | DateTime / lastStatusStored | Number / healty |Rollershutter / rollershutter|(see down) / rollershutter_state|(see down) / mode|(see down) / fan|Switch / status|Number / setpoint|Switch / setAsMeasured|Switch / measured|Switch / statusAlarm|Switch / onOffAlarm|Switch / rearmAlarm|
|-- |-- | --|- | -- | -- |--|--|--|--|--|--|--|--|
|souliss:t21|x|x||x|
|souliss:t22|x|x|x|x|
|souliss:t31|x|x|||x|x|x|x|x|x|
|souliss:t41|x|x|||||||||x|x|x|
|souliss:t42|x|x|||||||||x||x|

rollershutter_state = opening, closing, limSwitch_open , limSwitch_close, state_open, state_close, NoLimSwitch

mode = COOLING_MODE, HEATING_MODE, POWEREDOFF_MODE

fan = AUTO, HIGH, MEDIUM, LOW, FANOFF


|Thing type / Channel| DateTime / lastStatusStored |Number / healty|Number / value|
|-- |-- | -- | --|
|souliss:t51|x|x|x|
|souliss:t52|x|x|x|
|souliss:t53|x|x|x|
|souliss:t54|x|x|x|
|souliss:t55|x|x|x|
|souliss:t56|x|x|x|
|souliss:t57|x|x|x|
|souliss:t58|x|x|x|

|Thing type / Channel|  DateTime / lastStatusStored |Number / healty|Number / value|
|-- |-- | -- | --|
|souliss:t61|x|x|x|
|souliss:t62|x|x|x|
|souliss:t63|x|x|x|
|souliss:t64|x|x|x|
|souliss:t65|x|x|x|
|souliss:t66|x|x|x|
|souliss:t67|x|x|x|
|souliss:t68|x|x|x|
|souliss:topic|x||x|

### Parameters
|Thing type| Parameters Name and Default Value| Description|
|-- |-- |-- |
|Gateway|GATEWAY_IP_ADDRESS="" |Will be resolved by discovery if auto configured|
|"|GATEWAY_PORT_NUMBER=230 ||
|"|PREFERRED_LOCAL_PORT_NUMBER=0 |Default port is 0. It means that it is random|
|"|PING_INTERVAL=30 |Interval in seconds to check for device presence|
|"|SUBSCRIBTION_INTERVAL=2|Interval in minutes to subcribe Souliss Gateway|
|"|HEALTHY_INTERVAL=35|Interval in seconds to send nodes healthy|
|"|USER_INDEX=70|Generally the default value work good. It must is different from other user interfaces (ex: SoulissApp)|
|"|NODE_INDEX=120|Generally the default value work good. It must is different from other user interfaces (ex: SoulissApp)|
|T11|sleep=5|Set sleep timer in cycles|
|T12|||
|T13|||
|T14|||
|T16|sleep=5|Set sleep timer in cycles|
|T19|sleep=5|Set sleep timer in cycles|
|T1A|||
|T21|||
|T22|||
|T31|||
|T4x|||
|T5x|||
|T6x|||


## Manual Things Configuration

If after discovery your thing is not listed you can add it manually.
You have to choice it from disponible items. Firts gateway, after items!
To configure Gateway you can leave default value on Thing ID and write your value on "IP or Host Name" and "Gateway port".

To configure a typical (items) you have to choice your "Name" and "Location", you have to choice your "Gateway" and insert correct "Thing ID".

Thing ID is [node]-[slot]
For example, if you have two nodes and you want configure a typical on second node at slot seven, you must write 
Thing ID: 
```
2-7
```


## Basic UI and Classic UI
Examples to configure items in Basic UI and Classic UI
Thing <binding_id>:<type_id>:<thing_id> "Label" @ "Location" [ <parameters> ]
    
The general syntax for .things files is defined as follows (parts in <..> are required):
```
Bridge <binding_id>:<type_id>:<bridge_id> "<Souliss Gateway Name>" [ <parameters> ]
{  
Thing <type_id> <thing_id>  [ <parameters> ]
}

```


souliss.things:
```
Bridge souliss:gateway:105 "Souliss Gateway - 105" [GATEWAY_IP_ADDRESS="192.168.1.105", GATEWAY_PORT_NUMBER=230, PREFERRED_LOCAL_PORT_NUMBER=0, PING_INTERVAL=30, SUBSCRIBTION_INTERVAL=2, HEALTHY_INTERVAL=38, USER_INDEX=72, NODE_INDEX=38]
{  
Thing t14 1-6 "Portoncino"@"Rientro"
Thing t14 1-7 "Cancello"@"Rientro"
Thing t57 1-4 "Consumo"@"Soggiorno"
Thing t57 4-0 "Fotovoltaico"@"Soggiorno"
Thing t57 4-6 "Pannelli Gruppo 1"@"Soggiorno"
Thing t57 4-8 "Pannelli Gruppo 2"@"Soggiorno"
Thing t52 4-10 "Temp.Pannelli Gruppo 1"@"Soggiorno"
Thing t52 4-12 "Temp.Pannelli Gruppo 2"@"Soggiorno"

Thing t52 3-0 "Temperatura Boiler Solare Termico"
Thing t52 3-2 "Temperatura Termocamino"
Thing t11 3-4 "Acqua Termocamino"
Thing t11 3-6 "Auto: Boiler / Termocamino"	
Thing t31 3-7  "Acqua Auto: Boiler / Termocamino"	

Thing t31 6-0 "Termostato Soggiorno"@"Soggiorno"
Thing t53 6-7 "Umidità"@"Soggiorno"
Thing t19 6-9 "Termostato Soggiorno - Luminosità"@"Soggiorno"

Thing t11 5-0 "Tettoia"@"Giardino"

Thing t11 12-0 "Divano"@"Soggiorno" [sleep=10] 

Thing t16 8-0 "LYT1"

Thing t11 10-0 "Albero di Natale"
Thing t11 11-0 "Birra"@"Soppalco"
Thing t52 11-1 "Birra - Temp 1"@"Soppalco"
Thing t52 11-3 "Birra - Temp 2"@"Soppalco"
}
```
You have to write your Gateway IP Number and leave all other to default values


default.items:

```
Group    Home                 "Tonino"        <house>

Group    FamilyRoom           "Soggiorno"     <parents_2_4>   (Home)
Group    Divano               "Divano"        (Home)
Group    Outside              "Esterno"   <garden>   (Home)
Group    TV                   "TV"   <television>   (Home)
Group    Elettricita
Group    Diagnostic
Group    TermostatoSoggiorno

Switch   tettoia  "Tettoia"  <light>    (Outside)   ["Lighting"]   {autoupdate="false", channel="souliss:t11:105:5-0:onoff"}
String	 tettoia_aggiornamento	"Agg [%1$td.%1$tm.%1$tY %1$tk:%1$tM:%1$tS]"	<keyring> (Outside, Diagnostic)  {channel="souliss:t31:105:5-0:lastStatusStored"}

Switch   portoncino         "Portoncino"          <light>         (FamilyRoom)         ["Lighting"]   {autoupdate="false",channel="souliss:t14:105:1-6:pulse"}
Switch   cancello         "Cancello"          <light>         (FamilyRoom)         ["Lighting"]   {autoupdate="false",channel="souliss:t14:105:1-7:pulse"}

Number   FamilyRoom_Temperature   "Temperatura [%.1f °C]"   <temperature>  (FamilyRoom)                  {channel="souliss:t31:105:6-0:measured"}
Number   FamilyRoom_Humidity      "Umidità [%.1f %%]"       <humidity>      (FamilyRoom)                     {channel="souliss:t53:105:6-7:value"}
String	AggiornamentoNodo6	"Agg [%1$td.%1$tm.%1$tY %1$tk:%1$tM:%1$tS]"	<keyring> (FamilyRoom, Diagnostic)  {channel="souliss:t31:105:6-0:lastStatusStored"}

Number   Consumo      "Consumo [%.1f W]"       <energy>      (FamilyRoom, Elettricita)                     {channel="souliss:t57:105:1-4:value"}
Number   Fotovoltaico      "Fotovoltaico [%.1f W]"       <energy>      (FamilyRoom, Elettricita)                     {channel="souliss:t57:105:4-0:value"}
String	AggiornamentoNodo1	"Agg.Consumi [%1$td.%1$tm.%1$tY %1$tk:%1$tM:%1$tS]"	<keyring> (FamilyRoom, Elettricita, Diagnostic)  {channel="souliss:t57:105:1-4:lastStatusStored"}
String	AggiornamentoNodo4	"Agg.Fotovoltaico [%1$td.%1$tm.%1$tY %1$tk:%1$tM:%1$tS]"	<keyring> (FamilyRoom, Elettricita, Diagnostic)  {channel="souliss:t57:105:4-0:lastStatusStored"}
                                  
Switch divano "Divano" <light> (FamilyRoom, Divano ) ["Switchable"] {autoupdate="false", channel="souliss:t11:105:12-0:onoff"}
String divano_aggiornamento	"Agg. [%1$td.%1$tm.%1$tY %1$tk:%1$tM:%1$tS]"	<keyring> (FamilyRoom, Divano, Diagnostic)  {channel="souliss:t57:105:12-0:lastStatusStored"}
String divano_healty	"Salute"	<keyring> (FamilyRoom, Divano, Diagnostic)  {channel="souliss:t57:105:12-0:healty"}

Number termostatosoggiorno_temperatura  "Temperatura [%.1f °C]" <temperature> (TermostatoSoggiorno) {channel="souliss:t31:105:6-0:measured"}
Number termostatosoggiorno_umidita "Umidità [%.1f %%]" <temperature>   (TermostatoSoggiorno)       {channel="souliss:t53:105:6-7:value" }

Number termostatosoggiorno_umidita "Umidità" <humidity>   (TermostatoSoggiorno)  {channel="souliss:t53:105:6-7:value" }

Number termostatosoggiorno_temperatura  "Temperatura" <temperature> (TermostatoSoggiorno) {channel="souliss:t31:105:6-0:measured"}
Number termostatosoggiorno_setpoint "Regola Set Point [%.1f °c]"    <heating> (TermostatoSoggiorno) {autoupdate="false", channel="souliss:t31:105:6-0:setpoint"}
Switch termostatosoggiorno_setasmeasured "Set temp. attuale" <heating> (TermostatoSoggiorno)  {channel="souliss:t31:105:6-0:setAsMeasured"}
String termostatosoggiorno_modo "Modo" (TermostatoSoggiorno) {autoupdate="false", channel="souliss:t31:105:6-0:mode"}
Switch termostatosoggiorno_power "Termostato" <powerIcon> (TermostatoSoggiorno) {channel="souliss:t31:105:6-0:system"}
Switch termostatosoggiorno_fire "Fire" <fire> (TermostatoSoggiorno) {channel="souliss:t31:105:6-0:fire"}

Dimmer  TermostatoSoggiorno_displayBright   "Lumin.min. display" (TermostatoSoggiorno)      {channel="souliss:t19:105:6-9" }
String TermostatoSoggiorno_aggiornamento "Agg.[%1$td.%1$tm.%1$tY %1$tk:%1$tM:%1$tS]" <keyring> (TermostatoSoggiorno, Diagnostic)  {channel="souliss:t31:105:6-0:lastStatusStored"}
Number TermostatoSoggiorno_healty "Salute" <keyring> (TermostatoSoggiorno, Diagnostic )  {channel="souliss:t31:105:6-0:healty"}
```

default.sitemaps:
```
sitemap default label="Tonino" {
    Frame {
        Text label="Rientro casa" icon="light" {
           Switch item=portoncino mappings=[ON="Apri"]
           Switch item=cancello mappings=[ON="Apri"]
        }
    }        
         
 Frame {
        Group item=Outside
    }

Text item=Consumo label="Consumo [%.1f W]"
Text item=Fotovoltaico label="Fotovoltaico [%.1f W]"

Frame {

        Group item=Elettricita label="Elettricità" icon="energy"
}

Frame {  
       Group item=Divano icon="light"
}

Frame label="Temperature"{	

            Text label="Temperatura e umidità" icon="temperature" {
            Default item=FamilyRoom_Temperature label="Temperatura"
            Default item=FamilyRoom_Humidity label="Umidità"
            Default item=AggiornamentoNodo6 icon="icon16x16"
        
}

Text label="Termostato soggiorno" icon="temperature" {
            Setpoint item=termostatosoggiorno_setpoint step=0.5 minValue=10 maxValue=30
            Default item=termostatosoggiorno_temperatura
            Default item=termostatosoggiorno_umidita
            Switch item=termostatosoggiorno_setasmeasured mappings=[ON="Set"]
            Switch item=termostatosoggiorno_modo label="Heating Mode" mappings=[HEATING_MODE="Set"] 
            Switch item=termostatosoggiorno_power label="Power On/Off"
            Default item=termostatosoggiorno_fire label="Fire"
	        Text item=termostatoSoggiorno_aggiornamento label="Aggiornato: [%1$td.%1$tm.%1$tY %1$tk:%1$tM:%1$tS]" icon="icon16x16"
            Default item=termostatoSoggiorno_healty
	        Slider item=termostatoSoggiorno_displayBright
	}		
}


}
```



## Community

Souliss is a small community and actualy it don't have sufficient human resource to be more active on Openhab official community

These are some very popular forum:

English Group, [here](https://groups.google.com/forum/#!forum/souliss)

Italian Group, [here](https://groups.google.com/forum/#!forum/souliss-it)

Spanish Group, [here] (https://groups.google.com/forum/#!forum/souliss-es)

## Contribution
Officiale repository for contribution in souliss github area: [here](https://github.com/souliss)



## Download 

To download latest compiled binding go to releases tab: [here](https://github.com/souliss/bindingopenhab2/releases)
