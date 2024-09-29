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
package org.openhab.binding.enphase.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link EnvoyConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class EnvoyConfiguration {

    public static final String DEFAULT_USERNAME = "envoy";
    private static final int DEFAULT_REFRESH_MINUTES = 5;

    public String serialNumber = "";
    public String hostname = "";
    public String username = DEFAULT_USERNAME;
    public String password = "";
    public String jwt = "";
    public boolean autoJwt = true;
    public String siteName = "";
    public int refresh = DEFAULT_REFRESH_MINUTES;

    @Override
    public String toString() {
        return "EnvoyConfiguration [serialNumber=" + serialNumber + ", hostname=" + hostname + ", username=" + username
                + ", password=" + password + ", refresh=" + refresh + "]";
    }
}
