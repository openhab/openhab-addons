/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.cul.max.internal.messages;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.openhab.binding.cul.max.internal.messages.constants.MaxCulMsgType;

/**
 * @author Johannes Goehr (johgoe) - Migration to OpenHab 3.0
 */
class SetTemperatureMsgTest {

    @Test // 7.1 4:30
    public void checkSampleMessage() {

        SetTemperatureMsg testling = new SetTemperatureMsg("Z0E5805400CCBDD01020300A227150931");
        assertEquals(MaxCulMsgType.SET_TEMPERATURE, testling.msgType);
    }
}
