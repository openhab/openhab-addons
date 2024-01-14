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
package org.openhab.binding.sleepiq.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SleepIQException} is the base exception class from which other
 * sleepiq exceptions are derived.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class SleepIQException extends Exception {
    private static final long serialVersionUID = 1L;

    public SleepIQException(String message) {
        super(message);
    }
}
