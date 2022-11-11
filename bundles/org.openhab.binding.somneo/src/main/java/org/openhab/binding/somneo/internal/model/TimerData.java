/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.somneo.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents the program timer state from the API.
 *
 * @author Michael Myrcik - Initial contribution
 */
@NonNullByDefault
public class TimerData {

    @SerializedName("rlxmn")
    private int relaxMinutes;

    @SerializedName("rlxsc")
    private int relaxSeconds;

    @SerializedName("dskmn")
    private int sunsetMinutes;

    @SerializedName("dsksc")
    private int sunsetSeconds;

    public int remainingTimeRelax() {
        return relaxMinutes * 60 + relaxSeconds;
    }

    public int remainingTimeSunset() {
        return sunsetMinutes * 60 + sunsetSeconds;
    }
}
