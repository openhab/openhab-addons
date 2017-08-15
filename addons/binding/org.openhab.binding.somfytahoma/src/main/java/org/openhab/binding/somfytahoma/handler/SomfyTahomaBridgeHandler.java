/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.somfytahoma.handler;

import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.thing.binding.ConfigStatusBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.somfytahoma.config.SomfyTahomaConfig;
import org.openhab.binding.somfytahoma.internal.SomfyTahomaException;
import org.openhab.binding.somfytahoma.internal.discovery.SomfyTahomaItemDiscoveryService;
import org.openhab.binding.somfytahoma.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.openhab.binding.somfytahoma.SomfyTahomaBindingConstants.*;

/**
 * The {@link SomfyTahomaBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaBridgeHandler extends ConfigStatusBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(SomfyTahomaBridgeHandler.class);

    private String cookie;

    /**
     * Future to poll for updated
     */
    private ScheduledFuture<?> pollFuture;

    /**
     * Our configuration
     */
    protected SomfyTahomaConfig thingConfig;

    //Gson & parser
    private final Gson gson = new Gson();

    private SomfyTahomaItemDiscoveryService discoveryService = null;

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

        login();
        scheduler.schedule(() -> startDiscovery(), 1, TimeUnit.SECONDS);
        initPolling(thingConfig.getRefresh());
    }

    /**
     * starts this things polling future
     */
    private void initPolling(int refresh) {
        stopPolling();
        pollFuture = scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    updateTahomaStates();
                } catch (Exception e) {
                    logger.debug("Exception during poll!", e);
                }
            }
        }, 10, refresh, TimeUnit.SECONDS);

    }

    private synchronized void login() {
        String url = null;

        if (StringUtils.isEmpty(thingConfig.getEmail()) || StringUtils.isEmpty(thingConfig.getPassword())) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Can not access device as username and/or password are null");
            return;
        }

        try {
            url = TAHOMA_URL + "login";
            String urlParameters = "userId=" + thingConfig.getEmail() + "&userPassword=" + thingConfig.getPassword();
            byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);

            URL cookieUrl = new URL(url);
            HttpsURLConnection connection = (HttpsURLConnection) cookieUrl.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            setConnectionDefaults(connection);
            connection.setRequestProperty("Content-Length", Integer.toString(postData.length));
            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                wr.write(postData);
            }

            //get cookie
            String headerName;
            for (int i = 1; (headerName = connection.getHeaderFieldKey(i)) != null; i++) {
                if (headerName.equals("Set-Cookie")) {
                    cookie = connection.getHeaderField(i);
                    break;
                }
            }

            InputStream response = connection.getInputStream();
            String line = readResponse(response);

            SomfyTahomaLoginResponse data = gson.fromJson(line, SomfyTahomaLoginResponse.class);

            if (data.isSuccess()) {
                String version = data.getVersion();
                logger.debug("SomfyTahoma cookie: {}", cookie);
                logger.debug("SomfyTahoma version: {}", version);
                updateStatus(ThingStatus.ONLINE);
            } else {
                logger.debug("Login response: {}", line);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Error logging in");
                throw new SomfyTahomaException(line);
            }
        } catch (MalformedURLException e) {
            logger.error("The URL '{}' is malformed!", url, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "The URL '" + url + "' is malformed: ");
        } catch (Exception e) {
            logger.error("Cannot get login cookie!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Cannot get login cookie");
        }
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

    public void startDiscovery() {
        if (discoveryService != null) {
            listDevices();
            listActionGroups();
        }
    }

    @Override
    public void dispose() {
        stopPolling();
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


    private void listActionGroups() {
        String groups = getGroups();

        SomfyTahomaActionGroupResponse data = gson.fromJson(groups, SomfyTahomaActionGroupResponse.class);
        for (SomfyTahomaActionGroup group : data.getActionGroups()) {
            String oid = group.getOid();
            String label = group.getLabel();
            //actiongroups use oid as deviceURL
            String deviceURL = oid;
            discoveryService.actionGroupDiscovered(label, deviceURL, oid);
        }
    }

    private String getGroups() {
        String url = null;

        try {
            url = TAHOMA_URL + "getActionGroups";
            String urlParameters = "";

            InputStream response = sendDataToTahomaWithCookie(url, urlParameters);

            return readResponse(response);

        } catch (MalformedURLException e) {
            logger.error("The URL '{}' is malformed!", url, e);
        } catch (IOException e) {
            if (e.toString().contains(UNAUTHORIZED)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unauthorized");
            }
            logger.error("Cannot send getActionGroups command!", e);
        } catch (Exception e) {
            logger.error("Cannot send getActionGroups command!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
        return "";
    }

    private void listDevices() {
        String url = null;

        try {
            url = TAHOMA_URL + "getSetup";
            String urlParameters = "";

            InputStream response = sendDataToTahomaWithCookie(url, urlParameters);

            String line = readResponse(response);

            SomfyTahomaSetupResponse data = gson.fromJson(line, SomfyTahomaSetupResponse.class);
            SomfyTahomaSetup setup = data.getSetup();
            for (SomfyTahomaDevice device : setup.getDevices()) {
                if (device.isRollerShutter()) {
                    discoveryService.rollerShutterDiscovered(device.getLabel(), device.getDeviceURL(), device.getOid());
                    continue;
                }
                if (device.isAwning()) {
                    discoveryService.awningDiscovered(device.getLabel(), device.getDeviceURL(), device.getOid());
                    continue;
                }
                if (device.isOnOff()) {
                    discoveryService.onOffDiscovered(device.getLabel(), device.getDeviceURL(), device.getOid());
                    continue;
                }
                if (device.isExteriorScreen()) {
                    discoveryService.exteriorScreenDiscovered(device.getLabel(), device.getDeviceURL(), device.getOid());
                    continue;
                }
                if (device.isGarageDoor()) {
                    discoveryService.garageDoorDiscovered(device.getLabel(), device.getDeviceURL(), device.getOid());
                    continue;
                }
                if (!device.isKnownUnsupported()) {
                    logger.warn("Detected a new unsupported device: {}", device.getUiClass());
                }
            }
            for (SomfyTahomaGateway gateway : setup.getGateways()) {
                discoveryService.gatewayDiscovered(gateway.getGatewayId());
            }
        } catch (MalformedURLException e) {
            logger.error("The URL '{}' is malformed!", url, e);
        } catch (IOException e) {
            if (e.toString().contains(UNAUTHORIZED)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unauthorized");
            }
            logger.error("Cannot send listDevices command!", e);
        } catch (Exception e) {
            logger.error("Cannot send listDevices command!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }

    }

    public void setDiscoveryService(SomfyTahomaItemDiscoveryService somfyTahomaItemDiscoveryService) {
        this.discoveryService = somfyTahomaItemDiscoveryService;
    }

    private State getState(SomfyTahomaThingHandler handler, String deviceUrl) {
        String url = null;

        logger.debug("Getting state for a device: {}", deviceUrl);
        try {
            url = TAHOMA_URL + "getStates";
            String urlParameters = "[{\"deviceURL\": \"" + deviceUrl + "\", \"states\": [{\"name\": \"" + handler.getStateName() + "\"}]}]";

            InputStream response = sendDataToTahomaWithCookie(url, urlParameters);
            String line = readResponse(response);

            SomfyTahomaStatesResponse data = gson.fromJson(line, SomfyTahomaStatesResponse.class);
            SomfyTahomaDeviceWithState device = data.getDevices().get(0);
            if (device.hasStates()) {
                SomfyTahomaState state = device.getStates().get(0);
                if (state.getType() == TYPE_PERCENT) {
                    Double value = (Double) state.getValue();
                    return new PercentType(value.intValue());
                }
                if (state.getType() == TYPE_ONOFF) {
                    String value = state.getValue().toString().toLowerCase();
                    return value.equals("on") ? OnOffType.ON : OnOffType.OFF;
                }
            } else {
                logger.warn("Device: {} has not returned any state", deviceUrl);
                return UnDefType.UNDEF;
            }
        } catch (MalformedURLException e) {
            logger.error("The URL '{}' is malformed!", url, e);
        } catch (IOException e) {
            if (e.toString().contains(UNAUTHORIZED)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unauthorized");
                return UnDefType.NULL;
            }
        } catch (Exception e) {
            logger.error("Cannot send getStates command!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }

        return null;
    }

    private void updateTahomaStates() {
        logger.debug("Updating Tahoma States...");
        for (Thing thing : getThing().getThings()) {
            logger.debug("Updating thing {} with UID {}", thing.getLabel(), thing.getThingTypeUID());
            if (thing.getThingTypeUID().equals(THING_TYPE_GATEWAY)) {
                String id = thing.getConfiguration().get("id").toString();
                updateGatewayState(thing, id);
            } else {
                String url = thing.getConfiguration().get("url").toString();
                updateThingState(thing, url);
            }
        }
    }

    public void updateThingState(Thing thing, String url) {
        SomfyTahomaThingHandler handler = (SomfyTahomaThingHandler) thing.getHandler();
        if (handler.getStateName() != null) {
            State state = getState(handler, url);
            if (state == null || state.equals(UnDefType.UNDEF)) {
                return;
            }

            if (state.equals(UnDefType.NULL)) {
                //relogin
                login();
                state = getState(handler, url);
            }

            for (Channel channel : thing.getChannels()) {
                updateState(channel.getUID(), state);
            }
        }

    }

    public void updateChannelState(SomfyTahomaThingHandler handler, ChannelUID channelUID, String url) {
        if (handler.getStateName() != null) {
            State state = getState(handler, url);
            if (state == null || state.equals(UnDefType.UNDEF)) {
                return;
            }

            if (state.equals(UnDefType.NULL)) {
                //relogin
                login();
                state = getState(handler, url);
            }

            updateState(channelUID, state);
        }
    }

    private void logout() {
        try {
            sendToTahomaWithCookie(TAHOMA_URL + "logout");
            cookie = "";
        } catch (Exception e) {
            logger.error("Cannot send logout command!", e);
        }
    }

    public String readResponse(InputStream response) throws Exception {
        String line;
        StringBuilder body = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(response));

        while ((line = reader.readLine()) != null) {
            body.append(line).append("\n");
        }
        line = body.toString();
        logger.trace("Response: {}", line);
        return line;
    }

    public InputStream sendToTahomaWithCookie(String url) throws Exception {
        logger.trace("Sending GET to Tahoma url: {}", url);
        URL cookieUrl = new URL(url);
        HttpsURLConnection connection = (HttpsURLConnection) cookieUrl.openConnection();
        connection.setDoOutput(false);
        connection.setRequestMethod("GET");
        setConnectionDefaults(connection);
        connection.setRequestProperty("Cookie", cookie);

        return connection.getInputStream();
    }

    public InputStream sendDataToTahomaWithCookie(String url, String postData) throws Exception {
        logger.trace("Sending POST to Tahoma to url: {} with data: {}", url, postData);
        URL cookieUrl = new URL(url);
        HttpsURLConnection connection = (HttpsURLConnection) cookieUrl.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        setConnectionDefaults(connection);
        connection.setRequestProperty("Content-Length", Integer.toString(postData.length()));
        connection.setRequestProperty("Cookie", cookie);
        try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
            wr.write(postData.getBytes(StandardCharsets.UTF_8));
        }

        return connection.getInputStream();
    }

    public void setConnectionDefaults(HttpsURLConnection connection) {
        connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("User-Agent", TAHOMA_AGENT);
        connection.setRequestProperty("Accept-Language", "en-US");
        connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
        connection.setUseCaches(false);
    }

    public InputStream sendDeleteToTahomaWithCookie(String url) throws Exception {

        URL cookieUrl = new URL(url);
        HttpsURLConnection connection = (HttpsURLConnection) cookieUrl.openConnection();
        connection.setDoOutput(false);
        connection.setRequestMethod("DELETE");
        setConnectionDefaults(connection);
        connection.setRequestProperty("Cookie", cookie);

        return connection.getInputStream();
    }

    public void sendCommand(String io, String command, String params) {
        String url = null;

        try {
            url = TAHOMA_URL + "apply";

            String urlParameters = "{\"actions\": [{\"deviceURL\": \"" + io + "\", \"commands\": [{ \"name\": \"" + command + "\", \"parameters\": " + params + "}]}]}";
            logger.debug("Sending apply: {}", urlParameters);

            InputStream response = sendDataToTahomaWithCookie(url, urlParameters);
            String line = readResponse(response);

            SomfyTahomaApplyResponse data = gson.fromJson(line, SomfyTahomaApplyResponse.class);

            if (!StringUtils.isEmpty(data.getExecId())) {
                logger.debug("Exec id: {}", data.getExecId());
            } else {
                logger.warn("Command response: {}", line);
                throw new SomfyTahomaException(line);
            }
        } catch (MalformedURLException e) {
            logger.error("The URL '{}' is malformed!", url, e);
        } catch (IOException e) {
            if (e.toString().contains(UNAUTHORIZED)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unauthorized");
            }
            logger.error("Cannot send apply command!", e);
        } catch (Exception e) {
            logger.error("Cannot send apply command!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    public String getCurrentExecutions(String type) {
        String url = null;

        try {
            url = TAHOMA_URL + "getCurrentExecutions";
            String urlParameters = "";

            InputStream response = sendDataToTahomaWithCookie(url, urlParameters);
            String line = readResponse(response);

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
        } catch (MalformedURLException e) {
            logger.error("The URL '{}' is malformed!", url, e);
        } catch (IOException e) {
            if (e.toString().contains(UNAUTHORIZED)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unauthorized");
            }
            logger.error("Cannot send getCurrentExecutions command!", e);
        } catch (Exception e) {
            logger.error("Cannot send getCurrentExecutions command!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }

        return null;
    }

    public void cancelExecution(String executionId) {
        String url = null;

        try {
            url = DELETE_URL + executionId;
            sendDeleteToTahomaWithCookie(url);

        } catch (MalformedURLException e) {
            logger.error("The URL '{}' is malformed!", e);
        } catch (IOException e) {
            if (e.toString().contains(UNAUTHORIZED)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unauthorized");
            }
            logger.error("Cannot cancel execution!", e);
        } catch (Exception e) {
            logger.error("Cannot cancel execution!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    public ArrayList<SomfyTahomaAction> getTahomaActions(String actionGroup) {
        String groups = getGroups();

        SomfyTahomaActionGroupResponse data = gson.fromJson(groups, SomfyTahomaActionGroupResponse.class);
        for (SomfyTahomaActionGroup group : data.getActionGroups()) {
            if (group.getOid().equals(actionGroup)) {
                return group.getActions();
            }
        }

        return new ArrayList<>();
    }

    private void updateGatewayState(Thing thing, String id) {
        if (thing.getChannels().size() == 0) {
            return;
        }
        String version = getTahomaVersion(id);
        for (Channel channel : thing.getChannels()) {
            if (channel.getUID().getId().equals(VERSION)) {
                logger.debug("Updating version channel");
                updateState(channel.getUID(), new StringType(version));
            }
        }

    }

    public String getTahomaVersion(String gatewayId) {
        try {
            String url = SETUP_URL + gatewayId + "/version";
            InputStream response = sendToTahomaWithCookie(url);
            String line = readResponse(response);
            SomfyTahomaVersionResponse data = gson.fromJson(line, SomfyTahomaVersionResponse.class);
            logger.debug("Tahoma version: {}", data.getResult());

            return data.getResult();
        } catch (Exception e) {
            logger.error("Cannot get Tahoma gateway version!", e);
        }
        return "";
    }
}
