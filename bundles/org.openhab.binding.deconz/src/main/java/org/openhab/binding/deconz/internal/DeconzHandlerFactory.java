/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import org.openhab.binding.deconz.internal.handler.LightThingHandler;
import org.openhab.binding.deconz.internal.handler.SensorThermostatThingHandler;
import org.openhab.binding.deconz.internal.handler.SensorThingHandler;
import org.openhab.binding.deconz.internal.netutils.AsyncHttpClient;
import org.openhab.binding.deconz.internal.types.LightType;
import org.openhab.binding.deconz.internal.types.LightTypeDeserializer;
import org.openhab.binding.deconz.internal.types.ThermostatMode;
import org.openhab.binding.deconz.internal.types.ThermostatModeGsonTypeAdapter;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link DeconzHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author David Graeff - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.deconz")
@NonNullByDefault
public class DeconzHandlerFactory extends BaseThingHandlerFactory {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(DeconzBridgeHandler.SUPPORTED_THING_TYPES, LightThingHandler.SUPPORTED_THING_TYPE_UIDS,
                    SensorThingHandler.SUPPORTED_THING_TYPES, SensorThermostatThingHandler.SUPPORTED_THING_TYPES)
            .flatMap(Set::stream).collect(Collectors.toSet());

    private final Gson gson;
    private final WebSocketFactory webSocketFactory;
    private final HttpClientFactory httpClientFactory;
    private final StateDescriptionProvider stateDescriptionProvider;

    @Activate
    public DeconzHandlerFactory(final @Reference WebSocketFactory webSocketFactory,
            final @Reference HttpClientFactory httpClientFactory,
            final @Reference StateDescriptionProvider stateDescriptionProvider) {
        this.webSocketFactory = webSocketFactory;
        this.httpClientFactory = httpClientFactory;
        this.stateDescriptionProvider = stateDescriptionProvider;

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LightType.class, new LightTypeDeserializer());
        gsonBuilder.registerTypeAdapter(ThermostatMode.class, new ThermostatModeGsonTypeAdapter());
        gson = gsonBuilder.create();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (DeconzBridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new DeconzBridgeHandler((Bridge) thing, webSocketFactory,
                    new AsyncHttpClient(httpClientFactory.getCommonHttpClient()), gson);
        } else if (LightThingHandler.SUPPORTED_THING_TYPE_UIDS.contains(thingTypeUID)) {
            return new LightThingHandler(thing, gson, stateDescriptionProvider);
        } else if (SensorThingHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new SensorThingHandler(thing, gson);
        } else if (SensorThermostatThingHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new SensorThermostatThingHandler(thing, gson);
        }

        return null;
    }
}
