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

import static org.openhab.binding.lgthinq.internal.LGThinQBindingConstants.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.lgthinq.internal.LGThinQBindingConstants;
import org.openhab.binding.lgthinq.lgservices.api.RestResult;
import org.openhab.binding.lgthinq.lgservices.api.RestUtils;
import org.openhab.binding.lgthinq.lgservices.api.TokenManager;
import org.openhab.binding.lgthinq.lgservices.api.TokenResult;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqAccessException;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqDeviceV1MonitorExpiredException;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqDeviceV1OfflineException;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqException;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqUnmarshallException;
import org.openhab.binding.lgthinq.lgservices.model.AbstractSnapshotDefinition;
import org.openhab.binding.lgthinq.lgservices.model.CapabilityDefinition;
import org.openhab.binding.lgthinq.lgservices.model.CapabilityFactory;
import org.openhab.binding.lgthinq.lgservices.model.DevicePowerState;
import org.openhab.binding.lgthinq.lgservices.model.DeviceTypes;
import org.openhab.binding.lgthinq.lgservices.model.LGDevice;
import org.openhab.binding.lgthinq.lgservices.model.MonitoringResultFormat;
import org.openhab.binding.lgthinq.lgservices.model.ResultCodes;
import org.openhab.binding.lgthinq.lgservices.model.SnapshotBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The {@link LGThinQAbstractApiClientService} - base class for all LG API client service. It's provide commons methods
 * to communicate to the LG Cloud and exchange basic data.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("unchecked")
