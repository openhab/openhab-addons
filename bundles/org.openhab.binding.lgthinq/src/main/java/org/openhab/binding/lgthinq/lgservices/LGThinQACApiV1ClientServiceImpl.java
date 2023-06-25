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
package org.openhab.binding.lgthinq.lgservices;

import static org.openhab.binding.lgthinq.internal.LGThinQBindingConstants.V1_CONTROL_OP;
import static org.openhab.binding.lgthinq.internal.api.LGThinqCanonicalModelUtil.LG_ROOT_TAG_V1;

import java.io.IOException;
import java.util.Base64;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.internal.api.RestResult;
import org.openhab.binding.lgthinq.internal.errors.LGThinqApiException;
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
    private static final LGThinQACApiClientService instance;
    private static final Logger logger = LoggerFactory.getLogger(LGThinQACApiV1ClientServiceImpl.class);

    static {
        instance = new LGThinQACApiV1ClientServiceImpl(ACCapability.class, ACCanonicalSnapshot.class);
    }

    protected LGThinQACApiV1ClientServiceImpl(Class<ACCapability> capabilityClass,
            Class<ACCanonicalSnapshot> snapshotClass) {
        super(capabilityClass, snapshotClass);
    }

    @Override
    protected void beforeGetDataDevice(@NonNull String bridgeName, @NonNull String deviceId) {
        // Nothing to do on V1 ACCapability here
    }

    public static LGThinQACApiClientService getInstance() {
        return instance;
    }

    /**
     * Get snapshot data from the device.
     * <b>It works only for API V2 device versions!</b>
     *
     * @param deviceId device ID for de desired V2 LG Thinq.
     * @param capDef
     * @return return map containing metamodel of settings and snapshot
     * @throws LGThinqApiException if some communication error occur.
     */
    @Override
    @Nullable
    public ACCanonicalSnapshot getDeviceData(@NonNull String bridgeName, @NonNull String deviceId,
            @NonNull CapabilityDefinition capDef) throws LGThinqApiException {
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
    public ExtendedDeviceInfo getExtendedDeviceInfo(@NonNull String bridgeName, @NonNull String deviceId)
            throws LGThinqApiException {
        ExtendedDeviceInfo info = new ExtendedDeviceInfo();
        try {
            RestResult resp = sendCommand(bridgeName, deviceId, V1_CONTROL_OP, "Config", "Get", "",
                    "InOutInstantPower");
            handleGenericErrorResult(resp);
            readDataResultNodeToObject(resp.getJsonResponse(), info);

            resp = sendCommand(bridgeName, deviceId, V1_CONTROL_OP, "Config", "Get", "", "Filter");
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
