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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.TooManyListenersException;
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
import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.ObjectInUseException;
import javax.sip.PeerUnavailableException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.TransportNotSupportedException;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.AuthorizationHeader;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
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
import org.openhab.binding.dahuadoor.internal.media.SipAudioOffer;
import org.openhab.binding.dahuadoor.internal.media.SipBackchannelRtpRelay;
import org.openhab.binding.dahuadoor.internal.media.SipSdpParser;
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
    private static final String BINDING_PREFIX = "dahuadoor";
    private static final String USER_AGENT = "openHAB";

    // Configuration
    private final String vtoIp;
    private final String sipExtension;
    private final String username;
    private final String password;
    private final int localSipPort;
    private final String localIp;
    private final String realm;
    private final int localAudioRtpPort;
    private final @Nullable SipBackchannelRtpRelay backchannelRelay;
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
    private static final long OUTGOING_RINGING_TIMEOUT_SECONDS = 60;
    private static final long ACK_FALLBACK_TIMEOUT_MILLIS = 300;
    private final ScheduledExecutorService callStateTimeoutScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, BINDING_PREFIX + "-sip-answering-timeout");
        thread.setDaemon(true);
        return thread;
    });

    private long cseqCounter = 1;
    private @Nullable String registerCallId;
    private @Nullable String registerFromTag;

    private @Nullable ServerTransaction inviteServerTransaction;
    private @Nullable Request inviteRequest;
    private @Nullable ClientTransaction inviteClientTransaction;
    private @Nullable String currentInviteSdp;
    private @Nullable Dialog activeDialog;
    private @Nullable String currentCallerId;
    private @Nullable String outgoingCallId;
    private boolean pendingHangupAfterAck = false;
    private @Nullable ScheduledFuture<?> answeringTimeoutFuture;
    private @Nullable ScheduledFuture<?> ackFallbackFuture;
    private @Nullable ScheduledFuture<?> outgoingRingingTimeoutFuture;
    private SipCallState callState = SipCallState.IDLE;

    // Audio talkback
    private final SipSdpParser sdpParser = new SipSdpParser();
    private @Nullable SipAudioOffer currentAudioOffer;

    public enum SipCallState {
        IDLE,
        RINGING,
        ANSWERING,
        OUTGOING_RINGING,
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
     * @param localSipPort Local UDP port for SIP communication (e.g., 5060)
     * @param localIp Local IP address (auto-detected)
     * @param realm SIP realm (typically "VDP" for Dahua)
     * @param localAudioRtpPort Local RTP source port advertised in the SDP answer
     * @param backchannelRelay relay for browser backchannel audio
     * @param listener Callback interface for SIP events
     * @param errorHandler Error callback
     * @throws PeerUnavailableException if SIP factory components cannot be created
     * @throws TransportNotSupportedException if UDP transport cannot be initialized
     * @throws InvalidArgumentException if SIP parameters are invalid
     * @throws ObjectInUseException if SIP resources are already in use
     * @throws TooManyListenersException if SIP listener registration fails
     */
    public SipClient(String vtoIp, String sipExtension, String username, String password, int localSipPort,
            String localIp, String realm, int localAudioRtpPort, @Nullable SipBackchannelRtpRelay backchannelRelay,
            SipEventListener listener, Consumer<String> errorHandler) throws PeerUnavailableException,
            TransportNotSupportedException, InvalidArgumentException, ObjectInUseException, TooManyListenersException {
        this.vtoIp = vtoIp;
        this.sipExtension = sipExtension;
        this.username = username;
        this.password = password;
        this.localSipPort = localSipPort;
        this.localIp = localIp;
        this.realm = realm;
        this.localAudioRtpPort = localAudioRtpPort;
        this.backchannelRelay = backchannelRelay;
        this.listener = listener;
        this.errorHandler = errorHandler;

        initializeSipStack();
    }

    public void initializeSipStack() throws PeerUnavailableException, TransportNotSupportedException,
            InvalidArgumentException, ObjectInUseException, TooManyListenersException {
        SipStack localSipStack = null;
        ListeningPoint localListeningPoint = null;
        SipProvider localSipProvider = null;
        String initStep = "create SIP stack";
        try {
            SipFactory sipFactory = SipFactory.getInstance();
            sipFactory.setPathName("gov.nist");

            Properties properties = new Properties();
            String stackName = buildSipStackName();
            properties.setProperty("javax.sip.STACK_NAME", stackName);
            properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "0"); // No JAIN-SIP logging
            // Do not set javax.sip.IP_ADDRESS here. JAIN-SIP reuses stacks by IP address, which
            // prevents multiple SipClient instances on the same host from attaching their own listeners.

            localSipStack = sipFactory.createSipStack(properties);
            initStep = "create SIP factories";
            HeaderFactory localHeaderFactory = sipFactory.createHeaderFactory();
            AddressFactory localAddressFactory = sipFactory.createAddressFactory();
            MessageFactory localMessageFactory = sipFactory.createMessageFactory();

            initStep = "create listening point";
            localListeningPoint = localSipStack.createListeningPoint(localIp, localSipPort, "udp");

            initStep = "create SIP provider";
            localSipProvider = localSipStack.createSipProvider(localListeningPoint);

            initStep = "attach SIP listener";
            localSipProvider.addSipListener(this);

            sipStack = localSipStack;
            headerFactory = localHeaderFactory;
            addressFactory = localAddressFactory;
            messageFactory = localMessageFactory;
            listeningPoint = localListeningPoint;
            sipProvider = localSipProvider;

            logger.debug("SIP stack initialized for {} on {}:{}", sipExtension, localIp, localSipPort);
        } catch (PeerUnavailableException | TransportNotSupportedException | InvalidArgumentException
                | ObjectInUseException | TooManyListenersException | RuntimeException e) {
            cleanupFailedInitialization(localSipStack, localSipProvider, localListeningPoint);
            logger.warn("Failed to initialize SIP stack for {} on {}:{} during {} ({}): {}", sipExtension, localIp,
                    localSipPort, initStep, e.getClass().getSimpleName(), e.getMessage(), e);
            throw e;
        }
    }

    private void cleanupFailedInitialization(@Nullable SipStack stack, @Nullable SipProvider provider,
            @Nullable ListeningPoint lp) {
        try {
            if (provider != null) {
                provider.removeSipListener(this);
                if (lp != null) {
                    provider.removeListeningPoint(lp);
                }
            }
        } catch (ObjectInUseException | RuntimeException e) {
            logger.debug("Ignoring SIP provider cleanup error for {}: {}", sipExtension, e.getMessage(), e);
        }
        try {
            if (stack != null && provider != null) {
                stack.deleteSipProvider(provider);
            }
        } catch (ObjectInUseException | RuntimeException e) {
            logger.debug("Ignoring SIP provider deletion error for {}: {}", sipExtension, e.getMessage(), e);
        }
        try {
            if (stack != null && lp != null) {
                stack.deleteListeningPoint(lp);
            }
        } catch (ObjectInUseException | RuntimeException e) {
            logger.debug("Ignoring SIP listening point cleanup error for {}: {}", sipExtension, e.getMessage(), e);
        }
        try {
            if (stack != null) {
                stack.stop();
            }
        } catch (RuntimeException e) {
            logger.debug("Ignoring SIP stack stop error for {}: {}", sipExtension, e.getMessage(), e);
        }
    }

    private String buildSipStackName() {
        String stackIdentity = sipExtension + ":" + localSipPort + ":" + localIp;
        return BINDING_PREFIX + "-sip-client-" + UUID.nameUUIDFromBytes(stackIdentity.getBytes(StandardCharsets.UTF_8));
    }

    private synchronized long nextRegisterCSeq() {
        return cseqCounter++;
    }

    private synchronized String ensureRegisterFromTag() {
        String fromTag = registerFromTag;
        if (fromTag == null || fromTag.isBlank()) {
            fromTag = UUID.randomUUID().toString().substring(0, 8);
            registerFromTag = fromTag;
        }
        return fromTag;
    }

    private synchronized String ensureRegisterCallId(SipProvider provider) {
        String callId = registerCallId;
        if (callId == null || callId.isBlank()) {
            callId = provider.getNewCallId().getCallId();
            registerCallId = callId;
        }
        return callId;
    }

    private String encodeUserPart(String userPart) {
        return userPart.replace("#", "%23");
    }

    private SipURI createLocalUserUri(AddressFactory addrFactory, String host) throws ParseException {
        return addrFactory.createSipURI(encodeUserPart(sipExtension), host);
    }

    private SipURI createLocalContactUri(AddressFactory addrFactory) throws ParseException {
        return addrFactory.createSipURI(encodeUserPart(sipExtension), localIp);
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
                logger.error("SIP stack not initialized for extension {}", sipExtension);
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
            FromHeader fromHeader = hdrFactory.createFromHeader(fromAddress, ensureRegisterFromTag());

            // To: <sip:9901%232@172.18.1.111:5060>
            ToHeader toHeader = hdrFactory.createToHeader(fromAddress, null);

            // Via
            ViaHeader viaHeader = hdrFactory.createViaHeader(localIp, localSipPort, "udp", null);
            List<ViaHeader> viaHeaders = new ArrayList<>();
            viaHeaders.add(viaHeader);

            // Call-ID
            CallIdHeader callIdHeader = hdrFactory.createCallIdHeader(ensureRegisterCallId(provider));

            // CSeq
            CSeqHeader cSeqHeader = hdrFactory.createCSeqHeader(nextRegisterCSeq(), Request.REGISTER);

            // Max-Forwards
            MaxForwardsHeader maxForwards = hdrFactory.createMaxForwardsHeader(70);

            // Create request
            Request request = msgFactory.createRequest(requestURI, Request.REGISTER, callIdHeader, cSeqHeader,
                    fromHeader, toHeader, viaHeaders, maxForwards);

            // Contact: <sip:9901%232@192.168.x.x:5060>
            SipURI contactURI = addrFactory.createSipURI(encodedExtension, localIp);
            contactURI.setPort(localSipPort);
            Address contactAddress = addrFactory.createAddress(contactURI);
            ContactHeader contactHeader = hdrFactory.createContactHeader(contactAddress);
            request.addHeader(contactHeader);

            // Expires: 60
            ExpiresHeader expiresHeader = hdrFactory.createExpiresHeader(60);
            request.addHeader(expiresHeader);

            // User-Agent
            UserAgentHeader userAgentHeader = hdrFactory.createUserAgentHeader(Arrays.asList(USER_AGENT));
            request.addHeader(userAgentHeader);

            // Send
            ClientTransaction localRegisterTransaction = provider.getNewClientTransaction(request);
            localRegisterTransaction.sendRequest();

        } catch (SipException | ParseException | InvalidArgumentException | RuntimeException e) {
            logger.warn("Failed to send REGISTER: {}", e.getMessage(), e);
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
            FromHeader fromHeader = hdrFactory.createFromHeader(fromAddress, ensureRegisterFromTag());

            ToHeader toHeader = hdrFactory.createToHeader(fromAddress, null);

            ViaHeader viaHeader = hdrFactory.createViaHeader(localIp, localSipPort, "udp", null);
            List<ViaHeader> viaHeaders = new ArrayList<>();
            viaHeaders.add(viaHeader);

            CallIdHeader callIdHeader = hdrFactory.createCallIdHeader(ensureRegisterCallId(provider));
            CSeqHeader cSeqHeader = hdrFactory.createCSeqHeader(nextRegisterCSeq(), Request.REGISTER);
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

            UserAgentHeader userAgentHeader = hdrFactory.createUserAgentHeader(Arrays.asList(USER_AGENT));
            request.addHeader(userAgentHeader);

            // Add Authorization header with Digest
            String uri = requestURI.toString();
            String response = DigestAuthHelper.calculateResponse(username, realm, password, "REGISTER", uri, nonce);

            AuthorizationHeader authHeader = hdrFactory.createAuthorizationHeader("Digest");
            authHeader.setUsername(username);
            authHeader.setRealm(realm);
            authHeader.setNonce(nonce);
            authHeader.setURI(requestURI);
            authHeader.setResponse(response);
            authHeader.setAlgorithm("MD5");
            request.addHeader(authHeader);

            // Send
            ClientTransaction localRegisterTransaction = provider.getNewClientTransaction(request);
            localRegisterTransaction.sendRequest();

        } catch (SipException | ParseException | InvalidArgumentException | RuntimeException e) {
            logger.warn("Failed to send authenticated REGISTER: {}", e.getMessage(), e);
            errorHandler.accept("SIP auth failed: " + e.getMessage());
        }
    }

    private void sendUnregister() {
        try {
            AddressFactory addrFactory = addressFactory;
            HeaderFactory hdrFactory = headerFactory;
            MessageFactory msgFactory = messageFactory;
            SipProvider provider = sipProvider;

            if (addrFactory == null || hdrFactory == null || msgFactory == null || provider == null) {
                return;
            }

            SipURI requestURI = addrFactory.createSipURI(null, vtoIp);
            requestURI.setPort(5060);

            String encodedExtension = sipExtension.replace("#", "%23");
            SipURI fromURI = addrFactory.createSipURI(encodedExtension, vtoIp);
            fromURI.setPort(5060);
            Address fromAddress = addrFactory.createAddress(fromURI);
            FromHeader fromHeader = hdrFactory.createFromHeader(fromAddress, ensureRegisterFromTag());
            ToHeader toHeader = hdrFactory.createToHeader(fromAddress, null);

            ViaHeader viaHeader = hdrFactory.createViaHeader(localIp, localSipPort, "udp", null);
            List<ViaHeader> viaHeaders = new ArrayList<>();
            viaHeaders.add(viaHeader);

            CallIdHeader callIdHeader = hdrFactory.createCallIdHeader(ensureRegisterCallId(provider));
            CSeqHeader cSeqHeader = hdrFactory.createCSeqHeader(nextRegisterCSeq(), Request.REGISTER);
            MaxForwardsHeader maxForwards = hdrFactory.createMaxForwardsHeader(70);

            Request request = msgFactory.createRequest(requestURI, Request.REGISTER, callIdHeader, cSeqHeader,
                    fromHeader, toHeader, viaHeaders, maxForwards);

            // Contact: * with Expires: 0 to unregister all bindings
            ContactHeader contactHeader = hdrFactory.createContactHeader();
            contactHeader.setWildCard();
            request.addHeader(contactHeader);

            ExpiresHeader expiresHeader = hdrFactory.createExpiresHeader(0);
            request.addHeader(expiresHeader);

            UserAgentHeader userAgentHeader = hdrFactory.createUserAgentHeader(Arrays.asList(USER_AGENT));
            request.addHeader(userAgentHeader);

            ClientTransaction tx = provider.getNewClientTransaction(request);
            tx.sendRequest();
            logger.debug("Sent REGISTER Expires:0 (unregister) for {}", sipExtension);
        } catch (SipException | ParseException | InvalidArgumentException | RuntimeException e) {
            logger.debug("Failed to send unregister for {}: {}", sipExtension, e.getMessage());
        }
    }

    /**
     * Dispose SIP client and clean up resources.
     * Must be called before creating a new instance to prevent "Provider already attached" error.
     */
    public void dispose() {
        try {
            cancelAnsweringTimeout();
            cancelOutgoingRingingTimeout();
            clearAudioPath("dispose");
            callStateTimeoutScheduler.shutdownNow();
            synchronized (this) {
                clearCallContextLocked(SipCallState.IDLE);
            }

            // Send unregister before tearing down the stack so the VTO clears the binding immediately
            sendUnregister();

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
            }

            // Stop SIP stack
            if (stack != null) {
                stack.stop();
                sipStack = null;
            }
        } catch (SipException | RuntimeException e) {
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
        } catch (SipException | ParseException | InvalidArgumentException | RuntimeException e) {
            logger.warn("Error processing SIP request: {}", e.getMessage(), e);
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

        if (Request.REGISTER.equals(cseq.getMethod())) {
            handleRegisterResponse(response);
        } else if (Request.INVITE.equals(cseq.getMethod())) {
            handleOutgoingInviteResponse(response, responseEvent.getClientTransaction(), responseEvent.getDialog());
        } else if (Request.BYE.equals(cseq.getMethod()) && statusCode == Response.OK) {
            clearAudioPath("local-bye-ok");
            synchronized (this) {
                cancelAnsweringTimeout();
                cancelOutgoingRingingTimeout();
                clearCallContextLocked(SipCallState.IDLE);
            }
            listener.onCallEnded();
        }
    }

    private void handleInvite(Request request, @Nullable ServerTransaction serverTransaction)
            throws SipException, ParseException, InvalidArgumentException {
        MessageFactory msgFactory = messageFactory;
        HeaderFactory hdrFactory = headerFactory;
        AddressFactory addrFactory = addressFactory;

        if (msgFactory == null || hdrFactory == null || addrFactory == null || serverTransaction == null) {
            logger.debug("Cannot handle INVITE: SIP stack not ready or transaction null");
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
            SipAudioOffer parsedAudioOffer = sdpParser.parseAudioOffer(currentInviteSdp).orElse(null);
            currentAudioOffer = parsedAudioOffer;
            if (parsedAudioOffer != null) {
                logger.info("SIP INVITE audio target: {}:{} codec={} PT={}", parsedAudioOffer.getRemoteHost(),
                        parsedAudioOffer.getRemotePort(), parsedAudioOffer.getCodecName(),
                        parsedAudioOffer.getPayloadType());
            } else {
                logger.warn("SIP INVITE: could not parse audio offer from SDP");
            }
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

    private void handleCancel(Request request, @Nullable ServerTransaction serverTransaction)
            throws SipException, ParseException, InvalidArgumentException {
        MessageFactory msgFactory = messageFactory;

        if (msgFactory == null || serverTransaction == null) {
            logger.debug("Cannot handle CANCEL: SIP stack not ready or transaction null");
            return;
        }

        // Send 200 OK to CANCEL
        Response ok = msgFactory.createResponse(Response.OK, request);
        serverTransaction.sendResponse(ok);

        boolean shouldCancelCall;
        boolean shouldActivateAudio;
        synchronized (this) {
            if (callState == SipCallState.RINGING || callState == SipCallState.IDLE) {
                // RFC 3261 §9.2: CANCEL is only effective if no final response has been sent yet
                logger.info("Call cancelled by VTO (state={})", callState);
                clearAudioPath("cancel");
                cancelAnsweringTimeout();
                clearCallContextLocked(SipCallState.IDLE);
                shouldCancelCall = true;
                shouldActivateAudio = false;
            } else if (callState == SipCallState.ANSWERING) {
                // Dahua VTO firmware quirk: VTO sends CANCEL immediately after our 200 OK and never
                // sends ACK. Treat CANCEL-in-ANSWERING as implicit ACK to activate the audio path.
                logger.info("CANCEL received in ANSWERING state - Dahua VTO workaround: activating audio path");
                cancelAnsweringTimeout();
                ServerTransaction localInviteServerTransaction = inviteServerTransaction;
                if (activeDialog == null && localInviteServerTransaction != null) {
                    activeDialog = localInviteServerTransaction.getDialog();
                }
                callState = SipCallState.ACTIVE;
                shouldCancelCall = false;
                shouldActivateAudio = true;
            } else {
                // A final response (200 OK) was already sent - CANCEL must not affect dialog state
                logger.info("CANCEL received in state {} - ignoring per RFC 3261 §9.2 (final response already sent)",
                        callState);
                shouldCancelCall = false;
                shouldActivateAudio = false;
            }
        }
        if (shouldActivateAudio) {
            SipAudioOffer offer;
            synchronized (this) {
                offer = currentAudioOffer;
            }
            if (offer != null) {
                activateAudioPath(offer);
            } else {
                logger.warn("CANCEL-in-ANSWERING workaround: no audio offer available");
            }
            listener.onCallActive();
        }
        if (shouldCancelCall) {
            listener.onCallCancelled();
        }
    }

    private void handleAck(Request request) {
        logger.debug("Received ACK");
        boolean shouldSendDeferredBye;
        synchronized (this) {
            if (callState != SipCallState.ANSWERING && callState != SipCallState.TERMINATING) {
                logger.debug("Ignoring ACK in state {}", callState);
                return;
            }
            logger.debug("ACK received by {} - activating call", sipExtension);
            cancelAnsweringTimeout();
            cancelAckFallbackTimeout();
            ServerTransaction localInviteServerTransaction = inviteServerTransaction;
            if (activeDialog == null && localInviteServerTransaction != null) {
                activeDialog = localInviteServerTransaction.getDialog();
            }
            callState = SipCallState.ACTIVE;
            shouldSendDeferredBye = pendingHangupAfterAck;
            pendingHangupAfterAck = false;
        }
        SipAudioOffer offer;
        synchronized (this) {
            offer = currentAudioOffer;
        }
        if (offer != null) {
            activateAudioPath(offer);
        } else {
            logger.warn("ACK received but no audio offer available - skipping backchannel startup");
        }
        listener.onCallActive();
        if (shouldSendDeferredBye) {
            logger.debug("ACK received, executing deferred hangup");
            sendBye("deferred-after-ack");
        }
    }

    private void handleBye(Request request, @Nullable ServerTransaction serverTransaction)
            throws SipException, ParseException, InvalidArgumentException {
        MessageFactory msgFactory = messageFactory;

        if (msgFactory == null || serverTransaction == null) {
            logger.debug("Cannot handle BYE: SIP stack not ready or transaction null");
            return;
        }

        Response ok = msgFactory.createResponse(Response.OK, request);
        serverTransaction.sendResponse(ok);

        clearAudioPath("remote-bye");
        synchronized (this) {
            cancelAnsweringTimeout();
            clearCallContextLocked(SipCallState.HUNGUP);
        }
        listener.onCallEnded();
        logger.info("Call terminated by remote BYE");
    }

    private void handleRegisterResponse(Response response) {
        int statusCode = response.getStatusCode();

        if (statusCode == Response.UNAUTHORIZED) {
            WWWAuthenticateHeader authHeader = (WWWAuthenticateHeader) response.getHeader(WWWAuthenticateHeader.NAME);
            if (authHeader != null) {
                String nonce = authHeader.getNonce();
                sendAuthenticatedRegister(nonce);
            }
        } else if (statusCode == Response.OK) {
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

    public synchronized boolean sendInvite(String target) {
        if (callState != SipCallState.IDLE) {
            logger.warn("Cannot send INVITE: state is {} (expected IDLE)", callState);
            return false;
        }

        try {
            AddressFactory addrFactory = addressFactory;
            HeaderFactory hdrFactory = headerFactory;
            MessageFactory msgFactory = messageFactory;
            SipProvider provider = sipProvider;

            if (addrFactory == null || hdrFactory == null || msgFactory == null || provider == null) {
                logger.warn("Cannot send INVITE: SIP stack not initialized");
                return false;
            }

            String encodedTarget = encodeUserPart(target);
            SipURI requestURI = addrFactory.createSipURI(encodedTarget, vtoIp);
            requestURI.setPort(5060);

            SipURI fromURI = createLocalUserUri(addrFactory, vtoIp);
            fromURI.setPort(5060);
            FromHeader fromHeader = hdrFactory.createFromHeader(addrFactory.createAddress(fromURI),
                    UUID.randomUUID().toString().substring(0, 8));

            ToHeader toHeader = hdrFactory.createToHeader(addrFactory.createAddress(requestURI), null);
            ViaHeader viaHeader = hdrFactory.createViaHeader(localIp, localSipPort, "udp", null);
            String callId = provider.getNewCallId().getCallId();
            CallIdHeader callIdHeader = hdrFactory.createCallIdHeader(callId);
            CSeqHeader cSeqHeader = hdrFactory.createCSeqHeader(1L, Request.INVITE);
            MaxForwardsHeader maxForwards = hdrFactory.createMaxForwardsHeader(70);
            Request invite = msgFactory.createRequest(requestURI, Request.INVITE, callIdHeader, cSeqHeader, fromHeader,
                    toHeader, List.of(viaHeader), maxForwards);

            SipURI contactUri = createLocalContactUri(addrFactory);
            contactUri.setPort(localSipPort);
            invite.addHeader(hdrFactory.createContactHeader(addrFactory.createAddress(contactUri)));

            byte[] sdpBytes = buildOutgoingOfferSdp().getBytes(StandardCharsets.UTF_8);
            ContentTypeHeader contentTypeHeader = hdrFactory.createContentTypeHeader("application", "sdp");
            invite.setContent(sdpBytes, contentTypeHeader);

            callState = SipCallState.OUTGOING_RINGING;
            currentCallerId = target;
            currentInviteSdp = null;
            currentAudioOffer = null;
            outgoingCallId = callId;
            ClientTransaction newInviteTransaction = provider.getNewClientTransaction(invite);
            inviteClientTransaction = newInviteTransaction;
            newInviteTransaction.sendRequest();
            scheduleOutgoingRingingTimeout();

            logger.info("Sent INVITE to {} from {}", target, sipExtension);
            return true;
        } catch (SipException | ParseException | InvalidArgumentException | RuntimeException e) {
            logger.warn("Failed to send INVITE: {}", e.getMessage(), e);
            cancelOutgoingRingingTimeout();
            clearCallContextLocked(SipCallState.IDLE);
            return false;
        }
    }

    private String buildOutgoingOfferSdp() {
        long sessionId = System.currentTimeMillis() / 1000L;
        int audioPort = localAudioRtpPort > 0 ? localAudioRtpPort : 0;
        return "v=0\r\n" + "o=- " + sessionId + " 1 IN IP4 " + localIp + "\r\n" + "s=openHAB DahuaDoor\r\n"
                + "c=IN IP4 " + localIp + "\r\n" + "t=0 0\r\n" + "m=audio " + audioPort + " RTP/AVP 97 8 0 101\r\n"
                + "a=rtpmap:97 PCM/16000\r\n" + "a=rtpmap:8 PCMA/8000\r\n" + "a=rtpmap:0 PCMU/8000\r\n"
                + "a=rtpmap:101 telephone-event/8000\r\n" + "a=fmtp:101 0-15\r\n" + "a=ptime:20\r\n" + "a=sendrecv\r\n"
                + "m=video 30000 RTP/AVP 96\r\n" + "a=rtpmap:96 H264/90000\r\n" + "a=recvonly\r\n";
    }

    public synchronized boolean sendOkResponse() {
        try {
            MessageFactory msgFactory = messageFactory;
            HeaderFactory hdrFactory = headerFactory;
            AddressFactory addrFactory = addressFactory;

            if (msgFactory == null || hdrFactory == null || addrFactory == null) {
                logger.warn("Cannot send 200 OK: SIP stack not initialized");
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

            String inviteSdp = currentInviteSdp;
            if (inviteSdp != null && !inviteSdp.isBlank()) {
                int localAudioPort = localAudioRtpPort;
                sdpParser.buildAnswerSdp(inviteSdp, localIp, localAudioPort).ifPresentOrElse(answerSdp -> {
                    try {
                        ContentTypeHeader contentTypeHeader = hdrFactory.createContentTypeHeader("application", "sdp");
                        ok.setContent(answerSdp, contentTypeHeader);
                        logger.debug("Attached SDP answer to 200 OK with local audio port {}", localAudioPort);
                    } catch (ParseException e) {
                        throw new IllegalStateException("Failed to create SDP content type header", e);
                    }
                }, () -> logger.warn("Incoming INVITE contains SDP offer, but SDP answer generation failed"));
            }

            localInviteServerTransaction.sendResponse(ok);

            activeDialog = localInviteServerTransaction.getDialog();
            callState = SipCallState.ANSWERING;
            scheduleAnsweringTimeout();
            scheduleAckFallbackTimeout();
            logger.debug("Sent 200 OK for incoming INVITE");
            return true;
        } catch (SipException | ParseException | InvalidArgumentException | RuntimeException e) {
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

            if (callState == SipCallState.OUTGOING_RINGING) {
                return cancelOutgoingInvite(trigger);
            }

            if (callState == SipCallState.RINGING) {
                Request localInviteRequest = inviteRequest;
                ServerTransaction localInviteServerTransaction = inviteServerTransaction;
                if (msgFactory == null || localInviteRequest == null || localInviteServerTransaction == null) {
                    logger.debug("Ignoring hangup in RINGING state: invite transaction already unavailable");
                    cancelAnsweringTimeout();
                    clearAudioPath("ringing-hangup-no-transaction");
                    clearCallContextLocked(SipCallState.IDLE);
                    listener.onCallEnded();
                    return true;
                }
                Response decline = msgFactory.createResponse(Response.BUSY_HERE, localInviteRequest);
                localInviteServerTransaction.sendResponse(decline);
                cancelAnsweringTimeout();
                clearAudioPath("ringing-rejected");
                clearCallContextLocked(SipCallState.IDLE);
                logger.debug("Rejected incoming INVITE with 486 Busy Here");
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
                        logger.debug("Sent BYE for SIP call in ANSWERING state (trigger={})", trigger);
                        listener.onCallTerminating();
                        return true;
                    } catch (SipException | RuntimeException e) {
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
                cancelAnsweringTimeout();
                clearAudioPath("bye-no-dialog");
                clearCallContextLocked(SipCallState.IDLE);
                listener.onCallEnded();
                return true;
            }

            if (!DialogState.CONFIRMED.equals(dialog.getState())) {
                logger.debug("Skipping BYE because SIP dialog is not CONFIRMED (state={})", dialog.getState());
                cancelAnsweringTimeout();
                clearAudioPath("bye-dialog-not-confirmed");
                clearCallContextLocked(SipCallState.IDLE);
                listener.onCallEnded();
                return true;
            }

            Request byeRequest = dialog.createRequest(Request.BYE);
            ClientTransaction byeTransaction = provider.getNewClientTransaction(byeRequest);
            dialog.sendRequest(byeTransaction);

            callState = SipCallState.TERMINATING;
            pendingHangupAfterAck = false;
            scheduleAnsweringTimeout();
            logger.debug("Sent BYE for active SIP call");
            listener.onCallTerminating();
            return true;
        } catch (SipException | ParseException | InvalidArgumentException | RuntimeException e) {
            String message = e.getMessage();
            if (message != null && message.contains("not yet established or terminated")) {
                logger.debug("Ignoring BYE for non-established or terminated dialog");
                cancelAnsweringTimeout();
                clearAudioPath("bye-not-established");
                clearCallContextLocked(SipCallState.IDLE);
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
                    || inviteServerTransaction != null || inviteClientTransaction != null || activeDialog != null
                    || currentCallerId != null || outgoingCallId != null;
            if (!shouldNotifyEnded) {
                return;
            }
            cancelAnsweringTimeout();
            cancelOutgoingRingingTimeout();
            clearAudioPath("dialog-terminated");
            clearCallContextLocked(SipCallState.IDLE);
        }
        listener.onCallEnded();
    }

    private void handleOutgoingInviteResponse(Response response, @Nullable ClientTransaction clientTransaction,
            @Nullable Dialog dialog) {
        int statusCode = response.getStatusCode();
        if (statusCode == Response.TRYING || statusCode == 101) {
            return;
        }

        if (statusCode == Response.RINGING || statusCode == Response.SESSION_PROGRESS) {
            logger.debug("Remote ringing ({})", statusCode);
            return;
        }

        if (statusCode == Response.OK) {
            cancelOutgoingRingingTimeout();
            try {
                Dialog localDialog = dialog;
                if (localDialog == null && clientTransaction != null) {
                    localDialog = clientTransaction.getDialog();
                }
                CSeqHeader cSeqHeader = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
                if (localDialog != null && cSeqHeader != null) {
                    Request ack = localDialog.createAck(cSeqHeader.getSeqNumber());
                    localDialog.sendAck(ack);
                }

                byte[] rawContent = response.getRawContent();
                String responseSdp = rawContent != null && rawContent.length > 0
                        ? new String(rawContent, StandardCharsets.UTF_8)
                        : null;
                SipAudioOffer offer = sdpParser.parseAudioOffer(responseSdp).orElse(null);

                synchronized (this) {
                    activeDialog = localDialog;
                    currentInviteSdp = responseSdp;
                    currentAudioOffer = offer;
                    callState = SipCallState.ACTIVE;
                }

                if (offer != null) {
                    activateAudioPath(offer);
                } else {
                    logger.warn("Outgoing call 200 OK did not negotiate a supported audio offer");
                }
                listener.onCallActive();
            } catch (InvalidArgumentException | SipException e) {
                logger.warn("Failed to finalize outgoing INVITE: {}", e.getMessage(), e);
                clearAudioPath("outgoing-ack-failed");
                synchronized (this) {
                    clearCallContextLocked(SipCallState.IDLE);
                }
                listener.onCallEnded();
            }
            return;
        }

        if (statusCode == Response.REQUEST_TERMINATED) {
            cancelOutgoingRingingTimeout();
            clearAudioPath("outgoing-487");
            synchronized (this) {
                clearCallContextLocked(SipCallState.IDLE);
            }
            listener.onCallEnded();
            return;
        }

        if (statusCode >= Response.BAD_REQUEST) {
            cancelOutgoingRingingTimeout();
            logger.info("Outgoing call failed with {}", statusCode);
            clearAudioPath("outgoing-error-" + statusCode);
            synchronized (this) {
                clearCallContextLocked(SipCallState.IDLE);
            }
            listener.onCallEnded();
        }
    }

    private synchronized boolean cancelOutgoingInvite(String trigger) {
        SipProvider provider = sipProvider;
        ClientTransaction clientTransaction = inviteClientTransaction;
        if (provider == null || clientTransaction == null) {
            cancelOutgoingRingingTimeout();
            clearAudioPath("outgoing-cancel-no-transaction");
            clearCallContextLocked(SipCallState.IDLE);
            listener.onCallEnded();
            return true;
        }

        try {
            Request cancelRequest = clientTransaction.createCancel();
            ClientTransaction cancelTransaction = provider.getNewClientTransaction(cancelRequest);
            cancelTransaction.sendRequest();
            callState = SipCallState.TERMINATING;
            scheduleAnsweringTimeout();
            logger.debug("Sent CANCEL for outgoing INVITE (trigger={})", trigger);
            listener.onCallTerminating();
            return true;
        } catch (SipException | RuntimeException e) {
            logger.warn("Failed to cancel outgoing INVITE: {}", e.getMessage(), e);
            cancelOutgoingRingingTimeout();
            clearAudioPath("outgoing-cancel-failed");
            clearCallContextLocked(SipCallState.IDLE);
            listener.onCallEnded();
            return false;
        }
    }

    private synchronized void cancelAnsweringTimeout() {
        ScheduledFuture<?> timeoutFuture = answeringTimeoutFuture;
        if (timeoutFuture != null) {
            timeoutFuture.cancel(false);
            answeringTimeoutFuture = null;
        }
    }

    private synchronized void cancelAckFallbackTimeout() {
        ScheduledFuture<?> timeoutFuture = ackFallbackFuture;
        if (timeoutFuture != null) {
            timeoutFuture.cancel(false);
            ackFallbackFuture = null;
        }
    }

    private synchronized void cancelOutgoingRingingTimeout() {
        ScheduledFuture<?> timeoutFuture = outgoingRingingTimeoutFuture;
        if (timeoutFuture != null) {
            timeoutFuture.cancel(false);
            outgoingRingingTimeoutFuture = null;
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
                clearAudioPath("state-timeout-" + timeoutState.name().toLowerCase());
                clearCallContextLocked(SipCallState.IDLE);
                answeringTimeoutFuture = null;
                shouldNotifyEnded = true;
            }
            if (shouldNotifyEnded) {
                listener.onCallEnded();
            }
        }, timeoutSeconds, TimeUnit.SECONDS);
    }

    private synchronized void scheduleOutgoingRingingTimeout() {
        cancelOutgoingRingingTimeout();
        if (callState != SipCallState.OUTGOING_RINGING) {
            return;
        }

        outgoingRingingTimeoutFuture = callStateTimeoutScheduler.schedule(() -> {
            boolean shouldNotifyEnded = false;
            synchronized (SipClient.this) {
                if (callState != SipCallState.OUTGOING_RINGING) {
                    return;
                }
                logger.warn("Outgoing SIP call stuck in {} for {}s - forcing call end", SipCallState.OUTGOING_RINGING,
                        OUTGOING_RINGING_TIMEOUT_SECONDS);
                clearAudioPath("state-timeout-outgoing-ringing");
                clearCallContextLocked(SipCallState.IDLE);
                outgoingRingingTimeoutFuture = null;
                shouldNotifyEnded = true;
            }
            if (shouldNotifyEnded) {
                listener.onCallEnded();
            }
        }, OUTGOING_RINGING_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    private synchronized void scheduleAckFallbackTimeout() {
        cancelAckFallbackTimeout();
        if (callState != SipCallState.ANSWERING) {
            return;
        }

        ackFallbackFuture = callStateTimeoutScheduler.schedule(() -> {
            SipAudioOffer offer;
            boolean shouldSendDeferredBye;

            synchronized (SipClient.this) {
                if (callState != SipCallState.ANSWERING) {
                    ackFallbackFuture = null;
                    return;
                }

                cancelAnsweringTimeout();
                ServerTransaction localInviteServerTransaction = inviteServerTransaction;
                if (activeDialog == null && localInviteServerTransaction != null) {
                    activeDialog = localInviteServerTransaction.getDialog();
                }

                offer = currentAudioOffer;
                callState = SipCallState.ACTIVE;
                shouldSendDeferredBye = pendingHangupAfterAck;
                pendingHangupAfterAck = false;
                ackFallbackFuture = null;
            }

            if (offer != null) {
                logger.info("No ACK received after 200 OK within {} ms - activating audio path via fallback",
                        ACK_FALLBACK_TIMEOUT_MILLIS);
                activateAudioPath(offer);
            } else {
                logger.warn("ACK fallback triggered but no audio offer available - skipping audio path activation");
            }

            listener.onCallActive();

            if (shouldSendDeferredBye) {
                logger.debug("ACK fallback active, executing deferred hangup");
                sendBye("deferred-after-ack-timeout");
            }
        }, ACK_FALLBACK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
    }

    private void activateAudioPath(SipAudioOffer offer) {
        SipBackchannelRtpRelay relay = backchannelRelay;
        if (relay != null) {
            logger.debug("Activating SIP audio path via backchannel relay for {}:{} (pt={}, codec={} rate={}Hz)",
                    offer.getRemoteHost(), offer.getRemotePort(), offer.getPayloadType(), offer.getCodecName(),
                    offer.getClockRate());
            relay.setTarget(offer, "ack");
        } else {
            logger.warn(
                    "No backchannel relay available - skipping SIP audio path activation for {}:{} (pt={}, codec={} rate={}Hz)",
                    offer.getRemoteHost(), offer.getRemotePort(), offer.getPayloadType(), offer.getCodecName(),
                    offer.getClockRate());
        }
    }

    private void clearAudioPath(String reason) {
        logger.debug("Clearing SIP audio path (reason={})", reason);
        SipBackchannelRtpRelay relay = backchannelRelay;
        if (relay != null) {
            relay.setTarget(null, reason);
        }
    }

    private void clearCallContextLocked(SipCallState nextState) {
        callState = nextState;
        cancelAckFallbackTimeout();
        inviteRequest = null;
        inviteServerTransaction = null;
        inviteClientTransaction = null;
        currentInviteSdp = null;
        currentAudioOffer = null;
        activeDialog = null;
        currentCallerId = null;
        outgoingCallId = null;
        pendingHangupAfterAck = false;
    }
}
