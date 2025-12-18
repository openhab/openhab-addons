# Bluelink Binding (Hyundai/Genesis)

This binding integrates Hyundai vehicles equipped with Bluelink connected car services.
It allows you to monitor your vehicle's status and control various features remotely.

Genesis vehicles may also work, though this has not been tested.

**NOTE: The binding only support the US region at the moment, because the Bluelink API
differs by region.**

## Supported Things

| Thing Type | Description                                  |
|------------|----------------------------------------------|
| `account`  | Bridge representing your Bluelink account    |
| `vehicle`  | A vehicle connected to your Bluelink account |

## Discovery

Once an account bridge is configured and online, the binding will automatically discover vehicles
registered to your account.

## Thing Configuration

### `account` Bridge

| Parameter  | Required | Description                                              |
|------------|----------|----------------------------------------------------------|
| `username` | Yes      | Bluelink account email                                   |
| `password` | Yes      | Bluelink account password                                |
| `pin`      | No       | Bluelink service PIN (required for lock/unlock commands) |
| `region`   | No       | Country code (`US`), autodetected if absent              |

### `vehicle` Thing

| Parameter              | Required | Default | Description                                       |
|------------------------|----------|---------|---------------------------------------------------|
| `vin`                  | Yes      | -       | Vehicle Identification Number (VIN)               |
| `refreshInterval`      | No       | 30      | Status refresh interval in minutes (cached)       |
| `forceRefreshInterval` | No       | 240     | Status refresh interval in minutes (from vehicle) |

The `refreshInterval` parameter controls how often cached data is fetched from Kia/Hyundai servers.
In contrast, `forceRefreshInterval` controls fetching of data from the vehicle.
If this is too aggressive, you will quickly run into the daily rate limit.

Since we cannot fetch up-to-date data very often, the binding will often reflect stale data.
The Force Refresh action can be used to refresh on demand.

## Actions

Vehicle things support the following actions:

| Action         | Parameters                                                 | Description                                                |
|----------------|------------------------------------------------------------|------------------------------------------------------------|
| Force Refresh  | -                                                          | Fetch up-to-date data from the vehicle and update channels |
| Climate Start  | temperature, heated features, defrost, engine run duration | Start climate control                                      |
| Climate Stop   | -                                                          | Stop climate control                                       |
| Lock Vehicle   | -                                                          |                                                            |
| Unlock Vehicle | -                                                          |                                                            |
| Start Charging | -                                                          |                                                            |
| Stop Charging  | -                                                          |                                                            |

## Channels

### Status Group

| Channel          | Type                 | Read/Write | Description                                |
|------------------|----------------------|------------|--------------------------------------------|
| `locked`         | Switch               | R          | Door lock status (ON=locked, OFF=unlocked) |
| `engine-running` | Switch               | R          | Engine running status                      |
| `odometer`       | Number:Length        | R          | Odometer reading                           |
| `battery-level`  | Number:Dimensionless | R          | 12V battery state of charge (%)            |
| `location`       | Location             | R          | GPS coordinates                            |
| `last-update`    | DateTime             | R          | Last vehicle data update time              |

### Doors Group

| Channel       | Type    | Read/Write | Description            |
|---------------|---------|------------|------------------------|
| `front-left`  | Contact | R          | Front left door state  |
| `front-right` | Contact | R          | Front right door state |
| `rear-left`   | Contact | R          | Rear left door state   |
| `rear-right`  | Contact | R          | Rear right door state  |
| `trunk`       | Contact | R          | Trunk state            |
| `hood`        | Contact | R          | Hood state             |

### Windows Group

| Channel       | Type    | Read/Write | Description              |
|---------------|---------|------------|--------------------------|
| `front-left`  | Contact | R          | Front left window state  |
| `front-right` | Contact | R          | Front right window state |
| `rear-left`   | Contact | R          | Rear left window state   |
| `rear-right`  | Contact | R          | Rear right window state  |

### Climate Group

