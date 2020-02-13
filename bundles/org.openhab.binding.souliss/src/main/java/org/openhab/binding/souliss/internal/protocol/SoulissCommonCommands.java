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
public class SoulissCommonCommands {

    private static Logger logger = LoggerFactory.getLogger(SoulissCommonCommands.class);

    public static void sendFORCEFrame(DatagramSocket datagramSocket, String soulissNodeIPAddressOnLAN, byte nodeIndex,
            byte userIndex, int IDNode, int slot, byte shortCommand) {
        sendFORCEFrame(datagramSocket, soulissNodeIPAddressOnLAN, nodeIndex, userIndex, IDNode, slot, shortCommand,
                null, null, null);
    }

    /*
     * used for set dimmer value. It set command at first byte and dimmerVal to
     * second byte
     */
    public static void sendFORCEFrame(DatagramSocket datagramSocket, String soulissNodeIPAddressOnLAN, byte nodeIndex,
            byte userIndex, int IDNode, int slot, byte shortCommand, byte lDimmer) {
        sendFORCEFrame(datagramSocket, soulissNodeIPAddressOnLAN, nodeIndex, userIndex, IDNode, slot, shortCommand,
                lDimmer, null, null);
    }

    /*
     * send force frame with command and RGB value
     */
    public static void sendFORCEFrame(DatagramSocket datagramSocket, String soulissNodeIPAddressOnLAN, byte nodeIndex,
            byte userIndex, int IDNode, int slot, byte shortCommand, Byte byte1, Byte byte2, Byte byte3) {
        ArrayList<Byte> MACACOframe = new ArrayList<Byte>();
        MACACOframe.add(SoulissBindingUDPConstants.Souliss_UDP_function_force);

        // PUTIN, STARTOFFEST, NUMBEROF
        MACACOframe.add((byte) 0x0);// PUTIN
        MACACOframe.add((byte) 0x0);// PUTIN

        MACACOframe.add((byte) (IDNode));// Start Offset

        if (byte1 == null && byte2 == null && byte3 == null) {
            MACACOframe.add((byte) ((byte) slot + 1)); // Number Of
        } else if (byte2 == null && byte3 == null) {
            MACACOframe.add((byte) ((byte) slot + 2)); // Number Of byte of
                                                       // payload= command +
                                                       // set byte
        } else {
            MACACOframe.add((byte) ((byte) slot + 4)); // Number Of byte of
                                                       // payload= OnOFF + Red
                                                       // + Green + Blu
        }

        for (int i = 0; i <= slot - 1; i++) {
            MACACOframe.add((byte) 00); // pongo a zero i byte precedenti lo
                                        // slot da modificare
        }
        MACACOframe.add(shortCommand);// PAYLOAD

        if (byte1 != null && byte2 != null && byte3 != null) {
            MACACOframe.add(byte1.byteValue());// PAYLOAD RED
            MACACOframe.add(byte2.byteValue());// PAYLOAD GREEN
            MACACOframe.add(byte3.byteValue());// PAYLOAD BLUE
        } else if (byte1 != null) {
            MACACOframe.add(byte1.byteValue());// PAYLOAD DIMMER
        }

        logger.debug("sendFORCEFrame - {}, soulissNodeIPAddressOnLAN: {}", MaCacoToString(MACACOframe),
                soulissNodeIPAddressOnLAN);
        send(datagramSocket, MACACOframe, soulissNodeIPAddressOnLAN, nodeIndex, userIndex);

    }

    /*
     * T61 send framte to push the setpoint value
     */

