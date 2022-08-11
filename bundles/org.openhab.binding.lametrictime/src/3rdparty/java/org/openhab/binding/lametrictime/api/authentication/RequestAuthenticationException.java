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
package org.openhab.binding.lametrictime.api.authentication;

import javax.ws.rs.ProcessingException;

/**
 * Exception thrown by security request authentication.
 *
 * @author Petr Bouda (petr.bouda at oracle.com)
 */
public class RequestAuthenticationException extends ProcessingException {

    /**
     * Creates new instance of this exception with exception cause.
     *
     * @param cause Exception cause.
     */
    public RequestAuthenticationException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates new instance of this exception with exception message.
     *
     * @param message Exception message.
     */
    public RequestAuthenticationException(String message) {
        super(message);
    }

    /**
     * Creates new instance of this exception with exception message and exception cause.
     *
     * @param message Exception message.
     * @param cause Exception cause.
     */
    public RequestAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

}
