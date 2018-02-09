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
 * Exception thrown when calling LMS command line interface, and
 * the LMS is set up to require authentication.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class SqueezeBoxNotAuthorizedException extends Exception {

    private static final long serialVersionUID = -5190671725971757821L;

    public SqueezeBoxNotAuthorizedException(String message) {
        super(message);
    }

    public SqueezeBoxNotAuthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
