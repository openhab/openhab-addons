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
package org.openhab.binding.satel.internal.command;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
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
@NonNullByDefault
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
    public Optional<LocalDateTime> getIntegraTime() {
        // parse current date and time
        try {
            final byte[] payload = getResponse().getPayload();
            return Optional
                    .of(LocalDateTime.of(bcdToInt(payload, 0, 2), bcdToInt(payload, 2, 1), bcdToInt(payload, 3, 1),
                            bcdToInt(payload, 4, 1), bcdToInt(payload, 5, 1), bcdToInt(payload, 6, 1)));
        } catch (DateTimeException e) {
            logger.debug("Invalid date/time set in the system", e);
            return Optional.empty();
        }
    }

    /**
     * @return first status byte
     */
    public byte getStatusByte1() {
        return getResponse().getPayload()[7];
    }

    /**
     * @return second status byte
     */
    public byte getStatusByte2() {
        return getResponse().getPayload()[8];
    }

    @Override
    protected boolean isResponseValid(SatelMessage response) {
        if (response.getPayload().length != 9) {
            logger.debug("Invalid payload length: {}", response.getPayload().length);
            return false;
        }
        return true;
    }

    @Override
    protected void handleResponseInternal(final EventDispatcher eventDispatcher) {
        // dispatch version event
        eventDispatcher.dispatchEvent(new IntegraStatusEvent(getIntegraTime(), getStatusByte1(), getStatusByte2()));
    }
}
