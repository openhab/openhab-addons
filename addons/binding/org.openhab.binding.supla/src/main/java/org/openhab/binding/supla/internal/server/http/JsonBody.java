package org.openhab.binding.supla.internal.server.http;

import org.openhab.binding.supla.internal.mappers.JsonMapper;

import java.nio.charset.Charset;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class JsonBody extends Body {
    private final JsonMapper mapper;

    public JsonBody(Map<String, String> body, JsonMapper mapper) {
        super(body);
        this.mapper = checkNotNull(mapper);
    }

    @Override
    public byte[] buildBytesToSend() {
        return mapper.map(body).getBytes(Charset.forName("UTF-8"));
    }
}
