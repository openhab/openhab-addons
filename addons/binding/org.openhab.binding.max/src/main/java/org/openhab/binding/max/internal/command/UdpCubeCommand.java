/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.internal.command;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.util.Enumeration;
import java.util.HashMap;

import org.openhab.binding.max.internal.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/**
 * The {@link UdpCubeCommand} is responsible for sending UDP commands to the MAX!
 * Cube LAN gateway.
 *
 * @author Marcel Verpaalen - Initial contribution
 * @since 2.0
 *
 */
public class UdpCubeCommand {

    static final String MAXCUBE_COMMAND_STRING = "eQ3Max*\0";

    private final Logger logger = LoggerFactory.getLogger(UdpCubeCommand.class);

    static boolean commandRunning = false;

    private udpCommandType commandType;
    private String serialNumber;
    private HashMap<String, String> commandResponse = new HashMap<>();
    private String ipAddress = null;

    public UdpCubeCommand(udpCommandType commandType, String serialNumber) {
        this.commandType = commandType;
        this.serialNumber = serialNumber;
    }

    /**
     * UDP command types
     * RESET - R Reset
     * DISCOVERY - I Identify
     * NETWORK - N Get network address
     * URL - h get URL information
     * DEFAULTNET - c get network default info
     */
    public enum udpCommandType {
        RESET,
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
        if (serialNumber == null || serialNumber.isEmpty()) {
            serialNumber = "**********";
        }
        if (commandType.equals(udpCommandType.RESET)) {
            commandString = MAXCUBE_COMMAND_STRING + serialNumber + "R";
        } else if (commandType.equals(udpCommandType.DISCOVERY)) {
            commandString = MAXCUBE_COMMAND_STRING + serialNumber + "I";
        } else if (commandType.equals(udpCommandType.NETWORK)) {
            commandString = MAXCUBE_COMMAND_STRING + serialNumber + "N";
        } else if (commandType.equals(udpCommandType.URL)) {
            commandString = MAXCUBE_COMMAND_STRING + serialNumber + "h";
        } else if (commandType.equals(udpCommandType.DEFAULTNET)) {
            commandString = MAXCUBE_COMMAND_STRING + serialNumber + "c";
        } else {
            logger.info("Unknown Command {}", commandType.toString());
            return false;
        }
        commandResponse.clear();
        logger.debug("Send {} command to MAX! Cube {}", commandType.toString(), serialNumber);
        sendUdpCommand(commandString, ipAddress);
        logger.trace("Done sending command.");
        receiveUdpCommandResponse();
        logger.debug("Done receiving response.");
        return true;
    }

    private void receiveUdpCommandResponse() {

        DatagramSocket bcReceipt = null;

        try {
            commandRunning = true;
            bcReceipt = new DatagramSocket(23272);
            bcReceipt.setReuseAddress(true);
            bcReceipt.setSoTimeout(5000);

            while (commandRunning) {
                // Wait for a response
                byte[] recvBuf = new byte[1500];
                DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
                bcReceipt.receive(receivePacket);

                // We have a response
                String message = new String(receivePacket.getData(), receivePacket.getOffset(),
                        receivePacket.getLength());
                logger.trace("Broadcast response from {} : {} '{}'", receivePacket.getAddress(), message.length(),
                        message);

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
                                Utils.getHex(message.substring(21, 24).getBytes()).replace(" ", "").toLowerCase());
                        commandResponse.put("firmwareVersion",
                                Utils.getHex(message.substring(24, 26).getBytes()).replace(" ", "."));
                    } else {
                        // TODO: Further parsing of the other message types
                        commandResponse.put("messageResponse", Utils.getHex(message.substring(24).getBytes()));
                    }

                    commandRunning = false;
                    logger.debug("MAX! UDP response");
                    for (String key : commandResponse.keySet()) {
                        logger.debug("{}:{}{}", key, Strings.repeat(" ", 25 - key.length()), commandResponse.get(key));
                    }
                }
            }
        } catch (SocketTimeoutException e) {
            logger.trace("No further response");
            commandRunning = false;
        } catch (IOException e) {
            logger.debug("IO error during MAX! Cube response: {}", e.getMessage());
            commandRunning = false;
        } finally {
            // Close the port!
            try {
                if (bcReceipt != null) {
                    bcReceipt.close();
                }
            } catch (Exception e) {
                logger.debug("{}", e.getMessage());
            }
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

            byte[] sendData = commandString.getBytes();

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
                                logger.info("{}", e.getMessage(), e);
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
    public HashMap<String, String> getCommandResponse() {
        return commandResponse;
    }

}
