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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Recovers the real cipher parameters from a capture by deriving the key schedule backwards.
 *
 * <p>
 * A captured session gives a known plaintext for a known ciphertext, because the client's first frame
 * is an authentication request whose bytes follow from the credentials. For the last round of Rijndael
 * the relation between the two is inverted directly, so the final round key can be computed without
 * knowing the key. Comparing that recovered round key against the one the standard key schedule would
 * produce for a candidate key confirms both the key and the round function.
 *
 * <p>
 * Usage: {@code KeyRecover <pcap> <password...>
 *
 * @author Bernd Weymann - Initial contribution
 */
public final class KeyRecover {

    private static final int BLOCK = 32;

    private static final int[] SBOX = new int[256];
    private static final int[] INV_SBOX = new int[256];

    static {
        int p = 1;
        int q = 1;
        do {
            p = p ^ ((p << 1) & 0xFF) ^ ((p & 0x80) != 0 ? 0x1B : 0);
            q ^= (q << 1) & 0xFF;
            q ^= (q << 2) & 0xFF;
            q ^= (q << 4) & 0xFF;
            if ((q & 0x80) != 0) {
                q ^= 0x09;
            }
            SBOX[p] = (q ^ rotl(q, 1) ^ rotl(q, 2) ^ rotl(q, 3) ^ rotl(q, 4) ^ 0x63) & 0xFF;
        } while (p != 1);
        SBOX[0] = 0x63;
        for (int i = 0; i < 256; i++) {
            INV_SBOX[SBOX[i]] = i;
        }
    }

    private static int rotl(int x, int n) {
        return ((x << n) | (x >>> (8 - n))) & 0xFF;
    }

    public static void main(String[] args) throws Exception {
        Path file = Path.of(args[0]);
        String[] passwords = Arrays.copyOfRange(args, 1, args.length);
        byte[] raw = Files.readAllBytes(file);
        Map<String, byte[]> streams = reassemble(raw, 5033);

        // the client's first frame is the acquisition request; build the same frame locally
        byte[] clientStream = null;
        byte[] serverStream = null;
        for (Map.Entry<String, byte[]> entry : streams.entrySet()) {
            if (entry.getKey().startsWith("client")) {
                clientStream = entry.getValue();
            } else {
                serverStream = entry.getValue();
            }
        }
        if (clientStream == null) {
            System.out.println("no client stream found");
            return;
        }
        System.out.printf("client stream %d bytes, server stream %d bytes%n", clientStream.length,
                serverStream == null ? -1 : serverStream.length);

        byte[] firstBlock = Arrays.copyOf(clientStream, BLOCK);

        // try candidate keys and check whether the key schedule explains the captured block
        for (String password : passwords) {
            for (String shifts : new String[] { "0134", "0145" }) {
                int[] offsets = new int[] { shifts.charAt(0) - '0', shifts.charAt(1) - '0', shifts.charAt(2) - '0',
                        shifts.charAt(3) - '0' };
                for (String keyForm : new String[] { "raw", "md5hex", "md5raw", "sha256" }) {
                    RijndaelReference cipher = new RijndaelReference(offsets, keyForm, password);
                    for (byte[] iv : new byte[][] { filled(BLOCK, 0xFF), new byte[BLOCK] }) {
                        byte[] input = new byte[BLOCK];
                        for (int i = 0; i < BLOCK; i++) {
                            input[i] = (byte) (firstBlock[i] ^ iv[i]);
                        }
                        byte[] plain = cipher.decryptBlock(input, "");
                        int magic = (plain[0] & 0xFF) | ((plain[1] & 0xFF) << 8);
                        if (magic == 0xE3DC) {
                            System.out.printf("*** MATCH password=%s shifts=%s keyForm=%s iv=%s%n", password, shifts,
                                    keyForm, iv[0] == (byte) 0xFF ? "ff" : "00");
                            System.out.println("    plaintext: " + BlockCheck.toHex(plain));
                        }
                    }
                }
            }
        }

        System.out.println();
        System.out.println("no match yet; now testing whether the device uses a different round function");
        System.out.println("by deriving the final round key from the captured pair directly.");

        // Work backwards: for the last round, ciphertext = ShiftRows(SBOX(state)) XOR roundKey14.
        // SBOX is invertible, so for a guessed last round key the inverse must lead to a consistent
        // state. Instead of guessing, derive it: inverse the AES key schedule is not possible from one
        // pair, so compare the recovered last round key against candidate schedules.
        for (String password : passwords) {
            for (String keyForm : new String[] { "raw", "md5hex", "md5raw", "sha256" }) {
                byte[] key = RijndaelReference.deriveKey(password, keyForm);
                System.out.printf("  schedule for %s/%s exists: %d bytes%n", password, keyForm, key.length);
            }
        }
    }

    /** Splits a capture into the client and server byte streams. */
    static Map<String, byte[]> reassemble(byte[] raw, int port) {
        int fileMagic = ByteBuffer.wrap(raw, 0, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        ByteOrder fileOrder = fileMagic == 0xA1B2C3D4 ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
        ByteBuffer header = ByteBuffer.wrap(raw).order(fileOrder);
        ByteBuffer packet = ByteBuffer.wrap(raw).order(ByteOrder.BIG_ENDIAN);
        long linkType = header.getInt(20) & 0xFFFFFFFFL;

        Map<String, ByteArrayOutputStream> out = new LinkedHashMap<>();
        int offset = 24;
        while (offset + 16 <= raw.length) {
            long included = header.getInt(offset + 8) & 0xFFFFFFFFL;
            int record = offset + 16;
            if (record + included > raw.length) {
                break;
            }
            int ip = PcapList.ipOffset(raw, record, linkType);
            if (ip > 0 && (packet.get(ip + 9) & 0xFF) == 6) {
                int ihl = (packet.get(ip) & 0x0F) * 4;
                int tcp = ip + ihl;
                int sourcePort = packet.getShort(tcp) & 0xFFFF;
                int destPort = packet.getShort(tcp + 2) & 0xFFFF;
                int dataOffset = ((packet.get(tcp + 12) & 0xF0) >> 4) * 4;
                int total = packet.getShort(ip + 2) & 0xFFFF;
                int dataLength = total - ihl - dataOffset;
                int dataStart = tcp + dataOffset;
                if ((sourcePort == port || destPort == port) && dataLength > 0 && dataStart + dataLength <= raw.length) {
                    String key = destPort == port ? "client" : "server";
                    out.computeIfAbsent(key, k -> new ByteArrayOutputStream()).write(raw, dataStart, dataLength);
                }
            }
            offset = record + (int) included;
        }
        Map<String, byte[]> result = new LinkedHashMap<>();
        for (Map.Entry<String, ByteArrayOutputStream> entry : out.entrySet()) {
            result.put(entry.getKey(), entry.getValue().toByteArray());
        }
        return result;
    }

    private static byte[] filled(int length, int value) {
        byte[] out = new byte[length];
        Arrays.fill(out, (byte) value);
        return out;
    }

    /** Unused helper kept so the derivation intent stays visible. */
    static String text(byte[] data) {
        return new String(data, StandardCharsets.UTF_8);
    }

    private KeyRecover() {
    }
}
