/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.homeconnect.internal.servlet;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.auth.client.oauth2.AccessTokenResponse;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthException;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthResponseException;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.homeconnect.internal.configuration.ApiBridgeConfiguration;
import org.openhab.binding.homeconnect.internal.handler.HomeConnectBridgeHandler;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * HomeConnect bridge configuration servlet.
 *
 * @author Jonas Brüstel - Initial Contribution
 */
@NonNullByDefault
@Component(service = BridgeConfigurationServlet.class, scope = ServiceScope.SINGLETON)
public class BridgeConfigurationServlet extends AbstractServlet {

    private static final long serialVersionUID = -9058701058178609079L;
    private static final String TEMPLATE = "bridge_configuration.html";
    private static final String TEMPLATE_BRIDGE = "part_bridge.html";
    private static final String TEMPLATE_SUCCESS = "success.html";
    private static final String PLACEHOLDER_KEY_BRIDGES = "bridges";
    private static final String PLACEHOLDER_KEY_LABEL = "label";
    private static final String PLACEHOLDER_KEY_UID = "uid";
    private static final String PLACEHOLDER_KEY_CLIENT_ID = "clientId";
    private static final String PLACEHOLDER_KEY_CLIENT_SECRET = "clientSecret";
    private static final String PLACEHOLDER_STATUS = "thingStatus";

    private final Logger logger = LoggerFactory.getLogger(BridgeConfigurationServlet.class);
    private final ArrayList<HomeConnectBridgeHandler> bridgeHandlers;
    private final HttpService httpService;

    @Activate
    public BridgeConfigurationServlet(@Reference HttpService httpService) {
        this.bridgeHandlers = new ArrayList<HomeConnectBridgeHandler>();
        this.httpService = httpService;

        try {
            logger.debug("Initialize bridge configuration servlet... ({})", SERVLET_BASE_PATH);
            httpService.registerServlet(SERVLET_BASE_PATH, this, null, httpService.createDefaultHttpContext());
        } catch (NamespaceException e) {
            try {
                httpService.unregister(SERVLET_BASE_PATH);
                httpService.registerServlet(SERVLET_BASE_PATH, this, null, httpService.createDefaultHttpContext());
            } catch (ServletException | NamespaceException ex) {
                logger.error("Could not register bridge configuration servlet! ({})", SERVLET_BASE_PATH, ex);
            }
        } catch (ServletException e) {
            logger.error("Could not register bridge configuration servlet! ({})", SERVLET_BASE_PATH, e);
        }
    }

    /**
     * Add Home Connect bridge handler to configuration servlet, to allow user to authenticate against Home Connect API.
     *
     * @param bridgeHandler bridge handler
     */
    public synchronized void addBridgeHandler(HomeConnectBridgeHandler bridgeHandler) {
        if (!bridgeHandlers.contains(bridgeHandler)) {
            bridgeHandlers.add(bridgeHandler);
        }
    }

    /**
     * Remove Home Connect bridge handler from configuration servlet.
     *
     * @param bridgeHandler bridge handler
     */
    public synchronized void removeBridgeHandler(HomeConnectBridgeHandler bridgeHandler) {
        if (bridgeHandlers.contains(bridgeHandler)) {
            bridgeHandlers.remove(bridgeHandler);
        }
    }

    @Deactivate
    protected void dispose() {
        try {
            logger.info("Unregister bridge configuration servlet ({}).", SERVLET_BASE_PATH);
            httpService.unregister(SERVLET_BASE_PATH);
        } catch (IllegalArgumentException e) {
            logger.warn("Could not unregister bridge configuration servlet. Failed wth {}", e.getMessage());
        }
    }

