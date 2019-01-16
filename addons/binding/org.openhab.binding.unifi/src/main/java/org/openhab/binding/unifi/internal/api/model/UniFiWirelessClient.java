/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.unifi.internal.api.model;

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

    @Override
    public Boolean isWired() {
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
