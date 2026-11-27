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
package org.openhab.binding.evohome.internal.api.models.v2.dto.request;

import java.time.Instant;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Builder for mode API requests
 *
 * @author Jasper van Zuijlen - Initial contribution
 * @author Kovacs Istvan - Exact system mode expiry
 *
 */
public class ModeBuilder implements RequestBuilder<Mode> {

    private String mode;
    private boolean hasSetMode;
    private @Nullable Instant until;

    /**
     * Creates a new mode command
     *
     * @return A mode command or null when the configuration is invalid
     *
     */
    @Override
    public Mode build() {
        if (hasSetMode) {
            return new Mode(mode, until);
        }
        return null;
    }

    public ModeBuilder setMode(String mode) {
        this.hasSetMode = true;
        this.mode = mode;
        this.until = null;
        return this;
    }

    public ModeBuilder setTemporaryMode(String mode, Instant until) {
        setMode(mode);
        this.until = until;
        return this;
    }
}
