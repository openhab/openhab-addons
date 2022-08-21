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
package org.openhab.binding.arcam.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ArcamHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Joep Admiraal - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.arcam", service = ThingHandlerFactory.class)
public class ArcamHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(ArcamHandlerFactory.class);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        boolean result = ArcamBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
        logger.debug("supportsThingType thingTypeUID: {}, result: {}", thingTypeUID.getAsString(), result);

        return result;
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        logger.debug("ArcamHandlerFactory createHandler");
        if (ArcamBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new ArcamHandler(thing);
        }

        return null;
    }
}
