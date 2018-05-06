/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gardena.internal.exception;

import java.io.IOException;

/**
 * Exception if something happens in the communication to Gardena Smart Home.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class GardenaException extends IOException {

    private static final long serialVersionUID = 8568935118878542270L;

    public GardenaException(String message) {
        super(message);
    }

    public GardenaException(Throwable ex) {
        super(ex);
    }

    public GardenaException(String message, Throwable cause) {
        super(message, cause);
    }
}
