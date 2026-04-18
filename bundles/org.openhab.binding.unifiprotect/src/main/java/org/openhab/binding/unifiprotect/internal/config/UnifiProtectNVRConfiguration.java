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
package org.openhab.binding.unifiprotect.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link UnifiProtectNVRConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UnifiProtectNVRConfiguration {
    public String hostname = "";

    // Private API credentials (required)
    public String username = "";
    public String password = "";
    public int port = 443;

    // Public API token (optional - will be auto-created if not provided)
    public String token = "";
}
