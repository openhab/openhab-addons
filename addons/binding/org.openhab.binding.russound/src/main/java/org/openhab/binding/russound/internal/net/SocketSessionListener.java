/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.internal.net;

import java.io.IOException;

/**
 * Interface defining a listener to a {@link SocketSession} that will receive responses and/or exceptions from the
 * socket
 *
 * @author Tim Roberts - Initial contribution
 */
public interface SocketSessionListener {
    /**
     * Called when a command has completed with the response for the command
     *
     * @param response a non-null, possibly empty response
     * @throws InterruptedException if the response processing was interrupted
     */
    public void responseReceived(String response) throws InterruptedException;

    /**
     * Called when a command finished with an exception or a general exception occurred while reading
     *
     * @param e a non-null io exception
     * @throws InterruptedException if the exception processing was interrupted
     */
    public void responseException(IOException e) throws InterruptedException;
}
