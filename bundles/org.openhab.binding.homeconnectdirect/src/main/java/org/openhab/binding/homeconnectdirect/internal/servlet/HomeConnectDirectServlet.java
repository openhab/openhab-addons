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
package org.openhab.binding.homeconnectdirect.internal.servlet;

import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.SERVLET_ASSETS_PATH;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.SERVLET_BASE_PATH;
import static org.openhab.binding.homeconnectdirect.internal.common.utils.StringUtils.CLOSING_BRACE;
import static org.openhab.binding.homeconnectdirect.internal.common.utils.StringUtils.EMPTY_STRING;
import static org.openhab.binding.homeconnectdirect.internal.common.utils.StringUtils.OPENING_BRACE;
import static org.openhab.binding.homeconnectdirect.internal.common.utils.StringUtils.SLASH;
import static org.openhab.binding.homeconnectdirect.internal.common.utils.StringUtils.equalsIgnoreCase;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.CONTENT_TYPE_FORM_DATA;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.CONTENT_TYPE_HTML;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.CONTENT_TYPE_JSON;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.CONTENT_TYPE_ZIP;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.DELETE;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.GET;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.HA_ID;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.LOG_FILE_ID;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.NOT_FOUND;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.PATH_API_APPLIANCES;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.PATH_API_LOGS;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.PATH_API_MESSAGES;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.PATH_API_PROFILES;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.POST;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.UID;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.UNAUTHORIZED;

import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.homeconnectdirect.internal.common.utils.ConfigurationUtils;
import org.openhab.binding.homeconnectdirect.internal.common.utils.StringUtils;
import org.openhab.binding.homeconnectdirect.internal.service.profile.ApplianceProfileService;
import org.openhab.binding.homeconnectdirect.internal.servlet.handler.api.ApplianceRequestHandler;
import org.openhab.binding.homeconnectdirect.internal.servlet.handler.api.LogRequestHandler;
import org.openhab.binding.homeconnectdirect.internal.servlet.handler.api.MessageRequestHandler;
import org.openhab.binding.homeconnectdirect.internal.servlet.handler.api.ProfileRequestHandler;
import org.openhab.binding.homeconnectdirect.internal.servlet.handler.page.AuthRequestHandler;
import org.openhab.binding.homeconnectdirect.internal.servlet.handler.page.IndexRequestHandler;
import org.openhab.binding.homeconnectdirect.internal.servlet.model.Error;
import org.openhab.binding.homeconnectdirect.internal.servlet.routing.RequestHandler;
import org.openhab.binding.homeconnectdirect.internal.servlet.routing.RequestHandlerContext;
import org.openhab.binding.homeconnectdirect.internal.servlet.routing.RequestHandlerException;
import org.openhab.binding.homeconnectdirect.internal.servlet.routing.Route;
import org.openhab.core.thing.ThingRegistry;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 *
 * Home Connect Direct UI servlet.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
@Component(service = HomeConnectDirectServlet.class, scope = ServiceScope.SINGLETON, immediate = true)
public class HomeConnectDirectServlet extends HttpServlet {

    @Serial
    private static final long serialVersionUID = -283768147024034490L;

    private static final String ASSET_CLASSPATH = "assets";
    private static final String MULTIPART_KEY = "org.eclipse.jetty.multipartConfig";

