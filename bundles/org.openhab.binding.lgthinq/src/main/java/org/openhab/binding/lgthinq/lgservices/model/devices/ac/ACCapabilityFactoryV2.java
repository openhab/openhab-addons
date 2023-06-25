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
package org.openhab.binding.lgthinq.lgservices.model.devices.ac;

import java.util.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.internal.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.internal.errors.LGThinqException;
import org.openhab.binding.lgthinq.lgservices.model.CommandDefinition;
import org.openhab.binding.lgthinq.lgservices.model.LGAPIVerion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * The {@link ACCapabilityFactoryV2}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class ACCapabilityFactoryV2 extends AbstractACCapabilityFactory {
    private static final Logger logger = LoggerFactory.getLogger(ACCapabilityFactoryV2.class);

    @Override
    protected List<LGAPIVerion> getSupportedAPIVersions() {
        return List.of(LGAPIVerion.V2_0);
    }

    @Override
    protected Map<String, CommandDefinition> getCommandsDefinition(JsonNode rootNode) throws LGThinqApiException {
        Map<String, CommandDefinition> result = new HashMap<>();
        JsonNode controlDeviceNode = rootNode.path("ControlDevice");
        if (controlDeviceNode.isArray()) {
            controlDeviceNode.forEach(c -> {
                String ctrlKey = c.path("ctrlKey").asText();
                // commands variations are described separated by pipe "|"
                String[] commands = c.path("command").asText().split("\\|");
                String dataValues = c.path("dataValue").asText();
                int i = 0;
                for (String cOpt : commands) {
                    CommandDefinition cd = new CommandDefinition();
                    cd.setCommand(ctrlKey);
                    cd.setCmdOpt(cOpt);
                    cd.setCmdOptValue(dataValues.replaceAll("[{%}]", ""));
                    cd.setRawCommand(c.toPrettyString());
                    result.put(ctrlKey, cd);
                    i++;
                }
            });
        }
        return result;
    }

    @Override
    protected String getOpModeNodeName() {
        return "airState.opMode";
    }

    @Override
    protected String getFanSpeedNodeName() {
        return "airState.windStrength";
    }

    @Override
    protected String getSupOpModeNodeName() {
        return "support.airState.opMode";
    }

    @Override
    protected String getSupFanSpeedNodeName() {
        return "support.airState.windStrength";
    }

    @Override
    protected String getJetModeNodeName() {
        return "airState.wMode.jet";
    }

    @Override
    protected String getSupSubRacModeNodeName() {
        return "support.racSubMode";
    }

    @Override
    protected String getSupRacModeNodeName() {
        return "support.racMode";
    }

    @Override
    protected String getAutoDryStateNodeName() {
        return "airState.miscFuncState.autoDry";
    }

    @Override
    protected String getAirCleanStateNodeName() {
        return "airState.wMode.airClean";
    }

    @Override
    protected String getOptionsMapNodeName() {
        return "value_mapping";
    }

    @Override
    protected String getValuesNodeName() {
        return "Value";
    }

    @Override
    protected String getDataTypeFeatureNodeName() {
        return "dataType";
    }

    @Override
    protected Map<String, String> extractFeatureOptions(JsonNode optionsNode) {
        Map<String, String> options = new HashMap<>();
        optionsNode.fields().forEachRemaining(o -> {
            options.put(o.getKey(), o.getValue().path("label").asText());
        });
        return options;
    }

    @Override
    public ACCapability create(JsonNode rootNode) throws LGThinqException {
        ACCapability cap = super.create(rootNode);
        Map<String, CommandDefinition> cmd = getCommandsDefinition(rootNode);
        // set energy and filter availability (extended info)
        cap.setEnergyMonitorAvailable(cmd.containsKey("energyStateCtrl"));
        cap.setFilterMonitorAvailable(cmd.containsKey("filterMngStateCtrl"));
        return cap;
    }
}
