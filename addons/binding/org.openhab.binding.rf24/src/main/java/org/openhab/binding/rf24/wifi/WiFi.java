package org.openhab.binding.rf24.wifi;

import java.nio.ByteOrder;
import java.util.List;
import java.util.Optional;

import pl.grzeslowski.smarthome.proto.sensor.Sensor;
import pl.grzeslowski.smarthome.rf24.helpers.Pipe;

public interface WiFi extends AutoCloseable {
    void init();

    @Override
    void close();

    boolean write(Pipe pipe, Sensor.SensorRequest cmd);

    Optional<Sensor.SensorResponse> read(List<Pipe> pipes, ByteOrder byteOrder);

    Optional<Sensor.SensorResponse> read(Pipe pipe);

    Optional<Sensor.SensorResponse> read(Pipe pipe, ByteOrder byteOrder);
}
