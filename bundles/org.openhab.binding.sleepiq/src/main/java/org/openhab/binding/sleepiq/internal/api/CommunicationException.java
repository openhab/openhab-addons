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
package org.openhab.binding.sleepiq.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link CommunicationException} is thrown when there's an error communicating with
 * the sleepiq service.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class CommunicationException extends SleepIQException {
    private static final long serialVersionUID = 1L;

    public CommunicationException(String message) {
        super(message);
    }
}
