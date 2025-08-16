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

import java.util.List;

import org.openhab.binding.sungrow.internal.client.dto.BaseRequest;

import com.google.gson.annotations.SerializedName;

/**
 * @author Christian Kemper - Initial contribution
 */
public class DeviceList extends BaseApiOperation<DeviceList.Request, DeviceList.Response> {

    private final Request request;

    DeviceList(String plantId) {
        super("/openapi/getDeviceList");
        request = new Request(plantId);
    }

    @Override
    public Request getRequest() {
        return request;
    }

    public static class Request extends BaseRequest {
        @SerializedName("ps_id")
        private String plantId;

        @SerializedName("curPage")
        private int currentPage;

        @SerializedName("size")
        private int pageSize;

        public Request(String plantId) {
            this.plantId = plantId;
            this.currentPage = 1;
            this.pageSize = 10;
        }

        public String getPlantId() {
            return plantId;
        }

        public void setPlantId(String plantId) {
            this.plantId = plantId;
        }

        public int getCurrentPage() {
            return currentPage;
        }

        public void setCurrentPage(int currentPage) {
            this.currentPage = currentPage;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }
    }

    public static class Response {
        private int rowCount;
        @SerializedName("pageList")
        private List<Device> devices;

        public int getRowCount() {
            return rowCount;
        }

        public void setRowCount(int rowCount) {
            this.rowCount = rowCount;
        }

        public List<Device> getDevices() {
            return devices;
        }

        public void setDevices(List<Device> devices) {
            this.devices = devices;
        }
    }

    public static class Device {
        @SerializedName("chnnl_id")
        private Integer channelId;

        @SerializedName("device_sn")
        private String serial;

        @SerializedName("ps_key")
        private String plantDeviceId;

        @SerializedName("device_type")
        private DeviceType deviceType;

        @SerializedName("device_name")
        private String deviceName;

        @SerializedName("type_name")
        private String deviceTypeName;

        @SerializedName("device_model_code")
        private String modelCode;

        public Integer getChannelId() {
            return channelId;
        }

        public String getSerial() {
            return serial;
        }

        public String getPlantDeviceId() {
            return plantDeviceId;
        }

        public DeviceType getDeviceType() {
            return deviceType;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public String getDeviceTypeName() {
            return deviceTypeName;
        }

        public String getModelCode() {
            return modelCode;
        }
    }

    public enum DeviceType {
        @SerializedName("1")
        Inverter,
        @SerializedName("2")
        Container,
        @SerializedName("3")
        Grid_Connection_Point,
        @SerializedName("4")
        Combiner_Box,
        @SerializedName("5")
        Meteo_Station,
        @SerializedName("6")
        Transformer,
        @SerializedName("7")
        Meter,
        @SerializedName("8")
        UPS,
        @SerializedName("9")
        Data_Logger,
        @SerializedName("10")
        PV_String,
        @SerializedName("11")
        Plant,
        @SerializedName("12")
        Circuit_Protection,
        @SerializedName("13")
        Splitting_Device,
        @SerializedName("14")
        Energy_Storage_System,
        @SerializedName("15")
        Sampling_Device,
        @SerializedName("16")
        EMU,
        @SerializedName("17")
        Unit,
        @SerializedName("18")
        Temperature_Humidity_Sensor,
        @SerializedName("19")
        Intelligent_Power_Distribution_Cabinet,
        @SerializedName("20")
        Display_Device,
        @SerializedName("21")
        AC_Power_Distributed_Cabinet,
        @SerializedName("22")
        Communication_Module,
        @SerializedName("23")
        System_BMS,
        @SerializedName("24")
        Array_BMS,
        @SerializedName("25")
        DC_DC,
        @SerializedName("26")
        Energy_Management_System,
        @SerializedName("27")
        Tracking_System,
        @SerializedName("28")
        Wind_Energy_Converter,
        @SerializedName("29")
        SVG,
        @SerializedName("30")
        PT_Cabinet,
        @SerializedName("31")
        Bus_Protection,
        @SerializedName("32")
        Cleaning_Device,
        @SerializedName("33")
        Direct_Current_Cabinet,
        @SerializedName("34")
        Public_Measurement_and_Control,
        @SerializedName("37")
        Energy_Storage_System_2,
        @SerializedName("43")
        Battery,
        @SerializedName("44")
        Battery_Cluster_Management_Unit,
        @SerializedName("45")
        Local_Controller,
        @SerializedName("52")
        Battery_System_Controller
    }
}
