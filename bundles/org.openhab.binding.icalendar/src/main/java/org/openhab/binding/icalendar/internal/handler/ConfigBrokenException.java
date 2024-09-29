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
package org.openhab.binding.icalendar.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception or semantically describe configuration errors. Message is meant to be shown to the user.
 *
 * @author Michael Wodniok - Initial contribution
 */
@NonNullByDefault
public class ConfigBrokenException extends Exception {
    private static final long serialVersionUID = -3805312008429711152L;

    public ConfigBrokenException(String message) {
        super(message);
    }
}
