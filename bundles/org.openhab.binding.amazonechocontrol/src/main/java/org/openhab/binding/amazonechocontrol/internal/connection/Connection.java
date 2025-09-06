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
package org.openhab.binding.amazonechocontrol.internal.connection;

import static org.eclipse.jetty.http.HttpStatus.NO_CONTENT_204;
import static org.eclipse.jetty.http.HttpStatus.OK_200;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.CAPABILITY_REGISTRATION;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants;
import org.openhab.binding.amazonechocontrol.internal.ConnectionException;
import org.openhab.binding.amazonechocontrol.internal.dto.AscendingAlarmModelTO;
import org.openhab.binding.amazonechocontrol.internal.dto.CookieTO;
import org.openhab.binding.amazonechocontrol.internal.dto.DeviceNotificationStateTO;
import org.openhab.binding.amazonechocontrol.internal.dto.DeviceTO;
import org.openhab.binding.amazonechocontrol.internal.dto.DoNotDisturbDeviceStatusTO;
import org.openhab.binding.amazonechocontrol.internal.dto.EnabledFeedTO;
import org.openhab.binding.amazonechocontrol.internal.dto.EnabledFeedsTO;
import org.openhab.binding.amazonechocontrol.internal.dto.EqualizerTO;
import org.openhab.binding.amazonechocontrol.internal.dto.NotificationSoundTO;
import org.openhab.binding.amazonechocontrol.internal.dto.NotificationStateTO;
import org.openhab.binding.amazonechocontrol.internal.dto.NotificationTO;
import org.openhab.binding.amazonechocontrol.internal.dto.PlaySearchPhraseTO;
import org.openhab.binding.amazonechocontrol.internal.dto.TOMapper;
import org.openhab.binding.amazonechocontrol.internal.dto.request.AnnouncementTO;
import org.openhab.binding.amazonechocontrol.internal.dto.request.AuthRegisterTO;
import org.openhab.binding.amazonechocontrol.internal.dto.request.BehaviorOperationValidateTO;
import org.openhab.binding.amazonechocontrol.internal.dto.request.ExchangeTokenTO;
import org.openhab.binding.amazonechocontrol.internal.dto.request.StartRoutineTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.AscendingAlarmModelsTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.AuthRegisterResponseTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.AuthRegisterTokensTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.AuthTokenTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.AutomationPayloadTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.AutomationTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.AutomationTriggerTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.BluetoothStateTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.BluetoothStatesTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.BootstrapAuthenticationTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.BootstrapTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.CustomerHistoryRecordTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.CustomerHistoryRecordsTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.DeviceListTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.DeviceNotificationStatesTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.DoNotDisturbDeviceStatusesTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.EndpointTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.ListItemTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.ListMediaSessionTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.MediaSessionTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.MusicProviderTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.NamedListsInfoTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.NamedListsItemsTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.NotificationListResponseTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.NotificationSoundResponseTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.PlayerStateTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.UsersMeTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.WakeWordTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.WakeWordsTO;
import org.openhab.binding.amazonechocontrol.internal.dto.smarthome.JsonSmartHomeDevice;
import org.openhab.binding.amazonechocontrol.internal.dto.smarthome.SmartHomeBaseDevice;
import org.openhab.binding.amazonechocontrol.internal.util.HttpRequestBuilder;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unbescape.json.JsonEscape;
import org.unbescape.json.JsonEscapeLevel;
import org.unbescape.json.JsonEscapeType;
import org.unbescape.xml.XmlEscape;
import org.unbescape.xml.XmlEscapeLevel;
import org.unbescape.xml.XmlEscapeType;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link Connection} is responsible for the connection to the amazon server and handling of the commands
 *
 * @author Michael Geramb - Initial contribution
 * @author Jan N. Klug - Refactored to use jetty client, add {@link HttpRequestBuilder}
 */
@NonNullByDefault
public class Connection {
    private static final String THING_THREADPOOL_NAME = "thingHandler";
    private static final long EXPIRES_IN = 432000; // five days

    private final Logger logger = LoggerFactory.getLogger(Connection.class);

    protected final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool(THING_THREADPOOL_NAME);

    private final Gson gson;

    private LoginData loginData;
    private CookieManager cookieManager = new CookieManager();
    private final HttpRequestBuilder requestBuilder;
    private @Nullable Date verifyTime;
    private long connectionExpiryTime = 0;
    private long accessTokenExpiryTime = 0;
    private @Nullable String customerName;
    private @Nullable String accessToken;

    private final Map<Integer, AnnouncementWrapper> announcements = Collections.synchronizedMap(new LinkedHashMap<>());
    private final Map<Integer, TextWrapper> textToSpeeches = Collections.synchronizedMap(new LinkedHashMap<>());
    private final Map<Integer, TextWrapper> textCommands = Collections.synchronizedMap(new LinkedHashMap<>());

    private final Map<Integer, Volume> volumes = Collections.synchronizedMap(new LinkedHashMap<>());
    private final Map<String, LinkedBlockingQueue<QueueObject>> devices = Collections
            .synchronizedMap(new LinkedHashMap<>());

    private final Map<TimerType, ScheduledFuture<?>> timers = new ConcurrentHashMap<>();
    private final Map<TimerType, Lock> locks = new ConcurrentHashMap<>();

    private enum TimerType {
        ANNOUNCEMENT,
        TTS,
        VOLUME,
        DEVICES,
        TEXT_COMMAND
    }

    public Connection(@Nullable Connection oldConnection, Gson gson, HttpClient httpClient) {
        this.gson = gson;

        this.requestBuilder = new HttpRequestBuilder(httpClient, cookieManager, gson);
        if (oldConnection != null) {
            LoginData oldLoginData = oldConnection.getLoginData();
            this.loginData = new LoginData(cookieManager, oldLoginData.getDeviceId(), oldLoginData.getFrc(),
                    oldLoginData.getSerial());
        } else {
            this.loginData = new LoginData(cookieManager);
        }

        replaceTimer(TimerType.DEVICES,
                scheduler.scheduleWithFixedDelay(this::handleExecuteSequenceNode, 0, 500, TimeUnit.MILLISECONDS));
    }

    public HttpRequestBuilder getRequestBuilder() {
        return requestBuilder;
    }

    public LoginData getLoginData() {
        // update cookies
        return loginData;
    }

    public @Nullable Date getVerifyTime() {
        return verifyTime;
    }

    public String getRetailDomain() {
        return loginData.getRetailDomain();
    }

    public String getRetailUrl() {
        return loginData.getRetailUrl();
    }

    public String getAlexaServer() {
        return this.loginData.getWebsiteApiUrl();
    }

    public String getCustomerName() {
        return Objects.requireNonNullElse(customerName, "Unknown");
    }

    public boolean isSequenceNodeQueueRunning() {
        return devices.values().stream().anyMatch(
                (queueObjects) -> (queueObjects.stream().anyMatch(queueObject -> queueObject.future != null)));
    }

    public boolean restoreLogin(@Nullable String data, @Nullable String overloadedDomain) {
        try {
            // verify stored data
            if (data != null && !data.isEmpty() && loginData.deserialize(data)) {
                if (overloadedDomain != null) {
                    loginData.setRetailDomain(overloadedDomain);
                }
                renewTokens();
                if (verifyLogin()) {
                    return true;
                }
            }
        } catch (ConnectionException e) {
            // no action
        }
        this.loginData.setLoginTime(null);
        return false;
    }

    private boolean tryGetBootstrap() {
        try {
            BootstrapTO result = requestBuilder.get(getAlexaServer() + "/api/bootstrap").retry(false).redirect(false)
                    .syncSend(BootstrapTO.class);
            BootstrapAuthenticationTO authentication = result.authentication;
            if (authentication != null && authentication.authenticated) {
                this.customerName = authentication.customerName;
                this.loginData.setAccountCustomerId(authentication.customerId);
                return authentication.authenticated;
            }
        } catch (ConnectionException e) {
            logger.debug("Bootstrapping failed", e);
        }
        return false;
    }

