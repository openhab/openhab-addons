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
package org.openhab.binding.lgthinq.lgapi;

import static org.openhab.binding.lgthinq.internal.LGThinqBindingConstants.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.internal.api.RestResult;
import org.openhab.binding.lgthinq.internal.api.RestUtils;
import org.openhab.binding.lgthinq.internal.api.TokenManager;
import org.openhab.binding.lgthinq.internal.api.TokenResult;
import org.openhab.binding.lgthinq.internal.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.internal.errors.LGThinqDeviceV1MonitorExpiredException;
import org.openhab.binding.lgthinq.internal.errors.LGThinqDeviceV1OfflineException;
import org.openhab.binding.lgthinq.internal.errors.RefreshTokenException;
import org.openhab.binding.lgthinq.lgapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The {@link LGThinqApiV2ClientServiceImpl}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class LGThinqApiV2ClientServiceImpl extends LGThinqApiClientServiceImpl {
    private static final LGThinqApiClientService instance;
    private static final Logger logger = LoggerFactory.getLogger(LGThinqApiV2ClientServiceImpl.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TokenManager tokenManager;

    static {
        instance = new LGThinqApiV2ClientServiceImpl();
    }

    private LGThinqApiV2ClientServiceImpl() {
        tokenManager = TokenManager.getInstance();
    }

    @Override
    protected TokenManager getTokenManager() {
        return tokenManager;
    }

    public static LGThinqApiClientService getInstance() {
        return instance;
    }

    private Map<String, String> getCommonV2Headers(String language, String country, String accessToken,
            String userNumber) {
        return getCommonHeaders(language, country, accessToken, userNumber);
    }

    /**
     * Get snapshot data from the device.
     * <b>It works only for API V2 device versions!</b>
     * 
     * @param deviceId device ID for de desired V2 LG Thinq.
     * @return return map containing metamodel of settings and snapshot
     * @throws LGThinqApiException if some communication error occur.
     */
    @Override
    @Nullable
    public ACSnapShot getAcDeviceData(@NonNull String bridgeName, @NonNull String deviceId) throws LGThinqApiException {
        Map<String, Object> deviceSettings = getDeviceSettings(bridgeName, deviceId);
        if (deviceSettings.get("snapshot") != null) {
            Map<String, Object> snapMap = (Map<String, Object>) deviceSettings.get("snapshot");

            ACSnapShot shot = objectMapper.convertValue(snapMap, ACSnapShotV2.class);
            shot.setOnline((Boolean) snapMap.get("online"));
            return shot;
        }
        return null;
    }

    public RestResult sendControlCommands(String bridgeName, String deviceId, String command, String keyName, int value)
            throws Exception {
        TokenResult token = tokenManager.getValidRegisteredToken(bridgeName);
        UriBuilder builder = UriBuilder.fromUri(token.getGatewayInfo().getApiRootV2())
                .path(String.format(V2_CTRL_DEVICE_CONFIG_PATH, deviceId));
        Map<String, String> headers = getCommonV2Headers(token.getGatewayInfo().getLanguage(),
                token.getGatewayInfo().getCountry(), token.getAccessToken(), token.getUserInfo().getUserNumber());
        String payload = String.format("{\n" + "\"ctrlKey\": \"basicCtrl\",\n" + "\"command\": \"%s\",\n"
                + "\"dataKey\": \"%s\",\n" + "\"dataValue\": %d}", command, keyName, value);
        return RestUtils.postCall(builder.build().toURL().toString(), headers, payload);
    }

    @Override
    public void turnDevicePower(String bridgeName, String deviceId, DevicePowerState newPowerState)
            throws LGThinqApiException {
        try {
            RestResult resp = sendControlCommands(bridgeName, deviceId, "Operation", "airState.operation",
                    newPowerState.commandValue());
            handleV2GenericErrorResult(resp);
        } catch (Exception e) {
            throw new LGThinqApiException("Error adjusting device power", e);
        }
    }

    @Override
    public void changeOperationMode(String bridgeName, String deviceId, int newOpMode) throws LGThinqApiException {
        try {
            RestResult resp = sendControlCommands(bridgeName, deviceId, "Set", "airState.opMode", newOpMode);
            handleV2GenericErrorResult(resp);
        } catch (LGThinqApiException e) {
            throw e;
        } catch (Exception e) {
            throw new LGThinqApiException("Error adjusting operation mode", e);
        }
    }

    @Override
    public void changeFanSpeed(String bridgeName, String deviceId, int newFanSpeed) throws LGThinqApiException {
        try {
            RestResult resp = sendControlCommands(bridgeName, deviceId, "Set", "airState.windStrength", newFanSpeed);
            handleV2GenericErrorResult(resp);
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
            RestResult resp = sendControlCommands(bridgeName, deviceId, "Set", "airState.tempState.target",
                    newTargetTemp.commandValue());
            handleV2GenericErrorResult(resp);
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
     * @throws LGThinqApiException If some communication error occur.
     */
    @Override
    public String startMonitor(String bridgeName, String deviceId)
            throws LGThinqApiException, LGThinqDeviceV1OfflineException, IOException {
        throw new UnsupportedOperationException("Not supported in V2 API.");
    }

    @Override
    @NonNull
    @SuppressWarnings("ignoring Map type check")
    public ACCapability getDeviceCapability(String deviceId, String uri, boolean forceRecreate) throws LGThinqApiException {
        try {
            File regFile = new File(String.format(BASE_CAP_CONFIG_DATA_FILE, deviceId));
            ACCapability acCap = new ACCapability();
            Map<String, Object> mapper;
            if (regFile.isFile() || forceRecreate) {
                try (InputStream in = new URL(uri).openStream()) {
                    Files.copy(in, regFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
            mapper = objectMapper.readValue(regFile, new TypeReference<>() {
            });
            Map<String, Object> cap = (Map<String, Object>) mapper.get("Value");
            if (cap == null) {
                throw new LGThinqApiException("Error extracting capabilities supported by the device");
            }

            Map<String, Object> opModes = (Map<String, Object>) cap.get("airState.opMode");
            if (opModes == null) {
                throw new LGThinqApiException("Error extracting opModes supported by the device");
            } else {
                Map<String, String> modes = new HashMap<String, String>();
                ((Map<String, String>) opModes.get("value_mapping")).forEach((k, v) -> {
                    modes.put(v, k);
                });
                acCap.setOpMod(modes);
            }
            Map<String, Object> fanSpeed = (Map<String, Object>) cap.get("airState.windStrength");
            if (fanSpeed == null) {
                throw new LGThinqApiException("Error extracting fanSpeed supported by the device");
            } else {
                Map<String, String> fanModes = new HashMap<String, String>();
                ((Map<String, String>) fanSpeed.get("value_mapping")).forEach((k, v) -> {
                    fanModes.put(v, k);
                });
                acCap.setFanSpeed(fanModes);

            }
            // Set supported modes for the device
            Map<String, Map<String, String>> supOpModes = (Map<String, Map<String, String>>) cap
                    .get("support.airState.opMode");
            acCap.setSupportedOpMode(new ArrayList<>(supOpModes.get("value_mapping").values()));
            acCap.getSupportedOpMode().remove("@NON");
            Map<String, Map<String, String>> supFanSpeeds = (Map<String, Map<String, String>>) cap
                    .get("support.airState.windStrength");
            acCap.setSupportedFanSpeed(new ArrayList<>(supFanSpeeds.get("value_mapping").values()));
            acCap.getSupportedFanSpeed().remove("@NON");
            return acCap;
        } catch (IOException e) {
            throw new LGThinqApiException("Error reading IO interface", e);
        }
    }

    private void handleV2GenericErrorResult(@Nullable RestResult resp) throws LGThinqApiException {
        Map<String, Object> metaResult;
        if (resp == null) {
            return;
        }
        if (resp.getStatusCode() != 200) {
            logger.error("Error returned by LG Server API. The reason is:{}", resp.getJsonResponse());
            throw new LGThinqApiException(
                    String.format("Error returned by LG Server API. The reason is:%s", resp.getJsonResponse()));
        } else {
            try {
                metaResult = objectMapper.readValue(resp.getJsonResponse(), new TypeReference<Map<String, Object>>() {
                });
                if (!"0000".equals(metaResult.get("resultCode"))) {
                    throw new LGThinqApiException(
                            String.format("Status error executing endpoint. resultCode must be 0000, but was:%s",
                                    metaResult.get("resultCode")));
                }
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Unknown error occurred deserializing json stream", e);
            }

        }
    }

    private Map<String, Object> handleDeviceSettingsResult(RestResult resp) throws LGThinqApiException {
        return genericHandleDeviceSettingsResult(resp, logger, objectMapper);
    }

    @Override
    public void stopMonitor(String bridgeName, String deviceId, String workId)
            throws LGThinqApiException, RefreshTokenException, IOException, LGThinqDeviceV1OfflineException {
        throw new UnsupportedOperationException("Not supported in V2 API.");
    }

    @Override
    @Nullable
    public ACSnapShot getMonitorData(@NonNull String bridgeName, @NonNull String deviceId, @NonNull String workId)
            throws LGThinqApiException, LGThinqDeviceV1MonitorExpiredException, IOException {
        throw new UnsupportedOperationException("Not supported in V2 API.");
    }
}
