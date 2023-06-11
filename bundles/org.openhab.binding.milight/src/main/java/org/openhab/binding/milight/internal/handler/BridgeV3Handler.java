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
package org.openhab.binding.milight.internal.handler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.milight.internal.MilightBindingConstants;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;

/**
 * The {@link BridgeV3Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class BridgeV3Handler extends AbstractBridgeHandler {
    protected final DatagramPacket discoverPacketV3;
    protected final byte[] buffer = new byte[1024];
    protected final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
    private @Nullable ScheduledFuture<?> running;

    public BridgeV3Handler(Bridge bridge, int bridgeOffset) {
        super(bridge, bridgeOffset);
        discoverPacketV3 = new DatagramPacket(MilightBindingConstants.DISCOVER_MSG_V3,
                MilightBindingConstants.DISCOVER_MSG_V3.length);
    }

    @Override
    public void thingUpdated(Thing thing) {
        super.thingUpdated(thing);
    }

    /**
     * Creates a discovery object and the send queue. The initial IP address may be null
     * or is not matching with the real IP address of the bridge. The discovery class will send
     * a broadcast packet to find the bridge with the respective bridge ID. The response in bridgeDetected()
     * may lead to a recreation of the send queue object.
     *
     * The keep alive timer that is also setup here, will send keep alive packets periodically.
     * If the bridge doesn't respond anymore (e.g. DHCP IP change), the initial session handshake
     * starts all over again.
     */
    @Override
    protected void startConnectAndKeepAlive() {
        if (address == null) {
            if (!config.bridgeid.matches("^([0-9A-Fa-f]{12})$")) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "bridgeID invalid!");
                return;
            }
            try {
                address = InetAddress.getByAddress(new byte[] { (byte) 255, (byte) 255, (byte) 255, (byte) 255 });
            } catch (UnknownHostException neverHappens) {
            }
        }

        if (config.port == 0) {
            config.port = MilightBindingConstants.PORT_VER3;
        }

        try {
            socket = new DatagramSocket(null);
            socket.setReuseAddress(true);
            socket.setBroadcast(true);
            socket.bind(null);
        } catch (SocketException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }

        running = scheduler.scheduleWithFixedDelay(this::receive, 0, config.refreshTime, TimeUnit.SECONDS);
    }

    protected void stopKeepAlive() {
        if (running != null) {
            running.cancel(false);
            running = null;
        }
        if (socket != null) {
            socket.close();
        }
    }

    @Override
    public void dispose() {
        stopKeepAlive();
    }

    private void receive() {
        try {
            discoverPacketV3.setAddress(address);
            discoverPacketV3.setPort(MilightBindingConstants.PORT_DISCOVER);

            final int attempts = 5;
            int timeoutsCounter = 0;
            for (timeoutsCounter = 1; timeoutsCounter <= attempts; ++timeoutsCounter) {
                try {
                    packet.setLength(buffer.length);
                    socket.setSoTimeout(500 * timeoutsCounter);
                    socket.send(discoverPacketV3);
                    socket.receive(packet);
                } catch (SocketTimeoutException e) {
                    continue;
                }
                // We expect packets with a format like this: 10.1.1.27,ACCF23F57AD4,HF-LPB100
                final String received = new String(packet.getData());
                final String[] msg = received.split(",");

                if (msg.length != 2 && msg.length != 3) {
                    // That data packet does not belong to a Milight bridge. Just ignore it.
                    continue;
                }

                // First argument is the IP
                try {
                    InetAddress.getByName(msg[0]);
                } catch (UnknownHostException ignored) {
                    // That data packet does not belong to a Milight bridge, we expect an IP address as first
                    // argument. Just ignore it.
                    continue;
                }

                // Second argument is the MAC address
                if (msg[1].length() != 12) {
                    // That data packet does not belong to a Milight bridge, we expect a MAC address as second
                    // argument.
                    // Just ignore it.
                    continue;
                }

                final InetAddress addressOfBridge = ((InetSocketAddress) packet.getSocketAddress()).getAddress();
                final String bridgeID = msg[1];

                if (!config.bridgeid.isEmpty() && !bridgeID.equals(config.bridgeid)) {
                    // We found a bridge, but it is not the one that is handled by this handler
                    if (!config.host.isEmpty()) { // The user has set a host address -> but wrong bridge found!
                        stopKeepAlive();
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                "Wrong bridge found on host address. Change bridgeid or host configuration.");
                        break;
                    }
                    continue;
                }

                // IP address has changed, reestablish communication
                if (!addressOfBridge.equals(this.address)) {
                    this.address = addressOfBridge;
                    Configuration c = editConfiguration();
                    c.put(BridgeHandlerConfig.CONFIG_HOST_NAME, addressOfBridge.getHostAddress());
                    preventReinit = true;
                    updateConfiguration(c);
                    preventReinit = false;
                } else if (config.bridgeid.isEmpty()) { // bridge id was not set and is now known. Store it.
                    config.bridgeid = bridgeID;
                    Configuration c = editConfiguration();
                    c.put(BridgeHandlerConfig.CONFIG_BRIDGE_ID, bridgeID);
                    preventReinit = true;
                    updateConfiguration(c);
                    preventReinit = false;
                }

                updateStatus(ThingStatus.ONLINE);
                break;
            }
            if (timeoutsCounter > attempts) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Bridge did not respond!");
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }
}
