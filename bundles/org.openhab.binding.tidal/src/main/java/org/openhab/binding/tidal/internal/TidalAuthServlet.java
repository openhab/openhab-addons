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
package org.openhab.binding.tidal.internal;

import java.io.IOException;
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.UrlEncoded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TidalAuthServlet} manages the authorization with the Tidal Web API. The servlet implements the
 * Authorization Code flow and saves the resulting refreshToken with the bridge.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class TidalAuthServlet extends HttpServlet {

    private static final long serialVersionUID = -4719613645562518231L;

    private static final String CONTENT_TYPE = "text/html;charset=UTF-8";

    // Simple HTML templates for inserting messages.
    private static final String HTML_NO_BRIDE = "<p class='block'>Manually add a Tidal Bridge to authorize it here.<p>";
    private static final String HTML_USER_AUTHORIZED = "<p class='block authorized'>Bridge authorized for user %s.</p>";
    private static final String HTML_ERROR = "<p class='block error'>Call to Tidal failed with error: %s</p>";

    private static final Pattern MESSAGE_KEY_PATTERN = Pattern.compile("\\$\\{([^\\}]+)\\}");

    // Keys present in the index.html
    private static final String KEY_PAGE_REFRESH = "pageRefresh";
    private static final String HTML_META_REFRESH_CONTENT = "<meta http-equiv='refresh' content='10; url=%s'>";
    private static final String KEY_AUTHORIZED_USER = "authorizedUser";
    private static final String KEY_ERROR = "error";
    private static final String KEY_PLAYERS = "players";
    private static final String KEY_REDIRECT_URI = "redirectUri";
    // Keys present in the player.html
    private static final String BRIDGE_ID = "bridge.id";
    private static final String BRIDGE_NAME = "bridge.name";
    private static final String BRIDGE_TIDAL_USER_ID = "bridge.user";
    private static final String BRIDGE_AUTHORIZED_CLASS = "bridge.authorized";
    private static final String BRIDGE_AUTHORIZE = "bridge.authorize";

    private final Logger logger = LoggerFactory.getLogger(TidalAuthServlet.class);
    private final TidalAuthService tidalAuthService;
    private final String indexTemplate;

    public TidalAuthServlet(TidalAuthService tidalAuthService, String indexTemplate) {
        this.tidalAuthService = tidalAuthService;
        this.indexTemplate = indexTemplate;
    }

    @Override
    protected void doGet(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp)
            throws ServletException, IOException {
        logger.debug("Tidal auth callback servlet received GET request {}.", req.getRequestURI());
        final String servletBaseURL = req.getRequestURL().toString();
        final Map<String, String> replaceMap = new HashMap<>();

        String redirectUri = servletBaseURL;
        redirectUri = redirectUri.replace("http://", "https://");
        // redirectUri = "https://tidal.com/android/login/auth";
        String template = indexTemplate;
        handleTidalRedirect(replaceMap, redirectUri, req.getQueryString());
        resp.setContentType(CONTENT_TYPE);
        replaceMap.put(KEY_REDIRECT_URI, redirectUri);
        template = formatAccountHandlers(template, redirectUri);
        resp.getWriter().append(replaceKeysFromMap(template, replaceMap));
        resp.getWriter().close();
    }

    /**
     * Handles a possible call from Tidal to the redirect_uri. If that is the case Tidal will pass the authorization
     * codes via the url and these are processed. In case of an error this is shown to the user. If the user was
     * authorized this is passed on to the handler. Based on all these different outcomes the HTML is generated to
     * inform the user.
     *
     * @param replaceMap a map with key String values that will be mapped in the HTML templates.
     * @param servletBaseURL the servlet base, which should be used as the Tidal redirect_uri value
     * @param queryString the query part of the GET request this servlet is processing
     */
    private void handleTidalRedirect(Map<String, String> replaceMap, String redirectUri, @Nullable String queryString) {
        replaceMap.put(KEY_AUTHORIZED_USER, "");
        replaceMap.put(KEY_ERROR, "");
        replaceMap.put(KEY_PAGE_REFRESH, "");

        if (queryString != null) {
            final MultiMap<String> params = new MultiMap<>();
            UrlEncoded.decodeTo(queryString, params, StandardCharsets.UTF_8.name());
            final String reqCode = params.getString("code");
            final String reqState = params.getString("state");
            final String reqError = params.getString("error");

            if (!StringUtil.isBlank(reqError)) {
                logger.debug("Tidal redirected with an error: {}", reqError);
                replaceMap.put(KEY_ERROR, String.format(HTML_ERROR, reqError));
            } else if (!StringUtil.isBlank(reqState)) {
                try {
                    replaceMap.put(KEY_AUTHORIZED_USER, String.format(HTML_USER_AUTHORIZED,
                            tidalAuthService.authorize(redirectUri, reqState, reqCode)));
                } catch (RuntimeException e) {
                    logger.debug("Exception during authorizaton: ", e);
                    replaceMap.put(KEY_ERROR, String.format(HTML_ERROR, e.getMessage()));
                }
            }
        }
    }

    /**
     * Formats the HTML of all available Tidal Bridge Players and returns it as a String
     *
     * @param playerTemplate The player template to format the player values in
     * @param servletBaseURL the redirect_uri to be used in the authorization url created on the authorization button.
     * @return A String with the players formatted with the player template
     */
    private String formatAccountHandlers(String template, String servletBaseURL) {
        final List<TidalAccountHandler> accountHandlers = tidalAuthService.getTidalAccountHandlers();

        return accountHandlers.isEmpty() ? HTML_NO_BRIDE
                : accountHandlers.stream().map(p -> formatAccountHandler(template, p, servletBaseURL))
                        .collect(Collectors.joining());
    }

    /**
     * Formats the HTML of a Tidal Bridge Player and returns it as a String
     *
     * @param playerTemplate The player template to format the player values in
     * @param handler The handler for the player to format
     * @param servletBaseURL the redirect_uri to be used in the authorization url created on the authorization button.
     * @return A String with the player formatted with the player template
     */
    private String formatAccountHandler(String playerTemplate, TidalAccountHandler handler, String redirectUri) {
        final Map<String, String> map = new HashMap<>();

        map.put(BRIDGE_ID, handler.getUID().getAsString());
        map.put(BRIDGE_NAME, handler.getLabel());
        final String tidalUser = handler.getUser();

        if (handler.isAuthorized()) {
            map.put(BRIDGE_AUTHORIZED_CLASS, " authorized");
            map.put(BRIDGE_TIDAL_USER_ID, String.format(" (Authorized user: %s)", tidalUser));
        } else if (!StringUtil.isBlank(tidalUser)) {
            map.put(BRIDGE_AUTHORIZED_CLASS, " Unauthorized");
            map.put(BRIDGE_TIDAL_USER_ID, String.format(" (Unauthorized user: %s)", tidalUser));
        } else {
            map.put(BRIDGE_AUTHORIZED_CLASS, "");
            map.put(BRIDGE_TIDAL_USER_ID, "");
        }

        map.put(BRIDGE_AUTHORIZE, handler.formatAuthorizationUrl(redirectUri));

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
}
