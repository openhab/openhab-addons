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
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.milight.MilightBindingConstants;
import org.openhab.binding.milight.internal.protocol.MilightV6SessionManager.SessionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Milight bridges v3/v4/v5 and v6 can be discovered by sending specially formated UDP packets.
 * This class sends UDP packets on port PORT_DISCOVER up to three times in a row
 * and listens for the response and will call discoverResult.bridgeDetected() eventually.
 *
 * The response of the bridges is unfortunately very generic and is the unmodified response of
 * any HF-LPB100 wifi chipset. Therefore other devices as the Orvibo Smart Plugs are recognised
 * as Milight Bridges as well. For v5/v6 there are some additional checks to make sure we are
 * talking to a Milight.
 *
 * @author David Graeff - Initial contribution
 */
public class MilightDiscover extends Thread {
    /**
     * Result callback interface.
     */
    public interface DiscoverResult {
        void bridgeDetected(InetAddress addr, String id, int version);

        void noBridgeDetected();
    }

    ///// Network
    private byte[] discoverBufferV3 = "Link_Wi-Fi".getBytes();
    private byte[] discoverBufferV6 = "HF-A11ASSISTHREAD".getBytes();
    private final DatagramPacket discoverPacketV3;
    private final DatagramPacket discoverPacketV6;
    private boolean willbeclosed = false;
    private DatagramSocket datagramSocket;
    private byte[] buffer = new byte[1024];
    private DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

    ///// Debug
    private Logger logger = LoggerFactory.getLogger(MilightDiscover.class);

    ///// Result and resend
    private final DiscoverResult discoverResult;
    private int resendCounter = 0;
    private ScheduledFuture<?> resendTimer;
    private final int resendTimeoutInMillis;
    private final int resendAttempts;
    private InetAddress destIP;
    private ScheduledExecutorService scheduler;

    public MilightDiscover(DiscoverResult discoverResult, int resendTimeoutInMillis, int resendAttempts)
            throws SocketException {
        this.resendAttempts = resendAttempts;
        this.resendTimeoutInMillis = resendTimeoutInMillis;
        discoverPacketV3 = new DatagramPacket(discoverBufferV3, discoverBufferV3.length);
        discoverPacketV6 = new DatagramPacket(discoverBufferV6, discoverBufferV6.length);
        datagramSocket = new DatagramSocket(null);
        datagramSocket.setBroadcast(true);
        datagramSocket.bind(null);
        this.discoverResult = discoverResult;
    }

    /**
     * Closes the socket and waits for the thread to shutdown.
     * You cannot reuse this object after calling release.
     */
    public void release() {
        if (datagramSocket == null) {
            return;
        }
        stopResend();
        willbeclosed = true;
        datagramSocket.close();
        if (Thread.currentThread() != this) {
            try {
                join(500);
            } catch (InterruptedException e) {
            }
            interrupt();
        }
    }

    /**
     * Used by the scheduler to resend discover messages. Stops after a configured amount of attempts.
     */
    private class SendDiscoverRunnable implements Runnable {
        @Override
        public void run() {
            // Stop after a certain amount of attempts
            if (++resendCounter > resendAttempts) {
                stopResend();
                // If we tried to discover a specific bridge, we apparently failed. Report this to the observer.
                if (destIP != null) {
                    discoverResult.noBridgeDetected();
                }
                return;
            }

            if (destIP != null) {
                sendDiscover(destIP);
                return;
            }

            Enumeration<NetworkInterface> e;
            try {
                e = NetworkInterface.getNetworkInterfaces();
            } catch (SocketException e1) {
                logger.error("Could not enumerate network interfaces for sending the discover packet!");
                stopResend();
                return;
            }
            while (e.hasMoreElements()) {
                NetworkInterface networkInterface = e.nextElement();
                for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = address.getBroadcast();
                    if (broadcast != null && !address.getAddress().isLoopbackAddress()) {
                        sendDiscover(broadcast);
                    }
                }
            }
        }

