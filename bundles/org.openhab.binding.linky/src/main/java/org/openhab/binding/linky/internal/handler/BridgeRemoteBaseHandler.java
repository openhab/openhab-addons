/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.linky.internal.handler;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.linky.internal.api.EnedisHttpApi;
import org.openhab.binding.linky.internal.config.LinkyBridgeConfiguration;
import org.openhab.binding.linky.internal.constants.LinkyBindingConstants;
import org.openhab.binding.linky.internal.types.LinkyException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.net.http.TrustAllTrustManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * {@link BridgeRemoteBaseHandler} is the base handler to access enedis data.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public abstract class BridgeRemoteBaseHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(BridgeRemoteBaseHandler.class);

    protected final HttpService httpService;
    protected final BundleContext bundleContext;
    protected final HttpClient httpClient;
    protected final EnedisHttpApi enedisApi;

    protected final Gson gson;

    protected @Nullable LinkyBridgeConfiguration config;
    protected boolean connected = false;

    private static final int REQUEST_BUFFER_SIZE = 8000;
    private static final int RESPONSE_BUFFER_SIZE = 200000;

    private List<String> registeredPrmId = new ArrayList<>();

    public BridgeRemoteBaseHandler(Bridge bridge, final @Reference HttpClientFactory httpClientFactory,
            final @Reference OAuthFactory oAuthFactory, final @Reference HttpService httpService,
            ComponentContext componentContext, Gson gson) {
        super(bridge);

        SslContextFactory sslContextFactory = new SslContextFactory.Client();
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[] { TrustAllTrustManager.getInstance() }, null);
            sslContextFactory.setSslContext(sslContext);
        } catch (NoSuchAlgorithmException e) {
            logger.warn("An exception occurred while requesting the SSL encryption algorithm : '{}'", e.getMessage(),
                    e);
        } catch (KeyManagementException e) {
            logger.warn("An exception occurred while initialising the SSL context : '{}'", e.getMessage(), e);
        }

        this.gson = gson;
        this.httpService = httpService;
        this.bundleContext = componentContext.getBundleContext();

        this.httpClient = httpClientFactory.createHttpClient(LinkyBindingConstants.BINDING_ID, sslContextFactory);
        this.httpClient.setFollowRedirects(false);
        this.httpClient.setRequestBufferSize(REQUEST_BUFFER_SIZE);
        this.httpClient.setResponseBufferSize(RESPONSE_BUFFER_SIZE);

        this.enedisApi = new EnedisHttpApi(this, gson, this.httpClient);
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    @Override
    public synchronized void initialize() {
        logger.debug("Initializing Linky Remote bridge handler.");

        updateStatus(ThingStatus.UNKNOWN);

        scheduler.submit(() -> {
            try {
                httpClient.start();

                try {
                    connectionInit();
                    updateStatus(ThingStatus.ONLINE);
                } catch (LinkyException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                }
            } catch (Exception e) {
                logger.warn("Unable to start Jetty HttpClient {}", e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        });
    }

    public abstract void connectionInit() throws LinkyException;

    public void registerNewPrmId(String prmId) {
        if (!registeredPrmId.contains(prmId)) {
            registeredPrmId.add(prmId);
        }
    }

    public List<String> getAllPrmId() {
        return registeredPrmId;
    }

    public boolean isConnected() {
        return connected;
    }

    public @Nullable EnedisHttpApi getEnedisApi() {
        return enedisApi;
    }

    @Override
    public void dispose() {
        logger.debug("Shutting down Linky API bridge handler.");
        super.dispose();
    }

    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(status, statusDetail, description);
    }

    public abstract String getToken(ThingBaseRemoteHandler handler) throws LinkyException;

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public abstract double getDivider();

    public abstract String getBaseUrl();

    public abstract String getContactUrl();

    public abstract String getContractUrl();

    public abstract String getIdentityUrl();

    public abstract String getAddressUrl();

    public abstract String getDailyConsumptionUrl();

    public abstract String getMaxPowerUrl();

    public abstract String getLoadCurveUrl();

    public abstract String getTempoUrl();

    public abstract DateTimeFormatter getApiDateFormat();

    public abstract DateTimeFormatter getApiDateFormatYearsFirst();

    public abstract boolean supportNewApiFormat();

    public Gson getGson() {
        return gson;
    }
}
