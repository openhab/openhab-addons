package org.openhab.binding.rf24.wifi;

import java.nio.ByteOrder;
import java.util.List;
import java.util.Optional;

import com.google.protobuf.InvalidProtocolBufferException;

import pl.grzeslowski.smarthome.proto.common.Basic;
import pl.grzeslowski.smarthome.proto.sensor.Sensor;
import pl.grzeslowski.smarthome.rpi.wifi.help.Pipe;

public interface WiFi extends AutoCloseable {
    void init();

    @Override
    void close();

    boolean write(Pipe pipe, Sensor.SensorCommandMessage cmd);

    Optional<Basic.AckMessage> read(List<Pipe> pipes, ByteOrder byteOrder) throws InvalidProtocolBufferException;

    Optional<Basic.AckMessage> read(Pipe pipe) throws InvalidProtocolBufferException;

    Optional<Basic.AckMessage> read(Pipe pipe, ByteOrder byteOrder) throws InvalidProtocolBufferException;
}
