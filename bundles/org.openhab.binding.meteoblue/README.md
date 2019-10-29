# meteoblue Binding

The meteoblue binding uses the [meteoblue weather service](https://content.meteoblue.com/en/content/view/full/4511)
to provide weather information.


## Supported Things

The binding has two thing types.

The first thing type is the weather thing. Each weather thing has the ID `weather` and retrieves weather data for one location.
The second thing type is the bridge thing. The bridge thing, which has the ID `bridge`, holds the API key to be used for all of
its child things.


## Thing Configuration

### Bridge Thing Configuration

| Property      | Default Value | Required? | Description          |
| ------------- |:-------------:| :-------: | -------------------- |
| apiKey        |               | Yes       | The api key to be used with the meteoblue service |


### Weather Thing Configuration

| Property      | Default Value | Required? | Description          |
| ------------- |:-------------:| :-------: | -------------------- |
| location      |               | Yes       | The latitude, longitude, and optionally altitude of the location, separated by commas (e.g. 45.6,45.7,45.8). Altitude, if given, should be in meters.
| refresh       | 240           | No        | The time between calls to refresh the weather data, in minutes |
| serviceType   | NonCommercial | No        | The service type to be used.  Either 'Commercial' or 'NonCommercial' |
| timeZone      |               | No        | The time zone to use for the location. Optional, but the service recommends it be specified. The service gets the time zone from a database if not specified. |


## Channels

### Channel Groups

| Group Name       | Description |
| ---------------- | ----------- |
| forecastToday    | Today's forecast |
| forecastTomorrow | Tomorrow's forecast |
| forecastDay2     | Forecast 2 days out |
| forecastDay3     | Forecast 3 days out |
| forecastDay4     | Forecast 4 days out |
| forecastDay5     | Forecast 5 days out |
| forecastDay6     | Forecast 6 days out |

### Channels

Each of the following channels is supported in all of the channel groups.

| Channel                  | Item Type          | Description |
| ------------------------ | ------------------ | ----------- |
| height                   | Number:Length      | Altitude above sea-level of the location (in meters) |
| forecastDate             | DateTime           | Forecast date |
| UVIndex                  | Number             | UltraViolet radiation index at ground level (0-16) |
| minTemperature           | Number:Temperature | Low temperature |
| maxTemperature           | Number:Temperature | High temperature |
| meanTemperature          | Number:Temperature | Mean temperature |
| feltTemperatureMin       | Number:Temperature | Low "feels like" temperature |
| feltTemperatureMax       | Number:Temperature | High "feels like" temperature |
| relativeHumidityMin      | Number             | Low relative humidity |
| relativeHumidityMax      | Number             | High relative humidity |
| relativeHumidityMean     | Number             | Mean relative humidity |
| precipitationProbability | Number             | Percentage probability of precipitation |
| precipitation            | Number:Length      | Total precipitation (water amount) |
| convectivePrecipitation  | Number:Length      | Total rainfall (water amount) |
| rainSpot                 | String             | Precipitation distribution around the location |
| rainArea                 | Image              | Color-coded image generated from rainSpot |
| snowFraction             | Number             | Percentage of precipitation falling as snow |
| snowFall                 | Number:Length      | Total snowfall (calculated) |
| cardinalWindDirection    | String             | Name of the wind direction (eg. N, S, E, W, etc.) |
| windDirection            | Number             | Wind direction (in degrees) |
| minWindSpeed             | Number:Speed       | Low wind speed  |
| maxWindSpeed             | Number:Speed       | High wind speed |
| meanWindSpeed            | Number:Speed       | Mean wind speed |
| minSeaLevelPressure      | Number:Pressure    | Low sea level pressure  |
| maxSeaLevelPressure      | Number:Pressure    | High sea level pressure |
| meanSeaLevelPressure     | Number:Pressure    | Mean sea level pressure |
| condition                | String             | A brief description of the forecast weather condition (e.g. 'Overcast') |
|                          |                    | Valid values range from 1 - 17 (see the [meteoblue docs](https://content.meteoblue.com/nl/service-specifications/standards/symbols-and-pictograms#eztoc14635_1_6)) |
| icon                     | Image              | Image used to represent the forecast (calculated) |
|                          |                    | see [Image icons](#image-icons) below
| predictability           | Number             | Estimated certainty of the forecast (percentage) |
| predictabilityClass      | Number             | Range 0-5 (0=very low, 5=very high) |
| precipitationHours       | Number             | Total hours of the day with precipitation |
| humidityGreater90Hours   | Number             | Total hours of the day with relative humidity greater than 90% |


## Image Icons

To show the weather image icons in the UI, the [image files](https://content.meteoblue.com/hu/service-specifications/standards/symbols-and-pictograms) need to be downloaded and installed in the `conf/icons/classic` folder.

In the "Downloads" section at the bottom of the page, download the file named `meteoblue_weather_pictograms_<date>.zip`.

The files to extract from the zip file and install in the folder will be named "iday*.png" or "iday*.svg".


## Full Example

demo.things:

```
Bridge meteoblue:bridge:metBridge "metBridge" [ apiKey="XXXXXXXXXXXX" ] {
	Thing weather A51 "Area 51" [ serviceType="NonCommercial", location="37.23,-115.5,1360", timeZone="America/Los_Angeles", refresh=240 ] {
	}
}
```

demo.items:

```
// ----------------- meteoblue GROUPS ------------------------------------------
Group				weatherDay0			"Today's Weather"														
Group				weatherDay1			"Tomorrow's Weather"													
Group				weatherDay2			"Weather in 2 days"														
Group				weatherDay3			"Weather in 3 days"														
Group				weatherDay4			"Weather in 4 days"														
Group				weatherDay5			"Weather in 5 days"														
Group				weatherDay6			"Weather in 6 days"														


// ----------------- meteoblue ITEMS -------------------------------------------
// ----------------- Today -----------------------------------------------------
DateTime			day1ForecastDate	"Forecast for [%1$tY/%1$tm/%1$td]"	<calendar>		(weatherDay0)		{channel="meteoblue:weather:metBridge:A51:forecastToday#forecastDate"}
String				day1PCode			"Pictocode [%d]"					<iday>			(weatherDay0)		{channel="meteoblue:weather:metBridge:A51:forecastToday#condition"}
String				day1Cond			"Condition [%s]"					<iday>			(weatherDay0)		{channel="meteoblue:weather:metBridge:A51:forecastToday#condition"}
String				day1Icon			"Icon [%s]"											(weatherDay0)		{channel="meteoblue:weather:metBridge:A51:forecastToday#icon"}
Number				day1UV				"UV Index [%d]"										(weatherDay0)		{channel="meteoblue:weather:metBridge:A51:forecastToday#UVIndex"}
Number:Temperature	day1TempL			"Low Temp [%.2f °F]"				<temperature>	(weatherDay0)		{channel="meteoblue:weather:metBridge:A51:forecastToday#minTemperature"}
Number:Temperature	day1TempH			"High Temp [%.2f °F]"				<temperature>	(weatherDay0)		{channel="meteoblue:weather:metBridge:A51:forecastToday#maxTemperature"}
Number				day1HumM			"Mean Humidity [%d %%]"				<humidity>		(weatherDay0)		{channel="meteoblue:weather:metBridge:A51:forecastToday#relativeHumidityMean"}
Number				day1PrecPr			"Prec. Prob. [%d %%]"								(weatherDay0)		{channel="meteoblue:weather:metBridge:A51:forecastToday#precipitationProbability"}
Number:Length		day1Prec			"Total Prec. [%.2f in]"				<rain>			(weatherDay0)		{channel="meteoblue:weather:metBridge:A51:forecastToday#precipitation"}
Number:Length		day1Rain			"Rainfall [%.2f in]"				<rain>			(weatherDay0)		{channel="meteoblue:weather:metBridge:A51:forecastToday#convectivePrecipitation"}
Image				day1RainArea		"Rain area"							<rain>			(weatherDay0)		{channel="meteoblue:weather:metBridge:A51:forecastToday#rainArea"}
Number				day1SnowF			"Snow fraction [%.2f]"				<climate>		(weatherDay0)		{channel="meteoblue:weather:metBridge:A51:forecastToday#snowFraction"}
Number:Length		day1Snow			"Snowfall [%.2f in]"				<rain>			(weatherDay0)		{channel="meteoblue:weather:metBridge:A51:forecastToday#snowFall"}
Number:Pressure		day1PressL			"Low Pressure [%d %unit%]"			<pressure>		(weatherDay0)		{channel="meteoblue:weather:metBridge:A51:forecastToday#minSeaLevelPressure"}
Number:Pressure		day1PressH			"High Pressure [%d %unit%]"			<pressure>		(weatherDay0)		{channel="meteoblue:weather:metBridge:A51:forecastToday#maxSeaLevelPressure"}
Number				day1WindDir			"Wind Direction [%d]"				<wind>			(weatherDay0)		{channel="meteoblue:weather:metBridge:A51:forecastToday#windDirection"}
String				day1CWindDir		"Cardinal Wind Direction [%s]"		<wind>			(weatherDay0)		{channel="meteoblue:weather:metBridge:A51:forecastToday#cardinalWindDirection"}
Number:Speed		day1WindSpL			"Low Wind Speed [%.2f mph]"			<wind>			(weatherDay0)		{channel="meteoblue:weather:metBridge:A51:forecastToday#minWindSpeed"}
Number:Speed		day1WindSpH			"High Wind Speed [%.2f mph]"		<wind>			(weatherDay0)		{channel="meteoblue:weather:metBridge:A51:forecastToday#maxWindSpeed"}


// ----------------- Day1 ------------------------------------------------------
DateTime			day1ForecastDate	"Forecast for [%1$tY/%1$tm/%1$td]"	<calendar>		(weatherDay1)		{channel="meteoblue:weather:metBridge:A51:forecastTomorrow#forecastDate"}
String				day1PCode			"Pictocode [%d]"					<iday>			(weatherDay1)		{channel="meteoblue:weather:metBridge:A51:forecastTomorrow#condition"}
String				day1Cond			"Condition [%s]"					<iday>			(weatherDay1)		{channel="meteoblue:weather:metBridge:A51:forecastTomorrow#condition"}
String				day1Icon			"Icon [%s]"											(weatherDay1)		{channel="meteoblue:weather:metBridge:A51:forecastTomorrow#icon"}
Number				day1UV				"UV Index [%d]"										(weatherDay1)		{channel="meteoblue:weather:metBridge:A51:forecastTomorrow#UVIndex"}
Number:Temperature	day1TempL			"Low Temp [%.2f °F]"				<temperature>	(weatherDay1)		{channel="meteoblue:weather:metBridge:A51:forecastTomorrow#minTemperature"}
Number:Temperature	day1TempH			"High Temp [%.2f °F]"				<temperature>	(weatherDay1)		{channel="meteoblue:weather:metBridge:A51:forecastTomorrow#maxTemperature"}
Number				day1HumM			"Mean Humidity [%d %%]"				<humidity>		(weatherDay1)		{channel="meteoblue:weather:metBridge:A51:forecastTomorrow#relativeHumidityMean"}
Number				day1PrecPr			"Prec. Prob. [%d %%]"								(weatherDay1)		{channel="meteoblue:weather:metBridge:A51:forecastTomorrow#precipitationProbability"}
Number:Length		day1Prec			"Total Prec. [%.2f in]"				<rain>			(weatherDay1)		{channel="meteoblue:weather:metBridge:A51:forecastTomorrow#precipitation"}
Number:Length		day1Rain			"Rainfall [%.2f in]"				<rain>			(weatherDay1)		{channel="meteoblue:weather:metBridge:A51:forecastTomorrow#convectivePrecipitation"}
Image				day1RainArea		"Rain area"							<rain>			(weatherDay1)		{channel="meteoblue:weather:metBridge:A51:forecastTomorrow#rainArea"}
Number				day1SnowF			"Snow fraction [%.2f]"				<climate>		(weatherDay1)		{channel="meteoblue:weather:metBridge:A51:forecastTomorrow#snowFraction"}
Number:Length		day1Snow			"Snowfall [%.2f in]"				<rain>			(weatherDay1)		{channel="meteoblue:weather:metBridge:A51:forecastTomorrow#snowFall"}
Number:Pressure		day1PressL			"Low Pressure [%d %unit%]"			<pressure>		(weatherDay1)		{channel="meteoblue:weather:metBridge:A51:forecastTomorrow#minSeaLevelPressure"}
Number:Pressure		day1PressH			"High Pressure [%d %unit%]"			<pressure>		(weatherDay1)		{channel="meteoblue:weather:metBridge:A51:forecastTomorrow#maxSeaLevelPressure"}
Number				day1WindDir			"Wind Direction [%d]"				<wind>			(weatherDay1)		{channel="meteoblue:weather:metBridge:A51:forecastTomorrow#windDirection"}
String				day1CWindDir		"Cardinal Wind Direction [%s]"		<wind>			(weatherDay1)		{channel="meteoblue:weather:metBridge:A51:forecastTomorrow#cardinalWindDirection"}
Number:Speed		day1WindSpL			"Low Wind Speed [%.2f mph]"			<wind>			(weatherDay1)		{channel="meteoblue:weather:metBridge:A51:forecastTomorrow#minWindSpeed"}
Number:Speed		day1WindSpH			"High Wind Speed [%.2f mph]"		<wind>			(weatherDay1)		{channel="meteoblue:weather:metBridge:A51:forecastTomorrow#maxWindSpeed"}


// ----------------- Day2 ------------------------------------------------------
DateTime			day2ForecastDate	"Forecast for [%1$tY/%1$tm/%1$td]"	<calendar>		(weatherDay2)		{channel="meteoblue:weather:metBridge:A51:forecastDay2#forecastDate"}
String				day2PCode			"Pictocode [%d]"					<iday>			(weatherDay2)		{channel="meteoblue:weather:metBridge:A51:forecastDay2#condition"}
String				day2Cond			"Condition [%s]"					<iday>			(weatherDay2)		{channel="meteoblue:weather:metBridge:A51:forecastDay2#condition"}
String				day2Icon			"Icon [%s]"											(weatherDay2)		{channel="meteoblue:weather:metBridge:A51:forecastDay2#icon"}
Number				day2UV				"UV Index [%d]"										(weatherDay2)		{channel="meteoblue:weather:metBridge:A51:forecastDay2#UVIndex"}
Number:Temperature	day2TempL			"Low Temp [%.2f °F]"				<temperature>	(weatherDay2)		{channel="meteoblue:weather:metBridge:A51:forecastDay2#minTemperature"}
Number:Temperature	day2TempH			"High Temp [%.2f °F]"				<temperature>	(weatherDay2)		{channel="meteoblue:weather:metBridge:A51:forecastDay2#maxTemperature"}
Number				day2HumM			"Mean Humidity [%d %%]"				<humidity>		(weatherDay2)		{channel="meteoblue:weather:metBridge:A51:forecastDay2#relativeHumidityMean"}
Number				day2PrecPr			"Prec. Prob. [%d %%]"								(weatherDay2)		{channel="meteoblue:weather:metBridge:A51:forecastDay2#precipitationProbability"}
Number:Length		day2Prec			"Total Prec. [%.2f in]"				<rain>			(weatherDay2)		{channel="meteoblue:weather:metBridge:A51:forecastDay2#precipitation"}
Number:Length		day2Rain			"Rainfall [%.2f in]"				<rain>			(weatherDay2)		{channel="meteoblue:weather:metBridge:A51:forecastDay2#convectivePrecipitation"}
Image				day2RainArea		"Rain area"							<rain>			(weatherDay2)		{channel="meteoblue:weather:metBridge:A51:forecastDay2#rainArea"}
Number				day2SnowF			"Snow fraction [%.2f]"				<climate>		(weatherDay2)		{channel="meteoblue:weather:metBridge:A51:forecastDay2#snowFraction"}
Number:Length		day2Snow			"Snowfall [%.2f in]"				<rain>			(weatherDay2)		{channel="meteoblue:weather:metBridge:A51:forecastDay2#snowFall"}
Number:Pressure		day2PressL			"Low Pressure [%d %unit%]"			<pressure>		(weatherDay2)		{channel="meteoblue:weather:metBridge:A51:forecastDay2#minSeaLevelPressure"}
Number:Pressure		day2PressH			"High Pressure [%d %unit%]"			<pressure>		(weatherDay2)		{channel="meteoblue:weather:metBridge:A51:forecastDay2#maxSeaLevelPressure"}
Number				day2WindDir			"Wind Direction [%d]"				<wind>			(weatherDay2)		{channel="meteoblue:weather:metBridge:A51:forecastDay2#windDirection"}
String				day2CWindDir		"Cardinal Wind Direction [%s]"		<wind>			(weatherDay2)		{channel="meteoblue:weather:metBridge:A51:forecastDay2#cardinalWindDirection"}
Number:Speed		day2WindSpL			"Low Wind Speed [%.2f mph]"			<wind>			(weatherDay2)		{channel="meteoblue:weather:metBridge:A51:forecastDay2#minWindSpeed"}
Number:Speed		day2WindSpH			"High Wind Speed [%.2f mph]"		<wind>			(weatherDay2)		{channel="meteoblue:weather:metBridge:A51:forecastDay2#maxWindSpeed"}


// ----------------- Day3 ------------------------------------------------------
DateTime			day3ForecastDate	"Forecast for [%1$tY/%1$tm/%1$td]"	<calendar>		(weatherDay3)		{channel="meteoblue:weather:metBridge:A51:forecastDay3#forecastDate"}
String				day3PCode			"Pictocode [%d]"					<iday>			(weatherDay3)		{channel="meteoblue:weather:metBridge:A51:forecastDay3#condition"}
String				day3Cond			"Condition [%s]"					<iday>			(weatherDay3)		{channel="meteoblue:weather:metBridge:A51:forecastDay3#condition"}
String				day3Icon			"Icon [%s]"											(weatherDay3)		{channel="meteoblue:weather:metBridge:A51:forecastDay3#icon"}
Number				day3UV				"UV Index [%d]"										(weatherDay3)		{channel="meteoblue:weather:metBridge:A51:forecastDay3#UVIndex"}
Number:Temperature	day3TempL			"Low Temp [%.2f °F]"				<temperature>	(weatherDay3)		{channel="meteoblue:weather:metBridge:A51:forecastDay3#minTemperature"}
Number:Temperature	day3TempH			"High Temp [%.2f °F]"				<temperature>	(weatherDay3)		{channel="meteoblue:weather:metBridge:A51:forecastDay3#maxTemperature"}
Number				day3HumM			"Mean Humidity [%d %%]"				<humidity>		(weatherDay3)		{channel="meteoblue:weather:metBridge:A51:forecastDay3#relativeHumidityMean"}
Number				day3PrecPr			"Prec. Prob. [%d %%]"								(weatherDay3)		{channel="meteoblue:weather:metBridge:A51:forecastDay3#precipitationProbability"}
Number:Length		day3Prec			"Total Prec. [%.2f in]"				<rain>			(weatherDay3)		{channel="meteoblue:weather:metBridge:A51:forecastDay3#precipitation"}
Number:Length		day3Rain			"Rainfall [%.2f in]"				<rain>			(weatherDay3)		{channel="meteoblue:weather:metBridge:A51:forecastDay3#convectivePrecipitation"}
Image				day3RainArea		"Rain area"							<rain>			(weatherDay3)		{channel="meteoblue:weather:metBridge:A51:forecastDay3#rainArea"}
Number				day3SnowF			"Snow fraction [%.2f]"				<climate>		(weatherDay3)		{channel="meteoblue:weather:metBridge:A51:forecastDay3#snowFraction"}
Number:Length		day3Snow			"Snowfall [%.2f in]"				<rain>			(weatherDay3)		{channel="meteoblue:weather:metBridge:A51:forecastDay3#snowFall"}
Number:Pressure		day3PressL			"Low Pressure [%d %unit%]"			<pressure>		(weatherDay3)		{channel="meteoblue:weather:metBridge:A51:forecastDay3#minSeaLevelPressure"}
Number:Pressure		day3PressH			"High Pressure [%d %unit%]"			<pressure>		(weatherDay3)		{channel="meteoblue:weather:metBridge:A51:forecastDay3#maxSeaLevelPressure"}
Number				day3WindDir			"Wind Direction [%d]"				<wind>			(weatherDay3)		{channel="meteoblue:weather:metBridge:A51:forecastDay3#windDirection"}
String				day3CWindDir		"Cardinal Wind Direction [%s]"		<wind>			(weatherDay3)		{channel="meteoblue:weather:metBridge:A51:forecastDay3#cardinalWindDirection"}
Number:Speed		day3WindSpL			"Low Wind Speed [%.2f mph]"			<wind>			(weatherDay3)		{channel="meteoblue:weather:metBridge:A51:forecastDay3#minWindSpeed"}
Number:Speed		day3WindSpH			"High Wind Speed [%.2f mph]"		<wind>			(weatherDay3)		{channel="meteoblue:weather:metBridge:A51:forecastDay3#maxWindSpeed"}


// ----------------- Day4 ------------------------------------------------------
DateTime			day4ForecastDate	"Forecast for [%1$tY/%1$tm/%1$td]"	<calendar>		(weatherDay4)		{channel="meteoblue:weather:metBridge:A51:forecastDay4#forecastDate"}
String				day4PCode			"Pictocode [%d]"					<iday>			(weatherDay4)		{channel="meteoblue:weather:metBridge:A51:forecastDay4#condition"}
String				day4Cond			"Condition [%s]"					<iday>			(weatherDay4)		{channel="meteoblue:weather:metBridge:A51:forecastDay4#condition"}
String				day4Icon			"Icon [%s]"											(weatherDay4)		{channel="meteoblue:weather:metBridge:A51:forecastDay4#icon"}
Number				day4UV				"UV Index [%d]"										(weatherDay4)		{channel="meteoblue:weather:metBridge:A51:forecastDay4#UVIndex"}
Number:Temperature	day4TempL			"Low Temp [%.2f °F]"				<temperature>	(weatherDay4)		{channel="meteoblue:weather:metBridge:A51:forecastDay4#minTemperature"}
Number:Temperature	day4TempH			"High Temp [%.2f °F]"				<temperature>	(weatherDay4)		{channel="meteoblue:weather:metBridge:A51:forecastDay4#maxTemperature"}
Number				day4HumM			"Mean Humidity [%d %%]"				<humidity>		(weatherDay4)		{channel="meteoblue:weather:metBridge:A51:forecastDay4#relativeHumidityMean"}
Number				day4PrecPr			"Prec. Prob. [%d %%]"								(weatherDay4)		{channel="meteoblue:weather:metBridge:A51:forecastDay4#precipitationProbability"}
Number:Length		day4Prec			"Total Prec. [%.2f in]"				<rain>			(weatherDay4)		{channel="meteoblue:weather:metBridge:A51:forecastDay4#precipitation"}
Number:Length		day4Rain			"Rainfall [%.2f in]"				<rain>			(weatherDay4)		{channel="meteoblue:weather:metBridge:A51:forecastDay4#convectivePrecipitation"}
Image				day4RainArea		"Rain area"							<rain>			(weatherDay4)		{channel="meteoblue:weather:metBridge:A51:forecastDay4#rainArea"}
Number				day4SnowF			"Snow fraction [%.2f]"				<climate>		(weatherDay4)		{channel="meteoblue:weather:metBridge:A51:forecastDay4#snowFraction"}
Number:Length		day4Snow			"Snowfall [%.2f in]"				<rain>			(weatherDay4)		{channel="meteoblue:weather:metBridge:A51:forecastDay4#snowFall"}
Number:Pressure		day4PressL			"Low Pressure [%d %unit%]"			<pressure>		(weatherDay4)		{channel="meteoblue:weather:metBridge:A51:forecastDay4#minSeaLevelPressure"}
Number:Pressure		day4PressH			"High Pressure [%d %unit%]"			<pressure>		(weatherDay4)		{channel="meteoblue:weather:metBridge:A51:forecastDay4#maxSeaLevelPressure"}
Number				day4WindDir			"Wind Direction [%d]"				<wind>			(weatherDay4)		{channel="meteoblue:weather:metBridge:A51:forecastDay4#windDirection"}
String				day4CWindDir		"Cardinal Wind Direction [%s]"		<wind>			(weatherDay4)		{channel="meteoblue:weather:metBridge:A51:forecastDay4#cardinalWindDirection"}
Number:Speed		day4WindSpL			"Low Wind Speed [%.2f mph]"			<wind>			(weatherDay4)		{channel="meteoblue:weather:metBridge:A51:forecastDay4#minWindSpeed"}
Number:Speed		day4WindSpH			"High Wind Speed [%.2f mph]"		<wind>			(weatherDay4)		{channel="meteoblue:weather:metBridge:A51:forecastDay4#maxWindSpeed"}


// ----------------- Day5 ------------------------------------------------------
DateTime			day5ForecastDate	"Forecast for [%1$tY/%1$tm/%1$td]"	<calendar>		(weatherDay5)		{channel="meteoblue:weather:metBridge:A51:forecastDay5#forecastDate"}
String				day5PCode			"Pictocode [%d]"					<iday>			(weatherDay5)		{channel="meteoblue:weather:metBridge:A51:forecastDay5#condition"}
String				day5Cond			"Condition [%s]"					<iday>			(weatherDay5)		{channel="meteoblue:weather:metBridge:A51:forecastDay5#condition"}
String				day5Icon			"Icon [%s]"											(weatherDay5)		{channel="meteoblue:weather:metBridge:A51:forecastDay5#icon"}
Number				day5UV				"UV Index [%d]"										(weatherDay5)		{channel="meteoblue:weather:metBridge:A51:forecastDay5#UVIndex"}
Number:Temperature	day5TempL			"Low Temp [%.2f °F]"				<temperature>	(weatherDay5)		{channel="meteoblue:weather:metBridge:A51:forecastDay5#minTemperature"}
Number:Temperature	day5TempH			"High Temp [%.2f °F]"				<temperature>	(weatherDay5)		{channel="meteoblue:weather:metBridge:A51:forecastDay5#maxTemperature"}
Number				day5HumM			"Mean Humidity [%d %%]"				<humidity>		(weatherDay5)		{channel="meteoblue:weather:metBridge:A51:forecastDay5#relativeHumidityMean"}
Number				day5PrecPr			"Prec. Prob. [%d %%]"								(weatherDay5)		{channel="meteoblue:weather:metBridge:A51:forecastDay5#precipitationProbability"}
Number:Length		day5Prec			"Total Prec. [%.2f in]"				<rain>			(weatherDay5)		{channel="meteoblue:weather:metBridge:A51:forecastDay5#precipitation"}
Number:Length		day5Rain			"Rainfall [%.2f in]"				<rain>			(weatherDay5)		{channel="meteoblue:weather:metBridge:A51:forecastDay5#convectivePrecipitation"}
Image				day5RainArea		"Rain area"							<rain>			(weatherDay5)		{channel="meteoblue:weather:metBridge:A51:forecastDay5#rainArea"}
Number				day5SnowF			"Snow fraction [%.2f]"				<climate>		(weatherDay5)		{channel="meteoblue:weather:metBridge:A51:forecastDay5#snowFraction"}
Number:Length		day5Snow			"Snowfall [%.2f in]"				<rain>			(weatherDay5)		{channel="meteoblue:weather:metBridge:A51:forecastDay5#snowFall"}
Number:Pressure		day5PressL			"Low Pressure [%d %unit%]"			<pressure>		(weatherDay5)		{channel="meteoblue:weather:metBridge:A51:forecastDay5#minSeaLevelPressure"}
Number:Pressure		day5PressH			"High Pressure [%d %unit%]"			<pressure>		(weatherDay5)		{channel="meteoblue:weather:metBridge:A51:forecastDay5#maxSeaLevelPressure"}
Number				day5WindDir			"Wind Direction [%d]"				<wind>			(weatherDay5)		{channel="meteoblue:weather:metBridge:A51:forecastDay5#windDirection"}
String				day5CWindDir		"Cardinal Wind Direction [%s]"		<wind>			(weatherDay5)		{channel="meteoblue:weather:metBridge:A51:forecastDay5#cardinalWindDirection"}
Number:Speed		day5WindSpL			"Low Wind Speed [%.2f mph]"			<wind>			(weatherDay5)		{channel="meteoblue:weather:metBridge:A51:forecastDay5#minWindSpeed"}
Number:Speed		day5WindSpH			"High Wind Speed [%.2f mph]"		<wind>			(weatherDay5)		{channel="meteoblue:weather:metBridge:A51:forecastDay5#maxWindSpeed"}


// ----------------- Day6 ------------------------------------------------------
DateTime			day6ForecastDate	"Forecast for [%1$tY/%1$tm/%1$td]"	<calendar>		(weatherDay6)		{channel="meteoblue:weather:metBridge:A51:forecastDay6#forecastDate"}
String				day6PCode			"Pictocode [%d]"					<iday>			(weatherDay6)		{channel="meteoblue:weather:metBridge:A51:forecastDay6#condition"}
String				day6Cond			"Condition [%s]"					<iday>			(weatherDay6)		{channel="meteoblue:weather:metBridge:A51:forecastDay6#condition"}
String				day6Icon			"Icon [%s]"											(weatherDay6)		{channel="meteoblue:weather:metBridge:A51:forecastDay6#icon"}
Number				day6UV				"UV Index [%d]"										(weatherDay6)		{channel="meteoblue:weather:metBridge:A51:forecastDay6#UVIndex"}
Number:Temperature	day6TempL			"Low Temp [%.2f °F]"				<temperature>	(weatherDay6)		{channel="meteoblue:weather:metBridge:A51:forecastDay6#minTemperature"}
Number:Temperature	day6TempH			"High Temp [%.2f °F]"				<temperature>	(weatherDay6)		{channel="meteoblue:weather:metBridge:A51:forecastDay6#maxTemperature"}
Number				day6HumM			"Mean Humidity [%d %%]"				<humidity>		(weatherDay6)		{channel="meteoblue:weather:metBridge:A51:forecastDay6#relativeHumidityMean"}
Number				day6PrecPr			"Prec. Prob. [%d %%]"								(weatherDay6)		{channel="meteoblue:weather:metBridge:A51:forecastDay6#precipitationProbability"}
Number:Length		day6Prec			"Total Prec. [%.2f in]"				<rain>			(weatherDay6)		{channel="meteoblue:weather:metBridge:A51:forecastDay6#precipitation"}
Number:Length		day6Rain			"Rainfall [%.2f in]"				<rain>			(weatherDay6)		{channel="meteoblue:weather:metBridge:A51:forecastDay6#convectivePrecipitation"}
Image				day6RainArea		"Rain area"							<rain>			(weatherDay6)		{channel="meteoblue:weather:metBridge:A51:forecastDay6#rainArea"}
Number				day6SnowF			"Snow fraction [%.2f]"				<climate>		(weatherDay6)		{channel="meteoblue:weather:metBridge:A51:forecastDay6#snowFraction"}
Number:Length		day6Snow			"Snowfall [%.2f in]"				<rain>			(weatherDay6)		{channel="meteoblue:weather:metBridge:A51:forecastDay6#snowFall"}
Number:Pressure		day6PressL			"Low Pressure [%d %unit%]"			<pressure>		(weatherDay6)		{channel="meteoblue:weather:metBridge:A51:forecastDay6#minSeaLevelPressure"}
Number:Pressure		day6PressH			"High Pressure [%d %unit%]"			<pressure>		(weatherDay6)		{channel="meteoblue:weather:metBridge:A51:forecastDay6#maxSeaLevelPressure"}
Number				day6WindDir			"Wind Direction [%d]"				<wind>			(weatherDay6)		{channel="meteoblue:weather:metBridge:A51:forecastDay6#windDirection"}
String				day6CWindDir		"Cardinal Wind Direction [%s]"		<wind>			(weatherDay6)		{channel="meteoblue:weather:metBridge:A51:forecastDay6#cardinalWindDirection"}
Number:Speed		day6WindSpL			"Low Wind Speed [%.2f mph]"			<wind>			(weatherDay6)		{channel="meteoblue:weather:metBridge:A51:forecastDay6#minWindSpeed"}
Number:Speed		day6WindSpH			"High Wind Speed [%.2f mph]"		<wind>			(weatherDay6)		{channel="meteoblue:weather:metBridge:A51:forecastDay6#maxWindSpeed"}
```

demo.sitemap:

````
sitemap weather label="Weather"
{
  Frame label="Weather" {
    Group item=weatherDay0
    Group item=weatherDay1
    Group item=weatherDay2
    Group item=weatherDay3
    Group item=weatherDay4
    Group item=weatherDay5
    Group item=weatherDay6
  }
}
````