    public boolean registerConnectionAsApp(String accessToken) {
        try {
            List<CookieTO> webSiteCookies = cookieManager.getCookieStore().get(URI.create("https://www.amazon.com"))
                    .stream().map(TOMapper::mapCookie).toList();

            AuthRegisterTO registerAppRequest = new AuthRegisterTO();
            registerAppRequest.registrationData.deviceSerial = loginData.getSerial();
            registerAppRequest.authData.accessToken = accessToken;
            registerAppRequest.userContextMap = Map.of("frc", loginData.getFrc());
            registerAppRequest.cookies.webSiteCookies = webSiteCookies;

            AuthRegisterResponseTO registerAppResponse = requestBuilder.post("https://api.amazon.com/auth/register")
                    .withContent(registerAppRequest)
                    .withHeaders(Map.of("x-amzn-identity-auth-domain", "api.amazon.com")).syncSend(
                            org.openhab.binding.amazonechocontrol.internal.dto.response.AuthRegisterTO.class).response;

            AuthRegisterTokensTO tokens = registerAppResponse.success.tokens;
            String refreshToken = tokens.bearer.refreshToken;

            this.loginData.setRefreshToken(refreshToken);

            if (refreshToken == null || refreshToken.isBlank()) {
                logger.warn("Could not determine refreshToken while trying to register as app.");
                return false;
            }

            exchangeToken(getRetailDomain());
            // Check which is the owner domain
            UsersMeTO usersMeResponse = requestBuilder.get("https://alexa.amazon.com/api/users/me?platform=ios&version="
                    + AmazonEchoControlBindingConstants.API_VERSION).syncSend(UsersMeTO.class);

            // Switch to owner domain
            exchangeToken(usersMeResponse.marketPlaceDomainName);
            EndpointTO endpoints = requestBuilder.get("https://alexa.amazon.com/api/endpoints")
                    .syncSend(EndpointTO.class);
            this.loginData.setRetailDomain(endpoints.retailDomain);
            this.loginData.setWebsiteApiUrl(endpoints.websiteApiUrl);
            this.loginData.setRetailUrl(endpoints.retailUrl);

            HttpRequestBuilder.HttpResponse response = requestBuilder
                    .put("https://api.amazonalexa.com/v1/devices/@self/capabilities")
                    .withContent(CAPABILITY_REGISTRATION)
                    .withHeaders(Map.of(HttpHeader.AUTHORIZATION.toString(), "Bearer " + tokens.bearer.accessToken))
                    .retry(false).syncSend();
            if (response.statusCode() != NO_CONTENT_204 && response.statusCode() != OK_200) {
                logger.warn("Registering capabilities failed, HTTP/2 stream will not work");
            }

            tryGetBootstrap();
            this.loginData.setDeviceName(registerAppResponse.success.extensions.deviceInfo.deviceName);

            return true;
        } catch (Exception e) {
            logger.warn("Registering as app failed: {}", e.getMessage());
            logout(false);
            return false;
        }
    }

    private void exchangeToken(String cookieDomain) throws ConnectionException {
        this.connectionExpiryTime = 0;
        String cookiesJson = "{\"cookies\":{\"." + cookieDomain + "\":[]}}";
        String cookiesBase64 = Base64.getEncoder().encodeToString(cookiesJson.getBytes());

        String exchangePostData = "di.os.name=iOS" //
                + "&app_version=" + AmazonEchoControlBindingConstants.API_VERSION //
                + "&domain=." + getRetailDomain() //
                + "&source_token=" + URLEncoder.encode(this.loginData.getRefreshToken(), StandardCharsets.UTF_8) //
                + "&requested_token_type=auth_cookies" //
                + "&source_token_type=refresh_token" //
                + "&di.hw.version=iPhone" //
                + "&di.sdk.version=" + AmazonEchoControlBindingConstants.DI_SDK_VERSION //
                + "&cookies=" + cookiesBase64 //
                + "&app_name=Amazon%20Alexa" //
                + "&di.os.version=" + AmazonEchoControlBindingConstants.DI_OS_VERSION;

        String url = getRetailUrl() + "/ap/exchangetoken";

        ExchangeTokenTO exchangeToken = requestBuilder.post(url).withContent(exchangePostData).withHeader("Cookie", "")
                .syncSend(ExchangeTokenTO.class);

        CookieStore cookieStore = cookieManager.getCookieStore();
        exchangeToken.response.tokens.cookies
                .forEach((domain, cookies) -> cookies.stream().map(cookie -> TOMapper.mapCookie(cookie, domain))
                        .forEach(httpCookie -> cookieStore.add(null, httpCookie)));

        if (!verifyLogin()) {
            throw new ConnectionException("Verify login failed after token exchange");
        }

        // renew at 80% expired
        this.connectionExpiryTime = System.currentTimeMillis() + (long) (Connection.EXPIRES_IN * 1000d / 0.8d);
    }

    public String getAccessToken() throws ConnectionException {
        String accessToken = this.accessToken;
        if (accessToken == null) {
            throw new ConnectionException("accessToken not set");
        }
        return accessToken;
    }

    /**
     * Check if tokens need to be renewed
     * <p />
     * The {@link #accessToken} is renewed when the current nextAlarmTime is above
     * {@link #accessTokenExpiryTime}, additionally the session tokens/cookies are renewed when the current
     * nextAlarmTime is
     * above {@link #connectionExpiryTime}
     *
     * @return {@code true} when the session tokens have been renewed, {@code false} otherwise
     * @throws ConnectionException when an error occurred
     */
    public boolean renewTokens() throws ConnectionException {
        if (System.currentTimeMillis() >= this.accessTokenExpiryTime) {
            String renewTokenPostData = "app_name=Amazon%20Alexa" //
                    + "&app_version=" + AmazonEchoControlBindingConstants.API_VERSION //
                    + "&di.sdk.version=" + AmazonEchoControlBindingConstants.DI_SDK_VERSION //
                    + "&source_token=" + URLEncoder.encode(loginData.getRefreshToken(), StandardCharsets.UTF_8) //
                    + "&package_name=com.amazon.echo" //
                    + "&di.hw.version=iPhone" //
                    + "&platform=iOS" //
                    + "&requested_token_type=access_token"//
                    + "&source_token_type=refresh_token" //
                    + "&di.os.name=iOS" //
                    + "&di.os.version=" + AmazonEchoControlBindingConstants.DI_OS_VERSION //
                    + "&current_version=6.12.4";

            AuthTokenTO tokenResponse = requestBuilder.post("https://api.amazon.com/auth/token")
                    .withContent(renewTokenPostData).syncSend(AuthTokenTO.class);

            String accessToken = tokenResponse.accessToken;
            this.accessToken = accessToken;
            if (accessToken == null) {
                throw new ConnectionException("Failed to renew access token, no token received.");
            }

            // renew at 80% expired
            this.accessTokenExpiryTime = System.currentTimeMillis() + (long) ((tokenResponse.expiresIn * 1000.0) / 0.8);

            if (System.currentTimeMillis() > this.connectionExpiryTime) {
                exchangeToken(loginData.getRetailDomain());
            }
        }
        return false;
    }

    public boolean isLoggedIn() {
        return loginData.getLoginTime() != null;
    }

