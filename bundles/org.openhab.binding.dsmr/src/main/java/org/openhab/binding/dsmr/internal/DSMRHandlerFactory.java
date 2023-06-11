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
package org.openhab.binding.dsmr.internal;

import static org.openhab.binding.dsmr.internal.DSMRBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dsmr.internal.handler.DSMRBridgeHandler;
import org.openhab.binding.dsmr.internal.handler.DSMRMeterHandler;
import org.openhab.binding.dsmr.internal.meter.DSMRMeterType;
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
 * The {@link DSMRHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author M. Volaart - Initial contribution
 * @author Hilbrand Bouwkamp - Refactored discovery service to use standard discovery class methods.
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.dsmr")
public class DSMRHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(DSMRHandlerFactory.class);

    private final SerialPortManager serialPortManager;

    @Activate
    public DSMRHandlerFactory(@Reference SerialPortManager serialPortManager) {
        this.serialPortManager = serialPortManager;
    }

    /**
     * Returns if the specified ThingTypeUID is supported by this handler.
     *
     * This handler support the THING_TYPE_DSMR_BRIDGE type and all ThingTypesUID that
     * belongs to the supported DSMRMeterType objects
     *
     * @param thingTypeUID {@link ThingTypeUID} to check
     * @return true if the specified ThingTypeUID is supported, false otherwise
     */
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        if (THING_TYPE_DSMR_BRIDGE.equals(thingTypeUID) || THING_TYPE_SMARTY_BRIDGE.equals(thingTypeUID)) {
            logger.debug("DSMR Bridge Thing {} supported", thingTypeUID);
            return true;
        } else {
            boolean thingTypeUIDIsMeter = DSMRMeterType.METER_THING_TYPES.contains(thingTypeUID);

            if (thingTypeUIDIsMeter) {
                logger.trace("{} is a supported DSMR Meter thing", thingTypeUID);
            }
            return thingTypeUIDIsMeter;
        }
    }

    /**
     * Create the ThingHandler for the corresponding Thing
     *
     * There are two handlers supported:
     * - DSMRBridgeHandler that handle the Thing that corresponds to the physical DSMR device and does the serial
     * communication
     * - MeterHandler that handles the Meter things that are a logical part of the physical device
     *
     * @param thing The Thing to create a ThingHandler for
     * @return ThingHandler for the given Thing or null if the Thing is not supported
     */
    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        logger.debug("Searching for thingTypeUID {}", thingTypeUID);

        if (THING_TYPE_DSMR_BRIDGE.equals(thingTypeUID) || THING_TYPE_SMARTY_BRIDGE.equals(thingTypeUID)) {
            return new DSMRBridgeHandler((Bridge) thing, serialPortManager);
        } else if (DSMRMeterType.METER_THING_TYPES.contains(thingTypeUID)) {
            return new DSMRMeterHandler(thing);
        }

        return null;
    }
}
