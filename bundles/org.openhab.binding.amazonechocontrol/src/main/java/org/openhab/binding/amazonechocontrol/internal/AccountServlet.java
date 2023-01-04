/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amazonechocontrol.internal.handler.AccountHandler;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonBluetoothStates;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonBluetoothStates.BluetoothState;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonBluetoothStates.PairedDevice;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonDevices.Device;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonMusicProvider;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonNotificationSound;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPlaylists;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPlaylists.PlayList;
import org.openhab.core.thing.Thing;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unbescape.html.HtmlEscape;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
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

    private final HttpService httpService;
    private final String servletUrlWithoutRoot;
    private final String servletUrl;
    private final AccountHandler account;
    private final String id;
    private @Nullable Connection connectionToInitialize;
    private final Gson gson;

    public AccountServlet(HttpService httpService, String id, AccountHandler account, Gson gson) {
        this.httpService = httpService;
        this.account = account;
        this.id = id;
        this.gson = gson;

        try {
            servletUrlWithoutRoot = "amazonechocontrol/" + URLEncoder.encode(id, StandardCharsets.UTF_8);
            servletUrl = "/" + servletUrlWithoutRoot;

            httpService.registerServlet(servletUrl, this, null, httpService.createDefaultHttpContext());
        } catch (NamespaceException | ServletException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    private Connection reCreateConnection() {
        Connection oldConnection = connectionToInitialize;
        if (oldConnection == null) {
            oldConnection = account.findConnection();
        }
        return new Connection(oldConnection, this.gson);
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

    void doVerb(String verb, @Nullable HttpServletRequest req, @Nullable HttpServletResponse resp) throws IOException {
        if (req == null) {
            return;
        }
        if (resp == null) {
            return;
        }
        String requestUri = req.getRequestURI();
        if (requestUri == null) {
            return;
        }
        String baseUrl = requestUri.substring(servletUrl.length());
        String uri = baseUrl;
        String queryString = req.getQueryString();
        if (queryString != null && queryString.length() > 0) {
            uri += "?" + queryString;
        }

        Connection connection = this.account.findConnection();
        if (connection != null && "/changedomain".equals(uri)) {
            Map<String, String[]> map = req.getParameterMap();
            String[] domainArray = map.get("domain");
            if (domainArray == null) {
                logger.warn("Could not determine domain");
                return;
            }
            String domain = domainArray[0];
            String loginData = connection.serializeLoginData();
            Connection newConnection = new Connection(null, this.gson);
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
            if ("POST".equals(verb) || "PUT".equals(verb)) {
                postData = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            }

            this.handleProxyRequest(connection, resp, verb, getUrl, null, postData, true, connection.getAmazonSite());
            return;
        }

        // handle post of login page
        connection = this.connectionToInitialize;
        if (connection == null) {
            returnError(resp, "Connection not in initialize mode.");
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
            String value = "";
            if ("failedSignInCount".equals(name)) {
                value = "ape:AA==";
            } else {
                String[] strings = map.get(name);
                if (strings != null && strings.length > 0 && strings[0] != null) {
                    value = strings[0];
                }
            }
            postDataBuilder.append(URLEncoder.encode(value, StandardCharsets.UTF_8.name()));
        }

        uri = req.getRequestURI();
        if (uri == null || !uri.startsWith(servletUrl)) {
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
    protected void doGet(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp) throws IOException {
        if (req == null) {
            return;
        }
        if (resp == null) {
            return;
        }
        String requestUri = req.getRequestURI();
        if (requestUri == null) {
            return;
        }
        String baseUrl = requestUri.substring(servletUrl.length());
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
                if ("/logout".equals(baseUrl) || "/logout/".equals(baseUrl)) {
                    this.connectionToInitialize = reCreateConnection();
                    this.account.setConnection(null);
                    resp.sendRedirect(this.servletUrl);
                    return;
                }
                // handle commands
                if ("/newdevice".equals(baseUrl) || "/newdevice/".equals(baseUrl)) {
                    this.connectionToInitialize = new Connection(null, this.gson);
                    this.account.setConnection(null);
                    resp.sendRedirect(this.servletUrl);
                    return;
                }

                if ("/devices".equals(baseUrl) || "/devices/".equals(baseUrl)) {
                    handleDevices(resp, connection);
                    return;
                }
                if ("/changeDomain".equals(baseUrl) || "/changeDomain/".equals(baseUrl)) {
                    handleChangeDomain(resp, connection);
                    return;
                }
                if ("/ids".equals(baseUrl) || "/ids/".equals(baseUrl)) {
                    String serialNumber = getQueryMap(queryString).get("serialNumber");
                    Device device = account.findDeviceJson(serialNumber);
                    if (device != null) {
                        Thing thing = account.findThingBySerialNumber(device.serialNumber);
                        handleIds(resp, connection, device, thing);
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

            if (!"/".equals(uri)) {
                String newUri = req.getServletPath() + "/";
                resp.sendRedirect(newUri);
                return;
            }

            String html = connection.getLoginPage();
            returnHtml(connection, resp, html, "amazon.com");
        } catch (URISyntaxException | InterruptedException e) {
            logger.warn("get failed with uri syntax error", e);
        }
    }

    public Map<String, String> getQueryMap(@Nullable String query) {
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
        html.append(HtmlEscape.escapeHtml4(message));
        // logout link
        html.append(" <a href='" + servletUrl + "/logout' >");
        html.append(HtmlEscape.escapeHtml4("Logout"));
        html.append("</a>");
        // newdevice link
        html.append(" | <a href='" + servletUrl + "/newdevice' >");
        html.append(HtmlEscape.escapeHtml4("Logout and create new device id"));
        html.append("</a>");
        // customer id
        html.append("<br>Customer Id: ");
        html.append(HtmlEscape.escapeHtml4(connection.getCustomerId()));
        // customer name
        html.append("<br>Customer Name: ");
        html.append(HtmlEscape.escapeHtml4(connection.getCustomerName()));
        // device name
        html.append("<br>App name: ");
        html.append(HtmlEscape.escapeHtml4(connection.getDeviceName()));
        // connection
        html.append("<br>Connected to: ");
        html.append(HtmlEscape.escapeHtml4(connection.getAlexaServer()));
        // domain
        html.append(" <a href='");
        html.append(servletUrl);
        html.append("/changeDomain'>Change</a>");

        // Main UI link
        html.append("<br><a href='/#!/settings/things/" + BINDING_ID + ":"
                + URLEncoder.encode(THING_TYPE_ACCOUNT.getId(), "UTF8") + ":" + URLEncoder.encode(id, "UTF8") + "'>");
        html.append(HtmlEscape.escapeHtml4("Check Thing in Main UI"));
        html.append("</a><br><br>");

        // device list
        html.append(
                "<table><tr><th align='left'>Device</th><th align='left'>Serial Number</th><th align='left'>State</th><th align='left'>Thing</th><th align='left'>Family</th><th align='left'>Type</th><th align='left'>Customer Id</th></tr>");
        for (Device device : this.account.getLastKnownDevices()) {

            html.append("<tr><td>");
            html.append(HtmlEscape.escapeHtml4(nullReplacement(device.accountName)));
            html.append("</td><td>");
            html.append(HtmlEscape.escapeHtml4(nullReplacement(device.serialNumber)));
            html.append("</td><td>");
            html.append(HtmlEscape.escapeHtml4(device.online ? "Online" : "Offline"));
            html.append("</td><td>");
            Thing accountHandler = account.findThingBySerialNumber(device.serialNumber);
            if (accountHandler != null) {
                html.append("<a href='" + servletUrl + "/ids/?serialNumber="
                        + URLEncoder.encode(device.serialNumber, "UTF8") + "'>"
                        + HtmlEscape.escapeHtml4(accountHandler.getLabel()) + "</a>");
            } else {
                html.append("<a href='" + servletUrl + "/ids/?serialNumber="
                        + URLEncoder.encode(device.serialNumber, "UTF8") + "'>" + HtmlEscape.escapeHtml4("Not defined")
                        + "</a>");
            }
            html.append("</td><td>");
            html.append(HtmlEscape.escapeHtml4(nullReplacement(device.deviceFamily)));
            html.append("</td><td>");
            html.append(HtmlEscape.escapeHtml4(nullReplacement(device.deviceType)));
            html.append("</td><td>");
            html.append(HtmlEscape.escapeHtml4(nullReplacement(device.deviceOwnerCustomerId)));
            html.append("</td>");
            html.append("</tr>");
        }
        html.append("</table>");
        createPageEndAndSent(resp, html);
    }

    private void handleDevices(HttpServletResponse resp, Connection connection)
            throws IOException, URISyntaxException, InterruptedException {
        returnHtml(connection, resp, "<html>" + HtmlEscape.escapeHtml4(connection.getDeviceListJson()) + "</html>");
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
                + HtmlEscape.escapeHtml4(BINDING_NAME + " - " + this.account.getThing().getLabel()));
        if (!title.isEmpty()) {
            html.append(" - ");
            html.append(HtmlEscape.escapeHtml4(title));
        }
        html.append("</title><head><body>");
        html.append("<h1>" + HtmlEscape.escapeHtml4(BINDING_NAME + " - " + this.account.getThing().getLabel()));
        if (!title.isEmpty()) {
            html.append(" - ");
            html.append(HtmlEscape.escapeHtml4(title));
        }
        html.append("</h1>");
        return html;
    }

    private void createPageEndAndSent(HttpServletResponse resp, StringBuilder html) {
        // account overview link
        html.append("<br><a href='" + servletUrl + "/../' >");
        html.append(HtmlEscape.escapeHtml4("Account overview"));
        html.append("</a><br>");

        html.append("</body></html>");
        resp.addHeader("content-type", "text/html;charset=UTF-8");
        try {
            resp.getWriter().write(html.toString());
        } catch (IOException e) {
            logger.warn("return html failed with IO error", e);
        }
    }

    private void handleIds(HttpServletResponse resp, Connection connection, Device device, @Nullable Thing thing)
            throws IOException, URISyntaxException {
        StringBuilder html;
        if (thing != null) {
            html = createPageStart("Channel Options - " + thing.getLabel());
        } else {
            html = createPageStart("Device Information - No thing defined");
        }
        renderBluetoothMacChannel(connection, device, html);
        renderAmazonMusicPlaylistIdChannel(connection, device, html);
        renderPlayAlarmSoundChannel(connection, device, html);
        renderMusicProviderIdChannel(connection, html);
        renderCapabilities(connection, device, html);
        createPageEndAndSent(resp, html);
    }

    private void renderCapabilities(Connection connection, Device device, StringBuilder html) {
        html.append("<h2>Capabilities</h2>");
        html.append("<table><tr><th align='left'>Name</th></tr>");
        device.getCapabilities().forEach(
                capability -> html.append("<tr><td>").append(HtmlEscape.escapeHtml4(capability)).append("</td></tr>"));
        html.append("</table>");
    }

    private void renderMusicProviderIdChannel(Connection connection, StringBuilder html) {
        html.append("<h2>").append(HtmlEscape.escapeHtml4("Channel " + CHANNEL_MUSIC_PROVIDER_ID)).append("</h2>");
        html.append("<table><tr><th align='left'>Name</th><th align='left'>Value</th></tr>");
        List<JsonMusicProvider> musicProviders = connection.getMusicProviders();
        for (JsonMusicProvider musicProvider : musicProviders) {
            List<String> properties = musicProvider.supportedProperties;
            String providerId = musicProvider.id;
            String displayName = musicProvider.displayName;
            if (properties != null && properties.contains("Alexa.Music.PlaySearchPhrase") && providerId != null
                    && !providerId.isEmpty() && "AVAILABLE".equals(musicProvider.availability) && displayName != null
                    && !displayName.isEmpty()) {
                html.append("<tr><td>");
                html.append(HtmlEscape.escapeHtml4(displayName));
                html.append("</td><td>");
                html.append(HtmlEscape.escapeHtml4(providerId));
                html.append("</td></tr>");
            }
        }
        html.append("</table>");
    }

    private void renderPlayAlarmSoundChannel(Connection connection, Device device, StringBuilder html) {
        html.append("<h2>").append(HtmlEscape.escapeHtml4("Channel " + CHANNEL_PLAY_ALARM_SOUND)).append("</h2>");
        List<JsonNotificationSound> notificationSounds = List.of();
        String errorMessage = "No notifications sounds found";
        try {
            notificationSounds = connection.getNotificationSounds(device);
        } catch (IOException | HttpException | URISyntaxException | JsonSyntaxException | ConnectionException
                | InterruptedException e) {
            errorMessage = e.getLocalizedMessage();
        }
        if (!notificationSounds.isEmpty()) {
            html.append("<table><tr><th align='left'>Name</th><th align='left'>Value</th></tr>");
            for (JsonNotificationSound notificationSound : notificationSounds) {
                if (notificationSound.folder == null && notificationSound.providerId != null
                        && notificationSound.id != null && notificationSound.displayName != null) {
                    String providerSoundId = notificationSound.providerId + ":" + notificationSound.id;

                    html.append("<tr><td>");
                    html.append(HtmlEscape.escapeHtml4(notificationSound.displayName));
                    html.append("</td><td>");
                    html.append(HtmlEscape.escapeHtml4(providerSoundId));
                    html.append("</td></tr>");
                }
            }
            html.append("</table>");
        } else {
            html.append(HtmlEscape.escapeHtml4(errorMessage));
        }
    }

    private void renderAmazonMusicPlaylistIdChannel(Connection connection, Device device, StringBuilder html) {
        html.append("<h2>").append(HtmlEscape.escapeHtml4("Channel " + CHANNEL_AMAZON_MUSIC_PLAY_LIST_ID))
                .append("</h2>");

        JsonPlaylists playLists = null;
        String errorMessage = "No playlists found";
        try {
            playLists = connection.getPlaylists(device);
        } catch (IOException | HttpException | URISyntaxException | JsonSyntaxException | ConnectionException
                | InterruptedException e) {
            errorMessage = e.getLocalizedMessage();
        }

        if (playLists != null) {
            Map<String, PlayList @Nullable []> playlistMap = playLists.playlists;
            if (playlistMap != null && !playlistMap.isEmpty()) {
                html.append("<table><tr><th align='left'>Name</th><th align='left'>Value</th></tr>");

                for (PlayList[] innerLists : playlistMap.values()) {
                    {
                        if (innerLists != null && innerLists.length > 0) {
                            PlayList playList = innerLists[0];
                            if (playList != null && playList.playlistId != null && playList.title != null) {
                                html.append("<tr><td>");
                                html.append(HtmlEscape.escapeHtml4(nullReplacement(playList.title)));
                                html.append("</td><td>");
                                html.append(HtmlEscape.escapeHtml4(nullReplacement(playList.playlistId)));
                                html.append("</td></tr>");
                            }
                        }
                    }
                }
                html.append("</table>");
            } else {
                html.append(HtmlEscape.escapeHtml4(errorMessage));
            }
        }
    }

    private void renderBluetoothMacChannel(Connection connection, Device device, StringBuilder html) {
        html.append("<h2>").append(HtmlEscape.escapeHtml4("Channel " + CHANNEL_BLUETOOTH_MAC)).append("</h2>");
        JsonBluetoothStates bluetoothStates = connection.getBluetoothConnectionStates();
        if (bluetoothStates == null) {
            return;
        }
        BluetoothState[] innerStates = bluetoothStates.bluetoothStates;
        if (innerStates == null) {
            return;
        }
        for (BluetoothState state : innerStates) {
            if (state == null) {
                continue;
            }
            String stateDeviceSerialNumber = state.deviceSerialNumber;
            if ((stateDeviceSerialNumber == null && device.serialNumber == null)
                    || (stateDeviceSerialNumber != null && stateDeviceSerialNumber.equals(device.serialNumber))) {
                List<PairedDevice> pairedDeviceList = state.getPairedDeviceList();
                if (!pairedDeviceList.isEmpty()) {
                    html.append("<table><tr><th align='left'>Name</th><th align='left'>Value</th></tr>");
                    for (PairedDevice pairedDevice : pairedDeviceList) {
                        html.append("<tr><td>");
                        html.append(HtmlEscape.escapeHtml4(nullReplacement(pairedDevice.friendlyName)));
                        html.append("</td><td>");
                        html.append(HtmlEscape.escapeHtml4(nullReplacement(pairedDevice.address)));
                        html.append("</td></tr>");
                    }
                    html.append("</table>");
                } else {
                    html.append(HtmlEscape.escapeHtml4("No bluetooth devices paired"));
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
                headers = new HashMap<>();
                headers.put("Referer", referer);
            }

            urlConnection = connection.makeRequest(verb, url, postData, json, false, headers, 0);
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
        } catch (URISyntaxException | ConnectionException | InterruptedException e) {
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
                .replace("https://www." + amazonSite + ":443" + "/", servletUrl + "/")
                .replace("https:&#x2F;&#x2F;www." + amazonSite + "&#x2F;", servletUrl + "/")
                .replace("https:&#x2F;&#x2F;www." + amazonSite + ":443" + "&#x2F;", servletUrl + "/")
                .replace("http://www." + amazonSite + "/", servletUrl + "/")
                .replace("http:&#x2F;&#x2F;www." + amazonSite + "&#x2F;", servletUrl + "/");

        resp.addHeader("content-type", "text/html;charset=UTF-8");
        try {
            resp.getWriter().write(resultHtml);
        } catch (IOException e) {
            logger.warn("return html failed with IO error", e);
        }
    }

    void returnError(HttpServletResponse resp, @Nullable String errorMessage) {
        try {
            String message = errorMessage != null ? errorMessage : "null";
            resp.getWriter().write("<html>" + HtmlEscape.escapeHtml4(message) + "<br><a href='" + servletUrl
                    + "'>Try again</a></html>");
        } catch (IOException e) {
            logger.info("Returning error message failed", e);
        }
    }
}
