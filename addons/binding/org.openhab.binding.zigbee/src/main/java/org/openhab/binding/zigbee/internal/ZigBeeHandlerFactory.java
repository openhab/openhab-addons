/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zigbee.internal;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.zigbee.ZigBeeBindingConstants;
import org.openhab.binding.zigbee.handler.ZigBeeCoordinatorCC2530Handler;
import org.openhab.binding.zigbee.handler.ZigBeeThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ZigBeeHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Chris Jackson - Initial contribution
 */
public class ZigBeeHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(ZigBeeHandlerFactory.class);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return ZigBeeBindingConstants.BINDING_ID.equals(thingTypeUID.getBindingId());
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        // Handle coordinators here
        if (thingTypeUID.equals(ZigBeeBindingConstants.COORDINATOR_TYPE_CC2530)) {
            return new ZigBeeCoordinatorCC2530Handler((Bridge) thing);
        }

        // Everything else gets handled in a single handler
        return new ZigBeeThingHandler(thing);
    }

    // @Override
    // public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID,
    // ThingUID bridgeUID) {

    // If this isn't the discovery type, then just create it
    // if(thingTypeUID.equals(ZigBeeBindingConstants.)) {
    // return super.createThing(thingTypeUID, configuration, thingUID, bridgeUID);
    // }

    // configuration.getProperties();

    // ChannelUID channelUID = null;
    // Channel channel = ChannelBuilder.create(channelUID, "").build();
    // List<Channel> channels = new ArrayList<Channel>();
    // Thing thing = ThingBuilder.create(thingTypeUID, thingUID).withLabel(label).withBridge(bridgeUID)
    // .withChannels(channels).build();

    // }
    /*
     * if (SUPPORTED_BRIDGE_TYPES_UIDS.contains(thingTypeUID)) {
     * ThingUID zigbeeBridgeUID = getBridgeThingUID(thingTypeUID, thingUID, configuration);
     * return super.createThing(thingTypeUID, configuration, zigbeeBridgeUID, null);
     * }
     * if (SUPPORTED_DEVICE_TYPES_UIDS.contains(thingTypeUID)) {
     * ThingUID deviceUID = getZigBeeDeviceUID(thingTypeUID, thingUID, configuration, bridgeUID);
     * return super.createThing(thingTypeUID, configuration, deviceUID, bridgeUID);
     * }
     * throw new IllegalArgumentException("The thing type " + thingTypeUID + " is not supported by the binding.");
     * }
     *
     * private ThingUID getBridgeThingUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration) {
     * if (thingUID == null) {
     * String SerialNumber = (String) configuration.get(ZigBeeBindingConstants.PARAMETER_PANID);
     * thingUID = new ThingUID(thingTypeUID, SerialNumber);
     * }
     * return thingUID;
     * }
     *
     * private ThingUID getZigBeeDeviceUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration,
     * ThingUID bridgeUID) {
     * String SerialNumber = (String) configuration.get(ZigBeeBindingConstants.PARAMETER_MACADDRESS);
     *
     * if (thingUID == null) {
     * thingUID = new ThingUID(thingTypeUID, SerialNumber, bridgeUID.getId());
     * }
     * return thingUID;
     * }
     */
}
