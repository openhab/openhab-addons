/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import java.io.IOException;
import java.util.*;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.api.RestResult;
import org.openhab.binding.lgthinq.api.RestUtils;
import org.openhab.binding.lgthinq.api.TokenManager;
import org.openhab.binding.lgthinq.api.TokenResult;
import org.openhab.binding.lgthinq.errors.LGApiException;
import org.openhab.binding.lgthinq.errors.LGDeviceV1OfflineException;
import org.openhab.binding.lgthinq.errors.RefreshTokenException;
import org.openhab.binding.lgthinq.lgapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The {@link LGApiV2ClientServiceImpl}
 *
 * @author Nemer Daud - Initial contribution
 */
public class LGApiV2ClientServiceImpl implements LGApiClientService {
    private static final LGApiClientService instance;
    private static final Logger logger = LoggerFactory.getLogger(LGApiV2ClientServiceImpl.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TokenManager tokenManager;

    static {
        instance = new LGApiV2ClientServiceImpl();
    }

    private LGApiV2ClientServiceImpl() {
        tokenManager = TokenManager.getInstance();
    }

    public static LGApiClientService getInstance() {
        return instance;
    }

    private Map<String, String> getCommonV2Headers(String language, String country, String accessToken,
            String userNumber) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("Content-type", "application/json;charset=UTF-8");
        headers.put("x-api-key", V2_API_KEY);
        headers.put("x-client-id", V2_CLIENT_ID);
        headers.put("x-country-code", country);
        headers.put("x-language-code", language);
        headers.put("x-message-id", UUID.randomUUID().toString());
        headers.put("x-service-code", SVC_CODE);
        headers.put("x-service-phase", V2_SVC_PHASE);
        headers.put("x-thinq-app-level", V2_APP_LEVEL);
        headers.put("x-thinq-app-os", V2_APP_OS);
        headers.put("x-thinq-app-type", V2_APP_TYPE);
        headers.put("x-thinq-app-ver", V2_APP_VER);
        headers.put("x-thinq-security-key", SECURITY_KEY);
        if (accessToken != null && !accessToken.isBlank())
            headers.put("x-emp-token", accessToken);
        if (userNumber != null && !userNumber.isBlank())
            headers.put("x-user-no", userNumber);
        return headers;
    }

    /**
     * Even using V2 URL, this endpoint support grab informations about account devices from V1 and V2.
     * 
     * @return list os LG Devices.
     * @throws LGApiException if some communication error occur.
     */
    @Override
    public List<LGDevice> listAccountDevices() throws LGApiException {
        try {
            TokenResult token = tokenManager.getValidRegisteredToken();
            UriBuilder builder = UriBuilder.fromUri(token.getGatewayInfo().getApiRootV2()).path(V2_LS_PATH);
            Map<String, String> headers = getCommonV2Headers(token.getGatewayInfo().getLanguage(),
                    token.getGatewayInfo().getCountry(), token.getAccessToken(), token.getUserInfo().getUserNumber());
            RestResult resp = RestUtils.getCall(builder.build().toURL().toString(), headers, null);
            return handleListAccountDevicesResult(resp);
        } catch (Exception e) {
            throw new LGApiException("Erros list account devices from LG Server API", e);
        }
    }

    /**
     * Get device settings and snapshot for a specific device.
     * <b>It works only for API V2 device versions!</b>
     * 
     * @param deviceId device ID for de desired V2 LG Thinq.
     * @return return map containing metamodel of settings and snapshot
     * @throws LGApiException if some communication error occur.
     */
    @Override
    public Map<String, Object> getDeviceSettings(String deviceId) throws LGApiException {
        try {
            TokenResult token = tokenManager.getValidRegisteredToken();
            UriBuilder builder = UriBuilder.fromUri(token.getGatewayInfo().getApiRootV2())
                    .path(String.format("%s/%s", V2_DEVICE_CONFIG_PATH, deviceId));
            Map<String, String> headers = getCommonV2Headers(token.getGatewayInfo().getLanguage(),
                    token.getGatewayInfo().getCountry(), token.getAccessToken(), token.getUserInfo().getUserNumber());
            RestResult resp = RestUtils.getCall(builder.build().toURL().toString(), headers, null);
            return handleDeviceSettingsResult(resp);
        } catch (Exception e) {
            throw new LGApiException("Erros list account devices from LG Server API", e);
        }
    }

