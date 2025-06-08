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
package org.openhab.binding.sedif.internal.factory;

import static org.openhab.binding.sedif.internal.constants.SedifBindingConstants.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sedif.internal.dto.ContractDetail;
import org.openhab.binding.sedif.internal.dto.Contracts;
import org.openhab.binding.sedif.internal.dto.MeterReading;
import org.openhab.binding.sedif.internal.dto.RuntimeTypeAdapterFactory;
import org.openhab.binding.sedif.internal.dto.Value;
import org.openhab.binding.sedif.internal.handler.BridgeSedifWebHandler;
import org.openhab.binding.sedif.internal.handler.ThingSedifHandler;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

/**
 * The {@link SedifHandlerFactory} is responsible for creating things handlers.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
@Component(immediate = true, service = ThingHandlerFactory.class, configurationPid = "binding.sedif")
public class SedifHandlerFactory extends BaseThingHandlerFactory {
    private final HttpClientFactory httpClientFactory;
    private final OAuthFactory oAuthFactory;
    private final HttpService httpService;
    private final ThingRegistry thingRegistry;
    private final ComponentContext componentContext;
    private final TimeZoneProvider timeZoneProvider;

    public static final DateTimeFormatter SEDIF_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSX");
    public static final DateTimeFormatter SEDIF_LOCALDATE_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd");
    public static final DateTimeFormatter SEDIF_LOCALDATETIME_FORMATTER = DateTimeFormatter
            .ofPattern("uuuu-MM-dd' 'HH:mm:ss");
    // new DateTimeFormatterBuilder()
    // .appendPattern("\"uuuu-MM-dd[' ']HH:mm:ss\"").toFormatter();

    private final Gson gson;

    private final LocaleProvider localeProvider;

    @Activate
    public SedifHandlerFactory(final @Reference LocaleProvider localeProvider,
            final @Reference HttpClientFactory httpClientFactory, final @Reference OAuthFactory oAuthFactory,
            final @Reference HttpService httpService, final @Reference ThingRegistry thingRegistry,
            ComponentContext componentContext, final @Reference TimeZoneProvider timeZoneProvider) {
        this.localeProvider = localeProvider;
        this.timeZoneProvider = timeZoneProvider;
        this.httpClientFactory = httpClientFactory;
        this.oAuthFactory = oAuthFactory;
        this.httpService = httpService;
        this.thingRegistry = thingRegistry;
        this.componentContext = componentContext;

        RuntimeTypeAdapterFactory<Value> adapter = RuntimeTypeAdapterFactory.of(Value.class);
        adapter.registerSubtype(Contracts.class, "contrats", "Contracts");
        adapter.registerSubtype(ContractDetail.class, "compteInfo", "ContractDetail");
        adapter.registerSubtype(MeterReading.class, "data", "Datas");

        gson = new GsonBuilder().registerTypeAdapterFactory(adapter).setDateFormat("yyyy-MM-dd")
                // LocalDate
                .registerTypeAdapter(LocalDate.class,
                        (JsonSerializer<LocalDate>) (src, typeOfSrc,
                                context) -> new JsonPrimitive(src.format(SEDIF_LOCALDATE_FORMATTER)))
                .registerTypeAdapter(LocalDate.class,
                        (JsonDeserializer<LocalDate>) (json, type, jsonDeserializationContext) -> LocalDate
                                .parse(json.getAsJsonPrimitive().getAsString(), SEDIF_LOCALDATE_FORMATTER))

                // LocalDateTime
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonDeserializer<LocalDateTime>) (json, type, jsonDeserializationContext) -> LocalDateTime
                                .parse(json.getAsJsonPrimitive().getAsString(), SEDIF_LOCALDATETIME_FORMATTER))
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonSerializer<LocalDateTime>) (src, typeOfSrc,
                                context) -> new JsonPrimitive(src.format(SEDIF_LOCALDATETIME_FORMATTER)))

                .setPrettyPrinting().create();
    }

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_DEVICE_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (THING_TYPE_WEB_SEDIF_BRIDGE.equals(thing.getThingTypeUID())) {
            BridgeSedifWebHandler handler = new BridgeSedifWebHandler((Bridge) thing, this.httpClientFactory,
                    this.oAuthFactory, this.httpService, thingRegistry, componentContext, gson);
            return handler;
        } else if (THING_TYPE_SEDIF.equals(thing.getThingTypeUID())) {
            ThingSedifHandler handler = new ThingSedifHandler(thing, localeProvider, timeZoneProvider, gson);
            return handler;
        }
        return null;
    }
}
