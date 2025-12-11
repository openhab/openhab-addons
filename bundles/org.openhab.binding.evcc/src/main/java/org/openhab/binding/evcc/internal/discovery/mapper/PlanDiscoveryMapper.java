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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.evcc.internal.EvccBindingConstants;
import org.openhab.binding.evcc.internal.discovery.Utils;
import org.openhab.binding.evcc.internal.handler.EvccBridgeHandler;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.util.HexUtils;

import com.google.gson.JsonObject;

/**
 * The {@link PlanDiscoveryMapper} is responsible for mapping the discovered plans from a vehicle to discovery results
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class PlanDiscoveryMapper {

    public Collection<DiscoveryResult> discoverFromVehicle(JsonObject vehicle, String id, String title,
            EvccBridgeHandler bridgeHandler) throws NoSuchAlgorithmException {
        List<DiscoveryResult> results = new ArrayList<>();
        JsonObject plan = vehicle.getAsJsonObject(JSON_KEY_PLAN);

        if (plan != null) {
            String label = "Active Plan for " + title;
            results.add(createPlanDiscoveryResult(label, createIdString(id, 0), 0, id, bridgeHandler));
        }
        for (int index = 1; index <= vehicle.get(JSON_KEY_REPEATING_PLANS).getAsJsonArray().size(); index++) {
            String label = "Repeating Plan " + index + " for " + title;
            results.add(createPlanDiscoveryResult(label, createIdString(id, index), index, id, bridgeHandler));
        }
        return results;
    }

    private DiscoveryResult createPlanDiscoveryResult(String label, String planID, int index, String vehicleID,
            EvccBridgeHandler bridgeHandler) {
        ThingUID uid = new ThingUID(EvccBindingConstants.THING_TYPE_PLAN, bridgeHandler.getThing().getUID(),
                Utils.sanitizeName(planID));
        return DiscoveryResultBuilder.create(uid).withLabel(label).withBridge(bridgeHandler.getThing().getUID())
                .withProperty(PROPERTY_ID, planID).withProperty(PROPERTY_VEHICLE_ID, vehicleID)
                .withProperty(PROPERTY_INDEX, String.valueOf(index)).withRepresentationProperty(PROPERTY_ID).build();
    }

    private String createIdString(String id, int index) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest((id + "Plan" + index).getBytes(StandardCharsets.UTF_8));
        return HexUtils.bytesToHex(digest);
    }
}
