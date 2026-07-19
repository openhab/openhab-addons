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

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.conf.AtvConfig;
import org.openhab.binding.atv.internal.client.conf.BaseService;
import org.openhab.binding.atv.internal.client.dto.ConnectOptions;
import org.openhab.binding.atv.internal.client.dto.PairingRequirement;
import org.openhab.binding.atv.internal.client.dto.ScanOptions;

/**
 * Various helper methods for connecting to and checking support for devices.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class Helpers {

    private Helpers() {
    }

    /**
     * Connects to the first discovered device.
     *
     * <p>
     * Convenience method that scans, picks the first device found, connects to it and
     * passes it to the handler; the device is closed when the handler's future completes.
     * An optional {@code notFound} callback is invoked when no device was found. Very
     * inflexible in many cases, but can be handy sometimes when trying things.
     *
     * @param scanOptions options used for the scan
     * @param handler receives the connected device and returns a future signaling when it
     *            is done with it
     * @param notFound invoked when no device was found, or {@code null}
     * @return future completing when the handler (or {@code notFound}) is done
     */
    public static CompletableFuture<Void> autoConnect(ScanOptions scanOptions,
            Function<AppleTV, CompletableFuture<Void>> handler, @Nullable Supplier<CompletableFuture<Void>> notFound) {
        return Atv.scan(scanOptions).thenCompose(atvs -> {
            if (atvs.isEmpty()) {
                return notFound != null ? notFound.get() : CompletableFuture.completedFuture(null);
            }
            // Take the first device found
            ConnectOptions connectOptions = ConnectOptions.defaults().withRuntime(scanOptions.runtime())
                    .withStorage(scanOptions.storage());
            return Atv.connect(atvs.get(0), connectOptions).thenCompose(atv -> {
                CompletableFuture<Void> handled;
                try {
                    handled = handler.apply(atv);
                } catch (RuntimeException e) {
                    handled = CompletableFuture.failedFuture(e);
                }
                return handled.handle((result, error) -> error)
                        .thenCompose(error -> atv.close()
                                .thenCompose(ignore -> error != null ? CompletableFuture.<Void> failedFuture(error)
                                        : CompletableFuture.<Void> completedFuture(null)));
            });
        });
    }

    /**
     * Returns if a device is supported: at least one service must have a pairing
     * requirement other than {@code Unsupported} or {@code Disabled}. Even if this method
     * returns {@code true}, pairing (or existing credentials) might still be needed.
     *
     * @param config configuration to check
     * @return {@code true} when the device is supported
     */
    public static boolean isDeviceSupported(AtvConfig config) {
        Set<PairingRequirement> requirements = EnumSet.noneOf(PairingRequirement.class);
        for (BaseService service : config.services()) {
            requirements.add(service.pairing());
        }
        requirements.removeAll(List.of(PairingRequirement.Unsupported, PairingRequirement.Disabled));
        return !requirements.isEmpty();
    }
}
