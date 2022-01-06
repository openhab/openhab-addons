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
package org.openhab.binding.fmiweather.internal.client.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Base class for FMI exceptions
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class FMIResponseException extends Exception {

    private static final long serialVersionUID = 938534003881018793L;

    public FMIResponseException(String message) {
        super(message);
    }

    public FMIResponseException(Throwable e) {
        super(e);
    }
}
