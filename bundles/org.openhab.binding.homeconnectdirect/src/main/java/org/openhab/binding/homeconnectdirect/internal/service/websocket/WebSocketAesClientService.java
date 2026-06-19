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
package org.openhab.binding.homeconnectdirect.internal.service.websocket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.ScheduledExecutorService;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.exception.WebSocketClientServiceException;
import org.openhab.core.thing.Thing;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebSocket client service using AES encryption.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@WebSocket
@NonNullByDefault
public class WebSocketAesClientService extends AbstractWebSocketClientService {

    private static final String HMAC_SHA_256 = "HmacSHA256";
    private static final String AES_CBC_NO_PADDING = "AES/CBC/NoPadding";
    private static final String AES = "AES";
    private static final String ENC = "ENC";
    private static final String MAC = "MAC";

    private final Cipher aesEncrypt;
    private final Cipher aesDecrypt;
    private final byte[] iv;
    private final byte[] macKey;
    private final Logger logger;

    private byte[] lastRxHmac;
    private byte[] lastTxHmac;

    public WebSocketAesClientService(Thing thing, URI uri, String base64EncodedKey,
            String base64EncodedInitializationVector, WebSocketHandler webSocketHandler,
            ScheduledExecutorService scheduler) throws WebSocketClientServiceException {
        super(thing, uri, webSocketHandler, scheduler);

        try {
            logger = LoggerFactory.getLogger(WebSocketAesClientService.class);

            byte[] key = Base64.getUrlDecoder().decode(base64EncodedKey);
            iv = Base64.getUrlDecoder().decode(base64EncodedInitializationVector);

            // init AES
            byte[] encryptionKey = hmac(key, ENC.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec keySpec = new SecretKeySpec(encryptionKey, AES);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            aesEncrypt = Cipher.getInstance(AES_CBC_NO_PADDING);
            aesEncrypt.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            aesDecrypt = Cipher.getInstance(AES_CBC_NO_PADDING);
            aesDecrypt.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            // init HMAC
            macKey = hmac(key, MAC.getBytes(StandardCharsets.UTF_8));
            lastRxHmac = new byte[16];
            lastTxHmac = new byte[16];

            // websocket
            setWebSocketClient(new WebSocketClient(new HttpClient()));
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new WebSocketClientServiceException(e.getMessage(), e);
        } catch (InvalidKeyException e) {
            var message = e.getMessage();
            try {
                if (javax.crypto.Cipher.getMaxAllowedKeyLength(AES) < 256) {
                    message = "The current cryptographic policy is set to 'limited', which restricts the use of stronger encryption algorithms and key lengths. "
                            + "To resolve this issue, ensure that the 'crypto.policy' property is set to 'unlimited' in the 'java.security' file located at: "
                            + "'<JAVA_HOME>/conf/security/java.security'. The unlimited policy is supported natively in your Java version.";
                }
            } catch (NoSuchAlgorithmException ignored) {
                // AES algorithm check failed, use original exception message
            }
            throw new WebSocketClientServiceException(message, e);
        }
    }

    @Override
    public void send(String message) {
        try {
            var encryptedMessage = encrypt(message);
            var session = getSession();

            if (session != null && session.isOpen()) {
                logger.debug(">> {} ({})", message, getThingUID());
                logger.trace(">> {} ({})", HexUtils.bytesToHex(encryptedMessage), getWebSocketHandler());
                ByteBuffer buffer = ByteBuffer.wrap(encryptedMessage);
                session.getRemote().sendBytes(buffer);
            }
        } catch (Exception e) {
            logger.error("Failed to send message! error={} thingUID={}", e.getMessage(), getThingUID());
        }
    }

    @OnWebSocketMessage
    public void onBinaryMessage(Session session, InputStream inputStream)
            throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int bytesRead;

        try (inputStream) {
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
            byte[] message = output.toByteArray();

            logger.trace("<< {} ({})", HexUtils.bytesToHex(message), getThingUID());
            var decryptedMessage = decrypt(message);
            var stringMessage = new String(decryptedMessage, StandardCharsets.UTF_8);
            logger.debug("<< {} ({})", stringMessage, getThingUID());
            getWebSocketHandler().onWebSocketMessage(stringMessage, this);
        }
    }

