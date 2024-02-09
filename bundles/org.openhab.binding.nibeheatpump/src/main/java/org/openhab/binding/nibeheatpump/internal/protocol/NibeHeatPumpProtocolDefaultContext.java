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
package org.openhab.binding.nibeheatpump.internal.protocol;

import java.nio.ByteBuffer;

/**
 * The {@link NibeHeatPumpProtocolDefaultContext} implements default Nibe protocol context.
 *
 *
 * @author Pauli Anttila - Initial contribution
 */
public class NibeHeatPumpProtocolDefaultContext implements NibeHeatPumpProtocolContext {
    private NibeHeatPumpProtocolStates state = NibeHeatPumpProtocolStates.WAIT_START;
    private final ByteBuffer buffer = ByteBuffer.allocate(1000);
    private final ByteBuffer msg = ByteBuffer.allocate(100);

    @Override
    public NibeHeatPumpProtocolStates state() {
        return this.state;
    }

    @Override
    public void state(NibeHeatPumpProtocolStates state) {
        this.state = state;
    }

    @Override
    public ByteBuffer buffer() {
        return buffer;
    }

    @Override
    public ByteBuffer msg() {
        return msg;
    }

    @Override
    public void sendAck() {
    }

    @Override
    public void sendNak() {
    }

    @Override
    public void msgReceived(byte[] data) {
    }

    @Override
    public void sendWriteMsg() {
    }

    @Override
    public void sendReadMsg() {
    }
}
