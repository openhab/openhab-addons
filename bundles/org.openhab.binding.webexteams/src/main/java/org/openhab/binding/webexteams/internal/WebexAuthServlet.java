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
package org.openhab.binding.webexteams.internal;

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
import org.eclipse.jetty.util.UrlEncoded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WebexAuthServlet} manages the authorization with the Webex API. The servlet implements the
 * Authorization Code flow and saves the resulting refreshToken with the bridge.
 *
 * @author Tom Deckers - Initial contribution
 */
@NonNullByDefault
public class WebexAuthServlet extends HttpServlet {
    static final long serialVersionUID = 42L;
    private static final String CONTENT_TYPE = "text/html;charset=UTF-8";

    // Simple HTML templates for inserting messages.
    private static final String HTML_EMPTY_ACCOUNTS = "<p class='block'>Manually add a Webex Account to authorize it here.<p>";
    private static final String HTML_USER_AUTHORIZED = "<div class='row authorized'>Account authorized for user %s.</div>";
    private static final String HTML_ERROR = "<p class='block error'>Call to Webex failed with error: %s</p>";

    private static final Pattern MESSAGE_KEY_PATTERN = Pattern.compile("\\$\\{([^\\}]+)\\}");

    // Keys present in the index.html
    private static final String KEY_PAGE_REFRESH = "pageRefresh";
    private static final String HTML_META_REFRESH_CONTENT = "<meta http-equiv='refresh' content='10; url=%s'>";
    private static final String KEY_AUTHORIZED_USER = "authorizedUser";
    private static final String KEY_ERROR = "error";
    private static final String KEY_ACCOUNTS = "accounts";
    private static final String KEY_REDIRECT_URI = "redirectUri";

    // Keys present in the account.html
    private static final String ACCOUNT_ID = "account.id";
    private static final String ACCOUNT_NAME = "account.name";
    private static final String ACCOUNT_USER_ID = "account.user";
    private static final String ACCOUNT_TYPE = "account.type";
    private static final String ACCOUNT_AUTHORIZE = "account.authorize";
    private static final String ACCOUNT_SHOWBTN = "account.showbtn";
    private static final String ACCOUNT_SHWOMSG = "account.showmsg";
    private static final String ACCOUNT_MSG = "account.msg";

    private final Logger logger = LoggerFactory.getLogger(WebexAuthServlet.class);
    private final WebexAuthService authService;
    private final String indexTemplate;
    private final String accountTemplate;

    public WebexAuthServlet(WebexAuthService authService, String indexTemplate, String accountTemplate) {
        this.authService = authService;
        this.indexTemplate = indexTemplate;
        this.accountTemplate = accountTemplate;
    }

    @Override
    protected void doGet(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp)
            throws ServletException, IOException {
        if (req != null && resp != null) {
            logger.debug("Webex auth callback servlet received GET request {}.", req.getRequestURI());
            final String servletBaseURL = req.getRequestURL().toString();
            final Map<String, String> replaceMap = new HashMap<>();

            handleRedirect(replaceMap, servletBaseURL, req.getQueryString());
            resp.setContentType(CONTENT_TYPE);
            replaceMap.put(KEY_REDIRECT_URI, servletBaseURL);
            replaceMap.put(KEY_ACCOUNTS, formatAccounts(this.accountTemplate, servletBaseURL));
            resp.getWriter().append(replaceKeysFromMap(this.indexTemplate, replaceMap));
            resp.getWriter().close();
        }
    }

    /**
     * Handles a possible call from Webex to the redirect_uri. If that is the case Webex will pass the authorization
     * codes via the url and these are processed. In case of an error this is shown to the user. If the user was
     * authorized this is passed on to the handler. Based on all these different outcomes the HTML is generated to
     * inform the user.
     *
     * @param replaceMap a map with key String values that will be mapped in the HTML templates.
     * @param servletBaseURL the servlet base, which should be used as the Webex redirect_uri value
     * @param queryString the query part of the GET request this servlet is processing
     */
    private void handleRedirect(Map<String, String> replaceMap, String servletBaseURL, @Nullable String queryString) {
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
            if (reqError != null && !reqError.isBlank()) {
                logger.debug("Webex redirected with an error: {}", reqError);
                replaceMap.put(KEY_ERROR, String.format(HTML_ERROR, reqError));
            } else if (!reqState.isBlank()) {
                try {
                    replaceMap.put(KEY_AUTHORIZED_USER, String.format(HTML_USER_AUTHORIZED,
                            authService.authorize(servletBaseURL, reqState, reqCode)));
                } catch (WebexTeamsException e) {
                    logger.debug("Exception during authorizaton: ", e);
                    replaceMap.put(KEY_ERROR, String.format(HTML_ERROR, e.getMessage()));
                }
            }
        }
    }

    /**
     * Formats the HTML of all available Webex Accounts and returns it as a String
     *
     * @param accountTemplate The account template to format the account values in
     * @param servletBaseURL the redirect_uri to be used in the authorization url created on the authorization button.
     * @return A String with the accounts formatted with the account template
     */
    private String formatAccounts(String accountTemplate, String servletBaseURL) {
        final List<WebexTeamsHandler> accounts = authService.getWebexTeamsHandlers();

        return accounts.isEmpty() ? HTML_EMPTY_ACCOUNTS
                : accounts.stream().map(p -> formatAccount(accountTemplate, p, servletBaseURL))
                        .collect(Collectors.joining());
    }

    /**
     * Formats the HTML of a Webex Account and returns it as a String
     *
     * @param accountTemplate The account template to format the account values in
     * @param handler The handler for the account to format
     * @param servletBaseURL the redirect_uri to be used in the authorization url created on the authorization button.
     * @return A String with the account formatted with the account template
     */
    private String formatAccount(String accountTemplate, WebexTeamsHandler handler, String servletBaseURL) {
        final Map<String, String> map = new HashMap<>();

        map.put(ACCOUNT_ID, handler.getUID().getAsString());
        map.put(ACCOUNT_NAME, handler.getLabel());
        final String webexUser = handler.getUser();

        if (!handler.isConfigured()) {
            map.put(ACCOUNT_USER_ID, "");
            map.put(ACCOUNT_SHOWBTN, "u-hide");
            map.put(ACCOUNT_SHWOMSG, "u-show");
            map.put(ACCOUNT_MSG, "Configure account.");
        } else if (handler.isAuthorized()) {
            map.put(ACCOUNT_USER_ID, String.format("Authorized user: %s", webexUser));
            map.put(ACCOUNT_SHOWBTN, "u-hide");
            map.put(ACCOUNT_SHWOMSG, "u-show");
            map.put(ACCOUNT_MSG, "Authorized.");
        } else if (webexUser.isBlank()) {
            map.put(ACCOUNT_USER_ID, "Unauthorized user");
            map.put(ACCOUNT_SHOWBTN, "u-show");
            map.put(ACCOUNT_SHWOMSG, "u-hide");
            map.put(ACCOUNT_MSG, "");
        } else {
            map.put(ACCOUNT_USER_ID, "");
            map.put(ACCOUNT_SHOWBTN, "u-hide");
            map.put(ACCOUNT_SHWOMSG, "u-show");
            map.put(ACCOUNT_MSG, "UNKNOWN");
        }

        map.put(ACCOUNT_TYPE, handler.accountType);
        map.put(ACCOUNT_AUTHORIZE, handler.formatAuthorizationUrl(servletBaseURL));
        return replaceKeysFromMap(accountTemplate, map);
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
