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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.satel.internal.event.EventDispatcher;
import org.openhab.binding.satel.internal.event.NewStatesEvent;
import org.openhab.binding.satel.internal.protocol.SatelMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command class for command that returns list of states changed since last state read.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
@NonNullByDefault
public class NewStatesCommand extends SatelCommandBase {

    private final Logger logger = LoggerFactory.getLogger(NewStatesCommand.class);

    public static final byte COMMAND_CODE = 0x7f;

    /**
     * Creates new command class instance.
     *
     * @param extended if <code>true</code> command will be sent as extended (256 zones or outputs)
     */
    public NewStatesCommand(boolean extended) {
        super(COMMAND_CODE, extended);
    }

    @Override
    protected boolean isResponseValid(SatelMessage response) {
        // validate response
        if (response.getPayload().length < 5 || response.getPayload().length > 6) {
            logger.debug("Invalid payload length: {}", response.getPayload().length);
            return false;
        }
        return true;
    }

    @Override
    protected void handleResponseInternal(final EventDispatcher eventDispatcher) {
        // dispatch event
        eventDispatcher.dispatchEvent(new NewStatesEvent(getResponse().getPayload()));
    }
}
