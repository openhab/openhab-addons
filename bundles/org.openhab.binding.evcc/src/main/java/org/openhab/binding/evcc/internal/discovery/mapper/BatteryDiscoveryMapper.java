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
import java.util.List;
import java.util.Locale;

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
 * The {@link BatteryDiscoveryMapper} iis responsible for mapping the discovered batteries to discovery results
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class BatteryDiscoveryMapper implements EvccDiscoveryMapper {

    @Override
    public Collection<DiscoveryResult> discover(JsonObject state, EvccBridgeHandler bridgeHandler) {
        List<DiscoveryResult> results = new ArrayList<>();
        JsonArray batteries = state.getAsJsonArray(JSON_MEMBER_BATTERY);
        if (batteries == null) {
            return results;
        }
        for (int i = 0; i < batteries.size(); i++) {
            JsonObject battery = batteries.get(i).getAsJsonObject();
            String title = battery.has("title") ? battery.get("title").getAsString().toLowerCase(Locale.ROOT)
                    : "battery" + i;

            ThingUID uid = new ThingUID(EvccBindingConstants.THING_TYPE_BATTERY, bridgeHandler.getThing().getUID(),
                    Utils.sanitizeName(title));
            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withLabel(title)
                    .withBridge(bridgeHandler.getThing().getUID()).withProperty(PROPERTY_INDEX, i)
                    .withProperty(PROPERTY_TYPE, PROPERTY_TYPE_BATTERY).withProperty(PROPERTY_TITLE, title)
                    .withRepresentationProperty(PROPERTY_TITLE).build();
            results.add(result);
        }
        return results;
    }
}
