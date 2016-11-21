package org.openhab.binding.rf24.wifi;

import java.util.Collection;
import java.util.Optional;

import pl.grzeslowski.smarthome.proto.sensor.Sensor;
import pl.grzeslowski.smarthome.rf24.helpers.Pipe;

public interface WiFi extends AutoCloseable {
    void init();

    @Override
    void close();

    boolean write(Pipe pipe, Sensor.SensorRequest cmd);

    Optional<Sensor.SensorResponse> read(Collection<Pipe> pipes);

    Optional<Sensor.SensorResponse> read(Pipe pipe);
}
