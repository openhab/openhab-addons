# Souliss Binding

[Souliss](http://www.souliss.net/) is a networking framework for Arduino and compatibles boards, and is designed to let you easily build a smart home that is distributed over multiple boards via Ethernet, WiFi, wireless point-to-point and RS485 bus.

Souliss is an open-source and community driven project, you can use the [wiki](https://github.com/souliss/souliss/wiki) and [Community](https://github.com/souliss/souliss/wiki/Community) to get help and share your results.  

## Prerequisites

The binding requires a deployed network.
As a minimum, you need one Souliss node with Ethernet or WiFi access configured as a [Gateway](https://github.com/souliss/souliss/wiki/Gateway).
A Gateway is a special node that is able to communicate with the user interfaces.
The binding interacts as a user interface for Souliss.

A starting point is the [Souliss wiki](https://github.com/souliss/souliss/wiki).
The best is to start with a single node and connect with SoulissApp.
The code for networking activities of this binding is based on [SoulissApp](https://github.com/souliss/souliss/wiki/SoulissApp) code, so once connected with SoulissApp, you can move to openHAB directly.

You can use SoulissApp and the Souliss binding at the same time, and generally up to five (by default, but can be increased) user interfaces simultaneously.

### Sketches

The easiest way is start with a simple example to control an ON/OFF light (though a relay).
You can go to project [Souliss](https://github.com/souliss/souliss), see a lot of examples sketches: [Souliss examples](https://github.com/souliss/souliss/tree/friariello/examples)

## Discovery

First add a gateway (one only is permitted on LAN at this moment), then discovery can find other things (Souliss Typicals)

## Supported Things

In Souliss Framework a Typical is one of predefined logic dedicated to smart home devices like lights, heating or antitheft.

Typical can be one of T11, T12, T13, T14, etc...

They are defined [here](https://github.com/souliss/souliss/wiki/Typicals).

Typicals match directly with openHAB Thing type.

| Device type                                               | Typical Code   | Thing type |
|-----------------------------------------------------------|----------------|------------|
| ON/OFF Digital Output with Timer Option                   | T11            | t11        |
| ON/OFF Digital Output with AUTO mode                      | T12            | t12        |
| Digital Input Value                                       | T13            | t13        |
| Pulse Digital Output                                      | T14            | t14        |
| RGB LED Strip                                             | T16            | t16        |
| ON/OFF Digital Output                                     | T18            | t18        |
| Single Color LED Strip                                    | T19            | t19        |
| Digital Input Pass Through                                | T1A            | t1A        |
| Motorized devices with limit switches                     | T21            | t21        |
| Motorized devices with limit switches and middle position | T22            | t22        |
| Temperature control                                       | T31            | t31        |
| Anti-theft integration (Main)                             | T41            | t41        |
| Anti-theft integration (Peer)                             | T42            | t42        |
| Analog input, half-precision floating point               | T51            | t51        |
| Temperature measure (-20, +50) °C                         | T52            | t52        |
| Humidity measure (0, 100) %                               | T53            | t53        |
| Light Sensor (0, 40) kLux                                 | T54            | t54        |
| Voltage (0, 400) V                                        | T55            | t55        |
| Current (0, 25)  A                                        | T56            | t56        |
| Power (0, 6500)  W                                        | T57            | t57        |
| Pressure measure (0, 1500) hPa                            | T58            | t58        |
| Analog Setpoint                                           | T61            | t61        |
| Analog Setpoint-Temperature measure (-20, +50) °C         | T62            | t62        |
| Analog Setpoint-Humidity measure (0, 100) %               | T63            | t63        |
| Analog Setpoint-Light Sensor (0, 40) kLux                 | T64            | t64        |
| Analog Setpoint-Voltage (0, 400) V                        | T65            | t65        |
| Analog Setpoint-Current (0, 25)  A                        | T66            | t66        |
| Analog Setpoint-Power (0, 6500)  W                        | T67            | t67        |
| Analog Setpoint-Pressure measure (0, 1500) hPa            | T68            | t68        |
| Broadcast messages                                        | Action Message | topic      |

### Channels

The following matrix lists the capabilities (channels) for each type:

| Thing type / Channel | Switch / onOff | Switch / sleep | DateTime / lastStatusStored | Number / healthy | Switch / autoMode | Contact / stateOnOff | Contact / stateOpenClose | Switch / pulse | Switch / whiteMode | Rollershutter / rollerBrightness | Dimmer / dimmerBrightness | Color / ledColor | Switch / one | Switch / two | Switch / three | Switch / four | Switch / five | Switch / six | Switch / seven | Switch / eight |
|----------------------|----------------|----------------|-----------------------------|------------------|-------------------|----------------------|--------------------------|----------------|--------------------|----------------------------------|---------------------------|------------------|--------------|--------------|----------------|---------------|---------------|--------------|----------------|----------------|
| t11                  | x              | x              | x                           | x                |                   |                      |                          |                |                    |                                  |                           |                  |              |              |                |               |               |              |                |                |
| t12                  | x              |                | x                           | x                | x                 |                      |                          |                |                    |                                  |                           |                  |              |              |                |               |               |              |                |                |
| t13                  |                |                | x                           | x                |                   | x                    | x                        |                |                    |                                  |                           |                  |              |              |                |               |               |              |                |                |
| t14                  |                |                | x                           | x                |                   |                      |                          | x              |                    |                                  |                           |                  |              |              |                |               |               |              |                |                |
| t16                  | x              | x              | x                           | x                |                   |                      |                          |                | x                  | x                                | x                         | x                |              |              |                |               |               |              |                |                |
| t18                  | x              |                | x                           | x                |                   |                      |                          |                |                    |                                  |                           |                  |              |              |                |               |               |              |                |                |
| t19                  | x              | x              | x                           | x                |                   |                      |                          |                |                    | x                                | x                         |                  |              |              |                |               |               |              |                |                |
| t1A                  |                |                | x                           | x                |                   |                      |                          |                |                    |                                  |                           |                  | x            | x            | x              | x             | x             | x            | x              | x              |

| Thing type / Channel | DateTime / lastStatusStored | Number / healthy | Rollershutter / rollerShutter | (see below) / rollerShutterState | (see down) / mode | (see down) / fan | Switch / status | Number / setPoint | Switch / setAsMeasured | Switch / measured | Switch / statusAlarm | Switch / onOffAlarm | Switch / rearmAlarm |
|----------------------|-----------------------------|------------------|-------------------------------|----------------------------------|-------------------|------------------|-----------------|-------------------|------------------------|-------------------|----------------------|---------------------|---------------------|
| t21                  | x                           | x                |                               | x                                |                   |                  |                 |                   |                        |                   |                      |                     |                     |
| t22                  | x                           | x                | x                             | x                                |                   |                  |                 |                   |                        |                   |                      |                     |                     |
| t31                  | x                           | x                |                               |                                  | x                 | x                | x               | x                 | x                      | x                 |                      |                     |                     |
| t41                  | x                           | x                |                               |                                  |                   |                  |                 |                   |                        |                   | x                    | x                   | x                   |
| t42                  | x                           | x                |                               |                                  |                   |                  |                 |                   |                        |                   | x                    |                     | x                   |

rollershutterstate = opening, closing, limSwitchOpen , limSwitchClose, stateOpen, stateClose, noLimSwitch

mode = COOLING_MODE, HEATING_MODE, POWEREDOFF_MODE

fan = AUTO, HIGH, MEDIUM, LOW, FANOFF

| Thing type / Channel | DateTime / lastStatusStored | Number / healthy | Number / value |
|----------------------|-----------------------------|------------------|----------------|
| t51                  | x                           | x                | x              |
| t52                  | x                           | x                | x              |
| t53                  | x                           | x                | x              |
| t54                  | x                           | x                | x              |
| t55                  | x                           | x                | x              |
| t56                  | x                           | x                | x              |
| t57                  | x                           | x                | x              |
| t58                  | x                           | x                | x              |

| Thing type / Channel | DateTime / lastStatusStored | Number / healthy | Number / value |
|----------------------|-----------------------------|------------------|----------------|
| t61                  | x                           | x                | x              |
| t62                  | x                           | x                | x              |
| t63                  | x                           | x                | x              |
| t64                  | x                           | x                | x              |
| t65                  | x                           | x                | x              |
| t66                  | x                           | x                | x              |
| t67                  | x                           | x                | x              |
| t68                  | x                           | x                | x              |
| topic                | x                           |                  | x              |

### Parameters

| Thing type | Parameters Name and Default Value | Description                                                                                           |
|------------|-----------------------------------|-------------------------------------------------------------------------------------------------------|
| Gateway    | gatewayLanAddress=""              | Mandatory - lan address of Gateway                                                                    |
| "          | gatewayWanAddress=""              | (advanced) When gateway is outside local network can insert domain/ip in this field                   |
| "          | gatewayPortNumber=230             | (advanced) Gateway UDP Port                                                                           |
| "          | preferredLocalPortNumber=23000    | (advanced) Local UDP Port                                                                             |
| "          | pingInterval=30                   | (advanced) Interval in seconds to check for device presence                                           |
| "          | subscriptionInterval=2            | (advanced) Interval in minutes to subscribe Souliss Gateway                                           |
| "          | healthyInterval=35                | (advanced) Interval in seconds to send nodes healthy                                                  |
| "          | userIndex=70                      | (advanced) Generally the default is good. It must be different from other ui (ex: SoulissApp)         |
| "          | nodeIndex=120                     | (advanced) Generally the default value work good. It must is different from other ui (ex: SoulissApp) |
| Txx (all)  | Node                              | Node of typical                                                                                       |
| Txx (all)  | Slot                              | Slot of typical                                                                                       |
| T11        | sleep=5                           | Set sleep timer in cycles                                                                             |
| T11        | secureSend=true                   | Ensure command is correctly executed                                                                  |
| T12        |                                   |                                                                                                       |
| T13        |                                   |                                                                                                       |
| T14        |                                   |                                                                                                       |
| T16        | sleep=5                           | Set sleep timer in cycles                                                                             |
| T19        | sleep=5                           | Set sleep timer in cycles                                                                             |
| T1A        |                                   |                                                                                                       |
| T21        |                                   |                                                                                                       |
| T22        |                                   |                                                                                                       |
| T31        |                                   |                                                                                                       |
| T4x        |                                   |                                                                                                       |
| T5x        |                                   |                                                                                                       |
| T6x        |                                   |                                                                                                       |

## Full Example

souliss.things:

```java
Bridge souliss:gateway:105 "Souliss Gateway - 105" [gatewayLanAddress="192.168.1.105", gatewayPortNumber=230, preferredLocalPortNumber=0, pingInterval=30, subscriptionInterval=2, healthyInterval=38, userIndex=72, nodeIndex=38,  timeoutToRequeue=5000, timeoutToRemovePacket=20000]
{  
Thing t14 1-6 "Portoncino"@"Rientro" [node=1,slot=6] //thing UID is named as node-slot only as mnemonic convention, but you are free to assign other values
Thing t14 1-7 "Cancello"@"Rientro" [node=1,slot=7]
Thing t57 1-4 "Consumo"@"Soggiorno" [node=1,slot=4]
Thing t57 4-0 "Fotovoltaico"@"Soggiorno" [node=4,slot=0]
Thing t57 4-6 "Pannelli Gruppo 1"@"Soggiorno" [node=4,slot=6]
Thing t57 4-8 "Pannelli Gruppo 2"@"Soggiorno" [node=4,slot=8]
Thing t52 4-10 "Temp.Pannelli Gruppo 1"@"Soggiorno" [node=4,slot=10]
Thing t52 4-12 "Temp.Pannelli Gruppo 2"@"Soggiorno" [node=4,slot=12]

Thing t52 3-0 "Temperatura Boiler Solare Termico" [node=3,slot=0]
Thing t52 3-2 "Temperatura Termocamino" [node=3,slot=2]
Thing t11 3-4 "Acqua Termocamino" [node=3,slot=4]
Thing t11 3-6 "Auto: Boiler / Termocamino" [node=3,slot=6]
Thing t31 3-7  "Acqua Auto: Boiler / Termocamino" [node=3,slot=7]

Thing t31 6-0 "Termostato Soggiorno"@"Soggiorno" [node=6,slot=0]
Thing t53 6-7 "Umidità"@"Soggiorno" [node=6,slot=7]
Thing t19 6-9 "Termostato Soggiorno - Luminosità"@"Soggiorno" [node=6,slot=9]

Thing t11 5-0 "Tettoia"@"Giardino"  [node=5,slot=0]

Thing t11 12-0 "Divano"@"Soggiorno" [node=12,slot=0,sleep=10, secureSend=false] 

Thing t16 8-0 "LYT1" [node=8,slot=0]

Thing t11 10-0 "Albero di Natale" [node=10,slot=0]
Thing t11 11-0 "Birra"@"Soppalco" [node=11,slot=0]
Thing t52 11-1 "Birra - Temp 1"@"Soppalco" [node=11,slot=1]
Thing t52 11-3 "Birra - Temp 2"@"Soppalco" [node=11,slot=3]
}

```

You have to write your Gateway IP Number and leave all other to default values

default.items:

```java
Group    Home                 "Tonino"        <house>

Group    FamilyRoom           "Soggiorno"     <parents_2_4>   (Home)
Group    Divano               "Divano"        (Home)
Group    Outside              "Esterno"   <garden>   (Home)
Group    TV                   "TV"   <television>   (Home)
Group    Elettricita
Group    Diagnostic
Group    TermostatoSoggiorno

Switch   tettoia                "Tettoia"                                   <light>     (Outside)   ["Lighting"]    {autoupdate="false", channel="souliss:t11:105:5-0:onoff"}
String   tettoia_aggiornamento  "Agg [%1$td.%1$tm.%1$tY %1$tk:%1$tM:%1$tS]" <keyring>   (Outside, Diagnostic)       {channel="souliss:t31:105:5-0:lastStatusStored"}

Switch   portoncino             "Portoncino"            <light>         (FamilyRoom)         ["Lighting"]   {autoupdate="false",channel="souliss:t14:105:1-6:pulse"}
Switch   cancello               "Cancello"              <light>         (FamilyRoom)         ["Lighting"]   {autoupdate="false",channel="souliss:t14:105:1-7:pulse"}

Number   FamilyRoom_Temperature   "Temperatura [%.1f °C]"                       <temperature>   (FamilyRoom)              {channel="souliss:t31:105:6-0:measured"}
Number   FamilyRoom_Humidity      "Umidità [%.1f %%]"                           <humidity>      (FamilyRoom)              {channel="souliss:t53:105:6-7:value"}
String   AggiornamentoNodo6       "Agg [%1$td.%1$tm.%1$tY %1$tk:%1$tM:%1$tS]"   <keyring>       (FamilyRoom, Diagnostic)  {channel="souliss:t31:105:6-0:lastStatusStored"}

Number   Consumo            "Consumo [%.1f W]"                                          <energy>  (FamilyRoom, Elettricita)              {channel="souliss:t57:105:1-4:value"}
Number   Fotovoltaico       "Fotovoltaico [%.1f W]"                                     <energy>  (FamilyRoom, Elettricita)              {channel="souliss:t57:105:4-0:value"}
String   AggiornamentoNodo1 "Agg.Consumi [%1$td.%1$tm.%1$tY %1$tk:%1$tM:%1$tS]"         <keyring> (FamilyRoom, Elettricita, Diagnostic)  {channel="souliss:t57:105:1-4:lastStatusStored"}
String   AggiornamentoNodo4 "Agg.Fotovoltaico [%1$td.%1$tm.%1$tY %1$tk:%1$tM:%1$tS]"    <keyring> (FamilyRoom, Elettricita, Diagnostic)  {channel="souliss:t57:105:4-0:lastStatusStored"}
                                  
Switch divano               "Divano"                                        <light> (FamilyRoom, Divano ) ["Switchable"]    {autoupdate="false", channel="souliss:t11:105:12-0:onOff"}
String divano_aggiornamento "Agg. [%1$td.%1$tm.%1$tY %1$tk:%1$tM:%1$tS]"    <keyring> (FamilyRoom, Divano, Diagnostic)      {channel="souliss:t57:105:12-0:lastStatusStored"}
String divano_healthy       "Salute"                                        <keyring> (FamilyRoom, Divano, Diagnostic)      {channel="souliss:t57:105:12-0:healthy"}

Number termostatosoggiorno_temperatura  "Temperatura [%.1f °C]" <temperature>   (TermostatoSoggiorno) {channel="souliss:t31:105:6-0:measured"}
Number termostatosoggiorno_umidita      "Umidità [%.1f %%]"     <temperature>   (TermostatoSoggiorno) {channel="souliss:t53:105:6-7:value" }

Number termostatosoggiorno_umidita "Umidità" <humidity>   (TermostatoSoggiorno)  {channel="souliss:t53:105:6-7:value" }

Number termostatosoggiorno_temperatura      "Temperatura"                   <temperature>   (TermostatoSoggiorno) {channel="souliss:t31:105:6-0:measured"}
Number termostatosoggiorno_setpoint         "Regola Set Point [%.1f °c]"    <heating>       (TermostatoSoggiorno) {autoupdate="false", channel="souliss:t31:105:6-0:sePpoint"}
Switch termostatosoggiorno_setasmeasured    "Set temp. attuale"             <heating>       (TermostatoSoggiorno) {channel="souliss:t31:105:6-0:setAsMeasured"}
String termostatosoggiorno_modo             "Modo"                                          (TermostatoSoggiorno) {autoupdate="false", channel="souliss:t31:105:6-0:mode"}
Switch termostatosoggiorno_power            "Termostato"                    <powerIcon>     (TermostatoSoggiorno) {channel="souliss:t31:105:6-0:system"}
Switch termostatosoggiorno_fire             "Fire"                          <fire>          (TermostatoSoggiorno) {channel="souliss:t31:105:6-0:fire"}

Dimmer  TermostatoSoggiorno_displayBright   "Lumin.min. display"                                    (TermostatoSoggiorno)               {channel="souliss:t19:105:6-9" }
String TermostatoSoggiorno_aggiornamento    "Agg.[%1$td.%1$tm.%1$tY %1$tk:%1$tM:%1$tS]" <keyring>   (TermostatoSoggiorno, Diagnostic)   {channel="souliss:t31:105:6-0:lastStatusStored"}
Number TermostatoSoggiorno_healthy          "Salute"                                    <keyring>   (TermostatoSoggiorno, Diagnostic )  {channel="souliss:t31:105:6-0:healthy"}
```

default.sitemaps:

```perl
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
            Default item=termostatoSoggiorno_healthy
            Slider item=termostatoSoggiorno_displayBright
   }
 }
}
```

## Community

Souliss is a small community and doesn't have sufficient human resources to be more active on openHAB official community.

These are some very popular forum:

English Group, [here](https://groups.google.com/forum/#!forum/souliss)

Italian Group, [here](https://groups.google.com/forum/#!forum/souliss-it)

Spanish Group, [here](https://groups.google.com/forum/#!forum/souliss-es)

## Contribution

Official repository for contributing to the Souliss project, GitHub page: [here](https://github.com/souliss)

## Known issues

Securesend is, at moment, enabled and tested only for t11...
