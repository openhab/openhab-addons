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
 * The {@link VelbusFirstGenerationDeviceModuleAddress} represents the address Velbus
 * module of the 1st generation.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
public class VelbusFirstGenerationDeviceModuleAddress extends VelbusModuleAddress {

    public VelbusFirstGenerationDeviceModuleAddress(byte address) {
        super(address, 0);
    }

    @Override
    public int getChannelNumber(VelbusChannelIdentifier velbusChannelIdentifier) {
        byte channelByte = velbusChannelIdentifier.getChannelByte();
        if (channelByte == (byte) 0x03) {
            return 1;
        } else if (channelByte == (byte) 0x0C) {
            return 2;
        }

        throw new IllegalArgumentException("The byte '" + channelByte
                + "' does not represent a valid channel on the first generation device with address '"
                + velbusChannelIdentifier.getAddress() + "'.");
    }

    @Override
    public VelbusChannelIdentifier getChannelIdentifier(int channelIndex) {
        if (channelIndex == 0) {
            return new VelbusChannelIdentifier(getAddress(), (byte) 0x03);
        } else if (channelIndex == 1) {
            return new VelbusChannelIdentifier(getAddress(), (byte) 0x0C);
        } else {
            throw new IllegalArgumentException("The channel index '" + channelIndex
                    + "' is not valid for the first generation device on the address '" + getAddress() + "'.");
        }
    }
}
