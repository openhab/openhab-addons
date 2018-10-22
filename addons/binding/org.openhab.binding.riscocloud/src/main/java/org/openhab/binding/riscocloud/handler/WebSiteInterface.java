package org.openhab.binding.riscocloud.handler;

import static org.openhab.binding.riscocloud.RiscoCloudBindingConstants.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openhab.binding.riscocloud.RiscoCloudBindingConstants;
import org.openhab.binding.riscocloud.json.ServerDatasHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class WebSiteInterface {
    final static Logger logger = LoggerFactory.getLogger(WebSiteInterface.class);
    private final static Gson gson = new Gson();

    public static LoginResult webSiteLogin(Configuration config) {
        LoginResult loginResult = new LoginResult();

        if (config.get(RiscoCloudBindingConstants.USERNAME) == null
                || config.get(RiscoCloudBindingConstants.WEBPASS) == null) {
            loginResult.error += " Parameter 'username' and 'webpass' must be configured.";
            loginResult.statusDescr = "Missing credentials";
        } else {
            try {
                Document document = null;
                String loginPage = null;
                String content = genInputJson(USER_PASS, config, 0);
                InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
                loginPage = HttpUtil.executeUrl("POST", (String) config.get(RiscoCloudBindingConstants.WEBUIURL), null,
                        stream, "application/json", 20000);
                // logger.debug("loginPage=" + loginPage);

                document = Jsoup.parse(loginPage);
                Element form = document.select("form").first();
                // logger.debug("form=" + form.toString());

                Elements sites = form.select("label[id$=_label_name]");
                // logger.debug("sites = " + sites.toString());
                for (Element site : sites) {
                    // logger.debug("site = " + site.toString());
                    Pattern pattern = Pattern.compile("site_(.*?)_");
                    Matcher matcher = pattern.matcher(site.attr("id"));
                    if (matcher.find()) {
                        // logger.debug("site added " + matcher.group(1) + "-" + site.text());
                        loginResult.siteList.put(Integer.parseInt(matcher.group(1)), site.text());
                    }
                }
                // logger.debug("siteList : " + loginResult.siteList.values() + " siteList.isEmpty() : " +
                // loginResult.siteList.isEmpty());
                if (loginResult.siteList.isEmpty()) {
                    loginResult.error += "No site found";
                    loginResult.statusDescr = "@text/offline.site-error";
                }
            } catch (IOException e) {
                loginResult.error += "Connection error to " + config.get(RiscoCloudBindingConstants.WEBUIURL);
                loginResult.errorDetail = e.getMessage();
                loginResult.statusDescr = "@text/offline.uri-error-1";

            } catch (IllegalArgumentException e) {
                loginResult.error += "caught exception !";
                loginResult.errorDetail = e.getMessage();
                loginResult.statusDescr = "@text/offline.uri-error-2";
            }
        }
        return loginResult;
    }

    public static LoginResult webSitePoll(Configuration config) throws IOException {
        LoginResult loginResult = new LoginResult();

        if (config == null) {
            loginResult = doConnect(config);
        } else {

            String jsonResponse = null;
            String content = genInputJson(POLL, config, 0);
            // logger.debug("jsonSent = {}", content);
            InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            jsonResponse = HttpUtil.executeUrl("POST", (String) config.get(RiscoCloudBindingConstants.REST_URL), null,
                    stream, "application/json", 20000);
            // logger.debug("webSitePoll() : apiResponse global = {}", jsonResponse);

            JsonParser parser = new JsonParser();
            JsonObject jsonValueObject = parser.parse(jsonResponse).getAsJsonObject();
            JsonElement jsonElement = jsonValueObject.get("error");
            if (!jsonElement.isJsonNull() && jsonElement.getAsInt() != 0) {
                loginResult = doConnect(config);
            } else {
                // logger.debug("apiResponse before fill = {}", jsonResponse);
                for (Map.Entry<String, String> entry : REST_URLS.entrySet()) {
                    String myJsonResponse = null;
                    // logger.debug("REST URL : Property = {} : url = {}", entry.getKey(), entry.getValue());
                    String property = entry.getKey();
                    String url = (String) config.get(entry.getValue());
                    // logger.debug("REST URL : key = {} : value = {} : Property = {} : url = {}", entry.getKey(),
                    // entry.getValue(), property, url);
                    myJsonResponse = HttpUtil.executeUrl("POST", url, null, null, 20000);
                    if (myJsonResponse != null) {
                        jsonResponse = jsonBuilder(jsonResponse, property, myJsonResponse);
                    }
                }
                // logger.debug("apiResponse after fill = {}", jsonResponse);
                // Map the JSON response to an object
                loginResult.serverDatasHandler = gson.fromJson(jsonResponse, ServerDatasHandler.class);
                if (loginResult.serverDatasHandler.getError() != 0) {
                    loginResult = doConnect(config);
                }
            }
        }

        return loginResult;
    }

    public static LoginResult webSiteSendCommand(Configuration config, String command, int idPart) throws IOException {
        LoginResult loginResult = new LoginResult();
        if (config == null) {
            return loginResult;
        }

        // switch (command) {
        // case ARM_FULL:
        // break;
        // default:
        // logger.debug("handleSiteUpdate() : function = '{}': idPart = '{}'", function, idPart);
        //
        // }

        String jsonResponse = null;

        String content = genInputJson(command, config, idPart);

        InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        jsonResponse = HttpUtil.executeUrl("POST", (String) config.get(RiscoCloudBindingConstants.ARM_DISARM_URL), null,
                stream, "application/json", 20000);

        return loginResult;
    }

    private @Nullable static LoginResult doConnect(Configuration config) {
        LoginResult loginResult = new LoginResult();
        ServerDatasHandler newServerDatasHandler = null;

        // Check if a pincode has been provided during the bridge creation
        if (config.get(RiscoCloudBindingConstants.PINCODE) == null) {
            loginResult.error += " Parameter 'pincode' must be configured.";
            loginResult.statusDescr = "Missing credentials";
        } else {
            try {
                // Run the HTTP request login
                String jsonResponse = null;

                String content = genInputJson(SITEID_PIN, config, 0);
                // logger.debug("jsonSent = {}", content);

                InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
                HttpUtil.executeUrl("POST", (String) config.get(RiscoCloudBindingConstants.SITE_URL), null, stream,
                        "application/json", 20000);

                // HttpUtil.executeUrl("POST", (String) config.get(RiscoCloudBindingConstants.OVERVIEWURL), null, null,
                // 20000);

                jsonResponse = HttpUtil.executeUrl("POST", (String) config.get(RiscoCloudBindingConstants.REST_URL),
                        null, null, 20000);

                // logger.debug("doConnect() : apiResponse = {}", jsonResponse);
                // Map the JSON response to an object
                newServerDatasHandler = gson.fromJson(jsonResponse, ServerDatasHandler.class);
                if (newServerDatasHandler.getError() != 0) {
                    loginResult.error = "Login refused";
                    loginResult.errorDetail = "" + newServerDatasHandler.getError();
                    loginResult.statusDescr = "Error : possible invalid credentials";
                } else {
                }

            } catch (IllegalArgumentException e) {
                loginResult.errorDetail = e.getMessage();
                loginResult.statusDescr = "@text/offline.uri-error";
            } catch (IOException e) {
                loginResult.error += "Connection error to " + config.get(RiscoCloudBindingConstants.SITE_URL);
                loginResult.statusDescr = "@text/offline.uri-error";
            }

        }

        return loginResult;
    }

    private static String genInputJson(String typeJson, Configuration config, int idPart) {
        String jsonGenerated = "{}";
        if (typeJson.equals(USER_PASS)) {
            jsonGenerated = "{'username' : '" + config.get(RiscoCloudBindingConstants.USERNAME) + "',"
                    + "'password' : '" + config.get(RiscoCloudBindingConstants.WEBPASS) + "'}";
        } else if (typeJson.equals(SITEID_PIN)) {
            jsonGenerated = "{'SelectedSiteId' : '" + config.get(RiscoCloudBindingConstants.SITE_ID) + "',"
                    + "'Pin' : '" + config.get(RiscoCloudBindingConstants.PINCODE) + "'}";
        } else if (typeJson.equals(POLL)) {
            jsonGenerated = "{'IsAlive' : 'true'}";
        } else if (typeJson.equals(ARM_FULL)) {
            jsonGenerated = "{'type' : '" + idPart + ":armed', 'passcode' : '"
                    + config.get(RiscoCloudBindingConstants.PINCODE) + "', 'bypassZoneId' : -1 }";
        } else if (typeJson.equals(ARM_PART)) {
            jsonGenerated = "{'type' : '" + idPart + ":partially', 'passcode' : '"
                    + config.get(RiscoCloudBindingConstants.PINCODE) + "', 'bypassZoneId' : -1 }";
        } else if (typeJson.equals(DISARM)) {
            jsonGenerated = "{'type' : '" + idPart + ":disarmed', 'passcode' : '"
                    + config.get(RiscoCloudBindingConstants.PINCODE) + "', 'bypassZoneId' : -1 }";
        }
        return jsonGenerated;
    }

    private static String jsonBuilder(String jsonToFill, String property, String jsonFrom) {
        // logger.debug("jsonBuilder() : before : jsonToFill = {} : property = {} : jsonFrom = {}", jsonToFill,
        // property,jsonFrom);
        try {
            JsonParser parser = new JsonParser();
            JsonObject jsonValueObject = parser.parse(jsonFrom).getAsJsonObject();
            JsonElement jsonElement = jsonValueObject.get(property);
            JsonObject jsonToFillObject = null;
            // logger.debug("jsonBuilder() : jsonValueObject = {}", jsonValueObject);
            if (jsonValueObject.has(property) && !jsonElement.isJsonNull()) {
                jsonToFillObject = parser.parse(jsonToFill).getAsJsonObject();
                jsonToFillObject.add(property, jsonElement);
                // logger.debug("jsonBuilder() : jsonToFillObject = {}", jsonToFillObject);
                jsonToFill = jsonToFillObject.toString();
            }
        } catch (Exception e) {
            logger.debug("jsonBuilder() : Got exception = {}", e);
        }
        // logger.debug("jsonBuilder() : after : jsonToFill = {} : property = {} : jsonFrom = {}", jsonToFill,
        // property,jsonFrom);
        return jsonToFill;
    }
}
