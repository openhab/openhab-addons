/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
