# SenseEnergy Binding

This binding supports the Sense Energy monitor (sense.com) which can be used to provide accurate energy usage statistics in the home.
In addition to providing real-time and historical energy production and usage data, Sense also uses machine learning to predict specific device usage.
This binding interfaces with the Sense cloud and local monitor to provide openHAB real-time energy updates for the whole house and any known devices.
Note, the binding currently assumes (and the configuration tested) that the monitor is installed in a solar enabled configuration.
If you have a device in a different configuration, feedback would be welcome and support might be added to the binding.

Sense also allows smart plugs and other devices to provide real-time usage allowing Sense to incorporate accurate usage statistics into your data.
This binding also supports creation of virtual proxy devices in openHAB which can send to Sense any energy usage for openHAB aware devices.
Here are several examples of how a virtual proxy device can be used:

- A pool pump known in openHAB can update its power when on to Sense
- A switch or dimmer where the power is known can get send to Sense when on.
- A fan / scene represented as a String can be used to notify Sense of the power usage when a particualar scene is on.

Using the openHAB follow profile with the channel(s) of the proxy device is an easy way to link these proxy devices to your openHAB setup. (see examples)

This binding builds off the following works in understanding the Sense API:

- <https://github.com/scottbonline/sense>
- <https://github.com/cbpowell/SenseLink/>

## Supported Things

| Thing Id         | Label                        | Type   | Description                                                        |
|------------------|------------------------------|--------|--------------------------------------------------------------------|
| cloud-connector  | Sense Energy Cloud Connector | Bridge | This represents the cloud account to interface with the Sense Ener |
| monitor          | Sense Monitor Device         | Bridge | This interfaces to a specific Sense energy monitor associated with |
| proxy-device     | Virtual Device Emulation     | Device | This is a proxy device used to provide real-time usage to Sense.   |

## Discovery

Initial configurtion involes creating a cloud-connector device with the email and password for the Sense cloud account.
Once the cloud-connector has been created and initialized, the monitor(s) associated with the account will be auto-discovered.
Virtual proxy devices are created manually attached to the monitor bridge.

## Thing Configuration

### Sense Energy Cloud Connector

The `cloud-connector` is configured with the email and password for your Sense Energy cloud account.
At present, the binding does not support multi-factor authentication which should be disabled via the Sense app settings.

| Name     | Type | Description                          | Default | Required | Advanced |
|----------|------|--------------------------------------|---------|----------|----------|
| email    | text | email for the Sense cloud account    | N/A     | yes      | no       |
| password | text | Password for the Sense cloud account | N/A     | yes      | no       |

### Sense Monitor Device

The monitor will be auto-discovered after the `cloud-connector` bridge goes online.
The only configuration parameter is the id, however, this is not available via the Sense app or sense.com.
When supporting textual configuration, you can monitor the openhab.log in order to see the id for your monitor device.

| Name | Type    | Description               | Default | Required | Advanced |
|------|---------|---------------------------|---------|----------|----------|
| id   | integer | ID for the monitor device | N/A     | yes      | no       |

### Virtual Proxy Device Emulation

Virtual proxy devices can be created in order to notify Sense of specific power usage of devices in your home.
These emulate a TP-Link Kasa HS110 smart plug and will report to Sense the power usage based on on the configuration of the proxy device and the state.
In order to use, you need to enable "TP-Link HS110/HS300 Smart Plug" in the Sense app.

| Name        | Type            | Description                                   | Default | Required | Advanced |
|-------------|-----------------|-----------------------------------------------|---------|----------|----------|
| powerLevels | text            | Power levels for different states.            | N/A     | no       | no       |
| voltage     | decimal         | Voltage level for the proxy device.           | 120     | yes      | no       |
| mac         | network-address | A spoof'ed MAC address for this proxy device. | random  | no       | yes      |

#### Power Levels

The power levels is a list representing different power levels or states of the device.
Here are several examples of how this can be configured:

| powerLevel parameter         | Description                                                                                                                                                                                                                                                  | Example Device                               |
|------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------|
| 10 W                         | The device is a simple ON/OFF switch or Dimmer with 0 W in the OFF or 0% state, and 10 W in the full ON or 100% state. For a dimmer, the power will be linearly interpolated over the full range [0 W, 10 W] depending on current dim level.                 | Light                                        |
| 800 mW,10 W                  | The device is a simple ON/OFF switch or Dimmer with 800 mW in the OFF or 0% state, and 10 W in the full ON or 100% state. For a dimmer, the power will be linearly interpolated over the full range [800 mW, 10 W] depending on current dim level.           | TV which standby power > 0 W                 |
| 0 W,1 W,3 W,8 W, 15 W        | A device which has non-linear power usage at different dim levels. This configuration would use 0 W at 0%, 1 W at 25%, 3 W at 50%, 8 W at 75% and 15 W at the full 100%. Other levels are linearly interpolated within the bounding points of the sub-range. | Dimmable light with non-linear usage profile |
| OFF=0 W,LOW=200 W,HIGH=400 W | A device with several power states with different power levels in state represented by a String.                                                                                                                                                             | A fan with OFF, LOW, HIGH states             |

