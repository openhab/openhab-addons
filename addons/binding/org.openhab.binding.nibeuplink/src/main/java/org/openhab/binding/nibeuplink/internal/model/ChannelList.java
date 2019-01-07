/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeuplink.internal.model;

import java.util.Set;

/**
 * interface which contains the public methods of the channellist classes
 *
 * @author Alexander Friese - initial contribution
 */
public interface ChannelList {

    /**
     * returns an unmodifiable set containing all available channels.
     *
     * @return
     */
    Set<Channel> getChannels();

    /**
     * returns the matching channel, null if no match was found
     *
     * @param channelCode the channelCode which identifies the channel
     * @return channel which belongs to the code. might be null if there is no channel found.
     */
    Channel fromCode(String channelCode);
}
