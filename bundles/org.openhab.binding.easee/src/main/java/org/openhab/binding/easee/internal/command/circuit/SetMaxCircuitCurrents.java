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
package org.openhab.binding.easee.internal.command.circuit;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.easee.internal.command.JsonResultProcessor;
import org.openhab.binding.easee.internal.handler.EaseeThingHandler;
import org.openhab.binding.easee.internal.model.ValidationException;
import org.openhab.core.thing.Channel;
import org.openhab.core.types.Command;

/**
 * implements the settings api call of the circuit.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class SetMaxCircuitCurrents extends SetCircuitSettings {
    private static final String PHASE1 = "maxCircuitCurrentP1";
    private static final String PHASE2 = "maxCircuitCurrentP2";
    private static final String PHASE3 = "maxCircuitCurrentP3";

    public SetMaxCircuitCurrents(EaseeThingHandler handler, Channel channel, Command command, String circuitId,
            JsonResultProcessor resultProcessor) {
        super(handler, channel, command, circuitId, resultProcessor);
    }

    /**
     * helper that transforms channelId + commandvalue in a JSON string that can be added as content to a POST request.
     *
     * @return converted JSON string
     * @throws ValidationException
     */
    @Override
    protected String getJsonContent() throws ValidationException {
        Map<String, String> content = new HashMap<>(3);
        String rawCommand = getCommandValue();
        String[] tokens = rawCommand.split(";");
        if (tokens.length == 3) {
            content.put(PHASE1, tokens[0]);
            content.put(PHASE2, tokens[1]);
            content.put(PHASE3, tokens[2]);
        } else {
            throw new ValidationException(
                    "malformed command string, expected: '<phase1>;<phase2>;<phase3>', actual: " + rawCommand);
        }
        return gson.toJson(content);
    }
}
