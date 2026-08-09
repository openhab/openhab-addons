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
package org.openhab.binding.atv.internal.client.auth;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.exceptions.AuthenticationError;
import org.openhab.binding.atv.internal.client.exceptions.InvalidStateError;
import org.openhab.binding.atv.internal.client.support.Chacha20Cipher;
import org.openhab.binding.atv.internal.client.support.CryptoKeys;
import org.openhab.binding.atv.internal.client.support.Hkdf;
import org.openhab.binding.atv.internal.client.support.Opack;
import org.openhab.binding.atv.internal.client.support.Tlv8;
import org.openhab.binding.atv.internal.client.support.Tlv8.TlvValue;

/**
 * Client-side HAP Pair-Setup state machine (M1-M6).
 *
 * <p>
 * Transport agnostic: every step consumes and produces raw TLV8 byte arrays which MRP wraps
 * in protobuf, Companion in OPACK and AirPlay in HTTP bodies.
 *
 * <p>
 * Regular flow: {@link #step1()} (M1) → {@link #step2(byte[], String)} (M2 in, M3 out) →
 * {@link #step3(byte[])} (M4 in, M5 out) → {@link #step4(byte[])} (M6 in, credentials out).
 *
 * <p>
 * Transient flow: pairing stops after M4 — call {@link #step1()} and
 * {@link #step2(byte[], String)} with {@link #TRANSIENT_PIN}, send M3, then derive session
 * keys directly from the SRP shared secret via {@link #encryptionKeys(String, String, String)}.
 * M1 then carries the extra {@code Flags} entry with {@code TransientPairing} (0x10).
 *
 * <p>
 * Note: the device proof in M4 is <em>not</em> verified against the SRP server proof (only
 * an error entry is checked), and {@link #step4(byte[])} does <em>not</em> verify the device
 * signature in M6.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class HapPairSetup implements PairVerifyProcedure {

    /** Fixed PIN used by transient pairing. */
    public static final String TRANSIENT_PIN = "3939";

    private enum Stage {
        INITIAL,
        M1_SENT,
        M3_SENT,
        M5_SENT
    }

    private final boolean transientMode;
    private final @Nullable String name;
    private final byte[] authPrivate;
    private final byte[] authPublic;
    private final byte[] pairingId;
    private final HapSrpClient srp;

    private Stage stage = Stage.INITIAL;
    private byte @Nullable [] sessionKey;

    /** Creates a regular (non-transient) pair-setup with fresh random keys. */
    public HapPairSetup() {
        this(false);
    }

    /**
     * Creates a pair-setup with fresh random keys.
     *
     * @param transientMode true for transient pairing (M1 carries the transient flag and
     *            the flow stops after M4)
     */
    public HapPairSetup(boolean transientMode) {
        this(transientMode, null, CryptoKeys.ed25519Generate().seed(),
                UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Creates a pair-setup with injectable key material, for reproducible tests.
     *
     * @param transientMode true for transient pairing
     * @param name optional display name included in M5 as an OPACK-packed {@code Name}
     *            entry, or {@code null}
     * @param ed25519Seed the 32-byte Ed25519 signing seed; also used as the SRP client
     *            private ephemeral
     * @param pairingId our pairing identifier bytes (typically the ASCII bytes of a
     *            random UUID string)
     */
    public HapPairSetup(boolean transientMode, @Nullable String name, byte[] ed25519Seed, byte[] pairingId) {
        this.transientMode = transientMode;
        this.name = name;
        this.authPrivate = ed25519Seed.clone();
        this.authPublic = CryptoKeys.ed25519PublicKey(authPrivate);
        this.pairingId = pairingId.clone();
        this.srp = new HapSrpClient(supplierOf(authPrivate));
    }

    private static Supplier<byte[]> supplierOf(byte[] bytes) {
        return bytes::clone;
    }

    /** Our pairing identifier bytes. */
    public byte[] pairingId() {
        return pairingId.clone();
    }

    /**
     * First pairing step: produces the M1 TLV starting pair-setup
     * ({@code Method=PairSetup}, {@code SeqNo=M1} and, in transient mode, the
     * {@code Flags=TransientPairing} entry).
     *
     * @return M1 TLV bytes
     */
    public byte[] step1() {
        expectStage(Stage.INITIAL, "step1");
        Map<Integer, byte[]> tlv = new LinkedHashMap<>();
        tlv.put(TlvValue.Method.value(), new byte[] { (byte) Tlv8.Method.PairSetup.value() });
        tlv.put(TlvValue.SeqNo.value(), new byte[] { (byte) Tlv8.State.M1.value() });
        if (transientMode) {
            tlv.put(TlvValue.Flags.value(), new byte[] { (byte) Tlv8.Flags.TransientPairing.value() });
        }
        stage = Stage.M1_SENT;
        return Tlv8.write(tlv);
    }

    /**
     * Second pairing step: consumes M2 (device salt and SRP public key), runs the SRP
     * handshake with the PIN and produces the M3 TLV (our public key and proof).
     *
     * @param m2Tlv M2 TLV bytes received from the device
     * @param pin the PIN shown on screen ({@link #TRANSIENT_PIN} for transient pairing)
     * @return M3 TLV bytes
     * @throws AuthenticationError if M2 contains an error entry or the device public key
     *             is invalid
     */
    public byte[] step2(byte[] m2Tlv, String pin) {
        expectStage(Stage.M1_SENT, "step2");
        Map<Integer, byte[]> pairingData = HapTlv.readChecked(m2Tlv);
        byte[] atvSalt = HapTlv.required(pairingData, TlvValue.Salt);
        byte[] atvPubKey = HapTlv.required(pairingData, TlvValue.PublicKey);

        srp.step1(pin);
        srp.process(atvPubKey, atvSalt);

        Map<Integer, byte[]> tlv = new LinkedHashMap<>();
        tlv.put(TlvValue.SeqNo.value(), new byte[] { (byte) Tlv8.State.M3.value() });
        tlv.put(TlvValue.PublicKey.value(), srp.publicKey());
        tlv.put(TlvValue.Proof.value(), srp.proof());
        stage = Stage.M3_SENT;
        return Tlv8.write(tlv);
    }

    /**
     * Third pairing step: consumes M4 (only checking for an error entry — the device proof
     * is not verified, see class javadoc) and produces the M5 TLV.
     *
     * @param m4Tlv M4 TLV bytes received from the device
     * @return M5 TLV bytes
     * @throws AuthenticationError if M4 contains an error entry
     */
    public byte[] step3(byte[] m4Tlv) {
        HapTlv.readChecked(m4Tlv);
        return step3();
    }

    /**
     * Third pairing step without consuming M4: produces the M5 TLV containing our
     * identifier, long-term public key and signature, encrypted with ChaCha20-Poly1305
     * (nonce {@code "PS-Msg05"}) under the {@code Pair-Setup-Encrypt} session key.
     *
     * @return M5 TLV bytes
     */
    public byte[] step3() {
        expectStage(Stage.M3_SENT, "step3");
        byte[] srpKey = srp.sessionKey();

        byte[] iosDeviceX = Hkdf.expand("Pair-Setup-Controller-Sign-Salt", "Pair-Setup-Controller-Sign-Info", srpKey);
        byte[] localSessionKey = Hkdf.expand("Pair-Setup-Encrypt-Salt", "Pair-Setup-Encrypt-Info", srpKey);
        this.sessionKey = localSessionKey;

        byte[] deviceInfo = concat(iosDeviceX, pairingId, authPublic);
        byte[] deviceSignature = CryptoKeys.ed25519Sign(authPrivate, deviceInfo);

        Map<Integer, byte[]> subTlv = new LinkedHashMap<>();
        subTlv.put(TlvValue.Identifier.value(), pairingId);
        subTlv.put(TlvValue.PublicKey.value(), authPublic);
        subTlv.put(TlvValue.Signature.value(), deviceSignature);
        String localName = name;
        if (localName != null) {
            subTlv.put(TlvValue.Name.value(), Opack.pack(Map.of("name", localName)));
        }

        Chacha20Cipher chacha = new Chacha20Cipher(localSessionKey, localSessionKey);
        byte[] encryptedData = chacha.encrypt(Tlv8.write(subTlv), "PS-Msg05".getBytes(StandardCharsets.UTF_8), null);

        Map<Integer, byte[]> tlv = new LinkedHashMap<>();
        tlv.put(TlvValue.SeqNo.value(), new byte[] { (byte) Tlv8.State.M5.value() });
        tlv.put(TlvValue.EncryptedData.value(), encryptedData);
        stage = Stage.M5_SENT;
        return Tlv8.write(tlv);
    }

    /**
     * Last pairing step: consumes M6, decrypts the device sub-TLV (nonce
     * {@code "PS-Msg06"}) and assembles the resulting credentials. The device signature
     * is <em>not</em> verified here.
     *
     * @param m6Tlv M6 TLV bytes received from the device
     * @return the new credentials (device LTPK, our LTSK/seed, device id, our pairing id)
     * @throws AuthenticationError if M6 contains an error entry or decryption fails
     */
    public HapCredentials step4(byte[] m6Tlv) {
        expectStage(Stage.M5_SENT, "step4");
        Map<Integer, byte[]> pairingData = HapTlv.readChecked(m6Tlv);
        byte[] encryptedData = HapTlv.required(pairingData, TlvValue.EncryptedData);

        byte[] localSessionKey = Objects.requireNonNull(sessionKey, "step3 must be called first");
        Chacha20Cipher chacha = new Chacha20Cipher(localSessionKey, localSessionKey);
        byte[] decryptedTlvBytes = chacha.decrypt(encryptedData, "PS-Msg06".getBytes(StandardCharsets.UTF_8), null);
        if (decryptedTlvBytes.length == 0) {
            throw new AuthenticationError("data decrypt failed");
        }

        Map<Integer, byte[]> decryptedTlv = Tlv8.read(decryptedTlvBytes);
        byte[] atvIdentifier = HapTlv.required(decryptedTlv, TlvValue.Identifier);
        byte[] atvPubKey = HapTlv.required(decryptedTlv, TlvValue.PublicKey);
        HapTlv.required(decryptedTlv, TlvValue.Signature); // present but not verified

        stage = Stage.INITIAL;
        return new HapCredentials(atvPubKey, authPrivate, atvIdentifier, pairingId);
    }

    /**
     * Verifies the device SRP proof received in M4 against the expected server proof.
     * Not called by the regular flow (which ignores the M4 proof); provided for callers
     * that want the extra check.
     *
     * @param proof the device proof from M4
     * @return true if the proof matches
     */
    public boolean verifyDeviceProof(byte[] proof) {
        return srp.verifyServerProof(proof);
    }

    /** Derives session keys directly from the SRP shared secret, used by transient pairing after M4. */
    @Override
    public EncryptionKeys encryptionKeys(String salt, String outputInfo, String inputInfo) {
        if (stage == Stage.INITIAL || stage == Stage.M1_SENT) {
            throw new InvalidStateError("SRP handshake not completed");
        }
        byte[] shared = srp.sessionKey();
        byte[] outputKey = Hkdf.expand(salt, outputInfo, shared);
        byte[] inputKey = Hkdf.expand(salt, inputInfo, shared);
        return new EncryptionKeys(outputKey, inputKey);
    }

    private void expectStage(Stage expected, String step) {
        if (stage != expected) {
            throw new InvalidStateError(step + " called in wrong state: " + stage);
        }
    }

    private static byte[] concat(byte[]... parts) {
        int length = 0;
        for (byte[] part : parts) {
            length += part.length;
        }
        byte[] result = new byte[length];
        int pos = 0;
        for (byte[] part : parts) {
            System.arraycopy(part, 0, result, pos, part.length);
            pos += part.length;
        }
        return result;
    }
}
