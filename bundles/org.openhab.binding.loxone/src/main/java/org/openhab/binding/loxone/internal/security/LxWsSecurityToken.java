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
package org.openhab.binding.loxone.internal.security;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.openhab.binding.loxone.internal.LxServerHandlerApi;
import org.openhab.binding.loxone.internal.LxWebSocket;
import org.openhab.binding.loxone.internal.types.LxErrorCode;
import org.openhab.binding.loxone.internal.types.LxResponse;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.id.InstanceUUID;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParseException;

/**
 * A token-based authentication algorithm with AES-256 encryption and decryption.
 *
 * The encryption algorithm uses public Miniserver key to RSA-encrypt own AES-256 key and initialization vector into a
 * session key. The encrypted session key is sent to the Miniserver. From this point on encryption (and decryption) of
 * the communication is possible and all further commands sent to the Miniserver are encrypted. The encryption makes use
 * of an additional salt value injected into the commands and updated frequently.
 *
 * To get the token, a hash key and salt values that are specific to the user are received from the Miniserver. These
 * values are used to compute a hash over user name and password using Miniserver's salt and key values (combined SHA1
 * and HMAC-SHA1 algorithm). This hash is sent to the Miniserver in an encrypted message to authorize the user and
 * obtain a token.
 *
 * Once a token is obtained, it can be used in all future authorizations instead of hashed user name and password.
 * When a token expires, it is refreshed.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
class LxWsSecurityToken extends LxWsSecurity {
    /**
     * A sub-response value structure that is received as a response to get key-salt request command sent to the
     * Miniserver during authentication procedure.
     *
     * @author Pawel Pieczul - initial contribution
     *
     */
    private class LxResponseKeySalt {
        String key;
        String salt;
        String hashAlg;
    }

    /**
     * A sub-response value structure that is received as a response to token request or token update command sent to
     * the Miniserver during authentication procedure.
     *
     * @author Pawel Pieczul - initial contribution
     *
     */
    private class LxResponseToken {
        String token;
        Integer validUntil;
        Boolean unsecurePass;
        @SuppressWarnings("unused")
        String key;
        @SuppressWarnings("unused")
        Integer tokenRights;
    }

    // length of salt used for encrypting commands
    private static final int SALT_BYTES = 16;
    // after salt aged or reached max use count, a new salt will be generated
    private static final int SALT_MAX_AGE_SECONDS = 60 * 60;
    private static final int SALT_MAX_USE_COUNT = 30;

    // defined by Loxone API, value 4 gives longest token expiration time
    private static final int TOKEN_PERMISSION = 4; // 2=web, 4=app
    // number of attempts for token refresh and delay between them
    private static final int TOKEN_REFRESH_RETRY_COUNT = 5;
    private static final int TOKEN_REFRESH_RETRY_DELAY_SECONDS = 10;
    // token will be refreshed 1 day before its expiration date
    private static final int TOKEN_REFRESH_SECONDS_BEFORE_EXPIRY = 24 * 60 * 60; // 1 day
    // if can't determine token expiration date, it will be refreshed after 2 days
    private static final int TOKEN_REFRESH_DEFAULT_SECONDS = 2 * 24 * 60 * 60; // 2 days

    // AES encryption random initialization vector length
    private static final int IV_LENGTH_BYTES = 16;

    private static final String CMD_GET_KEY_AND_SALT = "jdev/sys/getkey2/";
    private static final String CMD_GET_PUBLIC_KEY = "jdev/sys/getPublicKey";
    private static final String CMD_KEY_EXCHANGE = "jdev/sys/keyexchange/";
    private static final String CMD_REQUEST_TOKEN = "jdev/sys/gettoken/";
    private static final String CMD_GET_KEY = "jdev/sys/getkey";
    private static final String CMD_AUTH_WITH_TOKEN = "authwithtoken/";
    private static final String CMD_REFRESH_TOKEN = "jdev/sys/refreshtoken/";
    private static final String CMD_ENCRYPT_CMD = "jdev/sys/enc/";

    private static final String SETTINGS_TOKEN = "authToken";
    private static final String SETTINGS_PASSWORD = "password";

    private SecretKey aesKey;
    private Cipher aesEncryptCipher;
    private Cipher aesDecryptCipher;
    private SecureRandom secureRandom;
    private String salt;
    private int saltUseCount;
    private long saltTimeStamp;
    private boolean encryptionReady = false;
    private String token;
    private int tokenRefreshRetryCount;
    private ScheduledFuture<?> tokenRefreshTimer;
    private final Lock tokenRefreshLock = new ReentrantLock();
    private boolean sha256 = false;

    private final byte[] initVector = new byte[IV_LENGTH_BYTES];
    private final Logger logger = LoggerFactory.getLogger(LxWsSecurityToken.class);
    private static final ScheduledExecutorService SCHEDULER = ThreadPoolManager
            .getScheduledPool(LxWsSecurityToken.class.getName());

    /**
     * Create a token-based authentication instance.
     *
     * @param debugId instance of the client used for debugging purposes only
     * @param thingHandler API to the thing handler
     * @param socket websocket to perform communication with Miniserver
     * @param user user to authenticate
     * @param password password to authenticate
     */
    LxWsSecurityToken(int debugId, LxServerHandlerApi thingHandler, LxWebSocket socket, String user, String password) {
        super(debugId, thingHandler, socket, user, password);
    }

    @Override
    boolean execute() {
        logger.debug("[{}] Starting token-based authentication.", debugId);
        if (!initialize()) {
            return false;
        }
        if ((token == null || token.isEmpty()) && (password == null || password.isEmpty())) {
            return setError(LxErrorCode.USER_UNAUTHORIZED, "Enter password to acquire token.");
        }
        // Get Miniserver's public key - must be over http, not websocket
        String msg = socket.httpGet(CMD_GET_PUBLIC_KEY);
        LxResponse resp = socket.getResponse(msg);
        if (resp == null) {
            return setError(LxErrorCode.COMMUNICATION_ERROR, "Get public key failed - null response.");
        }
        // RSA cipher to encrypt our AES-256 key using Miniserver's public key
        Cipher rsaCipher = getRsaCipher(resp.getValueAsString());
        if (rsaCipher == null) {
            return false;
        }
        // Generate session key
        byte[] sessionKey = generateSessionKey(rsaCipher);
        if (sessionKey == null) {
            return false;
        }
        // Exchange keys
        resp = socket.sendCmdWithResp(CMD_KEY_EXCHANGE + Base64.getEncoder().encodeToString(sessionKey), true, false);
        if (!checkResponse(resp)) {
            return setError(null, "Key exchange failed.");
        }
        logger.debug("[{}] Keys exchanged.", debugId);
        encryptionReady = true;

        if (token == null || token.isEmpty()) {
            if (!acquireToken()) {
                return false;
            }
            logger.debug("[{}] Authenticated - acquired new token.", debugId);
        } else {
            if (!useToken()) {
                return false;
            }
            logger.debug("[{}] Authenticated - used stored token.", debugId);
        }

        return true;
    }

    @Override
    public String encrypt(String command) {
        if (!encryptionReady) {
            return command;
        }
        String str;
        if (salt != null && newSaltNeeded()) {
            String prevSalt = salt;
            salt = generateSalt();
            str = "nextSalt/" + prevSalt + "/" + salt + "/" + command + "\0";
        } else {
            if (salt == null) {
                salt = generateSalt();
            }
            str = "salt/" + salt + "/" + command + "\0";
        }

        logger.debug("[{}] Command for encryption: {}", debugId, str);
        try {
            String encrypted = Base64.getEncoder()
                    .encodeToString(aesEncryptCipher.doFinal(str.getBytes(StandardCharsets.UTF_8)));
            encrypted = URLEncoder.encode(encrypted, StandardCharsets.UTF_8);
            return CMD_ENCRYPT_CMD + encrypted;
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            logger.warn("[{}] Command encryption failed: {}", debugId, e.getMessage());
            return command;
        }
    }

    @Override
    public String decryptControl(String control) {
        String string = control;
        if (!encryptionReady || !string.startsWith(CMD_ENCRYPT_CMD)) {
            return string;
        }
        string = string.substring(CMD_ENCRYPT_CMD.length());
        try {
            byte[] bytes = Base64.getDecoder().decode(string);
            bytes = aesDecryptCipher.doFinal(bytes);
            string = new String(bytes, StandardCharsets.UTF_8);
            string = string.replaceAll("\0+.*$", "");
            string = string.replaceFirst("^salt/[^/]*/", "");
            string = string.replaceFirst("^nextSalt/[^/]*/[^/]*/", "");
            return string;
        } catch (IllegalArgumentException e) {
            logger.debug("[{}] Failed to decode base64 string: {}", debugId, string);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            logger.warn("[{}] Command decryption failed: {}", debugId, e.getMessage());
        }
        return string;
    }

    @Override
    public void cancel() {
        super.cancel();
        tokenRefreshLock.lock();
        try {
            if (tokenRefreshTimer != null) {
                logger.debug("[{}] Cancelling token refresh.", debugId);
                tokenRefreshTimer.cancel(true);
            }
        } finally {
            tokenRefreshLock.unlock();
        }
    }

    private boolean initialize() {
        try {
            encryptionReady = false;
            tokenRefreshRetryCount = TOKEN_REFRESH_RETRY_COUNT;
            if (Cipher.getMaxAllowedKeyLength("AES") < 256) {
                return setError(LxErrorCode.INTERNAL_ERROR,
                        "Enable Java cryptography unlimited strength (see binding doc).");
            }
            // generate a random key for the session
            KeyGenerator aesKeyGen = KeyGenerator.getInstance("AES");
            aesKeyGen.init(256);
            aesKey = aesKeyGen.generateKey();
            // generate an initialization vector
            secureRandom = new SecureRandom();
            secureRandom.nextBytes(initVector);
            IvParameterSpec ivSpec = new IvParameterSpec(initVector);
            // initialize aes cipher for command encryption
            aesEncryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            aesEncryptCipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);
            // initialize aes cipher for response decryption
            aesDecryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            aesDecryptCipher.init(Cipher.DECRYPT_MODE, aesKey, ivSpec);
            // get token value from configuration storage
            token = thingHandler.getSetting(SETTINGS_TOKEN);
            logger.debug("[{}] Retrieved token value: {}", debugId, token);
        } catch (InvalidParameterException e) {
            return setError(LxErrorCode.INTERNAL_ERROR, "Invalid parameter: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            return setError(LxErrorCode.INTERNAL_ERROR, "AES not supported on platform.");
        } catch (InvalidKeyException | NoSuchPaddingException | InvalidAlgorithmParameterException e) {
            return setError(LxErrorCode.INTERNAL_ERROR, "AES cipher initialization failed.");
        }
        return true;
    }

    private Cipher getRsaCipher(String key) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            String keyString = key.replace("-----BEGIN CERTIFICATE-----", "").replace("-----END CERTIFICATE-----", "");
            byte[] keyData = Base64.getDecoder().decode(keyString);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyData);
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            logger.debug("[{}] Miniserver public key: {}", debugId, publicKey);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.PUBLIC_KEY, publicKey);
            logger.debug("[{}] Initialized RSA public key cipher", debugId);
            return cipher;
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeySpecException e) {
            setError(LxErrorCode.INTERNAL_ERROR, "Exception enabling RSA cipher: " + e.getMessage());
            return null;
        }
    }

    private byte[] generateSessionKey(Cipher rsaCipher) {
        String key = HexUtils.bytesToHex(aesKey.getEncoded()) + ":" + HexUtils.bytesToHex(initVector);
        try {
            byte[] sessionKey = rsaCipher.doFinal(key.getBytes());
            logger.debug("[{}] Generated session key: {}", debugId, HexUtils.bytesToHex(sessionKey));
            return sessionKey;
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            setError(LxErrorCode.INTERNAL_ERROR, "Exception encrypting session key: " + e.getMessage());
            return null;
        }
    }

    private String hashCredentials(LxResponseKeySalt keySalt, boolean sha256) {
        try {
            MessageDigest msgDigest = MessageDigest.getInstance(sha256 ? "SHA-256" : "SHA-1");
            String pwdHashStr = password + ":" + keySalt.salt;
            byte[] rawData = msgDigest.digest(pwdHashStr.getBytes(StandardCharsets.UTF_8));
            String pwdHash = HexUtils.bytesToHex(rawData).toUpperCase();
            logger.debug("[{}] PWDHASH: {}", debugId, pwdHash);
            return hashString(user + ":" + pwdHash, keySalt.key, sha256);
        } catch (NoSuchAlgorithmException e) {
            logger.debug("[{}] Error hashing token credentials: {}", debugId, e.getMessage());
            return null;
        }
    }

    private boolean acquireToken() {
        // Get Miniserver hash key and salt - this command should be encrypted
        LxResponse resp = socket.sendCmdWithResp(CMD_GET_KEY_AND_SALT + user, true, true);
        if (!checkResponse(resp)) {
            return setError(null, "Hash key/salt get failed.");
        }
        LxResponseKeySalt keySalt = resp.getValueAs(thingHandler.getGson(), LxResponseKeySalt.class);
        if (keySalt == null) {
            return setError(null, "Error parsing hash key/salt json: " + resp.getValueAsString());
        }
        if ("SHA256".equals(keySalt.hashAlg)) {
            sha256 = true;
        }
        logger.debug("[{}] Hash key: {}, salt: {}", debugId, keySalt.key, keySalt.salt);
        // Hash user name, password, key and salt
        String hash = hashCredentials(keySalt, sha256);
        if (hash == null) {
            return false;
        }
        // Request token
        String uuid = InstanceUUID.get();
        resp = socket.sendCmdWithResp(CMD_REQUEST_TOKEN + hash + "/" + user + "/" + TOKEN_PERMISSION + "/"
                + (uuid != null ? uuid : "098802e1-02b4-603c-ffffeee000d80cfd") + "/openHAB", true, true);
        if (!checkResponse(resp)) {
            return setError(null, "Request token failed.");
        }

        try {
            LxResponseToken tokenResponse = parseTokenResponse(resp);
            if (tokenResponse == null) {
                return false;
            }
            token = tokenResponse.token;
            if (token == null) {
                return setError(LxErrorCode.INTERNAL_ERROR, "Received null token.");
            }
        } catch (JsonParseException e) {
            return setError(LxErrorCode.INTERNAL_ERROR, "Error parsing token response: " + e.getMessage());
        }

        persistToken();
        logger.debug("[{}] Token acquired.", debugId);
        return true;
    }

    private boolean useToken() {
        String hash = hashToken();
        if (hash == null) {
            return false;
        }
        LxResponse resp = socket.sendCmdWithResp(CMD_AUTH_WITH_TOKEN + hash + "/" + user, true, true);
        if (!checkResponse(resp)) {
            if (reason == LxErrorCode.USER_UNAUTHORIZED) {
                token = null;
                persistToken();
                return setError(null, "Enter password to generate a new token.");
            }
            return setError(null, "Token-based authentication failed.");
        }
        parseTokenResponse(resp);
        return true;
    }

    private String hashToken() {
        LxResponse resp = socket.sendCmdWithResp(CMD_GET_KEY, true, true);
        if (!checkResponse(resp)) {
            setError(null, "Get key command failed.");
            return null;
        }
        try {
            String hashKey = resp.getValueAsString();
            // here is a difference to the API spec, which says the string to hash is "user:token", but this is "token"
            String hash = hashString(token, hashKey, sha256);
            if (hash == null) {
                setError(null, "Error hashing token.");
            }
            return hash;
        } catch (ClassCastException | IllegalStateException e) {
            setError(LxErrorCode.INTERNAL_ERROR, "Error parsing Miniserver key.");
            return null;
        }
    }

    private void persistToken() {
        Map<String, String> properties = new HashMap<>();
        properties.put(SETTINGS_TOKEN, token);
        if (token != null) {
            properties.put(SETTINGS_PASSWORD, null);
        }
        thingHandler.setSettings(properties);
    }

    private LxResponseToken parseTokenResponse(LxResponse response) {
        LxResponseToken tokenResponse = response.getValueAs(thingHandler.getGson(), LxResponseToken.class);
        if (tokenResponse == null) {
            setError(LxErrorCode.INTERNAL_ERROR, "Error parsing token response.");
            return null;
        }
        Boolean unsecurePass = tokenResponse.unsecurePass;
        if (unsecurePass != null && unsecurePass) {
            logger.warn("[{}] Unsecure user password on Miniserver.", debugId);
        }
        long secondsToExpiry;
        Integer validUntil = tokenResponse.validUntil;
        if (validUntil == null) {
            secondsToExpiry = TOKEN_REFRESH_DEFAULT_SECONDS;
        } else {
            // validUntil is the end of token life-span in seconds from 2009/01/01
            Calendar loxoneCalendar = Calendar.getInstance();
            loxoneCalendar.clear();
            loxoneCalendar.set(2009, Calendar.JANUARY, 1);
            loxoneCalendar.add(Calendar.SECOND, validUntil);
            Calendar ohCalendar = Calendar.getInstance();
            secondsToExpiry = (loxoneCalendar.getTimeInMillis() - ohCalendar.getTimeInMillis()) / 1000;
            if (logger.isDebugEnabled()) {
                try {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    logger.debug("[{}] Token will expire on: {}.", debugId, format.format(loxoneCalendar.getTime()));
                } catch (IllegalArgumentException e) {
                    logger.debug("[{}] Token will expire in {} days.", debugId,
                            TimeUnit.SECONDS.toDays(secondsToExpiry));
                }
            }
            if (secondsToExpiry <= 0) {
                logger.warn("[{}] Time to token expiry is negative or zero: {}", debugId, secondsToExpiry);
                secondsToExpiry = TOKEN_REFRESH_DEFAULT_SECONDS;
            } else {
                int correction = TOKEN_REFRESH_SECONDS_BEFORE_EXPIRY;
                while (secondsToExpiry - correction < 0) {
                    correction /= 2;
                }
                secondsToExpiry -= correction;
            }
        }
        scheduleTokenRefresh(secondsToExpiry);
        return tokenResponse;
    }

    private void refreshToken() {
        tokenRefreshLock.lock();
        try {
            tokenRefreshTimer = null;
            String hash = hashToken();
            if (hash != null) {
                LxResponse resp = socket.sendCmdWithResp(CMD_REFRESH_TOKEN + hash + "/" + user, true, true);
                if (checkResponse(resp)) {
                    logger.debug("[{}] Successful token refresh.", debugId);
                    parseTokenResponse(resp);
                    return;
                }
            }
            logger.debug("[{}] Token refresh failed, retrying (retry={}).", debugId, tokenRefreshRetryCount);
            if (tokenRefreshRetryCount-- > 0) {
                scheduleTokenRefresh(TOKEN_REFRESH_RETRY_DELAY_SECONDS);
            } else {
                logger.warn("[{}] All token refresh attempts failed.", debugId);
            }
        } finally {
            tokenRefreshLock.unlock();
        }
    }

    private void scheduleTokenRefresh(long delay) {
        logger.debug("[{}] Setting token refresh in {} days.", debugId, TimeUnit.SECONDS.toDays(delay));
        tokenRefreshLock.lock();
        try {
            tokenRefreshTimer = SCHEDULER.schedule(this::refreshToken, delay, TimeUnit.SECONDS);
        } finally {
            tokenRefreshLock.unlock();
        }
    }

    private String generateSalt() {
        byte[] bytes = new byte[SALT_BYTES];
        secureRandom.nextBytes(bytes);
        String salt = HexUtils.bytesToHex(bytes);
        salt = URLEncoder.encode(salt, StandardCharsets.UTF_8);
        saltTimeStamp = timeElapsedInSeconds();
        saltUseCount = 0;
        logger.debug("[{}] Generated salt: {}", debugId, salt);
        return salt;
    }

    private boolean newSaltNeeded() {
        return (++saltUseCount > SALT_MAX_USE_COUNT || timeElapsedInSeconds() - saltTimeStamp > SALT_MAX_AGE_SECONDS);
    }

    private long timeElapsedInSeconds() {
        return TimeUnit.NANOSECONDS.toSeconds(System.nanoTime());
    }
}
