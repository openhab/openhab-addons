/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.nibeuplink.internal.model;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * interface which contains the public methods of the channellist classes
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public interface ChannelList {

    /**
     * returns an unmodifiable set containing all available channels.
     *
     * @return
     */
    Set<NibeChannel> getChannels();

    /**
     * returns the matching channel, null if no match was found
     *
     * @param channelCode the channelCode which identifies the channel
     * @return channel which belongs to the code. might be null if there is no channel found.
     */
    @Nullable
    NibeChannel fromCode(String channelCode);
}
