package org.openhab.binding.avmfritz.internal.handler;

import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for a FRITZ! lighting device
 *
 * @author Joshua Bacher - Initial contribution
 */
public class AVMFritzDimmingLightingDeviceHandler extends DeviceHandler implements ThingHandler {
    private final Logger logger = LoggerFactory.getLogger(AVMFritzDimmingLightingDeviceHandler.class);

    public AVMFritzDimmingLightingDeviceHandler(Thing thing) {
        super(thing);
        logger.debug("initialized avm fritz dimming light device handler.");
    }
}
