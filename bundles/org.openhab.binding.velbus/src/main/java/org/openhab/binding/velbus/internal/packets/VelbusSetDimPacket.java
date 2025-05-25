/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.velbus.internal.packets;

import static org.openhab.binding.velbus.internal.VelbusBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link VelbusSetDimPacket} represents a Velbus packet that can be used to
 * set the color of a channel on the DALI module.
 *
 * @author Daniel Rosengarten - Initial contribution
 */
@NonNullByDefault
public class VelbusSetDimPacket extends VelbusPacket {
    private byte[] data;

    public VelbusSetDimPacket(byte address, byte channel) {
        super(address, PRIO_HI, false);

        this.data = new byte[] { COMMAND_SET_DIMVALUE, channel, VALUE_UNCHANGED, VALUE_UNCHANGED, VALUE_UNCHANGED };
    }

    public void setDim(byte dim) {
        data[2] = dim;
    }

    public void setMode(byte mode) {
        data[3] = mode;
    }

    public void setLastUsedDim() {
        data[0] = COMMAND_RESTORE_LAST_DIMVALUE;
    }

    @Override
    protected byte[] getDataBytes() {
        return data;
    }
}