    /**
     * Get snapshot data from the device.
     * <b>It works only for API V2 device versions!</b>
     * 
     * @param deviceId device ID for de desired V2 LG Thinq.
     * @return return map containing metamodel of settings and snapshot
     * @throws LGApiException if some communication error occur.
     */
    @Override
    public ACSnapShot getAcDeviceData(String deviceId) throws LGApiException {
        Map<String, Object> deviceSettings = getDeviceSettings(deviceId);
        if (deviceSettings != null && deviceSettings.get("snapshot") != null) {
            Map<String, Object> snapMap = (Map<String, Object>) deviceSettings.get("snapshot");

            ACSnapShot shot = objectMapper.convertValue(snapMap, ACSnapShotV2.class);
            shot.setOnline((Boolean) snapMap.get("online"));
            return shot;
        }
        return null;
    }

    public RestResult sendControlCommands(String deviceId, String command, String keyName, int value) throws Exception {
        TokenResult token = tokenManager.getValidRegisteredToken();
        UriBuilder builder = UriBuilder.fromUri(token.getGatewayInfo().getApiRootV2())
                .path(String.format(V2_CTRL_DEVICE_CONFIG_PATH, deviceId));
        Map<String, String> headers = getCommonV2Headers(token.getGatewayInfo().getLanguage(),
                token.getGatewayInfo().getCountry(), token.getAccessToken(), token.getUserInfo().getUserNumber());
        String payload = String.format("{\n" + "\"ctrlKey\": \"basicCtrl\",\n" + "\"command\": \"%s\",\n"
                + "\"dataKey\": \"%s\",\n" + "\"dataValue\": %d}", command, keyName, value);
        return RestUtils.postCall(builder.build().toURL().toString(), headers, payload);
    }

    @Override
    public boolean turnDevicePower(String deviceId, DevicePowerState newPowerState) throws LGApiException {
        try {
            RestResult resp = sendControlCommands(deviceId, "Operation", "airState.operation",
                    newPowerState.commandValue());

            return handleV2GenericErrorResult(resp) != null;
        } catch (Exception e) {
            throw new LGApiException("Error adjusting device power", e);
        }
    }

    @Override
    public boolean changeOperationMode(String deviceId, ACOpMode newOpMode) throws LGApiException {
        try {
            RestResult resp = sendControlCommands(deviceId, "Set", "airState.opMode", newOpMode.commandValue());
            return handleV2GenericErrorResult(resp) != null;
        } catch (LGApiException e) {
            throw e;
        } catch (Exception e) {
            throw new LGApiException("Error adjusting operation mode", e);
        }
    }

    @Override
    public boolean changeFanSpeed(String deviceId, ACFanSpeed newFanSpeed) throws LGApiException {
        try {
            RestResult resp = sendControlCommands(deviceId, "Set", "airState.windStrength", newFanSpeed.commandValue());
            return handleV2GenericErrorResult(resp) != null;
        } catch (LGApiException e) {
            throw e;
        } catch (Exception e) {
            throw new LGApiException("Error adjusting operation mode", e);
        }
    }

    @Override
    public boolean changeTargetTemperature(String deviceId, ACTargetTmp newTargetTemp) throws LGApiException {
        try {
            RestResult resp = sendControlCommands(deviceId, "Set", "airState.tempState.target",
                    newTargetTemp.commandValue());
            return handleV2GenericErrorResult(resp) != null;
        } catch (LGApiException e) {
            throw e;
        } catch (Exception e) {
            throw new LGApiException("Error adjusting operation mode", e);
        }
    }

