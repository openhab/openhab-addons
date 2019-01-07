/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeuplink.internal.command;

import static org.openhab.binding.nibeuplink.NibeUplinkBindingConstants.MANAGE_API_BASE_URL;

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
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.nibeuplink.handler.NibeUplinkHandler;
import org.openhab.binding.nibeuplink.internal.callback.AbstractUplinkCommandCallback;
import org.openhab.binding.nibeuplink.internal.model.Channel;
import org.openhab.binding.nibeuplink.internal.model.SwitchChannel;
import org.openhab.binding.nibeuplink.internal.model.ValidationException;

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
        if (command instanceof QuantityType<?>) {
            return String.valueOf(((QuantityType<?>) command).doubleValue());
        } else if (command instanceof OnOffType && channel instanceof SwitchChannel) {
            return ((SwitchChannel) channel).mapValue((OnOffType) command);
        } else {
            return command.toString();
        }
    }

    @Override
    protected Request prepareRequest(Request requestToPrepare) {
        if (channel.isReadOnly()) {
            logger.info("channel '{}' does not support write access - value to set '{}'", channel.getFQName(), value);
            throw new UnsupportedOperationException(
                    "channel (" + channel.getFQName() + ") does not support write access");
        }

        // although we have integers openhab often transfers decimals which will then cause a validation error. So we
        // will shorten here.
        if (value.endsWith(".0")) {
            value = value.substring(0, value.length() - 2);
        }

        if (value.matches(channel.getValidationExpression())) {
            Fields fields = new Fields();
            fields.add(channel.getChannelCode(), value);

            FormContentProvider cp = new FormContentProvider(fields);

            requestToPrepare.header(HttpHeader.ACCEPT_ENCODING, StandardCharsets.UTF_8.name());
            requestToPrepare.content(cp);
            requestToPrepare.followRedirects(false);
            requestToPrepare.method(HttpMethod.POST);

            return requestToPrepare;
        } else {
            logger.info("channel '{}' does not allow value '{}' - validation rule '{}'", channel.getFQName(), value,
                    channel.getValidationExpression());
            throw new ValidationException(
                    "channel (" + channel.getFQName() + ") could not be updated due to a validation error");
        }
    }

    @Override
    protected String getURL() {
        return MANAGE_API_BASE_URL + config.getNibeId() + channel.getWriteApiUrlSuffix();
    }

    @Override
    public void onComplete(@Nullable Result result) {
        logger.debug("onComplete()");

        if (!HttpStatus.Code.FOUND.equals(getCommunicationStatus().getHttpCode()) && retries++ < MAX_RETRIES) {
            logger.debug("Could not set value '{}' for channel '{}' ({})", value, channel.getChannelCode(),
                    channel.getName());
            handler.getWebInterface().enqueueCommand(this);
        }
    }
}
