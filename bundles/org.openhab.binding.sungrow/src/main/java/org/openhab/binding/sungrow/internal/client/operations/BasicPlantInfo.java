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

import org.openhab.binding.sungrow.internal.client.dto.BaseRequest;

import com.google.gson.annotations.SerializedName;

/**
 * @author Christian Kemper - Initial contribution
 */
public class BasicPlantInfo extends BaseApiOperation<BasicPlantInfo.Request, BasicPlantInfo.Response> {

    private final Request request;

    BasicPlantInfo(String deviceSerialNumber) {
        super("/openapi/getPowerStationDetail");
        request = new Request();
        request.serialNumber = deviceSerialNumber;
    }

    @Override
    public Request getRequest() {
        return request;
    }

    public static class Request extends BaseRequest {
        @SerializedName("sn")
        private String serialNumber;

        public String getSerialNumber() {
            return serialNumber;
        }

        public void setSerialNumber(String serialNumber) {
            this.serialNumber = serialNumber;
        }
    }

    public static class Response {
        @SerializedName("design_capacity")
        private Integer installedPower;

        @SerializedName("ps_status")
        private Status status;

        public Integer getInstalledPower() {
            return installedPower;
        }

        public Status getStatus() {
            return status;
        }
    }

    public enum Status {
        @SerializedName("1")
        Online,
        @SerializedName("0")
        Offline
    }
}
