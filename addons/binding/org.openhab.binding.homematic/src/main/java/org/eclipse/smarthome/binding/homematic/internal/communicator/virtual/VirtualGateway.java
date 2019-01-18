/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.homematic.internal.communicator.virtual;

import java.io.IOException;

import org.eclipse.smarthome.binding.homematic.internal.communicator.HomematicGateway;
import org.eclipse.smarthome.binding.homematic.internal.communicator.HomematicGatewayAdapter;
import org.eclipse.smarthome.binding.homematic.internal.communicator.client.RpcClient;
import org.eclipse.smarthome.binding.homematic.internal.misc.HomematicClientException;
import org.eclipse.smarthome.binding.homematic.internal.model.HmDatapoint;
import org.eclipse.smarthome.binding.homematic.internal.model.HmDatapointConfig;
import org.eclipse.smarthome.binding.homematic.internal.model.HmInterface;

/**
 * Extends the HomematicGateway with a method called from a virtual datapoint.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public interface VirtualGateway extends HomematicGateway {

    /**
     * Sends the datapoint from a virtual datapoint.
     */
    public void sendDatapointIgnoreVirtual(HmDatapoint dp, HmDatapointConfig dpConfig, Object newValue)
            throws IOException, HomematicClientException;

    /**
     * Returns the rpc client.
     */
    public RpcClient<?> getRpcClient(HmInterface hmInterface) throws IOException;

    /**
     * Disables a boolean datapoint by setting the value to false after a given delay.
     */
    public void disableDatapoint(HmDatapoint dp, double delay);

    /**
     * Returns the event listener.
     */
    public HomematicGatewayAdapter getGatewayAdapter();
}
