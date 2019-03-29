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
package org.openhab.binding.internal.kostal.inverter;

import static org.openhab.binding.internal.kostal.inverter.thirdgeneration.ThirdGenerationBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.openhab.binding.internal.kostal.inverter.firstgeneration.WebscrapeHandler;
import org.openhab.binding.internal.kostal.inverter.thirdgeneration.ThirdGenerationHandler;
import org.openhab.binding.internal.kostal.inverter.thirdgeneration.ThirdGenerationInverterTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Christian Schneider - Initial contribution (as WebscrapeHandlerFactory.java)
 * @author René Stakemeier - extension for the third generation of KOSTAL inverters
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.kostalinverter")
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

    private HttpClient httpClient;

    public static final ThingTypeUID FIRST_GENERATION_INVERTER = new ThingTypeUID("kostalinverter", "kostalinverter");

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return thingTypeUID.equals(FIRST_GENERATION_INVERTER)
                || SUPPORTED_THIRD_GENERATION_THING_TYPES_UIDS.keySet().contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        // first generation
        if (FIRST_GENERATION_INVERTER.equals(thing.getThingTypeUID())) {
            return new WebscrapeHandler(thing);
        }
        // third generation
        if (SUPPORTED_THIRD_GENERATION_THING_TYPES_UIDS.containsKey(thing.getThingTypeUID())) {
            return new ThirdGenerationHandler(thing, httpClient,
                    SUPPORTED_THIRD_GENERATION_THING_TYPES_UIDS.get(thing.getThingTypeUID()));
        }
        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
    }

    @Reference
    protected void setHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    protected void unsetHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClient = null;
    }

}
