# EcoFlow Binding

This binding provides integration for power stations and micro inverters made by EcoFlow (<https://www.ecoflow.com/>).
It discovers devices and communicates to them by using the cloud services provided by EcoFLow.

## Supported Things

- EcoFlow cloud API (`ecoflow-api`)
- Delta 2 power station (`delta2`)
- Delta 2 Max power station (`delta2-max`)
- PowerStream micro inverter (`powerstream`)

## Discovery

At first, you need to manually create the bridge thing for the cloud API.
Once that is done, you can initiate a device scan and supported devices will be automatically discovered and added to the inbox.

## Thing Configuration

For the cloud API thing (`ecoflow-api`), you need to register for a developer account at EcoFlow's [developer page](https://developer.ecoflow.com).
Once that registration is approved, you can create access credentials under the `IoT Background` tab on that page, which you need for the thing configuration:

| Config    | Description                                |
|-----------|--------------------------------------------|
| accessKey | The access key obtained as described above |
| secretKey | The secret key obtained as described above |

Please note that the developer account can _not_ be used multiple times in parallel, as doing so will disturb event updates.

For the device things, there is no required configuration (when using discovery). The following parameters exist:

| Config       | Description                                                                      |
|--------------|----------------------------------------------------------------------------------|
| serialNumber | Required: The device's serial number. Filled automatically when using discovery. |

## Delta 2 Channels

The list below lists all channels supported for both the Delta 2 and Delta 2 Max power stations.

| Channel                         | Type                     | Description                                       | Read Only | Remarks |
|---------------------------------|--------------------------|---------------------------------------------------|-----------|---------|
| status#battery-level            | Number                   | Battery charge level (0-100%)                     | Yes       |         |
| status#input-power              | Number:Power             | Total battery input power                         | Yes       |         |
| status#output-power             | Number:Power             | Total battery output power                        | Yes       |         |
| status#remaining-charge-time    | Number:Time              | Remaining charge time as calculated by device     | Yes       | [1]     |
| status#remaining-discharge-time | Number:Time              | Remaining discharge time as calculated by device  | Yes       | [1]     |
| battery#temperature             | Number:Temperature       | Battery temperature                               | Yes       |         |
| battery#voltage                 | Number:ElectricPotential | Battery voltage                                   | Yes       |         |
| battery#current                 | Number:ElectricCurrent   | Battery input/output current                      | Yes       |         |
| battery#charger-type            | String                   | Type of currently active charger                  | Yes       | [2]     |
| battery#charge-limit            | Dimmer                   | Upper charge limit (0-100%)                       | Yes       |         |
| battery#discharge-limit         | Dimmer                   | Lower discharge limit (0-100%)                    | Yes       |         |
| battery#extra-battery-power     | Number:Power             | Power fed into / drawn from extra battery port    | Yes       |         |
| ac-input#set-charging-power     | Number:Power             | AC charge power target                            | No        | [3]     |
| ac-input#voltage                | Number:ElectricPotential | AC input voltage                                  | Yes       |         |
| ac-input#current                | Number:ElectricCurrent   | AC input current                                  | Yes       |         |
| ac-input#power                  | Number:Power             | AC input power                                    | Yes       |         |
| ac-input#frequency              | Number:Frequency         | AC input frequency                                | Yes       |         |
| ac-input#total-energy           | Number:Energy            | Amount of energy drawn from AC input              | Yes       | [4]     |
| ac-output#enabled               | Switch                   | Whether inverter / AC output is enabled           | No        |         |
| ac-output#xboost-enabled        | Switch                   | Whether X-Boost is enabled on AC output           | No        |         |
| ac-output#voltage               | Number:ElectricPotential | Inverter output voltage                           | Yes       |         |
| ac-output#current               | Number:ElectricCurrent   | Inverter output current                           | Yes       |         |
| ac-output#power                 | Number:Power             | Inverter output power                             | Yes       |         |
| ac-output#frequency             | Number:Frequency         | Inverter output frequency                         | Yes       |         |
| ac-output#temperature           | Number:Temperature       | Inverter temperature                              | Yes       |         |
| ac-output#total-energy          | Number:Energy            | Amount of energy provided by inverter             | Yes       | [4]     |
| dc-output#usb-enabled           | Switch                   | Whether USB output is enabled                     | No        |         |
| dc-output#12v-enabled           | Switch                   | Whether 12V car jack output is enabled            | No        |         |
| dc-output#usb1-power            | Number:Power             | Power drawn from left USB-A output                | Yes       |         |
| dc-output#usb2-power            | Number:Power             | Power drawn from right USB-A output               | Yes       |         |
| dc-output#qc-usb1-power         | Number:Power             | Power drawn from left quick charge USB-A output   | Yes       |         |
| dc-output#qc-usb2-power         | Number:Power             | Power drawn from right quick charge USB-A output  | Yes       |         |
| dc-output#usbc1-power           | Number:Power             | Power drawn from left USB-C output                | Yes       |         |
| dc-output#usbc2-power           | Number:Power             | Power drawn from right USB-C output               | Yes       |         |
| dc-output#12v-out-voltage       | Number:ElectricPotential | 12V car jack output voltage                       | Yes       |         |
| dc-output#12v-out-current       | Number:ElectricCurrent   | 12V car jack output current                       | Yes       |         |
| dc-output#12v-out-power         | Number:Power             | 12 V car jack output power                        | Yes       |         |
| dc-output#total-energy          | Number:Energy            | Total amount of energy drawn from DC outputs      | Yes       | [4]     |
| solar-input#voltage             | Number:ElectricPotential | PV(1) input voltage                               | Yes       |         |
| solar-input#current             | Number:ElectricCurrent   | PV(1) input current                               | Yes       |         |
| solar-input#power               | Number:Power             | PV(1) input power                                 | Yes       |         |
| solar-input#charge-state        | String                   | PV(1) charge state                                | Yes       | [5]     |
| solar-input#total-energy        | Number:Energy            | Total amount of energy provided by solar input(s) | Yes       |         |

Since the number of solar inputs and additional battery inputs varies between devices, the Delta 2 Max supports some additional channels:

| Channel                         | Type                     | Description                                       | Read Only | Remarks |
|---------------------------------|--------------------------|---------------------------------------------------|-----------|---------|
| battery#extra-battery2-power    | Number:Power             | Power fed into / drawn from second battery port   | Yes       |         |
| solar-input#voltage2            | Number:ElectricPotential | PV2 input voltage                                 | Yes       |         |
| solar-input#current2            | Number:ElectricCurrent   | PV2 input current                                 | Yes       |         |
| solar-input#power2              | Number:Power             | PV2 input power                                   | Yes       |         |
| solar-input#charge-state2       | String                   | PV2 charge state                                  | Yes       | [5]     |

Remarks:

- [1] If non applicable (charge time without charger or discharge time without load) the device reports those times as 5999 minutes.
- [2] Possible states: 'ac', 'dc' and 'solar'.
- [3] The range of valid values is 100..600 W. Smaller charge powers do not seem to work.
- [4] Storing those values in the device seems to be buggy, so it's possible (and was seen in the wild) for those counters to not increase monotonically.
- [5] Possible states: 'disabled', 'charging' and 'standby'. 'standby' happens in case of solar and AC charging both being active, AC gets priority in that case.

## PowerStream channels

The list below lists all channels supported for both the PowerStream micro inverter.

| Channel                       | Type                     | Description                                  | Read Only | Remarks |
|-------------------------------|--------------------------|----------------------------------------------|-----------|---------|
| inverter#status               | String                   | Inverter grid synchronization status         | Yes       | [1]     |
| inverter#input-voltage        | Number:ElectricPotential | AC voltage                                   | Yes       |         |
| inverter#input-frequency      | Number:Frequency         | AC frequency                                 | Yes       |         |
| inverter#output-power         | Number:Power             | AC output power                              | Yes       |         |
| inverter#output-target-power  | Number:Power             | AC output power target                       | No        | [2]     |
| inverter#supply-priority      | String                   | Whether charging or discharging is preferred | No        | [3]     |
| battery-input#voltage         | Number:ElectricPotential | Voltage of connected battery                 | Yes       |         |
| battery-input#current         | Number:ElectricCurrent   | Current flow out of / into battery           | Yes       |         |
| battery-input#power           | Number:Power             | Power drawn from / fed into battery          | Yes       |         |
| battery-input#temperature     | Number:Temperature       | Battery temperature                          | Yes       |         |
| battery-input#active          | Switch                   | Whether battery input is active              | Yes       |         |
| battery-input#battery-level   | Number                   | Charge level (0-100%) of connected battery   | Yes       |         |
| battery-input#charge-limit    | Dimmer                   | Upper battery charge limit (0-100%)          | No        |         |
| battery-input#discharge-limit | Dimmer                   | Lower battery discharge limit (0-100%)       | No        |         |
| pv1-input#voltage             | Number:ElectricPotential | Panel 1 voltage                              | Yes       |         |
| pv1-input#voltage-target      | Number:ElectricPotential | Panel 1 MPPT target voltage                  | Yes       |         |
| pv1-input#current             | Number:ElectricCurrent   | Panel 1 current                              | Yes       |         |
| pv1-input#power               | Number:Power             | Panel 1 power                                | Yes       |         |
| pv1-input#mppt-active         | Switch                   | Whether panel 1 MPPT is active               | Yes       |         |
| pv2-input#voltage             | Number:ElectricPotential | Panel 2 voltage                              | Yes       |         |
| pv2-input#voltage-target      | Number:ElectricPotential | Panel 2 MPPT target voltage                  | Yes       |         |
| pv2-input#current             | Number:ElectricCurrent   | Panel 2 current                              | Yes       |         |
| pv2-input#power               | Number:Power             | Panel 2 power                                | Yes       |         |
| pv2-input#mppt-active         | Switch                   | Whether panel 2 MPPT is active               | Yes       |         |

Remarks:

- [1] Possible states: 'idle', 'starting', 'synchronized' and 'disconnected'.
- [2] The range of valid values is 0..600/800 W. The upper limit depends on inverter type.
- [3] Possible states: 'prioIsSupply' (prefer AC output over battery storage), 'prioIsStorage' (prefer battery storage over AC output)

## Configuration in .things Files

If you want to create the API bridge in a .things file, the entry has to look as follows:

```java
Bridge ecoflow:ecoflow-api:ecoflowapi [ accessKey="YOUR_ACCESS_KEY", secretKey="YOUR_SECRET_KEY" ]
```

The devices are detected automatically.
If you also want to enter those manually, the syntax is as follows:

```java
Bridge ecoflow:ecoflow-api:ecoflowapi [ accessKey="YOUR_ACCESS_KEY", secretKey="YOUR_SECRET_KEY" ]
{
    Thing delta2-max myPowerStation "EcoFlow power station" [ serialNumber="serial number as printed on device" ]
}
```

## Item Configuration

You can link the channels listed above to items via the UI. If you want to do it via an `.items` file in your `$OPENHAB_CONF/items` folder, the syntax looks like this:

```java
Number Delta2BatteryLevel "Delta 2 Battery Level" { channel="ecoflow:delta2-max:ecoflowapi:myPowerStation:status#battery-level" }
Switch Delta2UsbEnabled   "Delta 2 USB Enabled"    { channel="ecoflow:delta2-max:ecoflowapi:myPowerStation:dc-output#usb-enabled" }
[...]
```
