/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.satel.internal.command;

import java.time.LocalDateTime;

import org.openhab.binding.satel.internal.event.EventDispatcher;
import org.openhab.binding.satel.internal.event.IntegraStatusEvent;
import org.openhab.binding.satel.internal.protocol.SatelMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command class for command that returns Integra RTC and basic status.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
public class IntegraStatusCommand extends SatelCommandBase {

    private final Logger logger = LoggerFactory.getLogger(IntegraStatusCommand.class);

    public static final byte COMMAND_CODE = 0x1a;

    /**
     * Creates new command class instance.
     */
    public IntegraStatusCommand() {
        super(COMMAND_CODE, false);
    }

    /**
     * @return date and time
     */
    public LocalDateTime getIntegraTime() {
        // parse current date and time
        return LocalDateTime.of(bcdToInt(response.getPayload(), 0, 2), bcdToInt(response.getPayload(), 2, 1),
                bcdToInt(response.getPayload(), 3, 1), bcdToInt(response.getPayload(), 4, 1),
                bcdToInt(response.getPayload(), 5, 1), bcdToInt(response.getPayload(), 6, 1));
    }

    /**
     * @return first status byte
     */
    public byte getStatusByte1() {
        return response.getPayload()[7];
    }

    /**
     * @return second status byte
     */
    public byte getStatusByte2() {
        return response.getPayload()[8];
    }

    @Override
    public boolean handleResponse(EventDispatcher eventDispatcher, SatelMessage response) {
        if (super.handleResponse(eventDispatcher, response)) {
            // dispatch version event
            eventDispatcher.dispatchEvent(new IntegraStatusEvent(getIntegraTime(), getStatusByte1(), getStatusByte2()));
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean isResponseValid(SatelMessage response) {
        if (response.getCommand() != COMMAND_CODE) {
            logger.debug("Invalid response code: {}", response.getCommand());
            return false;
        }
        if (response.getPayload().length != 9) {
            logger.debug("Invalid payload length: {}", response.getPayload().length);
            return false;
        }
        return true;
    }

}
