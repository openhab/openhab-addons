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
package org.openhab.binding.silvercrestwifisocket.internal.utils;

import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.openhab.binding.silvercrestwifisocket.internal.entities.SilvercrestWifiSocketRequest;
import org.openhab.binding.silvercrestwifisocket.internal.entities.SilvercrestWifiSocketResponse;
import org.openhab.binding.silvercrestwifisocket.internal.enums.SilvercrestWifiSocketResponseType;
import org.openhab.binding.silvercrestwifisocket.internal.enums.SilvercrestWifiSocketVendor;
import org.openhab.binding.silvercrestwifisocket.internal.exceptions.NotOneResponsePacketException;
import org.openhab.binding.silvercrestwifisocket.internal.exceptions.PacketIntegrityErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transforms the the received datagram packet to one
 *
 * @author Jaime Vaz - Initial contribution
 * @author Christian Heimerl - for integration of EasyHome
 *
 */
public class WifiSocketPacketConverter {

    private final Logger logger = LoggerFactory.getLogger(WifiSocketPacketConverter.class);

    private static final String REQUEST_PREFIX = "01";
    private static final String RESPONSE_PREFIX = "0142";
    private static final String LOCK_STATUS = "40";
    /* encryptDataLength */
    private static final String ENCRYPT_PREFIX = "00";
    private static final String PACKET_NUMBER = "FFFF";
    private static final String DEVICE_TYPE = "11";

    private static final String ENCRIPTION_KEY = "0123456789abcdef";

    private Cipher silvercrestEncryptCipher;
    private Cipher silvercrestDecryptCipher;

    /**
     * START_OF_RECEIVED_PACKET.
     * STX - pkt nbr - CompanyCode - device - authCode
     *
     * 00 -- 0029 -- C1 -- 11 -- 7150 (SilverCrest)
     * 00 -- 0029 -- C2 -- 11 -- 92DD (EasyHome)
     */
    private static final String REGEX_START_OF_RECEIVED_PACKET = "00([A-F0-9]{4})(?:C21192DD|C1117150)";
    private static final String REGEX_HEXADECIMAL_PAIRS = "([A-F0-9]{2})*";

    private static final String REGEX_START_OF_RECEIVED_PACKET_SEARCH_MAC_ADDRESS = REGEX_START_OF_RECEIVED_PACKET
            + "23" + REGEX_HEXADECIMAL_PAIRS;
    private static final String REGEX_START_OF_RECEIVED_PACKET_HEART_BEAT = REGEX_START_OF_RECEIVED_PACKET + "61"
            + REGEX_HEXADECIMAL_PAIRS;
    private static final String REGEX_START_OF_RECEIVED_PACKET_CMD_GPIO_EVENT = REGEX_START_OF_RECEIVED_PACKET + "06"
            + REGEX_HEXADECIMAL_PAIRS;
    private static final String REGEX_START_OF_RECEIVED_PACKET_QUERY_STATUS = REGEX_START_OF_RECEIVED_PACKET + "02"
            + REGEX_HEXADECIMAL_PAIRS;
    private static final String REGEX_START_OF_RECEIVED_PACKET_RESPONSE_GPIO_CHANGE_REQUEST = REGEX_START_OF_RECEIVED_PACKET
            + "01" + REGEX_HEXADECIMAL_PAIRS;

    /**
     * Default constructor of the packet converter.
     */
    public WifiSocketPacketConverter() {
        // init cipher
        byte[] encriptionKeyBytes;
        try {
            encriptionKeyBytes = ENCRIPTION_KEY.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec secretKey = new SecretKeySpec(encriptionKeyBytes, "AES");
            IvParameterSpec ivKey = new IvParameterSpec(encriptionKeyBytes);

            this.silvercrestEncryptCipher = Cipher.getInstance("AES/CBC/NoPadding");
            this.silvercrestEncryptCipher.init(Cipher.ENCRYPT_MODE, secretKey, ivKey);

            this.silvercrestDecryptCipher = Cipher.getInstance("AES/CBC/NoPadding");
            this.silvercrestDecryptCipher.init(Cipher.DECRYPT_MODE, secretKey, ivKey);
        } catch (Exception exception) {
            logger.debug(
                    "Failure on WifiSocketPacketConverter creation. There was a problem creating ciphers. Error: {}",
                    exception.getLocalizedMessage());
        }
    }

