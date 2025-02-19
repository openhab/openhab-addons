# Midea AC Binding

This binding integrates Air Conditioners that use the Midea protocol. Midea is an OEM for many brands.

An AC device is likely supported if it uses one of the following Android apps or it's iOS equivalent.

| Application                                  | Comment                               | Options      |
|--:-------------------------------------------|--:------------------------------------|--------------|
| Midea Air (com.midea.aircondition.obm)       | Full Support of key and token updates | Midea Air    |
| NetHome Plus (com.midea.aircondition)        | Full Support of key and token updates | NetHome Plus |
| SmartHome/MSmartHome (com.midea.ai.overseas) | Full Support of key and token updates | MSmartHome   |

Note: The Air Conditioner must already be set-up on the WiFi network and have a fixed IP Address with one of the three apps listed above for full discovery and key and token updates.

## Supported Things

This binding supports one Thing type `ac`.

## Discovery

Once the Air Conditioner is on the network (WiFi active) most required parameters will be discovered automatically.
An IP broadcast message is sent and every responding unit gets added to the Inbox.
As an alternative use the python application msmart-ng from <https://github.com/mill1000/midea-msmart> with the msmart-ng discover ipAddress option.

## Binding Configuration

No binding configuration is required.

## Thing Configuration

| Parameter   | Required ?  | Comment                                                           | Default |
|--:----------|--:----------|--:----------------------------------------------------------------|---------|
| ipAddress   | Yes         | IP Address of the device.                                         |         |
| ipPort      | Yes         | IP port of the device                                             | 6444    |
| deviceId    | Yes         | ID of the device. Leave 0 to do ID discovery (length 6 bytes).    | 0       |
| cloud       | Yes for V.3 | Cloud Provider name for email and password                        |         |
| email       | No          | Email for cloud account chosen in Cloud Provider.                 |         |
| password    | No          | Password for cloud account chosen in Cloud Provider.              |         |
| token       | Yes for V.3 | Secret Token (length 128 HEX)                                     |         |
| key         | Yes for V.3 | Secret Key (length 64 HEX)                                        |         |
| pollingTime | Yes         | Polling time in seconds. Minimum time is 30 seconds.              | 60      |
| timeout     | Yes         | Connecting timeout. Minimum time is 2 second, maximum 10 seconds. | 4       |
| promptTone  | Yes         | "Ding" tone when command is received and executed.                | False   |
| version     | Yes         | Version 3 has token, key and cloud requirements.                  | 0       |

## Channels

Following channels are available:

| Channel                      | Type               | Description                                                                                            | Read only | Advanced |
|--:---------------------------|--:-----------------|--:-----------------------------------------------------------------------------------------------------|--:--------|--:-------|
| power                        | Switch             | Turn the AC on and off.                                                                                |           |          |
| target-temperature           | Number:Temperature | Target temperature.                                                                                    |           |          |
| operational-mode             | String             | Operational mode: OFF (turns off), AUTO, COOL, DRY, HEAT, FAN ONLY                                     |           |          |
| fan-speed                    | String             | Fan speed: OFF (turns off), SILENT, LOW, MEDIUM, HIGH, AUTO. Not all modes supported by all units.     |           |          |
| swing-mode                   | String             | Swing mode: OFF, VERTICAL, HORIZONTAL, BOTH. Not all modes supported by all units.                     |           |          |
| eco-mode                     | Switch             | Eco mode - Cool only (Temperature is set to 24 C (75 F) and fan on AUTO)                               |           |          |
| turbo-mode                   | Switch             | Turbo mode, "Boost" in Midea Air app, long press "+" on IR Remote Controller. COOL and HEAT mode only. |           |          |
| sleep-function               | Switch             | Sleep function ("Moon with a star" icon on IR Remote Controller).                                      |           |          |
| indoor-temperature           | Number:Temperature | Indoor temperature measured in the room, where internal unit is installed.                             | Yes       |          |
| outdoor-temperature          | Number:Temperature | Outdoor temperature by external unit. Some units do not report reading when off.                       | Yes       |          |
| temperature-unit             | Switch             | Sets the LED display on the evaporator to Fahrenheit (true) or Celsius (false).                        |           | Yes      |
| on-timer                     | String             | Sets the future time to turn on the AC.                                                                |           | Yes      |
| off-timer                    | String             | Sets the future time to turn off the AC.                                                               |           | Yes      |
| screen-display               | Switch             | If device supports across LAN, turns off the LED display.                                              |           | Yes      |
| humidity                     | Number             | If device supports, the indoor humidity.                                                               | Yes       | Yes      |
| appliance-error              | Switch             | If device supports, appliance error                                                                    | Yes       | Yes      |
| auxiliary-heat               | Switch             | If device supports, auxiliary heat                                                                     | Yes       | Yes      |
| alternate-target-temperature | Number:Temperature | Alternate Target Temperature - not currently used                                                      | Yes       | Yes      |

