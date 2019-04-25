# SMHI Binding

This is a binding for fetching weather data from SMHI (Swedish Meteorological and Hydrological Institute) supporting [SMHI API pmp3g](https://opendata.smhi.se/apidocs/metfcst/).

## Supported Things

This binding supports the following thing types:

* SMHI Weather and Forecast

## Discovery

You have to manually add this thing based on that it just cover Northern part of Europe

## Thing Configuration

You will have to configure your desired location in format 60.123456,11.123456. 
Latitude and longitude for your location can be found using [LatLong.Net](https://www.latlong.net/).
You can either configure your location in Paper UI or by using an own .things file, see below for an example!
Latitude must be between 52.50 and 70.75.
Longitude must be between 2.25 and 38.00. 
You can also configure the refresh rate in minutes.

## Channels


Channel             |SMHI Parameter| Unit    | Level Type |Level (m) |Description                             | Value range 
 :---               | :---         | :---    | :---       | :---     | :---                                   | :--- 
time-stamp          |              |date/time|            |          |Forecast time                           |
condition           |              |text     |            |          |Weather Condition                       |
condition-id        |wsymb2        |code     |hl          |0         |Weather symbol                          |Integer, 1-27
temperature         |t             |C        |hl          |2         |Air temperature                         |Decimal number, one decimal
temperature-min     |              |C        |hl          |2         |Air temperature Min                     |Decimal number, one decimal
temperature-max     |              |C        |hl          |2         |Air temperature Max                     |Decimal number, one decimal
pressure            |msl           |hpa      |hmsl        |0         |Air pressure                            |Decimal number, one decimal 
humidity            |r             |%        |hl          |2         |Relative humidity                       |Integer, 0-100
wind-speed          |ws            |m/s      |hl          |10        |Wind speed  Decimal                     |number, one decimal
wind-direction      |wd            |degree   |hl          |10        |Wind direction                          |Integer
wind-gust           |gust          |m/s      |hl          |10        |Wind gust speed                         |Decimal number, one decimal
thunderstorm        |tstm          |%        |hl          |0         |Thunder probability                     |Integer, 0-100
cloud-cover         |tcc_mean      |octas    |hl          |0         |Mean value of total cloud cover         |Integer, 0-8
cloud-cover-low     |lcc_mean      |octas    |hl          |0         |Mean value of low level cloud cover     |Integer, 0-8
cloud-cover-medium  |mcc_mean      |octas    |hl          |0         |Mean value of medium level cloud cover  |Integer, 0-8
cloud-cover-high    |hcc_mean      |octas    |hl          |0         |Mean value of high level cloud cover    |Integer, 0-8
visibility          |vis           |km       |hl          |2         |Horizontal visibility                   |Decimal number, one decimal
precipitation-cat   |              |text     |            |          |Precipitation Category                  |
precipitation-cat-id|pcat          |category |hl          |0         |Precipitation category                  |Integer, 0-6
precipitation-mean  |pmean         |mm/h     |hl          |0         |Mean precipitation intensity            |Decimal number, one decimal
precipitation-median|pmedian       |mm/h     |hl          |0         |Median precipitation intensity          |Decimal number, one decimal
precipitation-min   |pmin          |mm/h     |hl          |0         |Minimum precipitation intensity         |Decimal number, one decimal
precipitation-max   |pmax          |mm/h     |hl          |0         |Maximum precipitation intensity         |Decimal number, one decimal
precipitation-frozen|spp           |%        |hl          |0         |Percent of precipitation in frozen form |Integer, -9 or 0-100

You can get more information at [SMHI API pmp3g Parameters](https://opendata.smhi.se/apidocs/metfcst/parameters.html).

## Full Example

smhi.things
```
smhi:weather-and-forecast:gavle [ weatherlocation="60.123456,17.123456", refresh=60 ]
```
smhi.items
```
Group gSmhi (All)
DateTime SMHI_ObsTime   			"Prognos tid [%1$tY-%1$tm-%1$td %1$tH:%1$tM]" 			<clock>	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:current#time-stamp"}
String SMHI_Condition   			"Vädret är [%s]" 					<sun_clouds>	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:current#condition"}
Number SMHI_Condition_No   			"Vädrerkod [%s]" 					<sun_clouds>	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:current#condition-id"}
Number SMHI_Temperature				"Temperatur [%.1f °C]"				<temperature>  	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:current#temperature"}
Number SMHI_Temperature_Min			"Temperatur Min [%.1f °C]"			<temperature>  	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:current#temperature-min"}
Number SMHI_Temperature_Max			"Temperatur Max [%.1f °C]"			<temperature>  	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:current#temperature-max"}
Number SMHI_Pressure    			"Lufttryck  [%.1f mb]"				<pressure>   	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:current#pressure"}
Number SMHI_Humidity				"Luftfuktighet [%.1f %%]"			<humidity>  	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:current#humidity"}
Number SMHI_WindSpeed				"Vindhastighet [%.1f m/s]"			<wind>  		(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:current#wind-speed"}
Number SMHI_WindDirection			"Vindriktning [%.0f °]"				<wind>  		(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:current#wind-direction"}
Number SMHI_WindGustSpeed			"Byvindar [%.1f m/s]"				<wind>  		(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:current#wind-gust"}
Number SMHI_ThunderProbability		"Åska sanolikhet [%.0f %%]"			<sun_clouds>  	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:current#thunderstorm"}
Number SMHI_CloudCover				"Molntäckning [%.0f]"				<sun_clouds>  	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:current#cloud-cover"}
Number SMHI_CloudCoverLow			"Molntäckning Låg [%.0f]"			<sun_clouds>  	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:current#cloud-cover-low"}
Number SMHI_CloudCoverMedium		"MolnTäckning Medel [%.0f]"			<sun_clouds>  	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:current#cloud-cover-medium"}
Number SMHI_CloudCoverHigh			"MolnTäckning Hög [%.0f]"			<sun_clouds>  	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:current#cloud-cover-high"}
Number SMHI_Visibility				"Sikt [%.1f km]"					<temperature>  	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:current#visibility"}
String SMHI_PrecipitationCat		"Nederbörd Kategori [%s]"			<rain>  		(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:current#precipitation-cat"}
Number SMHI_PrecipitationCat_No   	"Nederbörd Kategori Kod [%s]"		<rain>	        (gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:current#precipitation-cat-id"}
Number SMHI_PrecipitationMean		"Nederbörd Medel [%.1f mm/h]"		<rain>  		(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:current#precipitation-mean"}
Number SMHI_PrecipitationMedian		"Nederbörd Median [%.1f mm/h]"		<rain>  		(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:current#precipitation-median"}
Number SMHI_PrecipitationMin		"Nederbörd Min [%.1f mm/h]"			<rain>   		(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:current#precipitation-min"}
Number SMHI_PrecipitationMax		"Nederbörd Max [%.1f mm/h]"			<rain>		   	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:current#precipitation-max"}
Number SMHI_PrecipitationFrozen		"Frysen nederbörd [%.0f %%]"		<climate>  		(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:current#precipitation-frozen"}
```
```
DateTime SMHI_ObsTime_1   			"Prognos tid P1 [%1$tY-%1$tm-%1$td %1$tH:%1$tM]" <clock> (gSmhi, gPCR)  { channel="smhi:weather-and-forecast:gavle:forecastTomorrow#time-stamp"}
String SMHI_Condition_1   			"Vädret imorgon är [%s]" 			    <sun_clouds>	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastTomorrow#condition"}
Number SMHI_Condition_No_1   		"Vädrerkod P1 [%s]" 					<sun_clouds>	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastTomorrow#condition-id"}
Number SMHI_Temperature_1			"Temperatur P1 [%.1f °C]"				<temperature>  	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastTomorrow#temperature"}
Number SMHI_Temperature_Min_1		"Temperatur Min P1 [%.1f °C]"			<temperature>  	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastTomorrow#temperature-min"}
Number SMHI_Temperature_Max_1		"Temperatur Max P1 [%.1f °C]"			<temperature>  	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastTomorrow#temperature-max"}
Number SMHI_Pressure_1    			"Lufttryck  P1 [%.1f mb]"				<pressure>   	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastTomorrow#pressure"}
Number SMHI_Humidity_1				"Luftfuktighet P1 [%.1f %%]"			<humidity>  	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastTomorrow#humidity"}
Number SMHI_WindSpeed_1				"Vindhastighet P1 [%.1f m/s]"			<wind>  		(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastTomorrow#wind-speed"}
Number SMHI_WindDirection_1			"Vindriktning P1 [%.0f °]"				<wind>  		(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastTomorrow#wind-direction"}
Number SMHI_WindGustSpeed_1			"Byvindar P1 [%.1f m/s]"				<wind>  		(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastTomorrow#wind-gust"}
Number SMHI_ThunderProbability_1	"Åska sanolikhet P1 [%.0f %%]"			<sun_clouds>  	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastTomorrow#thunderstorm"}
Number SMHI_CloudCover_1			"Molntäckning P1 [%.0f]"				<sun_clouds>  	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastTomorrow#cloud-cover"}
Number SMHI_CloudCoverLow_1			"Molntäckning Låg P1 [%.0f]"			<sun_clouds>  	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastTomorrow#cloud-cover-low"}
Number SMHI_CloudCoverMedium_1		"MolnTäckning Medel P1 [%.0f]"			<sun_clouds>  	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastTomorrow#cloud-cover-medium"}
Number SMHI_CloudCoverHigh_1		"MolnTäckning Hög P1 [%.0f]"			<sun_clouds>  	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastTomorrow#cloud-cover-high"}
Number SMHI_Visibility_1			"Sikt P1 [%.1f km]"					    <temperature>  	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastTomorrow#visibility"}
String SMHI_PrecipitationCat_1		"Nederbörd Kategori P1 [%s]"			<rain>  		(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastTomorrow#precipitation-cat"}
Number SMHI_PrecipitationCat_No_1 	"Nederbörd Kategori Kod P1 [%s]"		<rain>	        (gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastTomorrow#precipitation-cat-id"}
Number SMHI_PrecipitationMean_1		"Nederbörd Medel P1 [%.1f mm/h]"		<rain>  		(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastTomorrow#precipitation-mean"}
Number SMHI_PrecipitationMedian_1	"Nederbörd Median P1 [%.1f mm/h]"		<rain>  		(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastTomorrow#precipitation-median"}
Number SMHI_PrecipitationMin_1		"Nederbörd Min P1 [%.1f mm/h]"			<rain>   		(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastTomorrow#precipitation-min"}
Number SMHI_PrecipitationMax_1		"Nederbörd Max P1 [%.1f mm/h]"			<rain>		   	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastTomorrow#precipitation-max"}
Number SMHI_PrecipitationFrozen_1	"Frysen nederbörd P1 [%.0f %%]"		    <climate>  		(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastTomorrow#precipitation-frozen"}
```
```
DateTime SMHI_ObsTime_2   			"Prognos tid P2 [%1$tY-%1$tm-%1$td %1$tH:%1$tM]" <clock> (gSmhi, gPCR)  { channel="smhi:weather-and-forecast:gavle:forecastDay2#time-stamp"}
String SMHI_Condition_2   			"Vädret Dag 2 är [%s]" 			    <sun_clouds>	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay2#condition"}
Number SMHI_Condition_No_2   		"Vädrerkod P2 [%s]" 					<sun_clouds>	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay2#condition-id"}
Number SMHI_Temperature_2			"Temperatur P2 [%.1f °C]"				<temperature>  	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay2#temperature"}
Number SMHI_Temperature_Min_2		"Temperatur Min P2 [%.1f °C]"			<temperature>  	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay2#temperature-min"}
Number SMHI_Temperature_Max_2		"Temperatur Max P2 [%.1f °C]"			<temperature>  	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay2#temperature-max"}
Number SMHI_Pressure_2    			"Lufttryck  P2 [%.1f mb]"				<pressure>   	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay2#pressure"}
Number SMHI_Humidity_2				"Luftfuktighet P2 [%.1f %%]"			<humidity>  	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay2#humidity"}
Number SMHI_WindSpeed_2				"Vindhastighet P2 [%.1f m/s]"			<wind>  		(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay2#wind-speed"}
Number SMHI_WindDirection_2			"Vindriktning P2 [%.0f °]"				<wind>  		(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay2#wind-direction"}
Number SMHI_WindGustSpeed_2			"Byvindar P2 [%.1f m/s]"				<wind>  		(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay2#wind-gust"}
Number SMHI_ThunderProbability_2	"Åska sanolikhet P2 [%.0f %%]"			<sun_clouds>  	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay2#thunderstorm"}
Number SMHI_CloudCover_2			"Molntäckning P2 [%.0f]"				<sun_clouds>  	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay2#cloud-cover"}
Number SMHI_CloudCoverLow_2			"Molntäckning Låg P2 [%.0f]"			<sun_clouds>  	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay2#cloud-cover-low"}
Number SMHI_CloudCoverMedium_2		"MolnTäckning Medel P2 [%.0f]"			<sun_clouds>  	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay2#cloud-cover-medium"}
Number SMHI_CloudCoverHigh_2		"MolnTäckning Hög P2 [%.0f]"			<sun_clouds>  	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay2#cloud-cover-high"}
Number SMHI_Visibility_2			"Sikt P2 [%.1f km]"					    <temperature>  	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay2#visibility"}
String SMHI_PrecipitationCat_2		"Nederbörd Kategori P2 [%s]"			<rain>  		(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay2#precipitation-cat"}
Number SMHI_PrecipitationCat_No_2   	"Nederbörd Kategori Kod P2 [%s]"	<rain>	        (gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay2#precipitation-cat-id"}
Number SMHI_PrecipitationMean_2		"Nederbörd Medel P2 [%.1f mm/h]"		<rain>  		(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay2#precipitation-mean"}
Number SMHI_PrecipitationMedian_2	"Nederbörd Median P2 [%.1f mm/h]"		<rain>  		(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay2#precipitation-median"}
Number SMHI_PrecipitationMin_2		"Nederbörd Min P2 [%.1f mm/h]"			<rain>   		(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay2#precipitation-min"}
Number SMHI_PrecipitationMax_2		"Nederbörd Max P2 [%.1f mm/h]"			<rain>		   	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay2#precipitation-max"}
Number SMHI_PrecipitationFrozen_2	"Frysen nederbörd P2 [%.0f %%]"		    <climate>  		(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay2#precipitation-frozen"}
```
```
DateTime SMHI_ObsTime_3   			"Prognos tid P3 [%1$tY-%1$tm-%1$td %1$tH:%1$tM]" <clock> (gSmhi, gPCR)  { channel="smhi:weather-and-forecast:gavle:forecastDay3#time-stamp"}
String SMHI_Condition_3   			"Vädret Dag 3 är [%s]" 			        <sun_clouds>	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay3#condition"}
Number SMHI_Condition_No_3   		"Vädrerkod P3 [%s]" 					<sun_clouds>	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay3#condition-id"}
Number SMHI_Temperature_3			"Temperatur P3 [%.1f °C]"				<temperature>  	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay3#temperature"}
Number SMHI_Temperature_Min_3		"Temperatur Min P3 [%.1f °C]"			<temperature>  	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay3#temperature-min"}
Number SMHI_Temperature_Max_3		"Temperatur Max P3 [%.1f °C]"			<temperature>  	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay3#temperature-max"}
Number SMHI_Pressure_3    			"Lufttryck  P3 [%.1f mb]"				<pressure>   	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay3#pressure"}
Number SMHI_Humidity_3				"Luftfuktighet P3 [%.1f %%]"			<humidity>  	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay3#humidity"}
Number SMHI_WindSpeed_3				"Vindhastighet P3 [%.1f m/s]"			<wind>  		(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay3#wind-speed"}
Number SMHI_WindDirection_3			"Vindriktning P3 [%.0f °]"				<wind>  		(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay3#wind-direction"}
Number SMHI_WindGustSpeed_3			"Byvindar P3 [%.1f m/s]"				<wind>  		(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay3#wind-gust"}
Number SMHI_ThunderProbability_3	"Åska sanolikhet P3 [%.0f %%]"			<sun_clouds>  	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay3#thunderstorm"}
Number SMHI_CloudCover_3			"Molntäckning P3 [%.0f]"				<sun_clouds>  	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay3#cloud-cover"}
Number SMHI_CloudCoverLow_3			"Molntäckning Låg P3 [%.0f]"			<sun_clouds>  	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay3#cloud-cover-low"}
Number SMHI_CloudCoverMedium_3		"MolnTäckning Medel P3 [%.0f]"			<sun_clouds>  	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay3#cloud-cover-medium"}
Number SMHI_CloudCoverHigh_3		"MolnTäckning Hög P3 [%.0f]"			<sun_clouds>  	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay3#cloud-cover-high"}
Number SMHI_Visibility_3			"Sikt P3 [%.1f km]"					    <temperature>  	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay3#visibility"}
String SMHI_PrecipitationCat_3		"Nederbörd Kategori P3 [%s]"			<rain>  		(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay3#precipitation-cat"}
Number SMHI_PrecipitationCat_No_3  	"Nederbörd Kategori Kod P3 [%s]"		<rain>	        (gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay3#precipitation-cat-id"}
Number SMHI_PrecipitationMean_3		"Nederbörd Medel P3 [%.1f mm/h]"		<rain>  		(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay3#precipitation-mean"}
Number SMHI_PrecipitationMedian_3	"Nederbörd Median P3 [%.1f mm/h]"		<rain>  		(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay3#precipitation-median"}
Number SMHI_PrecipitationMin_3		"Nederbörd Min P3 [%.1f mm/h]"			<rain>   		(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay3#precipitation-min"}
Number SMHI_PrecipitationMax_3		"Nederbörd Max P3 [%.1f mm/h]"			<rain>		   	(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay3#precipitation-max"}
Number SMHI_PrecipitationFrozen_3	"Frysen nederbörd P3 [%.0f %%]"		    <climate>  		(gSmhi, gPCR)   { channel="smhi:weather-and-forecast:gavle:forecastDay3#precipitation-frozen"}
```
## Remark
Note that current temperature min/max is from actual time to end of day so this have to be adjusted with historic values e.q.
```
rule "SMHI Min/Max Current "
when
    Item SMHI_Temperature_Min changed or
    Item SMHI_Temperature_Max changed
then
    if (SMHI_Temperature_Min.state > Weather_Temp_Min.state ){
        SMHI_Temperature_Min.postUpdate( Weather_Temp_Min.state)
        }

    if (SMHI_Temperature_Max.state < Weather_Temp_Max.state ){
        SMHI_Temperature_Max.postUpdate( Weather_Temp_Max.state)
        }
end
```
