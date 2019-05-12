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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.util.HexUtils;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonActivities;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonActivities.Activity;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonAnnouncementContent;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonAnnouncementTarget;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonAnnouncementTarget.TargetDevice;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonAscendingAlarm;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonAscendingAlarm.AscendingAlarmModel;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonAutomation;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonAutomation.Payload;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonAutomation.Trigger;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonBluetoothStates;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonDeviceNotificationState;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonDeviceNotificationState.DeviceNotificationState;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonDevices;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonDevices.Device;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonEnabledFeeds;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonEqualizer;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonExchangeTokenResponse;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonExchangeTokenResponse.Cookie;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonFeed;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonMediaState;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonMusicProvider;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonNotificationRequest;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonNotificationResponse;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonNotificationSound;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonNotificationSounds;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonNotificationsResponse;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPlaySearchPhraseOperationPayload;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPlayValidationResult;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPlayerState;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPlaylists;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonRegisterAppRequest;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonRegisterAppResponse;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonRegisterAppResponse.Bearer;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonRegisterAppResponse.DeviceInfo;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonRegisterAppResponse.Extensions;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonRegisterAppResponse.Response;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonRegisterAppResponse.Success;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonRegisterAppResponse.Tokens;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonRenewTokenResponse;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonStartRoutineRequest;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonUsersMeResponse;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonWakeWords;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonWakeWords.WakeWord;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonWebSiteCookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link Connection} is responsible for the connection to the amazon server and
 * handling of the commands
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class Connection {
    private static final long expiresIn = 432000; // five days
    private static final Pattern charsetPattern = Pattern.compile("(?i)\\bcharset=\\s*\"?([^\\s;\"]*)");

    private final Logger logger = LoggerFactory.getLogger(Connection.class);

    private final CookieManager cookieManager = new CookieManager();
    private String amazonSite = "amazon.com";
    private String alexaServer = "https://alexa.amazon.com";
    private final String userAgent;
    private String frc;
    private String serial;
    private String deviceId;

    private @Nullable String refreshToken;
    private @Nullable Date loginTime;
    private @Nullable Date verifyTime;
    private long renewTime = 0;
    private @Nullable String deviceName;
    private @Nullable String accountCustomerId;

    private final Gson gson = new Gson();
    private final Gson gsonWithNullSerialization;

    public Connection(@Nullable Connection oldConnection) {
        String frc = null;
        String serial = null;
        String deviceId = null;
        if (oldConnection != null) {
            frc = oldConnection.getFrc();
            serial = oldConnection.getSerial();
            deviceId = oldConnection.getDeviceId();

        }
        Random rand = new Random();
        if (frc != null) {
            this.frc = frc;
        } else {
            // generate frc
            byte[] frcBinary = new byte[313];
            rand.nextBytes(frcBinary);
            this.frc = Base64.getEncoder().encodeToString(frcBinary);
        }
        if (serial != null) {
            this.serial = serial;
        } else {
            // generate serial
            byte[] serialBinary = new byte[16];
            rand.nextBytes(serialBinary);
            this.serial = HexUtils.bytesToHex(serialBinary);
        }
        if (deviceId != null) {
            this.deviceId = deviceId;
        } else {
            // generate device id
            StringBuilder deviceIdBuilder = new StringBuilder();
            for (int i = 0; i < 64; i++) {
                deviceIdBuilder.append(rand.nextInt(9));
            }
            deviceIdBuilder.append("23413249564c5635564d32573831");
            this.deviceId = deviceIdBuilder.toString();
        }

        // build user agent
        this.userAgent = "AmazonWebView/Amazon Alexa/2.2.223830.0/iOS/11.4.1/iPhone";

        // setAmazonSite(amazonSite);

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonWithNullSerialization = gsonBuilder.create();
    }

    private void setAmazonSite(@Nullable String amazonSite) {
        String correctedAmazonSite = amazonSite != null ? amazonSite : "amazon.com";
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
    }

    public @Nullable Date tryGetLoginTime() {
        return loginTime;
    }

    public @Nullable Date tryGetVerifyTime() {
        return verifyTime;
    }

    public String getFrc() {
        return frc;
    }

    public String getSerial() {
        return serial;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getAmazonSite() {
        return amazonSite;
    }

    public String getAlexaServer() {
        return alexaServer;
    }

    public String getDeviceName() {
        String deviceName = this.deviceName;
        if (deviceName == null) {
            return "Unknown";
        }
        return deviceName;
    }

    public String serializeLoginData() {
        Date loginTime = this.loginTime;
        if (refreshToken == null || loginTime == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("6\n"); // version
        builder.append(frc);
        builder.append("\n");
        builder.append(serial);
        builder.append("\n");
        builder.append(deviceId);
        builder.append("\n");
        builder.append(refreshToken);
        builder.append("\n");
        builder.append(amazonSite);
        builder.append("\n");
        builder.append(deviceName);
        builder.append("\n");
        builder.append(accountCustomerId);
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

    public boolean tryRestoreLogin(@Nullable String data, @Nullable String overloadedDomain) {
        Date loginTime = tryRestoreSessionData(data, overloadedDomain);
        if (loginTime != null) {
            try {
                if (verifyLogin()) {
                    this.loginTime = loginTime;
                    return true;
                }
            } catch (IOException e) {
                return false;
            } catch (URISyntaxException e) {

            }
        }
        return false;
    }

    private @Nullable Date tryRestoreSessionData(@Nullable String data, @Nullable String overloadedDomain) {
        // verify store data
        if (StringUtils.isEmpty(data)) {
            return null;
        }
        Scanner scanner = new Scanner(data);
        String version = scanner.nextLine();
        // check if serialize version
        if (!version.equals("5") && !version.equals("6")) {
            scanner.close();
            return null;
        }
        int intVersion = Integer.parseInt(version);

        frc = scanner.nextLine();
        serial = scanner.nextLine();
        deviceId = scanner.nextLine();

        // Recreate session and cookies
        refreshToken = scanner.nextLine();
        String domain = scanner.nextLine();
        if (overloadedDomain != null) {
            domain = overloadedDomain;
        }
        setAmazonSite(domain);

        deviceName = scanner.nextLine();

        if (intVersion > 5) {
            String accountCustomerId = scanner.nextLine();
            if (!StringUtils.equals(accountCustomerId, "null")) {
                this.accountCustomerId = accountCustomerId;
            }
        }

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
            checkRenewSession();

            if (StringUtils.isEmpty(this.accountCustomerId)) {
                List<Device> devices = this.getDeviceList();
                for (Device device : devices) {
                    if (StringUtils.equals(device.serialNumber, this.serial)) {
                        this.accountCustomerId = device.deviceOwnerCustomerId;
                        break;
                    }
                }
                if (StringUtils.isEmpty(this.accountCustomerId)) {
                    for (Device device : devices) {
                        if (StringUtils.equals(device.accountName, "This Device")) {
                            this.accountCustomerId = device.deviceOwnerCustomerId;
                            String serial = device.serialNumber;
                            if (serial != null) {
                                this.serial = serial;
                            }
                            break;
                        }
                    }
                }
            }
        } catch (URISyntaxException | IOException | ConnectionException e) {
            logger.debug("Getting account customer Id failed {}", e);
        }
        return loginTime;
    }

    public String convertStream(HttpsURLConnection connection) throws IOException {
        InputStream input = connection.getInputStream();
        if (input == null) {
            return "";
        }

        InputStream readerStream;
        if (StringUtils.equalsIgnoreCase(connection.getContentEncoding(), "gzip")) {
            readerStream = new GZIPInputStream(connection.getInputStream());
        } else {
            readerStream = input;
        }
        String contentType = connection.getContentType();
        String charSet = null;
        if (contentType != null) {
            Matcher m = charsetPattern.matcher(contentType);
            if (m.find()) {
                charSet = m.group(1).trim().toUpperCase();
            }
        }

        Scanner inputScanner = StringUtils.isEmpty(charSet) ? new Scanner(readerStream, StandardCharsets.UTF_8.name())
                : new Scanner(readerStream, charSet);
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

    public String makeRequestAndReturnString(String verb, String url, @Nullable String postData, boolean json,
            @Nullable Map<String, String> customHeaders) throws IOException, URISyntaxException {
        HttpsURLConnection connection = makeRequest(verb, url, postData, json, true, customHeaders);
        return convertStream(connection);
    }

    public HttpsURLConnection makeRequest(String verb, String url, @Nullable String postData, boolean json,
            boolean autoredirect, @Nullable Map<String, String> customHeaders) throws IOException, URISyntaxException {
        String currentUrl = url;
        for (int i = 0; i < 30; i++) // loop for handling redirect, using automatic redirect is not possible, because
                                     // all response headers must be catched
        {
            int code;
            HttpsURLConnection connection = null;
            try {
                logger.debug("Make request to {}", url);
                connection = (HttpsURLConnection) new URL(currentUrl).openConnection();
                connection.setRequestMethod(verb);
                connection.setRequestProperty("Accept-Language", "en-US");
                if (customHeaders == null || !customHeaders.containsKey("User-Agent")) {
                    connection.setRequestProperty("User-Agent", userAgent);
                }
                connection.setRequestProperty("Accept-Encoding", "gzip");
                connection.setRequestProperty("DNT", "1");
                connection.setRequestProperty("Upgrade-Insecure-Requests", "1");
                if (customHeaders != null) {
                    for (String key : customHeaders.keySet()) {
                        String value = customHeaders.get(key);
                        if (StringUtils.isNotEmpty(value)) {
                            connection.setRequestProperty(key, value);
                        }
                    }
                }
                connection.setInstanceFollowRedirects(false);

                // add cookies
                URI uri = connection.getURL().toURI();

                if (customHeaders == null || !customHeaders.containsKey("Cookie")) {

                    StringBuilder cookieHeaderBuilder = new StringBuilder();
                    for (HttpCookie cookie : cookieManager.getCookieStore().get(uri)) {
                        if (cookieHeaderBuilder.length() > 0) {
                            cookieHeaderBuilder.append(";");
                        }
                        cookieHeaderBuilder.append(cookie.getName());
                        cookieHeaderBuilder.append("=");
                        cookieHeaderBuilder.append(cookie.getValue());
                        if (cookie.getName().equals("csrf")) {
                            connection.setRequestProperty("csrf", cookie.getValue());
                        }

                    }
                    if (cookieHeaderBuilder.length() > 0) {
                        String cookies = cookieHeaderBuilder.toString();
                        connection.setRequestProperty("Cookie", cookies);
                    }
                }
                if (postData != null) {

                    logger.debug("{}: {}", verb, postData);
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
                                if (StringUtils.isNotEmpty(cookieHeader)) {

                                    List<HttpCookie> cookies = HttpCookie.parse(cookieHeader);
                                    for (HttpCookie cookie : cookies) {
                                        cookieManager.getCookieStore().add(uri, cookie);
                                    }
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

                if (connection != null) {
                    connection.disconnect();
                }
                logger.warn("Request to url '{}' fails with unkown error", url, e);
                throw e;
            } catch (Exception e) {
                if (connection != null) {
                    connection.disconnect();
                }
                throw e;
            }
            if (code != 200) {
                throw new HttpException(code, verb + " url '" + url + "' failed: " + connection.getResponseMessage());
            }
        }
        throw new ConnectionException("Too many redirects");
    }

    public String registerConnectionAsApp(String oAutRedirectUrl)
            throws ConnectionException, IOException, URISyntaxException {
        URI oAutRedirectUri = new URI(oAutRedirectUrl);

        Map<String, String> queryParameters = new LinkedHashMap<String, String>();
        String query = oAutRedirectUri.getQuery();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            queryParameters.put(URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8.name()),
                    URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8.name()));
        }
        String accessToken = queryParameters.get("openid.oa2.access_token");

        Map<String, String> cookieMap = new HashMap<String, String>();

        List<JsonWebSiteCookie> webSiteCookies = new ArrayList<JsonWebSiteCookie>();
        for (HttpCookie cookie : getSessionCookies("https://www.amazon.com")) {
            cookieMap.put(cookie.getName(), cookie.getValue());
            webSiteCookies.add(new JsonWebSiteCookie(cookie.getName(), cookie.getValue()));
        }

        JsonWebSiteCookie[] webSiteCookiesArray = new JsonWebSiteCookie[webSiteCookies.size()];
        webSiteCookiesArray = webSiteCookies.toArray(webSiteCookiesArray);

        JsonRegisterAppRequest registerAppRequest = new JsonRegisterAppRequest(serial, accessToken, frc,
                webSiteCookiesArray);
        String registerAppRequestJson = gson.toJson(registerAppRequest);

        HashMap<String, String> registerHeaders = new HashMap<String, String>();
        registerHeaders.put("x-amzn-identity-auth-domain", "api.amazon.com");

        String registerAppResultJson = makeRequestAndReturnString("POST", "https://api.amazon.com/auth/register",
                registerAppRequestJson, true, registerHeaders);
        JsonRegisterAppResponse registerAppResponse = parseJson(registerAppResultJson, JsonRegisterAppResponse.class);

        Response response = registerAppResponse.response;
        if (response == null) {
            throw new ConnectionException("Error: No response received from register application");
        }
        Success success = response.success;
        if (success == null) {
            throw new ConnectionException("Error: No success received from register application");
        }

        Tokens tokens = success.tokens;
        if (tokens == null) {
            throw new ConnectionException("Error: No tokens received from register application");
        }
        Bearer bearer = tokens.bearer;
        if (bearer == null) {
            throw new ConnectionException("Error: No bearer received from register application");
        }
        this.refreshToken = bearer.refresh_token;
        if (StringUtils.isEmpty(this.refreshToken)) {
            throw new ConnectionException("Error: No refresh token received");
        }
        String usersMeResponseJson = makeRequestAndReturnString("GET",
                "https://alexa.amazon.com/api/users/me?platform=ios&version=2.2.223830.0", null, false, null);
        JsonUsersMeResponse usersMeResponse = parseJson(usersMeResponseJson, JsonUsersMeResponse.class);

        URI uri = new URI(usersMeResponse.marketPlaceDomainName);
        String host = uri.getHost();
        setAmazonSite(host);
        try {
            exhangeToken();
        } catch (Exception e) {
            logout();
            throw e;
        }
        String deviceName = null;
        Extensions extensions = success.extensions;
        if (extensions != null) {
            DeviceInfo deviceInfo = extensions.device_info;
            if (deviceInfo != null) {
                deviceName = deviceInfo.device_name;
            }
        }
        if (deviceName == null) {
            deviceName = "Unknown";
        }
        this.deviceName = deviceName;
        return deviceName;
    }

    private void exhangeToken() throws IOException, URISyntaxException {

        this.renewTime = 0;
        String cookiesJson = "{\"cookies\":{\"." + getAmazonSite() + "\":[]}}";
        String cookiesBase64 = Base64.getEncoder().encodeToString(cookiesJson.getBytes());

        String exchangePostData = "di.os.name=iOS&app_version=2.2.223830.0&domain=." + getAmazonSite()
                + "&source_token=" + URLEncoder.encode(this.refreshToken, "UTF8")
                + "&requested_token_type=auth_cookies&source_token_type=refresh_token&di.hw.version=iPhone&di.sdk.version=6.10.0&cookies="
                + cookiesBase64 + "&app_name=Amazon%20Alexa&di.os.version=11.4.1";

        HashMap<String, String> exchangeTokenHeader = new HashMap<String, String>();
        exchangeTokenHeader.put("Cookie", "");

        String exchangeTokenJson = makeRequestAndReturnString("POST",
                "https://www." + getAmazonSite() + "/ap/exchangetoken", exchangePostData, false, exchangeTokenHeader);
        JsonExchangeTokenResponse exchangeTokenResponse = gson.fromJson(exchangeTokenJson,
                JsonExchangeTokenResponse.class);

        org.openhab.binding.amazonechocontrol.internal.jsons.JsonExchangeTokenResponse.Response response = exchangeTokenResponse.response;
        if (response != null) {
            org.openhab.binding.amazonechocontrol.internal.jsons.JsonExchangeTokenResponse.Tokens tokens = response.tokens;
            if (tokens != null) {
                @Nullable
                Map<String, Cookie[]> cookiesMap = tokens.cookies;
                if (cookiesMap != null) {
                    for (String domain : cookiesMap.keySet()) {
                        Cookie[] cookies = cookiesMap.get(domain);
                        for (Cookie cookie : cookies) {
                            if (cookie != null) {
                                HttpCookie httpCookie = new HttpCookie(cookie.Name, cookie.Value);
                                httpCookie.setPath(cookie.Path);
                                httpCookie.setDomain(domain);
                                Boolean secure = cookie.Secure;
                                if (secure != null) {
                                    httpCookie.setSecure(secure);
                                }
                                this.cookieManager.getCookieStore().add(null, httpCookie);
                            }
                        }
                    }
                }
            }
        }
        if (!verifyLogin()) {
            throw new ConnectionException("Verify login failed after token exchange");
        }
        this.renewTime = (long) (System.currentTimeMillis() + Connection.expiresIn * 1000d / 0.8d); // start renew at
    }

    public boolean checkRenewSession() throws UnknownHostException, URISyntaxException, IOException {
        if (System.currentTimeMillis() >= this.renewTime) {
            String renewTokenPostData = "app_name=Amazon%20Alexa&app_version=2.2.223830.0&di.sdk.version=6.10.0&source_token="
                    + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8.name())
                    + "&package_name=com.amazon.echo&di.hw.version=iPhone&platform=iOS&requested_token_type=access_token&source_token_type=refresh_token&di.os.name=iOS&di.os.version=11.4.1&current_version=6.10.0";
            String renewTokenRepsonseJson = makeRequestAndReturnString("POST", "https://api.amazon.com/auth/token",
                    renewTokenPostData, false, null);
            parseJson(renewTokenRepsonseJson, JsonRenewTokenResponse.class);

            exhangeToken();
            return true;
        }
        return false;
    }

    public boolean getIsLoggedIn() {
        return loginTime != null;
    }

    public String getLoginPage() throws IOException, URISyntaxException {
        // clear session data
        logout();

        logger.debug("Start Login to {}", alexaServer);

        String mapMdJson = "{\"device_user_dictionary\":[],\"device_registration_data\":{\"software_version\":\"1\"},\"app_identifier\":{\"app_version\":\"2.2.223830\",\"bundle_id\":\"com.amazon.echo\"}}";
        String mapMdCookie = Base64.getEncoder().encodeToString(mapMdJson.getBytes());

        cookieManager.getCookieStore().add(new URI("https://www.amazon.com"), new HttpCookie("map-md", mapMdCookie));
        cookieManager.getCookieStore().add(new URI("https://www.amazon.com"), new HttpCookie("frc", frc));

        String loginFormHtml = makeRequestAndReturnString("https://www.amazon.com"
                + "/ap/signin?openid.return_to=https://www.amazon.com/ap/maplanding&openid.assoc_handle=amzn_dp_project_dee_ios&openid.identity=http://specs.openid.net/auth/2.0/identifier_select&pageId=amzn_dp_project_dee_ios&accountStatusPolicy=P1&openid.claimed_id=http://specs.openid.net/auth/2.0/identifier_select&openid.mode=checkid_setup&openid.ns.oa2=http://www.amazon.com/ap/ext/oauth/2&openid.oa2.client_id=device:"
                + deviceId
                + "&openid.ns.pape=http://specs.openid.net/extensions/pape/1.0&openid.oa2.response_type=token&openid.ns=http://specs.openid.net/auth/2.0&openid.pape.max_auth_age=0&openid.oa2.scope=device_auth_access");

        logger.debug("Received login form {}", loginFormHtml);
        return loginFormHtml;
    }

    public boolean verifyLogin() throws IOException, URISyntaxException {
        if (this.refreshToken == null) {
            return false;
        }
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

    public List<HttpCookie> getSessionCookies() {
        try {
            return cookieManager.getCookieStore().get(new URI(alexaServer));
        } catch (URISyntaxException e) {
            return new ArrayList<HttpCookie>();
        }
    }

    public List<HttpCookie> getSessionCookies(String server) {
        try {
            return cookieManager.getCookieStore().get(new URI(server));
        } catch (URISyntaxException e) {
            return new ArrayList<HttpCookie>();
        }
    }

    public void logout() {
        cookieManager.getCookieStore().removeAll();
        // reset all members
        refreshToken = null;
        loginTime = null;
        verifyTime = null;
        deviceName = null;
    }

    // parser
    private <T> T parseJson(String json, Class<T> type) throws JsonSyntaxException {
        try {
            return gson.fromJson(json, type);
        } catch (JsonParseException | IllegalStateException e) {
            logger.warn("Parsing json failed {}", e);
            logger.warn("Illegal json: {}", json);
            throw e;
        }
    }

    // commands and states

    public WakeWord[] getWakeWords() {
        String json;
        try {
            json = makeRequestAndReturnString(alexaServer + "/api/wake-word?cached=true");
            JsonWakeWords wakeWords = parseJson(json, JsonWakeWords.class);
            WakeWord[] result = wakeWords.wakeWords;
            if (result != null) {
                return result;
            }
        } catch (IOException | URISyntaxException e) {
            logger.info("getting wakewords failed {}", e);
        }
        return new WakeWord[0];
    }

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

    public Activity[] getActivities(int number, @Nullable Long startTime) {
        String json;
        try {
            json = makeRequestAndReturnString(alexaServer + "/api/activities?startTime="
                    + (startTime != null ? startTime : "") + "&size=" + number + "&offset=1");
            JsonActivities activities = parseJson(json, JsonActivities.class);
            Activity[] activiesArray = activities.activities;
            if (activiesArray != null) {
                return activiesArray;
            }
        } catch (IOException | URISyntaxException e) {
            logger.info("getting activities failed {}", e);
        }
        return new Activity[0];
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
        String json = makeRequestAndReturnString(alexaServer + "/api/cloudplayer/playlists?deviceSerialNumber="
                + device.serialNumber + "&deviceType=" + device.deviceType + "&mediaOwnerCustomerId="
                + (StringUtils.isEmpty(this.accountCustomerId) ? device.deviceOwnerCustomerId
                        : this.accountCustomerId));
        JsonPlaylists playlists = parseJson(json, JsonPlaylists.class);
        return playlists;
    }

    public void command(Device device, String command) throws IOException, URISyntaxException {
        String url = alexaServer + "/api/np/command?deviceSerialNumber=" + device.serialNumber + "&deviceType="
                + device.deviceType;
        makeRequest("POST", url, command, true, true, null);
    }

    public void notificationVolume(Device device, int volume) throws IOException, URISyntaxException {
        String url = alexaServer + "/api/device-notification-state/" + device.deviceType + "/" + device.softwareVersion
                + "/" + device.serialNumber;
        String command = "{\"deviceSerialNumber\":\"" + device.serialNumber + "\",\"deviceType\":\"" + device.deviceType
                + "\",\"softwareVersion\":\"" + device.softwareVersion + "\",\"volumeLevel\":" + volume + "}";
        makeRequest("PUT", url, command, true, true, null);
    }

    public void ascendingAlarm(Device device, boolean ascendingAlarm) throws IOException, URISyntaxException {
        String url = alexaServer + "/api/ascending-alarm/" + device.serialNumber;
        String command = "{\"ascendingAlarmEnabled\":" + (ascendingAlarm ? "true" : "false")
                + ",\"deviceSerialNumber\":\"" + device.serialNumber + "\",\"deviceType\":\"" + device.deviceType
                + "\",\"deviceAccountId\":null}";
        makeRequest("PUT", url, command, true, true, null);
    }

    public DeviceNotificationState[] getDeviceNotificationStates() {
        String json;
        try {
            json = makeRequestAndReturnString(alexaServer + "/api/device-notification-state");
            JsonDeviceNotificationState result = parseJson(json, JsonDeviceNotificationState.class);
            DeviceNotificationState[] deviceNotificationStates = result.deviceNotificationStates;
            if (deviceNotificationStates != null) {
                return deviceNotificationStates;
            }
        } catch (IOException | URISyntaxException e) {
            logger.info("Error getting device notification states {}", e);
        }
        return new DeviceNotificationState[0];
    }

    public AscendingAlarmModel[] getAscendingAlarm() {
        String json;
        try {
            json = makeRequestAndReturnString(alexaServer + "/api/ascending-alarm");
            JsonAscendingAlarm result = parseJson(json, JsonAscendingAlarm.class);
            AscendingAlarmModel[] ascendingAlarmModelList = result.ascendingAlarmModelList;
            if (ascendingAlarmModelList != null) {
                return ascendingAlarmModelList;
            }
        } catch (IOException | URISyntaxException e) {
            logger.info("Error getting device notification states {}", e);
        }
        return new AscendingAlarmModel[0];
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
                            + "&contentType=station&callSign=&mediaOwnerCustomerId="
                            + (StringUtils.isEmpty(this.accountCustomerId) ? device.deviceOwnerCustomerId
                                    : this.accountCustomerId),
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
                            + (StringUtils.isEmpty(this.accountCustomerId) ? device.deviceOwnerCustomerId
                                    : this.accountCustomerId)
                            + "&shuffle=false",
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
                            + (StringUtils.isEmpty(this.accountCustomerId) ? device.deviceOwnerCustomerId
                                    : this.accountCustomerId)
                            + "&shuffle=false",
                    command, true, true, null);
        }
    }

    public void sendNotificationToMobileApp(String customerId, String text, @Nullable String title)
            throws IOException, URISyntaxException {
        Map<String, Object> parameters = new Hashtable<String, Object>();
        parameters.put("notificationMessage", text);
        parameters.put("alexaUrl", "#v2/behaviors");
        if (title != null && !StringUtils.isEmpty(title)) {
            parameters.put("title", title);
        } else {
            parameters.put("title", "OpenHAB");
        }
        parameters.put("customerId", customerId);
        executeSequenceCommand(null, "Alexa.Notifications.SendMobilePush", parameters);
    }

    public void sendAnnouncement(Device device, String text, @Nullable String title, int ttsVolume, int standardVolume)
            throws IOException, URISyntaxException {
        Map<String, Object> parameters = new Hashtable<String, Object>();
        parameters.put("expireAfter", "PT5S");
        JsonAnnouncementContent[] contentArray = new JsonAnnouncementContent[1];
        JsonAnnouncementContent content = new JsonAnnouncementContent();
        if (StringUtils.isEmpty(title)) {
            content.display.title = "OpenHAB";
        } else {
            content.display.title = title;
        }
        content.display.body = text;
        if (text.startsWith("<speak>") && text.endsWith("</speak>")) {
            content.speak.type = "ssml";
            String plainText = text.replaceAll("<[^>]+>", "");
            content.display.body = plainText;
        } else {
            content.display.body = text;
        }
        content.speak.value = text;

        contentArray[0] = content;

        parameters.put("content", contentArray);

        JsonAnnouncementTarget target = new JsonAnnouncementTarget();
        target.customerId = device.deviceOwnerCustomerId;
        TargetDevice[] devices = new TargetDevice[1];
        TargetDevice deviceTarget = new TargetDevice();
        deviceTarget.deviceSerialNumber = device.serialNumber;
        deviceTarget.deviceTypeId = device.deviceType;
        devices[0] = deviceTarget;
        target.devices = devices;
        parameters.put("target", target);

        String accountCustomerId = this.accountCustomerId;
        String customerId = StringUtils.isEmpty(accountCustomerId) ? device.deviceOwnerCustomerId : accountCustomerId;

        if (customerId != null) {
            parameters.put("customerId", customerId);
        }
        executeSequenceCommandWithVolume(device, "AlexaAnnouncement", parameters, ttsVolume, standardVolume);
    }

    public void textToSpeech(Device device, String text, int ttsVolume, int standardVolume)
            throws IOException, URISyntaxException {
        Map<String, Object> parameters = new Hashtable<>();
        parameters.put("textToSpeak", text);
        executeSequenceCommandWithVolume(device, "Alexa.Speak", parameters, ttsVolume, standardVolume);
    }

    private void executeSequenceCommandWithVolume(@Nullable Device device, String command,
            @Nullable Map<String, Object> parameters, int ttsVolume, int standardVolume)
            throws IOException, URISyntaxException {
        if (ttsVolume != 0) {

            JsonArray nodesToExecute = new JsonArray();

            Map<String, Object> volumeParameters = new Hashtable<>();
            // add tts volume
            volumeParameters.clear();
            volumeParameters.put("value", ttsVolume);
            nodesToExecute.add(createExecutionNode(device, "Alexa.DeviceControls.Volume", volumeParameters));

            // add command
            nodesToExecute.add(createExecutionNode(device, command, parameters));

            // add volume
            volumeParameters.clear();
            volumeParameters.put("value", standardVolume);
            nodesToExecute.add(createExecutionNode(device, "Alexa.DeviceControls.Volume", volumeParameters));

            executeSequenceNodes(nodesToExecute);

        } else {
            executeSequenceCommand(device, command, parameters);
        }
    }

    // commands: Alexa.Weather.Play, Alexa.Traffic.Play, Alexa.FlashBriefing.Play, Alexa.GoodMorning.Play,
    // Alexa.SingASong.Play, Alexa.TellStory.Play, Alexa.Speak (textToSpeach)
    public void executeSequenceCommand(@Nullable Device device, String command,
            @Nullable Map<String, Object> parameters) throws IOException, URISyntaxException {
        JsonObject nodeToExecute = createExecutionNode(device, command, parameters);
        executeSequenceNode(nodeToExecute);
    }

    private void executeSequenceNode(JsonObject nodeToExecute) throws IOException, URISyntaxException {
        JsonObject sequenceJson = new JsonObject();
        sequenceJson.addProperty("@type", "com.amazon.alexa.behaviors.model.Sequence");
        sequenceJson.add("startNode", nodeToExecute);

        JsonStartRoutineRequest request = new JsonStartRoutineRequest();
        request.sequenceJson = gson.toJson(sequenceJson);
        String json = gson.toJson(request);

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Routines-Version", "1.1.218665");

        makeRequest("POST", alexaServer + "/api/behaviors/preview", json, true, true, null);
    }

    private void executeSequenceNodes(JsonArray nodesToExecute) throws IOException, URISyntaxException {
        JsonObject serialNode = new JsonObject();
        serialNode.addProperty("@type", "com.amazon.alexa.behaviors.model.SerialNode");

        serialNode.add("nodesToExecute", nodesToExecute);

        executeSequenceNode(serialNode);
    }

    private JsonObject createExecutionNode(@Nullable Device device, String command,
            @Nullable Map<String, Object> parameters) {
        JsonObject operationPayload = new JsonObject();
        if (device != null) {
            operationPayload.addProperty("deviceType", device.deviceType);
            operationPayload.addProperty("deviceSerialNumber", device.serialNumber);
            operationPayload.addProperty("locale", "");
            operationPayload.addProperty("customerId",
                    StringUtils.isEmpty(this.accountCustomerId) ? device.deviceOwnerCustomerId
                            : this.accountCustomerId);
        }
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

        JsonObject nodeToExecute = new JsonObject();
        nodeToExecute.addProperty("@type", "com.amazon.alexa.behaviors.model.OpaquePayloadOperationNode");
        nodeToExecute.addProperty("type", command);
        nodeToExecute.add("operationPayload", operationPayload);
        return nodeToExecute;
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
            String newCustomerId = "\"customerId\":\""
                    + (StringUtils.isEmpty(this.accountCustomerId) ? device.deviceOwnerCustomerId
                            : this.accountCustomerId)
                    + "\"";
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
        String json = makeRequestAndReturnString(alexaServer + "/api/behaviors/automations?limit=2000");
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

    public JsonNotificationResponse[] notifications() throws IOException, URISyntaxException {
        String response = makeRequestAndReturnString(alexaServer + "/api/notifications");
        JsonNotificationsResponse result = parseJson(response, JsonNotificationsResponse.class);
        JsonNotificationResponse[] notifications = result.notifications;
        if (notifications == null) {
            return new JsonNotificationResponse[0];
        }
        return notifications;
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
            headers.put("Routines-Version", "1.1.218665");
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
        payload.customerId = (StringUtils.isEmpty(this.accountCustomerId) ? device.deviceOwnerCustomerId
                : this.accountCustomerId);
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

    public JsonEqualizer getEqualizer(Device device) throws IOException, URISyntaxException {
        String json = makeRequestAndReturnString(
                alexaServer + "/api/equalizer/" + device.serialNumber + "/" + device.deviceType);
        return parseJson(json, JsonEqualizer.class);
    }

    public void SetEqualizer(Device device, JsonEqualizer settings) throws IOException, URISyntaxException {
        String postData = gson.toJson(settings);
        makeRequest("POST", alexaServer + "/api/equalizer/" + device.serialNumber + "/" + device.deviceType, postData,
                true, true, null);
    }
}
