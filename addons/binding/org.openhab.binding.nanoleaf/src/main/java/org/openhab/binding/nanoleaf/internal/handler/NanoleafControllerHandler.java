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
package org.openhab.binding.nanoleaf.internal.handler;

import static org.openhab.binding.nanoleaf.internal.NanoleafBindingConstants.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
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
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.nanoleaf.internal.NanoleafBindingConstants;
import org.openhab.binding.nanoleaf.internal.NanoleafControllerListener;
import org.openhab.binding.nanoleaf.internal.NanoleafException;
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
import org.openhab.binding.nanoleaf.internal.model.On;
import org.openhab.binding.nanoleaf.internal.model.Rhythm;
import org.openhab.binding.nanoleaf.internal.model.Sat;
import org.openhab.binding.nanoleaf.internal.model.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link NanoleafControllerHandler} is responsible for handling commands to the controller which
 * affect all panels connected to it (e.g. selected effect)
 *
 * @author Martin Raepple - Initial contribution
 */
@NonNullByDefault
public class NanoleafControllerHandler extends BaseBridgeHandler {

    // Pairing interval in seconds
    private final static int PAIRING_INTERVAL = 25;

    // Panel discovery interval in seconds
    private final static int PANEL_DISCOVERY_INTERVAL = 30;

    private final Logger logger = LoggerFactory.getLogger(NanoleafControllerHandler.class);
    private HttpClient httpClient;
    private List<NanoleafControllerListener> controllerListeners = new CopyOnWriteArrayList<>();

    // Pairing, update and panel discovery jobs
    private @NonNullByDefault({}) ScheduledFuture<?> pairingJob;
    private @NonNullByDefault({}) ScheduledFuture<?> updateJob;
    private @NonNullByDefault({}) ScheduledFuture<?> panelDiscoveryJob;

    // JSON parser for API responses
    private final Gson gson = new Gson();

    // Controller configuration settings and channel values
    private @Nullable String address;
    private int port;
    private int refreshIntervall;
    private @Nullable String authToken;
    private @Nullable ControllerInfo controllerInfo;

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

