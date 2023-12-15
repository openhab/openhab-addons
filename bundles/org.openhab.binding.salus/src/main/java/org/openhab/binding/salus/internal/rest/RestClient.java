package org.openhab.binding.salus.internal.rest;

import java.util.List;
import java.util.function.Function;

public interface RestClient {
    Response<String> get(String url, Header... headers);

    Response<String> post(String url, Content content, Header... headers);

    record Content(String body, String type) {
        public Content(String body) {
            this(body, "application/json");
        }
    }

    record Header(String name, List<String> values) {
        public Header(String name, String value) {
            this(name, List.of(value));
        }
    }

    record Response<T> (int statusCode, T body) {
        public <Y> Response<Y> map(Function<T, Y> mapper) {
            return new Response<>(statusCode, mapper.apply(body));
        }
    }
}
