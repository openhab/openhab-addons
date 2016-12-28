
package org.openhab.binding.solarlog.internal;

import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Collection of methods to help retrieve HTTP data from a SolarLog Server
 * Based on SqueezeBox HttpUtils
 *
 * @author Dan Cunningham
 * @author Svilen Valkanov - replaced Apache HttpClient with Jetty
 * @author Johann Richard - Adapted for SolarLog Binding
 */
public class HttpUtils {
    private static Logger logger = LoggerFactory.getLogger(HttpUtils.class);

    private static int TIMEOUT = 5000;
    private static HttpClient client = new HttpClient();
    /**
     * JSON request to get the Data from a SolarLog Device
     */
    private static final String JSON_REQ = "{\"801\":{\"170\":null}}";
    private static final String URL_POSTFIX = "/getjp";

    /**
     * Simple logic to perform a post request
     *
     * @param url
     * @param timeout
     * @return
     */
    public static String post(String url, String postData) throws Exception {
        if (!client.isStarted()) {
            client.start();
        }

        // @formatter:off
        ContentResponse response = client.newRequest(url)
                .method(HttpMethod.POST)
                .content(new StringContentProvider(postData))
                .timeout(TIMEOUT, TimeUnit.MILLISECONDS)
                .send();

        int statusCode = response.getStatus();

        if (statusCode != HttpStatus.OK_200) {
            String statusLine = response.getStatus() + " " + response.getReason();
            logger.error("Method failed: {}", statusLine);
            throw new Exception("Method failed: " + statusLine);
        }

        return response.getContentAsString();
    }

    /**
     * Retrieves the command line port (cli) from a SqueezeServer
     *
     * @param url
     * @return
     * @throws Exception
     */
    public static JsonElement getSolarLogData(String url) throws Exception {
        logger.trace("Posting JSON_REQ {} to URL {}", JSON_REQ, url + URL_POSTFIX);
        String json = HttpUtils.post(url + URL_POSTFIX, JSON_REQ);
        logger.trace("Recieved json from server {}", json);
        JsonElement resp = new JsonParser().parse(json);
        return resp;
    }

}