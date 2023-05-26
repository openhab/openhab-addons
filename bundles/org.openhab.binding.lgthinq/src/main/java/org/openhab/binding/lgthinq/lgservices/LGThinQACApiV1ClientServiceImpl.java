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

import java.io.IOException;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    @Override
    public double getInstantPowerConsumption(@NonNull String bridgeName, @NonNull String deviceId)
            throws LGThinqApiException, IOException {
        // TODO
        return 0;
    }

    // // TODO - Analise this to get power consumption
    // @Nullable
    // private RestResult getConfigCommands(String bridgeName, String deviceId, String keyName) throws Exception {
    // TokenResult token = tokenManager.getValidRegisteredToken(bridgeName);
    // UriBuilder builder = UriBuilder.fromUri(token.getGatewayInfo().getApiRootV1()).path(V1_CONTROL_OP);
    // Map<String, String> headers = getCommonHeaders(token.getGatewayInfo().getLanguage(),
    // token.getGatewayInfo().getCountry(), token.getAccessToken(), token.getUserInfo().getUserNumber());
    //
    // String payload = String.format("{\n" + " \"lgedmRoot\":{\n" + " \"cmd\": \"Config\","
    // + " \"cmdOpt\": \"Get\"," + " \"value\": \"%s\"," + " \"deviceId\": \"%s\","
    // + " \"workId\": \"%s\"," + " \"data\": \"\"" + " }\n" + "}", keyName, deviceId,
    // UUID.randomUUID().toString());
    // return RestUtils.postCall(builder.build().toURL().toString(), headers, payload);
    // }

    @Override
    public void turnDevicePower(String bridgeName, String deviceId, DevicePowerState newPowerState)
            throws LGThinqApiException {
        try {
            RestResult resp = sendControlCommands(bridgeName, deviceId, "", "Control", "Set", "Operation",
                    "" + newPowerState.commandValue());
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
            RestResult resp = sendControlCommands(bridgeName, deviceId, "", "Control", "Set", modeName, modeOnOff);
            handleGenericErrorResult(resp);
        } catch (Exception e) {
            throw new LGThinqApiException("Error adjusting " + modeName + " mode", e);
        }
    }

    @Override
    public void changeOperationMode(String bridgeName, String deviceId, int newOpMode) throws LGThinqApiException {
        try {
            RestResult resp = sendControlCommands(bridgeName, deviceId, "", "Control", "Set", "OpMode", "" + newOpMode);
            handleGenericErrorResult(resp);
        } catch (Exception e) {
            throw new LGThinqApiException("Error adjusting operation mode", e);
        }
    }

    @Override
    public void changeFanSpeed(String bridgeName, String deviceId, int newFanSpeed) throws LGThinqApiException {
        try {
            RestResult resp = sendControlCommands(bridgeName, deviceId, "", "Control", "Set", "WindStrength",
                    "" + newFanSpeed);
            handleGenericErrorResult(resp);
        } catch (Exception e) {
            throw new LGThinqApiException("Error adjusting fan speed", e);
        }
    }

    @Override
    public void changeTargetTemperature(String bridgeName, String deviceId, ACTargetTmp newTargetTemp)
            throws LGThinqApiException {
        try {
            RestResult resp = sendControlCommands(bridgeName, deviceId, "", "Control", "Set", "TempCfg",
                    "" + newTargetTemp.commandValue());
            handleGenericErrorResult(resp);
        } catch (Exception e) {
            throw new LGThinqApiException("Error adjusting target temperature", e);
        }
    }
}
