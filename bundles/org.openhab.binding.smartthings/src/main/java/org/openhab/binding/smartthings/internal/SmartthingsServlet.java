/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.util.stream.Collectors;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.smartthings.internal.api.SmartthingsApi;
import org.openhab.binding.smartthings.internal.api.SmartthingsNetworkConnector;
import org.openhab.binding.smartthings.internal.dto.ConfigurationResponse;
import org.openhab.binding.smartthings.internal.dto.ConfigurationResponse.configurationData.Page;
import org.openhab.binding.smartthings.internal.dto.ConfigurationResponse.configurationData.initialize;
import org.openhab.binding.smartthings.internal.dto.LifeCycle;
import org.openhab.binding.smartthings.internal.dto.LifeCycle.Data;
import org.openhab.binding.smartthings.internal.dto.SMEvent;
import org.openhab.binding.smartthings.internal.dto.SMEvent.device;
import org.openhab.binding.smartthings.internal.handler.SmartthingsBridgeHandler;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;
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
public class SmartthingsServlet extends HttpServlet {
    private static final String PATH = "/smartthings";
    private final Logger logger = LoggerFactory.getLogger(SmartthingsServlet.class);
    private @NonNullByDefault({}) SmartthingsBridgeHandler bridgeHandler;
    private @NonNullByDefault({}) HttpService httpService;
    private @NonNullByDefault({}) SmartthingsNetworkConnector networkConnector;
    private @NonNullByDefault({}) String token;
    private @Nullable EventAdmin eventAdmin;
    private Gson gson = new Gson();

    public SmartthingsServlet(SmartthingsBridgeHandler bridgeHandler, HttpService httpService,
            SmartthingsNetworkConnector networkConnector, String token) {
        this.bridgeHandler = bridgeHandler;
        this.httpService = httpService;
        this.networkConnector = networkConnector;
        this.token = token;
    }

    @Activate
    public void activate() {
        if (httpService == null) {
            logger.info("SmartthingsServlet.activate: httpService is unexpectedly null");
            return;
        }
        // try {
        Dictionary<String, String> servletParams = new Hashtable<String, String>();
        logger.info("registerServlet:" + PATH);
        // httpService.registerServlet(PATH, this, servletParams, httpService.createDefaultHttpContext());
        // httpService.registerResources(PATH + "/img", "web", null);

        //
        // } catch (ServletException | NamespaceException e) {
        // logger.warn("Could not start Smartthings servlet service: {}", e.getMessage());
        // }
    }

    protected void deactivate(ComponentContext componentContext) {
        if (httpService != null) {
            try {
                httpService.unregister(PATH);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Override
    public void init(@Nullable ServletConfig servletConfig) throws ServletException {

        logger.info("SmartthingsServlet:init");
        ServletContext context = servletConfig.getServletContext();
        BundleContext bundleContext = (BundleContext) context.getAttribute("osgi-bundlecontext");
    }

    @Reference
    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }

    @Reference
    protected void setEventAdmin(EventAdmin eventAdmin) {
        this.eventAdmin = eventAdmin;
    }

    protected void unsetEventAdmin(EventAdmin eventAdmin) {
        this.eventAdmin = null;
    }

    @Override
    protected void doGet(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp)
            throws ServletException, IOException {
        logger.info("SmartthingsServlet:doGet");
    }

    @Override
    protected void service(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp)
            throws ServletException, IOException {

        logger.info("SmartthingsServlet:service");

        if (req == null) {
            logger.debug("SmartthingsServlet.service unexpectedly received a null request. Request not processed");
            return;
        }
        String path = req.getRequestURI();

        // See what is in the path
        String[] pathParts = path.replace(PATH + "/", "").split("/");
        if (pathParts.length != 1) {
            logger.warn(
                    "Smartthing servlet received a path with zero or more than one parts. Only one part is allowed. path {}",
                    path);
            return;
        }

        BufferedReader rdr = new BufferedReader(req.getReader());
        String s = rdr.lines().collect(Collectors.joining());

        LifeCycle resultObj = gson.fromJson(s, LifeCycle.class);

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
            String token = resultObj.installData.authToken;
            String installedAppId = resultObj.installData.installedApp.installedAppId;

            logger.info("");

        } else if (resultObj.lifecycle.equals("UPDATE")) {
            String token = resultObj.updateData.authToken;
            String installedAppId = resultObj.updateData.installedApp.installedAppId;
            String subscriptionUri = "https://api.smartthings.com/v1/installedapps/" + installedAppId
                    + "/subscriptions";

            JsonObject res = networkConnector.DoRequest(JsonObject.class, subscriptionUri, null, token, "",
                    HttpMethod.GET);

            SMEvent evt = new SMEvent();
            evt.sourceType = "DEVICE";
            evt.device = new device("97806abc-ce85-4b28-9df2-31e33323cf62", "main", true, null);

            String body = gson.toJson(evt);
            JsonObject res2 = networkConnector.DoRequest(JsonObject.class, subscriptionUri, null, token, body,
                    HttpMethod.POST);

            evt = new SMEvent();
            evt.sourceType = "DEVICE";
            evt.device = new device("ee87617f-0c84-40a3-be25-e70e53f3fc6a", "main", true, null);

            body = gson.toJson(evt);
            res2 = networkConnector.DoRequest(JsonObject.class, subscriptionUri, null, token, body, HttpMethod.POST);

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
        } else if (resultObj.lifecycle.equals("CONFIGURATION") && resultObj.configurationData.phase().equals("PAGE")) {

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

            SmartthingsApi api = bridgeHandler.getSmartthingsApi();
            api.CreateAppOAuth(appId);

            logger.trace("CONFIRMATION {}", confirmUrl);
        }

        logger.trace("Smartthings servlet returning.");
        return;
    }

    private void publishEvent(String topic, String name, String data) {
        Dictionary<String, String> props = new Hashtable<String, String>();
        props.put(name, data);
        Event event = new Event(topic, props);
        if (eventAdmin != null) {
            eventAdmin.postEvent(event);
        } else {
            logger.debug("SmartthingsServlet:publishEvent eventAdmin is unexpectedly null");
        }
    }
}
