/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.tibber.internal;

import static org.openhab.binding.tibber.internal.TibberBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.tibber.internal.handler.TibberHandler;
import org.osgi.service.component.annotations.Component;
import org.slf4j.LoggerFactory;

/**
 * The {@link TibberHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Stian Kjoglum - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.tibber", service = ThingHandlerFactory.class)
public class TibberHandlerFactory extends BaseThingHandlerFactory {
    
    private org.slf4j.Logger logger = LoggerFactory.getLogger(TibberHandlerFactory.class);
    
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }
    
    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(TIBBER_THING_TYPE)) {
            TibberHandler tibberHandler = new TibberHandler(thing);
            return tibberHandler;
        } else {
            logger.warn("ThingHandler not found for {}", thing.getThingTypeUID());
            return null;
        }
    }
}
