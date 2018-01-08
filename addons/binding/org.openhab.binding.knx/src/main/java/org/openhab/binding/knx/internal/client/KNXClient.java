/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.client;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.knx.KNXTypeMapper;
import org.openhab.binding.knx.handler.GroupAddressListener;
import org.openhab.binding.knx.handler.StatusUpdateCallback;
import org.openhab.binding.knx.internal.channel.CommandSpec;
import org.openhab.binding.knx.internal.channel.ResponseSpec;
import org.openhab.binding.knx.internal.dpt.KNXCoreTypeMapper;
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
import tuwien.auto.calimero.device.ProcessCommunicationResponder;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.link.KNXNetworkLink;
import tuwien.auto.calimero.link.NetworkLinkListener;
import tuwien.auto.calimero.log.LogManager;
import tuwien.auto.calimero.mgmt.Destination;
import tuwien.auto.calimero.mgmt.ManagementClient;
import tuwien.auto.calimero.mgmt.ManagementClientImpl;
import tuwien.auto.calimero.mgmt.ManagementProcedures;
import tuwien.auto.calimero.mgmt.ManagementProceduresImpl;
import tuwien.auto.calimero.process.ProcessCommunicationBase;
import tuwien.auto.calimero.process.ProcessCommunicator;
import tuwien.auto.calimero.process.ProcessCommunicatorImpl;
import tuwien.auto.calimero.process.ProcessEvent;
import tuwien.auto.calimero.process.ProcessListenerEx;

