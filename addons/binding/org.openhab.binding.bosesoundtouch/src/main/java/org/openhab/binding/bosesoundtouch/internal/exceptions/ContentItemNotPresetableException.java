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
 * The {@link ContentItemNotPresetableException} class handles all nowPlaying Channels
 *
 * @author Thomas Traunbauer
 */
public class ContentItemNotPresetableException extends NoPresetFoundException {
    private static final long serialVersionUID = 1L;

    public ContentItemNotPresetableException() {
        super();
    }

    public ContentItemNotPresetableException(String message) {
        super(message);
    }

    public ContentItemNotPresetableException(String message, Throwable cause) {
        super(message, cause);
    }

    public ContentItemNotPresetableException(Throwable cause) {
        super(cause);
    }
}