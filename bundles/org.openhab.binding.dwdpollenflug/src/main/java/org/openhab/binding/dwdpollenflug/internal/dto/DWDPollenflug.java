/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.dwdpollenflug.internal.dto;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 *
 * @author Johannes DerOetzi Ott - Initial contribution
 */
@NonNullByDefault
public class DWDPollenflug {

    private final Map<Integer, DWDRegion> regions = new HashMap<>();

    public DWDPollenflug(DWDPollenflugJSON json) {
        for (DWDRegionJSON regionJSON : json.regions) {
            DWDRegion region = new DWDRegion(regionJSON);
            regions.put(region.getRegionID(), region);
        }
    }

    public @Nullable DWDRegion getRegion(int regionID) {
        return regions.get(regionID);
    }
}