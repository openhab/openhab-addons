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

    /**
     * Create RainSoftDevices instance from JSON Object.
     * {
     * "doorbots": [
     * {
     * "id": 5047591,
     * "description": "Front Door",
     * "device_id": "341513673f7a",
     * "time_zone": "Europe/Amsterdam",
     * "subscribed": true,
     * "subscribed_motions": true,
     * "battery_life": "93",
     * "external_connection": true,
     * "firmware_version": "Up to Date",
     * "kind": "doorbell_v3",
     * "latitude": 51.9436537,
     * "longitude": 4.5682205,
     * "address": "Nystadstraat 73, 3067 DT Rotterdam, Nederland",
     * "settings": {
     * "enable_vod": 1,
     * "exposure_control": 2,
     * "motion_zones": [
     * 1,
     * 1,
     * 1,
     * 1,
     * 1
     * ],
     * "motion_snooze_preset_profile": "low",
     * "motion_snooze_presets": [
     * "none",
     * "low",
     * "medium",
     * "high"
     * ],
     * "live_view_preset_profile": "middle",
     * "live_view_presets": [
     * "low",
     * "middle",
     * "high",
     * "highest"
     * ],
     * "pir_sensitivity_1": 0,
     * "vod_suspended": 0,
     * "doorbell_volume": 1,
     * "vod_status": "enabled"
     * },
     * "features": {
     * "motions_enabled": true,
     * "show_recordings": false,
     * "show_vod_settings": true
     * },
     * "owned": true,
     * "alerts": {
     * "connection": "online"
     * },
     * "motion_snooze": null,
     * "stolen": false,
     * "location_id": null,
     * "ring_id": null,
     * "owner": {
     * "id": 4445516,
     * "first_name": null,
     * "last_name": null,
     * "email": "xxx@acme.com"
     * }
     * }
     * ],
     * "authorized_doorbots": [],
     * "chimes": [],
     * "stickup_cams": [],
     * "base_stations": []
     * } *
     *
     * @param jsonRainSoftDevices the JSON rainsoft devices retrieved from the RainSoft API.
     */
    public RainSoftDevices(JSONObject jsonRainSoftDevices, RainSoftAccount rainSoftAccount) {
        addWCS((JSONArray) jsonRainSoftDevices.get(ApiConstants.DEVICES_DOORBOTS), rainSoftAccount);
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
