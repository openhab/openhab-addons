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

    // Keys present in the index.html
    private static final String KEY_BRIDGE_URI = "bridge.uri";

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
        /*
         *
         * final String servletBaseURL = req.getRequestURL().toString();
         * final String queryString = req.getQueryString();
         *
         *
         * String servletBaseURLSecure = servletBaseURL.replace("http://", "https://").replace("8080", "8443");
         * handleSmartthingsRedirect(replaceMap, servletBaseURLSecure, queryString);
         * resp.setContentType(CONTENT_TYPE);
         * LinkyAccountHandler accountHandler = linkyAuthService.getLinkyAccountHandler();
         */
        String uri = "https://mon-compte-particulier.enedis.fr/dataconnect/v1/oauth2/authorize?client_id=e551937c-5250-48bc-b4a6-2323af68db92&duration=P36M&response_type=code";

        // replaceMap.put(KEY_REDIRECT_URI, servletBaseURLSecure);
        replaceMap.put(KEY_BRIDGE_URI, uri);
        resp.getWriter().append(replaceKeysFromMap(indexTemplate, replaceMap));
        resp.getWriter().close();
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