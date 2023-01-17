/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * this class is used to map the live data json response
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class LiveDataResponseMeterless {
    public static class Power {
        public @Nullable Double power;
    }

    public static class Energy {
        public @Nullable Double energy;
    }

    public static class Overview {
        public @Nullable Power currentPower;
        public @Nullable Energy lastDayData;
        public @Nullable Energy lastMonthData;
        public @Nullable Energy lastYearData;
    }

    @Nullable
    private Overview overview;

    public final @Nullable Overview getOverview() {
        return overview;
    }

    public final void setOverview(Overview overview) {
        this.overview = overview;
    }
}
