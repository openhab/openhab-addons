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
import java.util.Arrays;

/**
 * A self contained Rijndael-256 with selectable row shifts and key derivation.
 *
 * <p>
 * It exists so a candidate implementation can be compared against captured traffic without going
 * through the binding. The round structure follows the Rijndael specification for a 256 bit block:
 * 14 rounds, four rows of eight bytes, the {@code Nk = 8} key schedule with the extra {@code SubWord}
 * every eighth word.
 *
 * @author Bernd Weymann - Initial contribution
 */
public final class RijndaelReference {

    private static final int BLOCK = 32;
    private static final int ROUNDS = 14;

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

    private static int xtime(int b) {
        b <<= 1;
        if ((b & 0x100) != 0) {
            b ^= 0x11B;
        }
        return b & 0xFF;
    }

    private static int gmul(int a, int b) {
        int p = 0;
        for (int i = 0; i < 8; i++) {
            if ((b & 1) != 0) {
                p ^= a;
            }
            boolean hi = (a & 0x80) != 0;
            a = (a << 1) & 0xFF;
            if (hi) {
                a ^= 0x1B;
            }
            b >>= 1;
        }
        return p;
    }

    private final int[] shifts;
    private final String keyForm;
    private final int[][] roundKeys;

    /** Builds a reference cipher with explicit key material, bypassing password derivation. */
    public static RijndaelReference withKeyMaterial(int[] shifts, byte[] keyMaterial) {
        return new RijndaelReference(shifts, keyMaterial);
    }

    private RijndaelReference(int[] shifts, byte[] keyMaterial) {
        this.shifts = shifts;
        this.keyForm = "explicit";
        byte[] key = new byte[BLOCK];
        Arrays.fill(key, (byte) 0xFF);
        System.arraycopy(keyMaterial, 0, key, 0, Math.min(keyMaterial.length, BLOCK));
        this.roundKeys = expandKey(key);
    }

    public RijndaelReference(int[] shifts, String keyForm, String password) throws Exception {
        this.shifts = shifts;
        this.keyForm = keyForm;
        this.roundKeys = expandKey(deriveKey(password));
    }

    /** Derives the 32 byte cipher key from the RSCP password. */
    static byte[] deriveKey(String password, String form) throws Exception {
        byte[] raw = password.getBytes(StandardCharsets.UTF_8);
        byte[] material;
        switch (form) {
            case "md5hex": {
                byte[] digest = java.security.MessageDigest.getInstance("MD5").digest(raw);
                StringBuilder hex = new StringBuilder();
                for (byte b : digest) {
                    hex.append(String.format("%02x", b));
                }
                material = hex.toString().getBytes(StandardCharsets.UTF_8);
                break;
            }
            case "md5raw":
                material = java.security.MessageDigest.getInstance("MD5").digest(raw);
                break;
            case "sha256":
                material = java.security.MessageDigest.getInstance("SHA-256").digest(raw);
                break;
            default:
                material = raw;
                break;
        }
        byte[] key = new byte[BLOCK];
        Arrays.fill(key, (byte) 0xFF);
        System.arraycopy(material, 0, key, 0, Math.min(material.length, BLOCK));
        return key;
    }

    private byte[] deriveKey(String password) throws Exception {
        return deriveKey(password, keyForm);
    }

    private static int[][] expandKey(byte[] key) {
        int nk = 8;
        int[] w = new int[4 * (ROUNDS + 1)];
        for (int i = 0; i < nk; i++) {
            w[i] = ((key[4 * i] & 0xFF) << 24) | ((key[4 * i + 1] & 0xFF) << 16) | ((key[4 * i + 2] & 0xFF) << 8)
                    | (key[4 * i + 3] & 0xFF);
        }
        int rcon = 1;
        for (int i = nk; i < w.length; i++) {
            int temp = w[i - 1];
            if (i % nk == 0) {
                temp = subWord(rotWord(temp)) ^ (rcon << 24);
                rcon = xtime(rcon);
            } else if (i % nk == 4) {
                temp = subWord(temp);
            }
            w[i] = w[i - nk] ^ temp;
        }
        int[][] keys = new int[ROUNDS + 1][BLOCK];
        for (int r = 0; r <= ROUNDS; r++) {
            for (int c = 0; c < 4; c++) {
                int word = w[4 * r + c];
                keys[r][4 * c] = (word >>> 24) & 0xFF;
                keys[r][4 * c + 1] = (word >>> 16) & 0xFF;
                keys[r][4 * c + 2] = (word >>> 8) & 0xFF;
                keys[r][4 * c + 3] = word & 0xFF;
            }
        }
        return keys;
    }

    private static int subWord(int w) {
        return (SBOX[(w >>> 24) & 0xFF] << 24) | (SBOX[(w >>> 16) & 0xFF] << 16) | (SBOX[(w >>> 8) & 0xFF] << 8)
                | SBOX[w & 0xFF];
    }

    private static int rotWord(int w) {
        return (w << 8) | (w >>> 24);
    }

    private void shiftRows(int[] s) {
        for (int r = 1; r < 4; r++) {
            int shift = shifts[r];
            int[] row = new int[8];
            for (int c = 0; c < 8; c++) {
                row[c] = s[4 * c + r];
            }
            for (int c = 0; c < 8; c++) {
                s[4 * c + r] = row[(c + shift) % 8];
            }
        }
    }

