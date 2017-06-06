package org.openhab.binding.nest.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.httpclient.util.URIUtil;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.nest.NestBindingConstants;
import org.openhab.binding.nest.config.NestBridgeConfiguration;
import org.openhab.binding.nest.internal.data.AccessTokenData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Keeps track of the access token, refreshing it if needed.
 *
 * @author David Bennett
 */
public class NestAccessToken {
    private Logger logger = LoggerFactory.getLogger(NestAccessToken.class);
    private NestBridgeConfiguration config;
    private String accessToken;

    public NestAccessToken(NestBridgeConfiguration config) {
        this.config = config;
    }

    /** Get the current access token, refreshing if needed. */
    public String getAccessToken() throws IOException {
        if (config.accessToken == null) {
            refreshAccessToken();
        } else {
            accessToken = config.accessToken;
        }
        return accessToken;
    }

    private void refreshAccessToken() throws IOException {

        try {
            StringBuilder stuff = new StringBuilder().append("client_id=").append(URIUtil.encodeQuery(config.clientId))
                    .append("&client_secret=").append(URIUtil.encodeQuery(config.clientSecret)).append("&code=")
                    .append(config.pincode).append("&grant_type=authorization_code");
            logger.info("Result " + stuff.toString());
            InputStream stream = new ByteArrayInputStream(stuff.toString().getBytes(StandardCharsets.UTF_8));
            String result = HttpUtil.executeUrl("POST", NestBindingConstants.NEST_ACCESS_TOKEN_URL, stream,
                    "application/x-www-form-urlencoded", 10000);
            logger.info("Result " + result);
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            AccessTokenData data = gson.fromJson(result, AccessTokenData.class);
            accessToken = data.getAccessToken();
            logger.info("Access token " + accessToken);
            logger.info("Expiration Time " + data.getExpiresIn());
        } catch (IOException e) {
            logger.error("Unable to get the nest access token ", e);
            throw e;
        }
    }
}
