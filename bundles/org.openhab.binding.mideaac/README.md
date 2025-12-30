# Midea AC Binding

This binding integrates Air Conditioners (type AC) and Dehumidifiers that use the Midea protocol. Midea is an OEM for many brands.

A Midea device is likely supported if it uses one of the following Android apps or it's iOS equivalent.

| Cloud Provider                               | Comment                                  | Options      | Default |
|----------------------------------------------|------------------------------------------|--------------|---------|
| Midea Air (com.midea.aircondition.obm)       | Full Support of key and token updates    | Midea Air    |         |
| NetHome Plus (com.midea.aircondition)        | Full Support of key and token updates    | NetHome Plus | Yes     |
| SmartHome/MSmartHome (com.midea.ai.overseas) | Note: Reports that this cloud is offline | MSmartHome   |         |

Note: The Midea device must already be set-up on your WiFi network with a fixed IP Address to be discovered.

## Supported Things

This binding supports Thing types (Air Conditioner) `ac`  and (Dehumidifier) `a1`

## Discovery

Once the thing is on your network, activating the Inbox scan with this binding will send an IP broadcast message.
Every responding unit gets added to the Inbox. When adding each thing, the required parameters will be populated with either
discovered values or the default settings. For a V.3 device, in the unlikely event the defaults did not get the token and key,
enter your cloud provider, email and password.

## Binding Configuration

No binding configuration is required.

## Thing Configuration

| Parameter      | Required ?  | Comment                                                           | Default                   | Advanced |
|----------------|-------------|-------------------------------------------------------------------|---------------------------|----------|
| ipAddress      | Yes         | IP Address of the device.                                         |                           |          |
| ipPort         | Yes         | IP port of the device                                             | 6444                      | Yes      |
| deviceId       | Yes         | ID of the device. Leave 0 to do ID discovery.                     | 0                         | Yes      |
| cloud          | Yes for V.3 | Your Cloud Provider name (or default).                            | NetHome Plus              |          |
| email          | No          | Email for your cloud account (or default).                        | nethome+us@mailinator.com |          |
| password       | No          | Password for your cloud account (or default).                     | password1                 |          |
| token          | Yes for V.3 | Secret Token - Retrieved from cloud                               |                           | Yes      |
| key            | Yes for V.3 | Secret Key - Retrieved from cloud                                 |                           | Yes      |
| pollingTime    | Yes         | Frequency to Poll AC Status in seconds. Minimum is 30.            | 60 seconds                |          |
| keyTokenUpdate | No          | Frequency to update key-token from cloud in hours.  Minimum is 24 | 0 hours (disabled)        | Yes      |
| energyPoll     | Yes         | Frequency to poll energy data (if supported)                      | 0 minutes (disabled)      |          |
| timeout        | Yes         | Socket connection timeout in seconds. Min. is 2, max. 10.         | 4 seconds                 | Yes      |
| promptTone     | Yes         | "Ding" tone when command is received and executed.                | false                     |          |
| version        | Yes         | Version 3 has token, key and cloud requirements.                  | 3                         | Yes      |
| energyDecode   | Yes         | Binary Coded Decimal (BCD) = true. Big-endian = false.            | true                      | Yes      |
| deviceType     | Yes         | (Air Conditioner) `ac`  and (Dehumidifier) `a1`                   | ac                        | Yes      |

## Channels

Following channels are available:
Note:  After discovery, the thing properties dropdown on the Thing UI page will show what channels and modes your device supports.

