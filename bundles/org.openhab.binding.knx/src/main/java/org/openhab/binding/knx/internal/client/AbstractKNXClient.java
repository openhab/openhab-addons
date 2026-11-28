/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.dpt.ValueEncoder;
import org.openhab.binding.knx.internal.handler.GroupAddressListener;
import org.openhab.binding.knx.internal.handler.KNXBridgeBaseThingHandler;
import org.openhab.binding.knx.internal.handler.KNXBridgeBaseThingHandler.CommandExtensionData;
import org.openhab.binding.knx.internal.i18n.KNXTranslationProvider;
import org.openhab.binding.knx.internal.monitor.ConnectionKind;
import org.openhab.binding.knx.internal.monitor.DataType;
import org.openhab.binding.knx.internal.monitor.IncomingTap;
import org.openhab.binding.knx.internal.monitor.MonitorDecoder;
import org.openhab.binding.knx.internal.monitor.NotDecryptedReason;
import org.openhab.binding.knx.internal.monitor.OwnSend;
import org.openhab.binding.knx.internal.monitor.SecurityState;
import org.openhab.binding.knx.internal.monitor.SendOutcome;
import org.openhab.binding.knx.internal.monitor.TelegramMonitor;
import org.openhab.binding.knx.internal.monitor.TelegramMonitor.NotDecryptedWarning;
import org.openhab.binding.knx.internal.monitor.TelegramType;
import org.openhab.binding.knx.internal.security.KeyringFacts;
import org.openhab.binding.knx.internal.security.SecureSenderCheck;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.calimero.CloseEvent;
import io.calimero.DataUnitBuilder;
import io.calimero.DetachEvent;
import io.calimero.FrameEvent;
import io.calimero.GroupAddress;
import io.calimero.IndividualAddress;
import io.calimero.KNXAddress;
import io.calimero.KNXException;
import io.calimero.KNXIllegalArgumentException;
import io.calimero.KNXTimeoutException;
import io.calimero.cemi.CEMILData;
import io.calimero.cemi.CemiTData;
import io.calimero.datapoint.CommandDP;
import io.calimero.datapoint.Datapoint;
import io.calimero.device.ProcessCommunicationResponder;
import io.calimero.dptxlator.TranslatorTypes;
import io.calimero.link.KNXLinkClosedException;
import io.calimero.link.KNXNetworkLink;
import io.calimero.link.NetworkLinkListener;
import io.calimero.link.medium.KNXMediumSettings;
import io.calimero.mgmt.Destination;
import io.calimero.mgmt.ManagementClient;
import io.calimero.mgmt.ManagementProcedures;
import io.calimero.mgmt.TransportLayerImpl;
import io.calimero.process.ProcessCommunication;
import io.calimero.process.ProcessCommunicator;
import io.calimero.process.ProcessEvent;
import io.calimero.process.ProcessListener;
import io.calimero.secure.KnxSecureException;
import io.calimero.secure.SecureApplicationLayer;
import io.calimero.secure.Security;

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
    // TPCI + APCI + SCF + sequence number + one byte of APDU + MAC; shorter frames the library reports itself
    private static final int SECURE_FRAME_MIN_LENGTH = 14;

    private final Logger logger = LoggerFactory.getLogger(AbstractKNXClient.class);

    private final ThingUID thingUID;
    private final int responseTimeout;
    private final int readingPause;
    private final int autoReconnectPeriod;
    private final int readRetriesLimit;
    private final StatusUpdateCallback statusUpdateCallback;
    private final ScheduledExecutorService knxScheduler;
    private final CommandExtensionData commandExtensionData;
    private final TelegramMonitor telegramMonitor;
    protected final Security openhabSecurity;
    private final Supplier<@Nullable KeyringFacts> keyringFacts;

    private volatile @Nullable SecureSenderCheck senderCheck;
    private @Nullable ReadTrackingCommunicator processCommunicator;
    private @Nullable ProcessCommunicationResponder responseCommunicator;
    // A Data Secure receiver drops a telegram whose sequence number is not above the last one it accepted, so
    // commands, reads and read responses must reach the link in the order their numbers were taken.
    private final Lock sendLock = new ReentrantLock();
    private @Nullable ManagementProcedures managementProcedures;
    private @Nullable ManagementClient managementClient;
    private @Nullable KNXNetworkLink link;
    private @Nullable DeviceInfoClient deviceInfoClient;
    private @Nullable ScheduledFuture<?> busJob;
    private @Nullable ScheduledFuture<?> connectJob;
    private @Nullable CustomSecureManagement tappedLayer;
    private @Nullable IncomingTap incomingTap;

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
            TelegramMonitor telegramMonitor, Security openhabSecurity, Supplier<@Nullable KeyringFacts> keyringFacts,
            StatusUpdateCallback statusUpdateCallback) {
        this.autoReconnectPeriod = autoReconnectPeriod;
        this.thingUID = thingUID;
        this.responseTimeout = responseTimeout;
        this.readingPause = readingPause;
        this.readRetriesLimit = readRetriesLimit;
        this.knxScheduler = knxScheduler;
        this.statusUpdateCallback = statusUpdateCallback;
        this.commandExtensionData = commandExtensionData;
        this.telegramMonitor = telegramMonitor;
        this.openhabSecurity = openhabSecurity;
        this.keyringFacts = keyringFacts;
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

    /** Gives the link of the previous connection a moment to go down; tests skip it. */
    protected void pauseBeforeConnecting() throws InterruptedException {
        Thread.sleep(1000);
    }

    /** {@return how this client reaches the bus, for the group monitor} */
    protected abstract ConnectionKind connectionKind();

    CustomSecureManagement newSecureManagement(TransportLayerImpl transportLayer) throws KNXLinkClosedException {
        return new CustomSecureManagement(transportLayer, openhabSecurity);
    }

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
            pauseBeforeConnecting();
            logger.debug("Bridge {} is connecting to KNX bus", thingUID);

            // now establish (possibly encrypted) connection, according to settings (tunnel, routing, secure...)
            KNXNetworkLink link = establishConnection();
            this.link = link;
            SecureSenderCheck senderCheck = newSenderCheck(link);
            this.senderCheck = senderCheck;

            // one transport layer implementation, to be shared by all following classes
            TransportLayerImpl tl = new TransportLayerImpl(link);

            // new SecureManagement / SecureApplicationLayer, based on the keyring (if any)
            // SecureManagement does not offer a public ctor which can use a given TL.
            // Protected ctor using given TransportLayerImpl is available (custom class to be inherited)
            // which also copies the relevant content of the supplied SAL to a new SAL instance created
            // by SecureManagement ctor.
            CustomSecureManagement sal = newSecureManagement(tl);
            attachTap(sal);

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
            ReadTrackingCommunicator processCommunicator = newProcessCommunicator(link, sal, sendLock);
            processCommunicator.responseTimeout(Duration.ofSeconds(responseTimeout));
            processCommunicator.addProcessListener(processListener);
            this.processCommunicator = processCommunicator;

            // ProcessCommunicationResponder provides responses to requests from KNX bus (Calimero).
            ProcessCommunicationResponder responseCommunicator = newResponseCommunicator(link, sal, sendLock);
            this.responseCommunicator = responseCommunicator;

            // register this class, callbacks will be triggered
            link.addLinkListener(this);

            // create a job carrying out read requests
            busJob = knxScheduler.scheduleWithFixedDelay(this::readNextQueuedDatapoint, 0, readingPause,
                    TimeUnit.MILLISECONDS);

            publishOnline(senderCheck);
            connectJob = null;
            telegramMonitor.useChannelTypes(address -> monitorDataType(groupAddressListeners, address));
            telegramMonitor.connected(connectionKind(), ownAddressOf(link.getKNXMedium()));

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

    /**
     * The address openHAB sends from: the one the router assigned to a tunnel, else the configured one (always the
     * case in router mode).
     */
    static IndividualAddress ownAddressOf(KNXMediumSettings settings) {
        try {
            return settings.assignedAddress().orElseGet(settings::getDeviceAddress);
        } catch (RuntimeException e) {
            // the library's supplier throws when the gateway's connect response carried no tunnel address
            return settings.getDeviceAddress();
        }
    }

    private @Nullable SecureSenderCheck newSenderCheck(KNXNetworkLink link) {
        KeyringFacts facts = keyringFacts.get();
        if (facts == null || facts.securedGroupAddresses().isEmpty()) {
            return null;
        }
        SecureSenderCheck check = new SecureSenderCheck(facts, ownAddressOf(link.getKNXMedium()));
        if (check.ownAddressNotInKeyring()) {
            logger.warn(
                    "Bridge {}: the keyring has no interface {}. Devices will ignore what openHAB sends to secured "
                            + "group addresses. Add it in ETS, download, and export the keyring again.",
                    thingUID, check.ownAddress());
        } else if (check.ownAddressLinkedToNothing()) {
            logger.warn(
                    "Bridge {}: ETS has not linked {} to any secured group address. Devices will ignore what "
                            + "openHAB sends to them. Link it in ETS, download, and export the keyring again.",
                    thingUID, check.ownAddress());
        }
        return check;
    }

    /** Sending is never held back: this only records what the telegram is about to do. */
    private void noteSecuredSend(GroupAddress groupAddress) {
        SecureSenderCheck check = senderCheck;
        if (check != null && check.noteSend(groupAddress)) {
            logger.warn(
                    "Bridge {}: ETS has not linked {} to {}. Devices will ignore what openHAB sends there. Link it "
                            + "in ETS, download, and export the keyring again.",
                    thingUID, check.ownAddress(), groupAddress);
            try {
                publishOnline(check);
            } catch (RuntimeException e) {
                // an exception here would skip the telegram and, in the read job, end the job for every datapoint
                logger.debug("Bridge {} could not publish the warning: {}", thingUID, e.getMessage());
            }
        }
    }

    /**
     * Publishes ONLINE with whatever the check has found so far. Synchronized with connect and disconnect, so a
     * telegram of an old connection cannot bring a warning back after it was released or lost.
     */
    private synchronized void publishOnline(@Nullable SecureSenderCheck check) {
        if (check != null && (!check.equals(senderCheck) || !isConnected())) {
            return;
        }
        String description = check == null ? null : check.statusDescription();
        if (description == null) {
            statusUpdateCallback.updateStatus(ThingStatus.ONLINE);
        } else {
            statusUpdateCallback.updateStatus(ThingStatus.ONLINE, description);
        }
    }

    private synchronized void disconnect(@Nullable Exception e) {
        disconnect(e, null);
    }

    private synchronized void disconnect(@Nullable Exception e, @Nullable ThingStatusDetail detail) {
        releaseConnection();
        if (e != null) {
            statusUpdateCallback.updateStatus(detail != null ? detail : ThingStatusDetail.COMMUNICATION_ERROR,
                    KNXTranslationProvider.I18N.getLocalizedException(e));
        } else {
            statusUpdateCallback.updateStatus(ThingStatus.OFFLINE);
        }
    }

    /** One listener on the secure layer of this connection feeds the group monitor with received telegrams. */
    private void attachTap(CustomSecureManagement sal) {
        telegramMonitor.resetNotDecrypted();
        sal.onFailure(this::securityFailed);
        IncomingTap tap = new IncomingTap(telegramMonitor, openhabSecurity.groupKeys()::containsKey);
        sal.addListener(tap);
        tappedLayer = sal;
        incomingTap = tap;
    }

    private void detachTap() {
        CustomSecureManagement sal = tappedLayer;
        IncomingTap tap = incomingTap;
        if (sal != null) {
            sal.onFailure(null);
            if (tap != null) {
                sal.removeListener(tap);
            }
        }
        tappedLayer = null;
        incomingTap = null;
        telegramMonitor.disconnected();
    }

    private void securityFailed(int errorType, IndividualAddress source, KNXAddress destination) {
        reportNotDecrypted(source, destination, NotDecryptedReason.ofLibraryErrorType(errorType));
    }

    /**
     * One row and one count for a secured telegram that was not decrypted; only group destinations other than 0/0/0.
     */
    private void reportNotDecrypted(IndividualAddress source, KNXAddress destination,
            @Nullable NotDecryptedReason reason) {
        if (!(destination instanceof GroupAddress group) || group.getRawAddress() == 0) {
            return;
        }
        try {
            NotDecryptedWarning warning = telegramMonitor.notDecrypted(source, group, reason, this::usedByChannel);
            if (warning != null) {
                logger.warn(
                        "Bridge {}: secured telegrams to {} from {} could not be decrypted ({}). Update the sending "
                                + "devices in ETS, download, and export the keyring again.",
                        thingUID, group,
                        warning.senders().stream().map(IndividualAddress::toString).collect(Collectors.joining(", ")),
                        reason == null ? "unknown reason" : reason);
            }
        } catch (RuntimeException e) {
            logger.debug("The group monitor could not record a secured telegram: {}", e.toString());
        }
    }

    private boolean usedByChannel(GroupAddress address) {
        return groupAddressListeners.stream().anyMatch(listener -> listener.listensTo(address));
    }

    /**
     * Finds what the library drops without a word, from the raw frame, and in the order the library checks: frame
     * length and control field belong to the library's own report; with no group key the library is silent; tool
     * access on a group address is the library's again; then the sender list. It looks only at the frame, the keys
     * and the sender list, never at a sequence number, so it does not matter which listener runs first. Must never
     * throw: a listener that does is removed by the link.
     */
    private void checkRawSecuredFrame(CEMILData frame, GroupAddress destination, byte[] payload) {
        try {
            CustomSecureManagement sal = tappedLayer;
            KNXNetworkLink tmpLink = link;
            if (sal == null || tmpLink == null) {
                return;
            }
            IndividualAddress own = tmpLink.getKNXMedium().getDeviceAddress();
            IndividualAddress source = frame.getSource();
            // the transport layer never hands the library a frame from our own address
            if (source.equals(own) || payload.length < SECURE_FRAME_MIN_LENGTH) {
                return;
            }
            int scf = payload[2] & 0xff;
            boolean toolAccess = (scf & 0x80) != 0;
            boolean systemBroadcast = (scf & 0x08) != 0;
            int algorithm = (scf >> 4) & 0x7;
            int service = scf & 0x7;
            if (algorithm > 1 || service == 1 || service > 3 || (systemBroadcast && !toolAccess)) {
                return;
            }
            // only a secure data PDU is a secured group telegram; sync services are not
            if (service != 0) {
                return;
            }
            Security security = sal.security();
            if (!security.groupKeys().containsKey(destination)) {
                reportNotDecrypted(source, destination, NotDecryptedReason.NO_KEY);
                return;
            }
            if (toolAccess) {
                return;
            }
            Set<IndividualAddress> senders = security.groupSenders(own).get(destination);
            if (senders != null && !senders.isEmpty() && !senders.contains(source)) {
                reportNotDecrypted(source, destination, NotDecryptedReason.SENDER_NOT_LISTED);
            }
        } catch (RuntimeException e) {
            logger.debug("The group monitor could not check a secured frame: {}", e.toString());
        }
    }

    protected void releaseConnection() {
        logger.debug("Bridge {} is disconnecting from KNX bus", thingUID);
        detachTap();
        var tmpLink = link;
        if (tmpLink != null) {
            tmpLink.removeLinkListener(this);
        }
        senderCheck = null;
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
        ReadTrackingCommunicator processCommunicator = this.processCommunicator;
        if (processCommunicator == null) {
            return;
        }
        ReadDatapoint datapoint = readDatapoints.poll();
        if (datapoint != null) {
            datapoint.incrementRetries();
            noteSecuredSend(datapoint.getDatapoint().getMainAddress());
            // recorded before the call: the answer arrives while the call waits for it
            OwnSend request = recordReadRequest(datapoint.getDatapoint());
            AtomicBoolean requestWentOut = new AtomicBoolean();
            SendOutcome outcome = SendOutcome.FAILED;
            try {
                logger.trace("Sending a Group Read Request telegram for {}", datapoint.getDatapoint().getMainAddress());
                processCommunicator.read(datapoint.getDatapoint(), () -> requestWentOut.set(true));
                outcome = SendOutcome.SENT;
            } catch (KNXException e) {
                outcome = readOutcome(e, requestWentOut.get());
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
                outcome = readOutcome(e, requestWentOut.get());
                logger.debug("Interrupted sending KNX read request");
            } catch (Exception e) {
                outcome = readOutcome(e, requestWentOut.get());
                // Any other exception: Fail gracefully, i.e. notify user and continue reading next DP.
                // Not catching this would end the scheduled read for all DPs in case of an error.
                // Severity is warning as this is likely caused by a configuration error.
                logger.warn("Error reading datapoint {}: {}", datapoint.getDatapoint().getMainAddress(),
                        e.getMessage());
            } finally {
                endSend(request, outcome);
            }
        }
    }

    /**
     * The library sends the request and then waits for the answer in the same call (ProcessCommunicatorImpl
     * readFromGroup), and both a missing confirmation of the request and a missing answer end in a
     * KNXTimeoutException; only the moment the request went out tells them apart.
     */
    static SendOutcome readOutcome(Exception e, boolean requestWentOut) {
        if (requestWentOut) {
            return e instanceof KNXTimeoutException ? SendOutcome.NO_ANSWER : SendOutcome.SENT;
        }
        return confirmationTimedOut(e) ? SendOutcome.NOT_CONFIRMED : SendOutcome.FAILED;
    }

    /** The library turns an interrupted write into a KNXTimeoutException too, and sets the interrupt flag again. */
    private static boolean confirmationTimedOut(Exception e) {
        return e instanceof KNXTimeoutException && !Thread.currentThread().isInterrupted();
    }

    private IndividualAddress monitorSource() {
        KNXNetworkLink tmpLink = link;
        return tmpLink == null ? new IndividualAddress(0) : ownAddressOf(tmpLink.getKNXMedium());
    }

    private SecurityState monitorSecurity(GroupAddress destination) {
        return openhabSecurity.groupKeys().containsKey(destination) ? SecurityState.SECURED : SecurityState.PLAIN;
    }

    private OwnSend recordReadRequest(Datapoint datapoint) {
        try {
            GroupAddress destination = datapoint.getMainAddress();
            return telegramMonitor.sending(monitorSource(), destination, TelegramType.READ,
                    datapoint.dptId().toString(), null, monitorSecurity(destination));
        } catch (RuntimeException e) {
            logger.debug("The group monitor could not record a read request: {}", e.toString());
            return OwnSend.NONE;
        }
    }

    private void endSend(OwnSend send, SendOutcome outcome) {
        try {
            send.ended(outcome);
        } catch (RuntimeException e) {
            logger.debug("The group monitor could not record an outcome: {}", e.toString());
        }
    }

    private void recordSend(GroupAddress destination, TelegramType type, byte[] payload, String dpt, Type value,
            SendOutcome outcome) {
        try {
            telegramMonitor.sent(monitorSource(), destination, type, payload, dpt, MonitorDecoder.text(value),
                    monitorSecurity(destination), outcome);
        } catch (RuntimeException e) {
            logger.debug("The group monitor could not record a telegram: {}", e.toString());
        }
    }

    /** {@return the value bytes the library builds for the frame, as it does inside write; empty if it cannot} */
    private static byte[] payloadOf(Datapoint datapoint, String mappedValue) {
        try {
            return TranslatorTypes.createTranslator(datapoint.dptId(), mappedValue).getData();
        } catch (KNXException | RuntimeException e) {
            return new byte[0];
        }
    }

    static ReadTrackingCommunicator newProcessCommunicator(KNXNetworkLink link, SecureApplicationLayer sal,
            Lock sendLock) throws KNXLinkClosedException {
        return new ReadTrackingCommunicator(link, sal, sendLock);
    }

    static ProcessCommunicationResponder newResponseCommunicator(KNXNetworkLink link, SecureApplicationLayer sal,
            Lock sendLock) throws KNXLinkClosedException {
        return new ProcessCommunicationResponder(link, sal) {
            @Override
            @NonNullByDefault({})
            public void write(final Datapoint dp, final String value) throws KNXException {
                sendLock.lock();
                try {
                    super.write(dp, value);
                } finally {
                    sendLock.unlock();
                }
            }
        };
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
            // a send that is already under way must not report ONLINE for a link that is gone
            senderCheck = null;
            final String reason = closeEvent.getReason();
            statusUpdateCallback.updateStatus(ThingStatusDetail.COMMUNICATION_ERROR,
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
                                checkRawSecuredFrame(f, ga, payload);
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
    public Optional<IndividualAddress> ownAddress() {
        KNXNetworkLink tmpLink = link;
        return tmpLink == null ? Optional.empty() : Optional.of(ownAddressOf(tmpLink.getKNXMedium()));
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
    public void readOnce(Datapoint datapoint) {
        synchronized (this) {
            // a read of the same address that waits in the queue already goes out in its place
            ReadDatapoint once = new ReadDatapoint(datapoint, 1);
            if (!readDatapoints.contains(once)) {
                readDatapoints.add(once);
            }
        }
    }

    @Override
    public void readDatapoint(Datapoint datapoint) {
        synchronized (this) {
            ReadDatapoint retryDatapoint = new ReadDatapoint(datapoint, readRetriesLimit);
            // a waiting read of the address with fewer tries (readOnce) gives way: this one goes out for both
            if (!readDatapoints.removeIf(
                    waiting -> waiting.equals(retryDatapoint) && waiting.getLimit() < retryDatapoint.getLimit())
                    && readDatapoints.contains(retryDatapoint)) {
                return;
            }
            readDatapoints.add(retryDatapoint);
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

    /** {@return the data type the first channel that uses the address gives it, null if none does} */
    static @Nullable DataType monitorDataType(Iterable<GroupAddressListener> listeners, GroupAddress destination) {
        for (GroupAddressListener listener : listeners) {
            if (listener.listensTo(destination)) {
                DataType dataType = listener.monitorDataType(destination);
                if (dataType != null) {
                    return dataType;
                }
            }
        }
        return null;
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
    public boolean writeToKNX(OutboundSpec commandSpec) throws KNXException {
        ProcessCommunicator processCommunicator = this.processCommunicator;
        KNXNetworkLink link = this.link;
        if (processCommunicator == null || link == null) {
            logger.debug("Cannot write to KNX bus (processCommunicator: {}, link: {})",
                    processCommunicator == null ? "Not OK" : "OK",
                    link == null ? "Not OK" : (link.isOpen() ? "Open" : "Closed"));
            return false;
        }
        GroupAddress groupAddress = commandSpec.getGroupAddress();

        logger.trace("writeToKNX groupAddress '{}', commandSpec '{}:{} {}'", groupAddress, groupAddress,
                commandSpec.getDPT(), commandSpec.getValue());

        return sendToKNX(processCommunicator, groupAddress, commandSpec.getDPT(), commandSpec.getValue(),
                TelegramType.WRITE);
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

        sendToKNX(responseCommunicator, groupAddress, responseSpec.getDPT(), responseSpec.getValue(),
                TelegramType.RESPONSE);
    }

    private boolean sendToKNX(ProcessCommunication communicator, GroupAddress groupAddress, String dpt, Type type,
            TelegramType telegramType) throws KNXException {
        if (!connectIfNotAutomatic()) {
            return false;
        }

        Datapoint datapoint = new CommandDP(groupAddress, thingUID.toString(), 0,
                NORMALIZED_DPT.getOrDefault(dpt, dpt));
        String mappedValue = ValueEncoder.encode(type, dpt);
        if (mappedValue == null) {
            logger.debug("Value '{}' of type '{}' cannot be mapped to datapoint '{}'", type, type.getClass(),
                    datapoint);
            return false;
        }
        noteSecuredSend(groupAddress);
        logger.trace("sendToKNX mappedValue: '{}' groupAddress: '{}'", mappedValue, groupAddress);
        byte[] payload = payloadOf(datapoint, mappedValue);

        for (int i = 0;; i++) {
            SendOutcome outcome = SendOutcome.FAILED;
            try {
                communicator.write(datapoint, mappedValue);
                outcome = SendOutcome.SENT;
                logger.debug("Wrote value '{}' to datapoint '{}' ({}. attempt).", type, datapoint, i);
                break;
            } catch (KNXException e) {
                outcome = confirmationTimedOut(e) ? SendOutcome.NOT_CONFIRMED : SendOutcome.FAILED;
                if (i < MAX_SEND_ATTEMPTS - 1) {
                    logger.debug("Value '{}' could not be sent to KNX bus using datapoint '{}': {}. Will retry.", type,
                            datapoint, e.getLocalizedMessage());
                } else {
                    logger.warn("Value '{}' could not be sent to KNX bus using datapoint '{}': {}. Giving up now.",
                            type, datapoint, e.getLocalizedMessage());
                    throw e;
                }
            } catch (KnxSecureException e) {
                // a missing key or sender is a configuration error, retrying does not help
                throw new KNXException(e.getMessage(), e);
            } finally {
                recordSend(groupAddress, telegramType, payload, dpt, type, outcome);
            }
        }
        return true;
    }
}
