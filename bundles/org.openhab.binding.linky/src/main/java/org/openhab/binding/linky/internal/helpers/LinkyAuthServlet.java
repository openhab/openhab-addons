/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.linky.internal.helpers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
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
import org.openhab.binding.linky.internal.handler.BridgeRemoteApiHandler;
import org.openhab.binding.linky.internal.types.LinkyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The LinkyAuthServlet manages the authorization with the Linky Web API. The servlet implements the
 * Authorization Code flow and saves the resulting refreshToken with the bridge.
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Laurent Arnal - Rewrite addon to use official dataconect API
 */
@NonNullByDefault
public class LinkyAuthServlet extends HttpServlet {

    private static final long serialVersionUID = -4719613645562518231L;

    private static final Pattern MESSAGE_KEY_PATTERN = Pattern.compile("\\$\\{([^\\}]+)\\}");

    private static final String CONTENT_TYPE = "text/html;charset=UTF-8";

    private static final String HTML_USER_AUTHORIZED = "<p class='block authorized'>Addon authorized for %s.</p>";
    private static final String HTML_ERROR = "<p class='block error'>Call to Enedis failed with error: %s</p>";

    // Keys present in the index.html
    private static final String KEY_AUTHORIZE_URI = "authorize.uri";
    private static final String KEY_RETRIEVE_TOKEN_URI = "retrieveToken.uri";
    private static final String KEY_REDIRECT_URI = "redirectUri";
    private static final String KEY_CODE = "code.Value";
    private static final String KEY_PRMID = "prmId.Value";
    private static final String KEY_PRMID_OPTION = "prmId.Option";
    private static final String KEY_AUTHORIZED_USER = "authorizedUser";
    private static final String KEY_CB_DISPLAY_CONFIRMATION = "cb.displayConfirmation";
    private static final String KEY_CB_DISPLAY_ERROR = "cb.displayError";
    private static final String KEY_CB_DISPLAY_INSTRUCTION = "cb.displayInstruction";
    private static final String KEY_ERROR = "error";
    private static final String KEY_PAGE_REFRESH = "pageRefresh";
    private static final String TEMPLATE_PATH = "templates/";

    private final Logger logger = LoggerFactory.getLogger(LinkyAuthServlet.class);
    private final String index;
    private final String enedisStep1;
    private final String enedisStep2;
    private final String enedisStep3;
    private final String myelectricaldataStep1;
    private final String myelectricaldataStep2;
    private final String myelectricaldataStep3;

    private BridgeRemoteApiHandler apiBridgeHandler;