    /**
     * Method that transforms one {@link SilvercrestWifiSocketRequest} to one byte array to be ready to be transmitted.
     *
     * @param requestPacket the {@link SilvercrestWifiSocketRequest}.
     * @return the byte array with the message.
     */
    public byte[] transformToByteMessage(final SilvercrestWifiSocketRequest requestPacket) {
        byte[] requestDatagram = null;
        String fullCommand = ENCRYPT_PREFIX + PACKET_NUMBER + requestPacket.getVendor().getCompanyCode() + DEVICE_TYPE
                + requestPacket.getVendor().getAuthenticationCode()
                + String.format(requestPacket.getType().getCommand(), requestPacket.getMacAddress());

        byte[] inputByte = hexStringToByteArray(fullCommand);
        byte[] bEncrypted;
        try {
            bEncrypted = this.silvercrestEncryptCipher.doFinal(inputByte);
            int encryptDataLength = bEncrypted.length;

            logger.trace("Encrypted data={{}}", byteArrayToHexString(inputByte));
            logger.trace("Decrypted data={{}}", byteArrayToHexString(bEncrypted));
            String cryptedCommand = byteArrayToHexString(bEncrypted);

            String packetString = REQUEST_PREFIX + LOCK_STATUS + requestPacket.getMacAddress()
                    + Integer.toHexString(encryptDataLength) + cryptedCommand;

            logger.trace("Request Packet: {}", packetString);
            logger.trace("Request packet decrypted data: [{}] with lenght: {}", fullCommand, fullCommand.length());
            requestDatagram = hexStringToByteArray(packetString);
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            logger.debug("Failure processing the build of the request packet for mac '{}' and type '{}'",
                    requestPacket.getMacAddress(), requestPacket.getType());
        }
        return requestDatagram;
    }

    /**
     * Decrypts one response {@link DatagramPacket}.
     *
     * @param packet the {@link DatagramPacket}
     * @return the {@link SilvercrestWifiSocketResponse} is successfully decrypted.
     * @throws PacketIntegrityErrorException if the message has some integrity error.
     * @throws NotOneResponsePacketException if the message received is not one response packet.
     */
    public SilvercrestWifiSocketResponse decryptResponsePacket(final DatagramPacket packet)
            throws PacketIntegrityErrorException, NotOneResponsePacketException {
        SilvercrestWifiSocketResponse responsePacket = this.decryptResponsePacket(
                WifiSocketPacketConverter.byteArrayToHexString(packet.getData(), packet.getLength()));

        responsePacket.setHostAddress(packet.getAddress().getHostAddress());

        return responsePacket;
    }

