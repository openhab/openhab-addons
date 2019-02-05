/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.amazonechocontrol.internal.handler.AccountHandler;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonBluetoothStates;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonBluetoothStates.BluetoothState;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonBluetoothStates.PairedDevice;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonDevices.Device;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonMusicProvider;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonNotificationSound;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPlaylists;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPlaylists.PlayList;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 *
 * Provides the following functions
 * --- Login ---
 * Simple http proxy to forward the login dialog from amazon to the user through the binding
 * so the user can enter a captcha or other extended login information
 * --- List of devices ---
 * Used to get the device information of new devices which are currently not known
 * --- List of IDs ---
 * Simple possibility for a user to get the ids needed for writing rules
 *
 * @author Michael Geramb - Initial Contribution
 */
@NonNullByDefault
public class AccountServlet extends HttpServlet {

    private static final long serialVersionUID = -1453738923337413163L;
    private static final String FORWARD_URI_PART = "/FORWARD/";
    private static final String PROXY_URI_PART = "/PROXY/";

    private final Logger logger = LoggerFactory.getLogger(AccountServlet.class);

    HttpService httpService;
    String servletUrlWithoutRoot;
    String servletUrl;
    AccountHandler account;
    String id;
    @Nullable
    Connection connectionToInitialize;
    Gson gson = new Gson();

    public AccountServlet(HttpService httpService, String id, AccountHandler account) {
        this.httpService = httpService;
        this.account = account;
        this.id = id;
        try {
            servletUrlWithoutRoot = "amazonechocontrol/" + URLEncoder.encode(id, "UTF8");
        } catch (UnsupportedEncodingException e) {
            servletUrlWithoutRoot = "";
            servletUrl = "";
            logger.warn("Register servlet fails {}", e);
            return;
        }
        servletUrl = "/" + servletUrlWithoutRoot;
        try {
            httpService.registerServlet(servletUrl, this, null, httpService.createDefaultHttpContext());
        } catch (ServletException e) {
            logger.warn("Register servlet fails {}", e);
        } catch (NamespaceException e) {
            logger.warn("Register servlet fails {}", e);
        }
    }

    private Connection reCreateConnection() {
        Connection oldConnection = connectionToInitialize;
        if (oldConnection == null) {
            oldConnection = account.findConnection();
        }
        return new Connection(oldConnection);
    }

    public void dispose() {
        httpService.unregister(servletUrl);
    }

    @Override
    protected void doPut(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp)
            throws ServletException, IOException {
        doVerb("PUT", req, resp);
    }

    @Override
    protected void doDelete(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp)
            throws ServletException, IOException {
        doVerb("DELETE", req, resp);
    }

    @Override
    protected void doPost(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp)
            throws ServletException, IOException {
        doVerb("POST", req, resp);
    }

