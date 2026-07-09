/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.tuya.internal.cloud;

import static org.openhab.binding.tuya.internal.TuyaBindingConstants.CLOUD_RETRY_DELAY;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.CLOUD_RETRY_MAX;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.QR_LOGIN_DELAY;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.QR_LOGIN_DURATION;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.QR_LOGIN_INTERVAL;

import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.tuya.internal.TuyaTokenDB;
import org.openhab.binding.tuya.internal.cloud.dto.CommandRequest;
import org.openhab.binding.tuya.internal.cloud.dto.DeviceListInfo;
import org.openhab.binding.tuya.internal.cloud.dto.DeviceSchema;
import org.openhab.binding.tuya.internal.cloud.dto.DeviceStatus;
import org.openhab.binding.tuya.internal.cloud.dto.FactoryInformation;
import org.openhab.binding.tuya.internal.cloud.dto.HomeInformation;
import org.openhab.binding.tuya.internal.cloud.dto.QrToken;
import org.openhab.binding.tuya.internal.cloud.dto.ResultResponse;
import org.openhab.binding.tuya.internal.cloud.dto.Token;
import org.openhab.binding.tuya.internal.config.ProjectConfiguration;
import org.openhab.binding.tuya.internal.config.SmartLifeConfiguration;
import org.openhab.binding.tuya.internal.util.CryptoUtil;
import org.openhab.binding.tuya.internal.util.JoiningMapCollector;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link TuyaSmartLifeAPI} is an implementation of the Tuya OpenApi specification
 * as used by the Smart Life app.
 *
 * @author Mike Jagdis - Initial contribution
 */
@NonNullByDefault
public class TuyaSmartLifeAPI extends TuyaOpenAPI {
    protected static final String TUYA_CLIENT_ID = "HA_3y9q4ak7g4ephrvke";

    private static final String TUYA_SCHEMA = "haauthorize";

    private static final int QR_LOGIN_TRIES = (QR_LOGIN_DURATION + QR_LOGIN_INTERVAL - 1) / QR_LOGIN_INTERVAL;

    protected static final Type responseDeviceListType //
            = TypeToken.getParameterized(List.class, DeviceListInfo.class).getType();

    protected static final Type responseHomeInformationListType //
            = TypeToken.getParameterized(List.class, HomeInformation.class).getType();

    private static final Type responseQrTokenType = TypeToken.getParameterized(ResultResponse.class, //
            QrToken.class //
    ).getType();

    private static final Type responseStringType = TypeToken.getParameterized(ResultResponse.class, //
            String.class //
    ).getType();

    private final Logger logger = LoggerFactory.getLogger(TuyaSmartLifeAPI.class);

    private String qrToken = "";
    private int qrLoginTries = 0;

    public TuyaSmartLifeAPI(ApiStatusCallback callback, ScheduledExecutorService scheduler, Gson gson,
            HttpClient httpClient) {
        super(callback, scheduler, gson, httpClient);
    }

    private String getUserCode() {
        return ((SmartLifeConfiguration) config).userCode;
    }

    @Override
    public void setConfiguration(ProjectConfiguration configuration) {
        super.setConfiguration(configuration);

        token = TuyaTokenDB.get(getUserCode());
        refreshToken();
    }

    public String getQrLoginText() {
        stopRefreshTokenJob();

        qrToken = "";

        CompletableFuture<String> future = new CompletableFuture<>();

        httpClient.newRequest("https://apigw.iotbing.com" //
                + "/v1.0/m/life/home-assistant/qrcode/tokens?clientid=" + TUYA_CLIENT_ID //
                + "&schema=" + TUYA_SCHEMA //
                + "&usercode=" + getUserCode()) //
                .method(HttpMethod.POST) //
                .timeout(5, TimeUnit.SECONDS) //
                .send(new TuyaContentListener(future));

        qrToken = future //
                .thenApply(content -> {
                    ResultResponse<QrToken> result //
                            = Objects.requireNonNull(gson.fromJson(content, responseQrTokenType));
                    var qrTokenObj = result.result;
                    return (qrTokenObj != null ? qrTokenObj.qrToken : "");
                }) //
                .exceptionally(t -> {
                    var cause = (t instanceof CompletionException ? Objects.requireNonNullElse(t.getCause(), t) : t);

                    logger.debug("Exception: ", cause);

                    throw new CompletionException(cause);
                }) //
                .join();

        if (!qrToken.isBlank()) {
            qrLoginTries = QR_LOGIN_TRIES;

            synchronized (this) {
                stopRefreshTokenJob();
                refreshTokenJob = scheduler.scheduleWithFixedDelay(this::getQrLoginResult, //
                        QR_LOGIN_DELAY, QR_LOGIN_INTERVAL, TimeUnit.SECONDS);
            }

            return "tuyaSmart--qrLogin?token=" + qrToken;
        }

        return "";
    }

