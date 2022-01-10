/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.atlona.internal.net;

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
     */
    public void responseReceived(String response);

    /**
     * Called when a command finished with an exception or a general exception occurred while reading
     *
     * @param e a non-null exception
     */
    public void responseException(Exception e);
}
