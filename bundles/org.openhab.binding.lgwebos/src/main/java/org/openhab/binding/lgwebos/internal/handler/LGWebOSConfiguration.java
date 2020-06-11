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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link LGWebOSConfiguration} class contains the thing configuration
 * parameters for LGWebOS devices
 *
 * @author Sebastian Prehn - Initial contribution
 */
@NonNullByDefault
public class LGWebOSConfiguration {
    @Nullable
    String host; // name has to match LGWebOSBindingConstants.CONFIG_HOST
    int port = 3000; // 3001 for TLS
    @Nullable
    String key; // name has to match LGWebOSBindingConstants.CONFIG_KEY

    public String getHost() {
        String h = host;
        return h == null ? "" : h;
    }

    public String getKey() {
        String k = key;
        return k == null ? "" : k;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "WebOSConfiguration [host=" + host + ", port=" + port + ", key.length=" + getKey().length() + "]";
    }

}
