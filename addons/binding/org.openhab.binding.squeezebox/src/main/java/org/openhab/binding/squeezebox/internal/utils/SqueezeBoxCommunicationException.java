/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.squeezebox.internal.utils;

/**
 * Exception thrown when unable to communicate with LMS server.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class SqueezeBoxCommunicationException extends Exception {
    private static final long serialVersionUID = 1540489268747099161L;

    public SqueezeBoxCommunicationException(String message) {
        super(message);
    }

    public SqueezeBoxCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
