package org.openhab.binding.blink.internal.service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;

@NonNullByDefault
public class BaseBlinkApiService {

    private final Logger logger = LoggerFactory.getLogger(BaseBlinkApiService.class);

    private static final String HEADER_TOKEN_AUTH = "token-auth";
    private final String BASE_URL = "https://rest-{tier}.immedia-semi.com";
    public static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";

    HttpClient httpClient;
    Gson gson;

    protected BaseBlinkApiService(HttpClient httpClient, Gson gson) {
        this.httpClient = httpClient;
        this.gson = gson;
    }

    protected <T> T apiRequest(String tier, String uri, HttpMethod method, @Nullable String token,
            @Nullable Map<String, String> params, Class<T> classOfT) throws IOException {
        String json = request(tier, uri, method, token, params);
        return gson.fromJson(json, classOfT);
    }

    private String request(String tier, String uri, HttpMethod method, @Nullable String token,
            @Nullable Map<String, String> params)
            throws IOException {
        String baseUrl = BASE_URL.replace("{tier}", tier);
        String url = baseUrl + uri;
        try {
            final Request request = httpClient.newRequest(url).method(method.toString());
            if (params != null)
                params.forEach(request::param);
            request.header(HttpHeader.ACCEPT, CONTENT_TYPE_JSON);
            if (token != null)
                request.header(HEADER_TOKEN_AUTH, token);
            ContentResponse contentResponse = request.send();
            if (contentResponse.getStatus() != 200) {
                throw new IOException("Blink API Call unsuccessful <Status " + contentResponse.getStatus() + ">");
            }
            return contentResponse.getContentAsString();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.error("Error calling Blink API. Reason: {}", e.getMessage());
            throw new IOException(e);
        }
    }

}
