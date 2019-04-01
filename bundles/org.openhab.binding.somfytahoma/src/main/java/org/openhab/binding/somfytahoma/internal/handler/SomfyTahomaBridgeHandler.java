/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.thing.binding.ConfigStatusBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.somfytahoma.internal.SomfyTahomaException;
import org.openhab.binding.somfytahoma.internal.config.SomfyTahomaConfig;
import org.openhab.binding.somfytahoma.internal.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openhab.binding.somfytahoma.internal.SomfyTahomaBindingConstants.*;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The {@link SomfyTahomaBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaBridgeHandler extends ConfigStatusBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(SomfyTahomaBridgeHandler.class);

    // Instantiate and configure the SslContextFactory
    private SslContextFactory sslContextFactory = new SslContextFactory();

    // Instantiate HttpClient with the SslContextFactory
    private HttpClient httpClient = new HttpClient(sslContextFactory);

    /**
     * Future to poll for updates
     */
    @Nullable
    private ScheduledFuture<?> pollFuture;

    /**
     * Future to poll for status
     */
    @Nullable
    private ScheduledFuture<?> statusFuture;


    /**
     * Our configuration
     */
    protected SomfyTahomaConfig thingConfig = new SomfyTahomaConfig();

    // Gson & parser
    private final Gson gson = new Gson();

    public SomfyTahomaBridgeHandler(Bridge thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        String thingUid = getThing().getUID().toString();
        thingConfig = getConfigAs(SomfyTahomaConfig.class);
        thingConfig.setThingUid(thingUid);

        httpClient.setFollowRedirects(false);

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

    }

    public synchronized void login() {
        String url;

        if (StringUtils.isEmpty(thingConfig.getEmail()) || StringUtils.isEmpty(thingConfig.getPassword())) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Can not access device as username and/or password are null");
            return;
        }

        try {
            if (httpClient.isStarted()) {
                httpClient.stop();
            }
            httpClient.start();

            url = TAHOMA_URL + "login";
            String urlParameters = "userId=" + thingConfig.getEmail() + "&userPassword=" + thingConfig.getPassword();

            ContentResponse response = sendRequestBuilder(url, HttpMethod.POST)
                    .content(new StringContentProvider(urlParameters), "application/x-www-form-urlencoded; charset=UTF-8")
                    .send();

            logger.trace("Login response: {}", response.getContentAsString());
            SomfyTahomaLoginResponse data = gson.fromJson(response.getContentAsString(), SomfyTahomaLoginResponse.class);
            if (data.isSuccess()) {
                logger.debug("SomfyTahoma version: {}", data.getVersion());
                updateStatus(ThingStatus.ONLINE);
            } else {
                logger.debug("Login response: {}", response.getContentAsString());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Error logging in");
                throw new SomfyTahomaException(response.getContentAsString());
            }
        } catch (JsonSyntaxException e) {
            logger.debug("Received invalid data", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Received invalid data");
        } catch (SomfyTahomaException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Unauthorized. Please check credentials");
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.debug("Cannot get login cookie!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Cannot get login cookie");
        } catch (Exception e) {
            logger.debug("Cannot start http client", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Cannot start http client");
        }

    }

    private ArrayList<SomfyTahomaEvent> getEvents() {
        String url;
        String line = "";

        try {
            url = TAHOMA_URL + "getEvents";

            line = sendDataToTahomaWithCookie(url, "");
            SomfyTahomaEvent[] array = gson.fromJson(line, SomfyTahomaEvent[].class);
            return new ArrayList<>(Arrays.asList(array));
        } catch (JsonSyntaxException e) {
            logger.debug("Received data: {} is not JSON", line, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Received invalid data");
        } catch (SomfyTahomaException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unauthorized. Please check credentials");
        } catch (ExecutionException e) {
            if (isAuthenticationChallenge(e)) {
                login();
            } else {
                logger.debug("Cannot get Tahoma events!", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } catch (InterruptedException | TimeoutException e) {
            logger.debug("Cannot get Tahoma events!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
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
        stopPolling();

        try {
            httpClient.stop();
        } catch (Exception e) {
            logger.error("Cannot stop http client", e);
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
            SomfyTahomaActionGroupResponse data = gson.fromJson(groups, SomfyTahomaActionGroupResponse.class);
            return data.getActionGroups();
        } catch (JsonSyntaxException e) {
            logger.debug("Received data: {} is not JSON", groups, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Received invalid data");
        }
        return new ArrayList<>();
    }

    private @Nullable String getGroups() {
        String url;

        try {
            url = TAHOMA_URL + "getActionGroups";
            String urlParameters = "";

            return sendDataToTahomaWithCookie(url, urlParameters);
        } catch (SomfyTahomaException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unauthorized. Please check credentials");
            return UNAUTHORIZED;
        } catch (ExecutionException e) {
            if (isAuthenticationChallenge(e)) {
                login();
                return UNAUTHORIZED;
            } else {
                logger.debug("Cannot send getActionGroups command!", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } catch (InterruptedException | TimeoutException e) {
            logger.debug("Cannot send getActionGroups command!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
        return null;
    }

    public @Nullable SomfyTahomaSetup listDevices() {
        String url;
        String line = "";

        try {
            url = TAHOMA_URL + "getSetup";
            String urlParameters = "";

            line = sendDataToTahomaWithCookie(url, urlParameters);
            SomfyTahomaSetupResponse data = gson.fromJson(line, SomfyTahomaSetupResponse.class);
            return data.getSetup();
        } catch (JsonSyntaxException e) {
            logger.debug("Received data: {} is not JSON", line, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Received invalid data");
        } catch (SomfyTahomaException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unauthorized. Please check credentials");
        } catch (ExecutionException e) {
            if (isAuthenticationChallenge(e)) {
                login();
            } else {
                logger.debug("Cannot send getSetup command!", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } catch (InterruptedException | TimeoutException e) {
            logger.debug("Cannot send getSetup command!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
        return null;
    }


    private String getFormattedParameters(Collection<String> stateNames) {
        ArrayList<String> uniqueNames = new ArrayList<>();
        for (String name : stateNames) {
            if (!uniqueNames.contains(name)) {
                uniqueNames.add(name);
            }
        }
        StringBuilder sb = new StringBuilder("{\"name\": \"" + STATUS_STATE + "\"}");
        for (String name : uniqueNames) {
            sb.append(',');
            sb.append("{\"name\": \"").append(name).append("\"}");
        }
        logger.debug("Formatted parameters: {}", sb.toString());
        return sb.toString();
    }


    public @Nullable List<SomfyTahomaState> getAllStates(Collection<String> stateNames, String deviceUrl) {
        String url;
        String line = "";

        logger.debug("Getting states for a device: {}", deviceUrl);
        try {
            url = TAHOMA_URL + "getStates";
            String urlParameters = "[{\"deviceURL\": \"" + deviceUrl + "\", \"states\": ["
                    + getFormattedParameters(stateNames) + "]}]";

            line = sendDataToTahomaWithCookie(url, urlParameters);

            SomfyTahomaStatesResponse data = gson.fromJson(line, SomfyTahomaStatesResponse.class);
            SomfyTahomaDeviceWithState device = data.getDevices().get(0);
            if (!device.hasStates()) {
                logger.debug("Device: {} has not returned any state", deviceUrl);
            }
            return device.getStates();
        } catch (JsonSyntaxException e) {
            logger.debug("Received data: {} is not JSON", line, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Received invalid data");
        } catch (SomfyTahomaException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unauthorized. Please check credentials");
        } catch (ExecutionException e) {
            if (isAuthenticationChallenge(e)) {
                login();
            } else {
                logger.debug("Cannot send getStates command!", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } catch (InterruptedException | TimeoutException e) {
            logger.debug("Cannot send getStates command!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }

        return null;
    }

    private void getTahomaUpdates() {
        logger.debug("Getting Tahoma Updates...");
        if (ThingStatus.OFFLINE.equals(thing.getStatus())) {
            logger.debug("Doing relogin");
            login();
        }

        ArrayList<SomfyTahomaEvent> events = getEvents();
        logger.debug("Got total of {} events", events.size());
        for (SomfyTahomaEvent event : events) {
            processEvent(event);
        }
    }

    private void processEvent(SomfyTahomaEvent event) {
        logger.debug("Got event: {}", event.getName());
        switch (event.getName()) {
            case "DeviceStateChangedEvent":
                processStateChangedEvent(event);
                break;
            case "RefreshAllDevicesStatesCompletedEvent":
                scheduler.schedule(() -> {
                    //force update thing states
                    updateAllStates();
                }, 1, TimeUnit.SECONDS);
                break;
            default:
                //ignore other states
        }
    }

    private synchronized void updateAllStates() {
        logger.debug("Updating all states");
        SomfyTahomaSetup setup = listDevices();
        if (setup != null) {
            for (SomfyTahomaDevice device : setup.getDevices()) {
                String url = device.getDeviceURL();
                List<SomfyTahomaState> states = device.getStates();
                Thing th = getThingByDeviceUrl(url);
                if (th == null) {
                    continue;
                }
                SomfyTahomaBaseThingHandler handler = (SomfyTahomaBaseThingHandler) th.getHandler();
                if (handler != null) {
                    handler.updateThingStatus(states);
                }
            }
        }
    }

    private void processStateChangedEvent(SomfyTahomaEvent event) {
        String deviceUrl = event.getDeviceUrl();
        ArrayList<SomfyTahomaState> states = event.getDeviceStates();
        logger.debug("States for device {} : {}", deviceUrl, states.toString());
        Thing thing = getThingByDeviceUrl(deviceUrl);

        if (thing != null) {
            logger.debug("Updating status of thing: {}", thing.getUID().getId());
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

    private void refreshTahomaStates() {
        logger.debug("Refreshing Tahoma states...");
        if (ThingStatus.OFFLINE.equals(thing.getStatus())) {
            logger.debug("Doing relogin");
            login();
        }

        //force Tahoma to ask for actual states
        refreshDeviceStates();
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
            sendGetToTahomaWithCookie(TAHOMA_URL + "logout");
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.debug("Cannot send logout command!", e);
        }
    }

    private String sendDataToTahomaWithCookie(String url, String urlParameters) throws InterruptedException, ExecutionException, TimeoutException, SomfyTahomaException {
        logger.trace("Sending POST to Tahoma to url: {} with data: {}", url, urlParameters);
        ContentResponse response = sendRequestBuilder(url, HttpMethod.POST)
                .content(new StringContentProvider(urlParameters), "application/json;charset=UTF-8")
                .send();

        logger.trace("Response: {}", response.getContentAsString());
        if (response.getStatus() < 200 || response.getStatus() >= 300) {
            logger.error("Received error code: {}", response.getStatus());
            if (response.getStatus() == 404) {
                throw new SomfyTahomaException("Not logged in");
            }
        }
        return response.getContentAsString();
    }

    private String sendGetToTahomaWithCookie(String url) throws InterruptedException, ExecutionException, TimeoutException, SomfyTahomaException {
        return sendMethodToTahomaWithCookie(url, HttpMethod.GET);
    }

    private String sendPutToTahomaWithCookie(String url) throws InterruptedException, ExecutionException, TimeoutException, SomfyTahomaException {
        return sendMethodToTahomaWithCookie(url, HttpMethod.PUT);
    }

    private String sendDeleteToTahomaWithCookie(String url) throws InterruptedException, ExecutionException, TimeoutException, SomfyTahomaException {
        return sendMethodToTahomaWithCookie(url, HttpMethod.DELETE);
    }

    private String sendMethodToTahomaWithCookie(String url, HttpMethod method) throws InterruptedException, ExecutionException, TimeoutException, SomfyTahomaException {
        logger.trace("Sending {} to Tahoma to url: {}", method.asString(), url);
        ContentResponse response = sendRequestBuilder(url, method).send();

        logger.trace("Response: {}", response.getContentAsString());
        if (response.getStatus() < 200 || response.getStatus() >= 300) {
            logger.error("Received error code: {}", response.getStatus());
            if (response.getStatus() == 404) {
                throw new SomfyTahomaException("Not logged in");
            }
        }
        return response.getContentAsString();
    }

    private Request sendRequestBuilder(String url, HttpMethod method) {
        return httpClient.newRequest(url)
                .method(method)
                .header(HttpHeader.ACCEPT_LANGUAGE, "en-US,en")
                .header(HttpHeader.ACCEPT_ENCODING, "gzip, deflate")
                .header("X-Requested-With", "XMLHttpRequest")
                .timeout(TAHOMA_TIMEOUT, TimeUnit.SECONDS)
                .agent(TAHOMA_AGENT);
    }

    public void sendCommand(String io, String command, String params) {
        Boolean result = sendCommandInternal(io, command, params);
        if (result != null && !result) {
            sendCommandInternal(io, command, params);
        }
    }

    private @Nullable Boolean sendCommandInternal(String io, String command, String params) {
        String url;
        String line = "";

        try {
            url = TAHOMA_URL + "apply";

            String value = params.equals("[]") ? command : params.replace("\"", "");
            String urlParameters = "{\"label\":\"" + getThingLabelByURL(io) + " - " + value + " - OH2\",\"actions\":[{\"deviceURL\":\"" + io + "\",\"commands\":[{\"name\":\""
                    + command + "\",\"parameters\":" + params + "}]}]}";

            line = sendDataToTahomaWithCookie(url, urlParameters);

            SomfyTahomaApplyResponse data = gson.fromJson(line, SomfyTahomaApplyResponse.class);

            if (!StringUtils.isEmpty(data.getExecId())) {
                logger.debug("Exec id: {}", data.getExecId());
            } else {
                logger.warn("Apply command response: {}", line);
                throw new SomfyTahomaException(line);
            }
            return true;
        } catch (JsonSyntaxException e) {
            logger.debug("Received data: {} is not JSON", line, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Received invalid data");
        } catch (SomfyTahomaException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unauthorized. Please check credentials");
            return false;
        } catch (ExecutionException e) {
            if (isAuthenticationChallenge(e)) {
                login();
                return false;
            } else {
                logger.debug("Cannot send apply command {} with params {}!", command, params, e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } catch (InterruptedException | TimeoutException e) {
            logger.debug("Cannot send apply command {} with params {}!", command, params, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
        return null;
    }

    private String getThingLabelByURL(String io) {
        Thing th = getThingByDeviceUrl(io);
        if (th != null) {
            if (th.getProperties().containsKey("label")) {
                //Return label from Tahoma
                return th.getProperties().get("label").replace("\"", "");
            }
            //Return label from OH2
            return th.getLabel() != null ? th.getLabel().replace("\"", "") : "";
        }
        return "null";
    }

    public @Nullable String getCurrentExecutions(String type) {
        String execId = getCurrentExecutionsInternal(type);
        if (StringUtils.equals(execId, UNAUTHORIZED)) {
            execId = getCurrentExecutionsInternal(type);
        }
        return StringUtils.equals(execId, UNAUTHORIZED) ? null : execId;
    }

    private @Nullable String getCurrentExecutionsInternal(String type) {
        String url;
        String line = "";

        try {
            url = TAHOMA_URL + "getCurrentExecutions";
            String urlParameters = "";

            line = sendDataToTahomaWithCookie(url, urlParameters);

            SomfyTahomaExecutionsResponse data = gson.fromJson(line, SomfyTahomaExecutionsResponse.class);
            for (SomfyTahomaExecution execution : data.getExecutions()) {
                String execId = execution.getId();
                SomfyTahomaActionGroup group = execution.getActionGroup();
                for (SomfyTahomaAction action : group.getActions()) {
                    if (action.getDeviceURL().equals(type)) {
                        return execId;
                    }
                }
            }
            return null;
        } catch (JsonSyntaxException e) {
            logger.debug("Received data: {} is not JSON", line, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Received invalid data");
        } catch (SomfyTahomaException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unauthorized. Please check credentials");
            return UNAUTHORIZED;
        } catch (ExecutionException e) {
            if (isAuthenticationChallenge(e)) {
                login();
                return UNAUTHORIZED;
            } else {
                logger.debug("Cannot send getCurrentExecutions command!", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } catch (InterruptedException | TimeoutException e) {
            logger.debug("Cannot send getCurrentExecutions command!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
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
        } catch (SomfyTahomaException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unauthorized. Please check credentials");
            return false;
        } catch (ExecutionException e) {
            if (isAuthenticationChallenge(e)) {
                login();
                return false;
            } else {
                logger.debug("Cannot cancel execution!", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } catch (InterruptedException | TimeoutException e) {
            logger.debug("Cannot cancel execution!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
        return null;
    }

    public @Nullable String getTahomaVersion(String gatewayId) {
        String line = "";
        try {
            String url = SETUP_URL + gatewayId + "/version";
            line = sendGetToTahomaWithCookie(url);
            SomfyTahomaVersionResponse data = gson.fromJson(line, SomfyTahomaVersionResponse.class);
            logger.debug("Tahoma version: {}", data.getResult());

            return data.getResult();
        } catch (JsonSyntaxException e) {
            logger.debug("Received data: {} is not JSON", line, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Received invalid data");
        } catch (SomfyTahomaException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unauthorized. Please check credentials");
            return UNAUTHORIZED;
        } catch (ExecutionException e) {
            if (isAuthenticationChallenge(e)) {
                login();
                return UNAUTHORIZED;
            } else {
                logger.debug("Cannot get Tahoma gateway version!", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } catch (InterruptedException | TimeoutException e) {
            logger.debug("Cannot get Tahoma gateway version!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
        return null;
    }

    public void executeActionGroup(String id) {
        String execId = executeActionGroupInternal(id);
        if (UNAUTHORIZED.equals(execId)) {
            executeActionGroupInternal(id);
        }
    }

    public @Nullable String executeActionGroupInternal(String id) {
        String line = "";
        try {
            String url = EXEC_URL + id;

            line = sendDataToTahomaWithCookie(url, "");
            SomfyTahomaApplyResponse data = gson.fromJson(line, SomfyTahomaApplyResponse.class);
            if (data.getExecId().isEmpty()) {
                logger.debug("Got empty exec response");
            }
            return data.getExecId();
        } catch (JsonSyntaxException e) {
            logger.debug("Received data: {} is not JSON", line, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Received invalid data");
        } catch (SomfyTahomaException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unauthorized. Please check credentials");
            return UNAUTHORIZED;
        } catch (ExecutionException e) {
            if (isAuthenticationChallenge(e)) {
                login();
                return UNAUTHORIZED;
            } else {
                logger.debug("Cannot exec execution group!", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } catch (InterruptedException | TimeoutException e) {
            logger.debug("Cannot exec execution group!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
        return null;
    }

    public void refreshDeviceStates() {
        try {
            sendPutToTahomaWithCookie(REFRESH_URL);
        } catch (SomfyTahomaException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unauthorized. Please check credentials");
        } catch (ExecutionException e) {
            if (isAuthenticationChallenge(e)) {
                login();
            } else {
                logger.debug("Cannot refresh device states!", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } catch (InterruptedException | TimeoutException e) {
            logger.debug("Cannot refresh device states!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    public @Nullable String getTahomaStatus(String gatewayId) {
        String line = "";
        try {
            String url = SETUP_URL + gatewayId;
            line = sendGetToTahomaWithCookie(url);
            SomfyTahomaStatusResponse data = gson.fromJson(line, SomfyTahomaStatusResponse.class);
            logger.debug("Tahoma status: {}", data.getConnectivity().getStatus());
            return data.getConnectivity().getStatus();
        } catch (JsonSyntaxException e) {
            logger.debug("Received data: {} is not JSON", line, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Received invalid data");
        } catch (SomfyTahomaException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unauthorized. Please check credentials");
            return UNAUTHORIZED;
        } catch (ExecutionException e) {
            if (isAuthenticationChallenge(e)) {
                login();
                return UNAUTHORIZED;
            } else {
                logger.debug("Cannot get Tahoma gateway status!", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } catch (InterruptedException | TimeoutException e) {
            logger.debug("Cannot get Tahoma gateway status!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
        return null;
    }

    private boolean isAuthenticationChallenge(Exception ex) {
        return ex.getMessage().contains(AUTHENTICATION_CHALLENGE);
    }
}
