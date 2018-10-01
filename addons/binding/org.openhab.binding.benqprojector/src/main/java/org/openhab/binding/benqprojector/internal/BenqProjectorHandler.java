/**
 * Copyright (c) 2010-2018 by the respective copyright holders
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.benqprojector.internal;

import static org.openhab.binding.benqprojector.internal.BenqProjectorBindingConstants.CHANNEL_PROJECTOR_MAPPING;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.benqprojector.internal.BenqProjectorSerialInterface.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BenqProjectorHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author René Treffer - Initial contribution
 */
@NonNullByDefault
public class BenqProjectorHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(BenqProjectorHandler.class);

    @Nullable
    private BenqProjectorConfiguration config;

    @Nullable
    private volatile BenqProjectorSerialInterface serial;

    @Nullable
    private ScheduledFuture<?> pollingJob;

    public BenqProjectorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (logger.isDebugEnabled()) {
            logger.debug("channel %s (%s) received %s (%s)", channelUID.toString(), channelUID.getId(),
                    command.toString(), command.getClass().getCanonicalName());
        }

        if (serial == null) {
            return;
        }

        Channel channel = thing.getChannel(channelUID.getId());
        if (channel == null) {
            return;
        }

        String channelId = channel.getUID().getId();

        if (!CHANNEL_PROJECTOR_MAPPING.containsKey(channelId)) {
            logger.warn("BUG: missing channelId mapping for \"%s\"", channelId);
            return;
        }
        String projChannel = CHANNEL_PROJECTOR_MAPPING.get(channelId);

        String itemType = channel.getAcceptedItemType();
        if (itemType != null) {
            switch (itemType) {
                case "String":
                    if (handleTextCommand(channelId, projChannel, command)) {
                        return;
                    }
                    break;
                case "Switch":
                    if (handleSwitchCommand(channelId, projChannel, command)) {
                        return;
                    }
                    break;
                case "Dimmer":
                    if (handleDimmerCommand(channelId, projChannel, command)) {
                        return;
                    }
                    break;
                default:
                    logger.warn("BUG: unhandled channelId %s type: %v", channelId, itemType);
            }
        } else {
            logger.warn("BUG: channelId %s type is NULL", channelId);
        }

        logger.warn("BUG: unhandled command for channel \"%s\": %s", channelId, command);
    }

    private boolean handleTextCommand(String channel, String projChannel, Command cmd) {
        BenqProjectorSerialInterface serial = this.serial;
        if (serial == null) {
            return false;
        }

        if (cmd == RefreshType.REFRESH) {
            return refreshText(channel, projChannel);
        }

        try {
            Response resp = serial.put(projChannel, cmd.toString());
            return resp.success;
        } catch (IOException e) {
            logger.warn("could not switch projector " + channel + " to " + cmd, e);
        }
        logger.warn("unknown command for projector " + channel + ": " + cmd);
        return false;
    }

    private boolean handleSwitchCommand(String channel, String projChannel, Command cmd) {
        BenqProjectorSerialInterface serial = this.serial;
        if (serial == null) {
            return false;
        }

        if (cmd == RefreshType.REFRESH) {
            return refreshSwitch(channel, projChannel);
        }

        Command onoff = cmd;
        if (cmd instanceof State) {
            Command newCommand = ((State) cmd).as(OnOffType.class);
            if (newCommand != null) {
                onoff = newCommand;
            }
        }
        if (onoff == OnOffType.ON || onoff == OnOffType.OFF) {
            try {
                Response resp = serial.put(projChannel, onoff.toString());
                return resp.success;
            } catch (IOException e) {
                logger.warn("could not switch projector " + channel + " to " + onoff, e);
            }
        }
        logger.warn("unknown command for projector " + channel + ": " + cmd);
        return false;
    }

    private boolean handleDimmerCommand(String channel, String projChannel, Command cmd) {
        BenqProjectorSerialInterface serial = this.serial;
        if (serial == null) {
            return false;
        }

        if (cmd == RefreshType.REFRESH) {
            return refreshDimmer(channel, projChannel);
        }

        Command pct = cmd;
        if (cmd instanceof State) {
            Command newCommand = ((State) cmd).as(PercentType.class);
            if (newCommand != null) {
                pct = newCommand;
            }
        }
        if (pct instanceof PercentType) {
            int target = ((PercentType) pct).intValue();
            int current = -100;

            try {
                Response resp = serial.get(projChannel);
                if (!resp.success) {
                    logger.warn("could not get current value for projector " + channel + ": " + resp.error);
                    return false;
                }
                current = Integer.parseInt(resp.value);
            } catch (IOException e) {
                logger.warn("could not get current value for projector " + channel, e);
                return false;
            }

            // converge the current value and the target value (we can only increment/decrement values by 1)
            // abort if the convergence fails (delta stops falling) or if we reach a delta of 0

            int delta = Math.abs(current - target);
            int olddelta = delta + 1;
            while (olddelta > delta && delta > 0) {
                olddelta = delta;
                String value = (current < target) ? "+" : "-";
                try {
                    // update value
                    Response resp = serial.put(projChannel, value);
                    if (!resp.success) {
                        logger.warn("could not update value for projector " + channel + ": " + resp.error);
                        return false;
                    }
                    // verify value
                    resp = serial.get(projChannel);
                    if (!resp.success) {
                        logger.warn("could not get current value for projector " + channel + ": " + resp.error);
                        return false;
                    }
                    current = Integer.parseInt(resp.value);
                } catch (IOException e) {
                    logger.warn("could not update value for projector " + channel, e);
                    return false;
                }
                delta = Math.abs(current - target);
            }
        }
        logger.warn("unknown command for projector " + channel + ": " + cmd);
        return false;
    }

    @Override
    @SuppressWarnings("null")
    public void initialize() {
        config = getConfigAs(BenqProjectorConfiguration.class);

        serial = new BenqProjectorSerial(config.serialPort, config.serialSpeed);

        updateState();

        int pollingPeriod = config.refreshInterval;
        pollingJob = scheduler.scheduleWithFixedDelay(() -> {
            logger.debug("scheduled refresh of data");
            updateState();
        }, pollingPeriod, pollingPeriod, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        if (pollingJob != null) {
            pollingJob.cancel(true);
        }
        if (serial != null) {
            serial.close();
        }
    }

    public boolean updateState() {
        BenqProjectorSerialInterface serial = this.serial;
        if (serial == null) {
            return false;
        }

        if (!serial.check()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                    "Could not perform a handshake with the projector - reopening the serial console");
            serial.reset();
            return false;
        }

        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "ready");

        boolean success = false;

        for (Channel channel : thing.getChannels()) {
            logger.debug("channel " + channel.getUID() + " :: " + channel.getAcceptedItemType());
            String channelId = channel.getUID().getId();
            if (!CHANNEL_PROJECTOR_MAPPING.containsKey(channelId)) {
                logger.warn("BUG: missing channelId mapping for \"" + channelId + "\"");
                updateState(channel.getUID(), UnDefType.NULL);
                continue;
            }
            String projChannel = CHANNEL_PROJECTOR_MAPPING.get(channelId);
            String itemType = channel.getAcceptedItemType();
            if (itemType != null) {
                switch (itemType) {
                    case "String":
                        success |= refreshText(channelId, projChannel);
                        break;
                    case "Switch":
                        success |= refreshSwitch(channelId, projChannel);
                        break;
                    case "Dimmer":
                        success |= refreshDimmer(channelId, projChannel);
                        break;
                }
            }
        }

        return success;
    }

    private boolean refreshText(String channel, String projChannel) {
        BenqProjectorSerialInterface serial = this.serial;
        if (serial == null) {
            return false;
        }

        try {
            Response resp = serial.get(projChannel);
            if (resp.success) {
                updateState(channel, StringType.valueOf(resp.value));
                return true;
            } else {
                updateState(channel, UnDefType.UNDEF);
            }
        } catch (IOException e) {
            logger.debug("could not refresh " + channel + " setting", e);
        }
        return false;
    }

    private boolean refreshSwitch(String channel, String projChannel) {
        BenqProjectorSerialInterface serial = this.serial;
        if (serial == null) {
            return false;
        }

        try {
            Response resp = serial.get(projChannel);
            if (resp.success) {
                updateState(channel, "ON".equalsIgnoreCase(resp.value) ? OnOffType.ON : OnOffType.OFF);
                return true;
            } else {
                updateState(channel, UnDefType.UNDEF);
            }
        } catch (IOException e) {
            logger.debug("could not refresh " + channel + " setting", e);
        }
        return false;
    }

    private boolean refreshDimmer(String channel, String projChannel) {
        BenqProjectorSerialInterface serial = this.serial;
        if (serial == null) {
            return false;
        }

        try {
            Response resp = serial.get(projChannel);
            if (resp.success) {
                updateState(channel, PercentType.valueOf(resp.value));
                return true;
            } else {
                updateState(channel, UnDefType.UNDEF);
            }
        } catch (IOException | NumberFormatException e) {
            logger.debug("could not refresh power setting", e);
        }
        return false;
    }

}
