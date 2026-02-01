# Add state machine support for Eltako FSB14 blinds

## Summary
Adds comprehensive blind control with independent slat positioning for Eltako FSB14 actuators. This enables precise control of both rollershutter position and slat angle through a state machine implementation.

## Motivation
Standard rollershutter control doesn't support independent slat positioning required for venetian blinds. Users need to control:
- Rollershutter height (0-100%)
- Slat angle (0-100%) independently

Previous attempts using rules became unmaintainable due to timing complexity and feedback handling.

## Solution Overview

### New Features
- **State Machine:** Tracks multi-stage movements (position → slat adjustment)
- **New Channels:**
  - `rollershutter` - Existing channel for height control
  - `dimmer` - New channel for slat angle control
  - `statemachine` - New status channel showing current state
- **New Configuration:**
  - `swapTime` - Slat rotation time in 100ms units (e.g., 14 = 1.4s)
  - `configMode` - "blinds" or "rollershutter" (legacy mode)

### Architecture
- **Send Path:** `A5_3F_7F_EltakoFSB` sends commands and manages state transitions
- **Receive Path:** `PTM200Message` processes feedback and updates state machine
- **Backward Compatible:** Legacy mode when `configMode != "blinds"`

## Implementation Details

### Modified Files
Core implementation:
- `A5_3F_7F_EltakoFSB.java` - Command generation with state machine support
- `PTM200Message.java` - Feedback processing with state machine updates
- `EnOceanBaseActuatorHandler.java` - Handler support for new channels

State machine components (new):
- `STMStateMachine.java` - State machine implementation
- `STMState.java` - State definitions (IDLE, MOVEMENT_*, POSITION_REACHED, etc.)
- `STMAction.java` - Action definitions (POSITION_REQUEST_*, SLATS_POS_REQUEST, etc.)
- `STMTransition.java` - State transition definitions
- `STMTransitionConfiguration.java` - Configuration for blind-specific transitions
- `STMCallbackAction.java` - Callback interface for state machine actions

Configuration:
- `channels.xml` - New channel definitions (dimmer, statemachine)

### State Machine Flow
```
IDLE → MOVEMENT_POSITION_UP/DOWN → POSITION_REACHED → 
MOVEMENT_SLATS → SLATS_DONE → IDLE

Calibration flow (first use):
INVALID → CALIBRATION_UP → IDLE → CALIBRATION_DOWN → IDLE
```

### Configuration Example
```java
Thing enocean:rollershutter:fgw14usb:0000b0ec "Blinds Living Room" {
    Channels: 
        Type rollershutter:rollershutter [
            shutTime=65,        // Full travel time in seconds
            swapTime=14,        // Slat rotation time in 100ms units
            configMode="blinds" // Enable advanced blind control
        ]
}
```

### Items Example
```java
Rollershutter LivingRoom_Blinds "Blinds Living Room" { 
    channel="enocean:rollershutter:fgw14usb:0000b0ec:rollershutter", 
    autoupdate="false"
}
Dimmer LivingRoom_Blinds_Slats "Slats Living Room" { 
    channel="enocean:rollershutter:fgw14usb:0000b0ec:dimmer", 
    autoupdate="false"
}
String LivingRoom_Blinds_Status "Status" { 
    channel="enocean:rollershutter:fgw14usb:0000b0ec:statemachine"
}
```

## Requirements
⚠️ **Important:** Requires feedback telegrams from FSB14:
- FAM14: Add FSB14 to feedback list (use PCT14 tool, set mode 7)
- Addresses > 126: Must use FAM14/FGW14-USB connection (no radio feedback for high addresses)
- The Eltako bus must be connected via FAM or FGW14-USB for feedback telegrams

## Testing
- Tested on openHAB 4.1.1
- Tested with Eltako FSB14 actuators
- Tested with FAM14/FGW14-USB gateways
- Community testing requested in forum: https://community.openhab.org/t/statemachine-for-eltako-fsb14-rollershutters/145978

## Known Limitations
1. **State Persistence:** State is lost on binding restart (requires recalibration)
   - TODO: Add state persistence via Thing properties
2. **Architecture:** State machine is shared across two EEP classes (see TODO comments in code)
   - TODO: Move state machine to handler level for better separation of concerns
3. **Command Blocking:** Commands are ignored while state machine is busy (by design for safety)
4. **Dynamic Adjustment:** Slat angle can be changed during movement, but requires careful timing

## Future Improvements
- [ ] Move state machine to handler level for better separation
- [ ] Add state persistence via Thing properties
- [ ] Add calibration wizard for initial setup
- [ ] Support for pause/resume during movement
- [ ] Enhanced error handling and recovery

## Breaking Changes
None - fully backward compatible via `configMode` parameter. Existing configurations work without changes.

## Checklist
- [x] Code follows openHAB coding guidelines
- [x] Added `@NonNullByDefault` annotations
- [x] Added copyright headers to all new files
- [x] Backward compatibility maintained (legacy mode)
- [x] TODO comments added for architectural improvements
- [x] Build passes with Maven 3.9.6
- [x] Spotless formatting applied
- [x] Static code analysis warnings addressed
- [ ] Documentation updated (README.md) - to be added
- [x] Community tested (forum post from December)

## Related Issues
- Community forum discussion: https://community.openhab.org/t/statemachine-for-eltako-fsb14-rollershutters/145978
- Original binding: https://github.com/openhab/openhab-addons/tree/main/bundles/org.openhab.binding.enocean

## Author Notes
This implementation has been in community testing since December 2024. The architecture uses state machine coupling across EEP classes (documented in TODO comments) as a pragmatic solution for closed-loop control. Future refactoring to move state machine to handler level is planned but should be done with community feedback.

Happy to address any review comments and make adjustments as needed!

@openhab/add-ons-maintainers
