/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.knx.internal.client;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.KNXTypeMapper;
import org.openhab.binding.knx.internal.dpt.KNXCoreTypeMapper;
import org.openhab.binding.knx.internal.handler.GroupAddressListener;
import org.openhab.binding.knx.internal.i18n.KNXTranslationProvider;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.CloseEvent;
import tuwien.auto.calimero.DetachEvent;
import tuwien.auto.calimero.FrameEvent;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.KNXException;
import tuwien.auto.calimero.KNXIllegalArgumentException;
import tuwien.auto.calimero.datapoint.CommandDP;
import tuwien.auto.calimero.datapoint.Datapoint;
import tuwien.auto.calimero.device.ProcessCommunicationResponder;
import tuwien.auto.calimero.link.KNXNetworkLink;
import tuwien.auto.calimero.link.NetworkLinkListener;
import tuwien.auto.calimero.mgmt.Destination;
import tuwien.auto.calimero.mgmt.ManagementClient;
import tuwien.auto.calimero.mgmt.ManagementClientImpl;
import tuwien.auto.calimero.mgmt.ManagementProcedures;
import tuwien.auto.calimero.mgmt.ManagementProceduresImpl;
import tuwien.auto.calimero.process.ProcessCommunication;
import tuwien.auto.calimero.process.ProcessCommunicator;
import tuwien.auto.calimero.process.ProcessCommunicatorImpl;
import tuwien.auto.calimero.process.ProcessEvent;
import tuwien.auto.calimero.process.ProcessListener;
import tuwien.auto.calimero.secure.KnxSecureException;
import tuwien.auto.calimero.secure.SecureApplicationLayer;
import tuwien.auto.calimero.secure.Security;

