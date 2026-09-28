/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Cintains filter estimated lifetime
 *
 * @author Michał Boroński - Initial contribution
 * @author Marcel Verpaalen - Add null handling and code cleanup
 */
@NonNullByDefault
public class PhilipsAirPurifierFiltersDTO {

    @SerializedName("fltsts0")
    @Expose
    @Nullable
    Integer preFilter;

    @SerializedName("fltsts1")
    @Expose
    @Nullable
    Integer hepaFilter;

    @SerializedName("fltsts2")
    @Expose
    @Nullable
    Integer carbonFilter;

    @SerializedName("wicksts")
    @Expose
    @Nullable
    Integer wickFilter;

    public @Nullable Integer getPreFilter() {
        return preFilter;
    }

    public void setPreFilter(@Nullable Integer preFilter) {
        this.preFilter = preFilter;
    }

    public @Nullable Integer getCarbonFilter() {
        return carbonFilter;
    }

    public void setCarbonFilter(@Nullable Integer carbonFilter) {
        this.carbonFilter = carbonFilter;
    }

    public @Nullable Integer getHepaFilter() {
        return hepaFilter;
    }

    public void setHepaFilter(@Nullable Integer hepaFilter) {
        this.hepaFilter = hepaFilter;
    }

    public @Nullable Integer getWickFilter() {
        return wickFilter;
    }

    public void setWickFilter(@Nullable Integer wickFilter) {
        this.wickFilter = wickFilter;
    }
}
