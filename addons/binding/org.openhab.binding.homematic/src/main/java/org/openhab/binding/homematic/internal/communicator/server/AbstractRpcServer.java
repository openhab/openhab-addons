/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.communicator.server;

import static org.openhab.binding.homematic.internal.misc.HomematicConstants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.homematic.internal.communicator.parser.DeleteDevicesParser;
import org.openhab.binding.homematic.internal.communicator.parser.EventParser;
import org.openhab.binding.homematic.internal.communicator.parser.NewDevicesParser;
import org.openhab.binding.homematic.internal.model.HmDatapointInfo;

/**
 * Common RPC server methods.
 *
 * @author Gerhard Riegler - Initial contribution
 */

public abstract class AbstractRpcServer implements RpcServer {
    private RpcEventListener listener;

    public AbstractRpcServer(RpcEventListener listener) {
        this.listener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getListMethods() {
        List<String> events = new ArrayList<String>();
        events.add(RPC_METHODNAME_SYSTEM_MULTICALL);
        events.add(RPC_METHODNAME_EVENT);
        events.add(RPC_METHODNAME_DELETE_DEVICES);
        events.add(RPC_METHODNAME_NEW_DEVICES);
        return events;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleEvent(Object[] message) throws IOException {
        EventParser eventParser = new EventParser();
        HmDatapointInfo dpInfo = eventParser.parse(message);
        listener.eventReceived(dpInfo, eventParser.getValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleNewDevice(Object[] message) throws IOException {
        NewDevicesParser ndParser = new NewDevicesParser();
        List<String> adresses = ndParser.parse(message);
        listener.newDevices(adresses);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleDeleteDevice(Object[] message) throws IOException {
        DeleteDevicesParser ddParser = new DeleteDevicesParser();
        List<String> adresses = ddParser.parse(message);
        listener.deleteDevices(adresses);
    }

}
