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
package org.openhab.binding.bluetooth.daikinmadoka.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteOrder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaMessage;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaValue;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.GetIndoorOutoorTemperatures;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.GetOperationHoursCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.SetPowerstateCommand;
import org.openhab.core.library.types.OnOffType;

/**
 *
 * @author blafois - Initial contribution
 *
 */
@NonNullByDefault
public class MadokaMessageTest {

    @Test
    public void testMessageBuildTemperature() {
        byte[][] resp = new GetIndoorOutoorTemperatures().getRequest();
        assertArrayEquals(resp[0], new byte[] { 0x00, 0x06, 0x00, 0x01, 0x10, 0x00, 0x00 });
    }

    @Test
    public void testMessageBuildSetPower() {
        boolean powered = true;
        MadokaValue mv = new MadokaValue(0x20, 1, new byte[] { 1 });
        byte[][] resp = MadokaMessage.createRequest(new SetPowerstateCommand(OnOffType.ON), mv);
        assertArrayEquals(new byte[] { 0x00, 0x07, 0x00, 0x40, 0x20, 0x20, 0x01, (byte) (powered ? 0x01 : 0x00) },
                resp[0]);
    }

    @Test
    public void testMessageBuildSize() {
        byte[][] resp = MadokaMessage.createRequest(new GetIndoorOutoorTemperatures());
        assertEquals(1, resp.length);
    }

    @Test
    public void testOperationHoursCommand() {
        byte[][] resp = new GetOperationHoursCommand().getRequest();
        assertEquals(2, resp.length);
        assertArrayEquals(new byte[] { 0x00, 0x19, 0x00, 0x01, 0x12, 0x02, 0x01, 0x00, 0x40, 0x00, 0x41, 0x00, 0x42,
                0x00, 0x43, 0x00, 0x44, 0x00, 0x45, 0x00 }, resp[0]);
        assertArrayEquals(new byte[] { 0x01, 0x46, 0x00, 0x47, 0x00, 0x48, 0x00 }, resp[1]);
    }

    @Test
    public void testParseOperationHours() {
        MadokaValue mv = new MadokaValue(0, 4, new byte[] { (byte) 0xF4, 0x03, 0x00, 0x00 });
        Long v = mv.getComputedValue(ByteOrder.LITTLE_ENDIAN);
        assertEquals(1012, v);
    }
}
