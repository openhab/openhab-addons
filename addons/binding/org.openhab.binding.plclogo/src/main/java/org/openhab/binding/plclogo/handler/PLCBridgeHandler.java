/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plclogo.handler;

import static org.openhab.binding.plclogo.PLCLogoBindingConstants.*;

import java.time.DateTimeException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.plclogo.PLCLogoBindingConstants.Layout;
import org.openhab.binding.plclogo.internal.PLCLogoClient;
import org.openhab.binding.plclogo.internal.config.PLCLogoBridgeConfiguration;
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

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_DEVICE);

    private static final Map<Integer, @Nullable String> logged = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(PLCBridgeHandler.class);

    @Nullable
    private volatile PLCLogoClient client; // S7 client used for communication with Logo!
    private final Set<PLCCommonHandler> handlers = new HashSet<>();
    private AtomicReference<PLCLogoBridgeConfiguration> config = new AtomicReference<>();

    @Nullable
    private ScheduledFuture<?> rtcJob;
    private AtomicReference<ZonedDateTime> rtc = new AtomicReference<>(ZonedDateTime.now());
    private final Runnable rtcReader = new Runnable() {
        private final @Nullable Channel channel = getThing().getChannel(RTC_CHANNEL);

        @Override
        public void run() {
            if (channel != null) {
                handleCommand(channel.getUID(), RefreshType.REFRESH);
            } else {
                logger.warn("Can not update channel {}: {}.", channel, client);
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
            Layout memory = LOGO_MEMORY_BLOCK.get(getLogoFamily()).get("SIZE");
            if ((memory != null) && (client != null)) {
                try {
                    int result = client.readDBArea(1, 0, memory.length, S7Client.S7WLByte, buffer);
                    if (result == 0) {
                        synchronized (handlers) {
                            for (PLCCommonHandler handler : handlers) {
                                if (handler == null) {
                                    logger.warn("Skip processing of invalid handler.");
                                    continue;
                                }

                                int length = handler.getBufferLength();
                                int address = handler.getStartAddress();
                                if ((length > 0) && (address != PLCCommonHandler.INVALID)) {
                                    handler.setData(Arrays.copyOfRange(buffer, address, address + length));
                                } else {
                                    logger.warn("Invalid handler {} found.", handler.getClass().getSimpleName());
                                }
                            }
                        }
                    } else {
                        logger.warn("Can not read data from LOGO!: {}.", S7Client.ErrorText(result));
                    }
                } catch (Exception exception) {
                    logger.error("Reader thread got exception: {}.", exception.getMessage());
                }
            } else {
                logger.warn("Either memory block {} or LOGO! client {} is invalid.", memory, client);
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
        Objects.requireNonNull(thing, "PLCBridgeHandler: Thing may not be null.");
        if (ThingStatus.ONLINE != thing.getStatus()) {
            return;
        }

        String channelId = channelUID.getId();
        if (!RTC_CHANNEL.equals(channelId) || (client == null)) {
            logger.warn("Can not update channel {}: {}.", channelUID, client);
            return;
        }

        if (command instanceof RefreshType) {
            byte[] buffer = { 0, 0, 0, 0, 0, 0, 0 };
            int result = client.readDBArea(1, LOGO_STATE.intValue(), buffer.length, S7Client.S7WLByte, buffer);
            if (result == 0) {
                ZonedDateTime clock = ZonedDateTime.now();
                if (!LOGO_0BA7.equalsIgnoreCase(getLogoFamily())) {
                    try {
                        int year = clock.getYear() / 100;
                        clock = clock.withYear(100 * year + buffer[1]);
                        clock = clock.withMonth(buffer[2]);
                        clock = clock.withDayOfMonth(buffer[3]);
                        clock = clock.withHour(buffer[4]);
                        clock = clock.withMinute(buffer[5]);
                        clock = clock.withSecond(buffer[6]);
                    } catch (DateTimeException exception) {
                        clock = ZonedDateTime.now();
                        logger.warn("Return local server time: {}.", exception.getMessage());
                    }
                }
                rtc.set(clock);
                updateState(channelUID, new DateTimeType(clock));

                Map<Integer, @Nullable String> states = LOGO_STATES.get(getLogoFamily());
                for (Integer key : states.keySet()) {
                    int code = buffer[0] & key.intValue();
                    if ((code == key.intValue()) && !logged.containsKey(key)) {
                        String message = states.get(key);
                        if (message != null) {
                            logged.put(code, message);
                            logger.info("LOGO! diagnostics returned: {}.", message);
                        }
                    }
                }

                Channel channel = thing.getChannel(channelId);
                if (logger.isTraceEnabled() && (channel != null)) {
                    String raw = Arrays.toString(buffer);
                    String type = channel.getAcceptedItemType();
                    logger.trace("Channel {} accepting {} received {}.", channelUID, type, raw);
                }
            } else {
                logger.warn("Can not read data from LOGO!: {}.", S7Client.ErrorText(result));
            }
        } else {
            logger.debug("Not supported command {} received.", command);
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initialize LOGO! bridge handler.");

        Thing thing = getThing();
        Objects.requireNonNull(thing, "PLCBridgeHandler: Thing may not be null.");

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
            try {
                Thread.sleep(100);
            } catch (InterruptedException exception) {
                logger.debug("Dispose of RTC job throw an exception: {}.", exception.getMessage());
            }
            rtcJob = null;
            logger.info("Destroy RTC job for {}.", config.get().getAddress());
        }

        if (readerJob != null) {
            readerJob.cancel(false);
            try {
                Thread.sleep(100);
            } catch (InterruptedException exception) {
                logger.debug("Dispose of reader job throw an exception: {}.", exception.getMessage());
            }
            readerJob = null;
            logger.info("Destroy reader job for {}.", config.get().getAddress());
        }

        if (disconnect()) {
            client = null;
        }
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        super.childHandlerInitialized(childHandler, childThing);
        if (childHandler instanceof PLCCommonHandler) {
            PLCCommonHandler handler = (PLCCommonHandler) childHandler;
            synchronized (handlers) {
                if (!handlers.contains(handler)) {
                    handlers.add(handler);
                }
            }
        }
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof PLCCommonHandler) {
            PLCCommonHandler handler = (PLCCommonHandler) childHandler;
            synchronized (handlers) {
                if (handlers.contains(handler)) {
                    handlers.remove(handler);
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
        if (client != null) {
            Integer local = config.get().getLocalTSAP();
            Integer remote = config.get().getRemoteTSAP();
            if (!client.isConnected() && (local != null) && (remote != null)) {
                client.Connect(config.get().getAddress(), local.intValue(), remote.intValue());
            }
            result = client.isConnected();
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
        if (client != null) {
            if (client.isConnected()) {
                client.Disconnect();
            }
            result = !client.isConnected();
        }
        return result;
    }

}
