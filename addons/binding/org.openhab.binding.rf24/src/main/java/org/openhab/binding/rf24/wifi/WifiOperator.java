package org.openhab.binding.rf24.wifi;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.rf24.wifi.Rf24Thread.OnMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import pl.grzeslowski.smarthome.common.io.id.IdUtils;
import pl.grzeslowski.smarthome.common.io.id.TransmitterId;

public class WifiOperator implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(WifiOperator.class);
    private static final long INITIAL_DELAY = TimeUnit.SECONDS.toMillis(5);
    private static final long DELAY = TimeUnit.MILLISECONDS.toMillis(50);

    private final WiFi wifi;
    private final TransmitterId transmitterId;
    private final Rf24Thread rf24Thread;
    private ExecutorService executor;

    public WifiOperator(IdUtils idUtils, WiFi wifi, TransmitterId transmitterId, ExecutorService executor) {
        this.wifi = Preconditions.checkNotNull(wifi);
        this.transmitterId = Preconditions.checkNotNull(transmitterId);
        this.executor = Preconditions.checkNotNull(executor);
        this.rf24Thread = new Rf24Thread(idUtils, wifi, transmitterId);
    }

    public void init() {
        logger.info("WifiOperator::init");
        wifi.init();
        // executor.scheduleWithFixedDelay(rf24Thread, INITIAL_DELAY, DELAY, TimeUnit.MILLISECONDS);
        executor.submit(rf24Thread);
    }

    @Override
    public void close() {
        logger.info("WifiOperator::close");
        rf24Thread.close();
        wifi.close();
    }

    public TransmitterId getTransmitterId() {
        return transmitterId;
    }

    public WiFi getWiFi() {
        return wifi;
    }

    public void addToNotify(OnMessage message) {
        rf24Thread.addToNotify(message);
    }

    public void removeFromNotify(OnMessage message) {
        rf24Thread.removeFromNotify(message);
    }
}
