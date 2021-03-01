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
package org.openhab.binding.somfytahoma.internal.handler;

import static org.openhab.binding.somfytahoma.internal.SomfyTahomaBindingConstants.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.StringUtils;
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
import org.openhab.binding.somfytahoma.internal.model.*;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.*;
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
        pollFuture = scheduler.scheduleWithFixedDelay(() -> {
            getTahomaUpdates();
        }, 10, thingConfig.getRefresh(), TimeUnit.SECONDS);

        statusFuture = scheduler.scheduleWithFixedDelay(() -> {
            refreshTahomaStates();
        }, 60, thingConfig.getStatusTimeout(), TimeUnit.SECONDS);

        reconciliationFuture = scheduler.scheduleWithFixedDelay(() -> {
            enableReconciliation();
        }, RECONCILIATION_TIME, RECONCILIATION_TIME, TimeUnit.SECONDS);
    }

    public synchronized void login() {
        String url;

        if (StringUtils.isEmpty(thingConfig.getEmail()) || StringUtils.isEmpty(thingConfig.getPassword())) {
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
            url = TAHOMA_API_URL + "login";
            String urlParameters = "userId=" + urlEncode(thingConfig.getEmail()) + "&userPassword="
                    + urlEncode(thingConfig.getPassword());

            ContentResponse response = sendRequestBuilder(url, HttpMethod.POST)
                    .content(new StringContentProvider(urlParameters),
                            "application/x-www-form-urlencoded; charset=UTF-8")
                    .send();

            if (logger.isTraceEnabled()) {
                logger.trace("Login response: {}", response.getContentAsString());
            }

            SomfyTahomaLoginResponse data = gson.fromJson(response.getContentAsString(),
                    SomfyTahomaLoginResponse.class);
            if (data.isSuccess()) {
                logger.debug("SomfyTahoma version: {}", data.getVersion());
                String id = registerEvents();
                if (id != null && !id.equals(UNAUTHORIZED)) {
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
            logger.debug("Received invalid data", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Received invalid data");
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            if (e instanceof ExecutionException) {
                if (e.getMessage().contains(AUTHENTICATION_CHALLENGE)) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Authentication challenge");
                    setTooManyRequests();
                    return;
                }
            }
            logger.debug("Cannot get login cookie!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Cannot get login cookie");
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void setTooManyRequests() {
        logger.debug("Too many requests error, suspending activity for {} seconds", SUSPEND_TIME);
        tooManyRequests = true;
        scheduler.schedule(this::enableLogin, SUSPEND_TIME, TimeUnit.SECONDS);
    }

    private @Nullable String registerEvents() {
        SomfyTahomaRegisterEventsResponse response = invokeCallToURL(TAHOMA_EVENTS_URL + "register", "",
                HttpMethod.POST, SomfyTahomaRegisterEventsResponse.class);
        return response != null ? response.getId() : null;
    }

    private String urlEncode(String text) {
        try {
            return URLEncoder.encode(text, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            return text;
        }
    }

    private void enableLogin() {
        tooManyRequests = false;
    }

    private List<SomfyTahomaEvent> getEvents() {
        SomfyTahomaEvent[] response = invokeCallToURL(TAHOMA_API_URL + "events/" + eventsId + "/fetch", "",
                HttpMethod.POST, SomfyTahomaEvent[].class);
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
        SomfyTahomaActionGroup[] list = invokeCallToURL(TAHOMA_API_URL + "actionGroups", "", HttpMethod.GET,
                SomfyTahomaActionGroup[].class);
        return list != null ? List.of(list) : List.of();
    }

    public @Nullable SomfyTahomaSetup getSetup() {
        return invokeCallToURL(TAHOMA_API_URL + "setup", "", HttpMethod.GET, SomfyTahomaSetup.class);
    }

    public List<SomfyTahomaDevice> getDevices() {
        SomfyTahomaDevice[] response = invokeCallToURL(SETUP_URL + "devices", "", HttpMethod.GET,
                SomfyTahomaDevice[].class);
        return response != null ? List.of(response) : List.of();
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
        JsonElement el = event.getAction();
        if (el.isJsonArray()) {
            SomfyTahomaAction[] actions = gson.fromJson(el, SomfyTahomaAction[].class);
            for (SomfyTahomaAction action : actions) {
                registerExecution(action.getDeviceURL(), event.getExecId());
            }
        } else {
            SomfyTahomaAction action = gson.fromJson(el, SomfyTahomaAction.class);
            registerExecution(action.getDeviceURL(), event.getExecId());
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
            sendGetToTahomaWithCookie(TAHOMA_API_URL + "logout");
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.debug("Cannot send logout command!", e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
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
        logger.trace("Sending {} to url: {} with data: {}", method.asString(), url, urlParameters);
        Request request = sendRequestBuilder(url, method);
        if (StringUtils.isNotEmpty(urlParameters)) {
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

    private Request sendRequestBuilder(String url, HttpMethod method) {
        return httpClient.newRequest(url).method(method).header(HttpHeader.ACCEPT_LANGUAGE, "en-US,en")
                .header(HttpHeader.ACCEPT_ENCODING, "gzip, deflate").header("X-Requested-With", "XMLHttpRequest")
                .timeout(TAHOMA_TIMEOUT, TimeUnit.SECONDS).agent(TAHOMA_AGENT);
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
        String value = params.equals("[]") ? command : params.replace("\"", "");
        String urlParameters = "{\"label\":\"" + getThingLabelByURL(io) + " - " + value
                + " - OH2\",\"actions\":[{\"deviceURL\":\"" + io + "\",\"commands\":[{\"name\":\"" + command
                + "\",\"parameters\":" + params + "}]}]}";
        SomfyTahomaApplyResponse response = invokeCallToURL(url, urlParameters, HttpMethod.POST,
                SomfyTahomaApplyResponse.class);
        if (response != null) {
            if (!response.getExecId().isEmpty()) {
                logger.debug("Exec id: {}", response.getExecId());
                registerExecution(io, response.getExecId());
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

    private String getThingLabelByURL(String io) {
        Thing th = getThingByDeviceUrl(io);
        if (th != null) {
            if (th.getProperties().containsKey(NAME_STATE)) {
                // Return label from Tahoma
                return th.getProperties().get(NAME_STATE).replace("\"", "");
            }
            // Return label from OH2
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
        return ex.getMessage().contains(AUTHENTICATION_CHALLENGE);
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        super.handleConfigurationUpdate(configurationParameters);
        if (configurationParameters.containsKey("email")) {
            thingConfig.setEmail(configurationParameters.get("email").toString());
        }
        if (configurationParameters.containsKey("password")) {
            thingConfig.setPassword(configurationParameters.get("password").toString());
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
                logger.debug("Cannot call url: {} with params: {}!", url, urlParameters, e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } catch (InterruptedException | TimeoutException e) {
            logger.debug("Cannot call url: {} with params: {}!", url, urlParameters, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
        return null;
    }
}
