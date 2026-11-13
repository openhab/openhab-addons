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
package org.openhab.binding.atv.internal.client.protocols.raop;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.capability.RemoteControl;
import org.openhab.binding.atv.internal.client.core.Capability;
import org.openhab.binding.atv.internal.client.core.CapabilitySource;

/**
 * Implementation of remote control functionality for RAOP.
 *
 * <p>
 * Pause stops playback until properly implemented. Volume up/down are exposed on
 * {@code Audio} instead of this interface.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class RaopRemoteControl implements RemoteControl, CapabilitySource {

    private final RaopPlaybackManager playbackManager;

    /**
     * Creates a new remote control instance.
     *
     * @param playbackManager playback manager holding the stream state
     */
    public RaopRemoteControl(RaopPlaybackManager playbackManager) {
        this.playbackManager = playbackManager;
    }

    @Override
    public CompletableFuture<Void> pause() {
        RaopStreamClient client = playbackManager.streamClient();
        if (client != null) {
            client.stop();
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> stop() {
        RaopStreamClient client = playbackManager.streamClient();
        if (client != null) {
            client.stop();
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public Set<Capability> capabilities() {
        return Set.of(Capability.RC_PAUSE, Capability.RC_STOP);
    }
}
