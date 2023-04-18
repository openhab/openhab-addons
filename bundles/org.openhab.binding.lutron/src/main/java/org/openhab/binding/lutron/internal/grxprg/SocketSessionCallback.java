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
package org.openhab.binding.lutron.internal.grxprg;

/**
 * Interface defining a callback from {@link SocketSession} when a response was received (or an exception occurred)
 *
 * @author Tim Roberts - Initial contribution
 */
public interface SocketSessionCallback {
    /**
     * Called when a command has completed with the response for the command
     *
     * @param response a non-null, possibly empty response
     */
    void responseReceived(String response);

    /**
     * Called when a command finished with an exception
     *
     * @param e a non-null exception
     */
    void responseException(Exception e);
}
