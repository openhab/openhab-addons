/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.handler;

import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.knx.GroupAddressListener;
import org.openhab.binding.knx.IndividualAddressListener;
import org.openhab.binding.knx.KNXBindingConstants;
import org.openhab.binding.knx.KNXBusListener;
import org.openhab.binding.knx.TelegramListener;
import org.openhab.binding.knx.internal.dpt.KNXTypeMapper;
import org.openhab.binding.knx.internal.factory.KNXThreadPoolFactory;
import org.openhab.binding.knx.internal.handler.BridgeConfiguration;
import org.openhab.binding.knx.internal.handler.RetryDatapoint;
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
import tuwien.auto.calimero.link.KNXNetworkLink;
import tuwien.auto.calimero.link.NetworkLinkListener;
import tuwien.auto.calimero.log.LogManager;
import tuwien.auto.calimero.mgmt.Destination;
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
 * @author Karel Goderis - Initial contribution
 */
public abstract class KNXBridgeBaseThingHandler extends BaseBridgeHandler implements NetworkLinkListener {

    private static final int ERROR_INTERVAL_MINUTES = 5;
    private static final int MAX_SEND_ATTEMPTS = 2;

    private final Logger logger = LoggerFactory.getLogger(KNXBridgeBaseThingHandler.class);

    // Data structures related to the communication infrastructure
    private Set<GroupAddressListener> groupAddressListeners = new ConcurrentHashMap<GroupAddressListener, Boolean>()
            .keySet(Boolean.TRUE);
    private Set<IndividualAddressListener> individualAddressListeners = new ConcurrentHashMap<IndividualAddressListener, Boolean>()
            .keySet(Boolean.TRUE);
    private Set<KNXBusListener> knxBusListeners = new CopyOnWriteArraySet<>();
    private final Collection<KNXTypeMapper> typeMappers = new CopyOnWriteArraySet<>();

    private final LinkedBlockingQueue<RetryDatapoint> readDatapoints = new LinkedBlockingQueue<RetryDatapoint>();
    protected ConcurrentHashMap<IndividualAddress, Destination> destinations = new ConcurrentHashMap<IndividualAddress, Destination>();

    // Data structures related to the KNX protocol stack
    private ProcessCommunicator processCommunicator = null;
    private ManagementProcedures managementProcedures;
    private ManagementClient managementClient;
    private KNXNetworkLink link;
    private final LogAdapter logAdapter = new LogAdapter();

    // Data structures related to the various jobs
    private ScheduledFuture<?> connectJob;
    private ScheduledFuture<?> busJob;
    private Boolean connectLock = false;

    private ScheduledExecutorService knxScheduler;

    private boolean shutdown = false;
    private long intervalTimestamp;
    private long errorsSinceStart;
    private long errorsSinceInterval;

    private BridgeConfiguration config;

    private final ProcessListenerEx processListener = new ProcessListenerEx() {

        @Override
        public void detached(DetachEvent e) {
            logger.error("The KNX network link was detached from the process communicator", e.getSource());
        }

        @Override
        public void groupWrite(ProcessEvent e) {
            processEvent("Group Write Request", e, (listener, source, destination, asdu) -> {
                listener.onGroupWrite(KNXBridgeBaseThingHandler.this, source, destination, asdu);
            });
        }

        @Override
        public void groupReadRequest(ProcessEvent e) {
            processEvent("Group Read Request", e, (listener, source, destination, asdu) -> {
                listener.onGroupRead(KNXBridgeBaseThingHandler.this, source, destination, asdu);
            });
        }

        @Override
        public void groupReadResponse(ProcessEvent e) {
            processEvent("Group Read Response", e, (listener, source, destination, asdu) -> {
                listener.onGroupReadResponse(KNXBridgeBaseThingHandler.this, source, destination, asdu);
            });
        }
    };

    @FunctionalInterface
    private interface ReadFunction<T, R> {
        R apply(T t) throws KNXException, InterruptedException;
    }

    @FunctionalInterface
    private interface ListenerNotification {
        void apply(TelegramListener listener, IndividualAddress source, GroupAddress destination, byte[] asdu);
    }

    public KNXBridgeBaseThingHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        errorsSinceStart = 0;
        errorsSinceInterval = 0;

