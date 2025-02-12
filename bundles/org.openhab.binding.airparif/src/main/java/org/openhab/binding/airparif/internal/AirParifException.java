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
package org.openhab.binding.airparif.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * An exception that occurred while communicating with AirParif API server or related processes.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class AirParifException extends Exception {
    private static final long serialVersionUID = 4234683995736417341L;

    public AirParifException(String format, Object... args) {
        super(format.formatted(args));
    }

    public AirParifException(Exception e, String format, Object... args) {
        super(format.formatted(args), e);
    }
}
