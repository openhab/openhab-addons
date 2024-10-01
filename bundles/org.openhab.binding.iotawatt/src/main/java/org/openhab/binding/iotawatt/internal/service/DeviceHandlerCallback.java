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
package org.openhab.binding.iotawatt.internal.service;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.iotawatt.internal.model.IoTaWattChannelType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.State;

/**
 * Allows the service to do callback to the device handler.
 *
 * @author Peter Rosenberg - Initial contribution
 */
@NonNullByDefault
public interface DeviceHandlerCallback {
    /**
     * Updates the status of the thing. The detail of the status will be 'NONE'.
     *
     * @param status the status
     */
    void updateStatus(ThingStatus status);

    /**
     * Updates the status of the thing.
     *
     * @param status the status
     * @param statusDetail the detail of the status
     */
    void updateStatus(ThingStatus status, ThingStatusDetail statusDetail);

    /**
     * Updates the status of the thing.
     *
     * @param status the status
     * @param statusDetail the detail of the status
     * @param description the description of the status
     */
    void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description);

    /**
     *
     * Updates the state of the thing.
     *
     * @param channelUID unique id of the channel, which was updated
     * @param state new state
     */
    void updateState(ChannelUID channelUID, State state);

    /**
     * @return The ThingUID of the Thing
     */
    ThingUID getThingUID();

    /**
     * Adds the channel to the Thing if the channel does not yet exist.
     * 
     * @param channelUID The ChannelUID of the channel to add
     * @param ioTaWattChannelType The IoTaWattChannelType of the channel to add
     */
    void addChannelIfNotExists(ChannelUID channelUID, IoTaWattChannelType ioTaWattChannelType);
}
