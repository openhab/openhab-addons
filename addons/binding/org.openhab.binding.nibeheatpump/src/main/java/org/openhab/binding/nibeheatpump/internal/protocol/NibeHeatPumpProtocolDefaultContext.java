/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
