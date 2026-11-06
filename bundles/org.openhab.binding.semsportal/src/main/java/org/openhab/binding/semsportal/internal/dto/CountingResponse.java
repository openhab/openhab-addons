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

/**
 * POJO containing the response to the request for the counters of an inverter: the energy it generated today, this
 * month, and in total. The portal groups the values by period, but each has a code of its own.
 *
 * @author Julio Vilmar Gesser - Initial contribution
 */
public class CountingResponse extends BaseResponse {
    public static final String TODAY = "proPvStatsToday";
    public static final String MONTH = "proPvStatsMonth";
    public static final String TOTAL = "proPvStatsTotal";

    private List<Group> data;

    /**
     * @param code the code of the value
     * @return the value, null if it is not in the response or not a number
     */
    public Double getValue(String code) {
        if (data != null) {
            for (Group group : data) {
                if (group.factors == null) {
                    continue;
                }
                for (Factor factor : group.factors) {
                    if (code.equals(factor.code) && factor.data != null) {
                        try {
                            return Double.valueOf(factor.data);
                        } catch (NumberFormatException e) {
                            return null;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static class Group {
        private List<Factor> factors;
    }

    private static class Factor {
        private String code;
        private String data;
    }
}
