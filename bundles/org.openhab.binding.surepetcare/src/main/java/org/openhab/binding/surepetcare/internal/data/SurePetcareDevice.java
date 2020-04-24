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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

/**
 * The {@link SurePetcareDevice} is the Java class used
 * as a DTO to represent a Sure Petcare device, such as a hub, a cat flap, a feeder etc.
 *
 * @author Rene Scherer - Initial contribution
 */
public class SurePetcareDevice extends SurePetcareBaseObject {

    // {
    // "id":346854,
    // "product_id":1,
    // "household_id":34563,
    // "name":"Home Hub",
    // "serial_number":"H008-0316428",
    // "mac_address":"00000467320C0B35",
    // "version":"MjY=",
    // "created_at":"2019-04-18T14:45:11+00:00",
    // "updated_at":"2019-09-11T08:25:22+00:00",
    // "control":{
    // "led_mode":4,
    // "pairing_mode":0
    // },
    // "status":{
    // "led_mode":4,
    // "pairing_mode":0,
    // "version":{
    // "device":{
    // "hardware":3,
    // "firmware":1.772
    // }
    // },
    // "online":true
    // }
    // },

    public enum ProductType {

        UNKNOWN(0, "Unknown"),
        HUB(1, "Hub"),
        PET_FLAP(3, "Pet Flap"),
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

    public class Control {
        public class Curfew {
            public Boolean enabled;
            public String unlockTime;
            public String lockTime;
        }

        public Integer locking;
        public Boolean fastPolling;
        public Integer ledMode;
        public Integer pairingMode;
        public List<Curfew> curfew = new ArrayList<Curfew>();
    }

    public class Status {
        public class Locking {
            public Integer mode;
        }

        public class Version {
            public class Device {
                public String hardware;
                public String firmware;
            }

            public Device device = new Device();
        }

        public class Signal {
            public Float deviceRssi;
            public Float hubRssi;
        }

        public Integer ledMode;
        public Integer pairingMode;
        public Locking locking;
        public Version version;
        public Float battery;
        // learn_mode - unknown type
        public Boolean online;
        public Signal signal = new Signal();
    }

    private Integer parentDeviceId;
    private Integer productId;
    private Integer householdId;
    private String name;
    private String serialNumber;
    private String macAddress;
    private Integer index;
    private Date pairingAt;
    private Control control = new Control();
    private SurePetcareDevice parent;
    private Status status = new Status();

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

    public Control getControl() {
        return control;
    }

    public void setControl(Control control) {
        this.control = control;
    }

    public SurePetcareDevice getParent() {
        return parent;
    }

    public void setParent(SurePetcareDevice parent) {
        this.parent = parent;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public @NonNull Map<String, Object> getThingProperties() {
        Map<String, Object> properties = super.getThingProperties();
        properties.put("householdId", householdId.toString());
        properties.put("productTypeId", productId.toString());
        properties.put("productName", ProductType.findByTypeId(productId).getName());
        return properties;
    }

    @Override
    public String toString() {
        return "Device [id=" + id + ", name=" + name + ",  product =" + ProductType.findByTypeId(productId).getName()
                + "]";
    }

}
