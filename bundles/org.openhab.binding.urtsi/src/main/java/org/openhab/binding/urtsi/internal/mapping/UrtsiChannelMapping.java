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
