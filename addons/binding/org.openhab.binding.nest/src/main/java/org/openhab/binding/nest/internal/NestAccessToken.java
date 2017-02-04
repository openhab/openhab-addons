package org.openhab.binding.nest.internal;

import java.io.IOException;
import java.time.LocalTime;
import java.util.Properties;

import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.nest.NestBindingConstants;
import org.openhab.binding.nest.config.NestBridgeConfiguration;
import org.openhab.binding.nest.internal.data.AccessTokenData;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Keeps track of the access token, refreshing it if needed.
 *
 * @author David Bennett
 */
public class NestAccessToken {
    private NestBridgeConfiguration config;
    private String access_token;
    private LocalTime expirationTime;

    public NestAccessToken(NestBridgeConfiguration config) {
        this.config = config;
    }

    /** Get the current access token, refreshing if needed. */
    public String getAccessToken() throws IOException {
        if (expirationTime == null || expirationTime.isAfter(LocalTime.now())) {
            refreshAccessToken();
        }
        return access_token;
    }

    private void refreshAccessToken() throws IOException {
        Properties httpHeader = new Properties();
        httpHeader.setProperty("client_id", config.clientId);
        httpHeader.setProperty("client_secret", config.clientSecret);
        httpHeader.setProperty("code", config.pincode);
        httpHeader.setProperty("grant_type", "authorization_code");
        String result = HttpUtil.executeUrl(HttpMethod.POST.toString(), NestBindingConstants.NEST_ACCESS_TOKEN_URL,
                httpHeader, null, "text/plain", 120);
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        AccessTokenData data = gson.fromJson(result, AccessTokenData.class);
        access_token = data.getAccessToken();
        // Update the expiration Code.
        expirationTime = LocalTime.now();
        expirationTime.plusSeconds(data.getExpiresIn());
    }
}
