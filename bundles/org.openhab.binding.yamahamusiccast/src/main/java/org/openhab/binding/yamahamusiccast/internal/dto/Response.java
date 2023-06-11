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
package org.openhab.binding.yamahamusiccast.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents the response received from the Yamaha model/device via the API.
 *
 * @author Lennert Coopman - Initial contribution
 */

public class Response {

    @SerializedName("response_code")
    private String responseCode;

    public String getResponseCode() {
        if (responseCode == null) {
            responseCode = "";
        }
        return responseCode;
    }
}
