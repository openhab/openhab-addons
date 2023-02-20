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
 * Thrown when a DTO is present but empty. In some circumstances the API v2 returns an empty DTO ("dtoName":{}) rather
 * than null ("dtoName":null). This indicates that the DTO is in principle supported by the containing resource, but
 * currently the DTO contains no actual state fields.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class DTOPresentButEmptyException extends Exception {
    private static final long serialVersionUID = -1;

    public DTOPresentButEmptyException() {
    }

    public DTOPresentButEmptyException(String message) {
        super(message);
    }
}
