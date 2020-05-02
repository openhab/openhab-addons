/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.smarther.internal.account;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.UrlEncoded;
import org.openhab.binding.smarther.internal.api.exception.SmartherAuthorizationException;
import org.openhab.binding.smarther.internal.api.model.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SmartherAuthorizationServlet} manages the authorization with the BTicino/Legrand API gateway. The servlet
 * implements the Authorization Code flow and saves the resulting refreshToken with the bridge.
 *
 * @author Fabio Possieri - Initial contribution
 */
@NonNullByDefault
public class SmartherAuthorizationServlet extends HttpServlet {

    private static final long serialVersionUID = 5199173744807168342L;

    private static final String CONTENT_TYPE = "text/html;charset=UTF-8";
    private static final String X_FORWARDED_PROTO = "X-Forwarded-Proto";

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

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final SmartherAccountService accountService;
    private final String indexTemplate;
    private final String applicationTemplate;

    public SmartherAuthorizationServlet(SmartherAccountService accountService, String indexTemplate,
            String applicationTemplate) {
        this.accountService = accountService;
        this.indexTemplate = indexTemplate;
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    protected void doGet(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp)
            throws ServletException, IOException {
        if (req == null || resp == null) {
            throw new SmartherAuthorizationException("Authorization callback with null request/response");
        }

        final String servletBaseURL = extractServletBaseURL(req);
        final Map<String, String> replaceMap = new HashMap<>();
        logger.debug("Authorization callback servlet received GET request {}", servletBaseURL);

        // Handle the received data
        handleSmartherRedirect(replaceMap, servletBaseURL, req.getQueryString());

        // Build response for the caller
        resp.setContentType(CONTENT_TYPE);
        replaceMap.put(KEY_REDIRECT_URI, servletBaseURL);
        replaceMap.put(KEY_APPLICATIONS, formatApplications(applicationTemplate, servletBaseURL));
        resp.getWriter().append(replaceKeysFromMap(indexTemplate, replaceMap));
        resp.getWriter().close();
    }

    /**
     * Extracts the servlet base URL from received HTTP request, possibly handling reverse proxies.
     *
     * @param req The received HTTP request
     * @return A string containing the servlet base URL
     */
    private String extractServletBaseURL(HttpServletRequest req) {
        final StringBuffer requestURL = req.getRequestURL();

        // Try to infer the real protocol from request headers
        final String realProtocol = StringUtils.defaultIfBlank(req.getHeader(X_FORWARDED_PROTO), req.getProtocol());

        return requestURL.replace(0, requestURL.indexOf(":"), realProtocol).toString();
    }

    /**
     * Handles a possible call from BTicino/Legrand API gateway to the redirect_uri. If that is the case,
     * BTicino/Legrand API gateway will pass the authorization codes via the url and these are processed. In case of an
     * error this is shown to the user. If the user was authorized this is passed on to the handler. Based on all these
     * different outcomes the HTML is generated to inform the user.
     *
     * @param replaceMap a map with key String values that will be mapped in the HTML templates
     * @param servletBaseURL the servlet base, which should be used as the BTicino/Legrand API gateway redirect_uri
     *            value
     * @param queryString the query part of the GET request this servlet is processing
     */
    private void handleSmartherRedirect(Map<String, String> replaceMap, String servletBaseURL,
            @Nullable String queryString) {
        replaceMap.put(KEY_AUTHORIZED_BRIDGE, "");
        replaceMap.put(KEY_ERROR, "");
        replaceMap.put(KEY_PAGE_REFRESH, "");

        if (queryString != null) {
            final MultiMap<String> params = new MultiMap<>();
            UrlEncoded.decodeTo(queryString, params, StandardCharsets.UTF_8.name());
            final String reqCode = params.getString("code");
            final String reqState = params.getString("state");
            final String reqError = params.getString("error");

            replaceMap.put(KEY_PAGE_REFRESH,
                    params.isEmpty() ? "" : String.format(HTML_META_REFRESH_CONTENT, servletBaseURL));
            if (!StringUtil.isBlank(reqError)) {
                logger.debug("Authorization redirected with an error: {}", reqError);
                replaceMap.put(KEY_ERROR, String.format(HTML_ERROR, reqError));
            } else if (!StringUtil.isBlank(reqState)) {
                try {
                    logger.debug("Received from authorization - state:[{}] code:[{}]", reqState, reqCode);
                    replaceMap.put(KEY_AUTHORIZED_BRIDGE, String.format(HTML_BRIDGE_AUTHORIZED,
                            accountService.authorize(servletBaseURL, reqState, reqCode)));
                } catch (RuntimeException e) {
                    logger.debug("Exception during authorizaton: ", e);
                    replaceMap.put(KEY_ERROR, String.format(HTML_ERROR, e.getMessage()));
                }
            }
        }
    }

    /**
     * Formats the HTML of all available Smarther Bridge application and returns it as a String
     *
     * @param applicationTemplate The application template to format the application values in
     * @param servletBaseURL the redirect_uri to be used in the authorization url created on the authorization button.
     * @return A String with the applications formatted with the application template
     */
    private String formatApplications(String applicationTemplate, String servletBaseURL) {
        final List<SmartherAccountHandler> applications = accountService.getSmartherAccountHandlers();

        return applications.isEmpty() ? HTML_EMPTY_APPLICATIONS
                : applications.stream().map(p -> formatApplication(applicationTemplate, p, servletBaseURL))
                        .collect(Collectors.joining());
    }

    /**
     * Formats the HTML of a Smarther Bridge application and returns it as a String
     *
     * @param applicationTemplate The application template to format the application values in
     * @param handler The handler for the application to format
     * @param servletBaseURL the redirect_uri to be used in the authorization url created on the authorization button
     * @return A String with the application formatted with the application template
     */
    private String formatApplication(String applicationTemplate, SmartherAccountHandler handler,
            String servletBaseURL) {
        final Map<String, String> map = new HashMap<>();

        map.put(APPLICATION_ID, handler.getUID().getAsString());
        map.put(APPLICATION_NAME, handler.getLabel());

        if (handler.isAuthorized()) {
            final String availableLocations = Location.toNameString(handler.listLocations());
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
     * Replaces all keys from the map found in the template with values from the map. If the key is not found the key
     * will be kept in the template.
     *
     * @param template template to replace keys with values
     * @param map map with key value pairs to replace in the template
     * @return a template with keys replaced
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
