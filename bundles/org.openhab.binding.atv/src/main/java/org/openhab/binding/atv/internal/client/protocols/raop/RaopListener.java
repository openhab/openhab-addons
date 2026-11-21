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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Listener interface for RAOP state changes.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface RaopListener {

    /**
     * Media started playing with metadata.
     *
     * @param playbackInfo what is being played
     */
    void playing(PlaybackInfo playbackInfo);

    /** Media stopped playing. */
    void stopped();
}
