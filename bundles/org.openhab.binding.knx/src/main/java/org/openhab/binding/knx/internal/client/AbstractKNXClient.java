/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static org.openhab.binding.knx.internal.dpt.DPTUtil.NORMALIZED_DPT;

import java.time.Duration;
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
import org.openhab.binding.knx.internal.dpt.ValueEncoder;
import org.openhab.binding.knx.internal.handler.GroupAddressListener;
import org.openhab.binding.knx.internal.handler.KNXBridgeBaseThingHandler;
import org.openhab.binding.knx.internal.handler.KNXBridgeBaseThingHandler.CommandExtensionData;
import org.openhab.binding.knx.internal.i18n.KNXTranslationProvider;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.CloseEvent;
import tuwien.auto.calimero.DataUnitBuilder;
import tuwien.auto.calimero.DetachEvent;
import tuwien.auto.calimero.FrameEvent;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.KNXAddress;
import tuwien.auto.calimero.KNXException;
import tuwien.auto.calimero.KNXIllegalArgumentException;
import tuwien.auto.calimero.cemi.CEMILData;
import tuwien.auto.calimero.cemi.CemiTData;
import tuwien.auto.calimero.datapoint.CommandDP;
import tuwien.auto.calimero.datapoint.Datapoint;
import tuwien.auto.calimero.device.ProcessCommunicationResponder;
import tuwien.auto.calimero.link.KNXNetworkLink;
import tuwien.auto.calimero.link.NetworkLinkListener;
import tuwien.auto.calimero.mgmt.Destination;
import tuwien.auto.calimero.mgmt.ManagementClient;
import tuwien.auto.calimero.mgmt.ManagementProcedures;
import tuwien.auto.calimero.mgmt.TransportLayerImpl;
import tuwien.auto.calimero.process.ProcessCommunication;
import tuwien.auto.calimero.process.ProcessCommunicator;
import tuwien.auto.calimero.process.ProcessCommunicatorImpl;
import tuwien.auto.calimero.process.ProcessEvent;
import tuwien.auto.calimero.process.ProcessListener;
import tuwien.auto.calimero.secure.KnxSecureException;
import tuwien.auto.calimero.secure.SecureApplicationLayer;
import tuwien.auto.calimero.secure.Security;

