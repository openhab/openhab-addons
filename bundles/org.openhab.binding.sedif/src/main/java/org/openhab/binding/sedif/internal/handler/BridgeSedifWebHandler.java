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
import org.openhab.binding.sedif.internal.dto.Actions;
import org.openhab.binding.sedif.internal.dto.Actions.Action;
import org.openhab.binding.sedif.internal.dto.AuraContext;
import org.openhab.binding.sedif.internal.dto.AuraResponse;
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

    protected final Gson gson;

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

            logger.debug("Step 1: getting authentification");
            String data = sedifApi.getContent(URL_SEDIF_AUTHENTICATE);

            logger.debug("Reception request SAML");
            // Document htmlDocument = Jsoup.parse(data);

            Actions actions = new Actions();
            Action action = actions.new Action();
            action.id = "81;a";
            action.descriptor = "apex://LightningLoginFormController/ACTION$login";
            action.callingDescriptor = "markup://c:loginForm";

            action.params.put("username", "");
            action.params.put("password", "");
            action.params.put("startUrl", "");

            actions.actions.add(action);

            String actionPayLoad = gson.toJson(actions);

            AuraContext context = new AuraContext();
            context.mode = "PROD";
            context.fwuid = "c1ItM3NYNWFUOE5oQkUwZk1sYW1vQWg5TGxiTHU3MEQ5RnBMM0VzVXc1cmcxMS4zMjc2OC4z";
            context.app = "siteforce:loginApp2";
            context.loaded.put("APPLICATION@markup://siteforce:loginApp2", "1155_2ewdZIvT00nk2lBbLpJljQ");
            context.globals = context.new Globals();
            context.uad = false;

            String contextPayLoad = gson.toJson(context);

            Fields fields = new Fields();
            fields.put("message", actionPayLoad);
            fields.put("aura.context", contextPayLoad);
            // fields.put("aura.pageURI", "/s/login");
            fields.put("aura.token", "");

            ContentResponse result = httpClient.POST(URL_SEDIF_AUTHENTICATE_POST)
                    .content(new FormContentProvider(fields)).send();

            String t1 = result.getContentAsString();
            logger.debug("aaaa");
            AuraResponse resp = gson.fromJson(t1, AuraResponse.class);

            Event event = resp.events.getFirst();
            String urlRedir = (String) event.attributes.values.get("url");

            String sid = "";
            String[] parts = urlRedir.split("&");
            for (String part : parts) {
                if (part.indexOf("sid") >= 0) {
                    sid = part;
                }
            }

            sid = sid.replace("sid=", "");

            ContentResponse result4 = httpClient.GET(urlRedir);
            String t4 = result4.getContentAsString();

            ContentResponse result3 = httpClient
                    .GET("https://connexion.leaudiledefrance.fr/espace-particuliers/s/contrat?tab=Detail");
            String t3 = result3.getContentAsString();

            String cookieKey = "__Host-ERIC_PROD683902336057978042";
            List<HttpCookie> lCookie = httpClient.getCookieStore().getCookies();

            String cookieVal = "";
            for (HttpCookie cookie : lCookie) {
                if (cookie.getName().indexOf("__Host-ERIC_") >= 0) {
                    cookieVal = cookie.getValue();
                }
            }

            // "eyJub25jZSI6IklnVjNDbnJWWUJVckZIdlFnWkxYdGU3OVRrNjVDZlU1amhQTk9YOE1aYzBcdTAwM2QiLCJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsImtpZCI6IntcInRcIjpcIjAwRGJLMDAwMDAwMDAwMVwiLFwidlwiOlwiMDJHYkswMDAwMDAwMDFkXCIsXCJhXCI6XCJjYWltYW5zaWduZXJcIn0iLCJjcml0IjpbImlhdCJdLCJpYXQiOjE3NDE2ODk2OTA0MTcsImV4cCI6MH0=..DjWiGxTRsTrwOeOcfb6D7dObzhGP1AAXvFeSu7FKegE="

            Actions actions2 = new Actions();
            Action action2 = actions2.new Action();
            action2.id = "215;a";
            action2.descriptor = "aura://ApexActionController/ACTION$execute";
            action2.callingDescriptor = "UNKNOWN";

            action2.params.put("namespace", "");
            action2.params.put("classname", "LTN015_ICL_ContratConsoHisto");
            action2.params.put("method", "getData");
            action2.params.put("cacheable", false);
            action2.params.put("isContinuation", false);

            Hashtable<String, Object> paramsSub = new Hashtable<String, Object>();
            paramsSub.put("contractId", "sf4gNd4bhyssgoLosTvGqPCTbHQPIXcwmuAUeNkpVj3Q%2Fxy5Yv%2B4te%2BxqGLaz1nv");
            paramsSub.put("TYPE_PAS", "JOURNEE");
            paramsSub.put("DATE_DEBUT", "2025-02-24");
            paramsSub.put("DATE_FIN", "2025-03-10");
            paramsSub.put("NUMERO_COMPTEUR", "0S8Nx%2B7VCog7Gosbt49fEu%2Boxy5s5pMnS1C6T15rQijpsS9Q%2BhZPa3R0u9ZH6nty");
            paramsSub.put("ID_PDS", "FDrF%2BQ7ifdzFSdqTAS%2BYNWhLTojTb7%2FEQuIqa%2B9nm5I%3D");

            action2.params.put("params", paramsSub);

            actions2.actions.add(action2);

            String actionPayLoad2 = gson.toJson(actions2);

            AuraContext context2 = new AuraContext();
            context2.mode = "PROD";
            context2.fwuid = "c1ItM3NYNWFUOE5oQkUwZk1sYW1vQWg5TGxiTHU3MEQ5RnBMM0VzVXc1cmcxMS4zMjc2OC4z";
            context2.app = "siteforce:communityApp";
            context2.loaded.put("APPLICATION@markup://siteforce:communityApp", "1233_vZx87dHGHIhS0MXRTe4D5w");
            context2.loaded.put("COMPONENT@markup://forceCommunity:embeddedServiceSidebar",
                    "1204_aG5sQosCeK7Nw4-6XGSxmw");
            context2.loaded.put("COMPONENT@markup://instrumentation:o11ySecondaryLoader", "387_xvXc6AnLRgqK6TofLxISPw");

            context2.globals = context.new Globals();
            context2.uad = false;

            String contextPayLoad2 = gson.toJson(context2);

            Fields fields2 = new Fields();
            fields2.put("message", actionPayLoad2);
            fields2.put("aura.context", contextPayLoad2);
            fields2.put("aura.pageURI",
                    "/espace-particuliers/s/contrat?tab=Detail#sf4gNd4bhyssgoLosTvGqPCTbHQPIXcwmuAUeNkpVj3Q%2Fxy5Yv%2B4te%2BxqGLaz1nv");
            fields2.put("aura.token", cookieVal);

            ContentResponse result2 = httpClient.POST(
                    "https://connexion.leaudiledefrance.fr/espace-particuliers/s/sfsites/aura?r=36&aura.ApexAction.execute=1")
                    .content(new FormContentProvider(fields2)).send();
            String t2 = result2.getContentAsString();
            logger.debug("aaaa");

            // https://connexion.leaudiledefrance.fr/espace-particuliers/s/sfsites/aura?r=36&aura.ApexAction.execute=1

            // aura.context:
            // {"mode":"PROD","fwuid":"c1ItM3NYNWFUOE5oQkUwZk1sYW1vQWg5TGxiTHU3MEQ5RnBMM0VzVXc1cmcxMS4zMjc2OC4z","app":"siteforce:loginApp2","loaded":{"APPLICATION@markup://siteforce:loginApp2":"1155_2ewdZIvT00nk2lBbLpJljQ"},"dn":[],"globals":{},"uad":false}

            /*
             * Element el = htmlDocument.select("form").first();
             * Element samlInput = el.select("input[name=SAMLRequest]").first();
             *
             * logger.debug("Step 2: send SSO SAMLRequest");
             * ContentResponse result = httpClient.POST(el.attr("action"))
             * .content(sedifApi.getFormContent("SAMLRequest", samlInput.attr("value"))).send();
             * if (result.getStatus() != HttpStatus.FOUND_302) {
             * throw new SedifException("Connection failed step 2");
             * }
             */
            logger.debug("aaaa");
        } catch (Exception ex) {
            logger.debug("error durring connect : {}" + ex.getMessage());
        }

        // has we reconnect, remove all previous cookie to start from fresh session
        /*
         *
         *
         * sedifApi.addCookie(SedifConfiguration.INTERNAL_AUTH_ID, lcConfig.internalAuthId);
         *
         *
         *
         * logger.debug("Reception request SAML");
         * Document htmlDocument = Jsoup.parse(data);
         * Element el = htmlDocument.select("form").first();
         * Element samlInput = el.select("input[name=SAMLRequest]").first();
         *
         * logger.debug("Step 2: send SSO SAMLRequest");
         * ContentResponse result = httpClient.POST(el.attr("action"))
         * .content(sedifApi.getFormContent("SAMLRequest", samlInput.attr("value"))).send();
         * if (result.getStatus() != HttpStatus.FOUND_302) {
         * throw new SedifException("Connection failed step 2");
         * }
         *
         * logger.debug("Get the location and the ReqID");
         * Matcher m = REQ_PATTERN.matcher(sedifApi.getLocation(result));
         * if (!m.find()) {
         * throw new SedifException("Unable to locate ReqId in header");
         * }
         *
         * String reqId = m.group(1);
         * String authenticateUrl = URL_MON_COMPTE
         * +
         * "/auth/json/authenticate?realm=/sedis&forward=true&spEntityID=SP-ODW-PROD&goto=/auth/SSOPOST/metaAlias/sedis/providerIDP?ReqID%"
         * + reqId + "%26index%3Dnull%26acsURL%3D" + BASE_URL
         * +
         * "/saml/SSO%26spEntityID%3DSP-ODW-PROD%26binding%3Durn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST&AMAuthCookie=";
         *
         * logger.debug("Step 3: auth1 - retrieve the template, thanks to cookie internalAuthId user is already set"
         * );
         * result = httpClient.POST(authenticateUrl).header("X-NoSession", "true").header("X-Password", "anonymous")
         * .header("X-Requested-With", "XMLHttpRequest").header("X-Username", "anonymous").send();
         * if (result.getStatus() != HttpStatus.OK_200) {
         * throw new SedifException("Connection failed step 3 - auth1: %s", result.getContentAsString());
         * }
         */

    }

}
