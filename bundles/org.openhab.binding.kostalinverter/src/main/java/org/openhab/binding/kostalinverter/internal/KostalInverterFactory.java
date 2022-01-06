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
package org.openhab.binding.kostalinverter.internal;

import static org.openhab.binding.kostalinverter.internal.thirdgeneration.ThirdGenerationBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.kostalinverter.internal.firstgeneration.WebscrapeHandler;
import org.openhab.binding.kostalinverter.internal.secondgeneration.SecondGenerationHandler;
import org.openhab.binding.kostalinverter.internal.thirdgeneration.ThirdGenerationHandler;
import org.openhab.binding.kostalinverter.internal.thirdgeneration.ThirdGenerationInverterTypes;
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
 * @author Christian Schneider - Initial contribution (as WebscrapeHandlerFactory.java)
 * @author René Stakemeier - extension for the third generation of KOSTAL inverters
 * @author Örjan Backsell - extension for the second generation of KOSTAL inverters
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.kostalinverter")
@NonNullByDefault
public class KostalInverterFactory extends BaseThingHandlerFactory {

    private static final Map<ThingTypeUID, ThirdGenerationInverterTypes> SUPPORTED_THIRD_GENERATION_THING_TYPES_UIDS = new HashMap<>();
    static {
        SUPPORTED_THIRD_GENERATION_THING_TYPES_UIDS.put(PIKOIQ42, ThirdGenerationInverterTypes.PIKOIQ_42);
        SUPPORTED_THIRD_GENERATION_THING_TYPES_UIDS.put(PIKOIQ55, ThirdGenerationInverterTypes.PIKOIQ_55);
        SUPPORTED_THIRD_GENERATION_THING_TYPES_UIDS.put(PIKOIQ70, ThirdGenerationInverterTypes.PIKOIQ_70);
        SUPPORTED_THIRD_GENERATION_THING_TYPES_UIDS.put(PIKOIQ85, ThirdGenerationInverterTypes.PIKOIQ_85);
        SUPPORTED_THIRD_GENERATION_THING_TYPES_UIDS.put(PIKOIQ100, ThirdGenerationInverterTypes.PIKOIQ_100);
        SUPPORTED_THIRD_GENERATION_THING_TYPES_UIDS.put(PLENTICOREPLUS42WITHBATTERY,
                ThirdGenerationInverterTypes.PLENTICORE_PLUS_42_WITH_BATTERY);
        SUPPORTED_THIRD_GENERATION_THING_TYPES_UIDS.put(PLENTICOREPLUS55WITHBATTERY,
                ThirdGenerationInverterTypes.PLENTICORE_PLUS_55_WITH_BATTERY);
        SUPPORTED_THIRD_GENERATION_THING_TYPES_UIDS.put(PLENTICOREPLUS70WITHBATTERY,
                ThirdGenerationInverterTypes.PLENTICORE_PLUS_70_WITH_BATTERY);
        SUPPORTED_THIRD_GENERATION_THING_TYPES_UIDS.put(PLENTICOREPLUS85WITHBATTERY,
                ThirdGenerationInverterTypes.PLENTICORE_PLUS_85_WITH_BATTERY);
        SUPPORTED_THIRD_GENERATION_THING_TYPES_UIDS.put(PLENTICOREPLUS100WITHBATTERY,
                ThirdGenerationInverterTypes.PLENTICORE_PLUS_100_WITH_BATTERY);
        SUPPORTED_THIRD_GENERATION_THING_TYPES_UIDS.put(PLENTICOREPLUS42WITHOUTBATTERY,
                ThirdGenerationInverterTypes.PLENTICORE_PLUS_42_WITHOUT_BATTERY);
        SUPPORTED_THIRD_GENERATION_THING_TYPES_UIDS.put(PLENTICOREPLUS55WITHOUTBATTERY,
                ThirdGenerationInverterTypes.PLENTICORE_PLUS_55_WITHOUT_BATTERY);
        SUPPORTED_THIRD_GENERATION_THING_TYPES_UIDS.put(PLENTICOREPLUS70WITHOUTBATTERY,
                ThirdGenerationInverterTypes.PLENTICORE_PLUS_70_WITHOUT_BATTERY);
        SUPPORTED_THIRD_GENERATION_THING_TYPES_UIDS.put(PLENTICOREPLUS85WITHOUTBATTERY,
                ThirdGenerationInverterTypes.PLENTICORE_PLUS_85_WITHOUT_BATTERY);
        SUPPORTED_THIRD_GENERATION_THING_TYPES_UIDS.put(PLENTICOREPLUS100WITHOUTBATTERY,
                ThirdGenerationInverterTypes.PLENTICORE_PLUS_100_WITHOUT_BATTERY);
    }

    public static final ThingTypeUID FIRST_GENERATION_INVERTER = new ThingTypeUID("kostalinverter", "kostalinverter");

    public static final ThingTypeUID SECOND_GENERATION_INVERTER = new ThingTypeUID("kostalinverter", "piko1020");

    private final HttpClient httpClient;

    @Activate
    public KostalInverterFactory(@Reference final HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return thingTypeUID.equals(FIRST_GENERATION_INVERTER) || thingTypeUID.equals(SECOND_GENERATION_INVERTER)
                || SUPPORTED_THIRD_GENERATION_THING_TYPES_UIDS.keySet().contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        // first generation
        if (FIRST_GENERATION_INVERTER.equals(thing.getThingTypeUID())) {
            return new WebscrapeHandler(thing);
        }
        // second generation
        if (SECOND_GENERATION_INVERTER.equals(thing.getThingTypeUID())) {
            return new SecondGenerationHandler(thing, httpClient);
        }
        // third generation
        ThirdGenerationInverterTypes inverterType = SUPPORTED_THIRD_GENERATION_THING_TYPES_UIDS
                .get(thing.getThingTypeUID());
        if (inverterType != null) {
            return new ThirdGenerationHandler(thing, httpClient, inverterType);
        }
        return null;
    }
}