    void doVerb(String verb, @Nullable HttpServletRequest req, @Nullable HttpServletResponse resp)
            throws ServletException, IOException {
        if (req == null) {
            return;
        }
        if (resp == null) {
            return;
        }
        String baseUrl = req.getRequestURI().substring(servletUrl.length());
        String uri = baseUrl;
        String queryString = req.getQueryString();
        if (queryString != null && queryString.length() > 0) {
            uri += "?" + queryString;
        }

        Connection connection = this.account.findConnection();
        if (connection != null && uri.equals("/changedomain")) {
            Map<String, String[]> map = req.getParameterMap();
            String domain = map.get("domain")[0];
            String loginData = connection.serializeLoginData();
            Connection newConnection = new Connection(null);
            if (newConnection.tryRestoreLogin(loginData, domain)) {
                account.setConnection(newConnection);
            }
            resp.sendRedirect(servletUrl);
            return;
        }
        if (uri.startsWith(PROXY_URI_PART)) {
            // handle proxy request

            if (connection == null) {
                returnError(resp, "Account not online");
                return;
            }
            String getUrl = "https://alexa." + connection.getAmazonSite() + "/"
                    + uri.substring(PROXY_URI_PART.length());

            String postData = null;
            if (verb == "POST" || verb == "PUT") {
                postData = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            }

            this.handleProxyRequest(connection, resp, verb, getUrl, null, postData, true, connection.getAmazonSite());
            return;
        }

        // handle post of login page
        connection = this.connectionToInitialize;
        if (connection == null) {
            returnError(resp, "Connection not in intialize mode.");
            return;
        }

        resp.addHeader("content-type", "text/html;charset=UTF-8");

        Map<String, String[]> map = req.getParameterMap();
        StringBuilder postDataBuilder = new StringBuilder();
        for (String name : map.keySet()) {
            if (postDataBuilder.length() > 0) {
                postDataBuilder.append('&');
            }

            postDataBuilder.append(name);
            postDataBuilder.append('=');
            String value = map.get(name)[0];
            if (name.equals("failedSignInCount")) {
                value = "ape:AA==";
            }
            postDataBuilder.append(URLEncoder.encode(value, StandardCharsets.UTF_8.name()));
        }

        uri = req.getRequestURI();
        if (!uri.startsWith(servletUrl)) {
            returnError(resp, "Invalid request uri '" + uri + "'");
            return;
        }
        String relativeUrl = uri.substring(servletUrl.length()).replace(FORWARD_URI_PART, "/");

        String site = connection.getAmazonSite();
        if (relativeUrl.startsWith("/ap/signin")) {
            site = "amazon.com";
        }
        String postUrl = "https://www." + site + relativeUrl;
        queryString = req.getQueryString();
        if (queryString != null && queryString.length() > 0) {
            postUrl += "?" + queryString;
        }
        String referer = "https://www." + site;
        String postData = postDataBuilder.toString();
        handleProxyRequest(connection, resp, "POST", postUrl, referer, postData, false, site);
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
        String baseUrl = req.getRequestURI().substring(servletUrl.length());
        String uri = baseUrl;
        String queryString = req.getQueryString();
        if (queryString != null && queryString.length() > 0) {
            uri += "?" + queryString;
        }
        logger.debug("doGet {}", uri);
        try {
            Connection connection = this.connectionToInitialize;
            if (uri.startsWith(FORWARD_URI_PART) && connection != null) {

                String getUrl = "https://www." + connection.getAmazonSite() + "/"
                        + uri.substring(FORWARD_URI_PART.length());

                this.handleProxyRequest(connection, resp, "GET", getUrl, null, null, false, connection.getAmazonSite());
                return;
            }

            connection = this.account.findConnection();
            if (uri.startsWith(PROXY_URI_PART)) {
                // handle proxy request

                if (connection == null) {
                    returnError(resp, "Account not online");
                    return;
                }
                String getUrl = "https://alexa." + connection.getAmazonSite() + "/"
                        + uri.substring(PROXY_URI_PART.length());

                this.handleProxyRequest(connection, resp, "GET", getUrl, null, null, false, connection.getAmazonSite());
                return;
            }

            if (connection != null && connection.verifyLogin()) {

                // handle commands
                if (baseUrl.equals("/logout") || baseUrl.equals("/logout/")) {
                    this.connectionToInitialize = reCreateConnection();
                    this.account.setConnection(null);
                    resp.sendRedirect(this.servletUrl);
                    return;
                }
                // handle commands
                if (baseUrl.equals("/newdevice") || baseUrl.equals("/newdevice/")) {
                    this.connectionToInitialize = new Connection(null);
                    this.account.setConnection(null);
                    resp.sendRedirect(this.servletUrl);
                    return;
                }

                if (baseUrl.equals("/devices") || baseUrl.equals("/devices/")) {
                    handleDevices(resp, connection);
                    return;
                }
                if (baseUrl.equals("/changeDomain") || baseUrl.equals("/changeDomain/")) {
                    handleChangeDomain(resp, connection);
                    return;
                }
                if (baseUrl.equals("/ids") || baseUrl.equals("/ids/")) {
                    String serialNumber = getQueryMap(queryString).get("serialNumber");
                    Device device = account.findDeviceJson(serialNumber);
                    if (device != null) {
                        Thing thing = account.findThingBySerialNumber(device.serialNumber);
                        if (thing != null) {
                            handleIds(resp, connection, device, thing);
                        }
                        return;
                    }
                }
                // return hint that everything is ok
                handleDefaultPageResult(resp, "The Account is logged in.", connection);
                return;
            }
            connection = this.connectionToInitialize;
            if (connection == null) {
                connection = this.reCreateConnection();
                this.connectionToInitialize = connection;
            }

            if (!uri.equals("/")) {
                String newUri = req.getServletPath() + "/";
                resp.sendRedirect(newUri);
                return;
            }

            String html = connection.getLoginPage();
            returnHtml(connection, resp, html, "amazon.com");
        } catch (URISyntaxException e) {
            logger.warn("get failed with uri syntax error {}", e);
        }
    }

