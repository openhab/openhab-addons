/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.onecta.internal.exception;

/**
 * @author Alexander Drent - Initial contribution
 */
public class DaikinCommunicationForbiddenException extends DaikinCommunicationException {

    public DaikinCommunicationForbiddenException(String message) {
        super(message);
    }

    public DaikinCommunicationForbiddenException(Throwable ex) {
        super(ex);
    }

    public DaikinCommunicationForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
