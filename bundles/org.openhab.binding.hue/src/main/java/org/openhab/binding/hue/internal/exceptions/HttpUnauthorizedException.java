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
package org.openhab.binding.hue.internal.exceptions;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Thrown when an HTTP call to the CLIP 2 bridge returns with an 'unauthorized' status code.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class HttpUnauthorizedException extends ApiException {
    private static final long serialVersionUID = -1;

    public HttpUnauthorizedException() {
    }

    public HttpUnauthorizedException(String message) {
        super(message);
    }
}
