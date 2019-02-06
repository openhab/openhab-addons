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
package org.openhab.binding.hdl.internal.handler;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.openhab.binding.hdl.internal.device.CommandType;
import org.openhab.binding.hdl.internal.device.DeviceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HdlPacket} is responsible for handling received packet on UDP,
 * and to make the packets to send on UDP.
 *
 * @based on HDL Packet from HDL binding to OpenHAB 1
 * @author stigla - Initial contribution
 */
public class HdlPacket {
    private static Logger logger = LoggerFactory.getLogger(HdlPacket.class);

    public String serialNr;
    public int sourceSubnetID;
    public int sourceDeviceID;
    public int sourceDevice = 0xfeff;
    public DeviceType sourcedeviceType;
    public int command;
    public CommandType commandType;
    public int targetSubnetID;
    public int targetDeviceID;
    public InetAddress replyAddress;

    public byte[] data;

    protected static final int[] CRC = { 0x0000, 0x1021, 0x2042, 0x3063, 0x4084, 0x50a5, 0x60c6, 0x70e7, 0x8108, 0x9129,
            0xa14a, 0xb16b, 0xc18c, 0xd1ad, 0xe1ce, 0xf1ef, 0x1231, 0x0210, 0x3273, 0x2252, 0x52b5, 0x4294, 0x72f7,
            0x62d6, 0x9339, 0x8318, 0xb37b, 0xa35a, 0xd3bd, 0xc39c, 0xf3ff, 0xe3de, 0x2462, 0x3443, 0x0420, 0x1401,
            0x64e6, 0x74c7, 0x44a4, 0x5485, 0xa56a, 0xb54b, 0x8528, 0x9509, 0xe5ee, 0xf5cf, 0xc5ac, 0xd58d, 0x3653,
            0x2672, 0x1611, 0x0630, 0x76d7, 0x66f6, 0x5695, 0x46b4, 0xb75b, 0xa77a, 0x9719, 0x8738, 0xf7df, 0xe7fe,
            0xd79d, 0xc7bc, 0x48c4, 0x58e5, 0x6886, 0x78a7, 0x0840, 0x1861, 0x2802, 0x3823, 0xc9cc, 0xd9ed, 0xe98e,
            0xf9af, 0x8948, 0x9969, 0xa90a, 0xb92b, 0x5af5, 0x4ad4, 0x7ab7, 0x6a96, 0x1a71, 0x0a50, 0x3a33, 0x2a12,
            0xdbfd, 0xcbdc, 0xfbbf, 0xeb9e, 0x9b79, 0x8b58, 0xbb3b, 0xab1a, 0x6ca6, 0x7c87, 0x4ce4, 0x5cc5, 0x2c22,
            0x3c03, 0x0c60, 0x1c41, 0xedae, 0xfd8f, 0xcdec, 0xddcd, 0xad2a, 0xbd0b, 0x8d68, 0x9d49, 0x7e97, 0x6eb6,
            0x5ed5, 0x4ef4, 0x3e13, 0x2e32, 0x1e51, 0x0e70, 0xff9f, 0xefbe, 0xdfdd, 0xcffc, 0xbf1b, 0xaf3a, 0x9f59,
            0x8f78, 0x9188, 0x81a9, 0xb1ca, 0xa1eb, 0xd10c, 0xc12d, 0xf14e, 0xe16f, 0x1080, 0x00a1, 0x30c2, 0x20e3,
            0x5004, 0x4025, 0x7046, 0x6067, 0x83b9, 0x9398, 0xa3fb, 0xb3da, 0xc33d, 0xd31c, 0xe37f, 0xf35e, 0x02b1,
            0x1290, 0x22f3, 0x32d2, 0x4235, 0x5214, 0x6277, 0x7256, 0xb5ea, 0xa5cb, 0x95a8, 0x8589, 0xf56e, 0xe54f,
            0xd52c, 0xc50d, 0x34e2, 0x24c3, 0x14a0, 0x0481, 0x7466, 0x6447, 0x5424, 0x4405, 0xa7db, 0xb7fa, 0x8799,
            0x97b8, 0xe75f, 0xf77e, 0xc71d, 0xd73c, 0x26d3, 0x36f2, 0x0691, 0x16b0, 0x6657, 0x7676, 0x4615, 0x5634,
            0xd94c, 0xc96d, 0xf90e, 0xe92f, 0x99c8, 0x89e9, 0xb98a, 0xa9ab, 0x5844, 0x4865, 0x7806, 0x6827, 0x18c0,
            0x08e1, 0x3882, 0x28a3, 0xcb7d, 0xdb5c, 0xeb3f, 0xfb1e, 0x8bf9, 0x9bd8, 0xabbb, 0xbb9a, 0x4a75, 0x5a54,
            0x6a37, 0x7a16, 0x0af1, 0x1ad0, 0x2ab3, 0x3a92, 0xfd2e, 0xed0f, 0xdd6c, 0xcd4d, 0xbdaa, 0xad8b, 0x9de8,
            0x8dc9, 0x7c26, 0x6c07, 0x5c64, 0x4c45, 0x3ca2, 0x2c83, 0x1ce0, 0x0cc1, 0xef1f, 0xff3e, 0xcf5d, 0xdf7c,
            0xaf9b, 0xbfba, 0x8fd9, 0x9ff8, 0x6e17, 0x7e36, 0x4e55, 0x5e74, 0x2e93, 0x3eb2, 0x0ed1, 0x1ef0 };

