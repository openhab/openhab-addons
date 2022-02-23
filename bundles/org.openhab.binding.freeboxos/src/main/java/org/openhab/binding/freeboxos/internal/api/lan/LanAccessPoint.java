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
package org.openhab.binding.freeboxos.internal.api.lan;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.wifi.WifiInformation;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link LanAccessPoint} is the Java class used to map the "LanHostL3Connectivity"
 * structure used by the Lan Hosts Browser API
 * https://dev.freebox.fr/sdk/os/lan/#lan-browser
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class LanAccessPoint {
    @SerializedName("uid")
    private @NonNullByDefault({}) String id;
    private @Nullable String type;
    private @Nullable WifiInformation wifiInformation;

    public @Nullable String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public @Nullable String getSsid() {
        WifiInformation localWifi = wifiInformation;
        return localWifi != null ? localWifi.getSsid() : null;
    }

    public int getSignal() {
        // Valid RSSI values goes from -120 to 0.
        WifiInformation localWifi = wifiInformation;
        return localWifi != null ? localWifi.getSignal() : 1;
    }
}