    public void getQrLoginResult() {
        CompletableFuture<String> future = new CompletableFuture<>();

        httpClient.newRequest("https://apigw.iotbing.com" //
                + "/v1.0/m/life/home-assistant/qrcode/tokens/" + qrToken + "?clientid=" + TUYA_CLIENT_ID //
                + "&usercode=" + getUserCode()) //
                .method(HttpMethod.GET) //
                .timeout(5, TimeUnit.SECONDS) //
                .send(new TuyaContentListener(future));

        future //
                .thenCompose(this::processTokenResponse) //
                .thenApply(token -> TuyaTokenDB.put(getUserCode(), token)) //
                .exceptionally(t -> {
                    var cause = (t instanceof CompletionException ? Objects.requireNonNullElse(t.getCause(), t) : t);

                    if (qrLoginTries > 0 //
                            && cause instanceof ConnectionException //
                            && !Objects.requireNonNullElse(cause.getMessage(), "").contains("E0020002")) {
                        logger.debug("QR login failed ({} more attempts): {}", qrLoginTries, cause.getMessage());
                        qrLoginTries--;
                    } else {
                        qrToken = "";
                        stopRefreshTokenJob();

                        if (cause instanceof ConnectionException) {
                            logger.info("QR login failed - try again: {}", cause.getMessage());
                        } else {
                            logger.debug("QR login exception: ", cause);
                        }

                        refreshToken();
                    }

                    return token;
                });
    }

    @Override
    public void dispose() {
        // Extend the token life as much as possible to avoid the user having to rescan QR codes.
        refreshToken();

        super.dispose();
    }

    @Override
    protected void refreshToken() {
        if (System.currentTimeMillis() > token.expireTimestamp) {
            stopRefreshTokenJob();
            logger.warn("The token has expired and cannot be refreshed. Try to re-login.");
            callback.tuyaOpenApiStatus(false);
        } else {
            request(HttpMethod.GET, "/v1.0/m/token/" + token.refreshToken, Map.of(), null) //
                    .thenCompose(content -> processRefreshTokenResponse(content)) //
                    .thenApply(token -> TuyaTokenDB.put(getUserCode(), token)) //
                    .exceptionally(this::logCauseAndThrow);
        }
    }

    protected CompletableFuture<Token> processRefreshTokenResponse(String contentString) {
        Token token = gson.fromJson(contentString, Token.class);

        if (token != null) {
            token.expireTimestamp = System.currentTimeMillis() + token.expire * 1000;
            logger.debug("Got token: {}", token);

            this.token = token;

            synchronized (this) {
                stopRefreshTokenJob();
                refreshTokenJob = scheduler.schedule(this::refreshToken, //
                        (token.expire - 60) * 1000 - (CLOUD_RETRY_DELAY * CLOUD_RETRY_MAX), TimeUnit.MILLISECONDS);
            }

            callback.tuyaOpenApiStatus(true);

            return CompletableFuture.completedFuture(token);
        }

        token = TuyaTokenDB.noToken;
        callback.tuyaOpenApiStatus(false);
        return CompletableFuture.failedFuture(new ConnectionException("Token refresh failed. Try to re-login."));
    }

