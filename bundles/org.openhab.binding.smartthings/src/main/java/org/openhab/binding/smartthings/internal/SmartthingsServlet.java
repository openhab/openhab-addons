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
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import org.openhab.binding.smartthings.internal.dto.ConfigurationResponse.ConfigurationData.Initialize;
import org.openhab.binding.smartthings.internal.dto.ConfigurationResponse.ConfigurationData.Page;
import org.openhab.binding.smartthings.internal.dto.LifeCycle;
import org.openhab.binding.smartthings.internal.dto.LifeCycle.Data;
import org.openhab.binding.smartthings.internal.dto.SMEvent;
import org.openhab.binding.smartthings.internal.dto.SMEvent.device;
import org.openhab.binding.smartthings.internal.dto.SmartthingsDevice;
import org.openhab.binding.smartthings.internal.dto.SmartthingsLocation;
import org.openhab.binding.smartthings.internal.handler.SmartthingsBridgeHandler;
import org.openhab.binding.smartthings.internal.handler.SmartthingsThingHandler;
import org.openhab.binding.smartthings.internal.type.SmartthingsException;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandler;
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

            StringBuffer requestUrl = req.getRequestURL();
            String servletBaseUrl = requestUrl != null ? requestUrl.toString() : "";
            String servletBaseURLSecure = servletBaseUrl.replace("http://", "https://").replace("8080", "8443");
            int p1 = servletBaseURLSecure.indexOf(SmartthingsServlet.PATH);
            if (p1 >= 0) {
                servletBaseURLSecure = servletBaseURLSecure.substring(0, p1 + SmartthingsServlet.PATH.length());
            }

            if (selectLocationTemplate != null && selectLocationTemplate.equals(template)) {
                setupApp(servletBaseURLSecure);

                try {
                    SmartthingsLocation[] locationList = api.getAllLocations();
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

            String uri = "https://account.smartthings.com/login?redirect=https%3A%2F%2Fstrongman-regional.api.smartthings.com%2F%3FappId%3D";
            uri = uri + bridgeHandler.getAppId();
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
                    String attr = data.events[0].deviceEvent.attribute;
                    String value = data.events[0].deviceEvent.value;

                    Bridge bridge = bridgeHandler.getThing();
                    List<Thing> things = bridge.getThings();

                    Optional<Thing> theThingOpt = things.stream().filter(x -> x.getProperties().containsValue(deviceId))
                            .findFirst();
                    if (theThingOpt.isPresent()) {
                        Thing theThing = theThingOpt.get();

                        ThingHandler handler = theThing.getHandler();
                        SmartthingsThingHandler smarthingsHandler = (SmartthingsThingHandler) handler;
                        smarthingsHandler.refreshDevice(theThing.getThingTypeUID().getId(), componentId, capa, attr,
                                value);

                        logger.info("aa");
                    }

                    logger.info("EVENT: {} {} {} {} {}", deviceId, componentId, capa, attr, value);
                } else if (resultObj.lifecycle.equals("INSTALL")) {
                    logger.info("");
                    String tokenInstallUpdate = resultObj.installData.authToken;
                    installedAppId = resultObj.installData.installedApp.installedAppId;
                    String locationId = resultObj.installData.installedApp.locationId;

                    try {
                        SmartthingsLocation loc = api.getLocation(locationId);
                        installedLocation = loc.name;
                    } catch (SmartthingsException ex) {
                        installedLocation = "Unable to retrieve location!!";
                    }

                    registerSubscriptions(tokenInstallUpdate, locationId);

                    setupInProgress = false;
                    logger.info("INSTALL");
                } else if (resultObj.lifecycle.equals("UPDATE")) {
                    String tokenInstallUpdate = resultObj.updateData.authToken;
                    installedAppId = resultObj.updateData.installedApp.installedAppId;
                    String locationId = resultObj.installData.installedApp.locationId;

                    String subscriptionUri = "https://api.smartthings.com/v1/installedapps/" + installedAppId
                            + "/subscriptions";

                    registerSubscriptions(tokenInstallUpdate, locationId);

                    logger.info("UPDATE");
                } else if (resultObj.lifecycle.equals("EXECUTE")) {
                    logger.info("EXCUTE");
                } else if (resultObj.lifecycle.equals("CONFIGURATION")
                        && resultObj.configurationData.phase().equals("INITIALIZE")) {
                    ConfigurationResponse response = new ConfigurationResponse();
                    response.configurationData = response.new ConfigurationData();

                    Initialize init = response.configurationData.new Initialize();
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
                    response.configurationData = response.new ConfigurationData();

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
                        this.networkConnector.doBasicRequest(confirmUrl, null, "", "", HttpMethod.GET);
                    } catch (Exception ex) {
                        logger.error("error during confirmation {}", confirmUrl);
                    }

                    try {
                        api.createAppOAuth(appId);
                    } catch (SmartthingsException ex) {
                        logger.error("Unable to setup app oauth settings!!");
                    }

                    logger.trace("CONFIRMATION {}", confirmUrl);
                }
            }
        }
        logger.trace("Smartthings servlet returning.");
    }

    protected void registerSubscriptions(String tokenInstallUpdate, String locationId) {
        try {
            String subscriptionUri = "https://api.smartthings.com/v1/installedapps/" + installedAppId
                    + "/subscriptions";

            // Remove old subscriptions before recreating them
            networkConnector.doRequest(JsonObject.class, subscriptionUri, null, tokenInstallUpdate, "",
                    HttpMethod.DELETE);

            networkConnector.doRequest(JsonObject.class, subscriptionUri, null, tokenInstallUpdate, "", HttpMethod.GET);

            SmartthingsApi api = bridgeHandler.getSmartthingsApi();
            SmartthingsDevice[] devices = api.getAllDevices();

            for (SmartthingsDevice dev : devices) {
                try {
                    if (!dev.locationId.equals(locationId)) {
                        continue;
                    }

                    SMEvent evt = new SMEvent();
                    evt.sourceType = "DEVICE";
                    evt.device = new device(dev.deviceId, "main", true, null);

                    String body = gson.toJson(evt);
                    networkConnector.doRequest(JsonObject.class, subscriptionUri, null, tokenInstallUpdate, body,
                            HttpMethod.POST);
                } catch (SmartthingsException ex) {
                    logger.error("Unable to register subscriptions: {} {} ", ex.getMessage(), dev.deviceId);
                }
            }
        } catch (SmartthingsException ex) {
            logger.error("Unable to register subscriptions: {}", ex.getMessage());
        }
    }

    protected void setupApp(String redirectUrl) {
        SmartthingsApi api = bridgeHandler.getSmartthingsApi();

        try {
            AppResponse appResponse = api.setupApp(redirectUrl);
            if (appResponse.oauthClientId != null && appResponse.oauthClientSecret != null) {
                bridgeHandler.updateConfig(appResponse.oauthClientId, appResponse.oauthClientSecret);
            }
            bridgeHandler.setAppId(appResponse.app.appId);
        } catch (SmartthingsException ex) {
            logger.info("Unable to setup Smartthings app !!");
        }
    }
}
