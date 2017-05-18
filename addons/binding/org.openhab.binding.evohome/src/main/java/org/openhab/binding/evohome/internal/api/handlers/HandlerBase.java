package org.openhab.binding.evohome.internal.api.handlers;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpProxy;
import org.eclipse.jetty.client.ProxyConfiguration;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HandlerBase {
    private static final Logger logger = LoggerFactory.getLogger(HandlerBase.class);

    @SuppressWarnings("unchecked")
    protected <TIn, TOut> TOut doRequest(HttpMethod method, String url, Map<String, String> headers,
            TIn requestContainer, TOut out) {
        try {
            SslContextFactory sslContextFactory = new SslContextFactory();
            sslContextFactory.setTrustAll(true);
            HttpClient httpClient = new HttpClient(sslContextFactory);
            httpClient.start();

            ProxyConfiguration proxyConfig = httpClient.getProxyConfiguration();
            HttpProxy proxy = new HttpProxy("localhost", 8888);
            proxyConfig.getProxies().add(proxy);

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
}
