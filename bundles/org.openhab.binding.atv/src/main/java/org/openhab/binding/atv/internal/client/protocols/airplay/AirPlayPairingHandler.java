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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.PairingHandler;
import org.openhab.binding.atv.internal.client.auth.AuthenticationType;
import org.openhab.binding.atv.internal.client.auth.HapCredentials;
import org.openhab.binding.atv.internal.client.conf.BaseService;
import org.openhab.binding.atv.internal.client.exceptions.BackOffError;
import org.openhab.binding.atv.internal.client.exceptions.ConnectionFailedError;
import org.openhab.binding.atv.internal.client.exceptions.NoCredentialsError;
import org.openhab.binding.atv.internal.client.exceptions.OperationTimeoutError;
import org.openhab.binding.atv.internal.client.exceptions.PairingError;
import org.openhab.binding.atv.internal.client.support.http.HttpConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Device pairing for AirPlay (and RAOP, which shares this handler).
 *
 * <p>
 * HAP pair-setup is used for AirPlay 2 devices and legacy pairing for AirPlay 1 devices.
 * Successful pairing stores the resulting credentials on the service; persisting them into
 * {@code Settings} storage is left to the relay (Wave 6), which owns the storage.
 *
 * <p>
 * Errors from the underlying procedures are mapped: timeouts become
 * {@link ConnectionFailedError}, {@link BackOffError} and {@link NoCredentialsError} pass
 * through, and everything else becomes {@link PairingError}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class AirPlayPairingHandler extends PairingHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AirPlayPairingHandler.class);

    private final String address;
    private final @Nullable String name;
    private final AirPlayMajorVersion airplayVersion;
    private final @Nullable HapCredentials newLegacyCredentials;

    private @Nullable HttpConnection http;
    private @Nullable AirPlayPairSetupProcedure pairingProcedure;
    private @Nullable String pinCode;
    private volatile boolean hasPaired;

    /**
     * Creates a new pairing handler.
     *
     * @param service the service to pair with (credentials are stored here on success)
     * @param address the device IP address
     * @param airplayVersion the major AirPlay version (selects HAP vs legacy pairing)
     * @param name our display name, shown on the device during HAP pairing
     */
    public AirPlayPairingHandler(BaseService service, String address, AirPlayMajorVersion airplayVersion,
            @Nullable String name) {
        this(service, address, airplayVersion, name, null);
    }

    /**
     * Creates a new pairing handler with injectable legacy credentials, for reproducible
     * tests.
     *
     * @param service the service to pair with
     * @param address the device IP address
     * @param airplayVersion the major AirPlay version (selects HAP vs legacy pairing)
     * @param name our display name, shown on the device during HAP pairing
     * @param newLegacyCredentials credentials to establish in the legacy flow, or
     *            {@code null} to generate fresh random ones
     */
    public AirPlayPairingHandler(BaseService service, String address, AirPlayMajorVersion airplayVersion,
            @Nullable String name, @Nullable HapCredentials newLegacyCredentials) {
        super(service);
        this.address = address;
        this.airplayVersion = airplayVersion;
        this.name = name;
        this.newLegacyCredentials = newLegacyCredentials;
    }

    @Override
    public boolean hasPaired() {
        return hasPaired;
    }

    @Override
    public CompletableFuture<Void> close() {
        if (http != null) {
            http.close();
        }
        return super.close();
    }

    @Override
    public CompletableFuture<Void> begin() {
        return HttpConnection.connect(address, service().port()).thenCompose(connection -> {
            this.http = connection;
            AirPlayPairSetupProcedure procedure = AirPlayAuth
                    .pairSetup(airplayVersion == AirPlayMajorVersion.AirPlayV2 ? AuthenticationType.HAP
                            : AuthenticationType.Legacy, connection, name, newLegacyCredentials);
            this.pairingProcedure = procedure;
            this.hasPaired = false;
            return mapErrors(procedure.startPairing());
        });
    }

    @Override
    public CompletableFuture<Void> finish() {
        AirPlayPairSetupProcedure procedure = pairingProcedure;
        String pin = pinCode;
        if (procedure == null) {
            return CompletableFuture.failedFuture(new PairingError("pairing was not started"));
        }
        if (pin == null) {
            return CompletableFuture.failedFuture(new PairingError("no pin given"));
        }

        return mapErrors(procedure.finishPairing("", pin)).thenAccept(credentials -> {
            service().setCredentials(credentials.toString());
            hasPaired = true;
        });
    }

    @Override
    public void pin(String pin) {
        this.pinCode = zeroFill(pin, 4);
        LOGGER.debug("AirPlay PIN changed to {}", pinCode);
    }

    @Override
    public boolean deviceProvidesPin() {
        return true;
    }

    private static String zeroFill(String value, int width) {
        StringBuilder builder = new StringBuilder(value);
        while (builder.length() < width) {
            builder.insert(0, '0');
        }
        return builder.toString();
    }

    /** Maps failures from pairing procedures to the exception types described above. */
    private static <T> CompletableFuture<T> mapErrors(CompletableFuture<T> future) {
        return future.handle((value, error) -> {
            if (error == null) {
                return value;
            }
            Throwable cause = error instanceof CompletionException && error.getCause() != null ? error.getCause()
                    : error;
            if (cause instanceof OperationTimeoutError) {
                throw new CompletionException(new ConnectionFailedError(String.valueOf(cause.getMessage()), cause));
            }
            if (cause instanceof BackOffError || cause instanceof NoCredentialsError) {
                throw new CompletionException(cause);
            }
            throw new CompletionException(new PairingError(String.valueOf(cause.getMessage()), cause));
        });
    }
}
