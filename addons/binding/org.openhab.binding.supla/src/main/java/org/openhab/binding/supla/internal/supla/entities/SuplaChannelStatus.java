package org.openhab.binding.supla.internal.supla.entities;

public final class SuplaChannelStatus {
    private final boolean connected;
    private final boolean enabled;

    public SuplaChannelStatus(boolean connected, boolean enabled) {
        this.connected = connected;
        this.enabled = enabled;
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String toString() {
        return "SuplaChannelStatus{" +
                "connected=" + connected +
                ", enabled=" + enabled +
                '}';
    }
}
