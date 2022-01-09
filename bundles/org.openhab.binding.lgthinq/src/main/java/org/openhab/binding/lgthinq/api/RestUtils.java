package org.openhab.binding.lgthinq.api;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.openhab.binding.lgthinq.handler.BridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class RestUtils {
    private static final Logger logger = LoggerFactory.getLogger(RestUtils.class);

    public static byte[] getOauth2Sig(String messageSign, String secret) {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        byte[] messageCrypt = new HmacUtils(HmacAlgorithms.HMAC_SHA_1, secretBytes).hmac(messageSign.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encode(messageCrypt);
    }

    public static byte[] getTokenSignature(String authUrl, String secretKey, Map<String, String> empData, String timestamp) {
        UriBuilder builder = UriBuilder.fromUri(authUrl);
        if (empData != null) {
            empData.forEach((k, v) -> {
                builder.queryParam(k, v);
            });
        }

        URI reqUri = builder.build();
        String signUrl = (empData != null && empData.size() >0) ? reqUri.getPath() + "?" + reqUri.getRawQuery() : reqUri.getPath();
        String messageToSign = String.format("%s\n%s", signUrl, timestamp);
        byte[] oauthSig = getOauth2Sig(messageToSign, secretKey);
        return oauthSig;
    }

    public  static RestResult getCall(String encodedUrl, Map<String, String> headers, Map<String, String> params) throws IOException {
        UriBuilder builder = UriBuilder.fromUri(encodedUrl);
        if (params != null) {
            params.forEach((k, v) -> {
                builder.queryParam(k, v);
            });
        }
        URI encodedUri = builder.build();

        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(encodedUri);
        if (headers != null) headers.forEach((k, v) -> {
            request.setHeader(k, v);
        });

        HttpResponse resp = client.execute(request);
        return new RestResult(EntityUtils.toString(resp.getEntity(), "UTF-8"),
                resp.getStatusLine().getStatusCode());
    }

    public static RestResult postCall(String encodedUrl, Map<String, String> headers, Map<String, String> formParams) throws IOException {
        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(encodedUrl);
        List<NameValuePair> pairs = new ArrayList<>();
        if (formParams != null) formParams.forEach((k, v) -> {
            pairs.add(new BasicNameValuePair(k, v));
        });
        if (headers != null) headers.forEach((k, v) -> {
            request.setHeader(k, v);
        });
        try {
            request.setEntity(new UrlEncodedFormEntity(pairs));
        } catch (UnsupportedEncodingException e) {
            logger.error("Definitively, it is unexpected.", e);
            return null;
        }
        HttpResponse resp = client.execute(request);
        return new RestResult(EntityUtils.toString(resp.getEntity(), "UTF-8"),
                resp.getStatusLine().getStatusCode());
    }

}
