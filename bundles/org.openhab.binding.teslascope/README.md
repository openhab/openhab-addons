# Teslascope Binding

This binding integrates [Tesla Electric Vehicles](https://www.tesla.com).
The integration happens through the [Teslascope](https://www.teslascope.com) API.

## Supported Things

All Tesla vehicles supported by Teslascope are supported.
Each vehicle is represented by its own `vehicle` Thing.

## Discovery

After (manually) adding a Teslascope Account bridge, registered vehicles will be auto discovered.

## `account` Bridge Configuration

Account configuration is necessary.
The easiest way to do this is from the UI.
Just add a new thing, select the Teslascope binding, then Teslascope Account Binding Thing, and enter the apiKey from the Teslascope website.

As a minimum, the apiKey is needed:

| Thing Parameter | Default Value | Required | Advanced | Description                                                                          |
|-----------------|---------------|----------|----------|--------------------------------------------------------------------------------------|
| apiKey          | N/A           | Yes      | No       | apiKey provided by Teslascope                                                        |
| refreshInterval | 60            | No       | Yes      | The frequency with which to refresh information from Teslascope specified in seconds |

## `vehicle` Thing Configuration

As a minimum, the publicID is needed:

| Thing Parameter | Default Value | Required | Advanced | Description                                                                          |
|-----------------|---------------|----------|----------|--------------------------------------------------------------------------------------|
| publicID        | N/A           | Yes      | No       | Vehicle Public ID listed in Teslascope                                               |
| refreshInterval | 60            | No       | Yes      | The frequency with which to refresh information from Teslascope specified in seconds |

## Channels

All vehicles support a huge number of channels - the following list shows the standard ones:

| Channel ID          | Item Type            | Label               | Description                                                                                 |
|---------------------|----------------------|---------------------|---------------------------------------------------------------------------------------------|
| auto-conditioning   | Switch               | Auto Conditioning   | Turns on auto-conditioning (a/c or heating)                                                 |
| battery-level       | Number:Dimensionless | Battery Level       | State of the battery in %                                                                   |
| charging-state      | String               | Charging State      | “Starting”, “Complete”, “Charging”, “Disconnected”, “Stopped”, “NoPower”                    |
| charge-port         | Switch               | Charge Port         | Open the Charge Port (ON) or indicates the state of the Charge Port (ON/OFF if Open/Closed) |
| climate             | Switch               | Climate             | Climate status indicator                                                                    |
| door-lock           | Switch               | Door Lock           | Lock or unlock the car                                                                      |
| inside-temp         | Number:Temperature   | Inside Temperature  | Indicates the inside temperature of the vehicle                                             |
| located-at-home     | Switch               | Located at Home     | Indicates if the vehicle is located at the address set as Home                              |
| located-at-work     | Switch               | Located at Work     | Indicates if the vehicle is located at the address set as Work                              |
| located-at-favorite | Switch               | Located at Favorite | Indicates if the vehicle is located at an address set as a Favorite                         |
| location            | Location             | Location            | The actual position of the vehicle                                                          |
| odometer            | Number:Length        | Odometer            | Odometer of the vehicle                                                                     |
| speed               | Number:Speed         | Speed               | Vehicle speed                                                                               |
| vin                 | String               | VIN                 | Vehicle Identification Number                                                               |
| vehicle-name        | String               | Name                | Vehicle Name                                                                                |
| vehicle-state       | String               | Vehicle State       | Vehicle State                                                                               |

Additionally, these advanced channels are available (not all are available on all vehicle types, e.g., the sunroof):

| Channel ID                  | Item Type                | Label                         | Description                                                                                              |
|-----------------------------|--------------------------|-------------------------------|----------------------------------------------------------------------------------------------------------|
| battery-range               | Number:Length            | Battery Range                 | Range of the battery                                                                                     |
| car-version                 | String                   | Car Version                   | Current software version of the car                                                                      |
| center-rear-seat-heater     | Switch                   | Center Rear Seat Heater       | Indicates if the center rear seat heater is switched on                                                  |
| charge                      | Switch                   | Charge                        | Start (ON) or stop (OFF) charging                                                                        |
| charge-amps                 | Number:ElectricCurrent   | Charger Current               | Current actually delivered by the charger                                                                |
| charge-current-request      | Number:ElectricCurrent   | Charger Current Requested     | Current requested from the charger                                                                       |
| charge-current-request-max  | Number:ElectricCurrent   | Max Charger Current Supported | Maximum current supported by the charger                                                                 |
| charge-energy-added         | Number:Energy            | Charge Energy Added           | Energy added, in kWh, during the last charging session                                                   |
| charge-limit-soc            | Number:Dimensionless     | Charge Limit SOC              | Current charging limit of the vehicle, in %                                                              |
| charge-limit-soc-min        | Number:Dimensionless     | Charge Limit SOC Min          | Minimum charging limit of the vehicle, in %                                                              |
| charge-limit-soc-max        | Number:Dimensionless     | Charge Limit SOC Max          | Maximum charging limit of the vehicle, in %                                                              |
| charge-limit-soc-standard   | Number:Dimensionless     | Charge Limit SOC Standard     | Standard charging limit of the vehicle, in %                                                             |
| charge-port-latch           | Switch                   | Charge Port Latch             | Indicate the Charge Port Latch status, (ON/OFF if latched/unlatched)                                     |
| charge-rate                 | Number:Speed             | Charge Rate                   | Distance per hour charging rate                                                                          |
| charger-power               | Number:Power             | Charger Power                 | Power actually delivered by the charger                                                                  |
| charger-voltage             | Number:ElectricPotential | Charger Voltage               | Voltage (V) actually presented by the charger                                                            |
| driver-front-door           | Contact                  | Driver Front Door             | Indicates if the front door at the driver's side is open                                                 |
| driver-rear-door            | Contact                  | Driver Rear Door              | Indicates if the rear door at the driver's side is open                                                  |
| driver-temp                 | Number:Temperature       | Driver Temperature            | Indicates the auto conditioning temperature set at the driver's side                                     |
| estimated-battery-range     | Number:Length            | Estimated Battery Range       | Estimated battery range                                                                                  |
| fan                         | Number                   | Fan                           | Indicates the speed (0-7) of the fan                                                                     |
| flash-lights                | Switch                   | Flash Lights                  | Flash the lights of the car (when ON is received)                                                        |
| front-defroster             | Switch                   | Front Defroster               | Indicates if the front defroster is enabled                                                              |
| front-trunk                 | Switch                   | Front Trunk                   | Indicates if the front trunk is opened, or open the front trunk when ON is received                      |
| heading                     | Number:Angle             | Heading                       | Indicates the (compass) heading of the car, in 0-360 degrees                                             |
| honk-horn                   | Switch                   | Honk the Horn                 | Honk the horn of the vehicle, when ON is received                                                        |
| homelink                    | Switch                   | Homelink Nearby               | Indicates if the Home Link is nearby                                                                     |
| left-temp-direction         | Number                   | Left Temperature Direction    | Not documented / To be defined                                                                           |
| location                    | Location                 | Location                      | The actual position of the vehicle                                                                       |
| left-seat-heater            | Switch                   | Left Seat Heater              | Indicates if the left seat heater is switched on                                                         |
| left-rear-seat-heater       | Switch                   | Left Rear Seat Heater         | Indicates if the left rear seat heater is switched on                                                    |
| left-rear-back-seat-heater  | Number                   | Left Rear Backseat Heater     | Indicates the level (0, 1, 2, or 3) of the left rear backseat heater                                     |
| min-available-temp          | Number:Temperature       | Minimum Temperature           | Indicates the minimal inside temperature of the vehicle                                                  |
| max-available-temp          | Number:Temperature       | Maximum Temperature           | Indicates the maximum inside temperature of the vehicle                                                  |
| outside-temp                | Number:Temperature       | Outside Temperature           | Indicates the outside temperature of the vehicle                                                         |
| passenger-temp              | Number                   | Passenger Temperature         | Indicates the auto conditioning temperature set at the passenger's side                                  |
| passenger-front-door        | Contact                  | Passenger Front Door          | Indicates if the front door at the passenger's side is opened                                            |
| passenger-rear-door         | Contact                  | Passenger Rear Door           | Indicates if the rear door at the passenger's side is opened                                             |
| power                       | Number:Power             | Power                         | Net kW flowing in (+) or out (-) of the battery                                                          |
| preconditioning             | Switch                   | Preconditioning               | Indicates if preconditioning is activated                                                                |
| rear-defroster              | Switch                   | Rear Defroster                | Indicates if the rear defroster is enabled                                                               |
| rear-trunk                  | Switch                   | Rear Trunk                    | Indicates if the rear trunk is opened, or open/close the rear trunk when ON/OFF is received              |
| right-seat-heater           | Switch                   | Right Seat Heater             | Indicates if the right seat heater is switched on                                                        |
| right-rear-seat-heater      | Switch                   | Right Rear Seat Heater        | Indicates if the right rear seat heater is switched on                                                   |
| right-rear-back-seat-heater | Number                   | Right Rear Backseat Heater    | Indicates the level (0, 1, 2, or 3) of the right rear backseat heater                                    |
| right-temp-direction        | Number                   | Right Temperature Direction   | Not documented / To be defined                                                                           |
| scheduled-charging-pending  | Switch                   | Scheduled Charging Pending    | Indicates if a scheduled charging session is still pending                                               |
| scheduled-charging-start    | DateTime                 | Scheduled Charging Start      | Indicates when the scheduled charging session will start, in yyyy-MM-dd'T'HH:mm:ss format                |
| sentry-mode                 | Switch                   | Sentry Mode                   | Activates or deactivates sentry mode                                                                     |
| shift-state                 | String                   | Shift State                   | Indicates the state of the transmission, “P”, “D”, “R”, or “N”                                           |
| side-mirror-heaters         | Switch                   | Side Mirror Heaters           | Indicates if the side mirror heaters are switched on                                                     |
| smart-preconditioning       | Switch                   | Smart Preconditioning         | Indicates if smart preconditioning is switched on                                                        |
| software-update-available   | Switch                   | Update Available              | Car software or map update available, automatically generated on non-empty "update status"               |
| software-update-status      | String                   | Update Status                 | Car software or map update status, e.g. "downloading_wifi_wait", "installing"                            |
| software-update-version     | String                   | Update Version                | Car software or map version to update to, e.g. "2023.32.9", "EU-2023.32-14783" for map updates, or empty |
| steering-wheel-heater       | Switch                   | Steering Wheel Heater         | Turns On/Off the steering wheel heater                                                                   |
| sunroof-state               | String                   | Sunroof State                 | Valid states are “unknown”, “open”, “closed”, “vent”, “comfort”. Accepts commands "close" and "vent".    |
| sunroof                     | Dimmer                   | Sunroof                       | Indicates the opening state of the sunroof (0% closed, 100% fully open)                                  |
| time-to-full-charge         | Number                   | Time To Full Charge           | Number of hours to fully charge the battery                                                              |
| tpms-pressure-fl            | Number:Pressure          | Tyre Pressure FL              | Tyre Pressure Front Left in Bar                                                                          |
| tpms-pressure-fr            | Number:Pressure          | Tyre Pressure FR              | Tyre Pressure Front Right in Bar                                                                         |
| tpms-pressure-rl            | Number:Pressure          | Tyre Pressure RL              | Tyre Pressure Rear Left in Bar                                                                           |
| tpms-pressure-rr            | Number:Pressure          | Tyre Pressure RR              | Tyre Pressure Rear Right in Bar                                                                          |
| tpms-soft-warning-fl        | Switch                   | Tyre Pressure Soft Warning FL | Tyre Pressure Soft Warning Front Left                                                                    |
| tpms-soft-warning-fr        | Switch                   | Tyre Pressure Soft Warning FR | Tyre Pressure Soft Warning Front Right                                                                   |
| tpms-soft-warning-rl        | Switch                   | Tyre Pressure Soft Warning RL | Tyre Pressure Soft Warning Rear Left                                                                     |
| tpms-soft-warning-rr        | Switch                   | Tyre Pressure Soft Warning RR | Tyre Pressure Soft Warning Rear Right                                                                    |
| usable-battery-level        | Number                   | Usable Battery Level          | Indicates the % of battery that can be used for vehicle functions like driving                           |
| valet-mode                  | Switch                   | Valet Mode                    | Enable or disable Valet Mode                                                                             |
| wiper-blade-heater          | Switch                   | Wiper Blade Heater            | Indicates if the wiper blade heater is switched on                                                       |

## Full Example

### `demo.things` Example

```java
Bridge teslascope:account:account [ apiKey="xxxx" ] {
    teslascope:vehicle:model3 [ apiKey="xxxx", publicID="aXb3" ]
}
```

### `example.items` Example

```java
String              TeslaVehicleName            {channel="teslascope:vehicle:model3:vehicle-name"}
String              TeslaVehicleState           {channel="teslascope:vehicle:model3:vehicle-state"}
String              TeslaVIN                    {channel="teslascope:vehicle:model3:vin"}
Number              TeslaSpeed                  {channel="teslascope:vehicle:model3:speed"}
String              TeslaShiftState             {channel="teslascope:vehicle:model3:shift-state"}
Number              TeslaOdometer               {channel="teslascope:vehicle:model3:odometer"}
Number              TeslaRange                  {channel="teslascope:vehicle:model3:range"}
Switch              TeslaHome                   {channel="teslascope:vehicle:model3:located-at-home"}
Switch              TeslaWork                   {channel="teslascope:vehicle:model3:located-at-work"}
Switch              TeslaFavorite               {channel="teslascope:vehicle:model3:located-at-favorite"}

Number              TeslaBatteryLevel           {channel="teslascope:vehicle:model3:battery-level"}
Number              TeslaPower                  {channel="teslascope:vehicle:model3:power"}
Number              TeslaBatteryRange           {channel="teslascope:vehicle:model3:battery-range"}
Number              TeslaEstBatteryRange        {channel="teslascope:vehicle:model3:estimated-battery-range"}
Switch              TeslaPreconditioning        {channel="teslascope:vehicle:model3:preconditioning"}
String              TeslaCurrentVersion         {channel="teslascope:vehicle:model3:car-version"}

Switch              TeslaCharge                 {channel="teslascope:vehicle:model3:charge"}

Number              TeslaChargeAmps             {channel="teslascope:vehicle:model3:charge-amps"}
Number              TeslaChargeCurrRequest      {channel="teslascope:vehicle:model3:charge-current-request"}
Number              TeslaChargeCurrRequestMax   {channel="teslascope:vehicle:model3:charge-current-request-max"}
Number              TeslaChargeLimitSOC         {channel="teslascope:vehicle:model3:charge-limit-soc"}
Number              TeslaChargeLimitSOCMin      {channel="teslascope:vehicle:model3:charge-limit-soc-min"}
Number              TeslaChargeLimitSOCMax      {channel="teslascope:vehicle:model3:charge-limit-soc-max"}
Number              TeslaChargeLimitSOCStd      {channel="teslascope:vehicle:model3:charge-limit-soc-std"}
Number              TeslaChargeRate             {channel="teslascope:vehicle:model3:charge-rate"}
String              TeslaChargingState          {channel="teslascope:vehicle:model3:charging-state"}
Switch              TeslaChargePort             {channel="teslascope:vehicle:model3:charge-port"}
Switch              TeslaChargePortLatch        {channel="teslascope:vehicle:model3:charge-port-latch"}
Number              TeslaChargerPower           {channel="teslascope:vehicle:model3:charger-power"}
Number              TeslaTimeToFullCharge       {channel="teslascope:vehicle:model3:time-to-full-charge"}

Number              TeslaChargerVoltage         {channel="teslascope:vehicle:model3:charger-voltage"}
Number              TeslaChargerPower           {channel="teslascope:vehicle:model3:charger-power"}

DateTime            TeslaScheduledChargingStart {channel="teslascope:vehicle:model3:scheduled-charging-start"}

Switch              TeslaDoorLock               {channel="teslascope:vehicle:model3:door-lock"}
Switch              TeslaHorn                   {channel="teslascope:vehicle:model3:honk-horn"}
Switch              TeslaSentry                 {channel="teslascope:vehicle:model3:sentry-mode"}
Switch              TeslaLights                 {channel="teslascope:vehicle:model3:flash-lights"}
Switch              TeslaValet                  {channel="teslascope:vehicle:model3:valet-mode"}

Switch              TeslaFrontDefrost           {channel="teslascope:vehicle:model3:front-defroster"}
Switch              TeslaRearDefrost            {channel="teslascope:vehicle:model3:rear-defroster"}
Switch              TeslaLeftSeatHeater         {channel="teslascope:vehicle:model3:left-seat-heater"}
Switch              TeslaRightSeatHeater        {channel="teslascope:vehicle:model3:right-seat-heater"}

Switch              TeslaHomelink               {channel="teslascope:vehicle:model3:homelink"}
Location            TeslaLocation               {channel="teslascope:vehicle:model3:location"}
Number              TeslaHeading                {channel="teslascope:vehicle:model3:heading"}

Switch              TeslaAutoconditioning       {channel="teslascope:vehicle:model3:auto-conditioning"}
Number:Temperature  TeslaTemperature            {channel="teslascope:vehicle:model3:temperature"}
Number:Temperature  TeslaInsideTemperature      {channel="teslascope:vehicle:model3:inside-temp"}
Number:Temperature  TeslaOutsideTemperature     {channel="teslascope:vehicle:model3:outside-temp"}

Number:Pressure     TeslaTPMSPressureFL         {channel="teslascope:vehicle:model3:tpms-pressure-fl"}
Number:Pressure     TeslaTPMSPressureFR         {channel="teslascope:vehicle:model3:tpms-pressure-fr"}
Number:Pressure     TeslaTPMSPressureRL         {channel="teslascope:vehicle:model3:tpms-pressure-rl"}
Number:Pressure     TeslaTPMSPressureRR         {channel="teslascope:vehicle:model3:tpms-pressure-rr"}

Switch              TeslaTPMSSoftWarningFL      {channel="teslascope:vehicle:model3:tpms-soft-warning-fl"}
Switch              TeslaTPMSSoftWarningFR      {channel="teslascope:vehicle:model3:tpms-soft-warning-fr"}
Switch              TeslaTPMSSoftWarningRL      {channel="teslascope:vehicle:model3:tpms-soft-warning-rl"}
Switch              TeslaTPMSSoftWarningRR      {channel="teslascope:vehicle:model3:tpms-soft-warning-rr"}
```

### `example.sitemap` Example

```perl
sitemap main label="Main"
{
    Text item=TeslaUsableBatteryLevel label="Car" icon="tesla" valuecolor=[<=20="red",>60="green"]
    {
        Frame
        {
            Text item=TeslaState label="State [%s]" icon=""
            Text item=TeslaHomelink label="Homelink Available[%s]" icon=""
            Text item=TeslaSpeed label="Speed [%.1f]"
            Text item=TeslaShiftState label="Shift State [%s]" icon=""
            Text item=TeslaShiftState
            Text item=TeslaOdometer label="Odometer [%.1f miles]"
            Text item=TeslaRange
            Text item=TeslaCurrentVersion
            Switch item=TeslaHome label="Vehicle at Home?"
            Switch item=TeslaWork label="Vehicle at Work?"
            Switch item=TeslaFavorite label="Vehicle at Favorite?"
        }
        Frame
        {
            Switch item=TeslaAutoconditioning label="Enable Heat or AC"
            Setpoint item=TeslaTemperature step=0.5 minValue=65 maxValue=78 label="Auto Conditioning Temperature [%.1f °F]"
            Text item=TeslaInsideTemperature label="Inside Temperature [%.1f °F]" valuecolor=[<=32="blue",>95="red"]
            Text item=TeslaOutsideTemperature label="Outside Temperature [%.1f °F]" valuecolor=[<=32="blue",>95="red"]
        }
        Frame
        {
            Text item=TeslaBatteryLevel
            Text item=TeslaPower
            Text item=TeslaBatteryRange label="Battery Range [%.1f miles]"
            Text item=TeslaEstBatteryRange label="Battery Est Range [%.1f miles]"
        }
        Frame
        {
            Switch item=TeslaCharge label="Charge"
            Text item=TeslaChargeAmps label="Current Charging rate in A"
            Text item=TeslaChargeCurrRequest label="Current Requested Charging rate in A"
            Text item=TeslaChargeCurrRequestMax label="Maximum Supported Charging Current in A"
            Slider item=TeslaChargeLimitSOC label="Charge Limit [%.0f]"
            Text item=TeslaChargeLimitSOCMin label="Minimum Charge Limit [%.0f]"
            Text item=TeslaChargeLimitSOCMax label="Maximum Charge Limit [%.0f]"
            Text item=TeslaChargeLimitSOCStd label="Standard Charge Limit [%.0f]"
            Text item=TeslaChargingState label="Charging State [%s]" icon=""
            Switch item=TeslaChargePort label="Charge Port Door Status"
            Switch item=TeslaChargePortLatch label="Charge Port Latch Status"
            Text item=TeslaTimeToFullCharge label="Time To Full Charge [%.1f hours]"
            Text item=TeslaPreconditioning label="Preconditioning [%s]" icon=""
            Text item=TeslaChargeRate label="Charge Rate [%d miles/hr]"
            Text item=TeslaScheduledChargingStart icon="time"
            Text item=TeslaChargerVoltage label="Charge Voltage [%.1f V]"
            Text item=TeslaChargerPower label="Charge Power [%.1f kW]"
        }
        Frame
        {
            Switch item=TeslaDoorLock label="Doorlock"
            Switch item=TeslaHorn label="Horn"
            Switch item=TeslaLights label="Lights"
            Switch item=TeslaValet label="Valet Mode"
            Switch item=TeslaSentry label="Sentry Mode"

            Switch item=TeslaFrontDefrost label="Defrost Front"
            Switch item=TeslaRearDefrost label="Defrost Rear"
            Switch item=TeslaLeftSeatHeater label="Seat Heat Left"
            Switch item=TeslaRightSeatHeater label="Seat Heat Right"
        }
        Frame
        {
            Mapview item=TeslaLocation height=10
        }
    }
}
```
