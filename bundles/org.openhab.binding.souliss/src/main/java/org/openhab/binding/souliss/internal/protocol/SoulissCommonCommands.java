/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.souliss.internal.protocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.souliss.SoulissBindingUDPConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provide to construct MaCaco and UDP frame
 *
 * @author Alessandro Del Pex
 * @author Tonino Fazio
 * @since 1.7.0
 */
@NonNullByDefault
public class SoulissCommonCommands {

    private final Logger logger = LoggerFactory.getLogger(SoulissCommonCommands.class);

    public final void sendFORCEFrame(@Nullable DatagramSocket datagramSocket,
            @Nullable String soulissNodeIPAddressOnLAN, byte nodeIndex, byte userIndex, int IDNode, int slot,
            byte shortCommand) {
        sendFORCEFrame(datagramSocket, soulissNodeIPAddressOnLAN, nodeIndex, userIndex, IDNode, slot, shortCommand,
                null, null, null);
    }

    /*
     * used for set dimmer value. It set command at first byte and dimmerVal to
     * second byte
     */
    public final void sendFORCEFrame(DatagramSocket datagramSocket, String soulissNodeIPAddressOnLAN, byte nodeIndex,
            byte userIndex, int IDNode, int slot, byte shortCommand, byte lDimmer) {
        sendFORCEFrame(datagramSocket, soulissNodeIPAddressOnLAN, nodeIndex, userIndex, IDNode, slot, shortCommand,
                lDimmer, null, null);
    }

    /*
     * send force frame with command and RGB value
     */
    public final void sendFORCEFrame(@Nullable DatagramSocket datagramSocket,
            @Nullable String soulissNodeIPAddressOnLAN, byte nodeIndex, byte userIndex, int IDNode, int slot,
            byte shortCommand, @Nullable Byte byte1, @Nullable Byte byte2, @Nullable Byte byte3) {
        ArrayList<Byte> macacoFrame = new ArrayList<Byte>();
        macacoFrame.add(SoulissBindingUDPConstants.SOULISS_UDP_FUNCTION_FORCE);

        // PUTIN, STARTOFFEST, NUMBEROF
        macacoFrame.add((byte) 0x0);// PUTIN
        macacoFrame.add((byte) 0x0);// PUTIN

        macacoFrame.add((byte) (IDNode));// Start Offset

        if (byte1 == null && byte2 == null && byte3 == null) {
            macacoFrame.add((byte) ((byte) slot + 1)); // Number Of
        } else if (byte2 == null && byte3 == null) {
            macacoFrame.add((byte) ((byte) slot + 2)); // Number Of byte of
                                                       // payload= command +
                                                       // set byte
        } else {
            macacoFrame.add((byte) ((byte) slot + 4)); // Number Of byte of
                                                       // payload= OnOFF + Red
                                                       // + Green + Blu
        }

        for (int i = 0; i <= slot - 1; i++) {
            macacoFrame.add((byte) 00); // pongo a zero i byte precedenti lo
                                        // slot da modificare
        }
        macacoFrame.add(shortCommand);// PAYLOAD

        if (byte1 != null && byte2 != null && byte3 != null) {
            macacoFrame.add(byte1.byteValue());// PAYLOAD RED
            macacoFrame.add(byte2.byteValue());// PAYLOAD GREEN
            macacoFrame.add(byte3.byteValue());// PAYLOAD BLUE
        } else if (byte1 != null) {
            macacoFrame.add(byte1.byteValue());// PAYLOAD DIMMER
        }

        logger.debug("sendFORCEFrame - {}, soulissNodeIPAddressOnLAN: {}", macacoToString(macacoFrame),
                soulissNodeIPAddressOnLAN);
        send(datagramSocket, macacoFrame, soulissNodeIPAddressOnLAN, nodeIndex, userIndex);
    }

    /*
     * T61 send framte to push the setpoint value
     */

