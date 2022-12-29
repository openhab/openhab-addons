# Jablotron Alarm Binding

This is the binding for Jablotron alarms.
<https://www.jablotron.com/en/jablotron-products/alarms/>

## Supported Things

| thing   | note                                      |
|---------|-------------------------------------------|
| bridge  | the bridge to your Jablonet cloud account |
| JA-80   | the OASIS alarm                           |
| JA-100  | with the thermometer support              |
| JA-100F | without the thermometer support           |

## Discovery

This binding supports auto discovery. Just manually add a bridge thing and supply login & password to your Jablonet account.

## Binding Configuration

Binding itself doesn't require specific configuration.

## Thing Configuration

| thing                | config parameter name | description                                                                        | type                  |
|----------------------|-----------------------|------------------------------------------------------------------------------------|-----------------------|
| bridge               | login                 | the login to your Jablonet account                                                 | mandatory             |
| bridge               | password              | the password to your Jablonet account                                              | mandatory             |
| bridge               | refresh               | the refresh time for all alarm warnings (ALARM, TAMPER triggers and SERVICE state) | optional, default=30s |
| bridge               | lang                  | the language of the alarm texts                                                    | optional, default=en  |
| JA-80/JA-100/JA-100F | refresh               | the channels refresh period in seconds                                             | optional, default=60s |
| JA-80/JA-100/JA-100F | serviceId             | the service ID which identifies the alarm                                          | mandatory             |
| JA-100/JA-100F       | code                  | the master code for controlling sections                                           | optional              |

## Channels

| thing                | channel name     | item type          | description                                               |
|----------------------|------------------|--------------------|-----------------------------------------------------------|
| bridge               | N/A              | N/A                | the bridge does not expose any channels                   |
| JA-80                | statusA          | Switch             | the status of the A section                               |
| JA-80                | statusB          | Switch             | the status of the AB/B section                            |
| JA-80                | statusABC        | Switch             | the status of the ABC section                             |
| JA-80                | statusPGX        | Switch             | the status of PGX                                         |
| JA-80                | statusPGY        | Switch             | the status of PGY                                         |
| JA-80                | command          | String             | the channel for sending keyboard codes to the OASIS alarm |
| JA-80/JA-100/JA-100F | lastEvent        | String             | the description of the last event                         |
| JA-80/JA-100/JA-100F | lastEventClass   | String             | the class of the last event - e.g. arm, disarm, ...       |
| JA-80/JA-100/JA-100F | lastEventInvoker | String             | the invoker of the last event                             |
| JA-80/JA-100/JA-100F | lastEventTime    | DateTime           | the time of the last event                                |
| JA-80/JA-100/JA-100F | lastCheckTime    | DateTime           | the time of the last checking                             |
| JA-80/JA-100/JA-100F | alarm            | N/A                | the alarm trigger, might fire ALARM or TAMPER events      |
| JA-100/JA-100F       | lastEventSection | String             | the section of the last event                             |
| JA-100               | state_%nr%       | String             | the section %nr% status/control                           |
| JA-100               | pgm_%nr%         | Switch             | the PG switch %nr% status/control                         |
| JA-100               | thermometer_%nr% | Number:Temperature | the thermometer %nr% value                                |
| JA-100               | thermostat_%nr%  | Number:Temperature | the thermostat %nr% value                                 |
| JA-100F              | sec-%nr%         | String             | the section %nr% status/control                           |
| JA-100F              | pg-%nr%          | Switch             | the PG switch %nr% status/control                         |
| JA-100F              | thm-%nr%         | Number:Temperature | the thermometer %nr% value                                |

The state, pgm, thermometer, thermostat, sec and pg channels for the JA-100/JA-100F alarms are dynamically created according to your configuration.

- The sections are represented by String channels (with possible values "set", "unset", "partialSet" for JA-100 and possible values "ARM", "PARTIAL_ARM" and "DISARM" for JA100-F)

## Full Example

# items file for JA80

```java
String  HouseAlarm "Alarm [%s]" <alarm>
String JablotronCode { channel="jablotron:oasis:8c93a5ed:50139:command", autoupdate="false" }
Switch ArmSectionA "Garage arming" <jablotron> (Alarm) { channel="jablotron:oasis:8c93a5ed:50139:statusA" }
Switch ArmSectionAB "1st floor arming" <jablotron> (Alarm) { channel="jablotron:oasis:8c93a5ed:50139:statusB" }
Switch ArmSectionABC "2nd floor arming" <jablotron> (Alarm) { channel="jablotron:oasis:8c93a5ed:50139:statusABC" }
String LastEvent "Last event code [%s]" <jablotron> { channel="jablotron:oasis:8c93a5ed:50139:lastEvent" }
String LastEventClass "Last event class [%s]" <jablotron> { channel="jablotron:oasis:8c93a5ed:50139:lastEventClass" }
String LastEventInvoker "Last event class [%s]" <jablotron> { channel="jablotron:oasis:8c93a5ed:50139:lastEventInvoker" }
DateTime LastEventTime "Last event [%1$td.%1$tm.%1$tY %1$tR]" <clock> { channel="jablotron:oasis:8c93a5ed:50139:lastEventTime" }
DateTime LastCheckTime "Last check [%1$td.%1$tm.%1$tY %1$tR]" <clock> { channel="jablotron:oasis:8c93a5ed:50139:lastCheckTime" }
Switch ArmControlPGX "PGX" <jablotron> (Alarm) { channel="jablotron:oasis:8c93a5ed:50139:statusPGX" }
Switch ArmControlPGY "PGY" <jablotron> (Alarm) { channel="jablotron:oasis:8c93a5ed:50139:statusPGY" }
```

# sitemap example for JA80

```java
Text item=HouseAlarm icon="alarm" {
          Switch item=ArmSectionA
          Switch item=ArmSectionAB
          Switch item=ArmSectionABC
          Text item=LastEvent
          Text item=LastEventInvoker
          Text item=LastEventClass
          Text item=LastEventTime
          Text item=LastCheckTime
          Switch item=ArmControlPGX
          Switch item=ArmControlPGY
          Switch item=JablotronCode label="Arm" mappings=[1234=" A ", 2345=" B ", 3456="ABC"]
          Switch item=JablotronCode label="Disarm" mappings=[9876="Disarm"]
      }
```

# rule example for JA80

```java
rule "Alarm"
when 
  Item ArmSectionA changed or Item ArmSectionAB changed or Item ArmSectionABC changed or 
  System started
then
   if( ArmSectionA.state == ON || ArmSectionAB.state == ON || ArmSectionABC.state == ON)
   {   postUpdate(HouseAlarm, "partial")  }
   if( ArmSectionA.state == OFF && ArmSectionAB.state == OFF && ArmSectionABC.state == OFF)
   {   postUpdate(HouseAlarm, "disarmed") }
   if( ArmSectionA.state == ON && ArmSectionAB.state == ON && ArmSectionABC.state == ON)
   {   postUpdate(HouseAlarm, "armed")    }
end

rule "Jablotron alarm trigger"
when
    Channel "jablotron:oasis:8c93a5ed:50139:alarm" triggered
then
    logInfo("default.rules", "Jablotron triggered " + receivedEvent.getEvent())
end
```
