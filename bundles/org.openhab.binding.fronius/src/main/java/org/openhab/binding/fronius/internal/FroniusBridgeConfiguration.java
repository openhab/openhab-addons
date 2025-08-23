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
package org.openhab.binding.fronius.internal;

import static org.openhab.binding.fronius.internal.FroniusBindingConstants.DEFAULT_REFRESH_PERIOD;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link FroniusBridgeConfiguration} is the class used to match the
 * bridge configuration.
 *
 * @author Thomas Rokohl - Initial contribution
 */
@NonNullByDefault
public class FroniusBridgeConfiguration {
    public String hostname = ""; // FroniusBridgeHandler enforces that this not empty
    public @Nullable String username;
    public @Nullable String password;
    public int refreshInterval = DEFAULT_REFRESH_PERIOD;
    public String scheme = "http";
}
