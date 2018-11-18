/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.paradoxalarm.internal.communication.messages;

import org.openhab.binding.paradoxalarm.internal.util.ParadoxBindingException;
import org.openhab.binding.paradoxalarm.internal.util.ParadoxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EpromRequestPayload} Object representing payload of IP packet which retrieves data from Paradox EPROM
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public class EpromRequestPayload extends MemoryRequestPayload implements IPPacketPayload {

    private static Logger logger = LoggerFactory.getLogger(EpromRequestPayload.class);

    public EpromRequestPayload(int address, byte bytesToRead) throws ParadoxBindingException {
        super(address, bytesToRead);
    }

    @Override
    protected byte calculateControlByte() {
        int address = getAddress();
        logger.debug("Address: {}", String.format("0x%02X,\t", address));
        byte controlByte = 0x00;
        byte[] shortToByteArray = ParadoxUtil.intToByteArray(address);
        if (shortToByteArray.length > 2) {
            byte bit16 = ParadoxUtil.getBit(address, 16);
            controlByte |= bit16 << 0;
            byte bit17 = ParadoxUtil.getBit(address, 17);
            controlByte |= bit17 << 1;
        }
        logger.debug("ControlByte value: " + controlByte);
        return controlByte;
    }
}
