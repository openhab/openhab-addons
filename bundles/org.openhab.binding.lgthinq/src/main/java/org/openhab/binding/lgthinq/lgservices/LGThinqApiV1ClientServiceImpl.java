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
import org.openhab.binding.lgthinq.lgservices.model.*;
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
public class LGThinqApiV1ClientServiceImpl extends LGThinqApiClientServiceImpl {
    private static final LGThinqApiClientService instance;
    private static final Logger logger = LoggerFactory.getLogger(LGThinqApiV1ClientServiceImpl.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TokenManager tokenManager;

    static {
        instance = new LGThinqApiV1ClientServiceImpl();
    }

    private LGThinqApiV1ClientServiceImpl() {
        tokenManager = TokenManager.getInstance();
    }

    public static LGThinqApiClientService getInstance() {
        return instance;
    }

    @Override
    protected TokenManager getTokenManager() {
        return tokenManager;
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
        throw new UnsupportedOperationException("Method not supported in V1 API device.");
    }

    public RestResult sendControlCommands(String bridgeName, String deviceId, String keyName, int value)
            throws Exception {
        TokenResult token = tokenManager.getValidRegisteredToken(bridgeName);
        UriBuilder builder = UriBuilder.fromUri(token.getGatewayInfo().getApiRootV1()).path(V1_CONTROL_OP);
        Map<String, String> headers = getCommonHeaders(token.getGatewayInfo().getLanguage(),
                token.getGatewayInfo().getCountry(), token.getAccessToken(), token.getUserInfo().getUserNumber());

        String payload = String.format(
                "{\n" + "   \"lgedmRoot\":{\n" + "      \"cmd\": \"Control\"," + "      \"cmdOpt\": \"Set\","
                        + "      \"value\": {\"%s\": \"%d\"}," + "      \"deviceId\": \"%s\","
                        + "      \"workId\": \"%s\"," + "      \"data\": \"\"" + "   }\n" + "}",
                keyName, value, deviceId, UUID.randomUUID().toString());
        return RestUtils.postCall(builder.build().toURL().toString(), headers, payload);
    }

    @Override
    public void turnDevicePower(String bridgeName, String deviceId, DevicePowerState newPowerState)
            throws LGThinqApiException {
        try {
            RestResult resp = sendControlCommands(bridgeName, deviceId, "Operation", newPowerState.commandValue());
            handleV1GenericErrorResult(resp);
        } catch (Exception e) {
            throw new LGThinqApiException("Error adjusting device power", e);
        }
    }

    @Override
    public void changeOperationMode(String bridgeName, String deviceId, int newOpMode) throws LGThinqApiException {
        try {
            RestResult resp = sendControlCommands(bridgeName, deviceId, "OpMode", newOpMode);

            handleV1GenericErrorResult(resp);
        } catch (Exception e) {
            throw new LGThinqApiException("Error adjusting operation mode", e);
        }
    }

    @Override
    public void changeFanSpeed(String bridgeName, String deviceId, int newFanSpeed) throws LGThinqApiException {
        try {
            RestResult resp = sendControlCommands(bridgeName, deviceId, "WindStrength", newFanSpeed);

            handleV1GenericErrorResult(resp);
        } catch (Exception e) {
            throw new LGThinqApiException("Error adjusting fan speed", e);
        }
    }

    @Override
    public void changeTargetTemperature(String bridgeName, String deviceId, ACTargetTmp newTargetTemp)
            throws LGThinqApiException {
        try {
            RestResult resp = sendControlCommands(bridgeName, deviceId, "TempCfg", newTargetTemp.commandValue());

            handleV1GenericErrorResult(resp);
        } catch (Exception e) {
            throw new LGThinqApiException("Error adjusting target temperature", e);
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

    @NonNull
    private Map<String, Object> handleV1GenericErrorResult(@Nullable RestResult resp)
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

    @Override
    @Nullable
    public ACSnapShot getMonitorData(@NonNull String bridgeName, @NonNull String deviceId, @NonNull String workId)
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
            ACSnapShot shot = new ACSnapShotV2();
            shot.setOnline(false);
            return shot;
        }
        if (envelop.get("workList") != null
                && ((Map<String, Object>) envelop.get("workList")).get("returnData") != null) {
            Map<String, Object> workList = ((Map<String, Object>) envelop.get("workList"));
            if (!"0000".equals(workList.get("returnCode"))) {
                LGThinqDeviceV1MonitorExpiredException e = new LGThinqDeviceV1MonitorExpiredException(
                        String.format("Monitor for device %s has expired. Please, refresh the monitor.", deviceId));
                logger.warn("{}", e.getMessage());
                throw e;
            }

            String jsonMonDataB64 = (String) workList.get("returnData");
            String jsonMon = new String(Base64.getDecoder().decode(jsonMonDataB64));
            ACSnapShot shot = objectMapper.readValue(jsonMon, ACSnapShotV1.class);
            shot.setOnline("E".equals(workList.get("deviceState")));
            return shot;
        } else {
            // no data available yet
            return null;
        }
    }

    private File getCapFileForDevice(String deviceId) {
        return new File(String.format(BASE_CAP_CONFIG_DATA_FILE, deviceId));
    }

    /**
     * Get capability em registry/cache on file for next consult
     * 
     * @param deviceId ID of the device
     * @param uri URI of the config capanility
     * @return return simplified capability
     * @throws LGThinqApiException If some error occurr
     */
    @Override
    @NonNull
    public ACCapability getACCapability(String deviceId, String uri, boolean forceRecreate) throws LGThinqApiException {
        try {
            File regFile = loadDeviceCapability(deviceId, uri, forceRecreate);
            Map<String, Object> mapper = objectMapper.readValue(regFile, new TypeReference<>() {
            });
            ACCapability acCap = new ACCapability();

            Map<String, Object> cap = (Map<String, Object>) mapper.get("Value");
            if (cap == null) {
                throw new LGThinqApiException("Error extracting capabilities supported by the device");
            }

            Map<String, Object> opModes = (Map<String, Object>) cap.get("OpMode");
            if (opModes == null) {
                throw new LGThinqApiException("Error extracting opModes supported by the device");
            } else {
                Map<String, String> modes = new HashMap<String, String>();
                ((Map<String, String>) opModes.get("option")).forEach((k, v) -> {
                    modes.put(v, k);
                });
                acCap.setOpMod(modes);
            }
            Map<String, Object> fanSpeed = (Map<String, Object>) cap.get("WindStrength");
            if (fanSpeed == null) {
                throw new LGThinqApiException("Error extracting fanSpeed supported by the device");
            } else {
                Map<String, String> fanModes = new HashMap<String, String>();
                ((Map<String, String>) fanSpeed.get("option")).forEach((k, v) -> {
                    fanModes.put(v, k);
                });
                acCap.setFanSpeed(fanModes);

            }
            // Set supported modes for the device

            Map<String, Map<String, String>> supOpModes = (Map<String, Map<String, String>>) cap.get("SupportOpMode");
            acCap.setSupportedOpMode(new ArrayList<>(supOpModes.get("option").values()));
            acCap.getSupportedOpMode().remove("@NON");
            Map<String, Map<String, String>> supFanSpeeds = (Map<String, Map<String, String>>) cap
                    .get("SupportWindStrength");
            acCap.setSupportedFanSpeed(new ArrayList<>(supFanSpeeds.get("option").values()));
            acCap.getSupportedFanSpeed().remove("@NON");

            return acCap;
        } catch (IOException e) {
            throw new LGThinqApiException("Error reading IO interface", e);
        }
    }
}
