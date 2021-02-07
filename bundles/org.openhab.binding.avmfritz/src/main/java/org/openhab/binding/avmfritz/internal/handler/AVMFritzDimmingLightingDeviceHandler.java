package org.openhab.binding.avmfritz.internal.handler;

import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * Handler for a FRITZ! lighting device
 *
 * @author Joshua Bacher - Initial contribution
 */
public class AVMFritzDimmingLightingDeviceHandler extends DeviceHandler implements ThingHandler {

    public AVMFritzDimmingLightingDeviceHandler(Thing thing) {
        super(thing);
    }
}
