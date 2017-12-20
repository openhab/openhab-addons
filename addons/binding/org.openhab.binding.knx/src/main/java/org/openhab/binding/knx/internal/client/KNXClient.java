package org.openhab.binding.knx.internal.client;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.knx.KNXBusListener;
import org.openhab.binding.knx.KNXTypeMapper;
import org.openhab.binding.knx.handler.GroupAddressListener;
import org.openhab.binding.knx.handler.IndividualAddressListener;
import org.openhab.binding.knx.handler.StatusUpdateCallback;
import org.openhab.binding.knx.handler.TelegramListener;
import org.openhab.binding.knx.internal.handler.RetryDatapoint;
import org.openhab.binding.knx.internal.logging.LogAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.CloseEvent;
import tuwien.auto.calimero.DetachEvent;
import tuwien.auto.calimero.FrameEvent;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.IndividualAddress;
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

@NonNullByDefault
public abstract class KNXClient implements NetworkLinkListener {

    private static final int MAX_SEND_ATTEMPTS = 2;

    private final Logger logger = LoggerFactory.getLogger(KNXClient.class);
    private final LogAdapter logAdapter = new LogAdapter();

    private boolean shutdown = false;
    private final int autoReconnectPeriod;
    private final Lock connectLock = new ReentrantLock();
    private final Condition connectedCondition = connectLock.newCondition();
    private final int readRetriesLimit;
    private volatile boolean connected = false;

    @Nullable
    private ScheduledFuture<?> busJob;

    @Nullable
    private ProcessCommunicator processCommunicator;

    @Nullable
    private ManagementProcedures managementProcedures;

    @Nullable
    private ManagementClient managementClient;

    @Nullable
    private DeviceInfoClient deviceInfoClient;

    @Nullable
    private KNXNetworkLink link;

    private final ScheduledExecutorService knxScheduler;

    @Nullable
    private ScheduledFuture<?> connectJob;

    private final Set<GroupAddressListener> groupAddressListeners = new ConcurrentHashMap<GroupAddressListener, Boolean>()
            .keySet(Boolean.TRUE);
    private final Set<IndividualAddressListener> individualAddressListeners = new ConcurrentHashMap<IndividualAddressListener, Boolean>()
            .keySet(Boolean.TRUE);
    private final Set<KNXBusListener> knxBusListeners = new CopyOnWriteArraySet<>();
    private final Collection<KNXTypeMapper> typeMappers = new CopyOnWriteArraySet<>();

    protected ConcurrentHashMap<IndividualAddress, Destination> destinations = new ConcurrentHashMap<>();

    @FunctionalInterface
    private interface ListenerNotification {
        void apply(TelegramListener listener, IndividualAddress source, GroupAddress destination, byte[] asdu);
    }

    @NonNullByDefault({})
    private final ProcessListenerEx processListener = new ProcessListenerEx() {

        @Override
        public void detached(DetachEvent e) {
            logger.error("The KNX network link was detached from the process communicator", e.getSource());
        }

        @Override
        public void groupWrite(ProcessEvent e) {
            processEvent("Group Write Request", e, (listener, source, destination, asdu) -> {
                listener.onGroupWrite(KNXClient.this, source, destination, asdu);
            });
        }

        @Override
        public void groupReadRequest(ProcessEvent e) {
            processEvent("Group Read Request", e, (listener, source, destination, asdu) -> {
                listener.onGroupRead(KNXClient.this, source, destination, asdu);
            });
        }

        @Override
        public void groupReadResponse(ProcessEvent e) {
            processEvent("Group Read Response", e, (listener, source, destination, asdu) -> {
                listener.onGroupReadResponse(KNXClient.this, source, destination, asdu);
            });
        }
    };

    private final LinkedBlockingQueue<@Nullable RetryDatapoint> readDatapoints = new LinkedBlockingQueue<>();

    private final ThingUID thingUID;

    private final int responseTimeout;

    private final int readingPause;

    private final StatusUpdateCallback statusUpdateCallback;

