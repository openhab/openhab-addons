/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.lametrictime.internal.api.local.dto;

/**
 * Pojo for bluetooth.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class Bluetooth {
    private Boolean active;

    /*
     * API sometimes calls this field 'mac' and other times calls it 'address'.
     * Additionally, Gson uses fields only (not methods). Therefore, if use the
     * same instance of this class to read one value and then try to write the
     * other without calling the setter, it won't work (the other value will be
     * null).
     */
    private String mac;
    private String address;

    private Boolean available;
    private Boolean discoverable;
    private String name;
    private Boolean pairable;

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Bluetooth withActive(Boolean active) {
        this.active = active;
        return this;
    }

    public String getMac() {
        return mac == null ? address : mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
        this.address = mac;
    }

    public Bluetooth withMac(String mac) {
        setMac(mac);
        return this;
    }

    public Boolean isAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public Bluetooth withAvailable(Boolean available) {
        this.available = available;
        return this;
    }

    public Boolean isDiscoverable() {
        return discoverable;
    }

    public void setDiscoverable(Boolean discoverable) {
        this.discoverable = discoverable;
    }

    public Bluetooth withDiscoverable(Boolean discoverable) {
        this.discoverable = discoverable;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bluetooth withName(String name) {
        this.name = name;
        return this;
    }

    public Boolean isPairable() {
        return pairable;
    }

    public void setPairable(Boolean pairable) {
        this.pairable = pairable;
    }

    public Bluetooth withPairable(Boolean pairable) {
        this.pairable = pairable;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Bluetooth [active=");
        builder.append(active);
        builder.append(", mac=");
        builder.append(getMac());
        builder.append(", available=");
        builder.append(available);
        builder.append(", discoverable=");
        builder.append(discoverable);
        builder.append(", name=");
        builder.append(name);
        builder.append(", pairable=");
        builder.append(pairable);
        builder.append("]");
        return builder.toString();
    }
}
