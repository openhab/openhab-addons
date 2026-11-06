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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * POJO for the request of the income of a power station in a period. Properties are private but used by Gson to
 * construct the request.
 *
 * @author Julio Vilmar Gesser - Initial contribution
 */
@SuppressWarnings("unused") // the fields are used by Gson
@NonNullByDefault
public class ProductionRequest {
    private String stationId;
    // The portal fails on a request for the income only: it also wants the generation that the income is based on
    private List<String> items = List.of("proSystemTotalStats", "profitProStats");
    private String dimension;
    private boolean isReport = false;
    private String startTime;
    private String endTime;

    /**
     * @param stationId the UUID of the power station
     * @param dimension the type of period: day (a single day) or year (any period, summed)
     * @param startTime start of the period, formatted as yyyy-MM-dd HH:mm:ss
     * @param endTime end of the period, formatted as yyyy-MM-dd HH:mm:ss
     */
    public ProductionRequest(String stationId, String dimension, String startTime, String endTime) {
        this.stationId = stationId;
        this.dimension = dimension;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