| Channel                | Type               | Read/Write | Description                  |
|------------------------|--------------------|------------|------------------------------|
| `hvac-on`              | Switch             | R          | Climate control status       |
| `temperature-setpoint` | Number:Temperature | R          | Cabin temperature setting    |
| `defrost`              | Switch             | R          | Defrost status               |
| `seat-front-left`      | Number             | R          | Driver seat heater (0-3)     |
| `seat-front-right`     | Number             | R          | Passenger seat heater (0-3)  |
| `seat-rear-left`       | Number             | R          | Rear left seat heater (0-3)  |
| `seat-rear-right`      | Number             | R          | Rear right seat heater (0-3) |
| `steering-heater`      | Switch             | R          | Steering wheel heater        |
| `rear-window-heater`   | Switch             | R          | Rear window heater           |
| `side-mirror-heater`   | Switch             | R          | Side mirror heater           |

### Range Group

| Channel       | Type          | Read/Write | Description             |
|---------------|---------------|------------|-------------------------|
| `total-range` | Number:Length | R          | Total driving range     |
| `ev-range`    | Number:Length | R          | EV-only driving range   |
| `fuel-range`  | Number:Length | R          | Fuel-only driving range |

### Fuel Group (ICE vehicles only)

This channel group is only available for internal combustion engine (ICE) vehicles.

| Channel            | Type                 | Read/Write | Description         |
|--------------------|----------------------|------------|---------------------|
| `level`            | Number:Dimensionless | R          | Fuel tank level (%) |
| `low-fuel-warning` | Switch               | R          | Low fuel warning    |

### Charging Group (Electric/Hybrid vehicles only)

This channel group is only available for electric and hybrid vehicles.

| Channel                 | Type                 | Read/Write | Description                     |
|-------------------------|----------------------|------------|---------------------------------|
| `soc`                   | Number:Dimensionless | R          | Battery state of charge (%)     |
| `charging`              | Switch               | R          | Charging status (ON to start)   |
| `plugged-in`            | Switch               | R          | Charge cable plugged in         |
| `charge-limit-dc`       | Number:Dimensionless | R          | DC charge limit (%)             |
| `charge-limit-ac`       | Number:Dimensionless | R          | AC charge limit (%)             |
| `time-to-full-current`  | Number:Time          | R          | Time to full (current charger)  |
| `time-to-full-fast`     | Number:Time          | R          | Time to full (fast charger)     |
| `time-to-full-portable` | Number:Time          | R          | Time to full (portable charger) |
| `time-to-full-station`  | Number:Time          | R          | Time to full (station charger)  |

### Warnings Group

| Channel             | Type   | Read/Write | Description               |
|---------------------|--------|------------|---------------------------|
| `tire-pressure`     | Switch | R          | Tire pressure warning     |
| `washer-fluid`      | Switch | R          | Washer fluid low warning  |
| `brake-fluid`       | Switch | R          | Brake fluid warning       |
| `smart-key-battery` | Switch | R          | Smart key battery warning |

## Full Example

### `bluelink.things` Example

```java
Bridge bluelink:account:myaccount "Bluelink Account" [ username="your@email.com", password="yourpassword", pin="1234" ] {
    Thing vehicle ioniq5 "Ioniq 5" [ vin="KMXXXXXXXXXXXXXXX", refreshInterval=10 ]
}
```

### `bluelink.items` Example

