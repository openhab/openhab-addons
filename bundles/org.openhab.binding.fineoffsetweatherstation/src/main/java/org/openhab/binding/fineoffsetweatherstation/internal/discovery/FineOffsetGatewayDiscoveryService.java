/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.fineoffsetweatherstation.internal.discovery;

import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.THING_TYPE_GATEWAY;
import static org.openhab.binding.fineoffsetweatherstation.internal.Utils.toUInt16;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetGatewayConfiguration;
import org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetSensorConfiguration;
import org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants;
import org.openhab.binding.fineoffsetweatherstation.internal.Utils;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.Command;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.ConversionContext;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.Protocol;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.MeasuredValue;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.SensorDevice;
import org.openhab.binding.fineoffsetweatherstation.internal.service.GatewayQueryService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.net.NetUtil;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
@Component(service = { DiscoveryService.class, FineOffsetGatewayDiscoveryService.class }, immediate = true)
public class FineOffsetGatewayDiscoveryService extends AbstractDiscoveryService {
    private static final int DISCOVERY_PORT = 46000;
    private static final int BUFFER_LENGTH = 255;

    private final Logger logger = LoggerFactory.getLogger(FineOffsetGatewayDiscoveryService.class);

    private static final long REFRESH_INTERVAL = 600;
    private static final int DISCOVERY_TIME = 10;
    private final TranslationProvider translationProvider;
    private final LocaleProvider localeProvider;
    private final @Nullable Bundle bundle;
    private @Nullable DatagramSocket clientSocket;
    private @Nullable Thread socketReceiveThread;
    private @Nullable ScheduledFuture<?> discoveryJob;

    @Activate
    public FineOffsetGatewayDiscoveryService(@Reference TranslationProvider translationProvider,
            @Reference LocaleProvider localeProvider) throws IllegalArgumentException {
        super(Collections.singleton(THING_TYPE_GATEWAY), DISCOVERY_TIME, true);
        this.translationProvider = translationProvider;
        this.localeProvider = localeProvider;
        this.bundle = FrameworkUtil.getBundle(FineOffsetGatewayDiscoveryService.class);
    }

