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

import java.net.HttpCookie;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
import org.openhab.binding.sedif.internal.dto.AuraContext;
import org.openhab.binding.sedif.internal.dto.AuraResponse;
import org.openhab.binding.sedif.internal.dto.Contract;
import org.openhab.binding.sedif.internal.dto.Contracts;
import org.openhab.binding.sedif.internal.dto.Event;
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
    protected @Nullable SedifBridgeConfiguration config;

    private Set<SedifListener> listeners = new CopyOnWriteArraySet<>();

    private HashMap<String, Contract> contractDict = new HashMap<>();

    protected final HttpClient httpClient;
    protected final SedifHttpApi sedifApi;

    protected final Gson gson;

    private @Nullable AuraContext appCtx;
    private @Nullable String token = "";

    protected boolean connected = false;

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
                connectionInit();
                if (connected) {
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
        connected = false;
        scheduleConnection(30);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public void connectionInit() throws SedifException {
        config = getConfigAs(SedifBridgeConfiguration.class);

        SedifBridgeConfiguration lcConfig = config;

        if (connected) {
            return;
        }

        sedifApi.removeAllCookie();

        String resultSt;

        // =====================================================================
        // Step 1: getting salesforces context from login page
        // =====================================================================
        resultSt = sedifApi.getContent(SedifHttpApi.URL_SEDIF_AUTHENTICATE);
        appCtx = sedifApi.extractAuraContext(resultSt);

        if (appCtx == null) {
            throw new SedifException("Unable to find app context in login process");
        } else {
            logger.debug("Account {}: Successfully retrieved context", lcConfig.username);
        }

        // =====================================================================
        // Step 2: Authenticate
        // =====================================================================
        AuraResponse resp = sedifApi.doAuth(lcConfig.username, lcConfig.password, appCtx);

        String urlRedir = "";
        if (resp != null) {
            Event event = resp.events.getFirst();
            Event.Attributes attr = event.attributes;
            if (attr != null) {
                urlRedir = (String) attr.values.get("url");
            }

            if (urlRedir.isBlank()) {
                throw new SedifException("Unable to find redir url in login process");
            }
        }

        // =====================================================================
        // Step 3: Confirm login
        // =====================================================================
        resultSt = sedifApi.getContent(urlRedir);

        // =====================================================================
        // Step 4: Get contract page
        // =====================================================================
        resultSt = sedifApi.getContent(SedifHttpApi.URL_SEDIF_CONTRAT);
        appCtx = sedifApi.extractAuraContext(resultSt);

        if (appCtx == null) {
            throw new SedifException("Unable to find app context in login process");
        } else {
            logger.debug("Successfully retrieved contract context");
        }

        // =====================================================================
        // Step 5: Get cookie auth
        // =====================================================================
        List<HttpCookie> lCookie = httpClient.getCookieStore().getCookies();
        token = "";
        for (HttpCookie cookie : lCookie) {
            if (cookie.getName().startsWith("__Host-ERIC_")) {
                token = cookie.getValue();
            }
        }

        if (token == null) {
            throw new SedifException("Unable to find token in login process");
        } else {
            logger.debug("Account: Successfully asquire token");
            sedifApi.setToken(token);
        }

        // =====================================================================
        // Step 6a: Get contract
        // =====================================================================
        Contracts contracts = sedifApi.getContracts(appCtx);
        if (contracts != null && contracts.contracts != null) {
            for (Contract contract : contracts.contracts) {
                String contractName = contract.name;
                if (contractName != null) {
                    contractDict.put(contractName, contract);
                    fireOnContractReceivedEvent(contract);
                }
            }
        }

        connected = true;
    }

    public @Nullable AuraContext getAppContext() {
        return appCtx;
    }

    public boolean isConnected() {
        return connected;
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
        return contractDict.get(contractName);
    }
}
