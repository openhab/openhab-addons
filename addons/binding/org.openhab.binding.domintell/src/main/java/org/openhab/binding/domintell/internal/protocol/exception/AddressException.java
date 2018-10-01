/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.domintell.internal.protocol.exception;

/**
* The {@link AddressException} class handles exceptions for incorrectly formatted Domintell addresses
*
* @author Gabor Bicskei - Initial contribution
*/
public class AddressException extends RuntimeException {
    /**
     * Constructs a new exception instance with a given message and the string representation
     * of a Domintell address that could not be parsed.
     *  @param message        exception message
     *
     */
    public AddressException(String message) {
        super(message);
    }
}
