/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.withings.internal.api.device;

import java.util.Date;
import java.util.List;

import org.openhab.binding.withings.internal.api.BaseResponseDTO;

import com.google.gson.annotations.SerializedName;

/**
 * @author Sven Strohschein - Initial contribution
 */
public class DevicesResponseDTO extends BaseResponseDTO {

    private DevicesBody body;

    public DevicesBody getBody() {
        return body;
    }

    public class DevicesBody {

        private List<Device> devices;

        public List<Device> getDevices() {
            return devices;
        }
    }

    public static class Device {

        @SerializedName("deviceid")
        private String deviceId;
        private String type;
        private String battery;
        private String model;
        @SerializedName("model_id")
        private Integer modelId;
        @SerializedName("last_session_date")
        private long lastSessionDate;

        public Device() {
        }

        public Device(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public String getType() {
            return type;
        }

        public String getBattery() {
            return battery;
        }

        public String getModel() {
            return model;
        }

        public Integer getModelId() {
            return modelId;
        }

        public Date getLastSessionDate() {
            return new Date(lastSessionDate * 1000L);
        }
    }
}