    private static void mixColumns(int[] s) {
        for (int c = 0; c < 8; c++) {
            int i = 4 * c;
            int a0 = s[i];
            int a1 = s[i + 1];
            int a2 = s[i + 2];
            int a3 = s[i + 3];
            s[i] = gmul(a0, 2) ^ gmul(a1, 3) ^ a2 ^ a3;
            s[i + 1] = a0 ^ gmul(a1, 2) ^ gmul(a2, 3) ^ a3;
            s[i + 2] = a0 ^ a1 ^ gmul(a2, 2) ^ gmul(a3, 3);
            s[i + 3] = gmul(a0, 3) ^ a1 ^ a2 ^ gmul(a3, 2);
        }
    }

    /** Encrypts a single block. The caller applies any chaining. */
    public byte[] encryptBlock(byte[] plain, String password) {
        int[] s = new int[BLOCK];
        for (int i = 0; i < BLOCK; i++) {
            s[i] = plain[i] & 0xFF;
        }
        xor(s, roundKeys[0]);
        for (int r = 1; r < ROUNDS; r++) {
            for (int i = 0; i < BLOCK; i++) {
                s[i] = SBOX[s[i]];
            }
            shiftRows(s);
            mixColumns(s);
            xor(s, roundKeys[r]);
        }
        for (int i = 0; i < BLOCK; i++) {
            s[i] = SBOX[s[i]];
        }
        shiftRows(s);
        xor(s, roundKeys[ROUNDS]);
        return toBytes(s);
    }

    /** Decrypts a single block. The caller applies any chaining. */
    public byte[] decryptBlock(byte[] encrypted, String password) {
        int[] s = new int[BLOCK];
        for (int i = 0; i < BLOCK; i++) {
            s[i] = encrypted[i] & 0xFF;
        }
        xor(s, roundKeys[ROUNDS]);
        for (int r = ROUNDS - 1; r >= 1; r--) {
            invShiftRows(s);
            for (int i = 0; i < BLOCK; i++) {
                s[i] = INV_SBOX[s[i]];
            }
            xor(s, roundKeys[r]);
            invMixColumns(s);
        }
        invShiftRows(s);
        for (int i = 0; i < BLOCK; i++) {
            s[i] = INV_SBOX[s[i]];
        }
        xor(s, roundKeys[0]);
        return toBytes(s);
    }

    private static byte[] toBytes(int[] state) {
        byte[] out = new byte[BLOCK];
        for (int i = 0; i < BLOCK; i++) {
            out[i] = (byte) state[i];
        }
        return out;
    }

    private void invShiftRows(int[] s) {
        for (int r = 1; r < 4; r++) {
            int shift = shifts[r];
            int[] row = new int[8];
            for (int c = 0; c < 8; c++) {
                row[c] = s[4 * c + r];
            }
            for (int c = 0; c < 8; c++) {
                s[4 * c + r] = row[(c - shift + 8) % 8];
            }
        }
    }

    private static void invMixColumns(int[] s) {
        for (int c = 0; c < 8; c++) {
            int i = 4 * c;
            int a0 = s[i];
            int a1 = s[i + 1];
            int a2 = s[i + 2];
            int a3 = s[i + 3];
            s[i] = gmul(a0, 14) ^ gmul(a1, 11) ^ gmul(a2, 13) ^ gmul(a3, 9);
            s[i + 1] = gmul(a0, 9) ^ gmul(a1, 14) ^ gmul(a2, 11) ^ gmul(a3, 13);
            s[i + 2] = gmul(a0, 13) ^ gmul(a1, 9) ^ gmul(a2, 14) ^ gmul(a3, 11);
            s[i + 3] = gmul(a0, 11) ^ gmul(a1, 13) ^ gmul(a2, 9) ^ gmul(a3, 14);
        }
    }

    /** Encrypts in CBC mode with the all 0xFF start vector the protocol uses. */
    public byte[] encryptCbc(byte[] plain) {
        int padded = ((plain.length + BLOCK - 1) / BLOCK) * BLOCK;
        byte[] input = Arrays.copyOf(plain, padded);
        byte[] out = new byte[padded];
        byte[] chain = new byte[BLOCK];
        Arrays.fill(chain, (byte) 0xFF);
        byte[] block = new byte[BLOCK];
        for (int offset = 0; offset < padded; offset += BLOCK) {
            for (int i = 0; i < BLOCK; i++) {
                block[i] = (byte) (input[offset + i] ^ chain[i]);
            }
            byte[] encrypted = encryptBlock(block, "");
            System.arraycopy(encrypted, 0, out, offset, BLOCK);
            System.arraycopy(encrypted, 0, chain, 0, BLOCK);
        }
        return out;
    }

    /** Decrypts in CBC mode with the all 0xFF start vector, inverting {@link #encryptCbc}. */
    public byte[] decryptCbc(byte[] encrypted) {
        int length = (encrypted.length / BLOCK) * BLOCK;
        byte[] out = new byte[length];
        byte[] chain = new byte[BLOCK];
        Arrays.fill(chain, (byte) 0xFF);
        byte[] current = new byte[BLOCK];
        for (int offset = 0; offset < length; offset += BLOCK) {
            System.arraycopy(encrypted, offset, current, 0, BLOCK);
            byte[] decrypted = decryptBlock(current, "");
            for (int i = 0; i < BLOCK; i++) {
                out[offset + i] = (byte) (decrypted[i] ^ chain[i]);
            }
            System.arraycopy(current, 0, chain, 0, BLOCK);
        }
        return out;
    }

    private static void xor(int[] state, int[] key) {
        for (int i = 0; i < BLOCK; i++) {
            state[i] ^= key[i];
        }
    }
}
