/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.milight.internal.MilightBindingConstants;

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
    private boolean running = false;

    public BridgeV3Handler(Bridge bridge) {
        super(bridge);
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
        if (running) {
            return;
        }
        if (port == 0) {
            port = MilightBindingConstants.PORT_VER3;
        }

        try {
            socket = new DatagramSocket(null);
            socket.setReuseAddress(true);
            socket.setBroadcast(true);
            socket.bind(new InetSocketAddress(MilightBindingConstants.PORT_DISCOVER));
        } catch (SocketException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }

        new Thread(this::receive).start();
        running = true;
    }

    protected void stopKeepAlive() {
        running = false;
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

            discoverPacketV3.setAddress(
                    InetAddress.getByAddress(new byte[] { (byte) 255, (byte) 255, (byte) 255, (byte) 255 }));
            discoverPacketV3.setPort(MilightBindingConstants.PORT_DISCOVER);
            socket.setSoTimeout(100);

            final int ATTEMPTS = 3;
            int timeoutsCounter = 0;
            while (running) {
                try {
                    packet.setLength(buffer.length);
                    socket.receive(packet);
                } catch (SocketTimeoutException e) {
                    if (timeoutsCounter >= ATTEMPTS) {
                        socket.setSoTimeout(config.refreshTime * 1000);
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                                "Bridge did not respond or the bridge's MAC address does not match with your configuration!");
                        timeoutsCounter = 0;
                    } else {
                        socket.setSoTimeout(300);
                        ++timeoutsCounter;
                        socket.send(discoverPacketV3);
                    }
                    continue;
                } catch (IOException e) {
                    // Socket closed -> Time to leave this thread
                    if (running) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                    }
                    break;
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

                if (!config.bridgeid.equals(bridgeID)) {
                    // We found a bridge, but it is not the one that is handled by this handler
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
                }

                timeoutsCounter = 0;
                updateStatus(ThingStatus.ONLINE);
                socket.setSoTimeout(config.refreshTime * 1000);
            }
        } catch (IOException e) {
            stopKeepAlive();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }

        this.socket = null;
        running = false;
    }
}