    public static void sendFORCEFrameT61SetPoint(DatagramSocket datagramSocket, String soulissNodeIPAddressOnLAN,
            byte nodeIndex, byte userIndex, int IDNode, int slot, Byte byte1, Byte byte2) {

        ArrayList<Byte> MACACOframe = new ArrayList<Byte>();
        MACACOframe.add(SoulissBindingUDPConstants.Souliss_UDP_function_force);

        // PUTIN, STARTOFFEST, NUMBEROF
        MACACOframe.add((byte) 0x0);// PUTIN
        MACACOframe.add((byte) 0x0);// PUTIN

        MACACOframe.add((byte) (IDNode));// Start Offset
        MACACOframe.add((byte) ((byte) slot + 2)); // Number Of byte of payload= command + set byte

        for (int i = 0; i <= slot - 1; i++) {
            MACACOframe.add((byte) 00); // pongo a zero i byte precedenti lo
                                        // slot da modificare
        }
        // PAYLOAD
        MACACOframe.add(byte1.byteValue());// first byte Setpoint Value
        MACACOframe.add(byte2.byteValue());// second byte Setpoint Value

        logger.debug("sendFORCEFrame - {}, soulissNodeIPAddressOnLAN: {}", MaCacoToString(MACACOframe),
                soulissNodeIPAddressOnLAN);

        send(datagramSocket, MACACOframe, soulissNodeIPAddressOnLAN, nodeIndex, userIndex);
    }

    /*
     * T31 send force frame with command and setpoint float
     */
    public static void sendFORCEFrameT31SetPoint(DatagramSocket datagramSocket, String soulissNodeIPAddressOnLAN,
            byte nodeIndex, byte userIndex, int IDNode, int slot, byte shortCommand, Byte byte1, Byte byte2) {
        ArrayList<Byte> MACACOframe = new ArrayList<Byte>();
        MACACOframe.add(SoulissBindingUDPConstants.Souliss_UDP_function_force);

        // PUTIN, STARTOFFEST, NUMBEROF
        MACACOframe.add((byte) 0x0);// PUTIN
        MACACOframe.add((byte) 0x0);// PUTIN

        MACACOframe.add((byte) (IDNode));// Start Offset
        MACACOframe.add((byte) ((byte) slot + 5)); // Number Of byte of payload= command + set byte

        for (int i = 0; i <= slot - 1; i++) {
            MACACOframe.add((byte) 00); // pongo a zero i byte precedenti lo
                                        // slot da modificare
        }
        MACACOframe.add(shortCommand);// PAYLOAD

        MACACOframe.add((byte) 0x0);// Empty - Temperature Measured Value
        MACACOframe.add((byte) 0x0);// Empty - Temperature Measured Value
        MACACOframe.add(byte1.byteValue());// Temperature Setpoint Value
        MACACOframe.add(byte2.byteValue());// Temperature Setpoint Value

        logger.debug("sendFORCEFrame - {}, soulissNodeIPAddressOnLAN: {}", MaCacoToString(MACACOframe),
                soulissNodeIPAddressOnLAN);
        send(datagramSocket, MACACOframe, soulissNodeIPAddressOnLAN, nodeIndex, userIndex);

    }

