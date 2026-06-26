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
package org.openhab.binding.ddwrt.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link DDWRTDeviceConfiguration} class contains fields mapping thing configuration parameters for
 * {@link DDWRTDeviceThingHandler}.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public class DDWRTDeviceConfiguration {

    public String hostname = "";
    public String user = "root";
    public boolean useSystemUser = false;
    public String password = "";
    public int port = 0;
    public int refreshInterval = 3;
    public String syslogPriority = "warning";

    /**
     * Returns the effective username for SSH connections.
     * When {@link #useSystemUser} is true, returns empty string so the SSH client
     * resolves from ~/.ssh/config or the system username.
     */
    public String getEffectiveUser() {
        return useSystemUser ? "" : user;
    }
}
