package org.openhab.binding.evohome.internal.api.handlers;

import java.net.URLEncoder;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.evohome.internal.api.EvohomeApiConstants;
import org.openhab.binding.evohome.internal.api.models.AuthenticationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class AuthenticationHandler extends HandlerBase {
    private final Logger logger = LoggerFactory.getLogger(AuthenticationHandler.class);

    private String applicationId;
    private AuthenticationResponse authenticationData = null;

    public AuthenticationResponse getAuthenticationData() {
        return authenticationData;
    }

    public boolean login(String username, String password, String applicationId) {
        this.applicationId = applicationId;

        SslContextFactory sslContextFactory = new SslContextFactory();
        HttpClient httpClient = new HttpClient(sslContextFactory);
        try {
            httpClient.start();

            // Building the http request discretely here, as it is the only one with a deviant content type
            Request request = httpClient.newRequest(EvohomeApiConstants.URL_AUTH);
            request.method(HttpMethod.POST);
            request.header("Authorization", "Basic YjAxM2FhMjYtOTcyNC00ZGJkLTg4OTctMDQ4YjlhYWRhMjQ5OnRlc3Q=");
            request.header("Accept", "application/json, application/xml, text/json, text/x-json, text/javascript, text/xml");

            String data = "Username=" + URLEncoder.encode(username, "UTF-8") + "&"
                    + "Password=" + URLEncoder.encode(password, "UTF-8") + "&"
                    + "Host=rs.alarmnet.com%2F&"
                    + "Pragma=no-cache&"
                    + "Cache-Control=no-store+no-cache&"
                    + "scope=EMEA-V1-Basic+EMEA-V1-Anonymous+EMEA-V1-Get-Current-User-Account&"
                    + "grant_type=password&"
                    + "Content-Type=application%2Fx-www-form-urlencoded%3B+charset%3Dutf-8&"
                    + "Connection=Keep-Alive";

            request.content(new StringContentProvider(data), "application/x-www-form-urlencoded");

            ContentResponse response = request.send();
            if (response.getStatus() == 200) {
                String reply = response.getContentAsString();
                authenticationData =  new Gson().fromJson(reply, AuthenticationResponse.class);
            } else {
                authenticationData = null;
            }
        } catch (Exception e) {
            authenticationData = null;
            logger.error("Authorization failed",e);
        }

        return authenticationData != null;
    }

    public String getApplicationId() {
        return applicationId;
    }
}
