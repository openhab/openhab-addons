/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.seneye.internal;

/**
 * There was an error while communicating with the seneye API.
 * This will be mostly temporary communication errors.
 *
 * @author Niko Tanghe - Initial contribution
 */

public class CommunicationException extends Exception {
    private static final long serialVersionUID = -1397248504578142737L;

    public CommunicationException(String message) {
        super(message);
    }

    public CommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
