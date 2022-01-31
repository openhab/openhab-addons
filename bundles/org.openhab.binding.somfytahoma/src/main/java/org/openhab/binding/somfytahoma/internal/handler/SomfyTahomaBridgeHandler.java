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
package org.openhab.binding.somfytahoma.internal.handler;

import static org.openhab.binding.somfytahoma.internal.SomfyTahomaBindingConstants.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.MediaType;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.somfytahoma.internal.config.SomfyTahomaConfig;
import org.openhab.binding.somfytahoma.internal.discovery.SomfyTahomaItemDiscoveryService;
import org.openhab.binding.somfytahoma.internal.model.SomfyTahomaAction;
import org.openhab.binding.somfytahoma.internal.model.SomfyTahomaActionGroup;
import org.openhab.binding.somfytahoma.internal.model.SomfyTahomaApplyResponse;
import org.openhab.binding.somfytahoma.internal.model.SomfyTahomaDevice;
import org.openhab.binding.somfytahoma.internal.model.SomfyTahomaEvent;
import org.openhab.binding.somfytahoma.internal.model.SomfyTahomaLoginResponse;
import org.openhab.binding.somfytahoma.internal.model.SomfyTahomaOauth2Error;
import org.openhab.binding.somfytahoma.internal.model.SomfyTahomaOauth2Reponse;
import org.openhab.binding.somfytahoma.internal.model.SomfyTahomaRegisterEventsResponse;
import org.openhab.binding.somfytahoma.internal.model.SomfyTahomaSetup;
import org.openhab.binding.somfytahoma.internal.model.SomfyTahomaState;
import org.openhab.binding.somfytahoma.internal.model.SomfyTahomaStatus;
import org.openhab.binding.somfytahoma.internal.model.SomfyTahomaStatusResponse;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link SomfyTahomaBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Ondrej Pecta - Initial contribution
 * @author Laurent Garnier - Other portals integration
 */
