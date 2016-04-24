/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.communicator.server;

import java.io.IOException;

/**
 * Simple RPC server interface to start and stop the server.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public interface RpcServer {

    /**
     * Starts the rpc server.
     */
    public void start() throws IOException;

    /**
     * Stops the rpc server.
     */
    public void shutdown();

}
