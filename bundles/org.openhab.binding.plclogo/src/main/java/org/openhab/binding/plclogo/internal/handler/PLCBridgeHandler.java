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
package org.openhab.binding.plclogo.internal.handler;

import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.*;

import java.time.DateTimeException;
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
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.Layout;
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
import org.openhab.core.thing.ThingTypeUID;
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

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_DEVICE);

    private final Logger logger = LoggerFactory.getLogger(PLCBridgeHandler.class);

    private Map<ChannelUID, String> oldValues = new HashMap<>();

    @Nullable
    private volatile PLCLogoClient client; // S7 client used for communication with Logo!
    private final Set<PLCCommonHandler> handlers = new HashSet<>();
    private AtomicReference<PLCLogoBridgeConfiguration> config = new AtomicReference<>();

    @Nullable
    private ScheduledFuture<?> rtcJob;
    private AtomicReference<ZonedDateTime> rtc = new AtomicReference<>(ZonedDateTime.now());
    private final Runnable rtcReader = new Runnable() {
        private final List<Channel> channels = getThing().getChannels();

        @Override
        public void run() {
            for (Channel channel : channels) {
                handleCommand(channel.getUID(), RefreshType.REFRESH);
            }
        }
    };

    @Nullable
    private ScheduledFuture<?> readerJob;
    private final Runnable dataReader = new Runnable() {
        // Buffer for block data read operation
        private final byte[] buffer = new byte[2048];

        @Override
        public void run() {
            PLCLogoClient localClient = client;
            Map<?, Layout> memory = LOGO_MEMORY_BLOCK.get(getLogoFamily());
            Layout layout = (memory != null) ? memory.get(MEMORY_SIZE) : null;
            if ((layout != null) && (localClient != null)) {
                try {
                    int result = localClient.readDBArea(1, 0, layout.length, S7Client.S7WLByte, buffer);
                    if (result == 0) {
                        synchronized (handlers) {
                            for (PLCCommonHandler handler : handlers) {
                                int length = handler.getBufferLength();
                                int address = handler.getStartAddress();
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
                } catch (Exception exception) {
                    logger.error("Reader thread got exception: {}.", exception.getMessage());
                }
            } else {
                logger.debug("Either memory block {} or LOGO! client {} is invalid.", memory, localClient);
            }
        }
    };

    /**
     * Constructor.
     */
    public PLCBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handle command {} on channel {}", command, channelUID);

        Thing thing = getThing();
        if (ThingStatus.ONLINE != thing.getStatus()) {
            return;
        }

        if (!(command instanceof RefreshType)) {
            logger.debug("Not supported command {} received.", command);
            return;
        }

        PLCLogoClient localClient = client;
        String channelId = channelUID.getId();
        Channel channel = thing.getChannel(channelId);
        Layout layout = LOGO_CHANNELS.get(channelId);
        if ((localClient != null) && (channel != null) && (layout != null)) {
            byte[] buffer = new byte[layout.length];
            Arrays.fill(buffer, (byte) 0);
            int result = localClient.readDBArea(1, layout.address, buffer.length, S7Client.S7WLByte, buffer);
            if (result == 0) {
                if (RTC_CHANNEL.equals(channelId)) {
                    ZonedDateTime clock = ZonedDateTime.now();
                    if (!LOGO_0BA7.equalsIgnoreCase(getLogoFamily())) {
                        try {
                            int year = clock.getYear() / 100;
                            clock = clock.withYear(100 * year + buffer[0]);
                            clock = clock.withMonth(buffer[1]);
                            clock = clock.withDayOfMonth(buffer[2]);
                            clock = clock.withHour(buffer[3]);
                            clock = clock.withMinute(buffer[4]);
                            clock = clock.withSecond(buffer[5]);
                        } catch (DateTimeException exception) {
                            clock = ZonedDateTime.now();
                            logger.info("Return local server time: {}.", exception.getMessage());
                        }
                    }
                    rtc.set(clock);
                    updateState(channelUID, new DateTimeType(clock));
                } else if (DAIGNOSTICS_CHANNEL.equals(channelId)) {
                    Map<Integer, String> states = LOGO_STATES.get(getLogoFamily());
                    if (states != null) {
                        for (Integer key : states.keySet()) {
                            String message = states.get(buffer[0] & key.intValue());
                            synchronized (oldValues) {
                                if (message != null && !Objects.equals(oldValues.get(channelUID), message)) {
                                    updateState(channelUID, new StringType(message));
                                    oldValues.put(channelUID, message);
                                }
                            }
                        }
                    }
                } else if (DAY_OF_WEEK_CHANNEL.equals(channelId)) {
                    String value = DAY_OF_WEEK.get(Integer.valueOf(buffer[0]));
                    synchronized (oldValues) {
                        if (value != null && !Objects.equals(oldValues.get(channelUID), value)) {
                            updateState(channelUID, new StringType(value));
                            oldValues.put(channelUID, value);
                        }
                    }
                } else {
                    logger.info("Invalid channel {} or client {} found.", channelUID, client);
                }

                if (logger.isTraceEnabled()) {
                    String raw = Arrays.toString(buffer);
                    String type = channel.getAcceptedItemType();
                    logger.trace("Channel {} accepting {} received {}.", channelUID, type, raw);
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
        config.set(getConfigAs(PLCLogoBridgeConfiguration.class));

        boolean configured = (config.get().getLocalTSAP() != null);
        configured = configured && (config.get().getRemoteTSAP() != null);

        if (configured) {
            if (client == null) {
                client = new PLCLogoClient();
            }
            configured = connect();
        } else {
            String message = "Can not initialize LOGO!. Please, check ip address / TSAP settings.";
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, message);
        }

        if (configured) {
            String host = config.get().getAddress();
            if (readerJob == null) {
                Integer interval = config.get().getRefreshRate();
                logger.info("Creating new reader job for {} with interval {} ms.", host, interval);
                readerJob = scheduler.scheduleWithFixedDelay(dataReader, 100, interval, TimeUnit.MILLISECONDS);
            }
            if (rtcJob == null) {
                logger.info("Creating new RTC job for {} with interval 1 s.", host);
                rtcJob = scheduler.scheduleAtFixedRate(rtcReader, 100, 1000, TimeUnit.MILLISECONDS);
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

        if (rtcJob != null) {
            rtcJob.cancel(false);
            rtcJob = null;
            logger.info("Destroy RTC job for {}.", config.get().getAddress());
        }

        if (readerJob != null) {
            readerJob.cancel(false);
            readerJob = null;
            logger.info("Destroy reader job for {}.", config.get().getAddress());
        }

        if (disconnect()) {
            client = null;
        }

        synchronized (oldValues) {
            oldValues.clear();
        }
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        super.childHandlerInitialized(childHandler, childThing);
        if (childHandler instanceof PLCCommonHandler plcCommonHandler) {
            synchronized (handlers) {
                if (!handlers.contains(plcCommonHandler)) {
                    handlers.add(plcCommonHandler);
                }
            }
        }
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof PLCCommonHandler plcCommonHandler) {
            synchronized (handlers) {
                if (handlers.contains(plcCommonHandler)) {
                    handlers.remove(plcCommonHandler);
                }
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
        return config.get().getFamily();
    }

    /**
     * Returns RTC was fetched last from Siemens LOGO!
     *
     * @return Siemens LOGO! RTC
     */
    public ZonedDateTime getLogoRTC() {
        return rtc.get();
    }

    @Override
    protected void updateConfiguration(Configuration configuration) {
        super.updateConfiguration(configuration);
        config.set(getConfigAs(PLCLogoBridgeConfiguration.class));
    }

    /**
     * Read connection parameter and connect to Siemens LOGO!
     *
     * @return True, if connected and false otherwise
     */
    private boolean connect() {
        boolean result = false;

        PLCLogoClient localClient = client;
        if (localClient != null) {
            Integer local = config.get().getLocalTSAP();
            Integer remote = config.get().getRemoteTSAP();
            if (!localClient.isConnected() && (local != null) && (remote != null)) {
                localClient.Connect(config.get().getAddress(), local.intValue(), remote.intValue());
            }
            result = localClient.isConnected();
        }
        return result;
    }

    /**
     * Disconnect from Siemens LOGO!
     *
     * @return True, if disconnected and false otherwise
     */
    private boolean disconnect() {
        boolean result = false;

        PLCLogoClient localClient = client;
        if (localClient != null) {
            if (localClient.isConnected()) {
                localClient.Disconnect();
            }
            result = !localClient.isConnected();
        }

        return result;
    }
}
