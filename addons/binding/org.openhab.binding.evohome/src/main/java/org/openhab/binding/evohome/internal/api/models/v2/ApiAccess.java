package org.openhab.binding.evohome.internal.api.models.v2;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.evohome.internal.api.models.v2.response.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ApiAccess {
    private static final Logger logger = LoggerFactory.getLogger(ApiAccess.class);
    private static Authentication authenticationData;
    private static String applicationId;

    public void setAuthentication(Authentication authentication) {
        ApiAccess.authenticationData = authentication;
    }

    public void setApplicationId(String applicationId) {
        ApiAccess.applicationId = applicationId;
    }

    @SuppressWarnings("unchecked")
    public <TIn, TOut> TOut doRequest(
            HttpMethod          method,
            String              url,
            Map<String, String> headers,
            TIn                 requestContainer,
            TOut                out) {
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

            if (requestContainer != null) {
                Gson gson = new GsonBuilder().create();
                String json = gson.toJson(requestContainer);
                request.method(method).content(new StringContentProvider(json), "application/json");
            }

            String reply = request.send().getContentAsString();
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

    public <TIn, TOut> TOut doAuthenticatedRequest(
            HttpMethod          method,
            String              url,
            Map<String, String> headers,
            TIn                 requestContainer,
            TOut                out) {

        if (authenticationData != null) {
            if (headers == null) {
                headers = new HashMap<String,String>();
            }

            headers.put("Authorization", "bearer " + authenticationData.AccessToken);
            headers.put("applicationId", applicationId);
            headers.put("Accept", "application/json, application/xml, text/json, text/x-json, text/javascript, text/xml");
        }

        return doRequest(method, url, headers, requestContainer, out);
    }
}
