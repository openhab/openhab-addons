# org.openhab.binding.hp1000

HP100X/WH260X weather stations binding for OpenHab witch supports the personal weather station upload protocol.

# Thing Configuration

Automatic discovery is not supported. You have to do a manual configuration.

## Configuration Parameters

### Hostname or IP

It's important do add the correct weather station hostname or ip adress. You can bind mulitple weather stations and they all use the same webhook endpoint {OpenHap-IP-Adress}:{OpenHap-Port}/weatherstation/updateweatherstation.php. Depends on the request ip source the relevant thing will be updated.

# Weather Station Configuration

Open the weather station web interface and go to the section Weather Network.

## Weather Station Settings

Remote Server: Customized

Server IP/Hostname: {OpenHap-IP-Adress}

Server Port: {OpenHap-Port}

Server Type: PHP

Station ID: {ignored}

Password: {ignored}
  
# Channels

```
Number   WeatherStation_OutdoorTemperature "Outdoor Temperature [%.1f °C]" {channel="hp1000:weatherstation:8d435fd4:tempf"}
Number   WeatherStation_OutdoorHumidity    "Outdoor Humidity [%d %%]"      {channel="hp1000:weatherstation:8d435fd4:humidity"}
Number   WeatherStation_Dewpoint           "Dewpoint [%.1f °C]"            {channel="hp1000:weatherstation:8d435fd4:dewptf"}
Number   WeatherStation_Windchill          "Windchill [%.1f °C]"           {channel="hp1000:weatherstation:8d435fd4:windchillf"}
String   WeatherStation_WindDegree         "Wind Degree [%s °]"            {channel="hp1000:weatherstation:8d435fd4:winddir"}
Number   WeatherStation_WindSpeed          "Wind Speed [%.1f km/h"         {channel="hp1000:weatherstation:8d435fd4:windspeedmph"}
Number   WeatherStation_GustSpeed          "Gust Speed  [%.1f km/h]"       {channel="hp1000:weatherstation:8d435fd4:windgustmph"}
Number   WeatherStation_HourlyRainRate     "Hourly Rain Rate [%.2f mm]"    {channel="hp1000:weatherstation:8d435fd4:rainin"}
Number   WeatherStation_DailyRain          "Daily Rain [%.2f mm]"          {channel="hp1000:weatherstation:8d435fd4:dailyrainin"}
Number   WeatherStation_WeeklyRain         "Weekly Rain [%.2f mm]"         {channel="hp1000:weatherstation:8d435fd4:weeklyrainin"}
Number   WeatherStation_MonthlyRain        "Monthly Rain [%.2f mm]"        {channel="hp1000:weatherstation:8d435fd4:monthlyrainin"}
Number   WeatherStation_YearlyRain         "Yearly Rain [%.2f mm]"         {channel="hp1000:weatherstation:8d435fd4:yearlyrainin"}
Number   WeatherStation_SolarRadiation     "Solar Radiation [%.2f w/m²]"   {channel="hp1000:weatherstation:8d435fd4:solarradiation"}
Number   WeatherStation_UV                 "UV [%d index]"                 {channel="hp1000:weatherstation:8d435fd4:uv"}
Number   WeatherStation_IndoorTemperature  "Indoor Temperature [%.1f °C]"  {channel="hp1000:weatherstation:8d435fd4:indoortempf"}
Number   WeatherStation_IndoorHumidity     "Indoor Humidity [%d %%]"       {channel="hp1000:weatherstation:8d435fd4:indoorhumidity"}
Number   WeatherStation_Pressure           "Pressure [%.1f hPa]"           {channel="hp1000:weatherstation:8d435fd4:baromin"}
String   WeatherStation_LowBatterie        "Low Batterie [%s]"             {channel="hp1000:weatherstation:8d435fd4:lowbatt"}
DateTime WeatherStation_DateUTC            "Date UTC [%s]"                 {channel="hp1000:weatherstation:8d435fd4:dateutc"}
String   WeatherStation_SoftwareType       "Software Type [%s]"            {channel="hp1000:weatherstation:8d435fd4:softwaretype"}
String   WeatherStation_Realtime           "Realtime [%s]"                 {channel="hp1000:weatherstation:8d435fd4:realtime"}
String   WeatherStation_UpdateFrequence    "Update Frequence [%s]"         {channel="hp1000:weatherstation:8d435fd4:rtfreq"}

```
