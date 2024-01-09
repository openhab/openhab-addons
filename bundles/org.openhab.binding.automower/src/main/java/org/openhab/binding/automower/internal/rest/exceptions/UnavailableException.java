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
package org.openhab.binding.automower.internal.rest.exceptions;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Markus Pfleger - Initial contribution
 */
@NonNullByDefault
public class UnavailableException extends AutomowerCommunicationException {
    private static final long serialVersionUID = 1L;

    public UnavailableException(int statusCode, Exception e) {
        super(statusCode, e);
    }
}
