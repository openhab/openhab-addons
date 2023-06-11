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
package org.openhab.binding.regoheatpump.internal.rego6xx;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Rego6xxProtocolException} is responsible for holding information about a Rego6xx protocol error.
 *
 * @author Boris Krivonog - Initial contribution
 */
@NonNullByDefault
public class Rego6xxProtocolException extends Exception {

    private static final long serialVersionUID = 7556083982084149686L;

    public Rego6xxProtocolException(String message) {
        super(message);
    }
}
