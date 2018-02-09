/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.milight.internal.protocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
 * (@see MilightV6SessionManager.sid1} and {@see MilightV6SessionManager.sid2}.
 *
 * We register ourself to the bridge now and finalise the handshake by sending a register command
 * {@see MilightV6SessionManager.send_registration} to the bridge.
 *
 * From this point on we are required to send keep alive packets to the bridge every ~10sec
 * to keep the session alive. Because each command we send is confirmed by the bridge, we know if
 * our session is still valid and can redo the session handshake if necessary.
 *
 * @author David Graeff <david.graeff@web.de>
 * @since 2.1
 */
public class MilightV6SessionManager implements Runnable {
    protected final Logger logger = LoggerFactory.getLogger(MilightV6SessionManager.class);

    // The used sequence number for a command will be present in the response of the iBox. This
    // allows us to identify failed command deliveries.
    private byte sequence_no = 0;
    // The sequence number is 16 bits, we use 8 bits only and a fixed value for the other 8 bits.
    private byte fixed_seq_no = 0x00;

    // Password bytes 1 and 2
    private byte pw1 = 0;
    private byte pw2 = 0;

    // Session bytes 1 and 2
    private byte sid1 = 0;
    private byte sid2 = 0;

    // Client session bytes 1 and 2. Those are fixed for now.
    private byte client_sid1 = (byte) 0xab;
    private byte client_sid2 = (byte) 0xde;