    @Override
    protected void startBackgroundDiscovery() {
        final @Nullable ScheduledFuture<?> discoveryJob = this.discoveryJob;
        if (discoveryJob == null || discoveryJob.isCancelled()) {
            this.discoveryJob = scheduler.scheduleWithFixedDelay(this::discover, 0, REFRESH_INTERVAL, TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        final @Nullable ScheduledFuture<?> discoveryJob = this.discoveryJob;
        if (discoveryJob != null) {
            discoveryJob.cancel(true);
            this.discoveryJob = null;
        }
    }

    @Override
    public void deactivate() {
        stopReceiverThreat();
        final DatagramSocket clientSocket = this.clientSocket;
        if (clientSocket != null) {
            clientSocket.close();
        }
        this.clientSocket = null;
        super.deactivate();
    }

    @Override
    protected void startScan() {
        final DatagramSocket clientSocket = getSocket();
        if (clientSocket != null) {
            logger.debug("Discovery using socket on port {}", clientSocket.getLocalPort());
            discover();
        } else {
            logger.debug("Discovery not started. Client DatagramSocket null");
        }
    }

    private void discover() {
        startReceiverThread();
        NetUtil.getAllBroadcastAddresses().forEach(broadcastAddress -> {
            sendBroadcastPacket(broadcastAddress, Command.CMD_BROADCAST.getPayloadAlternative());
            scheduler.schedule(() -> sendBroadcastPacket(broadcastAddress, Command.CMD_BROADCAST.getPayload()), 5,
                    TimeUnit.SECONDS);
        });
    }

    public void addSensors(ThingUID bridgeUID, Collection<SensorDevice> sensorDevices) {
        for (SensorDevice sensorDevice : sensorDevices) {
            ThingUID uid = new ThingUID(FineOffsetWeatherStationBindingConstants.THING_TYPE_SENSOR, bridgeUID,
                    sensorDevice.getSensorGatewayBinding().name());

            String model = sensorDevice.getSensorGatewayBinding().getSensor().name();
            String prefix = "thing.sensor." + model;
            @Nullable
            String name = translationProvider.getText(bundle, prefix + ".label", model, localeProvider.getLocale());
            DiscoveryResultBuilder builder = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID)
                    .withProperty(FineOffsetSensorConfiguration.SENSOR, sensorDevice.getSensorGatewayBinding().name())
                    .withProperty(Thing.PROPERTY_MODEL_ID, model)
                    .withRepresentationProperty(FineOffsetSensorConfiguration.SENSOR);

            @Nullable
            Integer channel = sensorDevice.getSensorGatewayBinding().getChannel();
            if (channel != null) {
                builder.withProperty("channel", channel);
                name += " " + translationProvider.getText(bundle, "channel", "channel", localeProvider.getLocale())
                        + " " + channel;
            }
            builder.withLabel(name);
            @Nullable
            String description = translationProvider.getText(bundle, prefix + ".description", model,
                    localeProvider.getLocale());
            if (description != null) {
                builder.withProperty("description", description);
            }

            DiscoveryResult result = builder.build();
            thingDiscovered(result);
        }
    }

    private void discovered(String ip, int port, byte[] macAddr, String name) {
        String id = String.valueOf(Utils.toUInt64(macAddr, 0));

        Map<String, Object> properties = new HashMap<>();
        properties.put(Thing.PROPERTY_MAC_ADDRESS, Utils.toHexString(macAddr, macAddr.length, ":"));
        properties.put(FineOffsetGatewayConfiguration.IP, ip);
        properties.put(FineOffsetGatewayConfiguration.PORT, port);
        FineOffsetGatewayConfiguration config = new Configuration(properties).as(FineOffsetGatewayConfiguration.class);
        Protocol protocol = determineProtocol(config);
        if (protocol != null) {
            properties.put(FineOffsetGatewayConfiguration.PROTOCOL, protocol.name());
        }

        ThingUID uid = new ThingUID(THING_TYPE_GATEWAY, id);
        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS)
                .withLabel(translationProvider.getText(bundle, "thing.gateway.label", name, localeProvider.getLocale()))
                .build();
        thingDiscovered(result);
        logger.debug("Thing discovered '{}'", result);
    }

    @Nullable
    private Protocol determineProtocol(FineOffsetGatewayConfiguration config) {
        ConversionContext conversionContext = new ConversionContext(ZoneOffset.UTC);
        for (Protocol protocol : Protocol.values()) {
            try (GatewayQueryService gatewayQueryService = protocol.getGatewayQueryService(config, null,
                    conversionContext)) {
                Collection<MeasuredValue> result = gatewayQueryService.getMeasuredValues();
                logger.trace("found {} measured values via protocol {}", result.size(), protocol);
                if (!result.isEmpty()) {
                    return protocol;
                }
            } catch (IOException e) {
                logger.warn("", e);
            }
        }
        return null;
    }

    synchronized @Nullable DatagramSocket getSocket() {
        DatagramSocket clientSocket = this.clientSocket;
        if (clientSocket != null && clientSocket.isBound()) {
            return clientSocket;
        }
        try {
            logger.debug("Getting new socket for discovery");
            clientSocket = new DatagramSocket();
            clientSocket.setReuseAddress(true);
            clientSocket.setBroadcast(true);
            this.clientSocket = clientSocket;
            return clientSocket;
        } catch (SocketException | SecurityException e) {
            logger.debug("Error getting socket for discovery: {}", e.getMessage());
        }
        return null;
    }

    private void closeSocket() {
        final @Nullable DatagramSocket clientSocket = this.clientSocket;
        if (clientSocket != null) {
            clientSocket.close();
        } else {
            return;
        }
        this.clientSocket = null;
    }

