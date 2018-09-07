/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gpstracker.internal.config;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.eclipse.smarthome.core.library.types.PointType;
import org.openhab.binding.gpstracker.internal.message.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Binding configuration POJO
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class GPSTrackerBindingConfiguration {
    /**
     * Class logger
     */
    private final Logger logger = LoggerFactory.getLogger(GPSTrackerBindingConfiguration.class);

    /**
     * Gson instance used to serialize/deserialize JSON configuration
     */
    private final Gson gson = new Gson();

    /**
     * Primary region name
     */
    private String name;

    /**
     * Primary location (this is copied from System/Regional settings)
     */
    private PointType location;

    /**
     * Primary region radius
     */
    private Integer radius;

    /**
     * Additional region definitions in JSON
     */
    private String additionalRegionsJSON;

    /**
     * Configured regions
     */
    private Map<String, Region> regions = new HashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Initializes the configuration by parsing additional region definition JSON.
     */
    public void init() {
        if (this.additionalRegionsJSON == null) {
            this.additionalRegionsJSON = "[]";
        }
        this.regions.clear();
        try {
            //parse additional region definitions and map them
            Region[] regionList = gson.fromJson(this.additionalRegionsJSON, Region[].class);
            for (Region r: regionList) {
                this.regions.put(r.getName(), r);
            }
        } catch (JsonSyntaxException e) {
            logger.error("Error parsing extra locations: {}", e.getMessage());
        }
        //add primary region if defined
        if (location != null ) {
            this.regions.put(name, new Region(name, location.toFullString(), radius));
        }
    }

    public Collection<Region> getRegions() {
        return this.regions.values();
    }

    public Region getRegionByName(String name) {
        return this.regions.get(name);
    }

    public PointType getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return "GPSTrackerBindingConfiguration{" +
                "name='" + name + '\'' +
                ", location=" + location+
                ", radius=" + radius +
                ", additionalRegionsJSON='" + additionalRegionsJSON + '\'' +
                ", regions=" + regions +
                '}';
    }

    public void setLocation(PointType location) {
        this.location =location;
    }
}
