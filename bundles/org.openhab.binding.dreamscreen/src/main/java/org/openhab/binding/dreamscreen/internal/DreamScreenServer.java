/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.dreamscreen.internal;

import static org.openhab.binding.dreamscreen.internal.DreamScreenBindingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.net.CidrAddress;
import org.eclipse.smarthome.core.net.NetworkAddressChangeListener;
import org.eclipse.smarthome.core.net.NetworkAddressService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.dreamscreen.internal.handler.DreamScreen4kHandler;
import org.openhab.binding.dreamscreen.internal.handler.DreamScreenBaseHandler;
import org.openhab.binding.dreamscreen.internal.handler.DreamScreenHdHandler;
import org.openhab.binding.dreamscreen.internal.handler.DreamScreenSidekickHandler;
import org.openhab.binding.dreamscreen.internal.message.DreamScreenMessage;
import org.openhab.binding.dreamscreen.internal.message.DreamScreenMessageInvalid;
import org.openhab.binding.dreamscreen.internal.message.RefreshMessage;
import org.openhab.binding.dreamscreen.internal.message.ScanMessage;
import org.openhab.binding.dreamscreen.internal.message.SerialNumberMessage;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DreamScreenServer} class handles all communications with the DreamScreen devices.
 *
 * @author Bruce Brouwer - Initial contribution
 */
@NonNullByDefault
@Component(service = { DreamScreenServer.class,
        DiscoveryService.class }, immediate = true, configurationPid = "discovery.dreamscreen")
public class DreamScreenServer extends AbstractDiscoveryService implements NetworkAddressChangeListener {
    private final static int DREAMSCREEN_PORT = 8888;
    private static final int DISCOVER_TIMEOUT_SECONDS = 10;

    private final Logger logger = LoggerFactory.getLogger(DreamScreenServer.class);
    private final Set<DreamScreenBaseHandler> handlers = Collections.synchronizedSet(new HashSet<>());
    private final Map<InetAddress, Integer> devices = new ConcurrentHashMap<>();

    private String thingUID = BINDING_ID;
    private @Nullable NetworkAddressService network;
    private @Nullable InetAddress hostAddress;
    private @Nullable InetAddress broadcastAddress;
    private @Nullable Thread server;
    private @Nullable DatagramSocket socket;
    private long scanning = 0;

    public DreamScreenServer() {
        super(SUPPORTED_THING_TYPES_UIDS, DISCOVER_TIMEOUT_SECONDS, true);
    }

    private void doScan() throws IOException {
        this.devices.clear();
        read(new ScanMessage());
    }

    void read(final DreamScreenMessage msg) throws IOException {
        final InetAddress address = this.broadcastAddress;
        if (address != null) {
            send(msg.broadcastReadPacket(address, DREAMSCREEN_PORT));
        } else {
            logger.debug("No broadcast address configured");
        }
    }

    public void read(final DreamScreenMessage msg, final InetAddress address) throws IOException {
        logger.debug("Sending {} to {}", msg, address);
        send(msg.readPacket(address, DREAMSCREEN_PORT));
    }

    public void write(final DreamScreenMessage msg, final InetAddress address) throws IOException {
        logger.debug("Sending {} to {}", msg, address);
        send(msg.writePacket(address, DREAMSCREEN_PORT));
    }

    private void send(final DatagramPacket packet) throws IOException {
        startServer().send(packet);
    }

    private boolean message(final DreamScreenMessage msg, final InetAddress address) {
        for (final DreamScreenBaseHandler handler : this.handlers) {
            if (handler.message(msg, address)) {
                return true;
            }
        }
        return false;
    }

    public boolean unlinkedMsg(final DreamScreenMessage msg, final InetAddress address) throws IOException {
        if (msg instanceof SerialNumberMessage) {
            processSerialNumber((SerialNumberMessage) msg, address);
        } else if (msg instanceof RefreshMessage) {
            processRefresh((RefreshMessage) msg, address);
        }
        return false;
    }

    private void processSerialNumber(final SerialNumberMessage msg, final InetAddress address) throws IOException {
        this.devices.put(address, msg.getSerialNumber());
        if (this.scanning > System.currentTimeMillis() - DISCOVER_TIMEOUT_SECONDS * 1000) {
            write(new RefreshMessage(), address);
        }
    }

    private void processRefresh(final RefreshMessage msg, final InetAddress address) {
        if (this.devices.containsKey(address)) {
            final int serialNumber = this.devices.get(address);
            discovered(msg, serialNumber);
        }
    }