```java
// Status
Switch        Car_Locked     "Locked [%s]"                  <lock>       { channel="bluelink:vehicle:myaccount:ioniq5:status#locked" }
Switch        Car_Engine     "Engine [%s]"                  <engine>     { channel="bluelink:vehicle:myaccount:ioniq5:status#engine-running" }
Number:Length Car_Odometer   "Odometer [%.0f %unit%]"       <car>        { channel="bluelink:vehicle:myaccount:ioniq5:status#odometer", unit="mi" }
Location Car_Location        "Location [%2$s°N %3$s°E %1$sm]" <location> { channel="bluelink:vehicle:myaccount:ioniq5:status#location" }
DateTime Car_Last_Update     "Last Update [%1$ta %1$tR]"                 { channel="bluelink:vehicle:myaccount:ioniq5:status#last-update" }
Number  Car_12V_Battery      "12V Battery [%.0f %%]"        <battery>    { channel="bluelink:vehicle:myaccount:ioniq5:status#battery-level" }

// Doors
Contact Car_Door_FL          "Front Left Door [%s]"         <door>       { channel="bluelink:vehicle:myaccount:ioniq5:doors#front-left" }
Contact Car_Door_FR          "Front Right Door [%s]"        <door>       { channel="bluelink:vehicle:myaccount:ioniq5:doors#front-right" }
Contact Car_Door_RL          "Rear Left Door [%s]"          <door>       { channel="bluelink:vehicle:myaccount:ioniq5:doors#rear-left" }
Contact Car_Door_RR          "Rear Right Door [%s]"         <door>       { channel="bluelink:vehicle:myaccount:ioniq5:doors#rear-right" }
Contact Car_Trunk            "Trunk [%s]"                   <door>       { channel="bluelink:vehicle:myaccount:ioniq5:doors#trunk" }
Contact Car_Hood             "Hood [%s]"                    <door>       { channel="bluelink:vehicle:myaccount:ioniq5:doors#hood" }

// Windows
Contact Car_Window_FL          "Front Left Window [%s]"     <window>       { channel="bluelink:vehicle:myaccount:ioniq5:windows#front-left" }
Contact Car_Window_FR          "Front Right Window [%s]"    <window>       { channel="bluelink:vehicle:myaccount:ioniq5:windows#front-right" }
Contact Car_Window_RL          "Rear Left Window [%s]"      <window>       { channel="bluelink:vehicle:myaccount:ioniq5:windows#rear-left" }
Contact Car_Window_RR          "Rear Right Window [%s]"     <window>       { channel="bluelink:vehicle:myaccount:ioniq5:windows#rear-right" }

// Climate
Number:Temperature Car_Temp  "Temperature setpoint [%.0f %unit%]" <temperature> { channel="bluelink:vehicle:myaccount:ioniq5:climate#temperature-setpoint" }
Switch  Car_Climate          "Climate [%s]"                 <climate>    { channel="bluelink:vehicle:myaccount:ioniq5:climate#hvac-on" }
Switch  Car_Defrost          "Defrost [%s]"                 <climate>    { channel="bluelink:vehicle:myaccount:ioniq5:climate#defrost" }
Number  Car_Heat_Seat_FL     "Front Left Seat Heater [%d]"  <climate>    { channel="bluelink:vehicle:myaccount:ioniq5:climate#seat-front-left" }
Number  Car_Heat_Seat_FR     "Front Right Seat Heater [%d]" <climate>    { channel="bluelink:vehicle:myaccount:ioniq5:climate#seat-front-right" }
Number  Car_Heat_Seat_RL     "Rear Left Seat Heater [%d]"   <climate>    { channel="bluelink:vehicle:myaccount:ioniq5:climate#seat-rear-left" }
Number  Car_Heat_Seat_RR     "Rear Right Seat Heater [%d]"  <climate>    { channel="bluelink:vehicle:myaccount:ioniq5:climate#seat-rear-right" }
Switch  Car_Heat_Steering    "Steering Wheel Heater [%s]"   <climate>    { channel="bluelink:vehicle:myaccount:ioniq5:climate#steering-heater" }
Switch  Car_Heat_Rear_Window "Rear Window Heater [%s]"      <climate>    { channel="bluelink:vehicle:myaccount:ioniq5:climate#rear-window-heater" }
Switch  Car_Heat_Side_Mirror "Side Mirror Heater [%s]"      <climate>    { channel="bluelink:vehicle:myaccount:ioniq5:climate#side-mirror-heater" }

// Range
Number:Length Car_Range      "Range [%.0f %unit%]"          <car>        { channel="bluelink:vehicle:myaccount:ioniq5:range#total-range", unit="mi" }
Number:Length Car_EV_Range   "EV Range [%.0f %unit%]"       <car>        { channel="bluelink:vehicle:myaccount:ioniq5:range#ev-range", unit="mi" }
Number:Length Car_Fuel_Range "Fuel Range [%.0f %unit%]"     <car>        { channel="bluelink:vehicle:myaccount:ioniq5:range#fuel-range", unit="mi" }

// Fuel (ICE vehicles only)
Number  Car_Fuel_Level       "Fuel Level [%.0f %%]"                      { channel="bluelink:vehicle:myaccount:ioniq5:fuel#level" }
Switch  Car_Fuel_Low         "Fuel Low [%s]"                             { channel="bluelink:vehicle:myaccount:ioniq5:fuel#low-fuel-warning" }

// Charging (EV/Hybrid vehicles only)
Number  Car_EV_SOC           "EV Battery [%.0f %%]"         <battery>    { channel="bluelink:vehicle:myaccount:ioniq5:charging#soc" }
Switch  Car_Charging         "Charging [%s]"                <battery>    { channel="bluelink:vehicle:myaccount:ioniq5:charging#charging" }
Switch  Car_PluggedIn        "Plugged In [%s]"              <poweroutlet> { channel="bluelink:vehicle:myaccount:ioniq5:charging#plugged-in" }
Number  Car_Charge_Limit_DC  "DC Charge Limit [%.0f %%]"    <battery>    { channel="bluelink:vehicle:myaccount:ioniq5:charging#charge-limit-dc" }
Number  Car_Charge_Limit_AC  "AC Charge Limit [%.0f %%]"    <battery>    { channel="bluelink:vehicle:myaccount:ioniq5:charging#charge-limit-ac" }
Number:Time Car_Time_To_Full_Current  "Time To Full (current charger) [%s]" { channel="bluelink:vehicle:myaccount:ioniq5:charging#time-to-full-current" }
Number:Time Car_Time_To_Full_Fast     "Time To Full (fast charger) [%s]" { channel="bluelink:vehicle:myaccount:ioniq5:charging#time-to-full-fast" }
Number:Time Car_Time_To_Full_Portable "Time To Full (portable charger) [%s]" { channel="bluelink:vehicle:myaccount:ioniq5:charging#time-to-full-portable" }
Number:Time Car_Time_To_Full_Station  "Time To Full (station charger) [%s]" { channel="bluelink:vehicle:myaccount:ioniq5:charging#time-to-full-station" }

// Warnings
Switch Car_Washer_Fluid_Warning  "Washer Fluid Warning [%s]"             { channel="bluelink:vehicle:myaccount:ioniq5:warnings#washer-fluid" }
Switch Car_Brake_Fluid_Warning   "Brake Fluid Warning [%s]"              { channel="bluelink:vehicle:myaccount:ioniq5:warnings#brake-fluid" }
Switch Car_Key_Battery_Warning   "Smart Key Battery Warning [%s]"        { channel="bluelink:vehicle:myaccount:ioniq5:warnings#smart-key-battery" }
Switch Car_Tire_Pressure_Warning "Tire Pressure Warning [%s]"            { channel="bluelink:vehicle:myaccount:ioniq5:warnings#tire-pressure" }
Switch Car_Tire_Pressure_Warning_FR "Tire Pressure Warning (Front Right) [%s]" { channel="bluelink:vehicle:myaccount:ioniq5:warnings#tire-pressure-front-right" }
Switch Car_Tire_Pressure_Warning_FL "Tire Pressure Warning (Front Left) [%s]" { channel="bluelink:vehicle:myaccount:ioniq5:warnings#tire-pressure-front-left" }
Switch Car_Tire_Pressure_Warning_RR "Tire Pressure Warning (Rear Right) [%s]" { channel="bluelink:vehicle:myaccount:ioniq5:warnings#tire-pressure-rear-right" }
Switch Car_Tire_Pressure_Warning_RL "Tire Pressure Warning (Rear Left) [%s]" { channel="bluelink:vehicle:myaccount:ioniq5:warnings#tire-pressure-rear-left" }
```
