/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.unifi.internal.api;

/**
 * The {@link UniFiInvalidHostException} signals there was a problem with the hostname of the controller.
 *
 * @author Matthew Bowman - Initial contribution
 */
public class UniFiInvalidHostException extends UniFiException {

    private static final long serialVersionUID = -7261308872245069364L;

    public UniFiInvalidHostException(String message) {
        super(message);
    }

    public UniFiInvalidHostException(String message, Throwable cause) {
        super(message, cause);
    }

    public UniFiInvalidHostException(Throwable cause) {
        super(cause);
    }

}
