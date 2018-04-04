/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.internal.exceptions;

/**
 * Will be thrown when the bridge was unable to resolve the Nest redirect URL.
 *
 * @author Wouter Born - Improve exception handling while sending data
 */
public class FailedResolvingNestUrlException extends Exception {
    public FailedResolvingNestUrlException(String message) {
        super(message);
    }

    public FailedResolvingNestUrlException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailedResolvingNestUrlException(Throwable cause) {
        super(cause);
    }
}
