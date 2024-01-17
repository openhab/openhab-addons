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
package org.openhab.binding.velbus.internal.packets;

import static org.openhab.binding.velbus.internal.VelbusBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link VelbusSetColorPacket} represents a Velbus packet that can be used to
 * set the color of a channel on the DALI module.
 *
 * @author Daniel Rosengarten - Initial contribution
 */
@NonNullByDefault
public class VelbusSetColorPacket extends VelbusPacket {
    private byte[] data;

    public VelbusSetColorPacket(byte address, byte channel) {
        super(address, PRIO_HI, false);

        this.data = new byte[] { COMMAND_SET_COLOR, channel, VALUE_UNCHANGED, VALUE_UNCHANGED, VALUE_UNCHANGED,
                VALUE_UNCHANGED, VALUE_UNCHANGED };
    }

    public void setBrightness(byte brightness) {
        data[2] = brightness;
    }

    public void setColor(byte r, byte g, byte b) {
        data[3] = r;
        data[4] = g;
        data[5] = b;
    }

    public void setColor(byte rgb[]) {
        data[3] = rgb[0];
        data[4] = rgb[1];
        data[5] = rgb[2];
    }

    public void setWhite(byte white) {
        data[6] = white;
    }

    @Override
    protected byte[] getDataBytes() {
        return data;
    }
}
