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
 * The {@link BoseSoundTouchNotFoundException} class handles all nowPlaying Channels
 *
 * @author Thomas Traunbauer
 */
public class BoseSoundTouchNotFoundException extends Exception {
    private static final long serialVersionUID = 1L;

    public BoseSoundTouchNotFoundException() {
        super();
    }

    public BoseSoundTouchNotFoundException(String message) {
        super(message);
    }

    public BoseSoundTouchNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public BoseSoundTouchNotFoundException(Throwable cause) {
        super(cause);
    }
}