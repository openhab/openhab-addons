/**
 * Copyright (c) 2014-2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.amazonechocontrol.internal;

/**
 * The {@link ConnectionException} is used for errors in the connection to the amazon server
 *
 * @author Michael Geramb - Initial contribution
 */
public class ConnectionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ConnectionException(String message) {
        super(message);
    }
}
