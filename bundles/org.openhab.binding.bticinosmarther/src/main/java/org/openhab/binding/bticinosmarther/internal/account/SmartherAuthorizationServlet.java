/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.bticinosmarther.internal.account;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;
import org.openhab.binding.bticinosmarther.internal.api.dto.Location;
import org.openhab.binding.bticinosmarther.internal.api.exception.SmartherGatewayException;
import org.openhab.binding.bticinosmarther.internal.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@code SmartherAuthorizationServlet} class acts as the registered endpoint for the user to automatically manage
 * the BTicino/Legrand API authorization process.
 * The servlet follows the OAuth2 Authorization Code flow, saving the resulting refreshToken within the Smarther Bridge.
 *
 * @author Fabio Possieri - Initial contribution
 */
@NonNullByDefault
public class SmartherAuthorizationServlet extends HttpServlet {

    private static final long serialVersionUID = 5199173744807168342L;

    private static final String CONTENT_TYPE = "text/html;charset=UTF-8";
    private static final String X_FORWARDED_PROTO = "X-Forwarded-Proto";

    // Http request parameters
    private static final String PARAM_CODE = "code";
    private static final String PARAM_STATE = "state";
    private static final String PARAM_ERROR = "error";

    // Simple HTML templates for inserting messages.
    private static final String HTML_EMPTY_APPLICATIONS = "<p class='block'>Manually add a Smarther Bridge to authorize it here<p>";
    private static final String HTML_BRIDGE_AUTHORIZED = "<p class='block authorized'>Bridge authorized for Client Id %s</p>";
    private static final String HTML_ERROR = "<p class='block error'>Call to Smarther API gateway failed with error: %s</p>";

    private static final Pattern MESSAGE_KEY_PATTERN = Pattern.compile("\\$\\{([^\\}]+)\\}");

    // Keys present in the index.html
    private static final String KEY_PAGE_REFRESH = "pageRefresh";
    private static final String HTML_META_REFRESH_CONTENT = "<meta http-equiv='refresh' content='10; url=%s'>";
    private static final String KEY_AUTHORIZED_BRIDGE = "authorizedBridge";
    private static final String KEY_ERROR = "error";
    private static final String KEY_APPLICATIONS = "applications";
    private static final String KEY_REDIRECT_URI = "redirectUri";
    // Keys present in the application.html
    private static final String APPLICATION_ID = "application.id";
    private static final String APPLICATION_NAME = "application.name";
    private static final String APPLICATION_LOCATIONS = "application.locations";
    private static final String APPLICATION_AUTHORIZED_CLASS = "application.authorized";
    private static final String APPLICATION_AUTHORIZE = "application.authorize";

    private final Logger logger = LoggerFactory.getLogger(SmartherAuthorizationServlet.class);

    private final SmartherAccountService accountService;
    private final String indexTemplate;
    private final String applicationTemplate;

