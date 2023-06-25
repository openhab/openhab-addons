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

import java.util.Collections;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.internal.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.FeatureDefinition;
import org.openhab.binding.lgthinq.lgservices.model.CommandDefinition;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * The {@link FridgeCapabilityFactoryV1}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class FridgeCapabilityFactoryV1 extends AbstractFridgeCapabilityFactory {
    @Override
    protected FeatureDefinition newFeatureDefinition(String featureName, JsonNode featuresNode,
            @Nullable String targetChannelId, @Nullable String refChannelId) {
        // TODO - Implement feature definition
        return FeatureDefinition.NULL_DEFINITION;
    }

    // TODO - Implement Commands parser
    @Override
    protected Map<String, CommandDefinition> getCommandsDefinition(JsonNode rootNode) throws LGThinqApiException {
        return Collections.emptyMap();
    }

    @Override
    public FridgeCapability getCapabilityInstance() {
        return new FridgeCanonicalCapability();
    }

    @Override
    protected String getMonitorValueNodeName() {
        return "MonitoringValue";
    }

    @Override
    protected String getFridgeTempCNodeName() {
        return "fridgeTemp_C";
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
    protected String getFreezerTempFNodeName() {
        return "freezerTemp_F";
    }

    @Override
    protected String getOptionsNodeName() {
        return "valueMapping";
    }
}
