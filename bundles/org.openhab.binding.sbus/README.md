# Sbus Binding

This binding integrates Sbus-compatible hardware with openHAB, allowing control and monitoring over UDP networks.
Sbus is a protocol used for home automation that communicates via UDP broadcast messages.
The binding supports various thing types including RGB/RGBW controllers, temperature sensors, switch controllers, and multiple sensor types.

## Supported Things

- `udp` - Sbus Bridge for UDP communication
- `rgbw` - RGB/RGBW Controllers for color and brightness control
- `temperature-sensor` - Temperature Sensors for monitoring environmental conditions
- `switch` - Switch Controllers for basic on/off and dimming control
- `contact-sensor` - Contact Sensors for monitoring open/closed states (supports both 012C and 02CA sensor types)
- `motion-sensor` - Motion Sensors for detecting movement
- `lux-sensor` - Light Level Sensors for measuring illuminance
- `temperature` - (Deprecated) Use `temperature-sensor` instead

## Discovery

Sbus hardware communicates via UDP broadcast, but manual configuration is required to set up things in openHAB.
Auto-discovery is not supported at this moment.

## Binding Configuration

The binding itself does not require any special configuration.

_note_ If openHAB is deployed in a Docker container, you must set the `network_mode` to host. Without this setting, messages on the host network will not reach the Docker container's internal networks.

## Thing Configuration

### Bridge Configuration

The Sbus Bridge has the following configuration parameters:

| Name    | Type    | Description                                          | Default | Required | Advanced  |
|:--------|:--------|:-----------------------------------------------------|:-------:|:--------:|:---------:|
| host    | text    | IP address for Sbus communication (typically broadcast) | N/A     | yes      | no        |
| port    | integer | UDP port number                                      | 6000    | no       | no        |
| timeout | integer | Response timeout in milliseconds                     | 3000    | no       | yes       |

### Thing Configuration

Most thing types share the same basic configuration parameters:

| Name    | Type    | Description                                          | Default | Required | Advanced  |
|:--------|:--------|:-----------------------------------------------------|:-------:|:--------:|:---------:|
| subnetId| integer | Subnet ID                                            | 1       | yes      | no        |
| id      | integer | Unit ID                                              | N/A     | yes      | no        |
| refresh | integer | Refresh interval in seconds (0 = listen-only mode)   | 30      | no       | yes       |

**Contact Sensor Additional Configuration:**

The `contact-sensor` thing type has an additional `type` parameter:

| Name    | Type    | Description                                          | Default | Required | Advanced  |
|:--------|:--------|:-----------------------------------------------------|:-------:|:--------:|:---------:|
| type    | text    | Sensor type: `012c` (dry contact) or `02ca` (multi-sensor) | 012c    | no       | no        |

**Listen-Only Mode:** Setting `refresh=0` enables listen-only mode where the binding only processes broadcast messages without actively polling. This is useful for sensors that automatically broadcast their status updates.

## Channels

### RGBW Controller Channels

| Channel | Type   | Read/Write | Description                                                |
|:--------|:-------|:----------:|:-----------------------------------------------------------|
| color   | Color  | RW         | HSB color picker that controls RGBW components (0-100%). Can be configured to disable the white channel. |
| switch  | Switch | RW         | On/Off control for the RGBW output with optional timer     |

The color channel of RGBW controllers supports these additional parameters:

| Parameter   | Type    | Description                                          | Default | Required | Advanced  |
|:------------|:--------|:-----------------------------------------------------|:-------:|:--------:|:---------:|
| channelNumber | integer | The physical channel number                        | N/A     | yes      | no        |
| enableWhite | boolean | Controls the white component support for RGB palette | true    | no       | yes       |

### Temperature Sensor Channels

| Channel     | Type                | Read/Write | Description                    |
|:------------|:--------------------|:----------:|:-------------------------------|
| temperature | Number:Temperature  | R          | Current temperature reading. Can be configured to use Celsius (default) or Fahrenheit units    |

### Switch Controller Channels

| Channel | Type           | Read/Write | Description                                               |
|:--------|:---------------|:----------:|:----------------------------------------------------------|
| switch  | Switch         | RW         | Basic ON/OFF state control                                |
| dimmer  | Dimmer         | RW         | ON/OFF state with timer transition                        |
| paired  | Rollershutter  | RW         | UP/DOWN/STOP control for two paired channels (e.g., rollershutters)|

### Contact Sensor Channels

| Channel | Type    | Read/Write | Description                                               |
|:--------|:--------|:----------:|:----------------------------------------------------------|
| contact | Contact | R          | Contact state (OPEN/CLOSED)                               |

### Motion Sensor Channels

| Channel | Type    | Read/Write | Description                                               |
|:--------|:--------|:----------:|:----------------------------------------------------------|
| motion  | Switch  | R          | Motion detection state (ON=motion detected, OFF=no motion)|

### Lux Sensor Channels

| Channel | Type    | Read/Write | Description                                               |
|:--------|:--------|:----------:|:----------------------------------------------------------|
| lux     | Number  | R          | Light level in LUX units                                  |

**Note:** All sensor channels require a `channelNumber` parameter to specify the physical channel number.

## Full Example

### Thing Configuration