| Channel             | Type               | Description                                                                                            | Read only | Advanced | AC | DH |
|---------------------|--------------------|--------------------------------------------------------------------------------------------------------|-----------|----------|----|----|
| power               | Switch             | Turn the Thing on or off.                                                                              |           |          |  X |  X |
| target-temperature  | Number:Temperature | Target temperature for AC.                                                                             |           |          |  X |    |
| operational-mode    | String             | AC Operational modes: AUTO, COOL, DRY, HEAT, FAN ONLY                                                  |           |          |  X |    |
| dehumidifier-mode   | String             | Dehumidifier Operational modes: AUTO, MANUAL, CONTINUOUS, CLOTHES DRY, SHOE DRY                        |           |          |    |  X |
| fan-speed           | String             | Fan speeds: SILENT, LOW, MEDIUM, HIGH, FULL, AUTO. Not all modes supported by all units.               |           |          |  X |    |
| dh-fan-speed        | String             | Fan speeds: OFF (turns off), LOWEST, LOW, MEDIUM, HIGH, AUTO.                                          |           |          |    |  X |
| swing-mode          | String             | Swing mode: OFF, VERTICAL, HORIZONTAL, BOTH. Not all modes supported by all units.                     |           |          |  X |    |
| dehumidifier-swing  | Switch             | Turns Dehumidifier Swing mode On or Off                                                                |           |          |    |  X |
| dh-tank-setpoint    | Number             | Dehumidifier Tank Setpoint 25%, 50%, 75%, 100%                                                         |           |          |    |  X |
| dehumidifier-tank   | Number             | Dehumidifier Tank Level                                                                                | Yes       |          |    |  X |
| eco-mode            | Switch             | Eco mode - Cool only (Temperature is set to 24 C (75 F) and fan on AUTO)                               |           |          |  X |    |
| turbo-mode          | Switch             | Turbo mode, "Boost" in Midea Air app, long press "+" on IR Remote Controller. COOL and HEAT only.      |           |          |  X |    |
| sleep-function      | Switch             | Sleep function ("Moon with a star" icon on IR Remote Controller).                                      |           |          |  X |    |
| indoor-temperature  | Number:Temperature | Indoor temperature measured in the room, where internal unit is installed.                             | Yes       |          |  X |  X |
| outdoor-temperature | Number:Temperature | Outdoor temperature by external unit. Some units do not report reading when off.                       | Yes       |          |  X |    |
| temperature-unit    | Switch             | Sets the LED display on the evaporator to Fahrenheit (true) or Celsius (false).                        |           | Yes      |  X |    |
| on-timer            | String             | Sets the future time to turn on the Device.                                                            |           | Yes      |  X |  X |
| off-timer           | String             | Sets the future time to turn off the Device.                                                           |           | Yes      |  X |  X |
| screen-display      | Switch             | If device supports across LAN, turns off the LED display.                                              |           | Yes      |  X |    |
| maximum-humidity    | Number             | Dehumidifier control point, AC If device supports in DRY mode                                          |           |          |  X |  X |
| humidity            | Number             | If device supports, the indoor room humidity.                                                          | Yes       | Yes      |  X |  X |
| energy-consumption  | Number             | If device supports, cumulative Kilowatt-Hours usage                                                    | Yes       | Yes      |  X |    |
| current-draw        | Number             | If device supports, instantaneous amperage usage                                                       | Yes       | Yes      |  X |    |
| power-consumption   | Number             | If device supports, instantaneous wattage reading                                                      | Yes       | Yes      |  X |    |
| appliance-error     | Switch             | If device supports, appliance error notification                                                       | Yes       | Yes      |  X |    |
| filter-status       | Switch             | If device supports, notification that filter needs cleaning                                            | Yes       | Yes      |  X |    |
| auxiliary-heat      | Switch             | If device supports, auxiliary heat (On or Off)                                                         | Yes       | Yes      |  X |    |
| dh-child-lock       | Switch             | If device supports, Child Lock (On or Off)                                                             | Yes       | Yes      |    |  X |
| dh-anion            | Switch             | If device supports, Anion (On or Off)                                                                  | Yes       | Yes      |    |  X |

## Examples

### `demo.things` Examples

