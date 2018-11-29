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
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.openhab.binding.paradoxalarm.internal.communication.messages.HeaderCommand;
import org.openhab.binding.paradoxalarm.internal.communication.messages.HeaderMessageType;
import org.openhab.binding.paradoxalarm.internal.communication.messages.IpMessagesConstants;
import org.openhab.binding.paradoxalarm.internal.communication.messages.ParadoxIPPacket;
import org.openhab.binding.paradoxalarm.internal.util.ParadoxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GenericCommunicator} Used for the common communication logic for all types of panels. Future use to
 * autodetect panels and provide information to factory to create the proper type of commumnicator.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public class GenericCommunicator implements IParadoxGenericCommunicator {

    protected static Logger logger = LoggerFactory.getLogger(GenericCommunicator.class);
    protected Socket socket;
    protected DataOutputStream tx;
    protected DataInputStream rx;
    protected final byte[] pcPassword;
    protected byte[] panelInfoBytes;
    protected String password;

    public GenericCommunicator(String ipAddress, int tcpPort, String ip150Password, String pcPassword)
            throws UnknownHostException, IOException, InterruptedException {
        socket = new Socket(ipAddress, tcpPort);
        socket.setSoTimeout(4000);
        tx = new DataOutputStream(socket.getOutputStream());
        rx = new DataInputStream(socket.getInputStream());
        password = ip150Password;
        this.pcPassword = ParadoxUtil.stringToBCD(pcPassword);
        loginSequence();
    }

    @Override
    public void close() throws IOException {
        logoutSequence();
        tx.close();
        rx.close();
        socket.close();
    }

    @Override
    public void loginSequence() throws IOException, InterruptedException {
        logger.debug("Login sequence started");
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
        byte[] receivedPacket = receivePacket();
        if (receivedPacket != null && receivedPacket.length >= 53) {
            panelInfoBytes = Arrays.copyOfRange(receivedPacket, 16, 53);
        }

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

    @Override
    public void logoutSequence() throws IOException {
        logger.debug("Logout sequence started");
        byte[] logoutMessage = new byte[] { 0x00, 0x07, 0x05, 0x00, 0x00, 0x00, 0x00 };
        ParadoxIPPacket logoutPacket = new ParadoxIPPacket(logoutMessage, true)
                .setMessageType(HeaderMessageType.SERIAL_PASSTHRU_REQUEST).setUnknown0((byte) 0x14);
        sendPacket(logoutPacket);
    }

    protected void sendPacket(ParadoxIPPacket packet) throws IOException {
        sendPacket(packet.getBytes());
    }

    private void sendPacket(byte[] packet) throws IOException {
        ParadoxUtil.printPacket("Tx Packet:", packet);
        tx.write(packet);
    }

    protected byte[] receivePacket() throws InterruptedException {
        for (int retryCounter = 0; retryCounter < 3; retryCounter++) {
            try {
                byte[] result = new byte[256];
                rx.read(result);
                ParadoxUtil.printPacket("RX:", result);
                return Arrays.copyOfRange(result, 0, result[1] + 16);
            } catch (IOException e) {
                logger.error("Unable to retrieve data from RX. {}", e.getMessage());
                Thread.sleep(200);
                if (retryCounter < 2) {
                    logger.info("Attempting one more time");
                }
            }
        }
        return new byte[0];
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

    @Override
    public byte[] getPanelInfoBytes() {
        return panelInfoBytes;
    }

}