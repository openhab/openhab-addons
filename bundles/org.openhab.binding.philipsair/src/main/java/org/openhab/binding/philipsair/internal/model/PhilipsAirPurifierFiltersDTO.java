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

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Cintains filter estimated lifetime
 * 
 * @author Michał Boroński - Initial contribution
 *
 */
@NonNullByDefault
public class PhilipsAirPurifierFiltersDTO {

    @SerializedName("fltsts0")
    @Expose
    int preFilter;

    @SerializedName("fltsts1")
    @Expose
    int carbonFilter;

    @SerializedName("fltsts2")
    @Expose
    int hepaFilter;

    @SerializedName("wicksts")
    @Expose
    int wickFilter;

    public int getPreFilter() {
        return preFilter;
    }

    public void setPreFilter(int preFilter) {
        this.preFilter = preFilter;
    }

    public int getCarbonFilter() {
        return carbonFilter;
    }

    public void setCarbonFilter(int carbonFilter) {
        this.carbonFilter = carbonFilter;
    }

    public int getHepaFilter() {
        return hepaFilter;
    }

    public void setHepaFilter(int hepaFilter) {
        this.hepaFilter = hepaFilter;
    }

    public int getWickFilter() {
        return wickFilter;
    }

    public void setWickFilter(int wickFilter) {
        this.wickFilter = wickFilter;
    }
}
