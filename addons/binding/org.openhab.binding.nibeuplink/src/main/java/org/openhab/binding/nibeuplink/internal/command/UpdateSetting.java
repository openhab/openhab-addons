/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeuplink.internal.command;

import static org.openhab.binding.nibeuplink.NibeUplinkBindingConstants.MANAGE_API_BASE_URL;

import java.nio.charset.StandardCharsets;

import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.Fields;
import org.openhab.binding.nibeuplink.handler.NibeUplinkHandler;
import org.openhab.binding.nibeuplink.internal.callback.AbstractUplinkCommandCallback;
import org.openhab.binding.nibeuplink.internal.model.Channel;
import org.openhab.binding.nibeuplink.internal.model.ValidationException;

/**
 * allows update of writable channels
 *
 * @author Alexander Friese - initial contribution
 */
public class UpdateSetting extends AbstractUplinkCommandCallback implements NibeUplinkCommand {

    private final NibeUplinkHandler handler;
    private final Channel channel;
    private String value;
    private int retries = 0;

    public UpdateSetting(NibeUplinkHandler handler, Channel channel, String value) {
        super(handler.getConfiguration());
        this.handler = handler;
        this.channel = channel;
        this.value = value;
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
    public void onComplete(Result result) {
        logger.debug("onComplete()");

        if (!HttpStatus.Code.FOUND.equals(getCommunicationStatus().getHttpCode()) && retries++ < MAX_RETRIES) {
            logger.debug("Could not set value '{}' for channel '{}' ({})", value, channel.getChannelCode(),
                    channel.getName());
            handler.getWebInterface().enqueueCommand(this);
        }
    }
}
