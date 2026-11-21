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

import com.google.gson.annotations.SerializedName;

/**
 * Portal host reported by the Robonect API.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public class Host {
    private @Nullable String friendly;

    @SerializedName("default")
    private @Nullable String defaultHost;

    /**
     * Returns the friendly host name.
     *
     * @return the friendly host name, or {@code null} if absent
     */
    public @Nullable String getFriendly() {
        return friendly;
    }

    /**
     * Returns the default host name.
     *
     * @return the default host name, or {@code null} if absent
     */
    public @Nullable String getDefault() {
        return defaultHost;
    }
}
