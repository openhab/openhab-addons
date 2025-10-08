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
package org.openhab.binding.myenergi.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link HarviSummary} is a DTO class used to represent a high level
 * summary of a Harvi device. It's used to deserialize JSON API results.
 *
 * @author Rene Scherer - Initial contribution
 *
 */
@NonNullByDefault
public class HarviSummary extends BaseSummary {

    // {"sno":10402186,"dat":"27-11-2020","tim":"15:26:57","ectp1":-11,"ectt1":"Generation","ectt2":"None","ectt3":"None","ect1p":1,"ect2p":1,"ect3p":1,"fwv":""}

    @SerializedName("ectt1")
    @Nullable
    public String clampName1;
    @SerializedName("ectt2")
    @Nullable
    public String clampName2;
    @SerializedName("ectt3")
    @Nullable
    public String clampName3;

    @SerializedName("ectp1")
    @Nullable
    public Integer clampPower1; // in Watts
    @SerializedName("ectp2")
    @Nullable
    public Integer clampPower2;
    @SerializedName("ectp3")
    @Nullable
    public Integer clampPower3;

    @SerializedName("ect1p")
    @Nullable
    public Integer clampPhase1;
    @SerializedName("ect2p")
    @Nullable
    public Integer clampPhase2;
    @SerializedName("ect3p")
    @Nullable
    public Integer clampPhase3;

    @Override
    public String toString() {
        return "HarviSummary [serialNumber=" + serialNumber + ", dat=" + dat + ", tim=" + tim + ", clampName1="
                + clampName1 + ", clampName2=" + clampName2 + ", clampName3=" + clampName3 + ", clampWatts1="
                + clampPower1 + ", clampWatts2=" + clampPower2 + ", clampWatts3=" + clampPower3 + ", clampPhase1="
                + clampPhase1 + ", clampPhase2=" + clampPhase2 + ", clampPhase3=" + clampPhase3 + ", firmwareVersion="
                + firmwareVersion + "]";
    }
}
