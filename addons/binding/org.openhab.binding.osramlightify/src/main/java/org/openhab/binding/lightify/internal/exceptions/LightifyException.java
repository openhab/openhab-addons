/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.osramlightify.internal.exceptions;

/**
 * Exception for Lightify errors.
 *
 * @author Mike Jagdis - Initial contribution
 */
public class LightifyException extends Exception {

    private static final long serialVersionUID = 1L;

    public LightifyException() {
        super();
    }

    public LightifyException(String message) {
        super(message);
    }

    public LightifyException(String message, Throwable cause) {
        super(message, cause);
    }

    public LightifyException(Throwable cause) {
        super(cause);
    }

}
