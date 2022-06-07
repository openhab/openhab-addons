/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.lgthinq.lgservices.model.fridge.v2;

import static org.openhab.binding.lgthinq.internal.LGThinQBindingConstants.TEMP_UNIT_CELSIUS_SYMBOL;
import static org.openhab.binding.lgthinq.internal.LGThinQBindingConstants.TEMP_UNIT_FAHRENHEIT_SYMBOL;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.lgservices.model.AbstractCapability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The {@link FridgeCapabilityV2}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class FridgeCapabilityV2 extends AbstractCapability
        implements org.openhab.binding.lgthinq.lgservices.model.fridge.FridgeCapability {

    private static final Logger logger = LoggerFactory.getLogger(FridgeCapabilityV2.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final Map<String, String> fridgeTempCMap = new LinkedHashMap<String, String>();
    private final Map<String, String> fridgeTempFMap = new LinkedHashMap<String, String>();
    private final Map<String, String> freezerTempCMap = new LinkedHashMap<String, String>();
    private final Map<String, String> freezerTempFMap = new LinkedHashMap<String, String>();

    public Map<String, String> getFridgeTempCMap() {
        return fridgeTempCMap;
    }

    public Map<String, String> getFridgeTempFMap() {
        return fridgeTempFMap;
    }

    public Map<String, String> getFreezerTempCMap() {
        return freezerTempCMap;
    }

    public Map<String, String> getFreezerTempFMap() {
        return freezerTempFMap;
    }

    @Override
    public void loadCapabilities(Object veryRootNode) {
        JsonNode node = mapper.valueToTree(veryRootNode);
        if (node.isNull()) {
            logger.error("Can't parse json capability for Fridge V2. The payload has been ignored");
            logger.debug("payload {}", veryRootNode);
            return;
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

        JsonNode fridgeTempCNode = node.path("MonitoringValue").path("fridgeTemp_C").path("valueMapping");
        JsonNode fridgeTempFNode = node.path("MonitoringValue").path("fridgeTemp_F").path("valueMapping");
        JsonNode freezerTempCNode = node.path("MonitoringValue").path("freezerTemp_C").path("valueMapping");
        JsonNode freezerTempFNode = node.path("MonitoringValue").path("freezerTemp_F").path("valueMapping");
        loadTempNode(fridgeTempCNode, fridgeTempCMap, TEMP_UNIT_CELSIUS_SYMBOL);
        loadTempNode(fridgeTempFNode, fridgeTempFMap, TEMP_UNIT_FAHRENHEIT_SYMBOL);
        loadTempNode(freezerTempCNode, freezerTempCMap, TEMP_UNIT_CELSIUS_SYMBOL);
        loadTempNode(freezerTempFNode, freezerTempFMap, TEMP_UNIT_FAHRENHEIT_SYMBOL);
    }

    private void loadTempNode(JsonNode tempNode, Map<String, String> capMap, String unit) {
        tempNode.forEach(v -> {
            // for each node like ' "1": {"index" : 1, "label" : "7", "_comment" : ""} '
            capMap.put(v.path("index").asText() + " " + unit, v.path("label").textValue() + " " + unit);
        });
    }
}
