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
package org.openhab.binding.elerotransmitterstick.internal.stick;

/**
 * The {@link ConnectException} is thrown on errors connecting to the elero transmitter stick.
 *
 * @author Volker Bier - Initial contribution
 */
public class ConnectException extends Exception {
    private static final long serialVersionUID = 946529257121090885L;

    public ConnectException(Throwable cause) {
        super(cause);
    }

    public ConnectException(String message) {
        super(message);
    }

    public ConnectException(String message, Exception e) {
        super(message, e);
    }
}
