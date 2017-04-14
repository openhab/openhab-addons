/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bosesoundtouch.types;

/**
 * The {@link OperationModeNotAvailableException} class handles all nowPlaying Channels
 *
 * @author Thomas Traunbauer
 */
public class OperationModeNotAvailableException extends Exception {
    private static final long serialVersionUID = 1L;

    public OperationModeNotAvailableException() {
        super();
    }

    public OperationModeNotAvailableException(String message) {
        super(message);
    }

    public OperationModeNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public OperationModeNotAvailableException(Throwable cause) {
        super(cause);
    }
}