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
package org.openhab.binding.solax.internal;

import static org.openhab.binding.solax.internal.SolaxBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SolaxHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.solax", service = ThingHandlerFactory.class)
public class SolaxHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(SolaxHandlerFactory.class);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        logger.debug("Attempting to create handler for thing {}", thing);
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_LOCAL_CONNECT_BRIDGE.equals(thingTypeUID)) {
            try {
                logger.debug("Creating bridge handler of type {}", THING_TYPE_LOCAL_CONNECT_BRIDGE);
                return new SolaxBridgeHandler((Bridge) thing);
            } catch (Exception e) {
                logger.error("Unable to create Solax Bridge handler", e);
            }
        } else if (THING_TYPE_INVERTER.equals(thingTypeUID)) {
            logger.debug("Creating inverter handler of type {}", THING_TYPE_INVERTER);
            return new SolaxInverterHandler(thing);
        } else {
            logger.warn("Unsupported thing/bridge type UID. UID={}", thingTypeUID);
            throw new IllegalStateException("Unsupported handler type");
        }

        return null;
    }
}
