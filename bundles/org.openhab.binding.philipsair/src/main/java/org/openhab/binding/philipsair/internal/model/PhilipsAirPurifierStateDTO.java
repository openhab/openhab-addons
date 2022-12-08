/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

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
@NonNullByDefault
public class PhilipsAirPurifierStateDTO {
    @SerializedName("reported")
    @Expose
    @Nullable
    private JsonObject reported;
    @SerializedName("desired")
    @Expose
    @Nullable
    private JsonObject desired;

    public @Nullable JsonObject getReported() {
        return reported;
    }

    public void setReported(JsonObject reported) {
        this.reported = reported;
    }

    public @Nullable JsonObject getDesired() {
        return desired;
    }

    public void setDesired(JsonObject desired) {
        this.desired = desired;
    }
}
