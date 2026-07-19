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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.support.http.HttpResponse;

/**
 * The {@code play_url} half of the RAOP {@code StreamProtocol} contract, scoped to what
 * AirPlay needs.
 *
 * <p>
 * The audio-streaming half of the contract ({@code setup}/{@code send_audio_packet})
 * belongs to the RAOP protocol implementation and is intentionally not part of this
 * interface.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface AirPlayStreamProtocol {

    /**
     * Plays media from a URL.
     *
     * @param timingServerPort local port of the NTP timing server
     * @param url URL to play
     * @param position start position in seconds
     * @return future response of the {@code /play} request (errors allowed, i.e. the
     *         future completes normally for non-2xx codes other than 403)
     */
    CompletableFuture<HttpResponse> playUrl(int timingServerPort, String url, double position);

    /** Tears down resources allocated by this protocol. */
    default void teardown() {
    }
}
