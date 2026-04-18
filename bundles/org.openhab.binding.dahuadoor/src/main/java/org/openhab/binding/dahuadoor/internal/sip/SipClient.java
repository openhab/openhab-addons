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
package org.openhab.binding.dahuadoor.internal.sip;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogState;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.AuthorizationHeader;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ExpiresHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.UserAgentHeader;
import javax.sip.header.ViaHeader;
import javax.sip.header.WWWAuthenticateHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAIN-SIP based SIP client for Dahua VTO integration.
 *
 * Features:
 * - REGISTER with Digest MD5 authentication
 * - Receive INVITE (doorbell events)
 * - Send 100 Trying / 180 Ringing
 * - Accept call with 200 OK and ACK handling (ANSWERING -> ACTIVE)
 * - Handle CANCEL and BYE with robust state transitions
 * - Deferred hangup path during ANSWERING (execute BYE after ACK)
 *
 * Limitations:
 * - No RTP media handling in SIP stack (media path handled by go2rtc)
 *
 * @author Sven Schad - Initial contribution
 */
@NonNullByDefault
public class SipClient implements SipListener {

    private final Logger logger = LoggerFactory.getLogger(SipClient.class);

    // Configuration
    private final String vtoIp;
    private final String sipExtension;
    private final String username;
    private final String password;
    private final int localSipPort;
    private final String localIp;
    private final String realm;
    private final SipEventListener listener;
    private final Consumer<String> errorHandler;

    // SIP Stack components
    private @Nullable SipStack sipStack;
    private @Nullable SipProvider sipProvider;
    private @Nullable ListeningPoint listeningPoint;
    private @Nullable MessageFactory messageFactory;
    private @Nullable HeaderFactory headerFactory;
    private @Nullable AddressFactory addressFactory;

