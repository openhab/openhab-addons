# Jablotron Alarm Binding

This is the OH2.x binding for Jablotron alarms.
https://www.jablotron.com/en/jablotron-products/alarms/

## Supported Things

* bridge (the bridge to your jablonet cloud account)
* JA-80 OASIS alarm
* JA-100 alarm (partial support, still under development)
 
Please contact me if you want to add other alarms (e.g. JA-100 etc)

## Discovery

This binding support auto discovery. Just manually add bridge thing and supply login & password to your Jablonet account.

## Binding Configuration

Binding itself doesn't require specific configuration.

## Thing Configuration

The bridge thing requires this configuration:

* login (login to your jablonet account)
* password (password to your jablonet account)

The oasis thing require this configuration (it is better to have it autodiscovered):

* serviceId (Jablotron internal service id of your alarm)
* url (an initialization url for the alarm)
* refresh (thing status refresh period in seconds)

## Channels

The bridge thing does not have any channels.
The oasis thing exposes these channels:

* statusA (the status of A section)
* statusB (the status of AB/B section)
* statusABC (the status of ABC section)
* statusPGX (the status of PGX)
* statusPGY (the status of PGY)
* command (the channel for sending codes to alarm)
* lastEvent (the text description of the last event)
* lastEventCode (the code of the last event)
* lastEventClass (the class of the last event - arm, disarm, service)
* lastEventTime (the time of the last event)
* lastCheckTime (the time of the last checking)
* alarm (the alarm status OFF/ON)

## Full Example

#items file

```
String  HouseAlarm "Alarm [%s]" <alarm>
String JablotronCode { channel="jablotron:oasis:8c93a5ed:50139:command", autoupdate="false" }
Switch	ArmSectionA	"Garage arming"	<jablotron>	(Alarm)	{ channel="jablotron:oasis:8c93a5ed:50139:statusA" }
Switch	ArmSectionAB	"1st floor arming"	<jablotron>	(Alarm)	{ channel="jablotron:oasis:8c93a5ed:50139:statusB" }
Switch	ArmSectionABC	"2nd floor arming"	<jablotron>	(Alarm)	{ channel="jablotron:oasis:8c93a5ed:50139:statusABC" }
String LastEvent "Last event code [%s]" <jablotron> { channel="jablotron:oasis:8c93a5ed:50139:lastEvent" }
DateTime LastEventTime "Last event [%1$td.%1$tm.%1$tY %1$tR]" <clock> { channel="jablotron:oasis:8c93a5ed:50139:lastEventTime" }
DateTime LastCheckTime "Last check [%1$td.%1$tm.%1$tY %1$tR]" <clock> { channel="jablotron:oasis:8c93a5ed:50139:lastCheckTime" }
Switch	ArmControlPGX	"PGX"	<jablotron>	(Alarm)	{ channel="jablotron:oasis:8c93a5ed:50139:statusPGX" }
Switch	ArmControlPGY	"PGY"	<jablotron>	(Alarm)	{ channel="jablotron:oasis:8c93a5ed:50139:statusPGY" }
```

#sitemap example

```
Text item=HouseAlarm icon="alarm" {
            Switch item=ArmSectionA
            Switch item=ArmSectionAB
            Switch item=ArmSectionABC
            Text item=LastEvent
            Text item=LastEventCode
            Text item=LastEventClass
            Text item=LastEventTime
            Text item=LastCheckTime
            Switch item=ArmControlPGX
            Switch item=ArmControlPGY
            Switch item=JablotronCode label="Arm" mappings=[1234=" A ",2345=" B ",3456="ABC"]
            Switch item=JablotronCode label="Disarm" mappings=[9876="Disarm"]
      }
```

#rule example

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
```