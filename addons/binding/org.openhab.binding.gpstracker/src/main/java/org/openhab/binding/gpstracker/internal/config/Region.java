/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gpstracker.internal.config;

import org.eclipse.smarthome.core.library.types.PointType;

/**
 * Region POJO
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class Region {
    /**
     * Region name
     */
    private String regionName;

    /**
     * Location coordinates
     */
    private String regionCenterLocation;

    /**
     * Region radius
     */
    private Integer regionRadius;

    public Region(String regionName, String regionCenterLocation, Integer regionRadius) {
        this.regionName = regionName;
        this.regionCenterLocation = regionCenterLocation;
        this.regionRadius = regionRadius;
    }

    public Region() {
    }

    public String getId() {
        return regionName != null ? regionName.replaceAll("[^a-zA-Z0-9_]", ""): "UnnamedRegion";
    }
    public String getRegionCenterLocation() {
        return regionCenterLocation;
    }

    public Integer getRegionRadius() {
        return regionRadius;
    }

    public PointType getLocation() {
        return regionCenterLocation != null ? new PointType(regionCenterLocation): null;
    }
}
