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
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

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
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.net.http.TrustAllTrustManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
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

    private Dictionary<String, Contract> contractDict = new Hashtable<String, Contract>();

    protected final HttpService httpService;
    protected final BundleContext bundleContext;
    protected final HttpClient httpClient;
    protected final SedifHttpApi sedifApi;
    protected final ThingRegistry thingRegistry;

    private static final DateTimeFormatter API_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter API_DATE_FORMAT_YEAR_FIRST = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static final String SEDIF_DOMAIN = ".leaudiledefrance.fr";
    private static final String BASE_URL = "https://connexion" + SEDIF_DOMAIN;
    private static final String URL_SEDIF_AUTHENTICATE = BASE_URL + "/s/login/";
    private static final String URL_SEDIF_AUTHENTICATE_POST = BASE_URL
            + "/s/sfsites/aura?r=1&other.LightningLoginForm.login=1";

    private static final String URL_SEDIF_CONTRAT = BASE_URL + "/espace-particuliers/s/contrat?tab=Detail";

    private static final String URL_SEDIF_SITE = BASE_URL
            + "/espace-particuliers/s/sfsites/aura?r=36&aura.ApexAction.execute=1";

    protected final Gson gson;
    private @Nullable String sid = "";

    private @Nullable AuraContext appCtx;

    private @Nullable String token = "";

    protected boolean connected = false;

    private static final int REQUEST_BUFFER_SIZE = 8000;
    private static final int RESPONSE_BUFFER_SIZE = 200000;

    public BridgeSedifWebHandler(Bridge bridge, final @Reference HttpClientFactory httpClientFactory,
            final @Reference OAuthFactory oAuthFactory, final @Reference HttpService httpService,
            final @Reference ThingRegistry thingRegistry, ComponentContext componentContext, Gson gson) {
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
        this.thingRegistry = thingRegistry;
        this.bundleContext = componentContext.getBundleContext();

        this.httpClient = httpClientFactory.createHttpClient(SedifBindingConstants.BINDING_ID, sslContextFactory);
        this.httpClient.setFollowRedirects(false);
        this.httpClient.setRequestBufferSize(REQUEST_BUFFER_SIZE);
        this.httpClient.setResponseBufferSize(RESPONSE_BUFFER_SIZE);

        try {
            httpClient.start();
        } catch (Exception e) {
            logger.warn("Unable to start Jetty HttpClient {}", e.getMessage());
        }

        this.sedifApi = new SedifHttpApi(this, gson, this.httpClient);

    }

    @Override
    public void initialize() {
        // TODO Auto-generated method stub
        updateStatus(ThingStatus.UNKNOWN);

        scheduler.submit(() -> {
            try {
                connectionInit();
                updateStatus(ThingStatus.ONLINE);
            } catch (SedifException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        });

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub

    }

    public String getBaseUrl() {
        return BASE_URL;
    }

    public synchronized void connectionInit() throws SedifException {
        config = getConfigAs(SedifBridgeConfiguration.class);

        SedifBridgeConfiguration lcConfig = config;

        try {
            sedifApi.removeAllCookie();

            String resultSt;

            // =====================================================================
            logger.debug("Step 1: getting salesforces context from login page");

            resultSt = sedifApi.getContent(URL_SEDIF_AUTHENTICATE);
            appCtx = sedifApi.extractAuraContext(resultSt);

            if (appCtx == null) {
                throw new SedifException("Unable to find app context in login process");
            }

            // =====================================================================
            logger.debug("Step 2: Authenticate");

            AuraResponse resp = sedifApi.doAuth(lcConfig.username, lcConfig.password);

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

                sid = "";
                String[] parts = urlRedir.split("&");
                for (String part : parts) {
                    if (part.indexOf("sid") >= 0) {
                        sid = part;
                    }
                }
                if (sid != null) {
                    sid = sid.replace("sid=", "");
                } else {
                    throw new SedifException("Unable to find sid in login process");
                }
            }

            // =====================================================================
            logger.debug("Step 3: Confirm login");

            resultSt = sedifApi.getContent(urlRedir);

            // =====================================================================
            logger.debug("Step 4: Get contract page");

            resultSt = sedifApi.getContent(URL_SEDIF_CONTRAT);
            appCtx = sedifApi.extractAuraContext(resultSt);

            if (appCtx == null) {
                throw new SedifException("Unable to find app context in login process");
            }
            // =====================================================================

            logger.debug("Step 5: Get cookie auth");
            List<HttpCookie> lCookie = httpClient.getCookieStore().getCookies();
            token = "";
            for (HttpCookie cookie : lCookie) {
                if (cookie.getName().indexOf("__Host-ERIC_") >= 0) {
                    token = cookie.getValue();
                }
            }

            if (token == null) {
                throw new SedifException("Unable to find token in login process");
            }

            // =====================================================================
            logger.debug("Step 6: Get contract");
            Contracts contracts = sedifApi.getContracts();
            if (contracts != null && contracts.contrats != null) {
                for (Contract contract : contracts.contrats) {
                    if (contract.Name != null) {
                        contractDict.put(contract.Name, contract);
                        fireOnContractReceivedEvent(contract);
                    }
                }
            }

            connected = true;
        } catch (Exception ex) {
            logger.debug("error durring connect : {}" + ex.getMessage());
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public String getUrlSedifSite() {
        return URL_SEDIF_SITE;
    }

    public String getUrlSedifAuth() {
        return URL_SEDIF_AUTHENTICATE_POST;
    }

    public DateTimeFormatter getApiDateFormat() {
        return API_DATE_FORMAT;
    }

    public DateTimeFormatter getApiDateFormatYearsFirst() {
        return API_DATE_FORMAT_YEAR_FIRST;
    }

    public @Nullable String getToken() {
        return token;
    }

    public @Nullable AuraContext getAppCtx() {
        return appCtx;
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

    public Contract getContract(String contractName) {
        return contractDict.get(contractName);
    }

}