        private void sendDiscover(InetAddress destIP) {
            discoverPacketV3.setAddress(destIP);
            discoverPacketV3.setPort(MilightBindingConstants.PORT_DISCOVER);
            discoverPacketV6.setAddress(destIP);
            discoverPacketV6.setPort(MilightBindingConstants.PORT_DISCOVER);

            try {
                datagramSocket.send(discoverPacketV3);
            } catch (IOException e) {
                logger.error("Sending a V3 discovery packet to {} failed. {}", destIP.getHostAddress(),
                        e.getLocalizedMessage());
            }

            try {
                datagramSocket.send(discoverPacketV6);
            } catch (IOException e) {
                logger.error("Sending a V6 discovery packet to {} failed. {}", destIP.getHostAddress(),
                        e.getLocalizedMessage());
            }
        }
    }

    /**
     * This will not stop the discovery thread (like dispose()), so discovery
     * packet responses can still be received, but will stop
     * re-sending discovery packets. Call sendDiscover() to restart sending
     * discovery packets.
     */
    public void stopResend() {
        if (resendTimer != null) {
            resendTimer.cancel(false);
            resendTimer = null;
        }
    }

    /**
     * Send a discover message and resends the message until either a valid response
     * is received or the resend counter reaches the maximum attempts.
     *
     * @param scheduler The scheduler is used for resending.
     */
    public void sendDiscover(ScheduledExecutorService scheduler) {
        // Do nothing if there is already a discovery running
        if (resendTimer != null) {
            return;
        }

        resendCounter = 0;
        this.scheduler = scheduler;
        resendTimer = scheduler.scheduleWithFixedDelay(new SendDiscoverRunnable(), 0, resendTimeoutInMillis,
                TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        try {
            while (!willbeclosed) {
                packet.setLength(buffer.length);
                datagramSocket.receive(packet);
                // We expect packets with a format like this: 10.1.1.27,ACCF23F57AD4,HF-LPB100
                String[] msg = new String(buffer, 0, packet.getLength()).split(",");

                if (msg.length != 2 && msg.length != 3) {
                    // That data packet does not belong to a Milight bridge. Just ignore it.
                    continue;
                }

                int version = 3; // Assume version 3
                // First argument is the IP
                try {
                    InetAddress.getByName(msg[0]);
                } catch (UnknownHostException ignored) {
                    // That data packet does not belong to a Milight bridge, we expect an IP address as first argument.
                    // Just ignore it.
                    continue;
                }

                // Second argument is the MAC address
                if (msg[1].length() != 12) {
                    // That data packet does not belong to a Milight bridge, we expect a MAC address as second argument.
                    // Just ignore it.
                    continue;
                }

                InetAddress addressOfBridge = ((InetSocketAddress) packet.getSocketAddress()).getAddress();
                if (msg.length == 3) {
                    version = 6; // It is probably version 6
                    if (!(msg[2].length() == 0 || "HF-LPB100".equals(msg[2]))) {
                        logger.trace("Unexpected data. We expected a HF-LPB100 or empty identifier {}", msg[2]);
                        continue;
                    }
                    if (!checkForV6Bridge(addressOfBridge, msg[1])) {
                        logger.trace("The device at IP {} does not seem to be a V6 Milight bridge", msg[0]);
                        continue;
                    }
                }

                stopResend();
                discoverResult.bridgeDetected(addressOfBridge, msg[1], version);
            }
        } catch (IOException e) {
            if (willbeclosed) {
                return;
            }
            logger.warn("{}", e.getLocalizedMessage());
        } catch (InterruptedException ignore) {
            // Ignore this exception, the thread is finished now anyway
        }
    }

    /**
     * We use the {@see MilightV6SessionManager} to establish a full session to the bridge. If we reach
     * the SESSION_VALID state within 1.3s, we can safely assume it is a V6 Milight bridge.
     *
     * @param addressOfBridge IP Address of the bridge
     * @return
     * @throws InterruptedException If waiting for the session is interrupted we throw this exception
     */
    private boolean checkForV6Bridge(InetAddress addressOfBridge, String bridgeID) throws InterruptedException {
        QueuedSend queuedSend;
        try {
            queuedSend = new QueuedSend();
            Semaphore s = new Semaphore(0);
            MilightV6SessionManager session = new MilightV6SessionManager(queuedSend, bridgeID, scheduler,
                    (SessionState state) -> {
                        if (state == SessionState.SESSION_VALID) {
                            s.release();
                        }
                    }, null);
            boolean success = s.tryAcquire(1, 1300, TimeUnit.MILLISECONDS);
            session.dispose();
            queuedSend.dispose();
            return success;
        } catch (SocketException e) {
            logger.debug("Could not create a udp socket", e);
        }
        return false;
    }

    /**
     * Perform a discovery on a fixed IP address
     *
     * @param addr The IP address
     */
    public void setFixedAddr(InetAddress addr) {
        destIP = addr;
    }
}
