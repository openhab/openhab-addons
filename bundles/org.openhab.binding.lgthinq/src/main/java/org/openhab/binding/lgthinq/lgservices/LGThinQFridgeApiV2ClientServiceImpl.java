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
package org.openhab.binding.lgthinq.lgservices;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.lgthinq.lgservices.api.RestResult;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.model.DevicePowerState;
import org.openhab.binding.lgthinq.lgservices.model.devices.fridge.FridgeCanonicalSnapshot;
import org.openhab.binding.lgthinq.lgservices.model.devices.fridge.FridgeCapability;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The {@link LGThinQFridgeApiV2ClientServiceImpl}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class LGThinQFridgeApiV2ClientServiceImpl
        extends LGThinQAbstractApiV2ClientService<FridgeCapability, FridgeCanonicalSnapshot>
        implements LGThinQFridgeApiClientService {

    protected LGThinQFridgeApiV2ClientServiceImpl(HttpClient httpClient) {
        super(FridgeCapability.class, FridgeCanonicalSnapshot.class, httpClient);
    }

    @Override
    protected boolean beforeGetDataDevice(String bridgeName, String deviceId) {
        // there's no before settings to send command
        return false;
    }

    @Override
    public void turnDevicePower(String bridgeName, String deviceId, DevicePowerState newPowerState) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void setFridgeTemperature(String bridgeId, String deviceId, FridgeCapability fridgeCapability,
            Integer targetTemperatureIndex, String tempUnit, @Nullable Map<String, Object> snapCmdData)
            throws LGThinqApiException {
        setTemperature("fridgeTemp", bridgeId, deviceId, targetTemperatureIndex, tempUnit);
    }

    @Override
    public void setFreezerTemperature(String bridgeId, String deviceId, FridgeCapability fridgeCapability,
            Integer targetTemperatureIndex, String tempUnit, @Nullable Map<String, Object> snapCmdData)
            throws LGThinqApiException {
        setTemperature("freezerTemp", bridgeId, deviceId, targetTemperatureIndex, tempUnit);
    }

    @Override
    public void setExpressMode(String bridgeId, String deviceId, String expressMode) throws LGThinqApiException {
        sendSimpleDataSetListCommand(bridgeId, deviceId, "expressMode", expressMode);
    }

    private void sendSimpleDataSetListCommand(String bridgeId, String deviceId, String feature, String value)
            throws LGThinqApiException {
        ObjectNode dataSetList = JsonNodeFactory.instance.objectNode();
        ObjectNode nodeData = dataSetList.putObject("dataSetList").putObject("refState");
        nodeData.put(feature, value);
        try {
            RestResult result = sendCommand(bridgeId, deviceId, "control-sync", "basicCtrl", "Set", null, null,
                    dataSetList);
            handleGenericErrorResult(result);
        } catch (Exception e) {
            throw new LGThinqApiException("Error sending command", e);
        }
    }

    @Override
    public void setExpressCoolMode(String bridgeId, String deviceId, boolean trueOnFalseOff)
            throws LGThinqApiException {
        sendSimpleDataSetListCommand(bridgeId, deviceId, "expressFridge", trueOnFalseOff ? "ON" : "OFF");
    }

    @Override
    public void setEcoFriendlyMode(String bridgeId, String deviceId, boolean trueOnFalseOff)
            throws LGThinqApiException {
        sendSimpleDataSetListCommand(bridgeId, deviceId, "ecoFriendly", trueOnFalseOff ? "ON" : "OFF");
    }

    @Override
    public void setIcePlus(String bridgeId, String deviceId, FridgeCapability fridgeCapability, boolean trueOnFalseOff,
            Map<String, Object> snapCmdData) {
        throw new UnsupportedOperationException("V2 Fridge doesn't support IcePlus feature. It mostly like a bug");
    }

    private void setTemperature(String tempFeature, String bridgeId, String deviceId, Integer targetTemperature,
            String tempUnit) throws LGThinqApiException {
        ObjectNode dataSetList = JsonNodeFactory.instance.objectNode();
        ObjectNode nodeData = dataSetList.putObject("dataSetList").putObject("refState");
        nodeData.put(tempFeature, targetTemperature).put("tempUnit", tempUnit);
        try {
            RestResult result = sendCommand(bridgeId, deviceId, "control-sync", "basicCtrl", "Set", null, null,
                    dataSetList);
            handleGenericErrorResult(result);
        } catch (Exception e) {
            throw new LGThinqApiException("Error sending command", e);
        }
    }
}
