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
package org.openhab.binding.roborock.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Class for holding the set of parameters used by the get_consumables response
 *
 * @author Paul Smedley - Initial Contribution
 *
 */

@NonNullByDefault
public class GetDndTimer {
    public int id;

    public @NonNullByDefault({}) Result[] result;

    public class Result {
        @SerializedName("start_hour")
        public int startHour;

        @SerializedName("start_minute")
        public int startMinute;

        @SerializedName("end_hour")
        public int endHour;

        @SerializedName("end_minute")
        public int endMinute;
        public int enabled;

        public @NonNullByDefault({}) Actions actions;
    }

    public class Actions {
        public int resume;
        public int vol;
        public int led;
        public int dust;
        public int dry;
    }

    public GetDndTimer() {
    }
}
