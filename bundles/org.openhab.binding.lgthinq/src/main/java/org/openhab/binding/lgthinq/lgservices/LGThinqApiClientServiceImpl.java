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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.internal.LGThinqBindingConstants;
import org.openhab.binding.lgthinq.internal.api.RestResult;
import org.openhab.binding.lgthinq.internal.api.RestUtils;
import org.openhab.binding.lgthinq.internal.api.TokenManager;
import org.openhab.binding.lgthinq.internal.api.TokenResult;
import org.openhab.binding.lgthinq.internal.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.internal.errors.LGThinqDeviceV1MonitorExpiredException;
import org.openhab.binding.lgthinq.internal.errors.LGThinqDeviceV1OfflineException;
import org.openhab.binding.lgthinq.internal.errors.RefreshTokenException;
import org.openhab.binding.lgthinq.lgservices.model.*;
import org.openhab.binding.lgthinq.lgservices.model.ac.ACSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The {@link LGThinqACApiV1ClientServiceImpl}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public abstract class LGThinqApiClientServiceImpl implements LGThinqApiClientService {
    private static final Logger logger = LoggerFactory.getLogger(LGThinqApiClientServiceImpl.class);
    protected final ObjectMapper objectMapper = new ObjectMapper();
    protected final TokenManager tokenManager;

    protected LGThinqApiClientServiceImpl() {
        this.tokenManager = TokenManager.getInstance();
    }

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
            TokenResult token = tokenManager.getValidRegisteredToken(bridgeName);
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
            TokenResult token = tokenManager.getValidRegisteredToken(bridgeName);
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

    @NonNull
    protected Map<String, Object> handleV1GenericErrorResult(@Nullable RestResult resp)
            throws LGThinqApiException, LGThinqDeviceV1OfflineException {
        Map<String, Object> metaResult;
        Map<String, Object> envelope = Collections.emptyMap();
        if (resp == null) {
            return envelope;
        }
        if (resp.getStatusCode() != 200) {
            logger.error("Error returned by LG Server API. The reason is:{}", resp.getJsonResponse());
            throw new LGThinqApiException(
                    String.format("Error returned by LG Server API. The reason is:%s", resp.getJsonResponse()));
        } else {
            try {
                metaResult = objectMapper.readValue(resp.getJsonResponse(), new TypeReference<>() {
                });
                envelope = (Map<String, Object>) metaResult.get("lgedmRoot");
                if (envelope == null) {
                    throw new LGThinqApiException(String.format(
                            "Unexpected json body returned (without root node lgedmRoot): %s", resp.getJsonResponse()));
                } else if (!"0000".equals(envelope.get("returnCd"))) {
                    logErrorResultCodeMessage((String) envelope.get("returnCd"));
                    if ("0106".equals(envelope.get("returnCd")) || "D".equals(envelope.get("deviceState"))) {
                        // Disconnected Device
                        throw new LGThinqDeviceV1OfflineException("Device is offline. No data available");
                    }
                    throw new LGThinqApiException(
                            String.format("Status error executing endpoint. resultCode must be 0000, but was:%s",
                                    metaResult.get("returnCd")));
                }
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Unknown error occurred deserializing json stream", e);
            }
        }
        return envelope;
    }

    public @Nullable Snapshot getMonitorData(@NonNull String bridgeName, @NonNull String deviceId,
            @NonNull String workId, DeviceTypes deviceType)
            throws LGThinqApiException, LGThinqDeviceV1MonitorExpiredException, IOException {
        TokenResult token = tokenManager.getValidRegisteredToken(bridgeName);
        UriBuilder builder = UriBuilder.fromUri(token.getGatewayInfo().getApiRootV1()).path(V1_MON_DATA_PATH);
        Map<String, String> headers = getCommonHeaders(token.getGatewayInfo().getLanguage(),
                token.getGatewayInfo().getCountry(), token.getAccessToken(), token.getUserInfo().getUserNumber());
        String jsonData = String.format("{\n" + "   \"lgedmRoot\":{\n" + "      \"workList\":[\n" + "         {\n"
                + "            \"deviceId\":\"%s\",\n" + "            \"workId\":\"%s\"\n" + "         }\n"
                + "      ]\n" + "   }\n" + "}", deviceId, workId);
        RestResult resp = RestUtils.postCall(builder.build().toURL().toString(), headers, jsonData);
        Map<String, Object> envelop = null;
        // to unify the same behaviour then V2, this method handle Offline Exception and return a dummy shot with
        // offline flag.
        try {
            envelop = handleV1GenericErrorResult(resp);
        } catch (LGThinqDeviceV1OfflineException e) {
            ACSnapshot shot = new ACSnapshot();
            shot.setOnline(false);
            return shot;
        }
        if (envelop.get("workList") != null
                && ((Map<String, Object>) envelop.get("workList")).get("returnData") != null) {
            Map<String, Object> workList = ((Map<String, Object>) envelop.get("workList"));
            if (!"0000".equals(workList.get("returnCode"))) {
                logErrorResultCodeMessage((String) workList.get("resultCode"));
                LGThinqDeviceV1MonitorExpiredException e = new LGThinqDeviceV1MonitorExpiredException(
                        String.format("Monitor for device %s has expired. Please, refresh the monitor.", deviceId));
                logger.warn("{}", e.getMessage());
                throw e;
            }

            String jsonMonDataB64 = (String) workList.get("returnData");
            String jsonMon = new String(Base64.getDecoder().decode(jsonMonDataB64));
            Snapshot shot = SnapshotFactory.getInstance().create(jsonMon, deviceType);
            shot.setOnline("E".equals(workList.get("deviceState")));
            return shot;
        } else {
            // no data available yet
            return null;
        }
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
    public Snapshot getDeviceData(@NonNull String bridgeName, @NonNull String deviceId) throws LGThinqApiException {
        Map<String, Object> deviceSettings = getDeviceSettings(bridgeName, deviceId);
        if (deviceSettings.get("snapshot") != null) {
            Map<String, Object> snapMap = (Map<String, Object>) deviceSettings.get("snapshot");
            if (logger.isDebugEnabled()) {
                try {
                    objectMapper.writeValue(new File(String.format(
                            LGThinqBindingConstants.THINQ_USER_DATA_FOLDER + File.separator + "thinq-%s-datatrace.json",
                            deviceId)), deviceSettings);
                } catch (IOException e) {
                    logger.error("Error saving data trace", e);
                }
            }
            if (snapMap == null) {
                // No snapshot value provided
                return null;
            }

            Snapshot shot = SnapshotFactory.getInstance().create(deviceSettings);
            shot.setOnline((Boolean) snapMap.get("online"));
            return shot;
        }
        return null;
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
        TokenResult token = tokenManager.getValidRegisteredToken(bridgeName);
        UriBuilder builder = UriBuilder.fromUri(token.getGatewayInfo().getApiRootV1()).path(V1_START_MON_PATH);
        Map<String, String> headers = getCommonHeaders(token.getGatewayInfo().getLanguage(),
                token.getGatewayInfo().getCountry(), token.getAccessToken(), token.getUserInfo().getUserNumber());
        String workerId = UUID.randomUUID().toString();
        String jsonData = String.format(" { \"lgedmRoot\" : {" + "\"cmd\": \"Mon\"," + "\"cmdOpt\": \"Start\","
                + "\"deviceId\": \"%s\"," + "\"workId\": \"%s\"" + "} }", deviceId, workerId);
        RestResult resp = RestUtils.postCall(builder.build().toURL().toString(), headers, jsonData);
        return Objects.requireNonNull((String) handleV1GenericErrorResult(resp).get("workId"),
                "Unexpected StartMonitor json result. Node 'workId' not present");
    }

    @Override
    public void stopMonitor(String bridgeName, String deviceId, String workId)
            throws LGThinqApiException, RefreshTokenException, IOException, LGThinqDeviceV1OfflineException {
        TokenResult token = tokenManager.getValidRegisteredToken(bridgeName);
        UriBuilder builder = UriBuilder.fromUri(token.getGatewayInfo().getApiRootV1()).path(V1_START_MON_PATH);
        Map<String, String> headers = getCommonHeaders(token.getGatewayInfo().getLanguage(),
                token.getGatewayInfo().getCountry(), token.getAccessToken(), token.getUserInfo().getUserNumber());
        String jsonData = String.format(" { \"lgedmRoot\" : {" + "\"cmd\": \"Mon\"," + "\"cmdOpt\": \"Stop\","
                + "\"deviceId\": \"%s\"," + "\"workId\": \"%s\"" + "} }", deviceId, workId);
        RestResult resp = RestUtils.postCall(builder.build().toURL().toString(), headers, jsonData);
        handleV1GenericErrorResult(resp);
    }
}
