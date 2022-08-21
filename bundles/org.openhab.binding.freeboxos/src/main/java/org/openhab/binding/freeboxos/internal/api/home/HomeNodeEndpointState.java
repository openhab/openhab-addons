package org.openhab.binding.freeboxos.internal.api.home;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.Response;

import com.google.gson.JsonElement;

@NonNullByDefault
public class HomeNodeEndpointState {
    public static class HomeNodeEndpointStateResponse extends Response<HomeNodeEndpointState> {
    }

    private @Nullable JsonElement value;

    private @Nullable String valueType;

    private int refresh;

    public @Nullable JsonElement getValue() {
        return value;
    }

    public @Nullable String getValueType() {
        return valueType;
    }

    public long getRefresh() {
        return refresh;
    }

    public @Nullable Boolean asBoolean() {
        if (value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isBoolean()) {
            return value.getAsBoolean();
        }
        return null;
    }

    public @Nullable Integer asInt() {
        if (value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
            return value.getAsInt();
        }
        return null;
    }
}
