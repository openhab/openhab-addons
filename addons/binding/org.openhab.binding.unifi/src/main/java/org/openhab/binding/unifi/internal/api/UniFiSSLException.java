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
 * The {@link UniFiSSLException} signals a failure establishing an SSL connection with the controller.
 *
 * @author Matthew Bowman - Initial contribution
 */
public class UniFiSSLException extends UniFiException {

    private static final long serialVersionUID = 4688857482270932413L;

    public UniFiSSLException(String message) {
        super(message);
    }

    public UniFiSSLException(String message, Throwable cause) {
        super(message, cause);
    }

    public UniFiSSLException(Throwable cause) {
        super(cause);
    }

}
