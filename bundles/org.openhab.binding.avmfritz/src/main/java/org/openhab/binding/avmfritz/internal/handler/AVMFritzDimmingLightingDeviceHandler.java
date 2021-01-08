package org.openhab.binding.avmfritz.internal.handler;

import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * Handler for a FRITZ! dimming lighting device
 *
 * @author Joshua Bacher - hacker
 */
public class AVMFritzDimmingLightingDeviceHandler extends AVMFritzLightingDeviceHandler implements ThingHandler {

    public AVMFritzDimmingLightingDeviceHandler(Thing thing) {
        super(thing);
    }
}