    /**
     * STX - pkt nbr - CompanyCode - device - authCode
     *
     * 00 -- 0029 -- C1 -- 11 -- 7150 (Silvercrest)
     * 00 -- 0029 -- C2 -- 11 -- 92DD (EasyHome)
     *
     * @param hexPacket the hex packet to convert
     * @return the converted response.
     * @throws PacketIntegrityErrorException the packet passed is not recognized.
     * @throws NotOneResponsePacketException the packet passed is not one response.
     */
    private SilvercrestWifiSocketResponse decryptResponsePacket(final String hexPacket)
            throws PacketIntegrityErrorException, NotOneResponsePacketException {
        if (!Pattern.matches(RESPONSE_PREFIX + REGEX_HEXADECIMAL_PAIRS, hexPacket)) {
            logger.trace("The packet received is not one response! \nPacket:[{}]", hexPacket);
            throw new NotOneResponsePacketException("The packet received is not one response.");
        }

        logger.trace("Response packet: {}", hexPacket);
        String macAddress = hexPacket.substring(4, 16);
        logger.trace("The mac address of the sender of the packet is: {}", macAddress);
        String decryptedData = this.decrypt(hexPacket.substring(18, hexPacket.length()));

        logger.trace("Response packet decrypted data: [{}] with lenght: {}", decryptedData, decryptedData.length());

        SilvercrestWifiSocketResponseType responseType;
        // check packet integrity
        if (Pattern.matches(REGEX_START_OF_RECEIVED_PACKET_SEARCH_MAC_ADDRESS, decryptedData)) {
            responseType = SilvercrestWifiSocketResponseType.DISCOVERY;
            logger.trace("Received answer of mac address search! lenght:{}", decryptedData.length());
        } else if (Pattern.matches(REGEX_START_OF_RECEIVED_PACKET_HEART_BEAT, decryptedData)) {
            responseType = SilvercrestWifiSocketResponseType.ACK;
            logger.trace("Received heart beat!");
        } else if (Pattern.matches(REGEX_START_OF_RECEIVED_PACKET_CMD_GPIO_EVENT, decryptedData)) {
            logger.trace("Received gpio event!");
            String status = decryptedData.substring(20, 22);
            responseType = "FF".equalsIgnoreCase(status) ? SilvercrestWifiSocketResponseType.ON
                    : SilvercrestWifiSocketResponseType.OFF;
            logger.trace("Socket status: {}", responseType);
        } else if (Pattern.matches(REGEX_START_OF_RECEIVED_PACKET_RESPONSE_GPIO_CHANGE_REQUEST, decryptedData)) {
            logger.trace("Received response from a gpio change request!");
            String status = decryptedData.substring(20, 22);
            responseType = "FF".equalsIgnoreCase(status) ? SilvercrestWifiSocketResponseType.ON
                    : SilvercrestWifiSocketResponseType.OFF;
            logger.trace("Socket status: {}", responseType);
        } else if (Pattern.matches(REGEX_START_OF_RECEIVED_PACKET_QUERY_STATUS, decryptedData)) {
            logger.trace("Received response from status query!");
            String status = decryptedData.substring(20, 22);
            responseType = "FF".equalsIgnoreCase(status) ? SilvercrestWifiSocketResponseType.ON
                    : SilvercrestWifiSocketResponseType.OFF;
            logger.trace("Socket status: {}", responseType);
        } else {
            throw new PacketIntegrityErrorException("The packet decrypted is with wrong format. \nPacket:[" + hexPacket
                    + "]  \nDecryptedPacket:[" + decryptedData + "]");
        }

        SilvercrestWifiSocketVendor vendor = SilvercrestWifiSocketVendor.fromCode(decryptedData.substring(6, 8));
        if (vendor == null) {
            throw new PacketIntegrityErrorException("Could not extract vendor from the decrypted packet. \nPacket:["
                    + hexPacket + "]  \nDecryptedPacket:[" + decryptedData + "]");
        }

        logger.trace("Decrypt success. Packet is from socket with mac address [{}] and type is [{}] and vendor is [{}]",
                macAddress, responseType, vendor);
        return new SilvercrestWifiSocketResponse(macAddress, responseType, vendor);
    }

    /**
     * Decrypts one received message with the correct cypher.
     *
     * @param inputData the cyphered message
     * @return the decrypted message.
     */
    private String decrypt(final String inputData) {
        byte[] inputByte = hexStringToByteArray(inputData);
        byte[] bDecrypted;
        try {
            bDecrypted = this.silvercrestDecryptCipher.doFinal(inputByte);
            logger.trace("Encrypted data={{}}", byteArrayToHexString(inputByte));
            logger.trace("Decrypted data={{}}", byteArrayToHexString(bDecrypted));
            return byteArrayToHexString(bDecrypted);
        } catch (Exception e) {
            logger.trace("Problem decrypting the input data. Bad reception?");
        }
        return null;
    }

    // String/Array/Hex manipulation
    /**
     * Converts one hexadecimal string to one byte array.
     *
     * @param str the string to convert.
     * @return the byte array.
     */
    private static byte[] hexStringToByteArray(final String str) {
        byte[] b = new byte[str.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(str.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }

    /**
     * Converts one full byte array to one hexadecimal string.
     *
     * @param array the byte array to convert.
     * @return the hexadecimal string.
     */
    private static String byteArrayToHexString(final byte[] array) {
        return byteArrayToHexString(array, array.length);
    }

    /**
     * Converts one partial byte array to one hexadecimal string.
     *
     * @param array the byte array to convert.
     * @param length the length to convert.
     * @return the hexadecimal string.
     */
    private static String byteArrayToHexString(final byte[] array, final int length) {
        if ((array == null) || (array.length == 0)) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        String hex = "";

        for (int i = 0; i < length; i++) {
            hex = Integer.toHexString(0xFF & array[i]).toUpperCase();
            if (hex.length() < 2) {
                hex = "0" + hex;
            }
            builder.append(hex);
        }
        return builder.toString();
    }
}
