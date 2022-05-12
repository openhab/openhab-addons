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
package org.openhab.binding.netatmo.internal.servlet;

import static org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.PARAM_ERROR;
import static org.openhab.core.auth.oauth2client.internal.Keyword.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.handler.ApiBridgeHandler;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AuthenticationServlet} manages the authorization with the Netatmo API. The servlet implements the
 * Authorization Code flow and saves the resulting refreshToken with the bridge.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class AuthenticationServlet extends HttpServlet implements NetatmoServlet {
    private static final long serialVersionUID = 4817341543768441689L;

    private static final String TEMPLATE_PATH = "templates/";
    private static final String TEMPLATE_ACCOUNT = TEMPLATE_PATH + "account.html";
    private static final String TEMPLATE_INDEX = TEMPLATE_PATH + "index.html";

    private static final String CONTENT_TYPE = "text/html;charset=UTF-8";

    // Simple HTML templates for inserting messages.
    private static final String HTML_EMPTY_ACCOUNTS = "<p class='block'>Manually add a Netatmo Bridge to authorize it here.<p>";
    private static final String HTML_ACCOUNT_AUTHORIZED = "<p class='block authorized'>Authorized Netatmo Account bridges.</p>";
    private static final String HTML_ERROR = "<p class='block error'>Call to Netatmo Connect failed with error: %s</p>";

    private static final Pattern MESSAGE_KEY_PATTERN = Pattern.compile("\\$\\{([^\\}]+)\\}");

    // Keys present in the index.html
    private static final String KEY_PAGE_REFRESH = "pageRefresh";
    private static final String HTML_META_REFRESH_CONTENT = "<meta http-equiv='refresh' content='10; url=%s'>";
    private static final String KEY_AUTHORIZED_ACCOUNT = "authorizedUser";
    private static final String KEY_ERROR = "error";
    private static final String KEY_ACCOUNTS = "accounts";
    private static final String KEY_REDIRECT_URI = "redirectUri";

    // Keys present in the account.html
    private static final String ACCOUNT_ID = "account.id";
    private static final String ACCOUNT_NAME = "account.name";
    private static final String ACCOUNT_AUTHORIZED_CLASS = "account.authorized";
    private static final String ACCOUNT_AUTHORIZE = "account.authorize";

    private final Logger logger = LoggerFactory.getLogger(AuthenticationServlet.class);
    private final ServletService servletService;
    private final String indexTemplate;
    private final String playerTemplate;
    private final BundleContext bundleContext;

    public AuthenticationServlet(ServletService servletService, BundleContext bundleContext) {
        this.servletService = servletService;
        this.bundleContext = bundleContext;
        try {
            this.indexTemplate = readTemplate(TEMPLATE_INDEX);
            this.playerTemplate = readTemplate(TEMPLATE_ACCOUNT);
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    String.format("Error reading ressource files, please file a bug report : %s", e.getMessage()));
        }
    }

    /**
     * Reads a template from file and returns the content as String.
     *
     * @param templateName name of the template file to read
     * @return The content of the template file
     * @throws IOException thrown when an HTML template could not be read
     */
    private String readTemplate(String templateName) throws IOException {
        final URL index = bundleContext.getBundle().getEntry(templateName);

        if (index == null) {
            throw new FileNotFoundException(String
                    .format("Cannot find '{}' - failed to initialize Netatmo Authentication servlet", templateName));
        } else {
            try (InputStream inputStream = index.openStream()) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.debug("Netatmo auth callback servlet received GET request {}.", req.getRequestURI());
        StringBuffer requestUrl = req.getRequestURL();
        if (requestUrl != null) {
            final String servletBaseURL = requestUrl.toString();
            final Map<String, String> replaceMap = new HashMap<>();

            handleRedirect(replaceMap, servletBaseURL, req.getQueryString());
            resp.setContentType(CONTENT_TYPE);
            replaceMap.put(KEY_REDIRECT_URI, servletBaseURL);
            replaceMap.put(KEY_ACCOUNTS, formatPlayers(playerTemplate, servletBaseURL));
            resp.getWriter().append(replaceKeysFromMap(indexTemplate, replaceMap));
            resp.getWriter().close();
        } else {
            logger.warn("Unexpected : requestUrl is null");
        }
    }

    /**
     * Handles a possible call from Netatmo to the redirect_uri. If that is the case it will pass the authorization
     * codes via the url and these are processed. In case of an error this is shown to the user. If the user was
     * authorized this is passed on to the handler. Based on all these different outcomes the HTML is generated to
     * inform the user.
     *
     * @param replaceMap a map with key String values that will be mapped in the HTML templates.
     * @param servletBaseURL the servlet base, which should be used as the Spotify redirect_uri value
     * @param queryString the query part of the GET request this servlet is processing
     */
    private void handleRedirect(Map<String, String> replaceMap, String servletBaseURL, @Nullable String queryString) {
        replaceMap.put(KEY_ERROR, "");
        replaceMap.put(KEY_PAGE_REFRESH, "");

        if (queryString != null) {
            final MultiMap<@Nullable String> params = new MultiMap<>();
            UrlEncoded.decodeTo(queryString, params, StandardCharsets.UTF_8.name());
            final String reqCode = params.getString(CODE);
            final String reqState = params.getString(STATE);
            final String reqError = params.getString(PARAM_ERROR);

            replaceMap.put(KEY_PAGE_REFRESH,
                    params.isEmpty() ? "" : String.format(HTML_META_REFRESH_CONTENT, servletBaseURL));
            if (reqError != null) {
                logger.debug("Netatmo redirected with an error: {}", reqError);
                replaceMap.put(KEY_ERROR, String.format(HTML_ERROR, reqError));
            } else if (reqState != null && reqCode != null) {
                try {
                    authorize(servletBaseURL, reqState, reqCode);
                    replaceMap.put(KEY_AUTHORIZED_ACCOUNT, HTML_ACCOUNT_AUTHORIZED);
                } catch (NetatmoException e) {
                    logger.debug("Exception during authorizaton: ", e);
                    replaceMap.put(KEY_ERROR, String.format(HTML_ERROR, e.getMessage()));
                }
            }
        }
    }

    /**
     * Call with Netatmo redirect uri returned State and Code values to get the refresh and access tokens and persist
     * these values
     *
     * @param servletBaseURL the servlet base, which will be the Netatmo redirect url
     * @param state The Netatmo returned state value
     * @param code The Netatmo returned code value
     * @throws NetatmoException
     */
    public void authorize(String servletBaseURL, String state, String code) throws NetatmoException {
        ApiBridgeHandler listener = servletService.getAccountHandlers().get(state);
        if (listener != null) {
            listener.receiveAuthorization(servletBaseURL, code);
            return;
        }
        throw new NetatmoException("Returned '%s' doesn't match any Bridge. Has it been removed?", state);
    }

    /**
     * Formats the HTML of all available Netatmo Account Bridges and returns it as a String
     *
     * @param playerTemplate The player template to format the player values in
     * @param servletBaseURL the redirect_uri to be used in the authorization url created on the authorization button.
     * @return A String with the players formatted with the player template
     */
    private String formatPlayers(String accountTemplate, String servletBaseURL) {
        final Collection<ApiBridgeHandler> handlers = servletService.getAccountHandlers().values();

        return handlers.isEmpty() ? HTML_EMPTY_ACCOUNTS
                : handlers.stream().map(p -> formatPlayer(accountTemplate, p, servletBaseURL))
                        .collect(Collectors.joining());
    }

    /**
     * Formats the HTML of a Netatmo Account Bridge and returns it as a String
     *
     * @param playerTemplate The player template to format the player values in
     * @param handler The handler for the player to format
     * @param servletBaseURL the redirect_uri to be used in the authorization url created on the authorization button.
     * @return A String with the player formatted with the player template
     */
    private String formatPlayer(String playerTemplate, ApiBridgeHandler handler, String servletBaseURL) {
        final Map<String, String> map = new HashMap<>();

        map.put(ACCOUNT_ID, handler.getUIDString());
        map.put(ACCOUNT_NAME, handler.getLabel());

        if (handler.isConnected()) {
            map.put(ACCOUNT_AUTHORIZED_CLASS, " authorized");
        } else {
            map.put(ACCOUNT_AUTHORIZED_CLASS, " Unauthorized");
        }

        map.put(ACCOUNT_AUTHORIZE, handler.formatAuthorizationUrl());
        return replaceKeysFromMap(playerTemplate, map);
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
                logger.debug("Error occurred during template filling, cause ", e);
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    @Override
    public String getPath() {
        return "connect";
    }
}
