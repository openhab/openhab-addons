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
package org.openhab.binding.wlanthermo.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.wlanthermo.internal.api.esp32.WlanThermoEsp32Handler;
import org.openhab.binding.wlanthermo.internal.api.mini.WlanThermoMiniHandler;
import org.openhab.binding.wlanthermo.internal.api.nano.WlanThermoNanoV1Handler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link WlanThermoHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Christian Schlipp - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.wlanthermo", service = ThingHandlerFactory.class)
public class WlanThermoHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>(
            Arrays.asList(WlanThermoBindingConstants.THING_TYPE_WLANTHERMO_NANO_V1,
                    WlanThermoBindingConstants.THING_TYPE_WLANTHERMO_MINI,
                    WlanThermoBindingConstants.THING_TYPE_WLANTHERMO_ESP32));
    private final HttpClient httpClient;

    @Activate
    public WlanThermoHandlerFactory(@Reference HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (WlanThermoBindingConstants.THING_TYPE_WLANTHERMO_NANO_V1.equals(thingTypeUID)) {
            return new WlanThermoNanoV1Handler(thing, httpClient);
        } else if (WlanThermoBindingConstants.THING_TYPE_WLANTHERMO_MINI.equals(thingTypeUID)) {
            return new WlanThermoMiniHandler(thing, httpClient);
        } else if (WlanThermoBindingConstants.THING_TYPE_WLANTHERMO_ESP32.equals(thingTypeUID)) {
            return new WlanThermoEsp32Handler(thing, httpClient);
        }

        return null;
    }
}
