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
package org.openhab.binding.tuya.internal.cloud;

import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.tuya.internal.cloud.dto.CommandRequest;
import org.openhab.binding.tuya.internal.cloud.dto.DeviceListInfo;
import org.openhab.binding.tuya.internal.cloud.dto.DeviceSchema;
import org.openhab.binding.tuya.internal.cloud.dto.FactoryInformation;
import org.openhab.binding.tuya.internal.cloud.dto.Login;
import org.openhab.binding.tuya.internal.cloud.dto.ResultResponse;
import org.openhab.binding.tuya.internal.cloud.dto.Token;
import org.openhab.binding.tuya.internal.config.ProjectConfiguration;
import org.openhab.binding.tuya.internal.util.CryptoUtil;
import org.openhab.binding.tuya.internal.util.JoiningMapCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link TuyaOpenAPI} is an implementation of the Tuya OpenApi specification
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class TuyaOpenAPI {
    private final Logger logger = LoggerFactory.getLogger(TuyaOpenAPI.class);
    private final ScheduledExecutorService scheduler;
    private ProjectConfiguration config = new ProjectConfiguration();
    private final HttpClient httpClient;

    private final ApiStatusCallback callback;
    private final Gson gson;

    private Token token = new Token();
    private @Nullable ScheduledFuture<?> refreshTokenJob;

    public TuyaOpenAPI(ApiStatusCallback callback, ScheduledExecutorService scheduler, Gson gson,
            HttpClient httpClient) {
        this.callback = callback;
        this.gson = gson;
        this.httpClient = httpClient;
        this.scheduler = scheduler;
    }

    public void setConfiguration(ProjectConfiguration configuration) {
        this.config = configuration;
    }

    public boolean isConnected() {
        return !token.accessToken.isEmpty() && System.currentTimeMillis() < token.expireTimestamp;
    }

    private void refreshToken() {
        if (System.currentTimeMillis() > token.expireTimestamp) {
            logger.warn("Cannot refresh token after expiry. Trying to re-login.");
            login();
        } else {
            stopRefreshTokenJob();
            request(HttpMethod.GET, "/v1.0/token/" + token.refreshToken, Map.of(), null).exceptionally(t -> "")
                    .thenAccept(this::processTokenResponse);
        }
    }

    public void login() {
        Login login = Login.fromProjectConfiguration(config);

        stopRefreshTokenJob();
        request(HttpMethod.POST, "/v1.0/iot-01/associated-users/actions/authorized-login", Map.of(), login)
                .exceptionally(t -> "").thenApply(this::processTokenResponse);
    }

    public void disconnect() {
        stopRefreshTokenJob();
        token = new Token();
    }

    private void stopRefreshTokenJob() {
        ScheduledFuture<?> refreshTokenJob = this.refreshTokenJob;
        if (refreshTokenJob != null) {
            refreshTokenJob.cancel(true);
            this.refreshTokenJob = null;
        }
    }

    private CompletableFuture<Void> processTokenResponse(String contentString) {
        if (contentString.isEmpty()) {
            this.token = new Token();
            callback.tuyaOpenApiStatus(false);
            return CompletableFuture.failedFuture(new ConnectionException("Failed to get token."));
        }

        Type type = TypeToken.getParameterized(ResultResponse.class, Token.class).getType();
        ResultResponse<Token> result = Objects.requireNonNull(gson.fromJson(contentString, type));

        if (result.success) {
            Token token = result.result;
            if (token != null) {
                token.expireTimestamp = result.timestamp + token.expire * 1000;
                logger.debug("Got token: {}", token);
                this.token = token;
                callback.tuyaOpenApiStatus(true);
                refreshTokenJob = scheduler.schedule(this::refreshToken, token.expire - 60, TimeUnit.SECONDS);
                return CompletableFuture.completedFuture(null);
            }
        }

        logger.warn("Request failed: {}, no token received", result);
        this.token = new Token();
        callback.tuyaOpenApiStatus(false);
        return CompletableFuture.failedFuture(new ConnectionException("Failed to get token."));
    }

    public CompletableFuture<List<FactoryInformation>> getFactoryInformation(List<String> deviceIds) {
        Map<String, String> params = Map.of("device_ids", String.join(",", deviceIds));
        return request(HttpMethod.GET, "/v1.0/iot-03/devices/factory-infos", params, null).thenCompose(
                s -> processResponse(s, TypeToken.getParameterized(List.class, FactoryInformation.class).getType()));
    }

    public CompletableFuture<List<DeviceListInfo>> getDeviceList(int page) {
        Map<String, String> params = Map.of(//
                "from", "", //
                "page_no", String.valueOf(page), //
                "page_size", "100");
        return request(HttpMethod.GET, "/v1.0/users/" + token.uid + "/devices", params, null).thenCompose(
                s -> processResponse(s, TypeToken.getParameterized(List.class, DeviceListInfo.class).getType()));
    }

    public CompletableFuture<DeviceSchema> getDeviceSchema(String deviceId) {
        return request(HttpMethod.GET, "/v1.1/devices/" + deviceId + "/specifications", Map.of(), null)
                .thenCompose(s -> processResponse(s, DeviceSchema.class));
    }

    public CompletableFuture<Boolean> sendCommand(String deviceId, CommandRequest command) {
        return request(HttpMethod.POST, "/v1.0/iot-03/devices/" + deviceId + "/commands", Map.of(), command)
                .thenCompose(s -> processResponse(s, Boolean.class));
    }

    private <T> CompletableFuture<T> processResponse(String contentString, Type type) {
        Type responseType = TypeToken.getParameterized(ResultResponse.class, type).getType();
        ResultResponse<T> resultResponse = Objects.requireNonNull(gson.fromJson(contentString, responseType));
        if (resultResponse.success) {
            return CompletableFuture.completedFuture(resultResponse.result);
        } else {
            if (resultResponse.code >= 1010 && resultResponse.code <= 1013) {
                logger.warn("Server reported invalid token. This should never happen. Trying to re-login.");
                callback.tuyaOpenApiStatus(false);
                return CompletableFuture.failedFuture(new ConnectionException(resultResponse.msg));
            }
            return CompletableFuture.failedFuture(new IllegalStateException(resultResponse.msg));
        }
    }

    private CompletableFuture<String> request(HttpMethod method, String path, Map<String, String> params,
            @Nullable Object body) {
        CompletableFuture<String> future = new CompletableFuture<>();
        long now = System.currentTimeMillis();

        String sign = signRequest(method, path, Map.of("client_id", config.accessId), List.of("client_id"), params,
                body, null, now);
        Map<String, String> headers = Map.of( //
                "client_id", config.accessId, //
                "t", Long.toString(now), //
                "Signature-Headers", "client_id", //
                "sign", sign, //
                "sign_method", "HMAC-SHA256", //
                "access_token", this.token.accessToken);

        String fullUrl = config.dataCenter + signUrl(path, params);
        Request request = httpClient.newRequest(URI.create(fullUrl));
        request.method(method);
        headers.forEach(request::header);
        if (body != null) {
            request.content(new StringContentProvider(gson.toJson(body)));
            request.header("Content-Type", "application/json");
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Sending to '{}': {}", fullUrl, requestToLogString(request));
        }

        request.send(new TuyaContentListener(future));
        return future;
    }

    // used for testing only
    void setToken(Token token) {
        this.token = token;
    }

    // package private to allow tests
    String signRequest(HttpMethod method, String path, Map<String, String> headers, List<String> signHeaders,
            Map<String, String> params, @Nullable Object body, @Nullable String nonce, long now) {
        String stringToSign = stringToSign(method, path, headers, signHeaders, params, body);
        String tokenToUse = path.startsWith("/v1.0/token") ? "" : this.token.accessToken;
        String fullStringToSign = this.config.accessId + tokenToUse + now + (nonce == null ? "" : nonce) + stringToSign;

        return CryptoUtil.hmacSha256(fullStringToSign, config.accessSecret);
    }

    private String stringToSign(HttpMethod method, String path, Map<String, String> headers, List<String> signHeaders,
            Map<String, String> params, @Nullable Object body) {
        String bodyString = CryptoUtil.sha256(body != null ? gson.toJson(body) : "");
        String headerString = headers.entrySet().stream().filter(e -> signHeaders.contains(e.getKey()))
                .sorted(Map.Entry.comparingByKey()).collect(JoiningMapCollector.joining(":", "\n"));
        String urlString = signUrl(path, params);
        // add extra \n after header string -> TUYAs documentation is wrong
        return method.asString() + "\n" + bodyString + "\n" + headerString + "\n\n" + urlString;
    }

    private String signUrl(String path, Map<String, String> params) {
        String paramString = params.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .collect(JoiningMapCollector.joining("=", "&"));
        if (paramString.isEmpty()) {
            return path;
        } else {
            return path + "?" + paramString;
        }
    }

    /**
     * create a log string from a {@link Request}
     *
     * @param request the request to log
     * @return the string representing the request
     */
    private String requestToLogString(Request request) {
        ContentProvider contentProvider = request.getContent();
        String contentString = contentProvider == null ? "null"
                : StreamSupport.stream(contentProvider.spliterator(), false)
                        .map(b -> StandardCharsets.UTF_8.decode(b).toString()).collect(Collectors.joining(", "));

        return "Method = {" + request.getMethod() + "}, Headers = {"
                + request.getHeaders().stream().map(HttpField::toString).collect(Collectors.joining(", "))
                + "}, Content = {" + contentString + "}";
    }
}
