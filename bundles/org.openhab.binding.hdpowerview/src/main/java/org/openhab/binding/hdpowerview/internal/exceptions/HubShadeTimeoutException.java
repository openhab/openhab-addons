/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.hdpowerview.internal.exceptions;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link HubShadeTimeoutException} is a custom exception for the HD PowerView Hub
 * which is thrown when a shade does not respond to a request.
 *
 * @author @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class HubShadeTimeoutException extends HubException {

    private static final long serialVersionUID = -362347489903471011L;

    public HubShadeTimeoutException(String message) {
        super(message);
    }
}
