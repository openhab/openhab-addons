/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.insteon.internal.handler;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.config.InsteonBridgeConfiguration;
import org.openhab.binding.insteon.internal.device.Device;
import org.openhab.binding.insteon.internal.device.InsteonModem;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.State;

/**
 * The {@link InsteonThingHandler} represents the insteon thing handler interface.
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public interface InsteonThingHandler extends ThingHandler {
    /**
     * Returns the thing device
     *
     * @return the device
     */
    public @Nullable Device getDevice();

    /**
     * Returns the thing id
     *
     * @return the thing id
     */
    public String getThingId();

    /**
     * Returns the thing information
     *
     * @return the thing information
     */
    public String getThingInfo();

    /**
     * Returns a map of all channels information
     *
     * @return a map of all channels information
     */
    public Map<String, String> getChannelsInfo();

    /**
     * Returns if the thing status is online
     *
     * @return true if the thing status is online
     */
    public boolean isOnline();

    /**
     * Updates a channel state
     *
     * @param channelUID the channel uid
     * @param state the channel state
     */
    public void updateState(ChannelUID channelUID, State state);

    /**
     * Triggers a channel event
     *
     * @param channelUID the channel uid
     * @param event the channel event name
     */
    public void triggerChannel(ChannelUID channelUID, String event);

    /**
     * Notifies that the bridge thing has been disposed
     */
    public void bridgeThingDisposed();

    /**
     * Notifies that the bridge thing has been updated
     *
     * @param config the bridge config
     * @param modem the bridge modem
     */
    public void bridgeThingUpdated(InsteonBridgeConfiguration config, InsteonModem modem);

    /**
     * Refreshes the thing
     */
    public void refresh();

    /**
     * Updates the thing status
     */
    public void updateStatus();
}
