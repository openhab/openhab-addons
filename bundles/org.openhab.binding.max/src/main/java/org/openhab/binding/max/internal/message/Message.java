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
package org.openhab.binding.max.internal.message;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;

/**
 * Base message for the messages received from the MAX! Cube.
 *
 * @author Andreas Heil - Initial contribution
 */
@NonNullByDefault
public abstract class Message {

    public static final String DELIMETER = ",";

    private final String raw;

    public Message(String raw) {
        this.raw = raw;
    }

    public abstract void debug(Logger logger);

    public abstract MessageType getType();

    protected final String getPayload() {
        return raw.substring(2, raw.length());
    }
}
