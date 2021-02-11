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
package org.openhab.binding.nanoleaf.internal.handler;

import static org.openhab.binding.nanoleaf.internal.NanoleafBindingConstants.*;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.nanoleaf.internal.NanoleafControllerListener;
import org.openhab.binding.nanoleaf.internal.NanoleafException;
import org.openhab.binding.nanoleaf.internal.NanoleafInterruptedException;
import org.openhab.binding.nanoleaf.internal.NanoleafUnauthorizedException;
import org.openhab.binding.nanoleaf.internal.OpenAPIUtils;
import org.openhab.binding.nanoleaf.internal.config.NanoleafControllerConfig;
import org.openhab.binding.nanoleaf.internal.model.AuthToken;
import org.openhab.binding.nanoleaf.internal.model.BooleanState;
import org.openhab.binding.nanoleaf.internal.model.Brightness;
import org.openhab.binding.nanoleaf.internal.model.ControllerInfo;
import org.openhab.binding.nanoleaf.internal.model.Ct;
import org.openhab.binding.nanoleaf.internal.model.Effects;
import org.openhab.binding.nanoleaf.internal.model.Hue;
import org.openhab.binding.nanoleaf.internal.model.IntegerState;
import org.openhab.binding.nanoleaf.internal.model.Layout;
import org.openhab.binding.nanoleaf.internal.model.On;
import org.openhab.binding.nanoleaf.internal.model.Rhythm;
import org.openhab.binding.nanoleaf.internal.model.Sat;
import org.openhab.binding.nanoleaf.internal.model.State;
import org.openhab.binding.nanoleaf.internal.model.TouchEvents;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link NanoleafControllerHandler} is responsible for handling commands to the controller which
 * affect all panels connected to it (e.g. selected effect)
 *
 * @author Martin Raepple - Initial contribution
 * @author Stefan Höhn - Canvas Touch Support
 */
@NonNullByDefault
public class NanoleafControllerHandler extends BaseBridgeHandler {

    // Pairing interval in seconds
    private static final int PAIRING_INTERVAL = 25;

    // Panel discovery interval in seconds
    private static final int PANEL_DISCOVERY_INTERVAL = 30;

    private final Logger logger = LoggerFactory.getLogger(NanoleafControllerHandler.class);
    private HttpClient httpClient;
    private List<NanoleafControllerListener> controllerListeners = new CopyOnWriteArrayList<>();

    // Pairing, update and panel discovery jobs and touch event job
    private @NonNullByDefault({}) ScheduledFuture<?> pairingJob;
    private @NonNullByDefault({}) ScheduledFuture<?> updateJob;
    private @NonNullByDefault({}) ScheduledFuture<?> panelDiscoveryJob;
    private @NonNullByDefault({}) ScheduledFuture<?> touchJob;

    // JSON parser for API responses
    private final Gson gson = new Gson();

    // Controller configuration settings and channel values
    private @Nullable String address;
    private int port;
    private int refreshIntervall;
    private @Nullable String authToken;
    private @Nullable String deviceType;
    private @NonNullByDefault({}) ControllerInfo controllerInfo;

    public NanoleafControllerHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing the controller (bridge)");
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        NanoleafControllerConfig config = getConfigAs(NanoleafControllerConfig.class);
        setAddress(config.address);
        setPort(config.port);
        setRefreshIntervall(config.refreshInterval);
        setAuthToken(config.authToken);

        Map<String, String> properties = getThing().getProperties();
        String propertyModelId = properties.get(Thing.PROPERTY_MODEL_ID);
        if (hasTouchSupport(propertyModelId)) {
            config.deviceType = DEVICE_TYPE_TOUCHSUPPORT;
        } else {
            config.deviceType = DEVICE_TYPE_LIGHTPANELS;
        }
        setDeviceType(config.deviceType);

        String propertyFirmwareVersion = properties.get(Thing.PROPERTY_FIRMWARE_VERSION);

