package org.openhab.binding.rf24.wifi;

import com.google.common.base.Preconditions;

import pl.grzeslowski.smarthome.common.io.id.TransmitterId;

public class X implements AutoCloseable {
    private final WiFi wifi;
    private final TransmitterId transmitterId;
    private final Rf24Thread rf24Thread;

    public X(WiFi wifi, TransmitterId transmitterId) {
        this.wifi = Preconditions.checkNotNull(wifi);
        this.transmitterId = Preconditions.checkNotNull(transmitterId);
        this.rf24Thread = new Rf24Thread(wifi, transmitterId);
    }

    public void init() {
        wifi.init();
        rf24Thread.start();
    }

    @Override
    public void close() {
        rf24Thread.close();
        wifi.close();
    }

    public TransmitterId getTransmitterId() {
        return transmitterId;
    }

    public WiFi getWiFi() {
        return wifi;
    }

    public TransmitterId geTransmitterId() {
        return transmitterId;
    }
}
