/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.somfytahoma.handler;

import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.thing.binding.ConfigStatusBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.somfytahoma.config.SomfyTahomaConfig;
import org.openhab.binding.somfytahoma.internal.SomfyTahomaException;
import org.openhab.binding.somfytahoma.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.openhab.binding.somfytahoma.SomfyTahomaBindingConstants.*;

/**
 * The {@link SomfyTahomaBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaBridgeHandler extends ConfigStatusBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(SomfyTahomaBridgeHandler.class);

    // Instantiate and configure the SslContextFactory
    private SslContextFactory sslContextFactory = new SslContextFactory();

    // Instantiate HttpClient with the SslContextFactory
    private HttpClient httpClient = new HttpClient(sslContextFactory);

    /**
     * Future to poll for updated
     */
    private ScheduledFuture<?> pollFuture;

    /**
     * Our configuration
     */
    protected SomfyTahomaConfig thingConfig;

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

        login();

        initPolling(thingConfig.getRefresh());
        logger.debug("Initialize done...");
    }

    /**
     * starts this things polling future
     */
    private void initPolling(int refresh) {
        stopPolling();
        pollFuture = scheduler.scheduleWithFixedDelay(() -> {
            updateTahomaStates();
        }, 10, refresh, TimeUnit.SECONDS);

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

            ContentResponse response = httpClient.newRequest(url)
                    .method(HttpMethod.POST)
                    .header(HttpHeader.ACCEPT_LANGUAGE, "en-US,en")
                    .header(HttpHeader.ACCEPT_ENCODING, "gzip, deflate")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .agent(TAHOMA_AGENT)
                    .content(new StringContentProvider(urlParameters), "application/x-www-form-urlencoded; charset=UTF-8")
                    .send();

            SomfyTahomaLoginResponse data = gson.fromJson(response.getContentAsString(), SomfyTahomaLoginResponse.class);

            if (data.isSuccess()) {
                logger.debug("SomfyTahoma version: {}", data.getVersion());
                updateStatus(ThingStatus.ONLINE);
            } else {
                logger.debug("Login response: {}", response.getContentAsString());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Error logging in");
                throw new SomfyTahomaException(response.getContentAsString());
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Cannot get login cookie!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Cannot get login cookie");
        } catch (Exception e) {
            logger.error("Cannot start http client", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Cannot start http client");
        }

    }

    private ArrayList<SomfyTahomaEvent> getEvents() {
        String url;
        String line = "";

        try {
            url = TAHOMA_URL + "getEvents";

            line = sendDataToTahomaWithCookie(url, "");
            logger.debug("Events response: {}", line);
            SomfyTahomaEvent[] array = gson.fromJson(line, SomfyTahomaEvent[].class);
            return filterStateChangedEvents(array);
        } catch (SomfyTahomaException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unauthorized");
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Cannot get Tahoma events!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
        return new ArrayList<>();
    }

    private ArrayList<SomfyTahomaEvent> filterStateChangedEvents(SomfyTahomaEvent[] events) {
        ArrayList<SomfyTahomaEvent> filtered = new ArrayList<>();

        for (SomfyTahomaEvent event : events) {
            if (event.getName().equals("DeviceStateChangedEvent")) {
                filtered.add(event);
            }
        }
        return filtered;
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
    }

    public ArrayList<SomfyTahomaActionGroup> listActionGroups() {
        String groups = getGroups();
        if (StringUtils.equals(groups, UNAUTHORIZED)) {
            login();
            groups = getGroups();
        }

        if (groups == null || groups.equals(UNAUTHORIZED)) {
            return new ArrayList<>();
        }

        SomfyTahomaActionGroupResponse data = gson.fromJson(groups, SomfyTahomaActionGroupResponse.class);
        return data.getActionGroups();
    }

    private String getGroups() {
        String url;

        try {
            url = TAHOMA_URL + "getActionGroups";
            String urlParameters = "";

            return sendDataToTahomaWithCookie(url, urlParameters);
        } catch (SomfyTahomaException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unauthorized");
            return UNAUTHORIZED;
        } catch (ExecutionException e) {
            if (e.toString().contains(AUTHENTICATION_CHALLENGE)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unauthorized");
                return UNAUTHORIZED;
            } else {
                logger.error("Cannot send getActionGroups command!", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } catch (InterruptedException | TimeoutException e) {
            logger.error("Cannot send getActionGroups command!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
        return null;
    }

    public SomfyTahomaSetup listDevices() {
        String url;

        try {
            url = TAHOMA_URL + "getSetup";
            String urlParameters = "";

            String line = sendDataToTahomaWithCookie(url, urlParameters);

            SomfyTahomaSetupResponse data = gson.fromJson(line, SomfyTahomaSetupResponse.class);
            return data.getSetup();
        } catch (SomfyTahomaException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unauthorized");
        } catch (ExecutionException e) {
            if (e.toString().contains(AUTHENTICATION_CHALLENGE)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unauthorized");
            } else {
                logger.error("Cannot send listDevices command!", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } catch (InterruptedException | TimeoutException e) {
            logger.error("Cannot send listDevices command!", e);
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


    public List<SomfyTahomaState> getAllStates(Collection<String> stateNames, String deviceUrl) {
        String url;
        String line = "";

        logger.debug("Getting states for a device: {}", deviceUrl);
        try {
            url = TAHOMA_URL + "getStates";
            String urlParameters = "[{\"deviceURL\": \"" + deviceUrl + "\", \"states\": ["
                    + getFormattedParameters(stateNames) + "]}]";

            line = sendDataToTahomaWithCookie(url, urlParameters);
            logger.trace("get states response:{}", line);

            SomfyTahomaStatesResponse data = gson.fromJson(line, SomfyTahomaStatesResponse.class);
            SomfyTahomaDeviceWithState device = data.getDevices().get(0);
            if (!device.hasStates()) {
                logger.debug("Device: {} has not returned any state", deviceUrl);
            }
            return device.getStates();
        } catch (SomfyTahomaException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unauthorized");
        } catch (ExecutionException e) {
            if (e.toString().contains(AUTHENTICATION_CHALLENGE)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unauthorized");
            } else {
                logger.error("Cannot send getStates command!", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } catch (InterruptedException | TimeoutException e) {
            logger.error("Cannot send getStates command!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }

        return null;
    }

    private void updateTahomaStates() {
        logger.debug("Updating Tahoma States...");
        if (thing.getStatus().equals(ThingStatus.OFFLINE)) {
            logger.debug("Doing relogin");
            login();
        }

        for (Thing th : getThing().getThings()) {
            logger.debug("Updating thing {} with UID {}", th.getLabel(), th.getThingTypeUID());
            if (th.getThingTypeUID().equals(THING_TYPE_GATEWAY)) {
                String id = th.getConfiguration().get("id").toString();
                updateGatewayState(th, id);
            }
        }

        ArrayList<SomfyTahomaEvent> events = getEvents();
        logger.debug("Got total of {} events", events.size());
        for (SomfyTahomaEvent event : events) {
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
    }

    private Thing getThingByDeviceUrl(String deviceUrl) {
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
            sendToTahomaWithCookie(TAHOMA_URL + "logout");
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Cannot send logout command!", e);
        }
    }

    private String sendToTahomaWithCookie(String url) throws InterruptedException, ExecutionException, TimeoutException, SomfyTahomaException {
        logger.trace("Sending GET to Tahoma url: {}", url);
        ContentResponse response = httpClient.newRequest(url)
                .method(HttpMethod.GET)
                .header(HttpHeader.ACCEPT_LANGUAGE, "en-US,en")
                .header(HttpHeader.ACCEPT_ENCODING, "gzip, deflate")
                .header("X-Requested-With", "XMLHttpRequest")
                .agent(TAHOMA_AGENT)
                .send();

        if (response.getStatus() < 200 || response.getStatus() >= 300) {
            logger.error("Received error code: {}", response.getStatus());
            if (response.getStatus() == 404) {
                throw new SomfyTahomaException("Not logged in");
            }
        }
        return response.getContentAsString();
    }

    private String sendDataToTahomaWithCookie(String url, String urlParameters) throws InterruptedException, ExecutionException, TimeoutException, SomfyTahomaException {
        logger.trace("Sending POST to Tahoma to url: {} with data: {}", url, urlParameters);

        ContentResponse response = httpClient.newRequest(url)
                .method(HttpMethod.POST)
                .header(HttpHeader.ACCEPT_LANGUAGE, "en-US,en")
                .header(HttpHeader.ACCEPT_ENCODING, "gzip, deflate")
                .header("X-Requested-With", "XMLHttpRequest")
                .agent(TAHOMA_AGENT)
                .content(new StringContentProvider(urlParameters))
                .send();

        if (response.getStatus() < 200 || response.getStatus() >= 300) {
            logger.error("Received error code: {}", response.getStatus());
            if (response.getStatus() == 404) {
                throw new SomfyTahomaException("Not logged in");
            }
        }
        return response.getContentAsString();
    }

    private String sendDeleteToTahomaWithCookie(String url) throws InterruptedException, ExecutionException, TimeoutException, SomfyTahomaException {
        logger.trace("Sending DELETE to Tahoma to url: {}", url);

        ContentResponse response = httpClient.newRequest(url)
                .method(HttpMethod.DELETE)
                .header(HttpHeader.ACCEPT_LANGUAGE, "en-US,en")
                .header(HttpHeader.ACCEPT_ENCODING, "gzip, deflate")
                .header("X-Requested-With", "XMLHttpRequest")
                .agent(TAHOMA_AGENT)
                .send();

        if (response.getStatus() < 200 || response.getStatus() >= 300) {
            logger.error("Received error code: {}", response.getStatus());
            if (response.getStatus() == 404) {
                throw new SomfyTahomaException("Not logged in");
            }
        }
        return response.getContentAsString();
    }

    public void sendCommand(String io, String command, String params) {
        Boolean result = sendCommandInternal(io, command, params);
        if (result != null && !result) {
            login();
            sendCommandInternal(io, command, params);
        }
    }

    private Boolean sendCommandInternal(String io, String command, String params) {
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
        } catch (SomfyTahomaException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unauthorized");
            return false;
        } catch (ExecutionException e) {
            if (e.toString().contains(AUTHENTICATION_CHALLENGE)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unauthorized");
                return false;
            } else {
                logger.error("Cannot send apply command {} with params {}!", command, params, e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } catch (InterruptedException | TimeoutException e) {
            logger.error("Cannot send apply command {} with params {}!", command, params, e);
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

    public String getCurrentExecutions(String type) {
        String execId = getCurrentExecutionsInternal(type);
        if (StringUtils.equals(execId, UNAUTHORIZED)) {
            login();
            execId = getCurrentExecutionsInternal(type);
        }
        return StringUtils.equals(execId, UNAUTHORIZED) ? null : execId;
    }

    private String getCurrentExecutionsInternal(String type) {
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
        } catch (SomfyTahomaException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unauthorized");
            return UNAUTHORIZED;
        } catch (ExecutionException e) {
            if (e.toString().contains(AUTHENTICATION_CHALLENGE)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unauthorized");
                return UNAUTHORIZED;
            } else {
                logger.error("Cannot send getCurrentExecutions command!", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } catch (InterruptedException | TimeoutException e) {
            logger.error("Cannot send getCurrentExecutions command!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
        return null;
    }

    public void cancelExecution(String executionId) {
        Boolean result = cancelExecutionInternal(executionId);
        if (result != null && !result) {
            login();
            cancelExecutionInternal(executionId);
        }
    }

    private Boolean cancelExecutionInternal(String executionId) {
        String url;

        try {
            url = DELETE_URL + executionId;
            sendDeleteToTahomaWithCookie(url);
            return true;
        } catch (SomfyTahomaException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unauthorized");
            return false;
        } catch (ExecutionException e) {
            if (e.toString().contains(AUTHENTICATION_CHALLENGE)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unauthorized");
                return false;
            } else {
                logger.error("Cannot cancel execution!", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } catch (InterruptedException | TimeoutException e) {
            logger.error("Cannot cancel execution!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
        return null;
    }

    private void updateGatewayState(Thing thing, String id) {
        if (thing.getChannels().size() == 0) {
            return;
        }
        String version = getTahomaVersion(id);
        if (StringUtils.equals(version, UNAUTHORIZED)) {
            login();
            version = getTahomaVersion(id);
        }

        if (version == null || version.equals(UNAUTHORIZED)) {
            return;
        }

        String status = getTahomaStatus(id);

        for (Channel channel : thing.getChannels()) {
            if (channel.getUID().getId().equals(VERSION)) {
                logger.debug("Updating version channel");
                updateState(channel.getUID(), new StringType(version));
            } else if (channel.getUID().getId().equals(STATUS)) {
                logger.debug("Updating status channel");
                updateState(channel.getUID(), new StringType(status));
            }
        }

    }

    public String getTahomaVersion(String gatewayId) {
        String line = "";
        try {
            String url = SETUP_URL + gatewayId + "/version";
            line = sendToTahomaWithCookie(url);
            SomfyTahomaVersionResponse data = gson.fromJson(line, SomfyTahomaVersionResponse.class);
            logger.debug("Tahoma version: {}", data.getResult());

            return data.getResult();
        } catch (SomfyTahomaException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unauthorized");
            return UNAUTHORIZED;
        } catch (ExecutionException e) {
            if (e.toString().contains(AUTHENTICATION_CHALLENGE)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unauthorized");
                return UNAUTHORIZED;
            } else {
                logger.error("Cannot get Tahoma gateway version!", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } catch (InterruptedException | TimeoutException e) {
            logger.error("Cannot get Tahoma gateway version!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
        return null;
    }

    public void executeActionGroup(String id) {
        String execId = executeActionGroupInternal(id);
        if (execId.equals(UNAUTHORIZED)) {
            login();
            executeActionGroupInternal(id);
        }
    }

    public String executeActionGroupInternal(String id) {
        String line = "";
        try {
            String url = EXEC_URL + id;

            line = sendDataToTahomaWithCookie(url, "");
            SomfyTahomaApplyResponse data = gson.fromJson(line, SomfyTahomaApplyResponse.class);
            if (data.getExecId() == null) {
                logger.warn("Got empty exec response");
            }
            return data.getExecId();
        } catch (SomfyTahomaException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unauthorized");
            return UNAUTHORIZED;
        } catch (ExecutionException e) {
            if (e.toString().contains(AUTHENTICATION_CHALLENGE)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unauthorized");
                return UNAUTHORIZED;
            } else {
                logger.error("Cannot exec execution group!", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } catch (InterruptedException | TimeoutException e) {
            logger.error("Cannot exec execution group!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
        return null;
    }

    public String getTahomaStatus(String gatewayId) {
        String line = "";
        try {
            String url = SETUP_URL + gatewayId;
            line = sendToTahomaWithCookie(url);
            SomfyTahomaStatusResponse data = gson.fromJson(line, SomfyTahomaStatusResponse.class);
            if (data.getConnectivity() == null) {
                logger.warn("Got empty connectivity response");
                return "N/A";
            }
            logger.debug("Tahoma status: {}", data.getConnectivity().getStatus());
            return data.getConnectivity().getStatus();
        } catch (SomfyTahomaException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unauthorized");
            return UNAUTHORIZED;
        } catch (ExecutionException e) {
            if (e.toString().contains(AUTHENTICATION_CHALLENGE)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unauthorized");
                return UNAUTHORIZED;
            } else {
                logger.error("Cannot get Tahoma gateway status!", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } catch (InterruptedException | TimeoutException e) {
            logger.error("Cannot get Tahoma gateway status!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
        return null;
    }

}
