/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hyperion.handler;

import static org.openhab.binding.hyperion.HyperionBindingConstants.*;

import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.hyperion.internal.connection.JsonTcpConnection;
import org.openhab.binding.hyperion.internal.protocol.ColorCommand;
import org.openhab.binding.hyperion.internal.protocol.CommandUnsuccessfulException;
import org.openhab.binding.hyperion.internal.protocol.EffectCommand;
import org.openhab.binding.hyperion.internal.protocol.HyperionCommand;
import org.openhab.binding.hyperion.internal.protocol.ServerInfoCommand;
import org.openhab.binding.hyperion.internal.protocol.v1.ActiveEffect;
import org.openhab.binding.hyperion.internal.protocol.v1.ActiveLedColor;
import org.openhab.binding.hyperion.internal.protocol.v1.ClearAllCommand;
import org.openhab.binding.hyperion.internal.protocol.v1.ClearCommand;
import org.openhab.binding.hyperion.internal.protocol.v1.Effect;
import org.openhab.binding.hyperion.internal.protocol.v1.Transform;
import org.openhab.binding.hyperion.internal.protocol.v1.TransformCommand;
import org.openhab.binding.hyperion.internal.protocol.v1.V1Info;
import org.openhab.binding.hyperion.internal.protocol.v1.V1Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/**
 * The {@link HyperionHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Daniel Walters - Initial contribution
 */
