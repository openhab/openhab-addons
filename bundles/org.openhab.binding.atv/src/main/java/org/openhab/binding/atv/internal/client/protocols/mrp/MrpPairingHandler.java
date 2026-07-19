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
package org.openhab.binding.atv.internal.client.protocols.mrp;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.PairingHandler;
import org.openhab.binding.atv.internal.client.auth.HapCredentials;
import org.openhab.binding.atv.internal.client.auth.HapPairSetup;
import org.openhab.binding.atv.internal.client.conf.BaseService;
import org.openhab.binding.atv.internal.client.core.AtvRuntime;
import org.openhab.binding.atv.internal.client.core.DeviceLoop;
import org.openhab.binding.atv.internal.client.exceptions.PairingError;
import org.openhab.binding.atv.internal.client.settings.InfoSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pairing handler for MRP: pairs via {@link MrpPairSetupProcedure} and, on success,
 * verifies the new credentials (needed from tvOS 14) before storing them on the service.
 * Persisting the credentials into {@code Settings} storage is left to the relay
 * (Wave 6), as Java settings records are immutable.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class MrpPairingHandler extends PairingHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MrpPairingHandler.class);

    private final MrpConnection connection;
    private final MrpProtocol protocol;
    private final HapPairSetup srp;
    private final MrpPairSetupProcedure pairingProcedure;
    private final DeviceLoop loop;

    private @Nullable String pinCode;
    private boolean hasPaired;

    /**
     * Creates a new pairing handler.
     *
     * @param address device address
     * @param service the MRP service to pair with
     * @param info identity settings used for {@code DEVICE_INFORMATION}
     * @param runtime runtime providing the scheduler
     */
    public MrpPairingHandler(String address, BaseService service, InfoSettings info, AtvRuntime runtime) {
        super(service);
        this.srp = new HapPairSetup();
        this.connection = new MrpConnection(address, service.port());
        this.loop = runtime.newDeviceLoop();
        this.protocol = new MrpProtocol(connection, service, info, loop, runtime, srp.pairingId());
        this.pairingProcedure = new MrpPairSetupProcedure(protocol, srp);
    }

    @Override
    public CompletableFuture<Void> close() {
        connection.close();
        loop.shutdown();
        return super.close();
    }

    @Override
    public boolean hasPaired() {
        return hasPaired;
    }

    @Override
    public CompletableFuture<Void> begin() {
        return pairingProcedure.startPairing()
                .exceptionallyCompose(t -> CompletableFuture.failedFuture(wrapPairingError(t)));
    }

    @Override
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public CompletableFuture<Void> finish() {
        String pin = pinCode;
        if (pin == null) {
            return CompletableFuture.failedFuture(new PairingError("no pin given"));
        }
        CompletableFuture<Void> result = new CompletableFuture<>();
        Thread.ofVirtual().name("mrp-pairing-finish").start(() -> {
            try {
                HapCredentials credentials = pairingProcedure.finishPairing(pin).join();
                String credentialsString = credentials.toString();
                LOGGER.debug("Verifying credentials {}", credentialsString);

                MrpPairVerifyProcedure verifier = new MrpPairVerifyProcedure(protocol,
                        HapCredentials.parse(credentialsString));
                verifier.verifyCredentials().join();

                service().setCredentials(credentialsString);
                hasPaired = true;
                MrpFutures.completeVoid(result);
            } catch (Throwable t) {
                result.completeExceptionally(wrapPairingError(t));
            }
        });
        return result;
    }

    private static Throwable wrapPairingError(Throwable t) {
        Throwable cause = t;
        if (t instanceof java.util.concurrent.CompletionException completion) {
            Throwable completionCause = completion.getCause();
            if (completionCause != null) {
                cause = completionCause;
            }
        }
        if (cause instanceof PairingError) {
            return cause;
        }
        return new PairingError(Objects.requireNonNullElse(cause.getMessage(), cause.toString()), cause);
    }

    @Override
    public boolean deviceProvidesPin() {
        return true;
    }

    @Override
    public void pin(String pin) {
        this.pinCode = zeroFill(pin, 4);
        LOGGER.debug("MRP PIN changed to {}", pinCode);
    }

    private static String zeroFill(String value, int width) {
        StringBuilder builder = new StringBuilder(value);
        while (builder.length() < width) {
            builder.insert(0, '0');
        }
        return builder.toString();
    }
}
