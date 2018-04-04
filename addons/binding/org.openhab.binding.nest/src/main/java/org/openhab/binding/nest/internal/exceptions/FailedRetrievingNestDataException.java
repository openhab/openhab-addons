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
 * Will be thrown when the bridge was unable to retrieve data.
 *
 * @author Martin van Wingerden - Added more centralized handling of failure when retrieving data
 */
public class FailedRetrievingNestDataException extends Exception {
    public FailedRetrievingNestDataException(String message) {
        super(message);
    }

    public FailedRetrievingNestDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailedRetrievingNestDataException(Throwable cause) {
        super(cause);
    }
}
