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
package org.openhab.binding.nanoleaf.internal.handler;

import static org.openhab.binding.nanoleaf.internal.NanoleafBindingConstants.*;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
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
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.nanoleaf.internal.NanoleafBindingConstants;
import org.openhab.binding.nanoleaf.internal.NanoleafControllerListener;
import org.openhab.binding.nanoleaf.internal.NanoleafException;
import org.openhab.binding.nanoleaf.internal.NanoleafUnauthorizedException;
import org.openhab.binding.nanoleaf.internal.OpenAPIUtils;
import org.openhab.binding.nanoleaf.internal.commanddescription.NanoleafCommandDescriptionProvider;
import org.openhab.binding.nanoleaf.internal.config.NanoleafControllerConfig;
import org.openhab.binding.nanoleaf.internal.discovery.NanoleafPanelsDiscoveryService;
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
import org.openhab.binding.nanoleaf.internal.model.PanelLayout;
import org.openhab.binding.nanoleaf.internal.model.Rhythm;
import org.openhab.binding.nanoleaf.internal.model.Sat;
import org.openhab.binding.nanoleaf.internal.model.State;
import org.openhab.binding.nanoleaf.internal.model.TouchEvents;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
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
import org.openhab.core.thing.binding.ThingHandlerService;
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
 * @author Kai Kreuzer - refactoring, bug fixing and code clean up
 */
@NonNullByDefault
public class NanoleafControllerHandler extends BaseBridgeHandler {

    // Pairing interval in seconds
    private static final int PAIRING_INTERVAL = 10;
    private static final int CONNECT_TIMEOUT = 10;

    private final Logger logger = LoggerFactory.getLogger(NanoleafControllerHandler.class);
    private HttpClientFactory httpClientFactory;
    private HttpClient httpClient;

    private @Nullable HttpClient httpClientSSETouchEvent;
    private @Nullable Request sseTouchjobRequest;
    private List<NanoleafControllerListener> controllerListeners = new CopyOnWriteArrayList<NanoleafControllerListener>();

    private @NonNullByDefault({}) ScheduledFuture<?> pairingJob;
    private @NonNullByDefault({}) ScheduledFuture<?> updateJob;
    private @NonNullByDefault({}) ScheduledFuture<?> touchJob;
    private final Gson gson = new Gson();

    private @Nullable String address;
    private int port;
    private int refreshIntervall;
    private @Nullable String authToken;
    private @Nullable String deviceType;
    private @NonNullByDefault({}) ControllerInfo controllerInfo;

    private boolean touchJobRunning = false;

