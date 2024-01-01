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
package org.openhab.binding.max.internal.message;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SMessage} contains information about Command execution results
 *
 * @author Bernd Michael Helm (bernd.helm at helmundwalter.de) - Initial contribution
 * @author Marcel Verpaalen - OH2 version + parsing of the message
 */
@NonNullByDefault
public final class SMessage extends Message {
    private final Logger logger = LoggerFactory.getLogger(SMessage.class);

    private int dutyCycle;
    private int freeMemorySlots;
    private boolean commandDiscarded = false;

    public SMessage(String raw) {
        super(raw);

        String[] tokens = this.getPayload().split(Message.DELIMETER);

        if (tokens.length == 3) {
            try {
                dutyCycle = Integer.parseInt(tokens[0], 16);
                commandDiscarded = tokens[1].contentEquals("1");
                freeMemorySlots = Integer.parseInt(tokens[2], 16);
            } catch (Exception e) {
                logger.debug("Exception occurred during parsing of S message: {}", e.getMessage(), e);
            }
        } else {
            logger.debug("Unexpected # of tolkens ({}) received in S message: {}", tokens.length, this.getPayload());
        }
    }

    public int getDutyCycle() {
        return dutyCycle;
    }

    public int getFreeMemorySlots() {
        return freeMemorySlots;
    }

    public boolean isCommandDiscarded() {
        return commandDiscarded;
    }

    @Override
    public void debug(Logger logger) {
        logger.trace("=== S Message === ");
        logger.trace("\tRAW : {}", this.getPayload());
        logger.trace("\tDutyCycle         : {}", this.dutyCycle);
        logger.trace("\tCommand Discarded : {}", this.commandDiscarded);
        logger.trace("\tFreeMemorySlots   : {}", this.freeMemorySlots);
    }

    @Override
    public MessageType getType() {
        return MessageType.S;
    }
}