    public final void sendFORCEFrameT61SetPoint(@Nullable DatagramSocket datagramSocket,
            @Nullable String soulissNodeIPAddressOnLAN, byte nodeIndex, byte userIndex, int IDNode, int slot,
            Byte byte1, Byte byte2) {
        ArrayList<Byte> macacoFrame = new ArrayList<Byte>();
        macacoFrame.add(SoulissBindingUDPConstants.SOULISS_UDP_FUNCTION_FORCE);

        // PUTIN, STARTOFFEST, NUMBEROF
        macacoFrame.add((byte) 0x0);// PUTIN
        macacoFrame.add((byte) 0x0);// PUTIN

        macacoFrame.add((byte) (IDNode));// Start Offset
        macacoFrame.add((byte) ((byte) slot + 2)); // Number Of byte of payload= command + set byte

        for (int i = 0; i <= slot - 1; i++) {
            macacoFrame.add((byte) 00); // pongo a zero i byte precedenti lo
                                        // slot da modificare
        }
        // PAYLOAD
        macacoFrame.add(byte1.byteValue());// first byte Setpoint Value
        macacoFrame.add(byte2.byteValue());// second byte Setpoint Value

        logger.debug("sendFORCEFrame - {}, soulissNodeIPAddressOnLAN: {}", macacoToString(macacoFrame),
                soulissNodeIPAddressOnLAN);

        send(datagramSocket, macacoFrame, soulissNodeIPAddressOnLAN, nodeIndex, userIndex);
    }

    /*
     * T31 send force frame with command and setpoint float
     */
    public final void sendFORCEFrameT31SetPoint(@Nullable DatagramSocket datagramSocket,
            @Nullable String soulissNodeIPAddressOnLAN, byte nodeIndex, byte userIndex, int IDNode, int slot,
            byte shortCommand, Byte byte1, Byte byte2) {
        ArrayList<Byte> macacoFrame = new ArrayList<Byte>();
        macacoFrame.add(SoulissBindingUDPConstants.SOULISS_UDP_FUNCTION_FORCE);

        // PUTIN, STARTOFFEST, NUMBEROF
        macacoFrame.add((byte) 0x0);// PUTIN
        macacoFrame.add((byte) 0x0);// PUTIN

        macacoFrame.add((byte) (IDNode));// Start Offset
        macacoFrame.add((byte) ((byte) slot + 5)); // Number Of byte of payload= command + set byte

        for (int i = 0; i <= slot - 1; i++) {
            macacoFrame.add((byte) 00); // pongo a zero i byte precedenti lo
                                        // slot da modificare
        }
        macacoFrame.add(shortCommand);// PAYLOAD

        macacoFrame.add((byte) 0x0);// Empty - Temperature Measured Value
        macacoFrame.add((byte) 0x0);// Empty - Temperature Measured Value
        macacoFrame.add(byte1.byteValue());// Temperature Setpoint Value
        macacoFrame.add(byte2.byteValue());// Temperature Setpoint Value

        logger.debug("sendFORCEFrame - {}, soulissNodeIPAddressOnLAN: {}", macacoToString(macacoFrame),
                soulissNodeIPAddressOnLAN);
        send(datagramSocket, macacoFrame, soulissNodeIPAddressOnLAN, nodeIndex, userIndex);
    }

    public final void sendDBStructFrame(@Nullable DatagramSocket socket, @Nullable String soulissNodeIPAddressOnLAN,
            byte nodeIndex, byte userIndex) {
        ArrayList<Byte> macacoFrame = new ArrayList<Byte>();
        macacoFrame.add((byte) SoulissBindingUDPConstants.SOULISS_UDP_FUNCTION_DBSTRUCT_REQ);
        macacoFrame.add((byte) 0x0);// PUTIN
        macacoFrame.add((byte) 0x0);// PUTIN
        macacoFrame.add((byte) 0x0);// Start Offset
        macacoFrame.add((byte) 0x0); // Number Of

        logger.debug("sendDBStructFrame - {}, soulissNodeIPAddressOnLAN: {}", macacoToString(macacoFrame),
                soulissNodeIPAddressOnLAN);
        send(socket, macacoFrame, soulissNodeIPAddressOnLAN, nodeIndex, userIndex);

        // Note:
        // Structure of DBStructFrame:
        // nodes = mac.get(5);
        // maxnodes = mac.get(6);
        // maxTypicalXnode = mac.get(7);
        // maxrequests = mac.get(8);
        // MaCacoIN_s = mac.get(9);
        // MaCacoTyp_s = mac.get(10);
        // MaCacoOUT_s = mac.get(11);
    }

