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
 * Portal address reported by the Robonect API.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public class Address {
    private @Nullable String url;
    private @Nullable Host host;

    /**
     * Returns the portal URL.
     *
     * @return the portal URL, or {@code null} if absent
     */
    public @Nullable String getUrl() {
        return url;
    }

    /**
     * Returns the portal host information.
     *
     * @return the portal host information, or {@code null} if absent
     */
    public @Nullable Host getHost() {
        return host;
    }
}
