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
package org.openhab.binding.myuplink.internal.command;

import static org.openhab.binding.myuplink.internal.MyUplinkBindingConstants.EMPTY;

import java.util.HashMap;
import java.util.Map;

import javax.measure.MetricPrefix;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.Request;
import org.openhab.binding.myuplink.internal.Utils;
import org.openhab.binding.myuplink.internal.handler.MyUplinkThingHandler;
import org.openhab.binding.myuplink.internal.model.ValidationException;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
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
    protected final Command command;

    /**
     * the constructor
     */
    public AbstractWriteCommand(MyUplinkThingHandler handler, Channel channel, Command command,
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
            Unit<?> unit = quantityCommand.getUnit();
            QuantityType<?> convertedType;
            if (unit.isCompatible(SIUnits.CELSIUS)) {
                convertedType = quantityCommand.toUnit(SIUnits.CELSIUS);
            } else if (unit.isCompatible(Units.KILOWATT_HOUR)) {
                convertedType = quantityCommand.toUnit(Units.KILOWATT_HOUR);
            } else if (unit.isCompatible(Units.LITRE_PER_MINUTE)) {
                convertedType = quantityCommand.toUnit(Units.LITRE_PER_MINUTE);
            } else if (unit.isCompatible(tech.units.indriya.unit.Units.WATT)) {
                convertedType = quantityCommand.toUnit(MetricPrefix.KILO(tech.units.indriya.unit.Units.WATT));
            } else {
                logger.warn("automatic conversion of unit '{}' to myUplink expected unit not supported.",
                        unit.getName());
                convertedType = quantityCommand;
            }
            return String.valueOf(convertedType != null ? convertedType.doubleValue() : UnDefType.NULL);
        } else if (command instanceof OnOffType onOffType) {
            // this is necessary because we must send 0/1 and not ON/OFF to the backend
            return OnOffType.ON.equals(onOffType) ? "1" : "0";
        } else {
            return command.toString();
        }
    }

    /**
     * helper that transforms channelId + commandvalue in a JSON string that can be added as content to a POST request.
     *
     * @return converted JSON string
     */
    protected String getJsonContent() {
        return buildJsonObject(channel.getUID().getIdWithoutGroup(), getCommandValue());
    }

    /**
     * helper that creates a simple json object as string.
     *
     * @param key identifier of the value
     * @param value the value to assign to the key
     *
     * @return converted JSON string
     */
    protected String buildJsonObject(String key, String value) {
        Map<String, String> content = new HashMap<>(1);
        content.put(key, value);

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
            logger.debug("channel '{}' does not allow value '{}' - validation rule '{}'", channelId, value, expr);
            throw new ValidationException("channel (" + channelId + ") could not be updated due to a validation error");
        }
    }

    @Override
    protected String getChannelGroup() {
        // this is a pure write command, thus no channel group needed.
        return EMPTY;
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