## Examples

### `demo.things` Example

```java
Thing mideaac:ac:mideaac "myAC" @ "Room" [ ipAddress="192.168.1.200", ipPort="6444", deviceId="deviceId", cloud="your cloud (e.g NetHome Plus)", email="yourclouduser@email.com", password="yourcloudpassword", token="token", key ="key", pollingTime = 60, timeout=4, promptTone="false", version="3"] 
```

Option to use the built-in binding discovery of ipPort, deviceId, token and key.

```java
Thing mideaac:ac:mideaac "myAC" @ "Room" [ ipAddress="192.168.1.200", ipPort="", deviceId="", cloud="your cloud (e.g NetHome Plus)", email="yourclouduser@email.com", password="yourcloudpassword", token="", key ="", pollingTime = 60, timeout=4, promptTone="false", version="3"] 
```

### `demo.items` Example

```java
Switch power "Power"                                                        { channel="mideaac:ac:mideaac:power" }
Number:Temperature target_temperature "Target Temperature [%.1f °F]"        { channel="mideaac:ac:mideaac:target-temperature" }
String operational_mode "Operational Mode"                                  { channel="mideaac:ac:mideaac:operational-mode" }
String fan_speed "Fan Speed"                                                { channel="mideaac:ac:mideaac:fan-speed" }
String swing_mode "Swing Mode"                                              { channel="mideaac:ac:mideaac:swing-mode" }
Number:Temperature indoor_temperature "Indoor Temperature [%.1f °F]"        { channel="mideaac:ac:mideaac:indoor-temperature" }
Number:Temperature outdoor_temperature "Current Temperature [%.1f °F]"      { channel="mideaac:ac:mideaac:outdoor-temperature" }
Switch eco_mode "Eco Mode"                                                  { channel="mideaac:ac:mideaac:eco-mode" }
Switch turbo_mode "Turbo Mode"                                              { channel="mideaac:ac:mideaac:turbo-mode" }
Switch sleep_function "Sleep function"                                      { channel="mideaac:ac:mideaac:sleep-function" }
Switch temperature_unit "Fahrenheit or Celsius"                             { channel="mideaac:ac:mideaac:temperature-unit" }
```

### `demo.sitemap` Example

```java
sitemap midea label="Split AC MBR"{
    Frame label="AC Unit" {
    Text item=outdoor_temperature label="Outdoor Temperature [%.1f °F]"
    Text item=indoor_temperature label="Indoor Temperature [%.1f °F]"
    Setpoint item=target_temperature label="Target Temperature [%.1f °F]" minValue=63.0 maxValue=78 step=1.0
    Switch item=power label="Midea AC Power"
    Switch item=temperature_unit label= "Temp Unit" mappings=[ON="Fahrenheit", OFF="Celsius"]
    Selection item=fan_speed label="Midea AC Fan Speed"
    Selection item=operational_mode label="Midea AC Mode"
    Selection item=swing_mode label="Midea AC Louver Swing Mode"
    }
}
```