```java
Thing mideaac:ac:mideaac "myAC" @ "Room" [ ipAddress="192.168.1.200", ipPort=6444, deviceId="deviceId", cloud="your cloud (e.g NetHome Plus)", email="yourclouduser@email.com", password="yourcloudpassword", token="token", key ="key", pollingTime = 60, keyTokenUpdate = 0, energyPoll = 0, timeout=4, promptTone="false", version="3", energyDecode="true", deviceType="ac"] 
Thing mideaac:a1:mideaac "myDehumidifier" @ "Room" [ ipAddress="192.168.1.200", ipPort=6444, deviceId="deviceId", cloud="your cloud (e.g NetHome Plus)", email="yourclouduser@email.com", password="yourcloudpassword", token="token", key ="key", pollingTime = 60, keyTokenUpdate = 0, energyPoll = 0, timeout=4, promptTone="false", version="3", deviceType="a1"] 
```

Minimal IP Address Option to use the built-in defaults.

```java
Thing mideaac:ac:air_conditioner "myAC" @ "myRoom" [ ipAddress="192.168.0.200"] or
Thing mideaac:a1:dehumidifier "myDehumidifier" @ "myRoom" [ ipAddress="192.168.0.200"]
```

### `demo.items` AC Example

```java
Switch power "Power"                                                        { channel="mideaac:ac:air_conditioner:power" }
Number:Temperature target_temperature "Target Temperature [%.1f °F]"        { channel="mideaac:ac:air_conditioner:target-temperature" }
String operational_mode "Operational Mode"                                  { channel="mideaac:ac:air_conditioner:operational-mode" }
String fan_speed "Fan Speed"                                                { channel="mideaac:ac:air_conditioner:fan-speed" }
String swing_mode "Swing Mode"                                              { channel="mideaac:ac:air_conditioner:dehumidifier-swing" }
Number:Temperature indoor_temperature "Indoor Temperature [%.1f °F]"        { channel="mideaac:ac:air_conditioner:indoor-temperature" }
Switch eco_mode "Eco Mode"                                                  { channel="mideaac:ac:air_conditioner:eco-mode" }
Switch turbo_mode "Turbo Mode"                                              { channel="mideaac:ac:air_conditioner:turbo-mode" }
Switch sleep_function "Sleep function"                                      { channel="mideaac:ac:air_conditioner:sleep-function" }
Switch temperature_unit "Fahrenheit or Celsius"                             { channel="mideaac:ac:air_conditioner:temperature-unit" }
```

### `demo.items` Dehumidifier Example

```java
Switch power "Power"                                                        { channel="mideaac:a1:dehumidifier:power" }
Number maximum-humidity "Maximim Humidity"                                  { channel="mideaac:a1:dehumidifier:maximum-humidity" }
String dehumidifier-mode "Dehumidifier Mode"                                { channel="mideaac:a1:dehumidifier:dehumidifier-mode" }
String dh-fan-speed "Dehumidifier Fan Speed"                                { channel="mideaac:a1:dehumidifier:dh-fan-speed" }
String dehumidifier-swing "Dehumidifier Swing Mode"                         { channel="mideaac:a1:dehumidifier:dehumidifier-swing" }
Number:Temperature indoor-temperature "Indoor Temperature [%.1f °F]"        { channel="mideaac:a1:dehumidifier:indoor-temperature" }
Number humidity "Humidity in Room"                                          { channel="mideaac:a1:dehumidifier:humidity" }
```

### `demo.sitemap` AC Example

```java
sitemap midea label="Midea AC"{
    Frame label="AC Unit" {
        Text item=outdoor_temperature label="Outdoor Temperature [%.1f °F]"
        Text item=indoor_temperature label="Indoor Temperature [%.1f °F]"
        Setpoint item=target_temperature label="Target Temperature [%.1f °F]" minValue=62.0 maxValue=86 step=1.0
        Switch item=power label="Midea AC Power"
        Switch item=temperature_unit label= "Temp Unit" mappings=[ON="Fahrenheit", OFF="Celsius"]
        Selection item=fan_speed label="Midea AC Fan Speed"
        Selection item=operational_mode label="Midea AC Mode"
        Selection item=swing_mode label="Midea AC Louver Swing Mode"
    }
}
```
