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
package org.openhab.binding.atv.internal.client.protocols.companion;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.PairingHandler;
import org.openhab.binding.atv.internal.client.auth.HapCredentials;
import org.openhab.binding.atv.internal.client.auth.HapPairSetup;
import org.openhab.binding.atv.internal.client.core.Core;
import org.openhab.binding.atv.internal.client.exceptions.PairingError;
import org.openhab.binding.atv.internal.client.support.CryptoKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pairing handler used to pair the Companion link protocol.
 *
 * <p>
 * On success the credential string is stored on the service. The {@code Settings}
 * records are immutable, so persisting them beyond that is left to the caller.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class CompanionPairingHandler extends PairingHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanionPairingHandler.class);

    private final ExecutorService executor = Executors
            .newThreadPerTaskExecutor(Thread.ofVirtual().name("companion-pairing-", 0).factory());
    private final CompanionProtocol protocol;
    private final CompanionPairSetupProcedure pairingProcedure;

    private @Nullable String pinCode;
    private boolean hasPaired;

    /**
     * Creates a new pairing handler.
     *
     * @param core protocol context
     * @param name display name registered on the device, or {@code null} to use the default
     */
    public CompanionPairingHandler(Core core, @Nullable String name) {
        super(core.service());
        CompanionConnection connection = new CompanionConnection(core.address(), core.service().port(), null);
        this.protocol = new CompanionProtocol(connection, core.service());
        HapPairSetup pairSetup = new HapPairSetup(false, name == null ? "openHAB" : name,
                CryptoKeys.ed25519Generate().seed(), UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
        this.pairingProcedure = new CompanionPairSetupProcedure(protocol, pairSetup);
    }

    @Override
    public CompletableFuture<Void> close() {
        protocol.stop();
        executor.shutdown();
        return super.close();
    }

    @Override
    public boolean hasPaired() {
        return hasPaired;
    }

    @Override
    public CompletableFuture<Void> begin() {
        LOGGER.debug("Start pairing Companion");
        return CompletableFuture.runAsync(() -> {
            try {
                pairingProcedure.startPairing();
            } catch (PairingError e) {
                throw e;
            } catch (Exception e) {
                throw new PairingError("failed to start pairing", e);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> finish() {
        LOGGER.debug("Finish pairing Companion");
        String pin = pinCode;
        if (pin == null) {
            return CompletableFuture.failedFuture(new PairingError("no pin given"));
        }
        return CompletableFuture.runAsync(() -> {
            HapCredentials credentials;
            try {
                credentials = pairingProcedure.finishPairing(pin);
            } catch (PairingError e) {
                throw e;
            } catch (Exception e) {
                throw new PairingError("failed to finish pairing", e);
            }
            service().setCredentials(credentials.toString());
            hasPaired = true;
        }, executor);
    }

    @Override
    public boolean deviceProvidesPin() {
        return true;
    }

    @Override
    public void pin(String pin) {
        pinCode = zfill(pin, 4);
        LOGGER.debug("Companion PIN changed to {}", pinCode);
    }

    private static String zfill(String value, int width) {
        StringBuilder padded = new StringBuilder(value);
        while (padded.length() < width) {
            padded.insert(0, '0');
        }
        return padded.toString();
    }
}