    public String getLoginPage() throws ConnectionException {
        // clear session data
        logout(false);

        logger.debug("Start Login to {}", getAlexaServer());

        String mapMdJson = "{\"device_user_dictionary\":[],\"device_registration_data\":{\"software_version\":\"1\"},\"app_identifier\":{\"app_version\":\"2.2.443692\",\"bundle_id\":\"com.amazon.echo\"}}";
        String mapMdCookie = Base64.getEncoder().encodeToString(mapMdJson.getBytes());

        cookieManager.getCookieStore().add(URI.create("https://www.amazon.com"), new HttpCookie("map-md", mapMdCookie));
        cookieManager.getCookieStore().add(URI.create("https://www.amazon.com"),
                new HttpCookie("frc", loginData.getFrc()));

        String url = "https://www.amazon.com/ap/signin" //
                + "?openid.return_to=https://www.amazon.com/ap/maplanding" //
                + "&openid.assoc_handle=amzn_dp_project_dee_ios" //
                + "&openid.identity=http://specs.openid.net/auth/2.0/identifier_select" //
                + "&pageId=amzn_dp_project_dee_ios" //
                + "&accountStatusPolicy=P1" //
                + "&openid.claimed_id=http://specs.openid.net/auth/2.0/identifier_select" //
                + "&openid.mode=checkid_setup" //
                + "&openid.ns.oa2=http://www.amazon.com/ap/ext/oauth/2" //
                + "&openid.oa2.client_id=device:" + loginData.getDeviceId() //
                + "&openid.ns.pape=http://specs.openid.net/extensions/pape/1.0" //
                + "&openid.oa2.response_type=token" //
                + "&openid.ns=http://specs.openid.net/auth/2.0&openid.pape.max_auth_age=0" //
                + "&openid.oa2.scope=device_auth_access";

        return requestBuilder.get(url).withHeader("authority", "www.amazon.com").syncSend(String.class);
    }

    public boolean verifyLogin() throws ConnectionException {
        if (this.loginData.getRefreshToken() == null || !tryGetBootstrap()) {
            verifyTime = null;
            return false;
        }
        verifyTime = new Date();
        if (loginData.getLoginTime() == null) {
            loginData.setLoginTime(verifyTime);
        }
        return true;
    }

    // current value in compute can be null
    private void replaceTimer(TimerType type, @Nullable ScheduledFuture<?> newTimer) {
        timers.compute(type, (timerType, oldTimer) -> {
            if (oldTimer != null) {
                oldTimer.cancel(true);
            }
            return newTimer;
        });
    }

    public void logout(boolean reset) {
        if (reset) {
            cookieManager = new CookieManager();
            loginData = new LoginData(cookieManager);
        } else {
            cookieManager.getCookieStore().removeAll();
            // reset all members
            loginData.setRefreshToken(null);
            loginData.setLoginTime(null);
        }

        verifyTime = null;
        connectionExpiryTime = 0;
        accessTokenExpiryTime = 0;
        customerName = null;
        accessToken = null;

        replaceTimer(TimerType.ANNOUNCEMENT, null);
        announcements.clear();
        replaceTimer(TimerType.TTS, null);
        textToSpeeches.clear();
        replaceTimer(TimerType.VOLUME, null);
        volumes.clear();
        replaceTimer(TimerType.DEVICES, null);
        textCommands.clear();
        replaceTimer(TimerType.TTS, null);

        devices.values().forEach((queueObjects) -> queueObjects.forEach((queueObject) -> {
            Future<?> future = queueObject.future;
            if (future != null) {
                future.cancel(true);
                queueObject.future = null;
            }
        }));
    }

    // commands and states
    public List<WakeWordTO> getWakeWords() {
        try {
            return requestBuilder.get(getAlexaServer() + "/api/wake-word?cached=true")
                    .syncSend(WakeWordsTO.class).wakeWords;
        } catch (ConnectionException e) {
            logger.info("Getting wake words failed", e);
        }
        return List.of();
    }

    public List<DeviceTO> getDeviceList() throws ConnectionException {
        DeviceListTO devices = requestBuilder.get(getAlexaServer() + "/api/devices-v2/device?cached=false")
                .syncSend(DeviceListTO.class);
        // @Nullable because of a limitation of the null-checker, we filter null-serialNumbers before
        Set<@Nullable String> serialNumbers = ConcurrentHashMap.newKeySet();
        return devices.devices.stream().filter(d -> d.serialNumber != null && serialNumbers.add(d.serialNumber))
                .toList();
    }

    public Map<String, JsonArray> getSmartHomeDeviceStatesJson(Set<SmartHomeBaseDevice> devices)
            throws ConnectionException {
        JsonObject requestObject = new JsonObject();
        JsonArray stateRequests = new JsonArray();
        Map<String, String> mergedApplianceMap = new HashMap<>();
        for (SmartHomeBaseDevice device : devices) {
            String applianceId = device.findId();
            if (applianceId != null) {
                JsonObject stateRequest;
                if (device instanceof JsonSmartHomeDevice
                        && ((JsonSmartHomeDevice) device).mergedApplianceIds != null) {
                    List<String> mergedApplianceIds = Objects
                            .requireNonNullElse(((JsonSmartHomeDevice) device).mergedApplianceIds, List.of());
                    for (String idToMerge : mergedApplianceIds) {
                        mergedApplianceMap.put(idToMerge, applianceId);
                        stateRequest = new JsonObject();
                        stateRequest.addProperty("entityId", idToMerge);
                        stateRequest.addProperty("entityType", "APPLIANCE");
                        stateRequests.add(stateRequest);
                    }
                } else {
                    stateRequest = new JsonObject();
                    stateRequest.addProperty("entityId", applianceId);
                    stateRequest.addProperty("entityType", "APPLIANCE");
                    stateRequests.add(stateRequest);
                }
            }
        }
        requestObject.add("stateRequests", stateRequests);
        JsonObject responseObject = requestBuilder.post(getAlexaServer() + "/api/phoenix/state")
                .withContent(requestObject).syncSend(JsonObject.class);

        JsonArray deviceStates = (JsonArray) responseObject.get("deviceStates");
        Map<String, JsonArray> result = new HashMap<>();
        for (JsonElement deviceState : deviceStates) {
            JsonObject deviceStateObject = deviceState.getAsJsonObject();
            JsonObject entity = deviceStateObject.get("entity").getAsJsonObject();
            String applianceId = entity.get("entityId").getAsString();
            JsonElement capabilityState = deviceStateObject.get("capabilityStates");
            if (capabilityState != null && capabilityState.isJsonArray()) {
                String realApplianceId = mergedApplianceMap.get(applianceId);
                if (realApplianceId != null) {
                    var capabilityArray = result.get(realApplianceId);
                    if (capabilityArray != null) {
                        capabilityArray.addAll(capabilityState.getAsJsonArray());
                        result.put(realApplianceId, capabilityArray);
                    } else {
                        result.put(realApplianceId, capabilityState.getAsJsonArray());
                    }
                } else {
                    result.put(applianceId, capabilityState.getAsJsonArray());
                }
            }
        }
        return result;
    }

    public PlayerStateTO getPlayerState(DeviceTO device) throws ConnectionException {
        return requestBuilder.get(getAlexaServer() + "/api/np/player?deviceSerialNumber=" + device.serialNumber
                + "&deviceType=" + device.deviceType + "&screenWidth=1440").syncSend(PlayerStateTO.class);
    }

    public List<MediaSessionTO> getMediaSessions(DeviceTO device) {
        try {
            String url = getAlexaServer() + "/api/np/list-media-sessions?deviceSerialNumber=" + device.serialNumber
                    + "&deviceType=" + device.deviceType;
            return requestBuilder.get(url).syncSend(ListMediaSessionTO.class).mediaSessionList;
        } catch (ConnectionException e) {
            logger.warn("Failed to update media sessions for {}: {}", device.serialNumber, e.getMessage());
        }
        return List.of();
    }

    public List<CustomerHistoryRecordTO> getActivities(long startTime, long endTime) {
        try {
            String url = getRetailUrl() + "/alexa-privacy/apd/rvh/customer-history-records?startTime=" + startTime
                    + "&endTime=" + endTime + "&maxRecordSize=1";
            CustomerHistoryRecordsTO customerHistoryRecords = requestBuilder.get(url)
                    .syncSend(CustomerHistoryRecordsTO.class);
            return customerHistoryRecords.customerHistoryRecords.stream()
                    .filter(r -> !"DEVICE_ARBITRATION".equals(r.utteranceType))
                    .sorted(Comparator.comparing(r -> r.timestamp)).toList();
        } catch (ConnectionException e) {
            logger.info("getting activities failed", e);
        }
        return List.of();
    }

