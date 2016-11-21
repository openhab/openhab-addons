package org.openhab.binding.rf24.wifi;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import pl.grzeslowski.smarthome.common.io.id.HardwareId;
import pl.grzeslowski.smarthome.common.io.id.IdUtils;
import pl.grzeslowski.smarthome.common.io.id.TransmitterId;
import pl.grzeslowski.smarthome.proto.sensor.Sensor.SensorResponse;
import pl.grzeslowski.smarthome.rf24.Rf24Adapter;
import pl.grzeslowski.smarthome.rf24.helpers.Pipe;

public class Rf24Thread extends Thread implements AutoCloseable {
    private static final long SLEEP_TIME = TimeUnit.SECONDS.toMillis(5);
    private static final IdUtils ID_UTILS = new IdUtils(Rf24Adapter.MAX_NUMBER_OF_READING_PIPES);
    private Logger logger = LoggerFactory.getLogger(Rf24Thread.class);

    private final AtomicBoolean exit = new AtomicBoolean(false);
    private final WiFi wifi;
    private final Collection<Pipe> pipes;
    private final Set<OnMessage> notify = new HashSet<>();

    public Rf24Thread(WiFi wifi, TransmitterId transmitterId) {
        super(Rf24Thread.class.getSimpleName());
        this.wifi = Preconditions.checkNotNull(wifi);
        Preconditions.checkNotNull(transmitterId);
        // @formatter:off
        this.pipes = ID_UTILS.findReceiversForTransmitter(transmitterId)
                .stream()
                .map(ID_UTILS::toCommonId)
                .map(HardwareId::fromCommonId)
                .map(HardwareId::getId)
                .map(id -> new Pipe(id))
                .collect(Collectors.toList());
        // @formatter:on
        setDaemon(true); // this thread should be close automatically on the end
    }

    @Override
    public void run() {
        // TODO
        // Preconditions.checkArgument(wifi.isInit());
        // or
        // if (!wifi.isInit()) return; // this is not the best cause thread will end
        // or
        // while(!wifi.isInit()) sleep(xxx);
        while (!exit.get()) {
            Optional<SensorResponse> read = wifi.read(pipes);
            if (read.isPresent()) {
                SensorResponse response = read.get();

                synchronized (notify) {
                    notify.stream().forEach(message -> message.onMessage(response));
                }
            } else {

                // sleep only when nothing in pipe is presented!
                logger.debug("Sleeping ({} ms), because there was nothing in the pipes...", SLEEP_TIME);
                sleep();
            }
        }
    }

    @Override
    public void close() {
        exit.set(true);
    }

    public void addToNotify(OnMessage message) {
        Preconditions.checkNotNull(message);
        synchronized (notify) {
            notify.add(message);
        }
    }

    public void removeFromNotify(OnMessage message) {
        Preconditions.checkNotNull(message);
        synchronized (notify) {
            notify.remove(Preconditions.checkNotNull(message));
        }
    }

    private void sleep() {
        try {
            Thread.sleep(SLEEP_TIME);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static interface OnMessage {
        void onMessage(SensorResponse response);
    }
}
