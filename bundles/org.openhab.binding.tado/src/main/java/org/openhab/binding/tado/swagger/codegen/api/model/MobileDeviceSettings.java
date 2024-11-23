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
public class MobileDeviceSettings {
    @SerializedName("geoTrackingEnabled")
    private Boolean geoTrackingEnabled = null;

    public MobileDeviceSettings geoTrackingEnabled(Boolean geoTrackingEnabled) {
        this.geoTrackingEnabled = geoTrackingEnabled;
        return this;
    }

    public Boolean isGeoTrackingEnabled() {
        return geoTrackingEnabled;
    }

    public void setGeoTrackingEnabled(Boolean geoTrackingEnabled) {
        this.geoTrackingEnabled = geoTrackingEnabled;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MobileDeviceSettings mobileDeviceSettings = (MobileDeviceSettings) o;
        return Objects.equals(this.geoTrackingEnabled, mobileDeviceSettings.geoTrackingEnabled);
    }

    @Override
    public int hashCode() {
        return Objects.hash(geoTrackingEnabled);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MobileDeviceSettings {\n");

        sb.append("    geoTrackingEnabled: ").append(toIndentedString(geoTrackingEnabled)).append("\n");
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
