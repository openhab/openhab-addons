/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.communicator.virtual;

import java.io.IOException;

import org.openhab.binding.homematic.internal.misc.HomematicClientException;
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
     * Adds the virtual datapoint to the device.
     */
    public void add(HmDevice device);

    /**
     * Returns true, if the virtual datapoint can handle the given datapoint.
     */
    public boolean canHandle(HmDatapoint dp, Object value);

    /**
     * Handles the special functionality for the given virtual datapoint.
     */
    public void handle(VirtualGateway gateway, HmDatapoint dp, HmDatapointConfig dpConfig, Object value)
            throws IOException, HomematicClientException;

}
