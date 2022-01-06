/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.atlona.internal.pro3;

/**
 *
 * @author Tim Roberts - Initial contribution
 */
public class AtlonaPro3Utilities {
    /**
     * Helper method to create a channel id from a group with no port number attached
     *
     * @param group a group name
     * @param channelId the channel id
     * @return group + "#" + channelId
     */
    public static String createChannelID(String group, String channelId) {
        return group + "#" + channelId;
    }

    /**
     * Helper method to create a channel id from a group, port number and channel id
     *
     * @param group the group name
     * @param portNbr the port number
     * @param channelId the channel id
     * @return group + portNbr + "#" + channelId
     */
    public static String createChannelID(String group, int portNbr, String channelId) {
        return group + portNbr + "#" + channelId;
    }
}
