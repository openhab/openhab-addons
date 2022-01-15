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
package org.openhab.binding.touchwand.internal.discovery;

import static org.openhab.binding.touchwand.internal.TouchWandBindingConstants.THING_TYPE_BRIDGE;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.touchwand.internal.TouchWandBindingConstants;
import org.openhab.binding.touchwand.internal.TouchWandBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link TouchWandControllerDiscoveryService} Discovery service for Touchwand Controllers.
 *
 * @author Roie Geron - Initial contribution
 */
@Component(service = DiscoveryService.class, configurationPid = "discovery.touchwand")
@NonNullByDefault
public class TouchWandControllerDiscoveryService extends AbstractDiscoveryService {

    private static final int SEARCH_TIME_SEC = 2;
    private static final int TOUCHWAND_BCAST_PORT = 35000;
    private final Logger logger = LoggerFactory.getLogger(TouchWandControllerDiscoveryService.class);

    private @Nullable Thread socketReceiveThread = null;
    private DatagramSocket listenSocket;

    public TouchWandControllerDiscoveryService() throws SocketException {
        super(TouchWandBridgeHandler.SUPPORTED_THING_TYPES, SEARCH_TIME_SEC, true);

        listenSocket = new DatagramSocket(TOUCHWAND_BCAST_PORT);
    }

    @Override
    protected void startScan() {
        DatagramSocket localListenSocket = listenSocket;
        runReceiveThread(localListenSocket);
    }

    @Override
    protected void stopScan() {
        super.stopScan();
    }

    @Override
    public void activate(@Nullable Map<String, Object> configProperties) {
        removeOlderResults(getTimestampOfLastScan());
        super.activate(configProperties);
    }

    @Override
    public void deactivate() {
        Thread mySocketReceiveThread = socketReceiveThread;
        if (mySocketReceiveThread != null) {
            mySocketReceiveThread.interrupt();
            socketReceiveThread = null;
        }

        listenSocket.close();
        super.deactivate();
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
                .withRepresentationProperty("ipAddress")
                .build()
        );
        // @formatter:on
    }

    protected void runReceiveThread(DatagramSocket socket) {
        Thread localSocketReceivedThread = socketReceiveThread = new ReceiverThread(socket);
        localSocketReceivedThread.setName(TouchWandBindingConstants.DISCOVERY_THREAD_ID);
        localSocketReceivedThread.setDaemon(true);
        localSocketReceivedThread.start();
    }

    private class ReceiverThread extends Thread {

        private static final int BUFFER_LENGTH = 256;
        private DatagramPacket dgram = new DatagramPacket(new byte[BUFFER_LENGTH], BUFFER_LENGTH);
        private DatagramSocket mySocket;

        public ReceiverThread(DatagramSocket socket) {
            mySocket = socket;
        }

        @Override
        public void run() {
            receiveData(dgram);
        }

        private void receiveData(DatagramPacket datagram) {
            try {
                while (!isInterrupted()) {
                    mySocket.receive(datagram);
                    InetAddress address = datagram.getAddress();
                    String sentence = new String(dgram.getData(), 0, dgram.getLength(), StandardCharsets.US_ASCII);
                    JsonObject bridge = JsonParser.parseString(sentence).getAsJsonObject();//
                    String name = bridge.get("name").getAsString();
                    addDeviceDiscoveryResult(name, address.getHostAddress().toString());
                    logger.debug("Received Datagram from {}:{} on Port {} message {}", address.getHostAddress(),
                            dgram.getPort(), mySocket.getLocalPort(), sentence);
                }
            } catch (IOException | JsonSyntaxException e) {
                if (!isInterrupted()) {
                    logger.debug("Error while receiving {}", e.getMessage());
                } else {
                    logger.debug("Receiver thread was interrupted {}", e.getMessage());
                }
            }
        }
    }
}
