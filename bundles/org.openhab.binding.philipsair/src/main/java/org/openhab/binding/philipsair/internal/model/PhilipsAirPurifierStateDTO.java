/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.philipsair.internal.model;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Holds status of particular features of the Air Purifier thing
 *
 * @author Michał Boroński - Initial contribution
 * @Nullable
 *
 */

public class PhilipsAirPurifierStateDTO {
    @SerializedName("reported")
    @Expose
    private JsonObject reported;
    @SerializedName("desired")
    @Expose
    private JsonObject desired;

    public JsonObject getReported() {
        return reported;
    }

    public void setReported(JsonObject reported) {
        this.reported = reported;
    }

    public JsonObject getDesired() {
        return desired;
    }

    public void setDesired(JsonObject desired) {
        this.desired = desired;
    }
}
