package org.openhab.binding.gruenbecksoftener.handler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.openhab.binding.gruenbecksoftener.internal.SoftenerConfiguration;
import org.openhab.binding.gruenbecksoftener.json.SoftenerEditData;
import org.openhab.binding.gruenbecksoftener.json.SoftenerXmlResponse;

public class HttpResponseFunction implements ResponseFunction {

    @Override
    public BiConsumer<SoftenerConfiguration, Stream<SoftenerInputData>> getResponseFunction(
            Function<String, SoftenerXmlResponse> responseConsumer, Consumer<SoftenerXmlResponse> response) {
        return (config, inputData) -> {

            HttpClient client = createHttpClient();
            PostMethod post = createPostMethod(config);

            // Installateur-Ebene Code 113
            // 2.7 Kundendienst-Ebene Code 142
            // 2.9 Tastbetrieb Code 653
            // 2.10 Zählerstände, Fehlerspeicher Ebene Code 245
            // 2.11 Rücksetzen von Zählerständen Code 189
            Map<String, List<SoftenerInputData>> inputByCode = inputData
                    .collect(Collectors.groupingBy(SoftenerInputData::getCode));
            inputByCode.entrySet().stream().forEach(entry -> {
                String show = entry.getValue().stream().map(SoftenerInputData::getDatapointId)
                        .collect(Collectors.joining("|"));
                String request = "id=624";
                String code = entry.getKey();
                if (code != null && !code.isEmpty()) {
                    request += code;
                }
                String body = request + "&show=" + show + "~";
                try {
                    post.setRequestEntity(new StringRequestEntity(body, "application/x-www-form-urlencoded", "UTF-8"));
                    client.executeMethod(post);
                    String responseBody = IOUtils.toString(post.getResponseBodyAsStream());
                    response.accept(responseConsumer.apply(responseBody));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        };
    }

    private PostMethod createPostMethod(SoftenerConfiguration config) {
        String urlStr = buildRequestURL(config);
        PostMethod post = new PostMethod(urlStr);
        post.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        // post.addRequestHeader("Cookie", "id=624, HaerteUnit=0");
        post.addRequestHeader("Connection", "keep-alive");
        return post;
    }

    private HttpClient createHttpClient() {
        HttpClient client = new HttpClient();
        HttpClientParams clientParams = new HttpClientParams();
        clientParams.setSoTimeout(5000);
        client.setParams(clientParams);
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
            Function<String, SoftenerXmlResponse> responseConsumer) throws HttpException, IOException {
        HttpClient client = createHttpClient();
        PostMethod postMethod = createPostMethod(config);
        // postMethod.setRequestBody(
        // "edit=D_C_1_1>2&id=2003&show=D_C_4_1|D_C_4_3|D_C_1_1|D_C_4_2|D_C_5_1|D_C_6_1|D_C_8_1|D_C_8_2|D_D_1|D_E_1|D_Y_9|D_Y_9_8|D_Y_9_24|D_C_7_1~");
        String body = "edit=" + edit.getDatapointId() + ">" + edit.getValue() + "&id=624&show=" + edit.getDatapointId()
                + "~";
        postMethod.setRequestEntity(new StringRequestEntity(body, "application/x-www-form-urlencoded", "UTF-8"));
        client.executeMethod(postMethod);
        String responseBody = IOUtils.toString(postMethod.getResponseBodyAsStream());
        return responseConsumer.apply(responseBody);
    }
}
