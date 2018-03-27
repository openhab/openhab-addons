/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.internal.message;

import org.openhab.binding.max.MaxBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link A_Message} Acknowledge the execution of a command
 *
 * @author Marcel Verpaalen - initial version
 * @since 2.0.0
 */
public final class A_Message extends Message {

    public A_Message(String raw) {
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
