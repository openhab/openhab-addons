# EnOcean Binding - Eltako FSB14 Advanced Blind Control

## Overview
The FSB14 actuator supports advanced blind control with independent positioning of rollershutter height and slat angle for venetian blinds.

## Configuration

### Thing Configuration
Enable advanced mode by setting `configMode="blinds"`:

```java
Thing enocean:rollershutter:gateway:device "My Blinds" (enocean:bridge:gateway) {
    Channels: 
        Type rollershutter:rollershutter [
            shutTime=65,        // Full travel time in seconds
            swapTime=14,        // Slat rotation time in 100ms units
            configMode="blinds" // Enable advanced blind control
        ]
}
```

### Channel Configuration Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| shutTime | int | Yes | - | Full travel time from 0% to 100% (seconds) |
| swapTime | int | No | 0 | Slat rotation time in 100ms units (e.g., 14 = 1.4s) |
| configMode | string | No | - | Set to "blinds" for advanced control, omit for legacy rollershutter mode |
| enoceanId | string | Yes | - | EnOcean device ID |
| senderIdOffset | int | Yes | - | Offset for sender ID |
| sendingEEPId | string | Yes | - | Must be "A5_3F_7F_EltakoFSB" |
| receivingEEPId | string | Yes | - | Must include "F6_00_00" and "A5_3F_7F_EltakoFSB" |

### Items Configuration

When `configMode="blinds"`, three channels are available:

```java
// Rollershutter height control (0-100%)
Rollershutter MyBlinds_Height "Blind Height" <blinds> {
    channel="enocean:rollershutter:gateway:device:rollershutter",
    autoupdate="false"
}

// Slat angle control (0-100%)
Dimmer MyBlinds_Slats "Slat Angle" <blinds> {
    channel="enocean:rollershutter:gateway:device:dimmer",
    autoupdate="false"
}

// State machine status (read-only)
String MyBlinds_Status "Status" {
    channel="enocean:rollershutter:gateway:device:statemachine"
}
```

**Important:** Always set `autoupdate="false"` for rollershutter and dimmer items to ensure accurate state tracking from device feedback.

## Usage

### Basic Commands

**Height Control:**
```java
// Move to 50% height
MyBlinds_Height.sendCommand(50)

// Fully open (0%)
MyBlinds_Height.sendCommand(UP)

// Fully close (100%)
MyBlinds_Height.sendCommand(DOWN)

// Stop movement
MyBlinds_Height.sendCommand(STOP)
```

**Slat Angle Control:**
```java
// Rotate slats to 75°
MyBlinds_Slats.sendCommand(75)

// Fully open slats (0%)
MyBlinds_Slats.sendCommand(0)

// Fully close slats (100%)
MyBlinds_Slats.sendCommand(100)
```

### State Machine States

The state machine provides feedback about current operation:

| State | Description |
|-------|-------------|
| `IDLE` | Ready to accept commands |
| `MOVEMENT_POSITION_UP` | Moving upward to target height |
| `MOVEMENT_POSITION_DOWN` | Moving downward to target height |
| `POSITION_REACHED` | Target height reached, ready for slat adjustment |
| `MOVEMENT_SLATS` | Adjusting slat angle |
| `MOVEMENT_CALIBRATION_UP` | Calibration: moving to top position |
| `MOVEMENT_CALIBRATION_DOWN` | Calibration: moving to bottom position |
| `INVALID` | Not calibrated, requires calibration |

### Initial Calibration

On first use or after power loss, the state machine requires calibration:

1. **Check status:** If `MyBlinds_Status` shows `INVALID`
2. **Move to top:** Send `UP` command
3. **Wait for completion:** Status changes to `IDLE`
4. **Move to bottom:** Send `DOWN` command  
5. **Wait for completion:** Status returns to `IDLE`
6. **Calibrated:** State machine now knows full travel range

The calibration process teaches the state machine the full travel time.

### Advanced Usage

**Scenes with Height and Slat Position:**
```java
// Morning scene: Half open with horizontal slats
rule "Morning Blinds"
when
    Time cron "0 0 7 * * ?"
then
    MyBlinds_Height.sendCommand(50)
    // Wait for position to be reached (check status or use timer)
    createTimer(now.plusSeconds(30), [ |
        MyBlinds_Slats.sendCommand(50)
    ])
end
```

**Dynamic Slat Adjustment:**
You can change slat angle during movement, but timing is critical. The state machine must be in `POSITION_REACHED` state before adjusting slats.

**Grouping Items:**
```java
Group gWindow_LivingRoom "Window Living Room" <blinds> ["Window"]
    Rollershutter MyBlinds_Height (gWindow_LivingRoom)
    Dimmer MyBlinds_Slats (gWindow_LivingRoom)
    String MyBlinds_Status (gWindow_LivingRoom)
```

In the UI, these items appear together, showing height, slat position, and current state.

## Requirements

### Hardware Setup
⚠️ **Critical:** The FSB14 must send feedback telegrams for state machine operation.

