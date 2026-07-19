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
package org.openhab.binding.atv.internal.client;

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.conf.BaseService;

/**
 * Base class for APIs used to pair with an Apple TV.
 *
 * <p>
 * Typical usage: call {@link #begin()}, feed the PIN via {@link #pin(String)} (either shown by the device or
 * entered on it depending on {@link #deviceProvidesPin()}), then call {@link #finish()} and check
 * {@link #hasPaired()}. Always call {@link #close()} to free resources.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public abstract class PairingHandler {

    private final BaseService service;

    /**
     * Initializes a new pairing handler.
     *
     * @param service service to pair with
     */
    protected PairingHandler(BaseService service) {
        this.service = service;
    }

    /**
     * Returns the service used for pairing. On successful pairing, generated credentials are stored on this service.
     *
     * @return service used for pairing
     */
    public BaseService service() {
        return service;
    }

    /**
     * Frees allocated resources after pairing.
     *
     * @return future completing when resources have been released
     */
    public CompletableFuture<Void> close() {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Sets the PIN code used for pairing.
     *
     * @param pin PIN code
     */
    public abstract void pin(String pin);

    /**
     * Returns whether the remote device presents a PIN code (that must be fed to {@link #pin(String)}) or expects a
     * PIN chosen by this library to be entered on the device.
     *
     * @return {@code true} if the remote device presents the PIN code
     */
    public abstract boolean deviceProvidesPin();

    /**
     * Returns if a successful pairing has been performed. The value is reset when pairing is restarted.
     *
     * @return {@code true} if pairing succeeded
     */
    public abstract boolean hasPaired();

    /**
     * Starts the pairing process.
     *
     * @return future completing when the pairing process has started
     */
    public abstract CompletableFuture<Void> begin();

    /**
     * Stops the pairing process.
     *
     * @return future completing when the pairing process has finished
     */
    public abstract CompletableFuture<Void> finish();
}
