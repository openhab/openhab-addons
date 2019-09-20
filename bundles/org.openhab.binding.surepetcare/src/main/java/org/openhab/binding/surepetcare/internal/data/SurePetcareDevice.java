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

        UNKNOWN(-1, "Unknown"),
        HUB(1, "Hub"),
        PET_FLAP(3, "Pet Flap"),
        CAT_FLAP(6, "Cat Flap");

        private final Integer typeId;
        private final String name;

        private ProductType(int typeId, String name) {
            this.typeId = typeId;
            this.name = name;
        }

        public Integer getTypeId() {
            return typeId;
        }

        public String getName() {
            return name;
        }

        public static @NonNull ProductType findByTypeId(final int typeId) {
            return Arrays.stream(values()).filter(value -> value.typeId.equals(typeId)).findFirst().orElse(UNKNOWN);
        }
    }

    public class Control {
        public class Curfew {
            public Boolean enabled;
            public String unlock_time;
            public String lock_time;
        }

        public Integer locking;
        public Boolean fast_polling;
        public Integer led_mode;
        public Integer pairing_mode;
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
            public Float device_rssi;
            public Float hub_rssi;
        }

        public Integer led_mode;
        public Integer pairing_mode;
        public Locking locking;
        public Version version;
        public Float battery;
        // learn_mode - unknown type
        public Boolean online;
        public Signal signal = new Signal();
    }

    private Integer parent_device_id;
    private Integer product_id;
    private Integer household_id;
    private String name;
    private String serial_number;
    private String mac_address;
    private Integer index;
    private Date pairing_at;
    private Control control = new Control();
    private SurePetcareDevice parent;
    private Status status = new Status();

    public Integer getProduct_id() {
        return product_id;
    }

    public Integer getParent_device_id() {
        return parent_device_id;
    }

    public void setParent_device_id(Integer parent_device_id) {
        this.parent_device_id = parent_device_id;
    }

    public void setProduct_id(Integer product_id) {
        this.product_id = product_id;
    }

    public Integer getHousehold_id() {
        return household_id;
    }

    public void setHousehold_id(Integer household_id) {
        this.household_id = household_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSerial_number() {
        return serial_number;
    }

    public void setSerial_number(String serial_number) {
        this.serial_number = serial_number;
    }

    public String getMac_address() {
        return mac_address;
    }

    public void setMac_address(String mac_address) {
        this.mac_address = mac_address;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Date getPairing_at() {
        return pairing_at;
    }

    public void setPairing_at(Date pairing_at) {
        this.pairing_at = pairing_at;
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
        properties.put("householdId", household_id.toString());
        properties.put("productTypeId", product_id.toString());
        properties.put("productName", ProductType.findByTypeId(product_id).getName());
        return properties;
    }

    @Override
    public String toString() {
        return "Device [id=" + id + ", name=" + name + ",  product =" + ProductType.findByTypeId(product_id).getName()
                + "]";
    }

}