#### MAC

Each proxy device must be configured with a MAC address.
The virtual device creates a random MAC address which is used in identification of the device to Sense.
Note, if configuring via the textual interace, it is important to provide the MAC field, otherwise a different MAC address will be randomized whenever openHAB restarts and the proxy device will appear as a new additional device to Sense.

## Channels

### Cloud-Connector

There are no channels associated with the cloud-connector bridge.

### Monitor

The monitor channels are organized into multiple groups.

#### General Channel Group

| Channel Id  | Label       | Type                       | Read/Write | Description                                                                     |
|-------------|-------------|----------------------------|------------|---------------------------------------------------------------------------------|
| frequency   | Frequency   | Number:Frequency           | R          | Electrical frequency detected by Sense.                                         |
| grid-power  | Grid Power  | Number:Power               | R          | Power consumed from the grid (negative if supplying power to grid).             |
| potential-1 | Potential 1 | Number:ElectricalPotential | R          | Potential measured on first 120V branch.                                        |
| potential-2 | Potential 2 | Number:ElectricalPotential | R          | Potential measured on second 120V branch.                                       |
| main-power  | Main Power  | Number:Power               | R          | Power detected by the main Sense clamp (only present in solar mode).            |
| solar-power | Solar Power | Number:Power               | R          | Power detected by the solar Sense clamp (only present in solar mode).           |
| leg-1-power | Leg 1 Power | Number:Power               | R          | Power detected by the first Sense clamp (only present when not in solar mode).  |
| leg-2-power | Leg 2 Power | Number:Power               | R          | Power detected by the second Sense clamp (only present when not in solar mode). |

#### Discovered Devices, Self-Reporting Devices and Proxy Devices Channel Groups

- Discovered devices are those which Sense has discovered using their algorithms.
- Self-reporting devices are any devices which report their power usage to Sense (i.e. energy reporting smart plugs).
- Proxy devices are any virtual proxy devices set up in openHAB where this binding reports their power usage.

| Channel                 | id                | Type         | Read/Write | Advanced | Description                                                      |
|-------------------------|-------------------|--------------|------------|----------|------------------------------------------------------------------|
| _Label_: Power          | _id_-device-power | Number:Power | R          | N        | Power consumed by the device.                                    |
| _Label_: On Off Trigger | _id_-trigger      | Trigger      | N/A        | Y        | Trigger channel to notify when device has been turned ON or OFF. |

### Proxy Device

Each proxy device has several channels that can be used to notify Sense of the current power usage for the device.
These can either attached to an openHAB item, or, can be used with the system:follow profile to follow the state of another channel (see example).

| Channel               | id                | Type                       | Read/Write | Description |
|----------             |--------           |--------                    |--------    |--------------- |
| Power Level           | proxy-device-power     | Number:Power               | W          | Sets a specific absolute real-time power usage for the device. |
| Device Switch         | proxy-device-switch    | Switch                     | W          | Sets the power level to either the ON or OFF defined in the powerLevels parameter. |
| Device Dimmer         | proxy-device-dimmer    | Dimmer                     | W          | Sets the power level to an interpolated value based on the powerLevels parameter. |
| Device State          | proxy-device-state     | String                     | W          | Sets the power level to the state sepecifice in the powerLevels parameter. |

## Full Example

### `demo.things` Example

```java
Bridge senseenergy:cloud-connector:cloud [ email="xxx", password="xxx" ] {
    Bridge monitor monitor1 [ id=869850 ] {
        Thing proxy-device poolpump "Sense Virtual Pool Pump"
        Thing proxy-device light "Sense Virtual Light" [ powerLevels="20 W" ]
        Thing proxy-device fan "Sense Virtual Fan" [ powerLevels="OFF=0 W,LOW=10 W, HIGH=20 W" ]
    }
}
```

### `demo.items` Example

