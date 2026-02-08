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
package org.openhab.binding.smartthings.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.smartthings.internal.api.SmartThingsApi;
import org.openhab.binding.smartthings.internal.api.SmartThingsNetworkConnector;
import org.openhab.binding.smartthings.internal.dto.AppResponse;
import org.openhab.binding.smartthings.internal.dto.ConfigurationResponse;
import org.openhab.binding.smartthings.internal.dto.ConfigurationResponse.ConfigurationData.Initialize;
import org.openhab.binding.smartthings.internal.dto.ConfigurationResponse.ConfigurationData.Page;
import org.openhab.binding.smartthings.internal.dto.LifeCycle;
import org.openhab.binding.smartthings.internal.dto.LifeCycle.Data;
import org.openhab.binding.smartthings.internal.dto.SmartThingsLocation;
import org.openhab.binding.smartthings.internal.handler.SmartThingsBridgeHandler;
import org.openhab.binding.smartthings.internal.handler.SmartThingsThingHandler;
import org.openhab.binding.smartthings.internal.type.SmartThingsException;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Receives all HTTP data from SmartThings
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("serial")
public class SmartThingsServlet extends SmartThingsBaseServlet {
    private static final String PATH = "/smartthings";

    private final Logger logger = LoggerFactory.getLogger(SmartThingsServlet.class);

    private Gson gson = new Gson();

    // Page templates

    private String installedLocation = "";

    public Boolean setupInProgress = false;

    public SmartThingsServlet(SmartThingsBridgeHandler bridgeHandler, HttpService httpService,
            SmartThingsNetworkConnector networkConnector) {
        super(bridgeHandler, httpService, networkConnector);
    }

    public void activate() {
        try {
            Dictionary<String, String> servletParams = new Hashtable<String, String>();
            logger.info("registerServlet:" + PATH);
            httpService.registerServlet(PATH, this, servletParams, httpService.createDefaultHttpContext());
            httpService.registerResources(PATH + "/img", "img", null);
            httpService.registerResources(PATH + "/web", "web", null);
        } catch (ServletException | NamespaceException e) {
            logger.warn("Could not start SmartThings servlet service: {}", e.getMessage());
        }
    }

    public void deactivate() {
        try {
            httpService.unregister(PATH);
            httpService.unregister(PATH + "/img");
            httpService.unregister(PATH + "/web");
        } catch (IllegalArgumentException e) {
            logger.warn("Could not stop SmartThings servlet service: {}", e.getMessage());
        }
    }

    @Override
    public void init(@Nullable ServletConfig servletConfig) throws ServletException {
        logger.info("SmartThingsServlet:init");
    }

