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
package org.openhab.binding.pixometer.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link MeterInstance} class is the representing java model for the json result for a meter from the pixometer
 * api
 *
 * @author Jerome Luckenbach - Initial Contribution
 *
 */
@NonNullByDefault
public class MeterInstance {

    private @NonNullByDefault({}) String url;
    private @NonNullByDefault({}) String owner;
    private @NonNullByDefault({}) String changedHash;
    private @NonNullByDefault({}) String created;
    private @NonNullByDefault({}) String modified;
    private @NonNullByDefault({}) String appearance;
    private @NonNullByDefault({}) Integer fractionDigits;
    private @NonNullByDefault({}) Boolean isDoubleTariff;
    private @NonNullByDefault({}) String locationInBuilding;
    private @NonNullByDefault({}) String meterId;
    private @NonNullByDefault({}) String physicalMedium;
    private @NonNullByDefault({}) String physicalUnit;
    private @NonNullByDefault({}) Integer integerDigits;
    private @NonNullByDefault({}) String registerOrder;
    private @NonNullByDefault({}) Object city;
    private @NonNullByDefault({}) Object zipCode;
    private @NonNullByDefault({}) Object address;
    private @NonNullByDefault({}) Object description;
    private @NonNullByDefault({}) Object label;
    private @NonNullByDefault({}) Integer resourceId;

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
