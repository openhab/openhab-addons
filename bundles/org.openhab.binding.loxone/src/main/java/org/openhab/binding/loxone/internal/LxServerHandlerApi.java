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
package org.openhab.binding.loxone.internal;

import java.io.IOException;
import java.util.Map;

import org.openhab.binding.loxone.internal.controls.LxControl;
import org.openhab.binding.loxone.internal.security.LxWsSecurity;
import org.openhab.binding.loxone.internal.types.LxUuid;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescription;

import com.google.gson.Gson;

/**
 * Representation of a Loxone Miniserver. It is an openHAB {@link Thing}, which is used to communicate with
 * objects (controls) configured in the Miniserver over channels.
 *
 * @author Pawel Pieczul - Initial contribution
 */
public interface LxServerHandlerApi {

    /**
     * Sends an action to a Loxone Miniserver's control.
     *
     * @param id identifier of the control
     * @param operation identifier of the operation
     * @throws IOException when communication error with Miniserver occurs
     */
    void sendAction(LxUuid id, String operation) throws IOException;

    /**
     * Add a control - creates internal data structures and channels in the framework.
     * This method should be used for all dynamically created controls, usually as a result of Miniserver's state update
     * messages, after the static configuration is setup.
     *
     * @param control control to be added
     */
    void addControl(LxControl control);

    /**
     * Remove a control - removes internal data structures and channels from the framework
     * This method should be used for all dynamically created controls, usually as a result of Miniserver's state update
     * messages, after the static configuration is setup.
     *
     * @param control control to remove
     */
    void removeControl(LxControl control);

    /**
     * Sets channel's state to a new value
     *
     * @param channelId channel ID
     * @param state new state value
     */
    void setChannelState(ChannelUID channelId, State state);

    /**
     * Sets a new channel state description. This method is called to dynamically change the way the channel state is
     * interpreted and displayed. It is called when a dynamic state update is received from the Miniserver with a new
     * way of displaying control's state.
     *
     * @param channelId channel ID
     * @param description a new state description
     */
    void setChannelStateDescription(ChannelUID channelId, StateDescription description);

    /**
     * Get configuration parameter from the thing configuration. This method is called by the {@link LxWsSecurity} class
     * to dynamically retrieve previously stored login token and its parameters.
     *
     * @param name parameter name
     * @return parameter value
     */
    String getSetting(String name);

    /**
     * Set configuration parameters in the thing configuration. This method is called by the {@link LxWsSecurity} class
     * to dynamically stored login token and its parameters received from the Miniserver.
     *
     * @param properties pairs of parameter names and values
     */
    void setSettings(Map<String, String> properties);

    /**
     * Get GSON object for reuse
     *
     * @return GSON object
     */
    Gson getGson();

    /**
     * Get ID of the Miniserver's Thing
     *
     * @return ID of the Thing
     */
    ThingUID getThingId();
}
