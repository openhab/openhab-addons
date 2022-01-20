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
package org.openhab.binding.icalendar.internal.logic;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception Class to encapsulate Exception data for binding.
 *
 * @author Michael Wodniok - Initial contribution
 */
@NonNullByDefault
public class CalendarException extends Exception {

    private static final long serialVersionUID = -2071400154241449096L;

    public CalendarException(String message) {
        super(message);
    }

    public CalendarException(String message, Exception source) {
        super(message, source);
    }

    public CalendarException(Exception source) {
        super("Implementation specific exception occurred", source);
    }
}
