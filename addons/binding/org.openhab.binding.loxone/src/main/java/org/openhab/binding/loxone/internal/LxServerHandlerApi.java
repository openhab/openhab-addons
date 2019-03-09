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
package org.openhab.binding.loxone.internal;

import java.io.IOException;
import java.util.Map;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateDescription;
import org.openhab.binding.loxone.internal.controls.LxControl;
import org.openhab.binding.loxone.internal.core.LxErrorCode;
import org.openhab.binding.loxone.internal.core.LxUuid;
import org.openhab.binding.loxone.internal.core.LxWsClient;

/**
 * This is the interface to the Miniserver thing handler that can be used by the {@link LxControl} objects.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public interface LxServerHandlerApi {
    /**
     * Set a new control's channel state in the framework.
     * Intended to be called by the control object.
     *
     * @param channelId channel ID to set the state for
     * @param state     state value to set
     */
    void setChannelState(ChannelUID channelId, State state);

    /**
     * Notify thing handler that a state description changed and must be populated.
     *
     * @param channelId   channel ID with the new state description
     * @param description new state description
     */
    void setChannelStateDescription(ChannelUID channelId, StateDescription description);

    /**
     * Add a new control to the handler structures, plus its states and subcontrols
     * Intended to be called by the control object to add extra controls (e.g. independent outputs or moods of a
     * lighting controller).
     *
     * @param control control to add
     */
    void addControl(LxControl control);

    /**
     * Updates handler structures to reflect removal of a control, its states and subcontrols
     * Intended to be called by the control object to remove extra controls.
     *
     * @param control control to remove from server structures
     */
    void removeControl(LxControl control);

    /**
     * Sends an operation to the Miniserver using current websocket connection.
     *
     * @param id        identifier of the control sending the operation
     * @param operation identifier of the operation
     * @throws IOException when communication error with Miniserver occurs
     */
    void sendAction(LxUuid id, String operation) throws IOException;

    /**
     * Obtain a thing ID of the handler.
     * Used by control objects to build channel IDs.
     *
     * @return thing ID
     */
    ThingUID getThingId();

    /**
     * Set configuration settings for the thing.
     * Used by security protocol to store token-related data, that must be preserved between the runs.
     *
     * @param settings map of setting name as a key and setting values
     */
    void setSettings(Map<String, String> settings);

    /**
     * Get a value from the configuration settings.
     * Used by security protocol to retrieve token-related data, that must be preserved between the runs.
     *
     * @param name name of the setting to retrieve
     * @return value of the setting
     */
    String getSetting(String name);

    /**
     * Parses received configuration, updates thing properties and creates appropriate objects and channels.
     * This call will remove any prior channel configuration.
     *
     * @param message configuration message received from the Miniserver
     * @param cfgApi  additional configuration structure if retrieved during connection setup
     */
    void setMiniserverConfig(String message, LxWsClient.LxResponseCfgApi cfgApi);

    /**
     * Set thing status to offline and assume the connection to the Miniserver is stopped.
     *
     * @param code   error code why we are going offline
     * @param reason details of the reason for going offline
     */
    void setOffline(LxErrorCode code, String reason);

    /**
     * Set thing status to online and assume the connection is established to the Miniserver.
     */
    void setOnline();

    void updateStateValue(LxUuid uuid, Object value);

}
