package org.openhab.binding.eightdevices.connection;

import org.openhab.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EightDevicesConnection {
    private final Logger logger = LoggerFactory.getLogger(EightDevicesConnection.class);

    private static final String WEBSERVICE_URL = "http://192.168.100.244:8888/";

    private static final String METHOD = "GET";

    private static final int TIMEOUT = 1 * 1000; // 1s

    public String getResponseFromQuery(String path) {
        // try {
        return HttpUtil.executeUrl(METHOD, WEBSERVICE_URL + path, TIMEOUT);

        /*
         * } catch (IOException e) {
         * logger.warn("Communication error occurred: {}", e.getMessage());
         * }
         */

        // return null;
    }
}
