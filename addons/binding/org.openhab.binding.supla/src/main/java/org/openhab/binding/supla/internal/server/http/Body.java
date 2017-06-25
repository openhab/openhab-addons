package org.openhab.binding.supla.internal.server.http;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Body {
    private final Map<String, String> body;

    public Body(Map<String, String> body) {
        if(body == null) {
            this.body = new HashMap<>();
        } else {
            this.body = ImmutableMap.copyOf(body);
        }
    }

    public Body() {
        this(null);
    }

    public Map<String, String> getBody() {
        return body;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Body)) return false;

        Body body1 = (Body) o;

        return body.equals(body1.body);
    }

    @Override
    public int hashCode() {
        return body.hashCode();
    }

    @Override
    public String toString() {
        return "Body{" +
                "body=" + body +
                '}';
    }
}
