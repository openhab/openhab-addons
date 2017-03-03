/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.communicator.message;

/**
 * A RPC request definition for sending data to the Homematic server.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public interface RpcRequest {

    /**
     * Adds arguments to the RPC method.
     */
    public void addArg(Object arg);

    /**
     * Generates the RPC data.
     */
    public byte[] createMessage();

    /**
     * Returns the name of the rpc method.
     */
    public String getMethodName();
}