    public @Nullable NamedListsInfoTO getNamedListInfo(String listId) {
        try {
            String url = getAlexaServer() + "/api/namedLists/" + listId + "?_=" + System.currentTimeMillis();
            return requestBuilder.get(url).syncSend(NamedListsInfoTO.class);
        } catch (ConnectionException e) {
            logger.info("getting information for list {} failed", listId, e);
        }
        return null;
    }

    public List<ListItemTO> getNamedListItems(String listId) {
        try {
            String url = getAlexaServer() + "/api/namedLists/" + listId + "/items?_=" + System.currentTimeMillis();
            return requestBuilder.get(url).syncSend(NamedListsItemsTO.class).list;
        } catch (ConnectionException e) {
            logger.info("getting items from list '{}' failed", listId, e);
        }
        return List.of();
    }

    public List<BluetoothStateTO> getBluetoothConnectionStates() {
        try {
            String url = getAlexaServer() + "/api/bluetooth?cached=true";
            return requestBuilder.get(url).syncSend(BluetoothStatesTO.class).bluetoothStates;
        } catch (ConnectionException e) {
            logger.debug("Failed to get bluetooth state: {}", e.getMessage());
            return List.of();
        }
    }

    public void command(DeviceTO device, Object command) throws ConnectionException {
        String url = getAlexaServer() + "/api/np/command?deviceSerialNumber=" + device.serialNumber + "&deviceType="
                + device.deviceType;
        requestBuilder.post(url).withContent(command).retry(false).syncSend();
    }

    public void smartHomeCommand(String entityId, String action, Map<String, Object> values)
            throws IOException, InterruptedException {
        String url = getAlexaServer() + "/api/phoenix/state";

        JsonObject json = new JsonObject();
        JsonArray controlRequests = new JsonArray();
        JsonObject controlRequest = new JsonObject();
        controlRequest.addProperty("entityId", entityId);
        controlRequest.addProperty("entityType", "APPLIANCE");
        JsonObject parameters = new JsonObject();
        parameters.addProperty("action", action);
        if (!values.isEmpty()) {
            values.forEach((property, value) -> {
                if (value instanceof QuantityType<?>) {
                    JsonObject propertyObj = new JsonObject();
                    propertyObj.addProperty("value", Double.toString(((QuantityType<?>) value).doubleValue()));
                    propertyObj.addProperty("scale",
                            ((QuantityType<?>) value).getUnit().equals(SIUnits.CELSIUS) ? "celsius" : "fahrenheit");
                    parameters.add(property, propertyObj);
                } else if (value instanceof Boolean) {
                    parameters.addProperty(property, (boolean) value);
                } else if (value instanceof String) {
                    parameters.addProperty(property, (String) value);
                } else if (value instanceof StringType) {
                    JsonObject propertyObj = new JsonObject();
                    propertyObj.addProperty("value", value.toString());
                    parameters.add(property, propertyObj);
                } else if (value instanceof Number) {
                    parameters.addProperty(property, (Number) value);
                } else if (value instanceof Character) {
                    parameters.addProperty(property, (Character) value);
                } else if (value instanceof JsonElement) {
                    parameters.add(property, (JsonElement) value);
                }
            });
        }
        controlRequest.add("parameters", parameters);
        controlRequests.add(controlRequest);
        json.add("controlRequests", controlRequests);

        try {
            JsonObject result = requestBuilder.put(url).withContent(json).syncSend(JsonObject.class);
            if (result == null) {
                return;
            }
            JsonElement errors = result.get("errors");
            if (errors != null && errors.isJsonArray()) {
                JsonArray errorList = errors.getAsJsonArray();
                if (!errorList.isEmpty()) {
                    logger.warn("Smart home device command failed. {}",
                            StreamSupport.stream(errorList.spliterator(), false).map(JsonElement::toString)
                                    .collect(Collectors.joining(" / ")));
                }
            }
        } catch (ConnectionException e) {
            logger.warn("Request to URL '{}' failed: {}", url, e.getMessage());
        }
    }

    public void setNotificationVolume(DeviceTO device, int volume) throws ConnectionException {
        String url = getAlexaServer() + "/api/device-notification-state/" + device.deviceType + "/"
                + device.softwareVersion + "/" + device.serialNumber;
        NotificationStateTO command = new NotificationStateTO();
        command.deviceSerialNumber = device.serialNumber;
        command.deviceType = device.deviceType;
        command.deviceSerialNumber = device.softwareVersion;
        command.volumeLevel = volume;
        requestBuilder.put(url).withContent(command).retry(false).syncSend();
    }

    public void setAscendingAlarm(DeviceTO device, boolean ascendingAlarm) throws ConnectionException {
        String url = getAlexaServer() + "/api/ascending-alarm/" + device.serialNumber;
        AscendingAlarmModelTO command = new AscendingAlarmModelTO();
        command.ascendingAlarmEnabled = ascendingAlarm;
        command.deviceSerialNumber = device.serialNumber;
        command.deviceType = device.deviceType;
        requestBuilder.put(url).withContent(command).retry(false).syncSend();
    }

    public void setDoNotDisturb(DeviceTO device, boolean doNotDisturb) throws ConnectionException {
        String url = getAlexaServer() + "/api/dnd/status";
        DoNotDisturbDeviceStatusTO command = new DoNotDisturbDeviceStatusTO();
        command.enabled = doNotDisturb;
        command.deviceSerialNumber = device.serialNumber;
        command.deviceType = device.deviceType;
        requestBuilder.put(url).withContent(command).retry(false).syncSend();
    }

    public List<DeviceNotificationStateTO> getDeviceNotificationStates() {
        try {
            return requestBuilder.get(getAlexaServer() + "/api/device-notification-state")
                    .syncSend(DeviceNotificationStatesTO.class).deviceNotificationStates;
        } catch (ConnectionException e) {
            logger.info("Error getting device notification states", e);
        }
        return List.of();
    }

    public List<AscendingAlarmModelTO> getAscendingAlarms() {
        try {
            return requestBuilder.get(getAlexaServer() + "/api/ascending-alarm")
                    .syncSend(AscendingAlarmModelsTO.class).ascendingAlarmModelList;
        } catch (ConnectionException e) {
            logger.info("Error getting ascending alarm states", e);
        }
        return List.of();
    }

    public List<DoNotDisturbDeviceStatusTO> getDoNotDisturbs() {
        try {
            return requestBuilder.get(getAlexaServer() + "/api/dnd/device-status-list")
                    .syncSend(DoNotDisturbDeviceStatusesTO.class).doNotDisturbDeviceStatusList;
        } catch (ConnectionException e) {
            logger.info("Error getting do not disturb status list", e);
        }
        return List.of();
    }

    public void bluetooth(DeviceTO device, @Nullable String address) throws ConnectionException {
        if (address == null || address.isEmpty()) {
            String url = getAlexaServer() + "/api/bluetooth/disconnect-sink/" + device.deviceType + "/"
                    + device.serialNumber;
            // disconnect
            requestBuilder.post(url).retry(false).syncSend();
        } else {
            String url = getAlexaServer() + "/api/bluetooth/pair-sink/" + device.deviceType + "/" + device.serialNumber;
            requestBuilder.post(url).withContent(Map.of("bluetoothDeviceAddress", address)).retry(false).syncSend();
        }
    }

    public void announcement(DeviceTO device, String speak, String bodyText, @Nullable String title,
            @Nullable Integer ttsVolume, @Nullable Integer standardVolume) {
        String trimmedSpeak = speak.replaceAll("\\s+", " ").trim();
        String trimmedBodyText = bodyText.replaceAll("\\s+", " ").trim();
        String plainSpeak = trimmedSpeak.replaceAll("<.+?>", "").trim();
        String plainBodyText = trimmedBodyText.replaceAll("<.+?>", "").trim();
        if (plainSpeak.isEmpty() && plainBodyText.isEmpty()) {
            return;
        }
        String escapedSpeak = trimmedSpeak.replace(plainSpeak,
                XmlEscape.escapeXml10(plainSpeak, XmlEscapeType.CHARACTER_ENTITY_REFERENCES_DEFAULT_TO_HEXA,
                        XmlEscapeLevel.LEVEL_1_ONLY_MARKUP_SIGNIFICANT));
        // we lock announcements until we have finished adding this one
        Lock lock = Objects.requireNonNull(locks.computeIfAbsent(TimerType.ANNOUNCEMENT, k -> new ReentrantLock()));
        lock.lock();
        try {
            AnnouncementWrapper announcement = Objects
                    .requireNonNull(announcements.computeIfAbsent(Objects.hash(escapedSpeak, plainBodyText, title),
                            k -> new AnnouncementWrapper(escapedSpeak, plainBodyText, title)));
            announcement.add(device, ttsVolume, standardVolume);

            // schedule an announcement only if it has not been scheduled before
            timers.computeIfAbsent(TimerType.ANNOUNCEMENT,
                    k -> scheduler.schedule(this::sendAnnouncement, 500, TimeUnit.MILLISECONDS));
        } finally {
            lock.unlock();
        }
    }

