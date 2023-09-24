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
package org.openhab.binding.nibeuplink.internal.command;

import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.Fields;
import org.openhab.binding.nibeuplink.internal.NibeUplinkBindingConstants;
import org.openhab.binding.nibeuplink.internal.callback.AbstractUplinkCommandCallback;
import org.openhab.binding.nibeuplink.internal.handler.ChannelUtil;
import org.openhab.binding.nibeuplink.internal.handler.NibeUplinkHandler;
import org.openhab.binding.nibeuplink.internal.model.ValidationException;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;

/**
 * allows update of writable channels
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class UpdateSetting extends AbstractUplinkCommandCallback implements NibeUplinkCommand {

    private final NibeUplinkHandler handler;
    private final Channel channel;
    private String value;
    private int retries = 0;

    public UpdateSetting(NibeUplinkHandler handler, Channel channel, Command command) {
        super(handler.getConfiguration());
        this.handler = handler;
        this.channel = channel;
        this.value = extractValue(command);
    }

    private String extractValue(Command command) {
        // this is necessary because we must not send the unit to the nibe backend
        if (command instanceof QuantityType<?> quantityCommand) {
            return String.valueOf(quantityCommand.doubleValue());
        } else if (command instanceof OnOffType onOffCommand) {
            return ChannelUtil.mapValue(channel, onOffCommand);
        } else {
            return command.toString();
        }
    }

    @Override
    protected Request prepareRequest(Request requestToPrepare) {
        ChannelTypeUID typeUID = channel.getChannelTypeUID();
        String channelId = channel.getUID().getIdWithoutGroup();

        if (typeUID == null || !typeUID.getId().startsWith(NibeUplinkBindingConstants.RW_CHANNEL_PREFIX)) {
            logger.info("channel '{}' does not support write access - value to set '{}'", channelId, value);
            throw new UnsupportedOperationException("channel (" + channelId + ") does not support write access");
        }

        // although we have integers openhab often transfers decimals which will then cause a validation error. So we
        // will shorten here.
        if (value.endsWith(".0")) {
            value = value.substring(0, value.length() - 2);
        }

        String expr = ChannelUtil.getValidationExpression(channel);

        if (value.matches(expr)) {
            Fields fields = new Fields();
            fields.add(channelId, value);

            FormContentProvider cp = new FormContentProvider(fields);

            requestToPrepare.header(HttpHeader.ACCEPT_ENCODING, StandardCharsets.UTF_8.name());
            requestToPrepare.content(cp);
            requestToPrepare.followRedirects(false);
            requestToPrepare.method(HttpMethod.POST);

            return requestToPrepare;
        } else {
            logger.info("channel '{}' does not allow value '{}' - validation rule '{}'", channelId, value, expr);
            throw new ValidationException("channel (" + channelId + ") could not be updated due to a validation error");
        }
    }

    @Override
    protected String getURL() {
        return NibeUplinkBindingConstants.MANAGE_API_BASE_URL + config.getNibeId()
                + ChannelUtil.getWriteApiUrlSuffix(channel);
    }

    @Override
    public void onComplete(@Nullable Result result) {
        logger.debug("onComplete()");

        if (!HttpStatus.Code.FOUND.equals(getCommunicationStatus().getHttpCode()) && retries++ < MAX_RETRIES) {
            logger.debug("Could not set value '{}' for channel '{}' ({})", value, channel.getUID().getId(),
                    channel.getLabel());
            handler.getWebInterface().enqueueCommand(this);
        }
    }
}
