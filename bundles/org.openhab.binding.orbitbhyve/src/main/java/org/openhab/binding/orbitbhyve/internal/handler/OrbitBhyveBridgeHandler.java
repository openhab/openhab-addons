/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.orbitbhyve.internal.handler;

import static org.openhab.binding.orbitbhyve.internal.OrbitBhyveBindingConstants.*;

import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketException;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.orbitbhyve.internal.OrbitBhyveConfiguration;
import org.openhab.binding.orbitbhyve.internal.discovery.OrbitBhyveDiscoveryService;
import org.openhab.binding.orbitbhyve.internal.model.OrbitBhyveDevice;
import org.openhab.binding.orbitbhyve.internal.model.OrbitBhyveProgram;
import org.openhab.binding.orbitbhyve.internal.model.OrbitBhyveSessionResponse;
import org.openhab.binding.orbitbhyve.internal.model.OrbitBhyveSocketEvent;
import org.openhab.binding.orbitbhyve.internal.net.OrbitBhyveSocket;
import org.openhab.core.config.core.status.ConfigStatusMessage;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ConfigStatusBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link OrbitBhyveBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class OrbitBhyveBridgeHandler extends ConfigStatusBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(OrbitBhyveBridgeHandler.class);

    private final HttpClient httpClient;

    private final WebSocketClient webSocketClient;

    private @Nullable ScheduledFuture<?> future = null;

    private @Nullable Session session;

    private @Nullable String sessionToken = null;

    private OrbitBhyveConfiguration config = new OrbitBhyveConfiguration();

    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    // Gson & parser
    private final Gson gson = new Gson();

    public OrbitBhyveBridgeHandler(Bridge thing, HttpClient httpClient, WebSocketClient webSocketClient) {
        super(thing);
        this.httpClient = httpClient;
        this.webSocketClient = webSocketClient;
    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        return Collections.emptyList();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(OrbitBhyveDiscoveryService.class);
    }

    @Override
    public void initialize() {
        config = getConfigAs(OrbitBhyveConfiguration.class);
        httpClient.setFollowRedirects(false);

        scheduler.execute(() -> {
            login();
            future = scheduler.scheduleWithFixedDelay(this::ping, 0, config.refresh, TimeUnit.SECONDS);
        });
        logger.debug("Finished initializing!");
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> localFuture = future;
        if (localFuture != null) {
            localFuture.cancel(true);
        }
        closeSession();
        super.dispose();
    }

    private boolean login() {
        try {
            String urlParameters = "{\"session\":{\"email\":\"" + config.email + "\",\"password\":\"" + config.password
                    + "\"}}";
            ContentResponse response = httpClient.newRequest(BHYVE_SESSION).method(HttpMethod.POST).agent(AGENT)
                    .content(new StringContentProvider(urlParameters), "application/json; charset=utf-8")
                    .timeout(BHYVE_TIMEOUT, TimeUnit.SECONDS).send();
            if (response.getStatus() == 200) {
                if (logger.isTraceEnabled()) {
                    logger.trace("response: {}", response.getContentAsString());
                }
                OrbitBhyveSessionResponse session = gson.fromJson(response.getContentAsString(),
                        OrbitBhyveSessionResponse.class);
                sessionToken = session.getOrbitSessionToken();
                logger.debug("token: {}", sessionToken);
                initializeWebSocketSession();
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Login response status:" + response.getStatus());
                return false;
            }
        } catch (TimeoutException | ExecutionException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Exception during login");
            return false;
        } catch (InterruptedException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Exception during login");
            Thread.currentThread().interrupt();
            return false;
        }
        updateStatus(ThingStatus.ONLINE);
        return true;
    }

    private synchronized void ping() {
        if (ThingStatus.OFFLINE == thing.getStatus()) {
            login();
        }

        if (ThingStatus.ONLINE == thing.getStatus()) {
            Session localSession = session;
            if (localSession == null || !localSession.isOpen()) {
                initializeWebSocketSession();
            }
            localSession = session;
            if (localSession != null && localSession.isOpen() && localSession.getRemote() != null) {
                try {
                    logger.debug("Sending ping");
                    localSession.getRemote().sendString("{\"event\":\"ping\"}");
                    updateAllStatuses();
                } catch (IOException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Error sending ping (IOException on web socket)");
                } catch (WebSocketException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            String.format("Error sending ping (WebSocketException: %s)", e.getMessage()));
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Web socket creation error");
            }
        }
    }

    public List<OrbitBhyveDevice> getDevices() {
        try {
            ContentResponse response = sendRequestBuilder(BHYVE_DEVICES, HttpMethod.GET).send();
            if (response.getStatus() == 200) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Devices response: {}", response.getContentAsString());
                }
                OrbitBhyveDevice[] devices = gson.fromJson(response.getContentAsString(), OrbitBhyveDevice[].class);
                return Arrays.asList(devices);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Get devices returned response status: " + response.getStatus());
            }
        } catch (TimeoutException | ExecutionException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Error during getting devices");
        } catch (InterruptedException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Error during getting devices");
            Thread.currentThread().interrupt();
        }
        return new ArrayList<>();
    }

    Request sendRequestBuilder(String uri, HttpMethod method) {
        return httpClient.newRequest(uri).method(method).agent(AGENT).header("Orbit-Session-Token", sessionToken)
                .timeout(BHYVE_TIMEOUT, TimeUnit.SECONDS);
    }

    public @Nullable OrbitBhyveDevice getDevice(String deviceId) {
        try {
            ContentResponse response = sendRequestBuilder(BHYVE_DEVICES + "/" + deviceId, HttpMethod.GET).send();
            if (response.getStatus() == 200) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Device response: {}", response.getContentAsString());
                }
                return gson.fromJson(response.getContentAsString(), OrbitBhyveDevice.class);
            } else {
                logger.debug("Returned status: {}", response.getStatus());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Returned status: " + response.getStatus());
            }
        } catch (TimeoutException | ExecutionException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error during getting device info: " + deviceId);
        } catch (InterruptedException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error during getting device info: " + deviceId);
            Thread.currentThread().interrupt();
        }
        return null;
    }

    public synchronized void processStatusResponse(String content) {
        updateStatus(ThingStatus.ONLINE);
        logger.trace("Got message: {}", content);
        OrbitBhyveSocketEvent event = gson.fromJson(content, OrbitBhyveSocketEvent.class);
        if (event != null) {
            processEvent(event);
        }
    }

    private void processEvent(OrbitBhyveSocketEvent event) {
        switch (event.getEvent()) {
            case "watering_in_progress_notification":
                disableZones(event.getDeviceId());
                Channel channel = getThingChannel(event.getDeviceId(), event.getStation());
                if (channel != null) {
                    logger.debug("Watering zone: {}", event.getStation());
                    updateState(channel.getUID(), OnOffType.ON);
                    String program = event.getProgram().getAsString();
                    if (!program.isEmpty() && !"manual".equals(program)) {
                        channel = getThingChannel(event.getDeviceId(), "program_" + program);
                        if (channel != null) {
                            updateState(channel.getUID(), OnOffType.ON);
                        }
                    }
                }
                break;
            case "watering_complete":
                logger.debug("Watering complete");
                disableZones(event.getDeviceId());
                disablePrograms(event.getDeviceId());
                updateDeviceStatus(event.getDeviceId());
                break;
            case "change_mode":
                logger.debug("Updating mode to: {}", event.getMode());
                Channel ch = getThingChannel(event.getDeviceId(), CHANNEL_MODE);
                if (ch != null) {
                    updateState(ch.getUID(), new StringType(event.getMode()));
                }
                ch = getThingChannel(event.getDeviceId(), CHANNEL_CONTROL);
                if (ch != null) {
                    updateState(ch.getUID(), OnOffType.from(!"off".equals(event.getMode())));
                }
                updateDeviceStatus(event.getDeviceId());
                break;
            case "rain_delay":
                updateDeviceStatus(event.getDeviceId());
                break;
            case "skip_active_station":
                disableZones(event.getDeviceId());
                break;
            case "program_changed":
                OrbitBhyveProgram program = gson.fromJson(event.getProgram(), OrbitBhyveProgram.class);
                if (program != null) {
                    updateDeviceProgramStatus(program);
                    updateDeviceStatus(program.getDeviceId());
                }
                break;
            default:
                logger.debug("Received event: {}", event.getEvent());
        }
    }

    private void updateAllStatuses() {
        List<OrbitBhyveDevice> devices = getDevices();
        for (Thing th : getThing().getThings()) {
            if (th.isEnabled()) {
                String deviceId = th.getUID().getId();
                ThingHandler handler = th.getHandler();
                if (handler instanceof OrbitBhyveSprinklerHandler sprinklerHandler) {
                    for (OrbitBhyveDevice device : devices) {
                        if (deviceId.equals(th.getUID().getId())) {
                            updateDeviceStatus(device, sprinklerHandler);
                        }
                    }
                }
            }
        }
    }

    private void updateDeviceStatus(@Nullable OrbitBhyveDevice device, @Nullable OrbitBhyveSprinklerHandler handler) {
        if (device != null && handler != null) {
            handler.setDeviceOnline(device.isConnected());
            handler.updateDeviceStatus(device.getStatus());
            handler.updateSmartWatering(device.getWaterSenseMode());
            return;
        }
    }

    private void updateDeviceStatus(String deviceId) {
        for (Thing th : getThing().getThings()) {
            if (deviceId.equals(th.getUID().getId())) {
                ThingHandler handler = th.getHandler();
                if (handler instanceof OrbitBhyveSprinklerHandler sprinklerHandler) {
                    OrbitBhyveDevice device = getDevice(deviceId);
                    updateDeviceStatus(device, sprinklerHandler);
                }
            }
        }
    }

    private void updateDeviceProgramStatus(OrbitBhyveProgram program) {
        for (Thing th : getThing().getThings()) {
            if (program.getDeviceId().equals(th.getUID().getId())) {
                ThingHandler handler = th.getHandler();
                if (handler instanceof OrbitBhyveSprinklerHandler sprinklerHandler) {
                    sprinklerHandler.updateProgram(program);
                }
            }
        }
    }

    private void disableZones(String deviceId) {
        disableChannel(deviceId, "zone_");
    }

    private void disablePrograms(String deviceId) {
        disableChannel(deviceId, "program_");
    }

    private void disableChannel(String deviceId, String name) {
        for (Thing th : getThing().getThings()) {
            if (deviceId.equals(th.getUID().getId())) {
                for (Channel ch : th.getChannels()) {
                    if (ch.getUID().getId().startsWith(name)) {
                        updateState(ch.getUID(), OnOffType.OFF);
                    }
                }
                return;
            }
        }
    }

    private @Nullable Channel getThingChannel(String deviceId, int station) {
        for (Thing th : getThing().getThings()) {
            if (deviceId.equals(th.getUID().getId())) {
                return th.getChannel("zone_" + station);
            }
        }
        logger.debug("Cannot find zone: {} for device: {}", station, deviceId);
        return null;
    }

    private @Nullable Channel getThingChannel(String deviceId, String name) {
        for (Thing th : getThing().getThings()) {
            if (deviceId.equals(th.getUID().getId())) {
                return th.getChannel(name);
            }
        }
        logger.debug("Cannot find channel: {} for device: {}", name, deviceId);
        return null;
    }

    private @Nullable Session createSession() {
        String url = BHYVE_WS_URL;
        URI uri = URI.create(url);

        try {
            // The socket that receives events
            OrbitBhyveSocket socket = new OrbitBhyveSocket(this);
            // Attempt Connect
            Future<Session> fut = webSocketClient.connect(socket, uri);
            // Wait for Connect
            return fut.get();
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Cannot connect websocket client");
        } catch (InterruptedException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Cannot create websocket session");
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Cannot create websocket session");
        }
        return null;
    }

    private synchronized void initializeWebSocketSession() {
        logger.debug("Initializing WebSocket session");
        closeSession();
        session = createSession();
        Session localSession = session;
        if (localSession != null) {
            logger.debug("WebSocket connected!");
            try {
                String msg = "{\"event\":\"app_connection\",\"orbit_session_token\":\"" + sessionToken + "\"}";
                logger.trace("sending message:\n {}", msg);
                localSession.getRemote().sendString(msg);
            } catch (IOException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Error sending hello string (IOException on web socket)");
            } catch (WebSocketException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        String.format("Error sending hello string (WebSocketException: %s)", e.getMessage()));
            }
        }
    }

    private void closeSession() {
        Session localSession = session;
        if (localSession != null && localSession.isOpen()) {
            localSession.close();
        }
    }

    public void runZone(String deviceId, String zone, int time) {
        String dateTime = format.format(new Date());
        try {
            ping();
            Session localSession = session;
            if (localSession != null && localSession.isOpen() && localSession.getRemote() != null) {
                localSession.getRemote()
                        .sendString("{\"event\":\"change_mode\",\"device_id\":\"" + deviceId + "\",\"timestamp\":\""
                                + dateTime + "\",\"mode\":\"manual\",\"stations\":[{\"station\":" + zone
                                + ",\"run_time\":" + time + "}]}");
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error during zone watering execution");
        }
    }

    public void runProgram(String deviceId, String program) {
        String dateTime = format.format(new Date());
        try {
            ping();
            Session localSession = session;
            if (localSession != null && localSession.isOpen() && localSession.getRemote() != null) {
                localSession.getRemote().sendString("{\"event\":\"change_mode\",\"mode\":\"manual\",\"program\":\""
                        + program + "\",\"device_id\":\"" + deviceId + "\",\"timestamp\":\"" + dateTime + "\"}");
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error sending program watering execution (IOException on web socket)");
        } catch (WebSocketException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    String.format("Error sending program watering execution (WebSocketException: %s)", e.getMessage()));
        }
    }

    public void enableProgram(OrbitBhyveProgram program, boolean enable) {
        try {
            String payLoad = "{\"sprinkler_timer_program\":{\"id\":\"" + program.getId() + "\",\"device_id\":\""
                    + program.getDeviceId() + "\",\"program\":\"" + program.getProgram() + "\",\"enabled\":" + enable
                    + "}}";
            logger.debug("updating program {} with data {}", program.getProgram(), payLoad);
            ContentResponse response = sendRequestBuilder(BHYVE_PROGRAMS + "/" + program.getId(), HttpMethod.PUT)
                    .content(new StringContentProvider(payLoad), "application/json; charset=utf-8").send();
            if (response.getStatus() == 200) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Enable programs response: {}", response.getContentAsString());
                }
                return;
            } else {
                logger.debug("Returned status: {}", response.getStatus());
                updateStatus(ThingStatus.OFFLINE);
            }
        } catch (TimeoutException | ExecutionException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Error during updating programs");
        } catch (InterruptedException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Error during updating programs");
            Thread.currentThread().interrupt();
        }
    }

    public void setRainDelay(String deviceId, int delay) {
        String dateTime = format.format(new Date());
        try {
            ping();
            Session localSession = session;
            if (localSession != null && localSession.isOpen() && localSession.getRemote() != null) {
                localSession.getRemote().sendString("{\"event\":\"rain_delay\",\"device_id\":\"" + deviceId
                        + "\",\"delay\":" + delay + ",\"timestamp\":\"" + dateTime + "\"}");
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error setting rain delay (IOException on web socket)");
        } catch (WebSocketException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    String.format("Error setting rain delay (WebSocketException: %s)", e.getMessage()));
        }
    }

    public void stopWatering(String deviceId) {
        String dateTime = format.format(new Date());
        try {
            ping();
            Session localSession = session;
            if (localSession != null && localSession.isOpen() && localSession.getRemote() != null) {
                localSession.getRemote().sendString("{\"event\":\"change_mode\",\"device_id\":\"" + deviceId
                        + "\",\"timestamp\":\"" + dateTime + "\",\"mode\":\"manual\",\"stations\":[]}");
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error sending stop watering (IOException on web socket)");
        } catch (WebSocketException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    String.format("Error sending stop watering (WebSocketException: %s)", e.getMessage()));
        }
    }

    public List<OrbitBhyveProgram> getPrograms() {
        try {
            ContentResponse response = sendRequestBuilder(BHYVE_PROGRAMS, HttpMethod.GET).send();
            if (response.getStatus() == 200) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Programs response: {}", response.getContentAsString());
                }
                OrbitBhyveProgram[] devices = gson.fromJson(response.getContentAsString(), OrbitBhyveProgram[].class);
                return Arrays.asList(devices);
            } else {
                logger.debug("Returned status: {}", response.getStatus());
                updateStatus(ThingStatus.OFFLINE);
            }
        } catch (TimeoutException | ExecutionException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Error during getting programs");
        } catch (InterruptedException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Error during getting programs");
            Thread.currentThread().interrupt();
        }
        return new ArrayList<>();
    }

    public void changeRunMode(String deviceId, String mode) {
        String dateTime = format.format(new Date());
        try {
            ping();
            Session localSession = session;
            if (localSession != null && localSession.isOpen() && localSession.getRemote() != null) {
                localSession.getRemote().sendString("{\"event\":\"change_mode\",\"mode\":\"" + mode
                        + "\",\"device_id\":\"" + deviceId + "\",\"timestamp\":\"" + dateTime + "\"}");
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error setting run mode (IOException on web socket)");
        } catch (WebSocketException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    String.format("Error setting run mode (WebSocketException: %s)", e.getMessage()));
        }
    }

    public void setSmartWatering(String deviceId, boolean enable) {
        OrbitBhyveDevice device = getDevice(deviceId);
        if (device != null && device.getId().equals(deviceId)) {
            device.setWaterSenseMode(enable ? "auto" : "off");
            updateDevice(deviceId, gson.toJson(device));
        }
    }

    private void updateDevice(String deviceId, String deviceString) {
        String payload = "{\"device\":" + deviceString + "}";
        logger.trace("New String: {}", payload);
        try {
            ContentResponse response = sendRequestBuilder(BHYVE_DEVICES + "/" + deviceId, HttpMethod.PUT)
                    .content(new StringContentProvider(payload), "application/json;charset=UTF-8").send();
            if (logger.isTraceEnabled()) {
                logger.trace("Device update response: {}", response.getContentAsString());
            }
            if (response.getStatus() != 200) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Update device response status: " + response.getStatus());
            }
        } catch (TimeoutException | ExecutionException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Error during updating device");
        } catch (InterruptedException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Error during updating device");
            Thread.currentThread().interrupt();
        }
    }
}