    public Map<String, String> getQueryMap(@Nullable String query) {
        Map<String, String> map = new HashMap<String, String>();
        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                String[] elements = param.split("=");
                if (elements.length == 2) {
                    String name = elements[0];
                    String value = "";
                    try {
                        value = URLDecoder.decode(elements[1], "UTF8");
                    } catch (UnsupportedEncodingException e) {
                        logger.info("Unsupported encoding {}", e);
                    }
                    map.put(name, value);
                }
            }
        }
        return map;
    }

    private void handleChangeDomain(HttpServletResponse resp, Connection connection) {
        StringBuilder html = createPageStart("Change Domain");
        html.append("<form action='");
        html.append(servletUrl);
        html.append("/changedomain' method='post'>\nDomain:\n<input type='text' name='domain' value='");
        html.append(connection.getAmazonSite());
        html.append("'>\n<br>\n<input type=\"submit\" value=\"Submit\">\n</form>");

        createPageEndAndSent(resp, html);
    }

    private void handleDefaultPageResult(HttpServletResponse resp, String message, Connection connection)
            throws IOException {
        StringBuilder html = createPageStart("");
        html.append(StringEscapeUtils.escapeHtml(message));
        // logout link
        html.append(" <a href='" + servletUrl + "/logout' >");
        html.append(StringEscapeUtils.escapeHtml("Logout"));
        html.append("</a>");
        // newdevice link
        html.append(" | <a href='" + servletUrl + "/newdevice' >");
        html.append(StringEscapeUtils.escapeHtml("Logout and create new device id"));
        html.append("</a>");
        // device name
        html.append("<br>App name: ");
        html.append(StringEscapeUtils.escapeHtml(connection.getDeviceName()));
        // connection
        html.append("<br>Connected to: ");
        html.append(StringEscapeUtils.escapeHtml(connection.getAlexaServer()));
        // domain
        html.append(" <a href='");
        html.append(servletUrl);
        html.append("/changeDomain'>Change</a>");

        // paper ui link
        html.append("<br><a href='/paperui/index.html#/configuration/things/view/" + BINDING_ID + ":"
                + URLEncoder.encode(THING_TYPE_ACCOUNT.getId(), "UTF8") + ":" + URLEncoder.encode(id, "UTF8") + "'>");
        html.append(StringEscapeUtils.escapeHtml("Check Thing in Paper UI"));
        html.append("</a><br><br>");

        // device list
        html.append(
                "<table><tr><th align='left'>Device</th><th align='left'>Serial Number</th><th align='left'>State</th><th align='left'>Thing</th><th align='left'>Type</th><th align='left'>Family</th></tr>");
        for (Device device : this.account.getLastKnownDevices()) {

            html.append("<tr><td>");
            html.append(StringEscapeUtils.escapeHtml(nullReplacement(device.accountName)));
            html.append("</td><td>");
            html.append(StringEscapeUtils.escapeHtml(nullReplacement(device.serialNumber)));
            html.append("</td><td>");
            html.append(StringEscapeUtils.escapeHtml(device.online ? "Online" : "Offline"));
            html.append("</td><td>");
            Thing accountHandler = account.findThingBySerialNumber(device.serialNumber);
            if (accountHandler != null) {
                html.append("<a href='" + servletUrl + "/ids/?serialNumber="
                        + URLEncoder.encode(device.serialNumber, "UTF8") + "'>"
                        + StringEscapeUtils.escapeHtml(accountHandler.getLabel()) + "</a>");
            } else {
                html.append("Not defined");
            }
            html.append("</td><td>");
            html.append(StringEscapeUtils.escapeHtml(nullReplacement(device.deviceFamily)));
            html.append("</td><td>");
            html.append(StringEscapeUtils.escapeHtml(nullReplacement(device.deviceType)));
            html.append("</td><td>");
            html.append("</td></tr>");
        }
        html.append("</table>");
        createPageEndAndSent(resp, html);
    }

    private void handleDevices(HttpServletResponse resp, Connection connection) throws IOException, URISyntaxException {
        returnHtml(connection, resp,
                "<html>" + StringEscapeUtils.escapeHtml(connection.getDeviceListJson()) + "</html>");
    }

    private String nullReplacement(@Nullable String text) {
        if (text == null) {
            return "<unknown>";
        }
        return text;
    }

    StringBuilder createPageStart(String title) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>"
                + StringEscapeUtils.escapeHtml(BINDING_NAME + " - " + this.account.getThing().getLabel()));
        if (StringUtils.isNotEmpty(title)) {
            html.append(" - ");
            html.append(StringEscapeUtils.escapeHtml(title));
        }
        html.append("</title><head><body>");
        html.append("<h1>" + StringEscapeUtils.escapeHtml(BINDING_NAME + " - " + this.account.getThing().getLabel()));
        if (StringUtils.isNotEmpty(title)) {
            html.append(" - ");
            html.append(StringEscapeUtils.escapeHtml(title));
        }
        html.append("</h1>");
        return html;
    }

    private void createPageEndAndSent(HttpServletResponse resp, StringBuilder html) {
        // account overview link
        html.append("<br><a href='" + servletUrl + "/../' >");
        html.append(StringEscapeUtils.escapeHtml("Account overview"));
        html.append("</a><br>");

        html.append("</body></html>");
        resp.addHeader("content-type", "text/html;charset=UTF-8");
        try {
            resp.getWriter().write(html.toString());
        } catch (IOException e) {
            logger.warn("return html failed with IO error {}", e);
        }
    }

    private void handleIds(HttpServletResponse resp, Connection connection, Device device, Thing thing)
            throws IOException, URISyntaxException {
        StringBuilder html = createPageStart("Channel Options - " + thing.getLabel());

        renderBluetoothMacChannel(connection, device, html);
        renderAmazonMusicPlaylistIdChannel(connection, device, html);
        renderPlayAlarmSoundChannel(connection, device, html);
        renderMusicProviderIdChannel(connection, html);

        createPageEndAndSent(resp, html);
    }

    private void renderMusicProviderIdChannel(Connection connection, StringBuilder html) {
        html.append("<h2>" + StringEscapeUtils.escapeHtml("Channel " + CHANNEL_MUSIC_PROVIDER_ID) + "</h2>");
        html.append("<table><tr><th align='left'>Name</th><th align='left'>Value</th></tr>");
        List<JsonMusicProvider> musicProviders = connection.getMusicProviders();
        for (JsonMusicProvider musicProvider : musicProviders) {
            @Nullable
            List<@Nullable String> properties = musicProvider.supportedProperties;
            String providerId = musicProvider.id;
            String displayName = musicProvider.displayName;
            if (properties != null && properties.contains("Alexa.Music.PlaySearchPhrase")
                    && StringUtils.isNotEmpty(providerId) && StringUtils.equals(musicProvider.availability, "AVAILABLE")
                    && StringUtils.isNotEmpty(displayName)) {
                html.append("<tr><td>");
                html.append(StringEscapeUtils.escapeHtml(displayName));
                html.append("</td><td>");
                html.append(StringEscapeUtils.escapeHtml(providerId));
                html.append("</td></tr>");
            }
        }
        html.append("</table>");
    }

    private void renderPlayAlarmSoundChannel(Connection connection, Device device, StringBuilder html) {
        html.append("<h2>" + StringEscapeUtils.escapeHtml("Channel " + CHANNEL_PLAY_ALARM_SOUND) + "</h2>");
        JsonNotificationSound[] notificationSounds = null;
        String errorMessage = "No notifications sounds found";
        try {
            notificationSounds = connection.getNotificationSounds(device);
        } catch (IOException | HttpException | URISyntaxException | JsonSyntaxException | ConnectionException e) {
            errorMessage = e.getLocalizedMessage();
        }
        if (notificationSounds != null) {
            html.append("<table><tr><th align='left'>Name</th><th align='left'>Value</th></tr>");
            for (JsonNotificationSound notificationSound : notificationSounds) {
                if (notificationSound.folder == null && notificationSound.providerId != null
                        && notificationSound.id != null && notificationSound.displayName != null) {
                    String providerSoundId = notificationSound.providerId + ":" + notificationSound.id;

                    html.append("<tr><td>");
                    html.append(StringEscapeUtils.escapeHtml(notificationSound.displayName));
                    html.append("</td><td>");
                    html.append(StringEscapeUtils.escapeHtml(providerSoundId));
                    html.append("</td></tr>");
                }
            }
            html.append("</table>");
        } else {
            html.append(StringEscapeUtils.escapeHtml(errorMessage));
        }
    }

    private void renderAmazonMusicPlaylistIdChannel(Connection connection, Device device, StringBuilder html) {
        html.append("<h2>" + StringEscapeUtils.escapeHtml("Channel " + CHANNEL_AMAZON_MUSIC_PLAY_LIST_ID) + "</h2>");

        JsonPlaylists playLists = null;
        String errorMessage = "No playlists found";
        try {
            playLists = connection.getPlaylists(device);
        } catch (IOException | HttpException | URISyntaxException | JsonSyntaxException | ConnectionException e) {
            errorMessage = e.getLocalizedMessage();
        }

        if (playLists != null) {
            Map<@NonNull String, @Nullable PlayList @Nullable []> playlistMap = playLists.playlists;
            if (playlistMap != null && !playlistMap.isEmpty()) {
                html.append("<table><tr><th align='left'>Name</th><th align='left'>Value</th></tr>");

                for (PlayList[] innerLists : playlistMap.values()) {
                    {
                        if (innerLists != null && innerLists.length > 0) {
                            PlayList playList = innerLists[0];
                            if (playList.playlistId != null && playList.title != null) {
                                html.append("<tr><td>");
                                html.append(StringEscapeUtils.escapeHtml(nullReplacement(playList.title)));
                                html.append("</td><td>");
                                html.append(StringEscapeUtils.escapeHtml(nullReplacement(playList.playlistId)));
                                html.append("</td></tr>");
                            }
                        }
                    }
                }
                html.append("</table>");
            } else {
                html.append(StringEscapeUtils.escapeHtml(errorMessage));
            }
        }
    }

    private void renderBluetoothMacChannel(Connection connection, Device device, StringBuilder html) {
        html.append("<h2>" + StringEscapeUtils.escapeHtml("Channel " + CHANNEL_BLUETOOTH_MAC) + "</h2>");
        JsonBluetoothStates bluetoothStates = connection.getBluetoothConnectionStates();
        BluetoothState[] innerStates = bluetoothStates.bluetoothStates;
        if (innerStates != null) {
            for (BluetoothState state : innerStates) {
                if (StringUtils.equals(state.deviceSerialNumber, device.serialNumber)) {
                    PairedDevice[] pairedDeviceList = state.pairedDeviceList;
                    if (pairedDeviceList != null && pairedDeviceList.length > 0) {
                        html.append("<table><tr><th align='left'>Name</th><th align='left'>Value</th></tr>");
                        for (PairedDevice pairedDevice : pairedDeviceList) {
                            html.append("<tr><td>");
                            html.append(StringEscapeUtils.escapeHtml(nullReplacement(pairedDevice.friendlyName)));
                            html.append("</td><td>");
                            html.append(StringEscapeUtils.escapeHtml(nullReplacement(pairedDevice.address)));
                            html.append("</td></tr>");
                        }
                        html.append("</table>");
                    } else {
                        html.append(StringEscapeUtils.escapeHtml("No bluetooth devices paired"));
                    }
                }
            }
        }
    }

    void handleProxyRequest(Connection connection, HttpServletResponse resp, String verb, String url,
            @Nullable String referer, @Nullable String postData, boolean json, String site) throws IOException {
        HttpsURLConnection urlConnection;
        try {
            Map<String, String> headers = null;
            if (referer != null) {
                headers = new HashMap<String, String>();
                headers.put("Referer", referer);
            }

            urlConnection = connection.makeRequest(verb, url, postData, json, false, headers);
            if (urlConnection.getResponseCode() == 302) {
                {
                    String location = urlConnection.getHeaderField("location");
                    if (location.contains("/ap/maplanding")) {

                        try {
                            connection.registerConnectionAsApp(location);
                            account.setConnection(connection);
                            handleDefaultPageResult(resp, "Login succeeded", connection);
                            this.connectionToInitialize = null;
                            return;
                        } catch (URISyntaxException | ConnectionException e) {
                            returnError(resp,
                                    "Login to '" + connection.getAmazonSite() + "' failed: " + e.getLocalizedMessage());
                            this.connectionToInitialize = null;
                            return;
                        }

                    }

                    String startString = "https://www." + connection.getAmazonSite() + "/";
                    String newLocation = null;
                    if (location.startsWith(startString) && connection.getIsLoggedIn()) {
                        newLocation = servletUrl + PROXY_URI_PART + location.substring(startString.length());
                    } else if (location.startsWith(startString)) {
                        newLocation = servletUrl + FORWARD_URI_PART + location.substring(startString.length());
                    } else {
                        startString = "/";
                        if (location.startsWith(startString)) {
                            newLocation = servletUrl + FORWARD_URI_PART + location.substring(startString.length());
                        }
                    }
                    if (newLocation != null) {
                        logger.debug("Redirect mapped from {} to {}", location, newLocation);

                        resp.sendRedirect(newLocation);
                        return;
                    }
                    returnError(resp, "Invalid redirect to '" + location + "'");
                    return;
                }
            }
        } catch (URISyntaxException | ConnectionException e) {
            returnError(resp, e.getLocalizedMessage());
            return;
        }
        String response = connection.convertStream(urlConnection);
        returnHtml(connection, resp, response, site);
    }

    private void returnHtml(Connection connection, HttpServletResponse resp, String html) {
        returnHtml(connection, resp, html, connection.getAmazonSite());
    }

    private void returnHtml(Connection connection, HttpServletResponse resp, String html, String amazonSite) {
        String resultHtml = html.replace("action=\"/", "action=\"" + servletUrl + "/")
                .replace("action=\"&#x2F;", "action=\"" + servletUrl + "/")
                .replace("https://www." + amazonSite + "/", servletUrl + "/")
                .replace("https:&#x2F;&#x2F;www." + amazonSite + "&#x2F;", servletUrl + "/")
                .replace("http://www." + amazonSite + "/", servletUrl + "/")
                .replace("http:&#x2F;&#x2F;www." + amazonSite + "&#x2F;", servletUrl + "/");

        resp.addHeader("content-type", "text/html;charset=UTF-8");
        try {
            resp.getWriter().write(resultHtml);
        } catch (IOException e) {
            logger.warn("return html failed with IO error {}", e);
        }
    }

    void returnError(HttpServletResponse resp, String errorMessage) {
        try {
            resp.getWriter().write("<html>" + StringEscapeUtils.escapeHtml(errorMessage) + "<br><a href='" + servletUrl
                    + "'>Try again</a></html>");
        } catch (IOException e) {
            logger.info("Returning error message failed {}", e);
        }
    }
}
