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
package org.openhab.binding.surepetcare.internal.data;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

/**
 * The {@link SurePetcareDevice} is the Java class used
 * as a DTO to represent a Sure Petcare device, such as a hub, a cat flap, a feeder etc.
 *
 * @author Rene Scherer - Initial contribution
 */
public class SurePetcareDevice extends SurePetcareBaseObject {

    public enum ProductType {

        UNKNOWN(0, "Unknown"),
        HUB(1, "Hub"),
        PET_FLAP(3, "Pet Flap"),
        PET_FEEDER(4, "Pet Feeder"),
        CAT_FLAP(6, "Cat Flap");

        private final Integer id;
        private final String name;

        private ProductType(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public Integer getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public static @NonNull ProductType findByTypeId(final int id) {
            return Arrays.stream(values()).filter(value -> value.id.equals(id)).findFirst().orElse(UNKNOWN);
        }
    }

    private Integer parentDeviceId;
    private Integer productId;
    private Integer householdId;
    private String name;
    private String serialNumber;
    private String macAddress;
    private Integer index;
    private Date pairingAt;
    private SurePetcareDeviceControl control = new SurePetcareDeviceControl();
    private SurePetcareDevice parent;
    private SurePetcareDeviceStatus status = new SurePetcareDeviceStatus();

    public Integer getProductId() {
        return productId;
    }

    public Integer getParentDeviceId() {
        return parentDeviceId;
    }

    public void setParentDeviceId(Integer parentDeviceId) {
        this.parentDeviceId = parentDeviceId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getHouseholdId() {
        return householdId;
    }

    public void setHouseholdId(Integer householdId) {
        this.householdId = householdId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Date getPairingAt() {
        return pairingAt;
    }

    public void setPairingAt(Date pairingAt) {
        this.pairingAt = pairingAt;
    }

    public SurePetcareDeviceControl getControl() {
        return control;
    }

    public void setControl(SurePetcareDeviceControl control) {
        this.control = control;
    }

    public SurePetcareDevice getParent() {
        return parent;
    }

    public void setParent(SurePetcareDevice parent) {
        this.parent = parent;
    }

    public SurePetcareDeviceStatus getStatus() {
        return status;
    }

    public void setStatus(SurePetcareDeviceStatus status) {
        this.status = status;
    }

    @Override
    public Map<String, Object> getThingProperties() {
        Map<String, Object> properties = super.getThingProperties();
        properties.put("householdId", householdId.toString());
        properties.put("productTypeId", productId.toString());
        properties.put("productName", ProductType.findByTypeId(productId).getName());
        return properties;
    }

    @Override
    public String toString() {
        return "Device [id=" + id + ", name=" + name + ", product=" + ProductType.findByTypeId(productId).getName()
                + "]";
    }

    public @NonNull ZonedDateTime getPairingAtAsZonedDateTime() {
        return pairingAt.toInstant().atZone(ZoneId.systemDefault());
    }

    public SurePetcareDevice assign(SurePetcareDevice newdev) {
        super.assign(newdev);
        this.parentDeviceId = newdev.parentDeviceId;
        this.productId = newdev.productId;
        this.householdId = newdev.productId;
        this.name = newdev.name;
        this.serialNumber = newdev.serialNumber;
        this.macAddress = newdev.macAddress;
        this.index = newdev.index;
        this.pairingAt = newdev.pairingAt;
        this.control = newdev.control;
        this.parent = newdev.parent;
        this.status = newdev.status;
        return this;
    }

}
