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
package org.openhab.binding.pjlinkdevice.internal.device.command;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception thrown whenever an error code or unexpected response is retrieved from the device.
 *
 * @author Nils Schnabel - Initial contribution
 */
@NonNullByDefault
public class CacheException extends RuntimeException {
    private static final long serialVersionUID = -3319800607314286998L;

    public CacheException(Throwable cause) {
        super(cause);
    }
}