/**
 * KNX Client which encapsulates the communication with the KNX bus via the calimero library.
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

    private final ThingUID thingUID;
    private final int responseTimeout;
    private final int readingPause;
    private final int autoReconnectPeriod;
    private final int readRetriesLimit;
    private final StatusUpdateCallback statusUpdateCallback;
    private final ScheduledExecutorService knxScheduler;
    private final CommandExtensionData commandExtensionData;
    protected final Security openhabSecurity;

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
            processEvent("Group Write", e, (listener, source, destination, asdu) -> listener
                    .onGroupWrite(AbstractKNXClient.this, source, destination, asdu));
        }

        @Override
        public void groupReadRequest(ProcessEvent e) {
            processEvent("Group Read Request", e, (listener, source, destination, asdu) -> listener
                    .onGroupRead(AbstractKNXClient.this, source, destination, asdu));
        }

        @Override
        public void groupReadResponse(ProcessEvent e) {
            processEvent("Group Read Response", e, (listener, source, destination, asdu) -> listener
                    .onGroupReadResponse(AbstractKNXClient.this, source, destination, asdu));
        }
    };

    public AbstractKNXClient(int autoReconnectPeriod, ThingUID thingUID, int responseTimeout, int readingPause,
            int readRetriesLimit, ScheduledExecutorService knxScheduler, CommandExtensionData commandExtensionData,
            Security openhabSecurity, StatusUpdateCallback statusUpdateCallback) {
        this.autoReconnectPeriod = autoReconnectPeriod;
        this.thingUID = thingUID;
        this.responseTimeout = responseTimeout;
        this.readingPause = readingPause;
        this.readRetriesLimit = readRetriesLimit;
        this.knxScheduler = knxScheduler;
        this.statusUpdateCallback = statusUpdateCallback;
        this.commandExtensionData = commandExtensionData;
        this.openhabSecurity = openhabSecurity;
    }

    public void initialize() {
        connect();
    }

    private void scheduleReconnectJob() {
        if (autoReconnectPeriod > 0) {
            // schedule connect job, for the first connection ignore autoReconnectPeriod and use 1 sec
            final long reconnectDelayS = (state == ClientState.INIT) ? 1 : autoReconnectPeriod;
            final String prefix = (state == ClientState.INIT) ? "re" : "";
            logger.debug("Bridge {} scheduling {}connect in {}s", thingUID, prefix, reconnectDelayS);
            connectJob = knxScheduler.schedule(this::connect, reconnectDelayS, TimeUnit.SECONDS);
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
            return connectJob == null && connect();
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

            // one transport layer implementation, to be shared by all following classes
            TransportLayerImpl tl = new TransportLayerImpl(link);

            // new SecureManagement / SecureApplicationLayer, based on the keyring (if any)
            // SecureManagement does not offer a public ctor which can use a given TL.
            // Protected ctor using given TransportLayerImpl is available (custom class to be inherited)
            // which also copies the relevant content of the supplied SAL to a new SAL instance created
            // by SecureManagement ctor.
            CustomSecureManagement sal = new CustomSecureManagement(tl, openhabSecurity);

            logger.debug("GAs: {}  Send: {}, S={}", sal.security().groupKeys().size(),
                    sal.security().groupSenders().size(),
                    KNXBridgeBaseThingHandler.secHelperGetSecureGroupAddresses(sal.security()));

            // ManagementClient provided by Calimero: allow reading device info, etc.
            // Note for KNX Secure: ManagementClientImpl does not provide a ctor with external SAL in Calimero 2.5.
            // Protected ctor using given ManagementClientImpl is available in >2.5 (custom class to be inherited)
            ManagementClient managementClient = new CustomManagementClientImpl(link, sal);
            managementClient.responseTimeout(Duration.ofSeconds(responseTimeout));
            this.managementClient = managementClient;

            // ManagementProcedures provided by Calimero: allow managing other KNX devices, e.g. check if an address is
            // reachable.
            // Note for KNX Secure: ManagementProceduresImpl currently does not provide a public ctor with external SAL.
            // Protected ctor using given ManagementClientImpl is available (custom class to be inherited)
            managementProcedures = new CustomManagementProceduresImpl(managementClient, tl);

            // OpenHab helper for reading device info, based on managementClient above
            deviceInfoClient = new DeviceInfoClientImpl(managementClient);

            // ProcessCommunicator provides main KNX communication (Calimero).
            final boolean useGoDiagnostics = true;
            ProcessCommunicator processCommunicator = new ProcessCommunicatorImpl(link, sal, useGoDiagnostics);
            processCommunicator.responseTimeout(Duration.ofSeconds(responseTimeout));
            processCommunicator.addProcessListener(processListener);
            this.processCommunicator = processCommunicator;

            // ProcessCommunicationResponder provides responses to requests from KNX bus (Calimero).
            ProcessCommunicationResponder responseCommunicator = new ProcessCommunicationResponder(link, sal);
            this.responseCommunicator = responseCommunicator;

            // register this class, callbacks will be triggered
            link.addLinkListener(this);

            // create a job carrying out read requests
            busJob = knxScheduler.scheduleWithFixedDelay(this::readNextQueuedDatapoint, 0, readingPause,
                    TimeUnit.MILLISECONDS);

            statusUpdateCallback.updateStatus(ThingStatus.ONLINE);
            connectJob = null;

            logger.info("Bridge {} connected to KNX bus", thingUID);

            state = ClientState.RUNNING;
            return true;
        } catch (InterruptedException e) {
            ClientState lastState = state;
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
            disconnect(e, ThingStatusDetail.CONFIGURATION_ERROR);
            return false;
        }
    }

    private synchronized void disconnect(@Nullable Exception e) {
        disconnect(e, null);
    }

    private synchronized void disconnect(@Nullable Exception e, @Nullable ThingStatusDetail detail) {
        releaseConnection();
        if (e != null) {
            statusUpdateCallback.updateStatus(ThingStatus.OFFLINE,
                    detail != null ? detail : ThingStatusDetail.COMMUNICATION_ERROR,
                    KNXTranslationProvider.I18N.getLocalizedException(e));
        } else {
            statusUpdateCallback.updateStatus(ThingStatus.OFFLINE);
        }
    }

    protected void releaseConnection() {
        logger.debug("Bridge {} is disconnecting from KNX bus", thingUID);
        var tmpLink = link;
        if (tmpLink != null) {
            tmpLink.removeLinkListener(this);
        }
        readDatapoints.clear();
        busJob = nullify(busJob, j -> j.cancel(true));
        deviceInfoClient = null;
        managementProcedures = nullify(managementProcedures, ManagementProcedures::detach);
        managementClient = nullify(managementClient, ManagementClient::detach);
        processCommunicator = nullify(processCommunicator, pc -> {
            pc.removeProcessListener(processListener);
            pc.detach();
        });
        responseCommunicator = nullify(responseCommunicator, rc -> {
            rc.removeProcessListener(processListener);
            rc.detach();
        });
        link = nullify(link, KNXNetworkLink::close);
        logger.trace("Bridge {} disconnected from KNX bus", thingUID);
    }

    private <T> @Nullable T nullify(@Nullable T target, @Nullable Consumer<T> lastWill) {
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
        boolean isHandled = false;
        for (GroupAddressListener listener : groupAddressListeners) {
            if (listener.listensTo(destination)) {
                isHandled = true;
                knxScheduler.schedule(() -> action.apply(listener, source, destination, asdu), 0, TimeUnit.SECONDS);
            }
        }
        // Store information about unhandled GAs, can be shown on console using knx:list-unknown-ga.
        // The idea is to store GA, message type, and size as key. The value counts the number of packets.
        if (!isHandled) {
            logger.trace("Address '{}' is not configured in openHAB", destination);
            final String type = switch (event.getServiceCode()) {
                case 0x80 -> "GROUP_WRITE";
                case 0x40 -> "GROUP_RESPONSE";
                case 0x00 -> "GROUP_READ";
                default -> "?";
            };
            final String key = String.format("%2d/%1d/%3d  %s(%02d)", destination.getMainGroup(),
                    destination.getMiddleGroup(), destination.getSubGroup8(), type, event.getASDU().length);
            commandExtensionData.unknownGA().compute(key, (k, v) -> v == null ? 1 : v + 1);
        }
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
            // TODO #8872: allow write access, currently only listening mode
            if (openhabSecurity.groupKeys().containsKey(datapoint.getDatapoint().getMainAddress())) {
                logger.debug("outgoing secure communication not implemented, explicit read from GA '{}' skipped",
                        datapoint.getDatapoint().getMainAddress());
                return;
            }

            datapoint.incrementRetries();
            try {
                logger.trace("Sending a Group Read Request telegram for {}", datapoint.getDatapoint().getMainAddress());
                processCommunicator.read(datapoint.getDatapoint());
            } catch (KNXException e) {
                // Note: KnxException does not cover KnxRuntimeException and subclasses KnxSecureException,
                // KnxIllegalArgumentException
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
        // NetworkLinkListener indication. This implementation is triggered whenever a frame is received.
        // It is not necessary for OH, as we process incoming group writes via different triggers.
        // However, this indication also covers encrypted data secure frames, which would typically
        // be dropped silently by the Calimero library (a log message is only visible when log level for Calimero
        // is set manually).

        // Implementation searches for incoming data secure frames which cannot be decoded due to missing key
        if (e != null) {
            final var cemi = e.getFrame();
            if (!(cemi instanceof CemiTData)) {
                final CEMILData f = (CEMILData) cemi;
                final int ctrl = f.getPayload()[0] & 0xfc;
                if (ctrl == 0) {
                    final KNXAddress dst = f.getDestination();
                    if (dst instanceof GroupAddress ga) {
                        if (dst.getRawAddress() != 0) {
                            final byte[] payload = f.getPayload();
                            final int service = DataUnitBuilder.getAPDUService(payload);
                            if (service == SecureApplicationLayer.SecureService) {
                                if (!openhabSecurity.groupKeys().containsKey(dst)) {
                                    logger.trace("Address '{}' cannot be decrypted, group key missing", dst);
                                    final String key = String.format(
                                            "%2d/%1d/%3d  secure: missing group key, cannot decrypt", ga.getMainGroup(),
                                            ga.getMiddleGroup(), ga.getSubGroup8());
                                    commandExtensionData.unknownGA().compute(key, (k, v) -> v == null ? 1 : v + 1);
                                }
                            }
                        }
                    }
                }
            }
        }
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
    public final void registerGroupAddressListener(GroupAddressListener listener) {
        groupAddressListeners.add(listener);
    }

    @Override
    public final void unregisterGroupAddressListener(GroupAddressListener listener) {
        groupAddressListeners.remove(listener);
    }

    @Override
    public boolean isConnected() {
        KNXNetworkLink tmpLink = link;
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
            logger.debug("Cannot write to KNX bus (processCommunicator: {}, link: {})",
                    processCommunicator == null ? "Not OK" : "OK",
                    link == null ? "Not OK" : (link.isOpen() ? "Open" : "Closed"));
            return;
        }
        GroupAddress groupAddress = commandSpec.getGroupAddress();

        logger.trace("writeToKNX groupAddress '{}', commandSpec '{}:{} {}'", groupAddress, groupAddress,
                commandSpec.getDPT(), commandSpec.getValue());

        sendToKNX(processCommunicator, groupAddress, commandSpec.getDPT(), commandSpec.getValue());
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

        sendToKNX(responseCommunicator, groupAddress, responseSpec.getDPT(), responseSpec.getValue());
    }

    private void sendToKNX(ProcessCommunication communicator, GroupAddress groupAddress, String dpt, Type type)
            throws KNXException {
        if (!connectIfNotAutomatic()) {
            return;
        }

        // TODO #8872: allow write access, currently only listening mode
        if (openhabSecurity.groupKeys().containsKey(groupAddress)) {
            logger.debug("outgoing secure communication not implemented, write to GA '{}' skipped", groupAddress);
            return;
        }

        Datapoint datapoint = new CommandDP(groupAddress, thingUID.toString(), 0,
                NORMALIZED_DPT.getOrDefault(dpt, dpt));
        String mappedValue = ValueEncoder.encode(type, dpt);
        if (mappedValue == null) {
            logger.debug("Value '{}' of type '{}' cannot be mapped to datapoint '{}'", type, type.getClass(),
                    datapoint);
            return;
        }
        logger.trace("sendToKNX mappedValue: '{}' groupAddress: '{}'", mappedValue, groupAddress);

        for (int i = 0;; i++) {
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
