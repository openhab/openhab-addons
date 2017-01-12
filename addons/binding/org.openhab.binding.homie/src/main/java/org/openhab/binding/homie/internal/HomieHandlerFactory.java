/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homie.internal;

import static org.openhab.binding.homie.HomieBindingConstants.HOMIE_THING_TYPE;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.homie.handler.HomieHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HomieHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Michael Kolb - Initial contribution
 */
public class HomieHandlerFactory extends BaseThingHandlerFactory {
    private static Logger logger = LoggerFactory.getLogger(HomieHandlerFactory.class);

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(HOMIE_THING_TYPE);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(HOMIE_THING_TYPE)) {
            logger.info("Create homie thing for " + thing.toString());
            return new HomieHandler(thing);
        }

        return null;
    }

    @Override
    public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID,
            ThingUID bridgeUID) {
        Thing result = super.createThing(thingTypeUID, configuration, thingUID, bridgeUID);

        return result;
    }

}