```java
Bridge sbus:udp:mybridge [ host="192.168.1.255", port=5000, timeout=5000 ] {
    Thing rgbw colorctrl [ id=72, refresh=30 ] {
        Channels:
            Type color-channel : color [ channelNumber=1, enableWhite=true ]   // Full RGBW control with white component
            Type switch-channel : power [ channelNumber=1 ]  // On/Off control for the RGBW output
    }
    
    Thing rgbw rgbonly [ id=73, refresh=30 ] {
        Channels:
            Type color-channel : color [ channelNumber=1, enableWhite=false ]  // RGB only, no white component
            Type switch-channel : power [ channelNumber=1 ]
    }
    
    Thing switch switch1 [ id=75, refresh=30 ] {
        Channels:
            Type switch-channel : first_switch  [ channelNumber=1 ]
            Type dimmer-channel : second_switch [ channelNumber=2 ]
            Type paired-channel : third_switch [ channelNumber=3, pairedChannelNumber=4 ]
    }
    
    Thing temperature-sensor temp1 [ id=62, refresh=30 ] {
        Channels:
            Type temperature-channel : temperature [ channelNumber=1 ]
    }
    
    Thing contact-sensor contact1 [ type="012c", id=80, refresh=30 ] {
        Channels:
            Type contact-channel : contact [ channelNumber=1 ]
    }

    
    Thing motion-sensor sensor_motion [ id=85, refresh=0 ] {
        Channels:
            Type motion-channel : motion [ channelNumber=1 ]
    }
    
    Thing lux-sensor sensor_lux [ id=85, refresh=0 ] {
        Channels:
            Type lux-channel : lux [ channelNumber=1 ]
    }
}
```

### Item Configuration

```java
// Temperature Sensor
Number:Temperature Temp_Sensor "Temperature [%.1f °C]" { channel="sbus:temperature-sensor:mybridge:temp1:temperature" }

// Basic Switch
Switch Light_Switch "Switch" { channel="sbus:switch:mybridge:switch1:switch" }

// Paired Channel (e.g., for rollershutters)
Rollershutter Rollershutter_Switch "Rollershutter [%s]" { channel="sbus:switch:mybridge:switch1:third_switch" }

// RGBW Controller with Power Control
Group   gLight      "RGBW Light"    <light>     ["Lighting"]
Color   rgbwColor    "Color"        <colorwheel> (gLight)   ["Control", "Light"]    { channel="sbus:rgbw:mybridge:colorctrl:color" }
Switch  rgbwPower    "Power"        <switch>     (gLight)   ["Switch", "Light"]     { channel="sbus:rgbw:mybridge:colorctrl:power" }

// Contact Sensor
Contact Contact_Sensor "Contact [%s]" <contact> { channel="sbus:contact-sensor:mybridge:contact1:contact" }

// Motion Sensor
Switch Motion_Sensor "Motion [%s]" <motion> { channel="sbus:motion-sensor:mybridge:sensor_motion:motion" }

// Lux Sensor
Number Lux_Sensor "Light Level [%.0f lux]" <sun> { channel="sbus:lux-sensor:mybridge:sensor_lux:lux" }
```

## Usage Notes

### 9-in-1 Sensor Configuration

9-in-1 sensors are multi-function sensors that combine motion detection, light level measurement, and dry contact monitoring in a single physical unit. To configure a 9-in-1 sensor in openHAB, you need to create **three separate things** that all reference the same physical sensor:

1. **contact-sensor** (type: `02ca`) - For dry contact channels
2. **motion-sensor** - For motion detection
3. **lux-sensor** - For light level sensing

All three things must use the **same subnet ID and unit ID** to represent the same physical sensor.

**Example for a 9-in-1 sensor with ID 85:**

```java
Thing contact-sensor sensor_contact [ type="02ca", id=85, refresh=0 ] {
    Channels:
        Type contact-channel : contact1 [ channelNumber=1 ]
        Type contact-channel : contact2 [ channelNumber=2 ]
}

Thing motion-sensor sensor_motion [ id=85, refresh=0 ] {
    Channels:
        Type motion-channel : motion [ channelNumber=1 ]
}

Thing lux-sensor sensor_lux [ id=85, refresh=0 ] {
    Channels:
        Type lux-channel : lux [ channelNumber=1 ]
}
```

**Benefits of this approach:**

- Clear separation of concerns - each thing handles one sensor type
- Flexible configuration - only create the things you need
- Follows openHAB best practices for thing organization
- Each thing can be configured independently

### Contact Sensor Types

The `contact-sensor` thing type supports two different sensor types via the `type` parameter:

- **`012c`** (default): 012C dry contact sensors 
- **`02ca`**: 02CA multi-sensor dry contacts 

Choose the appropriate type based on your hardware.

### RGB vs. RGBW Mode

The `enableWhite` parameter for color channels controls whether the white component is used:

- Set to `true` (default): Full RGBW control with white component
- Set to `false`: RGB-only control with no white component

This is useful for:

- Pure RGB fixtures without a white channel
- Creating saturated colors without white dilution
- Specialized color effects where white would wash out the intended color

### Color Control and On/Off Functionality

The Color item type in openHAB inherently supports both color selection and on/off functionality:
- The color picker controls hue and saturation
- The brightness component (0-100%) functions as the on/off control
  - When brightness is 0%, the light is OFF
  - When brightness is >0%, the light is ON

This is why a Color item shows both a color picker and an on/off button in the UI without requiring a separate Switch item.