    /**
     * Constructs a {@code SmartherAuthorizationServlet} associated to the given {@link SmartherAccountService} service
     * and with the given html index/application templates.
     *
     * @param accountService
     *            the account service to associate to the servlet
     * @param indexTemplate
     *            the html template to use as index page for the user
     * @param applicationTemplate
     *            the html template to use as application page for the user
     */
    public SmartherAuthorizationServlet(SmartherAccountService accountService, String indexTemplate,
            String applicationTemplate) {
        this.accountService = accountService;
        this.indexTemplate = indexTemplate;
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    protected void doGet(@Nullable HttpServletRequest request, @Nullable HttpServletResponse response)
            throws ServletException, IOException {
        if (request != null && response != null) {
            final String servletBaseURL = extractServletBaseURL(request);
            logger.debug("Authorization callback servlet received GET request {}", servletBaseURL);

            // Handle the received data
            final Map<String, String> replaceMap = new HashMap<>();
            handleSmartherRedirect(replaceMap, servletBaseURL, request.getQueryString());

            // Build a http 200 (Success) response for the caller
            response.setContentType(CONTENT_TYPE);
            response.setStatus(HttpStatus.OK_200);
            replaceMap.put(KEY_REDIRECT_URI, servletBaseURL);
            replaceMap.put(KEY_APPLICATIONS, formatApplications(applicationTemplate, servletBaseURL));
            response.getWriter().append(replaceKeysFromMap(indexTemplate, replaceMap));
            response.getWriter().close();
        } else if (response != null) {
            // Build a http 400 (Bad Request) error response for the caller
            response.setContentType(CONTENT_TYPE);
            response.setStatus(HttpStatus.BAD_REQUEST_400);
            response.getWriter().close();
        } else {
            throw new ServletException("Authorization callback with null request/response");
        }
    }

    /**
     * Extracts the servlet base url from the received http request, handling eventual reverse proxy.
     *
     * @param request
     *            the received http request
     *
     * @return a string containing the servlet base url
     */
    private String extractServletBaseURL(HttpServletRequest request) {
        final StringBuffer requestURL = request.getRequestURL();

        // Try to infer the real protocol from request headers
        String realProtocol = request.getHeader(X_FORWARDED_PROTO);
        if (realProtocol == null || realProtocol.isBlank()) {
            realProtocol = request.getScheme();
        }
        return requestURL.replace(0, requestURL.indexOf(":"), realProtocol).toString();
    }

    /**
     * Handles a call from BTicino/Legrand API gateway to the redirect_uri, dispatching the authorization flow to the
     * proper authorization handler.
     * If the user was authorized, this is passed on to the handler; in case of an error, this is shown to the user.
     * Based on all these different outcomes the html response is generated to inform the user.
     *
     * @param replaceMap
     *            a map with key string values to use in the html templates
     * @param servletBaseURL
     *            the servlet base url to compose the correct API gateway redirect_uri
     * @param queryString
     *            the querystring part of the received request, may be {@code null}
     */
    private void handleSmartherRedirect(Map<String, String> replaceMap, String servletBaseURL,
            @Nullable String queryString) {
        replaceMap.put(KEY_AUTHORIZED_BRIDGE, "");
        replaceMap.put(KEY_ERROR, "");
        replaceMap.put(KEY_PAGE_REFRESH, "");

        if (queryString != null) {
            final MultiMap<String> params = new MultiMap<>();
            UrlEncoded.decodeTo(queryString, params, StandardCharsets.UTF_8.name());
            final String reqCode = params.getString(PARAM_CODE);
            final String reqState = params.getString(PARAM_STATE);
            final String reqError = params.getString(PARAM_ERROR);

            replaceMap.put(KEY_PAGE_REFRESH,
                    params.isEmpty() ? "" : String.format(HTML_META_REFRESH_CONTENT, servletBaseURL));
            if (!StringUtil.isBlank(reqError)) {
                logger.debug("Authorization redirected with an error: {}", reqError);
                replaceMap.put(KEY_ERROR, String.format(HTML_ERROR, reqError));
            } else if (!StringUtil.isBlank(reqState)) {
                try {
                    logger.trace("Received from authorization - state:[{}] code:[{}]", reqState, reqCode);
                    replaceMap.put(KEY_AUTHORIZED_BRIDGE, String.format(HTML_BRIDGE_AUTHORIZED,
                            accountService.dispatchAuthorization(servletBaseURL, reqState, reqCode)));
                } catch (SmartherGatewayException e) {
                    logger.debug("Exception during authorizaton: ", e);
                    replaceMap.put(KEY_ERROR, String.format(HTML_ERROR, e.getMessage()));
                }
            }
        }
    }

    /**
     * Returns an html formatted text representing all the available Smarther Bridge applications.
     *
     * @param applicationTemplate
     *            the html template to format the application with
     * @param servletBaseURL
     *            the redirect_uri to link to the authorization button as authorization url
     *
     * @return a string containing the html formatted text
     */
    private String formatApplications(String applicationTemplate, String servletBaseURL) {
        final Set<SmartherAccountHandler> applications = accountService.getSmartherAccountHandlers();

        return applications.isEmpty() ? HTML_EMPTY_APPLICATIONS
                : applications.stream().map(p -> formatApplication(applicationTemplate, p, servletBaseURL))
                        .collect(Collectors.joining());
    }

    /**
     * Returns an html formatted text representing a given Smarther Bridge application.
     *
     * @param applicationTemplate
     *            the html template to format the application with
     * @param handler
     *            the Smarther application handler to use
     * @param servletBaseURL
     *            the redirect_uri to link to the authorization button as authorization url
     *
     * @return a string containing the html formatted text
     */
    private String formatApplication(String applicationTemplate, SmartherAccountHandler handler,
            String servletBaseURL) {
        final Map<String, String> map = new HashMap<>();

        map.put(APPLICATION_ID, handler.getUID().getAsString());
        map.put(APPLICATION_NAME, handler.getLabel());

        if (handler.isAuthorized()) {
            final String availableLocations = Location.toNameString(handler.getLocations());
            map.put(APPLICATION_AUTHORIZED_CLASS, " authorized");
            map.put(APPLICATION_LOCATIONS, String.format(" (Available locations: %s)", availableLocations));
        } else {
            map.put(APPLICATION_AUTHORIZED_CLASS, "");
            map.put(APPLICATION_LOCATIONS, "");
        }

        map.put(APPLICATION_AUTHORIZE, handler.formatAuthorizationUrl(servletBaseURL));
        return replaceKeysFromMap(applicationTemplate, map);
    }

    /**
     * Replaces all keys found in the template with the values matched from the map.
     * If a key is not found in the map, it is kept unchanged in the template.
     *
     * @param template
     *            the template to replace keys on
     * @param map
     *            the map containing the key/value pairs to replace in the template
     *
     * @return a string containing the resulting template after the replace process
     */
    private String replaceKeysFromMap(String template, Map<String, String> map) {
        final Matcher m = MESSAGE_KEY_PATTERN.matcher(template);
        final StringBuffer sb = new StringBuffer();

        while (m.find()) {
            try {
                final String key = m.group(1);
                m.appendReplacement(sb, Matcher.quoteReplacement(map.getOrDefault(key, "${" + key + '}')));
            } catch (RuntimeException e) {
                logger.warn("Error occurred during template filling, cause ", e);
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
