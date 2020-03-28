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

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Class to hold Response of Region
 * 
 * @author Johannes DerOetzi Ott - Initial contribution
 */
public class DWDRegion {
    private int region_id;
    private String region_name;
    private int partregion_id;
    private @Nullable String partregion_name;

    private Map<String, DWDPollen> pollen;

    public int getId() {
        if (isPartRegion()) {
            return partregion_id;
        } else {
            return region_id;
        }
    }

    public boolean isPartRegion() {
        return partregion_id > 0;
    }

    public int getRegionId() {
        return region_id;
    }

    public String getRegionName() {
        return region_name;
    }

    public int getPartregionId() {
        return partregion_id;
    }

    public String getPartregionName() {
        return partregion_name;
    }

    public Map<String, DWDPollen> getPollen() {
        return pollen;
    }
}
