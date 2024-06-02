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
package org.openhab.binding.windcentrale.internal.api;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.eclipse.jetty.http.HttpMethod.POST;
import static org.openhab.binding.windcentrale.internal.dto.CognitoGson.GSON;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.openhab.binding.windcentrale.internal.dto.AuthenticationResultResponse;
import org.openhab.binding.windcentrale.internal.dto.ChallengeResponse;
import org.openhab.binding.windcentrale.internal.dto.CognitoError;
import org.openhab.binding.windcentrale.internal.dto.InitiateAuthRequest;
import org.openhab.binding.windcentrale.internal.dto.RespondToAuthChallengeRequest;
import org.openhab.binding.windcentrale.internal.exception.InvalidAccessTokenException;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helps with authenticating users to Amazon Cognito to get a JWT access token which can be used for retrieving
 * information using the REST APIs.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Secure_Remote_Password_protocol">
 *      https://en.wikipedia.org/wiki/Secure_Remote_Password_protocol</a>
 * @see <a href="https://stackoverflow.com/questions/67528443/cognito-srp-using-aws-java-sdk-v2-x">
 *      https://stackoverflow.com/questions/67528443/cognito-srp-using-aws-java-sdk-v2-x</a>
 * @see <a href=
 *      "https://github.com/aws-samples/aws-cognito-java-desktop-app/blob/master/src/main/java/com/amazonaws/sample/cognitoui/AuthenticationHelper.java">
 *      https://github.com/aws-samples/aws-cognito-java-desktop-app/blob/master/src/main/java/com/amazonaws/sample/cognitoui/AuthenticationHelper.java</a>
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class AuthenticationHelper {

    private final Logger logger = LoggerFactory.getLogger(AuthenticationHelper.class);

    private static final String SRP_N_HEX = "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1" //
            + "29024E088A67CC74020BBEA63B139B22514A08798E3404DD" //
            + "EF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245" //
            + "E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7ED" //
            + "EE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3D" //
            + "C2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F" //
            + "83655D23DCA3AD961C62F356208552BB9ED529077096966D" //
            + "670C354E4ABC9804F1746C08CA18217C32905E462E36CE3B" //
            + "E39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9" //
            + "DE2BCBF6955817183995497CEA956AE515D2261898FA0510" //
            + "15728E5A8AAAC42DAD33170D04507A33A85521ABDF1CBA64" //
            + "ECFB850458DBEF0A8AEA71575D060C7DB3970F85A6E1E4C7" //
            + "ABF5AE8CDB0933D71E8C94E04A25619DCEE3D2261AD2EE6B" //
            + "F12FFA06D98A0864D87602733EC86A64521F2B18177B200C" //
            + "BBE117577A615D6C770988C0BAD946E208E24FA074E5AB31" //
            + "43DB5BFCE0FD108E4B82D120A93AD2CAFFFFFFFFFFFFFFFF";

    private static final BigInteger SRP_A;
    private static final BigInteger SRP_A2;
    private static final BigInteger SRP_G = BigInteger.valueOf(2);
    private static final BigInteger SRP_K;
    private static final BigInteger SRP_N = new BigInteger(SRP_N_HEX, 16);

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("EEE MMM d HH:mm:ss z yyyy", Locale.US).withZone(ZoneId.of("UTC"));
    private static final int DERIVED_KEY_SIZE = 16;
    private static final int EPHEMERAL_KEY_LENGTH = 1024;
    private static final String DERIVED_KEY_INFO = "Caldera Derived Key";
    private static final Duration REQUEST_TIMEOUT = Duration.ofMinutes(1);

    private static final String COGNITO_URL_FORMAT = "https://cognito-idp.%s.amazonaws.com/";
    private static final String INITIATE_AUTH_TARGET = "AWSCognitoIdentityProviderService.InitiateAuth";
    private static final String RESPOND_TO_AUTH_TARGET = "AWSCognitoIdentityProviderService.RespondToAuthChallenge";

    /**
     * Internal class for doing the HKDF calculations.
     */
    private static final class Hkdf {
        private static final int MAX_KEY_SIZE = 255;
        private final String algorithm;
        private @Nullable SecretKey prk;

        /**
         * @param algorithm The type of HMAC algorithm to be used
         */
        private Hkdf(String algorithm) {
            if (!algorithm.startsWith("Hmac")) {
                throw new IllegalArgumentException(
                        "Invalid algorithm " + algorithm + ". HKDF may only be used with HMAC algorithms.");
            }
            this.algorithm = algorithm;
        }

        /**
         * @param ikm the input key material
         * @param salt random bytes for salt
         */
        private void init(byte[] ikm, byte[] salt) {
            try {
                Mac mac = Mac.getInstance(algorithm);
                byte[] realSalt = salt.length == 0 ? new byte[mac.getMacLength()] : salt.clone();
                mac.init(new SecretKeySpec(realSalt, algorithm));
                SecretKeySpec key = new SecretKeySpec(mac.doFinal(ikm), algorithm);
                unsafeInitWithoutKeyExtraction(key);
            } catch (InvalidKeyException | NoSuchAlgorithmException e) {
                throw new IllegalStateException("Failed to initialize HKDF", e);
            }
        }

        /**
         * @param rawKey current secret key
         */
        private void unsafeInitWithoutKeyExtraction(SecretKey rawKey) {
            if (!rawKey.getAlgorithm().equals(algorithm)) {
                throw new IllegalArgumentException(
                        "Algorithm for the provided key must match the algorithm for this HKDF. Expected " + algorithm
                                + " but found " + rawKey.getAlgorithm());
            } else {
                prk = rawKey;
            }
        }

        private byte[] deriveKey(String info, int length) {
            if (prk == null) {
                throw new IllegalStateException("HKDF has not been initialized");
            }

            if (length < 0) {
                throw new IllegalArgumentException("Length must be a non-negative value");
            }

            Mac mac = createMac();
            if (length > MAX_KEY_SIZE * mac.getMacLength()) {
                throw new IllegalArgumentException(
                        "Requested keys may not be longer than 255 times the underlying HMAC length");
            }

            byte[] result = new byte[length];
            byte[] bytes = info.getBytes(UTF_8);
            byte[] t = {};
            int loc = 0;

            for (byte i = 1; loc < length; ++i) {
                mac.update(t);
                mac.update(bytes);
                mac.update(i);
                t = mac.doFinal();

                for (int x = 0; x < t.length && loc < length; ++loc) {
                    result[loc] = t[x];
                    ++x;
                }
            }

            return result;
        }

        /**
         * @return the generated message authentication code
         */
        private Mac createMac() {
            try {
                Mac mac = Mac.getInstance(algorithm);
                mac.init(prk);
                return mac;
            } catch (InvalidKeyException | NoSuchAlgorithmException e) {
                throw new IllegalStateException("Could not create MAC implementing algorithm: " + algorithm, e);
            }
        }
    }

    static {
        // Initialize the SRP variables
        try {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(SRP_N.toByteArray());

            byte[] digest = md.digest(SRP_G.toByteArray());
            SRP_K = new BigInteger(1, digest);

            BigInteger srpA;
            BigInteger srpA2;
            do {
                srpA2 = new BigInteger(EPHEMERAL_KEY_LENGTH, sr).mod(SRP_N);
                srpA = SRP_G.modPow(srpA2, SRP_N);
            } while (srpA.mod(SRP_N).equals(BigInteger.ZERO));

            SRP_A = srpA;
            SRP_A2 = srpA2;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SRP variables cannot be initialized due to missing algorithm", e);
        }
    }

    private final HttpClient httpClient;
    private final String userPoolId;
    private final String clientId;
    private final String region;

    public AuthenticationHelper(HttpClientFactory httpClientFactory, String userPoolId, String clientId,
            String region) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.userPoolId = userPoolId;
        this.clientId = clientId;
        this.region = region;
    }

    /**
     * Method to orchestrate the SRP Authentication.
     *
     * @param username username for the SRP request
     * @param password password for the SRP request
     * @return JWT token if the request is successful
     * @throws InvalidAccessTokenException when SRP authentication fails
     */
    public AuthenticationResultResponse performSrpAuthentication(String username, String password)
            throws InvalidAccessTokenException {
        InitiateAuthRequest initiateAuthRequest = InitiateAuthRequest.userSrpAuth(clientId, username,
                SRP_A.toString(16));
        try {
            ChallengeResponse challengeResponse = postInitiateAuthSrp(initiateAuthRequest);
            if ("PASSWORD_VERIFIER".equals(challengeResponse.challengeName)) {
                RespondToAuthChallengeRequest challengeRequest = createRespondToAuthChallengeRequest(challengeResponse,
                        password);
                return postRespondToAuthChallenge(challengeRequest);
            } else {
                throw new InvalidAccessTokenException(
                        "Unsupported authentication challenge: " + challengeResponse.challengeName);
            }
        } catch (IllegalStateException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new InvalidAccessTokenException("SRP Authentication failed", e);
        }
    }

    public AuthenticationResultResponse performTokenRefresh(String refreshToken) throws InvalidAccessTokenException {
        InitiateAuthRequest initiateAuthRequest = InitiateAuthRequest.refreshTokenAuth(clientId, refreshToken);
        try {
            return postInitiateAuthRefresh(initiateAuthRequest);
        } catch (IllegalStateException e) {
            throw new InvalidAccessTokenException("Token refresh failed", e);
        }
    }

    /**
     * Creates a response request to the SRP authentication challenge from the user pool.
     *
     * @param challengeResponse authentication challenge returned from the Cognito user pool
     * @param password password to be used to respond to the authentication challenge
     * @return request created for the previous authentication challenge
     */
    private RespondToAuthChallengeRequest createRespondToAuthChallengeRequest(ChallengeResponse challengeResponse,
            String password) throws InvalidKeyException, NoSuchAlgorithmException {
        String salt = challengeResponse.getSalt();
        String secretBlock = challengeResponse.getSecretBlock();
        String userIdForSrp = challengeResponse.getUserIdForSrp();
        String usernameInternal = challengeResponse.getUsername();

        if (secretBlock.isEmpty() || userIdForSrp.isEmpty() || usernameInternal.isEmpty()) {
            throw new IllegalArgumentException("Required authentication response challenge parameters are null");
        }

        BigInteger srpB = new BigInteger(challengeResponse.getSrpB(), 16);
        if (srpB.mod(SRP_N).equals(BigInteger.ZERO)) {
            throw new IllegalStateException("SRP error, B cannot be zero");
        }

        String timestamp = DATE_TIME_FORMATTER.format(Instant.now());

        byte[] key = getPasswordAuthenticationKey(userIdForSrp, password, srpB, new BigInteger(salt, 16));

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        mac.update(userPoolId.split("_", 2)[1].getBytes(UTF_8));
        mac.update(userIdForSrp.getBytes(UTF_8));
        mac.update(Base64.getDecoder().decode(secretBlock));
        byte[] hmac = mac.doFinal(timestamp.getBytes(UTF_8));

        String signature = new String(Base64.getEncoder().encode(hmac), UTF_8);

        return new RespondToAuthChallengeRequest(clientId, usernameInternal, secretBlock, signature, timestamp);
    }

    private byte[] getPasswordAuthenticationKey(String userId, String userPassword, BigInteger srpB, BigInteger salt) {
        try {
            // Authenticate the password
            // srpU = H(SRP_A, srpB)
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(SRP_A.toByteArray());

            BigInteger srpU = new BigInteger(1, md.digest(srpB.toByteArray()));
            if (srpU.equals(BigInteger.ZERO)) {
                throw new IllegalStateException("Hash of A and B cannot be zero");
            }

            // srpX = H(salt | H(poolName | userId | ":" | password))
            md.reset();
            md.update(userPoolId.split("_", 2)[1].getBytes(UTF_8));
            md.update(userId.getBytes(UTF_8));
            md.update(":".getBytes(UTF_8));

            byte[] userIdHash = md.digest(userPassword.getBytes(UTF_8));

            md.reset();
            md.update(salt.toByteArray());

            BigInteger srpX = new BigInteger(1, md.digest(userIdHash));
            BigInteger srpS = (srpB.subtract(SRP_K.multiply(SRP_G.modPow(srpX, SRP_N)))
                    .modPow(SRP_A2.add(srpU.multiply(srpX)), SRP_N)).mod(SRP_N);

            Hkdf hkdf = new Hkdf("HmacSHA256");
            hkdf.init(srpS.toByteArray(), srpU.toByteArray());
            return hkdf.deriveKey(DERIVED_KEY_INFO, DERIVED_KEY_SIZE);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private ChallengeResponse postInitiateAuthSrp(InitiateAuthRequest request) throws InvalidAccessTokenException {
        String responseContent = postJson(INITIATE_AUTH_TARGET, GSON.toJson(request));
        return Objects.requireNonNull(GSON.fromJson(responseContent, ChallengeResponse.class));
    }

    private AuthenticationResultResponse postInitiateAuthRefresh(InitiateAuthRequest request)
            throws InvalidAccessTokenException {
        String responseContent = postJson(INITIATE_AUTH_TARGET, GSON.toJson(request));
        return Objects.requireNonNull(GSON.fromJson(responseContent, AuthenticationResultResponse.class));
    }

    private AuthenticationResultResponse postRespondToAuthChallenge(RespondToAuthChallengeRequest request)
            throws InvalidAccessTokenException {
        String responseContent = postJson(RESPOND_TO_AUTH_TARGET, GSON.toJson(request));
        return Objects.requireNonNull(GSON.fromJson(responseContent, AuthenticationResultResponse.class));
    }

    private String postJson(String target, String requestContent) throws InvalidAccessTokenException {
        try {
            String url = String.format(COGNITO_URL_FORMAT, region);
            logger.debug("Posting JSON to: {}", url);
            ContentResponse contentResponse = httpClient.newRequest(url) //
                    .method(POST) //
                    .header("x-amz-target", target) //
                    .content(new StringContentProvider(requestContent), "application/x-amz-json-1.1") //
                    .timeout(REQUEST_TIMEOUT.toNanos(), TimeUnit.NANOSECONDS).send();

            String response = contentResponse.getContentAsString();
            if (contentResponse.getStatus() >= 400) {
                logger.debug("Cognito API error: {}", response);

                CognitoError error = GSON.fromJson(response, CognitoError.class);
                String message;
                if (error != null && !error.message.isBlank()) {
                    message = String.format("Cognito API error: %s (%s)", error.message, error.type);
                } else {
                    message = String.format("Cognito API error: %s (HTTP %s)", contentResponse.getReason(),
                            contentResponse.getStatus());
                }
                throw new InvalidAccessTokenException(message);
            } else {
                logger.trace("Response: {}", response);
            }
            return response;
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new InvalidAccessTokenException("Cognito API request failed: " + e.getMessage(), e);
        }
    }
}
