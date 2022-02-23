/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.freeboxos.internal.api.wifi;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.lan.LanAccessPoint;
import org.openhab.binding.freeboxos.internal.api.lan.LanHost;

/**
 * The {@link AccessPointHost} is the Java class used to map the "SwitchStatus"
 * structure used by the response of the switch status API
 * https://dev.freebox.fr/sdk/os/switch/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class AccessPointHost {
    public class AccessPointHostsResponse extends Response<List<AccessPointHost>> {
    }

    private @NonNullByDefault({}) String bssid;
    private @Nullable LanHost host;
    private @NonNullByDefault({}) String mac;
    private int signal;

    public int getSignal() {
        return signal;
    }

    public String getBssid() {
        return bssid;
    }

    public String getMac() {
        return mac.toLowerCase();
    }

    public @Nullable String getSsid() {
        LanHost localHost = host;
        if (localHost != null) {
            LanAccessPoint accessPoint = localHost.getAccessPoint();
            if (accessPoint != null) {
                return accessPoint.getSsid();
            }
        }
        return null;
    }
}
