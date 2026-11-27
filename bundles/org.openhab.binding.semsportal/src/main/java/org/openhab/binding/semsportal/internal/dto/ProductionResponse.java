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
 * POJO containing the response to the {@link ProductionRequest}
 *
 * @author Julio Vilmar Gesser - Initial contribution
 */
public class ProductionResponse extends BaseResponse {
    private Production data;

    /**
     * @return the income in the period, in the currency that is set in the portal
     */
    public Double getIncome() {
        return data == null ? null : data.income;
    }

    private static class Production {
        @SerializedName("profitProStats")
        private Double income;
    }
}
