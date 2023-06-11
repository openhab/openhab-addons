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
package org.openhab.binding.nibeuplink.internal.command;

import static org.openhab.binding.nibeuplink.internal.NibeUplinkBindingConstants.*;

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
import org.openhab.binding.nibeuplink.internal.callback.AbstractUplinkCommandCallback;
import org.openhab.binding.nibeuplink.internal.connector.StatusUpdateListener;
import org.openhab.binding.nibeuplink.internal.handler.NibeUplinkHandler;
import org.openhab.binding.nibeuplink.internal.model.DataResponse;
import org.openhab.binding.nibeuplink.internal.model.DataResponseTransformer;
import org.openhab.core.thing.Channel;

/**
 * generic command that retrieves status values for all channels defined in {@link VVM320Channels}
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class GenericStatusUpdate extends AbstractUplinkCommandCallback implements NibeUplinkCommand {
    private final NibeUplinkHandler handler;
    private final DataResponseTransformer transformer;
    private int retries = 0;

    public GenericStatusUpdate(NibeUplinkHandler handler) {
        super(handler.getConfiguration());
        this.handler = handler;
        this.transformer = new DataResponseTransformer(handler);
    }

    @Override
    protected Request prepareRequest(Request requestToPrepare) {
        Fields fields = new Fields();
        fields.add(DATA_API_FIELD_LAST_DATE, DATA_API_FIELD_LAST_DATE_DEFAULT_VALUE);
        fields.add(DATA_API_FIELD_ID, config.getNibeId());

        for (Channel channel : handler.getChannels()) {
            if (!handler.getDeadChannels().contains(channel)) {
                fields.add(DATA_API_FIELD_DATA, channel.getUID().getIdWithoutGroup());
            }
        }

        fields.add(DATA_API_FIELD_DATA, DATA_API_FIELD_DATA_DEFAULT_VALUE);
        FormContentProvider cp = new FormContentProvider(fields);

        requestToPrepare.header(HttpHeader.ACCEPT, "application/json");
        requestToPrepare.header(HttpHeader.ACCEPT_ENCODING, StandardCharsets.UTF_8.name());
        requestToPrepare.content(cp);
        requestToPrepare.followRedirects(false);
        requestToPrepare.method(HttpMethod.POST);

        return requestToPrepare;
    }

    @Override
    protected String getURL() {
        return DATA_API_URL;
    }

    @Override
    public void onComplete(@Nullable Result result) {
        logger.debug("onComplete()");

        if (!HttpStatus.Code.OK.equals(getCommunicationStatus().getHttpCode()) && retries++ < MAX_RETRIES) {
            StatusUpdateListener listener = getListener();
            if (listener != null) {
                listener.update(getCommunicationStatus());
            }
            handler.getWebInterface().enqueueCommand(this);
        } else {
            String json = getContentAsString(StandardCharsets.UTF_8);
            if (json != null && !json.isEmpty()) {
                logger.debug("JSON String: {}", json);
                DataResponse jsonObject = fromJson(json);
                if (jsonObject != null) {
                    handler.updateChannelStatus(transformer.transform(jsonObject));
                }
            }
        }
    }
}
