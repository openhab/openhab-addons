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
package org.openhab.binding.echonetlite.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.openhab.binding.echonetlite.internal.EchonetClass.AIRCON_HOMEAC;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * @author Michael Barker - Initial contribution
 */
@NonNullByDefault
class EpcTest {

    @Test
    void shouldLookupEpc() {
        final EchonetClass echonetClass = AIRCON_HOMEAC;

        for (Epc epc : Epc.Device.values()) {
            assertEquals(epc, Epc.lookup(echonetClass.groupCode(), echonetClass.classCode(), epc.code()));
        }

        for (Epc epc : Epc.AcGroup.values()) {
            assertEquals(epc, Epc.lookup(echonetClass.groupCode(), echonetClass.classCode(), epc.code()));
        }

        for (Epc epc : Epc.HomeAc.values()) {
            assertEquals(epc, Epc.lookup(echonetClass.groupCode(), echonetClass.classCode(), epc.code()));
        }
    }
}
