/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.toon.internal.api;

/**
 * The {@link ToonConnectionException} class indicates communication issues encountered by the api.
 *
 * @author Jorg de Jong - Initial contribution
 */
public class ToonConnectionException extends Exception {

    private static final long serialVersionUID = -6854850123941151190L;

    public ToonConnectionException(String message) {
        super(message);
    }

    public ToonConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
