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
package org.openhab.binding.emotiva.internal;

import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.*;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaProtocolVersion.PROTOCOL_V3;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.JAXBException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.emotiva.internal.dto.EmotivaPingDTO;
import org.openhab.binding.emotiva.internal.dto.EmotivaTransponderDTO;
import org.openhab.binding.emotiva.internal.protocol.EmotivaXmlUtils;
import org.openhab.core.common.AbstractUID;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This service is used for discovering Emotiva devices via sending an EmotivaPing UDP message.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 * @author Espen Fossen - Adapted to Emotiva binding
 */
@NonNullByDefault
public class EmotivaUdpBroadcastService {

    private final Logger logger = LoggerFactory.getLogger(EmotivaUdpBroadcastService.class);
    private static final int MAX_PACKET_SIZE = 512;
    @Nullable
    private DatagramSocket discoverSocket;
    private final EmotivaXmlUtils xmlUtils = new EmotivaXmlUtils();

    /**
     * The address to broadcast EmotivaPing message to.
     */
    private final String broadcastAddress;

    public EmotivaUdpBroadcastService(String broadcastAddress) throws IllegalArgumentException, JAXBException {
        if (broadcastAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing broadcast address");
        }
        this.broadcastAddress = broadcastAddress;
    }

    /**
     * Performs the actual discovery of Emotiva devices (things).
     */
    public Optional<DiscoveryResult> discoverThings() {
        try {
            final DatagramPacket receivePacket = new DatagramPacket(new byte[MAX_PACKET_SIZE], MAX_PACKET_SIZE);
            // No need to call close first, because the caller of this method already has done it.

            discoverSocket = new DatagramSocket(
                    new InetSocketAddress(EmotivaBindingConstants.DEFAULT_TRANSPONDER_PORT));
            final InetAddress broadcast = InetAddress.getByName(broadcastAddress);

            byte[] emotivaPingDTO = xmlUtils.marshallEmotivaDTO(new EmotivaPingDTO(PROTOCOL_V3.name()))
                    .getBytes(Charset.defaultCharset());
            final DatagramPacket discoverPacket = new DatagramPacket(emotivaPingDTO, emotivaPingDTO.length, broadcast,
                    EmotivaBindingConstants.DEFAULT_PING_PORT);

            DatagramSocket localDatagramSocket = discoverSocket;
            while (localDatagramSocket != null && discoverSocket != null) {
                localDatagramSocket.setBroadcast(true);
                localDatagramSocket.setSoTimeout(DEFAULT_UDP_SENDING_TIMEOUT);
                localDatagramSocket.send(discoverPacket);
                if (logger.isTraceEnabled()) {
                    logger.trace("Discovery package sent: {}",
                            new String(discoverPacket.getData(), StandardCharsets.UTF_8));
                }

                // Runs until the socket call gets a timeout and throws an exception. When a timeout is triggered it
                // means
                // no data was present and nothing new to discover.
                while (true) {
                    // Set packet length in case a previous call reduced the size.
                    receivePacket.setLength(MAX_PACKET_SIZE);
                    if (discoverSocket == null) {
                        break;
                    } else {
                        localDatagramSocket.receive(receivePacket);
                    }
                    logger.debug("Emotiva device discovery returned package with length '{}'",
                            receivePacket.getLength());
                    if (receivePacket.getLength() > 0) {
                        return thingDiscovered(receivePacket);
                    }
                }
            }
        } catch (SocketTimeoutException e) {
            logger.debug("Discovering poller timeout...");
        } catch (InterruptedIOException e) {
            logger.debug("Interrupted during discovery: {}", e.getMessage());
        } catch (IOException e) {
            logger.debug("Error during discovery: {}", e.getMessage());
        } finally {
            closeDiscoverSocket();
        }
        return Optional.empty();
    }

    /**
     * Closes the discovery socket and cleans the value. No need for synchronization as this method is called from a
     * synchronized context.
     */
    public void closeDiscoverSocket() {
        final DatagramSocket localDiscoverSocket = discoverSocket;
        if (localDiscoverSocket != null) {
            discoverSocket = null;
            if (!localDiscoverSocket.isClosed()) {
                localDiscoverSocket.close(); // this interrupts and terminates the listening thread
            }
        }
    }

    /**
     * Register a device (thing) with the discovered properties.
     *
     * @param packet containing data of detected device
     */
    private Optional<DiscoveryResult> thingDiscovered(DatagramPacket packet) {
        final String ipAddress = packet.getAddress().getHostAddress();
        String udpResponse = new String(packet.getData(), 0, packet.getLength() - 1, StandardCharsets.UTF_8);

        Object object;
        try {
            object = xmlUtils.unmarshallToEmotivaDTO(udpResponse);
        } catch (JAXBException e) {
            logger.debug("Could not unmarshal '{}:{}'", ipAddress, udpResponse.length());
            return Optional.empty();
        }

        if (object instanceof EmotivaTransponderDTO answerDto) {
            logger.trace("Processing Received '{}' with '{}' ", EmotivaTransponderDTO.class.getSimpleName(),
                    udpResponse);
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
                    .withProperty("revision", Objects.requireNonNullElse(answerDto.getRevision(), ""))
                    .withProperty("dataRevision", Objects.requireNonNullElse(answerDto.getDataRevision(), ""))
                    .withProperty("protocolVersion",
                            Objects.requireNonNullElse(answerDto.getControl().getVersion(),
                                    DEFAULT_EMOTIVA_PROTOCOL_VERSION))
                    .withProperty("keepAlive", answerDto.getControl().getKeepAlive())
                    .withProperty(EmotivaBindingConstants.UNIQUE_PROPERTY_NAME, ipAddress)
                    .withLabel(answerDto.getName())
                    .withRepresentationProperty(EmotivaBindingConstants.UNIQUE_PROPERTY_NAME).build();
            try {
                logger.debug("Adding newly discovered thing '{}:{}' with properties '{}'", THING_PROCESSOR, ipAddress,
                        discoveryResult.getProperties());

                return Optional.of(discoveryResult);
            } catch (Exception e) {
                logger.debug("Failed adding discovered thing '{}:{}' with properties '{}'", THING_PROCESSOR, ipAddress,
                        discoveryResult.getProperties(), e);
            }
        } else {
            logger.debug("Received message of unknown type in message '{}'", udpResponse);
        }
        return Optional.empty();
    }
}