    // State
    private static final long TERMINATING_TIMEOUT_SECONDS = 5;
    private static final long ANSWERING_TIMEOUT_SECONDS = 15;
    private final ScheduledExecutorService callStateTimeoutScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "dahuadoor-sip-answering-timeout");
        thread.setDaemon(true);
        return thread;
    });

    private long cseqCounter = 1;

    private @Nullable ServerTransaction inviteServerTransaction;
    private @Nullable Request inviteRequest;
    private @Nullable String currentInviteSdp;
    private @Nullable Dialog activeDialog;
    private @Nullable String currentCallerId;
    private boolean pendingHangupAfterAck = false;
    private @Nullable ScheduledFuture<?> answeringTimeoutFuture;
    private SipCallState callState = SipCallState.IDLE;

    public enum SipCallState {
        IDLE,
        RINGING,
        ANSWERING,
        ACTIVE,
        TERMINATING,
        HUNGUP
    }

    /**
     * Create and initialize SIP client.
     *
     * @param vtoIp VTO IP address (e.g., "172.18.1.111")
     * @param sipExtension SIP extension to register (e.g., "9901#2")
     * @param username SIP username (typically same as extension)
     * @param password SIP password
     * @param localSipPort Local UDP port for SIP communication (e.g., 5062)
     * @param localIp Local IP address (auto-detected)
     * @param realm SIP realm (typically "VDP" for Dahua)
     * @param listener Callback interface for SIP events
     * @param errorHandler Error callback
     * @throws Exception if SIP stack initialization fails
     */
    public SipClient(String vtoIp, String sipExtension, String username, String password, int localSipPort,
            String localIp, String realm, SipEventListener listener, Consumer<String> errorHandler) throws Exception {
        this.vtoIp = vtoIp;
        this.sipExtension = sipExtension;
        this.username = username;
        this.password = password;
        this.localSipPort = localSipPort;
        this.localIp = localIp;
        this.realm = realm;
        this.listener = listener;
        this.errorHandler = errorHandler;

        initializeSipStack();
    }

    public void initializeSipStack() throws Exception {
        SipFactory sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");

        Properties properties = new Properties();
        properties.setProperty("javax.sip.STACK_NAME", buildSipStackName());
        properties.setProperty("javax.sip.IP_ADDRESS", localIp);
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "0"); // No JAIN-SIP logging

        SipStack localSipStack = sipFactory.createSipStack(properties);
        HeaderFactory localHeaderFactory = sipFactory.createHeaderFactory();
        AddressFactory localAddressFactory = sipFactory.createAddressFactory();
        MessageFactory localMessageFactory = sipFactory.createMessageFactory();

        ListeningPoint localListeningPoint = localSipStack.createListeningPoint(localIp, localSipPort, "udp");
        SipProvider localSipProvider = localSipStack.createSipProvider(localListeningPoint);
        localSipProvider.addSipListener(this);

        sipStack = localSipStack;
        headerFactory = localHeaderFactory;
        addressFactory = localAddressFactory;
        messageFactory = localMessageFactory;
        listeningPoint = localListeningPoint;
        sipProvider = localSipProvider;

        logger.info("SIP stack initialized on {}:{}", localIp, localSipPort);
    }

    private String buildSipStackName() {
        String stackIdentity = sipExtension + ":" + localSipPort + ":" + localIp;
        return "dahuadoor-sip-client-" + UUID.nameUUIDFromBytes(stackIdentity.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Send REGISTER request (unauthenticated).
     * This triggers 401 Unauthorized response with nonce, which then triggers authenticated REGISTER.
     */
    public void sendRegister() {
        try {
            AddressFactory addrFactory = addressFactory;
            HeaderFactory hdrFactory = headerFactory;
            MessageFactory msgFactory = messageFactory;
            SipProvider provider = sipProvider;

            if (addrFactory == null || hdrFactory == null || msgFactory == null || provider == null) {
                logger.error("SIP stack not initialized");
                return;
            }

            // Request-URI: sip:172.18.1.111:5060
            SipURI requestURI = addrFactory.createSipURI(null, vtoIp);
            requestURI.setPort(5060);

            // From: <sip:9901%232@172.18.1.111:5060>
            String encodedExtension = sipExtension.replace("#", "%23");
            SipURI fromURI = addrFactory.createSipURI(encodedExtension, vtoIp);
            fromURI.setPort(5060);
            Address fromAddress = addrFactory.createAddress(fromURI);
            FromHeader fromHeader = hdrFactory.createFromHeader(fromAddress,
                    UUID.randomUUID().toString().substring(0, 8));

            // To: <sip:9901%232@172.18.1.111:5060>
            ToHeader toHeader = hdrFactory.createToHeader(fromAddress, null);

            // Via
            ViaHeader viaHeader = hdrFactory.createViaHeader(localIp, localSipPort, "udp", null);
            List<ViaHeader> viaHeaders = new ArrayList<>();
            viaHeaders.add(viaHeader);

            // Call-ID
            CallIdHeader callIdHeader = provider.getNewCallId();

            // CSeq
            CSeqHeader cSeqHeader = hdrFactory.createCSeqHeader(cseqCounter++, Request.REGISTER);

            // Max-Forwards
            MaxForwardsHeader maxForwards = hdrFactory.createMaxForwardsHeader(70);

            // Create request
            Request request = msgFactory.createRequest(requestURI, Request.REGISTER, callIdHeader, cSeqHeader,
                    fromHeader, toHeader, viaHeaders, maxForwards);

            // Contact: <sip:9901%232@192.168.x.x:5062>
            SipURI contactURI = addrFactory.createSipURI(encodedExtension, localIp);
            contactURI.setPort(localSipPort);
            Address contactAddress = addrFactory.createAddress(contactURI);
            ContactHeader contactHeader = hdrFactory.createContactHeader(contactAddress);
            request.addHeader(contactHeader);

            // Expires: 60
            ExpiresHeader expiresHeader = hdrFactory.createExpiresHeader(60);
            request.addHeader(expiresHeader);

            // User-Agent
            UserAgentHeader userAgentHeader = hdrFactory.createUserAgentHeader(Arrays.asList("openHAB/5.2.0"));
            request.addHeader(userAgentHeader);

            // Send
            ClientTransaction localRegisterTransaction = provider.getNewClientTransaction(request);
            localRegisterTransaction.sendRequest();

            logger.debug("Sent REGISTER (unauthenticated)");

        } catch (Exception e) {
            logger.error("Failed to send REGISTER: {}", e.getMessage(), e);
            errorHandler.accept("SIP REGISTER failed: " + e.getMessage());
        }
    }

    private void sendAuthenticatedRegister(String nonce) {
        try {
            AddressFactory addrFactory = addressFactory;
            HeaderFactory hdrFactory = headerFactory;
            MessageFactory msgFactory = messageFactory;
            SipProvider provider = sipProvider;

            if (addrFactory == null || hdrFactory == null || msgFactory == null || provider == null) {
                logger.error("SIP stack not initialized");
                return;
            }

            // Build request (same as sendRegister)
            SipURI requestURI = addrFactory.createSipURI(null, vtoIp);
            requestURI.setPort(5060);

            String encodedExtension = sipExtension.replace("#", "%23");
            SipURI fromURI = addrFactory.createSipURI(encodedExtension, vtoIp);
            fromURI.setPort(5060);
            Address fromAddress = addrFactory.createAddress(fromURI);
            FromHeader fromHeader = hdrFactory.createFromHeader(fromAddress,
                    UUID.randomUUID().toString().substring(0, 8));

            ToHeader toHeader = hdrFactory.createToHeader(fromAddress, null);

            ViaHeader viaHeader = hdrFactory.createViaHeader(localIp, localSipPort, "udp", null);
            List<ViaHeader> viaHeaders = new ArrayList<>();
            viaHeaders.add(viaHeader);

            CallIdHeader callIdHeader = provider.getNewCallId();
            CSeqHeader cSeqHeader = hdrFactory.createCSeqHeader(cseqCounter++, Request.REGISTER);
            MaxForwardsHeader maxForwards = hdrFactory.createMaxForwardsHeader(70);

            Request request = msgFactory.createRequest(requestURI, Request.REGISTER, callIdHeader, cSeqHeader,
                    fromHeader, toHeader, viaHeaders, maxForwards);

            SipURI contactURI = addrFactory.createSipURI(encodedExtension, localIp);
            contactURI.setPort(localSipPort);
            Address contactAddress = addrFactory.createAddress(contactURI);
            ContactHeader contactHeader = hdrFactory.createContactHeader(contactAddress);
            request.addHeader(contactHeader);

            ExpiresHeader expiresHeader = hdrFactory.createExpiresHeader(60);
            request.addHeader(expiresHeader);

            UserAgentHeader userAgentHeader = hdrFactory.createUserAgentHeader(Arrays.asList("openHAB/5.2.0"));
            request.addHeader(userAgentHeader);

            // Add Authorization header with Digest
            SipURI authUri = addrFactory.createSipURI(null, vtoIp);
            String uri = authUri.toString();
            String response = DigestAuthHelper.calculateResponse(username, realm, password, "REGISTER", uri, nonce);

            AuthorizationHeader authHeader = hdrFactory.createAuthorizationHeader("Digest");
            authHeader.setUsername(username);
            authHeader.setRealm(realm);
            authHeader.setNonce(nonce);
            authHeader.setURI(authUri);
            authHeader.setResponse(response);
            authHeader.setAlgorithm("MD5");
            request.addHeader(authHeader);

            // Send
            ClientTransaction localRegisterTransaction = provider.getNewClientTransaction(request);
            localRegisterTransaction.sendRequest();

            logger.debug("Sent REGISTER (with Digest auth)");

        } catch (Exception e) {
            logger.error("Failed to send authenticated REGISTER: {}", e.getMessage(), e);
            errorHandler.accept("SIP auth failed: " + e.getMessage());
        }
    }

    /**
     * Dispose SIP client and clean up resources.
     * Must be called before creating a new instance to prevent "Provider already attached" error.
     */
    public void dispose() {
        try {
            cancelAnsweringTimeout();
            callStateTimeoutScheduler.shutdownNow();
            synchronized (this) {
                callState = SipCallState.IDLE;
                inviteRequest = null;
                inviteServerTransaction = null;
                currentInviteSdp = null;
                activeDialog = null;
                currentCallerId = null;
                pendingHangupAfterAck = false;
            }

            SipProvider provider = sipProvider;
            SipStack stack = sipStack;
            ListeningPoint lp = listeningPoint;

            if (provider != null) {
                // Remove listener
                provider.removeSipListener(this);

                // Remove and delete listening point
                if (lp != null && stack != null) {
                    provider.removeListeningPoint(lp);
                    stack.deleteListeningPoint(lp);
                    listeningPoint = null;
                }

                // Delete provider
                if (stack != null) {
                    stack.deleteSipProvider(provider);
                }

                sipProvider = null;
            }

            // Stop SIP stack
            if (stack != null) {
                stack.stop();
                sipStack = null;
            }

            logger.debug("SIP client disposed successfully");
        } catch (Exception e) {
            logger.warn("Error disposing SIP client: {}", e.getMessage());
        }
    }

    // ========== SipListener Implementation ==========

    @Override
    public void processRequest(@Nullable RequestEvent requestEvent) {
        if (requestEvent == null) {
            return;
        }
        Request request = requestEvent.getRequest();
        String method = request.getMethod();

        logger.debug("Received SIP request: {}", method);

        try {
            ServerTransaction serverTransaction = requestEvent.getServerTransaction();
            SipProvider provider = sipProvider;

            if (serverTransaction == null && provider != null) {
                serverTransaction = provider.getNewServerTransaction(request);
            }

            if (Request.INVITE.equals(method)) {
                handleInvite(request, serverTransaction);
            } else if (Request.CANCEL.equals(method)) {
                handleCancel(request, serverTransaction);
            } else if (Request.ACK.equals(method)) {
                handleAck(request);
            } else if (Request.BYE.equals(method)) {
                handleBye(request, serverTransaction);
            }
        } catch (Exception e) {
            logger.error("Error processing SIP request: {}", e.getMessage(), e);
        }
    }

    @Override
    public void processResponse(@Nullable ResponseEvent responseEvent) {
        if (responseEvent == null) {
            return;
        }
        Response response = responseEvent.getResponse();
        int statusCode = response.getStatusCode();
        CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);

        if (cseq == null) {
            logger.debug("Received response without CSeq header: {}", statusCode);
            return;
        }

        logger.debug("Received SIP response: {} {} (CSeq: {} {})", statusCode, response.getReasonPhrase(),
                cseq.getSeqNumber(), cseq.getMethod());

        if (Request.REGISTER.equals(cseq.getMethod())) {
            handleRegisterResponse(response);
        } else if (Request.BYE.equals(cseq.getMethod()) && statusCode == Response.OK) {
            synchronized (this) {
                cancelAnsweringTimeout();
                callState = SipCallState.IDLE;
                inviteRequest = null;
                inviteServerTransaction = null;
                currentInviteSdp = null;
                activeDialog = null;
                currentCallerId = null;
                pendingHangupAfterAck = false;
            }
            listener.onCallEnded();
        }
    }

    private void handleInvite(Request request, @Nullable ServerTransaction serverTransaction) throws Exception {
        MessageFactory msgFactory = messageFactory;
        HeaderFactory hdrFactory = headerFactory;
        AddressFactory addrFactory = addressFactory;

        if (msgFactory == null || hdrFactory == null || addrFactory == null || serverTransaction == null) {
            logger.error("Cannot handle INVITE: SIP stack not ready or transaction null");
            return;
        }

        // Extract caller info
        FromHeader from = (FromHeader) request.getHeader(FromHeader.NAME);
        CallIdHeader callId = (CallIdHeader) request.getHeader(CallIdHeader.NAME);
        String callerId = from != null ? from.getAddress().getURI().toString() : "unknown";
        String callIdStr = callId != null ? callId.getCallId() : "unknown";

        logger.info("Incoming SIP call from {}, CallID: {}", callerId, callIdStr);

        synchronized (this) {
            cancelAnsweringTimeout();
            currentCallerId = callerId;
            inviteRequest = request;
            inviteServerTransaction = serverTransaction;
            byte[] inviteSdpRaw = request.getRawContent();
            currentInviteSdp = inviteSdpRaw != null && inviteSdpRaw.length > 0
                    ? new String(inviteSdpRaw, StandardCharsets.UTF_8)
                    : null;
            activeDialog = serverTransaction.getDialog();
            callState = SipCallState.RINGING;
        }

        // Send 100 Trying
        Response trying = msgFactory.createResponse(Response.TRYING, request);
        serverTransaction.sendResponse(trying);

        // Send 180 Ringing
        Response ringing = msgFactory.createResponse(Response.RINGING, request);

        // Add Contact header
        String encodedExtension = sipExtension.replace("#", "%23");
        SipURI contactURI = addrFactory.createSipURI(encodedExtension, localIp);
        contactURI.setPort(localSipPort);
        Address contactAddress = addrFactory.createAddress(contactURI);
        ContactHeader contactHeader = hdrFactory.createContactHeader(contactAddress);
        ringing.addHeader(contactHeader);

        serverTransaction.sendResponse(ringing);
        logger.debug("Sent 180 Ringing");

        // Trigger callback
        listener.onInviteReceived(callerId);
    }

    private void handleCancel(Request request, @Nullable ServerTransaction serverTransaction) throws Exception {
        MessageFactory msgFactory = messageFactory;

        if (msgFactory == null || serverTransaction == null) {
            logger.error("Cannot handle CANCEL: SIP stack not ready or transaction null");
            return;
        }

        // Send 200 OK to CANCEL
        Response ok = msgFactory.createResponse(Response.OK, request);
        serverTransaction.sendResponse(ok);

        logger.info("Call cancelled by VTO");
        synchronized (this) {
            cancelAnsweringTimeout();
            callState = SipCallState.IDLE;
            inviteRequest = null;
            inviteServerTransaction = null;
            currentInviteSdp = null;
            activeDialog = null;
            currentCallerId = null;
            pendingHangupAfterAck = false;
        }
        listener.onCallCancelled();
    }

    private void handleAck(Request request) {
        logger.debug("Received ACK");
        boolean shouldSendDeferredBye;
        synchronized (this) {
            if (callState != SipCallState.ANSWERING && callState != SipCallState.TERMINATING) {
                logger.debug("Ignoring ACK in state {}", callState);
                return;
            }
            cancelAnsweringTimeout();
            ServerTransaction localInviteServerTransaction = inviteServerTransaction;
            if (activeDialog == null && localInviteServerTransaction != null) {
                activeDialog = localInviteServerTransaction.getDialog();
            }
            callState = SipCallState.ACTIVE;
            shouldSendDeferredBye = pendingHangupAfterAck;
            pendingHangupAfterAck = false;
        }
        listener.onCallActive();
        if (shouldSendDeferredBye) {
            logger.debug("ACK received, executing deferred hangup");
            sendBye("deferred-after-ack");
        }
    }

    private void handleBye(Request request, @Nullable ServerTransaction serverTransaction) throws Exception {
        MessageFactory msgFactory = messageFactory;

        if (msgFactory == null || serverTransaction == null) {
            logger.error("Cannot handle BYE: SIP stack not ready or transaction null");
            return;
        }

        Response ok = msgFactory.createResponse(Response.OK, request);
        serverTransaction.sendResponse(ok);

        synchronized (this) {
            cancelAnsweringTimeout();
            callState = SipCallState.HUNGUP;
            inviteRequest = null;
            inviteServerTransaction = null;
            currentInviteSdp = null;
            activeDialog = null;
            currentCallerId = null;
            pendingHangupAfterAck = false;
        }
        logger.info("Call terminated by remote BYE");
    }

    private void handleRegisterResponse(Response response) {
        int statusCode = response.getStatusCode();

        if (statusCode == Response.UNAUTHORIZED) {
            logger.debug("Received 401 Unauthorized - extracting nonce");

            WWWAuthenticateHeader authHeader = (WWWAuthenticateHeader) response.getHeader(WWWAuthenticateHeader.NAME);
            if (authHeader != null) {
                String nonce = authHeader.getNonce();
                logger.debug("Nonce: {}", nonce);
                sendAuthenticatedRegister(nonce);
            }
        } else if (statusCode == Response.OK) {
            logger.info("SIP registration successful");
            listener.onRegistrationSuccess();
        } else {
            String reasonPhrase = response.getReasonPhrase();
            logger.warn("REGISTER failed with status {} {}", statusCode, reasonPhrase);
            listener.onRegistrationFailed(reasonPhrase == null || reasonPhrase.isBlank() ? "SIP " + statusCode
                    : "SIP " + statusCode + " " + reasonPhrase);
        }
    }

    public synchronized String getCallState() {
        return callState.name();
    }

    public synchronized @Nullable String getCurrentCallerId() {
        return currentCallerId;
    }

    public synchronized @Nullable String getCurrentInviteSdp() {
        return currentInviteSdp;
    }

    public synchronized boolean sendOkResponse() {
        try {
            MessageFactory msgFactory = messageFactory;
            HeaderFactory hdrFactory = headerFactory;
            AddressFactory addrFactory = addressFactory;

            if (msgFactory == null || hdrFactory == null || addrFactory == null) {
                logger.error("Cannot send 200 OK: SIP stack not initialized");
                return false;
            }

            if (callState != SipCallState.RINGING || inviteRequest == null || inviteServerTransaction == null) {
                logger.debug("Cannot send 200 OK in state {}", callState);
                return false;
            }

            Request localInviteRequest = Objects.requireNonNull(inviteRequest);
            ServerTransaction localInviteServerTransaction = Objects.requireNonNull(inviteServerTransaction);
            Response ok = msgFactory.createResponse(Response.OK, localInviteRequest);

            String encodedExtension = sipExtension.replace("#", "%23");
            SipURI contactURI = addrFactory.createSipURI(encodedExtension, localIp);
            contactURI.setPort(localSipPort);
            Address contactAddress = addrFactory.createAddress(contactURI);
            ContactHeader contactHeader = hdrFactory.createContactHeader(contactAddress);
            ok.addHeader(contactHeader);

            byte[] inviteSdp = localInviteRequest.getRawContent();
            if (inviteSdp != null && inviteSdp.length > 0) {
                logger.debug(
                        "Incoming INVITE contains SDP offer, but no local SDP answer is available yet; sending 200 OK without SDP body");
            }

            localInviteServerTransaction.sendResponse(ok);

            activeDialog = localInviteServerTransaction.getDialog();
            callState = SipCallState.ANSWERING;
            scheduleAnsweringTimeout();
            logger.info("Sent 200 OK for incoming INVITE");
            return true;
        } catch (Exception e) {
            logger.warn("Failed to send 200 OK: {}", e.getMessage(), e);
            return false;
        }
    }

    public synchronized boolean sendBye() {
        return sendBye("unspecified");
    }

    public synchronized boolean sendBye(String trigger) {
        try {
            MessageFactory msgFactory = messageFactory;

            if (callState == SipCallState.IDLE || callState == SipCallState.HUNGUP) {
                logger.debug("Ignoring hangup request in {} state", callState);
                return true;
            }

            if (callState == SipCallState.TERMINATING) {
                logger.debug("Ignoring duplicate hangup request while call is TERMINATING (trigger={})", trigger);
                return true;
            }

            if (callState == SipCallState.RINGING) {
                Request localInviteRequest = inviteRequest;
                ServerTransaction localInviteServerTransaction = inviteServerTransaction;
                if (msgFactory == null || localInviteRequest == null || localInviteServerTransaction == null) {
                    logger.debug("Ignoring hangup in RINGING state: invite transaction already unavailable");
                    callState = SipCallState.IDLE;
                    inviteRequest = null;
                    inviteServerTransaction = null;
                    currentInviteSdp = null;
                    activeDialog = null;
                    currentCallerId = null;
                    pendingHangupAfterAck = false;
                    cancelAnsweringTimeout();
                    listener.onCallEnded();
                    return true;
                }
                Response decline = msgFactory.createResponse(Response.BUSY_HERE, localInviteRequest);
                localInviteServerTransaction.sendResponse(decline);
                callState = SipCallState.IDLE;
                inviteRequest = null;
                inviteServerTransaction = null;
                currentInviteSdp = null;
                activeDialog = null;
                currentCallerId = null;
                pendingHangupAfterAck = false;
                cancelAnsweringTimeout();
                logger.info("Rejected incoming INVITE with 486 Busy Here");
                listener.onCallEnded();
                return true;
            }

            if (callState == SipCallState.ANSWERING) {
                logger.debug("Hangup requested while in ANSWERING state (trigger={})", trigger);
                Dialog dialog = activeDialog;
                ServerTransaction localInviteServerTransaction = inviteServerTransaction;
                if (dialog == null && localInviteServerTransaction != null) {
                    dialog = localInviteServerTransaction.getDialog();
                    activeDialog = dialog;
                }
                SipProvider provider = sipProvider;

                if (dialog != null && provider != null && DialogState.CONFIRMED.equals(dialog.getState())) {
                    try {
                        Request byeRequest = dialog.createRequest(Request.BYE);
                        ClientTransaction byeTransaction = provider.getNewClientTransaction(byeRequest);
                        dialog.sendRequest(byeTransaction);

                        callState = SipCallState.TERMINATING;
                        pendingHangupAfterAck = false;
                        scheduleAnsweringTimeout();
                        logger.info("Sent BYE for SIP call in ANSWERING state (trigger={})", trigger);
                        listener.onCallTerminating();
                        return true;
                    } catch (Exception e) {
                        logger.debug("Could not send BYE in ANSWERING state yet: {}", e.getMessage());
                    }
                }

                callState = SipCallState.TERMINATING;
                pendingHangupAfterAck = true;
                scheduleAnsweringTimeout();
                logger.debug("Deferring hangup in ANSWERING state until ACK is received (trigger={})", trigger);
                listener.onCallTerminating();
                return true;
            }

            Dialog dialog = activeDialog;
            ServerTransaction localInviteServerTransaction = inviteServerTransaction;
            if (dialog == null && localInviteServerTransaction != null) {
                dialog = localInviteServerTransaction.getDialog();
                activeDialog = dialog;
            }
            SipProvider provider = sipProvider;

            if (dialog == null || provider == null) {
                logger.debug("Cannot send BYE: no active SIP dialog");
                callState = SipCallState.IDLE;
                inviteRequest = null;
                inviteServerTransaction = null;
                currentInviteSdp = null;
                activeDialog = null;
                currentCallerId = null;
                pendingHangupAfterAck = false;
                cancelAnsweringTimeout();
                listener.onCallEnded();
                return true;
            }

            if (!DialogState.CONFIRMED.equals(dialog.getState())) {
                logger.debug("Skipping BYE because SIP dialog is not CONFIRMED (state={})", dialog.getState());
                callState = SipCallState.IDLE;
                inviteRequest = null;
                inviteServerTransaction = null;
                currentInviteSdp = null;
                activeDialog = null;
                currentCallerId = null;
                pendingHangupAfterAck = false;
                cancelAnsweringTimeout();
                listener.onCallEnded();
                return true;
            }

            Request byeRequest = dialog.createRequest(Request.BYE);
            ClientTransaction byeTransaction = provider.getNewClientTransaction(byeRequest);
            dialog.sendRequest(byeTransaction);

            callState = SipCallState.TERMINATING;
            pendingHangupAfterAck = false;
            scheduleAnsweringTimeout();
            logger.info("Sent BYE for active SIP call");
            listener.onCallTerminating();
            return true;
        } catch (Exception e) {
            String message = e.getMessage();
            if (message != null && message.contains("not yet established or terminated")) {
                logger.debug("Ignoring BYE for non-established or terminated dialog");
                cancelAnsweringTimeout();
                callState = SipCallState.IDLE;
                inviteRequest = null;
                inviteServerTransaction = null;
                currentInviteSdp = null;
                activeDialog = null;
                currentCallerId = null;
                pendingHangupAfterAck = false;
                listener.onCallEnded();
                return true;
            }
            logger.warn("Failed to send BYE: {}", message, e);
            return false;
        }
    }

    // Unused SipListener methods (required by interface)

    @Override
    public void processTimeout(@Nullable TimeoutEvent timeoutEvent) {
        logger.debug("SIP transaction timeout");
    }

    @Override
    public void processIOException(@Nullable IOExceptionEvent exceptionEvent) {
        if (exceptionEvent != null) {
            logger.warn("SIP IO exception: {}:{} ({})", exceptionEvent.getHost(), exceptionEvent.getPort(),
                    exceptionEvent.getTransport());
        }
    }

    @Override
    public void processTransactionTerminated(@Nullable TransactionTerminatedEvent transactionTerminatedEvent) {
        // Normal cleanup - no action needed
    }

    @Override
    public void processDialogTerminated(@Nullable DialogTerminatedEvent dialogTerminatedEvent) {
        boolean shouldNotifyEnded;
        synchronized (this) {
            shouldNotifyEnded = callState != SipCallState.IDLE || inviteRequest != null
                    || inviteServerTransaction != null || activeDialog != null || currentCallerId != null;
            if (!shouldNotifyEnded) {
                return;
            }
            cancelAnsweringTimeout();
            callState = SipCallState.IDLE;
            inviteRequest = null;
            inviteServerTransaction = null;
            currentInviteSdp = null;
            activeDialog = null;
            currentCallerId = null;
            pendingHangupAfterAck = false;
        }
        listener.onCallEnded();
    }

    private synchronized void cancelAnsweringTimeout() {
        ScheduledFuture<?> timeoutFuture = answeringTimeoutFuture;
        if (timeoutFuture != null) {
            timeoutFuture.cancel(false);
            answeringTimeoutFuture = null;
        }
    }

    private synchronized void scheduleAnsweringTimeout() {
        cancelAnsweringTimeout();
        if (callState != SipCallState.TERMINATING && callState != SipCallState.ANSWERING) {
            return;
        }
        final SipCallState timeoutState = callState;
        long timeoutSeconds = timeoutState == SipCallState.ANSWERING ? ANSWERING_TIMEOUT_SECONDS
                : TERMINATING_TIMEOUT_SECONDS;
        answeringTimeoutFuture = callStateTimeoutScheduler.schedule(() -> {
            boolean shouldNotifyEnded = false;
            synchronized (SipClient.this) {
                if (callState != timeoutState) {
                    return;
                }
                logger.warn("SIP call stuck in {} for {}s - forcing call end", timeoutState, timeoutSeconds);
                callState = SipCallState.IDLE;
                inviteRequest = null;
                inviteServerTransaction = null;
                currentInviteSdp = null;
                activeDialog = null;
                currentCallerId = null;
                pendingHangupAfterAck = false;
                answeringTimeoutFuture = null;
                shouldNotifyEnded = true;
            }
            if (shouldNotifyEnded) {
                listener.onCallEnded();
            }
        }, timeoutSeconds, TimeUnit.SECONDS);
    }
}
