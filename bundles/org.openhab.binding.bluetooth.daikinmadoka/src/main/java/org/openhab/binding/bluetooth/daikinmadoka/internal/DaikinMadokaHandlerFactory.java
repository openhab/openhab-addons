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
package org.openhab.binding.bluetooth.daikinmadoka.internal;

import static org.openhab.binding.bluetooth.daikinmadoka.DaikinMadokaBindingConstants.THING_TYPE_BRC1H;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.daikinmadoka.handler.DaikinMadokaHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DaikinMadokaHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Benjamin Lafois - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.bluetooth.daikinmadoka", service = ThingHandlerFactory.class)
public class DaikinMadokaHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_BRC1H);

    private final Logger logger = LoggerFactory.getLogger(DaikinMadokaHandlerFactory.class);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        logger.debug("Request to create handler for thing {}", thing.getThingTypeUID());

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_BRC1H.equals(thingTypeUID)) {
            logger.debug("Thing is matching BRC1H");

            return new DaikinMadokaHandler(thing);
        }

        return null;
    }
}
