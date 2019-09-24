/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.lgwebos.internal.handler;

/**
 * The {@link LGWebOSConfiguration} class contains the thing configuration
 * parameters for LGWebOS devices
 *
 * @author Sebastian Prehn - Initial contribution
 */
public class LGWebOSConfiguration {
    String host; // name has to match LGWebOSBindingConstants.CONFIG_HOST
    int port = 3000; // 3001 for TLS
    String key; // name has to match LGWebOSBindingConstants.CONFIG_KEY

    @Override
    public String toString() {
        return "WebOSConfiguration [host=" + host + ", port=" + port + ", key.length="
                + (key == null ? "null" : key.length()) + "]";
    }

}
