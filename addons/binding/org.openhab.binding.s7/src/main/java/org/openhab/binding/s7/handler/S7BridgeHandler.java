/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.s7.handler;

import static org.openhab.binding.s7.S7BindingConstants.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Moka7.S7;
import Moka7.S7Client;

/**
 * The {@link S7Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Laurent Sibilla - Initial contribution
 */
public class S7BridgeHandler extends BaseBridgeHandler {

    private Logger logger = LoggerFactory.getLogger(S7BridgeHandler.class);

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_SERVER);

    private static final int DEFAULT_POLLING_INTERVAL = 200; // in milliseconds

    private boolean lastBridgeConnectionState = false;

    private ScheduledFuture<?> pollingJob;

    private Runnable pollingRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                Refresh();
            } catch (Throwable t) {
                logger.error("An unexpected error occurred: {}", t.getMessage(), t);
            }
        }

        private boolean isReachable(String ipAddress) {
            try {
                // note that InetAddress.isReachable is unreliable, see
                // http://stackoverflow.com/questions/9922543/why-does-inetaddress-isreachable-return-false-when-i-can-ping-the-ip-address
                // That's why we do an HTTP access instead

                // If there is no connection, this line will fail
                client.Connect();
            } catch (Exception e) {
                return false;
            }
            return true;
        }
    };

    private S7Client client = null;

    public S7BridgeHandler(Bridge bridge) {
        super(bridge);
    }

    private long maxRefreshDuration = 0;
    private long totalRefreshDuration = 0;
    private int refreshDurationCount = 0;
    private long nextStateUpdate = System.currentTimeMillis();
    private long startupInhibit;

    protected void Refresh() {
        long startMillis = System.currentTimeMillis();

        if (client != null) {
            if (!client.Connected) {
                if (lastBridgeConnectionState) {
                    onConnectionLost();
                }
            } else if (client.Connected) {
                if (lastBridgeConnectionState) {
                    onConnectionEstablished();
                }

                Hashtable<Integer, byte[]> data = ReadData();

                Bridge bridge = this.getThing();

                for (Thing t : bridge.getThings()) {
                    ThingHandler handler = t.getHandler();

                    if (S7BaseThingHandler.class.isInstance(handler)) {
                        S7BaseThingHandler s7handler = (S7BaseThingHandler) handler;
                        s7handler.ProcessNewData(data);
                    } else if (handler != null) {
                        logger.warn("Unknown thing type in S7 bridge : {}:{}", handler.getClass().getSimpleName(),
                                handler.getClass().getSuperclass().getSimpleName());
                    }
                }
                /*
                 * long refreshDuration = System.currentTimeMillis() - startMillis;
                 * totalRefreshDuration += refreshDuration;
                 * refreshDurationCount++;
                 *
                 * if (nextStateUpdate <= System.currentTimeMillis()) {
                 * nextStateUpdate = Math.max(System.currentTimeMillis(), nextStateUpdate + 5000);
                 *
                 * updateState(CHANNEL_REFRESH_DURATION, new org.eclipse.smarthome.core.library.types.DecimalType(
                 * totalRefreshDuration / refreshDurationCount));
                 *
                 * totalRefreshDuration = 0;
                 * refreshDurationCount = 0;
                 * }
                 *
                 * if (maxRefreshDuration < refreshDuration) {
                 * maxRefreshDuration = refreshDuration;
                 * updateState(CHANNEL_MAX_REFRESH_DURATION,
                 * new org.eclipse.smarthome.core.library.types.DecimalType(maxRefreshDuration));
                 * }
                 */
            }
        }

    }

    @Override
    public void handleRemoval() {
        super.handleRemoval();

        destroyClient();
        destroyPollingJob();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        /*
         * if (channelUID.getId().equals(CHANNEL_LAMP)) {
         * }
         */
    }

    @Override
    public void dispose() {
        destroyPollingJob();
        destroyClient();
    }

    @Override
    public void initialize() {
        logger.info("Initializing S7 bridge handler.");
        createClient();

        if (client != null && client.Connected) {
            createPollingJob();
        } else {
            onConnectionLost();
        }
    }

    private void createClient() {
        if (getConfig().get(HOST) != null) {
            if (client == null) {
                client = new S7Client();
                connectClient();
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Cannot connect to S7 Server. IP address not set.");
        }
    }

    private boolean connectClient() {
        if (client.Connected) {
            return true;
        }

        String hostname = (String) getConfig().get(HOST);
        int localTSAP = ((java.math.BigDecimal) getConfig().get(LOCAL_TSAP)).intValue();
        int remoteTSAP = ((java.math.BigDecimal) getConfig().get(REMOTE_TSAP)).intValue();

        client.SetConnectionParams(hostname, localTSAP, remoteTSAP);

        client.Connect();

        if (client.Connected) {
            onConnectionEstablished();
        }

        return client.Connected;
    }

    private synchronized void onUpdate() {
        destroyClient();
        createClient();

        destroyPollingJob();
        createPollingJob();
    }

    private void destroyPollingJob() {
        if (pollingJob != null) {
            pollingJob.cancel(false);

            while (!pollingJob.isDone()) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }

            pollingJob = null;
        }
    }

    private void destroyClient() {
        if (client != null) {
            client.Disconnect();
            client = null;
        }
    }

    private void createPollingJob() {
        logger.debug("Creating new polling job for S7 server {} with interval {}ms.", getConfig().get(HOST),
                getConfig().get(POLLING_INTERVAL));
        int pollingInterval = DEFAULT_POLLING_INTERVAL;
        try {
            Object pollingIntervalConfig = getConfig().get(POLLING_INTERVAL);
            if (pollingIntervalConfig != null) {
                pollingInterval = ((BigDecimal) pollingIntervalConfig).intValue();
            } else {
                logger.info("Polling interval not configured for this S7 client. Using default value: {}s",
                        pollingInterval);
            }
        } catch (NumberFormatException ex) {
            logger.info("Wrong configuration value for polling interval. Using default value: {}s", pollingInterval);
        }

        startupInhibit = System.currentTimeMillis() + 5000;
        pollingJob = scheduler.scheduleAtFixedRate(pollingRunnable, 1, pollingInterval, TimeUnit.MILLISECONDS);
    }

    private int nReadError = 0;

    private Hashtable<Integer, byte[]> ReadData() {
        final int PALength = 64;
        final int PELength = 64;
        final int DBLength = 96;
        final int MKLength = 32;

        Hashtable<Integer, byte[]> data = new Hashtable<Integer, byte[]>();

        byte[] dataPA = new byte[PALength];
        byte[] dataPE = new byte[PELength];
        byte[] dataDB = new byte[DBLength];
        byte[] dataMK = new byte[MKLength];

        if (client.ReadArea(S7.S7AreaPA, 1, 0, PALength, dataPA) == 0) {
            data.put(S7.S7AreaPA, dataPA);
            nReadError = 0;
        } else {
            logger.error(S7Client.ErrorText(client.LastError));
            nReadError++;
        }

        if (client.ReadArea(S7.S7AreaPE, 1, 0, PELength, dataPE) == 0) {
            data.put(S7.S7AreaPE, dataPE);
            nReadError = 0;
        } else {
            logger.error(S7Client.ErrorText(client.LastError));
            nReadError++;
        }

        if (client.ReadArea(S7.S7AreaDB, 1, 0, DBLength, dataDB) == 0) {
            data.put(S7.S7AreaDB, dataDB);
            nReadError = 0;
        } else {
            logger.error(S7Client.ErrorText(client.LastError));
            nReadError++;
        }

        if (client.ReadArea(S7.S7AreaMK, 1, 0, MKLength, dataMK) == 0) {
            data.put(S7.S7AreaMK, dataMK);
            nReadError = 0;
        } else {
            logger.error(S7Client.ErrorText(client.LastError));
            nReadError++;
        }

        if (nReadError == 20) {
            this.destroyClient();
            this.createClient();
        }

        return data;
    }

    public void setBit(int Area, int Address, boolean value) {
        byte Data[] = new byte[1];

        synchronized (client) {
            client.ReadArea(Area, 1, Address / 8, 1, Data);
            S7.SetBitAt(Data, 0, Address % 8, value);
            client.WriteArea(Area, 1, Address / 8, 1, Data);
        }
    }

    private boolean initializingState = true;
    private boolean runningState = false;

    private void onConnectionEstablished() {
        if (!runningState || initializingState) {
            runningState = true;
            logger.info("Connection to S7 PLC {} established.", getConfig().get(HOST));

            updateStatus(ThingStatus.ONLINE);

            logger.info("S7 bridge {} now online.", getConfig().get(HOST));
        }
    }

    private void onConnectionLost() {
        if (runningState || initializingState) {
            runningState = false;
            logger.info("Connection to S7 PLC {} lost. Trying to reconnect in 5 seconds.", getConfig().get(HOST));
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    S7Client.ErrorText(client.LastError));

            new Thread() {
                @Override
                public void run() {
                    while (!client.Connected) {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (client == null) {
                            createClient();
                        } else if (!client.Connected) {
                            connectClient();
                        }
                    }
                }
            }.start();
        }
    }

    @Override
    public void thingUpdated(Thing thing) {
        // TODO Auto-generated method stub
        super.thingUpdated(thing);
    }
}
