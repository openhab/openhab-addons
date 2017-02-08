package org.openhab.binding.lightify.internal;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.lightify.handler.DeviceHandler;
import org.openhab.binding.lightify.handler.GatewayHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openhab.binding.lightify.internal.LightifyConstants.SUPPORTED_THING_TYPES_UIDS;

public class LightifyHandlerFactory extends BaseThingHandlerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(LightifyHandlerFactory.class);

    @Override
    protected ThingHandler createHandler(Thing thing) {
        if (GatewayHandler.SUPPORTED_TYPES.contains(thing.getThingTypeUID())) {
            return new GatewayHandler((Bridge) thing);
        }
        if (DeviceHandler.SUPPORTED_TYPES.contains(thing.getThingTypeUID())) {
            return new DeviceHandler(thing);
        }
        return null;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        LOGGER.info("Can handle: " + thingTypeUID);
        boolean supported = SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
        LOGGER.info("Will handle: " + thingTypeUID + "[" + supported + "]");
        return supported;
    }
}
