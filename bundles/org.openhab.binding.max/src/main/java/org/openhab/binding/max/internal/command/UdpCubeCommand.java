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
package org.openhab.binding.max.internal.command;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.max.internal.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UdpCubeCommand} is responsible for sending UDP commands to the MAX!
 * Cube LAN gateway.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class UdpCubeCommand {

    private static final String MAXCUBE_COMMAND_STRING = "eQ3Max*\0";

    private final Logger logger = LoggerFactory.getLogger(UdpCubeCommand.class);

    protected static boolean commandRunning;

    private final UdpCommandType commandType;
    private final String serialNumber;
    private Map<String, String> commandResponse = new HashMap<>();
    private String ipAddress;

    public UdpCubeCommand(UdpCommandType commandType, String serialNumber) {
        this.commandType = commandType;
        if (serialNumber == null || serialNumber.isEmpty()) {
            this.serialNumber = "**********";
        } else {
            this.serialNumber = serialNumber;
        }
    }

    /**
     * UDP command types
     * REBOOT - R Reboot
     * DISCOVERY - I Identify
     * NETWORK - N Get network address
     * URL - h get URL information
     * DEFAULTNET - c get network default info
     */
    public enum UdpCommandType {
        REBOOT,
        DISCOVERY,
        NETWORK,
        URL,
        DEFAULTNET
    }

    /**
     * Executes the composed {@link UdpCubeCommand} command
     */
    public synchronized boolean send() {
        String commandString;
        if (commandType.equals(UdpCommandType.REBOOT)) {
            commandString = MAXCUBE_COMMAND_STRING + serialNumber + "R";
        } else if (commandType.equals(UdpCommandType.DISCOVERY)) {
            commandString = MAXCUBE_COMMAND_STRING + serialNumber + "I";
        } else if (commandType.equals(UdpCommandType.NETWORK)) {
            commandString = MAXCUBE_COMMAND_STRING + serialNumber + "N";
        } else if (commandType.equals(UdpCommandType.URL)) {
            commandString = MAXCUBE_COMMAND_STRING + serialNumber + "h";
        } else if (commandType.equals(UdpCommandType.DEFAULTNET)) {
            commandString = MAXCUBE_COMMAND_STRING + serialNumber + "c";
        } else {
            logger.debug("Unknown Command {}", commandType);
            return false;
        }
        commandResponse.clear();
        logger.debug("Send {} command to MAX! Cube {}", commandType, serialNumber);
        sendUdpCommand(commandString, ipAddress);
        logger.trace("Done sending command.");
        receiveUdpCommandResponse();
        logger.debug("Done receiving response.");
        return true;
    }

    private void receiveUdpCommandResponse() {
        commandRunning = true;

        try (DatagramSocket bcReceipt = new DatagramSocket(23272)) {
            bcReceipt.setReuseAddress(true);
            bcReceipt.setSoTimeout(5000);

            while (commandRunning) {
                // Wait for a response
                byte[] recvBuf = new byte[1500];
                DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
                bcReceipt.receive(receivePacket);

                // We have a response
                String message = new String(receivePacket.getData(), receivePacket.getOffset(),
                        receivePacket.getLength(), StandardCharsets.UTF_8);
                if (logger.isDebugEnabled()) {
                    logger.debug("Broadcast response from {} : {} '{}'", receivePacket.getAddress(), message.length(),
                            message);
                }

                // Check if the message is correct
                if (message.startsWith("eQ3Max") && !message.equals(MAXCUBE_COMMAND_STRING)) {
                    commandResponse.put("maxCubeIP", receivePacket.getAddress().getHostAddress().toString());
                    commandResponse.put("maxCubeState", message.substring(0, 8));
                    commandResponse.put("serialNumber", message.substring(8, 18));
                    commandResponse.put("msgValidid", message.substring(18, 19));
                    String requestType = message.substring(19, 20);
                    commandResponse.put("requestType", requestType);

                    if (requestType.equals("I")) {
                        commandResponse.put("rfAddress",
                                Utils.getHex(message.substring(21, 24).getBytes(StandardCharsets.UTF_8))
                                        .replace(" ", "").toLowerCase());
                        commandResponse.put("firmwareVersion", Utils
                                .getHex(message.substring(24, 26).getBytes(StandardCharsets.UTF_8)).replace(" ", "."));
                    } else {
                        // TODO: Further parsing of the other message types
                        commandResponse.put("messageResponse",
                                Utils.getHex(message.substring(20).getBytes(StandardCharsets.UTF_8)));
                    }

                    commandRunning = false;
                    if (logger.isDebugEnabled()) {
                        final StringBuilder builder = new StringBuilder();
                        for (final Map.Entry<String, String> entry : commandResponse.entrySet()) {
                            builder.append(String.format("%s: %s\n", entry.getKey(), entry.getValue()));
                        }
                        logger.debug("MAX! UDP response {}", builder);
                    }
                }
            }
        } catch (SocketTimeoutException e) {
            logger.trace("No further response");
            commandRunning = false;
        } catch (IOException e) {
            logger.debug("IO error during MAX! Cube response: {}", e.getMessage());
            commandRunning = false;
        }
    }

    /**
     * Send broadcast message over all active interfaces
     *
     * @param commandString string to be used for the discovery
     * @param ipAddress IP address of the MAX! Cube
     *
     */
    private void sendUdpCommand(String commandString, String ipAddress) {
        DatagramSocket bcSend = null;
        try {
            bcSend = new DatagramSocket();
            bcSend.setBroadcast(true);

            byte[] sendData = commandString.getBytes(StandardCharsets.UTF_8);

            // Broadcast the message over all the network interfaces
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {

                    InetAddress[] broadcast = new InetAddress[3];
                    if (ipAddress != null && !ipAddress.isEmpty()) {
                        broadcast[0] = InetAddress.getByName(ipAddress);
                    } else {
                        broadcast[0] = InetAddress.getByName("224.0.0.1");
                        broadcast[1] = InetAddress.getByName("255.255.255.255");
                        broadcast[2] = interfaceAddress.getBroadcast();
                    }
                    for (InetAddress bc : broadcast) {
                        // Send the broadcast package!
                        if (bc != null) {
                            try {
                                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, bc, 23272);
                                bcSend.send(sendPacket);
                            } catch (IOException e) {
                                logger.debug("IO error during MAX! Cube UDP command sending: {}", e.getMessage());
                            } catch (Exception e) {
                                logger.debug("{}", e.getMessage(), e);
                            }
                            logger.trace("Request packet sent to: {} Interface: {}", bc.getHostAddress(),
                                    networkInterface.getDisplayName());
                        }
                    }
                }
            }
            logger.trace("Done looping over all network interfaces. Now waiting for a reply!");

        } catch (IOException e) {
            logger.debug("IO error during MAX! Cube UDP command sending: {}", e.getMessage());
        } finally {
            try {
                if (bcSend != null) {
                    bcSend.close();
                }
            } catch (Exception e) {
                // Ignore
            }
        }

    }

    /**
     * Set the IP address to send the command to. The command will be send to the address and broadcasted over all
     * active interfaces
     *
     * @param ipAddress IP address of the MAX! Cube
     *
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * Response of the MAX! Cube on the
     *
     */
    public Map<String, String> getCommandResponse() {
        return commandResponse;
    }

}
