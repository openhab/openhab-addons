/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.handler;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.knx.GroupAddressListener;
import org.openhab.binding.knx.IndividualAddressListener;
import org.openhab.binding.knx.KNXBindingConstants;
import org.openhab.binding.knx.KNXBridgeListener;
import org.openhab.binding.knx.discovery.KNXBusListener;
import org.openhab.binding.knx.internal.dpt.KNXCoreTypeMapper;
import org.openhab.binding.knx.internal.dpt.KNXTypeMapper;
import org.openhab.binding.knx.internal.logging.LogAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.CloseEvent;
import tuwien.auto.calimero.DetachEvent;
import tuwien.auto.calimero.FrameEvent;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.cemi.CEMILData;
import tuwien.auto.calimero.datapoint.CommandDP;
import tuwien.auto.calimero.datapoint.Datapoint;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.exception.KNXFormatException;
import tuwien.auto.calimero.exception.KNXIllegalArgumentException;
import tuwien.auto.calimero.exception.KNXRemoteException;
import tuwien.auto.calimero.exception.KNXTimeoutException;
import tuwien.auto.calimero.link.KNXLinkClosedException;
import tuwien.auto.calimero.link.KNXNetworkLink;
import tuwien.auto.calimero.link.NetworkLinkListener;
import tuwien.auto.calimero.log.LogManager;
import tuwien.auto.calimero.mgmt.Destination;
import tuwien.auto.calimero.mgmt.KNXDisconnectException;
import tuwien.auto.calimero.mgmt.ManagementClient;
import tuwien.auto.calimero.mgmt.ManagementClientImpl;
import tuwien.auto.calimero.mgmt.ManagementProcedures;
import tuwien.auto.calimero.mgmt.ManagementProceduresImpl;
import tuwien.auto.calimero.process.ProcessCommunicator;
import tuwien.auto.calimero.process.ProcessCommunicatorImpl;
import tuwien.auto.calimero.process.ProcessEvent;
import tuwien.auto.calimero.process.ProcessListenerEx;

/**
 * The {@link KNXBridgeBaseThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Kai Kreuzer / Karel Goderis - Initial contribution
 */
public abstract class KNXBridgeBaseThingHandler extends BaseThingHandler implements GroupAddressListener {

    // List of all Configuration parameters
    public static final String AUTO_RECONNECT_PERIOD = "autoReconnectPeriod";
    public static final String RESPONSE_TIME_OUT = "responseTimeOut";
    public static final String READ_RETRIES_LIMIT = "readRetriesLimit";
    public static final String READING_PAUSE = "readingPause";
    public final static String DPT = "dpt";
    public final static String INCREASE_DECREASE_DPT = "increasedecreaseDPT";
    public final static String PERCENT_DPT = "percentDPT";
    public final static String ADDRESS = "address";
    public final static String AUTO_UPDATE = "autoupdate";
    public final static String STATE_ADDRESS = "stateGA";
    public final static String INCREASE_DECREASE_ADDRESS = "increasedecreaseGA";
    public final static String PERCENT_ADDRESS = "percentGA";
    public final static String READ = "read";
    public final static String INTERVAL = "interval";

    public final static int ERROR_INTERVAL_MINUTES = 5;

    protected Logger logger = LoggerFactory.getLogger(KNXBridgeBaseThingHandler.class);

    private List<GroupAddressListener> groupAddressListeners = new CopyOnWriteArrayList<>();
    private List<IndividualAddressListener> individualAddressListeners = new CopyOnWriteArrayList<>();
    private List<KNXBridgeListener> knxBridgeListeners = new CopyOnWriteArrayList<>();
    private List<KNXBusListener> knxBusListeners = new CopyOnWriteArrayList<>();
    static protected Collection<KNXTypeMapper> typeMappers = new HashSet<KNXTypeMapper>();
    private LinkedBlockingQueue<RetryDatapoint> readDatapoints = new LinkedBlockingQueue<RetryDatapoint>();
    protected ConcurrentHashMap<IndividualAddress, Destination> destinations = new ConcurrentHashMap<IndividualAddress, Destination>();
    private List<ChannelUID> autoUpdateChannels = new CopyOnWriteArrayList<>();

    protected ItemChannelLinkRegistry itemChannelLinkRegistry;
    private ProcessCommunicator pc = null;
    private ProcessListenerEx pl = null;
    private NetworkLinkListener nll = null;
    private ManagementProcedures mp;
    private ManagementClient mc;
    protected KNXNetworkLink link;
    private final LogAdapter logAdapter = new LogAdapter();

    private ScheduledFuture<?> reconnectJob;
    private ScheduledFuture<?> busJob;
    private List<ScheduledFuture<?>> readJobs;

    // signals that the connection is shut down on purpose
    public boolean shutdown = false;
    private long intervalTimestamp;
    private long errorsSinceStart;
    private long errorsSinceInterval;

    public KNXBridgeBaseThingHandler(Thing thing, ItemChannelLinkRegistry itemChannelLinkRegistry) {
        super(thing);
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
    }

    @Override
    public void initialize() {

        // register ourselves as a Group Address Listener
        registerGroupAddressListener(this);

        // reset the counters
        errorsSinceStart = 0;
        errorsSinceInterval = 0;

        readJobs = new ArrayList<ScheduledFuture<?>>();

        LogManager.getManager().addWriter(null, logAdapter);
        logger.trace("Connecting bridge");
        connect();
    }

