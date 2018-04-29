package org.openhab.binding.openthermgateway.internal;

public interface OpenThermGatewayCallback {
    void connecting();

    void connected();

    void disconnected();

    void receiveMessage(Message message);
}
