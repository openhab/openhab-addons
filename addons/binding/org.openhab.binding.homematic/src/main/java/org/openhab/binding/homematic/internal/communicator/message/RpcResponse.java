/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.communicator.message;

/**
 * A RPC response definition for reveiving data from the Homematic server.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public interface RpcResponse {

    /**
     * Returns the decoded methodName.
     */
    public String getMethodName();

    /**
     * Returns the decoded data.
     */
    public Object[] getResponseData();

}