/**
 * KNX Client which encapsulates the communication with the KNX bus via the calimero libary.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
public abstract class AbstractKNXClient implements NetworkLinkListener, KNXClient {
    public enum ClientState {
        INIT,
        RUNNING,
        INTERRUPTED,
        DISPOSE
    }

    private ClientState state = ClientState.INIT;

    private static final int MAX_SEND_ATTEMPTS = 2;

    private final Logger logger = LoggerFactory.getLogger(AbstractKNXClient.class);
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
    private final LinkedBlockingQueue<ReadDatapoint> readDatapoints = new LinkedBlockingQueue<>();

    @FunctionalInterface
    private interface ListenerNotification {
        void apply(BusMessageListener listener, IndividualAddress source, GroupAddress destination, byte[] asdu);
    }

    @NonNullByDefault({})
    private final ProcessListener processListener = new ProcessListener() {

        @Override
        public void detached(DetachEvent e) {
            logger.debug("The KNX network link was detached from the process communicator");
        }

        @Override
        public void groupWrite(ProcessEvent e) {
            processEvent("Group Write", e, (listener, source, destination, asdu) -> {
                listener.onGroupWrite(AbstractKNXClient.this, source, destination, asdu);
            });
        }

        @Override
        public void groupReadRequest(ProcessEvent e) {
            processEvent("Group Read Request", e, (listener, source, destination, asdu) -> {
                listener.onGroupRead(AbstractKNXClient.this, source, destination, asdu);
            });
        }

        @Override
        public void groupReadResponse(ProcessEvent e) {
            processEvent("Group Read Response", e, (listener, source, destination, asdu) -> {
                listener.onGroupReadResponse(AbstractKNXClient.this, source, destination, asdu);
            });
        }
    };

    public AbstractKNXClient(int autoReconnectPeriod, ThingUID thingUID, int responseTimeout, int readingPause,
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
        if (!scheduleReconnectJob()) {
            connect();
        }
    }

    private boolean scheduleReconnectJob() {
        if (autoReconnectPeriod > 0) {
            // schedule connect job, for the first connection ignore autoReconnectPeriod and use 1 sec
            final long reconnectDelayS = (state == ClientState.INIT) ? 1 : autoReconnectPeriod;
            final String prefix = (state == ClientState.INIT) ? "re" : "";
            logger.debug("Bridge {} scheduling {}connect in {}s", thingUID, prefix, reconnectDelayS);
            connectJob = knxScheduler.schedule(this::connect, reconnectDelayS, TimeUnit.SECONDS);
            return true;
        } else {
            return false;
        }
    }

    private void cancelReconnectJob() {
        final ScheduledFuture<?> currentReconnectJob = connectJob;
        if (currentReconnectJob != null) {
            currentReconnectJob.cancel(true);
            connectJob = null;
        }
    }

    protected abstract KNXNetworkLink establishConnection() throws KNXException, InterruptedException;

    private synchronized boolean connectIfNotAutomatic() {
        if (!isConnected()) {
            return connectJob != null ? false : connect();
        }
        return true;
    }

    private synchronized boolean connect() {
        if (state == ClientState.INIT) {
            state = ClientState.RUNNING;
        } else if (state == ClientState.DISPOSE) {
            logger.trace("connect() ignored, closing down");
            return false;
        }

        if (isConnected()) {
            return true;
        }
        try {
            // We have a valid "connection" object, this is ensured by IPClient.java.
            // "releaseConnection" is actually removing all registered users of this connection and stopping
            // all threads.
            // Note that this will also kill this function in the following call to sleep in case of a
            // connection loss -> restart is via triggered via scheduledReconnect in handler for InterruptedException.
            releaseConnection();
            Thread.sleep(1000);
            logger.debug("Bridge {} is connecting to KNX bus", thingUID);

            // now establish (possibly encrypted) connection, according to settings (tunnel, routing, secure...)
            KNXNetworkLink link = establishConnection();
            this.link = link;

            // ManagementProcedures provided by Calimero: allow managing other KNX devices, e.g. check if an address is
            // reachable.
            // Note for KNX Secure: ManagmentProcedueresImpl currently does not provide a ctor with external SAL,
            // it internally creates an instance of ManagementClientImpl, which uses
            // Security.defaultInstallation().deviceToolKeys()
            // Protected ctor using given ManagementClientImpl is avalable (custom class to be inherited)
            managementProcedures = new ManagementProceduresImpl(link);

            // ManagementClient provided by Calimero: allow reading device info, etc.
            // Note for KNX Secure: ManagementClientImpl does not provide a ctor with external SAL in Calimero 2.5,
            // is uses global Security.defaultInstalltion().deviceToolKeys()
            // Current main branch includes a protected ctor (custom class to be inherited)
            // TODO Calimero>2.5: check if there is a new way to provide security info, there is a new protected ctor
            // TODO check if we can avoid creating another ManagementClient and re-use this from ManagemntProcedures
            ManagementClient managementClient = new ManagementClientImpl(link);
            managementClient.responseTimeout(Duration.ofSeconds(responseTimeout));
            this.managementClient = managementClient;

            // OH helper for reading device info, based on managementClient above
            deviceInfoClient = new DeviceInfoClientImpl(managementClient);

            // ProcessCommunicator provides main KNX communication (Calimero).
            // Note for KNX Secure: SAL to be provided
            ProcessCommunicator processCommunicator = new ProcessCommunicatorImpl(link);
            processCommunicator.responseTimeout(Duration.ofSeconds(responseTimeout));
            processCommunicator.addProcessListener(processListener);
            this.processCommunicator = processCommunicator;

            // ProcessCommunicationResponder provides responses to requests from KNX bus (Calimero).
            // Note for KNX Secure: SAL to be provided
            ProcessCommunicationResponder responseCommunicator = new ProcessCommunicationResponder(link,
                    new SecureApplicationLayer(link, Security.defaultInstallation()));
            this.responseCommunicator = responseCommunicator;

            // register this class, callbacks will be triggered
            link.addLinkListener(this);

            // create a job carrying out read requests
            busJob = knxScheduler.scheduleWithFixedDelay(() -> readNextQueuedDatapoint(), 0, readingPause,
                    TimeUnit.MILLISECONDS);

            statusUpdateCallback.updateStatus(ThingStatus.ONLINE);
            connectJob = null;

            logger.info("Bridge {} connected to KNX bus", thingUID);

            state = ClientState.RUNNING;
            return true;
        } catch (InterruptedException e) {
            final var lastState = state;
            state = ClientState.INTERRUPTED;

            logger.trace("Bridge {}, connection interrupted", thingUID);

            disconnect(e);
            if (lastState != ClientState.DISPOSE) {
                scheduleReconnectJob();
            }

            return false;
        } catch (KNXException | KnxSecureException e) {
            logger.debug("Bridge {} cannot connect: {}", thingUID, e.getMessage());
            disconnect(e);
            scheduleReconnectJob();
            return false;
        } catch (KNXIllegalArgumentException e) {
            logger.debug("Bridge {} cannot connect: {}", thingUID, e.getMessage());
            disconnect(e, Optional.of(ThingStatusDetail.CONFIGURATION_ERROR));
            return false;
        }
    }

    private void disconnect(@Nullable Exception e) {
        disconnect(e, Optional.empty());
    }

    private synchronized void disconnect(@Nullable Exception e, Optional<ThingStatusDetail> detail) {
        releaseConnection();
        if (e != null) {
            statusUpdateCallback.updateStatus(ThingStatus.OFFLINE, detail.orElse(ThingStatusDetail.COMMUNICATION_ERROR),
                    KNXTranslationProvider.I18N.getLocalizedException(e));
        } else {
            statusUpdateCallback.updateStatus(ThingStatus.OFFLINE);
        }
    }

    @SuppressWarnings("null")
    protected void releaseConnection() {
        logger.debug("Bridge {} is disconnecting from KNX bus", thingUID);
        var tmplink = link;
        if (tmplink != null) {
            link.removeLinkListener(this);
        }
        busJob = nullify(busJob, j -> j.cancel(true));
        readDatapoints.clear();
        responseCommunicator = nullify(responseCommunicator, rc -> {
            rc.removeProcessListener(processListener);
            rc.detach();
        });
        processCommunicator = nullify(processCommunicator, pc -> {
            pc.removeProcessListener(processListener);
            pc.detach();
        });
        deviceInfoClient = null;
        managementClient = nullify(managementClient, mc -> mc.detach());
        managementProcedures = nullify(managementProcedures, mp -> mp.detach());
        link = nullify(link, l -> l.close());
        logger.trace("Bridge {} disconnected from KNX bus", thingUID);
    }

    private <T> @Nullable T nullify(T target, @Nullable Consumer<T> lastWill) {
        if (target != null && lastWill != null) {
            lastWill.accept(target);
        }
        return null;
    }

    private void processEvent(String task, ProcessEvent event, ListenerNotification action) {
        GroupAddress destination = event.getDestination();
        IndividualAddress source = event.getSourceAddr();
        byte[] asdu = event.getASDU();
        logger.trace("Received a {} telegram from '{}' to '{}' with value '{}'", task, source, destination, asdu);
        for (GroupAddressListener listener : groupAddressListeners) {
            if (listener.listensTo(destination)) {
                knxScheduler.schedule(() -> action.apply(listener, source, destination, asdu), 0, TimeUnit.SECONDS);
            }
        }
    }

    /**
     * Transforms a {@link Type} into a datapoint type value for the KNX bus.
     *
     * @param type the {@link Type} to transform
     * @param dpt the datapoint type to which should be converted
     * @return the corresponding KNX datapoint type value as a string
     */
    @Nullable
    private String toDPTValue(Type type, String dpt) {
        return typeHelper.toDPTValue(type, dpt);
    }

    // datapoint is null at end of the list, warning is misleading
    @SuppressWarnings("null")
    private void readNextQueuedDatapoint() {
        if (!connectIfNotAutomatic()) {
            return;
        }
        ProcessCommunicator processCommunicator = this.processCommunicator;
        if (processCommunicator == null) {
            return;
        }
        ReadDatapoint datapoint = readDatapoints.poll();
        if (datapoint != null) {
            datapoint.incrementRetries();
            try {
                logger.trace("Sending a Group Read Request telegram for {}", datapoint.getDatapoint().getMainAddress());
                processCommunicator.read(datapoint.getDatapoint());
            } catch (KNXException e) {
                // Note: KnxException does not cover KnxRuntimeException and subclasses KnxSecureException,
                // KnxIllegArgumentException
                if (datapoint.getRetries() < datapoint.getLimit()) {
                    readDatapoints.add(datapoint);
                    logger.debug("Could not read value for datapoint {}: {}. Going to retry.",
                            datapoint.getDatapoint().getMainAddress(), e.getMessage());
                } else {
                    logger.warn("Giving up reading datapoint {}, the number of maximum retries ({}) is reached.",
                            datapoint.getDatapoint().getMainAddress(), datapoint.getLimit());
                }
            } catch (InterruptedException | CancellationException e) {
                logger.debug("Interrupted sending KNX read request");
                return;
            } catch (Exception e) {
                // Any other exception: Fail gracefully, i.e. notify user and continue reading next DP.
                // Not catching this would end the scheduled read for all DPs in case of an error.
                // Severity is warning as this is likely caused by a configuration error.
                logger.warn("Error reading datapoint {}: {}", datapoint.getDatapoint().getMainAddress(),
                        e.getMessage());
            }
        }
    }

    public void dispose() {
        state = ClientState.DISPOSE;

        cancelReconnectJob();
        disconnect(null);
    }

    @Override
    public void linkClosed(@Nullable CloseEvent closeEvent) {
        KNXNetworkLink link = this.link;
        if (link == null || closeEvent == null) {
            return;
        }
        if (!link.isOpen() && CloseEvent.USER_REQUEST != closeEvent.getInitiator()) {
            final String reason = closeEvent.getReason();
            statusUpdateCallback.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    KNXTranslationProvider.I18N.get(reason));
            logger.debug("KNX link has been lost (reason: {} on object {})", closeEvent.getReason(),
                    closeEvent.getSource().toString());
            scheduleReconnectJob();
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

    @Override
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

    @Override
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
        } catch (InterruptedException e) { // ignored as in Calimero pre-2.4.0
        } finally {
            if (destination != null) {
                destination.destroy();
            }
        }
    }

    @Override
    public void readDatapoint(Datapoint datapoint) {
        synchronized (this) {
            ReadDatapoint retryDatapoint = new ReadDatapoint(datapoint, readRetriesLimit);
            if (!readDatapoints.contains(retryDatapoint)) {
                readDatapoints.add(retryDatapoint);
            }
        }
    }

    @Override
    public final boolean registerGroupAddressListener(GroupAddressListener listener) {
        return groupAddressListeners.add(listener);
    }

    @Override
    public final boolean unregisterGroupAddressListener(GroupAddressListener listener) {
        return groupAddressListeners.remove(listener);
    }

    @Override
    public boolean isConnected() {
        final var tmpLink = link;
        return tmpLink != null && tmpLink.isOpen();
    }

    @Override
    public DeviceInfoClient getDeviceInfoClient() {
        DeviceInfoClient deviceInfoClient = this.deviceInfoClient;
        if (deviceInfoClient != null) {
            return deviceInfoClient;
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public void writeToKNX(OutboundSpec commandSpec) throws KNXException {
        ProcessCommunicator processCommunicator = this.processCommunicator;
        KNXNetworkLink link = this.link;
        if (processCommunicator == null || link == null) {
            logger.debug("Cannot write to KNX bus (processCommuicator: {}, link: {})",
                    processCommunicator == null ? "Not OK" : "OK",
                    link == null ? "Not OK" : (link.isOpen() ? "Open" : "Closed"));
            return;
        }
        GroupAddress groupAddress = commandSpec.getGroupAddress();

        logger.trace("writeToKNX groupAddress '{}', commandSpec '{}'", groupAddress, commandSpec);

        if (groupAddress != null) {
            sendToKNX(processCommunicator, link, groupAddress, commandSpec.getDPT(), commandSpec.getType());
        }
    }

    @Override
    public void respondToKNX(OutboundSpec responseSpec) throws KNXException {
        ProcessCommunicationResponder responseCommunicator = this.responseCommunicator;
        KNXNetworkLink link = this.link;
        if (responseCommunicator == null || link == null) {
            logger.debug("Cannot write to KNX bus (responseCommunicator: {}, link: {})",
                    responseCommunicator == null ? "Not OK" : "OK",
                    link == null ? "Not OK" : (link.isOpen() ? "Open" : "Closed"));
            return;
        }
        GroupAddress groupAddress = responseSpec.getGroupAddress();

        logger.trace("respondToKNX groupAddress '{}', responseSpec '{}'", groupAddress, responseSpec);

        if (groupAddress != null) {
            sendToKNX(responseCommunicator, link, groupAddress, responseSpec.getDPT(), responseSpec.getType());
        }
    }

    private void sendToKNX(ProcessCommunication communicator, KNXNetworkLink link, GroupAddress groupAddress,
            String dpt, Type type) throws KNXException {
        if (!connectIfNotAutomatic()) {
            return;
        }

        Datapoint datapoint = new CommandDP(groupAddress, thingUID.toString(), 0, dpt);
        String mappedValue = toDPTValue(type, dpt);

        logger.trace("sendToKNX mappedValue: '{}' groupAddress: '{}'", mappedValue, groupAddress);

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
                    logger.debug("Value '{}' could not be sent to KNX bus using datapoint '{}': {}. Will retry.", type,
                            datapoint, e.getLocalizedMessage());
                } else {
                    logger.warn("Value '{}' could not be sent to KNX bus using datapoint '{}': {}. Giving up now.",
                            type, datapoint, e.getLocalizedMessage());
                    throw e;
                }
            }
        }
    }
}
