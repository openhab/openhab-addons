/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.satel.internal.command;

import org.openhab.binding.satel.internal.event.EventDispatcher;
import org.openhab.binding.satel.internal.event.NewStatesEvent;
import org.openhab.binding.satel.internal.protocol.SatelMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command class for command that returns list of states changed since last
 * state read.
 *
 * @author Krzysztof Goworek - Initial contribution
 * @since 1.7.0
 */
public class NewStatesCommand extends SatelCommandBase {

    private final Logger logger = LoggerFactory.getLogger(NewStatesCommand.class);

    public static final byte COMMAND_CODE = 0x7f;

    /**
     * Creates new command class instance.
     *
     * @param extended
     *            if <code>true</code> command will be sent as extended (256
     *            zones or outputs)
     */
    public NewStatesCommand(boolean extended) {
        super(COMMAND_CODE, extended);
    }

    @Override
    public boolean handleResponse(EventDispatcher eventDispatcher, SatelMessage response) {
        if (super.handleResponse(eventDispatcher, response)) {
            // dispatch event
            eventDispatcher.dispatchEvent(new NewStatesEvent(response.getPayload()));
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean isResponseValid(SatelMessage response) {
        // validate response
        if (response.getCommand() != COMMAND_CODE) {
            logger.debug("Invalid response code: {}", response.getCommand());
            return false;
        }
        if (response.getPayload().length < 5 || response.getPayload().length > 6) {
            logger.debug("Invalid payload length: {}", response.getPayload().length);
            return false;
        }
        return true;
    }

}
