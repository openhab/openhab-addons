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
package org.openhab.binding.ring.internal.data;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ring.internal.ApiConstants;

import com.google.gson.JsonObject;

/**
 * @author Wim Vissers - Initial contribution
 */

@NonNullByDefault
public class Doorbot {

    /**
     * The JsonObject contains the data retrieved from the Ring API,
     * or the data to send to the API.
     */
    protected JsonObject jsonObject = new JsonObject();

    /**
     * Create from a JsonObject, example:
     * {
     * "id": 5047591,
     * "description": "Front Door"
     * }
     *
     * @param jsonObject
     */
    public Doorbot(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    /**
     * Get the Doorbot id.
     *
     * @return the id.
     */
    public String getId() {
        return jsonObject.get(ApiConstants.DOORBOT_ID).getAsString();
    }

    /**
     * Get the Doorbot description.
     *
     * @return the description.
     */
    public String getDescription() {
        return jsonObject.get(ApiConstants.DOORBOT_DESCRIPTION).getAsString();
    }
}
