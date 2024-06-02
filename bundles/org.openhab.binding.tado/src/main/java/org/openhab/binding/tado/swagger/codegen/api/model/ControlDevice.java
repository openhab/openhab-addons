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
public class ControlDevice {
    @SerializedName("deviceType")
    private String deviceType = null;

    @SerializedName("serialNo")
    private String serialNo = null;

    @SerializedName("batteryState")
    private String batteryState = null;

    public String getDeviceType() {
        return deviceType;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public String getBatteryState() {
        return batteryState;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ControlDevice controlDevice = (ControlDevice) o;
        return Objects.equals(this.deviceType, controlDevice.deviceType)
                && Objects.equals(this.serialNo, controlDevice.serialNo)
                && Objects.equals(this.batteryState, controlDevice.batteryState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceType, serialNo, batteryState);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ControlDevice {\n");

        sb.append("    deviceType: ").append(toIndentedString(deviceType)).append("\n");
        sb.append("    serialNo: ").append(toIndentedString(serialNo)).append("\n");
        sb.append("    batteryState: ").append(toIndentedString(batteryState)).append("\n");
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
