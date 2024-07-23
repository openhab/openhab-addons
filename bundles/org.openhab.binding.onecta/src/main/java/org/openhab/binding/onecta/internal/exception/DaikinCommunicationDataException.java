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
package org.openhab.binding.onecta.internal.exception;

/**
 * @author Alexander Drent - Initial contribution
 */
public class DaikinCommunicationDataException extends DaikinCommunicationException {

    public DaikinCommunicationDataException(String message) {
        super(message);
    }

    public DaikinCommunicationDataException(Throwable ex) {
        super(ex);
    }

    public DaikinCommunicationDataException(String message, Throwable cause) {
        super(message, cause);
    }
}