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
import org.openhab.binding.sedif.internal.dto.Action.ReturnValue;
import org.openhab.binding.sedif.internal.dto.Actions;
import org.openhab.binding.sedif.internal.dto.AuraCommand;
import org.openhab.binding.sedif.internal.dto.AuraContext;
import org.openhab.binding.sedif.internal.dto.AuraResponse;
import org.openhab.binding.sedif.internal.dto.ContractDetail;
import org.openhab.binding.sedif.internal.dto.Contracts;
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
        return getContent(logger, bridgeHandler, null, url, httpClient, null);
    }

    public String getContent(BridgeSedifWebHandler bridgeHandler, @Nullable ThingSedifHandler handler, String url,
            @Nullable AuraCommand cmd) throws SedifException {
        return getContent(logger, bridgeHandler, handler, url, httpClient, cmd);
    }

    private String getContent(Logger logger, BridgeSedifWebHandler bridgeHandler, @Nullable ThingSedifHandler handler,
            String url, HttpClient httpClient, @Nullable AuraCommand cmd) throws SedifException {
        try {
            Request request = httpClient.newRequest(url);

            request = request.agent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:47.0) Gecko/20100101 Firefox/47.0");

            if (cmd == null) {
                request = request.method(HttpMethod.GET);
            } else {
                Hashtable<String, Object> paramsSub = cmd.getParamsSub();
                Fields fields = new Fields();

                request = request.method(HttpMethod.POST);

                AuraContext appCtx = bridgeHandler.getAppCtx();
                if (appCtx != null) {
                    fields.put("aura.context", getAuraContextPayload(appCtx));
                }

                String token = bridgeHandler.getToken();
                fields.put("aura.token", token);

                if (handler != null) {
                    String contractId = handler.getContractId();
                    String meterIdB = handler.getMeterIdB();
                    String meterIdA = handler.getMeterIdA();

                    if (contractId != null && !"".equals(contractId)) {
                        paramsSub.put("contratId", contractId);
                        paramsSub.put("contractId", contractId);
                    }
                    if (meterIdB != null && !"".equals(meterIdB)) {
                        paramsSub.put("NUMERO_COMPTEUR", meterIdB);
                    }

                    if (meterIdA != null && !"".equals(meterIdA)) {
                        paramsSub.put("ID_PDS", meterIdA);
                    }
                }

                String msg = getActionPayload(cmd);
                fields.put("message", msg);

                request = request.content(new FormContentProvider(fields));
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

    public @Nullable AuraResponse doAuth(String userName, String userPassword) throws SedifException {
        // =====================================================================
        logger.debug("Step 3: DoAuth");

        AuraCommand cmd = AuraCommand.make("", "", "");
        cmd.setUser(userName, userPassword);

        AuraResponse response = getData(bridgeHandler, bridgeHandler.getUrlSedifAuth(), cmd, AuraResponse.class);
        return response;
    }

    public @Nullable Contracts getContracts() throws SedifException {
        // =====================================================================
        logger.debug("Step 6: Get contractDetails");

        AuraCommand cmd = AuraCommand.make("", "LTN009_ICL_ContratsGroupements", "getContratsGroupements");
        Actions actions = getData(bridgeHandler, bridgeHandler.getUrlSedifSite(), cmd, Actions.class);
        ReturnValue retValue = actions.actions.get(0).returnValue;
        Contracts contracts = (Contracts) ((retValue == null) ? null : retValue.returnValue);
        return contracts;
    }

    public @Nullable ContractDetail getContractDetails(String contractId) throws SedifException {
        // =====================================================================
        logger.debug("Step 7: Get contractDetails");

        AuraCommand cmd = AuraCommand.make("", "LTN008_ICL_ContratDetails", "getContratDetails");
        Hashtable<String, Object> paramsSub = cmd.getParamsSub();
        if (!"".equals(contractId)) {
            paramsSub.put("contratId", contractId);
            paramsSub.put("contractId", contractId);
        }
        Actions actions = getData(bridgeHandler, bridgeHandler.getUrlSedifSite(), cmd, Actions.class);
        ReturnValue retValue = actions.actions.get(0).returnValue;
        ContractDetail contractDetail = (ContractDetail) ((retValue == null) ? null : retValue.returnValue);
        return contractDetail;
    }

    public @Nullable MeterReading getConsumptionData(ThingSedifHandler handler, LocalDate from, LocalDate to)
            throws SedifException {
        logger.debug("Step 6: Get data");

        AuraCommand cmd = AuraCommand.make("", "LTN015_ICL_ContratConsoHisto", "getData");
        Hashtable<String, Object> paramsSub = cmd.getParamsSub();

        paramsSub.put("TYPE_PAS", "JOURNEE"); // SEMAINE MOIS

        MeterReading meterReading = getMeasures(handler, bridgeHandler.getUrlSedifSite(), cmd, from, to);
        return meterReading;
    }

    private @Nullable MeterReading getMeasures(ThingSedifHandler handler, String apiUrl, AuraCommand cmd,
            LocalDate from, LocalDate to) throws SedifException {
        String dtStart = from.format(bridgeHandler.getApiDateFormat());
        String dtEnd = to.format(bridgeHandler.getApiDateFormat());

        Hashtable<String, Object> paramsSub = cmd.getParamsSub();
        paramsSub.put("DATE_DEBUT", dtStart);
        paramsSub.put("DATE_FIN", dtEnd);

        Actions actions = getData(bridgeHandler, handler, apiUrl, cmd, Actions.class);
        ReturnValue retValue = actions.actions.get(0).returnValue;
        MeterReading meterResponse = (MeterReading) ((retValue == null) ? null : retValue.returnValue);
        return meterResponse;
    }

    public <T> T getData(BridgeSedifWebHandler bridgeHandler, String url, @Nullable AuraCommand cmd, Class<T> clazz)
            throws SedifException {
        return getData(bridgeHandler, null, url, cmd, clazz);
    }

    public <T> T getData(BridgeSedifWebHandler bridgeHandler, @Nullable ThingSedifHandler handler, String url,
            @Nullable AuraCommand cmd, Class<T> clazz) throws SedifException {
        if (!bridgeHandler.isConnected()) {
            bridgeHandler.initialize();
        }

        int numberRetry = 0;
        SedifException lastException = null;
        logger.debug("getData begin {}: {}", clazz.getName(), url);

        while (numberRetry < 3) {
            try {
                String data = getContent(bridgeHandler, handler, url, cmd);

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

    public String getActionPayload(AuraCommand cmd) {
        Actions actions = new Actions();
        Action action = new Action();

        AuraContext ctx = bridgeHandler.getAppCtx();
        if (ctx != null) {
            if (ctx.app != null && ctx.app.indexOf("login") >= 0) {
                action.descriptor = "apex://LightningLoginFormController/ACTION$login";
                action.callingDescriptor = "markup://c:loginForm";
            } else {
                action.descriptor = "aura://ApexActionController/ACTION$execute";
                action.callingDescriptor = "UNKNOWN";
            }
        }

        String nameSpace = cmd.getNameSpace();
        String className = cmd.getClassName();
        String method = cmd.getMethodName();
        String userName = cmd.getUserName();
        String userPassword = cmd.getUserPassword();

        if (userName != null && !"".equals(userName)) {
            action.params.put("username", userName);
        }
        if (userPassword != null && !"".equals(userPassword)) {
            action.params.put("password", userPassword);
        }

        if (nameSpace != null && !"".equals(nameSpace)) {
            action.params.put("namespace", nameSpace);
        }

        if (className != null && !"".equals(className)) {
            action.params.put("classname", className);
        }

        if (method != null && !"".equals(method)) {
            action.params.put("method", method);
        }

        action.params.put("cacheable", false);
        action.params.put("isContinuation", false);
        action.params.put("params", cmd.getParamsSub());
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