    private static final String PATH_API_PROFILE = PATH_API_PROFILES + SLASH + OPENING_BRACE + HA_ID + CLOSING_BRACE;
    private static final String PATH_API_THING_MESSAGES = PATH_API_MESSAGES + SLASH + OPENING_BRACE + UID
            + CLOSING_BRACE;
    private static final String PATH_API_THING_MESSAGE_RESOURCES = PATH_API_THING_MESSAGES + "/resources";
    private static final String PATH_API_THING_MESSAGE_VALUE_KEYS = PATH_API_THING_MESSAGES + "/value-keys";
    private static final String PATH_API_THING_MESSAGE_DESCRIPTION_CHANGE_KEYS = PATH_API_THING_MESSAGES
            + "/description-change-keys";
    private static final String PATH_API_APPLIANCE_YAML = PATH_API_APPLIANCES + SLASH + OPENING_BRACE + UID
            + CLOSING_BRACE + "/yaml";
    private static final String PATH_API_APPLIANCE_DSL = PATH_API_APPLIANCES + SLASH + OPENING_BRACE + UID
            + CLOSING_BRACE + "/dsl";
    private static final String PATH_API_APPLIANCE_DEVICE_DESCRIPTION = PATH_API_APPLIANCES + SLASH + OPENING_BRACE
            + UID + CLOSING_BRACE + "/device-description";
    private static final String PATH_API_LOG = PATH_API_LOGS + SLASH + OPENING_BRACE + LOG_FILE_ID + CLOSING_BRACE;
    private static final String PATH_API_LOG_MESSAGES = PATH_API_LOG + "/messages";
    private static final String PATH_API_LOG_MESSAGE_RESOURCES = PATH_API_LOG + "/resources";
    private static final String PATH_API_LOG_MESSAGE_VALUE_KEYS = PATH_API_LOG + "/value-keys";
    private static final String PATH_API_LOG_MESSAGE_DEVICE_DESCRIPTION = PATH_API_LOG + "/device-description";
    private static final String PATH_API_LOG_MESSAGE_DESCRIPTION_CHANGE_KEYS = PATH_API_LOG
            + "/description-change-keys";
    private static final String PATH_AUTH = "/auth";
    private static final String PATH_TOKEN = PATH_AUTH + "/token";
    private static final List<String> PATHS_INDEX = List.of("/", "/appliances", "/appliances/*", "/profiles", "/logs",
            "/logs/*");
    private static final String PATH_LOGIN = PATH_AUTH + "/login";
    private static final String PATH_LOGOUT = PATH_AUTH + "/logout";

    private final Logger logger;
    private final HttpService httpService;
    private final ThingRegistry thingRegistry;
    private final ApplianceProfileService applianceProfileService;
    private final ConfigurationAdmin configurationAdmin;
    private final MultipartConfigElement multipartConfig;
    private final Gson gson;
    private final ServletSecurityContext securityContext;
    private final List<Route> routes;

    @Activate
    public HomeConnectDirectServlet(@Reference HttpService httpService, @Reference ThingRegistry thingRegistry,
            @Reference ApplianceProfileService applianceProfileService,
            @Reference ConfigurationAdmin configurationAdmin) {
        logger = LoggerFactory.getLogger(HomeConnectDirectServlet.class);
        this.httpService = httpService;
        this.thingRegistry = thingRegistry;
        this.applianceProfileService = applianceProfileService;
        this.configurationAdmin = configurationAdmin;
        this.gson = ConfigurationUtils.createGson();
        this.securityContext = ServletSecurityContext.get();
        this.routes = new ArrayList<>();

        // register servlet
        try {
            logger.debug("Initialize Home Connect Direct servlet ({})", SERVLET_BASE_PATH);
            httpService.registerServlet(SERVLET_BASE_PATH, this, null, httpService.createDefaultHttpContext());
            httpService.registerResources(SERVLET_ASSETS_PATH, ASSET_CLASSPATH, null);
        } catch (ServletException | NamespaceException e) {
            logger.warn("Could not register Home Connect servlet! ({})", SERVLET_BASE_PATH, e);
        }

        // multipart config (maxFileSize=5MB, maxRequestSize=10MB, fileSizeThreshold=1MB)
        multipartConfig = new MultipartConfigElement(EMPTY_STRING, 5242880, 10485760, 1048576);
    }

