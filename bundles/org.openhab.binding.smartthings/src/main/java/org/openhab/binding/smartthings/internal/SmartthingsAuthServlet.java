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
package org.openhab.binding.smartthings.internal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.UrlEncoded;
import org.openhab.binding.smartthings.internal.api.SmartthingsNetworkConnector;
import org.openhab.binding.smartthings.internal.handler.SmartthingsBridgeHandler;
import org.openhab.binding.smartthings.internal.type.SmartthingsException;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SpotifyAuthServlet} manages the authorization with the Smartthings Web API. The servlet implements the
 * Authorization Code flow and saves the resulting refreshToken with the bridge.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class SmartthingsAuthServlet extends SmartthingsBaseServlet {

    private static final long serialVersionUID = -4719613645562518231L;

    private static final String CONTENT_TYPE = "text/html;charset=UTF-8";
    private final Logger logger = LoggerFactory.getLogger(SmartthingsAuthServlet.class);
    private final SmartthingsAuthService smartthingsAuthService;

    private final String indexTemplate;
    private static final String HTML_USER_AUTHORIZED = "<p class='block authorized'>Bridge authorized for user %s.</p>";
    private static final String HTML_ERROR = "<p class='block error'>Call to Smartthings failed with error: %s</p>";

    // Keys present in the index.html
    private static final String KEY_PAGE_REFRESH = "pageRefresh";
    private static final String HTML_META_REFRESH_CONTENT = "<meta http-equiv='refresh' content='10; url=%s'>";
    private static final String KEY_AUTHORIZED_USER = "authorizedUser";
    private static final String KEY_ERROR = "error";
    private static final String KEY_BRIDGE_URI = "bridge.uri";
    private static final String KEY_REDIRECT_URI = "redirectUri";
    private static final String KEY_LOCATIONID_OPTION = "locationId.Option";
    private static final String KEY_POOL_STATUS = "poolStatus";

    public SmartthingsAuthServlet(SmartthingsBridgeHandler bridgeHandler, SmartthingsAuthService smartthingsAuthService,
            HttpService httpService, SmartthingsNetworkConnector networkConnector, String token)
            throws SmartthingsException {

        super(bridgeHandler, httpService, networkConnector, token);

        this.smartthingsAuthService = smartthingsAuthService;

        try {
            this.indexTemplate = readTemplate("index-oauth.html");

        } catch (IOException e) {
            throw new SmartthingsException("unable to initialize auth servlet", e);
        }
    }

    @Override
    protected void doGet(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp)
            throws ServletException, IOException {
        logger.debug("Smartthings auth callback servlet received GET request {}.", req.getRequestURI());
        final Map<String, String> replaceMap = new HashMap<>();

        StringBuffer requestUrl = req.getRequestURL();
        String servletBaseUrl = requestUrl != null ? requestUrl.toString() : "";
        String template = indexTemplate;
        StringBuffer optionBuffer = new StringBuffer();

        String servletBaseURLSecure = servletBaseUrl.replace("http://", "https://").replace("8080", "8443");

        handleSmartthingsRedirect(replaceMap, servletBaseURLSecure, req.getQueryString());
        resp.setContentType(CONTENT_TYPE);
        SmartthingsAccountHandler accountHandler = smartthingsAuthService.getSmartthingsAccountHandler();

        replaceMap.put(KEY_REDIRECT_URI, servletBaseURLSecure);
        replaceMap.put(KEY_BRIDGE_URI, accountHandler.formatAuthorizationUrl(servletBaseURLSecure));

        resp.getWriter().append(replaceKeysFromMap(template, replaceMap));
        resp.getWriter().close();
    }

    /**
     * Handles a possible call from Spotify to the redirect_uri. If that is the case Spotify will pass the authorization
     * codes via the url and these are processed. In case of an error this is shown to the user. If the user was
     * authorized this is passed on to the handler. Based on all these different outcomes the HTML is generated to
     * inform the user.
     *
     * @param replaceMap a map with key String values that will be mapped in the HTML templates.
     * @param servletBaseURL the servlet base, which should be used as the Spotify redirect_uri value
     * @param queryString the query part of the GET request this servlet is processing
     */
    private void handleSmartthingsRedirect(Map<String, String> replaceMap, String servletBaseURL,
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

            replaceMap.put(KEY_PAGE_REFRESH,
                    params.isEmpty() ? "" : String.format(HTML_META_REFRESH_CONTENT, servletBaseURL));
            if (!StringUtil.isBlank(reqError)) {
                logger.debug("Spotify redirected with an error: {}", reqError);
                replaceMap.put(KEY_ERROR, String.format(HTML_ERROR, reqError));
            } else if (!StringUtil.isBlank(reqState)) {
                try {
                    replaceMap.put(KEY_AUTHORIZED_USER, String.format(HTML_USER_AUTHORIZED,
                            smartthingsAuthService.authorize(servletBaseURL, reqState, reqCode)));
                } catch (RuntimeException e) {
                    logger.debug("Exception during authorizaton: ", e);
                    replaceMap.put(KEY_ERROR, String.format(HTML_ERROR, e.getMessage()));
                }
            }
        }
    }

}
