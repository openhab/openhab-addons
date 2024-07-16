/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.dsmr.internal.device;

import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dsmr.internal.DSMRBindingConstants;
import org.openhab.binding.dsmr.internal.device.connector.DSMRErrorStatus;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1TelegramListener;
import org.openhab.binding.dsmr.internal.device.p1telegram.TelegramParser;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decodes messages send by The Luxembourgian Smart Meter "Smarty".
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class SmartyDecrypter implements TelegramParser {

    private enum State {
        WAITING_FOR_START_BYTE,
        READ_SYSTEM_TITLE_LENGTH,
        READ_SYSTEM_TITLE,
        READ_SEPARATOR_82,
        READ_PAYLOAD_LENGTH,
        READ_SEPARATOR_30,
        READ_FRAME_COUNTER,
        READ_PAYLOAD,
        READ_GCM_TAG
    }

    private static final byte START_BYTE = (byte) 0xDB;
    private static final byte SEPARATOR_82 = (byte) 0x82;
    private static final byte SEPARATOR_30 = 0x30;
    private static final int ADD_LENGTH = 17;
    private static final int IV_BUFFER_LENGTH = 40;
    private static final int GCM_TAG_LENGTH = 12;
    private static final int GCM_BITS = GCM_TAG_LENGTH * Byte.SIZE;
    private static final int MESSAGES_BUFFER_SIZE = 4096;
    private static final String ADDITIONAL_ADD_PREFIX = "30";

    private final Logger logger = LoggerFactory.getLogger(SmartyDecrypter.class);
    private final ByteBuffer iv = ByteBuffer.allocate(IV_BUFFER_LENGTH);
    private final ByteBuffer cipherText = ByteBuffer.allocate(MESSAGES_BUFFER_SIZE);
    private final TelegramParser parser;
    private @Nullable final SecretKeySpec secretKeySpec;

    private State state = State.WAITING_FOR_START_BYTE;
    private int currentBytePosition;
    private int changeToNextStateAt;
    private int ivLength;
    private int dataLength;
    private boolean lenientMode;
    private final P1TelegramListener telegramListener;
    private final byte[] addKey;

    /**
     * Constructor.
     *
     * @param parser parser of the Cosem messages
     * @param telegramListener
     * @param decryptionKey The key to decrypt the messages
     * @param additionalKey Additional optional key to decrypt the message
     */
    public SmartyDecrypter(final TelegramParser parser, final P1TelegramListener telegramListener,
            final String decryptionKey, final String additionalKey) {
        this.parser = parser;
        this.telegramListener = telegramListener;
        secretKeySpec = decryptionKey.isEmpty() ? null : new SecretKeySpec(HexUtils.hexToBytes(decryptionKey), "AES");
        addKey = HexUtils.hexToBytes(additionalKey.isBlank() ? DSMRBindingConstants.CONFIGURATION_ADDITIONAL_KEY_DEFAULT
                : ((additionalKey.length() == 32 ? (ADDITIONAL_ADD_PREFIX) : "") + additionalKey));
    }

    @Override
    public void parse(final byte[] data, final int length) {
        for (int i = 0; i < length; i++) {
            currentBytePosition++;
            if (processStateActions(data[i])) {
                processCompleted();
            }
        }
        if (lenientMode && secretKeySpec == null) {
            parser.parse(data, length);
        }
    }

    private boolean processStateActions(final byte rawInput) {
        // Safeguard against buffer overrun in case corrupt data is received.
        if (ivLength == IV_BUFFER_LENGTH) {
            reset();
            return false;
        }
        switch (state) {
            case WAITING_FOR_START_BYTE:
                if (rawInput == START_BYTE) {
                    reset();
                    state = State.READ_SYSTEM_TITLE_LENGTH;
                }
                break;
            case READ_SYSTEM_TITLE_LENGTH:
                state = State.READ_SYSTEM_TITLE;
                // 2 start bytes (position 0 and 1) + system title length
                changeToNextStateAt = 1 + rawInput;
                break;
            case READ_SYSTEM_TITLE:
                iv.put(rawInput);
                ivLength++;
                if (currentBytePosition >= changeToNextStateAt) {
                    state = State.READ_SEPARATOR_82;
                    changeToNextStateAt++;
                }
                break;
            case READ_SEPARATOR_82:
                if (rawInput == SEPARATOR_82) {
                    state = State.READ_PAYLOAD_LENGTH; // Ignore separator byte
                    changeToNextStateAt += 2;
                } else {
                    logger.debug("Missing separator (0x82). Dropping telegram.");
                    state = State.WAITING_FOR_START_BYTE;
                }
                break;
            case READ_PAYLOAD_LENGTH:
                dataLength <<= 8;
                dataLength |= rawInput & 0xFF;
                if (currentBytePosition >= changeToNextStateAt) {
                    state = State.READ_SEPARATOR_30;
                    changeToNextStateAt++;
                }
                break;
            case READ_SEPARATOR_30:
                if (rawInput == SEPARATOR_30) {
                    state = State.READ_FRAME_COUNTER;
                    // 4 bytes for frame counter
                    changeToNextStateAt += 4;
                } else {
                    logger.debug("Missing separator (0x30). Dropping telegram.");
                    state = State.WAITING_FOR_START_BYTE;
                }
                break;
            case READ_FRAME_COUNTER:
                iv.put(rawInput);
                ivLength++;
                if (currentBytePosition >= changeToNextStateAt) {
                    state = State.READ_PAYLOAD;
                    changeToNextStateAt += dataLength - ADD_LENGTH;
                }
                break;
            case READ_PAYLOAD:
                cipherText.put(rawInput);
                if (currentBytePosition >= changeToNextStateAt) {
                    state = State.READ_GCM_TAG;
                    changeToNextStateAt += GCM_TAG_LENGTH;
                }
                break;
            case READ_GCM_TAG:
                // All input has been read.
                cipherText.put(rawInput);
                if (currentBytePosition >= changeToNextStateAt) {
                    state = State.WAITING_FOR_START_BYTE;
                    return true;
                }
                break;
        }
        return false;
    }

    private void processCompleted() {
        try {
            final byte[] plainText = decrypt();

            if (plainText != null) {
                parser.parse(plainText, plainText.length);
            }
        } finally {
            reset();
        }
    }

    /**
     * Decrypts the collected message.
     *
     * @return the decrypted message
     */
    private byte @Nullable [] decrypt() {
        try {
            if (secretKeySpec != null) {
                final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                cipher.init(Cipher.DECRYPT_MODE, secretKeySpec,
                        new GCMParameterSpec(GCM_BITS, iv.array(), 0, ivLength));
                cipher.updateAAD(addKey);
                return cipher.doFinal(cipherText.array(), 0, cipherText.position());
            }
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            if (lenientMode || logger.isDebugEnabled()) {
                // log in lenient mode or when debug is enabled. But log to warn to also work when lenientMode is
                // enabled.
                logger.warn("Failed encrypted telegram: {}",
                        HexUtils.bytesToHex(Arrays.copyOf(cipherText.array(), cipherText.position())));
                logger.warn("Exception of failed decryption of telegram: ", e);
            }
            telegramListener.onError(DSMRErrorStatus.INVALID_DECRYPTION_KEY,
                    Objects.requireNonNullElse(e.getMessage(), ""));
        }
        return null;
    }

    @Override
    public void reset() {
        parser.reset();
        state = State.WAITING_FOR_START_BYTE;
        iv.clear();
        cipherText.clear();
        currentBytePosition = 0;
        changeToNextStateAt = 0;
        ivLength = 0;
        dataLength = 0;
    }

    @Override
    public void setLenientMode(final boolean lenientMode) {
        this.lenientMode = lenientMode;
        parser.setLenientMode(lenientMode);
    }
}
