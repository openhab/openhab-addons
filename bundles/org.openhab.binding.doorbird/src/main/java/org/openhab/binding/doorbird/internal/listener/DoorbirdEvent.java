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
package org.openhab.binding.doorbird.internal.listener;

import java.net.DatagramPacket;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.goterl.lazycode.lazysodium.LazySodiumJava;
import com.goterl.lazycode.lazysodium.SodiumJava;
import com.goterl.lazycode.lazysodium.exceptions.SodiumException;
import com.goterl.lazycode.lazysodium.interfaces.PwHash;
import com.sun.jna.NativeLong;

/**
 * The {@link DoorbirdEvent} is responsible for decoding event packets received
 * from the Doorbird.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class DoorbirdEvent {
    private final Logger logger = LoggerFactory.getLogger(DoorbirdEvent.class);

    // These values are extracted from the UDP packet
    private byte version;
    private int opslimit;
    private long memlimit;
    private byte[] salt = new byte[16];
    private byte[] nonce = new byte[8];
    private byte[] ciphertext = new byte[34];

    // Starting 6 characters from the user name
    private @Nullable String eventIntercomId;

    // Doorbell number for doorbell event, or "motion" for motion events
    private @Nullable String eventId;

    // Timestamp of event
    private long eventTimestamp;

    private boolean isDoorbellEvent;

    /*
     * We want a single instance of LazySodium. Also, try to load the libsodium library
     * from multiple sources.
     *
     * To load the libsodium library,
     * - first try to load the library from the resources that are bundled with
     * the LazySodium jar. (i.e. new SodiumJava())
     * - if that fails with an UnsatisfiedLinkError, then try to load the library
     * from the operating system (i.e. new SodiumJava("sodium").
     * - if both of these attempts fail, the binding will be functional, except for
     * its ability to decrypt the UDP events.
     */
    @NonNullByDefault
    private static class LazySodiumJavaHolder {
        private static final Logger LOGGER = LoggerFactory.getLogger(LazySodiumJavaHolder.class);

        static final @Nullable LazySodiumJava LAZY_SODIUM_JAVA_INSTANCE = loadLazySodiumJava();

        private static @Nullable LazySodiumJava loadLazySodiumJava() {
            LOGGER.debug("LazySodium has not been loaded yet. Try to load it now.");
            LazySodiumJava lazySodiumJava = null;
            try {
                lazySodiumJava = new LazySodiumJava(new SodiumJava());
                LOGGER.debug("Successfully loaded bundled libsodium crypto library!!");
            } catch (UnsatisfiedLinkError e1) {
                try {
                    LOGGER.debug("Unable to load bundled libsodium crypto library!! Try to load OS version.", e1);
                    lazySodiumJava = new LazySodiumJava(new SodiumJava("sodium"));
                    LOGGER.debug("Successfully loaded libsodium crypto library from operating system!!");
                } catch (UnsatisfiedLinkError e2) {
                    LOGGER.info("Failed to load libsodium crypto library!!", e2);
                    LOGGER.info("Try manually installing libsodium on your OS if libsodium supports your architecture");
                }
            }
            return lazySodiumJava;
        }
    }

    public static @Nullable LazySodiumJava getLazySodiumJavaInstance() {
        return LazySodiumJavaHolder.LAZY_SODIUM_JAVA_INSTANCE;
    }

    // Will be true if this is a valid Doorbird event
    public boolean isDoorbellEvent() {
        return isDoorbellEvent;
    }

    // Contains the intercomId for valid Doorbird events
    public @Nullable String getIntercomId() {
        return eventIntercomId;
    }

    // Contains the eventId for valid Doorbird events
    public @Nullable String getEventId() {
        return eventId;
    }

    // Contains the timestamp for valid Doorbird events
    public long getTimestamp() {
        return eventTimestamp;
    }

    /*
     * The following functions support the decryption of the doorbell event
     * using the LazySodium wrapper for the libsodium crypto library
     */
    public void decrypt(DatagramPacket p, String password) {
        isDoorbellEvent = false;

        int length = p.getLength();
        byte[] data = Arrays.copyOf(p.getData(), length);

        // A valid event contains a 3 byte signature followed by the decryption version
        if (length < 4) {
            return;
        }

        // Only the first 5 characters of the password are used to generate the decryption key
        if (password.length() < 5) {
            logger.info("Invalid password length, must be at least 5 characters");
            return;
        }
        String passwordFirstFive = password.substring(0, 5);

        try {
            // Load the message into the ByteBuffer
            ByteBuffer bb = ByteBuffer.allocate(length);
            bb.put(data, 0, length);
            bb.rewind();
            // Check for proper event signature
            if (!isValidSignature(bb)) {
                logger.trace("Received event not a doorbell event: {}", new String(data, StandardCharsets.US_ASCII));
                return;
            }
            // Get the decryption version
            version = getVersion(bb);
            if (version == 1) {
                // Decrypt using version 1 decryption scheme
                decryptV1(bb, passwordFirstFive);
            } else {
                logger.info("Don't know how to decrypt version {} doorbell event", version);
            }
        } catch (IndexOutOfBoundsException e) {
            logger.info("IndexOutOfBoundsException decrypting doorbell event", e);
        } catch (BufferUnderflowException e) {
            logger.info("BufferUnderflowException decrypting doorbell event", e);
        }
    }

    private boolean isValidSignature(ByteBuffer bb) throws IndexOutOfBoundsException, BufferUnderflowException {
        // Check the first three bytes for the proper signature
        return (bb.get() & 0xFF) == 0xDE && (bb.get() & 0xFF) == 0xAD && (bb.get() & 0xFF) == 0xBE;
    }

    private byte getVersion(ByteBuffer bb) throws IndexOutOfBoundsException, BufferUnderflowException {
        // Extract the decryption version from the packet
        return bb.get();
    }

    private void decryptV1(ByteBuffer bb, String password5) throws IndexOutOfBoundsException, BufferUnderflowException {
        LazySodiumJava sodium = getLazySodiumJavaInstance();
        if (sodium == null) {
            logger.debug("Unable to decrypt event because libsodium is not loaded");
            return;
        }
        if (bb.capacity() != 70) {
            logger.info("Received malformed version 1 doorbell event, length not 70 bytes");
            return;
        }
        // opslimit and memlimit are 4 bytes each
        opslimit = bb.getInt();
        memlimit = bb.getInt();
        // Get salt, nonce, and ciphertext arrays
        bb.get(salt, 0, salt.length);
        bb.get(nonce, 0, nonce.length);
        bb.get(ciphertext, 0, ciphertext.length);

        // Create the hash, which will be used to decrypt the ciphertext
        byte[] hash;
        try {
            logger.trace("Calling cryptoPwHash with passwordFirstFive='{}', opslimit={}, memlimit={}, salt='{}'",
                    password5, opslimit, memlimit, HexUtils.bytesToHex(salt, " "));
            String hashAsString = sodium.cryptoPwHash(password5, 32, salt, opslimit, new NativeLong(memlimit),
                    PwHash.Alg.PWHASH_ALG_ARGON2I13);
            hash = HexUtils.hexToBytes(hashAsString);
        } catch (SodiumException e) {
            logger.info("Got SodiumException", e);
            return;
        }

        // Set up the variables for the decryption algorithm
        byte[] m = new byte[30];
        long[] mLen = new long[30];
        byte[] nSec = null;
        byte[] c = ciphertext;
        long cLen = ciphertext.length;
        byte[] ad = null;
        long adLen = 0;
        byte[] nPub = nonce;
        byte[] k = hash;

        // Decrypt the ciphertext
        logger.trace("Call cryptoAeadChaCha20Poly1305Decrypt with ciphertext='{}', nonce='{}', key='{}'",
                HexUtils.bytesToHex(ciphertext, " "), HexUtils.bytesToHex(nonce, " "), HexUtils.bytesToHex(k, " "));
        boolean success = sodium.cryptoAeadChaCha20Poly1305Decrypt(m, mLen, nSec, c, cLen, ad, adLen, nPub, k);
        if (!success) {
            /*
             * Don't log at debug level since the decryption will fail for events encrypted with
             * passwords other than the password contained in the thing configuration (reference API
             * documentation for details)
             */
            logger.trace("Decryption FAILED");
            return;
        }
        int decryptedTextLength = (int) mLen[0];
        if (decryptedTextLength != 18L) {
            logger.info("Length of decrypted text is invalid, must be 18 bytes");
            return;
        }
        // Get event fields from decrypted text
        logger.debug("Received and successfully decrypted a Doorbird event!!");
        ByteBuffer b = ByteBuffer.allocate(decryptedTextLength);
        b.put(m, 0, decryptedTextLength);
        b.rewind();
        byte[] buf = new byte[8];
        b.get(buf, 0, 6);
        eventIntercomId = new String(buf, 0, 6).trim();
        b.get(buf, 0, 8);
        eventId = new String(buf, 0, 8).trim();
        eventTimestamp = b.getInt();

        logger.debug("Event is eventId='{}', intercomId='{}', timestamp={}", eventId, eventIntercomId, eventTimestamp);
        isDoorbellEvent = true;
    }
}