    public KNXClient(int autoReconnectPeriod, ThingUID thingUID, int responseTimeout, int readingPause,
            int readRetriesLimit, ScheduledExecutorService knxScheduler, StatusUpdateCallback statusUpdateCallback) {
        this.autoReconnectPeriod = autoReconnectPeriod;
        this.thingUID = thingUID;
        this.responseTimeout = responseTimeout;
        this.readingPause = readingPause;
        this.readRetriesLimit = readRetriesLimit;
        this.knxScheduler = knxScheduler;
        this.statusUpdateCallback = statusUpdateCallback;

        shutdown = false;
    }

    public void initialize() {
        registerLogAdapter();
        scheduleConnectJob();
    }

    private void registerLogAdapter() {
        LogManager.getManager().addWriter(null, logAdapter);
    }

    private void unregisterLogAdapter() {
        LogManager.getManager().removeWriter(null, logAdapter);
    }

    public void connect() throws KNXException, InterruptedException {
        try {
            closeConnection();

            logger.debug("Bridge {} is connecting to the KNX bus", thingUID);

            KNXNetworkLink link = establishConnection();
            this.link = link;

            managementProcedures = new ManagementProceduresImpl(link);

            ManagementClient managementClient = new ManagementClientImpl(link);
            managementClient.setResponseTimeout(responseTimeout);
            this.managementClient = managementClient;

            deviceInfoClient = new DeviceInfoClient(managementClient);

            ProcessCommunicator processCommunicator = new ProcessCommunicatorImpl(link);
            processCommunicator.setResponseTimeout(responseTimeout);
            processCommunicator.addProcessListener(processListener);
            this.processCommunicator = processCommunicator;

            link.addLinkListener(this);

            busJob = knxScheduler.scheduleWithFixedDelay(() -> readNextQueuedDatapoint(), 0, readingPause,
                    TimeUnit.MILLISECONDS);

            connected = true;
            statusUpdateCallback.updateStatus(ThingStatus.ONLINE);
        } catch (KNXException | InterruptedException e) {
            closeConnection();
            throw e;
        }

        connectLock.lock();
        try {
            connectedCondition.signalAll();
        } finally {
            connectLock.unlock();
        }
    }

    private void closeConnection() {
        logger.debug("Bridge {} is disconnecting from the KNX bus", thingUID);
        readDatapoints.clear();
        if (busJob != null) {
            busJob.cancel(true);
            busJob = null;
        }
        if (managementProcedures != null) {
            managementProcedures.detach();
            managementProcedures = null;
        }
        deviceInfoClient = null;
        if (managementClient != null) {
            managementClient.detach();
            managementClient = null;
        }
        if (processCommunicator != null) {
            processCommunicator.removeProcessListener(processListener);
        }
        if (processCommunicator != null) {
            processCommunicator.detach();
            processCommunicator = null;
        }
        if (link != null) {
            link.close();
            link = null;
        }

        statusUpdateCallback.updateStatus(ThingStatus.OFFLINE);
    }

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

