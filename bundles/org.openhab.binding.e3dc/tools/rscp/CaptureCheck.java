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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Recovers the RSCP cipher from a packet capture of a working client.
 *
 * <p>
 * The capture is treated as the reference: the tool extracts the TCP payloads exchanged on the RSCP
 * port, then tries every plausible cipher configuration against the first client payload. A
 * configuration is correct when it decrypts that payload into a frame whose magic matches the RSCP
 * magic, which is a 1 in 65536 coincidence rather than a guess.
 *
 * <p>
 * Only the first block is needed to identify the cipher, because the block depends on the key, the
 * start vector and the round function but not on the chaining of later blocks.
 *
 * <p>
 * Usage: {@code CaptureCheck <pcap> <rscpPassword> [port]
 *
 * @author Bernd Weymann - Initial contribution
 */
public final class CaptureCheck {

    private static final int RSCP_MAGIC = 0xE3DC;
    private static final int BLOCK = 32;

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("usage: CaptureCheck <pcap> <rscpPassword> [port]");
            System.exit(2);
        }
        Path file = Path.of(args[0]);
        String rscpPassword = args[1];
        int port = args.length > 2 ? Integer.parseInt(args[2]) : 5033;

        if (!selfTest()) {
            System.out.println("SELF TEST FAILED - the reader or the cipher is broken, results would be meaningless");
            System.exit(1);
        }
        System.out.println("self test passed: reader and cipher verified against a known frame");
        System.out.println();

        byte[] raw = Files.readAllBytes(file);
        List<byte[]> payloads = extract(raw, port);
        if (payloads.isEmpty()) {
            System.out.println("no TCP payload found on port " + port);
            System.exit(1);
        }
        System.out.printf("found %d TCP payloads on port %d%n", payloads.size(), port);
        for (int i = 0; i < payloads.size(); i++) {
            byte[] p = payloads.get(i);
            System.out.printf("  #%d  %d bytes  %s%n", i, p.length,
                    BlockCheck.toHex(Arrays.copyOf(p, Math.min(p.length, 32))));
        }
        System.out.println();

        // the first sizeable client payload is the authentication request
        byte[] candidate = null;
        for (byte[] p : payloads) {
            if (p.length >= BLOCK) {
                candidate = p;
                break;
            }
        }
        if (candidate == null) {
            System.out.println("no payload of at least " + BLOCK + " bytes to work with");
            System.exit(1);
        }
        byte[] firstBlock = Arrays.copyOf(candidate, BLOCK);
        System.out.println("testing the first " + BLOCK + " bytes of a " + candidate.length + " byte payload");
        System.out.println("ciphertext: " + BlockCheck.toHex(firstBlock));
        System.out.println();

        Map<String, Boolean> tried = new LinkedHashMap<>();
        for (String shifts : new String[] { "0134", "0145" }) {
            int[] offsets = new int[] { shifts.charAt(0) - '0', shifts.charAt(1) - '0', shifts.charAt(2) - '0',
                    shifts.charAt(3) - '0' };
            for (String keyForm : new String[] { "raw", "md5hex", "md5raw", "sha256" }) {
                RijndaelReference cipher = new RijndaelReference(offsets, keyForm, rscpPassword);
                byte[] plain = cipher.decryptBlock(firstBlock, "");
                ByteBuffer buffer = ByteBuffer.wrap(plain).order(ByteOrder.LITTLE_ENDIAN);
                int magic = buffer.getShort(0) & 0xFFFF;
                int ctrl = buffer.getShort(2) & 0xFFFF;
                int length = buffer.getShort(16) & 0xFFFF;
                boolean match = magic == RSCP_MAGIC;
                System.out.printf("shifts %s key=%-7s magic=0x%04X ctrl=0x%04X length=%d %s%n", shifts, keyForm, magic,
                        ctrl, length, match ? "  *** MATCH ***" : "");
                tried.put("shifts " + shifts + " key=" + keyForm, match);
            }
        }

        String winner = tried.entrySet().stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).findFirst()
                .orElse(null);
        System.out.println();
        if (winner == null) {
            System.out.println("no configuration matched - the capture may not start at the first client frame");
        } else {
            System.out.println("DEVICE CIPHER: " + winner);
            System.out.println("the binding can be aligned to this configuration");
        }
    }

    /**
     * Checks the reader and the cipher against a frame built and encrypted here.
     *
     * <p>
     * A capture from a live device cannot be validated on its own, so the same code path is exercised
     * first with a frame whose plaintext is known. Only if that round trip succeeds are the results for
     * the real capture meaningful.
     */
    private static boolean selfTest() throws Exception {
        byte[] plain = AuthFrames.authFrame("selftest", "selftest");
        RijndaelReference cipher = new RijndaelReference(new int[] { 0, 1, 3, 4 }, "raw", "selftestkey");
        byte[] encrypted = cbcEncrypt(cipher, plain);
        byte[] packet = ethernetPacket(encrypted, 5033);
        List<byte[]> recovered = extract(pcapFile(packet), 5033);
        if (recovered.size() != 1) {
            System.out.println("self test: expected one payload, found " + recovered.size());
            return false;
        }
        byte[] block = cipher.decryptCbc(Arrays.copyOf(recovered.get(0), BLOCK));
        int magic = ByteBuffer.wrap(block).order(ByteOrder.LITTLE_ENDIAN).getShort(0) & 0xFFFF;
        if (magic != RSCP_MAGIC) {
            System.out.printf("self test: recovered %d bytes, first block %s, magic 0x%04X%n",
                    recovered.get(0).length, BlockCheck.toHex(Arrays.copyOf(recovered.get(0), BLOCK)), magic);
            System.out.println("self test: expected plaintext " + BlockCheck.toHex(Arrays.copyOf(plain, BLOCK)));
        }
        return magic == RSCP_MAGIC;
    }

    static byte[] cbcEncrypt(RijndaelReference cipher, byte[] frame) {
        int padded = ((frame.length + BLOCK - 1) / BLOCK) * BLOCK;
        byte[] input = Arrays.copyOf(frame, padded);
        byte[] out = new byte[padded];
        byte[] chain = new byte[BLOCK];
        Arrays.fill(chain, (byte) 0xFF);
        byte[] block = new byte[BLOCK];
        for (int offset = 0; offset < padded; offset += BLOCK) {
            for (int i = 0; i < BLOCK; i++) {
                block[i] = (byte) (input[offset + i] ^ chain[i]);
            }
            byte[] enc = cipher.encryptBlock(block, "");
            System.arraycopy(enc, 0, out, offset, BLOCK);
            System.arraycopy(enc, 0, chain, 0, BLOCK);
        }
        return out;
    }

    /** Lays a payload out as Ethernet, IPv4 and TCP. */
    static byte[] ethernetPacket(byte[] payload, int port) {
        byte[] ip = new byte[20];
        ip[0] = 0x45;
        int total = 20 + 20 + payload.length;
        ip[2] = (byte) (total >> 8);
        ip[3] = (byte) total;
        ip[8] = 64;
        ip[9] = 6;
        ip[12] = (byte) 192;
        ip[13] = (byte) 168;
        ip[14] = 1;
        ip[15] = 50;
        ip[16] = (byte) 192;
        ip[17] = (byte) 168;
        ip[18] = 1;
        ip[19] = 30;

        byte[] tcp = new byte[20];
        tcp[2] = (byte) (port >> 8);
        tcp[3] = (byte) port;
        tcp[12] = 0x50;

        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        out.writeBytes(new byte[12]);
        out.writeBytes(new byte[] { 0x08, 0x00 });
        out.writeBytes(ip);
        out.writeBytes(tcp);
        out.writeBytes(payload);
        return out.toByteArray();
    }

    /** Wraps a packet in a classic little endian pcap file. */
    static byte[] pcapFile(byte[] packet) {
        ByteBuffer out = ByteBuffer.allocate(24 + 16 + packet.length).order(ByteOrder.LITTLE_ENDIAN);
        out.putInt(0xA1B2C3D4).putShort((short) 2).putShort((short) 4);
        out.putInt(0).putInt(0).putInt(65535).putInt(1);
        out.putInt(1).putInt(0).putInt(packet.length).putInt(packet.length);
        out.put(packet);
        return out.array();
    }

    /**
     * Reads a classic pcap file and collects the TCP payloads on the given port.
     *
     * <p>
     * The pcap global header may be written in either byte order, which the magic number at offset zero
     * tells apart. The encapsulated packet headers are a different matter: IPv4 and TCP always use
     * network byte order, so they are read as big endian regardless of the file's own order. Mixing the
     * two up yields lengths in the tens of thousands and the payload is never found.
     */
    private static List<byte[]> extract(byte[] raw, int port) {
        List<byte[]> payloads = new ArrayList<>();
        if (raw == null || raw.length < 24) {
            return payloads;
        }
        int fileMagic = ByteBuffer.wrap(raw, 0, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        if (fileMagic != 0xA1B2C3D4 && fileMagic != 0xD4C3B2A1) {
            return payloads; // not a classic pcap file
        }
        // if reading little endian produced the swapped constant, the file is big endian
        ByteOrder fileOrder = fileMagic == 0xA1B2C3D4 ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
        ByteBuffer header = ByteBuffer.wrap(raw).order(fileOrder);
        ByteBuffer packet = ByteBuffer.wrap(raw).order(ByteOrder.BIG_ENDIAN);
        long network = header.getInt(20) & 0xFFFFFFFFL;
        int offset = 24;
        while (offset + 16 <= raw.length) {
            long included = header.getInt(offset + 8) & 0xFFFFFFFFL;
            int record = offset + 16;
            if (record + included > raw.length) {
                break;
            }
            if (network == 1 || network == 113 || network == 276) { // Ethernet or Linux cooked v1 / v2
                int ip = PcapList.ipOffset(raw, record, network);
                if (ip > 0 && ip + 20 <= raw.length) {
                    int protocol = packet.get(ip + 9) & 0xFF;
                    int ihl = (packet.get(ip) & 0x0F) * 4;
                    if (protocol == 6) { // TCP
                        int tcp = ip + ihl;
                        if (tcp + 20 <= raw.length) {
                            int sourcePort = packet.getShort(tcp) & 0xFFFF;
                            int destPort = packet.getShort(tcp + 2) & 0xFFFF;
                            int dataOffset = ((packet.get(tcp + 12) & 0xF0) >> 4) * 4;
                            int totalLength = packet.getShort(ip + 2) & 0xFFFF;
                            int dataLength = totalLength - ihl - dataOffset;
                            int dataStart = tcp + dataOffset;
                            if ((sourcePort == port || destPort == port) && dataLength > 0
                                    && dataStart + dataLength <= raw.length) {
                                byte[] payload = new byte[dataLength];
                                System.arraycopy(raw, dataStart, payload, 0, dataLength);
                                payloads.add(payload);
                            }
                        }
                    }
                }
            }
            offset = record + (int) included;
        }
        return payloads;
    }

    private CaptureCheck() {
    }
}
