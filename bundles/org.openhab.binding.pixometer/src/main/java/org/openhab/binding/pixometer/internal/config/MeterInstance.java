/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.pixometer.internal.config;

/**
 * The {@link MeterInstance} class is the representing java model for the json result for a meter from the pixometer
 * api
 *
 * @author Jerome Luckenbach - Initial Contribution
 *
 */
public class MeterInstance {

    private String url;
    private String owner;
    private String changedHash;
    private String created;
    private String modified;
    private String appearance;
    private Integer fractionDigits;
    private Boolean isDoubleTariff;
    private String locationInBuilding;
    private String meterId;
    private String physicalMedium;
    private String physicalUnit;
    private Integer integerDigits;
    private String registerOrder;
    private Object city;
    private Object zipCode;
    private Object address;
    private Object description;
    private Object label;
    private Integer resourceId;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getChangedHash() {
        return changedHash;
    }

    public void setChangedHash(String changedHash) {
        this.changedHash = changedHash;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    public String getAppearance() {
        return appearance;
    }

    public void setAppearance(String appearance) {
        this.appearance = appearance;
    }

    public Integer getFractionDigits() {
        return fractionDigits;
    }

    public void setFractionDigits(Integer fractionDigits) {
        this.fractionDigits = fractionDigits;
    }

    public Boolean getIsDoubleTariff() {
        return isDoubleTariff;
    }

    public void setIsDoubleTariff(Boolean isDoubleTariff) {
        this.isDoubleTariff = isDoubleTariff;
    }

    public String getLocationInBuilding() {
        return locationInBuilding;
    }

    public void setLocationInBuilding(String locationInBuilding) {
        this.locationInBuilding = locationInBuilding;
    }

    public String getMeterId() {
        return meterId;
    }

    public void setMeterId(String meterId) {
        this.meterId = meterId;
    }

    public String getPhysicalMedium() {
        return physicalMedium;
    }

    public void setPhysicalMedium(String physicalMedium) {
        this.physicalMedium = physicalMedium;
    }

    public String getPhysicalUnit() {
        return physicalUnit;
    }

    public void setPhysicalUnit(String physicalUnit) {
        this.physicalUnit = physicalUnit;
    }

    public Integer getIntegerDigits() {
        return integerDigits;
    }

    public void setIntegerDigits(Integer integerDigits) {
        this.integerDigits = integerDigits;
    }

    public String getRegisterOrder() {
        return registerOrder;
    }

    public void setRegisterOrder(String registerOrder) {
        this.registerOrder = registerOrder;
    }

    public Object getCity() {
        return city;
    }

    public void setCity(Object city) {
        this.city = city;
    }

    public Object getZipCode() {
        return zipCode;
    }

    public void setZipCode(Object zipCode) {
        this.zipCode = zipCode;
    }

    public Object getAddress() {
        return address;
    }

    public void setAddress(Object address) {
        this.address = address;
    }

    public Object getDescription() {
        return description;
    }

    public void setDescription(Object description) {
        this.description = description;
    }

    public Object getLabel() {
        return label;
    }

    public void setLabel(Object label) {
        this.label = label;
    }

    public Integer getResourceId() {
        return resourceId;
    }

    public void setResourceId(Integer resourceId) {
        this.resourceId = resourceId;
    }
}
