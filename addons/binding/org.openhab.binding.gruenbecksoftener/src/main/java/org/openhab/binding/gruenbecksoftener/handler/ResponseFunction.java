package org.openhab.binding.gruenbecksoftener.handler;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.openhab.binding.gruenbecksoftener.data.SoftenerEditData;
import org.openhab.binding.gruenbecksoftener.data.SoftenerInputData;
import org.openhab.binding.gruenbecksoftener.data.SoftenerXmlResponse;
import org.openhab.binding.gruenbecksoftener.internal.SoftenerConfiguration;

public interface ResponseFunction {

    public BiConsumer<SoftenerConfiguration, Stream<SoftenerInputData>> getResponseFunction(
            Function<String, SoftenerXmlResponse> responseConsumer, Consumer<SoftenerXmlResponse> response);

    public SoftenerXmlResponse editParameter(SoftenerConfiguration config, SoftenerEditData edit,
            Function<String, SoftenerXmlResponse> responseConsumer) throws IOException;
}
