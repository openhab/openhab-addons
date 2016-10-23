/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.internal.connection;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.UriBuilder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openhab.binding.zoneminder.ZoneMinderConstants;
import org.openhab.binding.zoneminder.internal.api.ConfigData;
import org.openhab.binding.zoneminder.internal.api.ConfigEnum;
import org.openhab.binding.zoneminder.internal.api.Event;
import org.openhab.binding.zoneminder.internal.api.EventWrapper;
import org.openhab.binding.zoneminder.internal.api.MonitorDaemonStatus;
import org.openhab.binding.zoneminder.internal.api.MonitorData;
import org.openhab.binding.zoneminder.internal.api.MonitorWrapper;
import org.openhab.binding.zoneminder.internal.api.Pagination;
import org.openhab.binding.zoneminder.internal.api.ServerCpuLoad;
import org.openhab.binding.zoneminder.internal.api.ServerData;
import org.openhab.binding.zoneminder.internal.api.ServerDiskUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ZoneMinderHttpProxy {

    private Logger logger = LoggerFactory.getLogger(ZoneMinderHttpProxy.class);

    // public static final String PATH_ZONEMINDER_BASE = "/zm";
    private static final String SUBPATH_API = "/api";
    private static final String SUBPATH_SERVERLOGIN = "/index.php";
    private static final String SUBPATH_API_SERVERVERSION_JSON = "/host/getVersion.json";
    private static final String SUBPATH_API_SERVER_CPULOAD_JSON = "/host/getLoad.json";
    private static final String SUBPATH_API_SERVER_DISKPERCENT_JSON = "/host/getDiskPercent.json";
    private static final String SUBPATH_API_SERVER_DAEMON_CHECKSTATE = "/host/daemonCheck.json";

    private static final String SUBPATH_API_SERVER_GET_CONFIG_JSON = "/configs/view/{ConfigId}.json";

    private static final String SUBPATH_API_MONITORS_JSON = "/monitors.json";
    private static final String SUBPATH_API_MONITOR_SPECIFIC_JSON = "/monitors/{MonitorId}.json";
    private static final String SUBPATH_API_EVENTS_SPECIFIC_MONITOR_JSON = "/events/index/MonitorId:{MonitorId}.json";
    private static final String SUBPATH_API_MONITORSTATUS_JSON = "/monitors/daemonStatus/id:{MonitorId}/daemon:{DaemonName}.json";
    private static final String QUERY_CURRENTPAGE = "page={currentPage}";

    private static final String DAEMON_NAME_CAPTURE = "zmc";
    private static final String DAEMON_NAME_ANALYSIS = "zma";
    private static final String DAEMON_NAME_FRAME = "zmf";

    private Boolean isConnected = false;

    private URI uriZmServerRoot = null;
    private ProtocolType protocol;
    private String hostName;
    private Integer port = ZoneMinderConstants.DEFAULT_HTTP_PORT;
    private String userName;
    private String password;
    private String zoneminderBasePath = ""; // PATH_ZONEMINDER_BASE;

    protected JsonParser parser = new JsonParser();
    protected Gson gson = new Gson();

    private List<String> cookies;
    private HttpURLConnection conn;

    private final String USER_AGENT = "Mozilla/5.0";

    public ZoneMinderHttpProxy(String protocol, String hostName, Integer port, String basePath, String userName,
            String password) {

        this.protocol = ProtocolType.getEnum(protocol);
        this.hostName = hostName;
        this.port = port;
        this.zoneminderBasePath = fixPath(basePath);
        this.userName = userName;
        this.password = password;
    }

    public ZoneMinderHttpProxy(String protocol, String hostName, String basePath, String userName, String password) {

        this.protocol = ProtocolType.getEnum(protocol);
        this.hostName = hostName;
        this.port = ZoneMinderConstants.DEFAULT_HTTP_PORT;
        this.zoneminderBasePath = fixPath(basePath);
        this.userName = userName;
        this.password = password;
    }

    public Boolean getIsConnected() {
        return this.isConnected;
    }

    protected void setIsConnected(Boolean isConnected) {
        this.isConnected = isConnected;
    }

    protected String fixPath(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return path;

    }

    public Boolean connect() {

        URI uriLogin;
        isConnected = false;
        try {

            URL url = new URL(protocol.name(), hostName, port, zoneminderBasePath);

            this.uriZmServerRoot = UriBuilder.fromUri(url.toString()).build();

            // make sure cookies is turn on
            CookieHandler.setDefault(new CookieManager());

            // Make sure we are logged in to ZoneMinder
            uriLogin = UriBuilder.fromUri(uriZmServerRoot).path(SUBPATH_SERVERLOGIN).build();
            isConnected = ensureLogin(uriLogin, userName, password);

        } catch (Exception e) {
            logger.error("Exception when connecting to ZoneMinder Server Exception='{}'", e.getMessage());
            isConnected = false;
        }
        return isConnected;
    }

    public void close() {
        if (conn != null) {
            conn.disconnect();
        }
    }

    protected String resolveCommands(String url, String command, String commandValue) {
        String commandKey = "{" + command + "}";
        if (url.contains(commandKey)) {
            url = url.replace(commandKey, commandValue);
        }
        return url;
    }

    protected String sendPost(URI uri, String postParams) throws Exception {

        conn = (HttpURLConnection) uri.toURL().openConnection();

        // Acts like a browser
        conn.setUseCaches(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Host", uri.toURL().getHost());
        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        for (String cookie : this.cookies) {
            conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
        }
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Referer", uri.toURL().toString());
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", Integer.toString(postParams.length()));

        conn.setDoOutput(true);
        conn.setDoInput(true);

        // Send post request
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes(postParams);
        wr.flush();
        wr.close();

        int responseCode = conn.getResponseCode();

        if (responseCode == 200) {

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } else {
            String message = "";
            switch (responseCode) {
                case 404:
                    message = String.format(
                            "The URL '%s' was not found on ZoneMinder Server. Please verify that the server is accessible, and that your OpenHAB Bridge configuration is correct. (ResponseCode='%d', ResponseMessage='%s')",
                            uri.toString(), responseCode, conn.getResponseMessage());
                    break;
                default:
                    message = String.format(
                            "An error occured while communicating with ZoneMinder Server: URL='%s', ResponseCode='%d', ResponseMessage='%s'",
                            uri.toString(), responseCode, conn.getResponseMessage());
            }
            logger.error(message);
        }
        return null;

    }

    // TODO:: Use custom API Path
    /*
     * private String getApiPath() {
     * return PATH_API;
     * }
     */
    public ServerData getServerData() {
        ServerData serverData = null;
        JsonObject jsonObject = null;
        try {

            jsonObject = getJson(SUBPATH_API_SERVERVERSION_JSON);
            serverData = gson.fromJson(jsonObject, ServerData.class);

            serverData.setDaemonCheckState(getServerDaemonCheckState());
        } catch (Exception e) {
            logger.error("Error occurred in 'getServerVersion' Error message: '{}'", e.getMessage());
        }
        return serverData;
    }

    protected Boolean getServerDaemonCheckState() {

        try {
            return ((getJson(SUBPATH_API_SERVER_DAEMON_CHECKSTATE).get("result").getAsInt() == 1) ? true : false);

        } catch (Exception e) {
            logger.error("Error occurred in 'getServerVersion' Error message: '{}'", e.getMessage());
        }
        return null;

    }

    public ServerCpuLoad getServerCpuLoad() {

        JsonObject jsonObject = null;
        try {

            jsonObject = getJson(SUBPATH_API_SERVER_CPULOAD_JSON);
            // jsonObject = getJson(SUBPATH_API_SERVER_DISKPERCENT_JSON).get("load").get();
            // jsonObject
            // for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet())
            // {

            // }
        } catch (Exception e) {
            logger.error("Error occurred in 'getServerVersion' Error message: '{}'", e.getMessage());
        }
        return gson.fromJson(jsonObject, ServerCpuLoad.class);

    }

    public ServerDiskUsage getServerDiskUsage() {
        JsonObject jsonObject = null;
        try {
            jsonObject = getJson(SUBPATH_API_SERVER_DISKPERCENT_JSON).get("usage").getAsJsonObject();

            Set<Map.Entry<String, JsonElement>> entries = jsonObject.entrySet();
            for (Map.Entry<String, JsonElement> entry : entries) {
                if (entry.getKey().equalsIgnoreCase("Total")) {
                    JsonObject Test = (JsonObject) entry.getValue();
                    return gson.fromJson(Test, ServerDiskUsage.class);

                }
            }

        } catch (Exception e) {
            logger.error("Error occurred in 'getServerVersion' Error message: '{}'", e.getMessage());
        }
        return null;

    }

    public MonitorDaemonStatus getMonitorCaptureDaemonStatus(String monitorId) {
        return getMonitorStatus(monitorId, DAEMON_NAME_CAPTURE);
    }

    public MonitorDaemonStatus getMonitorAnalysisDaemonStatus(String monitorId) {
        return getMonitorStatus(monitorId, DAEMON_NAME_ANALYSIS);
    }

    public MonitorDaemonStatus getMonitorFrameDaemonStatus(String monitorId) {
        return getMonitorStatus(monitorId, DAEMON_NAME_FRAME);
    }

    protected MonitorDaemonStatus getMonitorStatus(String monitorId, String daemonName) {

        JsonObject jsonObject = null;
        JsonObject jsonObject1 = null;
        try {
            String strCommand = resolveCommands(SUBPATH_API_MONITORSTATUS_JSON, "MonitorId", monitorId);
            strCommand = resolveCommands(strCommand, "DaemonName", daemonName);
            jsonObject = getJson(strCommand);
            // jsonObject1 = jsonObject.getAsJsonObject("events");
        } catch (Exception e) {
            logger.error("Error occurred in 'getMonitorStatus' Error message: '{}'", e.getMessage());
        }

        return gson.fromJson(jsonObject, MonitorDaemonStatus.class);

    }

    public ArrayList<MonitorData> getMonitors() {

        JsonObject jsonObject = null;
        try {
            jsonObject = getJson(SUBPATH_API_MONITORS_JSON);
        } catch (Exception e) {
            logger.error("Error occurred in 'getMonitors' Error message: '{}'", e.getMessage());
        }
        if (jsonObject == null) {
            return null;
        }

        MonitorWrapper[] monitors = gson.fromJson(jsonObject.getAsJsonArray("monitors"), MonitorWrapper[].class);
        ArrayList<MonitorData> array = new ArrayList<MonitorData>();

        for (MonitorWrapper cur : monitors) {
            array.add(cur.getMonitor());
        }

        return array;
    }

    public MonitorData getMonitor(String MonitorId) {

        JsonObject jsonObject = null;
        try {
            jsonObject = getJson(resolveCommands(SUBPATH_API_MONITOR_SPECIFIC_JSON, "MonitorId", MonitorId))
                    .getAsJsonObject("monitor").getAsJsonObject("Monitor");
        } catch (Exception e) {
            logger.error("Error occurred in 'getMonitor' Error message: '{}'", e.getMessage());
        }
        if (jsonObject == null) {
            return null;
        }

        return gson.fromJson(jsonObject, MonitorData.class);

    }

    public ConfigData getConfig(ConfigEnum configId) {

        ConfigData configData = null;
        JsonObject jsonObject = null;
        try {
            jsonObject = getJson(resolveCommands(SUBPATH_API_SERVER_GET_CONFIG_JSON, "ConfigId", configId.name()))
                    .getAsJsonObject("config").getAsJsonObject("Config");

            configData = new ConfigData(jsonObject.get("Id"), jsonObject.get("Name"), jsonObject.get("Value"),
                    jsonObject.get("Type"), jsonObject.get("DefaultValue"), jsonObject.get("Readonly"));

        } catch (Exception e) {
            logger.error("Error occurred in 'getConfig' Error message: '{}'", e.getMessage());
        }
        if (jsonObject == null) {
            return null;
        }
        return configData;

    }

    public Event getLastEvent(Integer MonitorId) {

        JsonObject jsonObject = null;
        ArrayList<Event> list = new ArrayList<Event>();
        try {

            Integer curPageIdx = 1;
            Integer maxPages = 1;
            for (int i = 0; curPageIdx <= maxPages; curPageIdx++) {

                jsonObject = getJson(
                        resolveCommands(SUBPATH_API_EVENTS_SPECIFIC_MONITOR_JSON, "MonitorId", MonitorId.toString()),
                        resolveCommands(QUERY_CURRENTPAGE, "currentPage", curPageIdx.toString()));

                if (jsonObject == null) {
                    return null;
                }

                Pagination pagination = gson.fromJson(jsonObject.getAsJsonObject("pagination"), Pagination.class);
                if (curPageIdx == 1) {
                    maxPages = pagination.getPageCount();
                }
                EventWrapper[] events = gson.fromJson(jsonObject.getAsJsonArray("events"), EventWrapper[].class);

                for (EventWrapper cur : events) {
                    list.add(cur.getEvent());
                }
            }
        } catch (Exception e) {
            logger.error("Error occurred in 'getEvents' Error message: '{}'", e.getMessage());
        }

        // Return last event
        return list.get(list.size() - 1);
    }

    private JsonObject getJson(String methodPath) throws Exception {
        return getJson(methodPath, null);
    }

    private JsonObject getJson(String methodPath, String queryString) throws Exception {

        // Build Path to the required method in the API
        UriBuilder uriBuilder = UriBuilder.fromUri(uriZmServerRoot).path(SUBPATH_API).path(methodPath);
        if ((queryString != null) && (queryString != "")) {
            uriBuilder = uriBuilder.replaceQuery(queryString);
        }

        String result = getDocumentAsString(uriBuilder.build(), true);
        if (result == null) {
            return null;
        }
        return parser.parse(result).getAsJsonObject();
    }

    protected String getDocumentAsString(URI uri, Boolean verifyConnection) throws Exception {

        if ((verifyConnection) && (getIsConnected() == false)) {
            return null;
        }

        StringBuffer response = new StringBuffer();

        conn = (HttpURLConnection) uri.toURL().openConnection();
        // Set Connection timeout
        // conn.setConnectTimeout(5000);
        // default is GET
        conn.setRequestMethod("GET");
        conn.setUseCaches(false);

        // act like a browser
        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        if (cookies != null) {
            for (String cookie : this.cookies) {
                conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
            }
        }

        int responseCode = conn.getResponseCode();

        if (responseCode == 200) {

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();

            // Update the cookies
            setCookies(conn.getHeaderFields().get("Set-Cookie"));
        } else {
            String message = "";
            switch (responseCode) {
                case 404:
                    message = String.format(
                            "The URL '%s' was not found on ZoneMinder Server. Please verify that the server is accessible, and that your OpenHAB Bridge configuration is correct. (ResponseCode='%d', ResponseMessage='%s')",
                            uri.toString(), responseCode, conn.getResponseMessage());
                    break;
                default:
                    message = String.format(
                            "An error occured while communicating with ZoneMinder Server: URL='%s', ResponseCode='%d', ResponseMessage='%s'",
                            uri.toString(), responseCode, conn.getResponseMessage());
            }
            logger.error(message);
        }

        return response.toString();

    }

    private static String TAG_LOGGING_IN_PAGE = "Logging in";
    private static String TAG_LOGIN_PAGE = "ZoneMinder Login";
    private static String TAG_START_PAGE = "- Console";

    protected ZoneMinderWelcomePageEnum getFirstPageType(String html) {
        Document doc = Jsoup.parse(html);

        // Seems like we must provide user credentials
        if ((doc.getElementsContainingText(TAG_LOGIN_PAGE).size() > 0) && (doc.getElementById("loginForm") != null)) {
            return ZoneMinderWelcomePageEnum.LOGIN_PAGE;
        }
        // We are trying to login, don't know the result yet :-)
        else if (doc.getElementsContainingText(TAG_LOGGING_IN_PAGE).size() > 0) {
            return ZoneMinderWelcomePageEnum.LOGGING_IN_PAGE;
        }
        // Finally check if we ended on the startpage
        else if (doc.title().endsWith(TAG_START_PAGE)) {
            return ZoneMinderWelcomePageEnum.START_PAGE;
        }
        return ZoneMinderWelcomePageEnum.UNKNOWN_PAGE;
    }

    protected Boolean ensureLogin(URI uriLogin, String username, String password) throws Exception {
        // Fetch page
        String html = getDocumentAsString(uriLogin, false);

        ZoneMinderWelcomePageEnum pageType = getFirstPageType(html);

        // We didn't hit the login form. Server is probably not protected by login
        if (pageType == ZoneMinderWelcomePageEnum.LOGIN_PAGE) {

            // Get the Document
            Document doc = Jsoup.parse(html);

            // Lets see if we got a ZoneMinder FormLogin id
            Element loginForm = doc.getElementById("loginForm");

            // Lets do the magic....
            Elements inputElements = loginForm.getElementsByTag("input");
            List<String> paramList = new ArrayList<String>();
            for (Element inputElement : inputElements) {
                Elements cur = inputElement.getElementsByTag("input");
                String type = inputElement.attr("type");
                String key = inputElement.attr("name");
                String value = inputElement.attr("value");
                if (!type.equals("submit")) {
                    if (key.equals("username")) {
                        value = username;
                    } else if (key.equals("password")) {
                        value = password;
                    }
                    paramList.add(key + "=" + URLEncoder.encode(value, "UTF-8"));
                }
            }

            // build parameters list
            StringBuilder result = new StringBuilder();
            for (String param : paramList) {
                if (result.length() == 0) {
                    result.append(param);
                } else {
                    result.append("&" + param);
                }
            }

            Integer retries = 0;
            // Trying to login
            String responseHtml = sendPost(uriLogin, result.toString());
            while ((getFirstPageType(responseHtml) == ZoneMinderWelcomePageEnum.LOGGING_IN_PAGE) && (retries < 10)) {
                Thread.sleep(100);

                // Keep trying
                responseHtml = getDocumentAsString(uriLogin, false);
                retries++;
            }

            if (getFirstPageType(responseHtml) == ZoneMinderWelcomePageEnum.START_PAGE) {
                setIsConnected(true);
            } else {
                logger.error("Unable to login to ZoneMinder Server");
                setIsConnected(false);
            }
            /*
             * // Get the Document
             * Document docResponse = Jsoup.parse(response);
             * Element e = doc.getElementById("h2");
             * Thread.sleep(500);
             * response = sendPost(uriLogin, "");
             *
             * // Get the Document
             * docResponse = Jsoup.parse(response);
             *
             * // Lets see if we got a ZoneMinder FormLogin id
             * Element loginForm1 = docResponse.getElementById("loginForm");
             * if (loginForm == null) {
             * logger.debug("Success!");
             * } else {
             * logger.debug("Login failure!");
             * }
             */ /*
                * // Just try to call something in the API.
                * if (getServerVersion() == null) {
                * // Set isConnnected Boolean to True
                * setIsConnected(false);
                * } else {
                * String htmlAfter = getDocumentAsString(uriLogin, false);
                * Document docAfter = Jsoup.parse(htmlAfter);
                * // Set isConnnected Boolean to True
                * setIsConnected(true);
                * }
                */
        }
        // If we land directly on the main form login isn't activated in the config -> just continue :-)
        else if (pageType == ZoneMinderWelcomePageEnum.START_PAGE) {
            // Set isConnnected Boolean to True
            setIsConnected(true);
        }
        // If we didn't land on the main form something is wrong
        else {
            setIsConnected(false);
            logger.error(
                    "Couldn't connect to ZoneMinder Server. The URL '{}' seems to be incorrect. Fix the URL in OpenHab Bridge settings and try again.",
                    uriLogin.toString());

            return false;
        }

        return getIsConnected();
    }

    protected List<String> getCookies() {
        return cookies;
    }

    protected void setCookies(List<String> cookies) {
        this.cookies = cookies;
    }

}
