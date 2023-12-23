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
package org.openhab.binding.infokeydinrail.internal.handler;

import static org.openhab.binding.infokeydinrail.internal.InfokeyBindingConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.infokeydinrail.internal.MCP230xxModuleRunnable;
import org.openhab.binding.infokeydinrail.internal.PinMapperBoard;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.PinState;

/**
 * The {@link InfokeyOptoDinV1Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Themistoklis Anastasopoulos - Initial contribution
 */
// @NonNullByDefault
public class InfokeyOptoDinV1Handler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String serverIP = "127.0.0.1";
    private Integer address;
    private Integer busNumber;
    private InfokeyPinStateHolder pinStateHolder;
    private Map<ChannelUID, PinState> stateMap = new HashMap<>();
    private ScheduledFuture<?> pollingJob;
    /**
     * the polling interval mcp check interrupt register (optional, defaults to 50ms)
     */
    private static final int POLLING_INTERVAL = 1000;

    public InfokeyOptoDinV1Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command: {} on channelGroup {} on channel {}", command.toFullString(),
                channelUID.getGroupId(), channelUID.getIdWithoutGroup());

        if (!verifyChannel(channelUID)) {
            return;
        }

        String channelGroup = channelUID.getGroupId();

        switch (channelGroup) {
            case CHANNEL_GROUP_INPUT:
                handleInputCommand(channelUID, command);
                break;
            case CHANNEL_GROUP_OUTPUT:
                handleOutputCommand(channelUID, command);
            case CHANNEL_GROUP_PULSE:
                handleOutputCommand(channelUID, command);
            default:
                break;
        }
    }

    @Override
    public void initialize() {
        try {
            checkConfiguration();
            pinStateHolder = new InfokeyPinStateHolder(this.thing);
            updateStatus(ThingStatus.ONLINE);
        } catch (IllegalArgumentException | SecurityException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "An exception occurred while adding pin. Check pin configuration. Exception: " + e.getMessage());
        }
    }

    private boolean verifyChannel(ChannelUID channelUID) {
        if (!isChannelGroupValid(channelUID) || !isChannelValid(channelUID)) {
            logger.warn("Channel group or channel is invalid. Probably configuration problem");
            return false;
        }
        return true;
    }

    private void handleOutputCommand(ChannelUID channelUID, Command command) {
        logger.debug("Nothing to be done in handleCommand for switch.");
    }

    private void handleInputCommand(ChannelUID channelUID, Command command) {
        logger.debug("Nothing to be done in handleCommand for contact.");
    }

    private boolean isChannelGroupValid(ChannelUID channelUID) {
        if (!channelUID.isInGroup()) {
            logger.debug("Defined channel not in group: {}", channelUID.getAsString());
            return false;
        }
        boolean channelGroupValid = SUPPORTED_CHANNEL_GROUPS.contains(channelUID.getGroupId());
        logger.debug("Defined channel in group: {}. Valid: {}", channelUID.getGroupId(), channelGroupValid);

        return channelGroupValid;
    }

    private boolean isChannelValid(ChannelUID channelUID) {
        boolean channelValid = SUPPORTED_CHANNELS.contains(channelUID.getIdWithoutGroup());
        logger.debug("Is channel {} in supported channels: {}", channelUID.getIdWithoutGroup(), channelValid);
        return channelValid;
    }

    protected void checkConfiguration() {
        Configuration configuration = getConfig();
        address = Integer.parseInt((configuration.get(ADDRESS)).toString(), 16);
        busNumber = Integer.parseInt((configuration.get(BUS_NUMBER)).toString());
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    public void readInputPinState(PinState pinState, ChannelUID channelForPin) {

        boolean update = false;

        if ((!stateMap.containsKey(channelForPin))
                || (stateMap.containsKey(channelForPin) && stateMap.get(channelForPin) != pinState)) {
            update = true;
        }

        // logger.debug(
        // "readInputPinState1 channel {} with state received{}, stateMap.containsKey(channelForPin) : {} &&
        // stateMap.get(channelForPin) {}, update : {}",
        // channelForPin, pinState, stateMap.containsKey(channelForPin), stateMap.get(channelForPin), update);

        if (update) {
            OpenClosedType state = OpenClosedType.CLOSED;

            try {
                Configuration configuration = thing.getChannel(channelForPin).getConfiguration();

                String defaultOpenState = Objects.toString(configuration.get(INPUT_DEFAULT_OPEN_STATE), null);

                if (pinState == PinState.LOW) {
                    if (defaultOpenState.equalsIgnoreCase("LOW")) {
                        state = OpenClosedType.OPEN;
                    } else {
                        state = OpenClosedType.CLOSED;
                    }
                } else {
                    if (defaultOpenState.equalsIgnoreCase("HIGH")) {
                        state = OpenClosedType.OPEN;
                    } else {
                        state = OpenClosedType.CLOSED;
                    }
                }

                logger.debug("updating channel {} with state {} and default open state {}", channelForPin, state,
                        defaultOpenState);
                updateState(channelForPin, state);

                stateMap.put(channelForPin, pinState);
                // pin);

            } catch (Exception ex) {
                logger.debug("Ops!", ex);
            }
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        synchronized (this) {
            logger.debug("channel linked {}", channelUID.getAsString());
            if (!verifyChannel(channelUID)) {
                return;
            }
            String channelGroup = channelUID.getGroupId();

            if (channelGroup != null && channelGroup.equals(CHANNEL_GROUP_INPUT)) {

                logger.debug(
                        "channelLinked for channelGroup {} and channelUID {} channelUID.getId() : {} channelUID.getIdWithoutGroup() : {}",
                        channelGroup, channelUID, channelUID.getId(), channelUID.getIdWithoutGroup());

                if (pinStateHolder.getInputPin(channelUID) != null) {
                    return;
                }

                if (pinStateHolder.getInputSize() == 0) {
                    initializeScheduler();
                }

                pinStateHolder.addInputPin(PinMapperBoard.get(channelUID.getIdWithoutGroup()), channelUID);

            }
            super.channelLinked(channelUID);
        }
    }

    private void initializeScheduler() {
        logger.debug("initializing scheduler");

        try {
            Runnable runnable = new MCP230xxModuleRunnable(this, this.thing, serverIP, 17, busNumber,
                    Integer.toHexString(address));
            pollingJob = scheduler.scheduleAtFixedRate(runnable, 0, POLLING_INTERVAL, TimeUnit.MILLISECONDS);

        } catch (Exception ex) {
            logger.debug("Ops!", ex);
        }
    }

    public ChannelUID getChannelFromPin(Integer pin) {
        return pinStateHolder.getChannelFromPin(pin);
    }

    public List<Integer> getInputPins() {
        return pinStateHolder.getInputPins();
    }
}
