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
 *
 */
public class UpdateSetting extends AbstractUplinkCommandCallback implements NibeUplinkCommand {

    private final NibeUplinkHandler handler;
    private final Channel channel;
    private final String value;
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
            logger.warn("channel '{}' does not support write access - value to set '{}'", channel.getFQName(), value);
            throw new UnsupportedOperationException(
                    "channel (" + channel.getFQName() + ") does not support write access");
        }

        if (value.matches(channel.getValidationExpression())) {
            Fields fields = new Fields();
            fields.add(channel.getId(), value);

            FormContentProvider cp = new FormContentProvider(fields);

            requestToPrepare.header(HttpHeader.ACCEPT_ENCODING, StandardCharsets.UTF_8.name());
            requestToPrepare.content(cp);
            requestToPrepare.followRedirects(false);
            requestToPrepare.method(HttpMethod.POST);

            return requestToPrepare;
        } else {
            logger.warn("channel '{}' does not allow value '{}' - validation rule '{}'", channel.getFQName(), value,
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
            logger.warn("Could not set value '{}' for channel '{}' ({})", value, channel.getId(), channel.getName());
            handler.getWebInterface().executeCommand(this);
        }
    }

    // // HW per. Erh
    // // /System/xxxxx/Manage/2.9.1
    // // 47050 1
    // // 47050 0
    // // 47051 28
    // // 47052 12:00
    //

    // // Raumfühler
    // // /System/xxxxx/Manage/1.9.4
    // // 47394 1=ein 0=aus
    // // 47402 Heizfaktor 0-6
    // // 48793 Kühlfaktor 0-6

    //
    // // Urlaubsmodus
    // // /System/xxxxx/Manage/4.7
    // // 48043 10 An??
    // // 48043 0 Aus ??
    // // 48044 2018-03-16
    // // 48045 2018-03-18
    // // 48046 -4 Heizkurvenverschiebung
    // // 48047 -1 BW -1 = aus, 0 = Sparm
    // // 48048 1 Lüftungsstufe
    // // 48049 0 Kühlung 0=aus 1=ein
    // // 48051 16 Zieltemperatur

    //
    // // Automodus
    // // /System/xxxxx/Manage/4.2
    // // 47370 0 ZH
    // // 47371 0 Heizung
    // // 47372 1 Kühlung
    // // 47570 1 auto=0, man=1

    // TODO:DONE
    // // Heizkurve S1
    // // /System/xxxxx/Manage/1.9.1.1-S1
    // // 47007 4
    // // 47011 -1
    // // HW Luxus
    // // /System/xxxxx/Manage/2.1
    // // 48132 0 - aus
    // // 48132 4 - einmal
    // // HW Mode
    // // /System/xxxxx/Manage/2.2
    // // 47041 0, 1, 2
    // // Ventilation ERS
    // // /System/xxxxx/Manage/1.2
    // // 47260 2 (0-4, 0=normal)
}
