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
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Mower software version reported by version-1 Robonect firmware.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public class VersionMsw {

    private @Nullable String title;
    private @Nullable String version;
    private @Nullable String compiled;

    /**
     * Returns the title.
     *
     * @return the title, or {@code null} if not reported
     */
    public @Nullable String getTitle() {
        return title;
    }

    /**
     * Returns the version.
     *
     * @return the version, or {@code null} if not reported
     */
    public @Nullable String getVersion() {
        return version;
    }

    /**
     * Returns the compilation timestamp.
     *
     * @return the compilation timestamp, or {@code null} if not reported
     */
    public @Nullable String getCompiled() {
        return compiled;
    }
}
