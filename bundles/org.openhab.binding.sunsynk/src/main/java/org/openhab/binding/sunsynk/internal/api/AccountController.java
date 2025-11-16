/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.sunsynk.internal.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Objects;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.sunsynk.internal.api.dto.APIdata;
import org.openhab.binding.sunsynk.internal.api.dto.Client;
import org.openhab.binding.sunsynk.internal.api.dto.Details;
import org.openhab.binding.sunsynk.internal.api.dto.Inverter;
import org.openhab.binding.sunsynk.internal.api.dto.SunSynkLogin;
import org.openhab.binding.sunsynk.internal.api.dto.SunSynkPublicKey;
import org.openhab.binding.sunsynk.internal.api.dto.TokenRefresh;
import org.openhab.binding.sunsynk.internal.api.exception.SunSynkAuthenticateException;
import org.openhab.binding.sunsynk.internal.api.exception.SunSynkClientAuthenticateException;
import org.openhab.binding.sunsynk.internal.api.exception.SunSynkInverterDiscoveryException;
import org.openhab.binding.sunsynk.internal.api.exception.SunSynkTokenException;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link AccountController} is the internal class for a Sunsynk Connect
 * Account.
 * 
 * @author Lee Charlton - Initial contribution
 */

@NonNullByDefault
public class AccountController {
    private static final int TIMEOUT_IN_MS = 4000;
    private final Logger logger = LoggerFactory.getLogger(AccountController.class);
    private static final String BEARER_TYPE = "Bearer ";
    private static final long EXPIRYSECONDS = 100L; // 100 seconds before expiry
    private static final String SECRET_KEY = "POWER_VIEW"; // Is the SunSynk Connect App secret key
    private static final String SOURCE = "sunsynk"; // Is the SunSynk Connect App source identifier
    private Client sunAccount = new Client();
    private SunSynkPublicKey publicKey = new SunSynkPublicKey();

    public AccountController() {
    }

