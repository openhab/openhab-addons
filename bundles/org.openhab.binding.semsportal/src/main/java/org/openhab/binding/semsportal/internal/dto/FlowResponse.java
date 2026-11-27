/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.semsportal.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * POJO containing the response to the request for the power flow of a power station: its current output and the time
 * of the information.
 *
 * @author Julio Vilmar Gesser - Initial contribution
 */
public class FlowResponse extends BaseResponse {
    private Flow data;

    /**
     * @return the current output of the station in kW
     */
    public Double getCurrentOutput() {
        return data == null ? null : data.currentOutput;
    }

    /**
     * @return the time of the information, in ISO format and the time zone of the station
     */
    public String getRefreshTime() {
        return data == null ? null : data.refreshTime;
    }

    private static class Flow {
        @SerializedName("pAc")
        private Double currentOutput;
        private String refreshTime;
    }
}
