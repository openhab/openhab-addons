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
package org.openhab.binding.luxom.internal.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.luxom.internal.LuxomBindingConstants;
import org.openhab.binding.luxom.internal.handler.config.LuxomThingDimmerConfig;
import org.openhab.binding.luxom.internal.handler.util.PercentageConvertor;
import org.openhab.binding.luxom.internal.protocol.LuxomAction;
import org.openhab.binding.luxom.internal.protocol.LuxomCommand;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LuxomDimmerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Kris Jespers - Initial contribution
 */
@NonNullByDefault
public class LuxomDimmerHandler extends LuxomThingHandler {

    private final Logger logger = LoggerFactory.getLogger(LuxomDimmerHandler.class);

    public LuxomDimmerHandler(Thing thing) {
        super(thing);
    }

    @Nullable
    private LuxomThingDimmerConfig config;
    private final AtomicReference<Integer> lastLightLevel = new AtomicReference<>(0);

    @Override
    public void initialize() {
        super.initialize();
        config = getThing().getConfiguration().as(LuxomThingDimmerConfig.class);

        logger.debug("Initializing Switch handler for address {}", getAddress());

        initDeviceState();
    }

    @Override
    protected void initDeviceState() {
        logger.debug("Initializing device state for Switch {}", getAddress());
        @Nullable
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge configured");
        } else if (bridge.getStatus() == ThingStatus.ONLINE) {
            if (config != null && config.doesNotReply) {
                logger.warn("Switch {} will not reply, so always keeping it ONLINE", getAddress());
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Awaiting initial response");
                ping(); // handleUpdate() will set thing status to online when response arrives
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("dimmer at address {} received command {} for {}", this.getAddress(), command.toFullString(),
                channelUID);
        if (channelUID.getId().equals(LuxomBindingConstants.CHANNEL_SWITCH)) {
            if (command.equals(OnOffType.ON)) {
                set();
            } else if (command.equals(OnOffType.OFF)) {
                clear();
            }
        } else if (channelUID.getId().equals(LuxomBindingConstants.CHANNEL_BRIGHTNESS) && config != null) {
            if (command instanceof Number) {
                int level = ((Number) command).intValue();
                logger.trace("dimmer at address {} just setting dimmer level", this.getAddress());
                dim(level);
            } else if (command instanceof IncreaseDecreaseType) {
                IncreaseDecreaseType s = (IncreaseDecreaseType) command;
                int currentValue = lastLightLevel.get();
                int newValue;
                if (IncreaseDecreaseType.INCREASE.equals(s)) {
                    newValue = currentValue + config.stepPercentage;
                    // round down to step multiple
                    newValue = newValue - newValue % config.stepPercentage;
                    logger.trace("dimmer at address {} just increasing dimmer level", this.getAddress());
                    dim(newValue);
                } else {
                    newValue = currentValue - config.stepPercentage;
                    // round up to step multiple
                    newValue = newValue + newValue % config.stepPercentage;
                    logger.trace("dimmer at address {} just increasing dimmer level", this.getAddress());
                    dim(Math.max(newValue, 0));
                }
            } else if (command.equals(OnOffType.ON)) {
                if (config.onToLast) {
                    dim(lastLightLevel.get());
                } else {
                    dim(config.onLevel.intValue());
                }
            } else if (command.equals(OnOffType.OFF)) {
                dim(0);
            }
        }
    }

    @Override
    public void handleCommandCommingFromBridge(LuxomCommand command) {
        updateStatus(ThingStatus.ONLINE);
        if (command.getAction() == LuxomAction.CLEAR_RESPONSE) {
            updateState(LuxomBindingConstants.CHANNEL_SWITCH, OnOffType.OFF);
        } else if (command.getAction() == LuxomAction.SET_RESPONSE) {
            updateState(LuxomBindingConstants.CHANNEL_SWITCH, OnOffType.ON);
        } else if (command.getAction() == LuxomAction.DATA_BYTE_RESPONSE) {
            int percentage = PercentageConvertor.getPercentage(command.getData());

            if (percentage > 0) {
                lastLightLevel.set(percentage);
                updateState(LuxomBindingConstants.CHANNEL_SWITCH, OnOffType.ON);
                updateState(LuxomBindingConstants.CHANNEL_BRIGHTNESS, new PercentType(percentage));
            } else {
                updateState(LuxomBindingConstants.CHANNEL_SWITCH, OnOffType.OFF);
            }
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.debug("dimmer at address {} linked to channel {}", getAddress(), channelUID);
        if (channelUID.getId().equals(LuxomBindingConstants.CHANNEL_SWITCH)
                || channelUID.getId().equals(LuxomBindingConstants.CHANNEL_BRIGHTNESS)) {
            // Refresh state when new item is linked.
            if (this.config != null && !this.config.doesNotReply) {
                ping();
            }
        }
    }

    /**
     * example : *A,0,2,2B;*Z,057;
     */
    private void dim(int percentage) {
        logger.debug("dimming dimmer at address {} to {} %", this.getAddress(), percentage);
        List<CommandExecutionSpecification> commands = new ArrayList<>(3);
        if (percentage == 0) {
            commands.add(new CommandExecutionSpecification(LuxomAction.CLEAR.getCommand() + ",0," + getAddress()));
        } else {
            commands.add(new CommandExecutionSpecification(LuxomAction.SET.getCommand() + ",0," + getAddress()));
        }
        commands.add(new CommandExecutionSpecification(LuxomAction.DATA.getCommand() + ",0," + getAddress()));
        commands.add(new CommandExecutionSpecification(
                LuxomAction.DATA_BYTE.getCommand() + ",0" + PercentageConvertor.getHexRepresentation(percentage)));

        sendCommands(commands);
    }
}
