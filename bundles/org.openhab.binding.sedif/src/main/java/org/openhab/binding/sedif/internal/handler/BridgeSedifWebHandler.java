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
package org.openhab.binding.sedif.internal.handler;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.sedif.internal.api.SedifHttpApi;
import org.openhab.binding.sedif.internal.config.SedifBridgeConfiguration;
import org.openhab.binding.sedif.internal.constants.SedifBindingConstants;
import org.openhab.binding.sedif.internal.discovery.SedifDiscoveryService;
import org.openhab.binding.sedif.internal.dto.Contract;
import org.openhab.binding.sedif.internal.helpers.SedifListener;
import org.openhab.binding.sedif.internal.types.SedifException;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.net.http.TrustAllTrustManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * {@link BridgeSedifWebHandler} is the base handler to access sedif data.
 *
 * @author Laurent Arnal - Initial contribution
 *
 */
@NonNullByDefault
public class BridgeSedifWebHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(BridgeSedifWebHandler.class);

    private Set<SedifListener> listeners = new CopyOnWriteArraySet<>();

    protected final HttpClient httpClient;
    protected final SedifHttpApi sedifApi;

    protected final Gson gson;

    private static final int REQUEST_BUFFER_SIZE = 8000;
    private static final int RESPONSE_BUFFER_SIZE = 200000;

    public BridgeSedifWebHandler(Bridge bridge, final @Reference HttpClientFactory httpClientFactory, Gson gson) {
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

        this.httpClient = httpClientFactory.createHttpClient(SedifBindingConstants.BINDING_ID, sslContextFactory);
        this.httpClient.setFollowRedirects(false);
        this.httpClient.setRequestBufferSize(REQUEST_BUFFER_SIZE);
        this.httpClient.setResponseBufferSize(RESPONSE_BUFFER_SIZE);

        try {
            httpClient.start();
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }

        this.sedifApi = new SedifHttpApi(gson, this.httpClient);
    }

    @Override
    public void dispose() {
        super.dispose();
        try {
            httpClient.stop();
        } catch (Exception e) {
            logger.debug("HttpClient failed on bridge disposal: {}", e.getMessage(), e);
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        scheduleConnection(1);
    }

    public void scheduleConnection(int delay) {
        scheduler.schedule(() -> {
            try {
                SedifBridgeConfiguration config = getConfigAs(SedifBridgeConfiguration.class);
                sedifApi.connectionInit(config.username, config.password);
                HashMap<String, Contract> contracts = sedifApi.getAllContracts();
                for (Contract contract : contracts.values()) {
                    fireOnContractReceivedEvent(contract);
                }

                if (sedifApi.isConnected()) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Connection failed");
                }
            } catch (SedifException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }, delay, TimeUnit.SECONDS);
    }

    public void scheduleReconnect() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        sedifApi.disconnect();
        scheduleConnection(30);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public SedifHttpApi getSedifApi() {
        return sedifApi;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(SedifDiscoveryService.class);
    }

    public void addListener(final SedifListener listener) {
        listeners.add(listener);
    }

    public void removeListener(final SedifListener listener) {
        listeners.remove(listener);
    }

    protected void fireOnContractReceivedEvent(final Contract contract) {
        listeners.forEach(l -> l.onContractInit(contract));
    }

    public @Nullable Contract getContract(String contractName) {
        return sedifApi.getContract(contractName);
    }
}
