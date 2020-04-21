/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import static org.junit.Assert.assertArrayEquals;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.junit.Test;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaMessage;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaValue;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.GetIndoorOutoorTemperatures;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.SetPowerstateCommand;

/**
 *
 * @author blafois
 *
 */
public class MadokaMessageTest {

    @Test
    public void testMessageBuildTemperature() {
        byte[] resp = MadokaMessage.createRequest(new GetIndoorOutoorTemperatures());
        assertArrayEquals(resp, new byte[] { 0x00, 0x06, 0x00, 0x01, 0x10, 0x00, 0x00 });
    }

    @Test
    public void testMessageBuildSetPower() {
        boolean powered = true;
        MadokaValue mv = new MadokaValue(0x20, 1, new byte[] { 1 });
        byte[] resp = MadokaMessage.createRequest(new SetPowerstateCommand(OnOffType.ON), mv);
        assertArrayEquals(
                new byte[] { 0x00, 0x07, 0x00, 0x40, 0x20, 0x20, 0x01, (byte) (powered == true ? 0x01 : 0x00) }, resp);
    }

}
