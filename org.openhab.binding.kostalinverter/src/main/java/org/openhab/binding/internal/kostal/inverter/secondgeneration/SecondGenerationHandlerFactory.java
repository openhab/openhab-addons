/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

package org.openhab.binding.internal.kostal.inverter.secondgeneration;

import static org.openhab.binding.internal.kostal.inverter.secondgeneration.SecondGenerationBindingConstants.SECOND_GENERATION_INVERTER;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link SecondGenerationHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Örjan Backsell - Initial contribution Piko1020, Piko New Generation
 */
@NonNullByDefault
@Component(configurationPid = "binding.kostalinverter", service = ThingHandlerFactory.class)
public class SecondGenerationHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> kostalinverterpiko1020 = Collections.singleton(SECOND_GENERATION_INVERTER);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return kostalinverterpiko1020.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (SECOND_GENERATION_INVERTER.equals(thingTypeUID)) {
            return new SecondGenerationHandler(thing);
        }

        return null;
    }
}