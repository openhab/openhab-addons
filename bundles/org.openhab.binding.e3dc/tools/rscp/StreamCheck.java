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
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reassembles the two directions of an RSCP session and identifies the cipher against the handshake.
 *
 * <p>
 * A capture of a live session is a stream of large TCP segments rather than single protocol frames, so
 * the segments have to be put back together before anything can be decrypted. The client's first bytes
 * are the authentication request, which is exactly what the cipher has to be checked against: a correct
 * configuration decrypts it into a frame whose magic is the RSCP magic.
 *
 * <p>
 * Usage: {@code StreamCheck <pcap> <port>} then report the printed first blocks.
 *
 * @author Bernd Weymann - Initial contribution
 */
public final class StreamCheck {

    private static final int BLOCK = 32;

    public static void main(String[] args) throws Exception {
        Path file = Path.of(args[0]);
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 5033;
        byte[] raw = Files.readAllBytes(file);

        int fileMagic = ByteBuffer.wrap(raw, 0, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        ByteOrder fileOrder = fileMagic == 0xA1B2C3D4 ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
        ByteBuffer header = ByteBuffer.wrap(raw).order(fileOrder);
        ByteBuffer packet = ByteBuffer.wrap(raw).order(ByteOrder.BIG_ENDIAN);
        long linkType = header.getInt(20) & 0xFFFFFFFFL;

        // key is "sourceport>destport", value is the reassembled payload
        Map<String, ByteArrayOutputStream> streams = new LinkedHashMap<>();
        int offset = 24;
        while (offset + 16 <= raw.length) {
            long included = header.getInt(offset + 8) & 0xFFFFFFFFL;
            int record = offset + 16;
            if (record + included > raw.length) {
                break;
            }
            int ip = PcapList.ipOffset(raw, record, linkType);
            if (ip > 0) {
                int ihl = (packet.get(ip) & 0x0F) * 4;
                if ((packet.get(ip + 9) & 0xFF) == 6) {
                    int tcp = ip + ihl;
                    int sourcePort = packet.getShort(tcp) & 0xFFFF;
                    int destPort = packet.getShort(tcp + 2) & 0xFFFF;
                    int dataOffset = ((packet.get(tcp + 12) & 0xF0) >> 4) * 4;
                    int total = packet.getShort(ip + 2) & 0xFFFF;
                    int dataLength = total - ihl - dataOffset;
                    int dataStart = tcp + dataOffset;
                    if ((sourcePort == port || destPort == port) && dataLength > 0
                            && dataStart + dataLength <= raw.length) {
                        String key = sourcePort + ">" + destPort + " " + ip(packet.getInt(ip + 12)) + "->"
                                + ip(packet.getInt(ip + 16));
                        ByteArrayOutputStream stream = streams.computeIfAbsent(key, k -> new ByteArrayOutputStream());
                        stream.write(raw, dataStart, dataLength);
                    }
                }
            }
            offset = record + (int) included;
        }

        System.out.printf("link type %d, %d streams%n", linkType, streams.size());
        for (Map.Entry<String, ByteArrayOutputStream> entry : streams.entrySet()) {
            byte[] data = entry.getValue().toByteArray();
            System.out.printf("%n=== %s : %d bytes ===%n", entry.getKey(), data.length);
            System.out.println("  first 32: " + BlockCheck.toHex(Arrays.copyOf(data, Math.min(32, data.length))));
            System.out.println("  next  32: " + BlockCheck.toHex(
                    Arrays.copyOfRange(data, 32, Math.min(64, data.length))));
        }
    }

    private static String ip(int value) {
        return ((value >> 24) & 0xFF) + "." + ((value >> 16) & 0xFF) + "." + ((value >> 8) & 0xFF) + "."
                + (value & 0xFF);
    }

    private StreamCheck() {
    }
}
