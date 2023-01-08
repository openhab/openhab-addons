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
package org.openhab.binding.paradoxalarm.internal.communication.messages;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.paradoxalarm.internal.exceptions.ParadoxException;
import org.openhab.binding.paradoxalarm.internal.util.ParadoxUtil;

/**
 * The {@link RamRequestPayload} Object representing payload of IP packet which retrieves data from Paradox RAM
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class RamRequestPayload extends MemoryRequestPayload implements IPayload {

    private static final byte CONTROL_BYTE = ParadoxUtil.setBit((byte) 0, 7, 1);

    public RamRequestPayload(int address, byte bytesToRead) throws ParadoxException {
        super(address, bytesToRead);
    }

    @Override
    protected byte calculateControlByte() {
        return CONTROL_BYTE;
    }
}