    private void sendAnnouncement() {
        // we lock new announcements until we have dispatched everything
        Lock lock = Objects.requireNonNull(locks.computeIfAbsent(TimerType.ANNOUNCEMENT, k -> new ReentrantLock()));
        lock.lock();
        try {
            Iterator<AnnouncementWrapper> iterator = announcements.values().iterator();
            while (iterator.hasNext()) {
                AnnouncementWrapper announcement = iterator.next();
                try {
                    List<DeviceTO> devices = announcement.getDevices();
                    if (!devices.isEmpty()) {
                        AnnouncementTO announcementTO = new AnnouncementTO();
                        announcementTO.content = List.of(announcement.toAnnouncementTO());
                        announcementTO.customerId = loginData.getAccountCustomerId();
                        announcementTO.target.customerId = devices.get(0).deviceOwnerCustomerId;
                        announcementTO.target.devices = devices.stream().map(TOMapper::mapAnnouncementTargetDevice)
                                .toList();
                        executeSequenceCommandWithVolume(devices, "AlexaAnnouncement",
                                TOMapper.mapToMap(gson, announcementTO), announcement.getTtsVolumes(),
                                announcement.getStandardVolumes());
                    }
                } catch (Exception e) {
                    logger.warn("send announcement fails with unexpected error", e);
                }
                iterator.remove();
            }
        } finally {
            // the timer is done anyway immediately after we unlock
            timers.remove(TimerType.ANNOUNCEMENT);
            lock.unlock();
        }
    }

    public void textToSpeech(DeviceTO device, String text, @Nullable Integer ttsVolume,
            @Nullable Integer standardVolume) {
        String trimmedText = text.replaceAll("\\s+", " ").trim();
        String plainText = trimmedText.replaceAll("<.+?>", "").trim();
        if (plainText.isEmpty()) {
            return;
        }
        String escapedText = trimmedText.replace(plainText,
                XmlEscape.escapeXml10(plainText, XmlEscapeType.CHARACTER_ENTITY_REFERENCES_DEFAULT_TO_HEXA,
                        XmlEscapeLevel.LEVEL_1_ONLY_MARKUP_SIGNIFICANT));
        // we lock TTS until we have finished adding this one
        Lock lock = Objects.requireNonNull(locks.computeIfAbsent(TimerType.TTS, k -> new ReentrantLock()));
        lock.lock();
        try {
            TextWrapper textToSpeech = Objects.requireNonNull(
                    textToSpeeches.computeIfAbsent(Objects.hash(escapedText), k -> new TextWrapper(escapedText)));
            textToSpeech.add(device, ttsVolume, standardVolume);
            // schedule a TTS only if it has not been scheduled before
            timers.computeIfAbsent(TimerType.TTS,
                    k -> scheduler.schedule(this::sendTextToSpeech, 500, TimeUnit.MILLISECONDS));
        } finally {
            lock.unlock();
        }
    }

    private void sendTextToSpeech() {
        // we lock new TTS until we have dispatched everything
        Lock lock = Objects.requireNonNull(locks.computeIfAbsent(TimerType.TTS, k -> new ReentrantLock()));
        lock.lock();
        try {
            Iterator<TextWrapper> iterator = textToSpeeches.values().iterator();
            while (iterator.hasNext()) {
                TextWrapper textToSpeech = iterator.next();
                logger.trace("Executing textToSpeech {}", textToSpeech);
                try {
                    List<DeviceTO> devices = textToSpeech.getDevices();
                    if (!devices.isEmpty()) {
                        executeSequenceCommandWithVolume(devices, "Alexa.Speak",
                                Map.of("textToSpeak", textToSpeech.getText()), textToSpeech.getTtsVolumes(),
                                textToSpeech.getStandardVolumes());
                    }
                } catch (RuntimeException e) {
                    logger.warn("Send textToSpeech failed with unexpected error", e);
                }
                iterator.remove();
            }
        } finally {
            // the timer is done anyway immediately after we unlock
            timers.remove(TimerType.TTS);
            lock.unlock();
        }
    }

    public void textCommand(DeviceTO device, String text, @Nullable Integer ttsVolume,
            @Nullable Integer standardVolume) {
        String trimmedText = text.replaceAll("\\s+", " ").trim();
        String plainText = trimmedText.replaceAll("<.+?>", "").trim();
        if (plainText.isEmpty()) {
            return;
        }
        String escapedText = trimmedText.replace(plainText, JsonEscape.escapeJson(plainText,
                JsonEscapeType.SINGLE_ESCAPE_CHARS_DEFAULT_TO_UHEXA, JsonEscapeLevel.LEVEL_1_BASIC_ESCAPE_SET));

        // we lock TextCommands until we have finished adding this one
        Lock lock = Objects.requireNonNull(locks.computeIfAbsent(TimerType.TEXT_COMMAND, k -> new ReentrantLock()));
        lock.lock();
        try {
            TextWrapper textWrapper = Objects.requireNonNull(
                    textCommands.computeIfAbsent(Objects.hash(escapedText), k -> new TextWrapper(escapedText)));
            textWrapper.add(device, ttsVolume, standardVolume);
            // schedule a TextCommand only if it has not been scheduled before
            timers.computeIfAbsent(TimerType.TEXT_COMMAND,
                    k -> scheduler.schedule(this::sendTextCommand, 500, TimeUnit.MILLISECONDS));
        } finally {
            lock.unlock();
        }
    }

    private void sendTextCommand() {
        // we lock new textCommands until we have dispatched everything
        Lock lock = Objects.requireNonNull(locks.computeIfAbsent(TimerType.TEXT_COMMAND, k -> new ReentrantLock()));
        lock.lock();

        try {
            Iterator<TextWrapper> iterator = textCommands.values().iterator();
            while (iterator.hasNext()) {
                TextWrapper textCommand = iterator.next();
                logger.trace("Executing textCommand {}", textCommand);
                try {
                    List<DeviceTO> devices = textCommand.getDevices();
                    if (!devices.isEmpty()) {
                        executeSequenceCommandWithVolume(devices, "Alexa.TextCommand",
                                Map.of("text", textCommand.getText()), textCommand.getTtsVolumes(),
                                textCommand.getStandardVolumes());
                    }
                } catch (RuntimeException e) {
                    logger.warn("Sending textCommand failed with unexpected error", e);
                }
                iterator.remove();
            }
        } finally {
            // the timer is done anyway immediately after we unlock
            timers.remove(TimerType.TEXT_COMMAND);
            lock.unlock();
        }
    }

    public void setVolume(DeviceTO device, int vol) {
        // we lock volume until we have finished adding this one
        Lock lock = Objects.requireNonNull(locks.computeIfAbsent(TimerType.VOLUME, k -> new ReentrantLock()));
        lock.lock();
        try {
            Volume volume = Objects.requireNonNull(volumes.computeIfAbsent(vol, k -> new Volume()));
            volume.devices.add(device);
            volume.volumes.add(vol);
            // schedule a TTS only if it has not been scheduled before
            timers.computeIfAbsent(TimerType.VOLUME,
                    k -> scheduler.schedule(this::sendVolume, 500, TimeUnit.MILLISECONDS));
        } finally {
            lock.unlock();
        }
    }

