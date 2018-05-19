# valloxmv Binding

This binding is designed to connect to the web interface of Vallox MV series of ventilation unit.
It has been tested so far only with Vallox 350 MV and 510 MV.

## Supported Things

There is one thing supporting the connection via the web interface. There is NO support of former modbus connected devices.

## Discovery

This binding does not support any discovery, IP address has to be provided.

## Thing Configuration

The Thing needs the information at which IP the web interface could be reached and how often the values should be updated.
Minimum update interval is limited to 15 sec in order to avoid polling again before results have been evaluated.

## Channels

Overview of provided channels

| Channel ID                | Vallox Name                 | Description                       | Read/Write | Values               |
| :------------------------- | :--------------------------- |:-----------------------------------|:-:|:----------------------:|
| onoff                     | A_CYC_MODE                  | On off switch                     |rw| On/Off              |
| state                     | _several_                     | Current state of ventilation unit |rw| 1=FIREPLACE, 2=AWAY, 3=ATHOME, 4=BOOST    |
| fanspeed                  | A_CYC_FAN_SPEED             | Fan speed                         |r| 0 - 100 (%)          |
| fanspeedextract           | A_CYC_EXTR_FAN_SPEED        | Fan speed of extracting fan       |r| 1/min                |
| fanspeedsupply            | A_CYC_SUPP_FAN_SPEED        | Fan speed of supplying fan        |r| 1/min                |
| tempinside                | A_CYC_TEMP_EXTRACT_AIR      | Extracted air temp                |r| Number (°C)          |
| tempoutside               | A_CYC_TEMP_OUTDOOR_AIR      | Outside air temp                  |r| Number (°C)          |
| tempexhaust               | A_CYC_TEMP_EXHAUST_AIR      | Exhausted air temp                |r| Number (°C)          |
| tempincomingbeforeheating | A_CYC_TEMP_SUPPLY_CELL_AIR  | Incoming air temp (pre heating)   |r| Number (°C)          |
| tempincoming              | A_CYC_TEMP_SUPPLY_AIR       | Incoming air temp                 |r| Number (°C)          |
| humidity                  | A_CYC_RH_VALUE              | Extracted air humidity            |r| 0 - 100 (%)          |
| cellstate                 | A_CYC_CELL_STATE            | Current cell state                |r| 0=heat recovery, 1=cool recovery, 2=bypass, 3=defrosting          |
| uptimeyears               | A_CYC_TOTAL_UP_TIME_YEARS   | Total uptime years                |r| Y                    |
| uptimehours               | A_CYC_TOTAL_UP_TIME_HOURS   | Total uptime hours                |r| h                    |
| uptimehourscurrent        | A_CYC_CURRENT_UP_TIME_HOURS | Total uptime hours                |r| h                    |

## Full Example

### Things file ###

```
Thing valloxmv:valloxmv:lueftung [ip="192.168.1.3", updateinterval=60]
```


### Items file ###

```
Number State                   "Current state: [%d]"   {channel="valloxmv:valloxmv:lueftung:state"}
Number FanSpeed                "Fanspeed [%d %%]"  {channel="valloxmv:valloxmv:lueftung:fanspeed"}

Number Temp_TempInside         "Temp inside [%.1f °C]" <temperature>    {channel="valloxmv:valloxmv:lueftung:tempinside"}
Number Temp_TempOutside        "Temp outside [%.1f °C]"    <temperature>      {channel="valloxmv:valloxmv:lueftung:tempoutside"}
Number Temp_TempExhaust        "Temp outgoing [%.1f °C]"   <temperature>   {channel="valloxmv:valloxmv:lueftung:tempexhaust"}
Number Temp_TempIncoming       "Temp incoming [%.1f °C]"   <temperature>   {channel="valloxmv:valloxmv:lueftung:tempincoming"}

Number Humidity                "Humidity [%d %%]"  {channel="valloxmv:valloxmv:lueftung:humidity"}
```

## Example Values

There are even more values supported by the interface but the extraction for most of them has not been implemented so far.
    
