/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.DATE_TIME_CHANNEL;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.DAY_OF_WEEK;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.DAY_OF_WEEK_CHANNEL;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.DIAGNOSTIC_CHANNEL;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.LOGO_0BA7;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.LOGO_CHANNELS;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.LOGO_MEMORY_BLOCK;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.LOGO_STATES;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.MEMORY_SIZE;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.RTC_CHANNEL;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.plclogo.internal.PLCLogoClient;
import org.openhab.binding.plclogo.internal.config.PLCLogoBridgeConfiguration;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.i18n.TranslationProvider;
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
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Moka7.S7;
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

    private final Bundle bundle = FrameworkUtil.getBundle(getClass());
    private final TimeZoneProvider timeZone;
    private final TranslationProvider translation;

    private volatile @Nullable PLCLogoClient client; // S7 client used for communication with Logo!
    private final Set<PLCCommonHandler> handlers = new HashSet<>();
    private volatile @NonNullByDefault({}) PLCLogoBridgeConfiguration config;

    private @Nullable ScheduledFuture<?> rtcJob;
    private final Runnable rtcReader = new Runnable() {
        private final ChannelUID channel = new ChannelUID(getThing().getUID(), RTC_CHANNEL);

        @Override
        public void run() {
            handleCommand(channel, RefreshType.REFRESH);
        }
    };
    private volatile ZonedDateTime rtc;

    private @Nullable ScheduledFuture<?> readerJob;
    private final Runnable dataReader = new Runnable() {
        // Buffer for block data read operation
        private final byte[] buffer = new byte[2048];
        private final List<Channel> channels = getThing().getChannels();

        @Override
        public void run() {
            for (final var channel : channels) {
                // RTC channel is updated in rtcJob
                if (!RTC_CHANNEL.equalsIgnoreCase(channel.getUID().getId())) {
                    handleCommand(channel.getUID(), RefreshType.REFRESH);
                }
            }

            final var client = PLCBridgeHandler.this.client;
            final var memory = LOGO_MEMORY_BLOCK.get(getLogoFamily());
            final var layout = (memory != null) ? memory.get(MEMORY_SIZE) : null;
            if ((layout != null) && (client != null)) {
                final int result;
                try {
                    result = client.readBytes(0, layout.length(), buffer);
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
    public PLCBridgeHandler(final Bridge bridge, final TranslationProvider translation,
            final TimeZoneProvider timeZone) {
        super(bridge);
        this.timeZone = timeZone;
        this.translation = translation;
        config = getConfigAs(PLCLogoBridgeConfiguration.class);
        rtc = ZonedDateTime.now(timeZone.getTimeZone());
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
        final var layout = LOGO_CHANNELS.get(switch (channelId) {
            case DAY_OF_WEEK_CHANNEL -> DATE_TIME_CHANNEL;
            default -> channelId;
        });
        if ((client != null) && (layout != null)) {
            var buffer = new byte[layout.length()];
            Arrays.fill(buffer, (byte) 0);
            int result = client.readBytes(layout.address(), buffer.length, buffer);
            if (result == 0) {
                switch (channelId) {
                    case RTC_CHANNEL -> {
                        rtc = ZonedDateTime.now(timeZone.getTimeZone());
                        if (!LOGO_0BA7.equalsIgnoreCase(getLogoFamily())) {
                            for (int i = 0; i < buffer.length; i++) {
                                buffer[i] = S7.ByteToBCD(buffer[i]);
                            }
                            rtc = getDateTime(buffer);
                        }
                        updateState(channelUID, new DateTimeType(rtc));
                    }
                    case DIAGNOSTIC_CHANNEL -> {
                        final var states = LOGO_STATES.get(getLogoFamily());
                        if (states != null) {
                            final var stateText = Integer.toBinaryString(buffer[0]);
                            var message = translation.getText(bundle, states.get((int) buffer[0]), stateText, null);
                            for (var bit = 0; bit < Integer.SIZE; ++bit) {
                                if (((buffer[0] >>> bit) & 1) != 0) {
                                    message = translation.getText(bundle, states.get(1 << bit), stateText, null);
                                }
                            }
                            if (message == null) {
                                message = String.format("Unknown diagnostic bit is set in bitmask %s", stateText);
                            }
                            updateState(channelUID, new StringType(message));
                        } else {
                            updateState(channelUID, new StringType("LOGO! family is not supported"));
                        }
                    }
                    case DATE_TIME_CHANNEL -> {
                        updateState(channelUID, new DateTimeType(getDateTime(buffer)));
                    }
                    case DAY_OF_WEEK_CHANNEL -> {
                        var value = DAY_OF_WEEK.get(S7.BCDtoByte(buffer[7]));
                        if (value == null) {
                            value = String.format("Unknown day of week value %d received", S7.BCDtoByte(buffer[7]));
                        }
                        updateState(channelUID, new StringType(value));
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

        config = getConfigAs(PLCLogoBridgeConfiguration.class);
        final var address = config.getAddress();

        var client = this.client;
        if (client == null) {
            client = new PLCLogoClient();
            if (!client.isConnected()) {
                final var localTSAP = config.getLocalTSAP();
                final var remoteTSAP = config.getRemoteTSAP();
                if ((localTSAP != null) && (remoteTSAP != null)) {
                    final var result = client.Connect(address, localTSAP, remoteTSAP);
                    if (result != 0) {
                        String message = String.format("Can not initialize LOGO!. %s.", S7Client.ErrorText(result));
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
                    }
                } else {
                    String message = "Can not initialize LOGO!. Please, check ip address / TSAP settings.";
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, message);
                }
            }
        }

        if (client.isConnected()) {
            var readerJob = this.readerJob;
            if (readerJob == null) {
                final var interval = config.getRefreshRate();
                logger.info("Creating new reader job for {} with interval {} ms.", address, interval);
                readerJob = scheduler.scheduleWithFixedDelay(dataReader, 100, interval, TimeUnit.MILLISECONDS);
                this.readerJob = readerJob;
            }

            var rtcJob = this.rtcJob;
            if (rtcJob == null) {
                logger.info("Creating new RTC job for {} with interval 1 s.", address);
                rtcJob = scheduler.scheduleAtFixedRate(rtcReader, 100, 1000, TimeUnit.MILLISECONDS);
                this.rtcJob = rtcJob;
            }

            this.client = client;
            updateStatus(ThingStatus.ONLINE);
        } else {
            if (ThingStatus.OFFLINE != getThing().getStatus()) {
                String message = "Can not initialize LOGO!. Please, check network connection.";
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
            }
            this.client = null;
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
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof PLCCommonHandler handler) {
            super.childHandlerInitialized(handler, childThing);
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
            super.childHandlerDisposed(handler, childThing);
        }
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

    private ZonedDateTime getDateTime(final byte[] buffer) {
        ZonedDateTime result = ZonedDateTime.now(timeZone.getTimeZone());

        if (buffer.length >= 6) {
            final var calendar = new GregorianCalendar();
            final var year = result.getYear() - result.getYear() % 100;
            calendar.set(Calendar.YEAR, year + S7.BCDtoByte(buffer[0]));
            calendar.set(Calendar.MONTH, S7.BCDtoByte(buffer[1]) - 1);
            calendar.set(Calendar.DATE, S7.BCDtoByte(buffer[2]));
            calendar.set(Calendar.HOUR_OF_DAY, S7.BCDtoByte(buffer[3]));
            calendar.set(Calendar.MINUTE, S7.BCDtoByte(buffer[4]));
            calendar.set(Calendar.SECOND, S7.BCDtoByte(buffer[5]));
            calendar.set(Calendar.MILLISECOND, 0);
            result = calendar.toZonedDateTime();
        } else {
            logger.warn("Return local server time: {}.", "Not enough fields provided");
        }

        return result;
    }
}