    /**
     * Start monitor data form specific device. This is old one, <b>works only on V1 API supported devices</b>.
     * 
     * @param deviceId Device ID
     * @return Work1 to be uses to grab data during monitoring.
     * @throws LGApiException If some communication error occur.
     */
    @Override
    public String startMonitor(String deviceId) throws LGApiException, LGDeviceV1OfflineException, IOException {
        throw new UnsupportedOperationException("Not supported in V2 API.");
    }

    private Map<String, Object> handleV2GenericErrorResult(@Nullable RestResult resp) throws LGApiException {
        Map<String, Object> metaResult;
        if (resp == null) {
            return null;
        }
        if (resp.getStatusCode() != 200) {
            logger.error("Error returned by LG Server API. The reason is:{}", resp.getJsonResponse());
            throw new LGApiException(
                    String.format("Error returned by LG Server API. The reason is:%s", resp.getJsonResponse()));
        } else {
            try {
                metaResult = objectMapper.readValue(resp.getJsonResponse(), new TypeReference<Map<String, Object>>() {
                });
                if (!"0000".equals(metaResult.get("resultCode"))) {
                    throw new LGApiException(
                            String.format("Status error executing endpoint. resultCode must be 0000, but was:%s",
                                    metaResult.get("resultCode")));
                }
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Unknown error occurred deserializing json stream", e);
            }

        }
        return (Map<String, Object>) metaResult.get("result");
    }

    private Map<String, Object> handleDeviceSettingsResult(RestResult resp) throws LGApiException {
        Map<String, Object> deviceSettings;
        if (resp.getStatusCode() != 200) {
            logger.error("Error calling device settings from LG Server API. The reason is:{}", resp.getJsonResponse());
            throw new LGApiException(String.format("Error calling device settings from LG Server API. The reason is:%s",
                    resp.getJsonResponse()));
        } else {
            try {
                deviceSettings = objectMapper.readValue(resp.getJsonResponse(),
                        new TypeReference<Map<String, Object>>() {
                        });
                if (!"0000".equals(deviceSettings.get("resultCode"))) {
                    throw new LGApiException(
                            String.format("Status error getting device list. resultCode must be 0000, but was:%s",
                                    deviceSettings.get("resultCode")));
                }
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Unknown error occurred deserializing json stream", e);
            }

        }
        return (Map<String, Object>) deviceSettings.get("result");
    }

    private List<LGDevice> handleListAccountDevicesResult(RestResult resp) throws LGApiException {
        Map<String, Object> devicesResult;
        List<LGDevice> devices;
        if (resp.getStatusCode() != 200) {
            logger.error("Error calling device list from LG Server API. The reason is:{}", resp.getJsonResponse());
            throw new LGApiException(String.format("Error calling device list from LG Server API. The reason is:%s",
                    resp.getJsonResponse()));
        } else {
            try {
                devicesResult = objectMapper.readValue(resp.getJsonResponse(),
                        new TypeReference<Map<String, Object>>() {
                        });
                if (!"0000".equals(devicesResult.get("resultCode"))) {
                    throw new LGApiException(
                            String.format("Status error getting device list. resultCode must be 0000, but was:%s",
                                    devicesResult.get("resultCode")));
                }
                List<Map<String, Object>> items = (List<Map<String, Object>>) ((Map<String, Object>) devicesResult
                        .get("result")).get("item");
                devices = objectMapper.convertValue(items, new TypeReference<List<LGDevice>>() {
                });
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Unknown error occurred deserializing json stream.", e);
            }

        }

        return devices;
    }

    @Override
    public void stopMonitor(String deviceId, String workId)
            throws LGApiException, RefreshTokenException, IOException, LGDeviceV1OfflineException {
        throw new UnsupportedOperationException("Not supported in V2 API.");
    }

    @Override
    public ACSnapShot getMonitorData(String deviceId, String workId)
            throws LGApiException, IOException, RefreshTokenException {
        throw new UnsupportedOperationException("Not supported in V2 API.");
    }
}
