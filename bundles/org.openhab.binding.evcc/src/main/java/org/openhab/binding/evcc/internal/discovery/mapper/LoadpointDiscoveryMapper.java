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
package org.openhab.binding.evcc.internal.discovery.mapper;

import static org.openhab.binding.evcc.internal.EvccBindingConstants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.evcc.internal.EvccBindingConstants;
import org.openhab.binding.evcc.internal.discovery.Utils;
import org.openhab.binding.evcc.internal.handler.EvccBridgeHandler;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The {@link LoadpointDiscoveryMapper} is responsible for mapping the discovered loadpoints to discovery results
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class LoadpointDiscoveryMapper implements EvccDiscoveryMapper {

    @Override
    public Collection<DiscoveryResult> discover(JsonObject state, EvccBridgeHandler bridgeHandler) {
        List<DiscoveryResult> results = new ArrayList<>();
        JsonArray loadpoints = state.getAsJsonArray(JSON_MEMBER_LOADPOINTS);
        if (loadpoints == null) {
            return results;
        }
        for (int i = 0; i < loadpoints.size(); i++) {
            JsonObject lp = loadpoints.get(i).getAsJsonObject();
            String title = lp.has("title") ? lp.get("title").getAsString().toLowerCase(Locale.ROOT) : "loadpoint" + i;

            ThingUID uid = new ThingUID("DUMMY:DUMMY:DUMMY");
            Map<String, Object> properties = new HashMap<>();
            properties.put(PROPERTY_INDEX, i);
            properties.put(PROPERTY_TITLE, title);

            if (lp.has(JSON_MEMBER_CHARGER_FEATURE_HEATING)
                    && lp.get(JSON_MEMBER_CHARGER_FEATURE_HEATING).getAsBoolean()) {
                uid = new ThingUID(EvccBindingConstants.THING_TYPE_HEATING, bridgeHandler.getThing().getUID(),
                        Utils.sanitizeName(title));
                properties.put(PROPERTY_TYPE, PROPERTY_TYPE_HEATING);
            } else {
                uid = new ThingUID(EvccBindingConstants.THING_TYPE_LOADPOINT, bridgeHandler.getThing().getUID(),
                        Utils.sanitizeName(title));
                properties.put(PROPERTY_TYPE, PROPERTY_TYPE_LOADPOINT);
            }

            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withLabel(title)
                    .withBridge(bridgeHandler.getThing().getUID()).withProperties(properties)
                    .withRepresentationProperty(PROPERTY_TITLE).build();

            results.add(result);
        }

        return results;
    }
}
