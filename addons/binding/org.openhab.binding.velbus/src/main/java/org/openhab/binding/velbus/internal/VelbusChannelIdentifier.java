/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.velbus.internal;

/**
 * The {@link VelbusChannelIdentifier} represents a class with properties that uniquely identify a channel.
 *
 * @author Cedric Boon - Initial contribution
 */
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