    @Override
    public void init() {
        var indexHandler = new IndexRequestHandler();
        var authHandler = new AuthRequestHandler(securityContext);
        var applianceHandler = new ApplianceRequestHandler();
        var profileHandler = new ProfileRequestHandler(applianceProfileService);
        var messageHandler = new MessageRequestHandler(applianceProfileService);
        var logHandler = new LogRequestHandler();

        // html
        PATHS_INDEX.forEach(path -> registerHtmlRoute(path, indexHandler::indexPage));
        registerHtmlRoute(PATH_LOGIN, authHandler::loginPage);

        // auth api
        registerRoute(POST, PATH_TOKEN, List.of(CONTENT_TYPE_JSON), List.of(CONTENT_TYPE_JSON), false,
                authHandler::handleLogin);
        registerRoute(POST, PATH_LOGOUT, authHandler::handleLogout);

        // api
        registerRoute(GET, PATH_API_APPLIANCES, applianceHandler::getAppliances);
        registerRoute(GET, PATH_API_APPLIANCE_YAML, applianceHandler::createYamlCode);
        registerRoute(GET, PATH_API_APPLIANCE_DSL, applianceHandler::createDslCode);
        registerRoute(GET, PATH_API_APPLIANCE_DEVICE_DESCRIPTION, applianceHandler::getDeviceDescription);
        registerRoute(GET, PATH_API_PROFILES, profileHandler::getProfiles);
        registerRoute(POST, PATH_API_PROFILES, List.of(CONTENT_TYPE_FORM_DATA), List.of(CONTENT_TYPE_JSON), true,
                profileHandler::uploadProfile);
        registerRoute(GET, PATH_API_PROFILE, List.of(), List.of(CONTENT_TYPE_ZIP), true,
                profileHandler::downloadProfile);
        registerRoute(DELETE, PATH_API_PROFILE, profileHandler::deleteProfile);
        registerRoute(GET, PATH_API_THING_MESSAGES, List.of(), List.of(CONTENT_TYPE_ZIP), true,
                messageHandler::saveOrDownloadMessages);
        registerRoute(POST, PATH_API_THING_MESSAGES, messageHandler::sendMessage);
        registerRoute(GET, PATH_API_THING_MESSAGE_RESOURCES, messageHandler::getMessageResources);
        registerRoute(GET, PATH_API_THING_MESSAGE_VALUE_KEYS, messageHandler::getMessageValueKeys);
        registerRoute(GET, PATH_API_THING_MESSAGE_DESCRIPTION_CHANGE_KEYS,
                messageHandler::getMessageDescriptionChangeKeys);
        registerRoute(GET, PATH_API_LOGS, logHandler::getLogs);
        registerRoute(GET, PATH_API_LOG, List.of(), List.of(CONTENT_TYPE_ZIP), true, logHandler::downloadLog);
        registerRoute(DELETE, PATH_API_LOG, logHandler::deleteLog);
        registerRoute(GET, PATH_API_LOG_MESSAGES, logHandler::getLogMessages);
        registerRoute(GET, PATH_API_LOG_MESSAGE_RESOURCES, logHandler::getLogMessageResources);
        registerRoute(GET, PATH_API_LOG_MESSAGE_VALUE_KEYS, logHandler::getLogMessageValueKeys);
        registerRoute(GET, PATH_API_LOG_MESSAGE_DESCRIPTION_CHANGE_KEYS,
                logHandler::getLogMessageDescriptionChangeKeys);
        registerRoute(GET, PATH_API_LOG_MESSAGE_DEVICE_DESCRIPTION, logHandler::getDeviceDescription);
        registerRoute(POST, PATH_API_LOGS, List.of(CONTENT_TYPE_FORM_DATA), List.of(CONTENT_TYPE_JSON), true,
                logHandler::uploadLog);
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        var path = request.getPathInfo();
        var method = request.getMethod();
        var accept = request.getHeader(ACCEPT);
        var contentType = request.getHeader(CONTENT_TYPE);

        if (path == null || path.isEmpty()) {
            path = SLASH;
        }

        // configure multipart
        request.setAttribute(MULTIPART_KEY, multipartConfig);

        // routing
        for (Route route : routes) {
            Matcher matcher = route.path().matcher(path);

            if (equalsIgnoreCase(route.method(), method) && matcher.matches() && matchesAccept(route, accept)
                    && matchesContentType(route, contentType)) {
                var vars = new HashMap<String, String>();
                var pathVariables = route.pathVariables().stream().toList();
                for (int i = 0; i < pathVariables.size(); i++) {
                    vars.put(pathVariables.get(i), matcher.group(i + 1));
                }

                try {
                    if (!route.secured() || securityContext.isValidAuthorization(request, configurationAdmin)) {
                        route.handler().handle(new RequestHandlerContext(request, response, vars, configurationAdmin,
                                gson, thingRegistry));
                    } else {
                        sendError(request, response, UNAUTHORIZED, HttpStatus.UNAUTHORIZED_401);
                    }
                    return;
                } catch (RequestHandlerException e) {
                    logger.warn("Could not handle request! path: {} error: {}", path, e.getMessage(), e);
                    sendError(request, response, "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR_500);
                    return;
                }
            }
        }
        sendError(request, response, NOT_FOUND, HttpStatus.NOT_FOUND_404);
    }

    private boolean matchesAccept(Route route, @Nullable String accept) {
        if (route.produces().isEmpty()) {
            return true;
        } else if (accept == null) {
            return false;
        } else {
            for (String contentType : route.produces()) {
                if (StringUtils.containsIgnoreCase(accept, contentType)) {
                    return true;
                }
            }
            return false;
        }
    }

    private boolean matchesContentType(Route route, @Nullable String contentType) {
        if (route.consumes().isEmpty()) {
            return true;
        } else if (contentType == null) {
            return false;
        } else {
            for (String allowedContentType : route.consumes()) {
                if (StringUtils.containsIgnoreCase(contentType, allowedContentType)) {
                    return true;
                }
            }
            return false;
        }
    }

    private void registerHtmlRoute(String path, RequestHandler handler) {
        registerRoute(ServletConstants.GET, path, List.of(), List.of(CONTENT_TYPE_HTML), false, handler);
    }

    private void registerRoute(String method, String path, RequestHandler handler) {
        var consumes = List.of(CONTENT_TYPE_JSON);
        if (method.equals(GET) || method.equals(DELETE)) {
            consumes = List.of();
        }
        registerRoute(method, path, consumes, List.of(CONTENT_TYPE_JSON), true, handler);
    }

    private void registerRoute(String method, String path, List<String> consumes, List<String> produces,
            boolean secured, RequestHandler handler) {
        var vars = new HashSet<String>();

        StringBuilder regex = new StringBuilder("^");
        int lastIndex = 0;

        Matcher matcher = Pattern.compile("\\{([^/]+)\\}|\\*\\*|\\*").matcher(path);
        while (matcher.find()) {
            regex.append(Pattern.quote(path.substring(lastIndex, matcher.start())));
            if (matcher.group(1) != null) {
                vars.add(matcher.group(1));
                regex.append("([^/]+)");
            } else if ("**".equals(matcher.group())) {
                regex.append(".*");
            } else {
                regex.append("[^/]+");
            }
            lastIndex = matcher.end();
        }
        regex.append(Pattern.quote(path.substring(lastIndex)));
        regex.append("$");

        routes.add(new Route(method, Pattern.compile(regex.toString()), consumes, produces, vars, secured, handler));
    }

    private void sendError(HttpServletRequest request, HttpServletResponse response, String message, int status)
            throws IOException {
        if (StringUtils.containsIgnoreCase(request.getHeader(ACCEPT), CONTENT_TYPE_JSON)) {
            response.setContentType(CONTENT_TYPE_JSON);
            response.setStatus(status);
            response.getWriter().write(gson.toJson(new Error(status, message)));
        } else {
            response.setContentType(CONTENT_TYPE_HTML);
            response.sendError(status, message);
        }
    }

    @Deactivate
    protected void dispose() {
        securityContext.clearAllTokens();
        httpService.unregister(SERVLET_BASE_PATH);
        httpService.unregister(SERVLET_ASSETS_PATH);
    }
}
