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

import java.io.IOException;

/**
 * Simple RPC server interface.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public interface RpcServer {

    /**
     * Starts the rpc server.
     */
    void start() throws IOException;

    /**
     * Stops the rpc server.
     */
    void shutdown();
}
