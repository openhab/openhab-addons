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
package org.openhab.binding.amazonechocontrol.internal;

import static org.eclipse.jetty.util.StringUtil.isNotBlank;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.*;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlServlet.SERVLET_PATH;
import static org.openhab.binding.amazonechocontrol.internal.util.Util.findIn;
import static org.unbescape.html.HtmlEscape.escapeHtml4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.util.introspection.UberspectImpl;
import org.apache.velocity.util.introspection.UberspectPublicFields;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.openhab.binding.amazonechocontrol.internal.connection.Connection;
import org.openhab.binding.amazonechocontrol.internal.dto.DeviceTO;
import org.openhab.binding.amazonechocontrol.internal.dto.NotificationSoundTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.BluetoothStateTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.MusicProviderTO;
import org.openhab.binding.amazonechocontrol.internal.handler.AccountHandler;
import org.openhab.binding.amazonechocontrol.internal.util.HttpRequestBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletName;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AmazonEchoControlServlet} allows to log in to Amazon accounts using a proxy and shows information about
 * configured accounts and devices
 *
 * @author Michael Geramb - Initial Contribution
 * @author Jan N. Klug - Refactored to whiteboard, merged both servlets, use Velocity templates
 */
@Component(service = Servlet.class, immediate = true)
@HttpWhiteboardServletName(SERVLET_PATH)
@HttpWhiteboardServletPattern({ SERVLET_PATH, SERVLET_PATH + "/*" })
@NonNullByDefault
public class AmazonEchoControlServlet extends HttpServlet {
    public static final String SERVLET_PATH = "/" + BINDING_ID;
    private static final long serialVersionUID = -9158865063627039237L;
    private static final String PROXY_URI_PART = "/PROXY/";
    private static final String MAP_LANDING_PATH = "/ap/maplanding";
    private static final Pattern LINK_TARGET = Pattern.compile("(?<=\\s)(href|action|formaction)=\"([^\"]*)\"",
            Pattern.CASE_INSENSITIVE);

    private final Logger logger = LoggerFactory.getLogger(AmazonEchoControlServlet.class);
    private final VelocityEngine velocityEngine = new VelocityEngine();

    private final AmazonEchoControlHandlerFactory handlerFactory;

    @Activate
    public AmazonEchoControlServlet(@Reference AmazonEchoControlHandlerFactory handlerFactory) {
        this.handlerFactory = handlerFactory;

        velocityEngine.setProperty("introspector.uberspect.class",
                UberspectImpl.class.getName() + ", " + UberspectPublicFields.class.getName());
        velocityEngine.init();
    }

    private @Nullable AccountHandler getAccountHandler(String accountUid) {
        ThingUID thingUID = new ThingUID(THING_TYPE_ACCOUNT, URLDecoder.decode(accountUid, StandardCharsets.UTF_8));
        return handlerFactory.getAccountHandlers().stream().filter(h -> thingUID.equals(h.getThing().getUID()))
                .findAny().orElse(null);
    }

    @Override
    protected void doPut(@NonNullByDefault({}) HttpServletRequest req, @NonNullByDefault({}) HttpServletResponse resp)
            throws IOException {
        preProcess(HttpMethod.PUT, req, resp);
    }

    @Override
    protected void doDelete(@NonNullByDefault({}) HttpServletRequest req,
            @NonNullByDefault({}) HttpServletResponse resp) throws IOException {
        preProcess(HttpMethod.DELETE, req, resp);
    }

    @Override
    protected void doPost(@NonNullByDefault({}) HttpServletRequest req, @NonNullByDefault({}) HttpServletResponse resp)
            throws IOException {
        preProcess(HttpMethod.POST, req, resp);
    }

    @Override
    protected void doGet(@NonNullByDefault({}) HttpServletRequest req, @NonNullByDefault({}) HttpServletResponse resp)
            throws IOException {
        preProcess(HttpMethod.GET, req, resp);
    }

