package org.openhab.binding.avmfritz.internal.handler;

import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * Handler for a FRITZ! lighting device
 *
 * @author Joshua Bacher - Initial contribution
 */
public class AVMFritzLightingDeviceHandler extends DeviceHandler implements ThingHandler {

    public AVMFritzLightingDeviceHandler(Thing thing) {
        super(thing);
    }
}
