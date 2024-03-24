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
package org.openhab.binding.panamaxfurman.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.panamaxfurman.internal.protocol.PowerConditionerChannel;
import org.openhab.binding.panamaxfurman.internal.protocol.ProtocolMapper;
import org.openhab.binding.panamaxfurman.internal.protocol.event.PanamaxFurmanCommunicationEventListener;
import org.openhab.binding.panamaxfurman.internal.protocol.event.PanamaxFurmanConnectionActiveEvent;
import org.openhab.binding.panamaxfurman.internal.protocol.event.PanamaxFurmanConnectionBrokenEvent;
import org.openhab.binding.panamaxfurman.internal.protocol.event.PanamaxFurmanConnectivityEvent;
import org.openhab.binding.panamaxfurman.internal.protocol.event.PanamaxFurmanInformationReceivedEvent;
import org.openhab.binding.panamaxfurman.internal.transport.PanmaxFurmanConnector;
import org.openhab.binding.panamaxfurman.internal.util.TimeInterval;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PanamaxFurmanHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Dave Badia - Initial contribution
 */
@NonNullByDefault
public abstract class PanamaxFurmanAbstractHandler extends BaseThingHandler
        implements PanamaxFurmanCommunicationEventListener {
    private final Logger logger = LoggerFactory.getLogger(PanamaxFurmanAbstractHandler.class);

    private static final long AVOID_DUPLICATE_REQUESTS_WITHIN_MILLIS = TimeUnit.SECONDS.toMillis(3);
    private static final Map<String, Long> REQUEST_LAST_SENT_AT = new ConcurrentHashMap<>();
    private final ProtocolMapper protocolMapper;
    /**
     * Nullable since PanmaxFurmanConnector constructor will throw an exception if it can't connect
     */
    private @Nullable PanmaxFurmanConnector connector;
    private @Nullable ScheduledFuture<?> backgroundJob;

    public PanamaxFurmanAbstractHandler(Thing thing, ProtocolMapper protocolMapper) {
        super(thing);
        this.protocolMapper = protocolMapper;
    }

    /**
     * Request the implementation to build a connector for the protocol
     *
     * @return the connector
     */
    protected abstract @Nullable PanmaxFurmanConnector buildConnector(Configuration genericConfig);

    /**
     * The name of this device connection
     *
     * @return a description of this connection for logging purposes
     */
    protected abstract String getConnectionName();

    /**
     * The interval at which the device should be polled for status updates. Will vary depending on whether the
     * underlying protocol supports push updates or not
     *
     * @return the interval at which update polling should take place
     */
    protected abstract TimeInterval pollInterval();

    @Override
    public void initialize() {
        try {
            updateStatus(ThingStatus.UNKNOWN);
            this.connector = buildConnector(getConfig());
            logger.debug("starting scheduled status now then repeating {}", getConnectionName());
            // Request ID info immediately
            if (connector != null) {
                connector.sendRequestToDevice(protocolMapper.buildQueryString(PowerConditionerChannel.ID, null));
            }
            // Query the state every 5 minutes. This will also attempt to reconnect if the device was unreachable
            // A request for outlet 1 will return status for all outlets
            backgroundJob = scheduler.scheduleWithFixedDelay(() -> requestStatusOf("outlet1#power"), 0,
                    pollInterval().duration(), pollInterval().unit());
        } catch (Exception e) {
            logger.error("Caught exception during handler initialize.  Please raise a Github issue.", e);
            throw e;
        }
    }

    public void requestStatusOf(String channelString) {
        if (!checkConnected()) {
            return;
        }

        logger.trace("in request status {}  {}", channelString, getConnectionName());
        Integer outletNumber = PanamaxFurmanAbstractHandler.getOutletFromChannelUID(channelString);
        PowerConditionerChannel channel = PowerConditionerChannel.from(channelString);
        String commandToTransmitToThing = protocolMapper.buildQueryString(channel, outletNumber);
        if (commandToTransmitToThing == null) {
            return; // not supported, but still considered a success
        }
        if (System.currentTimeMillis() - REQUEST_LAST_SENT_AT.getOrDefault(commandToTransmitToThing,
                0L) < AVOID_DUPLICATE_REQUESTS_WITHIN_MILLIS) {
            logger.debug("Dropping request of '{}' since it was sent less than {}ms ago.   @{}",
                    commandToTransmitToThing, AVOID_DUPLICATE_REQUESTS_WITHIN_MILLIS, getConnectionName());
        } else {
            REQUEST_LAST_SENT_AT.put(commandToTransmitToThing, System.currentTimeMillis());
            connector.sendRequestToDevice(commandToTransmitToThing);
        }
    }

    /**
     * Invoked when the user wants to send a command to the Power Conditioner
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            requestStatusOf(channelUID.getId());
        } else {
            // Update command
            String channelString = channelUID.getId();
            Integer outletNumber = PanamaxFurmanAbstractHandler.getOutletFromChannelUID(channelString);
            PowerConditionerChannel channel = PowerConditionerChannel.from(channelString);
            if (channel.getStateClass() == null) {
                logger.warn("Channel {} had null state class, command not sent  {}.  @{}", channel, command.getClass(),
                        getConnectionName());
                return;
            } else if (!channel.getStateClass().isInstance(command)) {
                logger.warn(
                        "Update command {} NOT sent:  it expected State of class {} but was passed {}.  Please open a github issue.  @{}",
                        channel, channel.getStateClass(), command.getClass(), getConnectionName());
                return;
            }
            State stateToSet = channel.getStateClass().cast(command);
            String textToTransmitToThing = protocolMapper.buildUpdateString(channel, outletNumber, stateToSet);
            if (checkConnected()) {
                connector.sendRequestToDevice(textToTransmitToThing);
            }
        }
    }

    @Override
    public void onConnectivityEvent(PanamaxFurmanConnectivityEvent event) {
        logger.trace("processing PanamaxFurmanConnectivityEvent of {}.   @{}", event, getConnectionName());
        // Only invoke updateStatus if it has actually changed
        if (event instanceof PanamaxFurmanConnectionActiveEvent) {
            updateStatusIfChanged(ThingStatus.ONLINE);
        } else if (event instanceof PanamaxFurmanConnectionBrokenEvent brokenEvent) {
            updateStatusIfChanged(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    brokenEvent.getErrorDetail());
        } else {
            logger.warn("Unhandled PanamaxFurmanConnectivityEvent of {}.   @{}", event.getClass(), getConnectionName());
        }
    }

    @Override
    public void onInformationReceived(String data) {
        PanamaxFurmanInformationReceivedEvent event = protocolMapper.parseUpdateIfSupported(data);
        if (event != null) {
            State state = event.getState();
            if (state != null) {
                updateState(event.getChannelString(), state);
            }
        }
    }

    private void updateStatusIfChanged(ThingStatus status) {
        updateStatusIfChanged(status, ThingStatusDetail.NONE, null);
    }

    private void updateStatusIfChanged(ThingStatus status, ThingStatusDetail detail, @Nullable String errorDetail) {
        if (getThing().getStatus() != status) {
            super.updateStatus(status, detail, errorDetail);
        }
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> backgroundJob = this.backgroundJob;
        if (backgroundJob != null) {
            backgroundJob.cancel(false);
            this.backgroundJob = null;
        }
        if (connector != null) {
            connector.shutdown();
        }
        super.dispose();
    }

    private boolean checkConnected() {
        if (connector == null) {
            connector = buildConnector(getConfig());
        }
        return connector != null;
    }

    /**
     * Build the channelUID from the channel name and the outlet number.
     */
    public static String getChannelUID(String channelName, int outlet) {
        return String.format(PanamaxFurmanConstants.GROUP_CHANNEL_PATTERN, outlet, channelName);
    }

    /**
     * @return the outlet number or null if the outlet was not present or could not be extracted from the channelUID.
     */
    public static @Nullable Integer getOutletFromChannelUID(ChannelUID channelUID) {
        return getOutletFromChannelUID(channelUID.getId());
    }

    public static @Nullable Integer getOutletFromChannelUID(String channelString) {
        Integer outletNumber = null;

        Matcher matcher = PanamaxFurmanConstants.GROUP_CHANNEL_OUTLET_PATTERN.matcher(channelString);
        if (matcher.find()) {
            try {
                outletNumber = Integer.valueOf(matcher.group(1));
            } catch (NumberFormatException e) {
                LoggerFactory.getLogger(PanamaxFurmanAbstractHandler.class).warn(
                        "Caught exception trying to parse outlet number from '{}' {}", channelString, e.getMessage());
            }
            if (outletNumber == null) {
                LoggerFactory.getLogger(PanamaxFurmanAbstractHandler.class)
                        .warn("Could not parse outlet number from '{}'", channelString);
            }
        }
        return outletNumber;
    }
}
