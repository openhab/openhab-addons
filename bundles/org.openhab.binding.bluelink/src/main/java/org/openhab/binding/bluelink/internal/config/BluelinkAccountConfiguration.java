/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.bluelink.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Configuration for the Bluelink account bridge.
 *
 * @author Marcus Better - Initial contribution
 */
@NonNullByDefault
public class BluelinkAccountConfiguration {

    /**
     * Bluelink account username (email).
     */
    public @Nullable String username;

    /**
     * Bluelink account password.
     */
    public @Nullable String password;

    /**
     * Bluelink service PIN (required for lock/unlock commands).
     */
    public @Nullable String pin;

    /**
     * API region. Auto-detected from system locale if not set.
     */
    public @Nullable String region;

    /**
     * API base URL (for unit test use).
     */
    public @Nullable String apiBaseUrl;
}
