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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.UrlEncoded;
import org.openhab.binding.smartthings.internal.api.SmartthingsApi;
import org.openhab.binding.smartthings.internal.api.SmartthingsNetworkConnector;
import org.openhab.binding.smartthings.internal.dto.SmartthingsDevice;
import org.openhab.binding.smartthings.internal.dto.SmartthingsLocation;
import org.openhab.binding.smartthings.internal.handler.SmartthingsBridgeHandler;
import org.openhab.binding.smartthings.internal.type.SmartthingsException;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SpotifyAuthServlet} manages the authorization with the Smartthings Web API. The servlet implements the
 * Authorization Code flow and saves the resulting refreshToken with the bridge.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class SmartthingsAuthServlet extends SmartthingsBaseServlet {

    private static final long serialVersionUID = -4719613645562518231L;

    private static final String CONTENT_TYPE = "text/html;charset=UTF-8";
    private final Logger logger = LoggerFactory.getLogger(SmartthingsAuthServlet.class);
    private final SmartthingsAuthService smartthingsAuthService;

    private final String indexTemplate;
    private final String confirmTemplate;
    private static final String HTML_ERROR = "<p class='block error'>Call to Smartthings failed with error: %s</p>";

    // Keys present in the index.html
    private static final String KEY_ERROR = "error";
    private static final String KEY_BRIDGE_URI = "bridge.uri";
    private static final String KEY_REDIRECT_URI = "redirectUri";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_DEVICES_COUNT = "devicesCount";

    public SmartthingsAuthServlet(SmartthingsBridgeHandler bridgeHandler, SmartthingsAuthService smartthingsAuthService,
            HttpService httpService, SmartthingsNetworkConnector networkConnector) throws SmartthingsException {
        super(bridgeHandler, httpService, networkConnector);

        this.smartthingsAuthService = smartthingsAuthService;

        try {
            indexTemplate = readTemplate("index-oauth.html");
            confirmTemplate = readTemplate("confirmation.html");
        } catch (IOException e) {
            throw new SmartthingsException("unable to initialize auth servlet", e);
        }
    }

    @Override
    protected void doGet(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp)
            throws ServletException, IOException {
        if (req == null) {
            return;
        }
        if (resp == null) {
            return;
        }

        logger.debug("Smartthings auth callback servlet received GET request {}.", req.getRequestURI());

        String template = handleTemplate(req);
        resp.setContentType(CONTENT_TYPE);
        resp.getWriter().append(template);
        resp.getWriter().close();
    }

    private String handleTemplate(@Nullable HttpServletRequest req) {
        if (req == null) {
            return "";
        }

        StringBuffer requestUrl = req.getRequestURL();
        String queryString = req.getQueryString();
        String servletBaseURL = requestUrl != null ? requestUrl.toString() : "";
        String servletBaseURLSecure = servletBaseURL.replace("http://", "https://").replace("8080", "8443");
        SmartthingsAccountHandler accountHandler = smartthingsAuthService.getSmartthingsAccountHandler();

        Map<String, String> replaceMap = new HashMap<>();

        String template = "";
        replaceMap.put(KEY_LOCATION, "");
        replaceMap.put(KEY_DEVICES_COUNT, "");
        replaceMap.put(KEY_ERROR, "");

        template = indexTemplate;

        if (queryString != null) {
            final MultiMap<String> params = new MultiMap<>();
            UrlEncoded.decodeTo(queryString, params, StandardCharsets.UTF_8.name());
            final String reqCode = params.getString("code");
            final String reqState = params.getString("state");
            final String reqError = params.getString("error");

            if (!StringUtil.isBlank(reqError)) {
                template = confirmTemplate;
                logger.debug("Smartthings redirected with an error: {}", reqError);
                replaceMap.put(KEY_ERROR, String.format(HTML_ERROR, reqError));
            } else if (!StringUtil.isBlank(reqState)) {
                try {
                    if (!reqCode.isBlank()) {
                        template = confirmTemplate;

                        smartthingsAuthService.authorize(servletBaseURLSecure, reqState, reqCode);

                        SmartthingsApi api = bridgeHandler.getSmartthingsApi();
                        SmartthingsDevice[] devices = api.getAllDevices();
                        SmartthingsLocation[] locations = api.getAllLocations();

                        replaceMap.put(KEY_LOCATION, locations[0].name + " / " + locations[0].locationId);
                        replaceMap.put(KEY_DEVICES_COUNT, "" + devices.length);
                    }
                } catch (SmartthingsException e) {
                    logger.debug("Exception during authorizaton: ", e);
                    replaceMap.put(KEY_ERROR, String.format(HTML_ERROR, e.getMessage()));
                }
            }
        }

        replaceMap.put(KEY_REDIRECT_URI, servletBaseURLSecure);
        if (accountHandler != null) {
            replaceMap.put(KEY_BRIDGE_URI, accountHandler.formatAuthorizationUrl(servletBaseURLSecure, "myState"));
        }
        return replaceKeysFromMap(template, replaceMap);
    }
}
