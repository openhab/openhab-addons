package org.openhab.binding.restify.internal.config;

import static java.util.stream.Collectors.toMap;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.openhab.binding.restify.internal.RequestProcessor.Method;
import org.openhab.binding.restify.internal.Response;

public record Config(GlobalConfig globalConfig, Map<Endpoint, Response> responses) implements Serializable {
    public Config(GlobalConfig globalConfig, List<Response> responses) {
        this(globalConfig, responses.stream()
                .collect(toMap(response -> new Endpoint(response.path(), response.method()), Function.identity())));
    }

    public Optional<Response> findResponse(String path, Method method) {
        return Optional.ofNullable(responses.get(new Endpoint(path, method)));
    }

    public record Endpoint(String path, Method method) {
    }
}
