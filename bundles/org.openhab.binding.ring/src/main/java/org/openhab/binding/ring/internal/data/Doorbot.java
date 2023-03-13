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

import org.json.simple.JSONObject;
import org.openhab.binding.ring.internal.ApiConstants;

/**
 * @author Wim Vissers - Initial contribution
 */

public class Doorbot {

    /**
     * The JSONObject contains the data retrieved from the Ring API,
     * or the data to send to the API.
     */
    protected JSONObject jsonObject;

    /**
     * Create from a JSONObject, example:
     * {
     * "id": 5047591,
     * "description": "Front Door"
     * }
     *
     * @param jsonObject
     */
    public Doorbot(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    /**
     * Get the Doorbot id.
     *
     * @return the id.
     */
    @SuppressWarnings("unchecked")
    public String getId() {
        return jsonObject.getOrDefault(ApiConstants.DOORBOT_ID, "?").toString();
    }

    /**
     * Get the Doorbot description.
     *
     * @return the description.
     */
    @SuppressWarnings("unchecked")
    public String getDescription() {
        return jsonObject.getOrDefault(ApiConstants.DOORBOT_DESCRIPTION, "?").toString();
    }
}
