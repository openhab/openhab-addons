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
package org.openhab.binding.intesis.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link MessageReceivedEvent} is an event container for data point changes
 *
 * @author Hans-Jörg Merk - Initial contribution
 */
@NonNullByDefault
public class MessageReceivedEvent {
    protected String message;

    public MessageReceivedEvent(Object source, String message) {
        this.message = message;
    }

    /**
     * Gets the data-point of the event.
     *
     */
    @Nullable
    public String getMessage() {
        return this.message;
    }
}
