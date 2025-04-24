/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.wemo.internal.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link MissingHostException} is thrown when attempting to communicate
 * with a WeMo device while the host/IP address has not yet been obtained
 * through UPnP.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class MissingHostException extends WemoException {

    private static final long serialVersionUID = 1L;

    public MissingHostException(String message) {
        super(message);
    }
}