    private void sendVolume() {
        // we lock new volume until we have dispatched everything
        Lock lock = Objects.requireNonNull(locks.computeIfAbsent(TimerType.VOLUME, k -> new ReentrantLock()));
        lock.lock();
        try {
            Iterator<Volume> iterator = volumes.values().iterator();
            while (iterator.hasNext()) {
                Volume volume = iterator.next();
                try {
                    List<DeviceTO> devices = volume.devices;
                    if (!devices.isEmpty()) {
                        executeSequenceCommandWithVolume(devices, null, Map.of(), volume.volumes, List.of());
                    }
                } catch (Exception e) {
                    logger.warn("send volume fails with unexpected error", e);
                }
                iterator.remove();
            }
        } finally {
            // the timer is done anyway immediately after we unlock
            timers.remove(TimerType.VOLUME);
            lock.unlock();
        }
    }

    private void executeSequenceCommandWithVolume(List<DeviceTO> devices, @Nullable String command,
            Map<String, Object> parameters, List<@Nullable Integer> ttsVolumes,
            List<@Nullable Integer> standardVolumes) {
        JsonArray serialNodesToExecute = new JsonArray();

        JsonArray ttsVolumeNodesToExecute = new JsonArray();
        for (int i = 0; i < devices.size(); i++) {
            Integer ttsVolume = ttsVolumes.size() > i ? ttsVolumes.get(i) : null;
            Integer standardVolume = standardVolumes.size() > i ? standardVolumes.get(i) : null;
            if (ttsVolume != null && !ttsVolume.equals(standardVolume)) {
                ttsVolumeNodesToExecute.add(createExecutionNode(devices.get(i).deviceType, devices.get(i).serialNumber,
                        "Alexa.DeviceControls.Volume", Map.of("value", ttsVolume)));
            }
        }
        if (!ttsVolumeNodesToExecute.isEmpty()) {
            JsonObject parallelNodesToExecute = new JsonObject();
            parallelNodesToExecute.addProperty("@type", "com.amazon.alexa.behaviors.model.ParallelNode");
            parallelNodesToExecute.add("nodesToExecute", ttsVolumeNodesToExecute);
            serialNodesToExecute.add(parallelNodesToExecute);
        }

        if (command != null && !parameters.isEmpty()) {
            JsonArray commandNodesToExecute = new JsonArray();
            if ("Alexa.Speak".equals(command) || "Alexa.TextCommand".equals(command)) {
                for (DeviceTO device : devices) {
                    commandNodesToExecute
                            .add(createExecutionNode(device.deviceType, device.serialNumber, command, parameters));
                }
            } else {
                commandNodesToExecute.add(createExecutionNode(devices.get(0).deviceType, devices.get(0).serialNumber,
                        command, parameters));
            }
            if (!commandNodesToExecute.isEmpty()) {
                JsonObject parallelNodesToExecute = new JsonObject();
                parallelNodesToExecute.addProperty("@type", "com.amazon.alexa.behaviors.model.ParallelNode");
                parallelNodesToExecute.add("nodesToExecute", commandNodesToExecute);
                serialNodesToExecute.add(parallelNodesToExecute);
            }
        }

        JsonArray standardVolumeNodesToExecute = new JsonArray();
        for (int i = 0; i < devices.size(); i++) {
            Integer standardVolume = standardVolumes.size() > i ? standardVolumes.get(i) : null;
            Integer ttsVolume = ttsVolumes.size() > i ? ttsVolumes.get(i) : null;
            if (ttsVolume != null && standardVolume != null && !standardVolume.equals(ttsVolume)) {
                standardVolumeNodesToExecute.add(createExecutionNode(devices.get(i).deviceType,
                        devices.get(i).serialNumber, "Alexa.DeviceControls.Volume", Map.of("value", standardVolume)));
            }
        }
        if (!standardVolumeNodesToExecute.isEmpty() && !"AlexaAnnouncement".equals(command)) {
            JsonObject parallelNodesToExecute = new JsonObject();
            parallelNodesToExecute.addProperty("@type", "com.amazon.alexa.behaviors.model.ParallelNode");
            parallelNodesToExecute.add("nodesToExecute", standardVolumeNodesToExecute);
            serialNodesToExecute.add(parallelNodesToExecute);
        }

        if (!serialNodesToExecute.isEmpty()) {
            executeSequenceNodes(devices.stream().map(d -> d.serialNumber).toList(), serialNodesToExecute, false);

            if (!standardVolumeNodesToExecute.isEmpty() && "AlexaAnnouncement".equals(command)) {
                executeSequenceNodes(devices.stream().map(d -> d.serialNumber).toList(), standardVolumeNodesToExecute,
                        true);
            }
        }
    }

    // commands: Alexa.Weather.Play, Alexa.Traffic.Play, Alexa.FlashBriefing.Play,
    // Alexa.GoodMorning.Play,
    // Alexa.SingASong.Play, Alexa.TellStory.Play, Alexa.Speak (textToSpeach)
    public void executeSequenceCommand(DeviceTO device, String command, Map<String, Object> parameters) {
        JsonObject nodeToExecute = createExecutionNode(device.deviceType, device.serialNumber, command, parameters);
        executeSequenceNode(List.of(device.serialNumber), nodeToExecute);
    }

    private void executeSequenceNode(List<String> serialNumbers, JsonObject nodeToExecute) {
        QueueObject queueObject = new QueueObject();
        queueObject.deviceSerialNumbers = serialNumbers;
        queueObject.nodeToExecute = nodeToExecute;
        List<String> serials = new ArrayList<>();
        for (String serialNumber : serialNumbers) {
            if (serialNumber != null) {
                Objects.requireNonNull(this.devices.computeIfAbsent(serialNumber, k -> new LinkedBlockingQueue<>()))
                        .offer(queueObject);
                serials.add(serialNumber);
            }
        }
        logger.debug("Added {} device(s) {} to queue", queueObject.hashCode(), serials);
    }

    private void handleExecuteSequenceNode() {
        Lock lock = Objects.requireNonNull(locks.computeIfAbsent(TimerType.DEVICES, k -> new ReentrantLock()));
        if (lock.tryLock()) {
            try {
                for (String serialNumber : devices.keySet()) {
                    LinkedBlockingQueue<QueueObject> queueObjects = devices.get(serialNumber);
                    if (queueObjects != null) {
                        QueueObject queueObject = queueObjects.peek();
                        if (queueObject != null) {
                            Future<?> future = queueObject.future;
                            if (future == null || future.isDone()) {
                                boolean execute = true;
                                List<String> serials = new ArrayList<>();
                                for (String tmpDevice : queueObject.deviceSerialNumbers) {
                                    if (Objects.equals(tmpDevice, serialNumber)) {
                                        LinkedBlockingQueue<QueueObject> tmpQueueObjects = devices.get(tmpDevice);
                                        if (tmpQueueObjects != null) {
                                            QueueObject tmpQueueObject = tmpQueueObjects.peek();
                                            Future<?> tmpFuture = null;
                                            if (tmpQueueObject != null) {
                                                tmpFuture = tmpQueueObject.future;
                                            }
                                            if (!queueObject.equals(tmpQueueObject)
                                                    || (tmpFuture != null && !tmpFuture.isDone())) {
                                                execute = false;
                                                break;
                                            }

                                            serials.add(tmpDevice);
                                        }
                                    }
                                }
                                if (execute) {
                                    queueObject.future = scheduler.submit(() -> queuedExecuteSequenceNode(queueObject));
                                    logger.debug("thread {} device(s) {}", queueObject.hashCode(), serials);
                                }
                            }
                        }
                    }
                }
            } finally {
                lock.unlock();
            }
        }
    }

