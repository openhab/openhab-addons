# Teslascope Binding

This binding integrates [Tesla Electrical Vehicles](https://www.tesla.com).

The integration happens through the [Teslascope](https://www.teslascope.com) API.

## Supported Things

All Tesla vehicles supported by Teslascope are supported..

## Discovery

Auto-discovery is not currently supported.

## Thing Configuration

As a minimum, the IP address is needed:

- `apiKey` - apiKey provided by Teslascope
- `publicID` - Vehicle Public ID listed in Teslascope
- `refreshInterval` - The frequency with which to refresh information from Teslascope specified in seconds. Defaults to 60 seconds.

## Channels

All vehicles support a huge number of channels - the following list shows the standard ones:

| Channel ID       | Item Type            | Label              | Description                                                                                 |
|------------------|----------------------|--------------------|---------------------------------------------------------------------------------------------|
| autoconditioning | Switch               | Auto Conditioning  | Turns on auto-conditioning (a/c or heating)                                                 |
| batterylevel     | Number:Dimensionless | Battery Level      | State of the battery in %                                                                   |
| chargingstate    | String               | Charging State     | “Starting”, “Complete”, “Charging”, “Disconnected”, “Stopped”, “NoPower”                    |
| chargeport       | Switch               | Charge Port        | Open the Charge Port (ON) or indicates the state of the Charge Port (ON/OFF if Open/Closed) |
| climate          | Switch               | Climate            | Climate status indicator                                                                    |
| doorlock         | Switch               | Door Lock          | Lock or unlock the car                                                                      |
| insidetemp       | Number:Temperature   | Inside Temperature | Indicates the inside temperature of the vehicle                                             |
| location         | Location             | Location           | The actual position of the vehicle                                                          |
| odometer         | Number:Length        | Odometer           | Odometer of the vehicle                                                                     |
| speed            | Number:Speed         | Speed              | Vehicle speed                                                                               |
| vin              | String               | VIN                | Vehicle Identification Number                                                               |
| vehicle-name     | String               | Name               | Vehicle Name                                                                                |
| vehicle-state    | String               | Vehicle State      | Vehicle State                                                                               |

Additionally, these advanced channels are available (not all are available on all vehicle types, e.g., the sunroof):

| Channel ID                  | Item Type                | Label                         | Description                                                                                                      |
|-----------------------------|--------------------------|-------------------------------|------------------------------------------------------------------------------------------------------------------|
| battery-ange                | Number:Length            | Battery Range                 | Range of the battery                                                                                             |
| center-rear-seat-heater     | Switch                   | Center Rear Seat Heater       | Indicates if the center rear seat heater is switched on                                                          |
| charge                      | Switch                   | Charge                        | Start (ON) or stop (OFF) charging                                                                                |
| charge-energy-added         | Number:Energy            | Charge Energy Added           | Energy added, in kWh, during the last charging session                                                           |
| charge-limit-soc-standard   | Dimmer                   | Charge Limit SOC Standard     | Standard charging limit of the vehicle, in %                                                                     |
| charge-rate                 | Number:Speed             | Charge Rate                   | Distance per hour charging rate                                                                                  |
| charger-power               | Number:Power             | Charger Power                 | Power actually delivered by the charger                                                                          |
| charger-voltage             | Number:ElectricPotential | Charger Voltage               | Voltage (V) actually presented by the charger                                                                    |
| driver-front-door           | Contact                  | Driver Front Door             | Indicates if the front door at the driver's side is open                                                         |
| driver-rear-door            | Contact                  | Driver Rear Door              | Indicates if the rear door at the driver's side is open                                                         |
| driver-temp                 | Number:Temperature       | Driver Temperature            | Indicates the auto conditioning temperature set at the driver's side                                             |
| estimated-battery-range     | Number:Length            | Estimated Battery Range       | Estimated battery range                                                                                          |
| fan                         | Number                   | Fan                           | Indicates the speed (0-7) of the fan                                                                             |
| flash-lights                | Switch                   | Flash Lights                  | Flash the lights of the car (when ON is received)                                                                |
| front-defroster             | Switch                   | Front Defroster               | Indicates if the front defroster is enabled                                                                      |
| front-trunk                 | Switch                   | Front Trunk                   | Indicates if the front trunk is opened, or open the front trunk when ON is received                              |
| heading                     | Number:Angle             | Heading                       | Indicates the (compass) heading of the car, in 0-360 degrees                                                     |
| honk-horn                   | Switch                   | Honk the Horn                 | Honk the horn of the vehicle, when ON is received                                                                |
| homelink                    | Switch                   | Homelink Nearby               | Indicates if the Home Link is nearby                                                                             |
| left-temp-direction         | Number                   | Left Temperature Direction    | Not documented / To be defined                                                                                   |
| location                    | Location                 | Location                      | The actual position of the vehicle                                                                               |
| left-seat-heater            | Switch                   | Left Seat Heater              | Indicates if the left seat heater is switched on                                                                 |
| left-rear-seat-heater       | Switch                   | Left Rear Seat Heater         | Indicates if the left rear seat heater is switched on                                                            |
| left-rear-back-seat-heater  | Number                   | Left Rear Backseat Heater     | Indicates the level (0, 1, 2, or 3) of the left rear backseat heater                                             |
| min-available-temp          | Number:Temperature       | Minimum Temperature           | Indicates the minimal inside temperature of the vehicle                                                          |
| max-available-temp          | Number:Temperature       | Maximum Temperature           | Indicates the maximum inside temperature of the vehicle                                                          |
| outside-temp                | Number:Temperature       | Outside Temperature           | Indicates the outside temperature of the vehicle                                                                 |
| passenger-temp              | Number                   | Passenger Temperature         | Indicates the auto conditioning temperature set at the passenger's side                                          |
| passenger-front-door        | Contact                  | Passenger Front Door          | Indicates if the front door at the passenger's side is opened                                                    |
| passenger-rear-door         | Contact                  | Passenger Rear Door           | Indicates if the rear door at the passenger's side is opened                                                     |
| power                       | Number:Power             | Power                         | Net kW flowing in (+) or out (-) of the battery                                                                  |
| preconditioning             | Switch                   | Preconditioning               | Indicates if preconditioning is activated                                                                        |
| rear-defroster              | Switch                   | Rear Defroster                | Indicates if the rear defroster is enabled                                                                       |
| rear-trunk                  | Switch                   | Rear Trunk                    | Indicates if the rear trunk is opened, or open/close the rear trunk when ON/OFF is received                      |
| right-seat-heater           | Switch                   | Right Seat Heater             | Indicates if the right seat heater is switched on                                                                |
| right-rear-seat-heater      | Switch                   | Right Rear Seat Heater        | Indicates if the right rear seat heater is switched on                                                           |
| right-rear-back-seat-heater | Number                   | Right Rear Backseat Heater    | Indicates the level (0, 1, 2, or 3) of the right rear backseat heater                                            |
| right-temp-direction        | Number                   | Right Temperature Direction   | Not documented / To be defined                                                                                   |
| scheduled-charging-pending  | Switch                   | Scheduled Charging Pending    | Indicates if a scheduled charging session is still pending                                                       |
| scheduled-charging-start    | DateTime                 | Scheduled Charging Start      | Indicates when the scheduled charging session will start, in yyyy-MM-dd'T'HH:mm:ss format                        |
| sentry-mode                 | Switch                   | Sentry Mode                   | Activates or deactivates sentry mode                                                                             |
| shift-state                 | String                   | Shift State                   | Indicates the state of the transmission, “P”, “D”, “R”, or “N”                                                   |
| side-mirror-heaters         | Switch                   | Side Mirror Heaters           | Indicates if the side mirror heaters are switched on                                                             |
| smart-preconditioning       | Switch                   | Smart Preconditioning         | Indicates if smart preconditioning is switched on                                                                |
| software-update-available   | Switch                   | Update Available              | Car software or map update available, automatically generated on non-empty "update status"                      |
| software-update-status      | String                   | Update Status                 | Car software or map update status, e.g. "downloading_wifi_wait", "installing"                                    |
| software-update-version     | String                   | Update Version                | Car software or map version to update to, e.g. "2023.32.9", "EU-2023.32-14783" for map updates, or empty         |
| steering-wheel-heater       | Switch                   | Steering Wheel Heater         | Turns On/Off the steering wheel heater                                                                           |
| sunroof-state               | String                   | Sunroof State                 | Valid states are “unknown”, “open”, “closed”, “vent”, “comfort”. Accepts commands "close" and "vent".            |
| sunroof                     | Dimmer                   | Sunroof                       | Indicates the opening state of the sunroof (0% closed, 100% fully open)                                          |
| time-to-full-charge         | Number                   | Time To Full Charge           | Number of hours to fully charge the battery                                                                      |
| tpms-pressure-fl            | Number:Pressure          | Tyre Pressure FL              | Tyre Pressure Front Left in Bar                                                                                  |
| tpms-pressure-fr            | Number:Pressure          | Tyre Pressure FR              | Tyre Pressure Front Right in Bar                                                                                 |
| tpms-pressure-rl            | Number:Pressure          | Tyre Pressure RL              | Tyre Pressure Rear Left in Bar                                                                                   |
| tpms-pressure-rr            | Number:Pressure          | Tyre Pressure RR              | Tyre Pressure Rear Right in Bar                                                                                  |
| tpms-soft-warning-fl        | Switch                   | Tyre Pressure Soft Warning FL | Tyre Pressure Soft Warning Front Left                                                                            |
| tpms-soft-warning-fr        | Switch                   | Tyre Pressure Soft Warning FR | Tyre Pressure Soft Warning Front Right                                                                           |
| tpms-soft-warning-rl        | Switch                   | Tyre Pressure Soft Warning RL | Tyre Pressure Soft Warning Rear Left                                                                             |
| tpms-soft-warning-rr        | Switch                   | Tyre Pressure Soft Warning RR | Tyre Pressure Soft Warning Rear Right                                                                            |
| usable-battery-level        | Number                   | Usable Battery Level          | Indicates the % of battery that can be used for vehicle functions like driving                                   |
| valet-mode                  | Switch                   | Valet Mode                    | Enable or disable Valet Mode                                                                                     |
| wiper-blade-heater          | Switch                   | Wiperblade Heater             | Indicates if the wiperblade heater is switched on                                                                |

## Full Example

demo.Things:

```java
teslascope:service:model3 [ apiKey="xxxx", publicID="aXb3" ] {
}

### Item Configuration

```java
String              TeslaVehicleName            {channel="teslascope:service:model3:vehicle-name"}
String              TeslaVehicleState           {channel="teslascope:service:model3:vehicle-state"}
String              TeslaVIN                    {channel="teslascope:service:model3:vin"}
Number              TeslaSpeed                  {channel="teslascope:service:model3:speed"}
String              TeslaShiftState             {channel="teslascope:service:model3:shift-state"}
Number              TeslaOdometer               {channel="teslascope:service:model3:odometer"}
Number              TeslaRange                  {channel="teslascope:service:model3:range"}

Number              TeslaBatteryLevel           {channel="teslascope:service:model3:battery-level"}
Number              TeslaPower                  {channel="teslascope:service:model3:power"}
Number              TeslaBatteryRange           {channel="teslascope:service:model3:battery-range"}
Number              TeslaEstBatteryRange        {channel="teslascope:service:model3:estimated-battery-range"}
Switch              TeslaPreconditioning        {channel="teslascope:service:model3:preconditioning"}

Switch              TeslaCharge                 {channel="teslascope:service:model3:charge"}

Dimmer              TeslaChargeLimit            {channel="teslascope:service:model3:charge-limit"}
Number              TeslaChargeRate             {channel="teslascope:service:model3:charge-rate"}
String              TeslaChargingState          {channel="teslascope:service:model3:charging-state"}
Number              TeslaChargerPower           {channel="teslascope:service:model3:charger-power"}
Number              TeslaTimeToFullCharge       {channel="teslascope:service:model3:time-to-full-charge"}

Number              TeslaChargerVoltage         {channel="teslascope:service:model3:charger-voltage"}
Number              TeslaChargerPower           {channel="teslascope:service:model3:charger-power"}

DateTime            TeslaScheduledChargingStart {channel="teslascope:service:model3:scheduled-charging-start"}

Switch              TeslaDoorLock               {channel="teslascope:service:model3:door-lock"}
Switch              TeslaHorn                   {channel="teslascope:service:model3:honk-horn"}
Switch              TeslaSentry                 {channel="teslascope:service:model3:sentry-mode"}
Switch              TeslaLights                 {channel="teslascope:service:model3:flash-lights"}
Switch              TeslaValet                  {channel="teslascope:service:model3:valet-mode"}

Switch              TeslaFrontDefrost           {channel="teslascope:service:model3:front-defroster"}
Switch              TeslaRearDefrost            {channel="teslascope:service:model3:rear-defroster"}
Switch              TeslaLeftSeatHeater         {channel="teslascope:service:model3:left-seat-heater"}
Switch              TeslaRightSeatHeater        {channel="teslascope:service:model3:right-seat-heater"}

Switch              TeslaHomelink               {channel="teslascope:service:model3:homelink"}
Location            TeslaLocation               {channel="teslascope:service:model3:location"}
Number              TeslaHeading                {channel="teslascope:service:model3:heading"}

Switch              TeslaAutoconditioning       {channel="teslascope:service:model3:auto-conditioning"}
Number:Temperature  TeslaTemperature            {channel="teslascope:service:model3:temperature"}
Number:Temperature  TeslaInsideTemperature      {channel="teslascope:service:model3:inside-temp"}
Number:Temperature  TeslaOutsideTemperature     {channel="teslascope:service:model3:outside-temp"}

Number:Pressure     TeslaTPMSPressureFL         {channel="teslascope:service:model3:tpms-pressure-fl"}
Number:Pressure     TeslaTPMSPressureFR         {channel="teslascope:service:model3:tpms-pressure-fr"}
Number:Pressure     TeslaTPMSPressureRL         {channel="teslascope:service:model3:tpms-pressure-rl"}
Number:Pressure     TeslaTPMSPressureRR         {channel="teslascope:service:model3:tpms-pressure-rr"}

Switch              TeslaTPMSSoftWarningFL      {channel="teslascope:service:model3:tpms-soft-warning-fl"}
Switch              TeslaTPMSSoftWarningFR      {channel="teslascope:service:model3:tpms-soft-warning-fr"}
Switch              TeslaTPMSSoftWarningRL      {channel="teslascope:service:model3:tpms-soft-warning-rl"}
Switch              TeslaTPMSSoftWarningRR      {channel="teslascope:service:model3:tpms-soft-warning-rr"}
```

### Sitemap Configuration

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
            Slider item=TeslaChargeLimit label="Charge Limit [%.1f]"
            Text item=TeslaChargingState label="Charging State [%s]" icon=""
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
