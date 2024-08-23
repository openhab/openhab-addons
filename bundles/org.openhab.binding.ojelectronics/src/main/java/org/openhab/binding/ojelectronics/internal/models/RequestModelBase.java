/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.ojelectronics.internal.models;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Base model for all requests
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
public abstract class RequestModelBase {

    @SerializedName("APIKEY")
    public String apiKey = "";

    /**
     * Add API-Key
     *
     * @param apiKey API-Key
     * @return Model
     */
    public RequestModelBase withApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }
}
