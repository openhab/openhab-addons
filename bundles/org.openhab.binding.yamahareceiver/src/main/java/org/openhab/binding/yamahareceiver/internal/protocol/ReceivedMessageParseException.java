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
package org.openhab.binding.yamahareceiver.internal.protocol;

/**
 * An exception that is thrown if parsing of the received XML or JSON failed or
 * if data that was expected could not be found in the response.
 *
 * @author David Graeff - Initial contribution
 */
public class ReceivedMessageParseException extends Exception {

    private static final long serialVersionUID = 2703218443322787635L;

    /**
     * Constructs a ReceivedMessageParseException with the specified detail message.
     * A detail message is a String that describes this particular exception.
     *
     * @param s the detail message
     */
    public ReceivedMessageParseException(String s) {
        super(s);
    }

    public ReceivedMessageParseException(Exception e) {
        super(e);
    }
}
