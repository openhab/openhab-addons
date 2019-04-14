/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.paradoxalarm.internal.communication.messages;

import org.openhab.binding.paradoxalarm.internal.exceptions.ParadoxBindingException;
import org.openhab.binding.paradoxalarm.internal.util.ParadoxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EpromRequestPayload} Object representing payload of IP packet which retrieves data from Paradox EPROM
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public class EpromRequestPayload extends MemoryRequestPayload implements IPPacketPayload {

    private final Logger logger = LoggerFactory.getLogger(EpromRequestPayload.class);

    public EpromRequestPayload(int address, byte bytesToRead) throws ParadoxBindingException {
        super(address, bytesToRead);
    }

    @Override
    protected byte calculateControlByte() {
        int address = getAddress();
        logger.trace("Address: {}", String.format("0x%02X,\t", address));
        byte controlByte = 0x00;
        byte[] shortToByteArray = ParadoxUtil.intToByteArray(address);
        if (shortToByteArray.length > 2) {
            byte bit16 = ParadoxUtil.getBit(address, 16);
            controlByte |= bit16 << 0;
            byte bit17 = ParadoxUtil.getBit(address, 17);
            controlByte |= bit17 << 1;
        }
        logger.trace("ControlByte value: {}", controlByte);
        return controlByte;
    }
}
