/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.max.internal.factory;

import static org.openhab.binding.max.internal.MaxBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.max.internal.handler.MaxCubeBridgeHandler;
import org.openhab.binding.max.internal.handler.MaxDevicesHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MaxCubeHandlerFactory} is responsible for creating things and
 * thing handlers.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.max")
public class MaxCubeHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(MaxCubeHandlerFactory.class);

    @Override
    public @Nullable Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration,
            @Nullable ThingUID thingUID, @Nullable ThingUID bridgeUID) {
        if (CUBEBRIDGE_THING_TYPE.equals(thingTypeUID)) {
            ThingUID cubeBridgeUID = getBridgeThingUID(thingTypeUID, thingUID, configuration);
            return super.createThing(thingTypeUID, configuration, cubeBridgeUID, null);
        }
        if (supportsThingType(thingTypeUID) && bridgeUID != null) {
            ThingUID deviceUID = getMaxCubeDeviceUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, deviceUID, bridgeUID);
        }
        throw new IllegalArgumentException("The thing type " + thingTypeUID + " is not supported by the binding.");
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    private ThingUID getBridgeThingUID(ThingTypeUID thingTypeUID, @Nullable ThingUID thingUID,
            Configuration configuration) {
        if (thingUID == null) {
            String serialNumber = (String) configuration.get(Thing.PROPERTY_SERIAL_NUMBER);
            return new ThingUID(thingTypeUID, serialNumber);
        }
        return thingUID;
    }

    private ThingUID getMaxCubeDeviceUID(ThingTypeUID thingTypeUID, @Nullable ThingUID thingUID,
            Configuration configuration, ThingUID bridgeUID) {
        if (thingUID == null) {
            String serialNumber = (String) configuration.get(Thing.PROPERTY_SERIAL_NUMBER);
            return new ThingUID(thingTypeUID, serialNumber, bridgeUID.getId());
        }
        return thingUID;
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (CUBEBRIDGE_THING_TYPE.equals(thingTypeUID)) {
            return new MaxCubeBridgeHandler((Bridge) thing);
        } else if (SUPPORTED_DEVICE_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new MaxDevicesHandler(thing);
        } else {
            logger.debug("ThingHandler not found for {}", thingTypeUID);
            return null;
        }
    }
}
