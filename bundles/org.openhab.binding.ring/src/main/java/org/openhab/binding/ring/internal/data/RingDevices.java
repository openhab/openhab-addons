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
package org.openhab.binding.ring.internal.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openhab.binding.ring.internal.ApiConstants;
import org.openhab.binding.ring.internal.RingAccount;

/**
 *
 * @author Wim Vissers - Initial contribution
 * @author Chris Milbert - stickupcam contribution
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 */

public class RingDevices {
    private List<Doorbell> doorbells;
    private List<Stickupcam> stickupcams;

    /**
     * Create RingDevices instance from JSON Object.
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
     * @param jsonRingDevices the JSON ring devices retrieved from the Ring API.
     */
    public RingDevices(JSONObject jsonRingDevices, RingAccount ringAccount) {
        addDoorbells((JSONArray) jsonRingDevices.get(ApiConstants.DEVICES_DOORBOTS), ringAccount);
        addStickupCams((JSONArray) jsonRingDevices.get(ApiConstants.DEVICES_STICKUP_CAMS), ringAccount);
    }

    /**
     * Helper method to create the doorbell list.
     *
     * @param jsonDoorbells
     */
    private final void addDoorbells(JSONArray jsonDoorbells, RingAccount ringAccount) {
        doorbells = new ArrayList<>();
        for (Object obj : jsonDoorbells) {
            Doorbell doorbell = new Doorbell((JSONObject) obj);
            doorbell.setRingAccount(ringAccount);
            doorbells.add(doorbell);
        }
    }

    /**
     * Retrieve the Doorbells Collection.
     *
     * @return
     */
    public Collection<Doorbell> getDoorbells() {
        return doorbells;
    }

    /**
     * Retrieve the Stickupcams Collection.
     *
     * @return
     */
    public Collection<Stickupcam> getStickupcams() {
        return stickupcams;
    }

    /**
     * Helper method to create the stickupcam list.
     *
     * @param jsonStickupcams
     */
    private final void addStickupCams(JSONArray jsonStickupcams, RingAccount ringAccount) {
        stickupcams = new ArrayList<>();
        for (Object obj : jsonStickupcams) {
            Stickupcam stickupcam = new Stickupcam((JSONObject) obj);
            stickupcam.setRingAccount(ringAccount);
            stickupcams.add(stickupcam);
        }
    }

    /**
     * Retrieve a collection of all devices.
     *
     * @return
     */
    public Collection<RingDevice> getRingDevices() {
        List<RingDevice> result = new ArrayList<>();
        result.addAll(doorbells);
        result.addAll(stickupcams);
        return result;
    }
}
