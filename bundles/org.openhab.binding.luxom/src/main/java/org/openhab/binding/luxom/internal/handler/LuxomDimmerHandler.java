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
package org.openhab.binding.luxom.internal.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.luxom.internal.LuxomBindingConstants;
import org.openhab.binding.luxom.internal.handler.config.LuxomThingDimmerConfig;
import org.openhab.binding.luxom.internal.handler.util.PercentageConverter;
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

    private @Nullable LuxomThingDimmerConfig config;
    private final AtomicReference<Integer> lastLightLevel = new AtomicReference<>(0);

    @Override
    public void initialize() {
        super.initialize();
        config = getConfig().as(LuxomThingDimmerConfig.class);

        logger.debug("Initializing Switch handler for address {}", getAddress());

        initDeviceState();
    }

    @Override
    protected void initDeviceState() {
        logger.debug("Initializing device state for Switch {}", getAddress());
        @Nullable
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        } else if (ThingStatus.ONLINE.equals(bridge.getStatus())) {
            if (config != null && config.doesNotReply) {
                logger.debug("Switch {} will not reply, so always keeping it ONLINE", getAddress());
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "@text/status.awaiting-initial-response");
                ping(); // handleUpdate() will set thing status to online when response arrives
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("dimmer at address {} received command {} for {}", getAddress(), command.toFullString(),
                channelUID);
        if (LuxomBindingConstants.CHANNEL_SWITCH.equals(channelUID.getId())) {
            if (OnOffType.ON.equals(command)) {
                set();
            } else if (OnOffType.OFF.equals(command)) {
                clear();
            }
        } else if (LuxomBindingConstants.CHANNEL_BRIGHTNESS.equals(channelUID.getId()) && config != null) {
            if (command instanceof Number number) {
                int level = number.intValue();
                logger.trace("dimmer at address {} just setting dimmer level", getAddress());
                dim(level);
            } else if (command instanceof IncreaseDecreaseType increaseDecreaseCommand) {
                int currentValue = lastLightLevel.get();
                int newValue;
                if (IncreaseDecreaseType.INCREASE.equals(increaseDecreaseCommand)) {
                    newValue = currentValue + config.stepPercentage;
                    // round down to step multiple
                    newValue = newValue - newValue % config.stepPercentage;
                    logger.trace("dimmer at address {} just increasing dimmer level", getAddress());
                    dim(newValue);
                } else {
                    newValue = currentValue - config.stepPercentage;
                    // round up to step multiple
                    newValue = newValue + newValue % config.stepPercentage;
                    logger.trace("dimmer at address {} just increasing dimmer level", getAddress());
                    dim(Math.max(newValue, 0));
                }
            } else if (OnOffType.ON.equals(command)) {
                if (config.onToLast) {
                    dim(lastLightLevel.get());
                } else {
                    dim(config.onLevel.intValue());
                }
            } else if (OnOffType.OFF.equals(command)) {
                dim(0);
            }
        }
    }

    @Override
    public void handleCommandComingFromBridge(LuxomCommand command) {
        updateStatus(ThingStatus.ONLINE);
        if (LuxomAction.CLEAR_RESPONSE.equals(command.getAction())) {
            updateState(LuxomBindingConstants.CHANNEL_SWITCH, OnOffType.OFF);
        } else if (LuxomAction.SET_RESPONSE.equals(command.getAction())) {
            updateState(LuxomBindingConstants.CHANNEL_SWITCH, OnOffType.ON);
        } else if (LuxomAction.DATA_RESPONSE.equals(command.getAction())) {
            int percentage = PercentageConverter.getPercentage(command.getData());

            lastLightLevel.set(percentage);
            updateState(LuxomBindingConstants.CHANNEL_BRIGHTNESS, new PercentType(percentage));
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.debug("dimmer at address {} linked to channel {}", getAddress(), channelUID);
        if (LuxomBindingConstants.CHANNEL_SWITCH.equals(channelUID.getId())
                || LuxomBindingConstants.CHANNEL_BRIGHTNESS.equals(channelUID.getId())) {
            // Refresh state when new item is linked.
            if (config != null && !config.doesNotReply) {
                ping();
            }
        }
    }

    /**
     * example : *A,0,2,2B;*Z,057;
     */
    private void dim(int percentage) {
        logger.debug("dimming dimmer at address {} to {} %", getAddress(), percentage);
        List<CommandExecutionSpecification> commands = new ArrayList<>(3);
        if (percentage == 0) {
            commands.add(new CommandExecutionSpecification(LuxomAction.CLEAR.getCommand() + ",0," + getAddress()));
        } else {
            commands.add(new CommandExecutionSpecification(LuxomAction.SET.getCommand() + ",0," + getAddress()));
        }
        commands.add(new CommandExecutionSpecification(LuxomAction.DATA.getCommand() + ",0," + getAddress()));
        commands.add(new CommandExecutionSpecification(
                LuxomAction.DATA_BYTE.getCommand() + ",0" + PercentageConverter.getHexRepresentation(percentage)));

        sendCommands(commands);
    }
}
