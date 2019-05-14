package org.openhab.binding.somfymylink.internal;

public class SomfyMyLinkResponseBase {

    public String jsonrpc;
    public String error;
    public String id;

    public String getId() {
        return id;
    }

    public String getError() {
        return error;
    }

    public String getJsonRpc() {
        return jsonrpc;
    }
}
