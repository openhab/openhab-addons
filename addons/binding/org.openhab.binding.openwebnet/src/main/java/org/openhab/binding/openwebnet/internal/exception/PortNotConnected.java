/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openwebnet.internal.exception;

import java.io.IOException;

/**
 * Exception throw when a write attempt is done on an unconnected port
 *
 * @author Antoine Laydier
 *
 */
public class PortNotConnected extends IOException {

    private static final long serialVersionUID = 3980733123301461646L;

    /**
     * Constructs an {@code PortNotConnected} with the specified detail message.
     *
     * @param message
     *            The detail message (which is saved for later retrieval
     *            by the {@link #getMessage()} method)
     */
    public PortNotConnected(String message) {
        super(message);
    }
}
