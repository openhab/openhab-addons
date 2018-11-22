/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.paradoxalarm.internal.communication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openhab.binding.paradoxalarm.internal.communication.messages.EpromRequestPayload;
import org.openhab.binding.paradoxalarm.internal.communication.messages.HeaderCommand;
import org.openhab.binding.paradoxalarm.internal.communication.messages.HeaderMessageType;
import org.openhab.binding.paradoxalarm.internal.communication.messages.IPPacketPayload;
import org.openhab.binding.paradoxalarm.internal.communication.messages.IpMessagesConstants;
import org.openhab.binding.paradoxalarm.internal.communication.messages.ParadoxIPPacket;
import org.openhab.binding.paradoxalarm.internal.communication.messages.RamRequestPayload;
import org.openhab.binding.paradoxalarm.internal.model.ZoneStateFlags;
import org.openhab.binding.paradoxalarm.internal.util.ParadoxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Evo192Communicator} is responsible for handling communication to Evo192 alarm system via IP150 interface.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public class Evo192Communicator implements IParadoxCommunicator {

    private static Logger logger = LoggerFactory.getLogger(Evo192Communicator.class);

    private Socket socket;
    private DataOutputStream tx;
    private DataInputStream rx;
    private final byte[] pcPassword;

    private String password;

    MemoryMap memoryMap;

    public Evo192Communicator(String ipAddress, int tcpPort, String ip150Password, String pcPassword) throws Exception {
        socket = new Socket(ipAddress, tcpPort);
        socket.setSoTimeout(2000);
        tx = new DataOutputStream(socket.getOutputStream());
        rx = new DataInputStream(socket.getInputStream());
        password = ip150Password;
        this.pcPassword = ParadoxUtil.stringToBCD(pcPassword);

        loginSequence();
        initializeMemoryMap();
    }

    /*
     * (non-Javadoc)
     *
     * @see mainApp.ParadoxAdapter#close()
     */
    @Override
    public void close() throws IOException {
        tx.close();
        rx.close();
        socket.close();
    }

    /*
     * (non-Javadoc)
     *
     * @see mainApp.ParadoxAdapter#logoutSequence()
     */
    @Override
    public void logoutSequence() throws IOException {
        logger.debug("Logout sequence started");
        byte[] logoutMessage = new byte[] { 0x00, 0x07, 0x05, 0x00, 0x00, 0x00, 0x00 };
        ParadoxIPPacket logoutPacket = new ParadoxIPPacket(logoutMessage, true)
                .setMessageType(HeaderMessageType.SERIAL_PASSTHRU_REQUEST).setUnknown0((byte) 0x14);
        sendPacket(logoutPacket);
    }

    /*
     * (non-Javadoc)
     *
     * @see mainApp.ParadoxAdapter#loginSequence()
     */
    @Override
    public void loginSequence() throws IOException, InterruptedException {
        logger.debug("Step1");
        // 1: Login to module request (IP150 only)
        ParadoxIPPacket ipPacket = new ParadoxIPPacket(password, false).setCommand(HeaderCommand.CONNECT_TO_IP_MODULE);
        sendPacket(ipPacket);
        byte[] sendPacket = receivePacket();
        if (sendPacket[4] == 0x38) {
            logger.debug("Login OK");
        } else {
            logger.debug("Login failed");
        }

        logger.debug("Step2");
        // 2: Unknown request (IP150 only)
        ParadoxIPPacket step2 = new ParadoxIPPacket(ParadoxIPPacket.EMPTY_PAYLOAD, false)
                .setCommand(HeaderCommand.LOGIN_COMMAND1);
        sendPacket(step2);
        receivePacket();

        logger.debug("Step3");
        // 3: Unknown request (IP150 only)
        ParadoxIPPacket step3 = new ParadoxIPPacket(ParadoxIPPacket.EMPTY_PAYLOAD, false)
                .setCommand(HeaderCommand.LOGIN_COMMAND2);
        sendPacket(step3);
        receivePacket();

        logger.debug("Step4");
        // 4: Init communication over UIP softawre request (IP150 and direct serial)
        byte[] message4 = new byte[37];
        message4[0] = 0x72;
        ParadoxIPPacket step4 = new ParadoxIPPacket(message4, true)
                .setMessageType(HeaderMessageType.SERIAL_PASSTHRU_REQUEST);
        sendPacket(step4);
        receivePacket();

        logger.debug("Step5");
        // 5: Unknown request (IP150 only)
        ParadoxIPPacket step5 = new ParadoxIPPacket(IpMessagesConstants.UNKNOWN_IP150_REQUEST_MESSAGE01, false)
                .setCommand(HeaderCommand.SERIAL_CONNECTION_INITIATED);
        sendPacket(step5);
        receivePacket();

        logger.debug("Step6");
        // 6: Initialize serial communication request (IP150 and direct serial)
        byte[] message6 = new byte[37];
        message6[0] = 0x5F;
        message6[1] = 0x20;
        ParadoxIPPacket step6 = new ParadoxIPPacket(message6, true)
                .setMessageType(HeaderMessageType.SERIAL_PASSTHRU_REQUEST);
        sendPacket(step6);
        byte[] response6 = receivePacket();
        byte[] initializationMessage = Arrays.copyOfRange(response6, 16, response6.length);
        ParadoxUtil.printPacket("Init communication sub array: ", initializationMessage);

        logger.debug("Step7");
        // 7: Initialization request (in response to the initialization from the panel)
        // (IP150 and direct serial)
        byte[] message7 = generateInitializationRequest(initializationMessage, pcPassword);
        ParadoxIPPacket step7 = new ParadoxIPPacket(message7, true)
                .setMessageType(HeaderMessageType.SERIAL_PASSTHRU_REQUEST).setUnknown0((byte) 0x14);
        sendPacket(step7);
        byte[] finalResponse = receivePacket();
        if ((finalResponse[16] & 0xF0) == 0x10) {
            logger.debug("SUCCESSFUL LOGON");
        } else {
            logger.debug("LOGON FAILURE");
        }
        Thread.sleep(300);
        // TODO check why after a short sleep a 37 bytes packet is received after logon
        // ! ! !
        receivePacket();
    }

    /*
     * (non-Javadoc)
     *
     * @see mainApp.ParadoxAdapter#readPartitions()
     */
    @Override
    public List<String> readPartitionLabels() {
        List<String> result = new ArrayList<>();

        try {
            for (int i = 1; i <= 8; i++) {
                result.add(readPartitionLabel(i));
            }
        } catch (Exception e) {
            logger.debug("Unable to retrieve partition labels.\nException: " + e.getMessage());
        }
        return result;
    }

    public String readPartitionLabel(int partitionNo) throws Exception {
        logger.debug("Reading partition label: " + partitionNo);
        if (partitionNo < 1 || partitionNo > 8) {
            throw new Exception("Invalid partition number. Valid values are 1-8.");
        }

        int address = 0x3A6B + (partitionNo - 1) * 107;
        byte labelLength = 16;

        byte[] payloadResult = readEepromMemory(address, labelLength);

        String result = createString(payloadResult);
        logger.debug("Partition label: {}", result);
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see mainApp.ParadoxAdapter#readZones()
     */
    @Override
    public List<String> readZoneLabels() {
        List<String> result = new ArrayList<>();

        try {
            for (int i = 1; i <= 60; i++) {
                result.add(readZoneLabel(i));
            }
        } catch (Exception e) {
            logger.debug("Unable to retrieve zone labels.\nException: " + e.getMessage());
        }
        return result;
    }

    public String readZoneLabel(int zoneNumber) throws Exception {
        logger.debug("Reading zone label: " + zoneNumber);
        if (zoneNumber < 1 || zoneNumber > 192) {
            throw new Exception("Invalid zone number. Valid values are 1-192.");
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
        logger.debug("Zone label: " + result);
        return result;
    }

    @Override
    public List<byte[]> readPartitionFlags() throws Exception {
        List<byte[]> result = new ArrayList<byte[]>();

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
    public ZoneStateFlags readZoneStateFlags() throws Exception {
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

    public void initializeMemoryMap() throws Exception {
        List<byte[]> ramCache = new ArrayList<>();
        for (int i = 1; i <= 16; i++) {
            logger.debug("Reading memory page number: {}", i);
            ramCache.add(readRAMBlock(i));
        }
        ramCache.add(readRAMBlock(0x10));
        memoryMap = new MemoryMap(ramCache);
    }

    @Override
    public void refreshMemoryMap() throws Exception {
        for (int i = 1, j = 0; i <= 16; i++, j++) {
            logger.trace("Reading memory page number: {}", i);
            memoryMap.updateElement(j, readRAMBlock(i));
        }
    }

    public byte[] readRAMBlock(int blockNo) throws Exception {
        return readRAM(blockNo, (byte) 64);
    }

    public byte[] readRAM(int blockNo, byte bytesToRead) throws Exception {
        IPPacketPayload payload = new RamRequestPayload(blockNo, bytesToRead);
        return readMemory(payload);
    }

    private byte[] readEepromMemory(int address, byte bytesToRead) throws Exception {
        if (bytesToRead < 1 || bytesToRead > 64) {
            throw new Exception("Invalid bytes to read. Valid values are 1 to 64.");
        }

        IPPacketPayload payload = new EpromRequestPayload(address, bytesToRead);
        return readMemory(payload);
    }

    private byte[] readMemory(IPPacketPayload payload) throws Exception {
        ParadoxIPPacket readEpromIPPacket = new ParadoxIPPacket(payload)
                .setMessageType(HeaderMessageType.SERIAL_PASSTHRU_REQUEST).setUnknown0((byte) 0x14);

        sendPacket(readEpromIPPacket);
        return receivePacket((byte) 0x5);
    }

    private void sendPacket(ParadoxIPPacket packet) throws IOException {
        sendPacket(packet.getBytes());
    }

    private void sendPacket(byte[] packet) throws IOException {
        ParadoxUtil.printPacket("Tx Packet:", packet);
        tx.write(packet);
    }

    private byte[] receivePacket() throws InterruptedException {
        for (int retryCounter = 0; retryCounter < 3; retryCounter++) {
            try {
                byte[] result = new byte[256];
                rx.read(result);
                ParadoxUtil.printPacket("RX:", result);
                return Arrays.copyOfRange(result, 0, result[1] + 16);
            } catch (IOException e) {
                logger.debug("Unable to retrieve data from RX. {}", e.getMessage());
                Thread.sleep(100);
                if (retryCounter < 2) {
                    logger.debug("Attempting one more time");
                }
            }
        }
        return new byte[0];
    }

    /// <summary>
    /// This method reads data from the IP150 module. It can return multiple
    /// responses
    /// e.g. a live event is combined with another response.
    /// </summary>
    /// <param name="networkStream">The open active TCP/IP stream.</param>
    /// <param name="command">A panel command, e.g. 0x5 (read memory)</param>
    /// <returns>An array of an array of the raw bytes received from the TCP/IP
    /// stream.</returns>
    private byte[] receivePacket(byte command) throws IOException, InterruptedException {
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

        logger.error("Failed to receive data for command 0x{0:X}", command);
        return null;
    }

    private List<byte[]> splitResponsePackets(byte[] response) {
        List<byte[]> packets = new ArrayList<byte[]>();
        byte[] responseCopy = Arrays.copyOf(response, response.length);
        try {
            int totalLength = responseCopy.length;
            while (responseCopy.length > 0) {
                if (responseCopy.length < 16 || responseCopy[0] != (byte) 0xAA) {
                    // throw new Exception("No 16 byte header found");
                    logger.debug("No 16 byte header found");
                }

                byte[] header = Arrays.copyOfRange(responseCopy, 0, 16);
                byte messageLength = header[1];

                // Remove the header
                responseCopy = Arrays.copyOfRange(responseCopy, 16, totalLength);

                if (responseCopy.length < messageLength) {
                    throw new Exception("Unexpected end of data");
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
        } catch (Exception ex) {
            logger.error("Exception occurred: {}", ex.getMessage());
        }

        return packets;
    }

    private byte[] generateInitializationRequest(byte[] initializationMessage, byte[] pcPassword) {
        byte[] message7 = new byte[] {
                // Initialization command
                0x00,

                // Module address
                initializationMessage[1],

                // Not used
                0x00, 0x00,

                // Product ID
                initializationMessage[4],

                // Software version
                initializationMessage[5],

                // Software revision
                initializationMessage[6],

                // Software ID
                initializationMessage[7],

                // Module ID
                initializationMessage[8], initializationMessage[9],

                // PC Password
                pcPassword[0], pcPassword[1],

                // Modem speed
                0x0A,

                // Winload type ID
                0x30,

                // User code (for some reason Winload sends user code 021000)
                0x02, 0x10, 0x00,

                // Module serial number
                initializationMessage[17], initializationMessage[18], initializationMessage[19],
                initializationMessage[20],

                // EVO section 3030-3038 data
                initializationMessage[21], initializationMessage[22], initializationMessage[23],
                initializationMessage[24], initializationMessage[25], initializationMessage[26],
                initializationMessage[27], initializationMessage[28], initializationMessage[29],

                // Not used
                0x00, 0x00, 0x00, 0x00,

                // Source ID (0x02 = Winload through IP)
                0x02,

                // Carrier length
                0x00,

                // Checksum
                0x00 };
        return message7;
    }

    private String createString(byte[] payloadResult) throws UnsupportedEncodingException {
        return new String(payloadResult, "US-ASCII");
    }
}