    // We need the bridge mac (bridge ID) in many responses to the session commands.
    private byte[] BRIDGE_MAC = { (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0 };

    /**
     * The session handshake is a 3 way handshake.
     */
    public enum SessionState {
        // No session established and nothing in progress
        SESSION_INVALID,
        // Send "find bridge" and wait for response
        SESSION_WAIT_FOR_BRIDGE,
        // Send "get session bytes" and wait for response
        SESSION_WAIT_FOR_BRIDGE_SID,
        // Session bytes received, register session now
        SESSION_NEED_REGISTER,
        // Registration complete, session is valid now
        SESSION_VALID
    }

    private SessionState sessionState = SessionState.SESSION_INVALID;

    // Implement this interface to get notifications about the current session state.
    public interface ISessionState {
        void sessionStateChanged(SessionState state);
    }

    private final ISessionState observer;

    // Used to determine if the session needs a refresh
    private long lastSessionConfirmed = 0;
    // Quits the receive thread if set to true
    private boolean willbeclosed = false;
    // Keep track of send commands and their sequence number
    private Map<Byte, Long> used_sequence_no = new TreeMap<Byte, Long>();
    // The receive thread for all bridge responses.
    private Thread sessionThread;

    private final QueuedSend sendQueue;
    private final String bridgeId;

    // Used to create the timeout timer
    private ScheduledExecutorService scheduler;

    // The session timeout timer. Used for cancelling it if the handshake process progresses.
    private ScheduledFuture<?> checkHandshakeTimer = null;

    // Usually we only send BROADCAST packets. If we know the IP address of the bridge though,
    // we should try UNICAST packets before falling back to BROADCAST.
    // This allows communication with the bridge even if it is in another subnet.
    private InetAddress lastKnownIP;

    // Print out a lot of useful debug data for the session establishing
    private static final boolean DEBUG_SESSION = false;

    // Abort a session registration process after this time in seconds
    private static final long REG_TIMEOUT_SEC = 3;

    /**
     * A session manager for the V6 bridge needs a way to send data (a QueuedSend object), the destination bridge ID, a
     * scheduler for timeout timers and optionally an observer for session state changes.
     *
     * @param sendQueue A send queue. Never remove or change that object while the session manager is still working.
     * @param bridgeId Destination bridge ID. If the bridge ID for whatever reason changes, you need to create a new
     *            session manager object
     * @param scheduler A framework scheduler to create timeout events.
     * @param observer Get notifications of state changes
     * @param lastKnownIP If you know the bridge IP address, provide it here. Null otherwise.
     */
    public MilightV6SessionManager(QueuedSend sendQueue, String bridgeId, ScheduledExecutorService scheduler,
            ISessionState observer, InetAddress lastKnownIP) {
        this.sendQueue = sendQueue;
        this.bridgeId = bridgeId;
        this.scheduler = scheduler;
        this.observer = observer;
        this.lastKnownIP = lastKnownIP;
        for (int i = 0; i < 6; ++i) {
            BRIDGE_MAC[i] = Integer.valueOf(bridgeId.substring(i * 2, i * 2 + 2), 16).byteValue();
        }
        sessionThread = new Thread(this, "SessionThread");
        sessionThread.start();
    }

    // Return the first byte of the two bytes password for bridge access
    public byte getPw1() {
        return pw1;
    }

    // Return the second byte of the two bytes password for bridge access
    public byte getPw2() {
        return pw2;
    }

    // Return the first byte of the two bytes session id for bridge access
    public byte getSid1() {
        return sid1;
    }

    // Return the second byte of the two bytes session id for bridge access
    public byte getSid2() {
        return sid2;
    }

    // Set the password bytes for bridge access. Usually 0, 0.
    public void setPasswordBytes(byte pw1, byte pw2) {
        this.pw1 = pw1;
        this.pw2 = pw2;
    }

    // Set the session id bytes for bridge access. Usually they are acquired automatically
    // during the session handshake.
    public void setSessionID(byte sid1, byte sid2) {
        this.sid1 = sid1;
        this.sid2 = sid2;
        sessionState = SessionState.SESSION_NEED_REGISTER;
    }

    // Return the session bytes as hex string
    public String getSession() {
        return String.format("%02X %02X", sid1, sid2);
    }

    public long getLastSessionValidConfirmation() {
        return lastSessionConfirmed;
    }

    // Get the first byte of a new sequence number. Add that to a queue of used sequence numbers.
    // The bridge response will remove the queued number. This method also checks
    // for non confirmed sequence numbers older that 2 seconds and report them.
    public byte getNextSequenceNo1() {
        return fixed_seq_no;
    }

    // Get the second byte of a new sequence number. Add that to a queue of used sequence numbers.
    // The bridge response will remove the queued number. This method also checks
    // for non confirmed sequence numbers older that 2 seconds and report them.
    byte getNextSequenceNo2() {
        byte t = sequence_no;
        long current = System.currentTimeMillis();
        used_sequence_no.put(t, current);
        // Check old seq no:
        for (Iterator<Map.Entry<Byte, Long>> it = used_sequence_no.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Byte, Long> entry = it.next();
            if (entry.getValue() + 2000 < current) {
                logger.warn("Command not confirmed: {}", entry.getKey());
                it.remove();
            }
        }
        ++sequence_no;
        return t;
    }

    // You have to call that if you are done with this object, we have to clean up
    // some stuff, like the session receive thread.
    public void dispose() {
        willbeclosed = true;
        scheduler = null;
        if (sessionThread != null) {
            try {
                sessionThread.join(100);
            } catch (InterruptedException e) {
            }
            sessionThread.interrupt();
        }
        sessionThread = null;
    }

    private byte[] search_for_packet() {
        return new byte[] { (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0A, (byte) 0x02, client_sid1,
                client_sid2, (byte) 0x01, BRIDGE_MAC[0], BRIDGE_MAC[1], BRIDGE_MAC[2], BRIDGE_MAC[3], BRIDGE_MAC[4],
                BRIDGE_MAC[5] };
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
    private void send_search_for_broadcast() throws InterruptedException {
        byte[] buf = new byte[1000];

        DatagramPacket p = new DatagramPacket(buf, buf.length);
        p.setPort(sendQueue.getPort());
        p.setData(search_for_packet());
        if (lastKnownIP != null) {
            p.setAddress(lastKnownIP);
            try {
                sendQueue.datagramSocket.send(p);
                Thread.sleep(10);
                sendQueue.datagramSocket.send(p);
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
                    p.setAddress(broadcast);
                    try {
                        sendQueue.datagramSocket.send(p);
                        Thread.sleep(10);
                        sendQueue.datagramSocket.send(p);
                    } catch (IOException e) {
                        logger.warn("Could not send discovery packet! {}", e.getLocalizedMessage());
                    }
                }
            }
        }
    }

    // Search for a specific bridge (our bridge). A response will assign us session bytes.
    // private void send_search_for() {
    // sendQueue.queue(AbstractBulbInterface.CAT_SESSION, search_for_packet());
    // }

    private void send_establish_session() {
        byte unknown = (byte) 0x1E; // TODO: Either checksum or counter. Was 64 and 1e so far.
        byte[] t = { (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x16, (byte) 0x02, (byte) 0x62,
                (byte) 0x3A, (byte) 0xD5, (byte) 0xED, (byte) 0xA3, (byte) 0x01, (byte) 0xAE, (byte) 0x08, (byte) 0x2D,
                (byte) 0x46, (byte) 0x61, (byte) 0x41, (byte) 0xA7, (byte) 0xF6, (byte) 0xDC, (byte) 0xAF, client_sid1,
                client_sid2, (byte) 0x00, (byte) 0x00, unknown };
        sendQueue.queue(QueueItem.createNonRepeatable(AbstractBulbInterface.CAT_SESSION, t));
    }

    // Some apps first send {@see send_establish_session} and with the aquired session bytes they
    // subsequently send this command for establishing the session. This is not well documented unfortunately.
    void send_pre_registration() {
        byte[] t = { 0x30, 0, 0, 0, 3, sid1, sid2, 1, 0 };
        sendQueue.queue(QueueItem.createNonRepeatable(AbstractBulbInterface.CAT_SESSION, t));
    }

    // After the bridges knows our client session bytes and we know the bridge session bytes, we do a final
    // registration with this command. The response will again contain the bridge ID and the session should
    // be established by then.
    void send_registration() {
        byte[] t = { (byte) 0x80, 0x00, 0x00, 0x00, 0x11, sid1, sid2, getNextSequenceNo1(), getNextSequenceNo2(), 0x00,
                0x33, pw1, pw2, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) (0x33 + pw1 + pw2) };
        sendQueue.queue(QueueItem.createNonRepeatable(AbstractBulbInterface.CAT_SESSION, t));
    }

    /**
     * This will send keep alive messages and check for confirmed keep alive messages. If no valid confirmation for
     * a period of time has been received, we reestablish the session.
     *
     * @param periodic_interval_ms How often this method is called in ms. This is used to determine if a session is
     *            still valid.
     * @throws InterruptedException
     */
    public void keep_alive(int periodic_interval_ms) throws InterruptedException {
        if (lastSessionConfirmed != 0 && lastSessionConfirmed + 2 * periodic_interval_ms < System.currentTimeMillis()) {
            sessionState = SessionState.SESSION_INVALID;
            lastSessionConfirmed = 0;
        }
        if (sessionState == SessionState.SESSION_INVALID) {
            session_handshake_process();
        } else if (sessionState == SessionState.SESSION_VALID) {
            byte[] t = { (byte) 0xD0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, sid1, sid2 };
            sendQueue.queue(QueueItem.createNonRepeatable(AbstractBulbInterface.CAT_KEEP_ALIVE, t));
        }
    }

    /**
     * The main state machine of the session handshake. This will start a timer
     * that will reset the handshake if we do not get a satisfying response in time.
     *
     * @throws InterruptedException
     */
    private void session_handshake_process() throws InterruptedException {
        stop_timeout_timer();

        switch (sessionState) {
            case SESSION_INVALID:
                sessionState = SessionState.SESSION_WAIT_FOR_BRIDGE;
                observer.sessionStateChanged(sessionState);
                send_search_for_broadcast();
                break;
            case SESSION_WAIT_FOR_BRIDGE:
                send_search_for_broadcast();
                break;
            case SESSION_WAIT_FOR_BRIDGE_SID:
                observer.sessionStateChanged(sessionState);
                send_establish_session();
                break;
            case SESSION_NEED_REGISTER:
                observer.sessionStateChanged(sessionState);
                send_registration();
                break;
            case SESSION_VALID:
                observer.sessionStateChanged(sessionState);
                // Don't setup a handshake timer
                return;
            default:
                break;
        }

        checkHandshakeTimer = scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    reset_registration_process();
                } catch (InterruptedException ignored) {
                }
            }
        }, REG_TIMEOUT_SEC, TimeUnit.SECONDS);
    }

    private void stop_timeout_timer() {
        if (checkHandshakeTimer != null) {
            checkHandshakeTimer.cancel(false);
            checkHandshakeTimer = null;
        }

    }

    private void reset_registration_process() throws InterruptedException {
        if (sessionState != SessionState.SESSION_WAIT_FOR_BRIDGE) {
            logger.warn("Session registration aborted by timeout timer!");
        }
        // One reason we failed, might be that a last known IP is not correct anymore.
        lastKnownIP = null;
        sessionState = SessionState.SESSION_WAIT_FOR_BRIDGE;
        session_handshake_process();
    }

    private void logUnknownPacket(byte[] data, int len, String reason) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < len; ++i) {
            s.append(String.format("%02X ", data[i]));
        }
        logger.info("{} ({}): {}", reason, bridgeId, s);
    }

    /**
     * The session thread executes this run() method and a blocking UDP receive
     * is performed in a loop.
     */
    @Override
    public void run() {
        try {
            if (DEBUG_SESSION) {
                logger.debug("MilightCommunicationV6 receive thread ready");
            }
            byte[] buffer = new byte[1024];
            DatagramPacket r_packet = new DatagramPacket(buffer, buffer.length);

            session_handshake_process();

            // Now loop forever, waiting to receive packets and printing them.
            while (!willbeclosed) {
                r_packet.setLength(buffer.length);
                sendQueue.getSocket().receive(r_packet);
                int len = r_packet.getLength();

                if (len < 5 || buffer[1] != 0 || buffer[2] != 0 || buffer[3] != 0) {
                    logUnknownPacket(buffer, len, "Not an iBox response!");
                    continue;
                }

                int expected_len = buffer[4] + 5;

                if (expected_len > len) {
                    logUnknownPacket(buffer, len, "Unexpected size!");
                    continue;
                }
                switch (buffer[0]) {
                    // 18 00 00 00 40 02 (AC CF 23 F5 7A D4) 00 20 39 38 35 62 31 35 37 62 66 36 66 63 34 33 33 36 38 61
                    // 36 33 34 36 37 65 61 33 62 31 39 64 30 64 01 00 01 17 63 00 00 05 00 09 78 6C 69 6E 6B 5F 64 65
                    // 76 07 5B CD 15
                    // ASCII string contained: 985b157bf6fc43368a63467ea3b19d0dc .. xlink_dev
                    // Response to the v6 SEARCH and the SEARCH FOR commands to look for new or known devices. A client
                    // session id will be transfered in this process (!= session id)
                    case (byte) 0x18: {
                        boolean eq = ByteBuffer.wrap(BRIDGE_MAC, 0, 6).equals(ByteBuffer.wrap(buffer, 6, 6));
                        if (eq) {
                            if (DEBUG_SESSION) {
                                logger.debug("Session ID reestablished");
                            }
                            if (sessionState == SessionState.SESSION_WAIT_FOR_BRIDGE) {
                                sessionState = SessionState.SESSION_WAIT_FOR_BRIDGE_SID;
                            }
                            sendQueue.setAddress(r_packet.getAddress());
                            session_handshake_process();
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
                        boolean eq = ByteBuffer.wrap(BRIDGE_MAC, 0, 6).equals(ByteBuffer.wrap(buffer, 7, 6));
                        if (eq) {
                            if (DEBUG_SESSION) {
                                logger.debug("Session ID received: {}",
                                        String.format("%02X %02X", buffer[19], buffer[20]));
                            }
                            setSessionID(buffer[19], buffer[20]);
                            if (sessionState == SessionState.SESSION_WAIT_FOR_BRIDGE_SID) {
                                sessionState = SessionState.SESSION_NEED_REGISTER;
                            }
                            session_handshake_process();
                        } else {
                            logger.info("Session ID received, but not for our bridge ({})", bridgeId);
                            logUnknownPacket(buffer, len, "ID not matching");
                        }

                        break;
                    }
                    // 80 00 00 00 15 (AC CF 23 F5 7A D4) 05 02 00 34 00 00 00 00 00 00 00 00 00 00 34
                    // Response to the registration packet
                    case (byte) 0x80: {
                        boolean eq = ByteBuffer.wrap(BRIDGE_MAC, 0, 6).equals(ByteBuffer.wrap(buffer, 5, 6));
                        if (eq) {
                            sessionState = SessionState.SESSION_VALID;
                            session_handshake_process();
                            if (DEBUG_SESSION) {
                                logger.debug("Registration complete");
                            }
                        } else {
                            logger.info("Registration received, but not for our bridge ({})", bridgeId);
                            logUnknownPacket(buffer, len, "ID not matching");
                        }
                        break;
                    }
                    // 88 00 00 00 03 SN SN 00 // two byte sequence number, we use the later one only
                    case (byte) 0x88:
                        used_sequence_no.remove(buffer[6]);
                        if (buffer[07] == 0) {
                            if (DEBUG_SESSION) {
                                logger.debug("Confirmation received for command: {}", String.valueOf(buffer[6]));
                            }
                        } else {
                            logger.info("Bridge reports an invalid command: {}", String.valueOf(buffer[6]));
                        }
                        break;
                    // D8 00 00 00 07 (AC CF 23 F5 7A D4) 01
                    // Response to the keepAlive() packet
                    case (byte) 0xD8: {
                        boolean eq = ByteBuffer.wrap(BRIDGE_MAC, 0, 6).equals(ByteBuffer.wrap(buffer, 5, 6));
                        if (eq) {
                            sessionState = SessionState.SESSION_VALID;
                            lastSessionConfirmed = System.currentTimeMillis();
                            if (DEBUG_SESSION) {
                                logger.debug("Keep alive received");
                            }
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
        } catch (InterruptedException ignored) {
        }
        if (DEBUG_SESSION) {
            logger.debug("MilightCommunicationV6 receive thread ready stopped");
        }
    }

    // Return true if the session is established successfully
    public boolean isValid() {
        return sessionState == SessionState.SESSION_VALID;
    }
}
