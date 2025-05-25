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
package org.openhab.binding.lgthinq.lgservices.model.devices.fridge;

import static org.openhab.binding.lgthinq.lgservices.LGServicesConstants.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.lgservices.model.CommandDefinition;
import org.openhab.binding.lgthinq.lgservices.model.FeatureDefinition;
import org.openhab.binding.lgthinq.lgservices.model.LGAPIVerion;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * The {@link FridgeCapabilityFactoryV2}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class FridgeCapabilityFactoryV2 extends AbstractFridgeCapabilityFactory {
    @Override
    protected FeatureDefinition newFeatureDefinition(String featureName, JsonNode featuresNode,
            @Nullable String targetChannelId, @Nullable String refChannelId) {
        return FeatureDefinition.NULL_DEFINITION;
    }

    @Override
    protected Map<String, CommandDefinition> getCommandsDefinition(JsonNode rootNode) {
        // doesn't meter command definition for V2
        return Collections.emptyMap();
    }

    @Override
    protected List<LGAPIVerion> getSupportedAPIVersions() {
        return List.of(LGAPIVerion.V2_0);
    }

    @Override
    public FridgeCapability getCapabilityInstance() {
        return new FridgeCanonicalCapability();
    }

    private void loadGenericFeatNode(JsonNode featNode, Map<String, String> capMap,
            final Map<String, String> constantsMap) {
        featNode.fields().forEachRemaining(f -> {
            // for each node like ' "1": {"index" : 1, "label" : "7", "_comment" : ""} '
            if (!"IGNORE".equals(f.getKey())) {
                String translatedValue = constantsMap.get(f.getValue().path("label").asText());
                translatedValue = translatedValue == null ? f.getValue().path("label").asText() : translatedValue;
                capMap.put(f.getKey(), translatedValue);
            }
        });
    }

    @Override
    protected void loadTempNode(JsonNode tempNode, Map<String, String> capMap) {
        loadGenericFeatNode(tempNode, capMap, Collections.emptyMap());
    }

    @Override
    protected void loadTempUnitNode(JsonNode tempUnitNode, Map<String, String> tempUnitMap) {
        tempUnitMap.putAll(CAP_RE_TEMP_UNIT_V2_MAP);
    }

    @Override
    protected void loadIcePlus(JsonNode icePlusNode, Map<String, String> icePlusMap) {
        // not supported
    }

    @Override
    protected void loadFreshAirFilter(JsonNode freshAirFilterNode, Map<String, String> freshAirFilterMap) {
        loadGenericFeatNode(freshAirFilterNode, freshAirFilterMap, CAP_RE_FRESH_AIR_FILTER_MAP);
    }

    @Override
    protected void loadWaterFilter(JsonNode waterFilterNode, Map<String, String> waterFilterMap) {
        loadGenericFeatNode(waterFilterNode, waterFilterMap, CAP_RE_WATER_FILTER);
    }

    @Override
    protected void loadExpressFreezeMode(JsonNode expressFreezeModeNode, Map<String, String> expressFreezeModeMap) {
        loadGenericFeatNode(expressFreezeModeNode, expressFreezeModeMap, CAP_RE_EXPRESS_FREEZE_MODES);
    }

    @Override
    protected void loadSmartSavingMode(JsonNode smartSavingModeNode, Map<String, String> smartSavingModeMap) {
        loadGenericFeatNode(smartSavingModeNode, smartSavingModeMap, CAP_RE_SMART_SAVING_V2_MODE);
    }

    @Override
    protected void loadActiveSaving(JsonNode activeSavingNode, Map<String, String> activeSavingMap) {
        loadGenericFeatNode(activeSavingNode, activeSavingMap, CAP_RE_LABEL_ON_OFF);
    }

    @Override
    protected void loadAtLeastOneDoorOpen(JsonNode atLeastOneDoorOpenNode, Map<String, String> atLeastOneDoorOpenMap) {
        loadGenericFeatNode(atLeastOneDoorOpenNode, atLeastOneDoorOpenMap, CAP_RE_LABEL_CLOSE_OPEN);
    }

    @Override
    protected String getMonitorValueNodeName() {
        return "MonitoringValue";
    }

    @Override
    protected String getFridgeTempCNodeName() {
        return "fridgeTemp_C";
    }

    protected String getFridgeTempNodeName() {
        throw new UnsupportedOperationException(
                "Fridge Thinq2 doesn't support FridgeTemp node. It is most likely a bug");
    }

    @Override
    protected String getFridgeTempFNodeName() {
        return "fridgeTemp_F";
    }

    @Override
    protected String getFreezerTempCNodeName() {
        return "freezerTemp_C";
    }

    @Override
    protected String getFreezerTempNodeName() {
        throw new UnsupportedOperationException(
                "Fridge Thinq2 doesn't support FreezerTemp node. It is most likely a bug");
    }

    @Override
    protected String getFreezerTempFNodeName() {
        return "freezerTemp_F";
    }

    protected String getTempUnitNodeName() {
        return "tempUnit";
    }

    protected String getIcePlusNodeName() {
        return "UNSUPPORTED";
    }

    @Override
    protected String getFreshAirFilterNodeName() {
        return "freshAirFilter";
    }

    @Override
    protected String getWaterFilterNodeName() {
        return "waterFilter";
    }

    @Override
    protected String getExpressModeNodeName() {
        return "expressMode";
    }

    @Override
    protected String getSmartSavingModeNodeName() {
        return "smartSavingMode";
    }

    @Override
    protected String getActiveSavingNodeName() {
        return "activeSaving";
    }

    @Override
    protected String getAtLeastOneDoorOpenNodeName() {
        return "atLeastOneDoorOpen";
    }

    @Override
    protected String getExpressCoolNodeName() {
        return "expressFridge";
    }

    @Override
    protected String getOptionsNodeName() {
        return "valueMapping";
    }

    @Override
    protected String getEcoFriendlyNodeName() {
        return "ecoFriendly";
    }
}
