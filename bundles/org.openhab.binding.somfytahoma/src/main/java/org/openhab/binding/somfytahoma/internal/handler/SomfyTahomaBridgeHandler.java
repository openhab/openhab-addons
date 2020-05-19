/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.thing.binding.ConfigStatusBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.openhab.binding.somfytahoma.internal.config.SomfyTahomaConfig;
import org.openhab.binding.somfytahoma.internal.model.*;
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
public class SomfyTahomaBridgeHandler extends ConfigStatusBridgeHandler {

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
        } catch (UnsupportedEncodingException e) {
            logger.debug("Cannot login due to unsupported encoding", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unsupported encoding");
        }
    }

    private void setTooManyRequests() {
        logger.debug("Too many requests error, suspending activity for {} seconds", SUSPEND_TIME);
        tooManyRequests = true;
        scheduler.schedule(this::enableLogin, SUSPEND_TIME, TimeUnit.SECONDS);
    }

    private @Nullable String registerEvents() {
        String url;

        try {
            url = TAHOMA_EVENTS_URL + "register";

            String line = sendPostToTahomaWithCookie(url, "");
            SomfyTahomaRegisterEventsResponse response = gson.fromJson(line, SomfyTahomaRegisterEventsResponse.class);
            return response.getId();
        } catch (JsonSyntaxException e) {
            logger.debug("Received invalid data", e);
            return null;
        } catch (ExecutionException e) {
            if (isAuthenticationChallenge(e)) {
                reLogin();
                return UNAUTHORIZED;
            } else {
                logger.info("Cannot register events!", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } catch (InterruptedException | TimeoutException e) {
            logger.info("Cannot register events!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
        return null;
    }

    private String urlEncode(String text) throws UnsupportedEncodingException {
        return URLEncoder.encode(text, StandardCharsets.UTF_8.toString());
    }

    private void enableLogin() {
        tooManyRequests = false;
    }

    private List<SomfyTahomaEvent> getEvents() {
        String url;
        String line = "";

        try {
            url = TAHOMA_API_URL + "events/" + eventsId + "/fetch";

            line = sendPostToTahomaWithCookie(url, "");
            SomfyTahomaEvent[] array = gson.fromJson(line, SomfyTahomaEvent[].class);
            return new ArrayList<>(Arrays.asList(array));
        } catch (JsonSyntaxException e) {
            logger.debug("Received data: {} is not JSON", line, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Received invalid data");
        } catch (ExecutionException e) {
            if (isAuthenticationChallenge(e)) {
                reLogin();
            } else {
                logger.debug("Cannot get Tahoma events!", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } catch (InterruptedException | TimeoutException e) {
            logger.debug("Cannot get Tahoma events!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
        return new ArrayList<>();
    }

    @Override
    public void handleRemoval() {
        super.handleRemoval();
        logout();
    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        return Collections.emptyList();
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
        if (httpClient != null) {
            try {
                httpClient.stop();
            } catch (Exception e) {
                logger.debug("Error during http client stopping", e);
            }
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
        if (pollFuture != null && !pollFuture.isCancelled()) {
            pollFuture.cancel(true);
            pollFuture = null;
        }
        if (statusFuture != null && !statusFuture.isCancelled()) {
            statusFuture.cancel(true);
            statusFuture = null;
        }
        if (reconciliationFuture != null && !reconciliationFuture.isCancelled()) {
            reconciliationFuture.cancel(true);
            reconciliationFuture = null;
        }
    }

    public List<SomfyTahomaActionGroup> listActionGroups() {
        String groups = getGroups();
        if (StringUtils.equals(groups, UNAUTHORIZED)) {
            groups = getGroups();
        }

        if (groups == null || groups.equals(UNAUTHORIZED)) {
            return Collections.emptyList();
        }

        try {
            SomfyTahomaActionGroup[] list = gson.fromJson(groups, SomfyTahomaActionGroup[].class);
            return Arrays.asList(list);
        } catch (JsonSyntaxException e) {
            logger.debug("Received data: {} is not JSON", groups, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Received invalid data");
        }
        return new ArrayList<>();
    }

    private @Nullable String getGroups() {
        String url;

        try {
            url = TAHOMA_API_URL + "actionGroups";
            return sendGetToTahomaWithCookie(url);
        } catch (ExecutionException e) {
            if (isAuthenticationChallenge(e)) {
                reLogin();
                return UNAUTHORIZED;
            } else {
                logger.debug("Cannot send getActionGroups command!", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } catch (InterruptedException | TimeoutException e) {
            logger.debug("Cannot send getActionGroups command!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
        return null;
    }

    public @Nullable SomfyTahomaSetup getSetup() {
        String url;
        String line = "";

        try {
            url = TAHOMA_API_URL + "setup";
            line = sendGetToTahomaWithCookie(url);
            return gson.fromJson(line, SomfyTahomaSetup.class);
        } catch (JsonSyntaxException e) {
            logger.debug("Received data: {} is not JSON", line, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Received invalid data");
        } catch (ExecutionException e) {
            if (isAuthenticationChallenge(e)) {
                reLogin();
            } else {
                logger.debug("Cannot send getSetup command!", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } catch (InterruptedException | TimeoutException e) {
            logger.debug("Cannot send getSetup command!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
        return null;
    }

    public List<SomfyTahomaDevice> getDevices() {
        String url;
        String line = "";

        try {
            url = SETUP_URL + "devices";
            line = sendGetToTahomaWithCookie(url);
            return Arrays.asList(gson.fromJson(line, SomfyTahomaDevice[].class));
        } catch (JsonSyntaxException e) {
            logger.debug("Received data: {} is not JSON", line, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Received invalid data");
        } catch (ExecutionException e) {
            if (isAuthenticationChallenge(e)) {
                reLogin();
            } else {
                logger.debug("Cannot send get devices command!", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } catch (InterruptedException | TimeoutException e) {
            logger.debug("Cannot send get devices command!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
        return Collections.emptyList();
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

    public void sendCommand(String io, String command, String params) {
        if (ThingStatus.OFFLINE == thing.getStatus() && !reLogin()) {
            return;
        }

        Boolean result = sendCommandInternal(io, command, params);
        if (result != null && !result) {
            sendCommandInternal(io, command, params);
        }
    }

    private @Nullable Boolean sendCommandInternal(String io, String command, String params) {
        String url;
        String line = "";

        try {
            url = EXEC_URL + "apply";

            String value = params.equals("[]") ? command : params.replace("\"", "");
            String urlParameters = "{\"label\":\"" + getThingLabelByURL(io) + " - " + value
                    + " - OH2\",\"actions\":[{\"deviceURL\":\"" + io + "\",\"commands\":[{\"name\":\"" + command
                    + "\",\"parameters\":" + params + "}]}]}";

            line = sendPostToTahomaWithCookie(url, urlParameters);

            SomfyTahomaApplyResponse data = gson.fromJson(line, SomfyTahomaApplyResponse.class);

            if (!StringUtils.isEmpty(data.getExecId())) {
                logger.debug("Exec id: {}", data.getExecId());
                registerExecution(io, data.getExecId());
            } else {
                logger.warn("Apply command response: {}", line);
                return false;
            }
            return true;
        } catch (JsonSyntaxException e) {
            logger.debug("Received data: {} is not JSON", line, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Received invalid data");
        } catch (ExecutionException e) {
            if (isAuthenticationChallenge(e)) {
                reLogin();
                return false;
            } else {
                logger.debug("Cannot send apply command {} with params {}!", command, params, e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } catch (InterruptedException | TimeoutException e) {
            logger.debug("Cannot send apply command {} with params {}!", command, params, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
        return null;
    }

    private String getThingLabelByURL(String io) {
        Thing th = getThingByDeviceUrl(io);
        if (th != null) {
            if (th.getProperties().containsKey(NAME_STATE)) {
                // Return label from Tahoma
                return th.getProperties().get(NAME_STATE).replace("\"", "");
            }
            // Return label from OH2
            return th.getLabel() != null ? th.getLabel().replace("\"", "") : "";
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
        Boolean result = cancelExecutionInternal(executionId);
        if (result != null && !result) {
            cancelExecutionInternal(executionId);
        }
    }

    private @Nullable Boolean cancelExecutionInternal(String executionId) {
        String url;

        try {
            url = DELETE_URL + executionId;
            sendDeleteToTahomaWithCookie(url);
            return true;
        } catch (ExecutionException e) {
            if (isAuthenticationChallenge(e)) {
                reLogin();
                return false;
            } else {
                logger.debug("Cannot cancel execution!", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } catch (InterruptedException | TimeoutException e) {
            logger.debug("Cannot cancel execution!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
        return null;
    }

    public void executeActionGroup(String id) {
        if (ThingStatus.OFFLINE == thing.getStatus() && !reLogin()) {
            return;
        }
        String execId = executeActionGroupInternal(id);
        if (UNAUTHORIZED.equals(execId)) {
            execId = executeActionGroupInternal(id);
        }
        if (!UNAUTHORIZED.equals(execId) && execId != null) {
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
        String line = "";
        try {
            String url = EXEC_URL + id;

            line = sendPostToTahomaWithCookie(url, "");
            SomfyTahomaApplyResponse data = gson.fromJson(line, SomfyTahomaApplyResponse.class);
            if (data.getExecId().isEmpty()) {
                logger.debug("Got empty exec response");
            }
            return data.getExecId();
        } catch (JsonSyntaxException e) {
            logger.debug("Received data: {} is not JSON", line, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Received invalid data");
        } catch (ExecutionException e) {
            if (isAuthenticationChallenge(e)) {
                reLogin();
                return UNAUTHORIZED;
            } else {
                logger.debug("Cannot exec execution group!", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } catch (InterruptedException | TimeoutException e) {
            logger.debug("Cannot exec execution group!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
        return null;
    }

    public void forceGatewaySync() {
        try {
            sendPutToTahomaWithCookie(REFRESH_URL);
        } catch (ExecutionException e) {
            if (isAuthenticationChallenge(e)) {
                reLogin();
            } else {
                logger.debug("Cannot sync gateway!", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } catch (InterruptedException | TimeoutException e) {
            logger.debug("Cannot sync gateway!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public SomfyTahomaStatus getTahomaStatus(String gatewayId) {
        String line = "";
        try {
            String url = GATEWAYS_URL + gatewayId;
            line = sendGetToTahomaWithCookie(url);
            SomfyTahomaStatusResponse data = gson.fromJson(line, SomfyTahomaStatusResponse.class);
            logger.debug("Tahoma status: {}", data.getConnectivity().getStatus());
            logger.debug("Tahoma protocol version: {}", data.getConnectivity().getProtocolVersion());
            return data.getConnectivity();
        } catch (JsonSyntaxException e) {
            logger.debug("Received data: {} is not JSON", line, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Received invalid data");
        } catch (ExecutionException e) {
            if (isAuthenticationChallenge(e)) {
                reLogin();
                return new SomfyTahomaStatus();
            } else {
                logger.debug("Cannot get Tahoma gateway status!", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } catch (InterruptedException | TimeoutException e) {
            logger.debug("Cannot get Tahoma gateway status!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
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
        try {
            String line = sendGetToTahomaWithCookie(DEVICES_URL + urlEncode(url) + "/states/" + stateName);
            SomfyTahomaState state = gson.fromJson(line, SomfyTahomaState.class);
            if (StringUtils.isNotEmpty(state.getName())) {
                updateDevice(url, Arrays.asList(state));
            }
        } catch (UnsupportedEncodingException e) {
            logger.debug("Unsupported encoding!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        } catch (ExecutionException e) {
            if (isAuthenticationChallenge(e)) {
                reLogin();
            } else {
                logger.debug("Cannot refresh device states!", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } catch (InterruptedException | TimeoutException e) {
            logger.debug("Cannot refresh device states!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
