package org.openhab.binding.evohome.internal.api;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.evohome.internal.api.models.v2.response.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ApiAccess {
    private final Logger logger = LoggerFactory.getLogger(ApiAccess.class);
    private final HttpClient httpClient;

    // TODO remove static?
    private static Authentication authenticationData;
    private static String applicationId;

    public ApiAccess(HttpClient httpClient){
        this.httpClient = httpClient;
    }

    public void setAuthentication(Authentication authentication) {
        ApiAccess.authenticationData = authentication;
    }

    public void setApplicationId(String applicationId) {
        ApiAccess.applicationId = applicationId;
    }

    @SuppressWarnings("unchecked")
    public <TOut> TOut doRequest(HttpMethod method, String url, Map<String, String> headers, String requestData,
            String contentType, TOut out) {

        logger.debug("Requesting: [{}]", url);

        try {
            Request request = httpClient.newRequest(url).method(method);

            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    request.header(header.getKey(), header.getValue());
                }
            }

            if (requestData != null) {
                request.content(new StringContentProvider(requestData), contentType);
            }

            ContentResponse response = request.send();

            logger.debug("Response: {}\n{}\n{}", response.toString(), response.getHeaders().toString(),
                    response.getContentAsString());

            if ((response.getStatus() == HttpStatus.OK_200) || (response.getStatus() == HttpStatus.ACCEPTED_202)) {
                String reply = response.getContentAsString();
                if (out != null) {
                    out = (TOut) new Gson().fromJson(reply, out.getClass());
                }
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.error("Error in handling request", e);
        } catch (Exception e) {
            logger.error("Generic error in handling request", e);
        }

        return out;
    }

    @SuppressWarnings("unchecked")
    public <TIn, TOut> TOut doRequest(HttpMethod method, String url, Map<String, String> headers, TIn requestContainer,
            TOut out) {

        logger.debug("JSON request");

        String json = null;
        if (requestContainer != null) {
            Gson gson = new GsonBuilder().create();
            json = gson.toJson(requestContainer);
        }

        return doRequest(method, url, headers, json, "application/json", out);
    }

    public <TIn, TOut> TOut doAuthenticatedRequest(HttpMethod method, String url, Map<String, String> headers,
            TIn requestContainer, TOut out) {

        logger.debug("AUTH request");

        if (authenticationData != null) {
            if (headers == null) {
                headers = new HashMap<String, String>();
            }

            headers.put("Authorization", "bearer " + authenticationData.accessToken);
            headers.put("applicationId", applicationId);
            headers.put("Accept", "application/json, application/xml, text/json, text/x-json, text/javascript, text/xml");
        }

        return doRequest(method, url, headers, requestContainer, out);
    }
}
