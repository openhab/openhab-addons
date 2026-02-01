# Description

## Classification
**Enhancement / Novel Addition** - Adds advanced blind control with state machine for Eltako FSB14 actuators.

## Summary
Adds comprehensive blind control with independent slat positioning for Eltako FSB14 actuators. This enables precise control of both rollershutter position and slat angle through a state machine implementation.

## Motivation and Prior Discussion
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

## Documentation
- ✅ **New documentation added:** `FSB14_BLIND_CONTROL.md` - Complete user guide with configuration, usage, troubleshooting, and examples
- ✅ **TODO comments:** Added architectural improvement notes in code
- ⚠️ **Main README:** To be updated after review feedback (if accepted)

## Compliance Checklist
- [x] Code follows [openHAB coding guidelines](https://www.openhab.org/docs/developer/guidelines.html)
- [x] Added `@NonNullByDefault` annotations
- [x] Added copyright headers to all new files
- [x] Backward compatibility maintained (legacy mode via `configMode` parameter)
- [x] TODO comments added for architectural improvements
- [x] Build passes with Maven 3.9.6
- [x] Spotless formatting applied (`mvn spotless:apply`)
- [x] Static code analysis passed (0 errors, only info messages)
- [x] All commits are [signed-off](https://www.openhab.org/docs/developer/contributing.html#sign-your-work)
- [x] Community tested (forum post from December 2024)

## Related Issues
- Community forum discussion: https://community.openhab.org/t/statemachine-for-eltako-fsb14-rollershutters/145978
- Original binding: https://github.com/openhab/openhab-addons/tree/main/bundles/org.openhab.binding.enocean

## Testing

### Community Testing
- Forum discussion with testing request: https://community.openhab.org/t/statemachine-for-eltako-fsb14-rollershutters/145978
- Tested by community members since December 2024
- Tested on openHAB 4.1.1 with Eltako FSB14 actuators

### Build Artifacts
After PR is created, build artifacts will be available at:
```
https://openhab.jfrog.io/ui/native/libs-pullrequest-local/org/openhab/addons/bundles/org.openhab.binding.enocean/
```

### Test Instructions
1. Install JAR from build artifacts
2. Configure FSB14 with `configMode="blinds"` (see `FSB14_BLIND_CONTROL.md`)
3. Verify feedback telegrams from FSB14 (FAM14 configuration required)
4. Test height control, slat adjustment, and state machine transitions

## Notes for Reviewers

### Architectural Decisions
This implementation uses state machine coupling across EEP classes (documented in TODO comments) as a pragmatic solution for closed-loop control:
- **Send path:** `A5_3F_7F_EltakoFSB` generates commands and manages state transitions
- **Receive path:** `PTM200Message` processes feedback and updates state machine

This approach was chosen to:
- Implement closed-loop control with minimal changes to existing handler architecture
- Maintain backward compatibility
- Enable community testing quickly

Future refactoring to move state machine to handler level is documented in TODO comments and can be done as a follow-up PR based on maintainer feedback.

### Backward Compatibility
- ✅ Fully backward compatible via `configMode` parameter
- ✅ Legacy mode (omit `configMode`) works exactly as before
- ✅ No breaking changes to existing configurations

Happy to address any review comments and make adjustments as needed!

@openhab/add-ons-maintainers
