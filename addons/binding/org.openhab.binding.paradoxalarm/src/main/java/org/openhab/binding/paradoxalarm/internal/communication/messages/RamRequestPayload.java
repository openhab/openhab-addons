/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.paradoxalarm.internal.communication.messages;

import org.openhab.binding.paradoxalarm.internal.exceptions.ParadoxBindingException;
import org.openhab.binding.paradoxalarm.internal.util.ParadoxUtil;

/**
 * The {@link RamRequestPayload} Object representing payload of IP packet which retrieves data from Paradox RAM
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public class RamRequestPayload extends MemoryRequestPayload implements IPPacketPayload {

    public RamRequestPayload(int address, byte bytesToRead) throws ParadoxBindingException {
        super(address, bytesToRead);
    }

    @Override
    protected byte calculateControlByte() {
        byte byteValue = (byte) 0;
        return ParadoxUtil.setBit(byteValue, 7, 1);
    }

}
