package org.openhab.binding.rf24.wifi;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;

import pl.grzeslowski.smarthome.common.io.id.IdUtils;
import pl.grzeslowski.smarthome.common.io.id.TransmitterId;

public class X implements AutoCloseable {
    private static final long INITIAL_DELAY = TimeUnit.SECONDS.toMillis(5);
    private static final long DELAY = TimeUnit.SECONDS.toMillis(5);

    private final WiFi wifi;
    private final TransmitterId transmitterId;
    private final Rf24Thread rf24Thread;
    private ScheduledExecutorService executor;

    public X(IdUtils idUtils, WiFi wifi, TransmitterId transmitterId, ScheduledExecutorService executor) {
        this.wifi = Preconditions.checkNotNull(wifi);
        this.transmitterId = Preconditions.checkNotNull(transmitterId);
        this.executor = Preconditions.checkNotNull(executor);
        this.rf24Thread = new Rf24Thread(idUtils, wifi, transmitterId);
    }

    public void init() {
        wifi.init();
        executor.scheduleWithFixedDelay(rf24Thread, INITIAL_DELAY, DELAY, TimeUnit.MILLISECONDS);
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
