/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neato.internal;

/**
 * Exception to encapsulate any issues communicating with Neato APIs
 *
 * @author Jeff Lauterbach - Initial Contribution
 */
public class NeatoCommunicationException extends Exception {

    public NeatoCommunicationException(Throwable cause) {
        super("Error attempting to communicate with Neato", cause);
    }

    public NeatoCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }

}
