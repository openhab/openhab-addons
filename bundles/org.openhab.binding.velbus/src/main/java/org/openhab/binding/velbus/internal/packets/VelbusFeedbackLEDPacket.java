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
package org.openhab.binding.velbus.internal.packets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velbus.internal.VelbusChannelIdentifier;

/**
 * The {@link VelbusFeedbackLEDPacket} represents a Velbus packet that can be used to
 * set the feedback led (clear/set/slow blink/fast blink/very fast blink) of the given Velbus module.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
public class VelbusFeedbackLEDPacket extends VelbusPacket {
    private byte command;
    private byte channel;

    public VelbusFeedbackLEDPacket(VelbusChannelIdentifier velbusChannelIdentifier, byte command) {
        super(velbusChannelIdentifier.getAddress(), PRIO_LOW);

        this.channel = velbusChannelIdentifier.getChannelByte();
        this.command = command;
    }

    @Override
    protected byte[] getDataBytes() {
        return new byte[] { command, channel };
    }
}
