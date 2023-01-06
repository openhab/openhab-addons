# GCE Binding

This binding aims to handle various GCE Electronics equipments.
IPX800 is a 8 relay webserver from gce-electronics with a lot of possibilities:

- 8 Digital Input
- 8 Relay (250V / 10A / channel)
- 4 Analog Input
- 8 Counters
- Ability to cascade up to 3 extensions for a total of 32 inputs / 32 relay

Each IPX800 connected to openHAB must be configured with the setting 'Send data on status changed' on the website in M2M > TCP client.

To make it simple, IPX800 is a simple device that drives output and retrieves input.
On input we generally connect push buttons (for instance house switchs), on ouputs we can connect light bulbs for instance.

Features of the binding:

- Multi ipx support
- Direct TCP connection
- Auto reconnect
- Simple clic/Long press
- Pulse mode support

## Binding Configuration

There is no configuration at binding level.

## Thing Configuration

The IPX800v3 (ID : 'ipx800v3') accepts the following configuration parameters :

| Property            | Default | Required | Description                 |
|---------------------|---------|----------|-----------------------------|
| hostname            |         | Yes      | IP address or hostname.     |
| portNumber          | 9870    | No       | TCP client connection port. |
| pullInterval*       | 5000    | No       | Refresh interval (in ms)    |

The binding will query periodically the 'globalstatus.xml' page of the IPX to get fresh informations.
This is especially usefull for Analog inputs and Counter as modification of these values on PLC side does not trigger any M2M message.

The thing provides four groups of channels.

### Digital Inputs

This represents the inputs of the PLC. Each can be open or closed.
They are usually commuted by physical devices like pushbuttons, magnets...

#### Digital Input Channels (contacts)

Each input will have these associated channels:

| Group    | Channel Name           | Item Type   | R/W | Description                                                                 |
|----------|------------------------|-------------|-----|-----------------------------------------------------------------------------|
| contact  | `portnumber`           | Contact     |  R  | Status of the actual port (OPEN, CLOSED)                                    |
| contact  | `portnumber`-duration  | Number:Time |  R  | Updated when the port status changes to the duration of the previous state. |

Associated events:

| Channel Type ID    | Options           | Description                                      | Conf Dependency |
|--------------------|-------------------|--------------------------------------------------|-----------------|
| `portnumber`-event |                   | Triggered on or after a port status change       |                 |
|                    | PRESSED           | Triggered when state changes from OPEN to CLOSED |                 |
|                    | RELEASED          | Triggered when state changes from CLOSED to OPEN |                 |
|                    | LONG_PRESS        | Triggered when RELEASED after a long period      | longPressTime   |
|                    | SHORT_PRESS       | Triggered when RELEASED before a long period     | longPressTime   |
|                    | PULSE             | Triggered during CLOSED state                    | pulsePeriod     |

#### Configuration

| Property        | Default | Unit | Description                                                                     |
|-----------------|---------|------|---------------------------------------------------------------------------------|
| debouncePeriod  |    0(*) | ms   | Debounce time (ignores flappling within this time). No debounce is done if '0'. |
| longPressTime   |    0(*) | ms   | Delay before triggering long press event. Ignored if '0'.                       |
| pulsePeriod     |    0(*) | ms   | Period of pulse event triggering while the entry is closed. Ignored if '0'.     |
| pulseTimeout    |    0(*) | ms   | Period of time after pulsing will be stopped. None if '0'.                      |

- Values below 100ms should be avoided as the JVM could skip them and proceed in the same time slice.

### Digital Outputs Channels (relays)

Each output will have these associated channels:

| Group    | Channel Name           | Item Type   | R/W | Description                                                                 |
|----------|------------------------|-------------|-----|-----------------------------------------------------------------------------|
| relay    | `portnumber`           | Switch      | R/W | Status of the actual port (ON, OFF)                                         |
| relay    | `portnumber`-duration  | Number:Time |  R  | Updated when the port status changes to the duration of the previous state. |

#### Configuration

| Property        | Default | Description                                                              |
|-----------------|---------|--------------------------------------------------------------------------|
| pulse           |  false  | If set, the output will be in pulse mode, releasing it after the contact |

### Counters Channels

Each counter will have these associated channels:

| Group    | Channel Name             | Item Type   | R/W | Description                                                                    |
|----------|--------------------------|-------------|-----|--------------------------------------------------------------------------------|
| counter  | `counternumber`          | Number      |  R  | Actual value of the counter                                                    |
| counter  | `counternumber`-duration | Number:Time |  R  | Updated when the counter status changes to the duration of the previous state. |

#### Configuration

This channel has no configuration setting.

### Analog Inputs Channels

Each analog port will have these associated channels:

| Group  | Channel Name          | Item Type                | R/W | Description                                                                 |
|--------|-----------------------|--------------------------|-----|-----------------------------------------------------------------------------|
| analog | `portnumber`          | Number                   |  R  | Value of the port.                                                          |
| analog | `portnumber`-duration | Number:Time              |  R  | Updated when the port status changes to the duration of the previous state. |
| analog | `portnumber`-voltage  | Number:ElectricPotential |  R  | Electrical equivalency of the analogic value                                |

#### Configuration

| Property   | Default | Description                                                                         |
|------------|---------|-------------------------------------------------------------------------------------|
| hysteresis |    0    | If set, the channel will ignore status if change (+ or -) is less than hysteresis/2 |

## Rule Actions

Multiple actions are supported by this binding. In classic rules these are accessible as shown in the example below:

Getting ipxActions variable in scripts

```java
 val ipxActions = getActions("gce","gce:ipx800v3:43cc8d07")
 if(null === ipxActions) {
        logInfo("actions", "ipxActions not found, check thing ID")
        return
 } else {
        // do something with sunActions
 }
```

### resetCounter(counterId)

Resets the value of the given counter to 0.

- `counterId` (Integer) - id of the counter.

### reset(placeholder)

Restarts the PLC.

- `placeholder` (Integer) - This parameter is not used (can be null).

## Example

### Things

ipx800.things

```java

Thing gce:ipx800v3:ipx "IPX800" @ "diningroom" [hostname="192.168.0.144", portNumber=9870] {
    Channels:
        Type contact : contact#1 [       // Aimant Détection porte de garage ouverte
            debouncePeriod=2500,
            pulsePeriod=1000,
            pulseTimeout=60000
        ]
        Type contact : contact#2 [       // Aimant Détection porte de garage fermée
            debouncePeriod=2500
        ]
        Type relay : relay#8 [       // Actionneur porte de garage
            pulse=true    
        ]
}
```

ipx800.items

```java

Group gIPXInputs "Inputs" <input>
Contact input1 "Porte garage ouverte [%s]" <contact> (gIPXInputs) {channel="gce:ipx800v3:ipx:contact#1"}
Contact input2 "Porte garage fermée [%s]"  <contact> (gIPXInputs) {channel="gce:ipx800v3:ipx:contact#2"}

Group gIPXOutputs "Outputs" <output>          
Switch output3 "Chaudière" <furnace> (gIPXOutputs) {channel="gce:ipx800v3:ipx:relay#3"}
Switch output4 "Lumière Porche" <light> (gIPXOutputs) {channel="gce:ipx800v3:ipx:relay#4"}

```
