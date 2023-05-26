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
import org.openhab.binding.lgthinq.lgservices.model.LGAPIVerion;
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

    private void loadTempNode(JsonNode tempNode, Map<String, String> capMap, String unit) {
        tempNode.forEach(v -> {
            // for each node like ' "1": {"index" : 1, "label" : "7", "_comment" : ""} '
            capMap.put(v.path("index").asText() + " " + unit, v.path("label").textValue() + " " + unit);
        });
    }

    @Override
    public FridgeCapability create(JsonNode rootNode) throws LGThinqException {
        FridgeCapability frCap = super.create(rootNode);

        JsonNode node = mapper.valueToTree(rootNode);
        if (node.isNull()) {
            logger.error("Can't parse json capability for Fridge. The payload has been ignored");
            logger.debug("payload {}", rootNode);
            throw new LGThinqException("Can't parse json capability for Fridge. The payload has been ignored");
        }
        /**
         * iterate over valueMappings like:
         * "valueMapping": {
         * "1": {"index" : 1, "label" : "7", "_comment" : ""},
         * "2": {"index" : 2, "label" : "6", "_comment" : ""},
         * "3": {"index" : 3, "label" : "5", "_comment" : ""},
         * "4": {"index" : 4, "label" : "4", "_comment" : ""},
         * "5": {"index" : 5, "label" : "3", "_comment" : ""},
         * "6": {"index" : 6, "label" : "2", "_comment" : ""},
         * "7": {"index" : 7, "label" : "1", "_comment" : ""},
         * "255" : {"index" : 255, "label" : "IGNORE", "_comment" : ""}
         * }
         */

        JsonNode fridgeTempCNode = node.path(getMonitorValueNodeName()).path(getFridgeTempCNodeName())
                .path(getOptionsNodeName());
        JsonNode fridgeTempFNode = node.path(getMonitorValueNodeName()).path(getFridgeTempFNodeName())
                .path(getOptionsNodeName());
        JsonNode freezerTempCNode = node.path(getMonitorValueNodeName()).path(getFreezerTempCNodeName())
                .path(getOptionsNodeName());
        JsonNode freezerTempFNode = node.path(getMonitorValueNodeName()).path(getFreezerTempFNodeName())
                .path(getOptionsNodeName());
        loadTempNode(fridgeTempCNode, frCap.getFridgeTempCMap(), TEMP_UNIT_CELSIUS_SYMBOL);
        loadTempNode(fridgeTempFNode, frCap.getFridgeTempFMap(), TEMP_UNIT_FAHRENHEIT_SYMBOL);
        loadTempNode(freezerTempCNode, frCap.getFreezerTempCMap(), TEMP_UNIT_CELSIUS_SYMBOL);
        loadTempNode(freezerTempFNode, frCap.getFreezerTempFMap(), TEMP_UNIT_FAHRENHEIT_SYMBOL);
        return frCap;
    }

    @Override
    protected List<DeviceTypes> getSupportedDeviceTypes() {
        return List.of(DeviceTypes.REFRIGERATOR);
    }

    @Override
    protected List<LGAPIVerion> getSupportedAPIVersions() {
        return List.of(LGAPIVerion.V1_0, LGAPIVerion.V2_0);
    }

    protected abstract String getMonitorValueNodeName();

    protected abstract String getFridgeTempCNodeName();

    protected abstract String getFridgeTempFNodeName();

    protected abstract String getFreezerTempCNodeName();

    protected abstract String getFreezerTempFNodeName();

    protected abstract String getOptionsNodeName();
}