    @Override
    public void getAllDevices(boolean allSchemas, Consumer<DiscoveryResult> callback) {
        for (var home : getHomeList().exceptionally(this::logCauseAndThrow).exceptionally(t -> List.of()).join()) {
            processDevices(allSchemas, callback, //
                    getDeviceListByHome(home.ownerId) //
                            .exceptionally(this::logCauseAndThrow) //
                            .exceptionally(t -> List.of()).join());
        }
    }

    private CompletableFuture<List<HomeInformation>> getHomeList() {
        return request(HttpMethod.GET, "/v1.0/m/life/users/homes", Map.of(), null) //
                .thenCompose(s -> processResponse(s, responseHomeInformationListType));
    }

    private CompletableFuture<List<DeviceListInfo>> getDeviceListByHome(String homeId) {
        return request(HttpMethod.GET, "/v1.0/m/life/ha/home/devices", Map.of("homeId", homeId), null) //
                .thenCompose(s -> processResponse(s, responseDeviceListType));
    }

    @Override
    protected CompletableFuture<List<FactoryInformation>> getFactoryInformation(String deviceId) {
        // tuya-device-sharing-sdk does not have anything like factory-infos. TuyaOpenApi only
        // uses it to get the device's MAC address which we only do in order to make it available
        // to the user in case it is needed to search DHCP logs (for instance).
        return CompletableFuture.completedFuture(List.of());
    }

    private CompletableFuture<DeviceSchema> getDeviceSpecifications(String deviceId) {
        return request(HttpMethod.GET, "/v1.1/m/life/" + deviceId + "/specifications", Map.of(), null)
                .thenCompose(s -> processResponse(s, DeviceSchema.class));
    }

    private CompletableFuture<DeviceStatus> getDeviceStatus(String deviceId) {
        return request(HttpMethod.GET, "/v1.0/m/life/devices/" + deviceId + "/status", Map.of(), null)
                .thenCompose(s -> processResponse(s, DeviceStatus.class));
    }

    @Override
    protected CompletableFuture<DeviceSchema> getDeviceSchema(String deviceId) {
        var deviceStatusFuture = getDeviceStatus(deviceId);
        var deviceSpecificationsFuture = getDeviceSpecifications(deviceId);

        return deviceSpecificationsFuture.thenCombine(deviceStatusFuture, (deviceSchema, deviceStatus) -> {
            var dpMap = deviceStatus.dpStatusRelationDTOS.stream() //
                    .collect(Collectors.toMap(e -> e.statusCode, e -> e.dpId));

            deviceSchema.functions.forEach(e -> {
                e.dp_id = Objects.requireNonNullElse(dpMap.get(e.code), e.dp_id);
            });

            deviceSchema.status.forEach(e -> {
                e.dp_id = Objects.requireNonNullElse(dpMap.get(e.code), e.dp_id);
            });

            return deviceSchema;
        });
    }

    @Override
    public CompletableFuture<Boolean> sendCommand(String deviceId, CommandRequest command) {
        return request(HttpMethod.POST, "/v1.1/m/thing/" + deviceId + "/commands", Map.of(), command)
                .thenCompose(s -> processResponse(s, responseBooleanType));
    }

    private <T> CompletableFuture<T> processResponse(String contentString, Type responseType) {
        return CompletableFuture.completedFuture(Objects.requireNonNull(gson.fromJson(contentString, responseType)));
    }

    protected String secret_generating(String rid, String sid, String hash_key) {
        var message = hash_key;
        var mod = 16;

        if (!sid.isEmpty()) {
            var sid_length = sid.length();
            var length = (sid_length < mod ? sid_length : mod);
            var ecode = "";
            for (var i = 0; i < length; i++) {
                var idx = ((int) sid.charAt(i)) % mod;
                ecode = ecode + sid.charAt(idx);
            }
            message = message + "_" + ecode;
        }

        return CryptoUtil.hmacSha256(message, rid).substring(0, 16).toLowerCase(Locale.ENGLISH);
    }

