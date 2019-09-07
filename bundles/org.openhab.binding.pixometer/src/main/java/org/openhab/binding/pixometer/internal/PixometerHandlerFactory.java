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
package org.openhab.binding.pixometer.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.pixometer.handler.ApiserviceHandler;
import org.openhab.binding.pixometer.handler.MeterHandler;
import org.osgi.service.component.annotations.Component;

import jersey.repackaged.com.google.common.collect.Sets;

/**
 * The {@link PixometerHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jerome Luckenbach - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.pixometer", service = ThingHandlerFactory.class)
public class PixometerHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.union(
            PixometerBindingConstants.BRIDGE_THING_TYPES_UIDS, PixometerBindingConstants.SUPPORTED_THING_TYPES_UIDS);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(PixometerBindingConstants.THING_TYPE_ENERGYMETER)) {
            return new MeterHandler(thing);
        }

        if (thingTypeUID.equals(PixometerBindingConstants.THING_TYPE_GASMETER)) {
            return new MeterHandler(thing);
        }

        if (thingTypeUID.equals(PixometerBindingConstants.THING_TYPE_WATERMETER)) {
            return new MeterHandler(thing);
        }

        if (thingTypeUID.equals(PixometerBindingConstants.BRIDGE_THING_TYPE)) {
            return new ApiserviceHandler((Bridge) thing);
        }

        return null;
    }
}