**FAM14 Configuration:**
1. Connect Eltako bus via FAM14 or FGW14-USB
2. Use PCT14 tool to configure FAM14
3. Add FSB14 to feedback list (Feedback List tab)
4. Set FAM14 to operating mode 7
5. **For addresses > 126:** Feedback is NOT sent over radio, bus connection required

**Supported Gateways:**
- FAM14 (with proper feedback configuration)
- FGW14-USB (direct bus connection)

**Not Supported:**
- Radio-only gateways for addresses > 126
- USB300 without FAM14

### Software Requirements
- openHAB 4.0 or later
- EnOcean binding (included in this PR)

## Troubleshooting

### Commands Are Ignored
**Symptom:** Sending commands has no effect

**Solutions:**
- Check `MyBlinds_Status` - if not `IDLE`, state machine is busy
- Wait for current operation to complete
- Verify `autoupdate="false"` is set on items
- Check openHAB logs for errors

### No Slat Adjustment
**Symptom:** Height changes but slats don't adjust

**Solutions:**
- Verify `swapTime` is configured (not 0)
- Ensure `configMode="blinds"` is set
- Check that dimmer item is linked to dimmer channel
- Verify FSB14 supports slat control (model-specific)

### State Lost After Restart
**Symptom:** After binding restart, status shows `INVALID`

**Known Limitation:** State persistence not yet implemented

**Workaround:**
- Perform calibration after each restart
- OR: Store positions in items with persistence service
- Future version will add Thing properties persistence

### No Feedback Received
**Symptom:** Commands sent but state machine stuck

**Solutions:**
- Verify FAM14 feedback list includes FSB14
- Check FAM14 operating mode is 7
- For addresses > 126: Ensure bus connection (not radio)
- Check openHAB logs for received telegrams
- Verify `receivingEEPId` includes "A5_3F_7F_EltakoFSB"

### Position Inaccurate
**Symptom:** Blind stops at wrong position

**Solutions:**
- Recalibrate (UP → DOWN sequence)
- Verify `shutTime` is accurate (time for full travel)
- Check for mechanical issues with blind
- Ensure no external interference during movement

## Legacy Mode

If you don't need independent slat control, omit the `configMode` parameter:

```java
Thing enocean:rollershutter:gateway:device {
    Channels: 
        Type rollershutter:rollershutter [
            shutTime=65
            // No swapTime, no configMode
        ]
}
```

In legacy mode:
- Only rollershutter channel is available
- No state machine overhead
- Compatible with all previous configurations
- Behaves like standard rollershutter binding

## Technical Notes

### State Machine Architecture
The state machine coordinates multi-stage movements:
1. User commands → State transitions
2. Device sends movement commands
3. Device receives feedback → State updates
4. State machine triggers next action (e.g., slat adjustment after position reached)

### Feedback Processing
- **Send:** `A5_3F_7F_EltakoFSB` EEP generates commands
- **Receive:** `PTM200Message` EEP processes feedback
- **Coordination:** State machine links send and receive paths

### Performance
- State transitions: < 100ms
- Command processing: Immediate (if IDLE)
- Feedback processing: Real-time

## Examples

### Complete Configuration
```java
// .things file
Bridge enocean:fgw14usb:fgw14usb [ path="/dev/ttyUSB0" ] {
    Thing rollershutter 0000b0ec "Blinds Living Room" @ "Living Room" [
        enoceanId="000000EC",
        senderIdOffset=108,
        sendingEEPId="A5_3F_7F_EltakoFSB",
        receivingEEPId="F6_00_00,A5_3F_7F_EltakoFSB",
        pollingInterval=300,
        broadcastMessages=true,
        suppressRepeating=false
    ] {
        Channels:
            Type rollershutter:rollershutter [
                shutTime=65,
                swapTime=14,
                configMode="blinds"
            ]
    }
}

// .items file
Group gLivingRoom_Window "Living Room Window" <blinds> ["Window"]

Rollershutter LivingRoom_Blinds "Height [%d %%]" <blinds> (gLivingRoom_Window) {
    channel="enocean:rollershutter:fgw14usb:0000b0ec:rollershutter",
    autoupdate="false"
}

Dimmer LivingRoom_Blinds_Slats "Slats [%d %%]" <blinds> (gLivingRoom_Window) {
    channel="enocean:rollershutter:fgw14usb:0000b0ec:dimmer",
    autoupdate="false"
}

String LivingRoom_Blinds_Status "Status [%s]" (gLivingRoom_Window) {
    channel="enocean:rollershutter:fgw14usb:0000b0ec:statemachine"
}
```

### Sitemap
```java
sitemap home label="Home" {
    Frame label="Living Room" {
        Text item=gLivingRoom_Window label="Window Controls" {
            Default item=LivingRoom_Blinds
            Slider item=LivingRoom_Blinds_Slats step=10
            Text item=LivingRoom_Blinds_Status
        }
    }
}
```

## Support

- **Forum:** https://community.openhab.org/c/add-ons/bindings/
- **Documentation:** https://www.openhab.org/addons/bindings/enocean/
- **Issues:** https://github.com/openhab/openhab-addons/issues
- **Original Discussion:** https://community.openhab.org/t/statemachine-for-eltako-fsb14-rollershutters/145978
