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
package org.openhab.binding.linky.internal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * The xxx manages the authorization with the Linky Web API. The servlet implements the
 * Authorization Code flow and saves the resulting refreshToken with the bridge.
 *
 * @author Gaël L'hopital - Initial contribution *
 * @author Laurent Arnal - Rewrite addon to use official dataconect API
 */
@NonNullByDefault
public class LinkyAuthServlet extends HttpServlet {

    private static final long serialVersionUID = -4719613645562518231L;

    private static final Pattern MESSAGE_KEY_PATTERN = Pattern.compile("\\$\\{([^\\}]+)\\}");

    private static final String CONTENT_TYPE = "text/html;charset=UTF-8";

    private static final String HTML_USER_AUTHORIZED = "<p class='block authorized'>Addon authorized for %s.</p>";
    private static final String HTML_ERROR = "<p class='block error'>Call to Enedis failed with error: %s</p>";

    private static final String HTML_META_REFRESH_CONTENT = "<meta http-equiv='refresh' content='10; url=%s'>";

    // Keys present in the index.html
    private static final String KEY_AUTHORIZE_URI = "authorize.uri";
    private static final String KEY_RETRIEVE_TOKEN_URI = "retrieveToken.uri";
    private static final String KEY_REDIRECT_URI = "redirectUri";
    private static final String KEY_PRMID_OPTION = "prmId.Option";
    private static final String KEY_AUTHORIZED_USER = "authorizedUser";
    private static final String KEY_ERROR = "error";
    private static final String KEY_PAGE_REFRESH = "pageRefresh";

    private final Logger logger = LoggerFactory.getLogger(LinkyAuthServlet.class);
    private final LinkyAuthService linkyAuthService;
    private final String indexTemplate;

    public LinkyAuthServlet(LinkyAuthService linkyAuthService, String indexTemplate) {
        this.linkyAuthService = linkyAuthService;
        this.indexTemplate = indexTemplate;
    }

    @Override
    protected void doGet(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp)
            throws ServletException, IOException {
        logger.debug("Linky auth callback servlet received GET request {}.", req.getRequestURI());
        final Map<String, String> replaceMap = new HashMap<>();

        final String servletBaseURL = req.getRequestURL().toString();

        String servletBaseURLSecure = servletBaseURL;
        // .replace("http://", "https://");
        // .replace("8080", "8443");

        handleLinkyRedirect(replaceMap, servletBaseURLSecure, req.getQueryString());

        LinkyAccountHandler accountHandler = linkyAuthService.getLinkyAccountHandler();

        resp.setContentType(CONTENT_TYPE);

        StringBuffer optionBuffer = new StringBuffer();

        String[] prmIds = accountHandler.getAllPrmId();
        for (String prmId : prmIds) {
            optionBuffer.append("<option value=\"" + prmId + "\">" + prmId + "</option>");
        }

        replaceMap.put(KEY_PRMID_OPTION, optionBuffer.toString());
        replaceMap.put(KEY_REDIRECT_URI, servletBaseURLSecure);
        replaceMap.put(KEY_RETRIEVE_TOKEN_URI, servletBaseURLSecure + "?state=OK");
        replaceMap.put(KEY_AUTHORIZE_URI, accountHandler.formatAuthorizationUrl(servletBaseURLSecure));
        resp.getWriter().append(replaceKeysFromMap(indexTemplate, replaceMap));
        resp.getWriter().close();
    }

    /**
     * Handles a possible call from Enedis to the redirect_uri. If that is the case Spotify will pass the authorization
     * codes via the url and these are processed. In case of an error this is shown to the user. If the user was
     * authorized this is passed on to the handler. Based on all these different outcomes the HTML is generated to
     * inform the user.
     *
     * @param replaceMap a map with key String values that will be mapped in the HTML templates.
     * @param servletBaseURL the servlet base, which should be used as the Spotify redirect_uri value
     * @param queryString the query part of the GET request this servlet is processing
     */
    private void handleLinkyRedirect(Map<String, String> replaceMap, String servletBaseURL,
            @Nullable String queryString) {
        replaceMap.put(KEY_AUTHORIZED_USER, "");
        replaceMap.put(KEY_ERROR, "");
        replaceMap.put(KEY_PAGE_REFRESH, "");

        if (queryString != null) {
            final MultiMap<String> params = new MultiMap<>();
            UrlEncoded.decodeTo(queryString, params, StandardCharsets.UTF_8.name());
            final String reqCode = params.getString("code");
            final String reqState = params.getString("state");
            final String reqError = params.getString("error");

            replaceMap.put(KEY_PAGE_REFRESH, "");

            // params.isEmpty() ? "" : String.format(HTML_META_REFRESH_CONTENT, servletBaseURL)

            if (!StringUtil.isBlank(reqError)) {
                logger.debug("Spotify redirected with an error: {}", reqError);
                replaceMap.put(KEY_ERROR, String.format(HTML_ERROR, reqError));
            } else if (!StringUtil.isBlank(reqState)) {
                try {
                    replaceMap.put(KEY_AUTHORIZED_USER, String.format(HTML_USER_AUTHORIZED,
                            reqCode + " / " + linkyAuthService.authorize(servletBaseURL, reqState, reqCode)));
                } catch (RuntimeException e) {
                    logger.debug("Exception during authorizaton: ", e);
                    replaceMap.put(KEY_ERROR, String.format(HTML_ERROR, e.getMessage()));
                }
            }
        }
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