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
package org.openhab.binding.unifi.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration fields for the {@code unifi:controller} bridge. Bound by the framework from the thing's
 * configuration.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UniFiControllerConfiguration {

    public String host = "";
    public String username = "";
    public String password = "";
    public int port = 443;
    public boolean unifios = true;
    public int timeoutSeconds = 30;
}
