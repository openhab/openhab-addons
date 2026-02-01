# PR: Dahua Door Binding for VTO2202F Villastation

## Summary

This PR adds a new binding for Dahua VTO2202F Villastation door controllers, enabling openHAB integration with Dahua door intercoms.

## Features

### Device Support
- **VTO2202F Villastation**: Complete support for Dahua door intercom system

### Communication Protocols
- **DHIP Protocol**: Real-time event streaming for doorbell press, door status, etc.
- **HTTP Digest Authentication**: Secure communication with the device
- **Image Capture**: Automatic snapshot capture on doorbell press

### Channels
- `bell_button` (Trigger): Fires when doorbell is pressed
- `door_image` (Image): Provides camera snapshot on doorbell press
- `openDoor1` (Switch): Command to open door relay 1
- `openDoor2` (Switch): Command to open door relay 2

## Technical Implementation

### Event Handling
- **DahuaEventClient**: TCP socket client implementing DHIP protocol
  - Maintains persistent connection to device
  - Handles login with digest authentication
  - Processes JSON-based event stream
  - Automatic reconnection on connection loss

- **DahuaDoorHttpQueries**: HTTP client for device commands
  - Image retrieval with digest authentication
  - Door relay control

### Event Types Supported
The binding listens to comprehensive event types from the Dahua device:
- Door bell press
- Door open/close status
- Card swipe events
- Fingerprint authentication
- Network status changes
- Call state changes

## Use Cases

### Smart Home Integration
- Send smartphone notifications with door camera image when doorbell is pressed
- Automate door opening based on schedules or conditions
- Log visitor events with timestamps and images
- Integrate with presence detection systems

### Example Rule
```java
rule "Doorbell Notification"
when 
    Channel "dahuadoor:dahua_vth:frontdoor:bell_button" triggered PRESSED
then
    sendBroadcastNotification("Visitor at the door", "door", 
        "DoorTag", "Entrance", "notifications", null, 
        "item:DahuaDoor_Image", 
        "Open Door=command:DoorOpener:ON")
end
```

## Code Quality

All code follows openHAB standards:
- ✅ **@NonNullByDefault** annotations on all classes
- ✅ **Null-safe** field access patterns
- ✅ **Logger placeholders** instead of string concatenation
- ✅ **Copyright headers** with correct format (2010-2026)
- ✅ **Spotless formatting** applied
- ✅ **SAT analysis** passes with 0 errors
- ✅ **Java 21** compatibility

## Build Status

```
[INFO] BUILD SUCCESS
[INFO] openHAB Add-ons :: Bundles :: DahuaDoor Binding .... SUCCESS
[INFO] Code Analysis Tool: 0 error(s), 0 warning(s)
```

## Testing

The binding has been tested with:
- VTO2202F Villastation hardware
- Doorbell press events
- Image capture functionality
- Door relay control
- Connection loss and recovery

## Configuration

### Thing Configuration
| Parameter | Type | Description | Required |
|-----------|------|-------------|----------|
| hostname  | text | IP address or hostname | Yes |
| username  | text | Device username | Yes |
| password  | text | Device password | Yes |
| path      | text | Linux path for image storage | Yes |

**Note**: Windows paths are not currently supported.

## Documentation

Complete documentation provided in [README.md](bundles/org.openhab.binding.dahuadoor/README.md):
- Configuration examples
- Channel descriptions
- Rule examples with notification integration
- Full example setup

## Compatibility

- **openHAB Version**: 5.2.0-SNAPSHOT
- **Java Version**: 21
- **Platform**: Linux (image path handling)

## Future Enhancements

Potential improvements for future PRs:
- Windows path support
- Discovery via network scanning
- Additional device models (VTO/VTH series)
- Video stream support
- Two-way audio integration

## Related Issues

This binding addresses community requests for Dahua door intercom integration.

## Checklist

- [x] Code builds successfully
- [x] All SAT checks pass
- [x] @NonNullByDefault annotations added
- [x] Copyright headers correct
- [x] Spotless formatting applied
- [x] README.md documentation complete
- [x] Code signed off (DCO)
- [x] Tested with real hardware