    private void queuedExecuteSequenceNode(QueueObject queueObject) {
        JsonObject nodeToExecute = queueObject.nodeToExecute;
        ExecutionNodeObject executionNodeObject = getExecutionNodeObject(nodeToExecute);
        List<String> types = executionNodeObject.types;
        long delay = types.contains("Alexa.DeviceControls.Volume") ? 2000 : 0;
        delay += types.contains("Announcement") ? 3000 : 2000;

        try {
            JsonObject sequenceJson = new JsonObject();
            sequenceJson.addProperty("@type", "com.amazon.alexa.behaviors.model.Sequence");
            sequenceJson.add("startNode", nodeToExecute);

            StartRoutineTO request = new StartRoutineTO();
            request.sequenceJson = gson.toJson(sequenceJson);

            String text = executionNodeObject.text;
            if (text != null) {
                text = text.replaceAll("<.+?>", " ").replaceAll("\\s+", " ").trim();
                delay += text.length() * 150L;
            }
            requestBuilder.post(getAlexaServer() + "/api/behaviors/preview").withContent(request).syncSend();

            Thread.sleep(delay);
        } catch (ConnectionException | InterruptedException e) {
            logger.warn("execute sequence node fails with unexpected error", e);
        } finally {
            removeObjectFromQueueAfterExecutionCompletion(queueObject);
        }
    }

    private void removeObjectFromQueueAfterExecutionCompletion(QueueObject queueObject) {
        List<String> serials = new ArrayList<>();
        for (String serialNumber : queueObject.deviceSerialNumbers) {
            LinkedBlockingQueue<?> queue = devices.get(serialNumber);
            if (queue != null) {
                queue.remove(queueObject);
            }
            serials.add(serialNumber);
        }
        logger.debug("Removed {} device(s) {} from queue", queueObject.hashCode(), serials);
    }

    private void executeSequenceNodes(List<String> serialNumbers, JsonArray nodesToExecute, boolean parallel) {
        JsonObject serialNode = new JsonObject();
        if (parallel) {
            serialNode.addProperty("@type", "com.amazon.alexa.behaviors.model.ParallelNode");
        } else {
            serialNode.addProperty("@type", "com.amazon.alexa.behaviors.model.SerialNode");
        }

        serialNode.add("nodesToExecute", nodesToExecute);

        executeSequenceNode(serialNumbers, serialNode);
    }

    private JsonObject createExecutionNode(String deviceType, String serialNumber, String command,
            Map<String, Object> parameters) {
        JsonObject operationPayload = new JsonObject();
        operationPayload.addProperty("deviceType", deviceType);
        operationPayload.addProperty("deviceSerialNumber", serialNumber);
        operationPayload.addProperty("locale", "");
        operationPayload.addProperty("customerId", loginData.getAccountCustomerId());
        for (String key : parameters.keySet()) {
            Object value = parameters.get(key);
            if (value instanceof String) {
                operationPayload.addProperty(key, (String) value);
            } else if (value instanceof Number) {
                operationPayload.addProperty(key, (Number) value);
            } else if (value instanceof Boolean) {
                operationPayload.addProperty(key, (Boolean) value);
            } else if (value instanceof Character) {
                operationPayload.addProperty(key, (Character) value);
            } else {
                operationPayload.add(key, gson.toJsonTree(value));
            }
        }

        JsonObject nodeToExecute = new JsonObject();
        nodeToExecute.addProperty("@type", "com.amazon.alexa.behaviors.model.OpaquePayloadOperationNode");
        nodeToExecute.addProperty("type", command);
        if ("Alexa.TextCommand".equals(command)) {
            nodeToExecute.addProperty("skillId", "amzn1.ask.1p.tellalexa");
        }
        nodeToExecute.add("operationPayload", operationPayload);
        return nodeToExecute;
    }

    private ExecutionNodeObject getExecutionNodeObject(JsonObject nodeToExecute) {
        ExecutionNodeObject executionNodeObject = new ExecutionNodeObject();
        if (nodeToExecute.has("nodesToExecute")) {
            JsonArray serialNodesToExecute = nodeToExecute.getAsJsonArray("nodesToExecute");
            if (serialNodesToExecute != null && !serialNodesToExecute.isEmpty()) {
                for (int i = 0; i < serialNodesToExecute.size(); i++) {
                    JsonObject serialNodesToExecuteJsonObject = serialNodesToExecute.get(i).getAsJsonObject();
                    if (serialNodesToExecuteJsonObject.has("nodesToExecute")) {
                        JsonArray parallelNodesToExecute = serialNodesToExecuteJsonObject
                                .getAsJsonArray("nodesToExecute");
                        if (parallelNodesToExecute != null && !parallelNodesToExecute.isEmpty()) {
                            JsonObject parallelNodesToExecuteJsonObject = parallelNodesToExecute.get(0)
                                    .getAsJsonObject();
                            if (processNodesToExecuteJsonObject(executionNodeObject,
                                    parallelNodesToExecuteJsonObject)) {
                                break;
                            }
                        }
                    } else {
                        if (processNodesToExecuteJsonObject(executionNodeObject, serialNodesToExecuteJsonObject)) {
                            break;
                        }
                    }
                }
            }
        }

        return executionNodeObject;
    }

