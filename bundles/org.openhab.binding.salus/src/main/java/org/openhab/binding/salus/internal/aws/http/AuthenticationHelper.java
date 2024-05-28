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
package org.openhab.binding.salus.internal.aws.http;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.US;
import static java.util.Objects.requireNonNull;
import static java.util.SimpleTimeZone.UTC_TIME;
import static software.amazon.awssdk.services.cognitoidentityprovider.model.ChallengeNameType.PASSWORD_VERIFIER;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.SimpleTimeZone;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.salus.internal.rest.exceptions.SalusApiException;

import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentity.CognitoIdentityClient;
import software.amazon.awssdk.services.cognitoidentity.model.GetCredentialsForIdentityRequest;
import software.amazon.awssdk.services.cognitoidentity.model.GetCredentialsForIdentityResponse;
import software.amazon.awssdk.services.cognitoidentity.model.GetIdRequest;
import software.amazon.awssdk.services.cognitoidentity.model.GetIdResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.RespondToAuthChallengeRequest;

/**
 * Private class for SRP client side math.
 * <p>
 * Base on
 * https://github.com/aws-samples/aws-cognito-java-desktop-app/blob/master/src/main/java/com/amazonaws/sample/cognitoui/AuthenticationHelper.java
 * Plus bring up to SDKv2 https://stackoverflow.com/a/67729189/1819402
 * <p>
 * Implementation of SRP algorithm http://srp.stanford.edu/design.html
 * 
 * <pre>
 *   N    A large safe prime (N = 2q+1, where q is prime) All arithmetic is done modulo N.
 *   g    A generator modulo N
 *   k    Multiplier parameter (k = H(N, g) in SRP-6a, k = 3 for legacy SRP-6)
 *   s    User's salt
 *   I    Username
 *   p    Cleartext Password
 *   H()  One-way hash function
 *   ^    (Modular) Exponentiation
 *   u    Random scrambling parameter
 *   a,b  Secret ephemeral values
 *   A,B  Public ephemeral values
 *   x    Private key (derived from p and s)
 *   v    Password verifier
 * </pre>
 * 
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
class AuthenticationHelper {
    private static final String HEX_N = "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1"
            + "29024E088A67CC74020BBEA63B139B22514A08798E3404DD"//
            + "EF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245"//
            + "E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7ED"//
            + "EE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3D"//
            + "C2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F"//
            + "83655D23DCA3AD961C62F356208552BB9ED529077096966D"//
            + "670C354E4ABC9804F1746C08CA18217C32905E462E36CE3B"//
            + "E39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9"//
            + "DE2BCBF6955817183995497CEA956AE515D2261898FA0510"//
            + "15728E5A8AAAC42DAD33170D04507A33A85521ABDF1CBA64"//
            + "ECFB850458DBEF0A8AEA71575D060C7DB3970F85A6E1E4C7"//
            + "ABF5AE8CDB0933D71E8C94E04A25619DCEE3D2261AD2EE6B"//
            + "F12FFA06D98A0864D87602733EC86A64521F2B18177B200C"//
            + "BBE117577A615D6C770988C0BAD946E208E24FA074E5AB31"//
            + "43DB5BFCE0FD108E4B82D120A93AD2CAFFFFFFFFFFFFFFFF";
    private static final BigInteger N = new BigInteger(HEX_N, 16);
    private static final BigInteger g = BigInteger.valueOf(2);
    private static final BigInteger k;
    private static final int EPHEMERAL_KEY_LENGTH = 1024;
    private static final int DERIVED_KEY_SIZE = 16;
    private static final String DERIVED_KEY_INFO = "Caldera Derived Key";
    private static final ThreadLocal<MessageDigest> THREAD_MESSAGE_DIGEST = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException("Exception in authentication", e);
        }
    });
    private static final SecureRandom SECURE_RANDOM;
    private static final String ALGORITHM = "HmacSHA256";

    static {
        try {
            SECURE_RANDOM = SecureRandom.getInstance("SHA1PRNG");

            MessageDigest messageDigest = THREAD_MESSAGE_DIGEST.get();
            messageDigest.reset();
            messageDigest.update(N.toByteArray());
            byte[] digest = messageDigest.digest(g.toByteArray());
            k = new BigInteger(1, digest);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException(e.getMessage(), e);
        }
    }

    private BigInteger a;
    private BigInteger A;
    private final String userPoolID;
    private final String clientId;
    private final String region;

    AuthenticationHelper(String userPoolID, String clientid, String region) {
        do {
            a = new BigInteger(EPHEMERAL_KEY_LENGTH, SECURE_RANDOM).mod(N);
            A = g.modPow(a, N);
        } while (A.mod(N).equals(BigInteger.ZERO));

        this.userPoolID = userPoolID;
        this.clientId = clientid;
        this.region = region;
    }

    private byte[] getPasswordAuthenticationKey(String userId, byte[] userPassword, BigInteger B, BigInteger salt)
            throws ShortBufferException, NoSuchAlgorithmException, InvalidKeyException, SecurityException {
        // Authenticate the password
        // u = H(A, B)
        var messageDigest = THREAD_MESSAGE_DIGEST.get();
        messageDigest.reset();
        messageDigest.update(A.toByteArray());
        var u = new BigInteger(1, messageDigest.digest(B.toByteArray()));
        if (u.equals(BigInteger.ZERO)) {
            throw new SecurityException("Hash of A and B cannot be zero");
        }

        // x = H(salt | H(poolName | userId | ":" | password))
        messageDigest.reset();
        messageDigest.update(this.userPoolID.getBytes(UTF_8));
        messageDigest.update(userId.getBytes(UTF_8));
        messageDigest.update(":".getBytes(UTF_8));
        var userIdHash = messageDigest.digest(userPassword);

        messageDigest.reset();
        messageDigest.update(salt.toByteArray());
        BigInteger x = new BigInteger(1, messageDigest.digest(userIdHash));
        BigInteger S = (B.subtract(k.multiply(g.modPow(x, N))).modPow(a.add(u.multiply(x)), N)).mod(N);

        var hkdf = new Hkdf(ALGORITHM);
        hkdf.init(S.toByteArray(), u.toByteArray());
        return hkdf.deriveKey(DERIVED_KEY_INFO, DERIVED_KEY_SIZE);
    }

    /**
     * Method to orchestrate the SRP Authentication
     *
     * @param username Username for the SRP request
     * @param password Password for the SRP request
     * @return the JWT token if the request is successful else null.
     */
    public AuthenticationResultType performSRPAuthentication(String username, byte[] password)
            throws SalusApiException {
        var authReq = initiateUserSrpAuthRequest(username);
        var creds = AnonymousCredentialsProvider.create();
        try (var cognitoClient = CognitoIdentityProviderClient.builder()//
                .region(Region.of(this.region))//
                .credentialsProvider(creds)//
                .build()) {
            var authRes = cognitoClient.initiateAuth(authReq);
            if (!authRes.challengeName().equals(PASSWORD_VERIFIER)) {
                throw new SalusApiException("Unexpected challenge name: " + authRes.challengeName());
            }

            var challengeRequest = userSrpAuthRequest(authRes, password);
            var result = cognitoClient.respondToAuthChallenge(challengeRequest);

            return result.authenticationResult();
        } catch (ShortBufferException | NoSuchAlgorithmException | InvalidKeyException | SecurityException e) {
            throw new SalusApiException("Cannot perform SRP authentication!", e);
        }
    }

    /**
     * Initialize the authentication request for the first time.
     *
     * @param username The user for which the authentication request is created.
     * @return the Authentication request.
     */
    private InitiateAuthRequest initiateUserSrpAuthRequest(String username) {
        var authParams = Map.of("USERNAME", username, "SRP_A", this.A.toString(16));

        return InitiateAuthRequest.builder() //
                .authFlow(AuthFlowType.USER_SRP_AUTH) //
                .clientId(this.clientId) //
                .authParameters(authParams) //
                .build();
    }

    /**
     * Method is used to respond to the Auth challange from the user pool
     *
     * @param challenge The authenticaion challange returned from the cognito user pool
     * @param password The password to be used to respond to the authentication challenge.
     * @return the Request created for the previous authentication challenge.
     */
    private RespondToAuthChallengeRequest userSrpAuthRequest(InitiateAuthResponse challenge, byte[] password)
            throws ShortBufferException, NoSuchAlgorithmException, InvalidKeyException, SecurityException {
        var userIdForSRP = requireNonNull(challenge.challengeParameters().get("USER_ID_FOR_SRP"));
        var usernameInternal = requireNonNull(challenge.challengeParameters().get("USERNAME"));

        var srpB = new BigInteger(challenge.challengeParameters().get("SRP_B"), 16);
        if (srpB.mod(N).equals(BigInteger.ZERO)) {
            throw new SecurityException("SRP error, B cannot be zero");
        }

        var salt = new BigInteger(challenge.challengeParameters().get("SALT"), 16);
        var key = getPasswordAuthenticationKey(userIdForSRP, password, srpB, salt);

        var timestamp = new Date();
        var mac = Mac.getInstance(ALGORITHM);
        var keySpec = new SecretKeySpec(key, ALGORITHM);
        mac.init(keySpec);
        mac.update(this.userPoolID.getBytes(UTF_8));
        mac.update(userIdForSRP.getBytes(UTF_8));
        var challengeSecretBlock = requireNonNull(challenge.challengeParameters().get("SECRET_BLOCK"));
        byte[] secretBlock = Base64.getDecoder().decode(challengeSecretBlock);
        mac.update(secretBlock);
        var simpleDateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy", US);
        simpleDateFormat.setTimeZone(new SimpleTimeZone(UTC_TIME, "UTC"));
        var dateString = simpleDateFormat.format(timestamp);
        var dateBytes = dateString.getBytes(UTF_8);
        var hmac = mac.doFinal(dateBytes);

        var formatTimestamp = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy", US);
        formatTimestamp.setTimeZone(new SimpleTimeZone(UTC_TIME, "UTC"));

        var srpAuthResponses = Map.of("PASSWORD_CLAIM_SECRET_BLOCK", challengeSecretBlock, "PASSWORD_CLAIM_SIGNATURE",
                new String(Base64.getEncoder().encode(hmac), UTF_8), "TIMESTAMP", formatTimestamp.format(timestamp),
                "USERNAME", usernameInternal);

        return RespondToAuthChallengeRequest.builder()//
                .challengeName(challenge.challengeName())//
                .clientId(clientId)//
                .session(challenge.session())//
                .challengeResponses(srpAuthResponses)//
                .build();
    }

    public GetIdResponse getId(AuthenticationResultType accessToken) {
        try (var client = CognitoIdentityClient.builder().region(Region.of(region)).build()) {
            GetIdRequest getIdRequest = GetIdRequest.builder()
                    .logins(Map.of("cognito-idp.eu-central-1.amazonaws.com/eu-central-1_XGRz3CgoY",
                            accessToken.idToken()))
                    .identityPoolId("eu-central-1:60912c00-287d-413b-a2c9-ece3ccef9230").build();
            return client.getId(getIdRequest);
        }
    }

    public GetCredentialsForIdentityResponse getCredentialsForIdentity(AuthenticationResultType accessToken,
            String identityId) {
        try (var client = CognitoIdentityClient.builder().region(Region.of(region)).build()) {
            return client.getCredentialsForIdentity(GetCredentialsForIdentityRequest
                    .builder().identityId(identityId).logins(Map
                            .of("cognito-idp.eu-central-1.amazonaws.com/eu-central-1_XGRz3CgoY", accessToken.idToken()))
                    .build());
        }
    }

    /**
     * Internal class for doing the Hkdf calculations.
     */
    @SuppressWarnings("SameParameterValue")
    static final class Hkdf {
        private static final int MAX_KEY_SIZE = 255;
        private static final byte[] EMPTY_ARRAY = new byte[0];
        private final String algorithm;
        @Nullable
        private SecretKey prk = null;

        /**
         * @param algorithm REQUIRED: The type of HMAC algorithm to be used.
         */
        private Hkdf(String algorithm) {
            if (!algorithm.startsWith("Hmac")) {
                throw new IllegalArgumentException(
                        "Invalid algorithm " + algorithm + ". Hkdf may only be used with Hmac algorithms.");
            }
            this.algorithm = algorithm;
        }

        /**
         * @param ikm REQUIRED: The input key material.
         * @param salt REQUIRED: Random bytes for salt.
         */
        private void init(byte[] ikm, byte[] salt) throws InvalidKeyException, NoSuchAlgorithmException {
            var realSalt = salt.clone();
            var rawKeyMaterial = EMPTY_ARRAY;

            try {
                var e = Mac.getInstance(this.algorithm);
                if (realSalt.length == 0) {
                    realSalt = new byte[e.getMacLength()];
                    Arrays.fill(realSalt, (byte) 0);
                }

                e.init(new SecretKeySpec(realSalt, this.algorithm));
                rawKeyMaterial = e.doFinal(ikm);
                var key = new SecretKeySpec(rawKeyMaterial, this.algorithm);
                Arrays.fill(rawKeyMaterial, (byte) 0);
                this.unsafeInitWithoutKeyExtraction(key);
            } finally {
                Arrays.fill(rawKeyMaterial, (byte) 0);
            }
        }

        /**
         * @param rawKey REQUIRED: Current secret key.
         */
        private void unsafeInitWithoutKeyExtraction(SecretKey rawKey) throws InvalidKeyException {
            if (!rawKey.getAlgorithm().equals(this.algorithm)) {
                throw new InvalidKeyException(
                        "Algorithm for the provided key must match the algorithm for this Hkdf. Expected "
                                + this.algorithm + " but found " + rawKey.getAlgorithm());
            }
            this.prk = rawKey;
        }

        /**
         * @param info REQUIRED
         * @param length REQUIRED
         * @return converted bytes.
         */
        private byte[] deriveKey(String info, int length)
                throws ShortBufferException, NoSuchAlgorithmException, InvalidKeyException {
            return this.deriveKey(info.getBytes(UTF_8), length);
        }

        /**
         * @param info REQUIRED
         * @param length REQUIRED
         * @return converted bytes.
         */
        private byte[] deriveKey(byte[] info, int length)
                throws ShortBufferException, NoSuchAlgorithmException, InvalidKeyException {
            var result = new byte[length];
            this.deriveKey(info, length, result, 0);
            return result;
        }

        /**
         * @param info REQUIRED
         * @param length REQUIRED
         * @param output REQUIRED
         * @param offset REQUIRED
         */
        private void deriveKey(byte[] info, int length, byte[] output, int offset)
                throws ShortBufferException, NoSuchAlgorithmException, InvalidKeyException {
            this.assertInitialized();
            if (length < 0) {
                throw new IllegalArgumentException("Length must be a non-negative value.");
            }

            if (output.length < offset + length) {
                throw new ShortBufferException();
            }

            var mac = this.createMac();
            if (length > MAX_KEY_SIZE * mac.getMacLength()) {
                throw new IllegalArgumentException(
                        "Requested keys may not be longer than 255 times the underlying HMAC length.");
            }

            byte[] t = EMPTY_ARRAY;
            try {
                int loc = 0;

                for (byte i = 1; loc < length; ++i) {
                    mac.update(t);
                    mac.update(info);
                    mac.update(i);
                    t = mac.doFinal();

                    for (int x = 0; x < t.length && loc < length; ++loc) {
                        output[loc] = t[x];
                        ++x;
                    }
                }
            } finally {
                Arrays.fill(t, (byte) 0);
            }
        }

        /**
         * @return the generates message authentication code.
         */
        private Mac createMac() throws NoSuchAlgorithmException, InvalidKeyException {
            var ex = Mac.getInstance(this.algorithm);
            ex.init(this.prk);
            return ex;
        }

        /**
         * Checks for a valid pseudo-random key.
         */
        private void assertInitialized() {
            if (this.prk == null) {
                throw new IllegalStateException("Hkdf has not been initialized");
            }
        }
    }
}
