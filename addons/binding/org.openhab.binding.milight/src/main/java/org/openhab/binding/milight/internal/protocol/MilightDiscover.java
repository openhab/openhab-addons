/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
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
import java.net.SocketException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.milight.MilightBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Milight bridges can be discovered by sending specially formated UDP packets.
 * This class sends UDP packets on port PORT_SEND_DISCOVER up to three times in a row
 * and listens for the response and will call discoverResult.bridgeDetected() eventually.
 *
 * @author David Graeff - Initial contribution
 */
public class MilightDiscover extends Thread {
    /**
     * Result callback interface.
     */
    public interface DiscoverResult {
        void bridgeDetected(InetAddress addr, String id);

        void noBridgeDetected();
    }

    ///// Network
    private byte[] discoverbuffer = "Link_Wi-Fi".getBytes();
    final private DatagramPacket discoverPacket;
    private boolean willbeclosed = false;
    private DatagramSocket datagramSocket;
    private byte[] buffer = new byte[1024];
    private DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

    ///// Debug
    private Logger logger = LoggerFactory.getLogger(MilightDiscover.class);

    ///// Result and resend
    final private DiscoverResult discoverResult;
    private int resendCounter = 0;
    private ScheduledFuture<?> resendTimer;
    final private int resendTimeoutInMillis;
    final private int resendAttempts;

    public MilightDiscover(InetAddress broadcast, DiscoverResult discoverResult, int resendTimeoutInMillis,
            int resendAttempts) throws SocketException {
        this.resendAttempts = resendAttempts;
        this.resendTimeoutInMillis = resendTimeoutInMillis;
        discoverPacket = new DatagramPacket(discoverbuffer, discoverbuffer.length, broadcast,
                MilightBindingConstants.PORT_SEND_DISCOVER);
        datagramSocket = new DatagramSocket(null);
        datagramSocket.setBroadcast(true);
        datagramSocket.bind(null);
        this.discoverResult = discoverResult;
    }

    public void stopReceiving() {
        willbeclosed = true;
        datagramSocket.close();
        try {
            join(500);
        } catch (InterruptedException e) {
        }
        interrupt();
        if (datagramSocket != null) {
            datagramSocket.close();
            datagramSocket = null;
        }
    }

    /**
     * Used by the scheduler to resend discover messages. Stops after 3 attempts.
     */
    private class SendDiscoverRunnable implements Runnable {
        @Override
        public void run() {
            try {
                if (++resendCounter > resendAttempts) {
                    if (resendTimer != null) {
                        resendTimer.cancel(false);
                        resendTimer = null;
                    }
                    discoverResult.noBridgeDetected();
                    return;
                }
                datagramSocket.send(discoverPacket);
                logger.debug("Sent discovery packet");
            } catch (Exception e) {
                logger.error("Sending a discovery packet failed. " + e.getLocalizedMessage());
            }
        }
    }

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
        resendTimer = scheduler.scheduleWithFixedDelay(new SendDiscoverRunnable(), 0, resendTimeoutInMillis,
                TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        try {
            logger.debug("Discovery receive thread ready");

            // Now loop forever, waiting to receive packets and printing them.
            while (!willbeclosed) {
                datagramSocket.receive(packet);
                String[] msg = new String(buffer).split(",");
                if (msg.length >= 2 && msg[1].length() == 12) {
                    // Stop resend timer if we got a packet.
                    if (resendTimer != null) {
                        resendTimer.cancel(true);
                        resendTimer = null;
                    }
                    discoverResult.bridgeDetected(((InetSocketAddress) packet.getSocketAddress()).getAddress(), msg[1]);
                } else {
                    logger.error("Unexpected data received " + msg[0]);
                }

                // Reset the length of the packet before reusing it.
                packet.setLength(buffer.length);
            }
        } catch (IOException e) {
            if (willbeclosed) {
                return;
            }
            logger.error(e.getLocalizedMessage());
        }
    }
}
