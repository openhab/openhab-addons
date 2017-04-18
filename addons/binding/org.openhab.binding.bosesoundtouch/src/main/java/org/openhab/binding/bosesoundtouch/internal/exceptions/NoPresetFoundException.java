/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bosesoundtouch.internal.exceptions;

/**
 * The {@link NoPresetFoundException} class handles all nowPlaying Channels
 *
 * @author Thomas Traunbauer
 */
public class NoPresetFoundException extends Exception {
    private static final long serialVersionUID = 1L;

    public NoPresetFoundException() {
        super();
    }

    public NoPresetFoundException(String message) {
        super(message);
    }

    public NoPresetFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoPresetFoundException(Throwable cause) {
        super(cause);
    }
}