    @Override
    protected void service(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp)
            throws ServletException, IOException {
        SmartThingsApi api = bridgeHandler.getSmartThingsApi();

        if (req == null) {
            logger.debug("SmartThingsServlet.service unexpectedly received a null request. Request not processed");
            return;
        }
        if (resp == null) {
            return;
        }

        logger.info("SmartThingsServlet:service");
        String path = req.getRequestURI();

        if (path != null && path.contains("/smartthings/cb")) {
            BufferedReader rdr = new BufferedReader(req.getReader());
            String s = rdr.lines().collect(Collectors.joining());

            LifeCycle resultObj = gson.fromJson(s, LifeCycle.class);
            if (resultObj == null) {
                return;
            }

            String eventType = resultObj.getEventType();

            logger.trace("Callback called with eventType: {}", eventType);

            // ========================================
            // Event from webhook CB
            // ========================================

            if (eventType.equals(SmartThingsBindingConstants.EVENT_TYPE_EVENT)) {
                Data data = resultObj.eventData;
                String deviceId = data.events[0].deviceEvent.deviceId;
                String componentId = data.events[0].deviceEvent.componentId;
                String capa = data.events[0].deviceEvent.capability;
                String attr = data.events[0].deviceEvent.attribute;
                String value = data.events[0].deviceEvent.value;

                logger.trace(
                        "Callback called with lifeCycle (Event): deviceId : {}, componentId: {}, capa: {}, attr: {}, value:{} ",
                        deviceId, componentId, capa, attr, value);

                Bridge bridge = bridgeHandler.getThing();
                List<Thing> things = bridge.getThings();

                Optional<Thing> theThingOpt = things.stream().filter(x -> x.getProperties().containsValue(deviceId))
                        .findFirst();
                if (theThingOpt.isPresent()) {
                    Thing theThing = theThingOpt.get();

                    ThingHandler handler = theThing.getHandler();
                    SmartThingsThingHandler smarthingsHandler = (SmartThingsThingHandler) handler;
                    if (smarthingsHandler != null) {
                        smarthingsHandler.refreshDevice(theThing.getThingTypeUID().getId(), componentId, capa, attr,
                                value);
                    }
                }

                logger.info("EVENT: {} {} {} {} {}", deviceId, componentId, capa, attr, value);
            } else if (eventType.equals(SmartThingsBindingConstants.EVENT_TYPE_INSTALL)) {
                String appId = resultObj.installData.installedApp.installedAppId;
                String locationId = resultObj.installData.installedApp.locationId;

                try {
                    bridgeHandler.setAppId(appId);
                    SmartThingsLocation loc = api.getLocation(locationId);
                    installedLocation = loc.name;
                } catch (SmartThingsException ex) {
                    installedLocation = "Unable to retrieve location!!";
                }

                // registerSubscriptions(tokenInstallUpdate, locationId);

                setupInProgress = false;
                logger.info("INSTALL");
            } else if (eventType.equals(SmartThingsBindingConstants.EVENT_TYPE_UPDATE)) {
                String appId = resultObj.updateData.installedApp.installedAppId;
                bridgeHandler.setAppId(appId);

                // String subscriptionUri = "https://api.smartthings.com/v1/installedapps/" + installedAppId
                // + "/subscriptions";
                // registerSubscriptions(tokenInstallUpdate, locationId);

                logger.info("UPDATE");
            } else if (eventType.equals(SmartThingsBindingConstants.EVENT_TYPE_EXECUTE)) {
                logger.info("EXCUTE");
            } else if (eventType.equals(SmartThingsBindingConstants.EVENT_TYPE_CONFIGURATION)
                    && resultObj.configurationData.phase().equals(SmartThingsBindingConstants.PHASE_INITIALIZE)) {
                ConfigurationResponse response = new ConfigurationResponse();
                response.configurationData = response.new ConfigurationData();

                Initialize init = response.configurationData.new Initialize();
                response.configurationData.initialize = init;
                init.name = "openHAB";
                init.description = "openHAB";
                init.firstPageId = "1";
                init.id = "openHAB";

                init.permissions = new String[1];
                init.permissions[0] = "r:devices:*";

                String responseSt = gson.toJson(response);
                resp.getWriter().print(responseSt);
            } else if (eventType.equals(SmartThingsBindingConstants.EVENT_TYPE_CONFIGURATION)
                    && resultObj.configurationData.phase().equals(SmartThingsBindingConstants.PHASE_PAGE)) {
                ConfigurationResponse response = new ConfigurationResponse();
                response.configurationData = response.new ConfigurationData();

                Page page1 = response.configurationData.new Page();
                response.configurationData.page = page1;
                page1.pageId = "1";
                page1.nextPageId = null;
                page1.previousPageId = null;
                page1.name = "openHAB";
                page1.complete = true;

                String responseSt = gson.toJson(response);
                resp.getWriter().print(responseSt);
            } else if (eventType.equals(SmartThingsBindingConstants.EVENT_TYPE_CONFIRMATION)) {
                String appId = resultObj.confirmationData.appId();
                String confirmUrl = resultObj.confirmationData.confirmationUrl();

                bridgeHandler.setAppId(appId);

                String responseSt = "{";

                responseSt = responseSt + "\"targetUrl\": \"" + confirmUrl + "\"";
                responseSt = responseSt + "}";
                resp.getWriter().print(responseSt);

                try {
                    Thread.sleep(2000);
                    this.networkConnector.doBasicRequest(String.class, confirmUrl, null, "", "", HttpMethod.GET);
                } catch (Exception ex) {
                    logger.error("error during confirmation {}", confirmUrl);
                }

                // try {
                // api.createAppOAuth(appId);
                // } catch (SmartThingsException ex) {
                // logger.error("Unable to setup app oauth settings!!");
                // }

                logger.trace("CONFIRMATION {}", confirmUrl);
            }

        }
        logger.trace("SmartThings servlet returning.");
    }

    protected void setupApp(String redirectUrl) {
        SmartThingsApi api = bridgeHandler.getSmartThingsApi();

        try {
            AppResponse appResponse = api.setupApp(redirectUrl);
            if (appResponse.oauthClientId != null && appResponse.oauthClientSecret != null) {
                bridgeHandler.updateConfig(appResponse.oauthClientId, appResponse.oauthClientSecret);
            }
            bridgeHandler.setAppId(appResponse.app.appId);
        } catch (SmartThingsException ex) {
            logger.info("Unable to setup SmartThings app !!");
        }
    }

    public String getInstalledLocation() {
        return installedLocation;
    }
}
