package org.openhab.binding.evcc.internal.handler;

import com.google.gson.JsonObject;

public interface EvccJsonAwareHandler {
    void updateFromEvccState(JsonObject root);
}
