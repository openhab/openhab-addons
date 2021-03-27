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
package org.openhab.binding.myq.internal.handler;

import static org.openhab.binding.myq.internal.MyQBindingConstants.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.myq.internal.MyQDiscoveryService;
import org.openhab.binding.myq.internal.config.MyQAccountConfiguration;
import org.openhab.binding.myq.internal.dto.AccountDTO;
import org.openhab.binding.myq.internal.dto.ActionDTO;
import org.openhab.binding.myq.internal.dto.DevicesDTO;
import org.openhab.binding.myq.internal.dto.LoginRequestDTO;
import org.openhab.binding.myq.internal.dto.LoginResponseDTO;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link MyQAccountHandler} is responsible for communicating with the MyQ API based on an account.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class MyQAccountHandler extends BaseBridgeHandler {
    private static final String BASE_URL = "https://api.myqdevice.com/api";
    private static final Integer RAPID_REFRESH_SECONDS = 5;
    private final Logger logger = LoggerFactory.getLogger(MyQAccountHandler.class);
    private final Gson gsonUpperCase = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .create();
    private final Gson gsonLowerCase = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    private @Nullable Future<?> normalPollFuture;
    private @Nullable Future<?> rapidPollFuture;
    private @Nullable String securityToken;
    private @Nullable AccountDTO account;
    private @Nullable DevicesDTO devicesCache;
    private Integer normalRefreshSeconds = 60;
    private HttpClient httpClient;
    private String username = "";
    private String password = "";
    private String userAgent = "";

    public MyQAccountHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        MyQAccountConfiguration config = getConfigAs(MyQAccountConfiguration.class);
        normalRefreshSeconds = config.refreshInterval;
        username = config.username;
        password = config.password;
        // MyQ can get picky about blocking user agents apparently
        userAgent = MyQAccountHandler.randomString(40);
        securityToken = null;
        updateStatus(ThingStatus.UNKNOWN);
        restartPolls(false);
    }

    @Override
    public void dispose() {
        stopPolls();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(MyQDiscoveryService.class);
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        DevicesDTO localDeviceCaches = devicesCache;
        if (localDeviceCaches != null && childHandler instanceof MyQDeviceHandler) {
            MyQDeviceHandler handler = (MyQDeviceHandler) childHandler;
            localDeviceCaches.items.stream()
                    .filter(d -> ((MyQDeviceHandler) childHandler).getSerialNumber().equalsIgnoreCase(d.serialNumber))
                    .findFirst().ifPresent(handler::handleDeviceUpdate);
        }
    }

    /**
     * Sends an action to the MyQ API
     *
     * @param serialNumber
     * @param action
     */
    public void sendAction(String serialNumber, String action) {
        AccountDTO localAccount = account;
        if (localAccount != null) {
            try {
                HttpResult result = sendRequest(
                        String.format("%s/v5.1/Accounts/%s/Devices/%s/actions", BASE_URL, localAccount.account.id,
                                serialNumber),
                        HttpMethod.PUT, securityToken,
                        new StringContentProvider(gsonLowerCase.toJson(new ActionDTO(action))), "application/json");
                if (HttpStatus.isSuccess(result.responseCode)) {
                    restartPolls(true);
                } else {
                    logger.debug("Failed to send action {} : {}", action, result.content);
                }
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * Last known state of MyQ Devices
     *
     * @return cached MyQ devices
     */
    public @Nullable DevicesDTO devicesCache() {
        return devicesCache;
    }

    private void stopPolls() {
        stopNormalPoll();
        stopRapidPoll();
    }

    private synchronized void stopNormalPoll() {
        stopFuture(normalPollFuture);
        normalPollFuture = null;
    }

    private synchronized void stopRapidPoll() {
        stopFuture(rapidPollFuture);
        rapidPollFuture = null;
    }

    private void stopFuture(@Nullable Future<?> future) {
        if (future != null) {
            future.cancel(true);
        }
    }

    private synchronized void restartPolls(boolean rapid) {
        stopPolls();
        if (rapid) {
            normalPollFuture = scheduler.scheduleWithFixedDelay(this::normalPoll, 35, normalRefreshSeconds,
                    TimeUnit.SECONDS);
            rapidPollFuture = scheduler.scheduleWithFixedDelay(this::rapidPoll, 3, RAPID_REFRESH_SECONDS,
                    TimeUnit.SECONDS);
        } else {
            normalPollFuture = scheduler.scheduleWithFixedDelay(this::normalPoll, 0, normalRefreshSeconds,
                    TimeUnit.SECONDS);
        }
    }

    private void normalPoll() {
        stopRapidPoll();
        fetchData();
    }

    private void rapidPoll() {
        fetchData();
    }

    private synchronized void fetchData() {
        try {
            if (securityToken == null) {
                login();
                if (securityToken != null) {
                    getAccount();
                }
            }
            if (securityToken != null) {
                getDevices();
            }
        } catch (InterruptedException e) {
        }
    }

    private void login() throws InterruptedException {
        HttpResult result = sendRequest(BASE_URL + "/v5/Login", HttpMethod.POST, null,
                new StringContentProvider(gsonUpperCase.toJson(new LoginRequestDTO(username, password))),
                "application/json");
        LoginResponseDTO loginResponse = parseResultAndUpdateStatus(result, gsonUpperCase, LoginResponseDTO.class);
        if (loginResponse != null) {
            securityToken = loginResponse.securityToken;
        } else {
            securityToken = null;
            if (thing.getStatusInfo().getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR) {
                // bad credentials, stop trying to login
                stopPolls();
            }
        }
    }

    private void getAccount() throws InterruptedException {
        HttpResult result = sendRequest(BASE_URL + "/v5/My?expand=account", HttpMethod.GET, securityToken, null, null);
        account = parseResultAndUpdateStatus(result, gsonUpperCase, AccountDTO.class);
    }

    private void getDevices() throws InterruptedException {
        AccountDTO localAccount = account;
        if (localAccount == null) {
            return;
        }
        HttpResult result = sendRequest(String.format("%s/v5.1/Accounts/%s/Devices", BASE_URL, localAccount.account.id),
                HttpMethod.GET, securityToken, null, null);
        DevicesDTO devices = parseResultAndUpdateStatus(result, gsonLowerCase, DevicesDTO.class);
        if (devices != null) {
            devicesCache = devices;
            devices.items.forEach(device -> {
                ThingTypeUID thingTypeUID = new ThingTypeUID(BINDING_ID, device.deviceFamily);
                if (SUPPORTED_DISCOVERY_THING_TYPES_UIDS.contains(thingTypeUID)) {
                    for (Thing thing : getThing().getThings()) {
                        ThingHandler handler = thing.getHandler();
                        if (handler != null && ((MyQDeviceHandler) handler).getSerialNumber()
                                .equalsIgnoreCase(device.serialNumber)) {
                            ((MyQDeviceHandler) handler).handleDeviceUpdate(device);
                        }
                    }
                }
            });
        }
    }

    private synchronized HttpResult sendRequest(String url, HttpMethod method, @Nullable String token,
            @Nullable ContentProvider content, @Nullable String contentType) throws InterruptedException {
        try {
            Request request = httpClient.newRequest(url).method(method)
                    .header("MyQApplicationId", "JVM/G9Nwih5BwKgNCjLxiFUQxQijAebyyg8QUHr7JOrP+tuPb8iHfRHKwTmDzHOu")
                    .header("ApiVersion", "5.1").header("BrandId", "2").header("Culture", "en").agent(userAgent)
                    .timeout(10, TimeUnit.SECONDS);
            if (token != null) {
                request = request.header("SecurityToken", token);
            }
            if (content != null & contentType != null) {
                request = request.content(content, contentType);
            }
            // use asyc jetty as the API service will response with a 401 error when credentials are wrong,
            // but not a WWW-Authenticate header which causes Jetty to throw a generic execution exception which
            // prevents us from knowing the response code
            logger.trace("Sending {} to {}", request.getMethod(), request.getURI());
            final CompletableFuture<HttpResult> futureResult = new CompletableFuture<>();
            request.send(new BufferingResponseListener() {
                @NonNullByDefault({})
                @Override
                public void onComplete(Result result) {
                    futureResult.complete(new HttpResult(result.getResponse().getStatus(), getContentAsString()));
                }
            });
            HttpResult result = futureResult.get();
            logger.trace("Account Response - status: {} content: {}", result.responseCode, result.content);
            return result;
        } catch (ExecutionException e) {
            return new HttpResult(0, e.getMessage());
        }
    }

    @Nullable
    private <T> T parseResultAndUpdateStatus(HttpResult result, Gson parser, Class<T> classOfT) {
        if (HttpStatus.isSuccess(result.responseCode)) {
            try {
                T responseObject = parser.fromJson(result.content, classOfT);
                if (responseObject != null) {
                    if (getThing().getStatus() != ThingStatus.ONLINE) {
                        updateStatus(ThingStatus.ONLINE);
                    }
                    return responseObject;
                }
            } catch (JsonSyntaxException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Invalid JSON Response " + result.content);
            }
        } else if (result.responseCode == HttpStatus.UNAUTHORIZED_401) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Unauthorized - Check Credentials");
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Invalid Response Code " + result.responseCode + " : " + result.content);
        }
        return null;
    }

    private class HttpResult {
        public final int responseCode;
        public @Nullable String content;

        public HttpResult(int responseCode, @Nullable String content) {
            this.responseCode = responseCode;
            this.content = content;
        }
    }

    private static String randomString(int length) {
        int low = 97; // a-z
        int high = 122; // A-Z
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append((char) (low + (int) (random.nextFloat() * (high - low + 1))));
        }
        return sb.toString();
    }
}
