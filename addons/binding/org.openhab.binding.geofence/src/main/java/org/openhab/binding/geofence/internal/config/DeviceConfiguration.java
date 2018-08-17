/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.geofence.internal.config;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.openhab.binding.geofence.internal.message.Region;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Device level configuration POJO
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class DeviceConfiguration {
    //configuration parameters
    public static final String PARAM_EXTERNAL_REGION = "externalRegionsJSON";
    public static final String PARAM_IP_ADDRESS = "ipAddress";

    /**
     * Gson instance used to serialize/deserialize JSON configuration
     */
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Device IP address. This is updated from remote address of the HTTP track record until it is declared static.
     * If a static IP address is set a switch will be turned on when the device is on the local network.
     */
    private String ipAddress;

    /**
     * Declares the IP address static
     */
    private Boolean ipAddressStatic;

    /**
     * Device ping interval.
     */
    private Integer pingInterval;

    /**
     * Device level region definitions
     */
    private String externalRegionsJSON;

    /**
     * Map of device level regions
     */
    private Map<String, Region> regions = new HashMap<>();

    public void parseRegions() {
        if (externalRegionsJSON != null) {
            //parse external regions to map
            TypeToken typeToken = new TypeToken<List<Region>>() {
            };
            List<Region> regionList = gson.fromJson(externalRegionsJSON, typeToken.getType());
            regions = regionList.stream().collect(Collectors.toMap(Region::getName, Function.identity()));
        }
    }

    public String toJSON() {
        return gson.toJson(this.regions.values());
    }

    public Collection<Region> getRegions() {
        return this.regions.values();
    }

    public Region getRegionByName(String name) {
        return this.regions.get(name);
    }

    public void addRegion(Region region) {
        this.regions.put(region.getName(), region);
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public Boolean getIpAddressStatic() {
        return ipAddressStatic != null && ipAddressStatic;
    }

    public Integer getPingInterval() {
        return pingInterval;
    }
}