    protected String restful_sign(String hash_key, String query_encdata, String body_encdata,
            Map<String, String> headers) {
        var sign_str = headers.entrySet().stream().sorted(Map.Entry.comparingByKey()) //
                .filter(e -> !e.getValue().isEmpty()) //
                .collect(JoiningMapCollector.joining("=", "||"));

        if (!query_encdata.isEmpty()) {
            sign_str += query_encdata;
        }

        if (!body_encdata.isEmpty()) {
            sign_str += body_encdata;
        }

        return CryptoUtil.hmacSha256(sign_str, hash_key).toLowerCase(Locale.ENGLISH);
    }

    @Override
    protected CompletableFuture<String> apiRequest(HttpMethod method, String path, Map<String, String> params,
            @Nullable Object body) {
        try {
            var rid = UUID.randomUUID().toString();
            var sid = "";
            var hash_key = CryptoUtil.md5(rid + token.refreshToken);
            var secret = secret_generating(rid, sid, hash_key).getBytes(StandardCharsets.UTF_8);

            var query_encdata = "";
            if (!params.isEmpty()) {
                query_encdata = Base64.getEncoder().encodeToString( //
                        CryptoUtil.encryptAesGcm(gson.toJson(params).getBytes(StandardCharsets.UTF_8), secret, null,
                                null));
                params = Map.of("encdata", query_encdata);
            }

            Map<String, String> bodyData = null;
            var body_encdata = "";
            if (body != null) {
                body_encdata = Base64.getEncoder().encodeToString( //
                        CryptoUtil.encryptAesGcm(gson.toJson(body).getBytes(StandardCharsets.UTF_8), secret, null,
                                null));
                bodyData = Map.of("encdata", body_encdata);
            }

            var t = Long.toString(System.currentTimeMillis());
            // The python tuya-device-sharing-sdk uses time() * 1000 (i.e. milliseconds zeroed).
            // t = t.substring(0, t.length() - 3) + "000";

            // The order the headers appear in the request may be important?
            var headers = new LinkedHashMap<String, String>();
            headers.put("X-appKey", TUYA_CLIENT_ID);
            headers.put("X-requestId", rid);
            headers.put("X-sid", sid);
            headers.put("X-time", t);

            if (!token.accessToken.isEmpty()) {
                headers.put("X-token", token.accessToken);
            }

            headers.put("X-sign", restful_sign(hash_key, query_encdata, body_encdata, headers));

            String fullUrl = config.dataCenter + signUrl(path, params);
            Request request = httpClient.newRequest(URI.create(fullUrl)) //
                    .idleTimeout(CLOUD_RETRY_DELAY, TimeUnit.MILLISECONDS) // Retries SHOULD reconnect.
                    .method(method);

            headers.forEach(request::header);

            if (bodyData != null) {
                request.content(new StringContentProvider(gson.toJson(bodyData)));
                request.header("Content-Type", "application/json");
            }

            if (logger.isTraceEnabled()) {
                logger.trace("Sending to '{}': {}", fullUrl, requestToLogString(request));
            }

            CompletableFuture<String> future = new CompletableFuture<>();

            request.send(new TuyaContentListener(future));

            return future //
                    .thenCompose((content) -> {
                        ResultResponse<String> ret = Objects.requireNonNull(gson.fromJson(content, responseStringType));

                        if (!ret.success) {
                            var n = Integer.parseInt(ret.code);

                            if (n >= 1010 && n <= 1013) {
                                logger.warn("Cloud server reported {} {}. Try to re-login.", ret.code, ret.msg);
                                callback.tuyaOpenApiStatus(false);
                            }

                            return CompletableFuture.failedFuture(new ConnectionException( //
                                    path + ": " + ret.code + " " + ret.msg));
                        }

                        try {
                            var result = new String(Objects.requireNonNull( //
                                    CryptoUtil.decryptAesGcm( //
                                            Base64.getDecoder().decode(ret.result), //
                                            secret, null, null) //
                            ));

                            logger.trace("response after decrypt ret = {}", result);

                            return CompletableFuture.completedFuture(result);
                        } catch (Exception e) {
                            logger.debug("Decrypt failed, exception: ", e);
                            return CompletableFuture.failedFuture(e);
                        }
                    });
        } catch (Exception e) {
            logger.debug("Request failed, exception: ", e);
            return CompletableFuture.completedFuture("");
        }
    }
}
