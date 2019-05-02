/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.paradoxalarm.internal.communication;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openhab.binding.paradoxalarm.internal.communication.messages.EpromRequestPayload;
import org.openhab.binding.paradoxalarm.internal.communication.messages.HeaderMessageType;
import org.openhab.binding.paradoxalarm.internal.communication.messages.IPPacketPayload;
import org.openhab.binding.paradoxalarm.internal.communication.messages.ParadoxIPPacket;
import org.openhab.binding.paradoxalarm.internal.communication.messages.RamRequestPayload;
import org.openhab.binding.paradoxalarm.internal.exceptions.ParadoxBindingException;
import org.openhab.binding.paradoxalarm.internal.model.ZoneStateFlags;
import org.openhab.binding.paradoxalarm.internal.util.ParadoxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EvoCommunicator} is responsible for handling communication to Evo192 alarm system via IP150 interface.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public class EvoCommunicator extends GenericCommunicator implements IParadoxCommunicator {

    private final Logger logger = LoggerFactory.getLogger(EvoCommunicator.class);

    private MemoryMap memoryMap;

    public EvoCommunicator(String ipAddress, int tcpPort, String ip150Password, String pcPassword)
            throws UnknownHostException, IOException, InterruptedException, ParadoxBindingException {
        super(ipAddress, tcpPort, ip150Password, pcPassword);
        initializeMemoryMap();
    }

    @Override
    public List<String> readPartitionLabels() throws IOException, InterruptedException, ParadoxBindingException {
        List<String> result = new ArrayList<>();

        for (int i = 1; i <= 8; i++) {
            result.add(readPartitionLabel(i));
        }
        return result;
    }

    private String readPartitionLabel(int partitionNo)
            throws IOException, InterruptedException, ParadoxBindingException {
        logger.debug("Reading partition label: {}", partitionNo);
        if (partitionNo < 1 || partitionNo > 8) {
            throw new ParadoxBindingException("Invalid partition number. Valid values are 1-8.");
        }

        int address = 0x3A6B + (partitionNo - 1) * 107;
        byte labelLength = 16;

        byte[] payloadResult = readEepromMemory(address, labelLength);

        String result = createString(payloadResult);
        logger.debug("Partition label: {}", result);
        return result;
    }

    @Override
    public List<String> readZoneLabels() {
        List<String> result = new ArrayList<>();

        try {
            for (int i = 1; i <= 60; i++) {
                result.add(readZoneLabel(i));
            }
        } catch (IOException | ParadoxBindingException | InterruptedException e) {
            logger.debug("Unable to retrieve zone labels.", e);
        }
        return result;
    }

    private String readZoneLabel(int zoneNumber) throws ParadoxBindingException, IOException, InterruptedException {
        logger.debug("Reading zone label: {}", zoneNumber);
        if (zoneNumber < 1 || zoneNumber > 192) {
            throw new ParadoxBindingException("Invalid zone number. Valid values are 1-192.");
        }

        byte labelLength = 16;

        int address;
        if (zoneNumber <= 96) {
            address = 0x430 + (zoneNumber - 1) * 16;
        } else {
            address = 0x62F7 + (zoneNumber - 97) * 16;
        }

        byte[] payloadResult = readEepromMemory(address, labelLength);

        String result = createString(payloadResult);
        logger.debug("Zone label: {}", result);
        return result;
    }

    @Override
    public List<byte[]> readPartitionFlags() {
        List<byte[]> result = new ArrayList<>();

        byte[] element = memoryMap.getElement(2);
        byte[] firstBlock = Arrays.copyOfRange(element, 32, 64);

        element = memoryMap.getElement(3);
        byte[] secondBlock = Arrays.copyOfRange(element, 0, 16);
        byte[] mergeByteArrays = ParadoxUtil.mergeByteArrays(firstBlock, secondBlock);
        for (int i = 0; i < mergeByteArrays.length; i += 6) {
            result.add(Arrays.copyOfRange(mergeByteArrays, i, i + 6));
        }

        return result;
    }

    @Override
    public ZoneStateFlags readZoneStateFlags() {
        ZoneStateFlags result = new ZoneStateFlags();

        byte[] firstPage = memoryMap.getElement(0);
        byte[] secondPage = memoryMap.getElement(8);

        byte[] firstBlock = Arrays.copyOfRange(firstPage, 28, 40);
        byte[] secondBlock = Arrays.copyOfRange(secondPage, 0, 22);
        byte[] zonesOpened = ParadoxUtil.mergeByteArrays(firstBlock, secondBlock);
        result.setZonesOpened(zonesOpened);

        firstBlock = Arrays.copyOfRange(firstPage, 40, 52);
        secondBlock = Arrays.copyOfRange(secondPage, 22, 34);
        byte[] zonesTampered = ParadoxUtil.mergeByteArrays(firstBlock, secondBlock);
        result.setZonesTampered(zonesTampered);

        firstBlock = Arrays.copyOfRange(firstPage, 52, 52);
        secondBlock = Arrays.copyOfRange(secondPage, 34, 64);
        byte[] zonesLowBattery = ParadoxUtil.mergeByteArrays(firstBlock, secondBlock);
        result.setZonesLowBattery(zonesLowBattery);

        return result;
    }

    public void initializeMemoryMap() throws ParadoxBindingException, IOException, InterruptedException {
        List<byte[]> ramCache = new ArrayList<>();
        for (int i = 1; i <= 16; i++) {
            logger.debug("Reading memory page number: {}", i);
            ramCache.add(readRAMBlock(i));
        }
        ramCache.add(readRAMBlock(0x10));
        memoryMap = new MemoryMap(ramCache);
    }

    @Override
    public void refreshMemoryMap() throws ParadoxBindingException, IOException, InterruptedException {
        if (isOnline()) {
            for (int i = 1, j = 0; i <= 16; i++, j++) {
                logger.trace("Reading memory page number: {}", i);
                memoryMap.updateElement(j, readRAMBlock(i));
            }
        } else {
            logger.debug("Unable to refresh memory map. Communicator is offline");
        }
    }

    private byte[] readRAMBlock(int blockNo) throws ParadoxBindingException, IOException, InterruptedException {
        if (isOnline()) {
            return readRAM(blockNo, (byte) 64);
        } else {
            return new byte[0];
        }
    }

    private byte[] readRAM(int blockNo, byte bytesToRead)
            throws ParadoxBindingException, IOException, InterruptedException {
        IPPacketPayload payload = new RamRequestPayload(blockNo, bytesToRead);
        return readMemory(payload);
    }

    private byte[] readEepromMemory(int address, byte bytesToRead)
            throws IOException, InterruptedException, ParadoxBindingException {
        if (bytesToRead < 1 || bytesToRead > 64) {
            throw new ParadoxBindingException("Invalid bytes to read. Valid values are 1 to 64.");
        }

        IPPacketPayload payload = new EpromRequestPayload(address, bytesToRead);
        return readMemory(payload);
    }

    private byte[] readMemory(IPPacketPayload payload)
            throws IOException, InterruptedException, ParadoxBindingException {
        ParadoxIPPacket readEpromIPPacket = new ParadoxIPPacket(payload)
                .setMessageType(HeaderMessageType.SERIAL_PASSTHRU_REQUEST).setUnknown0((byte) 0x14);

        sendPacket(readEpromIPPacket);
        return receivePacket((byte) 0x5);
    }

    /**
     * This method reads data from the IP150 module. It can return multiple
     * responses e.g. a live event is combined with another response.
     * The open active TCP/IP stream.
     * A panel command, e.g. 0x5 (read memory
     * An array of an array of the raw bytes received from the TCP/IP
     * stream.
     *
     * @param command (currently it's only 0x5 but other commands can be used for different other areas)
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws ParadoxBindingException
     */
    private byte[] receivePacket(byte command) throws IOException, InterruptedException, ParadoxBindingException {
        if (command > 0xF) {
            command = ParadoxUtil.getHighNibble(command);
        }

        byte retryCounter = 0;

        // We might enter this too early, meaning the panel has not yet had time to
        // respond
        // to our command. We add a retry counter that will wait and retry.
        while (retryCounter < 3) {
            byte[] packetResponse = receivePacket();
            List<byte[]> responses = splitResponsePackets(packetResponse);
            for (byte[] response : responses) {
                // Message too short
                if (response.length < 17) {
                    continue;
                }

                // Response command (after header) is not related to reading memory
                if (ParadoxUtil.getHighNibble(response[16]) != command) {
                    continue;
                }

                return Arrays.copyOfRange(response, 22, response.length - 1);
            }

            // Give the panel time to send us a response
            Thread.sleep(100);

            retryCounter++;
        }

        logger.debug("Failed to receive data for command 0x{0:X}", command);
        return null;
    }

    private List<byte[]> splitResponsePackets(byte[] response) throws ParadoxBindingException {
        List<byte[]> packets = new ArrayList<>();
        byte[] responseCopy = Arrays.copyOf(response, response.length);
        int totalLength = responseCopy.length;
        while (responseCopy.length > 0) {
            if (responseCopy.length < 16 || responseCopy[0] != (byte) 0xAA) {
                logger.debug("No 16 byte header found");
            }

            byte[] header = Arrays.copyOfRange(responseCopy, 0, 16);
            byte messageLength = header[1];

            // Remove the header
            responseCopy = Arrays.copyOfRange(responseCopy, 16, totalLength);

            if (responseCopy.length < messageLength) {
                throw new ParadoxBindingException("Unexpected end of data");
            }

            // Check if there's padding bytes (0xEE)
            if (responseCopy.length > messageLength) {
                for (int i = messageLength; i < responseCopy.length; i++) {
                    if (responseCopy[i] == 0xEE) {
                        messageLength++;
                    } else {
                        break;
                    }
                }
            }

            byte[] message = Arrays.copyOfRange(responseCopy, 0, messageLength);

            responseCopy = Arrays.copyOfRange(responseCopy, messageLength, responseCopy.length);

            packets.add(ParadoxUtil.mergeByteArrays(header, message));
        }

        return packets;
    }

    private String createString(byte[] payloadResult) {
        return new String(payloadResult, StandardCharsets.US_ASCII);
    }

    @Override
    public void executeCommand(String command) {
        try {
            IP150Command ip150Command = IP150Command.valueOf(command);
            switch (ip150Command) {
                case LOGIN:
                    loginSequence();
                    return;
                case LOGOUT:
                    logoutSequence();
                    return;
                case RESET:
                    close();
                    loginSequence();
                    return;
                default:
                    logger.debug("Command {} not implemented.", command);
            }
        } catch (IOException | InterruptedException e) {
            logger.warn("Error while executing command {}. Exception:{}", command, e);
        }
    }
}
