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
package org.openhab.binding.lgthinq.lgservices.model.devices.ac;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqException;
import org.openhab.binding.lgthinq.lgservices.model.CommandDefinition;
import org.openhab.binding.lgthinq.lgservices.model.LGAPIVerion;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * The {@link ACCapabilityFactoryV1}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class ACCapabilityFactoryV1 extends AbstractACCapabilityFactory {

    @Override
    protected List<LGAPIVerion> getSupportedAPIVersions() {
        return List.of(LGAPIVerion.V1_0);
    }

    @Override
    protected Map<String, CommandDefinition> getCommandsDefinition(JsonNode rootNode) {
        return Collections.emptyMap();
    }

    @Override
    protected Map<String, String> extractFeatureOptions(JsonNode optionsNode) {
        Map<String, String> options = new HashMap<>();
        optionsNode.fields().forEachRemaining(o -> {
            options.put(o.getKey(), o.getValue().asText());
        });
        return options;
    }

    @Override
    public ACCapability create(JsonNode rootNode) throws LGThinqException {
        ACCapability cap = super.create(rootNode);
        // set energy and filter availability (extended info)
        cap.setEnergyMonitorAvailable(
                !rootNode.path("ControlWifi").path("action").path("GetInOutInstantPower").isMissingNode());
        cap.setFilterMonitorAvailable(
                !rootNode.path("ControlWifi").path("action").path("GetFilterUse").isMissingNode());
        return cap;
    }

    @Override
    protected String getDataTypeFeatureNodeName() {
        return "type";
    }

    @Override
    protected String getOpModeNodeName() {
        return "OpMode";
    }

    @Override
    protected String getFanSpeedNodeName() {
        return "WindStrength";
    }

    @Override
    protected String getSupOpModeNodeName() {
        return "SupportOpMode";
    }

    @Override
    protected String getSupFanSpeedNodeName() {
        return "SupportWindStrength";
    }

    @Override
    protected String getJetModeNodeName() {
        return "Jet";
    }

    @Override
    protected String getStepUpDownNodeName() {
        return "WDirVStep";
    }

    @Override
    protected String getStepLeftRightNodeName() {
        return "WDirHStep";
    }

    @Override
    protected String getSupSubRacModeNodeName() {
        return "SupportRACSubMode";
    }

    @Override
    protected String getSupRacModeNodeName() {
        return "SupportRACMode";
    }

    @Override
    protected String getAutoDryStateNodeName() {
        return "AutoDry";
    }

    @Override
    protected String getAirCleanStateNodeName() {
        return "AirClean";
    }

    @Override
    protected String getOptionsMapNodeName() {
        return "option";
    }

    @Override
    protected String getValuesNodeName() {
        return "Value";
    }

    @Override
    protected String getHpAirWaterSwitchNodeName() {
        throw new UnsupportedOperationException("Heat Pump Thinq V1 not implemented yet! Ignoring node");
    }
}
