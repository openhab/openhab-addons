/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.hyperion.internal.handler;

import static org.openhab.binding.hyperion.internal.HyperionBindingConstants.*;

import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.hyperion.internal.HyperionStateDescriptionProvider;
import org.openhab.binding.hyperion.internal.connection.JsonTcpConnection;
import org.openhab.binding.hyperion.internal.protocol.ColorCommand;
import org.openhab.binding.hyperion.internal.protocol.CommandUnsuccessfulException;
import org.openhab.binding.hyperion.internal.protocol.EffectCommand;
import org.openhab.binding.hyperion.internal.protocol.HyperionCommand;
import org.openhab.binding.hyperion.internal.protocol.ServerInfoCommand;
import org.openhab.binding.hyperion.internal.protocol.ng.Adjustment;
import org.openhab.binding.hyperion.internal.protocol.ng.AdjustmentCommand;
import org.openhab.binding.hyperion.internal.protocol.ng.Component;
import org.openhab.binding.hyperion.internal.protocol.ng.ComponentState;
import org.openhab.binding.hyperion.internal.protocol.ng.ComponentStateCommand;
import org.openhab.binding.hyperion.internal.protocol.ng.Hyperion;
import org.openhab.binding.hyperion.internal.protocol.ng.NgInfo;
import org.openhab.binding.hyperion.internal.protocol.ng.NgResponse;
import org.openhab.binding.hyperion.internal.protocol.ng.Priority;
import org.openhab.binding.hyperion.internal.protocol.ng.Value;
import org.openhab.binding.hyperion.internal.protocol.v1.ClearAllCommand;
import org.openhab.binding.hyperion.internal.protocol.v1.ClearCommand;
import org.openhab.binding.hyperion.internal.protocol.v1.Effect;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/**
 * The {@link HyperionNgHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Daniel Walters - Initial contribution
 */
