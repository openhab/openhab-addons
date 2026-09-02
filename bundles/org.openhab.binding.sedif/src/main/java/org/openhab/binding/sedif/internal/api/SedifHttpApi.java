/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import java.net.HttpCookie;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.Fields;
import org.openhab.binding.sedif.internal.constants.SedifBindingConstants;
import org.openhab.binding.sedif.internal.dto.Action;
import org.openhab.binding.sedif.internal.dto.Action.ReturnValue;
import org.openhab.binding.sedif.internal.dto.Actions;
import org.openhab.binding.sedif.internal.dto.AuraCommand;
import org.openhab.binding.sedif.internal.dto.AuraContext;
import org.openhab.binding.sedif.internal.dto.AuraResponse;
import org.openhab.binding.sedif.internal.dto.Contract;
import org.openhab.binding.sedif.internal.dto.ContractDetail;
import org.openhab.binding.sedif.internal.dto.ContractDetail.CompteInfo;
import org.openhab.binding.sedif.internal.dto.Contracts;
import org.openhab.binding.sedif.internal.dto.Event;
import org.openhab.binding.sedif.internal.dto.MeterReading;
import org.openhab.binding.sedif.internal.types.CommunicationFailedException;
import org.openhab.binding.sedif.internal.types.InvalidSessionException;
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

    private @Nullable AuraContext appCtx;
    private @Nullable String token = "";
    protected boolean connected = false;

    public static final String SEDIF_DOMAIN = ".leaudiledefrance.fr";
    public static final String BASE_URL = "https://connexion" + SEDIF_DOMAIN;
    public static final String URL_SEDIF_AUTHENTICATE = BASE_URL + "/s/login/";
    public static final String URL_SEDIF_AUTHENTICATE_POST = BASE_URL
            + "/s/sfsites/aura?r=1&other.LightningLoginForm.login=1";

    public static final String URL_SEDIF_CONTRAT = BASE_URL + "/espace-particuliers/s/contrat?tab=Detail";
    public static final String URL_SEDIF_SITE = BASE_URL
            + "/espace-particuliers/s/sfsites/aura?r=36&aura.ApexAction.execute=1";

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:47.0) Gecko/20100101 Firefox/47.0";

    private static final DateTimeFormatter API_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private HashMap<String, Contract> contractDict = new HashMap<>();

    public SedifHttpApi(Gson gson, HttpClient httpClient) {
        this.gson = gson;
        this.httpClient = httpClient;
    }

    public void connectionInit(String userName, String userPassword) throws SedifException {
        if (connected) {
            return;
        }

        removeAllCookie();

        // =====================================================================
        // Step 1: getting salesforces context from login page
        // =====================================================================
        String resultSt = getContent(SedifHttpApi.URL_SEDIF_AUTHENTICATE);
        appCtx = extractAuraContext(resultSt);

        if (appCtx == null) {
            throw new SedifException("Unable to find app context in login process");
        } else {
            logger.debug("Account {}: Successfully retrieved context", userName);
        }

        // =====================================================================
        // Step 2: Authenticate
        // =====================================================================
        AuraResponse resp = doAuth(userName, userPassword);

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
        resultSt = getContent(urlRedir);

        // =====================================================================
        // Step 4: Get contract page
        // =====================================================================
        resultSt = getContent(SedifHttpApi.URL_SEDIF_CONTRAT);
        appCtx = extractAuraContext(resultSt);

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
            logger.debug("Account: Successfully acquire token");
        }

        // =====================================================================
        // Step 6a: Get contract
        // =====================================================================
        Contracts contracts = getContracts();
        if (contracts != null && contracts.contracts != null) {
            for (Contract contract : contracts.contracts) {
                String contractName = contract.name;
                if (contractName != null) {
                    contractDict.put(contractName, contract);
                }
            }
        }

        connected = true;
    }

    public boolean isConnected() {
        return connected;
    }

    public void disconnect() {
        connected = false;
    }

    public void removeAllCookie() {
        httpClient.getCookieStore().removeAll();
    }

    public String getContent(String url) throws SedifException {
        return getContent("", null, url, null);
    }

    private String getContent(String contractId, @Nullable CompteInfo meterInfo, String url, @Nullable AuraCommand cmd)
            throws SedifException {
        try {
            Request request = httpClient.newRequest(url);
            request = request.timeout(SedifBindingConstants.REQUEST_TIMEOUT, TimeUnit.SECONDS);
            request = request.agent(USER_AGENT);

            if (cmd == null) {
                request = request.method(HttpMethod.GET);
            } else {
                Hashtable<String, Object> paramsSub = cmd.getParamsSub();
                Fields fields = new Fields();

                request = request.method(HttpMethod.POST);

                if (appCtx != null) {
                    fields.put("aura.context", getAuraContextPayload(appCtx));
                }

                fields.put("aura.token", token);

                // We put the to key syntax contratId & contractId because of an error on Sedif side.
                // Some api calls need the contratId syntax, and other the contactId syntax, so putting it both is ok
                if (!contractId.isBlank()) {
                    paramsSub.put("contratId", contractId);
                    paramsSub.put("contractId", contractId);
                }

                // If we have a meterInfo in params, put this info on request
                // We need this to read meter specific data like consumption
                if (meterInfo != null) {
                    String meterIdB = meterInfo.eLmb;
                    String meterIdA = meterInfo.eLma;

                    if (!meterIdB.isBlank()) {
                        paramsSub.put("NUMERO_COMPTEUR", meterIdB);
                    }

                    if (!meterIdA.isBlank()) {
                        paramsSub.put("ID_PDS", meterIdA);
                    }
                }

                String msg = getActionPayload(cmd);
                fields.put("message", msg);

                request = request.content(new FormContentProvider(fields));
            }

            ContentResponse result = request.send();

            if (result.getStatus() != HttpStatus.OK_200) {
                throw new SedifException("Error requesting '%s': %s", url, result.getContentAsString());
            }

            return result.getContentAsString();
        } catch (ExecutionException | TimeoutException e) {
            throw new CommunicationFailedException("Error getting url: '%s'", e, url);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CommunicationFailedException("Error getting url: '%s'", e, url);
        }
    }

    public @Nullable AuraResponse doAuth(String userName, String userPassword) throws SedifException {
        // =====================================================================
        logger.trace("Step 3: Invoke the Sedif auth endpoint to do the login");

        AuraCommand cmd = AuraCommand.make("", "", "");
        cmd.setUser(userName, userPassword);

        return getData("", null, URL_SEDIF_AUTHENTICATE_POST, cmd, AuraResponse.class);
    }

    public @Nullable Contracts getContracts() throws SedifException {
        // =====================================================================
        logger.trace("Step 6: Get the contracts associated with the sedif accounts");

        AuraCommand cmd = AuraCommand.make("", "LTN009_ICL_ContratsGroupements", "getContratsGroupements");
        Actions actions = getData("", null, URL_SEDIF_SITE, cmd, Actions.class);
        if (actions != null) {
            ReturnValue retValue = actions.actions.get(0).returnValue;
            return (Contracts) ((retValue == null) ? null : retValue.returnValue);
        }

        return null;
    }

    public @Nullable ContractDetail getContractDetails(String contractId) throws SedifException {
        // =====================================================================
        logger.trace("Step 7: Get the contract details to have information about end user");

        AuraCommand cmd = AuraCommand.make("", "LTN008_ICL_ContratDetails", "getContratDetails");
        Hashtable<String, Object> paramsSub = cmd.getParamsSub();

        // We put the to key syntax contratId & contractId because of an error on Sedif side.
        // Some api calls need the contratId syntax, and other the contactId syntax, so putting it both is ok
        if (!contractId.isBlank()) {
            paramsSub.put("contratId", contractId);
            paramsSub.put("contractId", contractId);
        }
        Actions actions = getData(contractId, null, URL_SEDIF_SITE, cmd, Actions.class);
        if (actions != null) {
            ReturnValue retValue = actions.actions.get(0).returnValue;
            return (ContractDetail) ((retValue == null) ? null : retValue.returnValue);
        }
        return null;
    }

    public @Nullable MeterReading getConsumptionData(String contractId, @Nullable CompteInfo meterInfo, LocalDate from,
            LocalDate to) throws SedifException {
        logger.trace(" Step 8: getConsumptionData() {} {} {}", contractId, from, to);

        AuraCommand cmd = AuraCommand.make("", "LTN015_ICL_ContratConsoHisto", "getData");
        Hashtable<String, Object> paramsSub = cmd.getParamsSub();

        paramsSub.put("TYPE_PAS", "JOURNEE"); // SEMAINE MOIS

        return getMeasures(contractId, meterInfo, URL_SEDIF_SITE, cmd, from, to);
    }

    private @Nullable MeterReading getMeasures(String contractId, @Nullable CompteInfo meterInfo, String apiUrl,
            AuraCommand cmd, LocalDate from, LocalDate to) throws SedifException {
        String dtStart = from.format(API_DATE_FORMAT);
        String dtEnd = to.format(API_DATE_FORMAT);

        Hashtable<String, Object> paramsSub = cmd.getParamsSub();
        paramsSub.put("DATE_DEBUT", dtStart);
        paramsSub.put("DATE_FIN", dtEnd);

        Actions actions = getData(contractId, meterInfo, apiUrl, cmd, Actions.class);
        if (actions != null) {
            ReturnValue retValue = actions.actions.get(0).returnValue;
            return (MeterReading) ((retValue == null) ? null : retValue.returnValue);
        }
        return null;
    }

    private @Nullable <T> T getData(String contractId, @Nullable CompteInfo meterInfo, String url,
            @Nullable AuraCommand cmd, Class<T> clazz) throws SedifException {
        logger.debug("getData begin {}: {}", clazz.getName(), url);

        try {
            String data = getContent(contractId, meterInfo, url, cmd);
            if (data.contains("aura:invalidSession")) {
                throw new InvalidSessionException("Communication with sedif failed, session invalid");
            }

            if (!data.isEmpty()) {
                try {
                    T result = Objects.requireNonNull(gson.fromJson(data, clazz));
                    logger.debug("getData success {}: {}", clazz.getName(), url);
                    return result;
                } catch (JsonSyntaxException e) {
                    logger.debug("Invalid JSON response not matching {}: {}", clazz.getName(), data);
                    throw new SedifException("Requesting '%s' returned an invalid JSON response", e, url);
                }
            }
        } catch (SedifException ex) {
            throw ex;
        }
        return null;
    }

    private String getAuraContextPayload(@Nullable AuraContext context) {
        if (context == null) {
            return "";
        }
        return gson.toJson(context);
    }

    private String getActionPayload(AuraCommand cmd) {
        Actions actions = new Actions();
        Action action = new Action();

        AuraContext lcAppCtx = appCtx;
        if (lcAppCtx != null) {
            String app = lcAppCtx.app;
            if (app != null && app.contains("login")) {
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

        if (userName != null && !userName.isBlank()) {
            action.params.put("username", userName);
        }
        if (userPassword != null && !userPassword.isBlank()) {
            action.params.put("password", userPassword);
        }

        if (nameSpace != null && !nameSpace.isBlank()) {
            action.params.put("namespace", nameSpace);
        }

        if (className != null && !className.isBlank()) {
            action.params.put("classname", className);
        }

        if (method != null && !method.isBlank()) {
            action.params.put("method", method);
        }

        action.params.put("cacheable", false);
        action.params.put("isContinuation", false);
        action.params.put("params", cmd.getParamsSub());
        actions.actions.add(action);

        return gson.toJson(actions);
    }

    public @Nullable AuraContext extractAuraContext(String html) throws SedifException {
        int pos1 = html.indexOf("resources.js");
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
            sub2 = URLDecoder.decode(sub2, StandardCharsets.UTF_8);

            return gson.fromJson(sub2, AuraContext.class);
        }

        return null;
    }

    public @Nullable Contract getContract(String contractName) {
        return contractDict.get(contractName);
    }

    public Map<String, Contract> getAllContracts() {
        return contractDict;
    }
}
