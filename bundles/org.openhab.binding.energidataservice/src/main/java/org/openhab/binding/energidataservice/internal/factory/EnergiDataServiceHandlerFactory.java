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
package org.openhab.binding.energidataservice.internal.factory;

import static org.openhab.binding.energidataservice.internal.EnergiDataServiceBindingConstants.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.energidataservice.internal.api.filter.DatahubTariffFilterFactory;
import org.openhab.binding.energidataservice.internal.handler.EnergiDataServiceHandler;
import org.openhab.binding.energidataservice.internal.provider.Co2EmissionProvider;
import org.openhab.binding.energidataservice.internal.provider.ElectricityPriceProvider;
import org.openhab.core.config.core.ConfigParser;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link EnergiDataServiceHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.energidataservice", service = ThingHandlerFactory.class)
public class EnergiDataServiceHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_SERVICE);
    private static final String DAY_AHEAD_TRANSITION_DATE_CONFIG = "dayAheadTransitionDate";

    private final HttpClient httpClient;
    private final TimeZoneProvider timeZoneProvider;
    private final ElectricityPriceProvider electricityPriceProvider;
    private final Co2EmissionProvider co2EmissionProvider;
    private final DatahubTariffFilterFactory datahubTariffFilterFactory = new DatahubTariffFilterFactory();

    @Activate
    public EnergiDataServiceHandlerFactory(final @Reference HttpClientFactory httpClientFactory,
            final @Reference TimeZoneProvider timeZoneProvider,
            final @Reference ElectricityPriceProvider electricityPriceProvider,
            final @Reference Co2EmissionProvider co2EmissionProvider, Map<String, Object> config) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.timeZoneProvider = timeZoneProvider;
        this.electricityPriceProvider = electricityPriceProvider;
        this.co2EmissionProvider = co2EmissionProvider;

        configChanged(config);
    }

    @Modified
    public void configChanged(Map<String, Object> config) {
        String dayAheadDateValue = ConfigParser.valueAs(config.get(DAY_AHEAD_TRANSITION_DATE_CONFIG), String.class);
        LocalDate dayAheadDate = dayAheadDateValue != null ? LocalDate.parse(dayAheadDateValue)
                : DAY_AHEAD_TRANSITION_DATE;
        electricityPriceProvider.setDayAheadTransitionDate(dayAheadDate);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_SERVICE.equals(thingTypeUID)) {
            return new EnergiDataServiceHandler(thing, httpClient, timeZoneProvider, electricityPriceProvider,
                    co2EmissionProvider, datahubTariffFilterFactory);
        }

        return null;
    }
}
