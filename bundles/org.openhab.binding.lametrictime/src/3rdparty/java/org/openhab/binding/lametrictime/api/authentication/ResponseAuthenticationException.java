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

import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.Response;

/**
 * Exception thrown by security response authentication.
 *
 * @author Petr Bouda (petr.bouda at oracle.com)
 */
public class ResponseAuthenticationException extends ResponseProcessingException {

    /**
     * Creates new instance of this exception with exception cause.
     *
     * @param response the response instance for which the processing failed.
     * @param cause Exception cause.
     */
    public ResponseAuthenticationException(Response response, Throwable cause) {
        super(response, cause);
    }

    /**
     * Creates new instance of this exception with exception message.
     *
     * @param response the response instance for which the processing failed.
     * @param message Exception message.
     */
    public ResponseAuthenticationException(Response response, String message) {
        super(response, message);
    }

    /**
     * Creates new instance of this exception with exception message and exception cause.
     *
     * @param response the response instance for which the processing failed.
     * @param message Exception message.
     * @param cause Exception cause.
     */
    public ResponseAuthenticationException(Response response, String message, Throwable cause) {
        super(response, message, cause);
    }

}
