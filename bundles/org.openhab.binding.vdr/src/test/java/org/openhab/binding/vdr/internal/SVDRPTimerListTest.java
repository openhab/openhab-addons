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
package org.openhab.binding.vdr.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.vdr.internal.svdrp.SVDRPException;
import org.openhab.binding.vdr.internal.svdrp.SVDRPTimerList;

/**
 * Specific unit tests to check if {@link SVDRPTimerList} parses SVDRP responses correctly
 *
 * @author Matthias Klocke - Initial contribution
 *
 */
@NonNullByDefault
public class SVDRPTimerListTest {
    private final String timerListResponseTimerActive = "1 1:1:2021-01-12:2013:2110:50:99:Charité (1/6)~Eiserne Lunge:Test\n"
            + "2 9:1:2021-01-12:2058:2200:50:99:Charité (2/6)~Blutsauger:Test";
    private final String timerListResponseTimerNotActive = "1 1:1:2021-01-12:2013:2110:50:99:Charité (1/6)~Eiserne Lunge:Test\n"
            + "2 1:1:2021-01-12:2058:2200:50:99:Charité (2/6)~Blutsauger:Test";

    @Test
    public void testParseTimerList() throws SVDRPException {
        SVDRPTimerList list = SVDRPTimerList.parse(timerListResponseTimerActive);
        assertEquals(true, list.isRecordingActive());
        list = SVDRPTimerList.parse(timerListResponseTimerNotActive);
        assertEquals(false, list.isRecordingActive());
    }
}
