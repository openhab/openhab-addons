/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
 * The {@link HubMaintenanceException} is a custom exception for the HD PowerView hub
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class HubMaintenanceException extends HubException {

    private static final long serialVersionUID = -708582495003057343L;

    public HubMaintenanceException(String message) {
        super(message);
    }
}
