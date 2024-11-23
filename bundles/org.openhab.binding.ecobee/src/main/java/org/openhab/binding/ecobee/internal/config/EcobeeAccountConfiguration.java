/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.ecobee.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link EcobeeAccountConfiguration} class contains fields mapping
 * to the account thing configuration parameters.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class EcobeeAccountConfiguration {

    /**
     * Ecobee API key
     */
    public @Nullable String apiKey;

    /**
     * Time in seconds between information refresh
     */
    public @Nullable Integer refreshIntervalNormal;

    /**
     * Time in seconds to wait after successful update, command or action before refresh
     */
    public @Nullable Integer refreshIntervalQuick;

    /**
     * Time in seconds to allow API request to complete
     */
    public @Nullable Integer apiTimeout;

    /*
     * Enable/disable automatic discovery
     */
    public @Nullable Boolean discoveryEnabled;
}
