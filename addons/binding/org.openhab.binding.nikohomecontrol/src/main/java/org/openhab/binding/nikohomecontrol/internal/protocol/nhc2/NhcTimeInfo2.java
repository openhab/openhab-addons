/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
    String timeZone = "";
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
    public String getTimeZone() {
        return timeZone;
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
