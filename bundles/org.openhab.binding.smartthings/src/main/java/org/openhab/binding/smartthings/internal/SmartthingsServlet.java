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
package org.openhab.binding.smartthings.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.smartthings.internal.api.SmartthingsApi;
import org.openhab.binding.smartthings.internal.api.SmartthingsNetworkConnector;
import org.openhab.binding.smartthings.internal.dto.AppResponse;
import org.openhab.binding.smartthings.internal.dto.ConfigurationResponse;
import org.openhab.binding.smartthings.internal.dto.ConfigurationResponse.configurationData.Page;
import org.openhab.binding.smartthings.internal.dto.ConfigurationResponse.configurationData.initialize;
import org.openhab.binding.smartthings.internal.dto.LifeCycle;
import org.openhab.binding.smartthings.internal.dto.LifeCycle.Data;
import org.openhab.binding.smartthings.internal.dto.SMEvent;
import org.openhab.binding.smartthings.internal.dto.SMEvent.device;
import org.openhab.binding.smartthings.internal.dto.SmartthingsLocation;
import org.openhab.binding.smartthings.internal.handler.SmartthingsBridgeHandler;
import org.openhab.binding.smartthings.internal.type.SmartthingsException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Receives all Http data from the Smartthings Hub
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("serial")
public class SmartthingsServlet extends SmartthingsBaseServlet {
    private static final String PATH = "/smartthings";

    private final Logger logger = LoggerFactory.getLogger(SmartthingsServlet.class);

    private Gson gson = new Gson();

    // Keys present in the index.html
    private static final String KEY_SETUP_URI = "setup.uri";
    private static final String KEY_REDIRECT_URI = "redirectUri";
    private static final String KEY_LOCATIONID_OPTION = "locationId.Option";
    private static final String KEY_POOL_STATUS = "poolStatus";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_APP_ID = "appId";

    // Page templates
    private @Nullable String indexTemplate;
    private @Nullable String selectLocationTemplate;
    private @Nullable String poolTemplate;
    private @Nullable String confirmationTemplate;

    private String installedLocation = "";
    private String installedAppId = "";

    public Boolean setupInProgress = false;

    public SmartthingsServlet(SmartthingsBridgeHandler bridgeHandler, HttpService httpService,
            SmartthingsNetworkConnector networkConnector, String token) {

        super(bridgeHandler, httpService, networkConnector, token);
    }

