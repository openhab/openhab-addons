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
package org.openhab.binding.freeathomesystem.internal.handler;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeathomesystem.internal.valuestateconverter.ValueStateConverter;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.State;

/**
 * The {@link ChannelUpdateHandler} is responsible for handling update commands, which are
 * sent by the free@home bridge via WebSocket connection.
 *
 * @author Andras Uhrin - Initial contribution
 *
 */
@NonNullByDefault
public class ChannelUpdateHandler {

    // assignmet sub-class between datapoints and OpenHAB channels
    public class DatapointChannelAssignment {
        FreeAtHomeDeviceHandler thingHandler;
        ValueStateConverter valueStateConverter;
        ChannelUID channelUID;

        public DatapointChannelAssignment(FreeAtHomeDeviceHandler handler, ValueStateConverter converter,
                ChannelUID uid) {
            thingHandler = handler;
            valueStateConverter = converter;
            channelUID = uid;
        }
    }

    Map<String, DatapointChannelAssignment> datapoints;

    public ChannelUpdateHandler() {
        datapoints = new HashMap<String, DatapointChannelAssignment>();
    }

    public boolean registerChannel(String eventDatapointID, FreeAtHomeDeviceHandler thingHandler, ChannelUID channelUID,
            ValueStateConverter valueConverter) {
        DatapointChannelAssignment datapointElement = new DatapointChannelAssignment(thingHandler, valueConverter,
                channelUID);

        datapoints.put(eventDatapointID, datapointElement);

        return true;
    }

    public boolean unregisterChannel(String eventDatapointID) {
        datapoints.remove(eventDatapointID);

        return true;
    }

    public boolean registerChannel(String deviceID, String deviceChannel, String deviceDatapoint,
            FreeAtHomeDeviceHandler thingHandler, ChannelUID channelUID, ValueStateConverter valueConverter) {
        String eventDatapointID = new String(deviceID + "/" + deviceChannel + "/" + deviceDatapoint);

        return registerChannel(eventDatapointID, thingHandler, channelUID, valueConverter);
    }

    public boolean unregisterChannel(String deviceID, String deviceChannel, String deviceDatapoint) {
        String eventDatapointID = new String(deviceID + "/" + deviceChannel + "/" + deviceDatapoint);

        return unregisterChannel(eventDatapointID);
    }

    // Method to call the bridge handler event update
    public boolean updateChannelByDatapointEvent(String eventDatapointID, String valueString) {
        boolean ret = false;

        DatapointChannelAssignment datapointElement = datapoints.get(eventDatapointID);

        if (datapointElement != null) {
            State state = datapointElement.valueStateConverter.convertToState(valueString);

            FreeAtHomeDeviceHandler deviceHandler = datapointElement.thingHandler;

            // Handle state change
            deviceHandler.handleEventBasedUpdate(datapointElement.channelUID, state);

            // if it is virtual device, give a feedback to free@home also
            if (deviceHandler.isThingHandlesVirtualDevice()) {
                deviceHandler.feedbackForVirtualDevice(datapointElement.channelUID, valueString);
            }

            ret = true;
        }

        return ret;
    }
}
