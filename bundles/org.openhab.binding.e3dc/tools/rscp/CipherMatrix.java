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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tries the realistic Rijndael-256 configurations against a device and reports which one it answers.
 *
 * <p>
 * Every candidate is built on {@link RijndaelReference}, which is checked against a recorded answer
 * vector, so a failure here points at the configuration rather than at the round function. The
 * configurations differ in key derivation, row shifts, chaining and the start vector, because those
 * are the places where implementations of this protocol diverge.
 *
 * <p>
 * Usage: {@code CipherMatrix <host> <rscpPassword> <user> <password> [port]
 *
 * @author Bernd Weymann - Initial contribution
 */
public final class CipherMatrix {

    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.err.println("usage: CipherMatrix <host> <rscpPassword> <user> <password> [port]");
            System.exit(2);
        }
        String host = args[0];
        String rscpPassword = args[1];
        String user = args[2];
        String password = args[3];
        int port = args.length > 4 ? Integer.parseInt(args[4]) : 5033;

        byte[] frame = AuthFrames.authFrame(user, password);
        System.out.printf("frame: %d bytes, %d blocks of 32%n", frame.length, (frame.length + 31) / 32);
        System.out.printf("plaintext first block: %s%n%n", BlockCheck.toHex(Arrays.copyOf(frame, 32)));

        Map<String, byte[]> results = new LinkedHashMap<>();
        for (String shifts : new String[] { "0134", "0145" }) {
            int[] offsets = new int[] { shifts.charAt(0) - '0', shifts.charAt(1) - '0', shifts.charAt(2) - '0',
                    shifts.charAt(3) - '0' };
            for (String keyForm : new String[] { "raw", "md5hex", "md5raw", "sha256" }) {
                RijndaelReference cipher = new RijndaelReference(offsets, keyForm, rscpPassword);
                byte[] encrypted = encrypt(cipher, frame);
                String label = "shifts " + shifts + " key=" + keyForm;
                results.put(label, encrypted);
                probe(host, port, label, encrypted, cipher, rscpPassword);
            }
        }

        System.out.println();
        System.out.println("=== distinct first blocks produced ===");
        Map<String, String> seen = new LinkedHashMap<>();
        for (Map.Entry<String, byte[]> entry : results.entrySet()) {
            String first = BlockCheck.toHex(Arrays.copyOf(entry.getValue(), 32));
            String previous = seen.putIfAbsent(first, entry.getKey());
            System.out.printf("  %s  %s%s%n", first, entry.getKey(),
                    previous == null ? "" : "  (same as " + previous + ")");
        }
        System.out.println();
        System.out.println("Give me any one of these first blocks and I can tell whether my cipher");
        System.out.println("matches your device: only a correct cipher produces the bytes on the wire.");
    }

    /** CBC over the whole frame, padding with zeroes to a whole number of 32 byte blocks. */
    private static byte[] encrypt(RijndaelReference cipher, byte[] frame) {
        int padded = ((frame.length + 31) / 32) * 32;
        byte[] input = Arrays.copyOf(frame, padded);
        byte[] out = new byte[padded];
        byte[] chain = new byte[32];
        Arrays.fill(chain, (byte) 0xFF);
        byte[] block = new byte[32];
        for (int offset = 0; offset < padded; offset += 32) {
            for (int i = 0; i < 32; i++) {
                block[i] = (byte) (input[offset + i] ^ chain[i]);
            }
            byte[] encrypted = cipher.encryptBlock(block, "");
            System.arraycopy(encrypted, 0, out, offset, 32);
            System.arraycopy(encrypted, 0, chain, 0, 32);
        }
        return out;
    }

    private static void probe(String host, int port, String label, byte[] encrypted, RijndaelReference cipher,
            String rscpPassword) {
        System.out.printf("--- %s (%d bytes)%n", label, encrypted.length);
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 5000);
            socket.setSoTimeout(6000);
            socket.getOutputStream().write(encrypted);
            socket.getOutputStream().flush();
            byte[] buffer = new byte[512];
            int read = socket.getInputStream().read(buffer);
            if (read < 0) {
                System.out.println("      closed without an answer");
                return;
            }
            int magic = 0;
            try {
                byte[] plain = cipher.decryptBlock(Arrays.copyOf(buffer, 32), "");
                magic = ByteBuffer.wrap(plain).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0xFFFF;
            } catch (Exception e) {
                // the reply is not necessarily decryptable with a wrong candidate
            }
            System.out.printf("      %d bytes back, magic=0x%04X%s%n", read, magic,
                    magic == 0xE3DC ? "  *** MATCH ***" : "");
        } catch (java.net.SocketTimeoutException e) {
            System.out.println("      no answer within the timeout");
        } catch (Exception e) {
            System.out.println("      error: " + e);
        }
    }

    private CipherMatrix() {
    }
}
