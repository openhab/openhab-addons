# Meteostick Binding

This is the binding for the [Meteostick](https://www.smartbedded.com/wiki/index.php/Meteostick) weather receiver dongle.
This is an RF receiver that can receive data directly from Davis weather devices (and others).

## Supported Things

This binding support 2 different things types

| Thing                | Type   | Description                       |
|----------------------|--------|-----------------------------------|
| meteostick_bridge    | Bridge | This is the Meteostick USB stick  |
| meteostick_davis_iss | Thing  | This is the Davis Vue ISS         |

## Binding Configuration

The Meteostick things need to be manually added - there is no discovery in the Meteostick binding.

First add and configure the Meteostick bridge - the port and frequency band for your region need to be set.
Next add the sensor and configure the channel number.

## Thing Configuration

### meteostick_bridge Configuration Options

| Option | Description                                        |
|--------|----------------------------------------------------|
| port   | Sets the serial port to be used for the stick      |
| mode   | Sets the mode (frequency band)                     |

Set mode to one of the following depending on your device and region:

| Mode  | Device       | Region           | Frequency |
|-------|--------------|------------------|-----------|
| 0     | Davis        | North America    | 915 Mhz   |
| 1     | Davis        | Europe           | 868 Mhz   |
| 2     | Davis        | Australia        | 915 Mhz   |
| 3     | Fine Offset  | North America    | 915 Mhz   |
| 4     | Fine Offset  | Europe           | 868 Mhz   |
| 5     | Davis        | New Zealand      | 931.5 Mhz |

### meteostick_davis_iss Configuration Options

| Option             | Description                                                                                                                                                                      |
|--------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| channel            | Sets the RF channel used for this sensor                                                                                                                                         |
| spoon              | Size of rain spoon assembly for this sensor in mm.  Default value is 0.254 (0.01") for use with Davis part number 7345.280.  Set to 0.2 for use with Davis part number 7345.319  |
| deltaWindDirection | For Davis 6410, 7911 & 7914 anemometers, if your anemometer cannot be mounted aiming true North set the direction it is aiming here (0 to 359 degrees). Default is 0 (for North) |

## Channels

### Meteostick

| Channel Type ID    | Item Type          | Description        |
|--------------------|--------------------|--------------------|
| pressure           | Number:Pressure    | Air pressure       |
| indoor-temperature | Number:Temperature | Indoor temperature |

### Davis ISS

| Channel Type ID     | Item Type             | Description                                     |
|---------------------|-----------------------|-------------------------------------------------|
| outdoor-temperature | Number:Temperature    | Outside temperature                             |
| humidity            | Number                | Humidity                                        |
| wind-direction      | Number:Angle          | Wind direction                                  |
| wind-direction-last2min-average | Number:Angle | Wind direction average over last 2 minutes   |
| wind-speed          | Number:Speed          | Wind speed                                      |
| wind-speed-last2min-average     | Number:Speed | Wind speed average over last 2 minutes       |
| wind-speed-last2min-maximum     | Number:Speed | Wind speed maximum over last 2 minutes       |
| rain-raw            | Number                | Raw rain counter from the tipping spoon sensor  |
| rain-currenthour    | Number:Length         | The rainfall in the last 60 minutes             |
| rain-lasthour       | Number:Length         | The rainfall in the previous hour               |
| rain-today          | Number:Length         | Accumulated rainfall for today
| solar-power         | Number                | Solar power from the sensor station             |
| signal-strength     | Number                | Received signal strength                        |
| low-battery         | Switch                | Low battery warning                             |

#### Rainfall

There are four channels associated with rainfall.
The raw counter from the tipping bucket is provided, the rainfall in the last 60 minutes is updated on each received rainfall and provides the past 60 minutes of rainfall.
The rainfall in the previous hour is the rainfall for each hour of the day and is updated on the hour.
The accumulated rainfall for today provides the amount of rain for the current date and will reset to 0 at timezone's midnight.

## Full Example

This example uploads weather data to for your personal weather station at Weather Underground every two minutes.

Steps:

1. Install the [MeteoStick](https://www.smartbedded.com/wiki/index.php/Meteostick) binding for use with your [Davis Vantage Vue Integrated Sensor Suite (ISS)](https://www.davisnet.com/solution/vantage-vue/).
1. [Register](https://www.wunderground.com/personal-weather-station/signup.asp) your personal weather station with Weather Underground and make note of the station ID and password issued.
1. Add the following files to your openHAB configuration:

### things/meteostick.things

Things can be defined in the .things file as follows:

```java
meteostick:meteostick_bridge:receiver [ port="/dev/tty.usbserial-AI02XA60", mode=1 ]
meteostick:meteostick_davis_iss:iss (meteostick:meteostick_bridge:receiver) [ channel=1, spoon=0.2, deltaWindDirection=0 ]
```

Note the configuration options for `port`, `mode`, `channel`, `deltaWindDirection` and `spoon` above and adjust as needed for your specific hardware.

### items/meteostick.items

```java
Number:Pressure MeteoStickPressure "Meteostick Pressure [%.1f hPa]"{ channel="meteostick:meteostick_bridge:receiver:pressure" }
Number:Temperature DavisVantageVueOutdoorTemperature "ISS Outdoor Temp [%.1f °C]" { channel="meteostick:meteostick_davis_iss:iss:outdoor-temperature" }
Number DavisVantageVueHumidity "ISS Humidity [%.0f %%]" { channel="meteostick:meteostick_davis_iss:iss:humidity" }
Number:Angle DavisVantageVueWindDirection "ISS Wind Direction [%.0f °]" { channel="meteostick:meteostick_davis_iss:iss:wind-direction" }
Number:Angle DavisVantageVueWindDirectionAverage "ISS Average Wind Direction [%.0f °]" { channel="meteostick:meteostick_davis_iss:iss:wind-direction-last2min-average" }
Number:Speed DavisVantageVueWindSpeed "ISS Wind Speed [%.1f m/s]" { channel="meteostick:meteostick_davis_iss:iss:wind-speed" }
Number:Speed DavisVantageVueWindSpeedAverage "ISS Average Wind Speed [%.1f m/s]" { channel="meteostick:meteostick_davis_iss:iss:wind-speed-last2min-average" }
Number:Speed DavisVantageVueWindSpeedMaximum "ISS Maximum Wind Speed [%.1f m/s]" { channel="meteostick:meteostick_davis_iss:iss:wind-speed-last2min-maximum" }
Number:Length DavisVantageVueRainCurrentHour "ISS Rain Current Hour [%.1f mm]" { channel="meteostick:meteostick_davis_iss:iss:rain-currenthour" }
Number:Length DavisVantageVueRainToday "ISS Rain Today [%.1f mm]" { channel="meteostick:meteostick_davis_iss:iss:rain-today" }
```

### rules/meteostick.rules

Replace `YOUR_ID` and `your_password` below with the values from the the Weather Underground registration process.

```java
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Map
import java.util.TimeZone

/* Uploads weather station data using the format documented here:

   https://feedback.weather.com/customer/en/portal/articles/2924682-pws-upload-protocol?b_id=17298
 */

rule PWS
when
 Item DavisVantageVueWindDirectionAverage received update
then
 val id = 'YOUR_ID'
 val pw = 'your_password'
 val sdf = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss')
 sdf.setTimeZone(TimeZone.getTimeZone('UTC'))
 val double rh = DavisVantageVueHumidity.getStateAs(DecimalType).doubleValue
 val double tempc = DavisVantageVueOutdoorTemperature.getStateAs(QuantityType).toUnit('°C').doubleValue
 val double dewptc = 243.04 * (Math.log(rh/100) + ((17.625 * tempc) / (243.04 + tempc))) / (17.625 - Math.log(rh/100) - ((17.625 * tempc) / (243.04 + tempc)))
 val double dewptf = new QuantityType(dewptc, CELSIUS).toUnit('°F').doubleValue
 val Map<String, Object> params = newLinkedHashMap(
  'action' ->           'updateraw',
  'ID' ->               id,
  'PASSWORD' ->         pw,
  'dateutc' ->          sdf.format(new Date()),
  'winddir' ->          DavisVantageVueWindDirection.getStateAs(QuantityType).toUnit('°').intValue,
  'windspeedmph' ->     DavisVantageVueWindSpeed.getStateAs(QuantityType).toUnit('mph').doubleValue,
  'windgustmph' ->      DavisVantageVueWindSpeedMaximum.getStateAs(QuantityType).toUnit('mph').doubleValue,
  'windgustdir' ->      DavisVantageVueWindDirectionAverage.getStateAs(QuantityType).toUnit('°').intValue,
  'windspdmph_avg2m' -> DavisVantageVueWindSpeedAverage.getStateAs(QuantityType).toUnit('mph').doubleValue,
  'winddir_avg2m' ->    DavisVantageVueWindDirectionAverage.getStateAs(QuantityType).toUnit('°').intValue,
  'humidity' ->         DavisVantageVueHumidity.state,
  'dewptf' ->           dewptf,
  'tempf' ->            DavisVantageVueOutdoorTemperature.getStateAs(QuantityType).toUnit('°F').doubleValue,
  'rainin' ->           DavisVantageVueRainCurrentHour.getStateAs(QuantityType).toUnit('in').doubleValue,
  'dailyrainin' ->	DavisVantageVueRainToday.getStateAs(QuantityType).toUnit('in').doubleValue,
  'baromin' ->          MeteoStickPressure.getStateAs(QuantityType).toUnit('inHg').doubleValue,
  'softwaretype' ->     'openHAB 2.4')

 var url = 'https://weatherstation.wunderground.com/weatherstation/updateweatherstation.php?'
 var first = true
 for (key : params.keySet()) {
  if (!first) {
   url += '&'
  }
  url += key + '=' + URLEncoder::encode(params.get(key).toString, 'UTF-8')
  first = false
 }

 logDebug('PWS', 'url is {}', url)
 sendHttpGetRequest(url)
end
```

openHAB will now report your weather station data to Weather Underground every two minutes.
