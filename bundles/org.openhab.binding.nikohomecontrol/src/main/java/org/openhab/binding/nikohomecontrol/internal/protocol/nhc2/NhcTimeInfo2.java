/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.nikohomecontrol.internal.protocol.nhc2;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * {@link NhcTimeInfo2} represents Niko Home Control II timeinfo. It is used when parsing the timeinfo response json.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public class NhcTimeInfo2 {
    @SerializedName(value = "GMTOffset")
    String gmtOffset = "";
    String timezone = "";
    String isDST = "";
    @SerializedName(value = "UTCTime")
    String utcTime = "";

    /**
     * @return the gMTOffset
     */
    public String getGMTOffset() {
        return gmtOffset;
    }

    /**
     * @return the timeZone
     */
    public String getTimezone() {
        return timezone;
    }

    /**
     * @return the isDST
     */
    public String getIsDst() {
        return isDST;
    }

    /**
     * @return the uTCTime
     */
    public String getUTCTime() {
        return utcTime;
    }
}