        try {
            if (StringUtils.isEmpty(getAddress()) || StringUtils.isEmpty(String.valueOf(getPort()))) {
                logger.warn("No IP address and port configured for the Nanoleaf controller");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                        "@text/error.nanoleaf.controller.noIp");
                stopAllJobs();
                return;
            } else if (!StringUtils.isEmpty(getThing().getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION))
                    && !OpenAPIUtils
                            .checkRequiredFirmware(getThing().getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION))) {
                logger.warn("Nanoleaf controller firmware is too old: {}. Must be equal or higher than {}",
                        getThing().getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION),
                        NanoleafBindingConstants.API_MIN_FW_VER);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/error.nanoleaf.controller.incompatibleFirmware");
                stopAllJobs();
                return;
            } else if (StringUtils.isEmpty(getAuthToken())) {
                logger.debug("No token found. Start pairing background job");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                        "@text/error.nanoleaf.controller.noToken");
                startPairingJob();
                stopUpdateJob();
                stopPanelDiscoveryJob();
                return;
            } else {
                logger.debug("Controller is online. Stop pairing job, start update & panel discovery jobs");
                updateStatus(ThingStatus.ONLINE);
                stopPairingJob();
                startUpdateJob();
                startPanelDiscoveryJob();
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
        config.address = this.getAddress();
        config.port = this.getPort();
        config.refreshInterval = this.getRefreshIntervall();
        config.authToken = this.getAuthToken();
        return config;
    }

    public synchronized void startPairingJob() {
        if ((pairingJob == null || pairingJob.isCancelled())) {
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
        if (StringUtils.isNotEmpty(getAuthToken())) {
            if ((updateJob == null || updateJob.isCancelled())) {
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

    private void runUpdate() {
        logger.debug("Run update job");
        try {
            updateFromControllerInfo();
            // controller might have been offline, e.g. for firmware update. In this case, return to online state
            if (ThingStatus.OFFLINE.equals(getThing().getStatus())) {
                logger.debug("Controller {} is back online", thing.getUID());
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (NanoleafUnauthorizedException nae) {
            logger.warn("Status update unauthorized: {}", nae.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/error.nanoleaf.controller.invalidToken");
            if (StringUtils.isEmpty(getAuthToken())) {
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
            if (StringUtils.isNotEmpty(getAuthToken())) {
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
                logger.debug("Pairing pending. Controller returns status code {}", authTokenResponse.getStatus());
                return;
            } else {
                // get auth token from response
                AuthToken authToken = gson.fromJson(authTokenResponse.getContentAsString(), AuthToken.class);

                if (StringUtils.isNotEmpty(authToken.getAuthToken())) {
                    logger.debug("Pairing succeeded.");

                    // Update and save the auth token in the thing configuration
                    Configuration config = editConfiguration();
                    config.put(NanoleafControllerConfig.AUTH_TOKEN, authToken.getAuthToken());
                    updateConfiguration(config);
                    updateStatus(ThingStatus.ONLINE);
                    // Update local field
                    setAuthToken(authToken.getAuthToken());

                    stopPairingJob();
                    startUpdateJob();
                    startPanelDiscoveryJob();
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
                if (StringUtils.isEmpty(getAuthToken())) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                            "@text/error.nanoleaf.controller.noToken");
                }
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

    private void updateFromControllerInfo() throws NanoleafException, NanoleafUnauthorizedException {
        logger.debug("Update channels for controller {}", thing.getUID());
        this.controllerInfo = receiveControllerInfo();
        boolean isOn = controllerInfo.getState().getOn().getValue();
        updateState(CHANNEL_POWER, isOn ? OnOffType.ON : OnOffType.OFF);
        updateState(CHANNEL_COLOR_TEMPERATURE_ABS,
                new DecimalType(controllerInfo.getState().getColorTemperature().getValue().intValue()));
        Float colorTempPercent = (controllerInfo.getState().getColorTemperature().getValue().floatValue()
                - controllerInfo.getState().getColorTemperature().getMin().floatValue())
                / (controllerInfo.getState().getColorTemperature().getMax().floatValue()
                        - controllerInfo.getState().getColorTemperature().getMin().floatValue())
                * PercentType.HUNDRED.intValue();
        updateState(CHANNEL_COLOR_TEMPERATURE, new PercentType(colorTempPercent.intValue()));
        updateState(CHANNEL_EFFECT, new StringType(controllerInfo.getEffects().getSelect()));
        updateState(CHANNEL_COLOR,
                new HSBType(new DecimalType(controllerInfo.getState().getHue().getValue()),
                        new PercentType(controllerInfo.getState().getSaturation().getValue()),
                        new PercentType(isOn ? controllerInfo.getState().getBrightness().getValue() : 0)));
        updateState(CHANNEL_COLOR_MODE, new StringType(controllerInfo.getState().getColorMode()));
        updateState(CHANNEL_RHYTHM_ACTIVE,
                controllerInfo.getRhythm().getRhythmActive().booleanValue() ? OnOffType.ON : OnOffType.OFF);
        updateState(CHANNEL_RHYTHM_MODE, new DecimalType(controllerInfo.getRhythm().getRhythmMode().intValue()));
        updateState(CHANNEL_RHYTHM_STATE,
                controllerInfo.getRhythm().getRhythmConnected().booleanValue() ? OnOffType.ON : OnOffType.OFF);

        // update bridge properties which may have changed, or are not present during discovery
        Map<String, String> properties = editProperties();
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, controllerInfo.getSerialNo());
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, controllerInfo.getFirmwareVersion());
        updateProperties(properties);

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
        return controllerInfo;
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
                        Brightness brightness = controllerInfo.getState().getBrightness();
                        if (command.equals(IncreaseDecreaseType.INCREASE)) {
                            brightness.setValue(Math.min(brightness.getMax().intValue(),
                                    brightness.getValue() + BRIGHTNESS_STEP_SIZE));
                        } else {
                            brightness.setValue(Math.max(brightness.getMin().intValue(),
                                    brightness.getValue() - BRIGHTNESS_STEP_SIZE));
                        }
                        stateObject.setState(brightness);
                        logger.debug("Setting controller brightness to {}", brightness.getValue());
                        // update controller info in case new command is sent before next update job interval
                        controllerInfo.getState().setBrightness(brightness);
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
                    state.setValue(Math.round((controllerInfo.getState().getColorTemperature().getMax()
                            - controllerInfo.getState().getColorTemperature().getMin())
                            * ((PercentType) command).intValue() / PercentType.HUNDRED.floatValue()
                            + controllerInfo.getState().getColorTemperature().getMin()));
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
        } else {
            logger.warn("Unhandled command type: {}", command.getClass().getName());
            return;
        }
        Request setNewEffectRequest = OpenAPIUtils.requestBuilder(httpClient, getControllerConfig(), API_EFFECT,
                HttpMethod.PUT);
        setNewEffectRequest.content(new StringContentProvider(gson.toJson(effects)), "application/json");
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

    private String getAddress() {
        return StringUtils.defaultString(this.address);
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

    private String getAuthToken() {
        return StringUtils.defaultString(authToken);
    }

    private void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    private void stopAllJobs() {
        stopPairingJob();
        stopUpdateJob();
        stopPanelDiscoveryJob();
    }
}
