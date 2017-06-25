package org.openhab.binding.supla.internal.server.http;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class Body {
    protected final Map<String, String> body;

    protected Body(Map<String, String> body) {
        if(body == null) {
            this.body = ImmutableMap.of();
        } else {
            this.body = ImmutableMap.copyOf(body);
        }
    }

    protected Body() {
        this(null);
    }

    public Map<String, String> getBody() {
        return body;
    }

    public abstract byte[] buildBytesToSend();

    @Override
    public String toString() {
        return "Body{" +
                "body=" + body +
                '}';
    }
}