    private void preProcess(HttpMethod method, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ServletUri servletUri = ServletUri.fromFullUri(req.getRequestURI());
        if (servletUri == null) {
            returnError(resp, null, "Could not parse URI for " + method + "/" + req.getRequestURI());
            return;
        }
        if ("static".equals(servletUri.account())) {
            serveStatic(resp, servletUri.request());
        } else if (!servletUri.account().isBlank()) {
            switch (method) {
                case DELETE, POST, PUT -> doAccountDeletePostPut(method, servletUri, req, resp);
                case GET -> doAccountGet(servletUri, req, resp);
                default -> returnError(resp, servletUri, "Can't handle " + method + " request for accounts.");
            }
        } else {
            if (HttpMethod.GET.equals(method)) {
                doBindingGet(resp);
            } else {
                returnError(resp, servletUri, "Can't handle " + method + " requests for the binding.");
            }
        }
    }

    private void doBindingGet(HttpServletResponse resp) throws IOException {
        VelocityContext ctx = new VelocityContext();
        ctx.put("servletPath", SERVLET_PATH);
        ctx.put("accounts", handlerFactory.getAccountHandlers().stream()
                .sorted(Comparator.comparing(h -> h.getThing().getUID().toString())).toList());

        StringWriter stringWriter = evaluateTemplate("WEB-INF/binding.vm", ctx);

        resp.addHeader(HttpHeader.CONTENT_TYPE.asString(), MimeTypes.Type.TEXT_HTML_UTF_8.asString());
        resp.getWriter().write(stringWriter.toString());
    }

    private void doAccountDeletePostPut(HttpMethod method, ServletUri uriParts, HttpServletRequest req,
            HttpServletResponse resp) throws IOException {
        String uri = uriParts.request();
        String queryString = req.getQueryString();
        if (queryString != null && !queryString.isEmpty()) {
            uri += "?" + queryString;
        }

        AccountHandler accountHandler = getAccountHandler(uriParts.account());
        if (accountHandler == null) {
            returnError(resp, uriParts, "Could not find account handler");
            return;
        }
        Connection connection = accountHandler.getConnection();
        if (uri.startsWith(PROXY_URI_PART)) {
            // handle proxy request
            String proxyUrl = connection.getAlexaServer() + "/" + uri.substring(PROXY_URI_PART.length());

            Object postData = null;
            if (HttpMethod.PUT.equals(method) || HttpMethod.POST.equals(method)) {
                postData = req.getReader().lines().collect(Collectors.joining());
            }

            this.handleProxyRequest(accountHandler, connection, resp, uriParts, method, proxyUrl, Map.of(), postData,
                    postData != null, null);
            return;
        }

        // handle post of login page
        if (connection.isLoggedIn()) {
            returnError(resp, uriParts, "Connection not in initialize mode.");
            return;
        }

        resp.addHeader(HttpHeader.CONTENT_TYPE.asString(), MimeTypes.Type.TEXT_HTML_UTF_8.asString());

        Map<String, String[]> map = req.getParameterMap();
        StringBuilder postDataBuilder = new StringBuilder();
        for (String name : map.keySet()) {
            if (!postDataBuilder.isEmpty()) {
                postDataBuilder.append('&');
            }

            postDataBuilder.append(name);
            postDataBuilder.append('=');
            String value = "";
            if ("failedSignInCount".equals(name)) {
                value = "ape:AA==";
            } else {
                String[] strings = map.get(name);
                if (strings != null && strings.length > 0) {
                    value = LoginDialogPage.unrewrite(strings[0], uriParts);
                }
            }
            postDataBuilder.append(URLEncoder.encode(value, StandardCharsets.UTF_8));
        }

        LoginDialogPage page = LoginDialogPage.fromRequest(LoginDialogPage.unrewrite(uri, uriParts),
                dialogHosts(connection.getRetailUrl()));
        if (page == null) {
            returnError(resp, uriParts, "Refusing to post to '" + uri + "'");
            return;
        }
        String postData = postDataBuilder.toString();
        handleProxyRequest(accountHandler, connection, resp, uriParts, method, page.url(),
                browserHeaders(req, page, method), postData, false, page);
    }

