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

import java.util.ArrayList;
import java.util.HashMap;
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
import org.openhab.binding.nanoleaf.internal.model.Palette;
import org.openhab.binding.nanoleaf.internal.model.Rhythm;
import org.openhab.binding.nanoleaf.internal.model.Sat;
import org.openhab.binding.nanoleaf.internal.model.State;
import org.openhab.binding.nanoleaf.internal.model.Write;
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

    private final Logger logger = LoggerFactory.getLogger(NanoleafControllerHandler.class);
    private HttpClient httpClient;
    private List<NanoleafControllerListener> controllerListeners = new CopyOnWriteArrayList<>();

    // Pairing interval
    private final static int PAIRING_INTERVAL = 25;

    // Pairing and channel update jobs
    private @Nullable ScheduledFuture<?> pairingJob;
    private @Nullable ScheduledFuture<?> updateJob;

    // JSON parser for API responses
    private final Gson gson = new Gson();

    public NanoleafControllerHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing the controller (bridge)");
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        NanoleafControllerConfig config = getConfigAs(NanoleafControllerConfig.class);

        if (StringUtils.isEmpty(config.address) || StringUtils.isEmpty(String.valueOf(config.port))) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    "IP address and/or port are not configured for the light panels.");
            logger.warn("No IP address and port configured for the Nanoleaf controller");
        } else if (StringUtils.isEmpty(config.authToken)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "No authorization token found.");
            startPairingJob();
        } else {
            updateStatus(ThingStatus.ONLINE);
            // controller is now online - stop pairing
            stopPairingJob();
            // ... and start update job
            startUpdateJob();
            // Update the discovery configuration
            Map<String, Object> configDiscovery = new HashMap<String, Object>();
            configDiscovery.put(NanoleafControllerConfig.DISCOVER_PANELS, config.discoverPanels);

            // Trigger a new discovery of connected panels
            for (NanoleafControllerListener controllerListener : controllerListeners) {
                controllerListener.applyConfig(configDiscovery);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command {} for channel {}", command, channelUID);
        try {
            if (command instanceof RefreshType) {
                updateFromControllerInfo();
            } else {
                switch (channelUID.getId()) {
                    case CHANNEL_POWER:
                        sendStateCommand(On.class.getName(), command);
                        break;
                    case CHANNEL_BRIGHTNESS:
                        if (command instanceof OnOffType) {
                            // On/Off sent to the dimmer item - turns controller on/off
                            sendStateCommand(On.class.getName(), command);
                        } else {
                            sendStateCommand(Brightness.class.getName(), command);
                        }
                        break;
                    case CHANNEL_HUE:
                        sendStateCommand(Hue.class.getName(), command);
                        break;
                    case CHANNEL_COLOR:
                        sendEffectCommand(command);
                        break;
                    case CHANNEL_COLOR_TEMPERATURE:
                        sendStateCommand(Ct.class.getName(), command);
                        break;
                    case CHANNEL_EFFECT:
                        sendEffectCommand(command);
                        break;
                    case CHANNEL_SATURATION:
                        if (command instanceof OnOffType) {
                            // On/Off sent to the dimmer item - turns controller on/off
                            sendStateCommand(On.class.getName(), command);
                        } else {
                            sendStateCommand(Sat.class.getName(), command);
                        }
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
                    "Invalid token. Replace with valid token or start pairing again.");
        } catch (NanoleafException ne) {
            logger.warn("Handling command {} to channelUID {} failed: {}", command, channelUID, ne.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Communication failed. Please check configuration");
        }
    }

    @Override
    public void handleRemoval() {
        super.handleRemoval();
        stopPairingJob();
        stopUpdateJob();
        logger.debug("Nanoleaf controller removed");
    }

    public boolean registerControllerListener(NanoleafControllerListener controllerListener) {
        return controllerListeners.add(controllerListener);
    }

    public boolean unregisterControllerListener(NanoleafControllerListener controllerListener) {
        return controllerListeners.remove(controllerListener);
    }

    public synchronized void startPairingJob() {
        if ((pairingJob == null || pairingJob.isCancelled())) {
            logger.debug("Start pairing job, interval={} sec", PAIRING_INTERVAL);
            pairingJob = scheduler.scheduleWithFixedDelay(pairingRunnable, 0, PAIRING_INTERVAL, TimeUnit.SECONDS);
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
        NanoleafControllerConfig controllerConfig = getConfigAs(NanoleafControllerConfig.class);
        if (StringUtils.isNotEmpty(controllerConfig.authToken)) {
            if ((updateJob == null || updateJob.isCancelled())) {
                logger.debug("Start controller status job, interval={} sec", controllerConfig.refreshInterval);
                updateJob = scheduler.scheduleWithFixedDelay(updateRunnable, 0, controllerConfig.refreshInterval,
                        TimeUnit.SECONDS);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                    "No authorization token found. To start pairing, press the on-off button of the controller for 5-7 seconds until the LED starts flashing in a pattern.");
        }
    }

    private synchronized void stopUpdateJob() {
        if (updateJob != null && !updateJob.isCancelled()) {
            logger.debug("Stop status job");
            updateJob.cancel(true);
            this.updateJob = null;
        }
    }

    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                updateFromControllerInfo();
            } catch (NanoleafUnauthorizedException nae) {
                logger.warn("Status update unauthorized: {}", nae.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Invalid token. Replace with valid token or start pairing again.");
                NanoleafControllerConfig controllerConfig = getConfigAs(NanoleafControllerConfig.class);
                if (StringUtils.isEmpty(controllerConfig.authToken)) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                            "Invalid token. Replace with valid token or start pairing again by pressing the on-off button of the controller for 5-7 seconds until the LED starts flashing in a pattern.");
                }
            } catch (NanoleafException ne) {
                logger.warn("Status update failed: {}", ne.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Communication failed. Please check configuration");
            }
        }
    };

    private final Runnable pairingRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                NanoleafControllerConfig panelsConfig = getConfigAs(NanoleafControllerConfig.class);
                if (StringUtils.isNotEmpty(panelsConfig.authToken)) {
                    if (pairingJob != null) {
                        pairingJob.cancel(false);
                    }
                    logger.debug("Authentication token found. Canceling pairing job");
                    return;
                }
                if (httpClient.isStarted()) {
                    httpClient.stop();
                }
                httpClient.start();
                ContentResponse authTokenResponse = OpenAPIUtils
                        .requestBuilder(httpClient, getConfig(), API_ADD_USER, HttpMethod.POST).send();
                logger.trace("Auth token response {}", authTokenResponse.getContentAsString());

                if (authTokenResponse.getStatus() != HttpStatus.OK_200) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Pairing failed. Press the on-off button for 5-7 seconds until the LED starts flashing in a pattern.");
                    logger.debug("Pairing failed with OpenAPI status code {}", authTokenResponse.getStatus());
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

                        startUpdateJob();
                        stopPairingJob();
                    } else {
                        logger.debug("No auth token found in response: {}", authTokenResponse.getContentAsString());
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "Pairing failed. Retry by holding the on-off button down for 5-7 seconds until the LED starts flashing in a pattern.");
                        throw new NanoleafException(authTokenResponse.getContentAsString());
                    }
                }
            } catch (JsonSyntaxException e) {
                logger.warn("Received invalid data", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Pairing failed: Received invalid data");
            } catch (NanoleafException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Pairing failed. No authorization token in response.");
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                logger.warn("Cannot send authorization request to controller: ", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, String
                        .format("Pairing failed. Cannot send authorization request: %s", e.getCause().getMessage()));
            } catch (Exception e) {
                logger.warn("Cannot start http client", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Pairing failed. Cannot start HTTP client");
            }
        }
    };

    private void updateFromControllerInfo() throws NanoleafException, NanoleafUnauthorizedException {
        ControllerInfo controllerInfo = getControllerInfo();
        // update channels
        updateState(CHANNEL_POWER, controllerInfo.getState().getOn().getValue() ? OnOffType.ON : OnOffType.OFF);
        updateState(CHANNEL_BRIGHTNESS,
                new PercentType(controllerInfo.getState().getBrightness().getValue().intValue()));
        updateState(CHANNEL_COLOR_TEMPERATURE,
                new DecimalType(controllerInfo.getState().getCt().getValue().intValue()));
        updateState(CHANNEL_HUE, new DecimalType(controllerInfo.getState().getHue().getValue().intValue()));
        updateState(CHANNEL_SATURATION, new PercentType(controllerInfo.getState().getSat().getValue().intValue()));
        updateState(CHANNEL_EFFECT, new StringType(controllerInfo.getEffects().getSelect()));
        updateState(CHANNEL_COLOR_MODE, new StringType(controllerInfo.getState().getColorMode()));
        updateState(CHANNEL_RHYTHM_ACTIVE,
                controllerInfo.getRhythm().getRhythmActive().booleanValue() ? OnOffType.ON : OnOffType.OFF);
        updateState(CHANNEL_RHYTHM_MODE, new DecimalType(controllerInfo.getRhythm().getRhythmMode().intValue()));
        updateState(CHANNEL_RHYTHM_STATE,
                controllerInfo.getRhythm().getRhythmConnected().booleanValue() ? OnOffType.ON : OnOffType.OFF);
        logger.debug("Update channels for controller {}", thing.getUID());

        // update properties
        Map<String, String> properties = editProperties();
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, controllerInfo.getSerialNo());
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, controllerInfo.getFirmwareVersion());
        properties.put(Thing.PROPERTY_VENDOR, controllerInfo.getManufacturer());
        properties.put(Thing.PROPERTY_MODEL_ID, controllerInfo.getModel());
        updateProperties(properties);

        // Trigger a new discovery of connected panels
        for (NanoleafControllerListener controllerListener : controllerListeners) {
            controllerListener.onControllerInfoFetched(getThing().getUID(), controllerInfo);
        }
    }

    private ControllerInfo getControllerInfo() throws NanoleafException, JsonSyntaxException {
        ContentResponse controllerInfoJSON = OpenAPIUtils.sendOpenAPIRequest(
                OpenAPIUtils.requestBuilder(httpClient, this.getConfig(), API_GET_CONTROLLER_INFO, HttpMethod.GET));
        ControllerInfo controllerInfo = gson.fromJson(controllerInfoJSON.getContentAsString(), ControllerInfo.class);
        return controllerInfo;
    }

    private void sendStateCommand(String stateClass, Command command) throws NanoleafException {
        try {
            State stateObject = new State();
            if (command instanceof DecimalType) {
                IntegerState state = (IntegerState) Class.forName(stateClass).newInstance();
                state.setValue(((DecimalType) command).intValue());
                stateObject.setState(state);
            } else if (command instanceof OnOffType) {
                BooleanState state = (BooleanState) Class.forName(stateClass).newInstance();
                state.setValue(OnOffType.ON.equals(command));
                stateObject.setState(state);
            } else {
                logger.warn("Unhandled command type: {}", command.getClass().getName());
                return;
            }
            Request setNewStateRequest = OpenAPIUtils.requestBuilder(httpClient, this.getConfig(), API_SET_VALUE,
                    HttpMethod.PUT);
            setNewStateRequest.content(new StringContentProvider(gson.toJson(stateObject)), "application/json");
            OpenAPIUtils.sendOpenAPIRequest(setNewStateRequest);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new NanoleafException(String.format("Send state command failed: %s", e.getMessage()));
        }
    }

    private void sendEffectCommand(Command command) throws NanoleafException {
        Effects effects = new Effects();
        if (command instanceof HSBType) {
            Write write = new Write();
            write.setCommand("display");
            write.setAnimType("solid");
            Palette palette = new Palette();
            palette.setBrightness(((HSBType) command).getBrightness().intValue());
            palette.setHue(((HSBType) command).getHue().intValue());
            palette.setSaturation(((HSBType) command).getSaturation().intValue());
            List<Palette> palettes = new ArrayList<Palette>();
            palettes.add(palette);
            write.setPalette(palettes);
            write.setColorType("HSB");
            effects.setWrite(write);
        } else if (command instanceof StringType) {
            effects.setSelect(command.toString());
        } else if (command instanceof OnOffType) {
            sendStateCommand(On.class.getName(), command);
        } else if (command instanceof PercentType) {
            // brightness to color channel
            sendStateCommand(Brightness.class.getName(), command);
        } else {
            logger.warn("Unhandled command type: {}", command.getClass().getName());
            return;
        }
        Request setNewEffectRequest = OpenAPIUtils.requestBuilder(httpClient, this.getConfig(), API_SELECT_EFFECT,
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
        Request setNewRhythmRequest = OpenAPIUtils.requestBuilder(httpClient, this.getConfig(), API_RHYTHM_MODE,
                HttpMethod.PUT);
        setNewRhythmRequest.content(new StringContentProvider(gson.toJson(rhythm)), "application/json");
        OpenAPIUtils.sendOpenAPIRequest(setNewRhythmRequest);
    }
}
