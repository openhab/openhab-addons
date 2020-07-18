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
package org.openhab.binding.solaredge.internal.model;

/**
 * this class is used to map the live data json response
 *
 * @author Alexander Friese - initial contribution
 */
public class LiveDataResponseMeterless {
    public static class Power {
        public Double power;
    }

    public static class Energy {
        public Double energy;
    }

    public static class Overview {
        public Power currentPower;
        public Energy lastDayData;
        public Energy lastMonthData;
        public Energy lastYearData;
    }

    private Overview overview;

    public final Overview getOverview() {
        return overview;
    }

    public final void setOverview(Overview overview) {
        this.overview = overview;
    }
}
