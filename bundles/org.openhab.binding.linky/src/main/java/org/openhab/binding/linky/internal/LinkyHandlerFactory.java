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
package org.openhab.binding.linky.internal;

import static java.time.temporal.ChronoField.*;
import static org.openhab.binding.linky.internal.LinkyBindingConstants.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.linky.internal.handler.D2LBridgeHandler;
import org.openhab.binding.linky.internal.handler.EnedisBridgeHandler;
import org.openhab.binding.linky.internal.handler.EnedisWebBridgeHandler;
import org.openhab.binding.linky.internal.handler.LinkyHandler;
import org.openhab.binding.linky.internal.handler.LinkyHandlerDirect;
import org.openhab.binding.linky.internal.handler.MyElectricalDataBridgeHandler;
import org.openhab.binding.linky.internal.utils.DoubleTypeAdapter;
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

/**
 * The {@link LinkyHandlerFactory} is responsible for creating things handlers.
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Laurent Arnal - Rewrite addon to use official dataconect API
 */
@NonNullByDefault
@Component(immediate = true, service = ThingHandlerFactory.class, configurationPid = "binding.linky")
public class LinkyHandlerFactory extends BaseThingHandlerFactory {
    private static final DateTimeFormatter LINKY_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSX");
    private static final DateTimeFormatter LINKY_LOCALDATE_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd");
    private static final DateTimeFormatter LINKY_LOCALDATETIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("uuuu-MM-dd['T'][' ']HH:mm").optionalStart().appendLiteral(':')
            .appendValue(SECOND_OF_MINUTE, 2).optionalStart().appendFraction(NANO_OF_SECOND, 0, 9, true).toFormatter();

    /*
     * ;
     *
     * DateTimeFormatter formatter1 = new DateTimeFormatterBuilder()
     * .appendPattern(DATE_TIME_FORMAT_PATTERN)
     * // optional decimal point followed by 1 to 6 digits
     * .optionalStart()
     * .appendPattern(".")
     * .appendFraction(ChronoField.MICRO_OF_SECOND, 1, 6, false)
     * .optionalEnd()
     * .toFormatter();
     */

    private final HttpClientFactory httpClientFactory;
    private final OAuthFactory oAuthFactory;
    private final HttpService httpService;
    private final ThingRegistry thingRegistry;
    private final ComponentContext componentContext;
    private final TimeZoneProvider timeZoneProvider;

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(ZonedDateTime.class,
                    (JsonDeserializer<ZonedDateTime>) (json, type, jsonDeserializationContext) -> ZonedDateTime
                            .parse(json.getAsJsonPrimitive().getAsString(), LINKY_FORMATTER))
            .registerTypeAdapter(LocalDate.class,
                    (JsonDeserializer<LocalDate>) (json, type, jsonDeserializationContext) -> LocalDate
                            .parse(json.getAsJsonPrimitive().getAsString(), LINKY_LOCALDATE_FORMATTER))
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonDeserializer<LocalDateTime>) (json, type, jsonDeserializationContext) -> {
                        try {
                            return LocalDateTime.parse(json.getAsJsonPrimitive().getAsString(),
                                    LINKY_LOCALDATETIME_FORMATTER);
                        } catch (Exception ex) {
                            return LocalDate.parse(json.getAsJsonPrimitive().getAsString(), LINKY_LOCALDATE_FORMATTER)
                                    .atStartOfDay();
                        }
                    })
            .registerTypeAdapter(Double.class, new DoubleTypeAdapter()).serializeNulls().create();

    private final LocaleProvider localeProvider;

    @Activate
    public LinkyHandlerFactory(final @Reference LocaleProvider localeProvider,
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
        if (THING_TYPE_API_ENEDIS_BRIDGE.equals(thing.getThingTypeUID())) {
            EnedisBridgeHandler handler = new EnedisBridgeHandler((Bridge) thing, this.httpClientFactory,
                    this.oAuthFactory, this.httpService, thingRegistry, componentContext, gson);
            return handler;
        } else if (THING_TYPE_API_WEB_ENEDIS_BRIDGE.equals(thing.getThingTypeUID())) {
            EnedisWebBridgeHandler handler = new EnedisWebBridgeHandler((Bridge) thing, this.httpClientFactory,
                    this.oAuthFactory, this.httpService, thingRegistry, componentContext, gson);
            return handler;
        } else if (THING_TYPE_API_MYELECTRICALDATA_BRIDGE.equals(thing.getThingTypeUID())) {
            MyElectricalDataBridgeHandler handler = new MyElectricalDataBridgeHandler((Bridge) thing,
                    this.httpClientFactory, this.oAuthFactory, this.httpService, thingRegistry, componentContext, gson);
            return handler;
        } else if (THING_TYPE_D2L__BRIDGE.equals(thing.getThingTypeUID())) {
            D2LBridgeHandler handler = new D2LBridgeHandler((Bridge) thing, this.httpClientFactory, this.oAuthFactory,
                    this.httpService, thingRegistry, componentContext, gson);
            return handler;
        } else if (THING_TYPE_LINKY.equals(thing.getThingTypeUID())) {
            LinkyHandler handler = new LinkyHandler(thing, localeProvider, timeZoneProvider);
            return handler;
        } else if (THING_TYPE_LINKY_DIRECT.equals(thing.getThingTypeUID())) {
            LinkyHandlerDirect handler = new LinkyHandlerDirect(thing, localeProvider, timeZoneProvider);
            return handler;
        }

        return null;
    }
}
