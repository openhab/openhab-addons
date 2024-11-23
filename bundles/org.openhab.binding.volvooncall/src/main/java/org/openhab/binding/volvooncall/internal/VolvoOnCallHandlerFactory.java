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
package org.openhab.binding.volvooncall.internal;

import static org.openhab.binding.volvooncall.internal.VolvoOnCallBindingConstants.*;

import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.volvooncall.internal.handler.VehicleHandler;
import org.openhab.binding.volvooncall.internal.handler.VehicleStateDescriptionProvider;
import org.openhab.binding.volvooncall.internal.handler.VolvoOnCallBridgeHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
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
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

/**
 * The {@link VolvoOnCallHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.volvooncall", service = ThingHandlerFactory.class)
public class VolvoOnCallHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(VolvoOnCallHandlerFactory.class);
    private final VehicleStateDescriptionProvider stateDescriptionProvider;
    private final Gson gson;
    private final HttpClientFactory httpClientFactory;

    @Activate
    public VolvoOnCallHandlerFactory(@Reference VehicleStateDescriptionProvider provider,
            @Reference HttpClientFactory httpClientFactory) {
        this.stateDescriptionProvider = provider;
        this.httpClientFactory = httpClientFactory;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(ZonedDateTime.class,
                        (JsonDeserializer<ZonedDateTime>) (json, type, jsonDeserializationContext) -> ZonedDateTime
                                .parse(json.getAsJsonPrimitive().getAsString().replaceAll("\\+0000", "Z")))
                .registerTypeAdapter(OpenClosedType.class,
                        (JsonDeserializer<OpenClosedType>) (json, type,
                                jsonDeserializationContext) -> json.getAsBoolean() ? OpenClosedType.OPEN
                                        : OpenClosedType.CLOSED)
                .registerTypeAdapter(OnOffType.class,
                        (JsonDeserializer<OnOffType>) (json, type, jsonDeserializationContext) -> OnOffType
                                .from(json.getAsBoolean()))
                .registerTypeAdapter(StringType.class, (JsonDeserializer<StringType>) (json, type,
                        jsonDeserializationContext) -> StringType.valueOf(json.getAsString()))
                .create();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (APIBRIDGE_THING_TYPE.equals(thingTypeUID)) {
            return new VolvoOnCallBridgeHandler((Bridge) thing, gson, httpClientFactory);
        } else if (VEHICLE_THING_TYPE.equals(thingTypeUID)) {
            return new VehicleHandler(thing, stateDescriptionProvider);
        }
        logger.warn("ThingHandler not found for {}", thing.getThingTypeUID());
        return null;
    }
}
