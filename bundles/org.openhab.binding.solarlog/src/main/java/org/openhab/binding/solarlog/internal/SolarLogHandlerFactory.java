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
package org.openhab.binding.solarlog.internal;

import static org.openhab.binding.solarlog.internal.SolarLogBindingConstants.THING_SOLARLOG;

import java.util.Collections;
import java.util.Set;

import org.openhab.binding.solarlog.internal.handler.SolarLogHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
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
