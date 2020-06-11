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
package org.openhab.binding.lutron.internal.handler;

import static org.openhab.binding.lutron.internal.LutronBindingConstants.*;

import java.math.BigDecimal;

import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.lutron.internal.config.BlindConfig;
import org.openhab.binding.lutron.internal.protocol.LutronCommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler responsible for communicating with Lutron blinds
 *
 * @author Bob Adair - Initial contribution based on Alan Tong's DimmerHandler
 */
public class BlindHandler extends LutronHandler {
    private static final Integer ACTION_LIFTLEVEL = 1;
    private static final Integer ACTION_TILTLEVEL = 9;
    private static final Integer ACTION_LIFTTILTLEVEL = 10;
    private static final Integer ACTION_STARTRAISINGTILT = 11;
    private static final Integer ACTION_STARTLOWERINGTILT = 12;
    private static final Integer ACTION_STOPTILT = 13;
    private static final Integer ACTION_STARTRAISINGLIFT = 14;
    private static final Integer ACTION_STARTLOWERINGLIFT = 15;
    private static final Integer ACTION_STOPLIFT = 16;
    private static final Integer ACTION_POSITION_UPDATE = 32; // undocumented in integration protocol guide
    private static final Integer PARAMETER_POSITION_UPDATE = 2; // undocumented in integration protocol guide

    private int tiltMax = 100; // max 50 for horizontal sheer, 100 for venetian

    private final Logger logger = LoggerFactory.getLogger(BlindHandler.class);

    private BlindConfig config;

    public BlindHandler(Thing thing) {
        super(thing);
    }

    @Override
    public int getIntegrationId() {
        if (config == null) {
            throw new IllegalStateException("handler configuration not initialized");
        }
        return config.integrationId;
    }

    @Override
    public void initialize() {
        config = getThing().getConfiguration().as(BlindConfig.class);
        if (config.integrationId <= 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No integrationId configured");
            return;
        }
        if (config.type == null || (!(BLIND_TYPE_SHEER.equalsIgnoreCase(config.type))
                && !(BLIND_TYPE_VENETIAN.equalsIgnoreCase(config.type)))) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Parameter type not set to valid value");
            return;
        }
        String blindType = config.type;
        if (BLIND_TYPE_SHEER.equalsIgnoreCase(blindType)) {
            tiltMax = 50;
        }
        logger.debug("Initializing Blind handler with type {} for integration ID {}", blindType, config.integrationId);
        initDeviceState();
    }

    @Override
    protected void initDeviceState() {
        logger.debug("Initializing device state for Shade {}", getIntegrationId());
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge configured");
        } else if (bridge.getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Awaiting initial response");
            queryOutput(ACTION_LIFTLEVEL); // handleUpdate() will set thing status to online when response arrives
            queryOutput(ACTION_TILTLEVEL);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        // Refresh state when new item is linked.
        if (channelUID.getId().equals(CHANNEL_BLINDLIFTLEVEL)) {
            queryOutput(ACTION_LIFTLEVEL);
        } else if (channelUID.getId().equals(CHANNEL_BLINDTILTLEVEL)) {
            queryOutput(ACTION_TILTLEVEL);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_BLINDLIFTLEVEL)) {
            handleLiftCommand(command);
        } else if (channelUID.getId().equals(CHANNEL_BLINDTILTLEVEL)) {
            handleTiltCommand(command);
        }
    }

    private void handleLiftCommand(Command command) {
        if (command instanceof PercentType) {
            int level = ((PercentType) command).intValue();
            output(ACTION_LIFTLEVEL, level, 0);
        } else if (command.equals(UpDownType.UP)) {
            output(ACTION_STARTRAISINGLIFT);
        } else if (command.equals(UpDownType.DOWN)) {
            output(ACTION_STARTLOWERINGLIFT);
        } else if (command.equals(StopMoveType.STOP)) {
            output(ACTION_STOPLIFT);
        } else if (command instanceof RefreshType) {
            queryOutput(ACTION_LIFTLEVEL);
        }
    }

    private void handleTiltCommand(Command command) {
        if (command instanceof PercentType) {
            int level = ((PercentType) command).intValue();
            output(ACTION_TILTLEVEL, Math.min(level, tiltMax), 0);
        } else if (command.equals(UpDownType.UP)) {
            output(ACTION_STARTRAISINGTILT);
        } else if (command.equals(UpDownType.DOWN)) {
            output(ACTION_STARTLOWERINGTILT);
        } else if (command.equals(StopMoveType.STOP)) {
            output(ACTION_STOPTILT);
        } else if (command instanceof RefreshType) {
            queryOutput(ACTION_TILTLEVEL);
        }
    }

    @Override
    public void handleUpdate(LutronCommandType type, String... parameters) {
        if (type == LutronCommandType.OUTPUT && parameters.length >= 2) {
            if (getThing().getStatus() == ThingStatus.UNKNOWN) {
                updateStatus(ThingStatus.ONLINE);
            }

            if (ACTION_LIFTLEVEL.toString().equals(parameters[0])) {
                BigDecimal liftLevel = new BigDecimal(parameters[1]);
                logger.trace("Blind {} received lift level: {}", getIntegrationId(), liftLevel);
                updateState(CHANNEL_BLINDLIFTLEVEL, new PercentType(liftLevel));
            } else if (ACTION_TILTLEVEL.toString().equals(parameters[0])) {
                BigDecimal tiltLevel = new BigDecimal(parameters[1]);
                logger.trace("Blind {} received tilt level: {}", getIntegrationId(), tiltLevel);
                updateState(CHANNEL_BLINDTILTLEVEL, new PercentType(tiltLevel));
            } else if (ACTION_LIFTTILTLEVEL.toString().equals(parameters[0]) && parameters.length > 2) {
                BigDecimal liftLevel = new BigDecimal(parameters[1]);
                BigDecimal tiltLevel = new BigDecimal(parameters[2]);
                logger.trace("Blind {} received lift/tilt level: {} {}", getIntegrationId(), liftLevel, tiltLevel);
                updateState(CHANNEL_BLINDLIFTLEVEL, new PercentType(liftLevel));
                updateState(CHANNEL_BLINDTILTLEVEL, new PercentType(tiltLevel));
            } else if (ACTION_POSITION_UPDATE.toString().equals(parameters[0])
                    && PARAMETER_POSITION_UPDATE.toString().equals(parameters[1]) && parameters.length >= 3) {
                BigDecimal level = new BigDecimal(parameters[2]);
                logger.trace("Blind {} received lift level position update: {}", getIntegrationId(), level);
                updateState(CHANNEL_BLINDLIFTLEVEL, new PercentType(level));
            }
        }
    }
}
