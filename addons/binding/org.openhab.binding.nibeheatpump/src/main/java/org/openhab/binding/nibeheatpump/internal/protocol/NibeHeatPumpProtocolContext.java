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
 * The {@link NibeHeatPumpProtocolContext} define interface for Nibe communication protocol context.
 *
 *
 * @author Pauli Anttila - Initial contribution
 */
public interface NibeHeatPumpProtocolContext {
    ByteBuffer buffer();

    ByteBuffer msg();

    NibeHeatPumpProtocolStates state();

    void state(NibeHeatPumpProtocolStates state);

    void sendAck();

    void sendNak();

    void sendWriteMsg();

    void sendReadMsg();

    void msgReceived(byte[] data);
}
