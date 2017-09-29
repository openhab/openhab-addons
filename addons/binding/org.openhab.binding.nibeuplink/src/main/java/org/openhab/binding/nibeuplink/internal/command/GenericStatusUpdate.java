/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeuplink.internal.command;

import static org.openhab.binding.nibeuplink.NibeUplinkBindingConstants.*;

import java.nio.charset.Charset;

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
import org.openhab.binding.nibeuplink.internal.model.GenericDataResponse;
import org.openhab.binding.nibeuplink.internal.model.VVM320Channels;

/**
 * generic command that retrieves status values for all channels defined in {@link VVM320Channels}
 *
 * @author afriese
 *
 */
public class GenericStatusUpdate extends AbstractUplinkCommandCallback implements NibeUplinkCommand {

    private final NibeUplinkHandler handler;
    private int retries = 0;

    public GenericStatusUpdate(NibeUplinkHandler handler) {
        super(handler.getConfiguration());
        this.handler = handler;
    }

    @Override
    protected Request prepareRequest(Request requestToPrepare) {

        Fields fields = new Fields();
        fields.add(DATA_API_FIELD_LAST_DATE, DATA_API_FIELD_LAST_DATE_DEFAULT_VALUE);
        fields.add(DATA_API_FIELD_ID, config.getNibeId());

        for (Channel channel : handler.getChannels()) {
            if (!handler.getDeadChannels().contains(channel)) {
                fields.add(DATA_API_FIELD_DATA, channel.getId());
            }
        }

        fields.add(DATA_API_FIELD_DATA, DATA_API_FIELD_DATA_DEFAULT_VALUE);
        FormContentProvider cp = new FormContentProvider(fields);

        requestToPrepare.header(HttpHeader.ACCEPT, "application/json");
        requestToPrepare.header(HttpHeader.ACCEPT_ENCODING, "UTF-8");
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
    public void onComplete(Result result) {
        logger.debug("onComplete()");

        if (!getCommunicationStatus().getHttpCode().equals(HttpStatus.OK_200) && retries++ < MAX_RETRIES) {
            if (getListener() != null) {
                getListener().update(getCommunicationStatus());
            }
            handler.getWebInterface().executeCommand(this);
        }

        String json = getContentAsString(Charset.forName("UTF-8"));
        if (json != null) {
            GenericDataResponse jsonObject = convertJson(json, GenericDataResponse.class);
            if (jsonObject != null) {
                handler.updateChannelStatus(jsonObject.getValues());
            }
        }
    }
}
