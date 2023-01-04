/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.network.internal.dhcp;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Parses a dhcp packet and extracts the OP code and all DHCP Options.
 *
 * Example:
 * DatagramSocket socket = new DatagramSocket(67);
 * while (true) {
 * DatagramPacket packet = new DatagramPacket(new byte[1500], 1500);
 * socket.receive(packet);
 * DHCPPacket dhcp = new DHCPPacket(packet);
 * InetAddress requestedAddress = dhcp.getRequestedIPAddress();
 * }
 *
 * If used this way, beware that a <tt>BadPacketExpcetion</tt> is thrown
 * if the datagram contains invalid DHCP data.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
class DHCPPacket {
    /** DHCP BOOTP CODES **/
    static final byte BOOTREQUEST = 1;
    static final byte BOOTREPLY = 2;

    /** DHCP MESSAGE CODES **/
    static final byte DHCPDISCOVER = 1;

    static final byte DHCPREQUEST = 3;
    static final byte DHCPDECLINE = 4;

    static final byte DHCPRELEASE = 7;
    static final byte DHCPINFORM = 8;

    /** DHCP OPTIONS CODE **/
    static final byte DHO_PAD = 0;

    static final byte DHO_DHCP_REQUESTED_ADDRESS = 50;

    static final byte DHO_DHCP_MESSAGE_TYPE = 53;

    static final byte DHO_END = -1;

    static final int BOOTP_ABSOLUTE_MIN_LEN = 236;
    static final int DHCP_MAX_MTU = 1500;

    // Magic cookie
    static final int MAGIC_COOKIE = 0x63825363;

    /**
     * If a DHCP datagram is malformed this Exception is thrown.
     *
     * It inherits from <tt>IllegalArgumentException</tt> and <tt>RuntimeException</tt>
     * so it doesn't need to be explicitly caught.
     *
     * @author David Gräff
     */
    static class BadPacketException extends IllegalArgumentException {
        private static final long serialVersionUID = 5866225879843384688L;

        BadPacketException(String message) {
            super(message);
        }

        BadPacketException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private byte op;
    private Map<Byte, byte[]> options;

    /**
     * Package private constructor for test suite.
     */
    DHCPPacket(byte[] messageType, byte @Nullable [] requestedIP) {
        this.op = BOOTREQUEST;
        this.options = new LinkedHashMap<>();
        options.put(DHO_DHCP_MESSAGE_TYPE, messageType);
        if (requestedIP != null) {
            options.put(DHO_DHCP_REQUESTED_ADDRESS, requestedIP);
        }
    }

    /**
     * Constructor for the <tt>DHCPPacket</tt> class. Parses the given datagram.
     */
    public DHCPPacket(DatagramPacket datagram) throws BadPacketException, IOException {
        this.op = BOOTREPLY;
        this.options = new LinkedHashMap<>();

        byte[] buffer = datagram.getData();
        int offset = datagram.getOffset();
        int length = datagram.getLength();

        // absolute minimum size for a valid packet
        if (length < BOOTP_ABSOLUTE_MIN_LEN) {
            throw new BadPacketException(
                    "DHCP Packet too small (" + length + ") absolute minimum is " + BOOTP_ABSOLUTE_MIN_LEN);
        }
        // maximum size for a valid DHCP packet
        if (length > DHCP_MAX_MTU) {
            throw new BadPacketException("DHCP Packet too big (" + length + ") max MTU is " + DHCP_MAX_MTU);
        }

        // turn buffer into a readable stream
        ByteArrayInputStream inBStream = new ByteArrayInputStream(buffer, offset, length);
        DataInputStream inStream = new DataInputStream(inBStream);

        byte[] dummy = new byte[128];

        // parse static part of packet
        this.op = inStream.readByte();
        inStream.readByte(); // read hardware type (ETHERNET)
        inStream.readByte(); // read hardware address length (6 bytes)
        inStream.readByte(); // read hops
        inStream.readInt(); // read transaction id
        inStream.readShort(); // read secsonds elapsed
        inStream.readShort(); // read flags
        inStream.readFully(dummy, 0, 4); // ciaddr
        inStream.readFully(dummy, 0, 4); // yiaddr
        inStream.readFully(dummy, 0, 4); // siaddr
        inStream.readFully(dummy, 0, 4); // giaddr
        inStream.readFully(dummy, 0, 16); // chaddr
        inStream.readFully(dummy, 0, 64); // sname
        inStream.readFully(dummy, 0, 128); // file

        // check for DHCP MAGIC_COOKIE
        inBStream.mark(4); // read ahead 4 bytes
        if (inStream.readInt() != MAGIC_COOKIE) {
            throw new BadPacketException("Packet seams to be truncated");
        }

        // DHCP Packet: parsing options
        int type = 0;

        while (true) {
            int r = inBStream.read();
            if (r < 0) {
                break;
            } // EOF

            type = (byte) r;

            if (type == DHO_PAD) {
                continue;
            } // skip Padding
            if (type == DHO_END) {
                break;
            } // break if end of options

            r = inBStream.read();
            if (r < 0) {
                break;
            } // EOF

            int len = Math.min(r, inBStream.available());
            byte[] unitOpt = new byte[len];
            inBStream.read(unitOpt);

            this.options.put((byte) type, unitOpt);
        }
        if (type != DHO_END) {
            throw new BadPacketException("Packet seams to be truncated");
        }
    }

    /**
     * Returns the op field (Message op code).
     *
     *
     * @return the op field.
     */
    public byte getOp() {
        return this.op;
    }

    /**
     * Return the DHCP Option Type.
     *
     * <p>
     * This is a short-cut for <tt>getOptionAsByte(DHO_DHCP_MESSAGE_TYPE)</tt>.
     *
     * @return option type, of <tt>null</tt> if not present.
     */
    public @Nullable Byte getDHCPMessageType() {
        byte[] opt = options.get(DHO_DHCP_MESSAGE_TYPE);
        if (opt == null) {
            return null;
        }
        if (opt.length != 1) {
            throw new BadPacketException(
                    "option " + DHO_DHCP_MESSAGE_TYPE + " is wrong size:" + opt.length + " should be 1");
        }
        return opt[0];
    }

    /**
     * Returns the requested IP address of a BOOTREQUEST packet.
     */
    public @Nullable InetAddress getRequestedIPAddress() throws IllegalArgumentException, UnknownHostException {
        byte[] opt = options.get(DHO_DHCP_REQUESTED_ADDRESS);
        if (opt == null) {
            return null;
        }
        if (opt.length != 4) {
            throw new BadPacketException(
                    "option " + DHO_DHCP_REQUESTED_ADDRESS + " is wrong size:" + opt.length + " should be 4");
        }
        return InetAddress.getByAddress(opt);
    }
}
