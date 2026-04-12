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
 * <p>
 * Since Phase F, connection details (host/username/password/port) come from the parent {@code unifi:controller}
 * bridge. The NVR configuration only carries Protect-specific options.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UnifiProtectNVRConfiguration {

    /** Public API token. Optional — auto-provisioned via the private API if blank. */
    public String token = "";
}