    private void sendBroadcastPacket(String broadcastAddress, byte[] requestMessage) {
        final @Nullable DatagramSocket socket = getSocket();
        if (socket != null) {
            InetSocketAddress addr = new InetSocketAddress(broadcastAddress, DISCOVERY_PORT);
            DatagramPacket datagramPacket = new DatagramPacket(requestMessage, requestMessage.length, addr);
            logger.trace("sendBroadcastPacket: send request: {}",
                    Utils.toHexString(requestMessage, requestMessage.length, ""));
            try {
                socket.send(datagramPacket);
            } catch (IOException e) {
                logger.trace("Discovery on {} error: {}", broadcastAddress, e.getMessage());
            }
        }
    }

    /**
     * starts the {@link ReceiverThread} thread
     */
    private synchronized void startReceiverThread() {
        final Thread srt = socketReceiveThread;
        if (srt != null) {
            if (srt.isAlive() && !srt.isInterrupted()) {
                return;
            }
        }
        stopReceiverThreat();
        Thread socketReceiveThread = new ReceiverThread();
        socketReceiveThread.start();
        this.socketReceiveThread = socketReceiveThread;
    }

    /**
     * Stops the {@link ReceiverThread} thread
     */
    private synchronized void stopReceiverThreat() {
        final Thread socketReceiveThread = this.socketReceiveThread;
        if (socketReceiveThread != null) {
            socketReceiveThread.interrupt();
            this.socketReceiveThread = null;
        }
        closeSocket();
    }

    /**
     * The thread, which waits for data and submits the unique results addresses to the discovery results
     */
    private class ReceiverThread extends Thread {
        @Override
        public void run() {
            DatagramSocket socket = getSocket();
            if (socket != null) {
                logger.debug("Starting discovery receiver thread for socket on port {}", socket.getLocalPort());
                receiveData(socket);
            }
        }

        /**
         * This method waits for data and submits the unique results addresses to the discovery results
         *
         * @param socket - The multicast socket to (re)use
         */
        private void receiveData(DatagramSocket socket) {
            DatagramPacket receivePacket = new DatagramPacket(new byte[BUFFER_LENGTH], BUFFER_LENGTH);
            try {
                while (!interrupted()) {
                    logger.trace("Thread {} waiting for data on port {}", this, socket.getLocalPort());
                    socket.receive(receivePacket);
                    String hostAddress = receivePacket.getAddress().getHostAddress();
                    logger.trace("Received {} bytes response from {}:{} on Port {}", receivePacket.getLength(),
                            hostAddress, receivePacket.getPort(), socket.getLocalPort());

                    byte[] messageBuf = Arrays.copyOfRange(receivePacket.getData(), receivePacket.getOffset(),
                            receivePacket.getOffset() + receivePacket.getLength());
                    if (logger.isTraceEnabled()) {
                        logger.trace("Discovery response received: {}",
                                Utils.toHexString(messageBuf, messageBuf.length, ""));
                    }

                    if (Command.CMD_BROADCAST.isHeaderValid(messageBuf)) {
                        String ip = InetAddress.getByAddress(Arrays.copyOfRange(messageBuf, 11, 15)).getHostAddress();
                        var macAddr = Arrays.copyOfRange(messageBuf, 5, 5 + 6);
                        var port = toUInt16(messageBuf, 15);
                        var len = Utils.toUInt8(messageBuf[17]);
                        String name = new String(messageBuf, 18, len);
                        scheduler.schedule(() -> {
                            try {
                                discovered(ip, port, macAddr, name);
                            } catch (Exception e) {
                                logger.debug("Error submitting discovered device at {}", ip, e);
                            }
                        }, 0, TimeUnit.SECONDS);
                    }
                }
            } catch (SocketException e) {
                logger.debug("Receiver thread received SocketException: {}", e.getMessage());
            } catch (IOException e) {
                logger.trace("Receiver thread was interrupted");
            }
            logger.debug("Receiver thread ended");
        }
    }
}
