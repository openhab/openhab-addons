package org.openhab.binding.dbquery.internal.domain;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public class QueryParameters {
    private final Map<String, @Nullable Object> params;

    public QueryParameters() {
        this.params = new HashMap<>();
    }

    public QueryParameters(Map<String, @Nullable Object> params) {
        this.params = params;
    }

    public void setParameter(String name, @Nullable Object value) {
        params.put(name, value);
    }

    public @Nullable Object getParameter(String paramName) {
        return params.get(paramName);
    }

    public int size() {
        return params.size();
    }
}
