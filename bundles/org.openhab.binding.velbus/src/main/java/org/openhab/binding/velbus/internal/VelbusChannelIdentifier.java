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
package org.openhab.binding.velbus.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link VelbusChannelIdentifier} represents a class with properties that uniquely identify a channel.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
public class VelbusChannelIdentifier {

    private byte address;
    private byte channel;

    public VelbusChannelIdentifier(byte address, byte channel) {
        this.address = address;
        this.channel = channel;
    }

    public byte getAddress() {
        return address;
    }

    public byte getChannelByte() {
        return channel;
    }

    public int getChannelNumberFromBitNumber() {
        int position = 0;
        byte remainingChannelBits = channel;
        while (remainingChannelBits != 0) {
            position++;
            if ((remainingChannelBits & ((byte) 1)) != 0) {
                return position;
            }
            remainingChannelBits = (byte) (remainingChannelBits >>> 1);
        }
        return position;
    }
}
