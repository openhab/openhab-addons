/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.communicator.server;

import java.util.List;

import org.openhab.binding.homematic.internal.model.HmDatapointInfo;

/**
 * Methods called by the RpcServer when a event is received.
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
