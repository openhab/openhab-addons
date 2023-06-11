/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.unifi.internal.api.dto;

import org.openhab.binding.unifi.internal.api.cache.UniFiControllerCache;
import org.openhab.binding.unifi.internal.api.util.UniFiTidyLowerCaseStringDeserializer;

import com.google.gson.annotations.JsonAdapter;

/**
 * A {@link UniFiWirelessClient} represents a wireless {@link UniFiClient}.
 *
 * A wireless client is not physically connected to the network - typically it is connected via a Wi-Fi adapter.
 *
 * @author Matthew Bowman - Initial contribution
 */
public class UniFiWirelessClient extends UniFiClient {

    @JsonAdapter(UniFiTidyLowerCaseStringDeserializer.class)
    private String apMac;

    private String essid;

    private Integer rssi;

    public UniFiWirelessClient(final UniFiControllerCache cache) {
        super(cache);
    }

    @Override
    public boolean isWired() {
        return false;
    }

    @Override
    public String getDeviceMac() {
        return apMac;
    }

    public String getEssid() {
        return essid;
    }

    public Integer getRssi() {
        return rssi;
    }
}
