/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.velbus.internal.packets;

import static org.openhab.binding.velbus.VelbusBindingConstants.COMMAND_MODULE_NAME_REQUEST;

/**
 * The {@link VelbusChannelNameRequestPacket} represents a Velbus packet that can be used to
 * request the name of a given channel.
 *
 * @author Cedric Boon - Initial contribution
 */
public class VelbusChannelNameRequestPacket extends VelbusPacket {
    private static final byte ALL_CHANNELS = (byte) 0xFF;

    public VelbusChannelNameRequestPacket(byte address) {
        super(address, PRIO_LOW);
    }

    @Override
    protected byte[] getDataBytes() {
        return new byte[] { COMMAND_MODULE_NAME_REQUEST, ALL_CHANNELS };
    }

}