    private byte[] hmac(byte[] key, byte[] msg) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(HMAC_SHA_256);
        SecretKeySpec secretKey = new SecretKeySpec(key, HMAC_SHA_256);
        mac.init(secretKey);
        return mac.doFinal(msg);
    }

    private byte[] decrypt(byte[] buf) throws NoSuchAlgorithmException, InvalidKeyException {
        if (buf.length < 32) {
            logger.error("Can not decrypt invalid message! Short message? {}", HexUtils.bytesToHex(buf));
        }

        if (buf.length % 16 != 0) {
            logger.error("Unaligned message? Probably bad padding: {}", HexUtils.bytesToHex(buf));
        }

        // Split the message into the encrypted message and the first 16 bytes of the HMAC
        byte[] encryptedMessage = Arrays.copyOfRange(buf, 0, buf.length - 16);
        byte[] applianceHmac = Arrays.copyOfRange(buf, buf.length - 16, buf.length);

        // Compute the expected HMAC on the encrypted message
        byte[] directionAndLastHmac = concatenateByteArrays(new byte[] { 0x43 }, this.lastRxHmac);
        byte[] ourHmac = createHmacMessage(directionAndLastHmac, encryptedMessage);

        if (!Arrays.equals(applianceHmac, ourHmac)) {
            logger.error("HMAC failure! appliance={} ourHmac={}, msgLength={}", HexUtils.bytesToHex(applianceHmac),
                    HexUtils.bytesToHex(ourHmac), buf.length);
        }

        this.lastRxHmac = applianceHmac;

        // Decrypt the message with CBC, so the last message block is mixed in
        byte[] msg;
        msg = aesDecrypt.update(encryptedMessage);

        // Check for padding and trim it off the end
        int padLen = msg[msg.length - 1] & 0xFF; // Convert to unsigned integer
        if (msg.length < padLen) {
            logger.error("Padding error! {}", HexUtils.bytesToHex(msg));
        }
        logger.trace("padding length={}", padLen);

        return Arrays.copyOfRange(msg, 0, msg.length - padLen);
    }

    public byte[] encrypt(String clearMsg) throws NoSuchAlgorithmException, InvalidKeyException {
        // Convert the UTF-8 string into a byte array
        byte[] clearMsgBytes = clearMsg.getBytes(StandardCharsets.UTF_8);
        logger.trace("encrypt: clearMsg={}", HexUtils.bytesToHex(clearMsgBytes));

        // Pad the buffer, adding an extra block if necessary
        int padLen = 16 - (clearMsgBytes.length % 16);
        if (padLen == 1) {
            padLen += 16;
        }
        logger.trace("encrypt: padLen={}", padLen);
        byte[] pad = new byte[padLen];
        pad[0] = 0x00;

        // Generate random bytes for the padding
        SecureRandom random = new SecureRandom();
        byte[] randomBytes = new byte[padLen - 2];
        random.nextBytes(randomBytes);
        System.arraycopy(randomBytes, 0, pad, 1, padLen - 2);

        pad[padLen - 1] = (byte) padLen;

        // Combine clear message and padding
        byte[] paddedMsg = concatenateByteArrays(clearMsgBytes, pad);

        // Encrypt the padded message with CBC, so there is chained state from the last cipher block sent
        byte[] encMsg;
        encMsg = aesEncrypt.update(paddedMsg);

        // Compute the HMAC of the encrypted message, chaining the HMAC of the previous message plus direction 'E'
        byte[] directionAndLastHmac = concatenateByteArrays(new byte[] { 0x45 }, this.lastTxHmac);
        this.lastTxHmac = createHmacMessage(directionAndLastHmac, encMsg);

        // Append the new HMAC to the message
        return concatenateByteArrays(encMsg, this.lastTxHmac);
    }

    // HMAC an inbound or outbound message, chaining the last HMAC
    private byte[] createHmacMessage(byte[] direction, byte[] encMsg)
            throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] hmacMsg = concatenateByteArrays(iv, direction, encMsg);
        byte[] fullHmac = hmac(macKey, hmacMsg);
        return Arrays.copyOfRange(fullHmac, 0, 16);
    }

    private byte[] concatenateByteArrays(byte[]... arrays) {
        int totalLength = 0;
        for (byte[] array : arrays) {
            totalLength += array.length;
        }
        byte[] result = new byte[totalLength];
        int currentIndex = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, currentIndex, array.length);
            currentIndex += array.length;
        }
        return result;
    }
}
