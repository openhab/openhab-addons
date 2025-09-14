package org.openhab.binding.homekit.internal.services;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.X25519PublicKeyParameters;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homekit.internal.crypto.CryptoUtils;
import org.openhab.binding.homekit.internal.crypto.Tlv8Codec;
import org.openhab.binding.homekit.internal.enums.PairingMethod;
import org.openhab.binding.homekit.internal.enums.PairingState;
import org.openhab.binding.homekit.internal.enums.TlvType;
import org.openhab.binding.homekit.internal.session.SessionKeys;
import org.openhab.binding.homekit.internal.transport.HttpTransport;

/**
 * Handles the 3-step pair-verify process with a HomeKit accessory.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class PairingVerifyService {

    private static final String PAIR_VERIFY_ENCRYPT_INFO = "Pair-Verify-Encrypt-Info";
    private static final String PAIR_VERIFY_ENCRYPT_SALT = "Pair-Verify-Encrypt-Salt";
    private static final String PV_MSG02 = "PV-Msg02";
    private static final String PV_MSG03 = "PV-Msg03";
    private static final String CONTENT_TYPE_TLV = "application/pairing+tlv8";
    private static final String ENDPOINT_PAIR_VERIFY = "/pair-verify";
    private static final String CONTROL_WRITE_ENCRYPTION_KEY = "Control-Write-Encryption-Key";
    private static final String CONTROL_READ_ENCRYPTION_KEY = "Control-Read-Encryption-Key";
    private static final String CONTROL_SALT = "Control-Salt";

    private final HttpTransport http;
    private final String baseUrl;
    private final String accessoryIdentifier;
    private final Ed25519PrivateKeyParameters controllerPrivateKey;
    private final AsymmetricCipherKeyPair controllerEphemeralKeyPair;

    public PairingVerifyService(HttpTransport http, String baseUrl, String accessoryIdentifier,
            Ed25519PrivateKeyParameters controllerPrivateKey) {
        this.http = http;
        this.baseUrl = baseUrl;
        this.accessoryIdentifier = accessoryIdentifier;
        this.controllerPrivateKey = controllerPrivateKey;
        this.controllerEphemeralKeyPair = CryptoUtils.generateCurve25519KeyPair();
    }

    public SessionKeys verify() throws Exception {
        // M1 — Send controller ephemeral public key
        byte[] controllerPublicKey = ((X25519PublicKeyParameters) controllerEphemeralKeyPair.getPublic()).getEncoded();

        Map<Integer, byte[]> tlv1 = Map.of( //
                TlvType.STATE.key, new byte[] { PairingState.M1.value }, //
                // TLVType.METHOD.key, new byte[] { PairingMethod.VERIFY.value }, // not required in Apple spec
                TlvType.PUBLIC_KEY.key, controllerPublicKey);
        Validator.validate(PairingMethod.VERIFY, tlv1);

        byte[] resp1 = http.post(baseUrl, ENDPOINT_PAIR_VERIFY, CONTENT_TYPE_TLV, Tlv8Codec.encode(tlv1));

        // M2 — Receive accessory ephemeral public key and encrypted TLV
        Map<Integer, byte[]> tlv2 = Tlv8Codec.decode(resp1);
        Validator.validate(PairingMethod.VERIFY, tlv2);

        byte[] accessoryPublicKeyBytes = tlv2.getOrDefault(TlvType.PUBLIC_KEY.key, new byte[0]);
        byte[] encrypted = tlv2.getOrDefault(TlvType.ENCRYPTED_DATA.key, new byte[0]);

        X25519PublicKeyParameters accessoryEphemeralKey = new X25519PublicKeyParameters(accessoryPublicKeyBytes, 0);
        byte[] sharedSecret = CryptoUtils.computeSharedSecret(controllerEphemeralKeyPair.getPrivate(),
                accessoryEphemeralKey);

        byte[] sessionKey = CryptoUtils.hkdf(sharedSecret, PAIR_VERIFY_ENCRYPT_SALT, PAIR_VERIFY_ENCRYPT_INFO);
        byte[] decrypted = CryptoUtils.decrypt(sessionKey, PV_MSG02, encrypted);
        Map<Integer, byte[]> innerTLV = Tlv8Codec.decode(decrypted);
        CryptoUtils.validateAccessory(innerTLV); // validates identifier + signature

        // M3 — Send encrypted controller identifier and signature
        byte[] verifyPayload = concat(controllerPublicKey, accessoryPublicKeyBytes);
        byte[] signature = CryptoUtils.signVerifyMessage(controllerPrivateKey, verifyPayload);

        byte[] controllerInfo = Tlv8Codec.encode(Map.of( //
                TlvType.IDENTIFIER.key, accessoryIdentifier.getBytes(StandardCharsets.UTF_8), //
                TlvType.SIGNATURE.key, signature));
        byte[] encryptedM3 = CryptoUtils.encrypt(sessionKey, PV_MSG03, controllerInfo);

        Map<Integer, byte[]> tlv3 = Map.of( //
                TlvType.STATE.key, new byte[] { PairingState.M3.value }, //
                TlvType.ENCRYPTED_DATA.key, encryptedM3);
        Validator.validate(PairingMethod.VERIFY, tlv3);

        byte[] resp3 = http.post(baseUrl, ENDPOINT_PAIR_VERIFY, CONTENT_TYPE_TLV, Tlv8Codec.encode(tlv3));

        // M4 — Final confirmation
        Map<Integer, byte[]> tlv4 = Tlv8Codec.decode(resp3);
        Validator.validate(PairingMethod.VERIFY, tlv4);

        // Derive directional session keys
        byte[] readKey = CryptoUtils.hkdf(sharedSecret, CONTROL_SALT, CONTROL_READ_ENCRYPTION_KEY);
        byte[] writeKey = CryptoUtils.hkdf(sharedSecret, CONTROL_SALT, CONTROL_WRITE_ENCRYPTION_KEY);

        return new SessionKeys(readKey, writeKey);
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] out = new byte[a.length + b.length];
        System.arraycopy(a, 0, out, 0, a.length);
        System.arraycopy(b, 0, out, a.length, b.length);
        return out;
    }

    /**
     * Helper that validates the TLV map for the specification required pairing state.
     */
    protected static class Validator {

        private static final Map<PairingState, Set<Integer>> SPECIFICATION_REQUIRED_KEYS = Map.of( //
                PairingState.M1, Set.of(TlvType.STATE.key, TlvType.PUBLIC_KEY.key), // TLVType.METHOD not required
                PairingState.M2, Set.of(TlvType.STATE.key, TlvType.PUBLIC_KEY.key, TlvType.ENCRYPTED_DATA.key), //
                PairingState.M3, Set.of(TlvType.STATE.key, TlvType.ENCRYPTED_DATA.key), //
                PairingState.M4, Set.of(TlvType.STATE.key));

        /**
         * Validates the TLV map for the specification required pairing state.
         *
         * @throws IllegalArgumentException if required keys are missing or state is invalid
         */
        public static void validate(PairingMethod method, Map<Integer, byte[]> tlv) throws IllegalArgumentException {
            if (tlv.containsKey(TlvType.ERROR.key)) {
                throw new IllegalArgumentException(
                        "Pairing method '%s' action failed with unknown error".formatted(method.name()));
            }

            byte[] stateBytes = tlv.get(TlvType.STATE.key);
            if (stateBytes == null || stateBytes.length != 1) {
                throw new IllegalArgumentException("Missing or invalid 'STATE' TLV (0x06)");
            }

            PairingState state = PairingState.from(stateBytes[0]);
            Set<Integer> expectedKeys = SPECIFICATION_REQUIRED_KEYS.get(state);

            if (expectedKeys == null) {
                throw new IllegalArgumentException(
                        "Pairing method '%s' unexpected state '%s'".formatted(method.name(), state.name()));
            }

            for (Integer key : expectedKeys) {
                if (!tlv.containsKey(key)) {
                    throw new IllegalArgumentException("Pairing method '%s' state '%s' required TLV '0x%02x' missing."
                            .formatted(method.name(), state.name(), key));
                }
            }
        }
    }
}
