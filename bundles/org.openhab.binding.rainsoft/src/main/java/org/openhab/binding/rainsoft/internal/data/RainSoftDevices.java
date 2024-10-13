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
package org.openhab.binding.rainsoft.internal.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openhab.binding.rainsoft.internal.ApiConstants;
import org.openhab.binding.rainsoft.internal.RainSoftAccount;

/**
 *
 * @author Ben Rosenblum - Initial contribution
 */

public class RainSoftDevices {
    private List<WCS> wcslist;

    public RainSoftDevices(JSONObject jsonRainSoftDevices, RainSoftAccount rainSoftAccount) {
        addWCS((JSONArray) jsonRainSoftDevices.get(ApiConstants.DEVICES_WCS), rainSoftAccount);
    }

    /**
     * Helper method to create the doorbell list.
     *
     * @param jsonWCSs
     */
    private final void addWCS(JSONArray jsonWCS, RainSoftAccount rainSoftAccount) {
        wcslist = new ArrayList<>();
        for (Object obj : jsonWCS) {
            WCS wcs = new WCS((JSONObject) obj);
            wcs.setRainSoftAccount(rainSoftAccount);
            wcslist.add(wcs);
        }
    }

    /**
     * Retrieve the WCSs Collection.
     *
     * @return
     */
    public Collection<WCS> getWCS() {
        return wcslist;
    }

    /**
     * Retrieve a collection of all devices.
     *
     * @return
     */
    public Collection<RainSoftDevice> getRainSoftDevices() {
        List<RainSoftDevice> result = new ArrayList<>();
        result.addAll(wcslist);
        return result;
    }
}
