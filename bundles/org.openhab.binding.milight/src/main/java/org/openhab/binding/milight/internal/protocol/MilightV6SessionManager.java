/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.milight.internal.protocol;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The milightV6 protocol is stateful and needs an established session for each client.
 * This class handles the password bytes, session bytes and sequence number.
 *
 * The session handshake is a 3-way handshake. First we are sending either a general
 * search for bridges command or a search for a specific bridge command (containing the bridge ID)
 * with our own client session bytes included.
 *
 * The response will assign as session bytes that we can use for subsequent commands
 * see {@link MilightV6SessionManager#clientSID1} and see {@link MilightV6SessionManager#clientSID2}.
 *
 * We register ourself to the bridge now and finalise the handshake by sending a register command
 * see {@link #sendRegistration(DatagramSocket)} to the bridge.
 *
 * From this point on we are required to send keep alive packets to the bridge every ~10sec
 * to keep the session alive. Because each command we send is confirmed by the bridge, we know if
 * our session is still valid and can redo the session handshake if necessary.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class MilightV6SessionManager implements Runnable, Closeable {
    protected final Logger logger = LoggerFactory.getLogger(MilightV6SessionManager.class);

    // The used sequence number for a command will be present in the response of the iBox. This
    // allows us to identify failed command deliveries.
    private int sequenceNo = 0;

    // Password bytes 1 and 2
    public byte[] pw = { 0, 0 };

    // Session bytes 1 and 2
    public byte[] sid = { 0, 0 };

    // Client session bytes 1 and 2. Those are fixed for now.
    public final byte clientSID1 = (byte) 0xab;
    public final byte clientSID2 = (byte) 0xde;

    // We need the bridge mac (bridge ID) in many responses to the session commands.
    private final byte[] bridgeMAC = { (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0 };

    /**
     * The session handshake is a 3 way handshake.
     */
    public enum SessionState {
        // No session established and nothing in progress
        SESSION_INVALID,
        // Send "find bridge" and wait for response
        SESSION_WAIT_FOR_BRIDGE,
        // Send "get session bytes" and wait for response
        SESSION_WAIT_FOR_SESSION_SID,
        // Session bytes received, register session now
        SESSION_NEED_REGISTER,
        // Registration complete, session is valid now
        SESSION_VALID,
        // The session is still active, a keep alive was just received.
        SESSION_VALID_KEEP_ALIVE,
    }

    public enum StateMachineInput {
        NO_INPUT,
        TIMEOUT,
        INVALID_COMMAND,
        KEEP_ALIVE_RECEIVED,
        BRIDGE_CONFIRMED,
        SESSION_ID_RECEIVED,
        SESSION_ESTABLISHED,
    }

    private SessionState sessionState = SessionState.SESSION_INVALID;

    // Implement this interface to get notifications about the current session state.
    public interface ISessionState {
        /**
         * Notifies about a state change of {@link MilightV6SessionManager}.
         * SESSION_VALID_KEEP_ALIVE will be reported in the interval, given to the constructor of
         * {@link MilightV6SessionManager}.
         *
         * @param state The new state
         * @param address The remote IP address. Only guaranteed to be non null in the SESSION_VALID* states.
         */
        void sessionStateChanged(SessionState state, @Nullable InetAddress address);
    }

    private final ISessionState observer;

    /** Used to determine if the session needs a refresh */
    private Instant lastSessionConfirmed = Instant.now();
    /** Quits the receive thread if set to true */
    private volatile boolean willbeclosed = false;
    /** Keep track of send commands and their sequence number */
    private final Map<Integer, Instant> usedSequenceNo = new TreeMap<>();
    /** The receive thread for all bridge responses. */
    private final Thread sessionThread;

    private final String bridgeId;
    private @Nullable DatagramSocket datagramSocket;
    private @Nullable CompletableFuture<DatagramSocket> startFuture;

    /**
     * Usually we only send BROADCAST packets. If we know the IP address of the bridge though,
     * we should try UNICAST packets before falling back to BROADCAST.
     * This allows communication with the bridge even if it is in another subnet.
     */
    private @Nullable final InetAddress destIP;
    /**
     * We cache the last known IP to avoid using broadcast.
     */
    private @Nullable InetAddress lastKnownIP;

    private final int port;

    /** The maximum duration for a session registration / keep alive process in milliseconds. */
    public static final int TIMEOUT_MS = 10000;
    /** A packet is handled as lost / not confirmed after this time */
    public static final int MAX_PACKET_IN_FLIGHT_MS = 2000;
    /** The keep alive interval. Must be between 100 and REG_TIMEOUT_MS milliseconds or 0 */
    private final int keepAliveInterval;

    /**
     * A session manager for the V6 bridge needs a way to send data (a QueuedSend object), the destination bridge ID, a
     * scheduler for timeout timers and optionally an observer for session state changes.
     *
     * @param bridgeId Destination bridge ID. If the bridge ID for whatever reason changes, you need to create a new
     *            session manager object
     * @param observer Get notifications of state changes
     * @param destIP If you know the bridge IP address, provide it here.
     * @param port The bridge port
     * @param keepAliveInterval The keep alive interval. Must be between 100 and REG_TIMEOUT_MS milliseconds.
     *            if it is equal to REG_TIMEOUT_MS, then a new session will be established instead of renewing the
     *            current one.
     * @param pw The two "password" bytes for the bridge
     */
    public MilightV6SessionManager(String bridgeId, ISessionState observer, @Nullable InetAddress destIP, int port,
            int keepAliveInterval, byte[] pw) {
        this.bridgeId = bridgeId;
        this.observer = observer;
        this.destIP = destIP;
        this.lastKnownIP = destIP;
        this.port = port;
        this.keepAliveInterval = keepAliveInterval;
        this.pw[0] = pw[0];
        this.pw[1] = pw[1];
        for (int i = 0; i < 6; ++i) {
            bridgeMAC[i] = Integer.valueOf(bridgeId.substring(i * 2, i * 2 + 2), 16).byteValue();
        }
        if (keepAliveInterval < 100 || keepAliveInterval > TIMEOUT_MS) {
            throw new IllegalArgumentException("keepAliveInterval not within given limits!");
        }

        sessionThread = new Thread(this, "SessionThread");
    }

    /**
     * Start the session thread if it is not already running
     */
    public CompletableFuture<DatagramSocket> start() {
        if (willbeclosed) {
            CompletableFuture<DatagramSocket> f = new CompletableFuture<>();
            f.completeExceptionally(new IllegalStateException("will be closed"));
            return f;
        }
        if (sessionThread.isAlive()) {
            DatagramSocket s = datagramSocket;
            assert s != null;
            return CompletableFuture.completedFuture(s);
        }

        CompletableFuture<DatagramSocket> f = new CompletableFuture<>();
        startFuture = f;
        sessionThread.start();
        return f;
    }

    /**
     * You have to call that if you are done with this object. Cleans up the receive thread.
     */
    @Override
    public void close() throws IOException {
        if (willbeclosed) {
            return;
        }
        willbeclosed = true;
        final DatagramSocket socket = datagramSocket;
        if (socket != null) {
            socket.close();
        }
        sessionThread.interrupt();
        try {
            sessionThread.join();
        } catch (InterruptedException e) {
        }
    }

    // Set the session id bytes for bridge access. Usually they are acquired automatically
    // during the session handshake.
    public void setSessionID(byte[] sid) {
        this.sid[0] = sid[0];
        this.sid[1] = sid[1];
        sessionState = SessionState.SESSION_NEED_REGISTER;
    }

    // Return the session bytes as hex string
    public String getSession() {
        return String.format("%02X %02X", this.sid[0], this.sid[1]);
    }

    public Instant getLastSessionValidConfirmation() {
        return lastSessionConfirmed;
    }

    // Get a new sequence number. Add that to a queue of used sequence numbers.
    // The bridge response will remove the queued number. This method also checks
    // for non confirmed sequence numbers older that 2 seconds and report them.
    public int getNextSequenceNo() {
        int currentSequenceNo = this.sequenceNo;
        usedSequenceNo.put(currentSequenceNo, Instant.now());
        ++sequenceNo;
        return currentSequenceNo;
    }

    public static byte firstSeqByte(int seq) {
        return (byte) (seq & 0xff);
    }

    public static byte secondSeqByte(int seq) {
        return (byte) ((seq >> 8) & 0xff);
    }

    /**
     * Send a search for bridgeID packet on all network interfaces.
     * This is used for the initial way to determine the IP of the bridge as well
     * as if the IP of a bridge has changed and the session got invalid because of that.
     *
     * A response will assign us session bytes.
     *
     * @throws InterruptedException
     */
    @SuppressWarnings({ "null", "unused" })
    private void sendSearchForBroadcast(DatagramSocket datagramSocket) {
        byte[] t = new byte[] { (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0A, (byte) 0x02,
                clientSID1, clientSID2, (byte) 0x01, bridgeMAC[0], bridgeMAC[1], bridgeMAC[2], bridgeMAC[3],
                bridgeMAC[4], bridgeMAC[5] };
        if (lastKnownIP != null) {
            try {
                datagramSocket.send(new DatagramPacket(t, t.length, lastKnownIP, port));
            } catch (IOException e) {
                logger.warn("Could not send discover packet! {}", e.getLocalizedMessage());
            }
            return;
        }

        Enumeration<NetworkInterface> enumNetworkInterfaces;
        try {
            enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException socketException) {
            logger.warn("Could not enumerate network interfaces for sending the discover packet!", socketException);
            return;
        }
        DatagramPacket packet = new DatagramPacket(t, t.length, lastKnownIP, port);
        while (enumNetworkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
            Iterator<InterfaceAddress> it = networkInterface.getInterfaceAddresses().iterator();
            while (it.hasNext()) {
                InterfaceAddress address = it.next();
                if (address == null) {
                    continue;
                }
                InetAddress broadcast = address.getBroadcast();
                if (broadcast != null && !address.getAddress().isLoopbackAddress()) {
                    packet.setAddress(broadcast);
                    try {
                        datagramSocket.send(packet);
                    } catch (IOException e) {
                        logger.warn("Could not send discovery packet! {}", e.getLocalizedMessage());
                    }
                }
            }
        }
    }

    // Search for a specific bridge (our bridge). A response will assign us session bytes.
    // private void send_search_for() {
    // sendQueue.queue(AbstractBulbInterface.CAT_SESSION, searchForPacket());
    // }

    private void sendEstablishSession(DatagramSocket datagramSocket) throws IOException {
        final InetAddress address = lastKnownIP;
        if (address == null) {
            return;
        }
        byte unknown = (byte) 0x1E; // Either checksum or counter. Was 64 and 1e so far.
        byte[] t = { (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x16, (byte) 0x02, (byte) 0x62,
                (byte) 0x3A, (byte) 0xD5, (byte) 0xED, (byte) 0xA3, (byte) 0x01, (byte) 0xAE, (byte) 0x08, (byte) 0x2D,
                (byte) 0x46, (byte) 0x61, (byte) 0x41, (byte) 0xA7, (byte) 0xF6, (byte) 0xDC, (byte) 0xAF, clientSID1,
                clientSID2, (byte) 0x00, (byte) 0x00, unknown };

        datagramSocket.send(new DatagramPacket(t, t.length, address, port));
    }

    // Some apps first send {@see send_establish_session} and with the aquired session bytes they
    // subsequently send this command for establishing the session. This is not well documented unfortunately.
    @SuppressWarnings("unused")
    private void sendPreRegistration(DatagramSocket datagramSocket) throws IOException {
        final InetAddress address = lastKnownIP;
        if (address == null) {
            return;
        }
        byte[] t = { 0x30, 0, 0, 0, 3, sid[0], sid[1], 1, 0 };
        datagramSocket.send(new DatagramPacket(t, t.length, address, port));
    }

    // After the bridges knows our client session bytes and we know the bridge session bytes, we do a final
    // registration with this command. The response will again contain the bridge ID and the session should
    // be established by then.
    private void sendRegistration(DatagramSocket datagramSocket) throws IOException {
        final InetAddress address = lastKnownIP;
        if (address == null) {
            return;
        }

        int seq = getNextSequenceNo();
        byte[] t = { (byte) 0x80, 0x00, 0x00, 0x00, 0x11, sid[0], sid[1], firstSeqByte(seq), secondSeqByte(seq), 0x00,
                0x33, pw[0], pw[1], 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) (0x33 + pw[0] + pw[1]) };
        datagramSocket.send(new DatagramPacket(t, t.length, address, port));
    }

    /**
     * Constructs a 0x80... command which us used for all colour,brightness,saturation,mode operations.
     * The session ID, password and sequence number is automatically inserted from this object.
     *
     * Produces data like:
     * 
     * <pre>
     * {@code
     * SN: Sequence number
     * S1: SessionID1
     * S2: SessionID2
     * P1/P2: Password bytes
     * WB: Remote (08) or iBox integrated bulb (00)
     * ZN: Zone {Zone1-4 0=All}
     * CK: Checksum
     *
     * #zone 1 on
     * &#64; 80 00 00 00 11 84 00 00 0c 00 31 00 00 08 04 01 00 00 00 01 00 3f
     *
     * Colors:
     * CC: Color value (hue)
     * 80 00 00 00 11 S1 S2 SN SN 00 31 P1 P2 WB 01 CC CC CC CC ZN 00 CK
     *
     * 80 00 00 00 11 D4 00 00 12 00 31 00 00 08 01 FF FF FF FF 01 00 38
     * }
     * </pre>
     *
     * @return
     */
    public byte[] makeCommand(byte wb, int zone, int... data) {
        int seq = getNextSequenceNo();
        byte[] t = { (byte) 0x80, 0x00, 0x00, 0x00, 0x11, sid[0], sid[1], MilightV6SessionManager.firstSeqByte(seq),
                MilightV6SessionManager.secondSeqByte(seq), 0x00, 0x31, pw[0], pw[1], wb, 0, 0, 0, 0, 0, (byte) zone, 0,
                0 };

        for (int i = 0; i < data.length; ++i) {
            t[14 + i] = (byte) data[i];
        }

        byte chksum = (byte) (t[10 + 0] + t[10 + 1] + t[10 + 2] + t[10 + 3] + t[10 + 4] + t[10 + 5] + t[10 + 6]
                + t[10 + 7] + t[10 + 8] + zone);
        t[21] = chksum;
        return t;
    }

    /**
     * Constructs a 0x3D or 0x3E link/unlink command.
     * The session ID, password and sequence number is automatically inserted from this object.
     *
     * WB: Remote (08) or iBox integrated bulb (00)
     */
    public byte[] makeLink(byte wb, int zone, boolean link) {
        int seq = getNextSequenceNo();
        byte[] t = { (link ? (byte) 0x3D : (byte) 0x3E), 0x00, 0x00, 0x00, 0x11, sid[0], sid[1],
                MilightV6SessionManager.firstSeqByte(seq), MilightV6SessionManager.secondSeqByte(seq), 0x00, 0x31,
                pw[0], pw[1], wb, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) zone, 0x00, 0x00 };

        byte chksum = (byte) (t[10 + 0] + t[10 + 1] + t[10 + 2] + t[10 + 3] + t[10 + 4] + t[10 + 5] + t[10 + 6]
                + t[10 + 7] + t[10 + 8] + zone);
        t[21] = chksum;
        return t;
    }

    /**
     * The main state machine of the session handshake.
     *
     * @throws InterruptedException
     * @throws IOException
     */
    private void sessionStateMachine(DatagramSocket datagramSocket, StateMachineInput input) throws IOException {
        final SessionState lastSessionState = sessionState;

        // Check for timeout
        final Instant current = Instant.now();
        final Duration timeElapsed = Duration.between(lastSessionConfirmed, current);
        if (timeElapsed.toMillis() > TIMEOUT_MS) {
            if (sessionState != SessionState.SESSION_WAIT_FOR_BRIDGE) {
                logger.warn("Session timeout!");
            }
            // One reason we failed, might be that a last known IP is not correct anymore.
            // Reset to the given dest IP (which might be null).
            lastKnownIP = destIP;
            sessionState = SessionState.SESSION_INVALID;
        }

        if (input == StateMachineInput.INVALID_COMMAND) {
            sessionState = SessionState.SESSION_INVALID;
        }

        // Check old seq no:
        for (Iterator<Map.Entry<Integer, Instant>> it = usedSequenceNo.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Integer, Instant> entry = it.next();
            if (Duration.between(entry.getValue(), current).toMillis() > MAX_PACKET_IN_FLIGHT_MS) {
                logger.debug("Command not confirmed: {}", entry.getKey());
                it.remove();
            }
        }

        switch (sessionState) {
            case SESSION_INVALID:
                usedSequenceNo.clear();
                sessionState = SessionState.SESSION_WAIT_FOR_BRIDGE;
                lastSessionConfirmed = Instant.now();
            case SESSION_WAIT_FOR_BRIDGE:
                if (input == StateMachineInput.BRIDGE_CONFIRMED) {
                    sessionState = SessionState.SESSION_WAIT_FOR_SESSION_SID;
                } else {
                    datagramSocket.setSoTimeout(150);
                    sendSearchForBroadcast(datagramSocket);
                    break;
                }
            case SESSION_WAIT_FOR_SESSION_SID:
                if (input == StateMachineInput.SESSION_ID_RECEIVED) {
                    if (ProtocolConstants.DEBUG_SESSION) {
                        logger.debug("Session ID received: {}", String.format("%02X %02X", this.sid[0], this.sid[1]));
                    }
                    sessionState = SessionState.SESSION_NEED_REGISTER;
                } else {
                    datagramSocket.setSoTimeout(300);
                    sendEstablishSession(datagramSocket);
                    break;
                }
            case SESSION_NEED_REGISTER:
                if (input == StateMachineInput.SESSION_ESTABLISHED) {
                    sessionState = SessionState.SESSION_VALID;
                    lastSessionConfirmed = Instant.now();
                    if (ProtocolConstants.DEBUG_SESSION) {
                        logger.debug("Registration complete");
                    }
                } else {
                    datagramSocket.setSoTimeout(300);
                    sendRegistration(datagramSocket);
                    break;
                }
            case SESSION_VALID_KEEP_ALIVE:
            case SESSION_VALID:
                if (input == StateMachineInput.KEEP_ALIVE_RECEIVED) {
                    lastSessionConfirmed = Instant.now();
                    observer.sessionStateChanged(SessionState.SESSION_VALID_KEEP_ALIVE, lastKnownIP);
                } else {
                    final InetAddress address = lastKnownIP;
                    if (keepAliveInterval > 0 && timeElapsed.toMillis() > keepAliveInterval && address != null) {
                        // Send keep alive
                        byte[] t = { (byte) 0xD0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, sid[0], sid[1] };
                        datagramSocket.send(new DatagramPacket(t, t.length, address, port));
                    }
                    // Increase socket timeout to wake up for the next keep alive interval
                    datagramSocket.setSoTimeout(keepAliveInterval);
                }
                break;
        }

        if (lastSessionState != sessionState) {
            observer.sessionStateChanged(sessionState, lastKnownIP);
        }
    }

    private void logUnknownPacket(byte[] data, int len, String reason) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < len; ++i) {
            s.append(String.format("%02X ", data[i]));
        }
        s.append("Sid: ");
        s.append(String.format("%02X ", clientSID1));
        s.append(String.format("%02X ", clientSID2));
        logger.info("{} ({}): {}", reason, bridgeId, s);
    }

    /**
     * The session thread executes this run() method and a blocking UDP receive
     * is performed in a loop.
     */
    @SuppressWarnings({ "null", "unused" })
    @Override
    public void run() {
        try (DatagramSocket datagramSocket = new DatagramSocket(null)) {
            this.datagramSocket = datagramSocket;
            datagramSocket.setBroadcast(true);
            datagramSocket.setReuseAddress(true);
            datagramSocket.setSoTimeout(150);
            datagramSocket.bind(null);

            if (ProtocolConstants.DEBUG_SESSION) {
                logger.debug("MilightCommunicationV6 receive thread ready");
            }

            // Inform the start future about the datagram socket
            CompletableFuture<DatagramSocket> f = startFuture;
            if (f != null) {
                f.complete(datagramSocket);
                startFuture = null;
            }

            byte[] buffer = new byte[1024];
            DatagramPacket rPacket = new DatagramPacket(buffer, buffer.length);

            sessionStateMachine(datagramSocket, StateMachineInput.NO_INPUT);

            // Now loop forever, waiting to receive packets and printing them.
            while (!willbeclosed) {
                rPacket.setLength(buffer.length);
                try {
                    datagramSocket.receive(rPacket);
                } catch (SocketTimeoutException e) {
                    sessionStateMachine(datagramSocket, StateMachineInput.TIMEOUT);
                    continue;
                }
                int len = rPacket.getLength();

                if (len < 5 || buffer[1] != 0 || buffer[2] != 0 || buffer[3] != 0) {
                    logUnknownPacket(buffer, len, "Not an iBox response!");
                    continue;
                }

                int expectedLen = buffer[4] + 5;

                if (expectedLen > len) {
                    logUnknownPacket(buffer, len, "Unexpected size!");
                    continue;
                }
                switch (buffer[0]) {
                    // 13 00 00 00 0A 03 D3 54 11 (AC CF 23 F5 7A D4)
                    case (byte) 0x13: {
                        boolean eq = ByteBuffer.wrap(bridgeMAC, 0, 6).equals(ByteBuffer.wrap(buffer, 9, 6));
                        if (eq) {
                            logger.debug("TODO: Feedback required");
                            // I have no clue what that packet means. But the bridge is going to timeout the next
                            // keep alive and it is a good idea to start the session again.
                        } else {
                            logger.info("Unknown 0x13 received, but not for our bridge ({})", bridgeId);
                        }
                        break;
                    }
                    // 18 00 00 00 40 02 (AC CF 23 F5 7A D4) 00 20 39 38 35 62 31 35 37 62 66 36 66 63 34 33 33 36 38 61
                    // 36 33 34 36 37 65 61 33 62 31 39 64 30 64 01 00 01 17 63 00 00 05 00 09 78 6C 69 6E 6B 5F 64 65
                    // 76 07 5B CD 15
                    // ASCII string contained: 985b157bf6fc43368a63467ea3b19d0dc .. xlink_dev
                    // Response to the v6 SEARCH and the SEARCH FOR commands to look for new or known devices.
                    // Our session id will be transfered in this process (!= bridge session id)
                    case (byte) 0x18: {
                        boolean eq = ByteBuffer.wrap(bridgeMAC, 0, 6).equals(ByteBuffer.wrap(buffer, 6, 6));
                        if (eq) {
                            if (ProtocolConstants.DEBUG_SESSION) {
                                logger.debug("Session ID reestablished");
                            }
                            lastKnownIP = rPacket.getAddress();
                            sessionStateMachine(datagramSocket, StateMachineInput.BRIDGE_CONFIRMED);
                        } else {
                            logger.info("Session ID received, but not for our bridge ({})", bridgeId);
                            logUnknownPacket(buffer, len, "ID not matching");
                        }

                        break;
                    }
                    // 28 00 00 00 11 00 02 (AC CF 23 F5 7A D4) 50 AA 4D 2A 00 01 SS_ID 00
                    // Response to the keepAlive() packet if session is not valid yet.
                    // Should contain the session ids
                    case (byte) 0x28: {
                        boolean eq = ByteBuffer.wrap(bridgeMAC, 0, 6).equals(ByteBuffer.wrap(buffer, 7, 6));
                        if (eq) {
                            this.sid[0] = buffer[19];
                            this.sid[1] = buffer[20];
                            sessionStateMachine(datagramSocket, StateMachineInput.SESSION_ID_RECEIVED);
                        } else {
                            logger.info("Session ID received, but not for our bridge ({})", bridgeId);
                            logUnknownPacket(buffer, len, "ID not matching");
                        }

                        break;
                    }
                    // 80 00 00 00 15 (AC CF 23 F5 7A D4) 05 02 00 34 00 00 00 00 00 00 00 00 00 00 34
                    // Response to the registration packet
                    case (byte) 0x80: {
                        boolean eq = ByteBuffer.wrap(bridgeMAC, 0, 6).equals(ByteBuffer.wrap(buffer, 5, 6));
                        if (eq) {
                            sessionStateMachine(datagramSocket, StateMachineInput.SESSION_ESTABLISHED);
                        } else {
                            logger.info("Registration received, but not for our bridge ({})", bridgeId);
                            logUnknownPacket(buffer, len, "ID not matching");
                        }
                        break;
                    }
                    // 88 00 00 00 03 SN SN OK // two byte sequence number, we use the later one only.
                    // OK: is 00 if ok or 01 if failed
                    case (byte) 0x88:
                        int seq = Byte.toUnsignedInt(buffer[6]) + Byte.toUnsignedInt(buffer[7]) * 256;
                        Instant timePacketWasSend = usedSequenceNo.remove(seq);
                        if (timePacketWasSend != null) {
                            if (ProtocolConstants.DEBUG_SESSION) {
                                logger.debug("Confirmation received for command: {}", String.valueOf(seq));
                            }
                            if (buffer[8] == 1) {
                                logger.warn("Command {} failed", seq);
                            }
                        } else {
                            // another participant might have established a session from the same host
                            logger.info("Confirmation received for unsend command. Sequence number: {}",
                                    String.valueOf(seq));
                        }
                        break;
                    // D8 00 00 00 07 (AC CF 23 F5 7A D4) 01
                    // Response to the keepAlive() packet
                    case (byte) 0xD8: {
                        boolean eq = ByteBuffer.wrap(bridgeMAC, 0, 6).equals(ByteBuffer.wrap(buffer, 5, 6));
                        if (eq) {
                            sessionStateMachine(datagramSocket, StateMachineInput.KEEP_ALIVE_RECEIVED);
                        } else {
                            logger.info("Keep alive received but not for our bridge ({})", bridgeId);
                            logUnknownPacket(buffer, len, "ID not matching");
                        }
                        break;
                    }
                    default:
                        logUnknownPacket(buffer, len, "No valid start byte");
                }
            }
        } catch (IOException e) {
            if (!willbeclosed) {
                logger.warn("Session Manager receive thread failed: {}", e.getLocalizedMessage(), e);
            }
        } finally {
            this.datagramSocket = null;
        }
        if (ProtocolConstants.DEBUG_SESSION) {
            logger.debug("MilightCommunicationV6 receive thread stopped");
        }
    }

    // Return true if the session is established successfully
    public boolean isValid() {
        return sessionState == SessionState.SESSION_VALID;
    }
}
