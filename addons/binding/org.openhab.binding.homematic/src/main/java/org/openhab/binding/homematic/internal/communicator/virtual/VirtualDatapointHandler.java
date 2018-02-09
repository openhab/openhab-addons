/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
    public String getName();

    /**
     * Adds the virtual datapoint to the device.
     */
    public void initialize(HmDevice device);

    /**
     * Returns true, if the virtual datapoint can handle a command for the given datapoint.
     */
    public boolean canHandleCommand(HmDatapoint dp, Object value);

    /**
     * Handles the special functionality for the given virtual datapoint.
     */
    public void handleCommand(VirtualGateway gateway, HmDatapoint dp, HmDatapointConfig dpConfig, Object value)
            throws IOException, HomematicClientException;

    /**
     * Returns true, if the virtual datapoint can handle the event for the given datapoint.
     */
    public boolean canHandleEvent(HmDatapoint dp);

    /**
     * Handles a event to extract data required for the virtual datapoint.
     */
    public void handleEvent(VirtualGateway gateway, HmDatapoint dp);

    /**
     * Returns the virtual datapoint in the given channel.
     */
    public HmDatapoint getVirtualDatapoint(HmChannel channel);
}
