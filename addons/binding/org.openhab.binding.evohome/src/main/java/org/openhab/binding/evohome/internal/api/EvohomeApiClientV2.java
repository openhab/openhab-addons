package org.openhab.binding.evohome.internal.api;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.evohome.configuration.EvohomeGatewayConfiguration;
import org.openhab.binding.evohome.internal.api.models.v1.DataModelResponse;
import org.openhab.binding.evohome.internal.api.models.v2.Authentication;
import org.openhab.binding.evohome.internal.api.models.v2.Locations;
import org.openhab.binding.evohome.internal.api.models.v2.UserAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class EvohomeApiClientV2 implements EvohomeApiClient {
    private static final Logger logger = LoggerFactory.getLogger(EvohomeApiClientV2.class);

    private EvohomeGatewayConfiguration configuration = null;

    private Authentication authenticationData;

    private UserAccount useraccount;

    public EvohomeApiClientV2(EvohomeGatewayConfiguration configuration) {
        this.configuration = configuration;
    }

    @SuppressWarnings("unchecked")
    private <TIn, TOut> TOut doRequest(HttpMethod method, String url, Map<String, String> headers,
            TIn requestContainer, TOut out) {
        try {
            SslContextFactory sslContextFactory = new SslContextFactory();
            HttpClient httpClient = new HttpClient(sslContextFactory);
            httpClient.start();
            Request request = httpClient.newRequest(url).method(method);

            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    request.header(header.getKey(), header.getValue());
                }
            }

            if (requestContainer == null) {
                logger.debug("Do request '" + method + "' for url '" + url + "'");
            } else {
                Gson gson = new GsonBuilder().create();
                String json = gson.toJson(requestContainer);
                logger.debug("Do request '" + method + "' for url '" + url + "' with data '" + json + "'");
                request.method(method).content(new StringContentProvider(json), "application/json");
            }

            String reply = request.send().getContentAsString();
            logger.debug("Got reply: " + reply);

            if (out != null) {
                out = (TOut) new Gson().fromJson(reply, out.getClass());
            }

        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.error("Error in handling request", e);
        } catch (Exception e) {
            logger.error("Generic error in handling request", e);
        }

        return out;
    }

    private <TIn, TOut> TOut doAuthenticatedRequest(HttpMethod method, String url, Map<String, String> headers,
            TIn requestContainer, TOut out) {

        if (authenticationData != null) {
            if (headers == null) {
                headers = new HashMap<String,String>();
            }

            headers.put("Authorization", "bearer " + authenticationData.AccessToken);
            headers.put("applicationId", configuration.applicationId);
            headers.put("Accept", "application/json, application/xml, text/json, text/x-json, text/javascript, text/xml");
        }

        return doRequest(method, url, headers, requestContainer, out);
    }

    @Override
    public boolean login() {
        SslContextFactory sslContextFactory = new SslContextFactory();
        HttpClient httpClient = new HttpClient(sslContextFactory);
        try {
            httpClient.start();

            // Building the http request discretely here, as it is the only one with a deviant content type
            Request request = httpClient.newRequest(EvohomeApiConstants.URL_V2_AUTH);
            request.method(HttpMethod.POST);
            request.header("Authorization", "Basic YjAxM2FhMjYtOTcyNC00ZGJkLTg4OTctMDQ4YjlhYWRhMjQ5OnRlc3Q=");
            request.header("Accept", "application/json, application/xml, text/json, text/x-json, text/javascript, text/xml");

            String data = "Username=" + URLEncoder.encode(configuration.username, "UTF-8") + "&"
                    + "Password=" + URLEncoder.encode(configuration.password, "UTF-8") + "&"
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
                authenticationData =  new Gson().fromJson(reply, Authentication.class);
            } else {
                authenticationData = null;
            }
        } catch (Exception e) {
            authenticationData = null;
            logger.error("Authorization failed",e);
        }

        useraccount = getUserAccount();
        Locations locations = getLocations();


        return authenticationData != null;
    }

    public UserAccount getUserAccount() {
        String url = EvohomeApiConstants.URL_V2_BASE + EvohomeApiConstants.URL_V2_ACCOUNT;

        UserAccount userAccount =  new UserAccount();
        userAccount = doAuthenticatedRequest(HttpMethod.GET, url, null, null, userAccount);

        return userAccount;
    }

    public Locations getLocations() {
        Locations locations = null;
        if (useraccount != null) {
            String url = EvohomeApiConstants.URL_V2_BASE + EvohomeApiConstants.URL_V2_LOCATIONS;
            url = String.format(url, useraccount.UserId);

            locations = new Locations();
            locations = doAuthenticatedRequest(HttpMethod.GET, url, null, null, locations);
        }

        return locations;
    }

    @Override
    public void logout() {
        // userInfo = null;
    }

    @Override
    public DataModelResponse[] getData() {
        // TODO Auto-generated method stub
        return null;
    }

}
