package org.openhab.binding.eightdevices.connection;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.openhab.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EightDevicesConnection {
    private final Logger logger = LoggerFactory.getLogger(EightDevicesConnection.class);

    private static final String WEBSERVICE_URL = "http://192.168.100.244:8888/";

    private static final int TIMEOUT = 1 * 1000; // 1s

    private String args;

    private final static String argsCallback = "{" + "      \"url\": \"http://192.168.100.15:5727/notification/\","
            + "      \"headers\": {}" + "}";

    public String getResponseFromQuery(String path) {
        return HttpUtil.executeUrl("GET", WEBSERVICE_URL + path, TIMEOUT);
    }

    public String SetCallback() {
        InputStream stream = new ByteArrayInputStream(argsCallback.getBytes());
        return HttpUtil.executeUrl("PUT", WEBSERVICE_URL + "notification/callback", stream, "application/json",
                TIMEOUT);
    }

    public String PutRequest(String path, Boolean arg) {

        if (arg) {
            args = "{" + "      \"type\": \"Buffer\"," + "      \"data\": [225,22,218,1]" + "}";
        }
        if (!arg) {
            args = "{" + "      \"type\": \"Buffer\"," + "      \"data\": [225,22,218,0]" + "}";
        }
        InputStream stream = new ByteArrayInputStream(args.getBytes());
        return HttpUtil.executeUrl("PUT", WEBSERVICE_URL + path, stream, "application/vnd.oma.lwm2m+tlv", TIMEOUT);
    }
}
