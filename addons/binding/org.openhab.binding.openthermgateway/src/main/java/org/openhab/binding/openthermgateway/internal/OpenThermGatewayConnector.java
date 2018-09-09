package org.openhab.binding.openthermgateway.internal;

public interface OpenThermGatewayConnector extends Runnable {
    public void sendCommand(CommandType command, String message);

    public boolean isConnected();
}