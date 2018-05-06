/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solarlog.internal;

import static org.openhab.binding.solarlog.SolarLogBindingConstants.THING_SOLARLOG;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.solarlog.handler.SolarLogHandler;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SolarLogHandlerFactory} is responsible for creating things and thing
 * handlers. It is completely boiler-plate and nothing special at all.
 *
 * @author Johann Richard - Initial contribution
 */
@Component(configurationPid = "binding.solarlog", service = ThingHandlerFactory.class)
public class SolarLogHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(SolarLogHandlerFactory.class);
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_SOLARLOG);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        logger.debug("Create Thing Handler {}", THING_SOLARLOG);
        if (THING_SOLARLOG.equals(thingTypeUID)) {
            return new SolarLogHandler(thing);
        }

        return null;
    }
}
