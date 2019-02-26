/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
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
