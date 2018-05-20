/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.amazonechocontrol.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonAutomation;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonAutomation.Payload;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonAutomation.Trigger;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonBluetoothStates;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonDevices;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonDevices.Device;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonEnabledFeeds;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonFeed;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonMediaState;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonMusicProvider;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonNotificationRequest;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonNotificationResponse;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonNotificationSound;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonNotificationSounds;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPlaySearchPhraseOperationPayload;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPlayValidationResult;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPlayerState;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPlaylists;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonStartRoutineRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link Connection} is responsible for the connection to the amazon server and
 * handling of the commands
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class Connection {
    private final Logger logger = LoggerFactory.getLogger(Connection.class);

    private final CookieManager cookieManager = new CookieManager();
    private final String email;
    private final String password;
    private final String amazonSite;
    private final String alexaServer;
    private final String accountThingId;

    private @Nullable String sessionId;
    private @Nullable Date loginTime;
    private @Nullable Date verifyTime;

    private final Gson gson = new Gson();
    private final Gson gsonWithNullSerialization;

    public Connection(@Nullable String email, @Nullable String password, @Nullable String amazonSite,
            @Nullable String accountThingId) {
        this.accountThingId = accountThingId != null ? accountThingId : "";
        this.email = email != null ? email : "";
        this.password = password != null ? password : "";

        String correctedAmazonSite = amazonSite != null ? amazonSite : "";
        if (correctedAmazonSite.toLowerCase().startsWith("http://")) {
            correctedAmazonSite = correctedAmazonSite.substring(7);
        }
        if (correctedAmazonSite.toLowerCase().startsWith("https://")) {
            correctedAmazonSite = correctedAmazonSite.substring(8);
        }
        if (correctedAmazonSite.toLowerCase().startsWith("www.")) {
            correctedAmazonSite = correctedAmazonSite.substring(4);
        }
        if (correctedAmazonSite.toLowerCase().startsWith("alexa.")) {
            correctedAmazonSite = correctedAmazonSite.substring(6);
        }
        this.amazonSite = correctedAmazonSite;
        alexaServer = "https://alexa." + this.amazonSite;

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonWithNullSerialization = gsonBuilder.create();
    }

    public @Nullable Date tryGetLoginTime() {
        return loginTime;
    }

    public @Nullable Date tryGetVerifyTime() {
        return verifyTime;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getAmazonSite() {
        return amazonSite;
    }

    public String serializeLoginData() {
        Date loginTime = this.loginTime;
        if (sessionId == null || loginTime == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("4\n"); // version
        builder.append(email);
        builder.append("\n");
        builder.append(password.hashCode());
        builder.append("\n");
        builder.append(sessionId);
        builder.append("\n");
        builder.append(loginTime.getTime());
        builder.append("\n");
        List<HttpCookie> cookies = cookieManager.getCookieStore().getCookies();
        builder.append(cookies.size());
        builder.append("\n");
        for (HttpCookie cookie : cookies) {
            writeValue(builder, cookie.getName());
            writeValue(builder, cookie.getValue());
            writeValue(builder, cookie.getComment());
            writeValue(builder, cookie.getCommentURL());
            writeValue(builder, cookie.getDomain());
            writeValue(builder, cookie.getMaxAge());
            writeValue(builder, cookie.getPath());
            writeValue(builder, cookie.getPortlist());
            writeValue(builder, cookie.getVersion());
            writeValue(builder, cookie.getSecure());
            writeValue(builder, cookie.getDiscard());
        }
        return builder.toString();
    }

    private void writeValue(StringBuilder builder, @Nullable Object value) {
        if (value == null) {
            builder.append('0');
        } else {
            builder.append('1');
            builder.append("\n");
            builder.append(value.toString());
        }
        builder.append("\n");
    }

    private String readValue(Scanner scanner) {
        if (scanner.nextLine().equals("1")) {
            String result = scanner.nextLine();
            if (result != null) {
                return result;
            }
        }
        return "";
    }

    public boolean tryRestoreLogin(@Nullable String data) {
        // verify store data
        if (StringUtils.isEmpty(data)) {
            return false;
        }
        Scanner scanner = new Scanner(data);
        String version = scanner.nextLine();
        if (!version.equals("4")) {
            scanner.close();
            return false;
        }

        // check if email or password was changed in the mean time
        String email = scanner.nextLine();
        if (!email.equals(this.email)) {
            scanner.close();
            return false;
        }
        int passwordHash = Integer.parseInt(scanner.nextLine());
        if (passwordHash != this.password.hashCode()) {
            scanner.close();
            return false;
        }

        // Recreate session and cookies
        sessionId = scanner.nextLine();
        Date loginTime = new Date(Long.parseLong(scanner.nextLine()));
        CookieStore cookieStore = cookieManager.getCookieStore();
        cookieStore.removeAll();

        Integer numberOfCookies = Integer.parseInt(scanner.nextLine());
        for (Integer i = 0; i < numberOfCookies; i++) {
            String name = readValue(scanner);
            String value = readValue(scanner);

            HttpCookie clientCookie = new HttpCookie(name, value);
            clientCookie.setComment(readValue(scanner));
            clientCookie.setCommentURL(readValue(scanner));
            clientCookie.setDomain(readValue(scanner));
            clientCookie.setMaxAge(Long.parseLong(readValue(scanner)));
            clientCookie.setPath(readValue(scanner));
            clientCookie.setPortlist(readValue(scanner));
            clientCookie.setVersion(Integer.parseInt(readValue(scanner)));
            clientCookie.setSecure(Boolean.parseBoolean(readValue(scanner)));
            clientCookie.setDiscard(Boolean.parseBoolean(readValue(scanner)));

            cookieStore.add(null, clientCookie);
        }

        scanner.close();
        try {
            if (verifyLogin()) {
                this.loginTime = loginTime;
                return true;
            }
        } catch (IOException e) {
            logger.info("verify login fails with io exception: {}", e);
        } catch (URISyntaxException e) {
            logger.warn("verify login fails with uri syntax exception: {}", e);
        }
        // anything goes wrong, remove session data
        cookieStore.removeAll();
        this.sessionId = null;
        this.loginTime = null;
        this.verifyTime = null;
        return false;
    }

    public String convertStream(@Nullable InputStream input) throws IOException {
        if (input == null) {
            return "";
        }
        Scanner inputScanner = new Scanner(input);
        Scanner scannerWithoutDelimiter = inputScanner.useDelimiter("\\A");
        String result = scannerWithoutDelimiter.hasNext() ? scannerWithoutDelimiter.next() : null;
        inputScanner.close();
        scannerWithoutDelimiter.close();
        input.close();
        if (result == null) {
            result = "";
        }
        return result;
    }

    public String makeRequestAndReturnString(String url) throws IOException, URISyntaxException {
        return makeRequestAndReturnString("GET", url, null, false, null);
    }

    private String makeRequestAndReturnString(String verb, String url, @Nullable String postData, boolean json,
            @Nullable Map<String, String> customHeaders) throws IOException, URISyntaxException {
        HttpsURLConnection connection = makeRequest(verb, url, postData, json, true, customHeaders);
        return convertStream(connection.getInputStream());
    }

    public HttpsURLConnection makeRequest(String verb, String url, @Nullable String postData, boolean json,
            boolean autoredirect, @Nullable Map<String, String> customHeaders) throws IOException, URISyntaxException {
        String currentUrl = url;
        for (int i = 0; i < 30; i++) // loop for handling redirect, using automatic redirect is not possible, because
                                     // all response headers must be catched
        {
            int code;
            HttpsURLConnection connection;
            try {
                logger.debug("Make request to {}", url);
                connection = (HttpsURLConnection) new URL(currentUrl).openConnection();
                connection.setRequestMethod(verb);
                connection.setRequestProperty("Accept-Language", "en-US");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 openHAB/1.0.0.0");
                connection.setRequestProperty("DNT", "1");
                connection.setRequestProperty("Upgrade-Insecure-Requests", "1");
                if (customHeaders != null) {
                    for (String key : customHeaders.keySet()) {
                        String value = customHeaders.get(key);
                        connection.setRequestProperty(key, value);
                    }
                }
                connection.setInstanceFollowRedirects(false);

                // add cookies
                URI uri = connection.getURL().toURI();

                StringBuilder cookieHeaderBuilder = new StringBuilder();
                for (HttpCookie cookie : cookieManager.getCookieStore().get(uri)) {
                    if (cookieHeaderBuilder.length() > 0) {
                        cookieHeaderBuilder.insert(0, "; ");
                    }
                    cookieHeaderBuilder.insert(0, cookie);
                    if (cookie.getName().equals("csrf")) {
                        connection.setRequestProperty("csrf", cookie.getValue());
                    }

                }
                if (cookieHeaderBuilder.length() > 0) {
                    connection.setRequestProperty("Cookie", cookieHeaderBuilder.toString());
                }
                if (postData != null) {

                    // post data
                    byte[] postDataBytes = postData.getBytes(StandardCharsets.UTF_8);
                    int postDataLength = postDataBytes.length;

                    connection.setFixedLengthStreamingMode(postDataLength);

                    if (json) {
                        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    } else {
                        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    }
                    connection.setRequestProperty("Content-Length", Integer.toString(postDataLength));
                    if (verb == "POST") {
                        connection.setRequestProperty("Expect", "100-continue");
                    }

                    connection.setDoOutput(true);
                    OutputStream outputStream = connection.getOutputStream();
                    outputStream.write(postDataBytes);
                    outputStream.close();
                }
                // handle result
                code = connection.getResponseCode();
                String location = null;

                // handle response headers
                Map<String, List<String>> headerFields = connection.getHeaderFields();
                for (Map.Entry<String, List<String>> header : headerFields.entrySet()) {
                    String key = header.getKey();
                    if (StringUtils.isNotEmpty(key)) {
                        if (key.equalsIgnoreCase("Set-Cookie")) {
                            // store cookie
                            for (String cookieHeader : header.getValue()) {
                                List<HttpCookie> cookies = HttpCookie.parse(cookieHeader);
                                for (HttpCookie cookie : cookies) {
                                    cookieManager.getCookieStore().add(uri, cookie);
                                }
                            }
                        }
                        if (key.equalsIgnoreCase("Location")) {
                            // get redirect location
                            location = header.getValue().get(0);
                            if (StringUtils.isNotEmpty(location)) {
                                location = uri.resolve(location).toString();
                                // check for https
                                if (location.toLowerCase().startsWith("http://")) {
                                    // always use https
                                    location = "https://" + location.substring(7);
                                    logger.debug("Redirect corrected to {}", location);
                                }
                            }
                        }
                    }
                }
                if (code == 200) {
                    logger.debug("Call to {} succeeded", url);
                    return connection;
                }
                if (code == 302 && location != null) {
                    logger.debug("Redirected to {}", location);
                    currentUrl = location;
                    if (autoredirect) {
                        continue;
                    }
                    return connection;
                }
            } catch (IOException e) {
                logger.warn("Request to url '{}' fails with unkown error", url, e);
                throw e;
            }
            if (code != 200) {
                throw new HttpException(code, verb + " url '" + url + "' failed: " + connection.getResponseMessage());
            }
        }
        throw new ConnectionException("Too many redirects");
    }

    public boolean getIsLoggedIn() {
        return loginTime != null;
    }

    public String getLoginPage() throws IOException, URISyntaxException {
        // clear session data
        cookieManager.getCookieStore().removeAll();
        sessionId = null;
        loginTime = null;
        verifyTime = null;

        logger.debug("Start Login to {}", alexaServer);
        // get login form
        String loginFormHtml = makeRequestAndReturnString(alexaServer);

        logger.debug("Received login form {}", loginFormHtml);

        // get session id from cookies
        for (HttpCookie cookie : cookieManager.getCookieStore().getCookies()) {
            if (cookie.getName().equalsIgnoreCase("session-id")) {
                sessionId = cookie.getValue();
                break;
            }
        }
        if (sessionId == null) {
            throw new ConnectionException("No session id received");
        }
        cookieManager.getCookieStore().add(new URL("https://www." + amazonSite).toURI(),
                HttpCookie.parse("session-id=" + sessionId).get(0));
        return loginFormHtml;
    }

    public void makeLogin() throws IOException, URISyntaxException {
        try {
            String loginFormHtml = getLoginPage();
            // read hidden form inputs, the will be used later in the url and for posting
            Pattern inputPattern = Pattern
                    .compile("<input\\s+type=\"hidden\"\\s+name=\"(?<name>[^\"]+)\"\\s+value=\"(?<value>[^\"]*)\"");
            Matcher matcher = inputPattern.matcher(loginFormHtml);

            StringBuilder postDataBuilder = new StringBuilder();
            while (matcher.find()) {

                postDataBuilder.append(URLEncoder.encode(matcher.group("name"), "UTF-8"));
                postDataBuilder.append('=');
                postDataBuilder.append(URLEncoder.encode(matcher.group("value"), "UTF-8"));
                postDataBuilder.append('&');
            }

            String queryParameters = postDataBuilder.toString() + "session-id=" + URLEncoder.encode(sessionId, "UTF-8");
            logger.debug("Login query String: {}", queryParameters);

            postDataBuilder.append("email");
            postDataBuilder.append('=');
            postDataBuilder.append(URLEncoder.encode(email, "UTF-8"));
            postDataBuilder.append('&');
            postDataBuilder.append("password");
            postDataBuilder.append('=');
            postDataBuilder.append(URLEncoder.encode(password, "UTF-8"));

            String postData = postDataBuilder.toString();

            String response = postLoginData(queryParameters, postData);
            if (response != null) {
                Document htmlDocument = Jsoup.parse(response);
                Element authWarningBoxElement = htmlDocument.getElementById("auth-warning-message-box");
                String error = null;
                if (authWarningBoxElement != null) {
                    error = authWarningBoxElement.text();
                }
                if (StringUtils.isNotEmpty(error)) {
                    throw new ConnectionException(
                            "Login fails. Check your credentials and try to login with your webbrowser to http(s)://<youropenhab:yourport>/amazonechocontrol/"
                                    + accountThingId + System.lineSeparator() + "" + error);
                } else {
                    throw new ConnectionException(
                            "Login fails. Check your credentials and try to login with your webbrowser to http(s)://<youropenhab:yourport>/amazonechocontrol/"
                                    + accountThingId);
                }
            }

        } catch (Exception e) {
            // clear session data
            cookieManager.getCookieStore().removeAll();
            sessionId = null;
            loginTime = null;
            verifyTime = null;
            logger.info("Login failed: {}", e.getLocalizedMessage());
            // rethrow
            throw e;
        }
    }

    public @Nullable String postLoginData(@Nullable String optionalQueryParameters, @Nullable String postData)
            throws IOException, URISyntaxException {
        // build query parameters
        @Nullable
        String queryParameters = optionalQueryParameters;
        if (queryParameters == null) {
            queryParameters = "session-id=" + URLEncoder.encode(sessionId, "UTF-8");
        }

        // build referer link
        String referer = "https://www." + amazonSite + "/ap/signin?" + queryParameters;
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Referer", referer);

        // make the request
        URLConnection request = makeRequest("POST", "https://www." + amazonSite + "/ap/signin", postData, false, true,
                headers);

        String response = convertStream(request.getInputStream());
        logger.debug("Received content after login {}", response);

        String host = request.getURL().getHost();
        if (!host.equalsIgnoreCase(new URI(alexaServer).getHost())) {
            return response;
        }
        if (response.contains("<title>Amazon Alexa</title>")) {
            logger.debug("Response seems to be alexa app");
        } else {

            logger.info("Response maybe not valid");
        }

        // verify login
        if (!verifyLogin()) {
            return response;
        }
        logger.debug("Login succeeded");
        return null;
    }

    public boolean verifyLogin() throws IOException, URISyntaxException {
        String response = makeRequestAndReturnString(alexaServer + "/api/bootstrap?version=0");
        Boolean result = response.contains("\"authenticated\":true");
        if (result) {
            verifyTime = new Date();
            if (loginTime == null) {
                loginTime = verifyTime;
            }
        }
        return result;
    }

    public void logout() {
        cookieManager.getCookieStore().removeAll();
        sessionId = null;
        loginTime = null;
        verifyTime = null;
    }

    // parser
    private <T> T parseJson(String json, Class<T> type) throws JsonSyntaxException {
        try {
            return gson.fromJson(json, type);
        } catch (JsonSyntaxException e) {
            logger.warn("Parsing json failed {}", e);
            logger.warn("Illegal json: {}", json);
            throw e;
        }
    }

    // commands and states

    public List<Device> getDeviceList() throws IOException, URISyntaxException {
        String json = getDeviceListJson();
        JsonDevices devices = parseJson(json, JsonDevices.class);
        Device[] result = devices.devices;
        if (result == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(result));
    }

    public String getDeviceListJson() throws IOException, URISyntaxException {
        String json = makeRequestAndReturnString(alexaServer + "/api/devices-v2/device?cached=false");
        return json;
    }

    public JsonPlayerState getPlayer(Device device) throws IOException, URISyntaxException {
        String json = makeRequestAndReturnString(alexaServer + "/api/np/player?deviceSerialNumber="
                + device.serialNumber + "&deviceType=" + device.deviceType + "&screenWidth=1440");
        JsonPlayerState playerState = parseJson(json, JsonPlayerState.class);
        return playerState;
    }

    public JsonMediaState getMediaState(Device device) throws IOException, URISyntaxException {
        String json = makeRequestAndReturnString(alexaServer + "/api/media/state?deviceSerialNumber="
                + device.serialNumber + "&deviceType=" + device.deviceType);
        JsonMediaState mediaState = parseJson(json, JsonMediaState.class);
        return mediaState;
    }

    public JsonBluetoothStates getBluetoothConnectionStates() {
        String json;
        try {
            json = makeRequestAndReturnString(alexaServer + "/api/bluetooth?cached=true");
        } catch (IOException | URISyntaxException e) {
            logger.debug("failed to get bluetooth state: {}", e.getMessage());
            return new JsonBluetoothStates();
        }
        JsonBluetoothStates bluetoothStates = parseJson(json, JsonBluetoothStates.class);
        return bluetoothStates;
    }

    public JsonPlaylists getPlaylists(Device device) throws IOException, URISyntaxException {
        String json = makeRequestAndReturnString(
                alexaServer + "/api/cloudplayer/playlists?deviceSerialNumber=" + device.serialNumber + "&deviceType="
                        + device.deviceType + "&mediaOwnerCustomerId=" + device.deviceOwnerCustomerId);
        JsonPlaylists playlists = parseJson(json, JsonPlaylists.class);
        return playlists;
    }

    public void command(Device device, String command) throws IOException, URISyntaxException {
        String url = alexaServer + "/api/np/command?deviceSerialNumber=" + device.serialNumber + "&deviceType="
                + device.deviceType;
        makeRequest("POST", url, command, true, true, null);
    }

    public void bluetooth(Device device, @Nullable String address) throws IOException, URISyntaxException {
        if (StringUtils.isEmpty(address)) {
            // disconnect
            makeRequest("POST",
                    alexaServer + "/api/bluetooth/disconnect-sink/" + device.deviceType + "/" + device.serialNumber, "",
                    true, true, null);
        } else {
            makeRequest("POST",
                    alexaServer + "/api/bluetooth/pair-sink/" + device.deviceType + "/" + device.serialNumber,
                    "{\"bluetoothDeviceAddress\":\"" + address + "\"}", true, true, null);
        }
    }

    public void playRadio(Device device, @Nullable String stationId) throws IOException, URISyntaxException {
        if (StringUtils.isEmpty(stationId)) {
            command(device, "{\"type\":\"PauseCommand\"}");
        } else {
            makeRequest("POST",
                    alexaServer + "/api/tunein/queue-and-play?deviceSerialNumber=" + device.serialNumber
                            + "&deviceType=" + device.deviceType + "&guideId=" + stationId
                            + "&contentType=station&callSign=&mediaOwnerCustomerId=" + device.deviceOwnerCustomerId,
                    "", true, true, null);
        }
    }

    public void playAmazonMusicTrack(Device device, @Nullable String trackId) throws IOException, URISyntaxException {
        if (StringUtils.isEmpty(trackId)) {
            command(device, "{\"type\":\"PauseCommand\"}");
        } else {
            String command = "{\"trackId\":\"" + trackId + "\",\"playQueuePrime\":true}";
            makeRequest("POST",
                    alexaServer + "/api/cloudplayer/queue-and-play?deviceSerialNumber=" + device.serialNumber
                            + "&deviceType=" + device.deviceType + "&mediaOwnerCustomerId="
                            + device.deviceOwnerCustomerId + "&shuffle=false",
                    command, true, true, null);
        }
    }

    public void playAmazonMusicPlayList(Device device, @Nullable String playListId)
            throws IOException, URISyntaxException {
        if (StringUtils.isEmpty(playListId)) {
            command(device, "{\"type\":\"PauseCommand\"}");
        } else {
            String command = "{\"playlistId\":\"" + playListId + "\",\"playQueuePrime\":true}";
            makeRequest("POST",
                    alexaServer + "/api/cloudplayer/queue-and-play?deviceSerialNumber=" + device.serialNumber
                            + "&deviceType=" + device.deviceType + "&mediaOwnerCustomerId="
                            + device.deviceOwnerCustomerId + "&shuffle=false",
                    command, true, true, null);
        }
    }

    public void textToSpeech(Device device, String text) throws IOException, URISyntaxException {
        Map<String, Object> parameters = new Hashtable<String, Object>();
        parameters.put("textToSpeak", text);
        executeSequenceCommand(device, "Alexa.Speak", parameters);
    }

    // commands: Alexa.Weather.Play, Alexa.Traffic.Play, Alexa.FlashBriefing.Play, Alexa.GoodMorning.Play,
    // Alexa.SingASong.Play, Alexa.TellStory.Play, Alexa.Speak (textToSpeach)
    public void executeSequenceCommand(Device device, String command, @Nullable Map<String, Object> parameters)
            throws IOException, URISyntaxException {
        JsonObject operationPayload = new JsonObject();
        operationPayload.addProperty("deviceType", device.deviceType);
        operationPayload.addProperty("deviceSerialNumber", device.serialNumber);
        operationPayload.addProperty("locale", "");
        operationPayload.addProperty("customerId", device.deviceOwnerCustomerId);
        if (parameters != null) {
            for (String key : parameters.keySet()) {
                Object value = parameters.get(key);
                if (value instanceof String) {
                    operationPayload.addProperty(key, (String) value);
                } else if (value instanceof Number) {
                    operationPayload.addProperty(key, (Number) value);
                } else if (value instanceof Boolean) {
                    operationPayload.addProperty(key, (Boolean) value);
                } else if (value instanceof Character) {
                    operationPayload.addProperty(key, (Character) value);
                } else {
                    operationPayload.add(key, gson.toJsonTree(value));
                }
            }
        }

        JsonObject startNode = new JsonObject();
        startNode.addProperty("@type", "com.amazon.alexa.behaviors.model.OpaquePayloadOperationNode");
        startNode.addProperty("type", command);
        startNode.add("operationPayload", operationPayload);

        JsonObject sequenceJson = new JsonObject();
        sequenceJson.addProperty("@type", "com.amazon.alexa.behaviors.model.Sequence");
        sequenceJson.add("startNode", startNode);

        JsonStartRoutineRequest request = new JsonStartRoutineRequest();
        request.sequenceJson = gson.toJson(sequenceJson);
        String json = gson.toJson(request);

        makeRequest("POST", alexaServer + "/api/behaviors/preview", json, true, true, null);
    }

    public void startRoutine(Device device, String utterance) throws IOException, URISyntaxException {
        JsonAutomation found = null;
        String deviceLocale = "";
        for (JsonAutomation routine : getRoutines()) {
            Trigger[] triggers = routine.triggers;
            if (triggers != null && routine.sequence != null) {
                for (JsonAutomation.Trigger trigger : triggers) {
                    if (trigger == null) {
                        continue;
                    }
                    Payload payload = trigger.payload;
                    if (payload == null) {
                        continue;
                    }
                    if (StringUtils.equalsIgnoreCase(payload.utterance, utterance)) {
                        found = routine;
                        deviceLocale = payload.locale;
                        break;
                    }
                }
            }
        }
        if (found != null) {
            String sequenceJson = gson.toJson(found.sequence);

            JsonStartRoutineRequest request = new JsonStartRoutineRequest();
            request.behaviorId = found.automationId;

            // replace tokens

            // "deviceType":"ALEXA_CURRENT_DEVICE_TYPE"
            String deviceType = "\"deviceType\":\"ALEXA_CURRENT_DEVICE_TYPE\"";
            String newDeviceType = "\"deviceType\":\"" + device.deviceType + "\"";
            sequenceJson = sequenceJson.replace(deviceType.subSequence(0, deviceType.length()),
                    newDeviceType.subSequence(0, newDeviceType.length()));

            // "deviceSerialNumber":"ALEXA_CURRENT_DSN"
            String deviceSerial = "\"deviceSerialNumber\":\"ALEXA_CURRENT_DSN\"";
            String newDeviceSerial = "\"deviceSerialNumber\":\"" + device.serialNumber + "\"";
            sequenceJson = sequenceJson.replace(deviceSerial.subSequence(0, deviceSerial.length()),
                    newDeviceSerial.subSequence(0, newDeviceSerial.length()));

            // "customerId": "ALEXA_CUSTOMER_ID"
            String customerId = "\"customerId\":\"ALEXA_CUSTOMER_ID\"";
            String newCustomerId = "\"customerId\":\"" + device.deviceOwnerCustomerId + "\"";
            sequenceJson = sequenceJson.replace(customerId.subSequence(0, customerId.length()),
                    newCustomerId.subSequence(0, newCustomerId.length()));

            // "locale": "ALEXA_CURRENT_LOCALE"
            String locale = "\"locale\":\"ALEXA_CURRENT_LOCALE\"";
            String newlocale = StringUtils.isNotEmpty(deviceLocale) ? "\"locale\":\"" + deviceLocale + "\""
                    : "\"locale\":null";
            sequenceJson = sequenceJson.replace(locale.subSequence(0, locale.length()),
                    newlocale.subSequence(0, newlocale.length()));

            request.sequenceJson = sequenceJson;

            String requestJson = gson.toJson(request);
            makeRequest("POST", alexaServer + "/api/behaviors/preview", requestJson, true, true, null);
        } else {
            logger.warn("Routine {} not found", utterance);
        }
    }

    public JsonAutomation[] getRoutines() throws IOException, URISyntaxException {
        String json = makeRequestAndReturnString(alexaServer + "/api/behaviors/automations");
        JsonAutomation[] result = parseJson(json, JsonAutomation[].class);
        return result;
    }

    public JsonFeed[] getEnabledFlashBriefings() throws IOException, URISyntaxException {
        String json = makeRequestAndReturnString(alexaServer + "/api/content-skills/enabled-feeds");
        JsonEnabledFeeds result = parseJson(json, JsonEnabledFeeds.class);
        JsonFeed[] enabledFeeds = result.enabledFeeds;
        if (enabledFeeds != null) {
            return enabledFeeds;
        }
        return new JsonFeed[0];
    }

    public void setEnabledFlashBriefings(JsonFeed[] enabledFlashBriefing) throws IOException, URISyntaxException {
        JsonEnabledFeeds enabled = new JsonEnabledFeeds();
        enabled.enabledFeeds = enabledFlashBriefing;
        String json = gsonWithNullSerialization.toJson(enabled);
        makeRequest("POST", alexaServer + "/api/content-skills/enabled-feeds", json, true, true, null);
    }

    public JsonNotificationSound[] getNotificationSounds(Device device) throws IOException, URISyntaxException {
        String json = makeRequestAndReturnString(
                alexaServer + "/api/notification/sounds?deviceSerialNumber=" + device.serialNumber + "&deviceType="
                        + device.deviceType + "&softwareVersion=" + device.softwareVersion);
        JsonNotificationSounds result = parseJson(json, JsonNotificationSounds.class);
        JsonNotificationSound[] notificationSounds = result.notificationSounds;
        if (notificationSounds != null) {
            return notificationSounds;
        }
        return new JsonNotificationSound[0];
    }

    public JsonNotificationResponse notification(Device device, String type, @Nullable String label,
            @Nullable JsonNotificationSound sound) throws IOException, URISyntaxException {
        Date date = new Date(new Date().getTime());
        long createdDate = date.getTime();
        Date alarm = new Date(createdDate + 5000); // add 5 seconds, because amazon does not except calls for times in
                                                   // the past (compared with the server time)
        long alarmTime = alarm.getTime();

        JsonNotificationRequest request = new JsonNotificationRequest();
        request.type = type;
        request.deviceSerialNumber = device.serialNumber;
        request.deviceType = device.deviceType;
        request.createdDate = createdDate;
        request.alarmTime = alarmTime;
        request.reminderLabel = label;
        request.sound = sound;
        request.originalDate = new SimpleDateFormat("yyyy-MM-dd").format(alarm);
        request.originalTime = new SimpleDateFormat("HH:mm:ss.SSSS").format(alarm);
        request.type = type;
        request.id = "create" + type;

        String data = gsonWithNullSerialization.toJson(request);
        String response = makeRequestAndReturnString("PUT", alexaServer + "/api/notifications/createReminder", data,
                true, null);
        JsonNotificationResponse result = parseJson(response, JsonNotificationResponse.class);
        return result;
    }

    public void stopNotification(JsonNotificationResponse notification) throws IOException, URISyntaxException {
        makeRequestAndReturnString("DELETE", alexaServer + "/api/notifications/" + notification.id, null, true, null);
    }

    public JsonNotificationResponse getNotificationState(JsonNotificationResponse notification)
            throws IOException, URISyntaxException {
        String response = makeRequestAndReturnString("GET", alexaServer + "/api/notifications/" + notification.id, null,
                true, null);
        JsonNotificationResponse result = parseJson(response, JsonNotificationResponse.class);
        return result;
    }

    public List<JsonMusicProvider> getMusicProviders() {
        String response;
        try {
            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Routines-Version", "1.1.201102");
            response = makeRequestAndReturnString("GET",
                    alexaServer + "/api/behaviors/entities?skillId=amzn1.ask.1p.music", null, true, headers);
        } catch (IOException | URISyntaxException e) {
            logger.warn("getMusicProviders fails: {}", e.getMessage());
            return new ArrayList<>();
        }
        if (StringUtils.isEmpty(response)) {
            return new ArrayList<>();
        }
        JsonMusicProvider[] result = parseJson(response, JsonMusicProvider[].class);
        return Arrays.asList(result);
    }

    public void playMusicVoiceCommand(Device device, String providerId, String voiceCommand)
            throws IOException, URISyntaxException {
        JsonPlaySearchPhraseOperationPayload payload = new JsonPlaySearchPhraseOperationPayload();
        payload.customerId = device.deviceOwnerCustomerId;
        payload.locale = "ALEXA_CURRENT_LOCALE";
        payload.musicProviderId = providerId;
        payload.searchPhrase = voiceCommand;

        String playloadString = gson.toJson(payload);

        JsonObject postValidataionJson = new JsonObject();

        postValidataionJson.addProperty("type", "Alexa.Music.PlaySearchPhrase");
        postValidataionJson.addProperty("operationPayload", playloadString);

        String postDataValidate = postValidataionJson.toString();

        String validateResultJson = makeRequestAndReturnString("POST",
                alexaServer + "/api/behaviors/operation/validate", postDataValidate, true, null);

        if (StringUtils.isNotEmpty(validateResultJson)) {
            JsonPlayValidationResult validationResult = parseJson(validateResultJson, JsonPlayValidationResult.class);
            JsonPlaySearchPhraseOperationPayload validatedOperationPayload = validationResult.operationPayload;
            if (validatedOperationPayload != null) {
                payload.sanitizedSearchPhrase = validatedOperationPayload.sanitizedSearchPhrase;
                payload.searchPhrase = validatedOperationPayload.searchPhrase;
            }
        }

        payload.locale = null;
        payload.deviceSerialNumber = device.serialNumber;
        payload.deviceType = device.deviceType;

        JsonObject sequenceJson = new JsonObject();
        sequenceJson.addProperty("@type", "com.amazon.alexa.behaviors.model.Sequence");
        JsonObject startNodeJson = new JsonObject();
        startNodeJson.addProperty("@type", "com.amazon.alexa.behaviors.model.OpaquePayloadOperationNode");
        startNodeJson.addProperty("type", "Alexa.Music.PlaySearchPhrase");
        startNodeJson.add("operationPayload", gson.toJsonTree(payload));
        sequenceJson.add("startNode", startNodeJson);

        JsonStartRoutineRequest startRoutineRequest = new JsonStartRoutineRequest();
        startRoutineRequest.sequenceJson = sequenceJson.toString();
        startRoutineRequest.status = null;

        String postData = gson.toJson(startRoutineRequest);
        makeRequest("POST", alexaServer + "/api/behaviors/preview", postData, true, true, null);
    }
}
