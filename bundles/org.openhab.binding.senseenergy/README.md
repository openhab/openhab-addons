# SenseEnergy Binding

This binding supports the Sense Energy monitor (sense.com) which can be used to provide accurate energy usage statistics in the home.
In addition to providing real-time and historical energy production and usage data, Sense also uses machine learning to predict specific device usage in the home.
This binding will interface with the Sense cloud and web-socket and provide to openHAB real-time energy use statistics at both the whole house, but also exposes the per device usage.
Note, the binding currently assumes (and is tested) that the monitor is installed in a solar enabled configuration.
Is someone has a device in a different configuration I would welcome the feedback and would be able to add support to the binding.

Sense also allows smart plugs and other devices to provide real-time usage to Sense allowing Sense to incorporate accurate usage data into your data.
This binding supports emulation of the smart plugs so data from openHAB can be sent to Sense.
Here are several examples of how this can be used:

- A pool pump configured in openHAB where the actual power is known can be communicated to Sense.
- A switch or dimmer controlling a light can be used to notify the lights power when on (or dimmed to a certain level) to Sense.
- A fan / scene represented as a String can be used to notify Sense of the power usage when a particualar scene is on.

Using the openHAB follow profile with the channel(s) of the proxy device is an easy way to link these proxy devices to your openHAB setup.

This binding builds off the following works in understanding the Sense API.

https://github.com/scottbonline/sense
https://github.com/cbpowell/SenseLink/


## Supported Things

| Thing                        | id               | Type          | Description                  |
|----------                    |---------         |--------       |------------------------------|
| Sense Energy Cloud Connector | cloud-connector  | Bridge        | This represents the cloud account to interface with the Sense Energy API.  |
| Sense Monitor Device         | monitor          | Bridge        | This interfaces to a specific Sense energy monitor associated with the account. |
| Virtual Device Emulation     | proxy-device     | Device        | This is a proxy device used to provide real-time usage to Sense. |


## Discovery

Once the cloud-connector has been created and initialized, the monitor(s) associated with the account will be auto-discovered. Virtual devices are created manually attached to the monitor bridge.

## Thing Configuration

### Sense Energy Cloud Connector

The cloud connector is configured with the email and password for your Sense Energy cloud account.
At present, the binding does not support multi-factor authentication which can be disabled via the Sense app settings on your phone.

| Name            | Type    | Description                           | Default | Required | Advanced |
|-----------------|---------|---------------------------------------|---------|----------|----------|
| email           | text    | email for the Sense cloud account     | N/A     | yes      | no       |
| password        | text    | Password for the Sense cloud account  | N/A     | yes      | no       |

### Sense Monitor Device

The monitor will be auto-discovered after the could-connector bridge goes online. The only configuration parameter is the id, however, this is not available via the Sense app or sense.com. If supporting textual configuration, you will have to monitor the log in order to see the id for your monitor device.

| Name            | Type    | Description                           | Default | Required | Advanced |
|-----------------|---------|---------------------------------------|---------|----------|----------|
| id              | integer | ID for the monitor device             | N/A     | yes      | no       |

### Virtual Device Emulation

Virtual devices can be created in order to notify Sense of specific power usage of devices in your home. 

| Name            | Type             | Description                                                                        | Default | Required | Advanced |
|-----------------|---------         |---------------------------------------                                             |---------|----------|----------|
| powerLevels     | text             | Power levels for different states.                                                 | N/A     | no       | no       |
| voltage         | decimal          | Voltage level for the proxy device.                                                | 120     | yes      | no       |
| mac             | network-address  | A spoof'ed MAC address for this proxy device.                                      | random  | no       | yes      |

#### Power Levels

The power levels will be a list of power levels representing different states of the device.
Here are several examples of how this can be configured:

| powerLevel parameter          | Description            | Example Device       |
|------------------             |------------------------|--------------------- |
| 10 W                          | The device is a simple ON/OFF switch or Dimmer with 0 W in the OFF or 0% state, and 10 W in the full ON or 100% state. For a dimmer, the power will be linearly interpolated over the full range [0 W, 10 W] | Light | 
| 800 mW,10 W                   | The device is a simple ON/OFF switch or Dimmer with 800 mW in the OFF or 0% state, and 10 W in the full ON or 100% state. For a dimmer, the power will be linearly interpolated over the full range [800 mW, 10 W] | TV which standby power > 0 W |
| 0 W,1 W,3 W,8 W, 15 W         | A device which has non-linear power usage at different dim levels. This configuration would use 0 W at 0%, 1 W at 25%, 3 W at 50%, 8 W at 75% and 15 W at the full 100%. Other levels are linearly interpolated within the bounding points of the sub-range. | Dimmable light with non-linear usage profile |
| OFF=0 W,LOW=200 W,HIGH=400 W  | A device with several power states with different power levels in each state. | A fan with OFF, LOW, HIGH states |

#### MAC

Each proxy device must be configured with a MAC address. In a typical configuration, one should leave this field blank and the binding will automatically create a randomized MAC address to be used.

## Channels

### Cloud-Connector

There are no channels associated with the cloud-connector bridge.

### Monitor

The monitor channels are organized into two groups.

#### General

