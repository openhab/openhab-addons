package org.openhab.binding.gruenbecksoftener.handler;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.openhab.binding.gruenbecksoftener.data.SoftenerEditData;
import org.openhab.binding.gruenbecksoftener.data.SoftenerInputData;
import org.openhab.binding.gruenbecksoftener.data.SoftenerXmlResponse;
import org.openhab.binding.gruenbecksoftener.internal.SoftenerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpResponseFunction implements ResponseFunction {

    private final Logger logger = LoggerFactory.getLogger(HttpResponseFunction.class);

    @Override
    public BiConsumer<SoftenerConfiguration, Stream<SoftenerInputData>> getResponseFunction(
            Function<String, SoftenerXmlResponse> responseConsumer, Consumer<SoftenerXmlResponse> response) {
        return (config, inputData) -> {

            HttpClient client = createHttpClient();
            try {
                client.start();
                // Installateur-Ebene Code 113
                // 2.7 Kundendienst-Ebene Code 142
                // 2.9 Tastbetrieb Code 653
                // 2.10 Zählerstände, Fehlerspeicher Ebene Code 245
                // 2.11 Rücksetzen von Zählerständen Code 189
                Map<String, List<SoftenerInputData>> inputByCode = inputData
                        .collect(Collectors.groupingBy(SoftenerInputData::getCode));
                inputByCode.entrySet().stream().forEach(entry -> {
                    Request post = createPostMethod(config, client);
                    String show = entry.getValue().stream().map(SoftenerInputData::getDatapointId)
                            .collect(Collectors.joining("|"));
                    String request = "id=624";
                    String code = entry.getKey();
                    if (code != null && !code.isEmpty()) {
                        request += "&code=" + code;
                    }
                    String body = request + "&show=" + show + "~";
                    try {
                        ContentResponse httpResponse = post
                                .content(new StringContentProvider("application/x-www-form-urlencoded", body,
                                        Charset.forName("UTF-8")))
                                .send();

                        response.accept(responseConsumer.apply(httpResponse.getContentAsString()));
                    } catch (InterruptedException | TimeoutException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (Exception e) {
                throw new RuntimeException("Failed to start HTTP client", e);
            } finally {
                try {
                    client.stop();
                } catch (Exception e) {
                    logger.warn("Failed to stop HTTP client", e);
                }
            }
        };
    }

    private Request createPostMethod(SoftenerConfiguration config, HttpClient client) {
        String urlStr = buildRequestURL(config);
        Request post = client.POST(urlStr);
        post.header("Content-Type", "application/x-www-form-urlencoded");
        post.header("Connection", "keep-alive");
        return post;
    }

    private HttpClient createHttpClient() {
        HttpClient client = new HttpClient();
        client.setConnectTimeout(5000);
        return client;
    }

    /**
     * Build request URL from configuration data
     *
     */
    private String buildRequestURL(SoftenerConfiguration config) {

        String urlStr = "http://" + StringUtils.trimToEmpty(config.host) + "/mux_http";
        return urlStr;
    }

    @Override
    public SoftenerXmlResponse editParameter(SoftenerConfiguration config, SoftenerEditData edit,
            Function<String, SoftenerXmlResponse> responseConsumer) throws IOException {
        HttpClient client = createHttpClient();
        try {
            Request postMethod = createPostMethod(config, client);
            client.start();
            // postMethod.setRequestBody(
            // "edit=D_C_1_1>2&id=2003&show=D_C_4_1|D_C_4_3|D_C_1_1|D_C_4_2|D_C_5_1|D_C_6_1|D_C_8_1|D_C_8_2|D_D_1|D_E_1|D_Y_9|D_Y_9_8|D_Y_9_24|D_C_7_1~");
            String body = "edit=" + edit.getDatapointId() + ">" + edit.getValue() + "&id=624&show="
                    + edit.getDatapointId() + "~";
            body += edit.getCode() != null ? ("&code=" + edit.getCode()) : "";
            ContentResponse response;
            try {
                response = postMethod.content(
                        new StringContentProvider("application/x-www-form-urlencoded", body, Charset.forName("UTF-8")))
                        .send();
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                throw new IOException(e);
            }
            return responseConsumer.apply(response.getContentAsString());
        } catch (Exception e) {
            throw new IOException("Failed to start HTTP client", e);
        } finally {
            try {
                client.stop();
            } catch (Exception e) {
                logger.warn("Failed to stop HTTP client", e);
            }
        }
    }
}
