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
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.HexFormat;
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
import org.openhab.binding.smartthings.internal.dto.Event;
import org.openhab.binding.smartthings.internal.dto.LifeCycle;
import org.openhab.binding.smartthings.internal.dto.LifeCycle.Data;
import org.openhab.binding.smartthings.internal.dto.SmartThingsDevice;
import org.openhab.binding.smartthings.internal.dto.SmartThingsLocation;
import org.openhab.binding.smartthings.internal.handler.SmartThingsAccountHandler;
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
import org.owasp.encoder.Encode;
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
    private static final String INIT_PARAM_SERVLET_NAME = "servlet-name";
    private final Logger logger = LoggerFactory.getLogger(SmartThingsServlet.class);
    private final SmartThingsAuthService smartThingsAuthService;
    private final SmartThingsLocalCallbackListener smartThingsLocalCallbackListener;
    private @NonNullByDefault({}) TranslationProvider translationProvider;
    private final String servletPath;

    private final String indexTemplate;
    private final String step1Template;
    private final String errorTemplate;
    private final String confirmTemplate;
    private static final String HTML_ERROR = "<p class='block error'>%s<pre>%s</pre></p>";
    private static final String HTML_CALLBACK_INFO = """
            <section class="info-panel">
                <div class="panel-label">%s</div>
                %s
                <p>%s</p>
                %s
            </section>""";
    private static final String HTML_CALLBACK_LINK = "<a class=\"callback-link\" href=\"%s\">%s</a>";
    private static final String EVENT_CALLBACK_DESCRIPTION = "SmartThings uses this URL for event callbacks.";
    private static final String CALLBACK_REQUIREMENTS = """
            <p class="block warn">SmartThings requires an HTTPS callback URL that is reachable from the internet.
                Verify that the displayed URL starts with https:// and can be reached by SmartThings.</p>""";
    private static final String CALLBACK_NOT_HTTPS = """
            <p class="block warn">This callback URL does not use HTTPS. SmartThings will not register event callbacks
                until the URL uses HTTPS and is reachable from the internet.</p>""";
    private static final String HTML_LOCALHOST_REDIRECT_INFO = """
            <section class="block warn localhost-redirect-info">
                <strong>Remote openHAB installation?</strong>
                <p>During this first authorization SmartThings may return your browser to
                    <code>http://localhost:61973/finish</code>. If openHAB runs on another computer, replace only
                    <code>localhost</code> in the browser address bar with your openHAB host name or IP address.
                    Keep the port, path, and query string unchanged.</p>
            </section>""";
    private static final String STEP_CREATE_APP = "step1";
    private static final String STEP_AUTHORIZE_LOCATION = "step2";
    private static final String MESSAGE_KEY_MISSING_REQ_CODE = "missing-req-code";
    private static final String MESSAGE_KEY_SMARTTHINGS_ERROR = "smartthing-error";

    // Keys present in the index.html
    private static final String KEY_ERROR = "error";
    private static final String KEY_BRIDGE_URI = "bridge.uri";
    private static final String KEY_CALLBACK_INFO = "callbackInfo";
    private static final String KEY_LOCALHOST_REDIRECT_INFO = "localhostRedirectInfo";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_DEVICES_COUNT = "devicesCount";
    private static final String KEY_AUTHORIZATION_URI = "authorizationUri";
    private static final String KEY_AUTHORIZATION_URI_JS = "authorizationUriJs";
    private static final String KEY_ASSET_BASE_URI = "assetBaseUri";

    private static final Pattern MESSAGE_KEY_PATTERN = Pattern.compile("\\$\\{([^\\}]+)\\}");

    private static final String TEMPLATE_PATH = "templates/";

    private final SecureRandom secureRandom = new SecureRandom();
    private Gson gson = new Gson();

    private String callBackURL = "";
    private String oauthRedirectUri = "";
    private String assetBaseUri = "";
    private String createAppState = "";
    private String authorizeLocationState = "";

    protected final SmartThingsBridgeHandler bridgeHandler;
    protected final HttpService httpService;

    public SmartThingsServlet(SmartThingsBridgeHandler bridgeHandler, String servletPath,
            SmartThingsAuthService smartthingsAuthService, TranslationProvider translationProvider,
            HttpService httpService) throws SmartThingsException {
        this.smartThingsAuthService = smartthingsAuthService;
        this.bridgeHandler = bridgeHandler;
        this.httpService = httpService;
        this.servletPath = servletPath;
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
            Dictionary<String, String> servletParams = createServletParams(servletPath);
            logger.trace("registerServlet: {}", servletPath);
            httpService.registerServlet(servletPath, this, servletParams, httpService.createDefaultHttpContext());
            httpService.registerResources(servletPath + SmartThingsBindingConstants.SMARTTHINGS_IMG_ALIAS, "img", null);
        } catch (ServletException | NamespaceException e) {
            logger.warn("Could not start SmartThings servlet service: {}", e.getMessage());
        }
    }

    public void deactivate() {
        smartThingsLocalCallbackListener.stopCallbackListener();
        try {
            httpService.unregister(servletPath);
            httpService.unregister(servletPath + SmartThingsBindingConstants.SMARTTHINGS_IMG_ALIAS);
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

        String requestPath = req.getRequestURI();
        if (requestPath == null) {
            return;
        }
        String requestUrlSt = getFullURL(req);
        String queryString = req.getQueryString();

        String template = handleTemplate(requestPath, queryString, requestUrlSt);
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
    public String handle(String path, @Nullable String query) {
        return handleTemplate(path, query, null);
    }

    private String handleTemplate(String requestPath, @Nullable String queryString,
            @Nullable String externalRequestUrl) {
        SmartThingsOAuthHandler oauthHandler = smartThingsAuthService
                .getSmartThingsOAuthHandler(bridgeHandler.getThing().getUID());

        if (oauthHandler == null) {
            logger.error("Account handler is null in SmartThingsServlet::handleTemplate");
            return "";
        }

        Map<String, String> replaceMap = new HashMap<>();

        String template = "";
        replaceMap.put(KEY_LOCATION, "");
        replaceMap.put(KEY_DEVICES_COUNT, "");
        replaceMap.put(KEY_ERROR, "");
        replaceMap.put(KEY_BRIDGE_URI, "");
        replaceMap.put(KEY_CALLBACK_INFO, "");
        replaceMap.put(KEY_LOCALHOST_REDIRECT_INFO, "");
        replaceMap.put(KEY_AUTHORIZATION_URI, "");
        replaceMap.put(KEY_AUTHORIZATION_URI_JS, "");
        replaceMap.put(KEY_ASSET_BASE_URI, Encode.forHtmlAttribute(assetBaseUri));

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

                replaceMap.put(KEY_ERROR, formatSmartThingsError(reqError));
            } else if (!StringUtil.isBlank(reqState)) {
                try {
                    if (!StringUtil.isBlank(reqCode)) {
                        if (isOAuthCallbackPath(requestPath, servletPath)) {
                            Optional<String> stateStep = getStateStep(reqState);
                            if (stateStep.isEmpty()) {
                                template = errorTemplate;
                                replaceMap.put(KEY_ERROR, String.format(HTML_ERROR, Encode.forHtml("Invalid state"),
                                        Encode.forHtml("The SmartThings authorization state did not match.")));
                            } else if (STEP_CREATE_APP.equals(stateStep.get())) {
                                template = step1Template;

                                logger.debug("Captured SmartThings authorization callback for app setup.");

                                // Finish OAuth flow
                                bridgeHandler.finishOAuth(callBackURL, oauthRedirectUri, reqCode,
                                        bridgeHandler.getThing().getUID().getId());
                                authorizeLocationState = createOAuthState(STEP_AUTHORIZE_LOCATION);
                                String authorizationUri = oauthHandler.formatAuthorizationUrl(oauthRedirectUri,
                                        authorizeLocationState, false);
                                String authorizationUriJs = authorizationUri.replace("\\", "\\\\").replace("\"",
                                        "\\\"");

                                replaceMap.put(KEY_AUTHORIZATION_URI, Encode.forHtml(authorizationUri));
                                replaceMap.put(KEY_AUTHORIZATION_URI_JS, Encode.forJavaScript(authorizationUriJs));
                            } else if (STEP_AUTHORIZE_LOCATION.equals(stateStep.get())) {
                                template = confirmTemplate;

                                smartThingsAuthService.authorize(bridgeHandler.getThing().getUID(), oauthRedirectUri,
                                        reqState, reqCode);
                                smartThingsLocalCallbackListener.stopCallbackListener();

                                SmartThingsApi api = bridgeHandler.getSmartThingsApi();
                                if (api == null) {
                                    logger.warn("API is null on step2 authentication");
                                    replaceMap.put(KEY_LOCATION, "Unknown location");
                                    replaceMap.put(KEY_DEVICES_COUNT, "0");
                                } else {
                                    SmartThingsDevice[] devices = api.getAllDevices();
                                    SmartThingsLocation[] locations = api.getAllLocations();

                                    replaceMap.put(KEY_LOCATION,
                                            Encode.forHtml(locations[0].name + " / " + locations[0].locationId));
                                    replaceMap.put(KEY_DEVICES_COUNT, "" + devices.length);

                                    SmartThingsAccountHandler bridgeAccountHandler = (SmartThingsAccountHandler) bridgeHandler;
                                    bridgeAccountHandler.initLocation(locations[0].locationId);
                                }
                            }
                        }
                    } else {
                        template = errorTemplate;
                        String missingReqCodeError = getTranslation(MESSAGE_KEY_MISSING_REQ_CODE);

                        replaceMap.put(KEY_ERROR, String.format(HTML_ERROR, Encode.forHtml(missingReqCodeError),
                                Encode.forHtml(reqError)));
                    }
                } catch (SmartThingsException e) {
                    template = errorTemplate;
                    logger.debug("Exception during authorization: ", e);

                    replaceMap.put(KEY_ERROR, formatSmartThingsError(SmartThingsException.getRootCauseMessage(e)));
                }
            } else {
                bridgeHandler.registerOAuth(true);
            }
        }
        // index case, first time we go to servlet without any queryString
        else {
            String requestUrl = externalRequestUrl != null ? externalRequestUrl : requestPath;
            String callbackUrl = bridgeHandler.getCallbackUrl();

            // calculate the callback URL
            callBackURL = callbackUrl;
            oauthRedirectUri = getOAuthRedirectUri(requestUrl);
            assetBaseUri = getAssetBaseUrl(requestUrl);

            replaceMap.put(KEY_CALLBACK_INFO, formatCallbackInfo(callBackURL, !callBackURL.isBlank()));
            replaceMap.put(KEY_ASSET_BASE_URI, Encode.forHtmlAttribute(assetBaseUri));

            try {
                String authorizationUri = "";
                if (!smartThingsLocalCallbackListener.startCallbackListener()) {
                    replaceMap.put(KEY_ERROR,
                            String.format(HTML_ERROR, Encode.forHtml("Unable to start OAuth callback listener"),
                                    Encode.forHtml("Port 61973 is not available.")));
                    return replaceKeysFromMap(template, replaceMap);
                }

                SmartThingsAccountHandler bridgeAccountHandler = (SmartThingsAccountHandler) bridgeHandler;
                // if app already create, go directly to step2 to reauthenticate a location
                if (bridgeAccountHandler.appCreated()) {
                    authorizeLocationState = createOAuthState(STEP_AUTHORIZE_LOCATION);
                    authorizationUri = oauthHandler.formatAuthorizationUrl(oauthRedirectUri, authorizeLocationState,
                            false);
                } else {
                    createAppState = createOAuthState(STEP_CREATE_APP);
                    replaceMap.put(KEY_LOCALHOST_REDIRECT_INFO, formatLocalhostRedirectInfo());
                    authorizationUri = oauthHandler.formatAuthorizationUrl(SmartThingsBindingConstants.REDIRECT_URI,
                            createAppState, true);
                }

                // Handle the first redirect to SmartThings when the user clicks the button.
                replaceMap.put(KEY_BRIDGE_URI, Encode.forHtml(authorizationUri));
            } catch (SmartThingsException ex) {
                replaceMap.put(KEY_BRIDGE_URI, "Error during oauth");
            }
        }

        return replaceKeysFromMap(template, replaceMap);
    }

    String formatSmartThingsError(String message) {
        return String.format(HTML_ERROR, Encode.forHtml(getTranslation(MESSAGE_KEY_SMARTTHINGS_ERROR)),
                Encode.forHtml(message));
    }

    static String formatCallbackInfo(String callbackUri) {
        return formatCallbackInfo(callbackUri, false);
    }

    static String formatCallbackInfo(String callbackUri, boolean callbackUrlConfigured) {
        boolean uriAvailable = !callbackUri.isBlank();
        boolean httpsUri = uriAvailable && isHttpsUri(callbackUri);
        if (!uriAvailable || (!callbackUrlConfigured && !httpsUri)) {
            return "";
        }

        String callbackTarget = HTML_CALLBACK_LINK.formatted(Encode.forHtmlAttribute(callbackUri),
                Encode.forHtml(callbackUri));
        String warning = callbackUrlConfigured ? formatCallbackWarning(httpsUri) : "";
        return HTML_CALLBACK_INFO.formatted(Encode.forHtml("Callback URL"), callbackTarget,
                Encode.forHtml(EVENT_CALLBACK_DESCRIPTION), warning);
    }

    private static String formatCallbackWarning(boolean httpsUri) {
        return httpsUri ? CALLBACK_REQUIREMENTS : CALLBACK_NOT_HTTPS;
    }

    static String formatLocalhostRedirectInfo() {
        return HTML_LOCALHOST_REDIRECT_INFO;
    }

    private static boolean isHttpsUri(String uri) {
        try {
            return "https".equalsIgnoreCase(URI.create(uri).getScheme());
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private String getTranslation(String key) {
        Locale locale = Locale.getDefault();
        Bundle bundle = FrameworkUtil.getBundle(getClass());
        String text = translationProvider.getText(bundle, key, null, locale);
        return text != null ? text : key;
    }

    private String getOAuthRedirectUri(String requestUrl) {
        if (requestUrl.startsWith("https://")) {
            return getAssetBaseUrl(requestUrl);
        }

        return SmartThingsBindingConstants.REDIRECT_URI;
    }

    private String getAssetBaseUrl(String requestUrl) {
        int queryIndex = requestUrl.indexOf('?');
        String url = queryIndex >= 0 ? requestUrl.substring(0, queryIndex) : requestUrl;
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private String createOAuthState(String step) {
        byte[] bytes = new byte[16];
        secureRandom.nextBytes(bytes);
        return step + ":" + HexFormat.of().formatHex(bytes);
    }

    private Optional<String> getStateStep(String state) {
        if (state.equals(createAppState)) {
            return Optional.of(STEP_CREATE_APP);
        }
        if (state.equals(authorizeLocationState)) {
            return Optional.of(STEP_AUTHORIZE_LOCATION);
        }
        return Optional.empty();
    }

    public static String getServletPath(String bridgeId) {
        return PATH + "/" + bridgeId;
    }

    static Dictionary<String, String> createServletParams(String servletPath) {
        Dictionary<String, String> servletParams = new Hashtable<>();
        servletParams.put(INIT_PARAM_SERVLET_NAME, getServletName(servletPath));
        return servletParams;
    }

    private static String getServletName(String servletPath) {
        String normalizedPath = servletPath.startsWith("/") ? servletPath.substring(1) : servletPath;
        return SmartThingsServlet.class.getName() + "." + normalizedPath.replace('/', '.');
    }

    static boolean isOAuthCallbackPath(String requestPath, String servletPath) {
        return "/finish".equals(requestPath) || servletPath.equals(requestPath)
                || (servletPath + "/").equals(requestPath);
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

        logger.trace("SmartThingsServlet:service");
        String path = req.getRequestURI();

        if (path == null) {
            return;
        }

        if (servletPath.equals(path) || (servletPath + "/").equals(path)) {
            super.service(req, resp);
        } else if ((servletPath + "/cb").equals(path)) {
            BufferedReader rdr = new BufferedReader(req.getReader());
            String s = rdr.lines().collect(Collectors.joining());

            @Nullable
            LifeCycle resultObj = gson.fromJson(s, LifeCycle.class);

            if (resultObj == null) {
                String responseSt = "Callback empty";
                resp.getWriter().print(responseSt);
                return;
            }

            String eventType = resultObj.getEventType();

            logger.trace("Callback called with eventType: {}", eventType);

            // ========================================
            // Event from webhook CB
            // ========================================

            if (eventType.equals(SmartThingsBindingConstants.EVENT_TYPE_EVENT)) {
                handleLifecycleEvents(resultObj.eventData);
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

    private void handleLifecycleEvents(@Nullable Data data) {
        if (data == null || data.events == null || data.events.length == 0) {
            logger.debug("SmartThings callback did not contain any events");
            return;
        }

        Bridge bridge = bridgeHandler.getThing();
        List<Thing> things = bridge.getThings();

        for (Event event : data.events) {
            if (event == null || event.deviceEvent == null) {
                continue;
            }

            String deviceId = event.deviceEvent.deviceId;
            String componentId = event.deviceEvent.componentId;
            String capa = event.deviceEvent.capability;
            String attr = event.deviceEvent.attribute;
            Object value = event.deviceEvent.value;

            logger.debug("SmartThings event: deviceId: {}, componentId: {}, capability: {}, attribute: {}, value: {}",
                    deviceId, componentId, capa, attr, value);

            if (deviceId == null) {
                continue;
            }

            Optional<Thing> theThingOpt = things.stream()
                    .filter(thing -> SmartThingsDeviceIdResolver.matches(thing, deviceId)).findFirst();
            if (theThingOpt.isPresent()) {
                logger.info("EVENT: {} {} {} {} {}", deviceId, componentId, capa, attr, value);
                Thing theThing = theThingOpt.get();

                ThingHandler handler = theThing.getHandler();
                SmartThingsThingHandler smartThingsHandler = (SmartThingsThingHandler) handler;
                if (smartThingsHandler != null) {
                    smartThingsHandler.refreshDevice(smartThingsHandler.getSmartThingsDeviceType(), componentId, capa,
                            attr, value);
                }
            } else {
                logger.info("Can't find device for EVENT: {} {} {} {} {}", deviceId, componentId, capa, attr, value);
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
                        "Cannot find '%s' - failed to initialize SmartThings servlet".formatted(templateName));
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
