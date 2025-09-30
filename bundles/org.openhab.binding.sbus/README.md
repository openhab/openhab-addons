# Sbus Binding

This binding integrates Sbus devices with openHAB, allowing control and monitoring of Sbus-compatible devices over UDP.
Sbus is a protocol used for home automation devices that communicate over UDP networks.
The binding supports various device types including RGB/RGBW controllers, temperature sensors, and switch controllers.

## Supported Things

- `udp` - Sbus Bridge for UDP communication
- `rgbw` - RGB/RGBW Controllers for color and brightness control
- `temperature` - Temperature Sensors for monitoring environmental conditions
- `switch` - Switch Controllers for basic on/off and dimming control
- `contact-sensor` - Traditional Contact Sensors for monitoring open/closed states
- `multi-sensor` - Multi-Sensor (9-in-1) devices with motion, lux, and dry contact capabilities

## Discovery

Sbus devices communicate via UDP broadcast, but manual configuration is required to set up the devices in openHAB.
Auto-discovery is not supported at this moment.

## Binding Configuration

The binding itself does not require any special configuration.

_note_ If openHAB is deployed in a Docker container, you must set the `network_mode` to host. Without this setting, messages on the host network will not reach the Docker container's internal networks.

## Thing Configuration

### Bridge Configuration

The Sbus Bridge has the following configuration parameters:

| Name    | Type    | Description                                          | Default | Required | Advanced  |
|:--------|:--------|:-----------------------------------------------------|:-------:|:--------:|:---------:|
| host    | text    | IP address of the Sbus device (typically broadcast)  | N/A     | yes      | no        |
| port    | integer | UDP port number                                      | 6000    | no       | no        |
| timeout | integer | Response timeout in milliseconds                     | 3000    | no       | yes       |

### Device Configuration

All device types share the same basic configuration parameters:

| Name    | Type    | Description                                          | Default | Required | Advanced  |
|:--------|:--------|:-----------------------------------------------------|:-------:|:--------:|:---------:|
| subnetId| integer | Subnet ID the device is part of                      | N/A     | yes      | no        |
| id      | integer | Device ID                                            | N/A     | yes      | no        |
| refresh | integer | Refresh interval in seconds (0 = listen-only mode)   | 30      | no       | yes       |

**Device Type Specific Notes:**

- **Traditional Contact Sensors (`contact-sensor`)**: Use ReadDryChannelsRequest protocol for simple contact monitoring
- **Multi-Sensor Devices (`multi-sensor`)**: Use ReadNineInOneStatusRequest protocol and support motion, lux, and dry contact channels from a single physical device

**Listen-Only Mode:** Setting `refresh=0` enables listen-only mode where handlers only process asynchronous broadcast messages (MotionSensorStatusReport) without actively polling. This is particularly useful for 9-in-1 sensor devices that broadcast status updates.

## Channels

### RGBW Controller Channels

| Channel | Type   | Read/Write | Description                                                |
|:--------|:-------|:----------:|:-----------------------------------------------------------|
| color   | Color  | RW         | HSB color picker that controls RGBW components (0-100%). Can be configured to disable the white channel. |
| switch  | Switch | RW         | On/Off control for the RGBW output with optional timer     |

The color channel of RGBW controllers supports these additional parameters:

| Parameter   | Type    | Description                                          | Default | Required | Advanced  |
|:------------|:--------|:-----------------------------------------------------|:-------:|:--------:|:---------:|
| channelNumber | integer | The physical channel number on the Sbus device     | N/A     | yes      | no        |
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

### Contact Sensor Channels (Traditional)

| Channel | Type    | Read/Write | Description                                               |
|:--------|:--------|:----------:|:----------------------------------------------------------|
| contact | Contact | R          | Contact state (OPEN/CLOSED) for traditional contact sensors |

### Multi-Sensor (9-in-1) Channels

Multi-sensor devices support multiple channel types from a single physical device:

| Channel | Type    | Read/Write | Description                                               |
|:--------|:--------|:----------:|:----------------------------------------------------------|
| contact | Contact | R          | Dry contact state (OPEN/CLOSED) - use channelNumber parameter to specify which contact (1 or 2) |
| motion  | Switch  | R          | Motion detection state (ON=motion detected, OFF=no motion)|
| lux     | Number  | R          | Light level in LUX units                                  |

The contact channel supports these additional parameters:

| Parameter     | Type    | Description                                          | Default | Required | Advanced  |
|:--------------|:--------|:-----------------------------------------------------|:-------:|:--------:|:---------:|
| channelNumber | integer | The dry contact number on the 9-in-1 device (1 or 2) | 1       | no       | no        |

