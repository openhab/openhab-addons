/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.atlona.internal.net;

/**
 * Interface defining a listener to a {@link SocketSession} that will receive responses and/or exceptions from the
 * socket
 *
 * @author Tim Roberts
 */
public interface SocketSessionListener {
    /**
     * Called when a command has completed with the response for the command
     *
     * @param response a non-null, possibly empty response
     */
    public void responseReceived(String response);

    /**
     * Called when a command finished with an exception or a general exception occurred while reading
     *
     * @param e a non-null exception
     */
    public void responseException(Exception e);
}
