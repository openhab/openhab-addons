/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.UrlEncoded;
import org.openhab.binding.smartthings.internal.api.SmartThingsApi;
import org.openhab.binding.smartthings.internal.api.SmartThingsNetworkConnector;
import org.openhab.binding.smartthings.internal.dto.LifeCycle;
import org.openhab.binding.smartthings.internal.dto.LifeCycle.Data;
import org.openhab.binding.smartthings.internal.dto.SmartThingsDevice;
import org.openhab.binding.smartthings.internal.dto.SmartThingsLocation;
import org.openhab.binding.smartthings.internal.handler.SmartThingsBridgeHandler;
import org.openhab.binding.smartthings.internal.handler.SmartThingsThingHandler;
import org.openhab.binding.smartthings.internal.type.SmartThingsException;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link SmartThingsServlet} manages the authorization with the SmartThings Web API. The servlet implements the
 * Authorization Code flow and saves the resulting refreshToken with the bridge.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class SmartThingsServlet extends HttpServlet
        implements SmartThingsLocalCallbackListener.ResponseHandlerListener {

    private static final long serialVersionUID = -4719613645562518231L;

    private static final String PATH = "/smartthings";

    private static final String CONTENT_TYPE = "text/html;charset=UTF-8";
    private final Logger logger = LoggerFactory.getLogger(SmartThingsServlet.class);
    private final SmartThingsAuthService smartThingsAuthService;
    private final SmartThingsLocalCallbackListener smartThingsLocalCallbackListener;
    private @NonNullByDefault({}) TranslationProvider translationProvider;

    private final String indexTemplate;
    private final String step1Template;
    private final String errorTemplate;
    private final String confirmTemplate;
    private static final String HTML_ERROR = "<p class='block error'>%s<pre>%s</pre></p>";
    private static final String HTML_WARNING = "<p class='block warn'>%s</p>";

    // Keys present in the index.html
    private static final String KEY_ERROR = "error";
    private static final String KEY_BRIDGE_URI = "bridge.uri";
    private static final String KEY_CALLBACK_URI = "callBackUri";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_DEVICES_COUNT = "devicesCount";
    private static final String KEY_AUTHORIZATION_URI = "authorizationUri";
    private static final String KEY_AUTHORIZATION_URI_JS = "authorizationUriJs";

    private static final Pattern MESSAGE_KEY_PATTERN = Pattern.compile("\\$\\{([^\\}]+)\\}");

    private static final String TEMPLATE_PATH = "templates/";

    private Gson gson = new Gson();

    private String servletBaseURL = "";

    protected final SmartThingsBridgeHandler bridgeHandler;
    protected final HttpService httpService;

    public SmartThingsServlet(SmartThingsBridgeHandler bridgeHandler, SmartThingsAuthService smartthingsAuthService,
            TranslationProvider translationProvider, HttpService httpService) throws SmartThingsException {
        this.smartThingsAuthService = smartthingsAuthService;
        this.bridgeHandler = bridgeHandler;
        this.httpService = httpService;
        this.smartThingsLocalCallbackListener = new SmartThingsLocalCallbackListener();
        this.smartThingsLocalCallbackListener.setListener(this);
        this.translationProvider = translationProvider;

        try {
            indexTemplate = readTemplate("index-oauth.html");
            step1Template = readTemplate("step1.html");
            errorTemplate = readTemplate("error.html");
            confirmTemplate = readTemplate("confirmation.html");
        } catch (IOException e) {
            throw new SmartThingsException("Unable to initialize auth servlet", e);
        }
    }

    public void activate() {
        try {
            Dictionary<String, String> servletParams = new Hashtable<String, String>();
            logger.trace("registerServlet: {}", PATH);
            httpService.registerServlet(PATH, this, servletParams, httpService.createDefaultHttpContext());
            httpService.registerResources(PATH + SmartThingsBindingConstants.SMARTTHINGS_IMG_ALIAS, "img", null);
            smartThingsLocalCallbackListener.startCallbackListener();
        } catch (ServletException | NamespaceException e) {
            logger.warn("Could not start SmartThings servlet service: {}", e.getMessage());
        }
    }

    public void deactivate() {
        smartThingsLocalCallbackListener.stopCallbackListener();
        try {
            httpService.unregister(PATH);
            httpService.unregister(PATH + "/img");
        } catch (IllegalArgumentException e) {
            logger.warn("Could not stop SmartThings servlet service: {}", e.getMessage());
        }
    }

    @Override
    public void init(@Nullable ServletConfig servletConfig) throws ServletException {
        logger.trace("SmartThingsServlet:init");
    }

    @Override
    protected void doGet(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp)
            throws ServletException, IOException {
        if (req == null) {
            return;
        }
        if (resp == null) {
            return;
        }

        logger.debug("SmartThings auth callback servlet received GET request {}.", req.getRequestURI());

        String requestUrlSt = getFullURL(req);
        String queryString = req.getQueryString();

        String template = handleTemplate(requestUrlSt, queryString);
        resp.setContentType(CONTENT_TYPE);
        resp.getWriter().append(template);
        resp.getWriter().close();
    }

    public static String getFullURL(HttpServletRequest req) {

        String scheme = extractFirst(req.getHeader("X-Forwarded-Proto"));
        if (scheme == null) {
            scheme = req.getScheme();
        }

        String hostHeader = extractFirst(req.getHeader("X-Forwarded-Host"));
        String host;
        int port = -1;

        if (hostHeader != null) {
            // peut contenir host:port
            if (hostHeader.contains(":")) {
                String[] parts = hostHeader.split(":", 2);
                host = parts[0];
                try {
                    port = Integer.parseInt(parts[1]);
                } catch (NumberFormatException ignored) {
                }
            } else {
                host = hostHeader;
            }
        } else {
            host = req.getServerName();
            port = req.getServerPort();
        }

        // X-Forwarded-Port prioritaire si présent
        String forwardedPort = extractFirst(req.getHeader("X-Forwarded-Port"));
        if (forwardedPort != null) {
            try {
                port = Integer.parseInt(forwardedPort);
            } catch (NumberFormatException ignored) {
            }
        }

        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(host);

        if (isNonStandardPort(scheme, port)) {
            url.append(":").append(port);
        }

        url.append(req.getRequestURI());

        if (req.getQueryString() != null) {
            url.append("?").append(req.getQueryString());
        }

        return url.toString();
    }

    private static @Nullable String extractFirst(@Nullable String header) {
        if (header == null || header.isEmpty()) {
            return null;
        }
        return header.split(",")[0].trim();
    }

    private static boolean isNonStandardPort(String scheme, int port) {
        if (port <= 0) {
            return false;
        }
        return !("http".equalsIgnoreCase(scheme) && port == 80) && !("https".equalsIgnoreCase(scheme) && port == 443);
    }

    @Override
    public String handle(String path, String query) {
        return handleTemplate(path, query);
    }

    private String handleTemplate(String requestUrl, @Nullable String queryString) {
        SmartThingsAccountHandler accountHandler = smartThingsAuthService.getSmartThingsAccountHandler();

        if (accountHandler == null) {
            logger.error("accountHandler==nul in SmartThingsServlet::handleTemplate");
            return "";
        }

        Map<String, String> replaceMap = new HashMap<>();

        String template = "";
        replaceMap.put(KEY_LOCATION, "");
        replaceMap.put(KEY_DEVICES_COUNT, "");
        replaceMap.put(KEY_ERROR, "");

        template = indexTemplate;

        if (queryString != null) {
            final MultiMap<@Nullable String> params = new MultiMap<>();
            UrlEncoded.decodeTo(queryString, params, StandardCharsets.UTF_8.name());
            final String reqCode = params.getString("code");
            final String reqState = params.getString("state");
            final String reqError = params.getString("error");

            if (!StringUtil.isBlank(reqError)) {
                template = confirmTemplate;
                logger.debug("SmartThings redirected with an error: {}", reqError);

                Locale locale = Locale.getDefault();
                Bundle bundle = FrameworkUtil.getBundle(getClass());
                String smartthingsError = translationProvider.getText(bundle, "redirectUriNotHttps", null, locale);

                replaceMap.put(KEY_ERROR, String.format(HTML_ERROR, smartthingsError, reqError));
            } else if (!StringUtil.isBlank(reqState)) {
                try {
                    if (!StringUtil.isBlank(reqCode)) {
                        if ("/finish".equals(requestUrl)) {
                            if ("step1".equals(reqState)) {
                                template = step1Template;

                                logger.debug("Captured auth code: {}", reqCode);

                                // Finish OAuth flow
                                bridgeHandler.finishOAuth(servletBaseURL, reqCode,
                                        bridgeHandler.getThing().getUID().getId());
                                String authorizationUri = accountHandler.formatAuthorizationUrl(
                                        SmartThingsBindingConstants.REDIRECT_URI, "step2", false);
                                String authorizationUriJs = authorizationUri.replace("\\", "\\\\").replace("\"",
                                        "\\\"");

                                replaceMap.put(KEY_AUTHORIZATION_URI, authorizationUri);
                                replaceMap.put(KEY_AUTHORIZATION_URI_JS, authorizationUriJs);

                            } else if ("step2".equals(reqState)) {
                                template = confirmTemplate;

                                smartThingsAuthService.authorize(SmartThingsBindingConstants.REDIRECT_URI, reqState,
                                        reqCode);

                                SmartThingsApi api = bridgeHandler.getSmartThingsApi();
                                if (api == null) {
                                    logger.warn("Api is null on step2 authentification");
                                    replaceMap.put(KEY_LOCATION, "Unknow location");
                                    replaceMap.put(KEY_DEVICES_COUNT, "0");

                                } else {
                                    SmartThingsDevice[] devices = api.getAllDevices();
                                    SmartThingsLocation[] locations = api.getAllLocations();

                                    replaceMap.put(KEY_LOCATION, locations[0].name + " / " + locations[0].locationId);
                                    replaceMap.put(KEY_DEVICES_COUNT, "" + devices.length);
                                }
                            }
                        }
                    } else {
                        // @todo: handle errors
                    }
                } catch (SmartThingsException e) {
                    template = errorTemplate;
                    logger.debug("Exception during authorizaton: ", e);
                    Locale locale = Locale.getDefault();
                    Bundle bundle = FrameworkUtil.getBundle(getClass());
                    String smartthingsError = translationProvider.getText(bundle, "redirectUriNotHttps", null, locale);

                    replaceMap.put(KEY_ERROR,
                            String.format(HTML_ERROR, smartthingsError, SmartThingsException.getRootCauseMessage(e)));
                }
            } else {
                bridgeHandler.registerOAuth(true);
            }
        }
        // index case, first time we go to servlet without any queryString
        else {
            // calculate the callback URL
            servletBaseURL = requestUrl;

            // Display it in page rendering for user confirmation
            replaceMap.put(KEY_CALLBACK_URI, servletBaseURL);

            if (!servletBaseURL.startsWith("https://")) {
                Locale locale = Locale.getDefault();
                Bundle bundle = FrameworkUtil.getBundle(getClass());
                String redirectUriError = translationProvider.getText(bundle, "redirectUriNotHttps", null, locale);
                replaceMap.put(KEY_ERROR, String.format(HTML_WARNING, redirectUriError));
            }

            try {
                String authorizationUri = accountHandler
                        .formatAuthorizationUrl(SmartThingsBindingConstants.REDIRECT_URI, "step1", true);

                // handle first redirection to Smartthings when user click button
                replaceMap.put(KEY_BRIDGE_URI, authorizationUri);
            } catch (SmartThingsException ex) {
                replaceMap.put(KEY_BRIDGE_URI, "Error during oauth");
            }

        }

        return replaceKeysFromMap(template, replaceMap);
    }

    @Override
    protected void service(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp)
            throws ServletException, IOException {
        if (req == null) {
            logger.debug("SmartThingsServlet.service unexpectedly received a null request. Request not processed");
            return;
        }
        if (resp == null) {
            return;
        }

        logger.info("SmartThingsServlet:service");
        String path = req.getRequestURI();

        if (path == null) {
            return;
        }

        if ("/smartthings".equals(path) || "/smartthings/".equals(path)) {
            super.service(req, resp);
        } else if ("/smartthings/cb".equals(path)) {
            BufferedReader rdr = new BufferedReader(req.getReader());
            String s = rdr.lines().collect(Collectors.joining());

            LifeCycle resultObj = gson.fromJson(s, LifeCycle.class);

            if (resultObj == null) {
                return;
            }

            String eventType = resultObj.getEventType();

            logger.trace("Callback called with eventType: {}", eventType);

            // ========================================
            // Event from webhook CB
            // ========================================

            if (eventType.equals(SmartThingsBindingConstants.EVENT_TYPE_EVENT)) {
                Data data = resultObj.eventData;
                String deviceId = data.events[0].deviceEvent.deviceId;
                String componentId = data.events[0].deviceEvent.componentId;
                String capa = data.events[0].deviceEvent.capability;
                String attr = data.events[0].deviceEvent.attribute;
                Object value = data.events[0].deviceEvent.value;

                logger.error(
                        "Callback called with lifeCycle (Event): deviceId : {}, componentId: {}, capa: {}, attr: {}, value:{} ",
                        deviceId, componentId, capa, attr, value);

                Bridge bridge = bridgeHandler.getThing();
                List<Thing> things = bridge.getThings();

                Optional<Thing> theThingOpt = things.stream().filter(x -> x.getProperties().containsValue(deviceId))
                        .findFirst();
                if (theThingOpt.isPresent()) {
                    Thing theThing = theThingOpt.get();

                    ThingHandler handler = theThing.getHandler();
                    SmartThingsThingHandler smarthingsHandler = (SmartThingsThingHandler) handler;
                    if (smarthingsHandler != null) {
                        smarthingsHandler.refreshDevice(theThing.getThingTypeUID().getId(), componentId, capa, attr,
                                value);
                    }
                }

                logger.info("EVENT: {} {} {} {} {}", deviceId, componentId, capa, attr, value);
            } else if (eventType.equals(SmartThingsBindingConstants.EVENT_TYPE_CONFIRMATION)) {
                String confirmUrl = resultObj.confirmationData.confirmationUrl();

                String responseSt = "{";
                responseSt = responseSt + "\"targetUrl\": \"" + confirmUrl + "\"";
                responseSt = responseSt + "}";
                resp.getWriter().print(responseSt);

                try {
                    Thread.sleep(2000);
                    SmartThingsNetworkConnector networkConnector = bridgeHandler.getNetworkConnector();
                    networkConnector.doBasicRequest(String.class, confirmUrl, null, "", "", HttpMethod.GET);
                } catch (Exception ex) {
                    logger.error("error during confirmation {}", confirmUrl);
                }

                logger.trace("CONFIRMATION {}", confirmUrl);
            }

        }
        logger.trace("SmartThings servlet returning.");
    }

    /**
     * Replaces all keys from the map found in the template with values from the map. If the key is not found the key
     * will be kept in the template.
     *
     * @param template template to replace keys with values
     * @param map map with key value pairs to replace in the template
     * @return a template with keys replaced
     */
    protected String replaceKeysFromMap(String template, Map<String, String> map) {
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

    /**
     * Reads a template from file and returns the content as String.
     *
     * @param templateName name of the template file to read
     * @return The content of the template file
     * @throws IOException thrown when an HTML template could not be read
     */
    protected String readTemplate(String templateName) throws IOException {
        try {
            final URL url = bridgeHandler.getBundleContext().getBundle().getEntry(TEMPLATE_PATH + templateName);

            if (url == null) {
                throw new FileNotFoundException(
                        String.format("Cannot find {}' - failed to initialize Linky servlet".formatted(templateName)));
            } else {
                try (InputStream inputStream = url.openStream()) {
                    return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
        } catch (IllegalStateException e) {
            // ignore - this happens when the bundle has already been deactivated
            return "";
        }
    }
}
