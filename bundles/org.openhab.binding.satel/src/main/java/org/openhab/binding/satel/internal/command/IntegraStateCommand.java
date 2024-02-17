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
package org.openhab.binding.satel.internal.command;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.satel.internal.event.EventDispatcher;
import org.openhab.binding.satel.internal.event.IntegraStateEvent;
import org.openhab.binding.satel.internal.protocol.SatelMessage;
import org.openhab.binding.satel.internal.types.StateType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command class for commands that return state of Integra objects, like
 * partitions (armed, alarm, entry time), zones (violation, tamper, alarm),
 * outputs and doors (opened, opened long).
 *
 * @author Krzysztof Goworek - Initial contribution
 */
@NonNullByDefault
public class IntegraStateCommand extends SatelCommandBase {

    private final Logger logger = LoggerFactory.getLogger(IntegraStateCommand.class);

    private StateType stateType;

    /**
     * Constructs new command instance for specified type of state.
     *
     * @param stateType type of state
     * @param extended if <code>true</code> command will be sent as extended (256 zones or outputs)
     */
    public IntegraStateCommand(StateType stateType, boolean extended) {
        super(stateType.getRefreshCommand(), extended);
        this.stateType = stateType;
    }

    /**
     * @return <code>true</code> if current command is extended (256 zones/outputs)
     */
    public boolean isExtended() {
        return Arrays.equals(EXTENDED_CMD_PAYLOAD, this.getPayload());
    }

    @Override
    protected boolean isResponseValid(SatelMessage response) {
        // validate response
        if (response.getPayload().length != this.stateType.getPayloadLength(isExtended())) {
            logger.debug("Invalid payload length for state type {}: {}", this.stateType, response.getPayload().length);
            return false;
        }
        return true;
    }

    @Override
    protected void handleResponseInternal(final EventDispatcher eventDispatcher) {
        // dispatch event
        eventDispatcher.dispatchEvent(
                new IntegraStateEvent(getResponse().getCommand(), getResponse().getPayload(), isExtended()));
    }
}
