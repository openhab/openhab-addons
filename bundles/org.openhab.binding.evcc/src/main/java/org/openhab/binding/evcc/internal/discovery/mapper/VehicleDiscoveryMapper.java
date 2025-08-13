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

import static org.openhab.binding.evcc.internal.EvccBindingConstants.JSON_MEMBER_VEHICLES;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.PROPERTY_ID;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.PROPERTY_TYPE;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.PROPERTY_TYPE_VEHICLE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.evcc.internal.EvccBindingConstants;
import org.openhab.binding.evcc.internal.discovery.Utils;
import org.openhab.binding.evcc.internal.handler.EvccBridgeHandler;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link VehicleDiscoveryMapper} is responsible for mapping the discovered vehicles to discovery results
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class VehicleDiscoveryMapper implements EvccDiscoveryMapper {

    @Override
    public Collection<DiscoveryResult> discover(JsonObject state, EvccBridgeHandler bridgeHandler) {
        List<DiscoveryResult> results = new ArrayList<>();
        JsonObject vehicles = state.getAsJsonObject(JSON_MEMBER_VEHICLES);
        if (vehicles == null) {
            return results;
        }
        for (Map.Entry<String, JsonElement> entry : vehicles.entrySet()) {
            JsonObject v = entry.getValue().getAsJsonObject();
            String id = entry.getKey();
            String title = v.has("title") ? v.get("title").getAsString() : id;

            ThingUID uid = new ThingUID(EvccBindingConstants.THING_TYPE_VEHICLE, bridgeHandler.getThing().getUID(),
                    Utils.sanitizeName(title));
            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withLabel(title)
                    .withBridge(bridgeHandler.getThing().getUID()).withProperty(PROPERTY_TYPE, PROPERTY_TYPE_VEHICLE)
                    .withProperty(PROPERTY_ID, id).withRepresentationProperty(PROPERTY_ID).build();

            results.add(result);
        }

        return results;
    }
}
