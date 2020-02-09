# Jablotron Alarm Binding

This is the OH2.x binding for Jablotron alarms.
https://www.jablotron.com/en/jablotron-products/alarms/

## Supported Things

* bridge (the bridge to your Jablonet cloud account)
* JA-80/OASIS alarm
* JA-100 alarm (no thermometer neither energy meter support)
* JA-100+/JA-100F alarm (no thermometer neither energy meter support)
 
## Discovery

This binding supports auto discovery. Just manually add a bridge thing and supply login & password to your Jablonet account.

## Binding Configuration

Binding itself doesn't require specific configuration.

## Thing Configuration

The bridge thing requires this configuration:

* login (the login to your Jablonet account)
* password (the password to your Jablonet account)
* refresh (the refresh time for all alarm warnings including ALARM, TAMPER triggers and SERVICE state flag)

optionally you can set

* lang (language of the alarm texts)

All alarm things have this configuration:

* refresh (thing channel refresh period in seconds, default is 60s)

The Ja100/JA100+ alarm thing has one extra parameter

 * code (alarm master code, used for controlling sections & PGMs)

## Channels

The bridge thing does not have any channels.
The OASIS alarm thing exposes these channels:

* statusA (the status of A section)
* statusB (the status of AB/B section)
* statusABC (the status of ABC section)
* statusPGX (the status of PGX)
* statusPGY (the status of PGY)
* command (the channel for sending keyboard codes to the OASIS alarm)
* lastEvent (the text description of the last event)
* lastEventClass (the class of the last event - e.g. arm, disarm, ...)
* lastEventInvoker (the invoker of the last event)
* lastEventTime (the time of the last event)
* lastCheckTime (the time of the last checking)
* alarm (the alarm trigger, might fire ALARM or TAMPER events)

The JA100/JA100+ things have these channels:

* lastEvent (the text description of the last event)
* lastEventClass (the class of the last event - arm, disarm, ...)
* lastEventInvoker (the invoker of the last event)
* lastEventSection (the section of the last event)
* lastEventTime (the time of the last event)
* lastCheckTime (the time of the last checking)
* alarm (the alarm trigger, might fire ALARM or TAMPER events)

all other channels for the JA100/+ alarms (sections, PGs) are dynamically created according to your configuration

* The sections are represented by String channels (with possible values "set", "unset", "partialSet" for JA100 and 
possible values "ARM", "PARTIAL_ARM" and "DISARM" for JA100+)
* The PGs (programmable gates) are represented by Switch channels 

## Full Example

#items file for JA80

```
String  HouseAlarm "Alarm [%s]" <alarm>
String JablotronCode { channel="jablotron:oasis:8c93a5ed:50139:command", autoupdate="false" }
Switch	ArmSectionA	"Garage arming"	<jablotron>	(Alarm)	{ channel="jablotron:oasis:8c93a5ed:50139:statusA" }
Switch	ArmSectionAB	"1st floor arming"	<jablotron>	(Alarm)	{ channel="jablotron:oasis:8c93a5ed:50139:statusB" }
Switch	ArmSectionABC	"2nd floor arming"	<jablotron>	(Alarm)	{ channel="jablotron:oasis:8c93a5ed:50139:statusABC" }
String LastEvent "Last event code [%s]" <jablotron> { channel="jablotron:oasis:8c93a5ed:50139:lastEvent" }
String LastEventClass "Last event class [%s]" <jablotron> { channel="jablotron:oasis:8c93a5ed:50139:lastEventClass" }
String LastEventInvoker "Last event class [%s]" <jablotron> { channel="jablotron:oasis:8c93a5ed:50139:lastEventInvoker" }
DateTime LastEventTime "Last event [%1$td.%1$tm.%1$tY %1$tR]" <clock> { channel="jablotron:oasis:8c93a5ed:50139:lastEventTime" }
DateTime LastCheckTime "Last check [%1$td.%1$tm.%1$tY %1$tR]" <clock> { channel="jablotron:oasis:8c93a5ed:50139:lastCheckTime" }
Switch	ArmControlPGX	"PGX"	<jablotron>	(Alarm)	{ channel="jablotron:oasis:8c93a5ed:50139:statusPGX" }
Switch	ArmControlPGY	"PGY"	<jablotron>	(Alarm)	{ channel="jablotron:oasis:8c93a5ed:50139:statusPGY" }
```

#sitemap example for JA80

```
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

#rule example for JA80

```
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
    Channel "jablotron:oasis:8c93a5ed:50939:alarm" triggered
then
    logInfo("default.rules", "Jablotron triggered " + receivedEvent.getEvent())
end
```