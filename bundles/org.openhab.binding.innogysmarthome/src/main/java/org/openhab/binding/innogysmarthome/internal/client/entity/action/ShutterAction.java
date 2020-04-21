package org.openhab.binding.innogysmarthome.internal.client.entity.action;

/**
 * Special {@link Action} needed to control shutters.
 *
 * @author Marco Mans
 */
public class ShutterAction extends Action {

    public enum ShutterActions {
        UP,
        DOWN,
        STOP
    }

    private final String TYPE_STOP_RAMP = "StopRamp";
    private final String TYPE_START_RAMP = "StartRamp";
    private final String DIRECTION_RAMP_UP = "RampUp";
    private final String DIRECTION_RAMP_DOWN = "RampDown";
    private static final String CONSTANT = "Constant";
    private final String NAMESPACE_COSIP = "CosipDevices.RWE";


    /**
     * Describes a Shutteraction
     *
     * @param capabilityId String of the 32 character capability id
     * @param action Which action to perform (UP, DOWN, STOP)
     */
    public ShutterAction(String capabilityId, ShutterActions action) {
        setTargetCapabilityById(capabilityId);
        setNamespace(NAMESPACE_COSIP);
        final ActionParams params = new ActionParams();

        if (ShutterActions.STOP.equals(action)) {
            setType(TYPE_STOP_RAMP);
        } else if (ShutterActions.UP.equals(action)) {
            setType(TYPE_START_RAMP);
            params.setRampDirection(new StringActionParam(CONSTANT, DIRECTION_RAMP_UP));
        } else if (ShutterActions.DOWN.equals(action)) {
            setType(TYPE_START_RAMP);
            params.setRampDirection(new StringActionParam(CONSTANT, DIRECTION_RAMP_DOWN));
        }
        setParams(params);
    }
}
