/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.plclogo.internal.handler;

import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.DAY_OF_WEEK;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.DAY_OF_WEEK_CHANNEL;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.DIAGNOSTIC_CHANNEL;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.LOGO_0BA7;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.LOGO_CHANNELS;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.LOGO_MEMORY_BLOCK;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.LOGO_STATES;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.MEMORY_SIZE;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.RTC_CHANNEL;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.plclogo.internal.PLCLogoClient;
import org.openhab.binding.plclogo.internal.config.PLCLogoBridgeConfiguration;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Moka7.S7Client;

/**
 * The {@link PLCBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alexander Falkenstern - Initial contribution
 */
@NonNullByDefault
public class PLCBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(PLCBridgeHandler.class);

    private final Map<ChannelUID, String> oldValues = new HashMap<>();

    @Nullable
    private volatile PLCLogoClient client; // S7 client used for communication with Logo!
    private final Set<PLCCommonHandler> handlers = new HashSet<>();
    private volatile @NonNullByDefault({}) PLCLogoBridgeConfiguration config;

    @Nullable
    private ScheduledFuture<?> rtcJob;
    private final Runnable rtcReader = new Runnable() {
        private final List<Channel> channels = getThing().getChannels();

        @Override
        public void run() {
            for (final var channel : channels) {
                handleCommand(channel.getUID(), RefreshType.REFRESH);
            }
        }
    };
    private volatile ZonedDateTime rtc = ZonedDateTime.now();

    @Nullable
    private ScheduledFuture<?> readerJob;
    private final Runnable dataReader = new Runnable() {
        // Buffer for block data read operation
        private final byte[] buffer = new byte[2048];

        @Override
        public void run() {
            final var client = PLCBridgeHandler.this.client;
            final var memory = LOGO_MEMORY_BLOCK.get(getLogoFamily());
            final var layout = (memory != null) ? memory.get(MEMORY_SIZE) : null;
            if ((layout != null) && (client != null)) {
                final int result;
                try {
                    result = client.readDBArea(1, 0, layout.length(), S7Client.S7WLByte, buffer);
                } catch (Exception exception) {
                    logger.error("Reader thread got exception: {}.", exception.getMessage());
                    return;
                }
                if (result == 0) {
                    synchronized (handlers) {
                        for (final var handler : handlers) {
                            final var length = handler.getBufferLength();
                            final var address = handler.getStartAddress();
                            if ((length > 0) && (address != PLCCommonHandler.INVALID)) {
                                handler.setData(Arrays.copyOfRange(buffer, address, address + length));
                            } else {
                                logger.debug("Invalid handler {} found.", handler.getClass().getSimpleName());
                            }
                        }
                    }
                } else {
                    logger.debug("Can not read data from LOGO!: {}.", S7Client.ErrorText(result));
                }
            } else {
                logger.debug("Either memory block {} or LOGO! client {} is invalid.", memory, client);
            }
        }
    };

    /**
     * Constructor.
     */
    public PLCBridgeHandler(Bridge bridge) {
        super(bridge);
        config = getConfigAs(PLCLogoBridgeConfiguration.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handle command {} on channel {}", command, channelUID);

        final var thing = getThing();
        if (ThingStatus.ONLINE != thing.getStatus()) {
            return;
        }

        if (!(command instanceof RefreshType)) {
            logger.debug("Not supported command {} received.", command);
            return;
        }

        final var client = this.client;
        final var channelId = channelUID.getId();
        final var layout = LOGO_CHANNELS.get(channelId);
        if ((client != null) && (layout != null)) {
            var buffer = new byte[layout.length()];
            Arrays.fill(buffer, (byte) 0);
            int result = client.readDBArea(1, layout.address(), buffer.length, S7Client.S7WLByte, buffer);
            if (result == 0) {
                switch (channelId) {
                    case RTC_CHANNEL -> {
                        rtc = ZonedDateTime.now();
                        if (!LOGO_0BA7.equalsIgnoreCase(getLogoFamily())) {
                            try {
                                final var year = rtc.getYear() - rtc.getYear() % 100;
                                final var date = LocalDate.of(year + buffer[0], buffer[1], buffer[2]);
                                final var time = LocalTime.of(buffer[3], buffer[4], buffer[5]);
                                rtc = ZonedDateTime.of(date, time, ZoneId.systemDefault());
                            } catch (DateTimeException exception) {
                                logger.info("Return local server time: {}.", exception.getMessage());
                            }
                        }
                        updateState(channelUID, new DateTimeType(rtc));
                    }
                    case DIAGNOSTIC_CHANNEL -> {
                        final var states = LOGO_STATES.get(getLogoFamily());
                        for (final var key : (states != null ? states.keySet() : Set.<Integer> of())) {
                            final var message = states.get(buffer[0] & key);
                            synchronized (oldValues) {
                                if ((message != null) && !Objects.equals(oldValues.get(channelUID), message)) {
                                    updateState(channelUID, new StringType(message));
                                    oldValues.put(channelUID, message);
                                }
                            }
                        }
                    }
                    case DAY_OF_WEEK_CHANNEL -> {
                        final var value = DAY_OF_WEEK.get((int) buffer[0]);
                        synchronized (oldValues) {
                            if ((value != null) && !value.equals(oldValues.get(channelUID))) {
                                updateState(channelUID, new StringType(value));
                                oldValues.put(channelUID, value);
                            }
                        }
                    }
                    default -> logger.info("Invalid channel {} found.", channelUID);
                }

                if (logger.isTraceEnabled()) {
                    final var channel = thing.getChannel(channelId);
                    if (channel == null) {
                        logger.trace("Invalid channel {} found.", channelUID);
                    } else {
                        final String raw = Arrays.toString(buffer);
                        final String type = channel.getAcceptedItemType();
                        logger.trace("Channel {} accepting {} received {}.", channelUID, type, raw);
                    }
                }
            } else {
                logger.debug("Can not read data from LOGO!: {}.", S7Client.ErrorText(result));
            }
        } else {
            logger.info("Invalid channel {} or client {} found.", channelUID, client);
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initialize LOGO! bridge handler.");

        synchronized (oldValues) {
            oldValues.clear();
        }
        config = getConfigAs(PLCLogoBridgeConfiguration.class);

        final var localTSAP = config.getLocalTSAP();
        final var remoteTSAP = config.getRemoteTSAP();
        boolean configured = (localTSAP != null) && (remoteTSAP != null);
        if (configured) {
            var client = this.client;
            if (client == null) {
                client = new PLCLogoClient();
                if (!client.isConnected()) {
                    client.Connect(config.getAddress(), localTSAP, remoteTSAP);
                }
                this.client = client;
            }
            configured = client.isConnected();
        } else {
            String message = "Can not initialize LOGO!. Please, check ip address / TSAP settings.";
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, message);
        }

        if (configured) {
            final var host = config.getAddress();

            var readerJob = this.readerJob;
            if (readerJob == null) {
                Integer interval = config.getRefreshRate();
                logger.info("Creating new reader job for {} with interval {} ms.", host, interval);
                readerJob = scheduler.scheduleWithFixedDelay(dataReader, 100, interval, TimeUnit.MILLISECONDS);
                this.readerJob = readerJob;
            }

            var rtcJob = this.rtcJob;
            if (rtcJob == null) {
                logger.info("Creating new RTC job for {} with interval 1 s.", host);
                rtcJob = scheduler.scheduleAtFixedRate(rtcReader, 100, 1000, TimeUnit.MILLISECONDS);
                this.rtcJob = rtcJob;
            }

            updateStatus(ThingStatus.ONLINE);
        } else {
            String message = "Can not initialize LOGO!. Please, check network connection.";
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
            client = null;
        }
    }

    @Override
    public void dispose() {
        logger.debug("Dispose LOGO! bridge handler.");
        super.dispose();

        final var rtcJob = this.rtcJob;
        if (rtcJob != null) {
            rtcJob.cancel(false);
            logger.info("Destroy RTC job for {}.", config.getAddress());
        }
        this.rtcJob = null;

        final var readerJob = this.readerJob;
        if (readerJob != null) {
            readerJob.cancel(false);
            logger.info("Destroy reader job for {}.", config.getAddress());
        }
        this.readerJob = null;

        final var client = this.client;
        if ((client != null) && client.isConnected()) {
            client.Disconnect();
            if (!client.isConnected()) {
                this.client = null;
            }
        }

        synchronized (oldValues) {
            oldValues.clear();
        }
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        super.childHandlerInitialized(childHandler, childThing);
        if (childHandler instanceof PLCCommonHandler handler) {
            synchronized (handlers) {
                handlers.add(handler);
            }
        }
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof PLCCommonHandler handler) {
            synchronized (handlers) {
                handlers.remove(handler);
            }
        }
        super.childHandlerDisposed(childHandler, childThing);
    }

    /**
     * Returns Siemens LOGO! communication client
     *
     * @return Configured Siemens LOGO! client
     */
    public @Nullable PLCLogoClient getLogoClient() {
        return client;
    }

    /**
     * Returns configured Siemens LOGO! family: 0BA7 or 0BA8
     *
     * @return Configured Siemens LOGO! family
     */
    public String getLogoFamily() {
        return config.getFamily();
    }

    /**
     * Returns RTC was fetched last from Siemens LOGO!
     *
     * @return Siemens LOGO! RTC
     */
    public ZonedDateTime getLogoRTC() {
        return rtc;
    }

    @Override
    protected void updateConfiguration(Configuration configuration) {
        super.updateConfiguration(configuration);
        config = getConfigAs(PLCLogoBridgeConfiguration.class);
    }
}
