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
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.CONFIG_DEVICE_ID;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.CONFIG_LOCAL_KEY;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.CONFIG_PRODUCT_ID;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.PROPERTY_CATEGORY;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.THING_TYPE_TUYA_DEVICE;

import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
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
import org.openhab.binding.tuya.internal.TuyaSchemaDB;
import org.openhab.binding.tuya.internal.TuyaTokenDB;
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
import org.openhab.binding.tuya.internal.util.SchemaDp;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
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
    protected static final Type tokenType = TypeToken.getParameterized(ResultResponse.class, //
            Token.class //
    ).getType();

    protected static final Type responseBooleanType = TypeToken.getParameterized(ResultResponse.class, //
            Boolean.class //
    ).getType();

    protected static final Type responseDeviceListInfoListType = TypeToken.getParameterized(ResultResponse.class, //
            TypeToken.getParameterized(List.class, DeviceListInfo.class).getType() //
    ).getType();

    protected static final Type responseDeviceSchemaType = TypeToken.getParameterized(ResultResponse.class, //
            DeviceSchema.class //
    ).getType();

    protected static final Type responseFactoryInformationListType = TypeToken.getParameterized(ResultResponse.class, //
            TypeToken.getParameterized(List.class, FactoryInformation.class).getType() //
    ).getType();

    protected final ScheduledExecutorService scheduler;
    protected final HttpClient httpClient;

    private final Logger logger = LoggerFactory.getLogger(TuyaOpenAPI.class);

    protected final ApiStatusCallback callback;
    protected final Gson gson;

    protected ProjectConfiguration config = new ProjectConfiguration();

    protected Token token = TuyaTokenDB.noToken;

    protected @Nullable ScheduledFuture<?> refreshTokenJob = null;

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

    protected void refreshToken() {
        if (System.currentTimeMillis() > token.expireTimestamp) {
            logger.warn("The token has expired and cannot be refreshed. Trying to re-login.");
            login();
        } else {
            request(HttpMethod.GET, "/v1.0/token/" + token.refreshToken, Map.of(), null) //
                    .thenCompose(this::processTokenResponse) //
                    .exceptionally(this::logCauseAndThrow);
        }
    }

    public void login() {
        stopRefreshTokenJob();

        Login login = Login.fromProjectConfiguration(config);

        request(HttpMethod.POST, "/v1.0/iot-01/associated-users/actions/authorized-login", Map.of(), login) //
                .thenCompose(this::processTokenResponse) //
                .exceptionally(t -> {
                    callback.tuyaOpenApiStatus(false);
                    return logCauseAndThrow(t);
                });
    }

    public void dispose() {
        stopRefreshTokenJob();
    }

    protected void stopRefreshTokenJob() {
        ScheduledFuture<?> refreshTokenJob;

        synchronized (this) {
            refreshTokenJob = this.refreshTokenJob;
            this.refreshTokenJob = null;
        }

        if (refreshTokenJob != null) {
            refreshTokenJob.cancel(true);
        }
    }

    protected CompletableFuture<Token> processTokenResponse(String contentString) {
        if (contentString.isEmpty()) {
            callback.tuyaOpenApiStatus(false);
            return CompletableFuture.failedFuture(new ConnectionException("Failed to get token."));
        }

        ResultResponse<Token> result = Objects.requireNonNull(gson.fromJson(contentString, tokenType));

        if (result.success) {
            Token token = result.result;
            if (token != null) {
                token.expireTimestamp = result.timestamp + token.expire * 1000;
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
        }

        return CompletableFuture.failedFuture(new ConnectionException( //
                "No token received: " + result.code + " " + result.msg));
    }

    public void getAllDevices(boolean allSchemas, Consumer<DiscoveryResult> callback) {
        var page = 1;
        int count;

        do {
            var devices = getDeviceList(page++) //
                    .exceptionally(e -> List.of()) //
                    .join();

            processDevices(allSchemas, callback, devices);

            count = devices.size();
        } while (count == 100);
    }

    protected void processDevices(boolean allSchemas, Consumer<DiscoveryResult> callback,
            List<DeviceListInfo> devices) {
        var fetching = new HashSet<String>();

        for (var device : devices) {
            CompletableFuture<List<FactoryInformation>> factoryInformationFuture = getFactoryInformation(device.id);

            if (!fetching.contains(device.productId) //
                    && (allSchemas || !TuyaSchemaDB.contains(device.productId))) {
                fetching.add(device.productId);

                var deviceSchemaFuture = getDeviceSchema(device.id).thenAccept(schema -> {
                    List<SchemaDp> schemaDps = new ArrayList<>(schema.functions.size() + schema.status.size());
                    schema.functions.forEach(description -> addUniqueSchemaDp(description, schemaDps, Boolean.FALSE));
                    schema.status.forEach(description -> addUniqueSchemaDp(description, schemaDps, Boolean.TRUE));

                    // Some schemas seem to "go missing" from the Smart Life API? If we have no local
                    // schema the device is unusable.
                    if (!schemaDps.isEmpty()) {
                        TuyaSchemaDB.put(device.productId, schemaDps);
                    } else if (!TuyaSchemaDB.contains(device.productId)) {
                        logger.warn("Ignoring remote schema for {} - the DP list is empty!", device.productId);
                    }
                });

                factoryInformationFuture = factoryInformationFuture //
                        .thenCombine(deviceSchemaFuture, (fiList, schema) -> fiList);
            }

            factoryInformationFuture //
                    .thenAccept(fiList -> {
                        if (TuyaSchemaDB.contains(device.productId)) {
                            var properties = new HashMap<String, Object>();

                            properties.put(PROPERTY_CATEGORY, device.category);
                            properties.put(CONFIG_LOCAL_KEY, device.localKey);
                            properties.put(CONFIG_DEVICE_ID, device.id);
                            properties.put(CONFIG_PRODUCT_ID, device.productId);

                            String deviceMac = fiList.stream().filter(fi -> fi.id.equals(device.id)).findAny()
                                    .map(fi -> fi.mac).orElse("");
                            if (deviceMac != null && !deviceMac.isEmpty()) {
                                properties.put(Thing.PROPERTY_MAC_ADDRESS, deviceMac.replaceAll("(..)(?!$)", "$1:"));
                            }

                            callback.accept(
                                    DiscoveryResultBuilder.create(new ThingUID(THING_TYPE_TUYA_DEVICE, device.id)) //
                                            .withLabel(device.name) //
                                            .withRepresentationProperty(CONFIG_DEVICE_ID) //
                                            .withProperties(properties) //
                                            .build());
                        } else {
                            logger.warn("No schema available: id {} product {} \"{}\"", //
                                    device.id, device.productId, device.name);
                        }
                    }) //
                    .exceptionally(this::logCauseAndThrow);
        }
    }

    private void addUniqueSchemaDp(DeviceSchema.Description description, List<SchemaDp> schemaDps, Boolean readOnly) {
        if (description.dp_id == 0 || schemaDps.stream().anyMatch(schemaDp -> schemaDp.id == description.dp_id)) {
            // dp is missing or already present, skip it
            return;
        }
        // some devices report the same function code for different dps
        // we add an index only if this is the case
        String originalCode = description.code;
        int index = 1;
        while (schemaDps.stream().anyMatch(schemaDp -> schemaDp.code.equals(description.code))) {
            description.code = originalCode + "_" + index;
        }

        schemaDps.add(SchemaDp.fromRemoteSchema(gson, description, readOnly));
    }

    protected CompletableFuture<List<FactoryInformation>> getFactoryInformation(String deviceId) {
        var url = "/v1.0/iot-03/devices/factory-infos";
        Map<String, String> params = Map.of("device_ids", deviceId);
        return request(HttpMethod.GET, url, params, null).thenCompose( //
                s -> processResponse(url, s, responseFactoryInformationListType));
    }

    private CompletableFuture<List<DeviceListInfo>> getDeviceList(int page) {
        var url = "/v1.0/users/" + token.uid + "/devices";
        Map<String, String> params = Map.of(//
                "from", "", //
                "page_no", String.valueOf(page), //
                "page_size", "100");
        return request(HttpMethod.GET, url, params, null).thenCompose( //
                s -> processResponse(url, s, responseDeviceListInfoListType));
    }

    protected CompletableFuture<DeviceSchema> getDeviceSchema(String deviceId) {
        var url = "/v1.1/devices/" + deviceId + "/specifications";
        return request(HttpMethod.GET, url, Map.of(), null)
                .thenCompose(s -> processResponse(url, s, responseDeviceSchemaType));
    }

    public CompletableFuture<Boolean> sendCommand(String deviceId, CommandRequest command) {
        var url = "/v1.0/iot-03/devices/" + deviceId + "/commands";
        return request(HttpMethod.POST, url, Map.of(), command)
                .thenCompose(s -> processResponse(url, s, responseBooleanType));
    }

    private <T> CompletableFuture<T> processResponse(String tag, String contentString, Type responseType) {
        ResultResponse<T> resultResponse = Objects.requireNonNull(gson.fromJson(contentString, responseType));

        if (resultResponse.success) {
            return CompletableFuture.completedFuture(resultResponse.result);
        } else {
            var n = Integer.parseInt(resultResponse.code);

            if (n >= 1010 && n <= 1013) {
                logger.warn("Server reported invalid token. This should never happen. Trying to re-login.");
                callback.tuyaOpenApiStatus(false);
            }

            return CompletableFuture.failedFuture(new ConnectionException( //
                    tag + ": " + resultResponse.code + " " + resultResponse.msg));
        }
    }

    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    private boolean tokenReferenceEquals(Token myToken) {
        return (token == myToken);
    }

    protected CompletableFuture<String> request(HttpMethod method, String path, Map<String, String> params,
            @Nullable Object body) {
        var lastKnownToken = token;
        CompletableFuture<String> future = apiRequest(method, path, params, body);

        for (var i = 0; i < CLOUD_RETRY_MAX; i++) {
            final int tryNumber = i;

            future = future.exceptionally(t -> {
                var cause = (t instanceof CompletionException ? Objects.requireNonNullElse(t.getCause(), t) : t);
                var msg = cause.getMessage();

                if (!tokenReferenceEquals(lastKnownToken) // Maybe refreshed under us? Try again...
                        || (cause instanceof ConnectionException //
                                && msg != null //
                                && !msg.contains(" 1010 ") && !msg.contains(" 1011 ") && !msg.contains(" 1012 ")
                                && !msg.contains(" 1013 "))) {
                    logger.trace("{}: retry {}", path, tryNumber);

                    var innerFuture = new CompletableFuture<String>();

                    return innerFuture.completeAsync( //
                            () -> apiRequest(method, path, params, body).join(), //
                            CompletableFuture.delayedExecutor(CLOUD_RETRY_DELAY, TimeUnit.MILLISECONDS) //
                    ).join();
                }

                logger.trace("{}: failed: {}", path, msg);

                throw new CompletionException(cause);
            });
        }

        return future;
    }

    protected CompletableFuture<String> apiRequest(HttpMethod method, String path, Map<String, String> params,
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
        Request request = httpClient.newRequest(URI.create(fullUrl)) //
                .idleTimeout(CLOUD_RETRY_DELAY, TimeUnit.MILLISECONDS) // Retries SHOULD reconnect.
                .method(method);

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

    protected String signUrl(String path, Map<String, String> params) {
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
    protected String requestToLogString(Request request) {
        ContentProvider contentProvider = request.getContent();
        String contentString = contentProvider == null ? "null"
                : StreamSupport.stream(contentProvider.spliterator(), false)
                        .map(b -> StandardCharsets.UTF_8.decode(b).toString()).collect(Collectors.joining(", "));

        return "Method = {" + request.getMethod() + "}, Headers = {"
                + request.getHeaders().stream().map(HttpField::toString).collect(Collectors.joining(", "))
                + "}, Content = {" + contentString + "}";
    }

    protected <T> T logCauseAndThrow(Throwable t) {
        var cause = (t instanceof CompletionException ? Objects.requireNonNullElse(t.getCause(), t) : t);

        if (cause instanceof ConnectionException) {
            logger.warn("{}", cause.getMessage());
        } else {
            logger.debug("Exception: ", cause);
        }

        throw new CompletionException(t);
    }
}
