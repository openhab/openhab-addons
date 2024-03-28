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
package org.openhab.binding.lgthinq.lgservices.model.devices.fridge;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.lgservices.FeatureDefinition;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * The {@link FridgeCapabilityFactoryV2}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class FridgeCapabilityFactoryV2 extends AbstractFridgeCapabilityFactory {
    @Override
    protected FeatureDefinition getFeatureDefinition(String featureName, JsonNode featuresNode) {
        return FeatureDefinition.NULL_DEFINITION;
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
