/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.broadlink.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception to handle authentication issues.
 *
 * @author Anton Jansen - Initial contribution
 */
@NonNullByDefault
public class BroadlinkAuthenticationException extends BroadlinkException {

    private static final long serialVersionUID = 6332210773192650617L;

    public BroadlinkAuthenticationException(String message) {
        super(message);
    }

    public BroadlinkAuthenticationException(String message, Exception e) {
        super(message, e);
    }
}
