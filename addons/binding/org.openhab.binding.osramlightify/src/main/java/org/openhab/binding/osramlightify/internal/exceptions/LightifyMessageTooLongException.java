/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.osramlightify.internal.exceptions;

/**
 * Exception for when Lightify messages are too long for the spec.
 *
 * @author Mike Jagdis - Initial contribution
 */
public final class LightifyMessageTooLongException extends LightifyException {

    private static final long serialVersionUID = 1L;

    public LightifyMessageTooLongException() {
        super();
    }

    public LightifyMessageTooLongException(String message) {
        super(message);
    }

    public LightifyMessageTooLongException(String message, Throwable cause) {
        super(message, cause);
    }

    public LightifyMessageTooLongException(Throwable cause) {
        super(cause);
    }

}
