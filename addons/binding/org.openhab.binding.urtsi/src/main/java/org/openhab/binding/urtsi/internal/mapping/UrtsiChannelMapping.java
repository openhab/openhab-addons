/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.urtsi.internal.mapping;

/**
 * The {@code UrtsiChannelMapping} is responsible for mapping the channel you select at the URTSI II device to the
 * channel which is transmitted via the serial port.
 *
 * @author Oliver Libutzki - Initial contribution
 *
 */
public class UrtsiChannelMapping {

    /**
     * Returns the mapped channel which is used to communicate with the URTSI II device. Returns null if the given
     * channel is not valid.
     *
     * @param configuredChannel the channel selected at the URTSI II device
     * @return returns the mapped channel, returns null is the given channel is not valid.
     */
    public static String getMappedChannel(String configuredChannel) {
        int channel = Integer.parseInt(configuredChannel, 16);
        if (channel == 0) {
            channel = 16;
        }
        if (channel < 1 || channel > 16) {
            return null;
        }
        return String.format("%02d", channel);
    }

}
