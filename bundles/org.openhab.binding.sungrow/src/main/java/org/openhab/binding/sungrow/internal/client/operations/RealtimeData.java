/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.sungrow.internal.client.operations;

import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.sungrow.internal.client.dto.BaseRequest;

import com.google.gson.annotations.SerializedName;

/**
 * @author Christian Kemper - Initial contribution
 */
public class RealtimeData extends BaseApiOperation<RealtimeData.Request, RealtimeData.Response> {

    public RealtimeData() {
        super("/openapi/getPVInverterRealTimeData");
        setRequest(new Request());
    }

    public static class Request extends BaseRequest {

        @SerializedName("ps_key_list")
        private List<String> plantKeys;

        @SerializedName("sn_list")
        private List<String> serials;

        public Request() {
            serials = new ArrayList<>();
            plantKeys = new ArrayList<>();
        }

        public List<String> getSerials() {
            return serials;
        }

        public void setSerials(List<String> serials) {
            this.serials = serials;
        }

        public List<String> getPlantKeys() {
            return plantKeys;
        }

        public void setPlantKeys(List<String> plantKeys) {
            this.plantKeys = plantKeys;
        }
    }

    public static class Response {
        @SerializedName("device_point_list")
        private List<DevicePointWrapper> devicePoints;

        @SerializedName("fail_sn_list")
        private List<String> failedSerials;

        @SerializedName("fail_ps_key_list")
        private List<String> failedPlantKeys;

        public List<DevicePointWrapper> getDevicePoints() {
            return devicePoints;
        }

        public List<String> getFailedSerials() {
            return failedSerials;
        }

        public List<String> getFailedPlantKeys() {
            return failedPlantKeys;
        }
    }

    public static class DevicePointWrapper {
        @SerializedName("device_point")
        private DevicePoint devicePoint;

        public DevicePoint getDevicePoint() {
            return devicePoint;
        }
    }

    public static class DevicePoint {

        @SerializedName("dev_fault_status")
        private FaultStatus faultStatus;

        @SerializedName("dev_status")
        private Status status;

        @SerializedName("device_name")
        private String name;

        @SerializedName("device_sn")
        private String serial;

        @SerializedName("device_time")
        private Long deviceUpdateTime;

        @SerializedName("uuid")
        private String uuid;

        // See more fields at https://developer-api.isolarcloud.com/#/document/api?id=10955&project_id=2&version=V1
        // Perhaps better provide data as map?
    }

    public enum FaultStatus {
        @SerializedName("1")
        Faulty,
        @SerializedName("2")
        Alarm,
        @SerializedName("4")
        Normal
    }

    public enum Status {
        @SerializedName("0")
        Undeployed,
        @SerializedName("1")
        Deployed

    }
}