    /**
     * Authenticates the Account Thing as a Sunsynk Client,
     * then encrypts the user password to call authentication of the user credentials
     * 
     * @param username The username you use with Sunsynk Connect App
     * @param userPassword The password you use with Sunsynk Connect App
     * @throws SunSynkAuthenticateException
     * @throws SunSynkTokenException
     */
    public void clientAuthenticate(String username, String userPassword)
            throws SunSynkClientAuthenticateException, SunSynkAuthenticateException {
        long nonce = Instant.now().toEpochMilli();
        try {
            String authEndpoint = "nonce=" + String.valueOf(nonce) + "&source=" + SOURCE + "&sign=" + getSign(nonce);
            httpGetPublicKey(authEndpoint);
            String encryptedPassword = getEncryptPassword(userPassword, this.publicKey.getPublicKey());
            userAuthenticate(username, encryptedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new SunSynkClientAuthenticateException("Error attempting to authenticate client:" + e.getMessage());
        } catch (NoSuchPaddingException e) {
            throw new SunSynkClientAuthenticateException(
                    "Required encryption algorithm or padding not available: " + e.getMessage());
        } catch (InvalidKeySpecException e) {
            throw new SunSynkClientAuthenticateException("The provided public key is invalid: " + e.getMessage());
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new SunSynkClientAuthenticateException("Error processing data block size/padding: " + e.getMessage());
        } catch (SunSynkAuthenticateException e) {
            String message = Objects.requireNonNullElse(e.getMessage(), "An unknown authentication error occurred");
            throw new SunSynkAuthenticateException(message);
        } catch (SunSynkTokenException e) {
            String message = Objects.requireNonNullElse(e.getMessage(), "An unknown token error occurred");
            ;
            throw new SunSynkAuthenticateException(message);
        } catch (Exception e) { // Catch the generic one last
            throw new SunSynkClientAuthenticateException("An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * Authenticates a Sunsynk Connect API account using a username and password.
     * 
     * @param username The username you use with Sunsynk Connect App
     * @param encryptedPassword The password you use with Sunsynk Connect App, salted and encrypted with the public key
     * @throws SunSynkAuthenticateException
     * @throws SunSynkTokenException
     */
    public void userAuthenticate(String username, String saltedPassword)
            throws SunSynkAuthenticateException, SunSynkTokenException {
        String payload = makeLoginBody(username, saltedPassword);
        httpTokenPost(payload);
    }

    /**
     * Checks if a Sunsynk Connect account token is expired and gets a new one if required.
     * 
     * @param username
     * @throws SunSynkAuthenticateException
     * @throws SunSynkTokenException
     */
    public void refreshAccount(String username) throws SunSynkAuthenticateException, SunSynkTokenException {
        Long expiresIn = this.sunAccount.getExpiresIn();
        Long issuedAt = this.sunAccount.getIssuedAt();
        if ((issuedAt + expiresIn) - Instant.now().getEpochSecond() > 30) { // > 30 seconds
            logger.debug("Account configuration token not expired.");
            return;
        }
        logger.debug("Account configuration token expired : {}", this.sunAccount.getData().toString());
        String payload = makeRefreshBody(username, this.sunAccount.getRefreshTokenString());
        httpTokenPost(payload);
    }

    @SuppressWarnings("unused") // We need client to be nullable. Then we check for null. Without this compiler warns of
                                // unused block under null check
    private void httpGetPublicKey(String endpoint) throws SunSynkClientAuthenticateException, JsonSyntaxException {
        Gson gson = new Gson();
        String response = "";
        String httpsURL = makeLoginURL("anonymous/publicKey?" + endpoint);
        Properties headers = new Properties();
        headers.setProperty(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        headers.setProperty(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        try {
            response = HttpUtil.executeUrl(HttpMethod.GET.asString(), httpsURL, headers, null,
                    MediaType.APPLICATION_JSON, TIMEOUT_IN_MS);
            logger.trace("SunSynk private key response: {}", response);
            @Nullable
            SunSynkPublicKey key = gson.fromJson(response, SunSynkPublicKey.class);
            if (key == null) {
                throw new SunSynkClientAuthenticateException("Failed get private key");
            }
            this.publicKey = key;
        } catch (IOException | JsonSyntaxException e) {
            if (logger.isDebugEnabled()) {
                String message = Objects.requireNonNullElse(e.getMessage(), "unknown error message");
                Throwable cause = e.getCause();
                String causeMessage = cause != null ? Objects.requireNonNullElse(cause.getMessage(), "unknown cause")
                        : "unknown cause";
                logger.debug("Error authorising Account Thing: Msg = {}. Cause = {}.", message, causeMessage);
            }
            throw new SunSynkClientAuthenticateException("Account Thing authorisation failed");
        }
    }

    @SuppressWarnings("unused") // We need client to be nullable. Then we check for null. Without this compiler warns of
                                // unused block under null check
    private void httpTokenPost(String payload) throws SunSynkAuthenticateException, SunSynkTokenException {
        Gson gson = new Gson();
        String response = "";
        String httpsURL = makeLoginURL("oauth/token/new");
        Properties headers = new Properties();
        headers.setProperty(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        headers.setProperty(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        InputStream stream = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));
        try {
            response = HttpUtil.executeUrl(HttpMethod.POST.asString(), httpsURL, headers, stream,
                    MediaType.APPLICATION_JSON, TIMEOUT_IN_MS);

            @Nullable
            Client client = gson.fromJson(response, Client.class);
            if (client == null) {
                throw new SunSynkAuthenticateException(
                        "Sun Synk Account could not be authenticated: Try re-enabling account");
            }
            this.sunAccount = client;
        } catch (IOException | JsonSyntaxException e) {
            throw new SunSynkAuthenticateException("Sun Synk Account could not be authenticated", e);
        }
        if (this.sunAccount.getCode() == 102) {
            logger.debug("Sun Synk Account could not be authenticated: {}.", this.sunAccount.getMsg());
            throw new SunSynkAuthenticateException(
                    "Sun Synk Accountfailed to authenticate: Check your password or email.");
        }
        if (this.sunAccount.getStatus() == 404) {
            logger.debug("Sun Synk Account could not be authenticated: 404 {} {}.", this.sunAccount.getError(),
                    this.sunAccount.getPath());
            throw new SunSynkAuthenticateException("Sun Synk Accountfailed to authenticate: 404 Not Found.");
        }
        getToken();
    }

    /**
     * Performs RSA encryption on raw data using a provided public key string.
     * It handles Base64 decoding, key loading, encryption with PKCS1 padding,
     * and Base64 encoding of the final output.
     *
     * @param userPassword The raw, unpadded byte array of credentials (e.g., "password").
     * @param publicKeyString The Base64 encoded X.509 public key string (the MIIC... string).
     * @return The final Base64 encoded encrypted password string.
     * @throws Exception if any crypto operations fail.
     */
    private String getEncryptPassword(String userPassword, String publicKeyString) throws Exception {
        // Prepare the credentials as bytes
        byte[] rawCredentials = (userPassword).getBytes(StandardCharsets.UTF_8);
        // Load the Public Key using DER format
        // The public_key_string (String) is Base64 decoded into raw bytes (bytes)
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyString);
        // Load the key using the X.509/DER loader
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        // Encrypt the raw data using library's built-in padding
        // Use the specific algorithm/padding scheme: RSA/ECB/PKCS1Padding
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        // The library handles generating 512 bytes, padding, and random bytes automatically
        byte[] encryptedBytes = cipher.doFinal(rawCredentials);
        // Format the Output
        // The website likely sends the encrypted bytes as a Base64 encoded string
        String finalEncryptedPassword = Base64.getEncoder().encodeToString(encryptedBytes);
        logger.trace("The final encrypted password string is: {} ", finalEncryptedPassword);
        return finalEncryptedPassword;
    }

    /**
     * Discovers a list of all inverter tied to a Sunsynk Connect Account
     * 
     * @return List of connected inverters
     * @throws SunSynkInverterDiscoveryException
     */
    @SuppressWarnings("unused")
    public ArrayList<Inverter> getDetails() throws SunSynkInverterDiscoveryException {
        Details output = new Details();
        ArrayList<Inverter> inverters = new ArrayList<>();
        try {
            Gson gson = new Gson();
            Properties headers = new Properties();
            String response = "";

            String httpsURL = makeLoginURL(
                    "api/v1/inverters?page=1&limit=10&total=0&status=-1&sn=&plantId=&type=-2&softVer=&hmiVer=&agentCompanyId=-1&gsn=");
            headers.setProperty(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
            headers.setProperty(HttpHeaders.AUTHORIZATION, BEARER_TYPE + APIdata.staticAccessToken);
            response = HttpUtil.executeUrl(HttpMethod.GET.asString(), httpsURL, headers, null,
                    MediaType.APPLICATION_JSON, TIMEOUT_IN_MS);
            @Nullable
            Details maybeDetails = gson.fromJson(response, Details.class);
            if (maybeDetails == null) {
                throw new SunSynkInverterDiscoveryException("Failed to discover Inverters");
            }
            output = maybeDetails;
        } catch (IOException | JsonSyntaxException e) {
            if (logger.isDebugEnabled()) {
                String message = Objects.requireNonNullElse(e.getMessage(), "unkown error message");
                Throwable cause = e.getCause();
                String causeMessage = cause != null ? Objects.requireNonNullElse(cause.getMessage(), "unkown cause")
                        : "unkown cause";
                logger.debug("Error attempting to find inverters registered to account: Msg = {}. Cause = {}.", message,
                        causeMessage);
            }
            throw new SunSynkInverterDiscoveryException("Failed to discover Inverters", e);
        }
        inverters = output.getInverters(APIdata.staticAccessToken);
        return inverters;
    }

    private static String makeLoginURL(String path) {
        return "https://api.sunsynk.net" + "/" + path;
    }

    private static String makeLoginBody(String username, String password) {
        Gson gson = new Gson();
        SunSynkLogin login = new SunSynkLogin(username, password);
        return gson.toJson(login);
    }

    private static String makeRefreshBody(String username, String refreshToken) {
        Gson gson = new Gson();
        TokenRefresh refresh = new TokenRefresh(username, refreshToken);
        return gson.toJson(refresh);
    }

    private void getToken() throws SunSynkAuthenticateException {
        APIdata data = this.sunAccount.getData();
        APIdata.staticAccessToken = data.getAccessToken();
    }

    private String getSign(Long nonce) throws NoSuchAlgorithmException {
        String inputString = "nonce=" + String.valueOf(nonce) + "&source=" + SOURCE + SECRET_KEY;

        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        byte[] hashBytes = messageDigest.digest(inputString.getBytes(StandardCharsets.UTF_8));

        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        logger.trace("nonce : {} encrypted nonce : {}", nonce.toString(), sb.toString());
        return sb.toString();
    }

    @Override
    public String toString() {
        try {
            return this.sunAccount.getData().toString();
        } catch (SunSynkAuthenticateException e) {
            return "Tried to print client data, value is null.";
        }
    }
}
