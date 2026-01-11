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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
import org.openhab.core.util.HexUtils;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final Logger logger = LoggerFactory.getLogger(ForecastDiscoveryMapper.class);

    @Override
    public Collection<DiscoveryResult> discover(JsonObject state, EvccBridgeHandler bridgeHandler) {
        List<DiscoveryResult> results = new ArrayList<>();
        JsonObject forecasts = state.getAsJsonObject(JSON_KEY_FORECAST);
        if (forecasts == null) {
            return results;
        }
        for (Map.Entry<String, JsonElement> entry : forecasts.entrySet()) {
            String forecastType = entry.getKey();
            ThingUID uid = new ThingUID(THING_TYPE_FORECAST, bridgeHandler.getThing().getUID(),
                    Utils.sanitizeName(forecastType));
            String label = "Forecast " + capitalizeFirstLetter(forecastType);
            String id = "";
            try {
                id = createIdString(label);
            } catch (NoSuchAlgorithmException e) {
                // should not happen
                logger.warn("Could not get hash algorithm instance");
            }
            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withLabel(label)
                    .withBridge(bridgeHandler.getThing().getUID()).withProperty(PROPERTY_TYPE, forecastType)
                    .withProperty(PROPERTY_ID, id).withRepresentationProperty(PROPERTY_ID).build();
            results.add(result);
        }
        return results;
    }

    private String createIdString(String label) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest((label).getBytes(StandardCharsets.UTF_8));
        // Use first 10 hex chars of the SHA to generate a stable, compact plan ID
        return HexUtils.bytesToHex(digest).substring(0, 10);
    }
}
