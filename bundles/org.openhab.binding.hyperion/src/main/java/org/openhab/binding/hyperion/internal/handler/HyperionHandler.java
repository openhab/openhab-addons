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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
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
                logger.debug("Could not connect to server.");
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
                logger.debug("Could not connect to server.");
                updateOnlineStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
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
        Optional<Transform> defaultTransform = transform.stream() // convert list to stream
                .findFirst();

        if (defaultTransform.isPresent()) {
            double luminanceGain = defaultTransform.get().getLuminanceGain();
            if (luminanceGain >= 0.0 && luminanceGain <= 1.0) {
                PercentType luminanceGainPercentType = new PercentType((int) (luminanceGain * 100));
                updateState(CHANNEL_BRIGHTNESS, luminanceGainPercentType);
            }
        } else {
            updateState(CHANNEL_BRIGHTNESS, UnDefType.NULL);
        }
    }

    private void updateEffect(List<ActiveEffect> activeEffects, List<Effect> effects) {
        Optional<ActiveEffect> effect = activeEffects.stream() // convert list to stream
                .findFirst();
        if (effect.isPresent()) {
            String path = effect.get().getScript();
            Optional<Effect> effectDescription = effects.stream() // convert list to stream
                    .filter(effectCandidate -> effectCandidate.getScript().equals(path)).findFirst();
            if (effectDescription.isPresent()) {
                String effectName = effectDescription.get().getName();
                updateState(CHANNEL_EFFECT, new StringType(effectName));
            }
        } else {
            updateState(CHANNEL_EFFECT, UnDefType.NULL);
        }
    }

    private void updateColor(List<ActiveLedColor> activeColors) {
        Optional<ActiveLedColor> color = activeColors.stream() // convert list to stream
                .findFirst();
        if (color.isPresent()) {
            List<Integer> rgbVals = color.get().getRGBValue();
            int r = rgbVals.get(0);
            int g = rgbVals.get(1);
            int b = rgbVals.get(2);
            HSBType hsbType = HSBType.fromRGB(r, g, b);
            updateState(CHANNEL_COLOR, hsbType);
        } else {
            updateState(CHANNEL_COLOR, UnDefType.NULL);
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
            logger.debug("Could not resolve host: {}", e.getMessage());
            updateOnlineStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
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
            } else if (CHANNEL_CLEAR.equals(channelUID.getId())) {
                handleClear(command);
            } else if (CHANNEL_EFFECT.equals(channelUID.getId())) {
                handleEffect(command);
            }
        } catch (IOException e) {
            logger.debug("Unable to send command: {}", command);
        } catch (CommandUnsuccessfulException e) {
            logger.debug("Server rejected the command: {}", e.getMessage());
        }
    }

    private void handleEffect(Command command) throws IOException, CommandUnsuccessfulException {
        if (command instanceof StringType) {
            String effectName = command.toString();

            Effect effect = new Effect(effectName);
            EffectCommand effectCommand = new EffectCommand(effect, priority);
            sendCommand(effectCommand);
        } else {
            logger.debug("Channel {} unable to process command {}", CHANNEL_EFFECT, command);
        }
    }

    private void handleBrightness(Command command) throws IOException, CommandUnsuccessfulException {
        if (command instanceof PercentType percentCommand) {
            Transform transform = new Transform();
            transform.setLuminanceGain(percentCommand.doubleValue() / 100);
            TransformCommand transformCommand = new TransformCommand(transform);
            sendCommand(transformCommand);
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
            sendCommand(colorCommand);
        } else {
            logger.debug("Channel {} unable to process command {}", CHANNEL_COLOR, command);
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

    public V1Response sendCommand(HyperionCommand command) throws IOException, CommandUnsuccessfulException {
        String commandJson = gson.toJson(command);
        String jsonResponse = connection.send(commandJson);
        V1Response response = gson.fromJson(jsonResponse, V1Response.class);
        if (!response.isSuccess()) {
            throw new CommandUnsuccessfulException(gson.toJson(command));
        }
        return response;
    }

    private void updateOnlineStatus(ThingStatus status, ThingStatusDetail detail, String message) {
        ThingStatus current = thing.getStatus();
        ThingStatusDetail currentDetail = thing.getStatusInfo().getStatusDetail();
        if (!current.equals(status) || !currentDetail.equals(detail)) {
            updateStatus(status, detail, message);
        }
    }
}
