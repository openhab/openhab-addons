package org.openhab.binding.octoprint.internal.services;

import com.google.gson.Gson;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.octoprint.internal.models.OctopiServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class HttpRequestService {
    private final Logger logger = LoggerFactory.getLogger(HttpRequestService.class);

    private final OctopiServer server;
    HttpClient httpClient = new HttpClient();

    HttpRequestService(OctopiServer octopiServer) {
        server = octopiServer;
    }

    ContentResponse getRequest(String route) {
        String uri = String.format("http%1$s/%2$s", server.ip, route);
        logger.debug("uri: {}", uri);
        Request request = httpClient.newRequest(uri)
                .header("X-Api-Key", server.apiKey)
                .header(HttpHeader.ACCEPT, "application/json")
                .method(HttpMethod.GET);
        try {
            return request.send();
        } catch (InterruptedException e) {
            //TODO
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            //TODO
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            //TODO
            throw new RuntimeException(e);
        }
    }

    Response postRequest(String route, Gson body) {
        String uri = String.format("http%1$s/%2$s", server.ip, route);
        logger.debug("uri: {}", uri);
        Request request = httpClient.newRequest(uri)
                .header("X-Api-Key", server.apiKey)
                .header(HttpHeader.ACCEPT, "application/json")
                .header(HttpHeader.CONTENT_TYPE, "application/json")
                .method(HttpMethod.POST)
                .content(new StringContentProvider(body.toString()), "application/json");
        try {
            return request.send();
        } catch (InterruptedException e) {
            //TODO
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            //TODO
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            //TODO
            throw new RuntimeException(e);
        }
    }
}
