package org.openhab.binding.gruenbecksoftener.handler;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.httpclient.HttpException;
import org.openhab.binding.gruenbecksoftener.internal.SoftenerConfiguration;
import org.openhab.binding.gruenbecksoftener.json.SoftenerEditData;
import org.openhab.binding.gruenbecksoftener.json.SoftenerXmlResponse;

public class MockResponseFunction implements ResponseFunction {

    @Override
    public BiConsumer<SoftenerConfiguration, Stream<SoftenerInputData>> getResponseFunction(
            Function<String, SoftenerXmlResponse> responseConsumer, Consumer<SoftenerXmlResponse> response) {
        return (config, inputData) -> {
            String responseBody = "<data><code>ok</code><D_Y_5>1234</D_Y_5><D_Y_10_1>abcd</D_Y_10_1></data>";
            response.accept(responseConsumer.apply(responseBody));
        };
    }

    @Override
    public SoftenerXmlResponse editParameter(SoftenerConfiguration config, SoftenerEditData edit,
            Function<String, SoftenerXmlResponse> responseConsumer) throws HttpException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

}