    private void discovered(final RefreshMessage msg, final int serialNumber) {
        final ThingTypeUID uid;
        logger.debug("Found DreamScreen {} named {}", serialNumber, msg.getName());
        switch (msg.getProductId()) {
            case DreamScreenHdHandler.PRODUCT_ID:
                uid = THING_TYPE_HD;
                break;
            case DreamScreen4kHandler.PRODUCT_ID:
                uid = THING_TYPE_4K;
                break;
            case DreamScreenSidekickHandler.PRODUCT_ID:
                uid = THING_TYPE_SIDEKICK;
                break;
            default:
                uid = null;
                break;
        }
        if (uid != null) {
            final String serialNumStr = Integer.toString(serialNumber);
            final ThingUID thingUID = new ThingUID(uid, serialNumStr);
            thingDiscovered(DiscoveryResultBuilder.create(thingUID).withLabel(msg.getName())
                    .withProperty(DreamScreenConfiguration.SERIAL_NUMBER, serialNumStr)
                    .withRepresentationProperty(DreamScreenConfiguration.SERIAL_NUMBER).build());
        }
    }

    private DatagramSocket startServer() throws IOException {
        DatagramSocket socket = this.socket;
        Thread server = this.server;
        if (socket == null || socket.isClosed() || server == null) {
            socket = new DatagramSocket(DREAMSCREEN_PORT, hostAddress);
            socket.setBroadcast(true);
            socket.setReuseAddress(true);
            this.socket = socket;

            server = new Thread(this::runServer, "OH-binding-" + thingUID);
            server.setDaemon(true);
            server.start();
            this.server = server;

            doScan();
        }
        return socket;
    }

    private void runServer() {
        final byte[] data = new byte[256];
        final DatagramSocket socket = this.socket;

        while (socket != null && !socket.isClosed()) {
            try {
                final DatagramPacket packet = new DatagramPacket(data, data.length);
                socket.receive(packet);

                final InetAddress address = packet.getAddress();

                if (!address.equals(this.hostAddress)) {
                    final DreamScreenMessage msg = DreamScreenMessage.fromPacket(packet);
                    logger.debug("Received {} from {}", msg, address);

                    if (!message(msg, address)) {
                        unlinkedMsg(msg, address);
                    }
                }
            } catch (DreamScreenMessageInvalid dsmi) {
                logger.trace("Message received is not a DreamScreen message", dsmi);
            } catch (IOException ioe) {
                logger.debug("Error communicating with DreamScreen devices", ioe);
            }
        }
    }

    private void stopServer() {
        final DatagramSocket socket = this.socket;
        final Thread server = this.server;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        if (server != null) {
            try {
                server.join(5000);
            } catch (InterruptedException e) {
                logger.debug("Failed to wait for server to stop", e);
            }
        }
    }

    @Override
    protected void startScan() {
        logger.debug("Starting DreamScreen discovery scan");
        try {
            this.scanning = System.currentTimeMillis();
            doScan();
        } catch (IOException e) {
            logger.debug("Error scanning for DreamScreen devices", e);
        }
    }

    public void addHandler(final DreamScreenBaseHandler handler) {
        this.thingUID = handler.getThing().getThingTypeUID().toString();
        this.handlers.add(handler);
        try {
            startServer();
        } catch (IOException e) {
            logger.debug("Error starting DreamScreen server", e);
        }
        for (final Entry<InetAddress, Integer> entry : this.devices.entrySet()) {
            if (handler.link(entry.getValue(), entry.getKey())) {
                break;
            }
        }
    }

    public void removeHandler(final DreamScreenBaseHandler handler) {
        this.handlers.remove(handler);
        if (this.handlers.isEmpty()) {
            stopServer();
        }
    }

    @Reference
    public void bindNetworkAddressService(final NetworkAddressService network) {
        this.network = network;
        network.addNetworkAddressChangeListener(this);
        configureNetwork();
    }

    public void unbindNetworkAddressService(final NetworkAddressService network) {
        network.removeNetworkAddressChangeListener(this);
        this.network = null;
    }

    @Override
    public void onChanged(List<CidrAddress> added, List<CidrAddress> removed) {
        configureNetwork();
    }

    @Override
    public void onPrimaryAddressChanged(@Nullable String oldPrimaryAddress, @Nullable String newPrimaryAddress) {
        configureNetwork();
    }

    private void configureNetwork() {
        final NetworkAddressService networkAddressService = this.network;
        final InetAddress oldHostAddress = this.hostAddress;

        if (networkAddressService != null) {
            try {
                final String host = networkAddressService.getPrimaryIpv4HostAddress();
                final InetAddress newHostAddress = host == null ? null : InetAddress.getByName(host);
                this.hostAddress = newHostAddress;

                final String broadcast = networkAddressService.getConfiguredBroadcastAddress();
                this.broadcastAddress = broadcast == null ? null : InetAddress.getByName(broadcast);

                if (newHostAddress == null) {
                    stopServer();
                } else if (!newHostAddress.equals(oldHostAddress)) {
                    stopServer();
                    if (!this.handlers.isEmpty()) {
                        startServer();
                    }
                }
            } catch (IOException e) {
                logger.debug("Unable to configure network", e);
            }
        }
    }

    @Deactivate
    public void dispose() {
        this.handlers.clear();
        final NetworkAddressService network = this.network;
        if (network != null) {
            network.removeNetworkAddressChangeListener(this);
        }
        stopServer();
    }
}
