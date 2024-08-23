/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.gce.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This interface defines interface to receive data from IPX800 controller.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public interface Ipx800EventListener {

    /**
     * Procedure for receive data from IPX800 controller.
     *
     * @param port Port (kind and number) receiving update
     * @param value value updated
     */
    void dataReceived(String port, double value);

    /**
     * Procedure for receiving information fatal error.
     *
     * @param e Error occurred.
     */
    void errorOccurred(Exception e);
}
