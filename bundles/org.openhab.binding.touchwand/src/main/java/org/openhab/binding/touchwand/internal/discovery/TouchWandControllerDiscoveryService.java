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

package org.openhab.binding.touchwand.internal.discovery;

import static org.openhab.binding.touchwand.internal.TouchWandBindingConstants.THING_TYPE_BRIDGE;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.touchwand.internal.TouchWandBridgeHandler;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TouchWandControllerDiscoveryService} Discovery service for Touchwand Controllers.
 *
 * @author Roie Geron - Initial contribution
 */
@Component(service = DiscoveryService.class, immediate = false, configurationPid = "discovery.touchwand")
public class TouchWandControllerDiscoveryService extends AbstractDiscoveryService {

    private static final int SEARCH_TIME = 2;
    private static final int TOUCHWAND_BCAST_PORT = 35000;
    private Thread socketReceiveThread = null;
    private DatagramSocket listenSocket = null;

    private final Logger logger = LoggerFactory.getLogger(TouchWandControllerDiscoveryService.class);

    public TouchWandControllerDiscoveryService() {
        super(TouchWandBridgeHandler.SUPPORTED_THING_TYPES, SEARCH_TIME, true);
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    protected void startScan() {
        try {
            listenSocket = new DatagramSocket(TOUCHWAND_BCAST_PORT);
        } catch (SocketException e) {
            logger.warn("SocketException {}", e.getMessage());
        }
        logger.trace("Starting TouchWand Controller discovery");
        runReceiveThread(listenSocket);
    }

    @Override
    protected synchronized void stopScan() {
        logger.trace("Stopping TouchWand Controller discovery");
        super.stopScan();
        deactivate();
    }

    @Override
    public void deactivate() {
        super.deactivate();
        if (socketReceiveThread != null) {
            socketReceiveThread.interrupt();
            socketReceiveThread = null;
        }
        if (listenSocket != null) {
            listenSocket.close();
        }
        logger.trace("Deactivate discovery services");
    }

    private void addDeviceDiscoveryResult(String label, String ip) {
        String id = ip.replaceAll("\\.", "");
        ThingUID thingUID = new ThingUID(THING_TYPE_BRIDGE, id);
        Map<String, Object> properties = new HashMap<>();
        properties.put("label", label);
        properties.put("ipAddress", ip);
        // @formatter:off
        logger.debug("Add new Bridge label:{} id {} ",label, id);
        thingDiscovered(DiscoveryResultBuilder.create(thingUID)
                .withThingType(THING_TYPE_BRIDGE)
                .withLabel(label)
                .withProperties(properties)
                .withRepresentationProperty(ip)
                .build()
        );
        // @formatter:on
    }

    protected void runReceiveThread(DatagramSocket socket) {
        socketReceiveThread = new ReceiverThread(socket);
        socketReceiveThread.start();
    }

    @NonNullByDefault
    private class ReceiverThread extends Thread {

        private static final int BUFFER_LENGTH = 256;
        private DatagramPacket dgram = new DatagramPacket(new byte[BUFFER_LENGTH], BUFFER_LENGTH);
        DatagramSocket mySocket;

        public ReceiverThread(DatagramSocket socket) {
            mySocket = socket;
        }

        @Override
        public void run() {
            logger.trace("Staring reveicer thread for socket");
            receiveData(dgram);
        }

        private void receiveData(DatagramPacket datagram) {
            try {
                while (true) {
                    mySocket.receive(datagram);
                    InetAddress address = datagram.getAddress();
                    String sentence = new String(dgram.getData(), 0, dgram.getLength());
                    addDeviceDiscoveryResult(sentence, address.getHostAddress().toString());
                    logger.debug("Received Datagram from {}:{} on Port {} message {}", address.getHostAddress(),
                            dgram.getPort(), mySocket.getLocalPort(), sentence);
                }
            } catch (IOException e) {
                if (!isInterrupted()) {
                    logger.warn("Error while receiving", e);
                } else {
                    logger.trace("Receiver thread was interrupted");
                }
            }
            logger.trace("Receiver thread ended");
        }
    }

}
