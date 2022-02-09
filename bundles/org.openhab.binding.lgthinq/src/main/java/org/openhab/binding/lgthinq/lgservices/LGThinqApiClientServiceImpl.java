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
package org.openhab.binding.lgthinq.lgservices;

import static org.openhab.binding.lgthinq.internal.LGThinqBindingConstants.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.internal.api.RestResult;
import org.openhab.binding.lgthinq.internal.api.RestUtils;
import org.openhab.binding.lgthinq.internal.api.TokenManager;
import org.openhab.binding.lgthinq.internal.api.TokenResult;
import org.openhab.binding.lgthinq.internal.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.model.Capability;
import org.openhab.binding.lgthinq.lgservices.model.CapabilityFactory;
import org.openhab.binding.lgthinq.lgservices.model.LGDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The {@link LGThinqApiV1ClientServiceImpl}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public abstract class LGThinqApiClientServiceImpl implements LGThinqApiClientService {
    private static final Logger logger = LoggerFactory.getLogger(LGThinqApiClientServiceImpl.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    protected abstract TokenManager getTokenManager();

    static Map<String, String> getCommonHeaders(String language, String country, String accessToken,
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
        if (!accessToken.isBlank())
            headers.put("x-emp-token", accessToken);
        if (!userNumber.isBlank())
            headers.put("x-user-no", userNumber);
        return headers;
    }

    /**
     * Even using V2 URL, this endpoint support grab informations about account devices from V1 and V2.
     * 
     * @return list os LG Devices.
     * @throws LGThinqApiException if some communication error occur.
     */
    @Override
    public List<LGDevice> listAccountDevices(String bridgeName) throws LGThinqApiException {
        try {
            TokenResult token = getTokenManager().getValidRegisteredToken(bridgeName);
            UriBuilder builder = UriBuilder.fromUri(token.getGatewayInfo().getApiRootV2()).path(V2_LS_PATH);
            Map<String, String> headers = getCommonHeaders(token.getGatewayInfo().getLanguage(),
                    token.getGatewayInfo().getCountry(), token.getAccessToken(), token.getUserInfo().getUserNumber());
            RestResult resp = RestUtils.getCall(builder.build().toURL().toString(), headers, null);
            return handleListAccountDevicesResult(resp);
        } catch (Exception e) {
            throw new LGThinqApiException("Erros list account devices from LG Server API", e);
        }
    }

    @Override
    public File loadDeviceCapability(String deviceId, String uri, boolean forceRecreate) throws LGThinqApiException {
        File regFile = new File(String.format(BASE_CAP_CONFIG_DATA_FILE, deviceId));
        try {
            if (regFile.isFile() || forceRecreate) {
                try (InputStream in = new URL(uri).openStream()) {
                    Files.copy(in, regFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException e) {
            throw new LGThinqApiException("Error reading IO interface", e);
        }
        return regFile;
    }

    /**
     * Get device settings and snapshot for a specific device.
     * <b>It works only for API V2 device versions!</b>
     * 
     * @param deviceId device ID for de desired V2 LG Thinq.
     * @return return map containing metamodel of settings and snapshot
     * @throws LGThinqApiException if some communication error occur.
     */
    @Override
    public Map<String, Object> getDeviceSettings(String bridgeName, String deviceId) throws LGThinqApiException {
        try {
            TokenResult token = getTokenManager().getValidRegisteredToken(bridgeName);
            UriBuilder builder = UriBuilder.fromUri(token.getGatewayInfo().getApiRootV2())
                    .path(String.format("%s/%s", V2_DEVICE_CONFIG_PATH, deviceId));
            Map<String, String> headers = getCommonHeaders(token.getGatewayInfo().getLanguage(),
                    token.getGatewayInfo().getCountry(), token.getAccessToken(), token.getUserInfo().getUserNumber());
            RestResult resp = RestUtils.getCall(builder.build().toURL().toString(), headers, null);
            return handleDeviceSettingsResult(resp);
        } catch (Exception e) {
            throw new LGThinqApiException("Erros list account devices from LG Server API", e);
        }
    }

    private Map<String, Object> handleDeviceSettingsResult(RestResult resp) throws LGThinqApiException {
        return genericHandleDeviceSettingsResult(resp, logger, objectMapper);
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> genericHandleDeviceSettingsResult(RestResult resp, Logger logger,
            ObjectMapper objectMapper) throws LGThinqApiException {
        Map<String, Object> deviceSettings;
        if (resp.getStatusCode() != 200) {
            logger.error("Error calling device settings from LG Server API. The reason is:{}", resp.getJsonResponse());
            throw new LGThinqApiException(String.format(
                    "Error calling device settings from LG Server API. The reason is:%s", resp.getJsonResponse()));
        } else {
            try {
                deviceSettings = objectMapper.readValue(resp.getJsonResponse(), new TypeReference<>() {
                });
                if (!"0000".equals(deviceSettings.get("resultCode"))) {
                    logErrorResultCodeMessage((String) deviceSettings.get("resultCode"));
                    throw new LGThinqApiException(
                            String.format("Status error getting device list. resultCode must be 0000, but was:%s",
                                    deviceSettings.get("resultCode")));
                }
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Unknown error occurred deserializing json stream", e);
            }

        }
        return Objects.requireNonNull((Map<String, Object>) deviceSettings.get("result"),
                "Unexpected json result asking for Device Settings. Node 'result' no present");
    }

    @SuppressWarnings("unchecked")
    private List<LGDevice> handleListAccountDevicesResult(RestResult resp) throws LGThinqApiException {
        Map<String, Object> devicesResult;
        List<LGDevice> devices;
        if (resp.getStatusCode() != 200) {
            logger.error("Error calling device list from LG Server API. The reason is:{}", resp.getJsonResponse());
            throw new LGThinqApiException(String
                    .format("Error calling device list from LG Server API. The reason is:%s", resp.getJsonResponse()));
        } else {
            try {
                devicesResult = objectMapper.readValue(resp.getJsonResponse(), new TypeReference<>() {
                });
                if (!"0000".equals(devicesResult.get("resultCode"))) {
                    logErrorResultCodeMessage((String) devicesResult.get("resultCode"));
                    throw new LGThinqApiException(
                            String.format("Status error getting device list. resultCode must be 0000, but was:%s",
                                    devicesResult.get("resultCode")));
                }
                List<Map<String, Object>> items = (List<Map<String, Object>>) ((Map<String, Object>) devicesResult
                        .get("result")).get("item");
                devices = objectMapper.convertValue(items, new TypeReference<>() {
                });
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Unknown error occurred deserializing json stream.", e);
            }

        }

        return devices;
    }

    protected static void logErrorResultCodeMessage(@Nullable String resultCode) {
        if (resultCode == null) {
            return;
        }
        String errMessage = ERROR_CODE_RESPONSE.get(resultCode.trim());
        logger.error("LG API report error processing the request -> resultCode=[{}], message=[{}]", resultCode,
                errMessage == null ? "UNKNOW ERROR MESSAGE" : errMessage);
    }

    /**
     * Get capability em registry/cache on file for next consult
     *
     * @param deviceId ID of the device
     * @param uri URI of the config capability
     * @return return simplified capability
     * @throws LGThinqApiException If some error occurr
     */
    public Capability getCapability(String deviceId, String uri, boolean forceRecreate) throws LGThinqApiException {
        try {
            File regFile = loadDeviceCapability(deviceId, uri, forceRecreate);
            Map<String, Object> mapper = objectMapper.readValue(regFile, new TypeReference<>() {
            });
            return CapabilityFactory.getInstance().create(mapper);
        } catch (IOException e) {
            throw new LGThinqApiException("Error reading IO interface", e);
        }
    }
}
