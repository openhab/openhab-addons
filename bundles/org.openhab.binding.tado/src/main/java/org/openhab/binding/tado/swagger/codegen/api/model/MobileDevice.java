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

import java.util.Objects;

import com.google.gson.annotations.SerializedName;

/**
 * Static imported copy of the Java file originally created by Swagger Codegen.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
public class MobileDevice {
    @SerializedName("id")
    private Integer id = null;

    @SerializedName("name")
    private String name = null;

    @SerializedName("settings")
    private MobileDeviceSettings settings = null;

    @SerializedName("location")
    private MobileDeviceLocation location = null;

    public Integer getId() {
        return id;
    }

    public MobileDevice name(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MobileDevice settings(MobileDeviceSettings settings) {
        this.settings = settings;
        return this;
    }

    public MobileDeviceSettings getSettings() {
        return settings;
    }

    public void setSettings(MobileDeviceSettings settings) {
        this.settings = settings;
    }

    public MobileDevice location(MobileDeviceLocation location) {
        this.location = location;
        return this;
    }

    public MobileDeviceLocation getLocation() {
        return location;
    }

    public void setLocation(MobileDeviceLocation location) {
        this.location = location;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MobileDevice mobileDevice = (MobileDevice) o;
        return Objects.equals(this.id, mobileDevice.id) && Objects.equals(this.name, mobileDevice.name)
                && Objects.equals(this.settings, mobileDevice.settings)
                && Objects.equals(this.location, mobileDevice.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, settings, location);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MobileDevice {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    settings: ").append(toIndentedString(settings)).append("\n");
        sb.append("    location: ").append(toIndentedString(location)).append("\n");
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
