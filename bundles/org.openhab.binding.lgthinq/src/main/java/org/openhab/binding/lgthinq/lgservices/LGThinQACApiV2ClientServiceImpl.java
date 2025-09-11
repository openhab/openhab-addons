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

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.lgthinq.lgservices.api.RestResult;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.model.DevicePowerState;
import org.openhab.binding.lgthinq.lgservices.model.DeviceTypes;
import org.openhab.binding.lgthinq.lgservices.model.devices.ac.ACCanonicalSnapshot;
import org.openhab.binding.lgthinq.lgservices.model.devices.ac.ACCapability;
import org.openhab.binding.lgthinq.lgservices.model.devices.ac.ACTargetTmp;
import org.openhab.binding.lgthinq.lgservices.model.devices.ac.ExtendedDeviceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The {@link LGThinQACApiV2ClientServiceImpl}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class LGThinQACApiV2ClientServiceImpl extends
        LGThinQAbstractApiV2ClientService<ACCapability, ACCanonicalSnapshot> implements LGThinQACApiClientService {

    private final Logger logger = LoggerFactory.getLogger(LGThinQACApiV2ClientServiceImpl.class);

    protected LGThinQACApiV2ClientServiceImpl(HttpClient httpClient) {
        super(ACCapability.class, ACCanonicalSnapshot.class, httpClient);
    }

    @Override
    public void turnDevicePower(String bridgeName, String deviceId, DevicePowerState newPowerState)
            throws LGThinqApiException {
        try {
            RestResult resp = sendBasicControlCommands(bridgeName, deviceId, "Operation", "airState.operation",
                    newPowerState.commandValue());
            handleGenericErrorResult(resp);
        } catch (Exception e) {
            throw new LGThinqApiException("Error adjusting device power", e);
        }
    }

    @Override
    public void turnCoolJetMode(String bridgeName, String deviceId, String modeOnOff) throws LGThinqApiException {
        turnGenericMode(bridgeName, deviceId, "airState.wMode.jet", modeOnOff);
    }

    public void turnAirCleanMode(String bridgeName, String deviceId, String modeOnOff) throws LGThinqApiException {
        turnGenericMode(bridgeName, deviceId, "airState.wMode.airClean", modeOnOff);
    }

    public void turnAutoDryMode(String bridgeName, String deviceId, String modeOnOff) throws LGThinqApiException {
        turnGenericMode(bridgeName, deviceId, "airState.miscFuncState.autoDry", modeOnOff);
    }

    public void turnEnergySavingMode(String bridgeName, String deviceId, String modeOnOff) throws LGThinqApiException {
        turnGenericMode(bridgeName, deviceId, "airState.powerSave.basic", modeOnOff);
    }

    protected void turnGenericMode(String bridgeName, String deviceId, String modeName, String modeOnOff)
            throws LGThinqApiException {
        try {
            RestResult resp = sendBasicControlCommands(bridgeName, deviceId, "Operation", modeName,
                    Integer.parseInt(modeOnOff));
            handleGenericErrorResult(resp);
        } catch (Exception e) {
            throw new LGThinqApiException("Error adjusting cool jet mode", e);
        }
    }

    @Override
    public void changeOperationMode(String bridgeName, String deviceId, int newOpMode) throws LGThinqApiException {
        try {
            RestResult resp = sendBasicControlCommands(bridgeName, deviceId, "Set", "airState.opMode", newOpMode);
            handleGenericErrorResult(resp);
        } catch (LGThinqApiException e) {
            throw e;
        } catch (Exception e) {
            throw new LGThinqApiException("Error adjusting operation mode", e);
        }
    }

    @Override
    public void changeFanSpeed(String bridgeName, String deviceId, int newFanSpeed) throws LGThinqApiException {
        try {
            RestResult resp = sendBasicControlCommands(bridgeName, deviceId, "Set", "airState.windStrength",
                    newFanSpeed);
            handleGenericErrorResult(resp);
        } catch (LGThinqApiException e) {
            throw e;
        } catch (Exception e) {
            throw new LGThinqApiException("Error adjusting operation mode", e);
        }
    }

    @Override
    public void changeStepUpDown(String bridgeName, String deviceId, ACCanonicalSnapshot currentSnap, int newStep)
            throws LGThinqApiException {
        try {
            RestResult resp = sendBasicControlCommands(bridgeName, deviceId, "Set", "airState.wDir.vStep", newStep);
            handleGenericErrorResult(resp);
        } catch (LGThinqApiException e) {
            throw e;
        } catch (Exception e) {
            throw new LGThinqApiException("Error adjusting operation mode", e);
        }
    }

    @Override
    public void changeStepLeftRight(String bridgeName, String deviceId, ACCanonicalSnapshot currentSnap, int newStep)
            throws LGThinqApiException {
        try {
            RestResult resp = sendBasicControlCommands(bridgeName, deviceId, "Set", "airState.wDir.hStep", newStep);
            handleGenericErrorResult(resp);
        } catch (LGThinqApiException e) {
            throw e;
        } catch (Exception e) {
            throw new LGThinqApiException("Error adjusting operation mode", e);
        }
    }

    @Override
    public void changeTargetTemperature(String bridgeName, String deviceId, ACTargetTmp newTargetTemp)
            throws LGThinqApiException {
        try {
            RestResult resp = sendBasicControlCommands(bridgeName, deviceId, "Set", "airState.tempState.target",
                    newTargetTemp.commandValue());
            handleGenericErrorResult(resp);
        } catch (LGThinqApiException e) {
            throw e;
        } catch (Exception e) {
            throw new LGThinqApiException("Error adjusting operation mode", e);
        }
    }

    /**
     * Start monitor data form specific device. This is old one, <b>works only on V1 API supported devices</b>.
     *
     * @param deviceId Device ID
     * @return Work1 to be uses to grab data during monitoring.
     */
    @Override
    public String startMonitor(String bridgeName, String deviceId) {
        throw new UnsupportedOperationException("Not supported in V2 API.");
    }

    @Override
    public void stopMonitor(String bridgeName, String deviceId, String workId) {
        throw new UnsupportedOperationException("Not supported in V2 API.");
    }

    @Override
    public @Nullable ACCanonicalSnapshot getMonitorData(String bridgeName, String deviceId, String workId,
            DeviceTypes deviceType, ACCapability deviceCapability) {
        throw new UnsupportedOperationException("Not supported in V2 API.");
    }

    @Override
    protected boolean beforeGetDataDevice(String bridgeName, String deviceId) {
        try {
            RestResult resp = sendCommand(bridgeName, deviceId, "control", "allEventEnable", "Set",
                    "airState.mon.timeout", "70");
            handleGenericErrorResult(resp);
            if (resp.getStatusCode() == 400) {
                // Access Denied. Return false to indicate user don't have access to this functionality
                return false;
            }
        } catch (Exception e) {
            logger.debug("Can't execute Before Update command", e);
        }
        return true;
    }

    /**
     * Expect receiving json of format: {
     * ...
     * result: {
     * data: {
     * ...
     * }
     * ...
     * }
     * }
     * Data node will be deserialized into the object informed
     *
     * @param jsonResult json result
     * @param obj object to be updated
     * @throws IOException if there are errors deserialization the jsonResult
     */
    private void readDataResultNodeToObject(String jsonResult, Object obj) throws IOException {
        JsonNode node = objectMapper.readTree(jsonResult);
        JsonNode data = node.path("result").path("data");
        if (data.isObject()) {
            objectMapper.readerForUpdating(obj).readValue(data);
        } else {
            logger.warn("Data returned by LG API to get energy state is not present. Result:{}", node.toPrettyString());
        }
    }

    @Override
    public ExtendedDeviceInfo getExtendedDeviceInfo(String bridgeName, String deviceId) throws LGThinqApiException {
        ExtendedDeviceInfo info = new ExtendedDeviceInfo();
        try {
            ObjectNode dataList = JsonNodeFactory.instance.objectNode();
            dataList.put("dataGetList", (Integer) null);
            dataList.put("dataSetList", (Integer) null);

            RestResult resp = sendCommand(bridgeName, deviceId, "control-sync", "energyStateCtrl", "Get",
                    "airState.energy.totalCurrent", "null", dataList);
            handleGenericErrorResult(resp);
            readDataResultNodeToObject(resp.getJsonResponse(), info);

            ObjectNode dataGetList = JsonNodeFactory.instance.objectNode();
            dataGetList.putArray("dataGetList").add("airState.filterMngStates.useTime")
                    .add("airState.filterMngStates.maxTime");
            resp = sendCommand(bridgeName, deviceId, "control-sync", "filterMngStateCtrl", "Get", null, null,
                    dataGetList);
            handleGenericErrorResult(resp);
            readDataResultNodeToObject(resp.getJsonResponse(), info);

            return info;
        } catch (LGThinqApiException e) {
            throw e;
        } catch (Exception e) {
            throw new LGThinqApiException("Error sending command to LG API: " + e.getMessage(), e);
        }
    }
}