**Note:** You can configure any combination of these channels on a single `multi-sensor` thing to match your 9-in-1 device capabilities.

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
    
    Thing temperature temp1 [ id=62, refresh=30 ] {
        Channels:
            Type temperature-channel : temperature [ channelNumber=1 ]
    }
    
    Thing switch switch1 [ id=75, refresh=30 ] {
        Channels:
            Type switch-channel : first_switch  [ channelNumber=1 ]
            Type dimmer-channel : second_switch [ channelNumber=2 ]
            Type paired-channel : third_switch [ channelNumber=3, pairedChannelNumber=4 ]
    }
    
    // Traditional contact sensor
    Thing contact-sensor contact1 [ id=80, refresh=30 ] {
        Channels:
            Type contact-channel : contact [ channelNumber=1 ]
    }
    
    // 9-in-1 multi-sensor device with all capabilities
    Thing multi-sensor multisensor1 [ id=85, refresh=0 ] {
        Channels:
            Type contact-channel : contact1 [ channelNumber=1 ]  // First dry contact
            Type contact-channel : contact2 [ channelNumber=2 ]  // Second dry contact  
            Type motion-channel : motion                         // Motion detection
            Type lux-channel : lux                              // Light level
    }
    
    // 9-in-1 sensor with only motion and lux (no contacts)
    Thing multi-sensor motionlux1 [ id=86, refresh=0 ] {
        Channels:
            Type motion-channel : motion
            Type lux-channel : lux
    }
}
```

### Item Configuration

```java
// Temperature Sensor
Number:Temperature Temp_Sensor "Temperature [%.1f °C]" { channel="sbus:temperature:mybridge:temp1:temperature" }

// Basic Switch
Switch Light_Switch "Switch" { channel="sbus:switch:mybridge:switch1:switch" }

// Paired Channel (e.g., for rollershutters)
Rollershutter Rollershutter_Switch "Rollershutter [%s]" { channel="sbus:switch:mybridge:switch1:third_switch" }

// RGBW Controller with Power Control
Group   gLight      "RGBW Light"    <light>     ["Lighting"]
Color   rgbwColor    "Color"        <colorwheel> (gLight)   ["Control", "Light"]    { channel="sbus:rgbw:mybridge:colorctrl:color" }
Switch  rgbwPower    "Power"        <switch>     (gLight)   ["Switch", "Light"]     { channel="sbus:rgbw:mybridge:colorctrl:power" }

// Traditional Contact Sensor
Contact Door_Contact "Door [%s]" <door> { channel="sbus:contact-sensor:mybridge:contact1:contact" }

// 9-in-1 Multi-Sensor Items
Contact Sensor_Contact1 "Sensor Contact 1 [%s]" <door> { channel="sbus:multi-sensor:mybridge:multisensor1:contact1" }
Contact Sensor_Contact2 "Sensor Contact 2 [%s]" <door> { channel="sbus:multi-sensor:mybridge:multisensor1:contact2" }
Switch Motion_Sensor "Motion [%s]" <motion> { channel="sbus:multi-sensor:mybridge:multisensor1:motion" }
Number Lux_Sensor "Light Level [%.0f lux]" <sun> { channel="sbus:multi-sensor:mybridge:multisensor1:lux" }

// Motion and Lux only sensor
Switch Motion_Only "Motion [%s]" <motion> { channel="sbus:multi-sensor:mybridge:motionlux1:motion" }
Number Lux_Only "Light Level [%.0f lux]" <sun> { channel="sbus:multi-sensor:mybridge:motionlux1:lux" }
```

### Sitemap Configuration

```perl
sitemap sbus label="Sbus Demo"
{
    Frame label="Sbus Controls" {
        Colorpicker item=rgbwColor
        Switch item=rgbwPower
        Text item=Temp_Sensor
        Switch item=Light_Switch
        Rollershutter item=Rollershutter_Switch
    }
    
    Frame label="Sensors" {
        Text item=Door_Contact
        Text item=Sensor_Contact1
        Text item=Sensor_Contact2
        Text item=Motion_Sensor
        Text item=Lux_Sensor
    }
}
```

## Usage Notes

### Sensor Device Types

The binding supports two distinct types of sensor devices:

#### Traditional Contact Sensors (`contact-sensor`)
- **Protocol**: Uses `ReadDryChannelsRequest/Response` 
- **Use Case**: Simple contact sensors with basic open/closed detection
- **Channels**: Contact channels only
- **Configuration**: Standard device configuration with channel numbers

#### Multi-Sensor (9-in-1) Devices (`multi-sensor`)
- **Protocol**: Uses `ReadNineInOneStatusRequest/Response` and `MotionSensorStatusReport` broadcasts
- **Use Case**: Advanced sensor devices combining multiple sensor types in one physical unit
- **Channels**: Any combination of contact, motion, and lux channels
- **Configuration**: Single device configuration with multiple channel types
- **Benefits**: 
  - Single polling job for all sensor data
  - Efficient communication with one physical device
  - Supports broadcast status updates for real-time responsiveness

**Choosing the Right Type:**
- Use `contact-sensor` for traditional, simple contact sensors
- Use `multi-sensor` for 9-in-1 devices that provide motion detection, light level sensing, and/or dry contact monitoring

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
