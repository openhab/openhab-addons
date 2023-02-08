/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.ZonedDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.*;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.homeconnect.internal.client.exception.ApplianceOfflineException;
import org.openhab.binding.homeconnect.internal.client.exception.AuthorizationException;
import org.openhab.binding.homeconnect.internal.client.exception.CommunicationException;
import org.openhab.binding.homeconnect.internal.client.model.ApiRequest;
import org.openhab.binding.homeconnect.internal.handler.AbstractHomeConnectThingHandler;
import org.openhab.binding.homeconnect.internal.handler.HomeConnectBridgeHandler;
import org.openhab.core.OpenHAB;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

/**
 *
 * Home Connect servlet.
 *
 * @author Jonas Brüstel - Initial Contribution
 */
@NonNullByDefault
@Component(service = HomeConnectServlet.class, scope = ServiceScope.SINGLETON, immediate = true)
public class HomeConnectServlet extends HttpServlet {

    private static final String SLASH = "/";
    private static final String SERVLET_NAME = "homeconnect";
    private static final String SERVLET_PATH = SLASH + SERVLET_NAME;
    private static final String ASSETS_PATH = SERVLET_PATH + "/asset";
    private static final String ROOT_PATH = SLASH;
    private static final String APPLIANCES_PATH = "/appliances";
    private static final String REQUEST_LOG_PATH = "/log/requests";
    private static final String EVENT_LOG_PATH = "/log/events";
    private static final String DEFAULT_CONTENT_TYPE = "text/html; charset=UTF-8";
    private static final String PARAM_CODE = "code";
    private static final String PARAM_STATE = "state";
    private static final String PARAM_EXPORT = "export";
    private static final String PARAM_ACTION = "action";
    private static final String PARAM_BRIDGE_ID = "bridgeId";
    private static final String PARAM_THING_ID = "thingId";
    private static final String PARAM_PATH = "path";
    private static final String PARAM_REDIRECT_URI = "redirectUri";
    private static final String ACTION_AUTHORIZE = "authorize";
    private static final String ACTION_CLEAR_CREDENTIALS = "clearCredentials";
    private static final String ACTION_SHOW_DETAILS = "show-details";
    private static final String ACTION_ALL_PROGRAMS = "all-programs";
    private static final String ACTION_AVAILABLE_PROGRAMS = "available-programs";
    private static final String ACTION_SELECTED_PROGRAM = "selected-program";
    private static final String ACTION_ACTIVE_PROGRAM = "active-program";
    private static final String ACTION_OPERATION_STATE = "operation-state";
    private static final String ACTION_POWER_STATE = "power-state";
    private static final String ACTION_DOOR_STATE = "door-state";
    private static final String ACTION_REMOTE_START_ALLOWED = "remote-control-start-allowed";
    private static final String ACTION_REMOTE_CONTROL_ACTIVE = "remote-control-active";
    private static final String ACTION_PUT_RAW = "put-raw";
    private static final String ACTION_GET_RAW = "get-raw";
    private static final DateTimeFormatter FILE_EXPORT_DTF = ISO_OFFSET_DATE_TIME;
    private static final String EMPTY_RESPONSE = "{}";
    private static final long serialVersionUID = -2449763690208703307L;

    private final Logger logger = LoggerFactory.getLogger(HomeConnectServlet.class);
    private final HttpService httpService;
    private final TemplateEngine templateEngine;
    private final Set<HomeConnectBridgeHandler> bridgeHandlers;
    private final Gson gson;

