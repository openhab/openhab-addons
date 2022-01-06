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
package org.openhab.binding.boschindego.internal;

import static org.openhab.binding.boschindego.internal.BoschIndegoBindingConstants.THING_TYPE_INDEGO;

import java.util.Collections;
import java.util.Set;

import org.openhab.binding.boschindego.internal.handler.BoschIndegoHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link BoschIndegoHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jonas Fleck - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.boschindego")
public class BoschIndegoHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_INDEGO);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_INDEGO)) {
            return new BoschIndegoHandler(thing);
        }

        return null;
    }
}
