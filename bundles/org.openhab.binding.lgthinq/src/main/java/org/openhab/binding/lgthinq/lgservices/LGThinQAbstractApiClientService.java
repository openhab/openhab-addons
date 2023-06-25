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

import static org.openhab.binding.lgthinq.internal.LGThinQBindingConstants.*;

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
import org.openhab.binding.lgthinq.internal.LGThinQBindingConstants;
import org.openhab.binding.lgthinq.internal.api.RestResult;
import org.openhab.binding.lgthinq.internal.api.RestUtils;
import org.openhab.binding.lgthinq.internal.api.TokenManager;
import org.openhab.binding.lgthinq.internal.api.TokenResult;
import org.openhab.binding.lgthinq.internal.errors.*;
import org.openhab.binding.lgthinq.lgservices.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The {@link LGThinQACApiV1ClientServiceImpl}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public abstract class LGThinQAbstractApiClientService<C extends CapabilityDefinition, S extends AbstractSnapshotDefinition>
        implements LGThinQApiClientService<C, S> {
    private static final Logger logger = LoggerFactory.getLogger(LGThinQAbstractApiClientService.class);
    protected final ObjectMapper objectMapper = new ObjectMapper();
    protected final TokenManager tokenManager;
    protected Class<C> capabilityClass;
    protected Class<S> snapshotClass;

    protected LGThinQAbstractApiClientService(Class<C> capabilityClass, Class<S> snapshotClass) {
        this.tokenManager = TokenManager.getInstance();
        this.capabilityClass = capabilityClass;
        this.snapshotClass = snapshotClass;
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
            if (!regFile.isFile() || forceRecreate) {
                try (InputStream in = new URL(uri).openStream()) {
                    Files.copy(in, regFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException e) {
            logger.error("Error reading resource from URI: {}", uri, e);
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
            throw new LGThinqApiException("Errors list account devices from LG Server API", e);
        }
    }

    private Map<String, Object> handleDeviceSettingsResult(RestResult resp) throws LGThinqApiException {
        return genericHandleDeviceSettingsResult(resp, logger, objectMapper);
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> genericHandleDeviceSettingsResult(RestResult resp, Logger logger,
            ObjectMapper objectMapper) throws LGThinqApiException {
        Map<String, Object> deviceSettings;
        Map<String, String> respMap = Collections.EMPTY_MAP;
        String resultCode = "???";
        if (resp.getStatusCode() != 200) {
            try {
                respMap = objectMapper.readValue(resp.getJsonResponse(), new TypeReference<>() {
                });
                resultCode = respMap.get("resultCode");
                if (resultCode != null) {
                    logger.error(
                            "Error calling device settings from LG Server API. The code is:{} and The reason is:{}",
                            resultCode, ResultCodes.fromCode(resultCode));
                    throw new LGThinqApiException("Error calling device settings from LG Server API.");
                }
            } catch (JsonProcessingException e) {
                // This exception doesn't matter, it's because response is not in json format. Logging raw response.
            }
            logger.error("Error calling device settings from LG Server API. The reason is:{}", resp.getJsonResponse());
            throw new LGThinqApiException(String.format(
                    "Error calling device settings from LG Server API. The reason is:%s", resp.getJsonResponse()));

        } else {
            try {
                deviceSettings = objectMapper.readValue(resp.getJsonResponse(), new TypeReference<>() {
                });
                String code = Objects.requireNonNullElse((String) deviceSettings.get("resultCode"), "");
                if (!ResultCodes.OK.containsResultCode(code)) {
                    logger.error("LG API report error processing the request -> resultCode=[{}], message=[{}]", code,
                            getErrorCodeMessage(code));
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
                String code = Objects.requireNonNullElse((String) devicesResult.get("resultCode"), "");
                if (!ResultCodes.OK.containsResultCode(code)) {
                    logger.error("LG API report error processing the request -> resultCode=[{}], message=[{}]", code,
                            getErrorCodeMessage(code));
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

    protected static String getErrorCodeMessage(@Nullable String code) {
        if (code == null) {
            return "";
        }
        ResultCodes resultCode = ResultCodes.fromCode(code);
        return resultCode.getDescription();
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

    private S handleV1OfflineException() {
        try {
            // As I don't know the current device status, then I reset to default values.
            S shot = snapshotClass.getDeclaredConstructor().newInstance();
            shot.setPowerStatus(DevicePowerState.DV_POWER_OFF);
            shot.setOnline(false);
            return shot;
        } catch (Exception ex) {
            logger.error("Unexpected Error. The default constructor of this Snapshot wasn't found", ex);
            throw new IllegalStateException("Unexpected Error. The default constructor of this Snapshot wasn't found",
                    ex);
        }
    }

    public @Nullable S getMonitorData(@NonNull String bridgeName, @NonNull String deviceId, @NonNull String workId,
            DeviceTypes deviceType, @NonNull C deviceCapability) throws LGThinqApiException,
            LGThinqDeviceV1MonitorExpiredException, IOException, LGThinqUnmarshallException {
        TokenResult token = tokenManager.getValidRegisteredToken(bridgeName);
        UriBuilder builder = UriBuilder.fromUri(token.getGatewayInfo().getApiRootV1()).path(V1_MON_DATA_PATH);
        Map<String, String> headers = getCommonHeaders(token.getGatewayInfo().getLanguage(),
                token.getGatewayInfo().getCountry(), token.getAccessToken(), token.getUserInfo().getUserNumber());
        String jsonData = String.format("{\n" + "   \"lgedmRoot\":{\n" + "      \"workList\":[\n" + "         {\n"
                + "            \"deviceId\":\"%s\",\n" + "            \"workId\":\"%s\"\n" + "         }\n"
                + "      ]\n" + "   }\n" + "}", deviceId, workId);
        RestResult resp = RestUtils.postCall(builder.build().toURL().toString(), headers, jsonData);
        Map<String, Object> envelop;
        // to unify the same behaviour then V2, this method handle Offline Exception and return a dummy shot with
        // offline flag.
        try {
            envelop = handleGenericErrorResult(resp);
        } catch (LGThinqDeviceV1OfflineException e) {
            return handleV1OfflineException();
        }
        if (envelop.get("workList") != null
                && ((Map<String, Object>) envelop.get("workList")).get("returnData") != null) {
            Map<String, Object> workList = ((Map<String, Object>) envelop.get("workList"));
            if (logger.isDebugEnabled()) {
                try {
                    objectMapper.writeValue(new File(String.format(
                            LGThinQBindingConstants.THINQ_USER_DATA_FOLDER + File.separator + "thinq-%s-datatrace.json",
                            deviceId)), workList);
                } catch (IOException e) {
                    logger.error("Error saving data trace", e);
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
                    logger.error("Returned data format not supported: {}", deviceCapability.getMonitoringDataFormat());
                    throw new LGThinqApiException("Returned data format not supported");
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
    public void initializeDevice(@NonNull String bridgeName, @NonNull String deviceId) throws LGThinqApiException {
    }

    /**
     * Perform some routine before getting data device. Depending on the kind of the device, this is needed
     * to update or prepare some informations before go to get the data.
     *
     */
    protected abstract void beforeGetDataDevice(@NonNull String bridgeName, @NonNull String deviceId)
            throws LGThinqApiException;

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
    public S getDeviceData(@NonNull String bridgeName, @NonNull String deviceId, @NonNull CapabilityDefinition capDef)
            throws LGThinqApiException {
        // Exec pre-conditions (normally ask for update monitoring sensors of the device - temp and power) before call
        // for data
        beforeGetDataDevice(bridgeName, deviceId);

        Map<String, Object> deviceSettings = getDeviceSettings(bridgeName, deviceId);
        if (deviceSettings.get("snapshot") != null) {
            Map<String, Object> snapMap = (Map<String, Object>) deviceSettings.get("snapshot");
            if (logger.isDebugEnabled()) {
                try {
                    objectMapper.writeValue(new File(String.format(
                            LGThinQBindingConstants.THINQ_USER_DATA_FOLDER + File.separator + "thinq-%s-datatrace.json",
                            deviceId)), deviceSettings);
                } catch (IOException e) {
                    logger.error("Error saving data trace", e);
                }
            }
            if (snapMap == null) {
                // No snapshot value provided
                return null;
            }
            S shot = (S) SnapshotBuilderFactory.getInstance().getBuilder(snapshotClass).createFromJson(deviceSettings,
                    capDef);
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
        return Objects.requireNonNull((String) handleGenericErrorResult(resp).get("workId"),
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
