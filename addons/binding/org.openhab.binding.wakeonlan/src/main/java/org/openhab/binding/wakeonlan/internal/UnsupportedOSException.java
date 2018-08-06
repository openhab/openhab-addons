/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * @author Ganesh Ingle <ganesh.ingle@asvilabs.com>
 */

package org.openhab.binding.wakeonlan.internal;

/**
 * Exception used by ping helper function when it doesn't know ping command syntax on running OS
 *
 * @author Ganesh Ingle - Initial contribution
 *
 */
public class UnsupportedOSException extends Exception {

    private static final long serialVersionUID = 1L;

    public UnsupportedOSException(String msg) {
        super(msg);
    }

    public UnsupportedOSException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public UnsupportedOSException(Throwable cause) {
        super(cause);
    }

    protected UnsupportedOSException() {
    }
}
