/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.melcloud.internal.api.json;

import java.util.List;

import com.google.gson.annotations.Expose;

/**
 * {@link Area} provides area specific information for JSON data returned from MELCloud API
 * Area Data
 * Generated with jsonschema2pojo
 *
 * @author Wietse van Buitenen - Initial contribution
 */
public class Area {

    @Expose
    private Integer iD;

    @Expose
    private String name;

    @Expose
    private Integer buildingId;

    @Expose
    private Integer floorId;

    @Expose
    private Integer accessLevel;

    @Expose
    private Boolean directAccess;

    @Expose
    private Object endDate;

    @Expose
    private Integer minTemperature;

    @Expose
    private Integer maxTemperature;

    @Expose
    private Boolean expanded;

    @Expose
    private List<Device> devices = null;

    public Integer getID() {
        return iD;
    }

    public void setID(Integer iD) {
        this.iD = iD;
    }

    public Integer getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(Integer buildingId) {
        this.buildingId = buildingId;
    }

    public Integer getFloorId() {
        return floorId;
    }

    public void setFloorId(Integer floorId) {
        this.floorId = floorId;
    }

    public Integer getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(Integer accessLevel) {
        this.accessLevel = accessLevel;
    }

    public Boolean getDirectAccess() {
        return directAccess;
    }

    public void setDirectAccess(Boolean directAccess) {
        this.directAccess = directAccess;
    }

    public Object getEndDate() {
        return endDate;
    }

    public void setEndDate(Object endDate) {
        this.endDate = endDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Device> getDevices() {
        return devices;
    }

    public void setDevices(List<Device> devices) {
        this.devices = devices;
    }

    public Integer getMinTemperature() {
        return minTemperature;
    }

    public void setMinTemperature(Integer minTemperature) {
        this.minTemperature = minTemperature;
    }

    public Integer getMaxTemperature() {
        return maxTemperature;
    }

    public void setMaxTemperature(Integer maxTemperature) {
        this.maxTemperature = maxTemperature;
    }

    public Boolean getExpanded() {
        return expanded;
    }

    public void setExpanded(Boolean expanded) {
        this.expanded = expanded;
    }
}