    @Override
    protected void doGet(@Nullable HttpServletRequest request, @Nullable HttpServletResponse response)
            throws ServletException, IOException {
        if (request == null || response == null) {
            throw new ServletException("Illegal state - Could not handle request!");
        }

        logger.debug("GET {}", SERVLET_BASE_PATH);

        String code = request.getParameter("code");
        String state = request.getParameter("state");

        addNoCacheHeader(response);

        if (!isEmpty(code) && !isEmpty(state)) {
            // callback handling from authorization server
            logger.debug("[oAuth] redirect from authorization server (code={}, state={}).", code, state);

            HomeConnectBridgeHandler bridgeHandler = getBridgeHandler(state);
            if (bridgeHandler == null) {
                response.sendError(HttpStatus.SC_BAD_REQUEST, "unknown bridge");
            } else {
                try {
                    String currentUrl = request.getScheme() + "://" + request.getServerName()
                            + ("http".equals(request.getScheme()) && request.getServerPort() == 80
                                    || "https".equals(request.getScheme()) && request.getServerPort() == 443 ? ""
                                            : ":" + request.getServerPort())
                            + request.getRequestURI()
                            + (request.getQueryString() != null ? "?" + request.getQueryString() : "");
                    AccessTokenResponse accessTokenResponse = bridgeHandler.getOAuthClientService()
                            .getAccessTokenResponseByAuthorizationCode(code, currentUrl);

                    logger.debug("access token response: {}", accessTokenResponse);

                    // inform bridge
                    bridgeHandler.dispose();
                    bridgeHandler.initialize();

                    final HashMap<String, String> replaceMap = new HashMap<>();
                    replaceMap.put(PLACEHOLDER_KEY_LABEL, bridgeHandler.getThing().getLabel() + "");
                    replaceMap.put(PLACEHOLDER_KEY_UID, bridgeHandler.getThing().getUID().getAsString());

                    response.setContentType(CONTENT_TYPE);
                    response.getWriter().append(replaceKeysFromMap(readHtmlTemplate(TEMPLATE_SUCCESS), replaceMap));
                    response.getWriter().close();
                } catch (OAuthException | OAuthResponseException e) {
                    logger.error("Could not fetch token!", e);
                    response.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Could not fetch token!");
                }
            }
        } else {
            // index page
            final HashMap<String, String> replaceMap = new HashMap<>();
            if (bridgeHandlers.isEmpty()) {
                replaceMap.put(PLACEHOLDER_KEY_BRIDGES,
                        "<p class='block'>No Home Connect bridge found. Please manually add 'Home Connect API' bridge and authorize it here.<p>");
            } else {
                replaceMap.put(PLACEHOLDER_KEY_BRIDGES, bridgeHandlers.stream()
                        .map(bridgeHandler -> renderBridgePart(bridgeHandler)).collect(Collectors.joining()));
            }

            response.setContentType(CONTENT_TYPE);
            response.getWriter().append(replaceKeysFromMap(readHtmlTemplate(TEMPLATE), replaceMap));
            response.getWriter().close();
        }
    }

    @Override
    protected void doPost(@Nullable HttpServletRequest request, @Nullable HttpServletResponse response)
            throws ServletException, IOException {
        if (request == null || response == null) {
            throw new ServletException("Illegal state - Could not handle request!");
        }

        logger.debug("POST {}", SERVLET_BASE_PATH);

        String bridgeUid = request.getParameter("uid");
        String task = request.getParameter("task");

        addNoCacheHeader(response);

        if (StringUtils.isEmpty(bridgeUid) || StringUtils.isEmpty(task)) {
            response.sendError(HttpStatus.SC_BAD_REQUEST, "uid or task parameter missing");
        } else {
            HomeConnectBridgeHandler bridgeHandler = getBridgeHandler(bridgeUid);
            if (bridgeHandler == null) {
                response.sendError(HttpStatus.SC_BAD_REQUEST, "unknown bridge");
            } else {
                if ("authorize".equals(task)) {
                    try {
                        String authorizationUrl = bridgeHandler.getOAuthClientService().getAuthorizationUrl(null, null,
                                bridgeHandler.getThing().getUID().getAsString());
                        logger.debug("Generated authorization url: {}", authorizationUrl);

                        response.sendRedirect(authorizationUrl);
                    } catch (OAuthException e) {
                        logger.error("Could not create authorization url!", e);
                        response.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Could not create authorization url!");
                    }
                } else {
                    logger.info("Remove access token for '{}' bridge.", bridgeHandler.getThing().getLabel());
                    try {
                        bridgeHandler.getOAuthClientService().remove();
                    } catch (OAuthException e) {
                        logger.error("Could not clear oAuth credentials.", e);
                    }
                    bridgeHandler.dispose();
                    bridgeHandler.initialize();

                    doGet(request, response);
                }
            }
        }
    }

    private @Nullable HomeConnectBridgeHandler getBridgeHandler(String bridgeUid) {
        for (HomeConnectBridgeHandler handler : bridgeHandlers) {
            if (handler.getThing().getUID().getAsString().equals(bridgeUid)) {
                return handler;
            }
        }
        return null;
    }

    private String renderBridgePart(HomeConnectBridgeHandler bridgeHandler) {
        Thing thing = bridgeHandler.getThing();
        HashMap<String, String> replaceMap = new HashMap<>();

        replaceMap.put(PLACEHOLDER_KEY_LABEL, thing.getLabel() + "");
        replaceMap.put(PLACEHOLDER_KEY_UID, thing.getUID().getAsString());
        replaceMap.put(PLACEHOLDER_STATUS, thing.getStatus().toString());

        ApiBridgeConfiguration configuration = bridgeHandler.getConfiguration();
        replaceMap.put(PLACEHOLDER_KEY_CLIENT_ID, configuration.getClientId());
        replaceMap.put(PLACEHOLDER_KEY_CLIENT_SECRET, configuration.getClientSecret());

        try {
            return replaceKeysFromMap(readHtmlTemplate(TEMPLATE_BRIDGE), replaceMap);
        } catch (IOException e) {
            logger.error("Could not render template {}!", TEMPLATE_BRIDGE, e);
            return "";
        }
    }
}
