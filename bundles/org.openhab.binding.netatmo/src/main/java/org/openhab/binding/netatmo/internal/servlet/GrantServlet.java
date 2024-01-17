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
package org.openhab.binding.netatmo.internal.servlet;

import static org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.PARAM_ERROR;
import static org.openhab.core.auth.oauth2client.internal.Keyword.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;
import org.openhab.binding.netatmo.internal.handler.ApiBridgeHandler;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GrantServlet} manages the authorization with the Netatmo API. The servlet implements the
 * Authorization Code flow and saves the resulting refreshToken with the bridge.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class GrantServlet extends NetatmoServlet {
    private static final long serialVersionUID = 4817341543768441689L;
    private static final Pattern MESSAGE_KEY_PATTERN = Pattern.compile("\\$\\{([^\\}]+)\\}");
    private static final String TEMPLATE_ACCOUNT = "template/account.html";
    private static final String CONTENT_TYPE = "text/html;charset=UTF-8";

    // Simple HTML templates for inserting messages.
    private static final String HTML_ERROR = "<p class='block error'>Call to Netatmo Connect failed with error: %s</p>";

    // Keys present in the account.html
    private static final String KEY_ERROR = "error";
    private static final String ACCOUNT_NAME = "account.name";
    private static final String ACCOUNT_AUTHORIZED_CLASS = "account.authorized";
    private static final String ACCOUNT_AUTHORIZE = "account.authorize";

    private final Logger logger = LoggerFactory.getLogger(GrantServlet.class);
    private final @NonNullByDefault({}) ClassLoader classLoader = GrantServlet.class.getClassLoader();
    private final String accountTemplate;

    public GrantServlet(ApiBridgeHandler handler, HttpService httpService) {
        super(handler, httpService, "connect");
        try (InputStream stream = classLoader.getResourceAsStream(TEMPLATE_ACCOUNT)) {
            accountTemplate = stream != null ? new String(stream.readAllBytes(), StandardCharsets.UTF_8) : "";
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to load template account file. Please file a bug report.");
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

            String label = handler.getThing().getLabel();
            replaceMap.put(ACCOUNT_NAME, label != null ? label : "");
            replaceMap.put(CLIENT_ID, handler.getId());
            replaceMap.put(ACCOUNT_AUTHORIZED_CLASS, handler.isConnected() ? " authorized" : " Unauthorized");
            replaceMap.put(ACCOUNT_AUTHORIZE,
                    handler.formatAuthorizationUrl().queryParam(REDIRECT_URI, servletBaseURL).build().toString());
            replaceMap.put(REDIRECT_URI, servletBaseURL);

            resp.setContentType(CONTENT_TYPE);
            resp.getWriter().append(replaceKeysFromMap(accountTemplate, replaceMap));
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
     * @param servletBaseURL the servlet base, which should be used as the redirect_uri value
     * @param queryString the query part of the GET request this servlet is processing
     */
    private void handleRedirect(Map<String, String> replaceMap, String servletBaseURL, @Nullable String queryString) {
        replaceMap.put(KEY_ERROR, "");

        if (queryString != null) {
            final MultiMap<@Nullable String> params = new MultiMap<>();
            UrlEncoded.decodeTo(queryString, params, StandardCharsets.UTF_8.name());
            final String reqCode = params.getString(CODE);
            final String reqState = params.getString(STATE);
            final String reqError = params.getString(PARAM_ERROR);

            if (reqError != null) {
                logger.debug("Netatmo redirected with an error: {}", reqError);
                replaceMap.put(KEY_ERROR, String.format(HTML_ERROR, reqError));
            } else if (reqState != null && reqCode != null) {
                handler.openConnection(reqCode, servletBaseURL);
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