    public LinkyAuthServlet(BridgeRemoteApiHandler apiBridgeHandler) throws LinkyException {
        this.apiBridgeHandler = apiBridgeHandler;

        try {
            this.index = readTemplate("index.html");
            this.enedisStep1 = readTemplate("enedis-step1.html");
            this.enedisStep2 = readTemplate("enedis-step2.html");
            this.enedisStep3 = readTemplate("enedis-step3-cb.html");
            this.myelectricaldataStep1 = readTemplate("myelectricaldata-step1.html");
            this.myelectricaldataStep2 = readTemplate("myelectricaldata-step2.html");
            this.myelectricaldataStep3 = readTemplate("myelectricaldata-step3.html");
        } catch (IOException e) {
            throw new LinkyException("unable to initialize auth servlet", e);
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
        final URL url = apiBridgeHandler.getBundleContext().getBundle().getEntry(TEMPLATE_PATH + templateName);

        if (url == null) {
            throw new FileNotFoundException(
                    String.format("Cannot find {}' - failed to initialize Linky servlet".formatted(templateName)));
        } else {
            try (InputStream inputStream = url.openStream()) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.debug("Linky auth callback servlet received GET request {}.", req.getRequestURI());
        final Map<String, String> replaceMap = new HashMap<>();

        StringBuffer requestUrl = req.getRequestURL();
        String servletBaseUrl = requestUrl != null ? requestUrl.toString() : "";

        String template = "";
        if (servletBaseUrl.contains("index")) {
            template = index;
        } else if (servletBaseUrl.contains("enedis-step1")) {
            template = enedisStep1;
        } else if (servletBaseUrl.contains("enedis-step2")) {
            template = enedisStep2;
        } else if (servletBaseUrl.contains("enedis-step3-cb")) {
            template = enedisStep3;
        } else if (servletBaseUrl.contains("myelectricaldata-step1")) {
            template = myelectricaldataStep1;
        } else if (servletBaseUrl.contains("myelectricaldata-step2")) {
            template = myelectricaldataStep2;
        } else if (servletBaseUrl.contains("myelectricaldata-step3")) {
            template = myelectricaldataStep3;
        } else if (servletBaseUrl.contains("enedis")) {
            template = enedisStep1;
        } else if (servletBaseUrl.contains("myelectricaldata")) {
            template = myelectricaldataStep1;
        } else {
            template = index;
        }

        // for some unknown reason, getRequestURL return a malformed URL mixing http:// and port 443
        if (servletBaseUrl.contains(":443")) {
            servletBaseUrl = servletBaseUrl.replace("http://", "https://");
            servletBaseUrl = servletBaseUrl.replace(":443", "");
        }

        try {
            handleLinkyRedirect(replaceMap, servletBaseUrl, req.getQueryString());

            resp.setContentType(CONTENT_TYPE);

            StringBuffer optionBuffer = new StringBuffer();

            List<String> prmIds = apiBridgeHandler.getAllPrmId();
            for (String prmId : prmIds) {
                optionBuffer.append("<option value=\"" + prmId + "\">" + prmId + "</option>");
            }

            final MultiMap<@Nullable String> params = new MultiMap<>();
            String queryString = req.getQueryString();
            if (queryString != null) {
                UrlEncoded.decodeTo(queryString, params, StandardCharsets.UTF_8.name());
            }
            final String usagePointId = params.getString("usage_point_id");
            final String code = params.getString("code");

            replaceMap.put(KEY_PRMID, usagePointId);
            replaceMap.put(KEY_CODE, code);

            replaceMap.put(KEY_PRMID_OPTION, optionBuffer.toString());
            replaceMap.put(KEY_REDIRECT_URI, servletBaseUrl);
            replaceMap.put(KEY_RETRIEVE_TOKEN_URI, servletBaseUrl + "?state=OK");

            String authorizeUri = apiBridgeHandler.formatAuthorizationUrl("");
            replaceMap.put(KEY_AUTHORIZE_URI, authorizeUri);
            resp.getWriter().append(replaceKeysFromMap(template, replaceMap));
            resp.getWriter().close();
        } catch (LinkyException ex) {
            resp.setContentType(CONTENT_TYPE);
            replaceMap.put(KEY_ERROR, "Error during request handling : " + ex.getMessage());
            resp.getWriter().append(replaceKeysFromMap(template, replaceMap));
            resp.getWriter().close();
        }
    }

    /**
     * Handles a possible call from Enedis to the redirect_uri. If that is the case Linky will pass the authorization
     * codes via the url and these are processed. In case of an error this is shown to the user. If the user was
     * authorized this is passed on to the handler. Based on all these different outcomes the HTML is generated to
     * inform the user.
     *
     * @param replaceMap a map with key String values that will be mapped in the HTML templates.
     * @param servletBaseURL the servlet base, which should be used as the Linky redirect_uri value
     * @param queryString the query part of the GET request this servlet is processing
     */
    private void handleLinkyRedirect(Map<String, String> replaceMap, String servletBaseURL,
            @Nullable String queryString) throws LinkyException {
        replaceMap.put(KEY_AUTHORIZED_USER, "");
        replaceMap.put(KEY_ERROR, "");
        replaceMap.put(KEY_PAGE_REFRESH, "");
        replaceMap.put(KEY_CB_DISPLAY_CONFIRMATION, "none");
        replaceMap.put(KEY_CB_DISPLAY_ERROR, "none");
        replaceMap.put(KEY_CB_DISPLAY_INSTRUCTION, "true");

        if (queryString != null) {
            final MultiMap<@Nullable String> params = new MultiMap<>();
            UrlEncoded.decodeTo(queryString, params, StandardCharsets.UTF_8.name());
            final String reqCode = params.getString("code");
            final String reqState = params.getString("state");
            final String reqError = params.getString("error");

            replaceMap.put(KEY_PAGE_REFRESH, "");

            // params.isEmpty() ? "" : String.format(HTML_META_REFRESH_CONTENT, servletBaseURL)

            if (!StringUtil.isBlank(reqError)) {
                logger.debug("Linky redirected with an error: {}", reqError);
                replaceMap.put(KEY_CB_DISPLAY_ERROR, "true");
                replaceMap.put(KEY_CB_DISPLAY_CONFIRMATION, "none");
                replaceMap.put(KEY_CB_DISPLAY_INSTRUCTION, "none");
                replaceMap.put(KEY_ERROR, String.format(HTML_ERROR, reqError));
            } else if (!StringUtil.isBlank(reqState)) {
                replaceMap.put(KEY_CB_DISPLAY_ERROR, "none");
                replaceMap.put(KEY_CB_DISPLAY_CONFIRMATION, "true");
                replaceMap.put(KEY_CB_DISPLAY_INSTRUCTION, "none");
                try {
                    replaceMap.put(KEY_AUTHORIZED_USER, String.format(HTML_USER_AUTHORIZED,
                            reqCode + " / " + apiBridgeHandler.authorize(servletBaseURL, reqState, reqCode)));
                } catch (LinkyException e) {
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
