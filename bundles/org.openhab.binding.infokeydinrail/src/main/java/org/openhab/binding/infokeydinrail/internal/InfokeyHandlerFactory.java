/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.infokeydinrail.internal;

import static org.openhab.binding.infokeydinrail.internal.InfokeyBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.infokeydinrail.internal.handler.Infokey2CoilRelayDinV1Handler;
import org.openhab.binding.infokeydinrail.internal.handler.InfokeyDhtHandler;
import org.openhab.binding.infokeydinrail.internal.handler.InfokeyMCP3008DinV1Handler;
import org.openhab.binding.infokeydinrail.internal.handler.InfokeyMosfetDinV1Handler;
import org.openhab.binding.infokeydinrail.internal.handler.InfokeyOptoDinV1Handler;
import org.openhab.binding.infokeydinrail.internal.handler.InfokeyRelayOptoDinV1Handler;
import org.openhab.binding.infokeydinrail.internal.handler.WaterFlowmeterYFS201;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link InfokeyHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Themistoklis Anastasopoulos - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.infokey", service = ThingHandlerFactory.class)
public class InfokeyHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(InfokeyHandlerFactory.class);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        logger.debug("Trying to create handler for {}", thingTypeUID.getAsString());

        if (THING_TYPE_RELAY_OPTO_DIN_V1.equals(thingTypeUID)) {
            logger.debug("Handler match for {}", thingTypeUID.getAsString());
            return new InfokeyRelayOptoDinV1Handler(thing);
        } else if (THING_TYPE_OPTO_DIN_V1.equals(thingTypeUID)) {
            logger.debug("Handler match for {}", thingTypeUID.getAsString());
            return new InfokeyOptoDinV1Handler(thing);
        } else if (THING_TYPE_MOSFET_DIN_V1.equals(thingTypeUID)) {
            logger.debug("Handler match for {}", thingTypeUID.getAsString());
            return new InfokeyMosfetDinV1Handler(thing);
        } else if (THING_TYPE_2COIL_RELAY_DIN_V1.equals(thingTypeUID)) {
            logger.debug("Handler match for {}", thingTypeUID.getAsString());
            return new Infokey2CoilRelayDinV1Handler(thing);
        } else if (THING_TYPE_MCP3008_DIN_V1.equals(thingTypeUID)) {
            logger.debug("Handler match for {}", thingTypeUID.getAsString());
            return new InfokeyMCP3008DinV1Handler(thing);
        } else if (THING_TYPE_DHT.equals(thingTypeUID)) {
            logger.debug("Handler match for {}", thingTypeUID.getAsString());
            return new InfokeyDhtHandler(thing);
        } else if (THING_TYPE_WATER_FLOWMETER.equals(thingTypeUID)) {
            logger.debug("Handler match for {}", thingTypeUID.getAsString());
            return new WaterFlowmeterYFS201(thing);
        }

        logger.debug("No handler match for {}", thingTypeUID.getAsString());
        return null;
    }
}
