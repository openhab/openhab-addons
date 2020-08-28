# GCE Binding

This binding aims to handle various GCE Electronics equipments.
IPX800 is a 8 relay webserver from gce-electronics with a lot of possibilities:

* 8 Digital Input
* 8 Relay (250v/ 10A / channel)
* 4 Analog Input
* 8 Counters
* Ability to cascade up to 3 extensions for a total of 32 inputs / 32 relay

Each IPX800 connected to openHAB must be configured with the setting 'Send data on status changed' on the website in M2M > TCP client.

To make it simple, IPX800 is a simple device that drives output and retrieves input. 
On input we generally connect push buttons (for instance house switchs), on ouputs we can connect light bulbs for instance.

Features of the binding:

 * Multi ipx support
 * Direct TCP connection
 * Auto reconnect
 * Simple clic/Long press
 * Pulse mode support

## Binding Configuration

There is no configuration at binding level.


## Thing Configuration

The IPX800v3 accepts the following configuration parameters

| Property            | Default | Required | Description                 |
|---------------------|---------|----------|-----------------------------|
| hostname            |         | Yes      | IP address or hostname.     |
| portNumber          | 9870    | No       | TCP client connection port. |

The thing provides four kinds of channels.

### Digital Inputs

#### Configuration

| Property        | Default | Description                                                                     |
|-----------------|---------|---------------------------------------------------------------------------------|
| debouncePeriod  |    0    | Debounce time (ignores flappling within this time). No debounce is done if '0'. |
| longPressTime   |    0    | Delay (in ms) before triggering long press event. Ignored if '0'.               |
| pulsePeriod     |    0    | Period of pulse event triggering while the entry is closed. Ignored if '0'.     |
| pulseTimeout    |    0    | Period of time after pulsing will be stopped. None if '0'.                      |


### Digital Outputs (relays)

#### Configuration

| Property        | Default | Description                                                                     |
|-----------------|---------|---------------------------------------------------------------------------------|
| pulse           |  false  |     |

### Counters

#### Configuration

| Property        | Default | Description                                                                     |
|-----------------|---------|---------------------------------------------------------------------------------|
| pullFrequency   |  5000   | Counter value refreshing frequency (in ms).                                     |

### Analog Inputs

#### Configuration

| Property        | Default | Description                                                                            |
|-----------------|---------|----------------------------------------------------------------------------------------|
| pullFrequency   |  5000   | Counter value refreshing frequency (in ms).                                            |
| histeresis      |  0      | Threshold that must be reached between two refreshes to trigger an update of the value |

## Item Configuration

### Syntax


### Item Types

#### Output


#### To be done

* Long press


### Example

```
```


