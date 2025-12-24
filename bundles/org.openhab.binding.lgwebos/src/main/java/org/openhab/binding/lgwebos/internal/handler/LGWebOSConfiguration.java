/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
    @Nullable
    String key; // name has to match LGWebOSBindingConstants.CONFIG_KEY
    @Nullable
    String macAddress; // name has to match LGWebOSBindingConstants.CONFIG_MAC_ADDRESS
    @Nullable
    String broadcastAddress; // name has to match LGWebOSBindingConstants.CONFIG_BROADCAST_ADDRESS
    boolean useTLS = true;

    public String getHost() {
        String h = host;
        return h == null ? "" : h;
    }

    public String getKey() {
        String k = key;
        return k == null ? "" : k;
    }

    public boolean getUseTLS() {
        return useTLS;
    }

    public String getMacAddress() {
        String m = macAddress;
        return m == null ? "" : m;
    }

    public String getBroadcastAddress() {
        String b = broadcastAddress;
        return b == null ? "" : b;
    }

    @Override
    public String toString() {
        return "WebOSConfiguration [host=" + host + ", useTLS=" + useTLS + ", key.length=" + getKey().length()
                + ", macAddress=" + macAddress + "]";
    }
}
