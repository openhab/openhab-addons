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
 * General exception class of the Broadlink binding
 *
 * @author Anton Jansen - Initial contribution
 */
@NonNullByDefault
public class BroadlinkException extends Exception {

    private static final long serialVersionUID = 1L;

    public BroadlinkException(String message) {
        super(message);
    }

    public BroadlinkException(String message, Exception e) {
        super(message, e);
    }
}