    @Activate
    public HomeConnectServlet(@Reference HttpService httpService) {
        bridgeHandlers = new CopyOnWriteArraySet<>();
        gson = new GsonBuilder().registerTypeAdapter(ZonedDateTime.class, (JsonSerializer<ZonedDateTime>) (src,
                typeOfSrc, context) -> new JsonPrimitive(src.format(DateTimeFormatter.ISO_DATE_TIME))).create();
        this.httpService = httpService;

        // register servlet
        try {
            logger.debug("Initialize Home Connect configuration servlet ({})", SERVLET_PATH);
            httpService.registerServlet(SERVLET_PATH, this, null, httpService.createDefaultHttpContext());
            httpService.registerResources(ASSETS_PATH, "assets", null);
        } catch (ServletException | NamespaceException e) {
            logger.warn("Could not register Home Connect servlet! ({})", SERVLET_PATH, e);
        }

        // setup template engine
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(getServletContext());
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix("/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setCacheable(true);
        templateEngine = new TemplateEngine();
        templateEngine.addDialect(new Java8TimeDialect());
        templateEngine.setTemplateResolver(templateResolver);
    }

    @Deactivate
    protected void dispose() {
        httpService.unregister(SERVLET_PATH);
        httpService.unregister(ASSETS_PATH);
    }

    @Override
    protected void doGet(@Nullable HttpServletRequest request, @Nullable HttpServletResponse response)
            throws IOException {
        if (request == null || response == null) {
            return;
        }
        response.setContentType(DEFAULT_CONTENT_TYPE);
        response.setCharacterEncoding(UTF_8.name());

        String path = request.getPathInfo();
        if (path == null || path.isEmpty() || path.equals(ROOT_PATH)) {
            String code = request.getParameter(PARAM_CODE);
            String state = request.getParameter(PARAM_STATE);
            if (code != null && state != null && !code.trim().isEmpty() && !state.trim().isEmpty()) {
                getBridgeAuthenticationPage(request, response, code, state);
            } else {
                getBridgesPage(request, response);
            }
        } else if (pathMatches(path, APPLIANCES_PATH)) {
            String action = request.getParameter(PARAM_ACTION);
            String thingId = request.getParameter(PARAM_THING_ID);
            if (action != null && thingId != null && !action.trim().isEmpty() && !thingId.trim().isEmpty()) {
                processApplianceActions(response, action, thingId);
            } else {
                getAppliancesPage(request, response);
            }
        } else if (pathMatches(path, REQUEST_LOG_PATH)) {
            String export = request.getParameter(PARAM_EXPORT);
            String bridgeId = request.getParameter(PARAM_BRIDGE_ID);
            if (export != null && bridgeId != null && !export.trim().isEmpty() && !bridgeId.trim().isEmpty()) {
                getRequestLogExport(response, bridgeId);
            } else {
                getRequestLogPage(request, response);
            }
        } else if (pathMatches(path, EVENT_LOG_PATH)) {
            String export = request.getParameter(PARAM_EXPORT);
            String bridgeId = request.getParameter(PARAM_BRIDGE_ID);
            if (export != null && bridgeId != null && !export.trim().isEmpty() && !bridgeId.trim().isEmpty()) {
                getEventLogExport(response, bridgeId);
            } else {
                getEventLogPage(request, response);
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(@Nullable HttpServletRequest request, @Nullable HttpServletResponse response)
            throws IOException {
        if (request == null || response == null) {
            return;
        }
        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String path = request.getPathInfo();
        if (path == null || path.isEmpty() || path.equals(ROOT_PATH)) {
            if (request.getParameter(PARAM_ACTION) != null && request.getParameter(PARAM_BRIDGE_ID) != null) {
                postBridgesPage(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else if (pathMatches(path, APPLIANCES_PATH)) {
            String requestPayload = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            String action = request.getParameter(PARAM_ACTION);
            String thingId = request.getParameter(PARAM_THING_ID);
            String targetPath = request.getParameter(PARAM_PATH);

            if ((ACTION_PUT_RAW.equals(action) || ACTION_GET_RAW.equals(action)) && thingId != null
                    && targetPath != null && action != null) {
                processRawApplianceActions(response, action, thingId, targetPath, requestPayload);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Add Home Connect bridge handler to configuration servlet, to allow user to authenticate against Home Connect API.
     *
     * @param bridgeHandler bridge handler
     */
    public void addBridgeHandler(HomeConnectBridgeHandler bridgeHandler) {
        bridgeHandlers.add(bridgeHandler);
    }

    /**
     * Remove Home Connect bridge handler from configuration servlet.
     *
     * @param bridgeHandler bridge handler
     */
    public void removeBridgeHandler(HomeConnectBridgeHandler bridgeHandler) {
        bridgeHandlers.remove(bridgeHandler);
    }

    private void getAppliancesPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        WebContext context = new WebContext(request, response, request.getServletContext());
        context.setVariable("bridgeHandlers", bridgeHandlers);
        templateEngine.process("appliances", context, response.getWriter());
    }

    private void processApplianceActions(HttpServletResponse response, String action, String thingId)
            throws IOException {
        Optional<HomeConnectBridgeHandler> bridgeHandler = getBridgeHandlerForThing(thingId);
        Optional<AbstractHomeConnectThingHandler> thingHandler = getThingHandler(thingId);

        if (bridgeHandler.isPresent() && thingHandler.isPresent()) {
            try {
                response.setContentType(MediaType.APPLICATION_JSON);
                String haId = thingHandler.get().getThingHaId();

                switch (action) {
                    case ACTION_SHOW_DETAILS: {
                        String actionResponse = bridgeHandler.get().getApiClient().getRaw(haId,
                                "/api/homeappliances/" + haId);
                        response.getWriter().write(actionResponse != null ? actionResponse : EMPTY_RESPONSE);
                        break;
                    }
                    case ACTION_ALL_PROGRAMS: {
                        String actionResponse = bridgeHandler.get().getApiClient().getRaw(haId,
                                "/api/homeappliances/" + haId + "/programs");
                        response.getWriter().write(actionResponse != null ? actionResponse : EMPTY_RESPONSE);
                        break;
                    }
                    case ACTION_AVAILABLE_PROGRAMS: {
                        String actionResponse = bridgeHandler.get().getApiClient().getRaw(haId,
                                "/api/homeappliances/" + haId + "/programs/available");
                        response.getWriter().write(actionResponse != null ? actionResponse : EMPTY_RESPONSE);
                        break;
                    }
                    case ACTION_SELECTED_PROGRAM: {
                        String actionResponse = bridgeHandler.get().getApiClient().getRaw(haId,
                                "/api/homeappliances/" + haId + "/programs/selected");
                        response.getWriter().write(actionResponse != null ? actionResponse : EMPTY_RESPONSE);
                        break;
                    }
                    case ACTION_ACTIVE_PROGRAM: {
                        String actionResponse = bridgeHandler.get().getApiClient().getRaw(haId,
                                "/api/homeappliances/" + haId + "/programs/active");
                        response.getWriter().write(actionResponse != null ? actionResponse : EMPTY_RESPONSE);
                        break;
                    }
                    case ACTION_OPERATION_STATE: {
                        String actionResponse = bridgeHandler.get().getApiClient().getRaw(haId,
                                "/api/homeappliances/" + haId + "/status/" + EVENT_OPERATION_STATE);
                        response.getWriter().write(actionResponse != null ? actionResponse : EMPTY_RESPONSE);
                        break;
                    }
                    case ACTION_POWER_STATE: {
                        String actionResponse = bridgeHandler.get().getApiClient().getRaw(haId,
                                "/api/homeappliances/" + haId + "/settings/" + EVENT_POWER_STATE);
                        response.getWriter().write(actionResponse != null ? actionResponse : EMPTY_RESPONSE);
                        break;
                    }
                    case ACTION_DOOR_STATE: {
                        String actionResponse = bridgeHandler.get().getApiClient().getRaw(haId,
                                "/api/homeappliances/" + haId + "/status/" + EVENT_DOOR_STATE);
                        response.getWriter().write(actionResponse != null ? actionResponse : EMPTY_RESPONSE);
                        break;
                    }
                    case ACTION_REMOTE_START_ALLOWED: {
                        String actionResponse = bridgeHandler.get().getApiClient().getRaw(haId,
                                "/api/homeappliances/" + haId + "/status/" + EVENT_REMOTE_CONTROL_START_ALLOWED);
                        response.getWriter().write(actionResponse != null ? actionResponse : EMPTY_RESPONSE);
                        break;
                    }
                    case ACTION_REMOTE_CONTROL_ACTIVE: {
                        String actionResponse = bridgeHandler.get().getApiClient().getRaw(haId,
                                "/api/homeappliances/" + haId + "/status/" + EVENT_REMOTE_CONTROL_ACTIVE);
                        response.getWriter().write(actionResponse != null ? actionResponse : EMPTY_RESPONSE);
                        break;
                    }
                    default:
                        response.sendError(HttpStatus.BAD_REQUEST_400, "Unknown action");
                        break;
                }
            } catch (CommunicationException | ApplianceOfflineException | AuthorizationException e) {
                logger.debug("Could not execute request! thingId={}, action={}, error={}", thingId, action,
                        e.getMessage());
                response.sendError(HttpStatus.INTERNAL_SERVER_ERROR_500, e.getMessage());
            }
        } else {
            response.sendError(HttpStatus.BAD_REQUEST_400, "Thing or bridge not found!");
        }
    }

    private void processRawApplianceActions(HttpServletResponse response, String action, String thingId, String path,
            String body) throws IOException {
        Optional<HomeConnectBridgeHandler> bridgeHandler = getBridgeHandlerForThing(thingId);
        Optional<AbstractHomeConnectThingHandler> thingHandler = getThingHandler(thingId);

        if (bridgeHandler.isPresent() && thingHandler.isPresent()) {
            try {
                response.setContentType(MediaType.APPLICATION_JSON);
                String haId = thingHandler.get().getThingHaId();

                if (ACTION_PUT_RAW.equals(action)) {
                    String actionResponse = bridgeHandler.get().getApiClient().putRaw(haId, path, body);
                    response.getWriter().write(actionResponse);
                } else if (ACTION_GET_RAW.equals(action)) {
                    String actionResponse = bridgeHandler.get().getApiClient().getRaw(haId, path, true);
                    if (actionResponse == null) {
                        response.getWriter().write("{\"status\": \"No response\"}");
                    } else {
                        response.getWriter().write(actionResponse);
                    }
                } else {
                    response.sendError(HttpStatus.BAD_REQUEST_400, "Unknown action");
                }
            } catch (CommunicationException | ApplianceOfflineException | AuthorizationException e) {
                logger.debug("Could not execute request! thingId={}, action={}, error={}", thingId, action,
                        e.getMessage());
                response.sendError(HttpStatus.INTERNAL_SERVER_ERROR_500, e.getMessage());
            }
        } else {
            response.sendError(HttpStatus.BAD_REQUEST_400, "Bridge or Thing not found!");
        }
    }

    private void getBridgesPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        WebContext context = new WebContext(request, response, request.getServletContext());
        context.setVariable("bridgeHandlers", bridgeHandlers);
        templateEngine.process("bridges", context, response.getWriter());
    }

    private void postBridgesPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = request.getParameter(PARAM_ACTION);
        String bridgeId = request.getParameter(PARAM_BRIDGE_ID);
        Optional<HomeConnectBridgeHandler> bridgeHandlerOptional = bridgeHandlers.stream().filter(
                homeConnectBridgeHandler -> homeConnectBridgeHandler.getThing().getUID().toString().equals(bridgeId))
                .findFirst();

        if (bridgeHandlerOptional.isPresent()
                && (ACTION_AUTHORIZE.equals(action) || ACTION_CLEAR_CREDENTIALS.equals(action))) {
            HomeConnectBridgeHandler bridgeHandler = bridgeHandlerOptional.get();
            if (ACTION_AUTHORIZE.equals(action)) {
                try {
                    String redirectUri = bridgeHandler.getConfiguration().isSimulator()
                            ? request.getParameter(PARAM_REDIRECT_URI)
                            : null;
                    String authorizationUrl = bridgeHandler.getOAuthClientService().getAuthorizationUrl(redirectUri,
                            null, bridgeHandler.getThing().getUID().getAsString());
                    logger.debug("Generated authorization url: {}", authorizationUrl);

                    response.sendRedirect(authorizationUrl);
                } catch (OAuthException e) {
                    logger.error("Could not create authorization url!", e);
                    response.sendError(HttpStatus.INTERNAL_SERVER_ERROR_500, "Could not create authorization url!");
                }
            } else {
                logger.info("Remove access token for '{}' bridge.", bridgeHandler.getThing().getLabel());
                try {
                    bridgeHandler.getOAuthClientService().remove();
                } catch (OAuthException e) {
                    logger.debug("Could not clear oAuth credentials. error={}", e.getMessage());
                }
                bridgeHandler.reinitialize();

                WebContext context = new WebContext(request, response, request.getServletContext());
                context.setVariable("action",
                        bridgeHandler.getThing().getUID().getAsString() + ACTION_CLEAR_CREDENTIALS);
                context.setVariable("bridgeHandlers", bridgeHandlers);
                templateEngine.process("bridges", context, response.getWriter());
            }
        } else {
            response.sendError(HttpStatus.BAD_REQUEST_400, "Unknown bridge or action is missing!");
        }
    }

    private void getRequestLogPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ArrayList<ApiRequest> requests = new ArrayList<>();
        bridgeHandlers.forEach(homeConnectBridgeHandler -> requests
                .addAll(homeConnectBridgeHandler.getApiClient().getLatestApiRequests()));

        WebContext context = new WebContext(request, response, request.getServletContext());
        context.setVariable("bridgeHandlers", bridgeHandlers);
        context.setVariable("requests", gson.toJson(requests));
        templateEngine.process("log-requests", context, response.getWriter());
    }

    private void getRequestLogExport(HttpServletResponse response, String bridgeId) throws IOException {
        Optional<HomeConnectBridgeHandler> bridgeHandler = getBridgeHandler(bridgeId);
        if (bridgeHandler.isPresent()) {
            response.setContentType(MediaType.APPLICATION_JSON);
            String fileName = String.format("%s__%s__requests.json", now().format(FILE_EXPORT_DTF),
                    bridgeId.replaceAll("[^a-zA-Z0-9]", "_"));
            response.setHeader("Content-disposition", "attachment; filename=" + fileName);

            HashMap<String, Object> responsePayload = new HashMap<>();
            responsePayload.put("openHAB", OpenHAB.getVersion());
            responsePayload.put("bundle", FrameworkUtil.getBundle(this.getClass()).getVersion().toString());
            List<ApiRequest> apiRequestList = bridgeHandler.get().getApiClient().getLatestApiRequests().stream()
                    .peek(apiRequest -> {
                        Map<String, String> headers = apiRequest.getRequest().getHeader();
                        if (headers.containsKey("authorization")) {
                            headers.put("authorization", "*replaced*");
                        } else if (headers.containsKey("Authorization")) {
                            headers.put("Authorization", "*replaced*");
                        }
                    }).collect(Collectors.toList());
            responsePayload.put("requests", apiRequestList);
            response.getWriter().write(gson.toJson(responsePayload));
        } else {
            response.sendError(HttpStatus.BAD_REQUEST_400, "Unknown bridge");
        }
    }

    private void getEventLogPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        WebContext context = new WebContext(request, response, request.getServletContext());
        context.setVariable("bridgeHandlers", bridgeHandlers);
        templateEngine.process("log-events", context, response.getWriter());
    }

    private void getEventLogExport(HttpServletResponse response, String bridgeId) throws IOException {
        Optional<HomeConnectBridgeHandler> bridgeHandler = getBridgeHandler(bridgeId);
        if (bridgeHandler.isPresent()) {
            response.setContentType(MediaType.APPLICATION_JSON);
            String fileName = String.format("%s__%s__events.json", now().format(FILE_EXPORT_DTF),
                    bridgeId.replaceAll("[^a-zA-Z0-9]", "_"));
            response.setHeader("Content-disposition", "attachment; filename=" + fileName);

            HashMap<String, Object> responsePayload = new HashMap<>();
            responsePayload.put("openHAB", OpenHAB.getVersion());
            responsePayload.put("bundle", FrameworkUtil.getBundle(this.getClass()).getVersion().toString());
            responsePayload.put("events", bridgeHandler.get().getEventSourceClient().getLatestEvents());
            response.getWriter().write(gson.toJson(responsePayload));
        } else {
            response.sendError(HttpStatus.BAD_REQUEST_400, "Unknown bridge");
        }
    }

    private void getBridgeAuthenticationPage(HttpServletRequest request, HttpServletResponse response, String code,
            String state) throws IOException {
        // callback handling from authorization server
        logger.debug("[oAuth] redirect from authorization server (code={}, state={}).", code, state);

        Optional<HomeConnectBridgeHandler> bridgeHandler = getBridgeHandler(state);
        if (bridgeHandler.isPresent()) {
            try {
                String redirectUri = bridgeHandler.get().getConfiguration().isSimulator()
                        ? request.getRequestURL().toString()
                        : null;
                AccessTokenResponse accessTokenResponse = bridgeHandler.get().getOAuthClientService()
                        .getAccessTokenResponseByAuthorizationCode(code, redirectUri);

                logger.debug("access token response: {}", accessTokenResponse);

                // inform bridge
                bridgeHandler.get().reinitialize();

                WebContext context = new WebContext(request, response, request.getServletContext());
                context.setVariable("action", bridgeHandler.get().getThing().getUID().getAsString() + ACTION_AUTHORIZE);
                context.setVariable("bridgeHandlers", bridgeHandlers);
                templateEngine.process("bridges", context, response.getWriter());
            } catch (OAuthException | OAuthResponseException e) {
                logger.error("Could not fetch token!", e);
                response.sendError(HttpStatus.INTERNAL_SERVER_ERROR_500, "Could not fetch token!");
            }
        } else {
            response.sendError(HttpStatus.BAD_REQUEST_400, "Unknown bridge");
        }
    }

    private boolean pathMatches(String path, String targetPath) {
        return targetPath.equals(path) || (targetPath + SLASH).equals(path);
    }

    private Optional<HomeConnectBridgeHandler> getBridgeHandler(String bridgeUid) {
        for (HomeConnectBridgeHandler handler : bridgeHandlers) {
            if (handler.getThing().getUID().getAsString().equals(bridgeUid)) {
                return Optional.of(handler);
            }
        }
        return Optional.empty();
    }

    private Optional<AbstractHomeConnectThingHandler> getThingHandler(String thingUid) {
        for (HomeConnectBridgeHandler handler : bridgeHandlers) {
            for (AbstractHomeConnectThingHandler thingHandler : handler.getThingHandler()) {
                if (thingHandler.getThing().getUID().getAsString().equals(thingUid)) {
                    return Optional.of(thingHandler);
                }
            }
        }
        return Optional.empty();
    }

    private Optional<HomeConnectBridgeHandler> getBridgeHandlerForThing(String thingUid) {
        for (HomeConnectBridgeHandler handler : bridgeHandlers) {
            for (AbstractHomeConnectThingHandler thingHandler : handler.getThingHandler()) {
                if (thingHandler.getThing().getUID().getAsString().equals(thingUid)) {
                    return Optional.of(handler);
                }
            }
        }
        return Optional.empty();
    }
}
