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
package org.openhab.binding.atv.internal.client.protocols.airplay;

import java.util.EnumSet;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.auth.AuthenticationType;
import org.openhab.binding.atv.internal.client.auth.EncryptionKeys;
import org.openhab.binding.atv.internal.client.auth.HapCredentials;
import org.openhab.binding.atv.internal.client.auth.HapSession;
import org.openhab.binding.atv.internal.client.conf.BaseService;
import org.openhab.binding.atv.internal.client.exceptions.NotSupportedError;
import org.openhab.binding.atv.internal.client.support.http.HttpConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Picks AirPlay authentication type based on device support.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class AirPlayAuth {

    /** HKDF salt for the AirPlay control channel keys ({@code CONTROL_SALT}). */
    public static final String CONTROL_SALT = "Control-Salt";

    /** HKDF info for the key encrypting outgoing data ({@code CONTROL_OUTPUT_INFO}). */
    public static final String CONTROL_OUTPUT_INFO = "Control-Write-Encryption-Key";

    /** HKDF info for the key decrypting incoming data ({@code CONTROL_INPUT_INFO}). */
    public static final String CONTROL_INPUT_INFO = "Control-Read-Encryption-Key";

    private static final Logger LOGGER = LoggerFactory.getLogger(AirPlayAuth.class);

    private AirPlayAuth() {
    }

    /**
     * Returns the procedure used for Pair-Setup.
     *
     * @param authType the authentication type to use
     * @param connection the HTTP connection to the device
     * @param name display name included in HAP pairing (see
     *            {@link AirPlayPairSetupProcedure}), may be {@code null}
     * @return the pair-setup procedure
     * @throws NotSupportedError if the authentication type does not support pair-setup
     */
    public static AirPlayPairSetupProcedure pairSetup(AuthenticationType authType, HttpConnection connection,
            @Nullable String name) {
        return pairSetup(authType, connection, name, null);
    }

    /**
     * Returns the procedure used for Pair-Setup, with injectable legacy credentials for
     * reproducible tests.
     *
     * @param authType the authentication type to use
     * @param connection the HTTP connection to the device
     * @param name display name included in HAP pairing, may be {@code null}
     * @param newLegacyCredentials credentials to establish in the legacy flow, or
     *            {@code null} to generate fresh random ones
     * @return the pair-setup procedure
     * @throws NotSupportedError if the authentication type does not support pair-setup
     */
    public static AirPlayPairSetupProcedure pairSetup(AuthenticationType authType, HttpConnection connection,
            @Nullable String name, @Nullable HapCredentials newLegacyCredentials) {
        LOGGER.debug("Setting up new AirPlay Pair-Setup procedure with type {}", authType);

        if (authType == AuthenticationType.Legacy) {
            LegacySrpAuthHandler legacySrp = new LegacySrpAuthHandler(
                    newLegacyCredentials != null ? newLegacyCredentials : LegacySrpAuthHandler.newCredentials());
            legacySrp.initialize();
            return new AirPlayLegacyPairSetupProcedure(connection, legacySrp);
        }
        if (authType == AuthenticationType.HAP) {
            return new AirPlayHapPairSetupProcedure(connection, name);
        }

        throw new NotSupportedError("authentication type " + authType + " does not support Pair-Setup");
    }

    /**
     * Returns the procedure used for Pair-Verify.
     *
     * @param credentials the stored credentials (their type selects the scheme)
     * @param connection the HTTP connection to the device
     * @return the pair-verify procedure
     */
    public static AirPlayPairVerifyProcedure pairVerify(HapCredentials credentials, HttpConnection connection) {
        LOGGER.debug("Setting up new AirPlay Pair-Verify procedure with type {}", credentials.type());

        if (credentials.type() == AuthenticationType.Null) {
            return new NullPairVerifyProcedure();
        }
        if (credentials.type() == AuthenticationType.Legacy) {
            LegacySrpAuthHandler legacySrp = new LegacySrpAuthHandler(credentials);
            legacySrp.initialize();
            return new AirPlayLegacyPairVerifyProcedure(connection, legacySrp);
        }

        if (credentials.type() == AuthenticationType.HAP) {
            return new AirPlayHapPairVerifyProcedure(connection, credentials);
        }
        return new AirPlayHapTransientPairVerifyProcedure(connection);
    }

    /**
     * Performs Pair-Verify on a connection and enables transparent encryption when the
     * scheme provides session keys: a {@link HapSession} with the {@code Control-Salt}
     * derived keys is installed as the connection's send/receive processors.
     *
     * @param credentials the stored credentials
     * @param connection the HTTP connection to the device
     * @return future completing with the used verifier
     */
    public static CompletableFuture<AirPlayPairVerifyProcedure> verifyConnection(HapCredentials credentials,
            HttpConnection connection) {
        AirPlayPairVerifyProcedure verifier = pairVerify(credentials, connection);
        return verifier.verifyCredentials().thenApply(hasEncryptionKeys -> {
            if (hasEncryptionKeys) {
                EncryptionKeys keys = verifier.encryptionKeys(CONTROL_SALT, CONTROL_OUTPUT_INFO, CONTROL_INPUT_INFO);
                HapSession session = new HapSession();
                session.enable(keys.outputKey(), keys.inputKey());
                connection.setReceiveProcessor(session::decrypt);
                connection.setSendProcessor(session::encrypt);
            }
            return verifier;
        });
    }

    /**
     * Extracts credentials from a service based on what is supported: stored credentials
     * win; otherwise transient credentials are used when the device announces
     * {@code SupportsSystemPairing} or
     * {@code SupportsCoreUtilsPairingAndEncryption}; otherwise no credentials.
     *
     * @param service the AirPlay service
     * @return the credentials to use
     */
    public static HapCredentials extractCredentials(BaseService service) {
        if (service.credentials().isPresent()) {
            return HapCredentials.parse(service.credentials().get());
        }

        EnumSet<AirPlayFlags> flags = AirPlayFlags
                .parse(service.properties().getOrDefault("features", service.properties().getOrDefault("ft", "0x0")));
        if (flags.contains(AirPlayFlags.SupportsSystemPairing)
                || flags.contains(AirPlayFlags.SupportsCoreUtilsPairingAndEncryption)) {
            return HapCredentials.TRANSIENT_CREDENTIALS;
        }

        return HapCredentials.NO_CREDENTIALS;
    }
}
