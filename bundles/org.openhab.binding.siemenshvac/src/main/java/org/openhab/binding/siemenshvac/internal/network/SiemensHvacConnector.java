package org.openhab.binding.siemenshvac.internal.network;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;
import org.openhab.binding.siemenshvac.internal.handler.SiemensHvacBridgeBaseThingHandler;

import com.google.gson.JsonObject;

public interface SiemensHvacConnector {

    public JsonObject DoRequest(@Nullable String req, @Nullable SiemensHvacCallback callback);

    public void WaitAllPendingRequest();

    public void onComplete(Request request);

    public void onError(Request request);

    public void setSiemensHvacBridgeBaseThingHandler(
            @Nullable SiemensHvacBridgeBaseThingHandler hvacBridgeBaseThingHandler);
}
