/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.emotiva.internal.discovery;

import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.SUPPORTED_THING_TYPES_UIDS;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.THING_PROCESSOR;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import javax.xml.bind.JAXBException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.emotiva.internal.EmotivaBindingConstants;
import org.openhab.binding.emotiva.internal.dto.EmotivaPingDTO;
import org.openhab.binding.emotiva.internal.dto.EmotivaTransponderDTO;
import org.openhab.binding.emotiva.internal.protocol.EmotivaXmlUtils;
import org.openhab.core.common.AbstractUID;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery service for Emotiva devices.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 * @author Espen Fossen - Adapted to Emotiva binding
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.emotiva")
public class EmotivaDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(EmotivaDiscoveryService.class);
    private static final int DISCOVERY_TIMEOUT_SECONDS = 5;
    private static final int MAX_PACKET_SIZE = 512;
    private static final int RECEIVE_JOB_TIMEOUT = 20000;
    private static final int UDP_PACKET_TIMEOUT = RECEIVE_JOB_TIMEOUT - 50;
    private static final String BROADCAST_ADDRESS = "255.255.255.255";
    @Nullable
    private DatagramSocket discoverSocket;
    private final EmotivaXmlUtils xmlUtils = new EmotivaXmlUtils();

    public EmotivaDiscoveryService() throws IllegalArgumentException, JAXBException {
        super(SUPPORTED_THING_TYPES_UIDS, DISCOVERY_TIMEOUT_SECONDS, false);
    }

    @Override
    protected void startScan() {
        logger.debug("Start scan for Emotiva devices.");
        discoverThings();
    }

    @Override
    protected void stopScan() {
        logger.debug("Stop scan for Emotiva devices.");
        closeDiscoverSocket();
        super.stopScan();
    }

    /**
     * Performs the actual discovery of Emotiva devices (things).
     */
    private void discoverThings() {
        try {
            final DatagramPacket receivePacket = new DatagramPacket(new byte[MAX_PACKET_SIZE], MAX_PACKET_SIZE);
            // No need to call close first, because the caller of this method already has done it.
            startDiscoverSocket();
            // Runs until the socket call gets a timeout and throws an exception. When a timeout is triggered it means
            // no data was present and nothing new to discover.
            while (true) {
                // Set packet length in case a previous call reduced the size.
                receivePacket.setLength(MAX_PACKET_SIZE);
                if (discoverSocket == null) {
                    break;
                } else {
                    discoverSocket.receive(receivePacket);
                }
                logger.debug("Emotiva device discovery returned package with length {}", receivePacket.getLength());
                if (receivePacket.getLength() > 0) {
                    thingDiscovered(receivePacket);
                }
            }
        } catch (SocketTimeoutException e) {
            logger.debug("Discovering poller timeout...");
        } catch (IOException e) {
            logger.debug("Error during discovery: {}", e.getMessage());
        } finally {
            closeDiscoverSocket();
            removeOlderResults(getTimestampOfLastScan());
        }
    }

    /**
     * Opens a {@link DatagramSocket} and sends a packet for discovery of Emotiva devices.
     *
     * @throws SocketException Error creating UDP socket
     * @throws UnknownHostException Could not send message on socket
     */
    private void startDiscoverSocket() throws SocketException, IOException {
        discoverSocket = new DatagramSocket(new InetSocketAddress(EmotivaBindingConstants.DEFAULT_TRANSPONDER_PORT));
        discoverSocket.setBroadcast(true);
        discoverSocket.setSoTimeout(UDP_PACKET_TIMEOUT);
        final InetAddress broadcast = InetAddress.getByName(BROADCAST_ADDRESS);

        byte[] emotivaPingDTO = xmlUtils.marshallEmotivaDTO(new EmotivaPingDTO("3.0"))
                .getBytes(Charset.defaultCharset());
        final DatagramPacket discoverPacket = new DatagramPacket(emotivaPingDTO, emotivaPingDTO.length, broadcast,
                EmotivaBindingConstants.DEFAULT_PING_PORT);
        discoverSocket.send(discoverPacket);
        if (logger.isTraceEnabled()) {
            logger.trace("Discovery package sent: {}", new String(discoverPacket.getData(), StandardCharsets.UTF_8));
        }
    }

    /**
     * Closes the discovery socket and cleans the value. No need for synchronization as this method is called from a
     * synchronized context.
     */
    private void closeDiscoverSocket() {
        if (discoverSocket != null) {
            discoverSocket.close();
            discoverSocket = null;
        }
    }

    /**
     * Register a device (thing) with the discovered properties.
     *
     * @param packet containing data of detected device
     */
    private void thingDiscovered(DatagramPacket packet) {
        final String ipAddress = packet.getAddress().getHostAddress();
        String udpResponse = new String(packet.getData(), 0, packet.getLength() - 1, StandardCharsets.UTF_8);

        Object object;
        try {
            object = xmlUtils.unmarshallToEmotivaDTO(udpResponse);
        } catch (JAXBException e) {
            logger.debug("Could not unmarshal {}:{} ", ipAddress, udpResponse.length());
            return;
        }

        if (object instanceof EmotivaTransponderDTO answerDto) {
            logger.debug("Processing Received {} with {} ", EmotivaTransponderDTO.class.getSimpleName(), udpResponse);
            final ThingUID thingUid = new ThingUID(
                    THING_PROCESSOR + AbstractUID.SEPARATOR + ipAddress.replace(".", ""));
            final DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUid)
                    .withThingType(THING_PROCESSOR).withProperty("ipAddress", ipAddress)
                    .withProperty("controlPort", answerDto.getControl().getControlPort())
                    .withProperty("notifyPort", answerDto.getControl().getNotifyPort())
                    .withProperty("infoPort", answerDto.getControl().getInfoPort())
                    .withProperty("setupPortTCP", answerDto.getControl().getSetupPortTCP())
                    .withProperty("menuNotifyPort", answerDto.getControl().getMenuNotifyPort())
                    .withProperty("model", answerDto.getModel())
                    .withProperty("revision", Objects.requireNonNullElse(answerDto.getRevision(), "2.0"))
                    .withProperty("dataRevision", Objects.requireNonNullElse(answerDto.getDataRevision(), "1.0"))
                    .withProperty("protocolVersion",
                            Objects.requireNonNullElse(answerDto.getControl().getVersion(), "2.0"))
                    .withProperty("keepAlive", answerDto.getControl().getKeepAlive())
                    .withProperty(EmotivaBindingConstants.UNIQUE_PROPERTY_NAME, ipAddress)
                    .withLabel(answerDto.getName())
                    .withRepresentationProperty(EmotivaBindingConstants.UNIQUE_PROPERTY_NAME).build();
            try {
                logger.debug("Adding newly discovered thing {}:{} with properties {}", THING_PROCESSOR, ipAddress,
                        discoveryResult.getProperties());

                thingDiscovered(discoveryResult);
            } catch (Exception e) {
                logger.debug("Failed adding discovered thing {}:{} with properties {}", THING_PROCESSOR, ipAddress,
                        discoveryResult.getProperties(), e);
            }
        } else {
            logger.debug("Received message of unknown type in message {}", udpResponse);
        }
    }
}
