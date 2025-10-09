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
package org.openhab.binding.tidal.internal.api.exception;

/**
 * Generic Tidal exception class.
 *
 * @author Laurent Arnal - Initial contribution
 */
public class TidalException extends RuntimeException {

    private static final long serialVersionUID = -8142837343923954830L;

    /**
     * Constructor.
     *
     * @param message Tidal error message
     */
    public TidalException(String message) {
        super(message);
    }

    /**
     * Constructor.
     *
     * @param message Tidal error message
     * @param cause Original cause of this exception
     */
    public TidalException(String message, Throwable cause) {
        super(message, cause);
    }
}
