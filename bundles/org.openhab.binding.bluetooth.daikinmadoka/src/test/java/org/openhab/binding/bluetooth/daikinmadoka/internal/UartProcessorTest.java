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
package org.openhab.binding.bluetooth.daikinmadoka.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.GetCleanFilterIndicatorCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.GetEyeBrightnessCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.GetFanspeedCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.GetIndoorOutoorTemperatures;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.GetOperationHoursCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.GetOperationmodeCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.GetPowerstateCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.GetSetpointCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.GetVersionCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.ResponseListener;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.SetEyeBrightnessCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.SetFanspeedCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.SetOperationmodeCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.SetPowerstateCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.SetSetpointCommand;

/**
 *
 * @author blafois - Initial contribution
 *
 */
@NonNullByDefault
public class UartProcessorTest implements ResponseListener {

    private boolean completed = false;

    @Test
    public void testUartProcessor() {
        BRC1HUartProcessor processor = new BRC1HUartProcessor(this);

        processor.chunkReceived(
                new byte[] { 0x01, 0x1F, 0x01, 0x03, 0x20, 0x01, 0x04, 0x21, 0x01, 0x01, 0x30, 0x01, 0x00 });

        processor.chunkReceived(new byte[] { 0x00, 0x1F, 0x00, 0x00, 0x30, 0x10, 0x01, 0x00, 0x13, 0x01, 0x1F, 0x15,
                0x01, 0x10, 0x16, 0x01, 0x12, 0x17, 0x01, 0x20 });

        assertTrue(completed);

        this.completed = false;

        processor.chunkReceived(new byte[] { 0x01, 0x01, 0x00, 0x31, 0x01, 0x01, 0x32, 0x01, 0x00, 0x40, 0x01, 0x00,
                (byte) 0xA0, 0x01, 0x10, (byte) 0xA1, 0x01, 0x10, (byte) 0xA2, 0x02 });
        assertFalse(completed);
        processor.chunkReceived(new byte[] { 0x00, 0x49, 0x00, 0x00, 0x40, 0x12, 0x01, 0x1C, 0x15, 0x01, (byte) 0xF0,
                0x20, 0x02, 0x0A, (byte) 0x80, 0x21, 0x02, 0x0A, (byte) 0x80, 0x30 });
        assertFalse(completed);
        processor.chunkReceived(new byte[] { 0x02, 0x08, 0x00, (byte) 0xA3, 0x02, 0x08, 0x00, (byte) 0xA4, 0x01, 0x11,
                (byte) 0xA5, 0x01, 0x11, (byte) 0xB0, 0x01, 0x20, (byte) 0xB1, 0x01, 0x20, (byte) 0xB2 });
        assertFalse(completed);
        processor.chunkReceived(new byte[] { 0x03, 0x02, 0x10, 0x00, (byte) 0xB3, 0x02, 0x10, 0x00, (byte) 0xB4, 0x01,
                0x17, (byte) 0xB5, 0x01, 0x17, (byte) 0xFE, 0x01, 0x02 });
        assertTrue(completed);
    }

    @Override
    public void receivedResponse(byte[] bytes) {
        this.completed = true;
    }

    @Override
    public void receivedResponse(GetVersionCommand command) {
    }

    @Override
    public void receivedResponse(GetFanspeedCommand command) {
    }

    @Override
    public void receivedResponse(GetOperationmodeCommand command) {
    }

    @Override
    public void receivedResponse(GetPowerstateCommand command) {
    }

    @Override
    public void receivedResponse(GetSetpointCommand command) {
    }

    @Override
    public void receivedResponse(GetIndoorOutoorTemperatures command) {
    }

    @Override
    public void receivedResponse(SetPowerstateCommand command) {
    }

    @Override
    public void receivedResponse(SetSetpointCommand command) {
    }

    @Override
    public void receivedResponse(SetOperationmodeCommand command) {
    }

    @Override
    public void receivedResponse(SetFanspeedCommand command) {
    }

    @Override
    public void receivedResponse(GetOperationHoursCommand command) {
    }

    @Override
    public void receivedResponse(GetEyeBrightnessCommand command) {
    }

    @Override
    public void receivedResponse(GetCleanFilterIndicatorCommand command) {
    }

    @Override
    public void receivedResponse(SetEyeBrightnessCommand command) {
    }
}