| Channel               | id                | Type                       | Read/Write | Description |
|----------             |--------           |--------                    |--------    |--------------- |
| Frequency             | frequency         | Number:Frequency           | R          | Electrical frequency detected by Sense. |
| Grid Power            | grid-power        | Number:Power               | R          | Power consumed from the grid (negative if supplying power to grid). |
| Potential 1           | potential-1       | Number:ElectricalPotential | R          | Potential measured on first 120V branch. |
| Potential 2           | potential-2       | Number:ElectricalPotential | R          | Potential measured on second 120V branch. |
| Main Power            | main-power        | Number:Power               | R          | Power detected by the main Sense clamp (only present in solar mode). |
| Solar Power           | solar-power       | Number:Power               | R          | Power detected by the solar Sense clamp (only present in solar mode). |
| Leg 1 Power           | leg-1-power       | Number:Power               | R          | Power detected by the first Sense clamp (only present when not in solar mode). |
| Leg 2 Power           | leg-2-power       | Number:Power               | R          | Power detected by the second Sense clamp (only present when not in solar mode). |

#### Discovered Devices

Every discovered device will have the following channels.

| Channel                 | id                | Type                       | Read/Write | Advanced        | Description |
|----------               |--------           |--------                    |--------    |---------------  |--------- |
| *Label*: Power          | *id*-device-power | Number:Power               | R          | N               | Power consumed by the device. |
| *Label*: On Off Trigger | *id*-trigger      | Trigger                    | N/A        | Y               | Trigger channel to notify when device has been turned ON or OFF. |

### Proxy Device

Each proxy device has several channels that can be used to notify Sense of the current power usage for the device.

| Channel               | id                | Type                       | Read/Write | Description |
|----------             |--------           |--------                    |--------    |--------------- |
| Power Level           | vdevice-power     | Number:Power               | W          | Sets a specific absolute real-time power usage for the device. |
| Device Switch         | vdevice-switch    | Switch                     | W          | Sets the power level to either the ON or OFF defined in the powerLevels parameter. |
| Device Dimmer         | vdevice-dimmer    | Dimmer                     | W          | Sets the power level to an interpolated value based on the powerLevels parameter. |
| Device State          | vdevice-state     | String                     | W          | Sets the power level to the state sepecifice in the powerLevels parameter. |

## Full Example

### Thing Configuration

```java
Bridge senseenergy:cloud-connector:cloud [ email="xxx", password="xxx" ] {
    Bridge monitor monitor1 [ id=869850 ] {
        Thing proxy-device poolpump "Sense Virtual Pool Pump"
        Thing proxy-device light "Sense Virtual Light" [ powerLevels="20 W" ]
        Thing proxy-device fan "Sense Virtual Fan" [ powerLevels="OFF=0 W,LOW=10 W, HIGH=20 W" ]
    }
}
```

### Item Configuration

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

// Virtual device "follow" channels. These should be the actually controlling items for your device.
Switch                    LightSwitch               "Light Switch"              { channel="senseenergy:proxy-device:cloud:monitor1:light:vdevice-switch"[profile="system:follow"] }
Dimmer                    LightDimmer               "Light Dimmer"              { channel="senseenergy:proxy-device:cloud:monitor1:light:vdevice-dimmer"[profile="system:follow"] }
Number:Power              PoolPump_Power            "Pool Pump Power"           { channel="senseenergy:proxy-device:cloud:monitor1:light:vdevice-power"[profile="system:follow"] }
String                    Fan_State                 "Fan State"                 { channel="senseenergy:proxy-device:cloud:monitor1:light:vdevice-state"[profile="system:follow"] }
```

### Rules

```java
rule "Sense Energy Discovered Device OnOff"
when
    Channel 'senseenergy:monitor:cloud:monitor1:discovered-devices#XXXX-trigger' triggered or
    Channel 'senseenergy:monitor:cloud:monitor1:discovered-devices#YYYY-trigger' triggered
then
    logInfo("SenseEnergy", "Sense Energy Discovered Device Triggered", "Message: {}", receivedEvent)
end
```

### Rule Actions

The binding also supports querying of trend totals over for different periods of time.

#### Map<String, Object> queryEnergyTrend(String scale, Instant datetime)

This function will query tthe Sense cloud for usage totals for a given period of time.

##### Parameters:

`scale` - the time scale for which the query should be over ("DAY", "WEEK", "MONTH", "YEAR").
`datetime` - the datetime in the period. this can be null to select the current datetime.

##### Returns:

The return is a Map<String, Object> object which contains the following values:

`consumption` - a QuantityType<Energy> of the total energy (KWh) used over the scale period.
`production` - a QuantityType<Energy> of the total energy (KWh) produced over the scale period.
`fromGrid` - a QuantityType<Energy> of the total energy (KWh) from the grid over the scale period.
`toGrid` - a QuantityType<Energy> of the total energy (KWh) to the grid over the scale period.
`netProduction` - a QuantityType<Energy> of the difference in energy (KWh) between what was produced and consumed during the scale period.
`solarPowered` - a QuantityType<Dimensionless> of the percent of solar energy production that was directly consumed (not sent to grid) during the scale period.

##### Example:

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

## Special notes

- For proxy device to work, the openHAB must be on the same sub-net as the Sense monitor and be able to received broadcast Datagram packets on port 9999. While the binding has not been tested in a Docker configuration, there are some potential issues with being able to receive on port 9999 (see https://github.com/cbpowell/SenseLink/).
- The Sense Energy Monitor can be configured in two different modes depending on whether the secondary current monitor is either attaced to the Solar circuit of another circuit in your house. Unfortunately, the JSON format from the API is different depending on the mode and currently the binding has only been tested and will work in the Solar mode to-date. If there are others wanting to use the setup in the other mode, I would be interested in enabling support for the other mode in the binding with assistance in receiving examples of the JSON format.

##TODO
code review
test authorization
