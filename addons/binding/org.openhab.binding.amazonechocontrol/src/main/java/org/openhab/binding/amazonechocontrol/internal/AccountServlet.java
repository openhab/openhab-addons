/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.amazonechocontrol.internal;

import static org.openhab.binding.amazonechocontrol.AmazonEchoControlBindingConstants.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.openhab.binding.amazonechocontrol.handler.AccountHandler;
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

    private final Logger logger = LoggerFactory.getLogger(AccountServlet.class);

    HttpService httpService;
    String servletUrlWithoutRoot;
    String servletUrl;
    AccountHandler account;
    AccountConfiguration configuration;
    String id;
    Connection connection;

    public AccountServlet(HttpService httpService, String id, AccountHandler account,
            AccountConfiguration configuration) {
        this.httpService = httpService;
        this.account = account;
        this.id = id;
        this.configuration = configuration;
        this.connection = reCreateConnection();
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
        return new Connection(configuration.email, configuration.password, configuration.amazonSite, this.id);
    }

    public void dispose() {
        httpService.unregister(servletUrl);
    }

    @Override
    protected void doPost(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp)
            throws ServletException, IOException {
        if (req == null) {
            return;
        }
        if (resp == null) {
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
            postDataBuilder.append(URLEncoder.encode(value, "UTF-8"));
            if (name.equals("email") && !value.equalsIgnoreCase(configuration.email)) {
                returnError(resp,
                        "Email must match the configured email of your thing. Change your configuration or retype your email.");
                return;
            }

            if (name.equals("password") && !value.equals(configuration.password)) {
                returnError(resp,
                        "Password must match the configured password of your thing. Change your configuration or retype your password.");
                return;
            }
        }

        String uri = req.getRequestURI();
        if (!uri.startsWith(servletUrl)) {
            returnError(resp, "Invalid request uri '" + uri + "'");
            return;
        }
        String relativeUrl = uri.substring(servletUrl.length()).replace(FORWARD_URI_PART, "/");

        String postUrl = "https://www." + connection.getAmazonSite() + relativeUrl;
        String queryString = req.getQueryString();
        if (queryString != null && queryString.length() > 0) {
            postUrl += "?" + queryString;
        }
        String referer = "https://www." + connection.getAmazonSite();
        String postData = postDataBuilder.toString();
        HandleProxyRequest(resp, "POST", postUrl, referer, postData);
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
            if (uri.startsWith(FORWARD_URI_PART)) {

                String getUrl = "https://www." + connection.getAmazonSite() + "/"
                        + uri.substring(FORWARD_URI_PART.length());

                this.HandleProxyRequest(resp, "GET", getUrl, null, null);
                return;
            }

            Connection connection = this.account.findConnection();
            if (connection != null && connection.verifyLogin()) {

                // handle diagnostic commands
                if (baseUrl.equals("/devices") || baseUrl.equals("/devices/")) {
                    handleDevices(resp, connection);
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
                handleDefaultPageResult(resp, "The Account is already logged in.");
                return;
            }

            if (!uri.equals("/")) {
                String newUri = req.getServletPath() + "/";
                resp.sendRedirect(newUri);
                return;
            }

            String html = this.connection.getLoginPage();
            returnHtml(resp, html);
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

    private void handleDefaultPageResult(HttpServletResponse resp, String message) throws IOException {
        StringBuilder html = createPageStart("Index");
        html.append(StringEscapeUtils.escapeHtml(message + " The account thing should be online."));
        html.append("<br><a href='/paperui/index.html#/configuration/things/view/" + BINDING_ID + ":"
                + URLEncoder.encode(THING_TYPE_ACCOUNT.getId(), "UTF8") + ":" + URLEncoder.encode(id, "UTF8") + "'>");
        html.append(StringEscapeUtils.escapeHtml("Check Thing in Paper UI"));
        html.append("</a><br><br>");

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
        returnHtml(resp, "<html>" + StringEscapeUtils.escapeHtml(connection.getDeviceListJson()) + "</html>");
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
                + StringEscapeUtils
                        .escapeHtml(BINDING_NAME + " - " + this.account.getThing().getLabel() + " - " + title)
                + "</title><head><body>");
        html.append("<h1>" + StringEscapeUtils
                .escapeHtml(BINDING_NAME + " - " + this.account.getThing().getLabel() + " - " + title) + "</h1>");
        return html;
    }

    private void createPageEndAndSent(HttpServletResponse resp, StringBuilder html) {
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

    void HandleProxyRequest(HttpServletResponse resp, String verb, String url, @Nullable String referer,
            @Nullable String postData) throws IOException {
        HttpsURLConnection urlConnection;
        try {
            Map<String, String> headers = null;
            if (referer != null) {
                headers = new HashMap<String, String>();
                headers.put("Referer", referer);
            }

            urlConnection = connection.makeRequest(verb, url, postData, false, false, headers);
            if (urlConnection.getResponseCode() == 302) {
                {
                    String location = urlConnection.getHeaderField("location");
                    if (location.contains("//alexa.")) {
                        if (connection.verifyLogin()) {
                            handleDefaultPageResult(resp, "Login succeeded");
                            account.setConnection(this.connection);
                            this.connection = reCreateConnection();
                            return;
                        }
                    }
                    String startString = "https://www." + connection.getAmazonSite() + "/";
                    String newLocation = null;
                    if (location.startsWith(startString)) {
                        newLocation = servletUrl + FORWARD_URI_PART + location.substring(startString.length());
                    } else {
                        startString = "/";
                        if (location.startsWith(startString)) {
                            newLocation = servletUrl + FORWARD_URI_PART + location.substring(startString.length());
                        }
                    }
                    if (newLocation != null) {
                        logger.debug("Redirect mapped from {} to {}", location, newLocation);
                        resp.addHeader("location", newLocation);
                        resp.sendError(302);
                        return;
                    }
                    returnError(resp, "Invalid redirect to '" + location + "'");
                    return;
                }
            }
        } catch (URISyntaxException e) {
            returnError(resp, e.getLocalizedMessage());
            return;
        }
        String response = connection.convertStream(urlConnection.getInputStream());
        returnHtml(resp, response);
    }

    private void returnHtml(HttpServletResponse resp, String html) {
        String resultHtml = html.replace("https://www." + connection.getAmazonSite() + "/", servletUrl + "/");
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