    @Override
    public void dispose() {

        if (reconnectJob != null) {
            reconnectJob.cancel(true);
        }

        disconnect();

        // unregister ourselves as a Group Address Status Listener
        unregisterGroupAddressListener(this);

        LogManager.getManager().removeWriter(null, logAdapter);
    }

    /**
     * Returns the KNXNetworkLink for talking to the KNX bus.
     * The link can be null, if it has not (yet) been established successfully.
     *
     * @return the KNX network link
     */
    public synchronized ProcessCommunicator getCommunicator() {
        if (link != null && !link.isOpen()) {
            connect();
        }
        return pc;
    }

    public int getReadRetriesLimit() {
        return ((BigDecimal) getConfig().get(READ_RETRIES_LIMIT)).intValue();
    }

    public abstract void establishConnection() throws KNXException;

    public synchronized void connect() {
        try {
            shutdown = false;

            if (mp != null) {
                mp.detach();
            }

            if (mc != null) {
                mc.detach();
            }

            if (pc != null) {
                if (pl != null) {
                    pc.removeProcessListener(pl);
                }
                pc.detach();
            }

            if (link != null && link.isOpen()) {
                link.close();
            }

            establishConnection();

            nll = new NetworkLinkListener() {
                @Override
                public void linkClosed(CloseEvent e) {
                    // if the link is lost, we want to reconnect immediately

                    onConnectionLost();

                    if (!link.isOpen() && !(CloseEvent.USER_REQUEST == e.getInitiator()) && !shutdown) {
                        logger.warn("KNX link has been lost (reason: {} on object {}) - reconnecting...", e.getReason(),
                                e.getSource().toString());
                        if (((BigDecimal) getConfig().get(AUTO_RECONNECT_PERIOD)).intValue() > 0) {
                            logger.info("KNX link will be retried in "
                                    + ((BigDecimal) getConfig().get(AUTO_RECONNECT_PERIOD)).intValue() + " seconds");

                            Runnable reconnectRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    if (shutdown) {
                                        reconnectJob.cancel(true);
                                    } else {
                                        logger.info("Trying to reconnect to KNX...");
                                        connect();
                                        if (link.isOpen()) {
                                            reconnectJob.cancel(true);
                                        }
                                    }
                                }
                            };

                            reconnectJob = scheduler.scheduleWithFixedDelay(reconnectRunnable,
                                    ((BigDecimal) getConfig().get(AUTO_RECONNECT_PERIOD)).intValue(),
                                    ((BigDecimal) getConfig().get(AUTO_RECONNECT_PERIOD)).intValue(), TimeUnit.SECONDS);

                        }
                    }
                }

                @Override
                public void indication(FrameEvent e) {

                    CEMILData cemid = (CEMILData) e.getFrame();
                    // logger.trace("Received indication frame from {} to {} : Code {} AckReq {} PosConf {} Repit {}",
                    // new Object[] { cemid.getSource(), cemid.getDestination(), cemid.getMessageCode(),
                    // cemid.isAckRequested(), cemid.isPositiveConfirmation(), cemid.isRepetition() });

                    if (intervalTimestamp == 0) {
                        intervalTimestamp = System.currentTimeMillis();
                        updateState(new ChannelUID(getThing().getUID(), KNXBindingConstants.ERRORS_STARTUP),
                                new DecimalType(errorsSinceStart));
                        updateState(new ChannelUID(getThing().getUID(), KNXBindingConstants.ERRORS_INTERVAL),
                                new DecimalType(errorsSinceInterval));
                    } else if ((System.currentTimeMillis() - intervalTimestamp) > 60 * 1000 * ERROR_INTERVAL_MINUTES) {
                        intervalTimestamp = System.currentTimeMillis();
                        errorsSinceInterval = 0;
                        updateState(new ChannelUID(getThing().getUID(), KNXBindingConstants.ERRORS_INTERVAL),
                                new DecimalType(errorsSinceInterval));
                    }

                    int messageCode = e.getFrame().getMessageCode();

                    switch (messageCode) {
                        case CEMILData.MC_LDATA_IND: {
                            CEMILData cemi = (CEMILData) e.getFrame();
                            if (cemi.isRepetition()) {
                                errorsSinceStart++;
                                errorsSinceInterval++;

                                updateState(new ChannelUID(getThing().getUID(), KNXBindingConstants.ERRORS_STARTUP),
                                        new DecimalType(errorsSinceStart));
                                updateState(new ChannelUID(getThing().getUID(), KNXBindingConstants.ERRORS_INTERVAL),
                                        new DecimalType(errorsSinceInterval));

                            }
                            break;
                        }
                    }
                }

                @Override
                public void confirmation(FrameEvent e) {

                    CEMILData cemid = (CEMILData) e.getFrame();
                    // logger.trace("Received confirmation frame from {} to {} : Code {} AckReq {} PosConf {} Repit {}",
                    // new Object[] { cemid.getSource(), cemid.getDestination(), cemid.getMessageCode(),
                    // cemid.isAckRequested(), cemid.isPositiveConfirmation(), cemid.isRepetition() });

                    if (intervalTimestamp == 0) {
                        intervalTimestamp = System.currentTimeMillis();
                        updateState(new ChannelUID(getThing().getUID(), KNXBindingConstants.ERRORS_STARTUP),
                                new DecimalType(errorsSinceStart));
                        updateState(new ChannelUID(getThing().getUID(), KNXBindingConstants.ERRORS_INTERVAL),
                                new DecimalType(errorsSinceInterval));
                    } else if ((System.currentTimeMillis() - intervalTimestamp) > 60 * 1000 * ERROR_INTERVAL_MINUTES) {
                        intervalTimestamp = System.currentTimeMillis();
                        errorsSinceInterval = 0;
                        updateState(new ChannelUID(getThing().getUID(), KNXBindingConstants.ERRORS_INTERVAL),
                                new DecimalType(errorsSinceInterval));
                    }

                    int messageCode = e.getFrame().getMessageCode();
                    switch (messageCode) {
                        case CEMILData.MC_LDATA_CON: {
                            CEMILData cemi = (CEMILData) e.getFrame();
                            if (!cemi.isPositiveConfirmation()) {
                                errorsSinceStart++;
                                errorsSinceInterval++;

                                updateState(new ChannelUID(getThing().getUID(), KNXBindingConstants.ERRORS_STARTUP),
                                        new DecimalType(errorsSinceStart));
                                updateState(new ChannelUID(getThing().getUID(), KNXBindingConstants.ERRORS_INTERVAL),
                                        new DecimalType(errorsSinceInterval));
                            }
                            break;
                        }

                    }
                }
            };

