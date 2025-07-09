package org.openhab.binding.evcc.internal.handler;

import org.eclipse.jdt.annotation.NonNull;

import com.google.gson.JsonObject;

public interface EvccJsonAwareHandler {
    void updateFromEvccState(@NonNull JsonObject root);
}
