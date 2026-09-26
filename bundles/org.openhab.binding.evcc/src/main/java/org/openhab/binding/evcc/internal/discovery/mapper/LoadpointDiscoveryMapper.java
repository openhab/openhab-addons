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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.evcc.internal.EvccBindingConstants;
import org.openhab.binding.evcc.internal.discovery.Utils;
import org.openhab.binding.evcc.internal.handler.EvccBridgeHandler;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The {@link LoadpointDiscoveryMapper} is responsible for mapping the discovered loadpoints to discovery results
 *
 * @author Marcel Goerentz - Initial contribution
 */
@Component(service = EvccDiscoveryMapper.class)
@NonNullByDefault
public class LoadpointDiscoveryMapper implements EvccDiscoveryMapper {

    private final Logger logger = LoggerFactory.getLogger(LoadpointDiscoveryMapper.class);

    @Override
    public Collection<DiscoveryResult> discover(JsonObject state, EvccBridgeHandler bridgeHandler) {
        List<DiscoveryResult> results = new ArrayList<>();
        JsonArray loadpoints = state.getAsJsonArray(JSON_KEY_LOADPOINTS);
        if (loadpoints == null) {
            return results;
        }
        for (int i = 0; i < loadpoints.size(); i++) {
            JsonObject lp = loadpoints.get(i).getAsJsonObject();
            // evcc requires every loadpoint to be configured with a title, so a missing or blank
            // title indicates the loadpoint's state has not been fully received yet. Skip it rather
            // than fabricating a placeholder name that would not match the real device.
            if (!lp.has(JSON_KEY_TITLE) || lp.get(JSON_KEY_TITLE).isJsonNull()
                    || lp.get(JSON_KEY_TITLE).getAsString().isBlank()) {
                logger.debug("Skipping discovery of loadpoint at index {} because it has no title", i);
                continue;
            }
            boolean heating = lp.has(JSON_KEY_CHARGER_FEATURE_HEATING)
                    && lp.get(JSON_KEY_CHARGER_FEATURE_HEATING).getAsBoolean();
            String title = lp.get(JSON_KEY_TITLE).getAsString();

            ThingUID uid = new ThingUID("DUMMY:DUMMY:DUMMY");
            Map<String, Object> properties = new HashMap<>();
            properties.put(PROPERTY_INDEX, i);
            properties.put(PROPERTY_TITLE, title);

            if (heating) {
                uid = new ThingUID(EvccBindingConstants.THING_TYPE_HEATING, bridgeHandler.getThing().getUID(),
                        Utils.sanitizeName(title));
            } else {
                uid = new ThingUID(EvccBindingConstants.THING_TYPE_LOADPOINT, bridgeHandler.getThing().getUID(),
                        Utils.sanitizeName(title));
            }

            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withLabel(title)
                    .withBridge(bridgeHandler.getThing().getUID()).withProperties(properties)
                    .withRepresentationProperty(PROPERTY_TITLE).build();

            results.add(result);
        }

        return results;
    }
}
