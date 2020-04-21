package org.openhab.binding.innogysmarthome.internal.client.entity.action;

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

    /*
        RAMP DOWN
        {"id":"276cec8746a24fca816795fc914d7d90",
         "type":"StartRamp",
         "target":"/capability/2bce36dbaeb541c58dc691f2bc9d2ca2",
         "namespace":"CosipDevices.RWE",
         "params":{"rampDirection":{"type":"Constant","value":"RampDown"}}}
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
