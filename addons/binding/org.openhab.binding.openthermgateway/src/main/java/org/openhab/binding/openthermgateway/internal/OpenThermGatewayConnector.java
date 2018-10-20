package org.openhab.binding.openthermgateway.internal;

public interface OpenThermGatewayConnector extends Runnable {
    public void sendCommand(GatewayCommand command);

    public boolean isConnected();

    public void stop();
}