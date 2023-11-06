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
 * The {@link AMessage} Acknowledge the execution of a command
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public final class AMessage extends Message {

    public AMessage(String raw) {
        super(raw);
    }

    @Override
    public void debug(Logger logger) {
        logger.trace("=== A Message === ");
        logger.trace("\tRAW : {}", this.getPayload());
        logger.debug("Cube Command Acknowledged");
    }

    @Override
    public MessageType getType() {
        return MessageType.A;
    }
}