    public void writeToKNX(GroupAddress address, @Nullable String dpt, Type value) throws KNXException {
        if (dpt == null /* || address == null || value == null */) {
            return;
        }
        scheduleAndWaitForConnection();

        KNXNetworkLink link = this.link;
        ProcessCommunicator processCommunicator = this.processCommunicator;

        if (processCommunicator == null || link == null) {
            logger.debug("Cannot write to the KNX bus (processCommuicator: {}, link: {})",
                    processCommunicator == null ? "Not OK" : "OK",
                    link == null ? "Not OK" : (link.isOpen() ? "Open" : "Closed"));
            return;
        }
        Datapoint datapoint = new CommandDP(address, thingUID.toString(), 0, dpt);
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
                    throw e;
                }
            }
        }
    }

    public void scheduleConnectJob() {
        if (connectJob != null && !connectJob.isDone()) {
            return;
        }
        logger.trace("Scheduling the connection attempt to the KNX bus");
        connectJob = knxScheduler.schedule(() -> {
            if (!shutdown) {
                try {
                    connect();
                } catch (KNXException | InterruptedException e) {
                    logger.debug("Error connecting to the bus: {}", e.getMessage(), e);
                }
            }
        }, autoReconnectPeriod, TimeUnit.SECONDS);
    }

    private void scheduleAndWaitForConnection() {
        connectLock.lock();
        try {
            while (!(connected)) {
                scheduleConnectJob();
                try {
                    connectedCondition.await();
                } catch (InterruptedException e) {
                    return;
                }
            }
        } finally {
            connectLock.unlock();
        }
    }

    private void cancelConnectJob() {
        if (connectJob != null) {
            connectJob.cancel(true);
            connectJob = null;
        }
    }

    protected abstract KNXNetworkLink establishConnection() throws KNXException, InterruptedException;

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
    @Nullable
    private String toDPTValue(Type type, String dpt) {
        for (KNXTypeMapper typeMapper : typeMappers) {
            String value = typeMapper.toDPTValue(type, dpt);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private void readNextQueuedDatapoint() {
        scheduleAndWaitForConnection();
        ProcessCommunicator processCommunicator = this.processCommunicator;
        if (!connected || processCommunicator == null) {
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

    public void dispose() {
        shutdown = true;
        cancelConnectJob();
        closeConnection();
        unregisterLogAdapter();
    }

    @Override
    public void linkClosed(@Nullable CloseEvent e) {
        KNXNetworkLink link = this.link;
        if (link == null || e == null) {
            return;
        }
        if (!link.isOpen() && !(CloseEvent.USER_REQUEST == e.getInitiator()) && !shutdown) {
            statusUpdateCallback.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    e.getReason());
            logger.warn("KNX link has been lost (reason: {} on object {})", e.getReason(), e.getSource().toString());
            if (autoReconnectPeriod > 0) {
                logger.info("KNX link will be retried in '{}' seconds", autoReconnectPeriod);
                scheduleConnectJob();
            }
        }
    }

    @Override
    public void indication(@Nullable FrameEvent e) {
        // handleFrameEvent(e);
    }

    @Override
    public void confirmation(@Nullable FrameEvent e) {
        // handleFrameEvent(e);
    }

    public final synchronized boolean isReachable(@Nullable IndividualAddress address) throws KNXException {
        ManagementProcedures managementProcedures = this.managementProcedures;
        if (managementProcedures == null || address == null) {
            return false;
        }
        try {
            return managementProcedures.isAddressOccupied(address);
        } catch (InterruptedException e) {
            logger.error("Could not reach address '{}': {}", address.toString(), e.getMessage());
            if (logger.isDebugEnabled()) {
                logger.error("", e);
            }
        }
        return false;
    }

    public final synchronized void restartNetworkDevice(@Nullable IndividualAddress address) {
        ManagementClient managementClient = this.managementClient;
        if (address == null || managementClient == null) {
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

    public void readDatapoint(Datapoint datapoint) {
        synchronized (this) {
            RetryDatapoint retryDatapoint = new RetryDatapoint(datapoint, readRetriesLimit);
            if (!readDatapoints.contains(retryDatapoint)) {
                readDatapoints.add(retryDatapoint);
            }
        }
    }

    public final boolean registerGroupAddressListener(GroupAddressListener listener) {
        return groupAddressListeners.contains(listener) ? true : groupAddressListeners.add(listener);
    }

    public final boolean unregisterGroupAddressListener(GroupAddressListener listener) {
        return groupAddressListeners.remove(listener);
    }

    public final boolean registerIndividualAddressListener(IndividualAddressListener listener) {
        return individualAddressListeners.contains(listener) ? true : individualAddressListeners.add(listener);
    }

    public final boolean unregisterIndividualAddressListener(IndividualAddressListener listener) {
        return individualAddressListeners.remove(listener);
    }

    public final void registerKNXBusListener(KNXBusListener knxBusListener) {
        knxBusListeners.add(knxBusListener);
    }

    public final void unregisterKNXBusListener(KNXBusListener knxBusListener) {
        knxBusListeners.remove(knxBusListener);
    }

    public boolean isConnected() {
        return connected;
    }

    public DeviceInfoClient getDeviceInfoClient() {
        if (deviceInfoClient != null) {
            return deviceInfoClient;
        } else {
            throw new IllegalStateException();
        }
    }

}