    /** Amazon rejects the sign-in post unless it carries the identity of the browser the page was served to */
    static Map<String, String> browserHeaders(HttpServletRequest req) {
        Map<String, String> headers = new HashMap<>();
        for (HttpHeader header : List.of(HttpHeader.USER_AGENT, HttpHeader.ACCEPT_LANGUAGE)) {
            String value = req.getHeader(header.asString());
            if (value != null && !value.isBlank()) {
                headers.put(header.asString(), value);
            }
        }
        return headers;
    }

    static Map<String, String> browserHeaders(HttpServletRequest req, LoginDialogPage page, HttpMethod method) {
        Map<String, String> headers = browserHeaders(req);
        headers.put(HttpHeader.REFERER.asString(), page.origin());
        if (!HttpMethod.GET.equals(method)) {
            headers.put(HttpHeader.ORIGIN.asString(), page.origin());
        }
        return headers;
    }

    private void doAccountGet(ServletUri uriParts, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String uri = uriParts.request();
        String queryString = req.getQueryString();
        if (isNotBlank(queryString)) {
            uri += "?" + queryString;
        }
        try {
            AccountHandler accountHandler = getAccountHandler(uriParts.account());
            if (accountHandler == null) {
                returnError(resp, uriParts, "Could not find account handler.");
                return;
            }

            Connection connection = accountHandler.getConnection();
            if (uri.startsWith(LoginDialogPage.FORWARD_URI_PART)) {
                if (connection.isLoggedIn()) {
                    returnError(resp, uriParts, "Connection not in initialize mode.");
                    return;
                }
                LoginDialogPage page = LoginDialogPage.fromRequest(LoginDialogPage.unrewrite(uri, uriParts),
                        dialogHosts(connection.getRetailUrl()));
                if (page == null) {
                    returnError(resp, uriParts, "Refusing to forward to '" + uri + "'");
                    return;
                }
                this.handleProxyRequest(accountHandler, connection, resp, uriParts, HttpMethod.GET, page.url(),
                        browserHeaders(req, page, HttpMethod.GET), null, false, page);
                return;
            }

            if (uri.startsWith(PROXY_URI_PART)) {
                // handle proxy request
                String proxyUrl = connection.getAlexaServer() + "/" + uri.substring(PROXY_URI_PART.length());

                this.handleProxyRequest(accountHandler, connection, resp, uriParts, HttpMethod.GET, proxyUrl, Map.of(),
                        null, false, null);
                return;
            }

            if (connection.verifyLogin()) {
                // handle commands
                if ("/logout".equals(uriParts.request()) || "/logout/".equals(uriParts.request())) {
                    accountHandler.resetConnection(false);
                    resp.sendRedirect(uriParts.buildFor("/"));
                    return;
                }
                // handle commands
                if ("/newdevice".equals(uriParts.request()) || "/newdevice/".equals(uriParts.request())) {
                    accountHandler.resetConnection(true);
                    resp.sendRedirect(uriParts.buildFor("/"));
                    return;
                }
                if ("/ids".equals(uriParts.request()) || "/ids/".equals(uriParts.request())) {
                    String serialNumber = getQueryMap(queryString).get("serialNumber");
                    DeviceTO device = accountHandler.findDevice(serialNumber);
                    if (device != null) {
                        Thing thing = accountHandler.getThingBySerialNumber(device.serialNumber);
                        if (thing == null) {
                            returnError(resp, uriParts, "No thing defined for " + serialNumber);
                        } else {
                            createDeviceDetailsResponse(resp, uriParts, connection, device, thing);
                        }
                        return;
                    }
                }
                // return hint that everything is ok
                createAccountPage(resp, uriParts, accountHandler, connection);
                return;
            }

            if (!uriParts.request().isBlank()) {
                resp.sendRedirect(SERVLET_PATH + "/" + uriParts.account());
                return;
            }

            String html = connection.getLoginPage(browserHeaders(req));
            LoginDialogPage signInPage = LoginDialogPage.signIn();
            returnHtml(resp, uriParts, html, signInPage.host(), signInPage.linkBase(uriParts));
        } catch (ConnectionException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to load the sign-in page: {}", e.getMessage(), e);
            } else {
                logger.warn("Failed to load the sign-in page: {}", e.getMessage());
            }
        }
    }

    private void createAccountPage(HttpServletResponse resp, ServletUri uriParts, AccountHandler accountHandler,
            Connection connection) throws IOException {
        VelocityContext ctx = new VelocityContext();
        ctx.put("servletPath", SERVLET_PATH);
        ctx.put("accountPath", uriParts.buildFor("/"));
        ctx.put("account", accountHandler);
        ctx.put("connection", connection);
        ctx.put("devices", accountHandler.getLastKnownDevices().stream()
                .sorted(Comparator.comparing(d -> d.serialNumber)).toList());
        ctx.put("DEVICE_TYPES", DEVICE_TYPES);

        StringWriter stringWriter = evaluateTemplate("WEB-INF/account-detail.vm", ctx);
        resp.addHeader(HttpHeader.CONTENT_TYPE.asString(), MimeTypes.Type.TEXT_HTML_UTF_8.asString());
        resp.getWriter().write(stringWriter.toString());
    }

    private void createDeviceDetailsResponse(HttpServletResponse resp, ServletUri uriParts, Connection connection,
            DeviceTO device, Thing thing) throws IOException {
        Map<String, List<ChannelOption>> channels = new HashMap<>();
        List<ChannelOption> musicProviders = connection.getMusicProviders().stream().filter(this::isValidMusicProvider)
                .map(p -> new ChannelOption(p.id, p.displayName)).sorted(Comparator.comparing(o -> o.value)).toList();
        channels.put(CHANNEL_MUSIC_PROVIDER_ID, musicProviders);

        List<ChannelOption> alarmSounds = connection.getNotificationSounds(device).stream()
                .filter(this::isValidAlarmSound).map(p -> new ChannelOption(p.providerId + ":" + p.id, p.displayName))
                .sorted(Comparator.comparing(o -> o.value)).toList();
        channels.put(CHANNEL_PLAY_ALARM_SOUND, alarmSounds);

        List<BluetoothStateTO> states = connection.getBluetoothConnectionStates();
        List<ChannelOption> pairedDevices = findIn(states, k -> k.deviceSerialNumber, device.serialNumber)
                .map(state -> state.pairedDeviceList)
                .map(list -> list.stream().map(p -> new ChannelOption(p.address, p.friendlyName))
                        .sorted(Comparator.comparing(o -> o.value)).toList())
                .orElse(List.of());
        channels.put(CHANNEL_BLUETOOTH_MAC, Objects.requireNonNull(pairedDevices));

        VelocityContext ctx = new VelocityContext();
        ctx.put("thing", thing);
        ctx.put("servletPath", SERVLET_PATH);
        ctx.put("accountPath", uriParts.buildFor("/"));
        ctx.put("channels", channels);
        ctx.put("capabilities", device.capabilities.stream().sorted().toList());

        StringWriter stringWriter = evaluateTemplate("WEB-INF/device-detail.vm", ctx);
        resp.addHeader(HttpHeader.CONTENT_TYPE.asString(), MimeTypes.Type.TEXT_HTML_UTF_8.asString());
        resp.getWriter().write(stringWriter.toString());
    }

    private boolean isValidMusicProvider(MusicProviderTO provider) {
        return provider.supportedProperties.contains("Alexa.Music.PlaySearchPhrase")
                && "AVAILABLE".equals(provider.availability) && isNotBlank(provider.displayName);
    }

    private boolean isValidAlarmSound(NotificationSoundTO sound) {
        return sound.folder == null && sound.providerId != null && sound.id != null && sound.displayName != null;
    }

    private void handleProxyRequest(AccountHandler accountHandler, Connection connection, HttpServletResponse resp,
            ServletUri uriParts, HttpMethod method, String url, Map<String, String> headers, @Nullable Object postData,
            boolean isJson, @Nullable LoginDialogPage page) throws IOException {
        String retailUrl = connection.getRetailUrl();
        List<String> dialogHosts = dialogHosts(retailUrl);
        String host = page == null ? dialogHosts.get(dialogHosts.size() - 1) : page.host();
        try {
            HttpRequestBuilder.HttpResponse response = connection.getRequestBuilder().builder(method, url)
                    .withContent(postData).withJson(isJson).withHeaders(headers).retry(false).redirect(false)
                    .syncSend();
            if (HttpStatus.isRedirection(response.statusCode())) {
                String location = LoginDialogPage.unrewrite(response.headers().get("location"), uriParts);
                LoginDialogPage target = page == null ? null
                        : LoginDialogPage.fromLocation(location, page, dialogHosts);
                if (target != null && target.pathAndQuery().startsWith(MAP_LANDING_PATH)) {
                    try {
                        URI oAuthRedirectUri = new URI(location);
                        String accessToken = getQueryMap(oAuthRedirectUri.getQuery()).get("openid.oa2.access_token");
                        if (accessToken == null) {
                            returnError(resp, uriParts,
                                    "Login to '" + host + "' failed: Could not extract accessToken.");
                        } else if (connection.registerConnectionAsApp(accessToken)) {
                            accountHandler.setConnection(connection);
                            resp.sendRedirect(SERVLET_PATH + "/" + uriParts.account());
                            // createAccountPage(resp, uriParts, accountHandler, connection);
                        } else {
                            returnError(resp, uriParts, "Login to '" + host + "' failed: Could not register as app.");
                        }
                        return;
                    } catch (URISyntaxException e) {
                        returnError(resp, uriParts, "Login to '" + host + "' failed: " + e.getLocalizedMessage());
                        accountHandler.resetConnection(false);
                        return;
                    }
                }

                String newLocation = page == null
                        ? proxyRedirectTarget(location, retailUrl, connection.isLoggedIn(), uriParts)
                        : target == null ? null : target.servletPath(uriParts);
                if (newLocation != null) {
                    logger.debug("Redirect mapped from {} to {}", location, newLocation);
                    resp.sendRedirect(newLocation);
                    return;
                }
                returnError(resp, uriParts, "Invalid redirect to '" + location + "'");
                return;
            }
            String linkBase = page == null ? uriParts.buildFor("/") : page.linkBase(uriParts);
            returnHtml(resp, uriParts, response.content(), host, linkBase);
        } catch (ConnectionException e) {
            returnError(resp, uriParts, e.getLocalizedMessage());
        }
    }

    /** the sign-in host first, the account's retail host last */
    static List<String> dialogHosts(String retailUrl) {
        String retailHost;
        try {
            retailHost = URI.create(retailUrl).getHost();
        } catch (IllegalArgumentException e) {
            retailHost = null;
        }
        if (retailHost == null || SIGN_IN_HOST.equalsIgnoreCase(retailHost)) {
            return List.of(SIGN_IN_HOST);
        }
        return List.of(SIGN_IN_HOST, retailHost.toLowerCase(Locale.ROOT));
    }

    static @Nullable String proxyRedirectTarget(String location, String retailUrl, boolean loggedIn,
            ServletUri uriParts) {
        String retailUrlWithSlash = retailUrl + "/";
        if (loggedIn && location.startsWith(retailUrlWithSlash)) {
            return uriParts.buildFor(PROXY_URI_PART + location.substring(retailUrlWithSlash.length()));
        }
        return null;
    }

    private void returnHtml(HttpServletResponse resp, ServletUri uriParts, String html, String host, String linkBase)
            throws IOException {
        resp.addHeader(HttpHeader.CONTENT_TYPE.asString(), MimeTypes.Type.TEXT_HTML_UTF_8.asString());
        resp.getWriter().write(rewriteLinks(html, host, linkBase));
    }

    static String rewriteLinks(String html, String host, String linkBase) {
        Matcher matcher = LINK_TARGET.matcher(html);
        StringBuilder rewritten = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(rewritten, Matcher.quoteReplacement(
                    matcher.group(1) + "=\"" + rewriteLinkTarget(matcher.group(2), host, linkBase) + "\""));
        }
        matcher.appendTail(rewritten);
        return rewritten.toString();
    }

    private static String rewriteLinkTarget(String target, String host, String linkBase) {
        for (String prefix : List.of("https://" + host + "/", "https://" + host + ":443/", "http://" + host + "/",
                "https:&#x2F;&#x2F;" + host + "&#x2F;", "https:&#x2F;&#x2F;" + host + ":443&#x2F;",
                "http:&#x2F;&#x2F;" + host + "&#x2F;")) {
            if (target.startsWith(prefix)) {
                return linkBase + target.substring(prefix.length());
            }
        }
        if (target.startsWith("/") && !target.startsWith("//")) {
            return linkBase + target.substring(1);
        }
        if (target.startsWith("&#x2F;") && !target.startsWith("&#x2F;&#x2F;")) {
            return linkBase + target.substring("&#x2F;".length());
        }
        return target;
    }

    void returnError(HttpServletResponse resp, @Nullable ServletUri uriParts, @Nullable String errorMessage)
            throws IOException {
        String message = errorMessage != null ? errorMessage : "null";
        String tryAgainUri = uriParts == null ? SERVLET_PATH + "/" : uriParts.buildFor("/");
        resp.getWriter()
                .write("<html>" + escapeHtml4(message) + "<br><a href='" + tryAgainUri + "'>Try again</a></html>");
    }

    private Map<String, String> getQueryMap(@Nullable String query) {
        Map<String, String> map = new HashMap<>();
        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                String[] elements = param.split("=");
                if (elements.length == 2) {
                    String name = elements[0];
                    String value = URLDecoder.decode(elements[1], StandardCharsets.UTF_8);
                    map.put(name, value);
                }
            }
        }
        return map;
    }

    private StringWriter evaluateTemplate(String template, VelocityContext ctx) {
        StringWriter stringWriter = new StringWriter();
        ClassLoader classLoader = AmazonEchoControlServlet.class.getClassLoader();
        if (classLoader == null) {
            return stringWriter;
        }
        try (InputStream inputStream = classLoader.getResourceAsStream(template)) {
            if (inputStream != null) {
                Reader reader = new InputStreamReader(inputStream);
                velocityEngine.evaluate(ctx, stringWriter, "VTL", reader);
            }
        } catch (IOException ignored) {
        }
        return stringWriter;
    }

    private void serveStatic(HttpServletResponse resp, String file) throws IOException {
        ClassLoader classLoader = AmazonEchoControlServlet.class.getClassLoader();
        if (classLoader == null) {
            resp.sendError(500);
            return;
        }
        try (InputStream inputStream = classLoader.getResourceAsStream("WEB-INF" + file)) {
            if (inputStream != null) {
                String content = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines()
                        .collect(Collectors.joining("\n"));
                resp.getWriter().write(content);
                return;
            }
        } catch (IOException ignored) {
        }
        resp.sendError(404);
    }

    public static class ChannelOption {
        public String value;
        public String displayName;

        public ChannelOption(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }
    }
}
