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
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.evcc.internal.EvccBindingConstants;
import org.openhab.binding.evcc.internal.handler.EvccBridgeHandler;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.util.HexUtils;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link PlanDiscoveryMapper} is responsible for mapping the discovered plans from a vehicle to discovery results
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
@Component(service = EvccDiscoveryMapper.class)
public class PlanDiscoveryMapper implements EvccDiscoveryMapper {

    private final Logger logger = LoggerFactory.getLogger(PlanDiscoveryMapper.class);
    private final BundleContext ctx;
    private final TranslationProvider tp;
    private final LocaleProvider lp;

    @Activate
    public PlanDiscoveryMapper(BundleContext ctx, @Reference TranslationProvider tp, @Reference LocaleProvider lp) {
        this.ctx = ctx;
        this.tp = tp;
        this.lp = lp;
    }

    @Override
    public Collection<DiscoveryResult> discover(JsonObject state, EvccBridgeHandler bridgeHandler) {
        List<DiscoveryResult> results = new ArrayList<>();
        JsonObject vehicles = state.getAsJsonObject(JSON_KEY_VEHICLES);
        if (vehicles == null) {
            return results;
        }
        for (Map.Entry<String, JsonElement> entry : vehicles.entrySet()) {
            JsonObject v = entry.getValue().getAsJsonObject();
            String id = entry.getKey();
            String title = v.has(JSON_KEY_TITLE) ? v.get(JSON_KEY_TITLE).getAsString() : id;
            if (v.has(JSON_KEY_PLAN) || v.has(JSON_KEY_REPEATING_PLANS)) {
                try {
                    results.addAll(discoverFromVehicle(v, id, title, bridgeHandler));
                } catch (NoSuchAlgorithmException e) {
                    // should not happen
                    logger.warn("Could not get hash algorithm instance");
                }
            }
        }
        return results;
    }

    public Collection<DiscoveryResult> discoverFromVehicle(JsonObject vehicle, String id, String title,
            EvccBridgeHandler bridgeHandler) throws NoSuchAlgorithmException {
        List<DiscoveryResult> results = new ArrayList<>();
        JsonObject plan = vehicle.getAsJsonObject(JSON_KEY_PLAN);

        if (plan != null) {
            String localizedLabel = tp.getText(ctx.getBundle(), "discovery.evcc.plan.one-time.label",
                    "One-time charging plan for {0}", lp.getLocale(), title);
            String label = localizedLabel == null ? "One-time charging plan for " + title : localizedLabel;
            results.add(createPlanDiscoveryResult(label, createIdString(id, 0), 0, id, bridgeHandler));
        }
        if (vehicle.has(JSON_KEY_REPEATING_PLANS) && vehicle.get(JSON_KEY_REPEATING_PLANS).isJsonArray()) {
            for (int index = 1; index <= vehicle.get(JSON_KEY_REPEATING_PLANS).getAsJsonArray().size(); index++) {
                String localizedLabel = tp.getText(ctx.getBundle(), "discovery.evcc.plan.repeating.label",
                        "Repeating plan {0} for {1}", lp.getLocale(), index, title);
                String label = localizedLabel == null ? "Repeating plan " + index + " for " + title : localizedLabel;
                results.add(createPlanDiscoveryResult(label, createIdString(id, index), index, id, bridgeHandler));
            }
        }
        return results;
    }

    private DiscoveryResult createPlanDiscoveryResult(String label, String planID, int index, String vehicleID,
            EvccBridgeHandler bridgeHandler) {
        ThingUID uid = new ThingUID(EvccBindingConstants.THING_TYPE_PLAN, bridgeHandler.getThing().getUID(), planID);
        return DiscoveryResultBuilder.create(uid).withLabel(label).withBridge(bridgeHandler.getThing().getUID())
                .withProperty(PROPERTY_ID, planID).withProperty(PROPERTY_VEHICLE_ID, vehicleID)
                .withProperty(PROPERTY_INDEX, String.valueOf(index)).withRepresentationProperty(PROPERTY_ID).build();
    }

    private String createIdString(String id, int index) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest((id + "Plan" + index).getBytes(StandardCharsets.UTF_8));
        // Use first 10 hex chars of the SHA to generate a stable, compact plan ID
        return HexUtils.bytesToHex(digest).substring(0, 10);
    }
}
