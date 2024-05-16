/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static org.openhab.binding.lgthinq.internal.LGThinQBindingConstants.*;

import java.util.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.internal.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.model.CommandDefinition;
import org.openhab.binding.lgthinq.lgservices.model.FeatureDefinition;
import org.openhab.binding.lgthinq.lgservices.model.LGAPIVerion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * The {@link FridgeCapabilityFactoryV1}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class FridgeCapabilityFactoryV1 extends AbstractFridgeCapabilityFactory {
    private static final Logger logger = LoggerFactory.getLogger(FridgeCapabilityFactoryV1.class);

    @Override
    protected FeatureDefinition newFeatureDefinition(String featureName, JsonNode featuresNode,
            @Nullable String targetChannelId, @Nullable String refChannelId) {
        // TODO - Implement feature definition
        return FeatureDefinition.NULL_DEFINITION;
    }

    @Override
    protected Map<String, CommandDefinition> getCommandsDefinition(JsonNode rootNode) throws LGThinqApiException {
        return getCommandsDefinitionV1(rootNode);
    }

    private void loadGenericFeatNode(JsonNode featNode, Map<String, String> capMap,
            final Map<String, String> constantsMap) {
        featNode.fields().forEachRemaining(f -> {
            // for each node like ' "1": {"index" : 1, "label" : "7", "_comment" : ""} '
            String translatedValue = constantsMap.get(f.getValue().asText());
            translatedValue = translatedValue == null ? f.getValue().asText() : translatedValue;
            capMap.put(f.getKey(), translatedValue);
        });
    }

    protected void loadTempNode(JsonNode tempNode, Map<String, String> capMap, String unit) {
        loadGenericFeatNode(tempNode, capMap, Collections.emptyMap());
    }

    @Override
    protected void loadTempUnitNode(JsonNode tempUnitNode, Map<String, String> tempUnitMap) {
        loadGenericFeatNode(tempUnitNode, tempUnitMap, Collections.emptyMap());
    }

    @Override
    protected void loadIcePlus(JsonNode icePlusNode, Map<String, String> icePlusMap) {
        loadGenericFeatNode(icePlusNode, icePlusMap, CAP_FR_ON_OFF);
    }

    @Override
    protected void loadFreshAirFilter(JsonNode freshAirFilterNode, Map<String, String> freshAirFilterMap) {
        loadGenericFeatNode(freshAirFilterNode, freshAirFilterMap, CAP_FR_FRESH_AIR_FILTER_MAP);
    }

    @Override
    protected void loadWaterFilter(JsonNode waterFilterNode, Map<String, String> waterFilterMap) {
        int minValue = waterFilterNode.path("min").asInt(0);
        int maxValue = waterFilterNode.path("max").asInt(6);
        for (int i = minValue; i <= maxValue; i++) {
            waterFilterMap.put(String.valueOf(i), i + CAP_FR_WATER_FILTER_USED_POSTFIX);
        }
    }

    @Override
    protected void loadExpressFreezeMode(JsonNode expressFreezeModeNode, Map<String, String> expressFreezeModeMap) {
        // not supported
    }

    @Override
    protected void loadSmartSavingMode(JsonNode smartSavingModeNode, Map<String, String> smartSavingModeMap) {
        loadGenericFeatNode(smartSavingModeNode, smartSavingModeMap, CAP_FR_SMART_SAVING_MODE);
    }

    @Override
    protected void loadActiveSaving(JsonNode activeSavingNode, Map<String, String> activeSavingMap) {
        int minValue = activeSavingNode.path("min").asInt(0);
        int maxValue = activeSavingNode.path("max").asInt(3);
        for (int i = minValue; i <= maxValue; i++) {
            activeSavingMap.put(String.valueOf(i), String.valueOf(i));
        }
    }

    @Override
    protected void loadAtLeastOneDoorOpen(JsonNode atLeastOneDoorOpenNode, Map<String, String> atLeastOneDoorOpenMap) {
        loadGenericFeatNode(atLeastOneDoorOpenNode, atLeastOneDoorOpenMap, Collections.emptyMap());
    }

    @Override
    protected List<LGAPIVerion> getSupportedAPIVersions() {
        return List.of(LGAPIVerion.V1_0);
    }

    @Override
    public FridgeCapability getCapabilityInstance() {
        return new FridgeCanonicalCapability();
    }

    @Override
    protected String getMonitorValueNodeName() {
        return "Value";
    }

    @Override
    protected String getFridgeTempCNodeName() {
        return "TempRefrigerator_C";
    }

    protected String getFridgeTempNodeName() {
        return "TempRefrigerator";
    }

    @Override
    protected String getFridgeTempFNodeName() {
        return "TempRefrigerator_F";
    }

    @Override
    protected String getFreezerTempCNodeName() {
        return "TempFreezer_C";
    }

    protected String getFreezerTempNodeName() {
        return "TempFreezer";
    }

    @Override
    protected String getFreezerTempFNodeName() {
        return "TempFreezer_F";
    }

    @Override
    protected String getOptionsNodeName() {
        return "option";
    }

    @Override
    protected String getEcoFriendlyNodeName() {
        return "UNSUPPORTED";
    }

    protected String getTempUnitNodeName() {
        return "TempUnit";
    }

    protected String getIcePlusNodeName() {
        return "IcePlus";
    }

    @Override
    protected String getFreshAirFilterNodeName() {
        return "FreshAirFilter";
    }

    @Override
    protected String getWaterFilterNodeName() {
        return "WaterFilterUsedMonth";
    }

    @Override
    protected String getExpressModeNodeName() {
        return "UNSUPPORTED";
    }

    @Override
    protected String getSmartSavingModeNodeName() {
        return "SmartSavingMode";
    }

    @Override
    protected String getActiveSavingNodeName() {
        return "SmartSavingModeStatus";
    }

    @Override
    protected String getAtLeastOneDoorOpenNodeName() {
        return "DoorOpenState";
    }

    @Override
    protected String getExpressCoolNodeName() {
        return "UNSUPPORTED";
    }
}
