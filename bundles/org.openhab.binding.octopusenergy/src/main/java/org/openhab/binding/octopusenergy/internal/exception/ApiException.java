/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.octopusenergy.internal.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ApiException} is thrown during API interactions.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class ApiException extends OctopusEnergyException {

    private static final long serialVersionUID = -7851429815604230535L;

    public ApiException() {
        super();
    }

    public ApiException(String message) {
        super(message);
    }

    public ApiException(Throwable cause) {
        super(cause);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