            pl = new ProcessListenerEx() {

                @Override
                public void detached(DetachEvent e) {
                    logger.error("Received detach Event");
                }

                @Override
                public void groupWrite(ProcessEvent e) {
                    onGroupWriteEvent(e);
                }

                @Override
                public void groupReadRequest(ProcessEvent e) {
                    onGroupReadEvent(e);
                }

                @Override
                public void groupReadResponse(ProcessEvent e) {
                    onGroupReadResponseEvent(e);
                }

            };

            if (link != null) {
                mp = new ManagementProceduresImpl(link);

                mc = new ManagementClientImpl(link);
                mc.setResponseTimeout((((BigDecimal) getConfig().get(RESPONSE_TIME_OUT)).intValue() / 1000));

                pc = new ProcessCommunicatorImpl(link);
                pc.setResponseTimeout(((BigDecimal) getConfig().get(RESPONSE_TIME_OUT)).intValue() / 1000);
                pc.addProcessListener(pl);

                link.addLinkListener(nll);
            }

            onConnectionResumed();

        } catch (KNXException e) {
            logger.error("Error connecting to KNX bus: {}", e.getMessage());
            disconnect();
            onConnectionLost();
        }
    }

    public synchronized void disconnect() {
        shutdown = true;

        if (readJobs != null) {
            for (ScheduledFuture<?> readJob : readJobs) {
                if (!readJob.isDone()) {
                    readJob.cancel(true);
                }
            }
        }

        if (busJob != null) {
            busJob.cancel(true);
        }

        if (pc != null) {
            if (pl != null) {
                pc.removeProcessListener(pl);
            }
            pc.detach();
        }

        if (nll != null) {
            link.removeLinkListener(nll);
        }

        if (link != null) {
            link.close();
        }

    }

    public void onConnectionLost() {
        updateStatus(ThingStatus.OFFLINE);

        for (KNXBridgeListener listener : knxBridgeListeners) {
            listener.onBridgeDisconnected(this);
        }
    }

    public void onConnectionResumed() {

        if (readJobs != null) {
            for (ScheduledFuture<?> readJob : readJobs) {
                readJob.cancel(true);
            }
        } else {
            readJobs = new ArrayList<ScheduledFuture<?>>();
        }

        if (busJob != null) {
            busJob.cancel(true);
        }

        readDatapoints = new LinkedBlockingQueue<RetryDatapoint>();

        busJob = scheduler.scheduleWithFixedDelay(new BusRunnable(), 0,
                ((BigDecimal) getConfig().get(READING_PAUSE)).intValue(), TimeUnit.MILLISECONDS);

        for (Channel channel : getThing().getChannels()) {

            Configuration channelConfiguration = channel.getConfiguration();
            String dpt = (String) channelConfiguration.get(DPT);
            String address = (String) channelConfiguration.get(ADDRESS);
            if (dpt != null && address != null) {
                Boolean read = false;
                if (channelConfiguration.get(READ) != null) {
                    read = ((Boolean) channelConfiguration.get(READ));
                }
                int readInterval = 0;
                if (channelConfiguration.get(INTERVAL) != null) {
                    readInterval = ((BigDecimal) channelConfiguration.get(INTERVAL)).intValue();
                }

                if (KNXCoreTypeMapper.toTypeClass(dpt) == null) {
                    logger.warn("DPT " + dpt + " is not supported by the KNX binding.");
                    return;
                }

                // create group address and datapoint
                try {
                    GroupAddress groupAddress = new GroupAddress(address);
                    Datapoint datapoint = new CommandDP(groupAddress, getThing().getUID().toString(), 0, dpt);

                    if (read && readInterval == 0) {
                        logger.debug("Scheduling reading out group address '{}'", address);
                        readJobs.add(scheduler.schedule(new ReadRunnable(datapoint, getReadRetriesLimit()), 0,
                                TimeUnit.SECONDS));
                    }

                    if (read && readInterval > 0) {
                        logger.debug("Scheduling reading out group address '{}' every '{}' seconds", address,
                                readInterval);
                        readJobs.add(scheduler.scheduleWithFixedDelay(
                                new ReadRunnable(datapoint, getReadRetriesLimit()), 0, readInterval, TimeUnit.SECONDS));
                    }

                } catch (KNXFormatException e) {
                    logger.warn("The datapoint for group address '{}' with DPT '{}' could not be initialised", address,
                            dpt);
                }
            }
        }

        updateStatus(ThingStatus.ONLINE);

        errorsSinceStart = 0;
        errorsSinceInterval = 0;

        for (KNXBridgeListener listener : knxBridgeListeners) {
            listener.onBridgeConnected(this);
        }
    }

    public boolean registerGroupAddressListener(GroupAddressListener listener) {
        if (listener == null) {
            throw new NullPointerException("It's not allowed to pass a null GroupAddressListener.");
        }
        boolean result = false;
        if (groupAddressListeners.contains(listener)) {
            result = true;
        } else {
            result = groupAddressListeners.add(listener);
        }
        return result;
    }

    public boolean unregisterGroupAddressListener(GroupAddressListener listener) {
        if (listener == null) {
            throw new NullPointerException("It's not allowed to pass a null GroupAddressListener.");
        }
        boolean result = groupAddressListeners.remove(listener);

        return result;
    }

    public boolean registerIndividualAddressListener(IndividualAddressListener listener) {
        if (listener == null) {
            throw new NullPointerException("It's not allowed to pass a null IndividualAddressListener.");
        }
        boolean result = false;
        if (individualAddressListeners.contains(listener)) {
            result = true;
        } else {
            result = individualAddressListeners.add(listener);
        }
        return result;
    }

    public boolean unregisterIndividualAddressListener(IndividualAddressListener listener) {
        if (listener == null) {
            throw new NullPointerException("It's not allowed to pass a null IndividualAddressListener.");
        }
        boolean result = individualAddressListeners.remove(listener);

        return result;
    }

    public boolean registerKNXBridgeListener(KNXBridgeListener listener) {
        if (listener == null) {
            throw new NullPointerException("It's not allowed to pass a null KNXBridgeListener.");
        }
        boolean result = false;
        if (knxBridgeListeners.contains(listener)) {
            result = true;
        } else {
            result = knxBridgeListeners.add(listener);
        }
        return result;
    }

    public boolean unregisterKNXBridgeListener(KNXBridgeListener listener) {
        if (listener == null) {
            throw new NullPointerException("It's not allowed to pass a null KNXBridgeListener.");
        }
        boolean result = knxBridgeListeners.remove(listener);

        return result;
    }

    public void addKNXTypeMapper(KNXTypeMapper typeMapper) {
        typeMappers.add(typeMapper);
    }

    public void removeKNXTypeMapper(KNXTypeMapper typeMapper) {
        typeMappers.remove(typeMapper);
    }

    public void registerKNXBusListener(KNXBusListener knxBusListener) {
        if (knxBusListener != null) {
            knxBusListeners.add(knxBusListener);
        }
    }

    public void unregisterKNXBusListener(KNXBusListener knxBusListener) {
        if (knxBusListener != null) {
            knxBusListeners.remove(knxBusListener);
        }
    }

    @Override
    public void handleUpdate(ChannelUID channelUID, State newState) {

        logger.trace("handleUpdate {} {}", channelUID, newState);

        if (channelUID != null) {
            Channel channel = this.getThing().getChannel(channelUID.getId());

            if (channel != null) {
                Configuration channelConfiguration = channel.getConfiguration();
                if (channelConfiguration.get(AUTO_UPDATE) != null && (boolean) channelConfiguration.get(AUTO_UPDATE)) {
                    if (autoUpdateChannels.contains(channelUID)) {
                        logger.debug("Removing {} from the autoUpdateChannels", channelUID);
                        autoUpdateChannels.remove(channelUID);
                    } else {
                        this.writeToKNX((String) channelConfiguration.get(ADDRESS),
                                (String) channelConfiguration.get(DPT), newState);
                    }
                } else {
                    this.writeToKNX((String) channelConfiguration.get(ADDRESS), (String) channelConfiguration.get(DPT),
                            newState);
                }
            } else {
                logger.error("No channel is associated with channelUID {}", channelUID);
            }
        }

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        logger.trace("handleCommand {} {}", channelUID, command);

        if (channelUID != null) {
            Channel channel = this.getThing().getChannel(channelUID.getId());
            if (channel != null) {
                if (command instanceof RefreshType) {

                    logger.debug("Refreshing channel {}", channelUID);

                    Configuration channelConfiguration = channel.getConfiguration();
                    String dpt = (String) channelConfiguration.get(DPT);
                    String address = (String) channelConfiguration.get(ADDRESS);
                    if (dpt != null && address != null) {

                        if (KNXCoreTypeMapper.toTypeClass(dpt) == null) {
                            logger.warn("DPT " + dpt + " is not supported by the KNX binding.");
                            return;
                        }

                        if (getThing().getStatus() == ThingStatus.ONLINE && channelConfiguration.get(READ) != null
                                && ((Boolean) channelConfiguration.get(READ))) {

                            try {
                                GroupAddress groupAddress = new GroupAddress(address);
                                Datapoint datapoint = new CommandDP(groupAddress, getThing().getUID().toString(), 0,
                                        dpt);

                                logger.debug("Scheduling reading out group address '{}'", address);

                                readJobs.add(scheduler.schedule(new ReadRunnable(datapoint, getReadRetriesLimit()), 0,
                                        TimeUnit.SECONDS));

                            } catch (KNXFormatException e) {
                                logger.warn(
                                        "The datapoint for group address '{}' with DPT '{}' could not be initialised",
                                        address, dpt);
                            }
                        }

                    }

                } else {
                    Configuration channelConfiguration = channel.getConfiguration();
                    this.writeToKNX((String) channelConfiguration.get(ADDRESS), (String) channelConfiguration.get(DPT),
                            command);
                    if (channelConfiguration.get(AUTO_UPDATE) != null && (boolean) channelConfiguration.get(AUTO_UPDATE)
                            && command instanceof State) {
                        logger.debug("Adding {} to the autoUpdateChannels", channelUID);
                        autoUpdateChannels.add(channelUID);
                        logger.debug("updateState {} {}", channelUID, command);
                        updateState(channelUID, (State) command);
                    }
                }
            } else {
                logger.error("No channel is associated with channelUID {}", channelUID);
            }
        }
    }

    public class BusRunnable implements Runnable {

        @Override
        public void run() {

            if (getThing().getStatus() == ThingStatus.ONLINE && pc != null) {

                RetryDatapoint datapoint = readDatapoints.poll();

                if (datapoint != null) {
                    datapoint.incrementRetries();

                    boolean success = false;
                    try {
                        logger.trace("Sending read request on the KNX bus for datapoint {}",
                                datapoint.getDatapoint().getMainAddress());
                        pc.read(datapoint.getDatapoint());
                        success = true;
                    } catch (KNXException e) {
                        logger.warn("Cannot read value for datapoint '{}' from KNX bus: {}",
                                datapoint.getDatapoint().getMainAddress(), e.getMessage());
                    } catch (KNXIllegalArgumentException e) {
                        logger.warn("Error sending KNX read request for datapoint '{}': {}",
                                datapoint.getDatapoint().getMainAddress(), e.getMessage());
                    } catch (InterruptedException e) {
                        logger.warn("Error sending KNX read request for datapoint '{}': {}",
                                datapoint.getDatapoint().getMainAddress(), e.getMessage());
                    }
                    if (!success) {
                        if (datapoint.getRetries() < datapoint.getLimit()) {
                            logger.debug(
                                    "Adding the read request (after attempt '{}') for datapoint '{}' at position '{}' in the queue",
                                    datapoint.getRetries(), datapoint.getDatapoint().getMainAddress(),
                                    readDatapoints.size() + 1);
                            readDatapoints.add(datapoint);
                        } else {
                            logger.debug("Giving up reading datapoint {} - nubmer of maximum retries ({}) reached.",
                                    datapoint.getDatapoint().getMainAddress(), datapoint.getLimit());
                        }
                    }
                }
            }
        }
    };

    public class ReadRunnable implements Runnable {

        private Datapoint datapoint;
        private int retries;

        public ReadRunnable(Datapoint datapoint, int retries) {
            this.datapoint = datapoint;
            this.retries = retries;
        }

        @Override
        public void run() {
            try {
                readDatapoint(datapoint, retries);
            } catch (Exception e) {
                logger.debug("Exception during poll : {}", e);
            }
        }
    };

    public class RetryDatapoint {

        private Datapoint datapoint;
        private int retries;
        private int limit;

        public Datapoint getDatapoint() {
            return datapoint;
        }

        public int getRetries() {
            return retries;
        }

        public void incrementRetries() {
            this.retries++;
        }

        public int getLimit() {
            return limit;
        }

        public RetryDatapoint(Datapoint datapoint, int limit) {
            this.datapoint = datapoint;
            this.retries = 0;
            this.limit = limit;
        }
    }

    public void readDatapoint(Datapoint datapoint, int retriesLimit) {
        synchronized (this) {
            if (datapoint != null) {
                RetryDatapoint retryDatapoint = new RetryDatapoint(datapoint, retriesLimit);
                logger.debug("Adding the read request for datapoint '{}' at position '{}' in the queue",
                        datapoint.getMainAddress(), readDatapoints.size() + 1);
                readDatapoints.add(retryDatapoint);
            }
        }
    }

    /**
     * Handles the given {@link ProcessEvent}. If the KNX ASDU is valid
     * it is passed on to the {@link IndividualAddressListener}s and {@link GroupAddressListener}s that are interested
     * in the telegram, and subsequently to the
     * {@link KNXBusListener}s that are interested in all KNX bus activity
     *
     * @param e the {@link ProcessEvent} to handle.
     */
    private void onGroupWriteEvent(ProcessEvent e) {
        try {
            GroupAddress destination = e.getDestination();
            IndividualAddress source = e.getSourceAddr();
            byte[] asdu = e.getASDU();
            if (asdu.length == 0) {
                return;
            }

            logger.trace("Received a Group Write telegram from '{}' for destination '{}'", e.getSourceAddr(),
                    destination);

            for (IndividualAddressListener listener : individualAddressListeners) {
                if (listener.listensTo(source)) {
                    if (listener instanceof GroupAddressListener
                            && ((GroupAddressListener) listener).listensTo(destination)) {
                        listener.onGroupWrite(this, source, destination, asdu);
                    } else {
                        listener.onGroupWrite(this, source, destination, asdu);
                    }

                }
            }

            for (GroupAddressListener listener : groupAddressListeners) {
                if (listener.listensTo(destination)) {
                    if (listener instanceof IndividualAddressListener
                            && !((IndividualAddressListener) listener).listensTo(source)) {
                        listener.onGroupWrite(this, source, destination, asdu);
                    } else {
                        listener.onGroupWrite(this, source, destination, asdu);
                    }
                }
            }

            for (KNXBusListener listener : knxBusListeners) {
                listener.onActivity(e.getSourceAddr(), destination, asdu);
            }

        } catch (RuntimeException re) {
            logger.error("Error while receiving event from KNX bus: " + re.toString());
        }
    }

    /**
     * Handles the given {@link ProcessEvent}. If the KNX ASDU is valid
     * it is passed on to the {@link IndividualAddressListener}s and {@link GroupAddressListener}s that are interested
     * in the telegram, and subsequently to the
     * {@link KNXBusListener}s that are interested in all KNX bus activity
     *
     * @param e the {@link ProcessEvent} to handle.
     */
    private void onGroupReadEvent(ProcessEvent e) {
        try {
            GroupAddress destination = e.getDestination();
            IndividualAddress source = e.getSourceAddr();

            logger.trace("Received a Group Read telegram from '{}' for destination '{}'", e.getSourceAddr(),
                    destination);

            byte[] asdu = e.getASDU();

            for (IndividualAddressListener listener : individualAddressListeners) {
                if (listener.listensTo(source)) {
                    if (listener instanceof GroupAddressListener
                            && ((GroupAddressListener) listener).listensTo(destination)) {
                        listener.onGroupRead(this, source, destination, asdu);
                    } else {
                        listener.onGroupRead(this, source, destination, asdu);
                    }

                }
            }

            for (GroupAddressListener listener : groupAddressListeners) {
                if (listener.listensTo(destination)) {
                    if (listener instanceof IndividualAddressListener
                            && !((IndividualAddressListener) listener).listensTo(source)) {
                        listener.onGroupRead(this, source, destination, asdu);
                    } else {
                        listener.onGroupRead(this, source, destination, asdu);
                    }
                }
            }

            for (KNXBusListener listener : knxBusListeners) {
                listener.onActivity(e.getSourceAddr(), destination, asdu);
            }

        } catch (RuntimeException re) {
            logger.error("Error while receiving event from KNX bus: " + re.toString());
        }
    }

    /**
     * Handles the given {@link ProcessEvent}. If the KNX ASDU is valid
     * it is passed on to the {@link IndividualAddressListener}s and {@link GroupAddressListener}s that are interested
     * in the telegram, and subsequently to the
     * {@link KNXBusListener}s that are interested in all KNX bus activity
     *
     * @param e the {@link ProcessEvent} to handle.
     */
    private void onGroupReadResponseEvent(ProcessEvent e) {
        try {
            GroupAddress destination = e.getDestination();
            IndividualAddress source = e.getSourceAddr();
            byte[] asdu = e.getASDU();
            if (asdu.length == 0) {
                return;
            }

            logger.trace("Received a Group Read Response telegram from '{}' for destination '{}'", e.getSourceAddr(),
                    destination);

            for (IndividualAddressListener listener : individualAddressListeners) {
                if (listener.listensTo(source)) {
                    if (listener instanceof GroupAddressListener
                            && ((GroupAddressListener) listener).listensTo(destination)) {
                        listener.onGroupReadResponse(this, source, destination, asdu);
                    } else {
                        listener.onGroupReadResponse(this, source, destination, asdu);
                    }

                }
            }

            for (GroupAddressListener listener : groupAddressListeners) {
                if (listener.listensTo(destination)) {
                    if (listener instanceof IndividualAddressListener
                            && !((IndividualAddressListener) listener).listensTo(source)) {
                        listener.onGroupReadResponse(this, source, destination, asdu);
                    } else {
                        listener.onGroupReadResponse(this, source, destination, asdu);
                    }
                }
            }

            for (KNXBusListener listener : knxBusListeners) {
                listener.onActivity(e.getSourceAddr(), destination, asdu);
            }

        } catch (RuntimeException re) {
            logger.error("Error while receiving event from KNX bus: " + re.toString());
        }
    }

    public void writeToKNX(String address, String dpt, Type value) {

        if (dpt != null && address != null && value != null) {

            GroupAddress groupAddress = null;
            try {
                groupAddress = new GroupAddress(address);
            } catch (Exception e) {
                logger.error("An exception occurred while creating a Group Address : '{}'", e.getMessage());
            }
            Datapoint datapoint = new CommandDP(groupAddress, getThing().getUID().toString(), 0, dpt);

            writeToKNX(datapoint, value);
        }
    }

    public void writeToKNX(Datapoint datapoint, Type value) {

        ProcessCommunicator pc = getCommunicator();

        if (pc != null && datapoint != null && value != null) {
            try {
                String dpt = toDPTValue(value, datapoint.getDPT());
                if (dpt != null) {
                    pc.write(datapoint, dpt);
                    logger.debug("Wrote value '{}' to datapoint '{}'", value, datapoint);
                } else {
                    logger.debug("Value '{}' can not be mapped to datapoint '{}'", value, datapoint);
                }
            } catch (KNXException e) {
                logger.debug("Value '{}' could not be sent to the KNX bus using datapoint '{}' - retrying one time: {}",
                        new Object[] { value, datapoint, e.getMessage() });
                try {
                    // do a second try, maybe the reconnection was successful
                    pc = getCommunicator();
                    pc.write(datapoint, toDPTValue(value, datapoint.getDPT()));
                    logger.debug("Wrote value '{}' to datapoint '{}' on second try", value, datapoint);
                } catch (KNXException e1) {
                    logger.error(
                            "Value '{}' could not be sent to the KNX bus using datapoint '{}' - giving up after second try: {}",
                            new Object[] { value, datapoint, e1.getMessage() });
                }
            }
        } else {
            logger.error("Could not get hold of KNX Process Communicator");
        }
    }

    @Override
    public boolean listensTo(GroupAddress destination) {
        // Bridges are allowed to listen to any GA that flies by on the bus, even if they do not have channel that
        // actively uses that GA
        return true;
    }

    @Override
    public void onGroupWrite(KNXBridgeBaseThingHandler bridge, IndividualAddress source, GroupAddress destination,
            byte[] asdu) {

        for (Channel channel : getThing().getChannels()) {

            // first process the data for the "main" address associated with each channel
            Configuration channelConfiguration = channel.getConfiguration();
            processDataReceived(destination, asdu, (String) channelConfiguration.get(DPT),
                    (String) channelConfiguration.get(ADDRESS), channel.getUID());

            // secondly, process the data for the "auxiliary" addresses associated with the channel, if of the right
            // type
            switch (channel.getAcceptedItemType()) {
                case "dimmer": {
                    processDataReceived(destination, asdu, (String) channelConfiguration.get(DPT),
                            (String) channelConfiguration.get(STATE_ADDRESS), channel.getUID());
                    break;
                }
            }
        }
    }

    @Override
    public void onGroupRead(KNXBridgeBaseThingHandler bridge, IndividualAddress source, GroupAddress destination,
            byte[] asdu) {
        // Nothing to do here - bridges should not respond to group read requests
    }

    @Override
    public void onGroupReadResponse(KNXBridgeBaseThingHandler bridge, IndividualAddress source,
            GroupAddress destination, byte[] asdu) {
        onGroupWrite(bridge, source, destination, asdu);
    }

    private void processDataReceived(GroupAddress destination, byte[] asdu, String dpt, String channelAddress,
            ChannelUID channelUID) {

        if (channelAddress != null && dpt != null) {
            GroupAddress channelGroupAddress = null;
            try {
                channelGroupAddress = new GroupAddress(channelAddress);
            } catch (KNXFormatException e) {
                logger.error("An exception occurred while creating a Group Address : '{}'", e.getMessage());
            }

            if (channelGroupAddress != null && channelGroupAddress.equals(destination)) {

                Datapoint datapoint = new CommandDP(destination, getThing().getUID().toString(), 0, dpt);
                Type type = getType(datapoint, asdu);

                if (type != null) {
                    if (type instanceof State) {
                        updateState(channelUID, (State) type);
                    } else {
                        postCommand(channelUID, (Command) type);
                    }
                    logger.trace("Processed event (channel='{}', value='{}', destination='{}')",
                            new Object[] { channelUID, type.toString(), destination.toString() });
                } else {
                    final char[] hexCode = "0123456789ABCDEF".toCharArray();
                    StringBuilder sb = new StringBuilder(2 + asdu.length * 2);
                    sb.append("0x");
                    for (byte b : asdu) {
                        sb.append(hexCode[(b >> 4) & 0xF]);
                        sb.append(hexCode[(b & 0xF)]);
                    }

                    logger.warn(
                            "Ignoring KNX bus data: couldn't transform to an openHAB type (not supported). Destination='{}', datapoint='{}', data='{}'",
                            new Object[] { destination.toString(), datapoint.toString(), sb.toString() });
                    return;
                }
            }
        }
    }

    /**
     * Transforms an openHAB type (command or state) into a datapoint type value for the KNX bus.
     *
     * @param type
     *            the openHAB command or state to transform
     * @param dpt
     *            the datapoint type to which should be converted
     *
     * @return the corresponding KNX datapoint type value as a string
     */
    public String toDPTValue(Type type, String dpt) {
        for (KNXTypeMapper typeMapper : typeMappers) {
            String value = typeMapper.toDPTValue(type, dpt);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    public String toDPTid(Class<? extends Type> type) {
        return KNXCoreTypeMapper.toDPTid(type);
    }

    /**
     * Transforms the raw KNX bus data of a given datapoint into an openHAB type (command or state)
     *
     * @param datapoint
     *            the datapoint to which the data belongs
     * @param asdu
     *            the byte array of the raw data from the KNX bus
     * @return the openHAB command or state that corresponds to the data
     */
    public Type getType(Datapoint datapoint, byte[] asdu) {
        for (KNXTypeMapper typeMapper : typeMappers) {
            Type type = typeMapper.toType(datapoint, asdu);
            if (type != null) {
                return type;
            }
        }
        return null;
    }

    public Type getType(GroupAddress destination, String dpt, byte[] asdu) {
        Datapoint datapoint = new CommandDP(destination, getThing().getUID().toString(), 0, dpt);
        return getType(datapoint, asdu);
    }

    protected Set<String> getLinkedItems(String channelId) {
        Channel channel = getThing().getChannel(channelId);
        if (channel != null) {
            return itemChannelLinkRegistry.getLinkedItems(channel.getUID());
        } else {
            throw new IllegalArgumentException("Channel with ID '" + channelId + "' does not exists.");
        }
    }

    synchronized public boolean isReachable(IndividualAddress address) {
        if (mp != null) {
            try {
                return mp.isAddressOccupied(address);
            } catch (KNXException | InterruptedException e) {
                logger.error("An exception occurred while trying to reach address '{}' : {}", address.toString(),
                        e.getMessage());
            }
        }
        return false;
    }

    synchronized public void restartNetworkDevice(IndividualAddress address) {
        if (address != null) {
            Destination destination = mc.createDestination(address, true);
            try {
                mc.restart(destination);
            } catch (KNXTimeoutException | KNXLinkClosedException e) {
                logger.error("An exception occurred while resetting the device with address {} : {}", address,
                        e.getMessage());
            }
        }
    }

    synchronized public IndividualAddress[] scanNetworkDevices(final int area, final int line) {
        try {
            return mp.scanNetworkDevices(area, line);
        } catch (final Exception e) {
            logger.error("An exception occurred while scanning the KNX bus : {}", e.getMessage());
        }

        return null;
    }

    synchronized public IndividualAddress[] scanNetworkRouters() {
        try {
            return mp.scanNetworkRouters();
        } catch (final Exception e) {
            logger.error("An exception occurred while scanning the KNX bus : {}", e.getMessage());
        }
        return null;
    }

    synchronized public byte[] readDeviceDescription(IndividualAddress address, int descType, boolean authenticate,
            long timeout) {
        Destination destination = null;

        boolean success = false;
        byte[] result = null;
        long now = System.currentTimeMillis();

        while (!success && (System.currentTimeMillis() - now) < timeout) {

            try {

                logger.debug("Reading Device Description of {} ", address);

                destination = mc.createDestination(address, true);

                if (authenticate) {
                    int access = mc.authorize(destination, (ByteBuffer.allocate(4)).put((byte) 0xFF).put((byte) 0xFF)
                            .put((byte) 0xFF).put((byte) 0xFF).array());
                }

                result = mc.readDeviceDesc(destination, descType);
                logger.debug("Reading Device Description of {} yields {} bytes", address,
                        result == null ? null : result.length);

                success = true;

            } catch (Exception e) {
                logger.error("An exception occurred while trying to read the device description for address '{}' : {}",
                        address.toString(), e.getMessage());
            } finally {
                if (destination != null) {
                    destination.destroy();
                }
            }
        }
        return result;
    }

    synchronized public byte[] readDeviceMemory(IndividualAddress address, int startAddress, int bytes,
            boolean authenticate, long timeout) {

        boolean success = false;
        byte[] result = null;
        long now = System.currentTimeMillis();

        while (!success && (System.currentTimeMillis() - now) < timeout) {
            Destination destination = null;
            try {

                logger.debug("Reading {} bytes at memory location {} of device {}",
                        new Object[] { bytes, startAddress, address });

                destination = mc.createDestination(address, true);

                if (authenticate) {
                    int access = mc.authorize(destination, (ByteBuffer.allocate(4)).put((byte) 0xFF).put((byte) 0xFF)
                            .put((byte) 0xFF).put((byte) 0xFF).array());
                }

                result = mc.readMemory(destination, startAddress, bytes);
                logger.debug("Reading {} bytes at memory location {} of device {} yields {} bytes",
                        new Object[] { bytes, startAddress, address, result == null ? null : result.length });

                success = true;
            } catch (KNXTimeoutException e) {
                logger.error("An KNXTimeoutException occurred while trying to read the memory for address '{}' : {}",
                        address.toString(), e.getMessage());
            } catch (KNXRemoteException e) {
                logger.error("An KNXRemoteException occurred while trying to read the memory for '{}' : {}",
                        address.toString(), e.getMessage());
            } catch (KNXDisconnectException e) {
                logger.error("An KNXDisconnectException occurred while trying to read the memory for '{}' : {}",
                        address.toString(), e.getMessage());
            } catch (KNXLinkClosedException e) {
                logger.error("An KNXLinkClosedException occurred while trying to read the memory for '{}' : {}",
                        address.toString(), e.getMessage());
            } catch (KNXException e) {
                logger.error("An KNXException occurred while trying to read the memory for '{}' : {}",
                        address.toString(), e.getMessage());
            } catch (InterruptedException e) {
                logger.error("An exception occurred while trying to read the memory for '{}' : {}", address.toString(),
                        e.getMessage());
                e.printStackTrace();
            } finally {
                if (destination != null) {
                    destination.destroy();
                }
            }
        }
        return result;
    }

    synchronized public byte[] readDeviceProperties(IndividualAddress address, final int interfaceObjectIndex,
            final int propertyId, final int start, final int elements, boolean authenticate, long timeout) {

        boolean success = false;
        byte[] result = null;
        long now = System.currentTimeMillis();

        while (!success && (System.currentTimeMillis() - now) < timeout) {
            Destination destination = null;
            try {
                logger.debug("Reading device property {} at index {} for {}", new Object[] { propertyId,
                        interfaceObjectIndex, address, result == null ? null : result.length });

                destination = mc.createDestination(address, true);

                if (authenticate) {
                    int access = mc.authorize(destination, (ByteBuffer.allocate(4)).put((byte) 0xFF).put((byte) 0xFF)
                            .put((byte) 0xFF).put((byte) 0xFF).array());
                }

                result = mc.readProperty(destination, interfaceObjectIndex, propertyId, start, elements);

                logger.debug("Reading device property {} at index {} for {} yields {} bytes", new Object[] { propertyId,
                        interfaceObjectIndex, address, result == null ? null : result.length });
                success = true;
            } catch (final Exception e) {
                logger.error("An exception occurred while reading a device property : {}", e.getMessage());
            } finally {
                if (destination != null) {
                    destination.destroy();
                }
            }
        }
        return result;
    }

}
