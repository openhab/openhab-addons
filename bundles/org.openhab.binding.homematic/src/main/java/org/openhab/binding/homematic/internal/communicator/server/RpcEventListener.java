/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.homematic.internal.communicator.server;

import java.util.List;

import org.openhab.binding.homematic.internal.model.HmDatapointInfo;

/**
 * Methods called by the RpcServer when an event is received.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public interface RpcEventListener {

    /**
     * Called when a new event is received from a Homeamtic gateway.
     */
    public void eventReceived(HmDatapointInfo dpInfo, Object newValue);

    /**
     * Called when new devices has been detected on the Homeamtic gateway.
     */
    public void newDevices(List<String> adresses);

    /**
     * Called when devices has been deleted from the Homeamtic gateway.
     */
    public void deleteDevices(List<String> addresses);
}
