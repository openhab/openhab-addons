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
package org.openhab.binding.unifi.internal.access.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link UnifiAccessBridgeConfiguration} class contains fields mapping thing configuration parameters.
 * <p>
 * Since Phase F, connection details (host/username/password) come from the parent {@code unifi:controller}
 * bridge. The Access bridge configuration only carries Access-specific options.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UnifiAccessBridgeConfiguration {
    public int refreshInterval = 300;
    public String token = "";
    public boolean autoManageToken = true;
    /** True when {@link #token} was created by the binding's auto-provisioning, not pasted by the user. */
    public boolean tokenAutoManaged = false;
}