public class HyperionNgHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(HyperionNgHandler.class);

    private static final String COLOR_PRIORITY = "COLOR";
    private static final String EFFECT_PRIORITY = "EFFECT";
    private static final String DEFAULT_ADJUSTMENT = "default";
    private static final String COMPONENTS_ALL = "ALL";

    private JsonTcpConnection connection;
    private ScheduledFuture<?> refreshFuture;
    private ScheduledFuture<?> connectFuture;
    private Gson gson = new Gson();

    private static final ServerInfoCommand SERVER_INFO_COMMAND = new ServerInfoCommand();

    private String address;
    private int port;
    private int refreshInterval;
    private int priority;
    private String origin;
    private HyperionStateDescriptionProvider stateDescriptionProvider;

    private Runnable refreshJob = new Runnable() {
        @Override
        public void run() {
            try {
                NgResponse response = sendCommand(SERVER_INFO_COMMAND);
                if (response.isSuccess()) {
                    handleServerInfoResponse(response);
                }
                updateOnlineStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
            } catch (IOException e) {
                updateOnlineStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            } catch (JsonParseException e) {
                logger.debug("{}", e.getMessage(), e);
            } catch (CommandUnsuccessfulException e) {
                logger.debug("Server rejected the command: {}", e.getMessage());
            }
        }
    };

    private Runnable connectionJob = new Runnable() {
        @Override
        public void run() {
            try {
                if (!connection.isConnected()) {
                    connection.connect();
                    updateOnlineStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
                }
            } catch (IOException e) {
                updateOnlineStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    };

    public HyperionNgHandler(Thing thing, HyperionStateDescriptionProvider stateDescriptionProvider) {
        super(thing);
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Hyperion.ng thing handler.");
        try {
            Configuration config = thing.getConfiguration();
            address = (String) config.get(PROP_HOST);
            port = ((BigDecimal) config.get(PROP_PORT)).intValue();
            refreshInterval = ((BigDecimal) config.get(PROP_POLL_FREQUENCY)).intValue();
            priority = ((BigDecimal) config.get(PROP_PRIORITY)).intValue();
            origin = (String) config.get(PROP_ORIGIN);

            connection = new JsonTcpConnection(address, port);
            connectFuture = scheduler.scheduleWithFixedDelay(connectionJob, 0, refreshInterval, TimeUnit.SECONDS);
            refreshFuture = scheduler.scheduleWithFixedDelay(refreshJob, 0, refreshInterval, TimeUnit.SECONDS);
        } catch (UnknownHostException e) {
            logger.debug("Could not resolve host: {}", e.getMessage());
            updateOnlineStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing of Hyperion.ng thing handler.");
        if (refreshFuture != null) {
            refreshFuture.cancel(true);
        }
        if (connectFuture != null) {
            connectFuture.cancel(true);
        }
        if (connection != null && connection.isConnected()) {
            try {
                connection.close();
            } catch (IOException e) {
                // do nothing
            }
        }
    }

    protected void handleServerInfoResponse(NgResponse response) {
        NgInfo info = response.getInfo();
        if (info != null) {
            // update Hyperion, older API compatibility
            Hyperion hyperion = info.getHyperion();
            if (hyperion != null) {
                updateHyperion(hyperion);
            }

            // populate the effect states
            List<Effect> effects = info.getEffects();
            populateEffects(effects);

            // update adjustments
            List<Adjustment> adjustments = info.getAdjustment();
            updateAdjustments(adjustments);

            // update components
            List<Component> components = info.getComponents();
            updateComponents(components);

            // update colors/effects
            List<Priority> priorities = info.getPriorities();
            updatePriorities(priorities);
        }
    }

    private void populateEffects(List<Effect> effects) {
        List<StateOption> options = new ArrayList<>();
        for (Effect effect : effects) {
            options.add(new StateOption(effect.getName(), effect.getName()));
        }
        stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_EFFECT), options);
    }

    private void updatePriorities(List<Priority> priorities) {
        populateClearPriorities(priorities);

        String regex = origin + ".*";

        // update color
        // find the color priority that has the same origin specified in the Thing configuration
        Optional<Priority> colorPriority = priorities.stream() // convert list to stream
                .filter(priority -> COLOR_PRIORITY.equals(priority.getComponentId())
                        && priority.getOrigin().matches(regex))
                .findFirst();

        // if there is no color priority for the openHAB origin then set channel to NULL
        if (colorPriority.isPresent()) {
            Value value = colorPriority.get().getValue();
            List<Integer> rgbVals = value.getRGB();
            int r = rgbVals.get(0);
            int g = rgbVals.get(1);
            int b = rgbVals.get(2);
            HSBType hsbType = HSBType.fromRGB(r, g, b);
            updateState(CHANNEL_COLOR, hsbType);
        } else {
            updateState(CHANNEL_COLOR, UnDefType.NULL);
        }

        // update effect
        // find the color priority that has the same origin specified in the Thing configuration
        Optional<Priority> effectPriority = priorities.stream() // convert list to stream
                .filter(priority -> EFFECT_PRIORITY.equals(priority.getComponentId())
                        && priority.getOrigin().matches(regex))
                .findFirst();

        // if there is no effect priority for the openHAB origin then set channel to NULL
        if (effectPriority.isPresent()) {
            String effectString = effectPriority.get().getOwner();
            StringType effect = new StringType(effectString);
            updateState(CHANNEL_EFFECT, effect);
        } else {
            updateState(CHANNEL_EFFECT, UnDefType.NULL);
        }
    }

    private void populateClearPriorities(List<Priority> priorities) {
        List<StateOption> options = new ArrayList<>();
        options.add(new StateOption("ALL", "ALL"));
        priorities.stream()
                .filter(priority -> priority.getPriority() >= 1 && priority.getPriority() <= 253 && priority.isActive())
                .forEach(priority -> {
                    options.add(new StateOption(priority.getPriority().toString(), priority.getPriority().toString()));
                });
        stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_CLEAR), options);
    }

    private void updateHyperion(Hyperion hyperion) {
        boolean isOff = hyperion.isOff();
        OnOffType hyperionState = isOff ? OnOffType.OFF : OnOffType.ON;
        updateState(CHANNEL_HYPERION_ENABLED, hyperionState);
    }

    private void updateComponents(List<Component> components) {
        for (Component component : components) {
            String componentName = component.getName();
            boolean componentIsEnabled = component.isEnabled();
            OnOffType componentState = componentIsEnabled ? OnOffType.ON : OnOffType.OFF;
            switch (componentName) {
                case COMPONENT_BLACKBORDER:
                    updateState(CHANNEL_BLACKBORDER, componentState);
                    break;
                case COMPONENT_SMOOTHING:
                    updateState(CHANNEL_SMOOTHING, componentState);
                    break;
                case COMPONENT_KODICHECKER:
                    updateState(CHANNEL_KODICHECKER, componentState);
                    break;
                case COMPONENT_FORWARDER:
                    updateState(CHANNEL_FORWARDER, componentState);
                    break;
                case COMPONENT_UDPLISTENER:
                    updateState(CHANNEL_UDPLISTENER, componentState);
                    break;
                case COMPONENT_BOBLIGHTSERVER:
                    updateState(CHANNEL_BOBLIGHTSERVER, componentState);
                    break;
                case COMPONENT_GRABBER:
                    updateState(CHANNEL_GRABBER, componentState);
                    break;
                case COMPONENT_V4L:
                    updateState(CHANNEL_V4L, componentState);
                    break;
                case COMPONENT_LEDDEVICE:
                    updateState(CHANNEL_LEDDEVICE, componentState);
                    break;
                case COMPONENT_ALL:
                    updateState(CHANNEL_HYPERION_ENABLED, componentState);
                    break;
                default:
                    logger.debug("Unknown component: {}", componentName);
            }
        }
    }

    private void updateAdjustments(List<Adjustment> adjustments) {
        Optional<Adjustment> defaultAdjustment = adjustments.stream() // convert list to stream
                .filter(adjustment -> DEFAULT_ADJUSTMENT.equals(adjustment.getId())).findFirst();

        if (defaultAdjustment.isPresent()) {
            int brightness = defaultAdjustment.get().getBrightness();
            PercentType brightnessState = new PercentType(brightness);
            updateState(CHANNEL_BRIGHTNESS, brightnessState);
        } else {
            updateState(CHANNEL_BRIGHTNESS, UnDefType.NULL);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (command instanceof RefreshType) {
                if (refreshFuture.isDone()) {
                    refreshFuture = scheduler.scheduleWithFixedDelay(refreshJob, 0, refreshInterval, TimeUnit.SECONDS);
                } else {
                    logger.debug("Previous refresh not yet completed");
                }
            } else if (CHANNEL_BRIGHTNESS.equals(channelUID.getId())) {
                handleBrightness(command);
            } else if (CHANNEL_COLOR.equals(channelUID.getId())) {
                handleColor(command);
            } else if (CHANNEL_HYPERION_ENABLED.equals(channelUID.getId())) {
                handleHyperionEnabled(command);
            } else if (CHANNEL_EFFECT.equals(channelUID.getId())) {
                handleEffect(command);
            } else if (CHANNEL_CLEAR.equals(channelUID.getId())) {
                handleClear(command);
            } else if (CHANNEL_BLACKBORDER.equals(channelUID.getId())) {
                handleComponentEnabled(channelUID.getId(), command);
            } else if (CHANNEL_SMOOTHING.equals(channelUID.getId())) {
                handleComponentEnabled(channelUID.getId(), command);
            } else if (CHANNEL_KODICHECKER.equals(channelUID.getId())) {
                handleComponentEnabled(channelUID.getId(), command);
            } else if (CHANNEL_FORWARDER.equals(channelUID.getId())) {
                handleComponentEnabled(channelUID.getId(), command);
            } else if (CHANNEL_UDPLISTENER.equals(channelUID.getId())) {
                handleComponentEnabled(channelUID.getId(), command);
            } else if (CHANNEL_GRABBER.equals(channelUID.getId())) {
                handleComponentEnabled(channelUID.getId(), command);
            } else if (CHANNEL_BOBLIGHTSERVER.equals(channelUID.getId())) {
                handleComponentEnabled(channelUID.getId(), command);
            } else if (CHANNEL_V4L.equals(channelUID.getId())) {
                handleComponentEnabled(channelUID.getId(), command);
            } else if (CHANNEL_LEDDEVICE.equals(channelUID.getId())) {
                handleComponentEnabled(channelUID.getId(), command);
            }
        } catch (IOException e) {
            updateOnlineStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (CommandUnsuccessfulException e) {
            logger.debug("Server rejected the command: {}", e.getMessage());
        }
    }

    private void handleComponentEnabled(String channel, Command command)
            throws IOException, CommandUnsuccessfulException {
        if (command instanceof OnOffType) {
            ComponentState componentState = new ComponentState();
            switch (channel) {
                case CHANNEL_BLACKBORDER:
                    componentState.setComponent(COMPONENT_BLACKBORDER);
                    break;
                case CHANNEL_SMOOTHING:
                    componentState.setComponent(COMPONENT_SMOOTHING);
                    break;
                case CHANNEL_KODICHECKER:
                    componentState.setComponent(COMPONENT_KODICHECKER);
                    break;
                case CHANNEL_FORWARDER:
                    componentState.setComponent(COMPONENT_FORWARDER);
                    break;
                case CHANNEL_UDPLISTENER:
                    componentState.setComponent(COMPONENT_UDPLISTENER);
                    break;
                case CHANNEL_BOBLIGHTSERVER:
                    componentState.setComponent(COMPONENT_BOBLIGHTSERVER);
                    break;
                case CHANNEL_GRABBER:
                    componentState.setComponent(COMPONENT_GRABBER);
                    break;
                case CHANNEL_V4L:
                    componentState.setComponent(COMPONENT_V4L);
                    break;
                case CHANNEL_LEDDEVICE:
                    componentState.setComponent(COMPONENT_LEDDEVICE);
                    break;
            }

            boolean state = command == OnOffType.ON ? true : false;
            componentState.setState(state);
            ComponentStateCommand stateCommand = new ComponentStateCommand(componentState);
            sendCommand(stateCommand);
        } else {
            logger.debug("Channel {} unable to process command {}", channel, command);
        }
    }

    private void handleHyperionEnabled(Command command) throws IOException, CommandUnsuccessfulException {
        if (command instanceof OnOffType) {
            ComponentState componentState = new ComponentState();
            componentState.setComponent(COMPONENTS_ALL);
            boolean state = command == OnOffType.ON ? true : false;
            componentState.setState(state);
            ComponentStateCommand stateCommand = new ComponentStateCommand(componentState);
            sendCommand(stateCommand);
        } else {
            logger.debug("Channel {} unable to process command {}", CHANNEL_HYPERION_ENABLED, command);
        }
    }

    private void handleBrightness(Command command) throws IOException, CommandUnsuccessfulException {
        if (command instanceof PercentType percentCommand) {
            int brightnessValue = percentCommand.intValue();

            Adjustment adjustment = new Adjustment();
            adjustment.setBrightness(brightnessValue);

            AdjustmentCommand brightnessCommand = new AdjustmentCommand(adjustment);
            sendCommand(brightnessCommand);
        } else {
            logger.debug("Channel {} unable to process command {}", CHANNEL_BRIGHTNESS, command);
        }
    }

    private void handleColor(Command command) throws IOException, CommandUnsuccessfulException {
        if (command instanceof HSBType hsbCommand) {
            Color c = new Color(hsbCommand.getRGB());
            int r = c.getRed();
            int g = c.getGreen();
            int b = c.getBlue();

            ColorCommand colorCommand = new ColorCommand(r, g, b, priority);
            colorCommand.setOrigin(origin);
            sendCommand(colorCommand);
        } else {
            logger.debug("Channel {} unable to process command {}", CHANNEL_COLOR, command);
        }
    }

    private void handleEffect(Command command) throws IOException, CommandUnsuccessfulException {
        if (command instanceof StringType) {
            String effectName = command.toString();

            Effect effect = new Effect(effectName);
            EffectCommand effectCommand = new EffectCommand(effect, priority);

            effectCommand.setOrigin(origin);

            sendCommand(effectCommand);
        } else {
            logger.debug("Channel {} unable to process command {}", CHANNEL_EFFECT, command);
        }
    }

    private void handleClear(Command command) throws IOException, CommandUnsuccessfulException {
        if (command instanceof StringType) {
            String cmd = command.toString();
            if ("ALL".equals(cmd)) {
                ClearAllCommand clearCommand = new ClearAllCommand();
                sendCommand(clearCommand);
            } else {
                int priority = Integer.parseInt(cmd);
                ClearCommand clearCommand = new ClearCommand(priority);
                sendCommand(clearCommand);
            }
        }
    }

    private void updateOnlineStatus(ThingStatus status, ThingStatusDetail detail, String message) {
        ThingStatusInfo currentStatusInfo = thing.getStatusInfo();
        ThingStatus currentStatus = currentStatusInfo.getStatus();
        ThingStatusDetail currentDetail = currentStatusInfo.getStatusDetail();
        if (!currentStatus.equals(status) || !currentDetail.equals(detail)) {
            updateStatus(status, detail, message);
        }
    }

    public NgResponse sendCommand(HyperionCommand command) throws IOException, CommandUnsuccessfulException {
        String commandJson = gson.toJson(command);
        String jsonResponse = connection.send(commandJson);
        NgResponse response = gson.fromJson(jsonResponse, NgResponse.class);
        if (!response.isSuccess()) {
            throw new CommandUnsuccessfulException(gson.toJson(command) + " - Reason: " + response.getError());
        }
        return response;
    }
}
