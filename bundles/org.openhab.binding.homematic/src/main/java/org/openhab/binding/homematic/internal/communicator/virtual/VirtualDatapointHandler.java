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
package org.openhab.binding.homematic.internal.communicator.virtual;

import java.io.IOException;

import org.openhab.binding.homematic.internal.misc.HomematicClientException;
import org.openhab.binding.homematic.internal.model.HmChannel;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDatapointConfig;
import org.openhab.binding.homematic.internal.model.HmDevice;

/**
 * Describes the methods used for a virtual datapoint. A virtual datapoint is generated datapoint with special
 * functions.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public interface VirtualDatapointHandler {

    /**
     * Returns the virtual datapoint name.
     */
    String getName();

    /**
     * Adds the virtual datapoint to the device.
     */
    void initialize(HmDevice device);

    /**
     * Returns true, if the virtual datapoint can handle a command for the given datapoint.
     */
    boolean canHandleCommand(HmDatapoint dp, Object value);

    /**
     * Handles the special functionality for the given virtual datapoint.
     */
    void handleCommand(VirtualGateway gateway, HmDatapoint dp, HmDatapointConfig dpConfig, Object value)
            throws IOException, HomematicClientException;

    /**
     * Returns true, if the virtual datapoint can handle the event for the given datapoint.
     */
    boolean canHandleEvent(HmDatapoint dp);

    /**
     * Handles an event to extract data required for the virtual datapoint.
     */
    void handleEvent(VirtualGateway gateway, HmDatapoint dp);

    /**
     * Returns the virtual datapoint in the given channel.
     */
    HmDatapoint getVirtualDatapoint(HmChannel channel);
}