        try {
            if (config.address.isEmpty() || String.valueOf(config.port).isEmpty()) {
                logger.warn("No IP address and port configured for the Nanoleaf controller");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                        "@text/error.nanoleaf.controller.noIp");
                stopAllJobs();
            } else if (propertyFirmwareVersion != null && !propertyFirmwareVersion.isEmpty() && !OpenAPIUtils
                    .checkRequiredFirmware(properties.get(Thing.PROPERTY_MODEL_ID), propertyFirmwareVersion)) {
                logger.warn("Nanoleaf controller firmware is too old: {}. Must be equal or higher than {}",
                        propertyFirmwareVersion, API_MIN_FW_VER_LIGHTPANELS);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/error.nanoleaf.controller.incompatibleFirmware");
                stopAllJobs();
            } else if (config.authToken == null || config.authToken.isEmpty()) {
                logger.debug("No token found. Start pairing background job");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                        "@text/error.nanoleaf.controller.noToken");
                startPairingJob();
                stopUpdateJob();
                stopPanelDiscoveryJob();
            } else {
                logger.debug("Controller is online. Stop pairing job, start update & panel discovery jobs");
                updateStatus(ThingStatus.ONLINE);
                stopPairingJob();
                startUpdateJob();
                startPanelDiscoveryJob();
                startTouchJob();
            }
        } catch (IllegalArgumentException iae) {
            logger.warn("Nanoleaf controller firmware version not in format x.y.z: {}",
                    getThing().getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION));
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/error.nanoleaf.controller.incompatibleFirmware");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command {} for channel {}", command, channelUID);
        if (!ThingStatus.ONLINE.equals(getThing().getStatusInfo().getStatus())) {
            logger.debug("Cannot handle command. Bridge is not online.");
            return;
        }
        try {
            if (command instanceof RefreshType) {
                updateFromControllerInfo();
            } else {
                switch (channelUID.getId()) {
                    case CHANNEL_POWER:
                    case CHANNEL_COLOR:
                    case CHANNEL_COLOR_TEMPERATURE:
                    case CHANNEL_COLOR_TEMPERATURE_ABS:
                    case CHANNEL_PANEL_LAYOUT:
                        sendStateCommand(channelUID.getId(), command);
                        break;
                    case CHANNEL_EFFECT:
                        sendEffectCommand(command);
                        break;
                    case CHANNEL_RHYTHM_MODE:
                        sendRhythmCommand(command);
                        break;
                    default:
                        logger.warn("Channel with id {} not handled", channelUID.getId());
                        break;
                }
            }
        } catch (NanoleafUnauthorizedException nae) {
            logger.warn("Authorization for command {} to channelUID {} failed: {}", command, channelUID,
                    nae.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/error.nanoleaf.controller.invalidToken");
        } catch (NanoleafException ne) {
            logger.warn("Handling command {} to channelUID {} failed: {}", command, channelUID, ne.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/error.nanoleaf.controller.communication");
        }
    }

    @Override
    public void handleRemoval() {
        // delete token for openHAB
        ContentResponse deleteTokenResponse;
        try {
            Request deleteTokenRequest = OpenAPIUtils.requestBuilder(httpClient, getControllerConfig(), API_DELETE_USER,
                    HttpMethod.DELETE);
            deleteTokenResponse = OpenAPIUtils.sendOpenAPIRequest(deleteTokenRequest);
            if (deleteTokenResponse.getStatus() != HttpStatus.NO_CONTENT_204) {
                logger.warn("Failed to delete token for openHAB. Response code is {}", deleteTokenResponse.getStatus());
                return;
            }
            logger.debug("Successfully deleted token for openHAB from controller");
        } catch (NanoleafUnauthorizedException e) {
            logger.warn("Attempt to delete token for openHAB failed. Token unauthorized.");
        } catch (NanoleafException ne) {
            logger.warn("Attempt to delete token for openHAB failed : {}", ne.getMessage());
        }
        stopAllJobs();
        super.handleRemoval();
        logger.debug("Nanoleaf controller removed");
    }

    @Override
    public void dispose() {
        stopAllJobs();
        super.dispose();
        logger.debug("Disposing handler for Nanoleaf controller {}", getThing().getUID());
    }

    public boolean registerControllerListener(NanoleafControllerListener controllerListener) {
        logger.debug("Register new listener for controller {}", getThing().getUID());
        boolean result = controllerListeners.add(controllerListener);
        if (result) {
            startPanelDiscoveryJob();
        }
        return result;
    }

    public boolean unregisterControllerListener(NanoleafControllerListener controllerListener) {
        logger.debug("Unregister listener for controller {}", getThing().getUID());
        boolean result = controllerListeners.remove(controllerListener);
        if (result) {
            stopPanelDiscoveryJob();
        }
        return result;
    }

    public NanoleafControllerConfig getControllerConfig() {
        NanoleafControllerConfig config = new NanoleafControllerConfig();
        config.address = Objects.requireNonNullElse(getAddress(), "");
        config.port = getPort();
        config.refreshInterval = getRefreshIntervall();
        config.authToken = getAuthToken();
        config.deviceType = Objects.requireNonNullElse(getDeviceType(), "");
        return config;
    }

    public synchronized void startPairingJob() {
        if (pairingJob == null || pairingJob.isCancelled()) {
            logger.debug("Start pairing job, interval={} sec", PAIRING_INTERVAL);
            pairingJob = scheduler.scheduleWithFixedDelay(this::runPairing, 0, PAIRING_INTERVAL, TimeUnit.SECONDS);
        }
    }

    private synchronized void stopPairingJob() {
        if (pairingJob != null && !pairingJob.isCancelled()) {
            logger.debug("Stop pairing job");
            pairingJob.cancel(true);
            this.pairingJob = null;
        }
    }

    private synchronized void startUpdateJob() {
        String localAuthToken = getAuthToken();
        if (localAuthToken != null && !localAuthToken.isEmpty()) {
            if (updateJob == null || updateJob.isCancelled()) {
                logger.debug("Start controller status job, repeat every {} sec", getRefreshIntervall());
                updateJob = scheduler.scheduleWithFixedDelay(this::runUpdate, 0, getRefreshIntervall(),
                        TimeUnit.SECONDS);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                    "@text/error.nanoleaf.controller.noToken");
        }
    }

    private synchronized void stopUpdateJob() {
        if (updateJob != null && !updateJob.isCancelled()) {
            logger.debug("Stop status job");
            updateJob.cancel(true);
            this.updateJob = null;
        }
    }

    public synchronized void startPanelDiscoveryJob() {
        logger.debug("Starting panel discovery job. Has Controller-Listeners: {} panelDiscoveryJob: {}",
                !controllerListeners.isEmpty(), panelDiscoveryJob);
        if (!controllerListeners.isEmpty() && (panelDiscoveryJob == null || panelDiscoveryJob.isCancelled())) {
            logger.debug("Start panel discovery job, interval={} sec", PANEL_DISCOVERY_INTERVAL);
            panelDiscoveryJob = scheduler.scheduleWithFixedDelay(this::runPanelDiscovery, 0, PANEL_DISCOVERY_INTERVAL,
                    TimeUnit.SECONDS);
        }
    }

    private synchronized void stopPanelDiscoveryJob() {
        if (controllerListeners.isEmpty() && panelDiscoveryJob != null && !panelDiscoveryJob.isCancelled()) {
            logger.debug("Stop panel discovery job");
            panelDiscoveryJob.cancel(true);
            this.panelDiscoveryJob = null;
        }
    }

    private synchronized void startTouchJob() {
        NanoleafControllerConfig config = getConfigAs(NanoleafControllerConfig.class);
        if (!config.deviceType.equals(DEVICE_TYPE_TOUCHSUPPORT)) {
            logger.debug("NOT starting TouchJob for Panel {} because it has wrong device type '{}' vs required '{}'",
                    this.getThing().getUID(), config.deviceType, DEVICE_TYPE_TOUCHSUPPORT);
            return;
        } else {
            logger.debug("Starting TouchJob for Panel {}", this.getThing().getUID());
        }

        String localAuthToken = getAuthToken();
        if (localAuthToken != null && !localAuthToken.isEmpty()) {
            if (touchJob == null || touchJob.isCancelled()) {
                logger.debug("Starting Touchjob now");
                touchJob = scheduler.schedule(this::runTouchDetection, 0, TimeUnit.SECONDS);
            }
        } else {
            logger.error("starting TouchJob for Controller {} failed - missing token", this.getThing().getUID());
        }
    }

    private boolean hasTouchSupport(@Nullable String deviceType) {
        return (MODELS_WITH_TOUCHSUPPORT.contains(deviceType));
    }

    private synchronized void stopTouchJob() {
        if (touchJob != null && !touchJob.isCancelled()) {
            logger.debug("Stop touch job");
            touchJob.cancel(true);
            this.touchJob = null;
        }
    }

    private void runUpdate() {
        logger.debug("Run update job");
        try {
            updateFromControllerInfo();
            startTouchJob(); // if device type has changed, start touch detection.
            // controller might have been offline, e.g. for firmware update. In this case, return to online state
            if (ThingStatus.OFFLINE.equals(getThing().getStatus())) {
                logger.debug("Controller {} is back online", thing.getUID());
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (NanoleafUnauthorizedException nae) {
            logger.warn("Status update unauthorized: {}", nae.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/error.nanoleaf.controller.invalidToken");
            String localAuthToken = getAuthToken();
            if (localAuthToken == null || localAuthToken.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                        "@text/error.nanoleaf.controller.noToken");
            }
        } catch (NanoleafException ne) {
            logger.warn("Status update failed: {}", ne.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/error.nanoleaf.controller.communication");
        } catch (RuntimeException e) {
            logger.warn("Update job failed", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/error.nanoleaf.controller.runtime");
        }
    }

    private void runPairing() {
        logger.debug("Run pairing job");
        try {
            String localAuthToken = getAuthToken();
            if (localAuthToken != null && !localAuthToken.isEmpty()) {
                if (pairingJob != null) {
                    pairingJob.cancel(false);
                }
                logger.debug("Authentication token found. Canceling pairing job");
                return;
            }
            ContentResponse authTokenResponse = OpenAPIUtils
                    .requestBuilder(httpClient, getControllerConfig(), API_ADD_USER, HttpMethod.POST).send();
            if (logger.isTraceEnabled()) {
                logger.trace("Auth token response: {}", authTokenResponse.getContentAsString());
            }

            if (authTokenResponse.getStatus() != HttpStatus.OK_200) {
                logger.debug("Pairing pending for {}. Controller returns status code {}", this.getThing().getUID(),
                        authTokenResponse.getStatus());
            } else {
                // get auth token from response
                AuthToken authTokenObject = gson.fromJson(authTokenResponse.getContentAsString(), AuthToken.class);
                localAuthToken = authTokenObject.getAuthToken();
                if (localAuthToken != null && !localAuthToken.isEmpty()) {
                    logger.debug("Pairing succeeded.");

                    // Update and save the auth token in the thing configuration
                    Configuration config = editConfiguration();
                    config.put(NanoleafControllerConfig.AUTH_TOKEN, localAuthToken);
                    updateConfiguration(config);

                    updateStatus(ThingStatus.ONLINE);
                    // Update local field
                    setAuthToken(localAuthToken);

                    stopPairingJob();
                    startUpdateJob();
                    startPanelDiscoveryJob();
                    startTouchJob();
                } else {
                    logger.debug("No auth token found in response: {}", authTokenResponse.getContentAsString());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/error.nanoleaf.controller.pairingFailed");
                    throw new NanoleafException(authTokenResponse.getContentAsString());
                }
            }
        } catch (JsonSyntaxException e) {
            logger.warn("Received invalid data", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/error.nanoleaf.controller.invalidData");
        } catch (NanoleafException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/error.nanoleaf.controller.noTokenReceived");
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.warn("Cannot send authorization request to controller: ", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/error.nanoleaf.controller.authRequest");
        } catch (RuntimeException e) {
            logger.warn("Pairing job failed", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/error.nanoleaf.controller.runtime");
        } catch (Exception e) {
            logger.warn("Cannot start http client", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/error.nanoleaf.controller.noClient");
        }
    }

    private void runPanelDiscovery() {
        logger.debug("Run panel discovery job");
        // Trigger a new discovery of connected panels
        for (NanoleafControllerListener controllerListener : controllerListeners) {
            try {
                controllerListener.onControllerInfoFetched(getThing().getUID(), receiveControllerInfo());
            } catch (NanoleafUnauthorizedException nue) {
                logger.warn("Panel discovery unauthorized: {}", nue.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/error.nanoleaf.controller.invalidToken");
                String localAuthToken = getAuthToken();
                if (localAuthToken == null || localAuthToken.isEmpty()) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                            "@text/error.nanoleaf.controller.noToken");
                }
            } catch (NanoleafInterruptedException nie) {
                logger.info("Panel discovery has been stopped.");
            } catch (NanoleafException ne) {
                logger.warn("Failed to discover panels: ", ne);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/error.nanoleaf.controller.communication");
            } catch (RuntimeException e) {
                logger.warn("Panel discovery job failed", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/error.nanoleaf.controller.runtime");
            }
        }
    }

    /**
     * This is based on the touch event detection described in https://forum.nanoleaf.me/docs/openapi#_842h3097vbgq
     */
    private static boolean touchJobRunning = false;

    private void runTouchDetection() {
        if (touchJobRunning) {
            logger.debug("touch job already running. quitting.");
            return;
        }
        try {
            touchJobRunning = true;
            URI eventUri = OpenAPIUtils.getUri(getControllerConfig(), API_EVENTS, "id=4");
            logger.debug("touch job registered on: {}", eventUri.toString());
            httpClient.newRequest(eventUri).send(new Response.Listener.Adapter() // request runs forever
            {
                @Override
                public void onContent(@Nullable Response response, @Nullable ByteBuffer content) {
                    String s = StandardCharsets.UTF_8.decode(content).toString();
                    logger.trace("content {}", s);

                    Scanner eventContent = new Scanner(s);
                    while (eventContent.hasNextLine()) {
                        String line = eventContent.nextLine().trim();
                        // we don't expect anything than content id:4, so we do not check that but only care about the
                        // data part
                        if (line.startsWith("data:")) {
                            String json = line.substring(5).trim(); // supposed to be JSON
                            try {
                                TouchEvents touchEvents = gson.fromJson(json, TouchEvents.class);
                                handleTouchEvents(Objects.requireNonNull(touchEvents));
                            } catch (JsonSyntaxException jse) {
                                logger.error("couldn't parse touch event json {}", json);
                            }
                        }
                    }
                    eventContent.close();
                    logger.debug("leaving touch onContent");
                    super.onContent(response, content);
                }

                @Override
                public void onSuccess(@Nullable Response response) {
                    logger.trace("touch event SUCCESS: {}", response);
                }

                @Override
                public void onFailure(@Nullable Response response, @Nullable Throwable failure) {
                    logger.trace("touch event FAILURE: {}", response);
                }

                @Override
                public void onComplete(@Nullable Result result) {
                    logger.trace("touch event COMPLETE: {}", result);
                }
            });
        } catch (RuntimeException | NanoleafException e) {
            logger.warn("setting up TouchDetection failed", e);
        } finally {
            touchJobRunning = false;
        }
        logger.debug("leaving run touch detection");
    }

    /**
     * Interate over all gathered touch events and apply them to the panel they belong to
     *
     * @param touchEvents
     */
    private void handleTouchEvents(TouchEvents touchEvents) {
        touchEvents.getEvents().forEach(event -> {
            logger.info("panel: {} gesture id: {}", event.getPanelId(), event.getGesture());

            // Iterate over all child things = all panels of that controller
            this.getThing().getThings().forEach(child -> {
                NanoleafPanelHandler panelHandler = (NanoleafPanelHandler) child.getHandler();
                if (panelHandler != null) {
                    logger.trace("Checking available panel -{}- versus event panel -{}-", panelHandler.getPanelID(),
                            event.getPanelId());
                    if (panelHandler.getPanelID().equals(event.getPanelId())) {
                        logger.debug("Panel {} found. Triggering item with gesture {}.", panelHandler.getPanelID(),
                                event.getGesture());
                        panelHandler.updatePanelGesture(event.getGesture());
                    }
                }
            });
        });
    }

    private void updateFromControllerInfo() throws NanoleafException {
        logger.debug("Update channels for controller {}", thing.getUID());
        this.controllerInfo = receiveControllerInfo();
        if (controllerInfo == null) {
            logger.debug("No Controller Info has been provided");
            return;
        }
        final State state = controllerInfo.getState();

        OnOffType powerState = state.getOnOff();
        updateState(CHANNEL_POWER, powerState);

        @Nullable
        Ct colorTemperature = state.getColorTemperature();

        float colorTempPercent = 0f;
        if (colorTemperature != null) {
            updateState(CHANNEL_COLOR_TEMPERATURE_ABS, new DecimalType(colorTemperature.getValue()));

            @Nullable
            Integer min = colorTemperature.getMin();
            int colorMin = (min == null) ? 0 : min;

            @Nullable
            Integer max = colorTemperature.getMax();
            int colorMax = (max == null) ? 0 : max;

            colorTempPercent = (colorTemperature.getValue() - colorMin) / (colorMax - colorMin)
                    * PercentType.HUNDRED.intValue();
        }

        updateState(CHANNEL_COLOR_TEMPERATURE, new PercentType(Float.toString(colorTempPercent)));
        updateState(CHANNEL_EFFECT, new StringType(controllerInfo.getEffects().getSelect()));

        @Nullable
        Hue stateHue = state.getHue();
        int hue = (stateHue != null) ? stateHue.getValue() : 0;
        @Nullable
        Sat stateSaturation = state.getSaturation();
        int saturation = (stateSaturation != null) ? stateSaturation.getValue() : 0;
        @Nullable
        Brightness stateBrightness = state.getBrightness();
        int brightness = (stateBrightness != null) ? stateBrightness.getValue() : 0;

        updateState(CHANNEL_COLOR, new HSBType(new DecimalType(hue), new PercentType(saturation),
                new PercentType(powerState == OnOffType.ON ? brightness : 0)));
        updateState(CHANNEL_COLOR_MODE, new StringType(state.getColorMode()));
        updateState(CHANNEL_RHYTHM_ACTIVE, controllerInfo.getRhythm().getRhythmActive() ? OnOffType.ON : OnOffType.OFF);
        updateState(CHANNEL_RHYTHM_MODE, new DecimalType(controllerInfo.getRhythm().getRhythmMode()));
        updateState(CHANNEL_RHYTHM_STATE,
                controllerInfo.getRhythm().getRhythmConnected() ? OnOffType.ON : OnOffType.OFF);
        // update bridge properties which may have changed, or are not present during discovery
        Map<String, String> properties = editProperties();
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, controllerInfo.getSerialNo());
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, controllerInfo.getFirmwareVersion());
        properties.put(Thing.PROPERTY_MODEL_ID, controllerInfo.getModel());
        properties.put(Thing.PROPERTY_VENDOR, controllerInfo.getManufacturer());
        updateProperties(properties);

        Configuration config = editConfiguration();

        if (hasTouchSupport(controllerInfo.getModel())) {
            config.put(NanoleafControllerConfig.DEVICE_TYPE, DEVICE_TYPE_TOUCHSUPPORT);
            logger.debug("Set to device type {}", DEVICE_TYPE_TOUCHSUPPORT);
        } else {
            config.put(NanoleafControllerConfig.DEVICE_TYPE, DEVICE_TYPE_LIGHTPANELS);
            logger.debug("Set to device type {}", DEVICE_TYPE_LIGHTPANELS);
        }
        updateConfiguration(config);

        getConfig().getProperties().forEach((key, value) -> {
            logger.trace("Configuration property: key {} value {}", key, value);
        });

        getThing().getProperties().forEach((key, value) -> {
            logger.debug("Thing property:  key {} value {}", key, value);
        });

        // update the color channels of each panel
        this.getThing().getThings().forEach(child -> {
            NanoleafPanelHandler panelHandler = (NanoleafPanelHandler) child.getHandler();
            if (panelHandler != null) {
                logger.debug("Update color channel for panel {}", panelHandler.getThing().getUID());
                panelHandler.updatePanelColorChannel();
            }
        });
    }

    private ControllerInfo receiveControllerInfo() throws NanoleafException, NanoleafUnauthorizedException {
        ContentResponse controllerlInfoJSON = OpenAPIUtils.sendOpenAPIRequest(OpenAPIUtils.requestBuilder(httpClient,
                getControllerConfig(), API_GET_CONTROLLER_INFO, HttpMethod.GET));
        ControllerInfo controllerInfo = gson.fromJson(controllerlInfoJSON.getContentAsString(), ControllerInfo.class);
        return Objects.requireNonNull(controllerInfo);
    }

    private void sendStateCommand(String channel, Command command) throws NanoleafException {
        State stateObject = new State();
        switch (channel) {
            case CHANNEL_POWER:
                if (command instanceof OnOffType) {
                    // On/Off command - turns controller on/off
                    BooleanState state = new On();
                    state.setValue(OnOffType.ON.equals(command));
                    stateObject.setState(state);
                } else {
                    logger.warn("Unhandled command type: {}", command.getClass().getName());
                    return;
                }
                break;
            case CHANNEL_COLOR:
                if (command instanceof OnOffType) {
                    // On/Off command - turns controller on/off
                    BooleanState state = new On();
                    state.setValue(OnOffType.ON.equals(command));
                    stateObject.setState(state);
                } else if (command instanceof HSBType) {
                    // regular color HSB command
                    IntegerState h = new Hue();
                    IntegerState s = new Sat();
                    IntegerState b = new Brightness();
                    h.setValue(((HSBType) command).getHue().intValue());
                    s.setValue(((HSBType) command).getSaturation().intValue());
                    b.setValue(((HSBType) command).getBrightness().intValue());
                    stateObject.setState(h);
                    stateObject.setState(s);
                    stateObject.setState(b);
                } else if (command instanceof PercentType) {
                    // brightness command
                    IntegerState b = new Brightness();
                    b.setValue(((PercentType) command).intValue());
                    stateObject.setState(b);
                } else if (command instanceof IncreaseDecreaseType) {
                    // increase/decrease brightness
                    if (controllerInfo != null) {
                        @Nullable
                        Brightness brightness = controllerInfo.getState().getBrightness();
                        int brightnessMin = 0;
                        int brightnessMax = 0;
                        if (brightness != null) {
                            @Nullable
                            Integer min = brightness.getMin();
                            brightnessMin = (min == null) ? 0 : min;
                            @Nullable
                            Integer max = brightness.getMax();
                            brightnessMax = (max == null) ? 0 : max;

                            if (IncreaseDecreaseType.INCREASE.equals(command)) {
                                brightness.setValue(
                                        Math.min(brightnessMax, brightness.getValue() + BRIGHTNESS_STEP_SIZE));
                            } else {
                                brightness.setValue(
                                        Math.max(brightnessMin, brightness.getValue() - BRIGHTNESS_STEP_SIZE));
                            }
                            stateObject.setState(brightness);
                            logger.debug("Setting controller brightness to {}", brightness.getValue());
                            // update controller info in case new command is sent before next update job interval
                            controllerInfo.getState().setBrightness(brightness);
                        } else {
                            logger.debug("Couldn't set brightness as it was null!");
                        }
                    }
                } else {
                    logger.warn("Unhandled command type: {}", command.getClass().getName());
                    return;
                }
                break;
            case CHANNEL_COLOR_TEMPERATURE:
                if (command instanceof PercentType) {
                    // Color temperature (percent)
                    IntegerState state = new Ct();
                    @Nullable
                    Ct colorTemperature = controllerInfo.getState().getColorTemperature();

                    int colorMin = 0;
                    int colorMax = 0;
                    if (colorTemperature != null) {
                        @Nullable
                        Integer min = colorTemperature.getMin();
                        colorMin = (min == null) ? 0 : min;

                        @Nullable
                        Integer max = colorTemperature.getMax();
                        colorMax = (max == null) ? 0 : max;
                    }

                    state.setValue(Math.round((colorMax - colorMin) * (100 - ((PercentType) command).intValue())
                            / PercentType.HUNDRED.floatValue() + colorMin));
                    stateObject.setState(state);
                } else {
                    logger.warn("Unhandled command type: {}", command.getClass().getName());
                    return;
                }
                break;
            case CHANNEL_COLOR_TEMPERATURE_ABS:
                if (command instanceof DecimalType) {
                    // Color temperature (absolute)
                    IntegerState state = new Ct();
                    state.setValue(((DecimalType) command).intValue());
                    stateObject.setState(state);
                } else {
                    logger.warn("Unhandled command type: {}", command.getClass().getName());
                    return;
                }
                break;
            case CHANNEL_PANEL_LAYOUT:
                @Nullable
                Layout layout = controllerInfo.getPanelLayout().getLayout();
                String layoutView = (layout != null) ? layout.getLayoutView() : "";
                logger.info("Panel layout and ids for controller {} \n{}", thing.getUID(), layoutView);
                updateState(CHANNEL_PANEL_LAYOUT, OnOffType.OFF);
                break;
            default:
                logger.warn("Unhandled command type: {}", command.getClass().getName());
                return;
        }

        Request setNewStateRequest = OpenAPIUtils.requestBuilder(httpClient, getControllerConfig(), API_SET_VALUE,
                HttpMethod.PUT);
        setNewStateRequest.content(new StringContentProvider(gson.toJson(stateObject)), "application/json");
        OpenAPIUtils.sendOpenAPIRequest(setNewStateRequest);
    }

    private void sendEffectCommand(Command command) throws NanoleafException {
        Effects effects = new Effects();
        if (command instanceof StringType) {
            effects.setSelect(command.toString());
        } else {
            logger.warn("Unhandled command type: {}", command.getClass().getName());
            return;
        }
        Request setNewEffectRequest = OpenAPIUtils.requestBuilder(httpClient, getControllerConfig(), API_EFFECT,
                HttpMethod.PUT);
        String content = gson.toJson(effects);
        logger.debug("sending effect command from controller {}: {}", getThing().getUID(), content);
        setNewEffectRequest.content(new StringContentProvider(content), "application/json");
        OpenAPIUtils.sendOpenAPIRequest(setNewEffectRequest);
    }

    private void sendRhythmCommand(Command command) throws NanoleafException {
        Rhythm rhythm = new Rhythm();
        if (command instanceof DecimalType) {
            rhythm.setRhythmMode(((DecimalType) command).intValue());
        } else {
            logger.warn("Unhandled command type: {}", command.getClass().getName());
            return;
        }
        Request setNewRhythmRequest = OpenAPIUtils.requestBuilder(httpClient, getControllerConfig(), API_RHYTHM_MODE,
                HttpMethod.PUT);
        setNewRhythmRequest.content(new StringContentProvider(gson.toJson(rhythm)), "application/json");
        OpenAPIUtils.sendOpenAPIRequest(setNewRhythmRequest);
    }

    private @Nullable String getAddress() {
        return address;
    }

    private void setAddress(String address) {
        this.address = address;
    }

    private int getPort() {
        return port;
    }

    private void setPort(int port) {
        this.port = port;
    }

    private int getRefreshIntervall() {
        return refreshIntervall;
    }

    private void setRefreshIntervall(int refreshIntervall) {
        this.refreshIntervall = refreshIntervall;
    }

    private @Nullable String getAuthToken() {
        return authToken;
    }

    private void setAuthToken(@Nullable String authToken) {
        this.authToken = authToken;
    }

    private @Nullable String getDeviceType() {
        return deviceType;
    }

    private void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    private void stopAllJobs() {
        stopPairingJob();
        stopUpdateJob();
        stopPanelDiscoveryJob();
        stopTouchJob();
    }
}
