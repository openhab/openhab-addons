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
 * The {@link UniFiInvalidCredentialsException} signals the credentials used to authenticate with the controller are
 * invalid.
 *
 * @author Matthew Bowman - Initial contribution
 */
public class UniFiInvalidCredentialsException extends UniFiException {

    private static final long serialVersionUID = -7159360851783088458L;

    public UniFiInvalidCredentialsException(String message) {
        super(message);
    }

}
