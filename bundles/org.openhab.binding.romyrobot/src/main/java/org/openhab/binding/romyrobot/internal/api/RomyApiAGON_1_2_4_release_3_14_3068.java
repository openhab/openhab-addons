package org.openhab.binding.romyrobot.internal.api;

import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.romyrobot.internal.RomyRobotConfiguration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RomyApiAGON_1_2_4_release_3_14_3068 implements RomyApi {

    private @NonNull String hostname;
    private @NonNull RomyRobotConfiguration config;
    protected HttpRequestSender http;
    private String firmwareVersion = "UNKNOWN";
    private String mode;
    private String activePumpVolume;
    private String charging;
    private int batteryLevel;
    private String powerStatus;
    private String maps;
    private int rssi;
    private String strategy;
    private String suctionMode;
    private static final String CMD_GET_ROBOT_ID = "get/robot_id";
    private static final String CMD_GET_STATUS = "get/status";
    private static final String CMD_GET_MAPS = "get/maps";
    private static final String CMD_GET_WIFI_STATUS = "get/wifi_status";
    private static final String CMD_GET_POWER_STATUS = "get/power_status";
    private static final @NonNull String UNKNOWN = "UNKNOWN";

    public RomyApiAGON_1_2_4_release_3_14_3068(final @NonNull HttpClient httpClient,
            final @NonNull RomyRobotConfiguration config) {
        this.config = config;
        if (config.hostname.startsWith("http://") || config.hostname.startsWith("https://")) {
            this.hostname = config.hostname;
        } else {
            this.hostname = "http://" + config.hostname;
        }

        this.http = new HttpRequestSender(httpClient);
    }

    /**
     * Returns the hostname and port formatted URL as a String.
     *
     * @return String representation of the OpenSprinkler API URL.
     */
    protected String getBaseUrl() {
        return hostname + ":" + config.port + "/";
    }

    @Override
    public void refresh() throws Exception {
        String returnContent;
        returnContent = http.sendHttpGet(getBaseUrl() + CMD_GET_ROBOT_ID, null);
        firmwareVersion = new ObjectMapper().readTree(returnContent).get("firmware").asText();
        if (firmwareVersion == null) {
            throw new Exception("There was a problem in the HTTP communication: firmware was empty.");
        }
        maps = http.sendHttpGet(getBaseUrl() + CMD_GET_MAPS, null);
        returnContent = http.sendHttpGet(getBaseUrl() + CMD_GET_POWER_STATUS, null);
        powerStatus = new ObjectMapper().readTree(returnContent).get("power_status").asText();

        returnContent = http.sendHttpGet(getBaseUrl() + CMD_GET_STATUS, null);
        JsonNode jsonNode = new ObjectMapper().readTree(returnContent);
        mode = jsonNode.get("mode").asText();
        activePumpVolume = jsonNode.get("active_pump_volume").asText();
        charging = jsonNode.get("charging").asText();
        batteryLevel = jsonNode.get("battery_level").asInt();

        returnContent = http.sendHttpGet(getBaseUrl() + CMD_GET_WIFI_STATUS, null);
        jsonNode = new ObjectMapper().readTree(returnContent);
        rssi = jsonNode.get("rssi").asInt();
    }

    @Override
    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    @Override
    public String getModeString() {
        return mode;
    }

    @Override
    public String getActivePumpVolume() {
        return activePumpVolume;
    }

    @Override
    public void setActivePumpVolume(@NonNull String volume) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setActivePumpVolume'");
    }

    @Override
    public String getStrategy() {
        return strategy;
    }

    @Override
    public void setStrategy(@NonNull String strategy) {
        this.strategy = strategy;
    }

    @Override
    public String getSuctionMode() {
        return suctionMode;
    }

    @Override
    public void setSuctionMode(@NonNull String suctionMode) {
        this.suctionMode = suctionMode;
    }

    @Override
    public int getBatteryLevel() {
        return batteryLevel;
    }

    @Override
    public String getChargingStatus() {
        return charging;
    }

    @Override
    public int getRssi() {
        return rssi;
    }

    @Override
    public String getPowerStatus() {
        return powerStatus;
    }

    @Override
    public String getAvailableMaps() {
        return maps;
    }

    /**
     * This class contains helper methods for communicating HTTP GET and HTTP POST
     * requests.
     *
     * @author Chris Graham - Initial contribution
     * @author Florian Schmidt - Reduce visibility of Http communication to Api
     */
    protected class HttpRequestSender {
        private static final int HTTP_OK_CODE = 200;
        private static final String USER_AGENT = "Mozilla/5.0";

        private final HttpClient httpClient;

        public HttpRequestSender(HttpClient httpClient) {
            this.httpClient = httpClient;
        }

        /**
         * Given a URL and a set parameters, send a HTTP GET request to the URL location
         * created by the URL and parameters.
         *
         * @param url The URL to send a GET request to.
         * @param urlParameters List of parameters to use in the URL for the GET
         *            request. Null if no parameters.
         * @return String contents of the response for the GET request.
         * @throws Exception
         */
        public String sendHttpGet(String url, @Nullable String urlParameters) throws Exception {
            String location = null;
            if (urlParameters != null) {
                location = url + "?" + urlParameters;
            } else {
                location = url;
            }
            ContentResponse response;
            try {
                response = withGeneralProperties(httpClient.newRequest(location))
                        .timeout(config.timeout, TimeUnit.SECONDS).method(HttpMethod.GET).send();
            } catch (Exception e) {
                throw new Exception("Request to RomyRobot device failed: " + e.getMessage());
            }
            if (response.getStatus() != HTTP_OK_CODE) {
                throw new Exception(
                        "Error sending HTTP GET request to " + url + ". Got response code: " + response.getStatus());
            }
            String content = response.getContentAsString();
            if ("{\"result\":2}".equals(content)) {
                throw new Exception("Unauthorized, check your robot was unlocked");
            }
            return content;
        }

        private Request withGeneralProperties(Request request) {
            // TODO: request header configuration
            /*
             * request.header(HttpHeader.USER_AGENT, USER_AGENT);
             * if (!config.basicUsername.isEmpty() && !config.basicPassword.isEmpty()) {
             * String encoded = Base64.getEncoder().encodeToString(
             * (config.basicUsername + ":" + config.basicPassword).getBytes(StandardCharsets.UTF_8));
             * request.header(HttpHeader.AUTHORIZATION, "Basic " + encoded);
             * }
             */
            return request;
        }

        /**
         * Given a URL and a set parameters, send a HTTP POST request to the URL
         * location created by the URL and parameters.
         *
         * @param url The URL to send a POST request to.
         * @param urlParameters List of parameters to use in the URL for the POST
         *            request. Null if no parameters.
         * @return String contents of the response for the POST request.
         * @throws Exception
         */
        public String sendHttpPost(String url, String urlParameters) throws Exception {
            ContentResponse response;
            try {
                response = withGeneralProperties(httpClient.newRequest(url)).timeout(config.timeout, TimeUnit.SECONDS)
                        .method(HttpMethod.POST).content(new StringContentProvider(urlParameters)).send();
            } catch (Exception e) {
                throw new Exception("Request to RomyRobot device failed: " + e.getMessage());
            }
            if (response.getStatus() != HTTP_OK_CODE) {
                throw new Exception(
                        "Error sending HTTP POST request to " + url + ". Got response code: " + response.getStatus());
            }
            return response.getContentAsString();
        }
    }
}