public class HyperionHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(HyperionHandler.class);

    private JsonTcpConnection connection;
    private ScheduledFuture<?> refreshFuture;
    private ScheduledFuture<?> connectFuture;
    private Gson gson = new Gson();

    private static final ServerInfoCommand SERVER_INFO_COMMAND = new ServerInfoCommand();

    private String address;
    private int port;
    private int refreshInterval;
    private int priority;

    private Runnable refreshJob = new Runnable() {
        @Override
        public void run() {
            try {
                V1Response response = sendCommand(SERVER_INFO_COMMAND);
                if (response.isSuccess()) {
                    handleServerInfoResponse(response);
                }
            } catch (IOException e) {
                logger.warn("Could not connect to server.");
                updateOnlineStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            } catch (JsonParseException e) {
                logger.error("{}", e.getMessage(), e);
            } catch (CommandUnsuccessfulException e) {
                logger.warn("Server rejected the command: {}", e.getMessage());
            }
        }
    };

    private Runnable connectionJob = new Runnable() {
        @Override
        public void run() {
            try {
                if (!connection.isConnected()) {
                    connection.connect();
                    updateOnlineStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
                }
            } catch (IOException e) {
                logger.error("Could not connect to server.");
                updateOnlineStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        }
    };

    public HyperionHandler(Thing thing) {
        super(thing);
    }

    protected void handleServerInfoResponse(V1Response response) {
        V1Info info = response.getInfo();
        if (info != null) {
            // update Color
            List<ActiveLedColor> activeColors = info.getActiveLedColor();
            updateColor(activeColors);

            // update effect
            List<ActiveEffect> activeEffects = info.getActiveEffects();
            List<Effect> effects = info.getEffects();
            updateEffect(activeEffects, effects);

            // update transform
            List<Transform> transform = info.getTransform();
            updateTransform(transform);
        }
    }

    private void updateTransform(List<Transform> transform) {
        Transform defaultTransform = transform.stream() // convert list to stream
                .findFirst().orElse(null);

        if (defaultTransform == null) {
            updateState(CHANNEL_BRIGHTNESS, UnDefType.NULL);
        } else {
            double luminanceGain = defaultTransform.getLuminanceGain();
            if (luminanceGain >= 0.0 && luminanceGain <= 1.0) {
                PercentType luminanceGainPercentType = new PercentType((int) (luminanceGain * 100));
                updateState(CHANNEL_BRIGHTNESS, luminanceGainPercentType);
            }
        }
    }

    private void updateEffect(List<ActiveEffect> activeEffects, List<Effect> effects) {
        ActiveEffect effect = activeEffects.stream() // convert list to stream
                .findFirst().orElse(null);
        if (effect == null) {
            updateState(CHANNEL_EFFECT, UnDefType.NULL);
        } else {
            String path = effect.getScript();
            Effect effectDescription = effects.stream() // convert list to stream
                    .filter(effectCandidate -> effectCandidate.getScript().equals(path)).findFirst().orElse(null);
            String effectName = effectDescription.getName();
            updateState(CHANNEL_EFFECT, new StringType(effectName));
        }
    }

    private void updateColor(List<ActiveLedColor> activeColors) {
        ActiveLedColor color = activeColors.stream() // convert list to stream
                .findFirst().orElse(null);
        if (color == null) {
            updateState(CHANNEL_COLOR, UnDefType.NULL);
        } else {
            List<Integer> rgbVals = color.getRGBValue();
            int r = rgbVals.get(0);
            int g = rgbVals.get(1);
            int b = rgbVals.get(2);
            HSBType hsbType = HSBType.fromRGB(r, g, b);
            updateState(CHANNEL_COLOR, hsbType);
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Hyperion thing handler.");
        try {
            Configuration config = thing.getConfiguration();
            address = (String) config.get(PROP_HOST);
            port = ((BigDecimal) config.get(PROP_PORT)).intValue();
            refreshInterval = ((BigDecimal) config.get(PROP_POLL_FREQUENCY)).intValue();
            priority = ((BigDecimal) config.get(PROP_PRIORITY)).intValue();

            connection = new JsonTcpConnection(address, port);
            connectFuture = scheduler.scheduleWithFixedDelay(connectionJob, 0, refreshInterval, TimeUnit.SECONDS);
            refreshFuture = scheduler.scheduleWithFixedDelay(refreshJob, 0, refreshInterval, TimeUnit.SECONDS);
        } catch (UnknownHostException e) {
            logger.error("Could not resolve host: {}", e.getMessage());
            updateOnlineStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing of Hyperion thing handler.");
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
            } else if (CHANNEL_CLEAR_ALL.equals(channelUID.getId())) {
                handleClearAll(command);
            } else if (CHANNEL_CLEAR.equals(channelUID.getId())) {
                handleClear(command);
            } else if (CHANNEL_EFFECT.equals(channelUID.getId())) {
                handleEffect(command);
            }
        } catch (IOException e) {
            logger.error("Unable to send command: {}", command.toString());
        } catch (CommandUnsuccessfulException e) {
            logger.warn("Server rejected the command: {}", e.getMessage());
        }
    }

    private void handleEffect(Command command) throws IOException, CommandUnsuccessfulException {
        if (command instanceof StringType) {
            String effectName = command.toString();

            Effect effect = new Effect(effectName);
            EffectCommand effectCommand = new EffectCommand(effect, priority);
            sendCommand(effectCommand);
        } else {
            logger.warn("Channel {} unable to process command {}", CHANNEL_EFFECT, command.toString());
        }
    }

    private void handleBrightness(Command command) throws IOException, CommandUnsuccessfulException {
        if (command instanceof PercentType) {
            PercentType percent = (PercentType) command;
            Transform transform = new Transform();
            transform.setLuminanceGain(percent.doubleValue() / 100);
            TransformCommand transformCommand = new TransformCommand(transform);
            sendCommand(transformCommand);
        } else if (command instanceof IncreaseDecreaseType) {
            logger.warn("Channel {} unable to process command {}", CHANNEL_BRIGHTNESS, command.toString());
        } else {
            logger.warn("Channel {} unable to process command {}", CHANNEL_BRIGHTNESS, command.toString());
        }
    }

    private void handleColor(Command command) throws IOException, CommandUnsuccessfulException {
        if (command instanceof HSBType) {
            HSBType color = (HSBType) command;
            Color c = new Color(color.getRGB());
            int r = c.getRed();
            int g = c.getGreen();
            int b = c.getBlue();

            ColorCommand colorCommand = new ColorCommand(r, g, b, priority);
            sendCommand(colorCommand);
        } else {
            logger.warn("Channel {} unable to process command {}", CHANNEL_COLOR, command.toString());
        }
    }

    private void handleClearAll(Command command) throws IOException, CommandUnsuccessfulException {
        if (OnOffType.ON.equals(command)) {
            ClearAllCommand clearAllCommand = new ClearAllCommand();
            sendCommand(clearAllCommand);
            updateState(CHANNEL_CLEAR_ALL, OnOffType.OFF);
        }
    }

    private void handleClear(Command command) throws IOException, CommandUnsuccessfulException {
        if (OnOffType.ON.equals(command)) {
            ClearCommand clearCommand = new ClearCommand(priority);
            sendCommand(clearCommand);
            updateState(CHANNEL_CLEAR, OnOffType.OFF);
        }
    }

    public V1Response sendCommand(HyperionCommand command) throws IOException, CommandUnsuccessfulException {
        String commandJson = gson.toJson(command);
        String jsonResponse = connection.send(commandJson);
        V1Response response = gson.fromJson(jsonResponse, V1Response.class);
        if (!response.isSuccess()) {
            throw new CommandUnsuccessfulException(gson.toJson(command));
        }
        return response;
    }

    private void updateOnlineStatus(ThingStatus status, ThingStatusDetail detail) {
        ThingStatus current = thing.getStatus();
        ThingStatusDetail currentDetail = thing.getStatusInfo().getStatusDetail();
        if (!current.equals(status) || !currentDetail.equals(detail)) {
            updateStatus(status, detail);
        }
    }
}
