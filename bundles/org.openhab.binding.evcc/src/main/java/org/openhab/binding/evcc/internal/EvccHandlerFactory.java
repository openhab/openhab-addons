/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.evcc.internal;

import static org.openhab.binding.evcc.internal.EvccBindingConstants.*;

import java.util.Set;

import org.openhab.binding.evcc.internal.handler.EvccBatteryHandler;
import org.openhab.binding.evcc.internal.handler.EvccBridgeHandler;
import org.openhab.binding.evcc.internal.handler.EvccLoadpointHandler;
import org.openhab.binding.evcc.internal.handler.EvccPvHandler;
import org.openhab.binding.evcc.internal.handler.EvccSiteHandler;
import org.openhab.binding.evcc.internal.handler.EvccVehicleHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link EvccHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Florian Hotze - Initial contribution
 */
@Component(configurationPid = "binding.evcc", service = ThingHandlerFactory.class)
public class EvccHandlerFactory extends BaseThingHandlerFactory {

    @Reference
    private HttpClientFactory httpClientFactory;

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_BRIDGE, THING_TYPE_SITE,
            THING_TYPE_VEHICLE, THING_TYPE_LOADPOINT, THING_TYPE_BATTERY, THING_TYPE_PV);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID type = thing.getThingTypeUID();

        if (THING_TYPE_BRIDGE.equals(type) && thing instanceof Bridge) {
            return new EvccBridgeHandler((Bridge) thing, httpClientFactory);
        }

        if (THING_TYPE_SITE.equals(type)) {
            return new EvccSiteHandler(thing);
        }

        if (THING_TYPE_VEHICLE.equals(type)) {
            return new EvccVehicleHandler(thing);
        }

        if (THING_TYPE_LOADPOINT.equals(type)) {
            return new EvccLoadpointHandler(thing);
        }

        if (THING_TYPE_BATTERY.equals(type)) {
            return new EvccBatteryHandler(thing);
        }

        if (THING_TYPE_PV.equals(type)) {
            return new EvccPvHandler(thing);
        }

        return null;
    }
}
