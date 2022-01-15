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
package org.openhab.binding.webthing.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * The {@link ChannelHandler} class is a simplified abstraction of an openHAB Channel implementing
 * methods to observe a channel as well to update an Item associated to a channel
 *
 * @author Gregor Roth - Initial contribution
 */
@NonNullByDefault
public interface ChannelHandler {

    /**
     * register a listener to observer the channel regarding item change events
     *
     * @param channelUID the channel identifier
     * @param listener the listener to be notified
     */
    void observeChannel(ChannelUID channelUID, ItemChangedListener listener);

    /**
     * updates an Item state of a dedicated channel
     *
     * @param channelUID the channel identifier
     * @param command the state update command
     */
    void updateItemState(ChannelUID channelUID, Command command);

    /**
     * Listener that will be notified, if a Item state is changed
     */
    interface ItemChangedListener {

        /**
         * item change callback method
         * 
         * @param channelUID the channel identifier
         * @param stateCommand the item change command
         */
        void onItemStateChanged(ChannelUID channelUID, State stateCommand);
    }
}
