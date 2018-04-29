package org.openhab.binding.openthermgateway.internal;

public interface OpenThermGatewayConnector extends Runnable {
    void sendCommand(CommandType command, String message);
}