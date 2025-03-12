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
package org.openhab.binding.sedif.internal.api;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.LocalDate;
import java.util.Hashtable;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.Fields;
import org.openhab.binding.sedif.internal.dto.Action;
import org.openhab.binding.sedif.internal.dto.Actions;
import org.openhab.binding.sedif.internal.dto.AuraContext;
import org.openhab.binding.sedif.internal.dto.MeterReading;
import org.openhab.binding.sedif.internal.handler.BridgeSedifWebHandler;
import org.openhab.binding.sedif.internal.handler.ThingSedifHandler;
import org.openhab.binding.sedif.internal.types.SedifException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * {@link SedifHttpApi} wraps the Sedif Webservice.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class SedifHttpApi {

    private final Logger logger = LoggerFactory.getLogger(SedifHttpApi.class);
    private final Gson gson;
    private final HttpClient httpClient;
    private final BridgeSedifWebHandler bridgeHandler;

    public SedifHttpApi(BridgeSedifWebHandler bridgeHandler, Gson gson, HttpClient httpClient) {
        this.gson = gson;
        this.httpClient = httpClient;
        this.bridgeHandler = bridgeHandler;
    }

    public void removeAllCookie() {
        httpClient.getCookieStore().removeAll();
    }

    public String getLocation(ContentResponse response) {
        return response.getHeaders().get(HttpHeader.LOCATION);
    }

    public String getContent(String url) throws SedifException {
        return getContent(logger, bridgeHandler, url, httpClient, "");
    }

    public String getContent(BridgeSedifWebHandler handler, String url) throws SedifException {
        return getContent(logger, handler, url, httpClient, "");
    }

    private static String getContent(Logger logger, BridgeSedifWebHandler bridgeHandler, String url,
            HttpClient httpClient, String token) throws SedifException {
        try {
            Request request = httpClient.newRequest(url);

            request = request.agent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:47.0) Gecko/20100101 Firefox/47.0");
            request = request.method(HttpMethod.GET);
            if (!token.isEmpty()) {
                request = request.header("Authorization", "" + token);
                request = request.header("Accept", "application/json");
            }

            ContentResponse result = request.send();
            if (result.getStatus() == HttpStatus.TEMPORARY_REDIRECT_307
                    || result.getStatus() == HttpStatus.MOVED_TEMPORARILY_302) {
                String loc = result.getHeaders().get("Location");
                String newUrl = "";

                if (loc.startsWith("http://") || loc.startsWith("https://")) {
                    newUrl = loc;
                } else {
                    newUrl = bridgeHandler.getBaseUrl() + loc.substring(1);
                }

                request = httpClient.newRequest(newUrl);
                request = request.method(HttpMethod.GET);
                result = request.send();

                if (result.getStatus() == HttpStatus.TEMPORARY_REDIRECT_307
                        || result.getStatus() == HttpStatus.MOVED_TEMPORARILY_302) {
                    loc = result.getHeaders().get("Location");
                    String[] urlParts = loc.split("/");
                    if (urlParts.length < 4) {
                        throw new SedifException("malformed url : %s", loc);
                    }
                    return urlParts[3];
                }
            }
            if (result.getStatus() != 200) {
                throw new SedifException("Error requesting '%s': %s", url, result.getContentAsString());
            }

            String content = result.getContentAsString();
            logger.trace("getContent returned {}", content);
            return content;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new SedifException(e, "Error getting url: '%s'", url);
        }
    }

    public FormContentProvider getFormContent(String fieldName, String fieldValue) {
        Fields fields = new Fields();
        fields.put(fieldName, fieldValue);
        return new FormContentProvider(fields);
    }

    public @Nullable MeterReading getConsumptionData(ThingSedifHandler handler, LocalDate from, LocalDate to)
            throws SedifException {

        try {
            logger.debug("Step 6: Get data");

            Hashtable<String, Object> paramsSub = new Hashtable<String, Object>();
            Fields fields;

            paramsSub.clear();
            paramsSub.put("TYPE_PAS", "JOURNEE");
            paramsSub.put("DATE_DEBUT", "2025-02-24");
            paramsSub.put("DATE_FIN", "2025-03-10");

            String contractId = bridgeHandler.getContractId();
            String meterIdB = bridgeHandler.getMeterIdB();
            String meterIdA = bridgeHandler.getMeterIdA();
            String token = bridgeHandler.getToken();
            AuraContext appCtx = bridgeHandler.getAppCtx();

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
            if (appCtx != null) {
                fields.put("aura.context", getAuraContextPayload(appCtx));
            }
            fields.put("aura.token", token);

            ContentResponse result = httpClient.POST(bridgeHandler.getUrlSedifSite())
                    .content(new FormContentProvider(fields)).send();
            String resultSt = result.getContentAsString();
            Actions actions = gson.fromJson(resultSt, Actions.class);
            logger.debug("aaaa");

            return (MeterReading) actions.actions.get(0).returnValue.returnValue;

            // return getMeasures(handler, bridgeHandler.getUrlSedifSite(), from, to);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            throw new SedifException("error", e);
        }
    }

    private MeterReading getMeasures(ThingSedifHandler handler, String apiUrl, LocalDate from, LocalDate to)
            throws SedifException {
        String dtStart = from.format(bridgeHandler.getApiDateFormat());
        String dtEnd = to.format(bridgeHandler.getApiDateFormat());

        String url = String.format(apiUrl, dtStart, dtEnd);
        MeterReading meterResponse = getData(bridgeHandler, url, MeterReading.class);
        return meterResponse;
    }

    private <T> T getData(BridgeSedifWebHandler handler, String url, Class<T> clazz) throws SedifException {
        if (!bridgeHandler.isConnected()) {
            bridgeHandler.initialize();
        }

        int numberRetry = 0;
        SedifException lastException = null;
        logger.debug("getData begin {}: {}", clazz.getName(), url);

        while (numberRetry < 3) {
            try {
                String data = getContent(handler, url);

                if (!data.isEmpty()) {
                    try {
                        T result = Objects.requireNonNull(gson.fromJson(data, clazz));
                        logger.debug("getData success {}: {}", clazz.getName(), url);
                        return result;
                    } catch (JsonSyntaxException e) {
                        logger.debug("Invalid JSON response not matching {}: {}", clazz.getName(), data);
                        throw new SedifException(e, "Requesting '%s' returned an invalid JSON response", url);
                    }
                }
            } catch (SedifException ex) {
                lastException = ex;

                logger.debug("getData error {}: {} , retry{}", clazz.getName(), url, numberRetry);

                // try to reinit connection, fail after 3 attemps
                bridgeHandler.connectionInit();
            }
            numberRetry++;
        }

        logger.debug("getData error {}: {} , maxRetry", clazz.getName(), url);

        throw Objects.requireNonNull(lastException);
    }

    public String getAuraContext(String mainApp, String app, String appId, String fwuid) {
        AuraContext context = new AuraContext();
        context.mode = "PROD";
        context.fwuid = fwuid;
        context.app = mainApp;
        context.loaded.put(app, appId);
        context.globals = context.new Globals();
        context.uad = false;
        return gson.toJson(context);
    }

    public String getAuraContextPayload(AuraContext context) {
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

    public @Nullable AuraContext extractAuraContext(String html) throws SedifException {
        try {
            int pos1 = html.indexOf("resources.js");
            AuraContext context = null;
            if (pos1 >= 0) {
                String sub1 = html.substring(0, pos1 + 1);
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

                return context;
            }
        } catch (UnsupportedEncodingException e) {
            throw new SedifException("Can't decode context in extractAuraContext {}", e.getMessage(), e);
        }

        return null;
    }

}