    private boolean processNodesToExecuteJsonObject(ExecutionNodeObject executionNodeObject,
            JsonObject nodesToExecuteJsonObject) {
        if (nodesToExecuteJsonObject.has("type")) {
            executionNodeObject.types.add(nodesToExecuteJsonObject.get("type").getAsString());
            if (nodesToExecuteJsonObject.has("operationPayload")) {
                JsonObject operationPayload = nodesToExecuteJsonObject.getAsJsonObject("operationPayload");
                if (operationPayload != null) {
                    if (operationPayload.has("textToSpeak")) {
                        executionNodeObject.text = operationPayload.get("textToSpeak").getAsString();
                        return true;
                    } else if (operationPayload.has("text")) {
                        executionNodeObject.text = operationPayload.get("text").getAsString();
                        return true;
                    } else if (operationPayload.has("content")) {
                        JsonArray content = operationPayload.getAsJsonArray("content");
                        if (content != null && !content.isEmpty()) {
                            JsonObject contentJsonObject = content.get(0).getAsJsonObject();
                            if (contentJsonObject.has("speak")) {
                                JsonObject speak = contentJsonObject.getAsJsonObject("speak");
                                if (speak != null && speak.has("value")) {
                                    executionNodeObject.text = speak.get("value").getAsString();
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public void startRoutine(DeviceTO device, String utterance) throws ConnectionException {
        AutomationTO found = null;
        String deviceLocale = "";
        List<AutomationTO> routines = getAutomations();

        for (AutomationTO routine : routines) {
            if (routine.sequence != null) {
                for (AutomationTriggerTO trigger : routine.triggers) {
                    AutomationPayloadTO payload = trigger.payload;
                    if (payload == null) {
                        continue;
                    }
                    String payloadUtterance = payload.utterance;
                    if (utterance.equalsIgnoreCase(payloadUtterance)) {
                        found = routine;
                        deviceLocale = payload.locale;
                        break;
                    }
                }
            }
        }
        if (found != null) {
            String sequenceJson = gson.toJson(found.sequence);

            StartRoutineTO request = new StartRoutineTO();
            request.behaviorId = found.automationId;

            // replace tokens
            // "deviceType":"ALEXA_CURRENT_DEVICE_TYPE"
            String deviceType = "\"deviceType\":\"ALEXA_CURRENT_DEVICE_TYPE\"";
            String newDeviceType = "\"deviceType\":\"" + device.deviceType + "\"";
            sequenceJson = sequenceJson.replace(deviceType, newDeviceType);

            // "deviceSerialNumber":"ALEXA_CURRENT_DSN"
            String deviceSerial = "\"deviceSerialNumber\":\"ALEXA_CURRENT_DSN\"";
            String newDeviceSerial = "\"deviceSerialNumber\":\"" + device.serialNumber + "\"";
            sequenceJson = sequenceJson.replace(deviceSerial, newDeviceSerial);

            // "customerId": "ALEXA_CUSTOMER_ID"
            String customerId = "\"customerId\":\"ALEXA_CUSTOMER_ID\"";
            String newCustomerId = "\"customerId\":\"" + loginData.getAccountCustomerId() + "\"";
            sequenceJson = sequenceJson.replace(customerId, newCustomerId);

            // "locale": "ALEXA_CURRENT_LOCALE"
            String locale = "\"locale\":\"ALEXA_CURRENT_LOCALE\"";
            String newlocale = deviceLocale != null && !deviceLocale.isEmpty() ? "\"locale\":\"" + deviceLocale + "\""
                    : "\"locale\":null";
            sequenceJson = sequenceJson.replace(locale, newlocale);

            request.sequenceJson = sequenceJson;

            requestBuilder.post(getAlexaServer() + "/api/behaviors/preview").withContent(request).syncSend();
        } else {
            logger.warn("Routine {} not found", utterance);
        }
    }

    public List<AutomationTO> getAutomations() throws ConnectionException {
        return requestBuilder.get(getAlexaServer() + "/api/behaviors/v2/automations?limit=2000")
                .syncSend(AutomationTO.LIST_TYPE_TOKEN);
    }

    public List<EnabledFeedTO> getEnabledFlashBriefings() {
        try {
            return requestBuilder.get(getAlexaServer() + "/api/content-skills/enabled-feeds")
                    .syncSend(EnabledFeedsTO.class).enabledFeeds;
        } catch (ConnectionException e) {
            logger.warn("Failed to get enabled feeds: {}", e.getMessage());
        }
        return List.of();
    }

    public void setEnabledFlashBriefings(List<EnabledFeedTO> enabledFlashBriefing) throws ConnectionException {
        EnabledFeedsTO enabled = new EnabledFeedsTO();
        enabled.enabledFeeds = enabledFlashBriefing;
        requestBuilder.post(getAlexaServer() + "/api/content-skills/enabled-feeds").withContent(enabled).retry(false)
                .syncSend();
    }

    public List<NotificationSoundTO> getNotificationSounds(DeviceTO device) {
        try {
            return requestBuilder
                    .get(getAlexaServer() + "/api/notification/sounds?deviceSerialNumber=" + device.serialNumber
                            + "&deviceType=" + device.deviceType + "&softwareVersion=" + device.softwareVersion)
                    .syncSend(NotificationSoundResponseTO.class).notificationSounds;
        } catch (ConnectionException e) {
            return List.of();
        }
    }

    public List<NotificationTO> getNotifications() {
        try {
            return requestBuilder.get(getAlexaServer() + "/api/notifications")
                    .syncSend(NotificationListResponseTO.class).notifications;
        } catch (ConnectionException e) {
            logger.warn("Failed to get notifications: {}", e.getMessage());
        }
        return List.of();
    }

    public NotificationTO getNotification(String notificationId) throws ConnectionException {
        String url = getAlexaServer() + "/api/notifications/" + notificationId;
        return requestBuilder.get(url).syncSend(NotificationTO.class);
    }

    public @Nullable NotificationTO createNotification(DeviceTO device, String type, @Nullable String label,
            @Nullable NotificationSoundTO sound) throws ConnectionException {
        Instant createdTime = Instant.now();
        // add 5 seconds, because amazon does not accept calls for times in the past (compared with the server
        // nextAlarmTime)
        Instant alarmTime = createdTime.plusSeconds(5);
        ZonedDateTime zonedAlarmTime = alarmTime.atZone(ZoneId.systemDefault());

        NotificationTO request = new NotificationTO();
        request.status = "ON";
        request.deviceSerialNumber = device.serialNumber;
        request.deviceType = device.deviceType;
        request.createdDate = createdTime.getEpochSecond() * 1000;
        request.alarmTime = alarmTime.getEpochSecond() * 1000;
        request.reminderLabel = label;
        request.sound = sound;
        request.originalDate = zonedAlarmTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        request.originalTime = zonedAlarmTime.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSSS"));
        request.type = type;
        request.id = "create" + type;
        request.isSaveInFlight = true;
        request.isRecurring = false;

        String url = getAlexaServer() + "/api/notifications/createReminder";
        return requestBuilder.put(url).withContent(request).syncSend(NotificationTO.class);
    }

    public void deleteNotification(String notificationId) {
        try {
            requestBuilder.delete(getAlexaServer() + "/api/notifications/" + notificationId).syncSend();
        } catch (ConnectionException e) {
            logger.warn("Failed to delete notification {}: {}", notificationId, e.getMessage());
        }
    }

    public List<MusicProviderTO> getMusicProviders() {
        try {
            return requestBuilder.get(getAlexaServer() + "/api/behaviors/entities?skillId=amzn1.ask.1p.music")
                    .withHeader("Routines-Version", "3.0.264101").syncSend(MusicProviderTO.LIST_TYPE_TOKEN);
        } catch (ConnectionException e) {
            logger.warn("Failed to get music providers: {}", e.getMessage());
        }
        return List.of();
    }

    public void playMusicVoiceCommand(DeviceTO device, String providerId, String voiceCommand)
            throws ConnectionException {
        PlaySearchPhraseTO playSearchPhrase = new PlaySearchPhraseTO();
        playSearchPhrase.customerId = loginData.getAccountCustomerId();
        playSearchPhrase.musicProviderId = providerId;
        playSearchPhrase.searchPhrase = voiceCommand;

        BehaviorOperationValidateTO validationRequest = new BehaviorOperationValidateTO();
        validationRequest.type = "Alexa.Music.PlaySearchPhrase";
        validationRequest.operationPayload = gson.toJson(playSearchPhrase);

        PlaySearchPhraseTO validatedOperationPayload = requestBuilder
                .post(getAlexaServer() + "/api/behaviors/operation/validate").withContent(validationRequest)
                .syncSend(PlaySearchPhraseTO.VALIDATION_RESULT_TO_TYPE_TOKEN).operationPayload;

        if (validatedOperationPayload != null) {
            playSearchPhrase.sanitizedSearchPhrase = validatedOperationPayload.sanitizedSearchPhrase;
            playSearchPhrase.searchPhrase = validatedOperationPayload.searchPhrase;
        }

        playSearchPhrase.locale = null;
        playSearchPhrase.deviceSerialNumber = device.serialNumber;
        playSearchPhrase.deviceType = device.deviceType;

        JsonObject sequenceJson = new JsonObject();
        sequenceJson.addProperty("@type", "com.amazon.alexa.behaviors.model.Sequence");
        JsonObject startNodeJson = new JsonObject();
        startNodeJson.addProperty("@type", "com.amazon.alexa.behaviors.model.OpaquePayloadOperationNode");
        startNodeJson.addProperty("type", "Alexa.Music.PlaySearchPhrase");
        startNodeJson.add("operationPayload", gson.toJsonTree(playSearchPhrase));
        sequenceJson.add("startNode", startNodeJson);

        StartRoutineTO startRoutineRequest = new StartRoutineTO();
        startRoutineRequest.sequenceJson = sequenceJson.toString();
        startRoutineRequest.status = null;

        requestBuilder.post(getAlexaServer() + "/api/behaviors/preview").withContent(startRoutineRequest).syncSend();
    }

    public Optional<EqualizerTO> getEqualizer(DeviceTO device) {
        try {
            return Optional.of(requestBuilder
                    .get(getAlexaServer() + "/api/equalizer/" + device.serialNumber + "/" + device.deviceType)
                    .syncSend(EqualizerTO.class));
        } catch (ConnectionException e) {
            return Optional.empty();
        }
    }

    public boolean setEqualizer(DeviceTO device, EqualizerTO settings) {
        try {
            requestBuilder.post(getAlexaServer() + "/api/equalizer/" + device.serialNumber + "/" + device.deviceType)
                    .withContent(settings).retry(false).syncSend();
            return true;
        } catch (ConnectionException e) {
            return false;
        }
    }

    private static class Volume {
        public List<DeviceTO> devices = new ArrayList<>();
        public List<@Nullable Integer> volumes = new ArrayList<>();
    }

    private static class QueueObject {
        public @Nullable Future<?> future;
        public List<String> deviceSerialNumbers = List.of();
        public JsonObject nodeToExecute = new JsonObject();
    }

    private static class ExecutionNodeObject {
        public List<String> types = new ArrayList<>();
        public @Nullable String text;
    }
}
