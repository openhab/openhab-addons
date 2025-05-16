# Midea AC Binding

This binding integrates Air Conditioners that use the Midea protocol. Midea is an OEM for many brands.

An AC device is likely supported if it uses one of the following Android apps or it's iOS equivalent.

| Application                                  | Comment                                  | Options      | Default |
|----------------------------------------------|------------------------------------------|--------------|---------|
| Midea Air (com.midea.aircondition.obm)       | Full Support of key and token updates    | Midea Air    |         |
| NetHome Plus (com.midea.aircondition)        | Full Support of key and token updates    | NetHome Plus | Yes     |
| SmartHome/MSmartHome (com.midea.ai.overseas) | Note: Reports that this cloud is offline | MSmartHome   |         |

Note: The Air Conditioner must already be set-up on your WiFi network with a fixed IP Address to be discovered.

## Supported Things

This binding supports one Thing type `ac`.

## Discovery

Once the Air Conditioner is on your network activating the Inbox scan with this binding will send an IP broadcast message.
Every responding unit gets added to the Inbox. When adding each thing, the required parameters will be populated with either
discovered values or the default settings. For a V.3 device, in the unlikely event the defaults did not get the token and key,
enter your cloud provider, email and password. The thing properties dropdown on the Thing UI page will show supported AC functions.

## Binding Configuration

No binding configuration is required.

## Thing Configuration

| Parameter     | Required ?  | Comment                                                      | Default                   |
|---------------|-------------|--------------------------------------------------------------|---------------------------|
| ipAddress     | Yes         | IP Address of the device.                                    |                           |
| ipPort        | Yes         | IP port of the device                                        | 6444                      |
| deviceId      | Yes         | ID of the device. Leave 0 to do ID discovery.                | 0                         |
| cloud         | Yes for V.3 | Your Cloud Provider name (or default).                       | NetHome Plus              |
| email         | No          | Email for your cloud account (or default).                   | nethome+us@mailinator.com |
| password      | No          | Password for your cloud account (or default).                | password1                 |
| token         | Yes for V.3 | Secret Token - Retrieved from cloud                          |                           |
| key           | Yes for V.3 | Secret Key - Retrieved from cloud                            |                           |
| pollingTime   | Yes         | Frequency to Poll AC Status in seconds. Minimum is 30.       | 60 seconds                |
| keyTokenUpdate| No          | Frequency to update key and token from cloud in days         | 0 days (disabled)         |
| energyPoll    | Yes         | Frequency to poll energy data (if supported)                 | 0 minutes (disabled)      |
| timeout       | Yes         | Socket connection timeout in seconds. Min. is 2, max. 10.    | 4 seconds                 |
| promptTone    | Yes         | "Ding" tone when command is received and executed.           | false                     |
| version       | Yes         | Version 3 has token, key and cloud requirements.             | 0                         |
| energyDecode  | Yes         | Binary Coded Decimal (BCD) = true. Big-endian = false.       | true

## Channels

Following channels are available:

| Channel                      | Type               | Description                                                                                            | Read only | Advanced |
|------------------------------|--------------------|--------------------------------------------------------------------------------------------------------|-----------|----------|
| power                        | Switch             | Turn the AC on or off.                                                                                 |           |          |
| target-temperature           | Number:Temperature | Target temperature.                                                                                    |           |          |
| operational-mode             | String             | Operational modes: OFF, AUTO, COOL, DRY, HEAT, FAN ONLY                                                |           |          |
| fan-speed                    | String             | Fan speeds: OFF (turns off), SILENT, LOW, MEDIUM, HIGH, AUTO. Not all modes supported by all units.    |           |          |
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
| kilowatt-hours               | Number             | If device supports, cumulative KWH usage                                                               | Yes       | Yes      |
| amperes                      | Number             | If device supports, current amperage usage                                                             | Yes       | Yes      |
| watts                        | Number             | If device supports, wattage                                                                            | Yes       | Yes      |
| appliance-error              | Switch             | If device supports, appliance error                                                                    | Yes       | Yes      |
| auxiliary-heat               | Switch             | If device supports, auxiliary heat                                                                     | Yes       | Yes      |

## Examples

### `demo.things` Examples

```java
Thing mideaac:ac:mideaac "myAC" @ "Room" [ ipAddress="192.168.1.200", ipPort=6444, deviceId="deviceId", cloud="your cloud (e.g NetHome Plus)", email="yourclouduser@email.com", password="yourcloudpassword", token="token", key ="key", pollingTime = 60, keyTokenUpdate = 0, energyPoll = 0, timeout=4, promptTone="false", version="3", energyDecode="true"] 
```

Minimal IP Address Option to use the built-in defaults.

```java
Thing mideaac:ac:mideaac "myAC" @ "myRoom" [ ipAddress="192.168.0.200"] 
```

### `demo.items` Examples

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

### `demo.sitemap` Examples

```java
sitemap midea label="Split AC"{
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
