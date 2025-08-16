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
import org.openhab.binding.sungrow.internal.client.dto.UnitValuePair;

import com.google.gson.annotations.SerializedName;

/**
 * @author Christian Kemper - Initial contribution
 */
public class PlantList extends BaseApiOperation<PlantList.Request, PlantList.Response> {

    PlantList() {
        super("/openapi/getPowerStationList");
    }

    @Override
    public Request getRequest() {
        Request request = new Request();
        request.currentPage = 1;
        request.pageSize = 10;
        return request;
    }

    public static class Request extends BaseRequest {
        @SerializedName("curPage")
        private int currentPage;

        @SerializedName("size")
        private int pageSize;
    }

    public static class Response {
        private int rowCount;
        @SerializedName("pageList")
        private List<Plant> plants;

        public int getRowCount() {
            return rowCount;
        }

        public void setRowCount(int rowCount) {
            this.rowCount = rowCount;
        }

        public List<Plant> getPlants() {
            return plants;
        }

        public void setPlants(List<Plant> plants) {
            this.plants = plants;
        }
    }

    public static class Plant {
        @SerializedName("ps_name")
        private String plantName;
        @SerializedName("ps_type")
        private PlantType plantType;
        @SerializedName("ps_id")
        private String plantId;
        @SerializedName("connect_type")
        private ConnectType connectType;

        @SerializedName("total_energy")
        private UnitValuePair totalEnergy;
        @SerializedName("curr_power")
        private UnitValuePair currentPower;
        @SerializedName("co2_reduce_total")
        private UnitValuePair co2ReduceTotal;
        @SerializedName("today_income")
        private UnitValuePair todayIncome;
        @SerializedName("equivalent_hour")
        private UnitValuePair equivalentHour;
        @SerializedName("total_income")
        private UnitValuePair totalIncome;
        @SerializedName("total_capcity")
        private UnitValuePair totalCapcity;
        @SerializedName("today_energy")
        private UnitValuePair todayEnergy;
        @SerializedName("co2_reduce")
        private UnitValuePair co2Reduce;

        @SerializedName("alarm_count")
        private int alarmCount;

        @SerializedName("latitude")
        private Double latitude;
        @SerializedName("longitude")
        private Double longitude;

        public String getPlantName() {
            return plantName;
        }

        public void setPlantName(String plantName) {
            this.plantName = plantName;
        }

        public PlantType getPlantType() {
            return plantType;
        }

        public void setPlantType(PlantType plantType) {
            this.plantType = plantType;
        }

        public String getPlantId() {
            return plantId;
        }

        public void setPlantId(String plantId) {
            this.plantId = plantId;
        }

        public ConnectType getConnectType() {
            return connectType;
        }

        public void setConnectType(ConnectType connectType) {
            this.connectType = connectType;
        }

        public UnitValuePair getTotalEnergy() {
            return totalEnergy;
        }

        public void setTotalEnergy(UnitValuePair totalEnergy) {
            this.totalEnergy = totalEnergy;
        }

        public UnitValuePair getCurrentPower() {
            return currentPower;
        }

        public void setCurrentPower(UnitValuePair currentPower) {
            this.currentPower = currentPower;
        }

        public UnitValuePair getCo2ReduceTotal() {
            return co2ReduceTotal;
        }

        public void setCo2ReduceTotal(UnitValuePair co2ReduceTotal) {
            this.co2ReduceTotal = co2ReduceTotal;
        }

        public UnitValuePair getTodayIncome() {
            return todayIncome;
        }

        public void setTodayIncome(UnitValuePair todayIncome) {
            this.todayIncome = todayIncome;
        }

        public UnitValuePair getEquivalentHour() {
            return equivalentHour;
        }

        public void setEquivalentHour(UnitValuePair equivalentHour) {
            this.equivalentHour = equivalentHour;
        }

        public UnitValuePair getTotalIncome() {
            return totalIncome;
        }

        public void setTotalIncome(UnitValuePair totalIncome) {
            this.totalIncome = totalIncome;
        }

        public UnitValuePair getTotalCapcity() {
            return totalCapcity;
        }

        public void setTotalCapcity(UnitValuePair totalCapcity) {
            this.totalCapcity = totalCapcity;
        }

        public UnitValuePair getTodayEnergy() {
            return todayEnergy;
        }

        public void setTodayEnergy(UnitValuePair todayEnergy) {
            this.todayEnergy = todayEnergy;
        }

        public UnitValuePair getCo2Reduce() {
            return co2Reduce;
        }

        public void setCo2Reduce(UnitValuePair co2Reduce) {
            this.co2Reduce = co2Reduce;
        }

        public int getAlarmCount() {
            return alarmCount;
        }

        public void setAlarmCount(int alarmCount) {
            this.alarmCount = alarmCount;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }
    }

    public enum PlantType {

        @SerializedName("1")
        Utility_Plant,

        @SerializedName("3")
        Distributed_PV_Plant,

        @SerializedName("4")
        Residential_PV_Plant,

        @SerializedName("5")
        Residential_Energy_Storage_Plant,

        @SerializedName("6")
        Village_Plant,

        @SerializedName("7")
        Distributed_Energy_Storage_Plant,

        @SerializedName("8")
        Poverty_Alleviation_Plant,

        @SerializedName("9")
        Wind_Power_Plant,

        @SerializedName("10")
        Utility_Energy_Storage_Plant,

        @SerializedName("12")
        C_and_I_Energy_Storage_Plant
    }

    public enum ConnectType {
        @SerializedName("1")
        Full_Grid,
        @SerializedName("2")
        Self_Used_Feedin,
        @SerializedName("3")
        Self_Used_No_Feedin,
        @SerializedName("4")
        Off_Grid
    }
}