```java
// General channels
Number:Frequency          Main_Frequency            "Main Frequency"            { channel="senseenergy:monitor:cloud:monitor1:general#frequency" }
Number:Power              To_Grid_Power             "To Grid Power"             { channel="senseenergy:monitor:cloud:monitor1:general#grid-power" }
Number:ElectricPotential  Branch_1_Potential        "Branch 1 Potential"        { channel="senseenergy:monitor:cloud:monitor1:general#potential-1" }
Number:ElectricPotential  Branch_2_Potential        "Branch 2 Potential"        { channel="senseenergy:monitor:cloud:monitor2:general#potential-2" }
Number:Power              Main_Power                "Main Power"                { channel="senseenergy:monitor:cloud:monitor1:general#main-power" }
Number:Power              Solar_Power               "Solar Power"               { channel="senseenergy:monitor:cloud:monitor1:general#solar-power" }

// Discovered device channels
Number:Power              Sense_AlwaysOn_Power      "Always-On Power"           { channel="senseenergy:monitor:cloud:monitor1:discovered-devices#always_on-device-power" }
Number:Power              Sense_PoolPump_Power      "Pool Pump Power"           { channel="senseenergy:monitor:cloud:monitor1:discovered-devices#Z0sBBkO1-device-power" }
Number:Power              Sense_Other_Power         "Other Power"               { channel="senseenergy:monitor:cloud:monitor1:discovered-devices#unknown-device-power" }

// Virtual proxy device "follow" channels. These should be the actually controlling items for your device.
Switch                    LightSwitch               "Light Switch"              { channel="senseenergy:proxy-device:cloud:monitor1:light:proxy-device-switch"[profile="system:follow"] }
Dimmer                    LightDimmer               "Light Dimmer"              { channel="senseenergy:proxy-device:cloud:monitor1:light:proxy-device-dimmer"[profile="system:follow"] }
Number:Power              PoolPump_Power            "Pool Pump Power"           { channel="senseenergy:proxy-device:cloud:monitor1:light:proxy-device-power"[profile="system:follow"] }
String                    Fan_State                 "Fan State"                 { channel="senseenergy:proxy-device:cloud:monitor1:light:proxy-device-state"[profile="system:follow"] }
```

### Rules

```java
rule "Sense Energy Discovered Device OnOff"
when
    Channel 'senseenergy:monitor:cloud:monitor1:discovered-devices#XXXX-trigger' triggered or
    Channel 'senseenergy:monitor:cloud:monitor1:self-reporting-devices#YYYY-trigger' triggered or
    Channel 'senseenergy:monitor:cloud:monitor1:proxy-devices#ZZZZ-trigger' triggered
then
    logInfo("SenseEnergy", "Sense Energy device turned ON/OFF - Event: {}", receivedEvent)
end
```

### Rule Actions

The binding also supports querying of trend totals over a periods of time.

#### Map<String, Object> queryEnergyTrend(String scale, Instant datetime)

This function will query tthe Sense cloud for usage totals for a given period of time.

##### Parameters

`scale` - the time scale for which the query should be over ("DAY", "WEEK", "MONTH", "YEAR").
`datetime` - the datetime in the period. this can be null to select the current datetime.

##### Returns

The return is a Map<String, Object> object which contains the following values:

`consumption` - a QuantityType<Energy> of the total energy (KWh) used over the scale period.
`production` - a QuantityType<Energy> of the total energy (KWh) produced over the scale period.
`fromGrid` - a QuantityType<Energy> of the total energy (KWh) from the grid over the scale period.
`toGrid` - a QuantityType<Energy> of the total energy (KWh) to the grid over the scale period.
`netProduction` - a QuantityType<Energy> of the difference in energy (KWh) between what was produced and consumed during the scale period.
`solarPowered` - a QuantityType<Dimensionless> of the percent of solar energy production that was directly consumed (not sent to grid) during the scale period.

##### Example

```java
rule "Sense Energy Update Trends"
when
    Time cron "0 0/15 * ? * *"
then
    logInfo("SenseEnergy", "Sense Energy Update Trends")
    
    val monitorActions = getActions("senseenergy", "senseenergy:monitor:cloud:monitor1")
    
    val dayTrends = monitorActions.queryEnergyTrend("DAY", null)
    logInfo("SenseEnergy", "Energy DAY trends {}", dayTrends.toString())

    val weekTrends = monitorActions.queryEnergyTrend("WEEK", null)
    logInfo("SenseEnergy", "Energy WEEK trends {}", weekTrends.toString())

    val monthTrends = monitorActions.queryEnergyTrend("MONTH", null)
    logInfo("SenseEnergy", "Energy MONTH trends {}", monthTrends.toString())

    val yearTrends = monitorActions.queryEnergyTrend("YEAR", null)
    logInfo("SenseEnergy", "Energy YEAR trends {}", yearTrends.toString())
end
```

## Special Notes

For proxy device to work, openHAB must be on the same sub-net as the Sense monitor and be able to receive broadcast Datagram packets on port 9999.
While the binding has not been tested in a Docker configuration, there are some potential issues with being able to receive on port 9999 (see <https://github.com/cbpowell/SenseLink/>).

The Sense Energy Monitor can be configured in two different modes depending on whether the secondary current monitor is either attaced to the Solar circuit of another circuit in your house.
Unfortunately, the JSON format from the API is different depending on the mode and currently the binding has only been tested and will work in the Solar mode.
If there are others wanting to use the setup in a different mode, I would be interested in enabling support.
