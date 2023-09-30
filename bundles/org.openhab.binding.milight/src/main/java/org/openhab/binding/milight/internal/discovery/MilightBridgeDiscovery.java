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
package org.openhab.binding.milight.internal.discovery;

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
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.milight.internal.MilightBindingConstants;
import org.openhab.binding.milight.internal.handler.BridgeHandlerConfig;
import org.openhab.binding.milight.internal.protocol.MilightV6SessionManager;
import org.openhab.binding.milight.internal.protocol.MilightV6SessionManager.ISessionState;
import org.openhab.binding.milight.internal.protocol.MilightV6SessionManager.SessionState;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
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
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.milight")
public class MilightBridgeDiscovery extends AbstractDiscoveryService implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(MilightBridgeDiscovery.class);

    ///// Static configuration
    private static final boolean ENABLE_V3 = true;
    private static final boolean ENABLE_V6 = true;

    private @Nullable ScheduledFuture<?> backgroundFuture;

    ///// Network
    private final int receivePort;
    private final DatagramPacket discoverPacketV3;
    private final DatagramPacket discoverPacketV6;
    private boolean willbeclosed = false;
    @NonNullByDefault({})
    private DatagramSocket datagramSocket;
    private final byte[] buffer = new byte[1024];
    private final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

    ///// Result and resend
    private int resendCounter = 0;
    private @Nullable ScheduledFuture<?> resendTimer;
    private final int resendTimeoutInMillis;
    private final int resendAttempts;

    public MilightBridgeDiscovery() throws IllegalArgumentException, UnknownHostException {
        super(MilightBindingConstants.BRIDGE_THING_TYPES_UIDS, 2, true);
        this.resendAttempts = 2000 / 200;
        this.resendTimeoutInMillis = 200;
        this.receivePort = MilightBindingConstants.PORT_DISCOVER;
        discoverPacketV3 = new DatagramPacket(MilightBindingConstants.DISCOVER_MSG_V3,
                MilightBindingConstants.DISCOVER_MSG_V3.length);
        discoverPacketV6 = new DatagramPacket(MilightBindingConstants.DISCOVER_MSG_V6,
                MilightBindingConstants.DISCOVER_MSG_V6.length);

        startDiscoveryService();
    }

    @Override
    protected void startBackgroundDiscovery() {
        if (backgroundFuture != null) {
            return;
        }

        backgroundFuture = scheduler.scheduleWithFixedDelay(this::startDiscoveryService, 50, 60000 * 30,
                TimeUnit.MILLISECONDS);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        stopScan();
        final ScheduledFuture<?> future = backgroundFuture;
        if (future != null) {
            future.cancel(false);
            this.backgroundFuture = null;
        }
        stop();
    }

    public void bridgeDetected(InetAddress addr, String id, int version) {
        ThingUID thingUID = new ThingUID(version == 6 ? MilightBindingConstants.BRIDGEV6_THING_TYPE
                : MilightBindingConstants.BRIDGEV3_THING_TYPE, id);

        Map<String, Object> properties = new TreeMap<>();
        properties.put(BridgeHandlerConfig.CONFIG_BRIDGE_ID, id);
        properties.put(BridgeHandlerConfig.CONFIG_HOST_NAME, addr.getHostAddress());

        String label = "Bridge " + id;

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withLabel(label)
                .withProperties(properties).build();
        thingDiscovered(discoveryResult);
    }

    @Override
    protected void startScan() {
        startDiscoveryService();
    }

    @Override
    protected synchronized void stopScan() {
        stop();
        super.stopScan();
    }

    /**
     * Used by the scheduler to resend discover messages. Stops after a configured amount of attempts.
     */
    private class SendDiscoverRunnable implements Runnable {
        @Override
        public void run() {
            // Stop after a certain amount of attempts
            if (++resendCounter > resendAttempts) {
                stop();
                return;
            }

            Enumeration<NetworkInterface> e;
            try {
                e = NetworkInterface.getNetworkInterfaces();
            } catch (SocketException e1) {
                logger.error("Could not enumerate network interfaces for sending the discover packet!");
                stop();
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
            if (ENABLE_V3) {
                discoverPacketV3.setAddress(destIP);
                discoverPacketV3.setPort(MilightBindingConstants.PORT_DISCOVER);
                try {
                    datagramSocket.send(discoverPacketV3);
                } catch (IOException e) {
                    logger.error("Sending a V3 discovery packet to {} failed. {}", destIP.getHostAddress(),
                            e.getLocalizedMessage());
                }
            }

            if (ENABLE_V6) {
                discoverPacketV6.setAddress(destIP);
                discoverPacketV6.setPort(MilightBindingConstants.PORT_DISCOVER);
                try {
                    datagramSocket.send(discoverPacketV6);
                } catch (IOException e) {
                    logger.error("Sending a V6 discovery packet to {} failed. {}", destIP.getHostAddress(),
                            e.getLocalizedMessage());
                }
            }
        }
    }

    /**
     * This will not stop the discovery thread (like dispose()), so discovery
     * packet responses can still be received, but will stop
     * re-sending discovery packets. Call sendDiscover() to restart sending
     * discovery packets.
     */
    public void stop() {
        if (resendTimer != null) {
            resendTimer.cancel(false);
            resendTimer = null;
        }

        if (willbeclosed) {
            return;
        }
        willbeclosed = true;
        datagramSocket.close();
    }

    /**
     * Send a discover message and resends the message until either a valid response
     * is received or the resend counter reaches the maximum attempts.
     */
    public void startDiscoveryService() {
        // Do nothing if there is already a discovery running
        if (resendTimer != null) {
            return;
        }

        willbeclosed = false;
        try {
            datagramSocket = new DatagramSocket(null);
            datagramSocket.setBroadcast(true);
            datagramSocket.setReuseAddress(true);
            datagramSocket.bind(null);
        } catch (SocketException e) {
            logger.error("Opening a socket for the milight discovery service failed. {}", e.getLocalizedMessage());
            return;
        }
        resendCounter = 0;
        resendTimer = scheduler.scheduleWithFixedDelay(new SendDiscoverRunnable(), 0, resendTimeoutInMillis,
                TimeUnit.MILLISECONDS);
        scheduler.execute(this);
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
                if (ENABLE_V6 && msg.length == 3) {
                    if (!(msg[2].length() == 0 || "HF-LPB100".equals(msg[2]))) {
                        logger.trace("Unexpected data. We expected a HF-LPB100 or empty identifier {}", msg[2]);
                        continue;
                    }
                    if (!checkForV6Bridge(addressOfBridge, msg[1])) {
                        logger.trace("The device at IP {} does not seem to be a V6 Milight bridge", msg[0]);
                        continue;
                    }
                    bridgeDetected(addressOfBridge, msg[1], 6);
                } else if (ENABLE_V3 && msg.length == 2) {
                    bridgeDetected(addressOfBridge, msg[1], 3);
                } else {
                    logger.debug("Unexpected data. Expected Milight bridge message");
                }
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
        Semaphore s = new Semaphore(0);
        ISessionState sessionState = (SessionState state, InetAddress address) -> {
            if (state == SessionState.SESSION_VALID) {
                s.release();
            }
            logger.debug("STATE CHANGE: {}", state);
        };

        try (MilightV6SessionManager session = new MilightV6SessionManager(bridgeID, sessionState, addressOfBridge,
                MilightBindingConstants.PORT_VER6, MilightV6SessionManager.TIMEOUT_MS, new byte[] { 0, 0 })) {
            session.start();
            return s.tryAcquire(1, 1300, TimeUnit.MILLISECONDS);
        } catch (IOException e) {
            logger.debug("checkForV6Bridge failed", e);
        }
        return false;
    }
}