    public static void sendDBStructFrame(DatagramSocket socket, String soulissNodeIPAddressOnLAN, byte nodeIndex,
            byte userIndex) {
        ArrayList<Byte> MACACOframe = new ArrayList<Byte>();
        MACACOframe.add((byte) SoulissBindingUDPConstants.Souliss_UDP_function_db_struct);
        MACACOframe.add((byte) 0x0);// PUTIN
        MACACOframe.add((byte) 0x0);// PUTIN
        MACACOframe.add((byte) 0x0);// Start Offset
        MACACOframe.add((byte) 0x0); // Number Of

        logger.debug("sendDBStructFrame - {}, soulissNodeIPAddressOnLAN: {}", MaCacoToString(MACACOframe),
                soulissNodeIPAddressOnLAN);
        send(socket, MACACOframe, soulissNodeIPAddressOnLAN, nodeIndex, userIndex);

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
    private static void send(DatagramSocket socket, ArrayList<Byte> MACACOframe, String sSoulissNodeIPAddressOnLAN,
            byte nodeIndex, byte userIndex) {
        ArrayList<Byte> buf = buildVNetFrame(MACACOframe, sSoulissNodeIPAddressOnLAN, userIndex, nodeIndex);
        byte[] merd = toByteArray(buf);

        InetAddress serverAddr;
        try {
            serverAddr = InetAddress.getByName(sSoulissNodeIPAddressOnLAN);
            DatagramPacket packet = new DatagramPacket(merd, merd.length, serverAddr,
                    SoulissBindingUDPConstants.SOULISS_GATEWAY_DEFAULT_PORT);
            SoulissBindingSendDispatcherJob.put(socket, packet);
            // socket.send(packet);
        } catch (IOException e) {
            logger.error("Error: ", e);
            logger.error(e.getMessage());
        }

    }

    /*
     * send UDP frame
     */
    private static void sendBroadcastNow(DatagramSocket socket, ArrayList<Byte> MACACOframe) {
        byte iUserIndex = SoulissBindingNetworkParameters.defaultUserIndex;
        byte iNodeIndex = SoulissBindingNetworkParameters.defaultNodeIndex;

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
                                ArrayList<Byte> buf = buildVNetFrame(MACACOframe, "255.255.255.255", iUserIndex,
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
            logger.error(e.getMessage());
        } catch (UnknownHostException e) {
            logger.error(e.getMessage());
        }
    }

    /*
     * Build VNet Frame
     */
    private static ArrayList<Byte> buildVNetFrame(ArrayList<Byte> MACACOframe2, String soulissNodeIPAddress,
            byte iUserIndex, byte iNodeIndex) {
        ArrayList<Byte> frame = new ArrayList<Byte>();
        InetAddress ip;
        try {
            ip = InetAddress.getByName(soulissNodeIPAddress);
        } catch (UnknownHostException e) {
            logger.error(e.getMessage());
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
    public static void sendMULTICASTFORCEFrame(DatagramSocket datagramSocket, String soulissNodeIPAddressOnLAN,
            byte nodeIndex, byte userIndex, byte typical, byte shortCommand) {

        ArrayList<Byte> MACACOframe = new ArrayList<Byte>();
        MACACOframe.add(SoulissBindingUDPConstants.Souliss_UDP_function_force_massive);

        // PUTIN, STARTOFFEST, NUMBEROF
        MACACOframe.add((byte) 0x0);// PUTIN
        MACACOframe.add((byte) 0x0);// PUTIN

        MACACOframe.add(typical);// Start Offset
        MACACOframe.add((byte) 1); // Number Of

        MACACOframe.add(shortCommand);// PAYLOAD
        logger.debug("sendMULTICASTFORCEFrame - {}, soulissNodeIPAddressOnLAN: {}", MaCacoToString(MACACOframe),
                soulissNodeIPAddressOnLAN);
        send(datagramSocket, MACACOframe, soulissNodeIPAddressOnLAN, nodeIndex, userIndex);
    }

    /**
     * Build PING Frame
     */
    public static void sendPing(DatagramSocket datagramSocket, String soulissNodeIPAddressOnLAN, byte nodeIndex,
            byte userIndex, byte putIn_1, byte punIn_2) {

        ArrayList<Byte> MACACOframe = new ArrayList<Byte>();
        MACACOframe.add(SoulissBindingUDPConstants.Souliss_UDP_function_ping);

        // PUTIN, STARTOFFEST, NUMBEROF
        MACACOframe.add(putIn_1);// PUTIN
        MACACOframe.add(punIn_2);// PUTIN

        MACACOframe.add((byte) 0x00);// Start Offset
        MACACOframe.add((byte) 0x00); // Number Of
        logger.debug("sendPing - {}, IP: {} to port {}", MaCacoToString(MACACOframe), soulissNodeIPAddressOnLAN,
                datagramSocket.getLocalPort());
        send(datagramSocket, MACACOframe, soulissNodeIPAddressOnLAN, nodeIndex, userIndex);
    }

    /**
     * Build BROADCAST PING Frame
     */
    public static void sendBroadcastGatewayDiscover(DatagramSocket datagramSocket) {

        ArrayList<Byte> MACACOframe = new ArrayList<Byte>();
        MACACOframe.add(SoulissBindingUDPConstants.Souliss_UDP_function_discover_GW_node_bcast);

        // PUTIN, STARTOFFEST, NUMBEROF
        MACACOframe.add((byte) 0x05);// PUTIN
        MACACOframe.add((byte) 0x00);// PUTIN

        MACACOframe.add((byte) 0x00);// Start Offset
        MACACOframe.add((byte) 0x00); // Number Of
        logger.debug("sendBroadcastPing - {} to port {}", MaCacoToString(MACACOframe), datagramSocket.getLocalPort());
        sendBroadcastNow(datagramSocket, MACACOframe);
    }

    /**
     * Build SUBSCRIPTION Frame
     */
    public static void sendSUBSCRIPTIONframe(DatagramSocket datagramSocket, String soulissNodeIPAddressOnLAN,
            byte nodeIndex, byte userIndex, int iNodes) {

        ArrayList<Byte> MACACOframe = new ArrayList<Byte>();
        MACACOframe.add(SoulissBindingUDPConstants.Souliss_UDP_function_subscribe);

        // PUTIN, STARTOFFEST, NUMBEROF
        MACACOframe.add((byte) 0x00);// PUTIN
        MACACOframe.add((byte) 0x00);// PUTIN
        MACACOframe.add((byte) 0x00);

        MACACOframe.add((byte) iNodes);
        logger.debug("sendSUBSCRIPTIONframe - {}, IP: {} - port: {}", MaCacoToString(MACACOframe),
                soulissNodeIPAddressOnLAN, datagramSocket.getLocalPort());
        send(datagramSocket, MACACOframe, soulissNodeIPAddressOnLAN, nodeIndex, userIndex);
    }

    /**
     * Build HEALTY REQUEST Frame
     */
    public static void sendHEALTY_REQUESTframe(DatagramSocket datagramSocket, String soulissNodeIPAddressOnLAN,
            byte nodeIndex, byte userIndex, int iNodes) {

        ArrayList<Byte> MACACOframe = new ArrayList<Byte>();
        MACACOframe.add(SoulissBindingUDPConstants.Souliss_UDP_function_healthyReq);

        // PUTIN, STARTOFFEST, NUMBEROF
        MACACOframe.add((byte) 0x00);// PUTIN
        MACACOframe.add((byte) 0x00);// PUTIN
        MACACOframe.add((byte) 0x00);
        MACACOframe.add((byte) iNodes);
        logger.debug("sendHEALTY_REQUESTframe - {}, IP: {} - port: {}", MaCacoToString(MACACOframe),
                soulissNodeIPAddressOnLAN, datagramSocket.getLocalPort());
        send(datagramSocket, MACACOframe, soulissNodeIPAddressOnLAN, nodeIndex, userIndex);
    }

    /**
     * Build TYPICAL REQUEST Frame
     */
    public static void sendTYPICAL_REQUESTframe(DatagramSocket datagramSocket, String soulissNodeIPAddressOnLAN,
            byte nodeIndex, byte userIndex, int nodes) {

        ArrayList<Byte> MACACOframe = new ArrayList<Byte>();
        MACACOframe.add(SoulissBindingUDPConstants.Souliss_UDP_function_typreq);
        // PUTIN, STARTOFFEST, NUMBEROF
        MACACOframe.add((byte) 0x00);// PUTIN
        MACACOframe.add((byte) 0x00);// PUTIN
        MACACOframe.add((byte) 0x00); // startOffset

        MACACOframe.add((byte) nodes); // iNodes
        // MACACOframe.add((byte) 0x00); // iNodes
        logger.debug("sendTYPICAL_REQUESTframe - {}, IP: {} - port: {}", MaCacoToString(MACACOframe),
                soulissNodeIPAddressOnLAN, datagramSocket.getLocalPort());
        send(datagramSocket, MACACOframe, soulissNodeIPAddressOnLAN, nodeIndex, userIndex);
    }

    static boolean flag = true;

    private static String MaCacoToString(ArrayList<Byte> mACACOframe) {
        while (!flag) {
        }
        ;
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
