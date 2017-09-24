/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solaredge.internal.command;

import static org.openhab.binding.solaredge.SolarEdgeBindingConstants.*;

import java.io.IOException;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.nio.charset.Charset;

import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.solaredge.handler.SolarEdgeHandler;
import org.openhab.binding.solaredge.internal.callback.AbstractCommandCallback;
import org.openhab.binding.solaredge.internal.model.LiveDataResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * generic command that retrieves status values for all channels defined in {@link VVM320Channels}
 *
 * @author afriese
 *
 */
public class LiveDataUpdate extends AbstractCommandCallback implements SolarEdgeCommand {

    private final SolarEdgeHandler handler;
    private int retries = 0;

    public LiveDataUpdate(SolarEdgeHandler handler) {
        super(handler.getConfiguration());
        this.handler = handler;
    }

    @Override
    protected Request prepareRequest(Request requestToPrepare, CookieStore cookieStore) {
        HttpCookie c = new HttpCookie(TOKEN_COOKIE_NAME, config.getToken());
        c.setDomain(TOKEN_COOKIE_DOMAIN);
        c.setPath(TOKEN_COOKIE_PATH);
        cookieStore.add(URI.create(getURL()), c);

        requestToPrepare.followRedirects(false);
        requestToPrepare.method(HttpMethod.GET);

        return requestToPrepare;
    }

    @Override
    protected String getURL() {
        return DATA_API_URL + config.getSolarId() + DATA_API_URL_LIVE_DATA_SUFFIX;
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
            LiveDataResponse jsonObject = convertJson(json);
            if (jsonObject != null) {
                handler.updateChannelStatus(jsonObject.getValues());
            }
        }
    }

    /**
     * converts the json response into an object
     *
     * @param jsonInString
     * @return
     */
    private LiveDataResponse convertJson(String jsonInString) {
        ObjectMapper mapper = new ObjectMapper();
        // not supported in v2.4.x
        // mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

        LiveDataResponse obj = null;
        try {
            logger.debug("JSON String: {}", jsonInString);
            obj = mapper.readValue(jsonInString, LiveDataResponse.class);
        } catch (IOException e) {
            logger.error("Caught IOException: {}", e.getMessage());
        }
        return obj;
    }

}
