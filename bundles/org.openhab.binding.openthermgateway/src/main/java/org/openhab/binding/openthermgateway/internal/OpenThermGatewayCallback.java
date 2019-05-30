package org.openhab.binding.openthermgateway.internal;

import org.eclipse.jdt.annotation.NonNull;

public interface OpenThermGatewayCallback {
    public void connecting();

    public void connected();

    public void disconnected();

    public void receiveMessage(@NonNull Message message);

    public void log(@NonNull LogLevel loglevel, @NonNull String message);

    public void log(@NonNull LogLevel loglevel, @NonNull String format, @NonNull String arg);

    public void log(@NonNull LogLevel loglevel, @NonNull String format, @NonNull Throwable t);
}