* A\_CYC\_APPL\_SW\_VERSION   0
* A\_CYC\_APPL\_SW\_VERSION\_1     0
* A\_CYC\_APPL\_SW\_VERSION\_2     0
* A\_CYC\_APPL\_SW\_VERSION\_3     0
* A\_CYC\_APPL\_SW\_VERSION\_4     0
* A\_CYC\_APPL\_SW\_VERSION\_5     0
* A\_CYC\_APPL\_SW\_VERSION\_6     0
* A\_CYC\_APPL\_SW\_VERSION\_7     256
* A\_CYC\_APPL\_SW\_VERSION\_8     2048
* A\_CYC\_APPL\_SW\_VERSION\_9     1024
* A\_CYC\_BOOT\_SW\_VERSION   0
* A\_CYC\_APPL\_SW\_SIZE\_0    0
* A\_CYC\_APPL\_SW\_SIZE\_1    0
* A\_CYC\_SERIAL\_NUMBER\_MSW     26230
* A\_CYC\_SERIAL\_NUMBER\_LSW     45641
* A\_CYC\_MACHINE\_TYPE  2
* A\_CYC\_MACHINE\_MODEL     6
* A\_CYC\_MASTER\_PASSWORD   8255
* A\_CYC\_CONFIGURATION\_MSW     47697
* A\_CYC\_CONFIGURATION\_LSW     3097
* A\_CYC\_CONFIGURATION\_NA\_0    0
* A\_CYC\_CONFIGURATION\_NA\_1    0
* A\_CYC\_CONFIGURATION\_NA\_2    0
* A\_CYC\_CONFIGURATION\_NA\_3    0
* A\_CYC\_CONFIGURATION\_CHECKSUM    112
    
    
* A\_CYC\_TYP\_SERIAL\_NUMBER\_MSW     0
* A\_CYC\_TYP\_SERIAL\_NUMBER\_LSW     0
* A\_CYC\_TYP\_PANEL\_MODEL   0
* A\_CYC\_TYP\_APPL\_SW\_VERSION   0
* A\_CYC\_TYP\_APPL\_SW\_VERSION\_1     0
* A\_CYC\_TYP\_APPL\_SW\_VERSION\_2     0
* A\_CYC\_TYP\_APPL\_SW\_VERSION\_3     0
* A\_CYC\_TYP\_APPL\_SW\_VERSION\_4     0
* A\_CYC\_TYP\_APPL\_SW\_VERSION\_5     0
* A\_CYC\_TYP\_APPL\_SW\_VERSION\_6     0
* A\_CYC\_TYP\_APPL\_SW\_VERSION\_7     0
* A\_CYC\_TYP\_APPL\_SW\_VERSION\_8     0
* A\_CYC\_TYP\_APPL\_SW\_VERSION\_9     0
* A\_CYC\_TYP\_BOOT\_SW\_VERSION   0
* A\_CYC\_TYP\_APPL\_SW\_SIZE\_0    0
* A\_CYC\_TYP\_APPL\_SW\_SIZE\_1    0
    
    
* **A_CYC_FAN_SPEED     70**
* **A_CYC_TEMP_EXTRACT_AIR  29223**
* **A_CYC_TEMP_EXHAUST_AIR  28526**
* **A_CYC_TEMP_OUTDOOR_AIR  28332**
* **A_CYC_TEMP_SUPPLY_CELL_AIR  29052**
* **A_CYC_TEMP_SUPPLY_AIR   29118**
* A\_CYC\_RH\_LEVEL  1
* A\_CYC\_CO2\_LEVEL     0
* A\_CYC\_EXTR\_FAN\_SPEED    2056
* A\_CYC\_SUPP\_FAN\_SPEED    1935
* **A_CYC_RH_VALUE  55**
* A\_CYC\_CO2\_VALUE     0
* A\_CYC\_FIREPLACE\_SWITCH  0
* A\_CYC\_DIGITAL\_INPUT     0
* A\_CYC\_ANALOG\_CTRL\_INPUT     53
* A\_CYC\_POST\_HEATING\_TRIM     441
* A\_CYC\_PWM\_OFFSET\_TRIM   699
* A\_CYC\_DEFROST\_TRIM  0
* A\_CYC\_VOLTAGE\_LOW   0
* A\_CYC\_ANALOG\_SENSOR\_INPUT   55
* A\_CYC\_RH\_SENSOR\_0   65535
* A\_CYC\_RH\_SENSOR\_1   65535
* A\_CYC\_RH\_SENSOR\_2   65535
* A\_CYC\_RH\_SENSOR\_3   65535
* A\_CYC\_RH\_SENSOR\_4   65535
* A\_CYC\_RH\_SENSOR\_5   65535
* A\_CYC\_CO2\_SENSOR\_0  65535
* A\_CYC\_CO2\_SENSOR\_1  65535
* A\_CYC\_CO2\_SENSOR\_2  65535
* A\_CYC\_CO2\_SENSOR\_3  65535
* A\_CYC\_CO2\_SENSOR\_4  65535
* A\_CYC\_CO2\_SENSOR\_5  65535
* A\_CYC\_DIP\_SWITCH\_0  0
* A\_CYC\_DIP\_SWITCH\_1  0
* A\_CYC\_DIP\_SWITCH\_2  0
* A\_CYC\_DIP\_SWITCH\_3  0
* A\_CYC\_TEMP\_OPTIONAL     22469
* A\_CYC\_VOC\_LEVEL     1000
* A\_CYC\_VOC\_SENSOR\_0  65535
* A\_CYC\_VOC\_SENSOR\_1  65535
* A\_CYC\_VOC\_SENSOR\_2  65535
* A\_CYC\_VOC\_SENSOR\_3  0
    
    
* **A_CYC_STATE     0**
* **A_CYC_MODE  0**
* A\_CYC\_DEFROSTING    0
* **A_CYC_BOOST_TIMER   0**
* **A_CYC_FIREPLACE_TIMER   0**
* A\_CYC\_EXTRA\_TIMER   0
* A\_CYC\_WEEKLY\_TIMER\_ENABLED  0
* A\_CYC\_CELL\_STATE    0
* A\_CYC\_TOTAL\_UP\_TIME\_YEARS   0
* A\_CYC\_TOTAL\_UP\_TIME\_HOURS   1913
* A\_CYC\_CURRENT\_UP\_TIME\_HOURS     177
* A\_CYC\_REMAINING\_TIME\_FOR\_FILTER     0
* A\_CYC\_LIMP\_MODE     0
* A\_CYC\_METRICS   0
* A\_CYC\_DEFROST\_COUNT\_IN\_24H  0
* A\_CYC\_DEFROST\_COUNT\_IN\_WEEK     0
* A\_CYC\_DEFROST\_SUPERMELT\_THRESHOLD   30
* A\_CYC\_ENABLED   1
* A\_CYC\_COMMAND   0
* A\_CYC\_MLV\_STATE     0
* A\_CYC\_UPD\_ADDRESS\_1     0
* A\_CYC\_UPD\_ADDRESS\_2     0
* A\_CYC\_CLOUD\_STATUS  0
    
    
* A\_CYC\_MINUTE    8
* A\_CYC\_HOUR  20
* A\_CYC\_DAY   22
* A\_CYC\_MONTH     11
* A\_CYC\_YEAR  17
* A\_CYC\_WEEKDAY   3
    
    
* A\_CYC\_IO\_EXTRACT\_FAN    190
* A\_CYC\_IO\_SUPPLY\_FAN     190
* A\_CYC\_IO\_ERROR  0
* A\_CYC\_IO\_HEATER     1
* A\_CYC\_IO\_EXTRA\_HEATER   0
* A\_CYC\_IO\_BYPASS     1
    
    
* A\_CYC\_IN\_EXTRACT\_FAN    50
* A\_CYC\_IN\_SUPPLY\_FAN     50
* A\_CYC\_IN\_ERROR  1
* A\_CYC\_IN\_HEATER     0
* A\_CYC\_IN\_EXTRA\_HEATER   0
* A\_CYC\_IN\_BYPASS     0
    
    
* A\_CYC\_BETA\_STATE    0
* A\_CYC\_GW\_ADDRESS\_1  49320
* A\_CYC\_GW\_ADDRESS\_2  25601
* A\_CYC\_MASK\_ADDRESS\_1    65535
* A\_CYC\_MASK\_ADDRESS\_2    65280
* A\_CYC\_RH\_0\_ADDRESS  0
* A\_CYC\_RH\_1\_ADDRESS  0
* A\_CYC\_RH\_2\_ADDRESS  0
* A\_CYC\_RH\_3\_ADDRESS  0
* A\_CYC\_RH\_4\_ADDRESS  0
* A\_CYC\_RH\_5\_ADDRESS  0
* A\_CYC\_CO2\_0\_ADDRESS     0
* A\_CYC\_CO2\_1\_ADDRESS     0
* A\_CYC\_CO2\_2\_ADDRESS     0
* A\_CYC\_CO2\_3\_ADDRESS     0
* A\_CYC\_CO2\_4\_ADDRESS     0
* A\_CYC\_CO2\_5\_ADDRESS     0
* A\_CYC\_ETH\_CLOUD\_ENABLED     0
* A\_CYC\_IP\_ADDRESS\_1  49320
* A\_CYC\_IP\_ADDRESS\_2  25690
* A\_CYC\_UUID0     7748
* A\_CYC\_UUID1     18312
* A\_CYC\_UUID2     4827
* A\_CYC\_UUID3     20138
* A\_CYC\_UUID4     43395
* A\_CYC\_UUID5     62443
* A\_CYC\_UUID6     23515
* A\_CYC\_UUID7     10264
    
    
* A\_CYC\_USED\_SETTINGS\_VARIABLES   76
* A\_CYC\_MODBUS\_ADDRESS    1
* A\_CYC\_MODBUS\_BAUD\_X100  192
* A\_CYC\_MODBUS\_FRAME  257
* A\_CYC\_EXTR\_FAN\_BALANCE\_BASE     100
* A\_CYC\_SUPP\_FAN\_BALANCE\_BASE     100
* A\_CYC\_FIREPLACE\_EXTR\_FAN    50
* A\_CYC\_FIREPLACE\_SUPP\_FAN    50
* A\_CYC\_RH\_BASIC\_LEVEL    58
* A\_CYC\_CO2\_THRESHOLD     900
* A\_CYC\_EXTRA\_ENABLED     0
* A\_CYC\_EXTRA\_AIR\_TEMP\_TARGET     28815
* A\_CYC\_EXTRA\_EXTR\_FAN    50
* A\_CYC\_EXTRA\_SUPP\_FAN    50
* A\_CYC\_EXTRA\_TIME    10
* A\_CYC\_AWAY\_RH\_CTRL\_ENABLED  0
* A\_CYC\_AWAY\_CO2\_CTRL\_ENABLED     0
* A\_CYC\_AWAY\_SPEED\_SETTING    35
* A\_CYC\_AWAY\_AIR\_TEMP\_TARGET  28815
* A\_CYC\_HOME\_RH\_CTRL\_ENABLED  0
* A\_CYC\_HOME\_CO2\_CTRL\_ENABLED     0
* A\_CYC\_HOME\_SPEED\_SETTING    70
* A\_CYC\_HOME\_AIR\_TEMP\_TARGET  29215
* A\_CYC\_BOOST\_RH\_CTRL\_ENABLED     0
* A\_CYC\_BOOST\_CO2\_CTRL\_ENABLED    0
* A\_CYC\_BOOST\_SPEED\_SETTING   100
* A\_CYC\_BOOST\_AIR\_TEMP\_TARGET     28815
* A\_CYC\_RELAY\_MODE    0
* A\_CYC\_DIGITAL\_INPUT\_1\_MODE  0
* A\_CYC\_DIGITAL\_INPUT\_2\_MODE  0
* A\_CYC\_ANALOG\_INPUT\_MODE     1
* A\_CYC\_DEFROST\_MODE  1
* A\_CYC\_DEFROST\_RH\_PARAM  55
* A\_CYC\_DEFROST\_TEMP\_PARAM    15
* A\_CYC\_MLV\_WINTER\_SETPOINT   26815
* A\_CYC\_MLV\_SUMMER\_SETPOINT   28315
* A\_CYC\_WATERHEATER\_STORED\_I  40697
* A\_CYC\_INSTALLATION\_DONE     0
* A\_CYC\_DEFROST\_RH\_OFFSET     20
* A\_CYC\_FILTER\_CHANGE\_INTERVAL    120
* A\_CYC\_CELL\_TYPE     1
* A\_CYC\_EXTRA\_HEATER\_TYPE     1
* A\_CYC\_POST\_HEATER\_TYPE  1
* A\_CYC\_BRANDING  0
* A\_CYC\_SIDEDNESS     0
* A\_CYC\_RH\_LEVEL\_MODE     0
* A\_CYC\_BOOST\_TIME    30
* A\_CYC\_FIREPLACE\_TIME    15
* A\_CYC\_FILTER\_CHANGED\_DAY    1
* A\_CYC\_FILTER\_CHANGED\_MONTH  1
* A\_CYC\_FILTER\_CHANGED\_YEAR   0
* A\_CYC\_SUPPLY\_HEATING\_ADJUST\_MODE    0
* A\_CYC\_MIN\_DEFROST\_TIME  12
* A\_CYC\_OPT\_TEMP\_SENSOR\_MODE  0
    
    
* A\_CYC\_LANGUAGE  0
* A\_CYC\_PARENTAL\_PASSWORD     1001
* A\_CYC\_USER\_PASSWORD     0
* A\_CYC\_ACCESS\_LEVEL  0
* A\_CYC\_PARENTAL\_CTRL\_ENABLED     0
* A\_CYC\_BOOST\_TIMER\_ENABLED   0
* A\_CYC\_FIREPLACE\_TIMER\_ENABLED   1
* A\_CYC\_SUMMER\_TIME\_AUTO\_ENAB     1
* A\_CYC\_12\_HOUR\_CLOCK\_ENABLED     0
* A\_CYC\_SLEEP\_DELAY   10
* A\_CYC\_BG\_LIGHT\_LEVEL    50
* A\_CYC\_EXTRA\_TIMER\_ENABLED   1
    
    
* A\_CYC\_SUPP\_FAN\_TEST     0
* A\_CYC\_EXTR\_FAN\_TEST     0
* A\_CYC\_BY\_PASS\_TEST  0
* A\_CYC\_HEATER\_TEST   0
* A\_CYC\_EXTRA\_HEATER\_TEST     0
* A\_CYC\_EFFICIENCY\_TEST   0
* A\_CYC\_SUPPLY\_EFFICIENCY     82
* A\_CYC\_EXTRACT\_EFFICIENCY    77
    
    
* A\_CYC\_TOTAL\_FAULT\_COUNT     0
* A\_CYC\_FAULT\_CODE    0
* A\_CYC\_FAULT\_SEVERITY    0
* A\_CYC\_FAULT\_FIRST\_DATE  0
* A\_CYC\_FAULT\_LAST\_DATE   0
* A\_CYC\_FAULT\_COUNT   0
* A\_CYC\_FAULT\_ACTIVITY    0
* A\_CYC\_FAULT\_CODE\_2  0
* A\_CYC\_FAULT\_SEVERITY\_2  0
* A\_CYC\_FAULT\_FIRST\_DATE\_2    0
* A\_CYC\_FAULT\_LAST\_DATE\_2     0
* A\_CYC\_FAULT\_COUNT\_2     0
* A\_CYC\_FAULT\_ACTIVITY\_2  0
* A\_CYC\_FAULT\_CODE\_3  0
* A\_CYC\_FAULT\_SEVERITY\_3  0
* A\_CYC\_FAULT\_FIRST\_DATE\_3    0
* A\_CYC\_FAULT\_LAST\_DATE\_3     0
* A\_CYC\_FAULT\_COUNT\_3     0
* A\_CYC\_FAULT\_ACTIVITY\_3  0
* A\_CYC\_FAULT\_CODE\_4  0
* A\_CYC\_FAULT\_SEVERITY\_4  0
* A\_CYC\_FAULT\_FIRST\_DATE\_4    0
* A\_CYC\_FAULT\_LAST\_DATE\_4     0
* A\_CYC\_FAULT\_COUNT\_4     0
* A\_CYC\_FAULT\_ACTIVITY\_4  0
* A\_CYC\_FAULT\_CODE\_5  0
* A\_CYC\_FAULT\_SEVERITY\_5  0
* A\_CYC\_FAULT\_FIRST\_DATE\_5    0
* A\_CYC\_FAULT\_LAST\_DATE\_5     0
* A\_CYC\_FAULT\_COUNT\_5     0
* A\_CYC\_FAULT\_ACTIVITY\_5  0
* A\_CYC\_FAULT\_CODE\_6  0
* A\_CYC\_FAULT\_SEVERITY\_6  0
* A\_CYC\_FAULT\_FIRST\_DATE\_6    0
* A\_CYC\_FAULT\_LAST\_DATE\_6     0
* A\_CYC\_FAULT\_COUNT\_6     0
* A\_CYC\_FAULT\_ACTIVITY\_6  0
* A\_CYC\_FAULT\_CODE\_7  0
* A\_CYC\_FAULT\_SEVERITY\_7  0
* A\_CYC\_FAULT\_FIRST\_DATE\_7    0
* A\_CYC\_FAULT\_LAST\_DATE\_7     0
* A\_CYC\_FAULT\_COUNT\_7     0
* A\_CYC\_FAULT\_ACTIVITY\_7  0
* A\_CYC\_FAULT\_CODE\_8  0
* A\_CYC\_FAULT\_SEVERITY\_8  0
* A\_CYC\_FAULT\_FIRST\_DATE\_8    0
* A\_CYC\_FAULT\_LAST\_DATE\_8     0
* A\_CYC\_FAULT\_COUNT\_8     0
* A\_CYC\_FAULT\_ACTIVITY\_8  0
* A\_CYC\_FAULT\_CODE\_9  0
* A\_CYC\_FAULT\_SEVERITY\_9  0
* A\_CYC\_FAULT\_FIRST\_DATE\_9    0
* A\_CYC\_FAULT\_LAST\_DATE\_9     0
* A\_CYC\_FAULT\_COUNT\_9     0
* A\_CYC\_FAULT\_ACTIVITY\_9  0
* A\_CYC\_FAULT\_CODE\_10     0
* A\_CYC\_FAULT\_SEVERITY\_10     0
* A\_CYC\_FAULT\_FIRST\_DATE\_10   0
* A\_CYC\_FAULT\_LAST\_DATE\_10    0
* A\_CYC\_FAULT\_COUNT\_10    0
* A\_CYC\_FAULT\_ACTIVITY\_10     0
* A\_CYC\_FAULT\_CODE\_11     0
* A\_CYC\_FAULT\_SEVERITY\_11     0
* A\_CYC\_FAULT\_FIRST\_DATE\_11   0
* A\_CYC\_FAULT\_LAST\_DATE\_11    0
* A\_CYC\_FAULT\_COUNT\_11    0
* A\_CYC\_FAULT\_ACTIVITY\_11     0
* A\_CYC\_FAULT\_CODE\_12     0
* A\_CYC\_FAULT\_SEVERITY\_12     0
* A\_CYC\_FAULT\_FIRST\_DATE\_12   0
* A\_CYC\_FAULT\_LAST\_DATE\_12    0
* A\_CYC\_FAULT\_COUNT\_12    0
* A\_CYC\_FAULT\_ACTIVITY\_12     0
* A\_CYC\_FAULT\_CODE\_13     0
* A\_CYC\_FAULT\_SEVERITY\_13     0
* A\_CYC\_FAULT\_FIRST\_DATE\_13   0
* A\_CYC\_FAULT\_LAST\_DATE\_13    0
* A\_CYC\_FAULT\_COUNT\_13    0
* A\_CYC\_FAULT\_ACTIVITY\_13     0
* A\_CYC\_FAULT\_CODE\_14     0
* A\_CYC\_FAULT\_SEVERITY\_14     0
* A\_CYC\_FAULT\_FIRST\_DATE\_14   0
* A\_CYC\_FAULT\_LAST\_DATE\_14    0
* A\_CYC\_FAULT\_COUNT\_14    0
* A\_CYC\_FAULT\_ACTIVITY\_14     0
* A\_CYC\_FAULT\_CODE\_15     0
* A\_CYC\_FAULT\_SEVERITY\_15     0
* A\_CYC\_FAULT\_FIRST\_DATE\_15   0
* A\_CYC\_FAULT\_LAST\_DATE\_15    0
* A\_CYC\_FAULT\_COUNT\_15    0
* A\_CYC\_FAULT\_ACTIVITY\_15     0
* A\_CYC\_FAULT\_CODE\_16     0
* A\_CYC\_FAULT\_SEVERITY\_16     0
* A\_CYC\_FAULT\_FIRST\_DATE\_16   0
* A\_CYC\_FAULT\_LAST\_DATE\_16    0
* A\_CYC\_FAULT\_COUNT\_16    0
* A\_CYC\_FAULT\_ACTIVITY\_16     0
* A\_CYC\_FAULT\_CODE\_17     0
* A\_CYC\_FAULT\_SEVERITY\_17     0
* A\_CYC\_FAULT\_FIRST\_DATE\_17   0
* A\_CYC\_FAULT\_LAST\_DATE\_17    0
* A\_CYC\_FAULT\_COUNT\_17    0
* A\_CYC\_FAULT\_ACTIVITY\_17     0
* A\_CYC\_FAULT\_CODE\_18     0
* A\_CYC\_FAULT\_SEVERITY\_18     0
* A\_CYC\_FAULT\_FIRST\_DATE\_18   0
* A\_CYC\_FAULT\_LAST\_DATE\_18    0
* A\_CYC\_FAULT\_COUNT\_18    0
* A\_CYC\_FAULT\_ACTIVITY\_18     0
* A\_CYC\_FAULT\_CODE\_19     0
* A\_CYC\_FAULT\_SEVERITY\_19     0
* A\_CYC\_FAULT\_FIRST\_DATE\_19   0
* A\_CYC\_FAULT\_LAST\_DATE\_19    0
* A\_CYC\_FAULT\_COUNT\_19    0
* A\_CYC\_FAULT\_ACTIVITY\_19     0
* A\_CYC\_FAULT\_CODE\_20     0
* A\_CYC\_FAULT\_SEVERITY\_20     0
* A\_CYC\_FAULT\_FIRST\_DATE\_20   0
* A\_CYC\_FAULT\_LAST\_DATE\_20    0
* A\_CYC\_FAULT\_COUNT\_20    0
* A\_CYC\_FAULT\_ACTIVITY\_20     0
* A\_CYC\_FAULT\_CODE\_21     0
* A\_CYC\_FAULT\_SEVERITY\_21     0
* A\_CYC\_FAULT\_FIRST\_DATE\_21   0
* A\_CYC\_FAULT\_LAST\_DATE\_21    0
* A\_CYC\_FAULT\_COUNT\_21    0
* A\_CYC\_FAULT\_ACTIVITY\_21     0
* A\_CYC\_FAULT\_CODE\_22     0
* A\_CYC\_FAULT\_SEVERITY\_22     0
* A\_CYC\_FAULT\_FIRST\_DATE\_22   0
* A\_CYC\_FAULT\_LAST\_DATE\_22    0
* A\_CYC\_FAULT\_COUNT\_22    0
* A\_CYC\_FAULT\_ACTIVITY\_22     0
* A\_CYC\_FAULT\_CODE\_23     0
* A\_CYC\_FAULT\_SEVERITY\_23     0
* A\_CYC\_FAULT\_FIRST\_DATE\_23   0
* A\_CYC\_FAULT\_LAST\_DATE\_23    0
* A\_CYC\_FAULT\_COUNT\_23    0
* A\_CYC\_FAULT\_ACTIVITY\_23     0
* A\_CYC\_FAULT\_CODE\_24     0
* A\_CYC\_FAULT\_SEVERITY\_24     0
* A\_CYC\_FAULT\_FIRST\_DATE\_24   0
* A\_CYC\_FAULT\_LAST\_DATE\_24    0
* A\_CYC\_FAULT\_COUNT\_24    0
* A\_CYC\_FAULT\_ACTIVITY\_24     0
* A\_CYC\_FAULT\_CODE\_25     0
* A\_CYC\_FAULT\_SEVERITY\_25     0
* A\_CYC\_FAULT\_FIRST\_DATE\_25   0
* A\_CYC\_FAULT\_LAST\_DATE\_25    0
* A\_CYC\_FAULT\_COUNT\_25    0
* A\_CYC\_FAULT\_ACTIVITY\_25     0
* A\_CYC\_FAULT\_CODE\_26     0
* A\_CYC\_FAULT\_SEVERITY\_26     0
* A\_CYC\_FAULT\_FIRST\_DATE\_26   0
* A\_CYC\_FAULT\_LAST\_DATE\_26    0
* A\_CYC\_FAULT\_COUNT\_26    0
* A\_CYC\_FAULT\_ACTIVITY\_26     0
* A\_CYC\_FAULT\_CODE\_27     0
* A\_CYC\_FAULT\_SEVERITY\_27     0
* A\_CYC\_FAULT\_FIRST\_DATE\_27   0
* A\_CYC\_FAULT\_LAST\_DATE\_27    0
* A\_CYC\_FAULT\_COUNT\_27    0
* A\_CYC\_FAULT\_ACTIVITY\_27     0
* A\_CYC\_FAULT\_CODE\_28     0
* A\_CYC\_FAULT\_SEVERITY\_28     0
* A\_CYC\_FAULT\_FIRST\_DATE\_28   0
* A\_CYC\_FAULT\_LAST\_DATE\_28    0
* A\_CYC\_FAULT\_COUNT\_28    0
* A\_CYC\_FAULT\_ACTIVITY\_28     0
* A\_CYC\_FAULT\_CODE\_29     0
* A\_CYC\_FAULT\_SEVERITY\_29     0
* A\_CYC\_FAULT\_FIRST\_DATE\_29   0
* A\_CYC\_FAULT\_LAST\_DATE\_29    0
* A\_CYC\_FAULT\_COUNT\_29    0
* A\_CYC\_FAULT\_ACTIVITY\_29     0
* A\_CYC\_FAULT\_CODE\_30     0
* A\_CYC\_FAULT\_SEVERITY\_30     0
* A\_CYC\_FAULT\_FIRST\_DATE\_30   0
* A\_CYC\_FAULT\_LAST\_DATE\_30    0
* A\_CYC\_FAULT\_COUNT\_30    0
* A\_CYC\_FAULT\_ACTIVITY\_30     0
* A\_CYC\_FAULT\_CODE\_31     0
* A\_CYC\_FAULT\_SEVERITY\_31     0
* A\_CYC\_FAULT\_FIRST\_DATE\_31   0
* A\_CYC\_FAULT\_LAST\_DATE\_31    0
* A\_CYC\_FAULT\_COUNT\_31    0
* A\_CYC\_FAULT\_ACTIVITY\_31     0
* A\_CYC\_FAULT\_CODE\_32     0
* A\_CYC\_FAULT\_SEVERITY\_32     0
* A\_CYC\_FAULT\_FIRST\_DATE\_32   0
* A\_CYC\_FAULT\_LAST\_DATE\_32    0
* A\_CYC\_FAULT\_COUNT\_32    0
* A\_CYC\_FAULT\_ACTIVITY\_32     0
* A\_CYC\_FAULT\_CODE\_33     0
* A\_CYC\_FAULT\_SEVERITY\_33     0
* A\_CYC\_FAULT\_FIRST\_DATE\_33   0
* A\_CYC\_FAULT\_LAST\_DATE\_33    0
* A\_CYC\_FAULT\_COUNT\_33    0
* A\_CYC\_FAULT\_ACTIVITY\_33     0
    
    
* A\_CYC\_SCHEDULE\_MONDAY\_00    0
* A\_CYC\_SCHEDULE\_MONDAY\_01    0
* A\_CYC\_SCHEDULE\_MONDAY\_02    0
* A\_CYC\_SCHEDULE\_MONDAY\_03    0
* A\_CYC\_SCHEDULE\_MONDAY\_04    0
* A\_CYC\_SCHEDULE\_MONDAY\_05    0
* A\_CYC\_SCHEDULE\_MONDAY\_06    0
* A\_CYC\_SCHEDULE\_MONDAY\_07    0
* A\_CYC\_SCHEDULE\_MONDAY\_08    3
* A\_CYC\_SCHEDULE\_MONDAY\_09    2
* A\_CYC\_SCHEDULE\_MONDAY\_10    0
* A\_CYC\_SCHEDULE\_MONDAY\_11    0
* A\_CYC\_SCHEDULE\_MONDAY\_12    0
* A\_CYC\_SCHEDULE\_MONDAY\_13    0
* A\_CYC\_SCHEDULE\_MONDAY\_14    0
* A\_CYC\_SCHEDULE\_MONDAY\_15    0
* A\_CYC\_SCHEDULE\_MONDAY\_16    3
* A\_CYC\_SCHEDULE\_MONDAY\_17    1
* A\_CYC\_SCHEDULE\_MONDAY\_18    0
* A\_CYC\_SCHEDULE\_MONDAY\_19    0
* A\_CYC\_SCHEDULE\_MONDAY\_20    0
* A\_CYC\_SCHEDULE\_MONDAY\_21    0
* A\_CYC\_SCHEDULE\_MONDAY\_22    0
* A\_CYC\_SCHEDULE\_MONDAY\_23    0
* A\_CYC\_SCHEDULE\_TUESDAY\_00   0
* A\_CYC\_SCHEDULE\_TUESDAY\_01   0
* A\_CYC\_SCHEDULE\_TUESDAY\_02   0
* A\_CYC\_SCHEDULE\_TUESDAY\_03   0
* A\_CYC\_SCHEDULE\_TUESDAY\_04   0
* A\_CYC\_SCHEDULE\_TUESDAY\_05   0
* A\_CYC\_SCHEDULE\_TUESDAY\_06   0
* A\_CYC\_SCHEDULE\_TUESDAY\_07   0
* A\_CYC\_SCHEDULE\_TUESDAY\_08   3
* A\_CYC\_SCHEDULE\_TUESDAY\_09   2
* A\_CYC\_SCHEDULE\_TUESDAY\_10   0
* A\_CYC\_SCHEDULE\_TUESDAY\_11   0
* A\_CYC\_SCHEDULE\_TUESDAY\_12   0
* A\_CYC\_SCHEDULE\_TUESDAY\_13   0
* A\_CYC\_SCHEDULE\_TUESDAY\_14   0
* A\_CYC\_SCHEDULE\_TUESDAY\_15   0
* A\_CYC\_SCHEDULE\_TUESDAY\_16   3
* A\_CYC\_SCHEDULE\_TUESDAY\_17   1
* A\_CYC\_SCHEDULE\_TUESDAY\_18   0
* A\_CYC\_SCHEDULE\_TUESDAY\_19   0
* A\_CYC\_SCHEDULE\_TUESDAY\_20   0
* A\_CYC\_SCHEDULE\_TUESDAY\_21   0
* A\_CYC\_SCHEDULE\_TUESDAY\_22   0
* A\_CYC\_SCHEDULE\_TUESDAY\_23   0
* A\_CYC\_SCHEDULE\_WEDNESDAY\_00     0
* A\_CYC\_SCHEDULE\_WEDNESDAY\_01     0
* A\_CYC\_SCHEDULE\_WEDNESDAY\_02     0
* A\_CYC\_SCHEDULE\_WEDNESDAY\_03     0
* A\_CYC\_SCHEDULE\_WEDNESDAY\_04     0
* A\_CYC\_SCHEDULE\_WEDNESDAY\_05     0
* A\_CYC\_SCHEDULE\_WEDNESDAY\_06     0
* A\_CYC\_SCHEDULE\_WEDNESDAY\_07     0
* A\_CYC\_SCHEDULE\_WEDNESDAY\_08     0
* A\_CYC\_SCHEDULE\_WEDNESDAY\_09     0
* A\_CYC\_SCHEDULE\_WEDNESDAY\_10     0
* A\_CYC\_SCHEDULE\_WEDNESDAY\_11     0
* A\_CYC\_SCHEDULE\_WEDNESDAY\_12     0
* A\_CYC\_SCHEDULE\_WEDNESDAY\_13     0
* A\_CYC\_SCHEDULE\_WEDNESDAY\_14     0
* A\_CYC\_SCHEDULE\_WEDNESDAY\_15     0
* A\_CYC\_SCHEDULE\_WEDNESDAY\_16     0
* A\_CYC\_SCHEDULE\_WEDNESDAY\_17     0
* A\_CYC\_SCHEDULE\_WEDNESDAY\_18     0
* A\_CYC\_SCHEDULE\_WEDNESDAY\_19     0
* A\_CYC\_SCHEDULE\_WEDNESDAY\_20     0
* A\_CYC\_SCHEDULE\_WEDNESDAY\_21     0
* A\_CYC\_SCHEDULE\_WEDNESDAY\_22     0
* A\_CYC\_SCHEDULE\_WEDNESDAY\_23     0
* A\_CYC\_SCHEDULE\_THURSDAY\_00  0
* A\_CYC\_SCHEDULE\_THURSDAY\_01  0
* A\_CYC\_SCHEDULE\_THURSDAY\_02  0
* A\_CYC\_SCHEDULE\_THURSDAY\_03  0
* A\_CYC\_SCHEDULE\_THURSDAY\_04  0
* A\_CYC\_SCHEDULE\_THURSDAY\_05  0
* A\_CYC\_SCHEDULE\_THURSDAY\_06  0
* A\_CYC\_SCHEDULE\_THURSDAY\_07  0
* A\_CYC\_SCHEDULE\_THURSDAY\_08  0
* A\_CYC\_SCHEDULE\_THURSDAY\_09  0
* A\_CYC\_SCHEDULE\_THURSDAY\_10  0
* A\_CYC\_SCHEDULE\_THURSDAY\_11  0
* A\_CYC\_SCHEDULE\_THURSDAY\_12  0
* A\_CYC\_SCHEDULE\_THURSDAY\_13  0
* A\_CYC\_SCHEDULE\_THURSDAY\_14  0
* A\_CYC\_SCHEDULE\_THURSDAY\_15  0
* A\_CYC\_SCHEDULE\_THURSDAY\_16  0
* A\_CYC\_SCHEDULE\_THURSDAY\_17  0
* A\_CYC\_SCHEDULE\_THURSDAY\_18  0
* A\_CYC\_SCHEDULE\_THURSDAY\_19  0
* A\_CYC\_SCHEDULE\_THURSDAY\_20  0
* A\_CYC\_SCHEDULE\_THURSDAY\_21  0
* A\_CYC\_SCHEDULE\_THURSDAY\_22  0
* A\_CYC\_SCHEDULE\_THURSDAY\_23  0
* A\_CYC\_SCHEDULE\_FRIDAY\_00    0
* A\_CYC\_SCHEDULE\_FRIDAY\_01    0
* A\_CYC\_SCHEDULE\_FRIDAY\_02    0
* A\_CYC\_SCHEDULE\_FRIDAY\_03    0
* A\_CYC\_SCHEDULE\_FRIDAY\_04    0
* A\_CYC\_SCHEDULE\_FRIDAY\_05    0
* A\_CYC\_SCHEDULE\_FRIDAY\_06    0
* A\_CYC\_SCHEDULE\_FRIDAY\_07    0
* A\_CYC\_SCHEDULE\_FRIDAY\_08    0
* A\_CYC\_SCHEDULE\_FRIDAY\_09    0
* A\_CYC\_SCHEDULE\_FRIDAY\_10    0
* A\_CYC\_SCHEDULE\_FRIDAY\_11    0
* A\_CYC\_SCHEDULE\_FRIDAY\_12    0
* A\_CYC\_SCHEDULE\_FRIDAY\_13    0
* A\_CYC\_SCHEDULE\_FRIDAY\_14    0
* A\_CYC\_SCHEDULE\_FRIDAY\_15    0
* A\_CYC\_SCHEDULE\_FRIDAY\_16    0
* A\_CYC\_SCHEDULE\_FRIDAY\_17    0
* A\_CYC\_SCHEDULE\_FRIDAY\_18    0
* A\_CYC\_SCHEDULE\_FRIDAY\_19    0
* A\_CYC\_SCHEDULE\_FRIDAY\_20    0
* A\_CYC\_SCHEDULE\_FRIDAY\_21    0
* A\_CYC\_SCHEDULE\_FRIDAY\_22    0
* A\_CYC\_SCHEDULE\_FRIDAY\_23    0
* A\_CYC\_SCHEDULE\_SATURDAY\_00  0
* A\_CYC\_SCHEDULE\_SATURDAY\_01  0
* A\_CYC\_SCHEDULE\_SATURDAY\_02  0
* A\_CYC\_SCHEDULE\_SATURDAY\_03  0
* A\_CYC\_SCHEDULE\_SATURDAY\_04  0
* A\_CYC\_SCHEDULE\_SATURDAY\_05  0
* A\_CYC\_SCHEDULE\_SATURDAY\_06  0
* A\_CYC\_SCHEDULE\_SATURDAY\_07  0
* A\_CYC\_SCHEDULE\_SATURDAY\_08  0
* A\_CYC\_SCHEDULE\_SATURDAY\_09  0
* A\_CYC\_SCHEDULE\_SATURDAY\_10  0
* A\_CYC\_SCHEDULE\_SATURDAY\_11  0
* A\_CYC\_SCHEDULE\_SATURDAY\_12  0
* A\_CYC\_SCHEDULE\_SATURDAY\_13  0
* A\_CYC\_SCHEDULE\_SATURDAY\_14  0
* A\_CYC\_SCHEDULE\_SATURDAY\_15  0
* A\_CYC\_SCHEDULE\_SATURDAY\_16  0
* A\_CYC\_SCHEDULE\_SATURDAY\_17  0
* A\_CYC\_SCHEDULE\_SATURDAY\_18  0
* A\_CYC\_SCHEDULE\_SATURDAY\_19  0
* A\_CYC\_SCHEDULE\_SATURDAY\_20  0
* A\_CYC\_SCHEDULE\_SATURDAY\_21  0
* A\_CYC\_SCHEDULE\_SATURDAY\_22  0
* A\_CYC\_SCHEDULE\_SATURDAY\_23  0
* A\_CYC\_SCHEDULE\_SUNDAY\_00    0
* A\_CYC\_SCHEDULE\_SUNDAY\_01    0
* A\_CYC\_SCHEDULE\_SUNDAY\_02    0
* A\_CYC\_SCHEDULE\_SUNDAY\_03    0
* A\_CYC\_SCHEDULE\_SUNDAY\_04    0
* A\_CYC\_SCHEDULE\_SUNDAY\_05    0
* A\_CYC\_SCHEDULE\_SUNDAY\_06    0
* A\_CYC\_SCHEDULE\_SUNDAY\_07    0
* A\_CYC\_SCHEDULE\_SUNDAY\_08    0
* A\_CYC\_SCHEDULE\_SUNDAY\_09    0
* A\_CYC\_SCHEDULE\_SUNDAY\_10    0
* A\_CYC\_SCHEDULE\_SUNDAY\_11    0
* A\_CYC\_SCHEDULE\_SUNDAY\_12    0
* A\_CYC\_SCHEDULE\_SUNDAY\_13    0
* A\_CYC\_SCHEDULE\_SUNDAY\_14    0
* A\_CYC\_SCHEDULE\_SUNDAY\_15    0
* A\_CYC\_SCHEDULE\_SUNDAY\_16    0
* A\_CYC\_SCHEDULE\_SUNDAY\_17    0
* A\_CYC\_SCHEDULE\_SUNDAY\_18    0
* A\_CYC\_SCHEDULE\_SUNDAY\_19    0
* A\_CYC\_SCHEDULE\_SUNDAY\_20    0
* A\_CYC\_SCHEDULE\_SUNDAY\_21    0
* A\_CYC\_SCHEDULE\_SUNDAY\_22    0
* A\_CYC\_SCHEDULE\_SUNDAY\_23    0
