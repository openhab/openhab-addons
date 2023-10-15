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
package org.openhab.binding.easee.internal.command;

import static org.openhab.binding.easee.internal.EaseeBindingConstants.CHANNEL_GROUP_NONE;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.Request;
import org.openhab.binding.easee.internal.Utils;
import org.openhab.binding.easee.internal.handler.EaseeThingHandler;
import org.openhab.binding.easee.internal.model.ValidationException;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Channel;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * base class for all write commands. common logic should be implemented here
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public abstract class AbstractWriteCommand extends AbstractCommand {
    private final Logger logger = LoggerFactory.getLogger(AbstractWriteCommand.class);

    protected final Channel channel;
    protected Command command;

    /**
     * the constructor
     */
    public AbstractWriteCommand(EaseeThingHandler handler, Channel channel, Command command,
            RetryOnFailure retryOnFailure, ProcessFailureResponse processFailureResponse,
            JsonResultProcessor resultProcessor) {
        super(handler, retryOnFailure, processFailureResponse, resultProcessor);
        this.channel = channel;
        this.command = command;
    }

    /**
     * helper method for write commands that extracts value from command.
     *
     * @return value as String without unit.
     */
    protected String getCommandValue() {
        if (command instanceof QuantityType<?> quantityCommand) {
            // this is necessary because we must not send the unit to the backend
            return String.valueOf(quantityCommand.doubleValue());
        } else if (command instanceof OnOffType) {
            // this is necessary because we must send booleans and not ON/OFF to the backend
            return String.valueOf(command.equals(OnOffType.ON));
        } else {
            return command.toString();
        }
    }

    /**
     * helper that transforms channelId + commandvalue in a JSON string that can be added as content to a POST request.
     *
     * @return converted JSON string
     * @throws ValidationException
     */
    protected String getJsonContent() throws ValidationException {
        Map<String, String> content = new HashMap<String, String>(1);
        content.put(channel.getUID().getIdWithoutGroup(), getCommandValue());

        return gson.toJson(content);
    }

    @Override
    protected Request prepareRequest(Request requestToPrepare) throws ValidationException {
        String channelId = channel.getUID().getIdWithoutGroup();
        String expr = Utils.getValidationExpression(channel);
        String value = getCommandValue();

        // quantity types are transformed to double and thus we might have decimals which could cause validation error.
        // So we will shorten here in case no decimals are needed.
        if (value.endsWith(".0")) {
            value = value.substring(0, value.length() - 2);
        }

        if (value.matches(expr)) {
            return prepareWriteRequest(requestToPrepare);
        } else {
            logger.info("channel '{}' does not allow value '{}' - validation rule '{}'", channelId, value, expr);
            throw new ValidationException("channel (" + channelId + ") could not be updated due to a validation error");
        }
    }

    @Override
    protected String getChannelGroup() {
        // this is a pure write command, thus no channel group needed.
        return CHANNEL_GROUP_NONE;
    }

    /**
     * concrete implementation has to prepare the write requests with additional parameters, etc
     *
     * @param requestToPrepare the request to prepare
     * @return prepared Request object
     * @throws ValidationException
     */
    protected abstract Request prepareWriteRequest(Request requestToPrepare) throws ValidationException;
}
