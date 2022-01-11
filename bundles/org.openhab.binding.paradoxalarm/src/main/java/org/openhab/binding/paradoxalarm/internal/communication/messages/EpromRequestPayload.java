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
package org.openhab.binding.paradoxalarm.internal.communication.messages;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.paradoxalarm.internal.exceptions.ParadoxException;
import org.openhab.binding.paradoxalarm.internal.util.ParadoxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EpromRequestPayload} Object representing payload of IP packet which retrieves data from Paradox EPROM
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class EpromRequestPayload extends MemoryRequestPayload implements IPayload {

    private final Logger logger = LoggerFactory.getLogger(EpromRequestPayload.class);

    public EpromRequestPayload(int address, byte bytesToRead) throws ParadoxException {
        super(address, bytesToRead);
    }

    @Override
    protected byte calculateControlByte() {
        int address = getAddress();
        logTraceHexFormatted("Address: {}", address);

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
