/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler;

import static org.eclipse.smarthome.io.net.http.HttpUtil.executeUrl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ThingStatusInfoBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.netatmo.config.NetatmoConfiguration;
import org.openhab.binding.netatmo.internal.NetatmoException;
import org.openhab.binding.netatmo.internal.OAuthCredentials;
import org.openhab.binding.netatmo.internal.messages.AbstractRequest;
import org.openhab.binding.netatmo.internal.messages.DeviceListBody;
import org.openhab.binding.netatmo.internal.messages.DeviceListRequest;
import org.openhab.binding.netatmo.internal.messages.NetatmoResponse;
import org.openhab.binding.netatmo.internal.messages.NetatmoResponses;
import org.openhab.binding.netatmo.internal.messages.RefreshTokenRequest;
import org.openhab.binding.netatmo.internal.messages.RefreshTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link NetatmoBridgeHandler} is the handler for a Netatmo API and connects it
 * to the framework. The devices and modules uses the
 * {@link NetatmoBridgeHandler} to request informations about their status
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 * @author Andreas Brenk - OH1 version
 *
 */
public class NetatmoBridgeHandler extends BaseBridgeHandler {
    private static final String HTTP_GET = "GET";
    private static final String HTTP_POST = "POST";
    private static final String CHARSET = "UTF-8";
    private static final String HTTP_CONTENT_TYPE = "application/x-www-form-urlencoded;charset=" + CHARSET;
    private static final int HTTP_REQUEST_TIMEOUT = 10000;
    private static final ObjectMapper JSON = new ObjectMapper();

    protected static final Properties HTTP_HEADERS;

    static {
        HTTP_HEADERS = new Properties();
        HTTP_HEADERS.put("Accept", "application/json");
    }

    private static Logger logger = LoggerFactory.getLogger(NetatmoBridgeHandler.class);

    private long refreshInterval;
    private ScheduledFuture<?> pollingJob;

    public DeviceListBody deviceList;
    public OAuthCredentials credentials;

    private synchronized void onUpdate() {
        if (pollingJob == null || pollingJob.isCancelled()) {
            pollingJob = scheduler.scheduleAtFixedRate(pollingRunnable, 1, refreshInterval, TimeUnit.MILLISECONDS);
        }
    }

    private Runnable pollingRunnable = new Runnable() {
        @Override
        public void run() {
            logger.debug("Querying Netatmo API");
            for (Thing thing : getThing().getThings()) {
                ThingHandler handler = thing.getHandler();
                if (handler instanceof AbstractEquipment) {
                    ((AbstractEquipment) handler).poll();
                }
            }
        }
    };

    public NetatmoBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Netatmo API bridge handler.");

        NetatmoConfiguration configuration = getConfigAs(NetatmoConfiguration.class);

        refreshInterval = configuration.refreshInterval;

        credentials = new OAuthCredentials(configuration.clientId, configuration.clientSecret,
                configuration.refreshToken);

        refreshAccessToken();

        deviceList = executeGet(new DeviceListRequest(credentials), NetatmoResponses.DeviceList.class, true);

        updateStatus(ThingStatus.ONLINE);
        // workaround for issue #92: getHandler() returns NULL after
        // configuration update. :
        getThing().setHandler(this);

        onUpdate();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Nothing to do, we're a read only binding
    }

    /**
     * Launches an http get request toward Netatmo API
     *
     * @param request
     *            The request to be execute
     * @param beanClass
     *            The response class of request
     * @param retryAuth
     *            True if the request shall be retried
     * @return
     */
    public <T extends NetatmoResponse<F>, F> F executeGet(AbstractRequest request, Class<T> beanClass,
            boolean retryAuth) {
        final String url = request.prepare();
        String json = executeUrl(HTTP_GET, url, HTTP_HEADERS, null, null, HTTP_REQUEST_TIMEOUT);

        try {
            T response = JSON.readValue(json, beanClass);
            try {
                response.evaluate();
                F result = response.getBody();
                return result;
            } catch (NetatmoException e) {
                if ((retryAuth && (e.getResponse().getError().isAccessTokenExpired()
                        || e.getResponse().getError().isAccessTokenInvalid()))) {
                    refreshAccessToken();
                    scheduler.execute(pollingRunnable);
                    return executeGet(request, beanClass, false);
                }
                logger.error("Could not execute user request {} {}", url, json);
                getThing().setStatusInfo(ThingStatusInfoBuilder
                        .create(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR).build());
            }
        } catch (IOException e1) {
            logger.error("Could not execute user request {} {}", url, json);
        }
        return null;
    }

    public void refreshAccessToken() {
        logger.debug("Refreshing access token.");

        final RefreshTokenRequest request = new RefreshTokenRequest(credentials);
        String url = request.prepare();

        try {
            InputStream stream = new ByteArrayInputStream(request.getContent().getBytes(CHARSET));
            String json = executeUrl(HTTP_POST, url, HTTP_HEADERS, stream, HTTP_CONTENT_TYPE, HTTP_REQUEST_TIMEOUT);
            RefreshTokenResponse response = JSON.readValue(json, RefreshTokenResponse.class);
            credentials.accessToken = response.getAccessToken();
        } catch (Exception e) {
            logger.error("An exception occurred while refreshing AccessToken {}", e);
        }

    }

}