    @Activate
    public void activate() {
        try {
            Dictionary<String, String> servletParams = new Hashtable<String, String>();
            logger.info("registerServlet:" + PATH);
            httpService.registerServlet(PATH, this, servletParams, httpService.createDefaultHttpContext());
            httpService.registerResources(PATH + "/img", "img", null);
            httpService.registerResources(PATH + "/web", "web", null);

            this.indexTemplate = readTemplate("index.html");
            this.selectLocationTemplate = readTemplate("selectlocation.html");
            this.poolTemplate = readTemplate("pool.ajax");
            this.confirmationTemplate = readTemplate("confirmation.html");
        } catch (ServletException | IOException | NamespaceException e) {
            logger.warn("Could not start Smartthings servlet service: {}", e.getMessage());
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        try {
            httpService.unregister(PATH);
        } catch (IllegalArgumentException e) {
            logger.warn("Could not stop Smartthings servlet service: {}", e.getMessage());
        }
    }

    @Override
    public void init(@Nullable ServletConfig servletConfig) throws ServletException {
        logger.info("SmartthingsServlet:init");
    }

    @Override
    protected void service(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp)
            throws ServletException, IOException {
        final Map<String, String> replaceMap = new HashMap<>();
        StringBuffer optionBuffer = new StringBuffer();
        SmartthingsApi api = bridgeHandler.getSmartthingsApi();

        if (req == null) {
            logger.debug("SmartthingsServlet.service unexpectedly received a null request. Request not processed");
            return;
        }
        if (resp == null) {
            return;
        }

        logger.info("SmartthingsServlet:service");
        String path = req.getRequestURI();

        if (path != null && !path.contains("/smartthings/cb")) {
            String template = "";
            if (path.contains("index")) {
                template = indexTemplate;
            } else if (path.contains("selectlocation")) {
                template = selectLocationTemplate;
            } else if (path.contains("pool")) {
                template = poolTemplate;
            } else if (path.contains("confirmation")) {
                template = confirmationTemplate;
            } else {
                template = indexTemplate;
            }

            if (selectLocationTemplate != null && selectLocationTemplate.equals(template)) {
                SetupApp();

                try {
                    SmartthingsLocation[] locationList = api.GetAllLocations();
                    for (SmartthingsLocation loc : locationList) {
                        optionBuffer.append("<option value=\"" + loc.locationId + "\">" + loc.name + "</option>");
                    }
                } catch (SmartthingsException ex) {
                    optionBuffer.append("Unable to retrieve locations !!");
                }
                setupInProgress = true;
            } else if (poolTemplate != null && poolTemplate.equals(template)) {
                if (setupInProgress) {
                    replaceMap.put(KEY_POOL_STATUS, "false");
                } else {
                    replaceMap.put(KEY_POOL_STATUS, "true");
                }
            } else if (confirmationTemplate != null && confirmationTemplate.equals(template)) {
                replaceMap.put(KEY_LOCATION, installedLocation);
                replaceMap.put(KEY_APP_ID, installedAppId);
            }

            StringBuffer requestUrl = req.getRequestURL();
            String servletBaseUrl = requestUrl != null ? requestUrl.toString() : "";
            String servletBaseURLSecure = servletBaseUrl.replace("http://", "https://").replace("8080", "8443");

            String locationId = "cb73e411-15b4-40e8-b6cd-f9a34f6ced4b";
            String uri = "https://account.smartthings.com/login?redirect=https%3A%2F%2Fstrongman-regional.api.smartthings.com%2F%3FappId%3D";
            uri = uri + bridgeHandler.getAppId();
            uri = uri + "%26locationId%3D" + locationId;
            uri = uri + "%26appType%3DENDPOINTAPP";
            uri = uri + "%26language%3Den";
            uri = uri + "%26clientOS%3Dweb";

            replaceMap.put(KEY_SETUP_URI, uri);
            replaceMap.put(KEY_REDIRECT_URI, servletBaseURLSecure);
            replaceMap.put(KEY_LOCATIONID_OPTION, optionBuffer.toString());

            if (template != null) {
                resp.getWriter().append(replaceKeysFromMap(template, replaceMap));
                resp.getWriter().close();
            }
        } else {

            BufferedReader rdr = new BufferedReader(req.getReader());
            String s = rdr.lines().collect(Collectors.joining());

            LifeCycle resultObj = gson.fromJson(s, LifeCycle.class);

            if (resultObj != null) {
                if (resultObj.lifecycle.equals("EVENT")) {
                    Data data = resultObj.eventData;
                    String deviceId = data.events[0].deviceEvent.deviceId;
                    String componentId = data.events[0].deviceEvent.componentId;
                    String capa = data.events[0].deviceEvent.capability;
                    String atttr = data.events[0].deviceEvent.attribute;
                    String value = data.events[0].deviceEvent.value;

                    logger.info("EVENT: {} {} {} {} {}", deviceId, componentId, capa, atttr, value);

                } else if (resultObj.lifecycle.equals("INSTALL")) {
                    logger.info("");
                    // String token = resultObj.installData.authToken;
                    installedAppId = resultObj.installData.installedApp.installedAppId;

                    try {
                        SmartthingsLocation loc = api.GetLocation(resultObj.installData.installedApp.locationId);
                        installedLocation = loc.name;
                    } catch (SmartthingsException ex) {
                        installedLocation = "Unable to retrieve location!!";
                    }

                    setupInProgress = false;
                    logger.info("");

                } else if (resultObj.lifecycle.equals("UPDATE")) {
                    String token = resultObj.updateData.authToken;
                    String installedAppId = resultObj.updateData.installedApp.installedAppId;
                    String subscriptionUri = "https://api.smartthings.com/v1/installedapps/" + installedAppId
                            + "/subscriptions";

                    networkConnector.DoRequest(JsonObject.class, subscriptionUri, null, token, "", HttpMethod.GET);

                    SMEvent evt = new SMEvent();
                    evt.sourceType = "DEVICE";
                    evt.device = new device("97806abc-ce85-4b28-9df2-31e33323cf62", "main", true, null);

                    String body = gson.toJson(evt);
                    networkConnector.DoRequest(JsonObject.class, subscriptionUri, null, token, body, HttpMethod.POST);

                    evt = new SMEvent();
                    evt.sourceType = "DEVICE";
                    evt.device = new device("ee87617f-0c84-40a3-be25-e70e53f3fc6a", "main", true, null);

                    body = gson.toJson(evt);
                    networkConnector.DoRequest(JsonObject.class, subscriptionUri, null, token, body, HttpMethod.POST);

                    logger.info("UPDATE");
                } else if (resultObj.lifecycle.equals("EXECUTE")) {
                    logger.info("EXCUTE");

                } else if (resultObj.lifecycle.equals("CONFIGURATION")
                        && resultObj.configurationData.phase().equals("INITIALIZE")) {
                    ConfigurationResponse response = new ConfigurationResponse();
                    response.configurationData = response.new configurationData();

                    initialize init = response.configurationData.new initialize();
                    response.configurationData.initialize = init;
                    init.name = "Openhab";
                    init.description = "Openhab";
                    init.firstPageId = "1";
                    init.id = "Openhab";

                    init.permissions = new String[1];
                    init.permissions[0] = "r:devices:*";

                    String responseSt = gson.toJson(response);
                    resp.getWriter().print(responseSt);
                } else if (resultObj.lifecycle.equals("CONFIGURATION")
                        && resultObj.configurationData.phase().equals("PAGE")) {

                    ConfigurationResponse response = new ConfigurationResponse();
                    response.configurationData = response.new configurationData();

                    Page page1 = response.configurationData.new Page();
                    response.configurationData.page = page1;
                    page1.pageId = "1";
                    page1.nextPageId = null;
                    page1.previousPageId = null;
                    page1.name = "Openhab";
                    page1.complete = true;

                    String responseSt = gson.toJson(response);
                    resp.getWriter().print(responseSt);
                } else if (resultObj.lifecycle.equals("CONFIRMATION")) {
                    String appId = resultObj.confirmationData.appId();
                    String confirmUrl = resultObj.confirmationData.confirmationUrl();

                    bridgeHandler.setAppId(appId);

                    String responseSt = "{";

                    responseSt = responseSt + "\"targetUrl\": \"" + confirmUrl + "\"";
                    responseSt = responseSt + "}";
                    resp.getWriter().print(responseSt);

                    try {
                        Thread.sleep(2000);
                        this.networkConnector.DoBasicRequest(confirmUrl, null, "", "", HttpMethod.GET);
                    } catch (Exception ex) {
                        logger.error("error during confirmation {}", confirmUrl);
                    }

                    try {
                        api.CreateAppOAuth(appId);
                    } catch (SmartthingsException ex) {
                        logger.error("Unable to setup app oauth settings!!");
                    }

                    logger.trace("CONFIRMATION {}", confirmUrl);
                }
            }
        }
        logger.trace("Smartthings servlet returning.");
    }

    protected void SetupApp() {
        SmartthingsApi api = bridgeHandler.getSmartthingsApi();

        try {
            AppResponse appResponse = api.SetupApp();
            if (appResponse.oauthClientId != null && appResponse.oauthClientSecret != null) {
                bridgeHandler.updateConfig(appResponse.oauthClientId, appResponse.oauthClientSecret);
            }
        } catch (SmartthingsException ex) {
            logger.info("Unable to setup Smartthings app !!");
        }
    }
}
