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
package org.openhab.binding.proteusecometer.internal;

import static org.openhab.binding.proteusecometer.internal.ProteusEcoMeterBindingConstants.THING_TYPE_ECO_METER_S;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.proteusecometer.internal.ecometers.handler.ProteusEcoMeterSHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ProteusEcoMeterHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Matthias Herrmann - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.proteusecometer", service = ThingHandlerFactory.class)
public class ProteusEcoMeterHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ECO_METER_S);
    private final Logger logger = LoggerFactory.getLogger(ProteusEcoMeterHandlerFactory.class);

    @Activate
    public ProteusEcoMeterHandlerFactory() {
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_ECO_METER_S.equals(thingTypeUID)) {
            logger.trace("Creating ProteusEcoMeterSHandler");
            return new ProteusEcoMeterSHandler(thing);
        }

        return null;
    }
}
