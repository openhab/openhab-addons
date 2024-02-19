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

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ZoneResult} class represents Cloudrain zone API results
 *
 * @author Till Koellmann - Initial contribution
 */
@NonNullByDefault
public class ZoneResult {

    @SerializedName("userZones")
    public Zone[] zones;

    public ZoneResult(Zone[] zones) {
        this.zones = zones;
    }

    public Zone[] getZones() {
        return zones;
    }

    public void setZones(Zone[] zones) {
        this.zones = zones;
    }

    public List<Zone> getZoneList() {
        return Arrays.asList(zones);
    }
}
