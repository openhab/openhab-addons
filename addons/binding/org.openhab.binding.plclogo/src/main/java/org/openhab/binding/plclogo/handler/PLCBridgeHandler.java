/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plclogo.handler;

import static org.openhab.binding.plclogo.PLCLogoBindingConstants.*;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
import org.openhab.binding.plclogo.config.PLCLogoBridgeConfiguration;
import org.openhab.binding.plclogo.internal.PLCLogoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Moka7.S7Client;

/**
 * The {@link PLCBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alexander Falkenstern - Initial contribution
 */
public class PLCBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(PLCBridgeHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_DEVICE);

    // S7 client this bridge belongs to
    private volatile PLCLogoClient client;
    private Set<PLCBlockHandler> handlers = new HashSet<>();
    private PLCLogoBridgeConfiguration config = getConfigAs(PLCLogoBridgeConfiguration.class);

    private Calendar rtc = Calendar.getInstance();
    private ScheduledFuture<?> rtcJob;
    private final Runnable rtcReader = new Runnable() {
        // Buffer for diagnostic data
        private final byte[] data = { 0, 0, 0, 0, 0, 0, 0 };

        @Override
        public void run() {
            try {
                if (client != null) {
                    int result = client.readDBArea(1, LOGO_STATE.intValue(), data.length, S7Client.S7WLByte, data);
                    if (result == 0) {
                        final Calendar calendar = getRtcAt(data, 1);
                        synchronized (rtc) {
                            rtc.setTimeZone(calendar.getTimeZone());
                            rtc.setTimeInMillis(calendar.getTimeInMillis());
                        }
                        final Channel channel = thing.getChannel(RTC_CHANNEL_ID);
                        updateState(channel.getUID(), new DateTimeType(rtc));

                        if (logger.isTraceEnabled()) {
                            final String raw = Arrays.toString(data);
                            final String type = channel.getAcceptedItemType();
                            logger.trace("Channel {} accepting {} received {}.", channel.getUID(), type, raw);
                        }
                    } else {
                        logger.warn("Can not read diagnostics from LOGO!: {}.", S7Client.ErrorText(result));
                    }
                } else {
                    logger.warn("LOGO! client {} is invalid.", client);
                }
            } catch (Exception exception) {
                logger.error("RTC thread got exception: {}.", exception.getMessage());
            } catch (Error error) {
                logger.error("RTC thread got error: {}.", error.getMessage());
                throw error;
            }
        }
    };

    private ScheduledFuture<?> readerJob = null;
    private final Runnable dataReader = new Runnable() {
        // Buffer for block data read operation
        private final byte[] buffer = new byte[2048];

        @Override
        public void run() {
            try {
                final Map<?, Integer> memory = LOGO_MEMORY_BLOCK.get(getLogoFamily());
                if ((memory != null) && (client != null)) {
                    final Integer size = memory.get("SIZE");
                    int result = client.readDBArea(1, 0, size.intValue(), S7Client.S7WLByte, buffer);
                    if (result == 0) {
                        synchronized (handlers) {
                            for (PLCBlockHandler handler : handlers) {
                                if (handler == null) {
                                    logger.warn("Skip processing of invalid handler.");
                                    continue;
                                }

                                final int address = handler.getAddress();
                                final int offset = handler.getBlockDataType().getByteCount();
                                if ((offset > 0) && (address != PLCBlockHandler.INVALID)) {
                                    handler.setData(Arrays.copyOfRange(buffer, address, address + offset));
                                } else {
                                    logger.warn("Invalid handler {} found.", handler.getClass().getSimpleName());
                                }
                            }
                        }
                    } else {
                        logger.warn("Can not read data from LOGO!: {}.", S7Client.ErrorText(result));
                    }
                } else {
                    logger.warn("Either memory block {} or LOGO! client {} is invalid.", memory, client);
                }
            } catch (Exception exception) {
                logger.error("Reader thread got exception: {}.", exception.getMessage());
            } catch (Error error) {
                logger.error("Reader thread got error: {}.", error.getMessage());
                throw error;
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

        final Thing thing = getThing();
        Objects.requireNonNull(thing, "PLCBridgeHandler: Thing may not be null.");
        if (ThingStatus.ONLINE != thing.getStatus()) {
            return;
        }

        final String channelId = channelUID.getId();
        final PLCLogoClient client = getLogoClient();
        if (!RTC_CHANNEL_ID.equals(channelId) || (client == null)) {
            logger.warn("Can not update channel {}: {}.", channelUID, client);
            return;
        }

        if (command instanceof RefreshType) {
            byte[] buffer = { 0, 0, 0, 0, 0, 0, 0 };
            int result = client.readDBArea(1, LOGO_STATE.intValue(), buffer.length, S7Client.S7WLByte, buffer);
            if (result == 0) {
                final Calendar calendar = getRtcAt(buffer, 1);
                synchronized (rtc) {
                    rtc.setTimeZone(calendar.getTimeZone());
                    rtc.setTimeInMillis(calendar.getTimeInMillis());
                }
                updateState(channelUID, new DateTimeType(rtc));
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

        final Thing thing = getThing();
        Objects.requireNonNull(thing, "PLCBridgeHandler: Thing may not be null.");

        synchronized (config) {
            config = getConfigAs(PLCLogoBridgeConfiguration.class);
        }

        boolean configured = (config.getAddress() != null);
        configured = configured && (config.getLocalTSAP() != null);
        configured = configured && (config.getRemoteTSAP() != null);

        if (configured) {
            if (client == null) {
                client = new PLCLogoClient();
            }
            configured = connect();
        } else {
            final String message = "Can not initialize LOGO!. Please, check ip address / TSAP settings.";
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, message);
        }

        if (configured) {
            final String host = config.getAddress();
            if (readerJob == null) {
                final Integer interval = config.getRefreshRate();
                logger.info("Creating new reader job for {} with interval {} ms.", host, interval);
                readerJob = scheduler.scheduleWithFixedDelay(dataReader, 100, interval, TimeUnit.MILLISECONDS);
            }
            if (rtcJob == null) {
                logger.info("Creating new RTC job for {} with interval 500 ms.", host);
                rtcJob = scheduler.scheduleWithFixedDelay(rtcReader, 100, 500, TimeUnit.MILLISECONDS);
            }

            PLCBridgeHandler.super.initialize();
        } else {
            final String message = "Can not initialize LOGO!. Please, check network connection.";
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
            while (!rtcJob.isDone()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException exception) {
                    logger.debug("Dispose of RTC job throw an exception: {}.", exception.getMessage());
                    break;
                }
            }
            rtcJob = null;
            logger.info("Destroy RTC job for {}.", config.getAddress());
        }

        if (readerJob != null) {
            readerJob.cancel(false);
            while (!readerJob.isDone()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException exception) {
                    logger.debug("Dispose of reader job throw an exception: {}.", exception.getMessage());
                    break;
                }
            }
            readerJob = null;
            logger.info("Destroy reader job for {}.", config.getAddress());
        }

        if (disconnect()) {
            client = null;
        }
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        super.childHandlerInitialized(childHandler, childThing);
        if (childHandler instanceof PLCBlockHandler) {
            final PLCBlockHandler handler = (PLCBlockHandler) childHandler;
            synchronized (handlers) {
                final String name = handler.getBlockName();
                if (!handlers.contains(handler)) {
                    handlers.add(handler);
                    logger.debug("Insert handler for block {}.", name);
                } else {
                    logger.info("Handler {} for block {} already registered.", childThing.getUID(), name);
                }
            }
        }
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof PLCBlockHandler) {
            final PLCBlockHandler handler = (PLCBlockHandler) childHandler;
            synchronized (handlers) {
                final String name = handler.getBlockName();
                if (handlers.contains(handler)) {
                    handlers.remove(handler);
                    logger.debug("Remove handler for block {}.", name);
                } else {
                    logger.info("Handler {} for block {} already disposed.", childThing.getUID(), name);
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
    public PLCLogoClient getLogoClient() {
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
    public Calendar getLogoRTC() {
        return rtc;
    }

    @Override
    protected void updateConfiguration(Configuration configuration) {
        super.updateConfiguration(configuration);
        synchronized (config) {
            config = getConfigAs(PLCLogoBridgeConfiguration.class);
        }
    }

    /**
     * Read connection parameter and connect to Siemens LOGO!
     *
     * @return True, if connected and false otherwise
     */
    private boolean connect() {
        boolean result = false;
        if (client != null) {
            if (!client.isConnected()) {
                final String host = config.getAddress();
                final Integer local = config.getLocalTSAP();
                final Integer remote = config.getRemoteTSAP();

                if ((host != null) && (local != null) && (remote != null)) {
                    client.Connect(host, local.intValue(), remote.intValue());
                }
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

    private Calendar getRtcAt(byte[] buffer, int pos) {
        Calendar calendar = Calendar.getInstance();
        if (!LOGO_0BA7.equalsIgnoreCase(getLogoFamily())) {
            final int year = calendar.get(Calendar.YEAR) / 100;
            calendar.set(Calendar.YEAR, 100 * year + buffer[pos]);
            calendar.set(Calendar.MONTH, buffer[pos + 1] - 1);
            calendar.set(Calendar.DAY_OF_MONTH, buffer[pos + 2]);
            calendar.set(Calendar.HOUR_OF_DAY, buffer[pos + 3]);
            calendar.set(Calendar.MINUTE, buffer[pos + 4]);
            calendar.set(Calendar.SECOND, buffer[pos + 5]);
            calendar.set(Calendar.MILLISECOND, 0);
        }
        return calendar;
    }

}
