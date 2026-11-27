/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.enphase.internal.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception thrown when the Envoy gateway reports that an endpoint doesn't exist. Unlike other connection problems this
 * is not transient, and means the feature served by the endpoint is not supported by the Envoy.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
public class EnvoyUnsupportedEndpointException extends EnvoyConnectionException {

    private static final long serialVersionUID = 1L;

    public EnvoyUnsupportedEndpointException(final String message) {
        super(message);
    }
}
