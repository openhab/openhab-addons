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
package org.openhab.binding.cloudrain.internal.api.model;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link IrrigationResult} class represents Cloudrain irrigation API results
 *
 * @author Till Koellmann - Initial contribution
 */
@NonNullByDefault
public class IrrigationResult {

    @SerializedName(value = "currentlyRunningZones", alternate = { "currentlyRunningIrrigationsInZone" })
    private Irrigation[] irrigations;

    public IrrigationResult(Irrigation[] irrigations) {
        this.irrigations = irrigations;
    }

    public void setIrrigations(Irrigation[] irrigations) {
        this.irrigations = irrigations;
    }

    public List<Irrigation> getIrrigationList() {
        return Arrays.asList(irrigations);
    }

    /**
     * Returns the first entry of the result list. This is useful in case we only expect one result.
     *
     * @return the first entry of the result list if available.
     */
    public @Nullable Irrigation getFirstEntry() {
        if (irrigations.length > 0) {
            return irrigations[0];
        }
        return null;
    }
}