    /*
     * send UDP frame
     */
    private final void send(@Nullable DatagramSocket socket, ArrayList<Byte> macacoFrame,
            @Nullable String sSoulissNodeIPAddressOnLAN, byte nodeIndex, byte userIndex) {
        ArrayList<Byte> buf = buildVNetFrame(macacoFrame, sSoulissNodeIPAddressOnLAN, userIndex, nodeIndex);
        byte[] merd = toByteArray(buf);

        InetAddress serverAddr;
        try {
            serverAddr = InetAddress.getByName(sSoulissNodeIPAddressOnLAN);
            DatagramPacket packet = new DatagramPacket(merd, merd.length, serverAddr,
                    SoulissBindingUDPConstants.SOULISS_GATEWAY_DEFAULT_PORT);
            SoulissBindingSendDispatcherJob.put(socket, packet, this.logger);
            // socket.send(packet);
        } catch (IOException e) {
            logger.error("Error: {} ", e.getMessage());
            // logger.error(e.getMessage());
        }
    }

    /*
     * send UDP frame
     */
    private final void sendBroadcastNow(DatagramSocket socket, ArrayList<Byte> macacoFrame) {
        byte iUserIndex = SoulissBindingNetworkParameters.DEFAULT_USER_INDEX;
        byte iNodeIndex = SoulissBindingNetworkParameters.DEFAULT_NODE_INDEX;

        // Broadcast the message over all the network interfaces
        Enumeration<NetworkInterface> interfaces;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress[] broadcast = new InetAddress[3];
                    broadcast[0] = InetAddress.getByName("224.0.0.1");
                    broadcast[1] = InetAddress.getByName("255.255.255.255");
                    broadcast[2] = interfaceAddress.getBroadcast();
                    for (InetAddress bc : broadcast) {
                        // Send the broadcast package!
                        if (bc != null) {
                            try {
                                ArrayList<Byte> buf = buildVNetFrame(macacoFrame, "255.255.255.255", iUserIndex,
                                        iNodeIndex);
                                byte[] merd = toByteArray(buf);
                                DatagramPacket packet = new DatagramPacket(merd, merd.length, bc,
                                        SoulissBindingUDPConstants.SOULISS_GATEWAY_DEFAULT_PORT);
                                socket.send(packet);

                            } catch (IOException e) {
                                logger.debug("IO error: {}", e.getMessage());
                            } catch (Exception e) {
                                logger.debug("{}", e.getMessage(), e);
                            }
                            logger.debug("Request packet sent to: {} Interface: {}", bc.getHostAddress(),
                                    networkInterface.getDisplayName());
                        }
                    }
                }
            }
        } catch (SocketException e) {
            logger.error("{}", e.getMessage());
        } catch (UnknownHostException e) {
            logger.error("{}", e.getMessage());
        }
    }

    /*
     * Build VNet Frame
     */
    private final ArrayList<Byte> buildVNetFrame(ArrayList<Byte> MACACOframe2, @Nullable String soulissNodeIPAddress,
            byte iUserIndex, byte iNodeIndex) {
        ArrayList<Byte> frame = new ArrayList<Byte>();
        InetAddress ip;
        try {
            ip = InetAddress.getByName(soulissNodeIPAddress);
        } catch (UnknownHostException e) {
            logger.error("{}", e.getMessage());
            return frame;
        }
        byte[] dude = ip.getAddress();

        frame.add((byte) 23);// Port

        frame.add((byte) (dude[3] & 0xFF));// es 192.168.1.XX BOARD

        // n broadcast : La comunicazione avviene utilizzando l'indirizzo IP
        // 255.255.255.255 a cui associare l'indirizzo vNet 0xFFFF.
        frame.add(soulissNodeIPAddress.compareTo(SoulissBindingUDPConstants.BROADCASTADDR) == 0 ? dude[2] : 0);
        // 192.168.XX.0

        frame.add(iNodeIndex); // NODE INDEX - source vNet address User Interface
        frame.add(iUserIndex);// USER INDEX - source vNet address User Interface

        // aggiunge in testa il calcolo
        frame.add(0, (byte) (frame.size() + MACACOframe2.size() + 1)); // Length
        frame.add(0, (byte) (frame.size() + MACACOframe2.size() + 1));// Length Check 2

        frame.addAll(MACACOframe2);
        return frame;
    }

    /**
     * Builds old-school byte array
     *
     * @param buf
     * @return
     */
    private static byte[] toByteArray(ArrayList<Byte> buf) {
        byte[] merd = new byte[buf.size()];
        for (int i = 0; i < buf.size(); i++) {
            merd[i] = buf.get(i);
        }
        return merd;
    }

    /**
     * Build MULTICAST FORCE Frame
     */
    public final void sendMULTICASTFORCEFrame(DatagramSocket datagramSocket, String soulissNodeIPAddressOnLAN,
            byte nodeIndex, byte userIndex, byte typical, byte shortCommand) {
        ArrayList<Byte> macacoFrame = new ArrayList<Byte>();
        macacoFrame.add(SoulissBindingUDPConstants.SOULISS_UDP_FUNCTION_FORCE_MASSIVE);

        // PUTIN, STARTOFFEST, NUMBEROF
        macacoFrame.add((byte) 0x0);// PUTIN
        macacoFrame.add((byte) 0x0);// PUTIN

        macacoFrame.add(typical);// Start Offset
        macacoFrame.add((byte) 1); // Number Of

        macacoFrame.add(shortCommand);// PAYLOAD
        logger.debug("sendMULTICASTFORCEFrame - {}, soulissNodeIPAddressOnLAN: {}", macacoToString(macacoFrame),
                soulissNodeIPAddressOnLAN);
        send(datagramSocket, macacoFrame, soulissNodeIPAddressOnLAN, nodeIndex, userIndex);
    }

    /**
     * Build PING Frame
     */
    public final void sendPing(@Nullable DatagramSocket datagramSocket, @Nullable String soulissNodeIPAddressOnLAN,
            byte nodeIndex, byte userIndex, byte putIn1, byte punIn_2) {
        ArrayList<Byte> macacoFrame = new ArrayList<Byte>();
        macacoFrame.add(SoulissBindingUDPConstants.SOULISS_UDP_FUNCTION_PING_REQ);

        // PUTIN, STARTOFFEST, NUMBEROF
        macacoFrame.add(putIn1);// PUTIN
        macacoFrame.add(punIn_2);// PUTIN

        macacoFrame.add((byte) 0x00);// Start Offset
        macacoFrame.add((byte) 0x00); // Number Of
        logger.debug("sendPing - {}, IP: {} to port {}", macacoToString(macacoFrame), soulissNodeIPAddressOnLAN,
                datagramSocket.getLocalPort());
        send(datagramSocket, macacoFrame, soulissNodeIPAddressOnLAN, nodeIndex, userIndex);
    }

    /**
     * Build BROADCAST PING Frame
     */
    public final void sendBroadcastGatewayDiscover(DatagramSocket datagramSocket) {
        ArrayList<Byte> macacoFrame = new ArrayList<Byte>();
        macacoFrame.add(SoulissBindingUDPConstants.SOULISS_UDP_FUNCTION_DISCOVER_GW_NODE_BCAST_REQ);

        // PUTIN, STARTOFFEST, NUMBEROF
        macacoFrame.add((byte) 0x05);// PUTIN
        macacoFrame.add((byte) 0x00);// PUTIN

        macacoFrame.add((byte) 0x00);// Start Offset
        macacoFrame.add((byte) 0x00); // Number Of
        logger.debug("sendBroadcastPing - {} to port {}", macacoToString(macacoFrame), datagramSocket.getLocalPort());
        sendBroadcastNow(datagramSocket, macacoFrame);
    }

    /**
     * Build SUBSCRIPTION Frame
     */
    public final void sendSUBSCRIPTIONframe(@Nullable DatagramSocket datagramSocket,
            @Nullable String soulissNodeIPAddressOnLAN, byte nodeIndex, byte userIndex, int iNodes) {
        ArrayList<Byte> macacoFrame = new ArrayList<Byte>();
        macacoFrame.add(SoulissBindingUDPConstants.SOULISS_UDP_FUNCTION_SUBSCRIBE_REQ);

        // PUTIN, STARTOFFEST, NUMBEROF
        macacoFrame.add((byte) 0x00);// PUTIN
        macacoFrame.add((byte) 0x00);// PUTIN
        macacoFrame.add((byte) 0x00);

        macacoFrame.add((byte) iNodes);
        logger.debug("sendSUBSCRIPTIONframe - {}, IP: {} - port: {}", macacoToString(macacoFrame),
                soulissNodeIPAddressOnLAN, datagramSocket.getLocalPort());
        send(datagramSocket, macacoFrame, soulissNodeIPAddressOnLAN, nodeIndex, userIndex);
    }

    /**
     * Build HEALTY REQUEST Frame
     */
    public final void sendHealthyRequestFrame(@Nullable DatagramSocket datagramSocket,
            @Nullable String soulissNodeIPAddressOnLAN, byte nodeIndex, byte userIndex, int iNodes) {
        ArrayList<Byte> macacoFrame = new ArrayList<Byte>();
        macacoFrame.add(SoulissBindingUDPConstants.SOULISS_UDP_FUNCTION_HEALTHY_REQ);

        // PUTIN, STARTOFFEST, NUMBEROF
        macacoFrame.add((byte) 0x00);// PUTIN
        macacoFrame.add((byte) 0x00);// PUTIN
        macacoFrame.add((byte) 0x00);
        macacoFrame.add((byte) iNodes);
        logger.debug("sendHealthyRequestFrame - {}, IP: {} - port: {}", macacoToString(macacoFrame),
                soulissNodeIPAddressOnLAN, datagramSocket.getLocalPort());
        send(datagramSocket, macacoFrame, soulissNodeIPAddressOnLAN, nodeIndex, userIndex);
    }

    /**
     * Build TYPICAL REQUEST Frame
     */
    public final void sendTypicalRequestFrame(@Nullable DatagramSocket datagramSocket,
            @Nullable String soulissNodeIPAddressOnLAN, byte nodeIndex, byte userIndex, int nodes) {
        ArrayList<Byte> macacoFrame = new ArrayList<Byte>();
        macacoFrame.add(SoulissBindingUDPConstants.SOULISS_UDP_FUNCTION_TYP_REQ);
        // PUTIN, STARTOFFEST, NUMBEROF
        macacoFrame.add((byte) 0x00);// PUTIN
        macacoFrame.add((byte) 0x00);// PUTIN
        macacoFrame.add((byte) 0x00); // startOffset

        macacoFrame.add((byte) nodes); // iNodes
        // macacoFrame.add((byte) 0x00); // iNodes
        logger.debug("sendTypicalRequestFrame - {}, IP: {} - port: {}", macacoToString(macacoFrame),
                soulissNodeIPAddressOnLAN, datagramSocket.getLocalPort());
        send(datagramSocket, macacoFrame, soulissNodeIPAddressOnLAN, nodeIndex, userIndex);
    }

    static boolean flag = true;

    private static String macacoToString(ArrayList<Byte> mACACOframe) {
        /*
         * while (!flag) {
         * };
         */
        // copio array per evitare modifiche concorrenti
        ArrayList<Byte> mACACOframe2 = new ArrayList<Byte>();
        mACACOframe2.addAll(mACACOframe);
        flag = false;
        StringBuilder sb = new StringBuilder();
        sb.append("HEX: [");
        for (byte b : mACACOframe2) {
            sb.append(String.format("%02X ", b));
        }
        sb.append("]");
        flag = true;
        return sb.toString();
    }
}
