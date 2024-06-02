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
package org.openhab.binding.tado.swagger.codegen.api.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.gson.annotations.SerializedName;

/**
 * Static imported copy of the Java file originally created by Swagger Codegen.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
public class Zone {
    @SerializedName("id")
    private Integer id = null;

    @SerializedName("name")
    private String name = null;

    @SerializedName("type")
    private TadoSystemType type = null;

    @SerializedName("devices")
    private List<ControlDevice> devices = null;

    public Integer getId() {
        return id;
    }

    public Zone name(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Zone type(TadoSystemType type) {
        this.type = type;
        return this;
    }

    public TadoSystemType getType() {
        return type;
    }

    public void setType(TadoSystemType type) {
        this.type = type;
    }

    public Zone devices(List<ControlDevice> devices) {
        this.devices = devices;
        return this;
    }

    public Zone addDevicesItem(ControlDevice devicesItem) {
        if (this.devices == null) {
            this.devices = new ArrayList<>();
        }
        this.devices.add(devicesItem);
        return this;
    }

    public List<ControlDevice> getDevices() {
        return devices;
    }

    public void setDevices(List<ControlDevice> devices) {
        this.devices = devices;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Zone zone = (Zone) o;
        return Objects.equals(this.id, zone.id) && Objects.equals(this.name, zone.name)
                && Objects.equals(this.type, zone.type) && Objects.equals(this.devices, zone.devices);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, type, devices);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Zone {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    devices: ").append(toIndentedString(devices)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
