/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.evcc.internal.discovery.mapper;

import static org.openhab.binding.evcc.internal.EvccBindingConstants.*;
import static org.openhab.binding.evcc.internal.handler.Utils.capitalizeFirstLetter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.evcc.internal.discovery.Utils;
import org.openhab.binding.evcc.internal.handler.EvccBridgeHandler;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link ForecastDiscoveryMapper} is responsible for mapping the discovered forecast objects
 *
 * @author Marcel Goerentz - Initial contribution
 */
@Component(service = EvccDiscoveryMapper.class)
@NonNullByDefault
public class ForecastDiscoveryMapper implements EvccDiscoveryMapper {

    @Override
    public Collection<DiscoveryResult> discover(JsonObject state, EvccBridgeHandler bridgeHandler) {
        List<DiscoveryResult> results = new ArrayList<>();
        JsonObject forecasts = state.getAsJsonObject(JSON_KEY_FORECAST);
        if (forecasts == null) {
            return results;
        }
        for (Map.Entry<String, JsonElement> entry : forecasts.entrySet()) {
            String forecastType = entry.getKey();
            if (!SUPPORTED_FORECAST_TYPES.contains(forecastType)) {
                continue;
            }
            ThingUID uid = new ThingUID(THING_TYPE_FORECAST, bridgeHandler.getThing().getUID(),
                    Utils.sanitizeName(forecastType));
            String label = "Forecast " + capitalizeFirstLetter(forecastType);
            String id = Utils.createIdString(List.of(label));
            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withLabel(label)
                    .withBridge(bridgeHandler.getThing().getUID()).withProperty(PROPERTY_TYPE, PROPERTY_FORECAST)
                    .withProperty(PROPERTY_SUBTYPE, forecastType).withProperty(PROPERTY_ID, id)
                    .withRepresentationProperty(PROPERTY_ID).build();
            results.add(result);
        }
        return results;
    }
}