        shutdown = false;
        config = getConfigAs(BridgeConfiguration.class);
        registerLogAdapter();
        initializeScheduler();
        scheduleConnectJob();
    }

    @Override
    public void dispose() {
        shutdown = true;
        cancelConnectJob();
        disconnect();
        unregisterLogAdapter();
    }

    private void initializeScheduler() {
        if (knxScheduler == null) {
            knxScheduler = KNXThreadPoolFactory.getPrioritizedScheduledPool(getThing().getUID().getBindingId(), 5);
        }
    }

    private void scheduleConnectJob() {
        logger.trace("Scheduling the connection attempt to the KNX bus");
        connectJob = knxScheduler.schedule(() -> {
            if (!shutdown) {
                connect();
            }
        }, config.getAutoReconnectPeriod().intValue(), TimeUnit.SECONDS);
    }

    private void scheduleAndWaitForConnection() {
        synchronized (connectLock) {
            while (!(getThing().getStatus() == ThingStatus.ONLINE)) {
                if (connectJob.isDone()) {
                    scheduleConnectJob();
                }
                try {
                    connectLock.wait();
                } catch (InterruptedException e) {
                    // Nothing to do here - we move on
                }
            }
        }
    }

    private void cancelConnectJob() {
        if (connectJob != null) {
            connectJob.cancel(true);
            connectJob = null;
        }
    }

    private void registerLogAdapter() {
        LogManager.getManager().addWriter(null, logAdapter);
    }

    private void unregisterLogAdapter() {
        LogManager.getManager().removeWriter(null, logAdapter);
    }

    protected final boolean registerGroupAddressListener(GroupAddressListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("GroupAddressListener must not be null");
        }
        return groupAddressListeners.contains(listener) ? true : groupAddressListeners.add(listener);
    }

    protected final boolean unregisterGroupAddressListener(GroupAddressListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("GroupAddressListener must not be null");
        }
        return groupAddressListeners.remove(listener);
    }

    protected final boolean registerIndividualAddressListener(IndividualAddressListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("IndividualAddressListener must not be null");
        }
        return individualAddressListeners.contains(listener) ? true : individualAddressListeners.add(listener);
    }

    protected final boolean unregisterIndividualAddressListener(IndividualAddressListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("IndividualAddressListener must not be null");
        }
        return individualAddressListeners.remove(listener);
    }

    public final void addKNXTypeMapper(KNXTypeMapper typeMapper) {
        typeMappers.add(typeMapper);
    }

    public final void removeKNXTypeMapper(KNXTypeMapper typeMapper) {
        typeMappers.remove(typeMapper);
    }

    public final void registerKNXBusListener(KNXBusListener knxBusListener) {
        if (knxBusListener != null) {
            knxBusListeners.add(knxBusListener);
        }
    }

    public final void unregisterKNXBusListener(KNXBusListener knxBusListener) {
        if (knxBusListener != null) {
            knxBusListeners.remove(knxBusListener);
        }
    }

    public final int getReadRetriesLimit() {
        return config.getReadRetriesLimit().intValue();
    }

    public final boolean isDiscoveryEnabled() {
        return config.getEnableDiscovery().booleanValue();
    }

    /**
     * Establish a communication channel to the KNX gateway.
     *
     * @return an established link to the KNX gateway. Must not be <code>null</code>
     * @throws KNXException if the link could not be established
     * @throws InterruptedException if it occurs
     *
     */
    protected abstract KNXNetworkLink establishConnection() throws KNXException, InterruptedException;

    private void connect() {
        try {
            closeConnection();

            logger.debug("Bridge {} is connecting to the KNX bus", getThing().getUID());
            link = establishConnection();

            managementProcedures = new ManagementProceduresImpl(link);

            managementClient = new ManagementClientImpl(link);
            managementClient.setResponseTimeout(config.getResponseTimeout().intValue());

            processCommunicator = new ProcessCommunicatorImpl(link);
            processCommunicator.setResponseTimeout(config.getResponseTimeout().intValue());
            processCommunicator.addProcessListener(processListener);

            link.addLinkListener(this);

            errorsSinceStart = 0;
            errorsSinceInterval = 0;
            intervalTimestamp = 0;

            busJob = knxScheduler.scheduleWithFixedDelay(() -> readNextQueuedDatapoint(), 0,
                    config.getReadingPause().intValue(), TimeUnit.MILLISECONDS);

            updateStatus(ThingStatus.ONLINE);
        } catch (KNXException e) {
            logger.error("Error connecting to the KNX bus: {}", e.getMessage());
            if (logger.isDebugEnabled()) {
                logger.error("", e);
            }
            closeConnection();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
        } catch (InterruptedException e) {
            disconnect();
        }

        synchronized (connectLock) {
            connectLock.notifyAll();
        }
    }

    private void closeConnection() {
        logger.debug("Bridge {} is disconnecting from the KNX bus", getThing().getUID());
        readDatapoints.clear();
        if (busJob != null) {
            busJob.cancel(true);
            busJob = null;
        }
        if (managementProcedures != null) {
            managementProcedures.detach();
            managementProcedures = null;
        }
        if (managementClient != null) {
            managementClient.detach();
            managementClient = null;
        }
        if (processCommunicator != null) {
            processCommunicator.removeProcessListener(processListener);
            processCommunicator.detach();
            processCommunicator = null;
        }
        if (link != null) {
            link.close();
            link = null;
        }
    }

    private void disconnect() {
        closeConnection();
        updateStatus(ThingStatus.OFFLINE);
    }

    @Override
    public void handleUpdate(ChannelUID channelUID, State newState) {
        // Nothing to do here
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Nothing to do here
    }

    private void readNextQueuedDatapoint() {
        scheduleAndWaitForConnection();
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            return;
        }
        RetryDatapoint datapoint = readDatapoints.poll();
        if (datapoint != null) {
            datapoint.incrementRetries();
            try {
                logger.trace("Sending a Group Read Request telegram for {}", datapoint.getDatapoint().getMainAddress());
                processCommunicator.read(datapoint.getDatapoint());
            } catch (KNXException e) {
                if (datapoint.getRetries() < datapoint.getLimit()) {
                    readDatapoints.add(datapoint);
                    logger.debug("Could not read value for datapoint {}: {}. Going to retry.",
                            datapoint.getDatapoint().getMainAddress(), e.getMessage());
                } else {
                    logger.warn("Giving up reading datapoint {}, the number of maximum retries ({}) is reached.",
                            datapoint.getDatapoint().getMainAddress(), datapoint.getLimit());
                }
            } catch (InterruptedException e) {
                logger.debug("Interrupted sending KNX read request");
                return;
            }
        }
    }

    public void readDatapoint(Datapoint datapoint, int retriesLimit) {
        synchronized (this) {
            if (datapoint != null) {
                RetryDatapoint retryDatapoint = new RetryDatapoint(datapoint, retriesLimit);
                if (!readDatapoints.contains(retryDatapoint)) {
                    readDatapoints.add(retryDatapoint);
                }
            }
        }
    }

    /**
     * Handles the given {@link ProcessEvent}.
     *
     * If the KNX ASDU is valid it is passed on to the {@link GroupAddressListener}s that are interested in the
     * telegram, and subsequently to the {@link KNXBusListener}s that are interested in all KNX bus activity
     *
     * @param e the {@link ProcessEvent} to handle.
     */
    private void processEvent(String task, ProcessEvent event, ListenerNotification action) {
        try {
            GroupAddress destination = event.getDestination();
            IndividualAddress source = event.getSourceAddr();
            byte[] asdu = event.getASDU();
            if (asdu.length == 0) {
                return;
            }
            logger.trace("Received a {} telegram from '{}' for destination '{}'", task, source, destination);
            for (IndividualAddressListener listener : individualAddressListeners) {
                if (listener.listensTo(source)) {
                    knxScheduler.schedule(() -> action.apply(listener, source, destination, asdu), 0, TimeUnit.SECONDS);
                }
            }
            for (GroupAddressListener listener : groupAddressListeners) {
                if (listener.listensTo(destination)) {
                    knxScheduler.schedule(() -> action.apply(listener, source, destination, asdu), 0, TimeUnit.SECONDS);
                }
            }
            for (KNXBusListener listener : knxBusListeners) {
                listener.onActivity(source, destination, asdu);
            }
        } catch (RuntimeException e) {
            logger.error("Error handling {} event from KNX bus: {}", task, e.getMessage());
            if (logger.isDebugEnabled()) {
                logger.error("", e);
            }
        }
    }

    public void writeToKNX(GroupAddress address, String dpt, Type value) {
        if (dpt == null || address == null || value == null) {
            return;
        }
        scheduleAndWaitForConnection();
        if (getThing().getStatus() != ThingStatus.ONLINE || processCommunicator == null || link == null) {
            logger.debug("Cannot write to the KNX bus (processCommuicator: {}, link: {})",
                    processCommunicator == null ? "Not OK" : "OK",
                    link == null ? "Not OK" : (link.isOpen() ? "Open" : "Closed"));
        }
        Datapoint datapoint = new CommandDP(address, getThing().getUID().toString(), 0, dpt);
        String mappedValue = toDPTValue(value, datapoint.getDPT());
        if (mappedValue == null) {
            logger.debug("Value '{}' cannot be mapped to datapoint '{}'", value, datapoint);
            return;
        }
        for (int i = 0; i < MAX_SEND_ATTEMPTS; i++) {
            try {
                processCommunicator.write(datapoint, mappedValue);
                logger.debug("Wrote value '{}' to datapoint '{}' ({}. attempt).", value, datapoint, i);
                break;
            } catch (KNXException e) {
                if (i < MAX_SEND_ATTEMPTS - 1) {
                    logger.debug("Value '{}' could not be sent to the KNX bus using datapoint '{}': {}. Will retry.",
                            value, datapoint, e.getMessage());
                } else {
                    logger.debug("Value '{}' could not be sent to the KNX bus using datapoint '{}': {}. Giving up now.",
                            value, datapoint, e.getMessage());
                    closeConnection();
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                }
            }
        }
    }

    /**
     * Transforms a {@link Type} into a datapoint type value for the KNX bus.
     *
     * @param type
     *            the {@link Type} to transform
     * @param dpt
     *            the datapoint type to which should be converted
     *
     * @return the corresponding KNX datapoint type value as a string
     */
    private String toDPTValue(Type type, String dpt) {
        for (KNXTypeMapper typeMapper : typeMappers) {
            String value = typeMapper.toDPTValue(type, dpt);
            if (value != null) {
                return value;
            }
        }
        return null;
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
    private Type getType(Datapoint datapoint, byte[] asdu) {
        for (KNXTypeMapper typeMapper : typeMappers) {
            Type type = typeMapper.toType(datapoint, asdu);
            if (type != null) {
                return type;
            }
        }
        return null;
    }

    public final Type getType(GroupAddress destination, String dpt, byte[] asdu) {
        Datapoint datapoint = new CommandDP(destination, getThing().getUID().toString(), 0, dpt);
        return getType(datapoint, asdu);
    }

    public final synchronized boolean isReachable(IndividualAddress address) {
        if (managementProcedures == null || address == null) {
            return false;
        }
        try {
            return managementProcedures.isAddressOccupied(address);
        } catch (KNXException | InterruptedException e) {
            logger.error("Could not reach address '{}': {}", address.toString(), e.getMessage());
            if (logger.isDebugEnabled()) {
                logger.error("", e);
            }
        }
        return false;
    }

    public final synchronized void restartNetworkDevice(IndividualAddress address) {
        if (address == null) {
            return;
        }
        Destination destination = null;
        try {
            destination = managementClient.createDestination(address, true);
            managementClient.restart(destination);
        } catch (KNXException e) {
            logger.error("Could not reset the device with address '{}': {}", address, e.getMessage());
            if (logger.isDebugEnabled()) {
                logger.error("", e);
            }
        } finally {
            if (destination != null) {
                destination.destroy();
            }
        }
    }

    public synchronized IndividualAddress[] scanNetworkDevices(final int area, final int line) {
        try {
            return managementProcedures.scanNetworkDevices(area, line);
        } catch (KNXException | InterruptedException e) {
            logger.error("Error scanning the KNX bus: {}", e.getMessage());
            if (logger.isDebugEnabled()) {
                logger.error("", e);
            }
        }
        return null;
    }

    public synchronized IndividualAddress[] scanNetworkRouters() {
        try {
            return managementProcedures.scanNetworkRouters();
        } catch (KNXException | InterruptedException e) {
            logger.error("An exception occurred while scanning the KNX bus: {}", e.getMessage());
            if (logger.isDebugEnabled()) {
                logger.error("", e);
            }
        }
        return null;
    }

    private byte[] readFromManagementClient(String task, long timeout, IndividualAddress address,
            ReadFunction<Destination, byte[]> function) {
        final long start = System.nanoTime();
        while ((System.nanoTime() - start) < TimeUnit.MILLISECONDS.toNanos(timeout)) {
            Destination destination = null;
            try {
                logger.debug("Going to {} of {} ", task, address);
                destination = managementClient.createDestination(address, true);
                byte[] result = function.apply(destination);
                logger.debug("Finished to {} of {}, result: {}", task, address, result == null ? null : result.length);
                return result;
            } catch (KNXException e) {
                logger.error("Could not {} of {}: {}", task, address, e.getMessage());
                if (logger.isDebugEnabled()) {
                    logger.error("", e);
                }
            } catch (InterruptedException e) {
                logger.debug("Interrupted to {}", task);
                return null;
            } finally {
                if (destination != null) {
                    destination.destroy();
                }
            }
        }
        return null;
    }

    private void authorize(boolean authenticate, Destination destination) throws KNXException, InterruptedException {
        if (authenticate) {
            managementClient.authorize(destination, (ByteBuffer.allocate(4)).put((byte) 0xFF).put((byte) 0xFF)
                    .put((byte) 0xFF).put((byte) 0xFF).array());
        }
    }

    public synchronized byte[] readDeviceDescription(IndividualAddress address, int descType, boolean authenticate,
            long timeout) {
        String task = "read the device description";
        return readFromManagementClient(task, timeout, address, destination -> {
            authorize(authenticate, destination);
            return managementClient.readDeviceDesc(destination, descType);
        });
    }

    public synchronized byte[] readDeviceMemory(IndividualAddress address, int startAddress, int bytes,
            boolean authenticate, long timeout) {
        String task = MessageFormat.format("read {0} bytes at memory location {1}", bytes, startAddress);
        return readFromManagementClient(task, timeout, address, destination -> {
            authorize(authenticate, destination);
            return managementClient.readMemory(destination, startAddress, bytes);
        });
    }

    public synchronized byte[] readDeviceProperties(IndividualAddress address, final int interfaceObjectIndex,
            final int propertyId, final int start, final int elements, boolean authenticate, long timeout) {
        String task = MessageFormat.format("read device property {} at index {}", propertyId, interfaceObjectIndex);
        return readFromManagementClient(task, timeout, address, destination -> {
            authorize(authenticate, destination);
            return managementClient.readProperty(destination, interfaceObjectIndex, propertyId, start, elements);
        });
    }

    public ScheduledExecutorService getScheduler() {
        return knxScheduler;
    }

    @Override
    public void linkClosed(CloseEvent e) {
        if (!link.isOpen() && !(CloseEvent.USER_REQUEST == e.getInitiator()) && !shutdown) {
            logger.warn("KNX link has been lost (reason: {} on object {})", e.getReason(), e.getSource().toString());
            if (config.getAutoReconnectPeriod().intValue() > 0) {
                logger.info("KNX link will be retried in '{}' seconds", config.getAutoReconnectPeriod().intValue());
                if (connectJob.isDone()) {
                    scheduleConnectJob();
                }
            }
        }
    }

    @Override
    public void indication(FrameEvent e) {
        handleFrameEvent(e);
    }

    @Override
    public void confirmation(FrameEvent e) {
        handleFrameEvent(e);
    }

    private void handleFrameEvent(FrameEvent e) {
        checkErrorCounterTimeouts();
        int messageCode = e.getFrame().getMessageCode();
        switch (messageCode) {
            case CEMILData.MC_LDATA_IND:
                if (((CEMILData) e.getFrame()).isRepetition()) {
                    incrementErrorCounter();
                }
                break;
            case CEMILData.MC_LDATA_CON:
                if (!((CEMILData) e.getFrame()).isPositiveConfirmation()) {
                    incrementErrorCounter();
                }
                break;
        }
    }

    private void checkErrorCounterTimeouts() {
        if (intervalTimestamp == 0) {
            intervalTimestamp = System.nanoTime();
            updateErrorCounterChannels();
        } else if ((System.nanoTime() - intervalTimestamp) > TimeUnit.MINUTES.toNanos(ERROR_INTERVAL_MINUTES)) {
            intervalTimestamp = System.nanoTime();
            errorsSinceInterval = 0;
            updateErrorCounterChannels();
        }
    }

    private void incrementErrorCounter() {
        errorsSinceStart++;
        errorsSinceInterval++;
        updateErrorCounterChannels();
    }

    private void updateErrorCounterChannels() {
        updateState(new ChannelUID(getThing().getUID(), KNXBindingConstants.ERRORS_STARTUP),
                new DecimalType(errorsSinceStart));
        updateState(new ChannelUID(getThing().getUID(), KNXBindingConstants.ERRORS_INTERVAL),
                new DecimalType(errorsSinceInterval));
    }

    public String getETSProjectFilename() {
        return config.getKnxProj();
    }
}
