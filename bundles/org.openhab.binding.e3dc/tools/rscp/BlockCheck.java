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
import java.nio.charset.StandardCharsets;

/**
 * Compares candidate Rijndael-256 variants block by block against a captured first block.
 *
 * <p>
 * Only the first cipher block depends on the key, the initialisation vector and the round function,
 * not on any chaining. Comparing it therefore isolates a wrong cipher from a wrong chaining or
 * padding scheme: a candidate whose first block matches the capture is using the right key, the right
 * start vector and the right round function, even if later blocks still differ.
 *
 * <p>
 * Usage: {@code BlockCheck <rscpPassword> <capturedFirstBlockHex> [user] [password]
 *
 * @author Bernd Weymann - Initial contribution
 */
public final class BlockCheck {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("usage: BlockCheck <rscpPassword> <capturedFirstBlockHex> [user] [password]");
            System.exit(2);
        }
        String password = args[0];
        byte[] captured = hex(args[1]);
        String user = args.length > 2 ? args[2] : "local.user";
        String userPassword = args.length > 3 ? args[3] : "imsart";

        byte[] frame = AuthFrames.authFrame(user, userPassword);
        byte[] plainFirstBlock = java.util.Arrays.copyOf(frame, 32);
        System.out.println("plaintext first block : " + toHex(plainFirstBlock));
        System.out.println("captured first block  : " + toHex(captured));
        System.out.println();

        for (String shifts : new String[] { "0134", "0145", "0234", "0123", "0345" }) {
            int[] offsets = new int[] { shifts.charAt(0) - '0', shifts.charAt(1) - '0', shifts.charAt(2) - '0',
                    shifts.charAt(3) - '0' };
            for (String keyForm : new String[] { "raw", "md5hex", "md5raw", "sha256" }) {
                RijndaelReference cipher = new RijndaelReference(offsets, keyForm, password);
                byte[] produced = cipher.encryptBlock(plainFirstBlock, password);
                boolean match = java.util.Arrays.equals(produced, captured);
                System.out.printf("shifts %s key=%-7s -> %s %s%n", shifts, keyForm, toHex(produced),
                        match ? "  *** MATCH ***" : "");
            }
        }
    }

    static byte[] hex(String value) {
        String compact = value.replace(" ", "").replace(":", "");
        byte[] out = new byte[compact.length() / 2];
        for (int i = 0; i < out.length; i++) {
            out[i] = (byte) Integer.parseInt(compact.substring(2 * i, 2 * i + 2), 16);
        }
        return out;
    }

    static String toHex(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length * 2);
        for (byte b : data) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private BlockCheck() {
    }
}
