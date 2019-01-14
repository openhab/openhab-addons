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
 * The {@link UniFiExpiredSessionException} signals the session with the controller has expired.
 *
 * @author Matthew Bowman - Initial contribution
 */
public class UniFiExpiredSessionException extends UniFiException {

    private static final long serialVersionUID = -2002650048964514035L;

    public UniFiExpiredSessionException(String message) {
        super(message);
    }

}
