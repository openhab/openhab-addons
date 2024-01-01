/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.meteostick.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.meteostick.internal.handler.MeteostickBridgeHandler;
import org.openhab.binding.meteostick.internal.handler.MeteostickSensorHandler;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MeteostickHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Chris Jackson - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.meteostick")
public class MeteostickHandlerFactory extends BaseThingHandlerFactory {
    private Logger logger = LoggerFactory.getLogger(MeteostickHandlerFactory.class);

    private final SerialPortManager serialPortManager;

    @Activate
    public MeteostickHandlerFactory(final @Reference SerialPortManager serialPortManager) {
        this.serialPortManager = serialPortManager;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return MeteostickBridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)
                | MeteostickSensorHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        logger.debug("MeteoStick thing factory: createHandler {} of type {}", thing.getThingTypeUID(), thing.getUID());

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (MeteostickBridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new MeteostickBridgeHandler((Bridge) thing, serialPortManager);
        }

        if (MeteostickSensorHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new MeteostickSensorHandler(thing);
        }

        return null;
    }
}
