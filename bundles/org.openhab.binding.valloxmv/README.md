# ValloxMV Binding

This binding is designed to connect to the web interface of Vallox MV series of ventilation unit.
It has been tested so far only with Vallox 350 MV and 510 MV.

## Supported Things

There is one thing (valloxmv) supporting the connection via the web interface of the Vallox MV. There is NO support of former modbus connected devices.

## Discovery

This binding does not support any discovery, IP address has to be provided.

## Thing Configuration

The Thing needs the information at which IP the web interface could be reached and how often the values should be updated.
Minimum update interval is limited to 15 sec in order to avoid polling again before results have been evaluated.

| Config                | Description                                           | Type  | Default |
| :-------------------- |:------------------------------------------------------|:-----:|:-------:|
| ip                    | IP address of web interface                           |string | n/a     |
| updateinterval        | Interval in seconds in which the interface is polled  |int    | 60      |

## Channels

Overview of provided channels

| Channel ID                | Vallox Name                 | Description                       | Read/Write | Values               |
| :------------------------- | :--------------------------- |:-----------------------------------|:-:|:----------------------:|
| onoff                     | A_CYC_MODE                  | On off switch                     |rw| On/Off               |
| state                     | _several_                     | Current state of ventilation unit |rw| 1=FIREPLACE, 2=AWAY, 3=ATHOME, 4=BOOST    |
| fanspeed                  | A_CYC_FAN_SPEED             | Fan speed                         |r | 0 - 100 (%)          |
| fanspeedextract           | A_CYC_EXTR_FAN_SPEED        | Fan speed of extracting fan       |r | 1/min                |
| fanspeedsupply            | A_CYC_SUPP_FAN_SPEED        | Fan speed of supplying fan        |r | 1/min                |
| tempinside                | A_CYC_TEMP_EXTRACT_AIR      | Extracted air temp                |r | Number (°C)          |
| tempoutside               | A_CYC_TEMP_OUTDOOR_AIR      | Outside air temp                  |r | Number (°C)          |
| tempexhaust               | A_CYC_TEMP_EXHAUST_AIR      | Exhausted air temp                |r | Number (°C)          |
| tempincomingbeforeheating | A_CYC_TEMP_SUPPLY_CELL_AIR  | Incoming air temp (pre heating)   |r | Number (°C)          |
| tempincoming              | A_CYC_TEMP_SUPPLY_AIR       | Incoming air temp                 |r | Number (°C)          |
| humidity                  | A_CYC_RH_VALUE              | Extracted air humidity            |r | 0 - 100 (%)          |
| cellstate                 | A_CYC_CELL_STATE            | Current cell state                |r | 0=heat recovery, 1=cool recovery, 2=bypass, 3=defrosting          |
| uptimeyears               | A_CYC_TOTAL_UP_TIME_YEARS   | Total uptime years                |r | Y                    |
| uptimehours               | A_CYC_TOTAL_UP_TIME_HOURS   | Total uptime hours                |r | h                    |
| uptimehourscurrent        | A_CYC_CURRENT_UP_TIME_HOURS | Current uptime in hours           |r | h                    |
| filterchangeddate         | A\_CYC\_FILTER\_CHANGED\_DAY/MONTH/YEAR | Last filter change    |r | date                 |
| remainingfilterdays       | A_CYC_CURRENT_UP_TIME_HOURS | Days until filter change          |r | d                    |
| extrfanbalancebase        | A_CYC_EXTR_FAN_BALANCE_BASE | Extract fan base speed            |rw| 0 - 100 (%)          |
| suppfanbalancebase        | A_CYC_SUPP_FAN_BALANCE_BASE | Supply fan base speed             |rw| 0 - 100 (%)          |
| homespeedsetting          | A_CYC_HOME_SPEED_SETTING    | Home fan speed                    |rw| 0 - 100 (%)          |
| awayspeedsetting          | A_CYC_AWAY_SPEED_SETTING    | Away fan speed                    |rw| 0 - 100 (%)          |
| boostspeedsetting         | A_CYC_BOOST_SPEED_SETTING   | Boost fan speed                   |rw| 0 - 100 (%)          |
| homeairtemptarget         | A_CYC_HOME_AIR_TEMP_TARGET  | Target temperature in home state  |rw| Number (°C)          |
| awayairtemptarget         | A_CYC_AWAY_AIR_TEMP_TARGET  | Target temperature in away state  |rw| Number (°C)          |
| boostairtemptarget        | A_CYC_BOOST_AIR_TEMP_TARGET | Target temperature in boost state |rw| Number (°C)          |
| boosttime                 | A_CYC_BOOST_TIME            | Timer value in boost profile      |rw| 1 - 65535 (min)      |
| boosttimerenabled         | A_CYC_BOOST_TIMER_ENABLED   | Timer enabled setting in boost profile |rw| On/Off               |
| fireplaceextrfan          | A_CYC_FIREPLACE_EXTR_FAN    | Fireplace profile extract fan speed |rw| 0 - 100 (%)          |
| fireplacesuppfan          | A_CYC_FIREPLACE_SUPP_FAN    | Fireplace profile supply fan speed |rw| 0 - 100 (%)          |
| fireplacetime             | A_CYC_FIREPLACE_TIME        | Timer value in fireplace profile  |rw| 1 - 65535 (min)      |
| fireplacetimerenabled     | A_CYC_FIREPLACE_TIMER_ENABLED | Timer enabled setting in fireplace profile |rw| On/Off               |
| extraairtemptarget        | A_CYC_EXTRA_AIR_TEMP_TARGET | Target temperature in extra profile |rw| Number (°C)          |
| extraextrfan              | A_CYC_EXTRA_EXTR_FAN        | Extra profile extract fan speed   |rw| 0 - 100 (%)          |
| extrasuppfan              | A_CYC_EXTRA_EXTR_FAN        | Extra profile supply fan speed    |rw| 0 - 100 (%)          |
| extratime                 | A_CYC_EXTRA_TIME            | Timer value in extra profile      |rw| 1 - 65535 (min)      |
| extratimerenabled         | A_CYC_EXTRA_TIMER_ENABLED   | Timer enabled setting in extra profile |rw| On/Off               |
| weeklytimerenabled        | A_CYC_WEEKLY_TIMER_ENABLED  | Weekly timer enabled setting      |rw| On/Off               |

## Example

### Things file

```java
Thing valloxmv:valloxmv:lueftung [ip="192.168.1.3", updateinterval=60]
```

### Items file

```java
Number State                   "Current state: [%d]"   {channel="valloxmv:valloxmv:lueftung:state"}
Number FanSpeed                "Fanspeed [%d %%]"  {channel="valloxmv:valloxmv:lueftung:fanspeed"}

Number Temp_TempInside         "Temp inside [%.1f °C]" <temperature>    {channel="valloxmv:valloxmv:lueftung:tempinside"}
Number Temp_TempOutside        "Temp outside [%.1f °C]"    <temperature>      {channel="valloxmv:valloxmv:lueftung:tempoutside"}
Number Temp_TempExhaust        "Temp outgoing [%.1f °C]"   <temperature>   {channel="valloxmv:valloxmv:lueftung:tempexhaust"}
Number Temp_TempIncoming       "Temp incoming [%.1f °C]"   <temperature>   {channel="valloxmv:valloxmv:lueftung:tempincoming"}

Number Humidity                "Humidity [%d %%]"  {channel="valloxmv:valloxmv:lueftung:humidity"}
```
