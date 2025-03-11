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
import java.net.URLDecoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.util.Fields;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.sedif.internal.api.SedifHttpApi;
import org.openhab.binding.sedif.internal.config.SedifConfiguration;
import org.openhab.binding.sedif.internal.constants.SedifBindingConstants;
import org.openhab.binding.sedif.internal.dto.Action;
import org.openhab.binding.sedif.internal.dto.Actions;
import org.openhab.binding.sedif.internal.dto.AuraContext;
import org.openhab.binding.sedif.internal.dto.AuraResponse;
import org.openhab.binding.sedif.internal.dto.ContractDetail;
import org.openhab.binding.sedif.internal.dto.Contracts;
import org.openhab.binding.sedif.internal.dto.Event;
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
    protected @Nullable SedifConfiguration config;

    protected final HttpService httpService;
    protected final BundleContext bundleContext;
    protected final HttpClient httpClient;
    protected final SedifHttpApi sedifApi;
    protected final ThingRegistry thingRegistry;

    public static final String SEDIF_DOMAIN = ".leaudiledefrance.fr";
    private static final String BASE_URL = "https://connexion" + SEDIF_DOMAIN;
    private static final String URL_SEDIF_AUTHENTICATE = BASE_URL + "/s/login/";
    private static final String URL_SEDIF_AUTHENTICATE_POST = BASE_URL
            + "/s/sfsites/aura?r=1&other.LightningLoginForm.login=1";

    private static final String URL_SEDIF_SITE = BASE_URL
            + "/espace-particuliers/s/sfsites/aura?r=36&aura.ApexAction.execute=1";

    protected final Gson gson;
    private @Nullable String fwuid = "";
    private @Nullable String appId = "";
    private @Nullable String sid = "";
    private @Nullable String token = "";

    private @Nullable String contractId = "";
    private @Nullable String meterIdA = "";
    private @Nullable String meterIdB = "";

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
        config = getConfigAs(SedifConfiguration.class);

        SedifConfiguration lcConfig = config;

        try {
            sedifApi.removeAllCookie();

            ContentResponse result;
            String resultSt;
            Fields fields;
            Actions actions;
            Action action;
            String actionPayLoad;

            // =====================================================================
            logger.debug("Step 1: getting salesforces context from login page");

            result = httpClient.GET(URL_SEDIF_AUTHENTICATE);
            resultSt = result.getContentAsString();

            int pos1 = resultSt.indexOf("resources.js");
            AuraContext context = null;
            if (pos1 >= 0) {
                String sub1 = resultSt.substring(0, pos1 + 1);
                int pos2 = sub1.lastIndexOf("<script");
                if (pos2 < 0) {
                    throw new SedifException("Unable to find app context in login process");
                }
                int pos3 = sub1.indexOf("%7B", pos2 + 1);
                if (pos3 < 0) {
                    throw new SedifException("Unable to find app context in login process");
                }
                int pos4 = sub1.lastIndexOf("%7D");
                if (pos4 < 0) {
                    throw new SedifException("Unable to find app context in login process");
                }

                String sub2 = sub1.substring(pos3, pos4 + 3);
                sub2 = URLDecoder.decode(sub2, "UTF-8");
                context = gson.fromJson(sub2, AuraContext.class);
            }

            // =====================================================================
            logger.debug("Step 2: Authenticate");

            if (context != null) {
                fwuid = context.fwuid;
                appId = context.loaded.get("APPLICATION@markup://siteforce:loginApp2");
            }

            String localAppId = this.appId;
            if (fwuid == null || localAppId == null) {
                throw new SedifException("Unable to find app context in login process");
            } else {
                fields = new Fields();
                fields.put("message", getLoginPayload("", ""));
                fields.put("aura.context",
                        getAuraContext("siteforce:loginApp2", "APPLICATION@markup://siteforce:loginApp2", localAppId));
                fields.put("aura.token", "");

                result = httpClient.POST(URL_SEDIF_AUTHENTICATE_POST).content(new FormContentProvider(fields)).send();
                resultSt = result.getContentAsString();

                AuraResponse resp = gson.fromJson(resultSt, AuraResponse.class);
                Event event = resp.events.getFirst();
                String urlRedir = (String) event.attributes.values.get("url");

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

                // =====================================================================
                logger.debug("Step 3: Confirm login");

                result = httpClient.GET(urlRedir);
                resultSt = result.getContentAsString();

                // =====================================================================
                logger.debug("Step 4: Get contract info");

                result = httpClient
                        .GET("https://connexion.leaudiledefrance.fr/espace-particuliers/s/contrat?tab=Detail");
                resultSt = result.getContentAsString();

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
                Hashtable<String, Object> paramsSub = new Hashtable<String, Object>();
                logger.debug("Step 6: Get contract");
                fields = new Fields();
                fields.put("message",
                        getActionPayload("", "LTN009_ICL_ContratsGroupements", "getContratsGroupements", paramsSub));
                fields.put("aura.context", getAuraContext("siteforce:communityApp",
                        "APPLICATION@markup://siteforce:communityApp", "1233_vZx87dHGHIhS0MXRTe4D5w"));
                fields.put("aura.token", token);

                result = httpClient.POST(URL_SEDIF_SITE).content(new FormContentProvider(fields)).send();
                resultSt = result.getContentAsString();
                actions = gson.fromJson(resultSt, Actions.class);

                Contracts contracts = (Contracts) actions.actions.get(0).returnValue.returnValue;
                contractId = contracts.contrats.get(1).Id;
                // =====================================================================
                logger.debug("Step 7: Get contractDetails");
                paramsSub.clear();

                if (contractId != null) {
                    paramsSub.put("contratId", contractId);
                }

                fields = new Fields();
                fields.put("message",
                        getActionPayload("", "LTN008_ICL_ContratDetails", "getContratDetails", paramsSub));
                fields.put("aura.context", getAuraContext("siteforce:communityApp",
                        "APPLICATION@markup://siteforce:communityApp", "1233_vZx87dHGHIhS0MXRTe4D5w"));
                fields.put("aura.token", token);

                result = httpClient.POST(URL_SEDIF_SITE).content(new FormContentProvider(fields)).send();
                resultSt = result.getContentAsString();
                actions = gson.fromJson(resultSt, Actions.class);

                ContractDetail contractDetail = (ContractDetail) actions.actions.get(0).returnValue.returnValue;
                meterIdB = contractDetail.compteInfo.get(0).ELEMB;
                meterIdA = contractDetail.compteInfo.get(0).ELEMA;

                // =====================================================================
                logger.debug("Step 6: Get data");

                paramsSub.clear();
                paramsSub.put("TYPE_PAS", "JOURNEE");
                paramsSub.put("DATE_DEBUT", "2025-02-24");
                paramsSub.put("DATE_FIN", "2025-03-10");

                if (contractId != null) {
                    paramsSub.put("contractId", contractId);
                }

                if (meterIdB != null) {
                    paramsSub.put("NUMERO_COMPTEUR", meterIdB);
                }
                if (meterIdA != null) {
                    paramsSub.put("ID_PDS", meterIdA);
                }

                fields = new Fields();
                fields.put("message", getActionPayload("", "LTN015_ICL_ContratConsoHisto", "getData", paramsSub));
                fields.put("aura.context", getAuraContext("siteforce:communityApp",
                        "APPLICATION@markup://siteforce:communityApp", "1233_vZx87dHGHIhS0MXRTe4D5w"));
                fields.put("aura.token", token);

                result = httpClient.POST(URL_SEDIF_SITE).content(new FormContentProvider(fields)).send();
                resultSt = result.getContentAsString();
                logger.debug("aaaa");
            }

        } catch (Exception ex) {
            logger.debug("error durring connect : {}" + ex.getMessage());
        }
    }

    public String getAuraContext(String mainApp, String app, String appId) {
        AuraContext context = new AuraContext();
        context.mode = "PROD";
        context.fwuid = this.fwuid;
        context.app = mainApp;
        context.loaded.put(app, appId);
        context.globals = context.new Globals();
        context.uad = false;
        return gson.toJson(context);
    }

    public String getLoginPayload(String userName, String password) {
        Actions actions = new Actions();
        Action action = new Action();
        // action.id = "81;a";
        action.descriptor = "apex://LightningLoginFormController/ACTION$login";
        action.callingDescriptor = "markup://c:loginForm";

        action.params.put("username", userName);
        action.params.put("password", password);
        action.params.put("startUrl", "");
        actions.actions.add(action);

        return gson.toJson(actions);
    }

    public String getActionPayload(String nameSpace, String className, String methodName,
            Hashtable<String, Object> paramsSub) {
        Actions actions = new Actions();
        Action action = new Action();
        action.descriptor = "aura://ApexActionController/ACTION$execute";
        action.callingDescriptor = "UNKNOWN";

        action.params.put("namespace", nameSpace);
        action.params.put("classname", className);
        action.params.put("method", methodName);
        action.params.put("cacheable", false);
        action.params.put("isContinuation", false);
        action.params.put("params", paramsSub);
        actions.actions.add(action);

        return gson.toJson(actions);
    }

}
