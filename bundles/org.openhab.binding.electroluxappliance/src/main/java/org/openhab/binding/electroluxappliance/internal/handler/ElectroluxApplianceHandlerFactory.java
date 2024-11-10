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
package org.openhab.binding.electroluxappliance.internal.handler;

import static org.openhab.binding.electroluxappliance.internal.ElectroluxApplianceBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link ElectroluxApplianceHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.electroluxappliance", service = ThingHandlerFactory.class)
public class ElectroluxApplianceHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ELECTROLUX_AIR_PURIFIER,
            THING_TYPE_ELECTROLUX_WASHING_MACHINE, THING_TYPE_BRIDGE);
    private final Gson gson;
    private HttpClient httpClient;
    private final Logger logger = LoggerFactory.getLogger(ElectroluxApplianceHandlerFactory.class);

    @Activate
    public ElectroluxApplianceHandlerFactory(@Reference HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.gson = new Gson();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_ELECTROLUX_AIR_PURIFIER.equals(thingTypeUID)) {
            return new ElectroluxAirPurifierHandler(thing);
        } else if (THING_TYPE_ELECTROLUX_WASHING_MACHINE.equals(thingTypeUID)) {
            return new ElectroluxWashingMachineHandler(thing);
        } else if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            return new ElectroluxApplianceBridgeHandler((Bridge) thing, httpClient, gson);
        }
        return null;
    }
}
