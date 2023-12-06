package org.openhab.binding.salusbinding.internal.rest;

import java.util.Map;
import java.util.function.Function;

public interface RestClient {
    static RestClient instance(String username, char[] password, String baseUrl) {
        return new ApacheHttpClient(username, password, baseUrl);
    }

    Response<String> get(String url);

    record Response<T>(int statusCode, T body) {
        public <Y> Response<Y> map(Function<T, Y> mapper) {
            return new Response<>(statusCode, mapper.apply(body));
        }
    }

}