public abstract class LGThinQAbstractApiClientService<C extends CapabilityDefinition, S extends AbstractSnapshotDefinition>
        implements LGThinQApiClientService<C, S> {
    protected final ObjectMapper objectMapper = new ObjectMapper();
    protected final TokenManager tokenManager;
    protected final Class<C> capabilityClass;
    protected final Class<S> snapshotClass;
    protected final HttpClient httpClient;
    private final Logger logger = LoggerFactory.getLogger(LGThinQAbstractApiClientService.class);
    private final String clientId = "";

    protected LGThinQAbstractApiClientService(Class<C> capabilityClass, Class<S> snapshotClass, HttpClient httpClient) {
        this.httpClient = httpClient;
        this.tokenManager = new TokenManager(httpClient);
        this.capabilityClass = capabilityClass;
        this.snapshotClass = snapshotClass;
    }

    protected static String getErrorCodeMessage(@Nullable String code) {
        if (code == null) {
            return "";
        }
        ResultCodes resultCode = ResultCodes.fromCode(code);
        return resultCode.getDescription();
    }

    /**
     * Retrieves the client ID based on the provided user number.
     *
     * @param userNumber the user number to generate the client ID
     * @return the generated client ID
     */
    private String getClientId(String userNumber) {
        if (!clientId.isEmpty()) {
            return clientId;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String data = userNumber + Instant.now().toString();
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("SHA-256 algorithm not found", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    Map<String, String> getCommonHeaders(String language, String country, String accessToken, String userNumber) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("Content-type", "application/json;charset=UTF-8");
        headers.put("x-api-key", LG_API_V2_API_KEY);
        headers.put("x-app-version", "LG ThinQ/5.0.28271");
        headers.put("x-client-id", getClientId(userNumber));
        headers.put("x-country-code", country);
        headers.put("x-language-code", language);
        headers.put("x-message-id", UUID.randomUUID().toString());
        headers.put("x-service-code", LG_API_SVC_CODE);
        headers.put("x-service-phase", LG_API_V2_SVC_PHASE);
        headers.put("x-thinq-app-level", LG_API_V2_APP_LEVEL);
        headers.put("x-thinq-app-os", LG_API_V2_APP_OS);
        headers.put("x-thinq-app-type", LG_API_V2_APP_TYPE);
        headers.put("x-thinq-app-ver", LG_API_V2_APP_VER);
        headers.put("x-thinq-app-logintype", "LGE");
        headers.put("x-origin", "app-native");
        headers.put("x-device-type", "601");

        if (!accessToken.isBlank()) {
            headers.put("x-emp-token", accessToken);
        }
        if (!userNumber.isBlank()) {
            headers.put("x-user-no", userNumber);
        }
        return headers;
    }

    /**
     * Even using V2 URL, this endpoint support grab information about account devices from V1 and V2.
     *
     * @return list os LG Devices.
     * @throws LGThinqApiException if some communication error occur.
     */
    @Override
    public List<LGDevice> listAccountDevices(String bridgeName) throws LGThinqApiException {
        try {
            TokenResult token = tokenManager.getValidRegisteredToken(bridgeName);
            UriBuilder builder = UriBuilder.fromUri(token.getGatewayInfo().getApiRootV2()).path(LG_API_V2_LS_PATH);
            Map<String, String> headers = getCommonHeaders(token.getGatewayInfo().getLanguage(),
                    token.getGatewayInfo().getCountry(), token.getAccessToken(), token.getUserInfo().getUserNumber());
            RestResult resp = RestUtils.getCall(httpClient, builder.build().toURL().toString(), headers, null);
            return handleListAccountDevicesResult(resp);
        } catch (Exception e) {
            throw new LGThinqApiException("Error listing account devices from LG Server API", e);
        }
    }

    @Override
    public File loadDeviceCapability(String deviceId, String uri, boolean forceRecreate) throws LGThinqApiException {
        File regFile = new File(String.format(getBaseCapConfigDataFile(), deviceId));
        try {
            if (!regFile.isFile() || forceRecreate) {
                try (InputStream in = new URI(uri).toURL().openStream()) {
                    Files.copy(in, regFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException | URISyntaxException e) {
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
                    .path(String.format("%s/%s", LG_API_V2_DEVICE_CONFIG_PATH, deviceId));
            Map<String, String> headers = getCommonHeaders(token.getGatewayInfo().getLanguage(),
                    token.getGatewayInfo().getCountry(), token.getAccessToken(), token.getUserInfo().getUserNumber());
            RestResult resp = RestUtils.getCall(httpClient, builder.build().toURL().toString(), headers, null);
            return handleDeviceSettingsResult(resp);
        } catch (LGThinqException e) {
            throw e;
        } catch (Exception e) {
            throw new LGThinqApiException("Errors list account devices from LG Server API", e);
        }
    }

    private Map<String, Object> handleDeviceSettingsResult(RestResult resp) throws LGThinqApiException {
        return genericHandleDeviceSettingsResult(resp, objectMapper);
    }

    /**
     * Handles the result of device settings retrieved from an API call.
     *
     * @param resp The RestResult object containing the API response
     * @param objectMapper The ObjectMapper to convert JSON to Java objects
     * @return A Map containing the device settings
     * @throws LGThinqApiException If an error occurs during handling the device settings result
     */
    protected Map<String, Object> genericHandleDeviceSettingsResult(RestResult resp, ObjectMapper objectMapper)
            throws LGThinqApiException {
        Map<String, Object> deviceSettings;
        Map<String, String> respMap;
        String resultCode;
        if (resp.getStatusCode() != 200) {
            if (resp.getStatusCode() == 400) {
                logger.warn("Error calling device settings from LG Server API. HTTP Status: {}. The reason is: {}",
                        resp.getStatusCode(), ResultCodes.getReasonResponse(resp.getJsonResponse()));
                throw new LGThinqAccessException(String.format(
                        "Error calling device settings from LG Server API. HTTP Status: %d. The reason is: %s",
                        resp.getStatusCode(), ResultCodes.getReasonResponse(resp.getJsonResponse())));
            }
            try {
                respMap = objectMapper.readValue(resp.getJsonResponse(), new TypeReference<>() {
                });
                resultCode = respMap.get("resultCode");
                if (resultCode != null) {
                    throw new LGThinqApiException(String.format(
                            "Error calling device settings from LG Server API. The code is: %s and The reason is: %s",
                            resultCode, ResultCodes.fromCode(resultCode)));
                }
            } catch (JsonProcessingException e) {
                // This exception doesn't matter, it's because response is not in json format. Logging raw response.
                logger.trace(
                        "Error calling device settings from LG Server API. Response is not in json format. Ignoring...",
                        e);
            }
            throw new LGThinqApiException(String.format(
                    "Error calling device settings from LG Server API. The reason is:%s", resp.getJsonResponse()));
        } else {
            try {
                deviceSettings = objectMapper.readValue(resp.getJsonResponse(), new TypeReference<>() {
                });
                String code = Objects.requireNonNullElse((String) deviceSettings.get("resultCode"), "");
                if (!ResultCodes.OK.containsResultCode(code)) {
                    throw new LGThinqApiException(String.format(
                            "LG API report error processing the request -> resultCode=[{%s], message=[%s]", code,
                            getErrorCodeMessage(code)));
                }
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Unknown error occurred deserializing json stream", e);
            }
        }
        return Objects.requireNonNull((Map<String, Object>) deviceSettings.get("result"),
                "Unexpected json result asking for Device Settings. Node 'result' no present");
    }

    private List<LGDevice> handleListAccountDevicesResult(RestResult resp) throws LGThinqApiException {
        Map<String, Object> devicesResult;
        List<LGDevice> devices;
        if (resp.getStatusCode() != 200) {
            if (resp.getStatusCode() == 400) {
                logger.warn("Error calling device list from LG Server API. HTTP Status: {}. The reason is: {}",
                        resp.getStatusCode(), ResultCodes.getReasonResponse(resp.getJsonResponse()));
                return Collections.emptyList();
            }
            throw new LGThinqApiException(
                    String.format("Error calling device list from LG Server API. HTTP Status: %s. The reason is: %s",
                            resp.getStatusCode(), ResultCodes.getReasonResponse(resp.getJsonResponse())));
        } else {
            try {
                devicesResult = objectMapper.readValue(resp.getJsonResponse(), new TypeReference<>() {
                });
                String code = Objects.requireNonNullElse((String) devicesResult.get("resultCode"), "");
                if (!ResultCodes.OK.containsResultCode(code)) {
                    throw new LGThinqApiException(
                            String.format("LG API report error processing the request -> resultCode=[%s], message=[%s]",
                                    code, getErrorCodeMessage(code)));
                }
                List<Map<String, Object>> items = (List<Map<String, Object>>) ((Map<String, Object>) Objects
                        .requireNonNull(devicesResult.get("result"), "Not expected null here")).get("item");
                devices = objectMapper.convertValue(items, new TypeReference<>() {
                });
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Unknown error occurred deserializing json stream.", e);
            }
        }

        return devices;
    }

    /**
     * Get capability em registry/cache on file for next consult
     *
     * @param deviceId ID of the device
     * @param uri URI of the config capability
     * @return return simplified capability
     * @throws LGThinqApiException If some error occurr
     */
    public C getCapability(String deviceId, String uri, boolean forceRecreate) throws LGThinqApiException {
        try {
            File regFile = loadDeviceCapability(deviceId, uri, forceRecreate);
            JsonNode rootNode = objectMapper.readTree(regFile);
            return CapabilityFactory.getInstance().create(rootNode, capabilityClass);
        } catch (IOException e) {
            throw new LGThinqApiException("Error reading IO interface", e);
        } catch (LGThinqException e) {
            throw new LGThinqApiException("Error parsing capability registry", e);
        }
    }

    public S buildDefaultOfflineSnapshot() {
        try {
            // As I don't know the current device status, then I reset to default values.

            S shot = snapshotClass.getDeclaredConstructor().newInstance();
            shot.setPowerStatus(DevicePowerState.DV_POWER_OFF);
            shot.setOnline(false);
            return shot;
        } catch (Exception ex) {
            throw new IllegalStateException("Unexpected Error. The default constructor of this Snapshot wasn't found",
                    ex);
        }
    }

    public @Nullable S getMonitorData(String bridgeName, String deviceId, String workId, DeviceTypes deviceType,
            C deviceCapability) throws LGThinqApiException, LGThinqDeviceV1MonitorExpiredException, IOException,
            LGThinqUnmarshallException {
        TokenResult token = tokenManager.getValidRegisteredToken(bridgeName);
        UriBuilder builder = UriBuilder.fromUri(token.getGatewayInfo().getApiRootV1()).path(LG_API_V1_MON_DATA_PATH);
        Map<String, String> headers = getCommonHeaders(token.getGatewayInfo().getLanguage(),
                token.getGatewayInfo().getCountry(), token.getAccessToken(), token.getUserInfo().getUserNumber());
        String jsonData = String.format("{\n" + "   \"lgedmRoot\":{\n" + "      \"workList\":[\n" + "         {\n"
                + "            \"deviceId\":\"%s\",\n" + "            \"workId\":\"%s\"\n" + "         }\n"
                + "      ]\n" + "   }\n" + "}", deviceId, workId);
        RestResult resp = RestUtils.postCall(httpClient, builder.build().toURL().toString(), headers, jsonData);
        Map<String, Object> envelop;
        // to unify the same behaviour then V2, this method handle Offline Exception and return a dummy shot with
        // offline flag.
        try {
            envelop = handleGenericErrorResult(resp);
        } catch (LGThinqDeviceV1OfflineException e) {
            return buildDefaultOfflineSnapshot();
        }
        Map<String, Object> workList = objectMapper
                .convertValue(envelop.getOrDefault("workList", Collections.emptyMap()), new TypeReference<>() {
                });
        if (workList.get("returnData") != null) {
            if (logger.isDebugEnabled()) {
                try {
                    objectMapper.writeValue(new File(String.format(LGThinQBindingConstants.getThinqUserDataFolder()
                            + File.separator + "thinq-%s-datatrace.json", deviceId)), workList);
                } catch (IOException e) {
                    // Only debug since datatrace is a trace data.
                    logger.debug("Unexpected error saving data trace", e);
                }
            }

            if (!ResultCodes.OK.containsResultCode("" + workList.get("returnCode"))) {
                String code = Objects.requireNonNullElse((String) workList.get("returnCode"), "");
                logger.debug("LG API report error processing the request -> resultCode=[{}], message=[{}]", code,
                        getErrorCodeMessage(code));
                LGThinqDeviceV1MonitorExpiredException e = new LGThinqDeviceV1MonitorExpiredException(
                        String.format("Monitor for device %s has expired. Please, refresh the monitor.", deviceId));
                logger.warn("{}", e.getMessage());
                throw e;
            }

            String monDataB64 = (String) workList.get("returnData");
            String monData = new String(Base64.getDecoder().decode(monDataB64));
            S shot;
            try {
                if (MonitoringResultFormat.JSON_FORMAT.equals(deviceCapability.getMonitoringDataFormat())) {
                    shot = (S) SnapshotBuilderFactory.getInstance().getBuilder(snapshotClass).createFromJson(monData,
                            deviceType, deviceCapability);
                } else if (MonitoringResultFormat.BINARY_FORMAT.equals(deviceCapability.getMonitoringDataFormat())) {
                    shot = (S) SnapshotBuilderFactory.getInstance().getBuilder(snapshotClass).createFromBinary(monData,
                            deviceCapability.getMonitoringBinaryProtocol(), deviceCapability);
                } else {
                    throw new LGThinqApiException(String.format("Returned data format not supported: %s",
                            deviceCapability.getMonitoringDataFormat()));
                }
                shot.setOnline("E".equals(workList.get("deviceState")));
            } catch (LGThinqUnmarshallException ex) {
                // error in the monitor Data returned. Device is irresponsible
                logger.debug("Monitored data returned for the device {} is unreadable. Device is not connected",
                        deviceId);
                throw ex;
            }
            return shot;
        } else {
            // no data available yet
            return null;
        }
    }

    @Override
    public void initializeDevice(String bridgeName, String deviceId) throws LGThinqApiException {
        logger.debug("Initializing device [{}] from bridge [{}]", deviceId, bridgeName);
    }

    /**
     * Perform some routine before getting data device. Depending on the kind of the device, this is needed
     * to update or prepare some informations before go to get the data.
     *
     * @return false if the device doesn't support pre-condition commands
     */
    protected abstract boolean beforeGetDataDevice(String bridgeName, String deviceId);

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
    public S getDeviceData(String bridgeName, String deviceId, CapabilityDefinition capDef) throws LGThinqApiException {
        // Exec pre-conditions (normally ask for update monitoring sensors of the device - temp and power) before call
        // for data
        if (capDef.isBeforeCommandSupported() && !beforeGetDataDevice(bridgeName, deviceId)) {
            capDef.setBeforeCommandSupported(false);
        }

        Map<String, Object> deviceSettings = getDeviceSettings(bridgeName, deviceId);
        if (deviceSettings.get("snapshot") != null) {
            Map<String, Object> snapMap = (Map<String, Object>) deviceSettings.get("snapshot");
            if (logger.isDebugEnabled()) {
                try {
                    objectMapper.writeValue(new File(String.format(LGThinQBindingConstants.getThinqUserDataFolder()
                            + File.separator + "thinq-%s-datatrace.json", deviceId)), deviceSettings);
                } catch (IOException e) {
                    logger.debug("Error saving data trace", e);
                }
            }
            if (snapMap == null) {
                // No snapshot value provided
                return null;
            }
            S shot = (S) SnapshotBuilderFactory.getInstance().getBuilder(snapshotClass).createFromJson(deviceSettings,
                    capDef);
            shot.setOnline((Boolean) snapMap.getOrDefault("online", Boolean.FALSE));
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
    public String startMonitor(String bridgeName, String deviceId) throws LGThinqApiException, IOException {
        TokenResult token = tokenManager.getValidRegisteredToken(bridgeName);
        UriBuilder builder = UriBuilder.fromUri(token.getGatewayInfo().getApiRootV1()).path(LG_API_V1_START_MON_PATH);
        Map<String, String> headers = getCommonHeaders(token.getGatewayInfo().getLanguage(),
                token.getGatewayInfo().getCountry(), token.getAccessToken(), token.getUserInfo().getUserNumber());
        String workerId = UUID.randomUUID().toString();
        String jsonData = String.format(" { \"lgedmRoot\" : {" + "\"cmd\": \"Mon\"," + "\"cmdOpt\": \"Start\","
                + "\"deviceId\": \"%s\"," + "\"workId\": \"%s\"" + "} }", deviceId, workerId);
        RestResult resp = RestUtils.postCall(httpClient, builder.build().toURL().toString(), headers, jsonData);
        Map<String, Object> respMap = handleGenericErrorResult(resp);
        if (respMap.isEmpty()) {
            logger.debug(
                    "Unexpected StartMonitor json null result. Possible causes: 1) you are monitoring the device in LG App at same time, 2) temporary problems in the server. Try again later");
        }
        return Objects.requireNonNull((String) handleGenericErrorResult(resp).get("workId"),
                "Unexpected StartMonitor json result. Node 'workId' not present");
    }

    @Override
    public void stopMonitor(String bridgeName, String deviceId, String workId) throws LGThinqApiException, IOException {
        TokenResult token = tokenManager.getValidRegisteredToken(bridgeName);
        UriBuilder builder = UriBuilder.fromUri(token.getGatewayInfo().getApiRootV1()).path(LG_API_V1_START_MON_PATH);
        Map<String, String> headers = getCommonHeaders(token.getGatewayInfo().getLanguage(),
                token.getGatewayInfo().getCountry(), token.getAccessToken(), token.getUserInfo().getUserNumber());
        String jsonData = String.format(" { \"lgedmRoot\" : {" + "\"cmd\": \"Mon\"," + "\"cmdOpt\": \"Stop\","
                + "\"deviceId\": \"%s\"," + "\"workId\": \"%s\"" + "} }", deviceId, workId);
        RestResult resp = RestUtils.postCall(httpClient, builder.build().toURL().toString(), headers, jsonData);
        handleGenericErrorResult(resp);
    }

    protected Map<String, String> getCommonV2Headers(String language, String country, String accessToken,
            String userNumber) {
        return getCommonHeaders(language, country, accessToken, userNumber);
    }

    protected abstract RestResult sendCommand(String bridgeName, String deviceId, String controlPath, String controlKey,
            String command, String keyName, String value) throws Exception;

    protected abstract RestResult sendCommand(String bridgeName, String deviceId, String controlPath, String controlKey,
            String command, @Nullable String keyName, @Nullable String value, @Nullable ObjectNode extraNode)
            throws Exception;

    protected abstract Map<String, Object> handleGenericErrorResult(@Nullable RestResult resp)
            throws LGThinqApiException;
}