    public HdlPacket() {
        try {
            replyAddress = InetAddress.getByAddress(new byte[] { 0, 0, 0, 0 });
        } catch (UnknownHostException e) {
        }
    }

    protected static int ubyte(byte a) {
        return (a) & 0xff;
    }

    protected static int ushort(byte h, byte l) {
        return ((h << 8) & 0xff00) | (l & 0xff);
    }

    protected static int computeCRC16(byte[] data, int offset, int count) {
        int crc = 0;
        int dat;

        for (int i = offset; i < offset + count; ++i) {
            dat = (crc >>> 8) & 0xff;
            crc = (crc << 8) & 0xffff;
            crc ^= CRC[(dat ^ data[i]) & 0xff];
        }

        return crc & 0xffff;
    }

    public static HdlPacket parse(byte[] data, int length) {
        // 4 bytes for IP + 10 bytes for HDLMIRACLE + 13 bytes min packet length == 27

        if (length < 27 || !(new String(data, 4, 10).equals("HDLMIRACLE"))) {
            return null;
        }

        if (length != 4 + 10 + 2 + data[16]) {
            return null;
        }

        if (computeCRC16(data, 16, data[16] - 2) != ushort(data[length - 2], data[length - 1])) {
            return null;
        }

        int offset = 17;

        HdlPacket packet = new HdlPacket();

        // System.out.println("All Data: " + data);

        packet.sourceSubnetID = data[offset];
        offset += 1;
        packet.sourceDeviceID = data[offset];
        offset += 1;
        packet.sourceDevice = ushort(data[offset], data[offset + 1]);
        offset += 2;
        packet.command = ushort(data[offset], data[offset + 1]);
        offset += 2;
        packet.targetSubnetID = data[offset];
        offset += 1;
        packet.targetDeviceID = data[offset];
        offset += 1;

        packet.serialNr = Integer.toString(packet.sourceSubnetID * 1000 + packet.sourceDeviceID);

        packet.sourcedeviceType = DeviceType.create(packet.sourceDevice);

        packet.commandType = CommandType.create(packet.command);

        packet.data = new byte[length - 27];
        System.arraycopy(data, 25, packet.data, 0, length - 27);

        logger.debug(
                "From Source: Subnet: {}, DeviceID: {} For DeviceNr: {} found DeviceType: {}, command: {}, commandType: {}"
                        + " To target: Subnet: {}, DeviceID: {}.",
                packet.sourceSubnetID, packet.sourceDeviceID, packet.sourceDevice, packet.sourcedeviceType,
                packet.command, packet.commandType, packet.targetSubnetID, packet.targetDeviceID);

        return packet;
    }

    // @Override
    // public String toString() {
    // return "[" + Integer.toHexString(sourceAddress) + " -> " + Integer.toHexString(targetAddress) + " : "
    // + Integer.toHexString(command) + "]";
    // }

    public String getSerialNr() {
        return serialNr;
    }

    public int getSourceDevice() {
        return sourceDevice;
    }

    public int getCommand() {
        return command;
    }

    public byte[] getData() {
        return data;
    }

    public void setSourceSubnetID(int a) {
        sourceSubnetID = a;
    }

    public void setSourceDeviceId(int a) {
        sourceDeviceID = a;
    }

    public void setTargetSubnetID(int a) {
        targetSubnetID = a;
    }

    public void setTargetDeviceId(int a) {
        targetDeviceID = a;
    }

    public void setSourceDevice(int a) {
        sourceDevice = a & 0xffff;
    }

    private void setCommand(int a) {
        command = a & 0xffff;
    }

    public void setCommandType(CommandType Type) {
        setCommand(Type.getValue());
    }

    public void setData(byte[] d) {
        data = d;
    }

    public void setReplyAddress(InetAddress addr) {
        replyAddress = addr;
    }

    public byte[] getBytes() {
        byte[] p = new byte[27 + (data != null ? data.length : 0)];

        if (replyAddress != null) {
            System.arraycopy(replyAddress.getAddress(), 0, p, 0, 4);
        }

        byte[] magic = "HDLMIRACLE".getBytes();
        System.arraycopy(magic, 0, p, 4, magic.length);

        int i = 14;
        p[i++] = (byte) 0xaa;
        p[i++] = (byte) 0xaa;
        p[i++] = (byte) (p.length - 16);
        p[i++] = (byte) (sourceSubnetID & 0xff); // src subnet
        p[i++] = (byte) (sourceDeviceID & 0xff); // src device id
        p[i++] = (byte) (sourceDevice >> 8); // src subnet
        p[i++] = (byte) sourceDevice; // src device id
        p[i++] = (byte) (command >> 8); // cmd id
        p[i++] = (byte) command; // cmd id
        p[i++] = (byte) (targetSubnetID & 0xff); // tgt subnet
        p[i++] = (byte) (targetDeviceID & 0xff); // tgt device id

        if (data != null) {
            System.arraycopy(data, 0, p, i, data.length);
        }

        int crc = computeCRC16(p, 16, p.length - 18);
        i = p.length - 2;
        p[i++] = (byte) (crc >> 8);
        p[i++] = (byte) crc;

        return p;
    }

}
