package org.openhab.binding.jellyfin.internal.api;

public class JellyfinOptions {
    private final ClientInfo clientInfo;
    private final DeviceInfo deviceInfo;
    private final boolean enableDebugging;

    private JellyfinOptions(Builder builder) {
        this.clientInfo = builder.clientInfo;
        this.deviceInfo = builder.deviceInfo;
        this.enableDebugging = builder.enableDebugging;
    }

    public ClientInfo getClientInfo() {
        return clientInfo;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public boolean isEnableDebugging() {
        return enableDebugging;
    }

    public static class Builder {
        private ClientInfo clientInfo;
        private DeviceInfo deviceInfo;
        private boolean enableDebugging = false;

        public Builder setClientInfo(ClientInfo clientInfo) {
            this.clientInfo = clientInfo;
            return this;
        }

        public Builder setDeviceInfo(DeviceInfo deviceInfo) {
            this.deviceInfo = deviceInfo;
            return this;
        }

        public Builder setEnableDebugging(boolean enableDebugging) {
            this.enableDebugging = enableDebugging;
            return this;
        }

        public JellyfinOptions build() {
            return new JellyfinOptions(this);
        }
    }
}