@NonNullByDefault
public class SomfyTahomaBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(SomfyTahomaBridgeHandler.class);

    /**
     * The shared HttpClient
     */
    private final HttpClient httpClient;

    /**
     * Future to poll for updates
     */
    private @Nullable ScheduledFuture<?> pollFuture;

    /**
     * Future to poll for status
     */
    private @Nullable ScheduledFuture<?> statusFuture;

    /**
     * Future to set reconciliation flag
     */
    private @Nullable ScheduledFuture<?> reconciliationFuture;

    // List of futures used for command retries
    private Collection<ScheduledFuture<?>> retryFutures = new ConcurrentLinkedQueue<ScheduledFuture<?>>();

    /**
     * List of executions
     */
    private Map<String, String> executions = new HashMap<>();

    // Too many requests flag
    private boolean tooManyRequests = false;

    // Silent relogin flag
    private boolean reLoginNeeded = false;

    // Reconciliation flag
    private boolean reconciliation = false;

    /**
     * Our configuration
     */
    protected SomfyTahomaConfig thingConfig = new SomfyTahomaConfig();

    /**
     * Id of registered events
     */
    private String eventsId = "";

    private Map<String, SomfyTahomaDevice> devicePlaces = new HashMap<>();

    private ExpiringCache<List<SomfyTahomaDevice>> cachedDevices = new ExpiringCache<>(Duration.ofSeconds(30),
            this::getDevices);

    // Gson & parser
    private final Gson gson = new Gson();

    public SomfyTahomaBridgeHandler(Bridge thing, HttpClientFactory httpClientFactory) {
        super(thing);
        this.httpClient = httpClientFactory.createHttpClient("somfy_" + thing.getUID().getId());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        thingConfig = getConfigAs(SomfyTahomaConfig.class);

        try {
            httpClient.start();
        } catch (Exception e) {
            logger.debug("Cannot start http client for: {}", thing.getBridgeUID().getId(), e);
            return;
        }

        scheduler.execute(() -> {
            login();
            initPolling();
            logger.debug("Initialize done...");
        });
    }

    /**
     * starts this things polling future
     */
    private void initPolling() {
        stopPolling();
        scheduleGetUpdates(10);

        statusFuture = scheduler.scheduleWithFixedDelay(() -> {
            refreshTahomaStates();
        }, 60, thingConfig.getStatusTimeout(), TimeUnit.SECONDS);

        reconciliationFuture = scheduler.scheduleWithFixedDelay(() -> {
            enableReconciliation();
        }, RECONCILIATION_TIME, RECONCILIATION_TIME, TimeUnit.SECONDS);
    }

    private void scheduleGetUpdates(long delay) {
        pollFuture = scheduler.schedule(() -> {
            getTahomaUpdates();
            scheduleNextGetUpdates();
        }, delay, TimeUnit.SECONDS);
    }

    private void scheduleNextGetUpdates() {
        ScheduledFuture<?> localPollFuture = pollFuture;
        if (localPollFuture != null) {
            localPollFuture.cancel(false);
        }
        scheduleGetUpdates(executions.isEmpty() ? thingConfig.getRefresh() : 2);
    }

    public synchronized void login() {
        if (thingConfig.getEmail().isEmpty() || thingConfig.getPassword().isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Can not access device as username and/or password are null");
            return;
        }

        if (tooManyRequests) {
            logger.debug("Skipping login due to too many requests");
            return;
        }

        if (ThingStatus.ONLINE == thing.getStatus() && !reLoginNeeded) {
            logger.debug("No need to log in, because already logged in");
            return;
        }

        reLoginNeeded = false;

        try {

            String urlParameters = "";

            // if cozytouch, must use oauth server
            if (thingConfig.getCloudPortal().equalsIgnoreCase(COZYTOUCH_PORTAL)) {
                logger.debug("CozyTouch Oauth2 authentication flow");
                urlParameters = "jwt=" + loginCozytouch();
            } else {
                urlParameters = "userId=" + urlEncode(thingConfig.getEmail()) + "&userPassword="
                        + urlEncode(thingConfig.getPassword());
            }

            ContentResponse response = sendRequestBuilder("login", HttpMethod.POST)
                    .content(new StringContentProvider(urlParameters),
                            "application/x-www-form-urlencoded; charset=UTF-8")
                    .send();

            if (logger.isTraceEnabled()) {
                logger.trace("Login response: {}", response.getContentAsString());
            }

            SomfyTahomaLoginResponse data = gson.fromJson(response.getContentAsString(),
                    SomfyTahomaLoginResponse.class);
            if (data == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Received invalid data (login)");
            } else if (data.isSuccess()) {
                logger.debug("SomfyTahoma version: {}", data.getVersion());
                String id = registerEvents();
                if (id != null && !UNAUTHORIZED.equals(id)) {
                    eventsId = id;
                    logger.debug("Events id: {}", eventsId);
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    logger.debug("Events id error: {}", id);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Error logging in: " + data.getError());
                if (data.getError().startsWith(TOO_MANY_REQUESTS)) {
                    setTooManyRequests();
                }
            }
        } catch (JsonSyntaxException e) {
            logger.debug("Received invalid data (login)", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Received invalid data (login)");
        } catch (ExecutionException e) {
            if (isAuthenticationChallenge(e) || isOAuthGrantError(e)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Error logging in (check your credentials)");
                setTooManyRequests();
            } else {
                logger.debug("Cannot get login cookie", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Cannot get login cookie");
            }
        } catch (TimeoutException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Getting login cookie timeout");
        } catch (InterruptedException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Getting login cookie interrupted");
            Thread.currentThread().interrupt();
        }
    }

    private void setTooManyRequests() {
        logger.debug("Too many requests or bad credentials for the cloud portal, suspending activity for {} seconds",
                SUSPEND_TIME);
        tooManyRequests = true;
        scheduler.schedule(this::enableLogin, SUSPEND_TIME, TimeUnit.SECONDS);
    }

    private @Nullable String registerEvents() {
        SomfyTahomaRegisterEventsResponse response = invokeCallToURL(EVENTS_URL + "register", "", HttpMethod.POST,
                SomfyTahomaRegisterEventsResponse.class);
        return response != null ? response.getId() : null;
    }

    private String urlEncode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }

    private void enableLogin() {
        tooManyRequests = false;
    }

    private List<SomfyTahomaEvent> getEvents() {
        SomfyTahomaEvent[] response = invokeCallToURL(EVENTS_URL + eventsId + "/fetch", "", HttpMethod.POST,
                SomfyTahomaEvent[].class);
        return response != null ? List.of(response) : List.of();
    }

    @Override
    public void handleRemoval() {
        super.handleRemoval();
        logout();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(SomfyTahomaItemDiscoveryService.class);
    }

    @Override
    public void dispose() {
        cleanup();
        super.dispose();
    }

    private void cleanup() {
        logger.debug("Doing cleanup");
        stopPolling();
        executions.clear();
        // cancel all scheduled retries
        retryFutures.forEach(x -> x.cancel(false));

        try {
            httpClient.stop();
        } catch (Exception e) {
            logger.debug("Error during http client stopping", e);
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);
        if (ThingStatus.UNINITIALIZED == bridgeStatusInfo.getStatus()) {
            cleanup();
        }
    }

    /**
     * Stops this thing's polling future
     */
    private void stopPolling() {
        ScheduledFuture<?> localPollFuture = pollFuture;
        if (localPollFuture != null && !localPollFuture.isCancelled()) {
            localPollFuture.cancel(true);
        }
        ScheduledFuture<?> localStatusFuture = statusFuture;
        if (localStatusFuture != null && !localStatusFuture.isCancelled()) {
            localStatusFuture.cancel(true);
        }
        ScheduledFuture<?> localReconciliationFuture = reconciliationFuture;
        if (localReconciliationFuture != null && !localReconciliationFuture.isCancelled()) {
            localReconciliationFuture.cancel(true);
        }
    }

    public List<SomfyTahomaActionGroup> listActionGroups() {
        SomfyTahomaActionGroup[] list = invokeCallToURL("actionGroups", "", HttpMethod.GET,
                SomfyTahomaActionGroup[].class);
        return list != null ? List.of(list) : List.of();
    }

    public @Nullable SomfyTahomaSetup getSetup() {
        SomfyTahomaSetup setup = invokeCallToURL("setup", "", HttpMethod.GET, SomfyTahomaSetup.class);
        if (setup != null) {
            saveDevicePlaces(setup.getDevices());
        }
        return setup;
    }

    public List<SomfyTahomaDevice> getDevices() {
        SomfyTahomaDevice[] response = invokeCallToURL(SETUP_URL + "devices", "", HttpMethod.GET,
                SomfyTahomaDevice[].class);
        List<SomfyTahomaDevice> devices = response != null ? List.of(response) : List.of();
        saveDevicePlaces(devices);
        return devices;
    }

    public synchronized @Nullable SomfyTahomaDevice getCachedDevice(String url) {
        List<SomfyTahomaDevice> devices = cachedDevices.getValue();
        if (devices != null) {
            for (SomfyTahomaDevice device : devices) {
                if (url.equals(device.getDeviceURL())) {
                    return device;
                }
            }
        }
        return null;
    }

    private void saveDevicePlaces(List<SomfyTahomaDevice> devices) {
        devicePlaces.clear();
        for (SomfyTahomaDevice device : devices) {
            if (!device.getPlaceOID().isEmpty()) {
                SomfyTahomaDevice newDevice = new SomfyTahomaDevice();
                newDevice.setPlaceOID(device.getPlaceOID());
                newDevice.setWidget(device.getWidget());
                devicePlaces.put(device.getDeviceURL(), newDevice);
            }
        }
    }

    private void getTahomaUpdates() {
        logger.debug("Getting Tahoma Updates...");
        if (ThingStatus.OFFLINE == thing.getStatus() && !reLogin()) {
            return;
        }

        List<SomfyTahomaEvent> events = getEvents();
        logger.trace("Got total of {} events", events.size());
        for (SomfyTahomaEvent event : events) {
            processEvent(event);
        }
    }

    private void processEvent(SomfyTahomaEvent event) {
        logger.debug("Got event: {}", event.getName());
        switch (event.getName()) {
            case "ExecutionRegisteredEvent":
                processExecutionRegisteredEvent(event);
                break;
            case "ExecutionStateChangedEvent":
                processExecutionChangedEvent(event);
                break;
            case "DeviceStateChangedEvent":
                processStateChangedEvent(event);
                break;
            case "RefreshAllDevicesStatesCompletedEvent":
                scheduler.schedule(this::updateThings, 1, TimeUnit.SECONDS);
                break;
            case "GatewayAliveEvent":
            case "GatewayDownEvent":
                processGatewayEvent(event);
                break;
            default:
                // ignore other states
        }
    }

    private synchronized void updateThings() {
        boolean needsUpdate = reconciliation;

        for (Thing th : getThing().getThings()) {
            if (ThingStatus.ONLINE != th.getStatus()) {
                needsUpdate = true;
            }
        }

        // update all states only if necessary
        if (needsUpdate) {
            updateAllStates();
            reconciliation = false;
        }
    }

    private void processExecutionRegisteredEvent(SomfyTahomaEvent event) {
        boolean invalidData = false;
        try {
            JsonElement el = event.getAction();
            if (el.isJsonArray()) {
                SomfyTahomaAction[] actions = gson.fromJson(el, SomfyTahomaAction[].class);
                if (actions == null) {
                    invalidData = true;
                } else {
                    for (SomfyTahomaAction action : actions) {
                        registerExecution(action.getDeviceURL(), event.getExecId());
                    }
                }
            } else {
                SomfyTahomaAction action = gson.fromJson(el, SomfyTahomaAction.class);
                if (action == null) {
                    invalidData = true;
                } else {
                    registerExecution(action.getDeviceURL(), event.getExecId());
                }
            }
        } catch (JsonSyntaxException e) {
            invalidData = true;
        }
        if (invalidData) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Received invalid data (execution registered)");
        }
    }

    private void processExecutionChangedEvent(SomfyTahomaEvent event) {
        if (FAILED_EVENT.equals(event.getNewState()) || COMPLETED_EVENT.equals(event.getNewState())) {
            logger.debug("Removing execution id: {}", event.getExecId());
            unregisterExecution(event.getExecId());
        }
    }

    private void registerExecution(String url, String execId) {
        if (executions.containsKey(url)) {
            executions.remove(url);
            logger.debug("Previous execution exists for url: {}", url);
        }
        executions.put(url, execId);
    }

    private void unregisterExecution(String execId) {
        if (executions.containsValue(execId)) {
            executions.values().removeAll(Collections.singleton(execId));
        } else {
            logger.debug("Cannot remove execution id: {}, because it is not registered", execId);
        }
    }

    private void processGatewayEvent(SomfyTahomaEvent event) {
        // update gateway status
        for (Thing th : getThing().getThings()) {
            if (THING_TYPE_GATEWAY.equals(th.getThingTypeUID())) {
                SomfyTahomaGatewayHandler gatewayHandler = (SomfyTahomaGatewayHandler) th.getHandler();
                if (gatewayHandler != null && gatewayHandler.getGateWayId().equals(event.getGatewayId())) {
                    gatewayHandler.refresh(STATUS);
                }
            }
        }
    }

    private synchronized void updateAllStates() {
        logger.debug("Updating all states");
        getDevices().forEach(device -> updateDevice(device));
    }

    private void updateDevice(SomfyTahomaDevice device) {
        String url = device.getDeviceURL();
        List<SomfyTahomaState> states = device.getStates();
        updateDevice(url, states);
    }

    private void updateDevice(String url, List<SomfyTahomaState> states) {
        Thing th = getThingByDeviceUrl(url);
        if (th == null) {
            return;
        }
        SomfyTahomaBaseThingHandler handler = (SomfyTahomaBaseThingHandler) th.getHandler();
        if (handler != null) {
            handler.updateThingStatus(states);
            handler.updateThingChannels(states);
        }
    }

    private void processStateChangedEvent(SomfyTahomaEvent event) {
        String deviceUrl = event.getDeviceUrl();
        List<SomfyTahomaState> states = event.getDeviceStates();
        logger.debug("States for device {} : {}", deviceUrl, states);
        Thing thing = getThingByDeviceUrl(deviceUrl);

        if (thing != null) {
            logger.debug("Updating status of thing: {}", thing.getLabel());
            SomfyTahomaBaseThingHandler handler = (SomfyTahomaBaseThingHandler) thing.getHandler();

            if (handler != null) {
                // update thing status
                handler.updateThingStatus(states);
                handler.updateThingChannels(states);
            }
        } else {
            logger.debug("Thing handler is null, probably not bound thing.");
        }
    }

    private void enableReconciliation() {
        logger.debug("Enabling reconciliation");
        reconciliation = true;
    }

    private void refreshTahomaStates() {
        logger.debug("Refreshing Tahoma states...");
        if (ThingStatus.OFFLINE == thing.getStatus() && !reLogin()) {
            return;
        }

        // force Tahoma to ask for actual states
        forceGatewaySync();
    }

    private @Nullable Thing getThingByDeviceUrl(String deviceUrl) {
        for (Thing th : getThing().getThings()) {
            String url = (String) th.getConfiguration().get("url");
            if (deviceUrl.equals(url)) {
                return th;
            }
        }
        return null;
    }

    private void logout() {
        try {
            eventsId = "";
            sendGetToTahomaWithCookie("logout");
        } catch (ExecutionException | TimeoutException e) {
            logger.debug("Cannot send logout command!", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String sendPostToTahomaWithCookie(String url, String urlParameters)
            throws InterruptedException, ExecutionException, TimeoutException {
        return sendMethodToTahomaWithCookie(url, HttpMethod.POST, urlParameters);
    }

    private String sendGetToTahomaWithCookie(String url)
            throws InterruptedException, ExecutionException, TimeoutException {
        return sendMethodToTahomaWithCookie(url, HttpMethod.GET);
    }

    private String sendPutToTahomaWithCookie(String url)
            throws InterruptedException, ExecutionException, TimeoutException {
        return sendMethodToTahomaWithCookie(url, HttpMethod.PUT);
    }

    private String sendDeleteToTahomaWithCookie(String url)
            throws InterruptedException, ExecutionException, TimeoutException {
        return sendMethodToTahomaWithCookie(url, HttpMethod.DELETE);
    }

    private String sendMethodToTahomaWithCookie(String url, HttpMethod method)
            throws InterruptedException, ExecutionException, TimeoutException {
        return sendMethodToTahomaWithCookie(url, method, "");
    }

    private String sendMethodToTahomaWithCookie(String url, HttpMethod method, String urlParameters)
            throws InterruptedException, ExecutionException, TimeoutException {
        logger.trace("Sending {} to url: {} with data: {}", method.asString(), getApiFullUrl(url), urlParameters);
        Request request = sendRequestBuilder(url, method);
        if (!urlParameters.isEmpty()) {
            request = request.content(new StringContentProvider(urlParameters), "application/json;charset=UTF-8");
        }

        ContentResponse response = request.send();

        if (logger.isTraceEnabled()) {
            logger.trace("Response: {}", response.getContentAsString());
        }

        if (response.getStatus() < 200 || response.getStatus() >= 300) {
            logger.debug("Received unexpected status code: {}", response.getStatus());
        }
        return response.getContentAsString();
    }

    private Request sendRequestBuilder(String subUrl, HttpMethod method) {
        return httpClient.newRequest(getApiFullUrl(subUrl)).method(method)
                .header(HttpHeader.ACCEPT_LANGUAGE, "en-US,en").header(HttpHeader.ACCEPT_ENCODING, "gzip, deflate")
                .header("X-Requested-With", "XMLHttpRequest").timeout(TAHOMA_TIMEOUT, TimeUnit.SECONDS)
                .agent(TAHOMA_AGENT);
    }

    /**
     * Performs the login for Cozytouch using OAUTH2 authorization.
     *
     * @return JSESSION ID cookie value.
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws InterruptedException
     * @throws JsonSyntaxException
     */
    private String loginCozytouch()
            throws InterruptedException, TimeoutException, ExecutionException, JsonSyntaxException {
        String authBaseUrl = "https://" + COZYTOUCH_OAUTH2_URL;

        String urlParameters = "grant_type=password&username=" + urlEncode(thingConfig.getEmail()) + "&password="
                + urlEncode(thingConfig.getPassword());

        ContentResponse response = httpClient.newRequest(authBaseUrl + COZYTOUCH_OAUTH2_TOKEN_URL)
                .method(HttpMethod.POST).header(HttpHeader.ACCEPT_LANGUAGE, "en-US,en")
                .header(HttpHeader.ACCEPT_ENCODING, "gzip, deflate").header("X-Requested-With", "XMLHttpRequest")
                .header(HttpHeader.AUTHORIZATION, "Basic " + COZYTOUCH_OAUTH2_BASICAUTH)
                .timeout(TAHOMA_TIMEOUT, TimeUnit.SECONDS).agent(TAHOMA_AGENT)
                .content(new StringContentProvider(urlParameters), "application/x-www-form-urlencoded; charset=UTF-8")
                .send();

        if (response.getStatus() != 200) {
            // Login error
            if (response.getHeaders().getField(HttpHeader.CONTENT_TYPE).getValue()
                    .equalsIgnoreCase(MediaType.APPLICATION_JSON)) {
                try {
                    SomfyTahomaOauth2Error error = gson.fromJson(response.getContentAsString(),
                            SomfyTahomaOauth2Error.class);
                    throw new ExecutionException(error.getErrorDescription(), null);
                } catch (JsonSyntaxException e) {

                }
            }
            throw new ExecutionException("Unknown error while attempting to log in.", null);
        }

        SomfyTahomaOauth2Reponse oauth2response = gson.fromJson(response.getContentAsString(),
                SomfyTahomaOauth2Reponse.class);

        logger.debug("OAuth2 Access Token: {}", oauth2response.getAccessToken());

        response = httpClient.newRequest(authBaseUrl + COZYTOUCH_OAUTH2_JWT_URL).method(HttpMethod.GET)
                .header(HttpHeader.AUTHORIZATION, "Bearer " + oauth2response.getAccessToken())
                .timeout(TAHOMA_TIMEOUT, TimeUnit.SECONDS).send();

        if (response.getStatus() == 200) {
            String jwt = response.getContentAsString();
            return jwt.replace("\"", "");
        } else {
            throw new ExecutionException(String.format("Failed to retrieve JWT token. ResponseCode=%d, ResponseText=%s",
                    response.getStatus(), response.getContentAsString()), null);
        }
    }

    private String getApiFullUrl(String subUrl) {
        return "https://" + thingConfig.getCloudPortal() + API_BASE_URL + subUrl;
    }

    public void sendCommand(String io, String command, String params, String url) {
        if (ThingStatus.OFFLINE == thing.getStatus() && !reLogin()) {
            return;
        }

        removeFinishedRetries();

        boolean result = sendCommandInternal(io, command, params, url);
        if (!result) {
            scheduleRetry(io, command, params, url, thingConfig.getRetries());
        }
    }

    private void repeatSendCommandInternal(String io, String command, String params, String url, int retries) {
        logger.debug("Retrying command, retries left: {}", retries);
        boolean result = sendCommandInternal(io, command, params, url);
        if (!result && (retries > 0)) {
            scheduleRetry(io, command, params, url, retries - 1);
        }
    }

    private boolean sendCommandInternal(String io, String command, String params, String url) {
        String value = "[]".equals(params) ? command : command + " " + params.replace("\"", "");
        String urlParameters = "{\"label\":\"" + getThingLabelByURL(io) + " - " + value
                + " - openHAB\",\"actions\":[{\"deviceURL\":\"" + io + "\",\"commands\":[{\"name\":\"" + command
                + "\",\"parameters\":" + params + "}]}]}";
        SomfyTahomaApplyResponse response = invokeCallToURL(url, urlParameters, HttpMethod.POST,
                SomfyTahomaApplyResponse.class);
        if (response != null) {
            if (!response.getExecId().isEmpty()) {
                logger.debug("Exec id: {}", response.getExecId());
                registerExecution(io, response.getExecId());
                scheduleNextGetUpdates();
            } else {
                logger.debug("ExecId is empty!");
                return false;
            }
            return true;
        }
        return false;
    }

    private void removeFinishedRetries() {
        retryFutures.removeIf(x -> x.isDone());
        logger.debug("Currently {} retries are scheduled.", retryFutures.size());
    }

    private void scheduleRetry(String io, String command, String params, String url, int retries) {
        retryFutures.add(scheduler.schedule(() -> {
            repeatSendCommandInternal(io, command, params, url, retries);
        }, thingConfig.getRetryDelay(), TimeUnit.MILLISECONDS));
    }

    public void sendCommandToSameDevicesInPlace(String io, String command, String params, String url) {
        SomfyTahomaDevice device = devicePlaces.get(io);
        if (device != null && !device.getPlaceOID().isEmpty()) {
            devicePlaces.forEach((deviceUrl, devicePlace) -> {
                if (device.getPlaceOID().equals(devicePlace.getPlaceOID())
                        && device.getWidget().equals(devicePlace.getWidget())) {
                    sendCommand(deviceUrl, command, params, url);
                }
            });
        } else {
            sendCommand(io, command, params, url);
        }
    }

    private String getThingLabelByURL(String io) {
        Thing th = getThingByDeviceUrl(io);
        if (th != null) {
            if (th.getProperties().containsKey(NAME_STATE)) {
                // Return label from Tahoma
                return th.getProperties().get(NAME_STATE).replace("\"", "");
            }
            // Return label from the thing
            String label = th.getLabel();
            return label != null ? label.replace("\"", "") : "";
        }
        return "null";
    }

    public @Nullable String getCurrentExecutions(String io) {
        if (executions.containsKey(io)) {
            return executions.get(io);
        }
        return null;
    }

    public void cancelExecution(String executionId) {
        invokeCallToURL(DELETE_URL + executionId, "", HttpMethod.DELETE, null);
    }

    public void executeActionGroup(String id) {
        if (ThingStatus.OFFLINE == thing.getStatus() && !reLogin()) {
            return;
        }
        String execId = executeActionGroupInternal(id);
        if (execId == null) {
            execId = executeActionGroupInternal(id);
        }
        if (execId != null) {
            registerExecution(id, execId);
            scheduleNextGetUpdates();
        }
    }

    private boolean reLogin() {
        logger.debug("Doing relogin");
        reLoginNeeded = true;
        login();
        return ThingStatus.OFFLINE != thing.getStatus();
    }

    public @Nullable String executeActionGroupInternal(String id) {
        SomfyTahomaApplyResponse response = invokeCallToURL(EXEC_URL + id, "", HttpMethod.POST,
                SomfyTahomaApplyResponse.class);
        if (response != null) {
            if (response.getExecId().isEmpty()) {
                logger.debug("Got empty exec response");
                return null;
            }
            return response.getExecId();
        }
        return null;
    }

    public void forceGatewaySync() {
        invokeCallToURL(REFRESH_URL, "", HttpMethod.PUT, null);
    }

    public SomfyTahomaStatus getTahomaStatus(String gatewayId) {
        SomfyTahomaStatusResponse data = invokeCallToURL(GATEWAYS_URL + gatewayId, "", HttpMethod.GET,
                SomfyTahomaStatusResponse.class);
        if (data != null) {
            logger.debug("Tahoma status: {}", data.getConnectivity().getStatus());
            logger.debug("Tahoma protocol version: {}", data.getConnectivity().getProtocolVersion());
            return data.getConnectivity();
        }
        return new SomfyTahomaStatus();
    }

    private boolean isAuthenticationChallenge(Exception ex) {
        String msg = ex.getMessage();
        return msg != null && msg.contains(AUTHENTICATION_CHALLENGE);
    }

    private boolean isOAuthGrantError(Exception ex) {
        String msg = ex.getMessage();
        return msg != null && msg.contains(AUTHENTICATION_OAUTH_GRANT_ERROR);
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        super.handleConfigurationUpdate(configurationParameters);
        if (configurationParameters.containsKey("email") || configurationParameters.containsKey("password")
                || configurationParameters.containsKey("portalUrl")) {
            reLoginNeeded = true;
            tooManyRequests = false;
        }
    }

    public synchronized void refresh(String url, String stateName) {
        SomfyTahomaState state = invokeCallToURL(DEVICES_URL + urlEncode(url) + "/states/" + stateName, "",
                HttpMethod.GET, SomfyTahomaState.class);
        if (state != null && !state.getName().isEmpty()) {
            updateDevice(url, List.of(state));
        }
    }

    private @Nullable <T> T invokeCallToURL(String url, String urlParameters, HttpMethod method,
            @Nullable Class<T> classOfT) {
        String response = "";
        try {
            switch (method) {
                case GET:
                    response = sendGetToTahomaWithCookie(url);
                    break;
                case PUT:
                    response = sendPutToTahomaWithCookie(url);
                    break;
                case POST:
                    response = sendPostToTahomaWithCookie(url, urlParameters);
                    break;
                case DELETE:
                    response = sendDeleteToTahomaWithCookie(url);
                default:
            }
            return classOfT != null ? gson.fromJson(response, classOfT) : null;
        } catch (JsonSyntaxException e) {
            logger.debug("Received data: {} is not JSON", response, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Received invalid data");
        } catch (ExecutionException e) {
            if (isAuthenticationChallenge(e)) {
                reLogin();
            } else {
                logger.debug("Cannot call url: {} with params: {}!", getApiFullUrl(url), urlParameters, e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } catch (TimeoutException e) {
            logger.debug("Timeout when calling url: {} with params: {}!", getApiFullUrl(url), urlParameters, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        } catch (InterruptedException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            Thread.currentThread().interrupt();
        }
        return null;
    }
}
