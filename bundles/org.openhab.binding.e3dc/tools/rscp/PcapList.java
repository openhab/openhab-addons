/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Lists every TCP payload in a capture with its direction and size.
 *
 * <p>
 * The sizes alone identify the block size a client is using: a payload whose length is a multiple of
 * sixteen but not of thirty two points at a 128 bit block cipher, while multiples of thirty two point
 * at a 256 bit block.
 *
 * @author Bernd Weymann - Initial contribution
 */
public final class PcapList {

    public static void main(String[] args) throws Exception {
        byte[] raw = Files.readAllBytes(Path.of(args[0]));
        int fileMagic = ByteBuffer.wrap(raw, 0, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        ByteOrder fileOrder = fileMagic == 0xA1B2C3D4 ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
        ByteBuffer header = ByteBuffer.wrap(raw).order(fileOrder);
        ByteBuffer packet = ByteBuffer.wrap(raw).order(ByteOrder.BIG_ENDIAN);
        System.out.printf("file %s, %d bytes, magic 0x%08X, order %s%n", args[0], raw.length, fileMagic, fileOrder);
        System.out.printf("link type %d%n", header.getInt(20));

        long linkType = header.getInt(20) & 0xFFFFFFFFL;
        int offset = 24;
        int index = 0;
        while (offset + 16 <= raw.length) {
            long included = header.getInt(offset + 8) & 0xFFFFFFFFL;
            int record = offset + 16;
            if (record + included > raw.length) {
                break;
            }
            int ip = ipOffset(raw, record, linkType);
            if (ip > 0) {
                int ihl = (packet.get(ip) & 0x0F) * 4;
                int protocol = packet.get(ip + 9) & 0xFF;
                int sourceIp = packet.getInt(ip + 12);
                int destIp = packet.getInt(ip + 16);
                if (protocol == 6) {
                    int tcp = ip + ihl;
                    int sourcePort = packet.getShort(tcp) & 0xFFFF;
                    int destPort = packet.getShort(tcp + 2) & 0xFFFF;
                    int dataOffset = ((packet.get(tcp + 12) & 0xF0) >> 4) * 4;
                    int total = packet.getShort(ip + 2) & 0xFFFF;
                    int dataLength = total - ihl - dataOffset;
                    int dataStart = tcp + dataOffset;
                    if ((sourcePort == 5033 || destPort == 5033) && dataLength > 0
                            && dataStart + dataLength <= raw.length) {
                        StringBuilder hex = new StringBuilder();
                        for (int i = 0; i < Math.min(dataLength, 16); i++) {
                            hex.append(String.format("%02x", raw[dataStart + i]));
                        }
                        System.out.printf("#%-3d %s:%d -> %s:%d  %d bytes  %s%s%n", index++, ip(sourceIp), sourcePort,
                                ip(destIp), destPort, dataLength, hex, dataLength > 16 ? "..." : "");
                    }
                }
            }
            offset = record + (int) included;
        }
    }

    /**
     * The offset of the IPv4 header inside a record, which depends on the link layer.
     *
     * <p>
     * A capture taken with {@code tcpdump -i any} uses the Linux cooked v2 header, which is a different
     * length from an Ethernet header and places the protocol field at a different position. Assuming
     * Ethernet for such a capture reads the packet headers at the wrong offset.
     */
    static int ipOffset(byte[] raw, int record, long linkType) {
        if (linkType == 1) {
            // Ethernet: 6 destination, 6 source, 2 ethertype
            return record + 14;
        }
        if (linkType == 276) {
            // Linux cooked v2: 2 protocol, 2 reserved, 4 interface, 1 address type,
            // 1 address length, 8 address, 2 protocol again
            return record + 20;
        }
        if (linkType == 113) {
            // Linux cooked v1: 2 packet type, 2 address type, 2 address length, 8 address, 2 protocol
            return record + 16;
        }
        return -1;
    }

    private static String ip(int value) {
        return ((value >> 24) & 0xFF) + "." + ((value >> 16) & 0xFF) + "." + ((value >> 8) & 0xFF) + "."
                + (value & 0xFF);
    }

    private PcapList() {
    }
}
