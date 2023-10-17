/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.openhab.binding.lgthinq.internal.LGThinQBindingConstants.TEMP_UNIT_CELSIUS_SYMBOL;
import static org.openhab.binding.lgthinq.internal.LGThinQBindingConstants.TEMP_UNIT_FAHRENHEIT_SYMBOL;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.internal.errors.LGThinqException;
import org.openhab.binding.lgthinq.lgservices.model.AbstractCapabilityFactory;
import org.openhab.binding.lgthinq.lgservices.model.DeviceTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * The {@link AbstractFridgeCapabilityFactory}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractFridgeCapabilityFactory extends AbstractCapabilityFactory<FridgeCapability> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractFridgeCapabilityFactory.class);

    protected abstract void loadTempNode(JsonNode tempNode, Map<String, String> capMap, String unit);

    @Override
    public FridgeCapability create(JsonNode rootNode) throws LGThinqException {
        FridgeCapability frCap = super.create(rootNode);
        JsonNode node = mapper.valueToTree(rootNode);
        if (node.isNull()) {
            logger.error("Can't parse json capability for Fridge. The payload has been ignored");
            logger.debug("payload {}", rootNode);
            throw new LGThinqException("Can't parse json capability for Fridge. The payload has been ignored");
        }

        JsonNode fridgeTempCNode = node.path(getMonitorValueNodeName()).path(getFridgeTempCNodeName())
                .path(getOptionsNodeName());
        JsonNode fridgeTempFNode = node.path(getMonitorValueNodeName()).path(getFridgeTempFNodeName())
                .path(getOptionsNodeName());
        JsonNode freezerTempCNode = node.path(getMonitorValueNodeName()).path(getFreezerTempCNodeName())
                .path(getOptionsNodeName());
        JsonNode freezerTempFNode = node.path(getMonitorValueNodeName()).path(getFreezerTempFNodeName())
                .path(getOptionsNodeName());
        JsonNode tempUnitNode = node.path(getMonitorValueNodeName()).path(getTempUnitNodeName())
                .path(getOptionsNodeName());
        JsonNode icePlusNode = node.path(getMonitorValueNodeName()).path(getIcePlusNodeName())
                .path(getOptionsNodeName());
        JsonNode freshAirFilterNode = node.path(getMonitorValueNodeName()).path(getFreshAirFilterNodeName())
                .path(getOptionsNodeName());
        JsonNode waterFilterNode = node.path(getMonitorValueNodeName()).path(getWaterFilterNodeName())
                .path(getOptionsNodeName());
        JsonNode expressModeNode = node.path(getMonitorValueNodeName()).path(getExpressModeNodeName())
                .path(getOptionsNodeName());
        JsonNode smartSavingModeNode = node.path(getMonitorValueNodeName()).path(getSmartSavingModeNodeName())
                .path(getOptionsNodeName());
        JsonNode activeSavingNode = node.path(getMonitorValueNodeName()).path(getActiveSavingNodeName())
                .path(getOptionsNodeName());
        JsonNode atLeastOneDoorOpenNode = node.path(getMonitorValueNodeName()).path(getAtLeastOneDoorOpenNodeName())
                .path(getOptionsNodeName());

        loadTempNode(fridgeTempCNode, frCap.getFridgeTempCMap(), TEMP_UNIT_CELSIUS_SYMBOL);
        loadTempNode(fridgeTempFNode, frCap.getFridgeTempFMap(), TEMP_UNIT_FAHRENHEIT_SYMBOL);
        loadTempNode(freezerTempCNode, frCap.getFreezerTempCMap(), TEMP_UNIT_CELSIUS_SYMBOL);
        loadTempNode(freezerTempFNode, frCap.getFreezerTempFMap(), TEMP_UNIT_FAHRENHEIT_SYMBOL);
        loadTempUnitNode(tempUnitNode, frCap.getTempUnitMap());
        loadIcePlus(icePlusNode, frCap.getIcePlusMap());
        loadFreshAirFilter(freshAirFilterNode, frCap.getFreshAirFilterMap());
        loadWaterFilter(waterFilterNode, frCap.getWaterFilterMap());
        loadExpressMode(expressModeNode, frCap.getExpressModeMap());
        loadSmartSavingMode(smartSavingModeNode, frCap.getSmartSavingMap());
        loadActiveSaving(activeSavingNode, frCap.getActiveSavingMap());
        loadAtLeastOneDoorOpen(atLeastOneDoorOpenNode, frCap.getAtLeastOneDoorOpenMap());

        frCap.getCommandsDefinition().putAll(getCommandsDefinition(node));
        return frCap;
    }

    protected abstract void loadTempUnitNode(JsonNode tempUnitNode, Map<String, String> tempUnitMap);

    protected abstract void loadIcePlus(JsonNode icePlusNode, Map<String, String> icePlusMap);

    protected abstract void loadFreshAirFilter(JsonNode freshAirFilterNode, Map<String, String> freshAirFilterMap);

    protected abstract void loadWaterFilter(JsonNode waterFilterNode, Map<String, String> waterFilterMap);

    protected abstract void loadExpressMode(JsonNode expressModeNode, Map<String, String> expressModeMap);

    protected abstract void loadSmartSavingMode(JsonNode smartSavingModeNode, Map<String, String> smartSavingModeMap);

    protected abstract void loadActiveSaving(JsonNode activeSavingNode, Map<String, String> activeSavingMap);

    protected abstract void loadAtLeastOneDoorOpen(JsonNode atLeastOneDoorOpenNode,
            Map<String, String> atLeastOneDoorOpenMap);

    @Override
    protected List<DeviceTypes> getSupportedDeviceTypes() {
        return List.of(DeviceTypes.REFRIGERATOR);
    }

    protected abstract String getMonitorValueNodeName();

    protected abstract String getFridgeTempCNodeName();

    protected abstract String getFridgeTempFNodeName();

    protected abstract String getFreezerTempCNodeName();

    protected abstract String getFreezerTempFNodeName();

    protected abstract String getTempUnitNodeName();

    protected abstract String getIcePlusNodeName();

    protected abstract String getFreshAirFilterNodeName();

    protected abstract String getWaterFilterNodeName();

    protected abstract String getExpressModeNodeName();

    protected abstract String getSmartSavingModeNodeName();

    protected abstract String getActiveSavingNodeName();

    protected abstract String getAtLeastOneDoorOpenNodeName();

    protected abstract String getOptionsNodeName();
}
