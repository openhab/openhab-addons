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
package org.openhab.binding.deconz.internal;

import static org.openhab.binding.deconz.internal.BindingConstants.*;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.eclipse.smarthome.io.net.http.WebSocketFactory;
import org.openhab.binding.deconz.internal.handler.DeconzBridgeHandler;
import org.openhab.binding.deconz.internal.handler.SensorThingHandler;
import org.openhab.binding.deconz.internal.netutils.AsyncHttpClient;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.gson.Gson;

/**
 * The {@link HandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author David Graeff - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.deconz")
@NonNullByDefault
public class HandlerFactory extends BaseThingHandlerFactory {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(BRIDGE_TYPE, THING_TYPE_PRESENCE_SENSOR, THING_TYPE_DAYLIGHT_SENSOR,
                    THING_TYPE_POWER_SENSOR, THING_TYPE_CONSUMPTION_SENSOR, THING_TYPE_LIGHT_SENSOR,
                    THING_TYPE_TEMPERATURE_SENSOR, THING_TYPE_HUMIDITY_SENSOR, THING_TYPE_PRESSURE_SENSOR,
                    THING_TYPE_SWITCH, THING_TYPE_OPENCLOSE_SENSOR, THING_TYPE_WATERLEAKAGE_SENSOR,
                    THING_TYPE_ALARM_SENSOR, THING_TYPE_VIBRATION_SENSOR).collect(Collectors.toSet()));

    private final Gson gson = new Gson();
    private final WebSocketFactory webSocketFactory;
    private final HttpClientFactory httpClientFactory;

    @Activate
    public HandlerFactory(final @Reference WebSocketFactory webSocketFactory,
            final @Reference HttpClientFactory httpClientFactory) {
        this.webSocketFactory = webSocketFactory;
        this.httpClientFactory = httpClientFactory;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (BRIDGE_TYPE.equals(thingTypeUID)) {
            return new DeconzBridgeHandler((Bridge) thing, webSocketFactory,
                    new AsyncHttpClient(httpClientFactory.getCommonHttpClient()), gson);
        } else {
            return new SensorThingHandler(thing, gson);
        }
    }
}
