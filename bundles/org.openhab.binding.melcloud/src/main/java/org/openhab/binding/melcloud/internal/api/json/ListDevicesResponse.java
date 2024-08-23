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

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link ListDevicesResponse} is responsible of JSON data For MELCloud API
 * Response of Devices List.
 * Generated with jsonschema2pojo
 *
 * @author Luca Calcaterra - Initial contribution
 */
public class ListDevicesResponse {

    @Expose
    private Integer iD;

    @Expose
    private String name;

    @Expose
    private String addressLine1;

    @Expose
    private Object addressLine2;

    @Expose
    private String city;

    @Expose
    private String postcode;

    @Expose
    private Double latitude;

    @Expose
    private Double longitude;

    @Expose
    private Object district;

    @Expose
    private Boolean fPDefined;

    @Expose
    private Boolean fPEnabled;

    @Expose
    private Integer fPMinTemperature;

    @Expose
    private Integer fPMaxTemperature;

    @Expose
    private Boolean hMDefined;

    @Expose
    private Boolean hMEnabled;

    @Expose
    private Object hMStartDate;

    @Expose
    private Object hMEndDate;

    @Expose
    private Integer buildingType;

    @Expose
    private Integer propertyType;

    @Expose
    private String dateBuilt;

    @Expose
    private Boolean hasGasSupply;

    @Expose
    private String locationLookupDate;

    @Expose
    private Integer country;

    @Expose
    private Integer timeZoneContinent;

    @Expose
    private Integer timeZoneCity;

    @Expose
    private Integer timeZone;

    @Expose
    private Integer location;

    @Expose
    private Boolean coolingDisabled;

    @Expose
    private Boolean expanded;

    @Expose
    private Structure structure;

    @Expose
    private Integer accessLevel;

    @Expose
    private Boolean directAccess;

    @Expose
    private Integer minTemperature;

    @Expose
    private Integer maxTemperature;

    @Expose
    private Object owner;

    @Expose
    private String endDate;

    @SerializedName("iDateBuilt")
    @Expose
    private Object iDateBuilt;

    @Expose
    private QuantizedCoordinates quantizedCoordinates;

    public Integer getID() {
        return iD;
    }

    public void setID(Integer iD) {
        this.iD = iD;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public Object getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(Object addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Object getDistrict() {
        return district;
    }

    public void setDistrict(Object district) {
        this.district = district;
    }

    public Boolean getFPDefined() {
        return fPDefined;
    }

    public void setFPDefined(Boolean fPDefined) {
        this.fPDefined = fPDefined;
    }

    public Boolean getFPEnabled() {
        return fPEnabled;
    }

    public void setFPEnabled(Boolean fPEnabled) {
        this.fPEnabled = fPEnabled;
    }

    public Integer getFPMinTemperature() {
        return fPMinTemperature;
    }

    public void setFPMinTemperature(Integer fPMinTemperature) {
        this.fPMinTemperature = fPMinTemperature;
    }

    public Integer getFPMaxTemperature() {
        return fPMaxTemperature;
    }

    public void setFPMaxTemperature(Integer fPMaxTemperature) {
        this.fPMaxTemperature = fPMaxTemperature;
    }

    public Boolean getHMDefined() {
        return hMDefined;
    }

    public void setHMDefined(Boolean hMDefined) {
        this.hMDefined = hMDefined;
    }

    public Boolean getHMEnabled() {
        return hMEnabled;
    }

    public void setHMEnabled(Boolean hMEnabled) {
        this.hMEnabled = hMEnabled;
    }

    public Object getHMStartDate() {
        return hMStartDate;
    }

    public void setHMStartDate(Object hMStartDate) {
        this.hMStartDate = hMStartDate;
    }

    public Object getHMEndDate() {
        return hMEndDate;
    }

    public void setHMEndDate(Object hMEndDate) {
        this.hMEndDate = hMEndDate;
    }

    public Integer getBuildingType() {
        return buildingType;
    }

    public void setBuildingType(Integer buildingType) {
        this.buildingType = buildingType;
    }

    public Integer getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(Integer propertyType) {
        this.propertyType = propertyType;
    }

    public String getDateBuilt() {
        return dateBuilt;
    }

    public void setDateBuilt(String dateBuilt) {
        this.dateBuilt = dateBuilt;
    }

    public Boolean getHasGasSupply() {
        return hasGasSupply;
    }

    public void setHasGasSupply(Boolean hasGasSupply) {
        this.hasGasSupply = hasGasSupply;
    }

    public String getLocationLookupDate() {
        return locationLookupDate;
    }

    public void setLocationLookupDate(String locationLookupDate) {
        this.locationLookupDate = locationLookupDate;
    }

    public Integer getCountry() {
        return country;
    }

    public void setCountry(Integer country) {
        this.country = country;
    }

    public Integer getTimeZoneContinent() {
        return timeZoneContinent;
    }

    public void setTimeZoneContinent(Integer timeZoneContinent) {
        this.timeZoneContinent = timeZoneContinent;
    }

    public Integer getTimeZoneCity() {
        return timeZoneCity;
    }

    public void setTimeZoneCity(Integer timeZoneCity) {
        this.timeZoneCity = timeZoneCity;
    }

    public Integer getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(Integer timeZone) {
        this.timeZone = timeZone;
    }

    public Integer getLocation() {
        return location;
    }

    public void setLocation(Integer location) {
        this.location = location;
    }

    public Boolean getCoolingDisabled() {
        return coolingDisabled;
    }

    public void setCoolingDisabled(Boolean coolingDisabled) {
        this.coolingDisabled = coolingDisabled;
    }

    public Boolean getExpanded() {
        return expanded;
    }

    public void setExpanded(Boolean expanded) {
        this.expanded = expanded;
    }

    public Structure getStructure() {
        return structure;
    }

    public void setStructure(Structure structure) {
        this.structure = structure;
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

    public Object getOwner() {
        return owner;
    }

    public void setOwner(Object owner) {
        this.owner = owner;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Object getIDateBuilt() {
        return iDateBuilt;
    }

    public void setIDateBuilt(Object iDateBuilt) {
        this.iDateBuilt = iDateBuilt;
    }

    public QuantizedCoordinates getQuantizedCoordinates() {
        return quantizedCoordinates;
    }

    public void setQuantizedCoordinates(QuantizedCoordinates quantizedCoordinates) {
        this.quantizedCoordinates = quantizedCoordinates;
    }
}