    public NanoleafControllerHandler(Bridge bridge, HttpClientFactory httpClientFactory) {
        super(bridge);
        this.httpClientFactory = httpClientFactory;
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    private void initializeTouchHttpClient() {
        String httpClientName = thing.getUID().getId();

        try {
            httpClientSSETouchEvent = httpClientFactory.createHttpClient(httpClientName);
            final HttpClient localHttpClientSSETouchEvent = this.httpClientSSETouchEvent;
            if (localHttpClientSSETouchEvent != null) {
                localHttpClientSSETouchEvent.setConnectTimeout(CONNECT_TIMEOUT * 1000L);
                localHttpClientSSETouchEvent.start();
            }
        } catch (Exception e) {
            logger.error(
                    "Long running HttpClient for Nanoleaf controller handler {} cannot be started. Creating Handler failed.",
                    httpClientName);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }

        logger.debug("Using long SSE httpClient={} for {}}", httpClientSSETouchEvent, httpClientName);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing the controller (bridge)");
        updateStatus(ThingStatus.UNKNOWN);
        NanoleafControllerConfig config = getConfigAs(NanoleafControllerConfig.class);
        setAddress(config.address);
        setPort(config.port);
        setRefreshIntervall(config.refreshInterval);
        String authToken = (config.authToken != null) ? config.authToken : "";
        setAuthToken(authToken);
        Map<String, String> properties = getThing().getProperties();
        String propertyModelId = properties.get(Thing.PROPERTY_MODEL_ID);
        if (hasTouchSupport(propertyModelId)) {
            config.deviceType = DEVICE_TYPE_TOUCHSUPPORT;
            initializeTouchHttpClient();
        } else {
            config.deviceType = DEVICE_TYPE_LIGHTPANELS;
        }

        setDeviceType(config.deviceType);
        String propertyFirmwareVersion = properties.get(Thing.PROPERTY_FIRMWARE_VERSION);

        try {
            if (!config.address.isEmpty() && !String.valueOf(config.port).isEmpty()) {
                if (propertyFirmwareVersion != null && !propertyFirmwareVersion.isEmpty() && !OpenAPIUtils
                        .checkRequiredFirmware(properties.get(Thing.PROPERTY_MODEL_ID), propertyFirmwareVersion)) {
                    logger.warn("Nanoleaf controller firmware is too old: {}. Must be equal or higher than {}",
                            propertyFirmwareVersion, API_MIN_FW_VER_LIGHTPANELS);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "@text/error.nanoleaf.controller.incompatibleFirmware");
                    stopAllJobs();
                } else if (authToken != null && !authToken.isEmpty()) {
                    stopPairingJob();
                    startUpdateJob();
                    startTouchJob();
                } else {
                    logger.debug("No token found. Start pairing background job");
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                            "@text/error.nanoleaf.controller.noToken");
                    startPairingJob();
                    stopUpdateJob();
                }
            } else {
                logger.warn("No IP address and port configured for the Nanoleaf controller");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                        "@text/error.nanoleaf.controller.noIp");
                stopAllJobs();
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
        } else {
            try {
                if (command instanceof RefreshType) {
                    updateFromControllerInfo();
                } else {
                    switch (channelUID.getId()) {
                        case CHANNEL_COLOR:
                        case CHANNEL_COLOR_TEMPERATURE:
                        case CHANNEL_COLOR_TEMPERATURE_ABS:
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
            } catch (NanoleafUnauthorizedException nue) {
                logger.debug("Authorization for command {} to channelUID {} failed: {}", command, channelUID,
                        nue.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/error.nanoleaf.controller.invalidToken");
            } catch (NanoleafException ne) {
                logger.debug("Handling command {} to channelUID {} failed: {}", command, channelUID, ne.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/error.nanoleaf.controller.communication");
            }
        }
    }

    @Override
    public void handleRemoval() {
        scheduler.execute(() -> {
            try {
                Request deleteTokenRequest = OpenAPIUtils.requestBuilder(httpClient, getControllerConfig(),
                        API_DELETE_USER, HttpMethod.DELETE);
                ContentResponse deleteTokenResponse = OpenAPIUtils.sendOpenAPIRequest(deleteTokenRequest);
                if (deleteTokenResponse.getStatus() != HttpStatus.NO_CONTENT_204) {
                    logger.warn("Failed to delete token for openHAB. Response code is {}",
                            deleteTokenResponse.getStatus());
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
        });
    }

    @Override
    public void dispose() {
        stopAllJobs();
        super.dispose();
        logger.debug("Disposing handler for Nanoleaf controller {}", getThing().getUID());
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(NanoleafPanelsDiscoveryService.class, NanoleafCommandDescriptionProvider.class);
    }

    public boolean registerControllerListener(NanoleafControllerListener controllerListener) {
        logger.debug("Register new listener for controller {}", getThing().getUID());
        return controllerListeners.add(controllerListener);
    }

    public boolean unregisterControllerListener(NanoleafControllerListener controllerListener) {
        logger.debug("Unregister listener for controller {}", getThing().getUID());
        return controllerListeners.remove(controllerListener);
    }

    public NanoleafControllerConfig getControllerConfig() {
        NanoleafControllerConfig config = new NanoleafControllerConfig();
        config.address = Objects.requireNonNullElse(getAddress(), "");
        config.port = getPort();
        config.refreshInterval = getRefreshInterval();
        config.authToken = getAuthToken();
        config.deviceType = Objects.requireNonNullElse(getDeviceType(), "");
        return config;
    }

    public String getLayout() {
        String layoutView = "";
        if (controllerInfo != null) {
            PanelLayout panelLayout = controllerInfo.getPanelLayout();
            Layout layout = panelLayout.getLayout();
            layoutView = layout != null ? layout.getLayoutView() : "";
        }

        return layoutView;
    }

    public synchronized void startPairingJob() {
        if (pairingJob == null || pairingJob.isCancelled()) {
            logger.debug("Start pairing job, interval={} sec", PAIRING_INTERVAL);
            pairingJob = scheduler.scheduleWithFixedDelay(this::runPairing, 0L, PAIRING_INTERVAL, TimeUnit.SECONDS);
        }
    }

    private synchronized void stopPairingJob() {
        logger.debug("Stop pairing job {}", pairingJob != null ? pairingJob.isCancelled() : "pairing job = null");
        if (pairingJob != null && !pairingJob.isCancelled()) {
            pairingJob.cancel(true);
            pairingJob = null;
            logger.debug("Stopped pairing job");
        }
    }

    private synchronized void startUpdateJob() {
        final String localAuthToken = getAuthToken();
        if (localAuthToken != null && !localAuthToken.isEmpty()) {
            if (updateJob == null || updateJob.isCancelled()) {
                logger.debug("Start controller status job, repeat every {} sec", getRefreshInterval());
                updateJob = scheduler.scheduleWithFixedDelay(this::runUpdate, 0L, getRefreshInterval(),
                        TimeUnit.SECONDS);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                    "@text/error.nanoleaf.controller.noToken");
        }
    }

    private synchronized void stopUpdateJob() {
        logger.debug("Stop update job {}", updateJob != null ? updateJob.isCancelled() : "update job = null");
        if (updateJob != null && !updateJob.isCancelled()) {
            updateJob.cancel(true);
            updateJob = null;
            logger.debug("Stopped status job");
        }
    }

    private synchronized void startTouchJob() {
        NanoleafControllerConfig config = getConfigAs(NanoleafControllerConfig.class);
        if (!config.deviceType.equals(DEVICE_TYPE_TOUCHSUPPORT)) {
            logger.debug(
                    "NOT starting TouchJob for Controller {} because it has wrong device type '{}' vs required '{}'",
                    this.getThing().getUID(), config.deviceType, DEVICE_TYPE_TOUCHSUPPORT);
        } else {
            logger.debug("Starting TouchJob for Controller {}", getThing().getUID());
            final String localAuthToken = getAuthToken();
            if (localAuthToken != null && !localAuthToken.isEmpty()) {
                if (touchJob != null && !touchJob.isDone()) {
                    logger.trace("tj: tj={} already running touchJobRunning = {}  cancelled={} done={}", touchJob,
                            touchJobRunning, touchJob == null ? null : touchJob.isCancelled(),
                            touchJob == null ? null : touchJob.isDone());
                } else {
                    logger.debug("tj: Starting NEW touch job : tj={} touchJobRunning={} cancelled={}  done={}",
                            touchJob, touchJobRunning, touchJob == null ? null : touchJob.isCancelled(),
                            touchJob == null ? null : touchJob.isDone());
                    touchJob = scheduler.scheduleWithFixedDelay(this::runTouchDetection, 0L, 1L, TimeUnit.SECONDS);
                }
            } else {
                logger.error("starting TouchJob for Controller {} failed - missing token", getThing().getUID());
            }

        }
    }

    private synchronized void stopTouchJob() {
        logger.debug("Stop touch job {}", touchJob != null ? touchJob.isCancelled() : "touchJob job = null");
        if (touchJob != null) {
            logger.trace("tj: touch job stopping for {} with client {}", thing.getUID(), httpClientSSETouchEvent);

            final Request localSSERequest = sseTouchjobRequest;
            if (localSSERequest != null) {
                localSSERequest.abort(new NanoleafException("Touch detection stopped"));
            }
            if (!touchJob.isCancelled()) {
                touchJob.cancel(true);
            }

            touchJob = null;
            touchJobRunning = false;
            logger.debug("tj: touch job stopped for {} with client {}", thing.getUID(), httpClientSSETouchEvent);
        }
    }

    private boolean hasTouchSupport(@Nullable String deviceType) {
        return NanoleafBindingConstants.MODELS_WITH_TOUCHSUPPORT.contains(deviceType);
    }

    private void runUpdate() {
        logger.debug("Run update job");

        try {
            updateFromControllerInfo();
            startTouchJob();
            updateStatus(ThingStatus.ONLINE);
        } catch (NanoleafUnauthorizedException nae) {
            logger.debug("Status update unauthorized for controller {}: {}", getThing().getUID(), nae.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/error.nanoleaf.controller.invalidToken");
            final String localAuthToken = getAuthToken();
            if (localAuthToken == null || localAuthToken.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                        "@text/error.nanoleaf.controller.noToken");
            }
        } catch (NanoleafException ne) {
            logger.debug("Status update failed for controller {} : {}", getThing().getUID(), ne.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/error.nanoleaf.controller.communication");
        } catch (RuntimeException e) {
            logger.debug("Update job failed", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/error.nanoleaf.controller.runtime");
        }
    }

    private void runPairing() {
        logger.debug("Run pairing job");

        try {
            final String localAuthToken = getAuthToken();
            if (localAuthToken != null && !localAuthToken.isEmpty()) {
                if (pairingJob != null) {
                    pairingJob.cancel(false);
                }

                logger.debug("Authentication token found. Canceling pairing job");
                return;
            }

            ContentResponse authTokenResponse = OpenAPIUtils
                    .requestBuilder(httpClient, getControllerConfig(), API_ADD_USER, HttpMethod.POST)
                    .timeout(20L, TimeUnit.SECONDS).send();
            String authTokenResponseString = (authTokenResponse != null) ? authTokenResponse.getContentAsString() : "";
            if (logger.isTraceEnabled()) {
                logger.trace("Auth token response: {}", authTokenResponseString);
            }

            if (authTokenResponse != null && authTokenResponse.getStatus() != HttpStatus.OK_200) {
                logger.debug("Pairing pending for {}. Controller returns status code {}", getThing().getUID(),
                        authTokenResponse.getStatus());
            } else {
                AuthToken authTokenObject = gson.fromJson(authTokenResponseString, AuthToken.class);
                authTokenObject = (authTokenObject != null) ? authTokenObject : new AuthToken();
                if (authTokenObject.getAuthToken().isEmpty()) {
                    logger.debug("No auth token found in response: {}", authTokenResponseString);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/error.nanoleaf.controller.pairingFailed");
                    throw new NanoleafException(authTokenResponseString);
                }

                logger.debug("Pairing succeeded.");
                Configuration config = editConfiguration();

                config.put(NanoleafControllerConfig.AUTH_TOKEN, authTokenObject.getAuthToken());
                updateConfiguration(config);
                updateStatus(ThingStatus.ONLINE);
                // Update local field
                setAuthToken(authTokenObject.getAuthToken());

                stopPairingJob();
                startUpdateJob();
                startTouchJob();
            }
        } catch (JsonSyntaxException e) {
            logger.warn("Received invalid data", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/error.nanoleaf.controller.invalidData");
        } catch (NanoleafException ne) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/error.nanoleaf.controller.noTokenReceived");
        } catch (ExecutionException | TimeoutException | InterruptedException e) {
            logger.debug("Cannot send authorization request to controller: ", e);
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

    private synchronized void runTouchDetection() {
        final HttpClient localhttpSSEClientTouchEvent = httpClientSSETouchEvent;
        int eventHashcode = -1;
        if (localhttpSSEClientTouchEvent != null) {
            eventHashcode = localhttpSSEClientTouchEvent.hashCode();
        }
        if (touchJobRunning) {
            logger.trace("tj: touch job {} touch job already running. quitting. {} controller {} with {}\",\n",
                    touchJob, eventHashcode, thing.getUID(), httpClientSSETouchEvent);
        } else {
            try {
                URI eventUri = OpenAPIUtils.getUri(getControllerConfig(), API_EVENTS, "id=4");
                logger.debug("tj: touch job request registering for {} with client {}", thing.getUID(),
                        httpClientSSETouchEvent);
                touchJobRunning = true;
                if (localhttpSSEClientTouchEvent != null) {
                    localhttpSSEClientTouchEvent.setIdleTimeout(CONNECT_TIMEOUT * 1000L);
                    sseTouchjobRequest = localhttpSSEClientTouchEvent.newRequest(eventUri);
                    final Request localSSETouchjobRequest = sseTouchjobRequest;
                    int requestHashCode = -1;
                    if (localSSETouchjobRequest != null) {
                        requestHashCode = localSSETouchjobRequest.hashCode();

                        logger.debug("tj: triggering new touch job request {} for {} with client {}", requestHashCode,
                                thing.getUID(), eventHashcode);
                        localSSETouchjobRequest.onResponseContent((response, content) -> {
                            String s = StandardCharsets.UTF_8.decode(content).toString();
                            logger.debug("touch detected for controller {}", thing.getUID());
                            logger.trace("content {}", s);
                            Scanner eventContent = new Scanner(s);

                            while (eventContent.hasNextLine()) {
                                String line = eventContent.nextLine().trim();
                                if (line.startsWith("data:")) {
                                    String json = line.substring(5).trim();

                                    try {
                                        TouchEvents touchEvents = gson.fromJson(json, TouchEvents.class);
                                        handleTouchEvents(Objects.requireNonNull(touchEvents));
                                    } catch (JsonSyntaxException e) {
                                        logger.error("Couldn't parse touch event json {}", json);
                                    }
                                }
                            }

                            eventContent.close();
                            logger.debug("leaving touch onContent");
                        }).onResponseSuccess((response) -> {
                            logger.trace("tj: r={} touch event SUCCESS: {}", response.getRequest(), response);
                        }).onResponseFailure((response, failure) -> {
                            logger.trace("tj: r={} touch event FAILURE. Touchjob not running anymore for controller {}",
                                    response.getRequest(), thing.getUID());
                        }).send((result) -> {
                            logger.trace(
                                    "tj: r={} touch event COMPLETE. Touchjob not running anymore for controller {}      failed: {}        succeeded: {}",
                                    result.getRequest(), thing.getUID(), result.isFailed(), result.isSucceeded());
                            touchJobRunning = false;
                        });
                    }
                }
                logger.trace("tj: started touch job request for {} with {} at {}", thing.getUID(),
                        httpClientSSETouchEvent, eventUri);
            } catch (NanoleafException | RuntimeException e) {
                logger.warn("tj: setting up TouchDetection failed for controller {} with {}\",\n", thing.getUID(),
                        httpClientSSETouchEvent);
                logger.warn("tj: setting up TouchDetection failed with exception", e);
            } finally {
                logger.trace("tj: touch job {} started for new request {} controller {} with {}\",\n",
                        touchJob.hashCode(), eventHashcode, thing.getUID(), httpClientSSETouchEvent);
            }

        }
    }

    private void handleTouchEvents(TouchEvents touchEvents) {
        touchEvents.getEvents().forEach((event) -> {
            logger.debug("panel: {} gesture id: {}", event.getPanelId(), event.getGesture());
            // Swipes go to the controller, taps go to the individual panel
            if (event.getPanelId().equals(CONTROLLER_PANEL_ID)) {
                logger.debug("Triggering controller {} with gesture {}.", thing.getUID(), event.getGesture());
                updateControllerGesture(event.getGesture());
            } else {
                getThing().getThings().forEach((child) -> {
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
            }
        });
    }

    /**
     * Apply the swipe gesture to the controller
     *
     * @param gesture Only swipes are supported on the complete nanoleaf panels
     */
    private void updateControllerGesture(int gesture) {
        switch (gesture) {
            case 2:
                triggerChannel(CHANNEL_SWIPE, CHANNEL_SWIPE_EVENT_UP);
                break;
            case 3:
                triggerChannel(CHANNEL_SWIPE, CHANNEL_SWIPE_EVENT_DOWN);
                break;
            case 4:
                triggerChannel(CHANNEL_SWIPE, CHANNEL_SWIPE_EVENT_LEFT);
                break;
            case 5:
                triggerChannel(CHANNEL_SWIPE, CHANNEL_SWIPE_EVENT_RIGHT);
                break;
        }
    }

    private void updateFromControllerInfo() throws NanoleafException {
        logger.debug("Update channels for controller {}", thing.getUID());
        controllerInfo = receiveControllerInfo();
        State state = controllerInfo.getState();

        OnOffType powerState = state.getOnOff();

        Ct colorTemperature = state.getColorTemperature();

        float colorTempPercent = 0.0F;
        int hue;
        int saturation;
        if (colorTemperature != null) {
            updateState(CHANNEL_COLOR_TEMPERATURE_ABS, new DecimalType(colorTemperature.getValue()));
            Integer min = colorTemperature.getMin();
            hue = min == null ? 0 : min;
            Integer max = colorTemperature.getMax();
            saturation = max == null ? 0 : max;
            colorTempPercent = (colorTemperature.getValue() - hue) / (saturation - hue)
                    * PercentType.HUNDRED.intValue();
        }

        updateState(CHANNEL_COLOR_TEMPERATURE, new PercentType(Float.toString(colorTempPercent)));
        updateState(CHANNEL_EFFECT, new StringType(controllerInfo.getEffects().getSelect()));
        Hue stateHue = state.getHue();
        hue = stateHue != null ? stateHue.getValue() : 0;

        Sat stateSaturation = state.getSaturation();
        saturation = stateSaturation != null ? stateSaturation.getValue() : 0;

        Brightness stateBrightness = state.getBrightness();
        int brightness = stateBrightness != null ? stateBrightness.getValue() : 0;

        updateState(CHANNEL_COLOR, new HSBType(new DecimalType(hue), new PercentType(saturation),
                new PercentType(powerState == OnOffType.ON ? brightness : 0)));
        updateState(CHANNEL_COLOR_MODE, new StringType(state.getColorMode()));
        updateState(CHANNEL_RHYTHM_ACTIVE, controllerInfo.getRhythm().getRhythmActive() ? OnOffType.ON : OnOffType.OFF);
        updateState(CHANNEL_RHYTHM_MODE, new DecimalType(controllerInfo.getRhythm().getRhythmMode()));
        updateState(CHANNEL_RHYTHM_STATE,
                controllerInfo.getRhythm().getRhythmConnected() ? OnOffType.ON : OnOffType.OFF);

        // update the color channels of each panel
        getThing().getThings().forEach(child -> {
            NanoleafPanelHandler panelHandler = (NanoleafPanelHandler) child.getHandler();
            if (panelHandler != null) {
                logger.debug("Update color channel for panel {}", panelHandler.getThing().getUID());
                panelHandler.updatePanelColorChannel();
            }
        });

        updateProperties();
        updateConfiguration();

        for (NanoleafControllerListener controllerListener : controllerListeners) {
            controllerListener.onControllerInfoFetched(getThing().getUID(), controllerInfo);
        }
    }

    private void updateConfiguration() {
        // only update the Thing config if value isn't set yet
        if (getConfig().get(NanoleafControllerConfig.DEVICE_TYPE) == null) {
            Configuration config = editConfiguration();
            if (hasTouchSupport(controllerInfo.getModel())) {
                config.put(NanoleafControllerConfig.DEVICE_TYPE, DEVICE_TYPE_TOUCHSUPPORT);
                logger.debug("Set to device type {}", DEVICE_TYPE_TOUCHSUPPORT);
            } else {
                config.put(NanoleafControllerConfig.DEVICE_TYPE, DEVICE_TYPE_LIGHTPANELS);
                logger.debug("Set to device type {}", DEVICE_TYPE_LIGHTPANELS);
            }
            updateConfiguration(config);
            if (logger.isTraceEnabled()) {
                getConfig().getProperties().forEach((key, value) -> {
                    logger.trace("Configuration property: key {} value {}", key, value);
                });
            }
        }
    }

    private void updateProperties() {
        // update bridge properties which may have changed, or are not present during discovery
        Map<String, String> properties = editProperties();
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, controllerInfo.getSerialNo());
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, controllerInfo.getFirmwareVersion());
        properties.put(Thing.PROPERTY_MODEL_ID, controllerInfo.getModel());
        properties.put(Thing.PROPERTY_VENDOR, controllerInfo.getManufacturer());
        updateProperties(properties);
        if (logger.isTraceEnabled()) {
            getThing().getProperties().forEach((key, value) -> {
                logger.trace("Thing property: key {} value {}", key, value);
            });
        }
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
                        int brightnessMin;
                        int brightnessMax;
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
                    logger.warn("Unhandled command {} with command type: {}", command, command.getClass().getName());
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
            Request setNewEffectRequest = OpenAPIUtils.requestBuilder(httpClient, getControllerConfig(), API_EFFECT,
                    HttpMethod.PUT);
            String content = gson.toJson(effects);
            logger.debug("sending effect command from controller {}: {}", getThing().getUID(), content);
            setNewEffectRequest.content(new StringContentProvider(content), "application/json");
            OpenAPIUtils.sendOpenAPIRequest(setNewEffectRequest);
        } else {
            logger.warn("Unhandled command type: {}", command.getClass().getName());
        }
    }

    private void sendRhythmCommand(Command command) throws NanoleafException {
        Rhythm rhythm = new Rhythm();
        if (command instanceof DecimalType) {
            rhythm.setRhythmMode(((DecimalType) command).intValue());
            Request setNewRhythmRequest = OpenAPIUtils.requestBuilder(httpClient, getControllerConfig(),
                    API_RHYTHM_MODE, HttpMethod.PUT);
            setNewRhythmRequest.content(new StringContentProvider(gson.toJson(rhythm)), "application/json");
            OpenAPIUtils.sendOpenAPIRequest(setNewRhythmRequest);
        } else {
            logger.warn("Unhandled command type: {}", command.getClass().getName());
        }
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

    private int getRefreshInterval() {
        return refreshIntervall;
    }

    private void setRefreshIntervall(int refreshIntervall) {
        this.refreshIntervall = refreshIntervall;
    }

    @Nullable
    private String getAuthToken() {
        return authToken;
    }

    private void setAuthToken(@Nullable String authToken) {
        this.authToken = authToken;
    }

    @Nullable
    private String getDeviceType() {
        return deviceType;
    }

    private void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    private void stopAllJobs() {
        stopPairingJob();
        stopUpdateJob();
        stopTouchJob();
    }
}
