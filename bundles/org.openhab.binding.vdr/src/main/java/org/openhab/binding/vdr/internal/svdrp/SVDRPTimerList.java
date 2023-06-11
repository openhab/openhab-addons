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
package org.openhab.binding.vdr.internal.svdrp;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SVDRPTimerList} contains SVDRP Response Data for a Timer list
 *
 * @author Matthias Klocke - Initial contribution
 */
@NonNullByDefault
public class SVDRPTimerList {

    private List<String> timers = new ArrayList<String>();

    /**
     * parse object from SVDRP Client Response
     *
     * @param message SVDRP Client Response
     * @return Timer List Object
     * @throws SVDRPParseResponseException thrown if response data is not parseable
     */
    public static SVDRPTimerList parse(String message) {
        SVDRPTimerList timers = new SVDRPTimerList();
        List<String> lines = new ArrayList<String>();

        StringTokenizer st = new StringTokenizer(message, System.lineSeparator());
        while (st.hasMoreTokens()) {
            String timer = st.nextToken();
            lines.add(timer);
        }
        timers.setTimers(lines);
        return timers;
    }

    /**
     * Is there currently an active Recording on SVDRP Client
     *
     * @return returns true if there is an active recording
     */
    public boolean isRecordingActive() {
        for (String line : timers) {
            String timerContent = line.substring(line.indexOf(" ") + 1);
            String timerStatus = timerContent.substring(0, timerContent.indexOf(":"));
            byte b = Byte.parseByte(timerStatus);
            if (((b >> 3) & 0x0001) == 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Set timers object of SVDRPTimerList
     *
     * @param timers timers to set
     */
    private void setTimers(List<String> timers) {
        this.timers = timers;
    }
}
