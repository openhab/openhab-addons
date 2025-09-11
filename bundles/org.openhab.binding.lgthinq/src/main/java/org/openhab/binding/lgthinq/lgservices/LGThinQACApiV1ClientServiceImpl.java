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

import static org.openhab.binding.lgthinq.lgservices.LGServicesConstants.*;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.lgthinq.lgservices.api.RestResult;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.model.CapabilityDefinition;
import org.openhab.binding.lgthinq.lgservices.model.DevicePowerState;
import org.openhab.binding.lgthinq.lgservices.model.devices.ac.ACCanonicalSnapshot;
import org.openhab.binding.lgthinq.lgservices.model.devices.ac.ACCapability;
import org.openhab.binding.lgthinq.lgservices.model.devices.ac.ACTargetTmp;
import org.openhab.binding.lgthinq.lgservices.model.devices.ac.ExtendedDeviceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * The {@link LGThinQACApiV1ClientServiceImpl}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class LGThinQACApiV1ClientServiceImpl extends
        LGThinQAbstractApiV1ClientService<ACCapability, ACCanonicalSnapshot> implements LGThinQACApiClientService {

    private final Logger logger = LoggerFactory.getLogger(LGThinQACApiV1ClientServiceImpl.class);

    protected LGThinQACApiV1ClientServiceImpl(HttpClient httpClient) {
        super(ACCapability.class, ACCanonicalSnapshot.class, httpClient);
    }

    @Override
    protected boolean beforeGetDataDevice(String bridgeName, String deviceId) {
        // there's no before settings to send command
        return false;
    }

    /**
     * Get snapshot data from the device.
     * <b>It works only for API V2 device versions!</b>
     *
     * @param deviceId device ID for de desired V2 LG Thinq.
     * @param capDef
     * @return return map containing metamodel of settings and snapshot
     */
    @Override
    @Nullable
    public ACCanonicalSnapshot getDeviceData(String bridgeName, String deviceId, CapabilityDefinition capDef) {
        throw new UnsupportedOperationException("Method not supported in V1 API device.");
    }

    private void readDataResultNodeToObject(String jsonResult, Object obj) throws IOException {
        JsonNode node = objectMapper.readTree(jsonResult);
        JsonNode data = node.path(LG_ROOT_TAG_V1).path("returnData");
        if (data.isTextual()) {
            // analyses if its b64 or not
            JsonNode format = node.path(LG_ROOT_TAG_V1).path("format");
            if ("B64".equals(format.textValue())) {
                String dataStr = new String(Base64.getDecoder().decode(data.textValue()));
                objectMapper.readerForUpdating(obj).readValue(dataStr);
            } else {
                objectMapper.readerForUpdating(obj).readValue(data.textValue());
            }
        } else {
            logger.warn("Data returned by LG API to get energy state is not present. Result:{}", node.toPrettyString());
        }
    }

    @Override
    public ExtendedDeviceInfo getExtendedDeviceInfo(String bridgeName, String deviceId) throws LGThinqApiException {
        ExtendedDeviceInfo info = new ExtendedDeviceInfo();
        try {
            RestResult resp = sendCommand(bridgeName, deviceId, LG_API_V1_CONTROL_OP, "Config", "Get", "",
                    "InOutInstantPower");
            handleGenericErrorResult(resp);
            readDataResultNodeToObject(resp.getJsonResponse(), info);

            resp = sendCommand(bridgeName, deviceId, LG_API_V1_CONTROL_OP, "Config", "Get", "", "Filter");
            handleGenericErrorResult(resp);
            readDataResultNodeToObject(resp.getJsonResponse(), info);

            return info;
        } catch (LGThinqApiException e) {
            throw e;
        } catch (Exception e) {
            throw new LGThinqApiException("Error sending command to LG API", e);
        }
    }

    @Override
    public void turnDevicePower(String bridgeName, String deviceId, DevicePowerState newPowerState)
            throws LGThinqApiException {
        try {
            RestResult resp = sendCommand(bridgeName, deviceId, "", "Control", "Set", "Operation",
                    String.valueOf(newPowerState.commandValue()));
            handleGenericErrorResult(resp);
        } catch (Exception e) {
            throw new LGThinqApiException("Error adjusting device power", e);
        }
    }

    @Override
    public void turnCoolJetMode(String bridgeName, String deviceId, String modeOnOff) throws LGThinqApiException {
        turnGenericMode(bridgeName, deviceId, "Jet", modeOnOff);
    }

    public void turnAirCleanMode(String bridgeName, String deviceId, String modeOnOff) throws LGThinqApiException {
        turnGenericMode(bridgeName, deviceId, "AirClean", modeOnOff);
    }

    public void turnAutoDryMode(String bridgeName, String deviceId, String modeOnOff) throws LGThinqApiException {
        turnGenericMode(bridgeName, deviceId, "AutoDry", modeOnOff);
    }

    public void turnEnergySavingMode(String bridgeName, String deviceId, String modeOnOff) throws LGThinqApiException {
        turnGenericMode(bridgeName, deviceId, "PowerSave", modeOnOff);
    }

    protected void turnGenericMode(String bridgeName, String deviceId, String modeName, String modeOnOff)
            throws LGThinqApiException {
        try {
            RestResult resp = sendCommand(bridgeName, deviceId, "", "Control", "Set", modeName, modeOnOff);
            handleGenericErrorResult(resp);
        } catch (Exception e) {
            throw new LGThinqApiException("Error adjusting " + modeName + " mode", e);
        }
    }

    @Override
    public void changeOperationMode(String bridgeName, String deviceId, int newOpMode) throws LGThinqApiException {
        try {
            RestResult resp = sendCommand(bridgeName, deviceId, "", "Control", "Set", "OpMode", "" + newOpMode);
            handleGenericErrorResult(resp);
        } catch (Exception e) {
            throw new LGThinqApiException("Error adjusting operation mode", e);
        }
    }

    @Override
    public void changeFanSpeed(String bridgeName, String deviceId, int newFanSpeed) throws LGThinqApiException {
        try {
            RestResult resp = sendCommand(bridgeName, deviceId, "", "Control", "Set", "WindStrength",
                    String.valueOf(newFanSpeed));
            handleGenericErrorResult(resp);
        } catch (Exception e) {
            throw new LGThinqApiException("Error adjusting fan speed", e);
        }
    }

    @Override
    public void changeStepUpDown(String bridgeName, String deviceId, ACCanonicalSnapshot currentSnap, int newStep)
            throws LGThinqApiException {
        Map<@Nullable String, @Nullable Object> subModeFeatures = Map.of("Jet", currentSnap.getCoolJetMode().intValue(),
                "PowerSave", currentSnap.getEnergySavingMode().intValue(), "WDirVStep", newStep, "WDirHStep",
                (int) currentSnap.getStepLeftRightMode());
        try {
            RestResult resp = sendCommand(bridgeName, deviceId, "", "Control", "Set", subModeFeatures, null);
            handleGenericErrorResult(resp);
        } catch (Exception e) {
            throw new LGThinqApiException("Error stepUpDown", e);
        }
    }

    @Override
    public void changeStepLeftRight(String bridgeName, String deviceId, ACCanonicalSnapshot currentSnap, int newStep)
            throws LGThinqApiException {
        Map<@Nullable String, @Nullable Object> subModeFeatures = Map.of("Jet", currentSnap.getCoolJetMode().intValue(),
                "PowerSave", currentSnap.getEnergySavingMode().intValue(), "WDirVStep",
                (int) currentSnap.getStepUpDownMode(), "WDirHStep", newStep);
        try {
            RestResult resp = sendCommand(bridgeName, deviceId, "", "Control", "Set", subModeFeatures, null);
            handleGenericErrorResult(resp);
        } catch (Exception e) {
            throw new LGThinqApiException("Error stepUpDown", e);
        }
    }

    @Override
    public void changeTargetTemperature(String bridgeName, String deviceId, ACTargetTmp newTargetTemp)
            throws LGThinqApiException {
        try {
            RestResult resp = sendCommand(bridgeName, deviceId, "", "Control", "Set", "TempCfg",
                    String.valueOf(newTargetTemp.commandValue()));
            handleGenericErrorResult(resp);
        } catch (Exception e) {
            throw new LGThinqApiException("Error adjusting target temperature", e);
        }
    }
}
