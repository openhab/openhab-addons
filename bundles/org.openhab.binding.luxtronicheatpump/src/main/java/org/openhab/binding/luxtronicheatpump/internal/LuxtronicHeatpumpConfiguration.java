/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.luxtronicheatpump.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link LuxtronicHeatpumpConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Stefan Giehl - Initial contribution
 */
@NonNullByDefault
public class LuxtronicHeatpumpConfiguration {

    public String ipAddress = "";
    public int port = 8888;
    public int refresh = 60000;

    public boolean isValid() {
        return !ipAddress.isEmpty() && port > 0 && refresh > 0;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("[IP=").append(ipAddress).append(",port=").append(port).append(",refresh=")
                .append(refresh).append("]").toString();
    }
}