/**
 * KNX Client which encapsulates the communication with the KNX bus via the calimero libary.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
public abstract class KNXClient implements NetworkLinkListener {

    private static final int MAX_SEND_ATTEMPTS = 2;

    private final Logger logger = LoggerFactory.getLogger(KNXClient.class);
    private final LogAdapter logAdapter = new LogAdapter();
    private final KNXTypeMapper typeHelper = new KNXCoreTypeMapper();

    private final ThingUID thingUID;
    private final int responseTimeout;
    private final int readingPause;
    private final int autoReconnectPeriod;
    private final int readRetriesLimit;
    private final StatusUpdateCallback statusUpdateCallback;
    private final ScheduledExecutorService knxScheduler;

    private @Nullable ProcessCommunicator processCommunicator;
    private @Nullable ProcessCommunicationResponder responseCommunicator;
    private @Nullable ManagementProcedures managementProcedures;
    private @Nullable ManagementClient managementClient;
    private @Nullable KNXNetworkLink link;
    private @Nullable DeviceInfoClient deviceInfoClient;
    private @Nullable ScheduledFuture<?> busJob;
    private @Nullable ScheduledFuture<?> connectJob;

    private final Set<GroupAddressListener> groupAddressListeners = new CopyOnWriteArraySet<>();
    private final LinkedBlockingQueue<@Nullable RetryDatapoint> readDatapoints = new LinkedBlockingQueue<>();

    @FunctionalInterface
    private interface ListenerNotification {
        void apply(BusMessageListener listener, IndividualAddress source, GroupAddress destination, byte[] asdu);
    }

    @NonNullByDefault({})
    private final ProcessListenerEx processListener = new ProcessListenerEx() {

        @Override
        public void detached(DetachEvent e) {
            logger.debug("The KNX network link was detached from the process communicator", e.getSource());
        }

        @Override
        public void groupWrite(ProcessEvent e) {
            processEvent("Group Write", e, (listener, source, destination, asdu) -> {
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

    public KNXClient(int autoReconnectPeriod, ThingUID thingUID, int responseTimeout, int readingPause,
            int readRetriesLimit, ScheduledExecutorService knxScheduler, StatusUpdateCallback statusUpdateCallback) {
        this.autoReconnectPeriod = autoReconnectPeriod;
        this.thingUID = thingUID;
        this.responseTimeout = responseTimeout;
        this.readingPause = readingPause;
        this.readRetriesLimit = readRetriesLimit;
        this.knxScheduler = knxScheduler;
        this.statusUpdateCallback = statusUpdateCallback;
    }

    public void initialize() {
        registerLogAdapter();
        connectJob = knxScheduler.scheduleWithFixedDelay(() -> connect(), 0, autoReconnectPeriod, TimeUnit.SECONDS);
    }

    private void registerLogAdapter() {
        LogManager.getManager().addWriter(null, logAdapter);
    }

    private void unregisterLogAdapter() {
        LogManager.getManager().removeWriter(null, logAdapter);
    }

    protected abstract KNXNetworkLink establishConnection() throws KNXException, InterruptedException;

    private synchronized boolean connect() {
        if (isConnected()) {
            return true;
        }
        try {
            closeConnection(null);

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

            ProcessCommunicationResponder responseCommunicator = new ProcessCommunicationResponder(link);
            this.responseCommunicator = responseCommunicator;

            link.addLinkListener(this);

            busJob = knxScheduler.scheduleWithFixedDelay(() -> readNextQueuedDatapoint(), 0, readingPause,
                    TimeUnit.MILLISECONDS);

            statusUpdateCallback.updateStatus(ThingStatus.ONLINE);
            return true;
        } catch (KNXException | InterruptedException e) {
            logger.debug("Error connecting to the bus: {}", e.getMessage(), e);
            closeConnection(e);
            return false;
        }
    }

    private void closeConnection(@Nullable Exception e) {
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

        if (e != null) {
            statusUpdateCallback.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    e.getLocalizedMessage());
        } else {
            statusUpdateCallback.updateStatus(ThingStatus.OFFLINE);
        }
    }

    private void processEvent(String task, ProcessEvent event, ListenerNotification action) {
        GroupAddress destination = event.getDestination();
        IndividualAddress source = event.getSourceAddr();
        byte[] asdu = event.getASDU();
        logger.trace("Received a {} telegram from '{}' to '{}'", task, source, destination);
        for (GroupAddressListener listener : groupAddressListeners) {
            if (listener.listensTo(destination)) {
                knxScheduler.schedule(() -> action.apply(listener, source, destination, asdu), 0, TimeUnit.SECONDS);
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
    @Nullable
    private String toDPTValue(Type type, String dpt) {
        return typeHelper.toDPTValue(type, dpt);
    }

    private void readNextQueuedDatapoint() {
        if (!connect()) {
            return;
        }
        ProcessCommunicator processCommunicator = this.processCommunicator;
        if (processCommunicator == null) {
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
        if (connectJob != null) {
            connectJob.cancel(true);
            connectJob = null;
        }
        closeConnection(null);
        unregisterLogAdapter();
    }

    @Override
    public void linkClosed(@Nullable CloseEvent closeEvent) {
        KNXNetworkLink link = this.link;
        if (link == null || closeEvent == null) {
            return;
        }
        if (!link.isOpen() && CloseEvent.USER_REQUEST != closeEvent.getInitiator()) {
            statusUpdateCallback.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    closeEvent.getReason());
            logger.debug("KNX link has been lost (reason: {} on object {})", closeEvent.getReason(),
                    closeEvent.getSource().toString());
            connect();
        }
    }

    @Override
    public void indication(@Nullable FrameEvent e) {
        // no-op
    }

    @Override
    public void confirmation(@Nullable FrameEvent e) {
        // no-op
    }

    public final synchronized boolean isReachable(@Nullable IndividualAddress address) throws KNXException {
        ManagementProcedures managementProcedures = this.managementProcedures;
        if (managementProcedures == null || address == null) {
            return false;
        }
        try {
            return managementProcedures.isAddressOccupied(address);
        } catch (InterruptedException e) {
            logger.debug("Interrupted pinging KNX device '{}'", address);
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
            logger.warn("Could not reset device with address '{}': {}", address, e.getMessage());
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

    public boolean isConnected() {
        return link != null && link.isOpen();
    }

    public DeviceInfoClient getDeviceInfoClient() {
        if (deviceInfoClient != null) {
            return deviceInfoClient;
        } else {
            throw new IllegalStateException();
        }
    }

    public void writeToKNX(CommandSpec commandSpec) throws KNXException {
        ProcessCommunicator processCommunicator = this.processCommunicator;
        KNXNetworkLink link = this.link;
        if (processCommunicator == null || link == null) {
            logger.debug("Cannot write to the KNX bus (processCommuicator: {}, link: {})",
                    processCommunicator == null ? "Not OK" : "OK",
                    link == null ? "Not OK" : (link.isOpen() ? "Open" : "Closed"));
            return;
        }
        GroupAddress groupAddress = commandSpec.getGroupAddress();
        if (groupAddress != null) {
            sendToKNX(processCommunicator, link, groupAddress, commandSpec.getDPT(), commandSpec.getCommand());
        }
    }

    public void respondToKNX(ResponseSpec responseSpec, State state) throws KNXException {
        ProcessCommunicationResponder responseCommunicator = this.responseCommunicator;
        KNXNetworkLink link = this.link;
        if (responseCommunicator == null || link == null) {
            logger.debug("Cannot write to the KNX bus (responseCommunicator: {}, link: {})",
                    responseCommunicator == null ? "Not OK" : "OK",
                    link == null ? "Not OK" : (link.isOpen() ? "Open" : "Closed"));
            return;
        }
        GroupAddress groupAddress = responseSpec.getGroupAddress();
        if (groupAddress != null) {
            sendToKNX(responseCommunicator, link, groupAddress, responseSpec.getDPT(), state);
        }
    }

    private void sendToKNX(ProcessCommunicationBase communicator, KNXNetworkLink link, GroupAddress groupAddress,
            String dpt, Type type) throws KNXException {
        if (!connect()) {
            return;
        }

        Datapoint datapoint = new CommandDP(groupAddress, thingUID.toString(), 0, dpt);
        String mappedValue = toDPTValue(type, dpt);
        if (mappedValue == null) {
            logger.debug("Value '{}' cannot be mapped to datapoint '{}'", type, datapoint);
            return;
        }
        for (int i = 0; i < MAX_SEND_ATTEMPTS; i++) {
            try {
                communicator.write(datapoint, mappedValue);
                logger.debug("Wrote value '{}' to datapoint '{}' ({}. attempt).", type, datapoint, i);
                break;
            } catch (KNXException e) {
                if (i < MAX_SEND_ATTEMPTS - 1) {
                    logger.debug("Value '{}' could not be sent to the KNX bus using datapoint '{}': {}. Will retry.",
                            type, datapoint, e.getLocalizedMessage());
                } else {
                    logger.warn("Value '{}' could not be sent to the KNX bus using datapoint '{}': {}. Giving up now.",
                            type, datapoint, e.getLocalizedMessage());
                    closeConnection(e);
                    throw e;
                }
            }
        }

    }

}
