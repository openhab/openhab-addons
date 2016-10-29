package org.openhab.binding.rf24.wifi;

import java.nio.ByteOrder;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.protobuf.InvalidProtocolBufferException;

import pl.grzeslowski.smarthome.proto.common.Basic;
import pl.grzeslowski.smarthome.proto.common.Basic.AckMessage;
import pl.grzeslowski.smarthome.proto.sensor.Sensor.SensorCommandMessage;
import pl.grzeslowski.smarthome.rpi.wifi.help.Pipe;

public class StubWiFi implements WiFi {
    private static final Logger logger = LoggerFactory.getLogger(StubWiFi.class);

    @Override
    public void init() {
        logger.info("StubWiFi.init()");
    }

    @Override
    public void close() {
        logger.info("StubWiFi.close()");
    }

    @Override
    public boolean write(Pipe pipe, SensorCommandMessage cmd) {
        logger.info("StubWiFi.write({}, {})", pipe, cmd);
        return true;
    }

    @Override
    public Optional<AckMessage> read(List<Pipe> pipes, ByteOrder byteOrder) throws InvalidProtocolBufferException {
        logger.info("StubWiFi.read({}, {})", Joiner.on(",").join(pipes), byteOrder);
        return Optional.empty();
    }

    @Override
    public Optional<Basic.AckMessage> read(Pipe pipe) throws InvalidProtocolBufferException {
        return read(Collections.singletonList(pipe), ByteOrder.LITTLE_ENDIAN);
    }

    @Override
    public Optional<Basic.AckMessage> read(Pipe pipe, ByteOrder byteOrder) throws InvalidProtocolBufferException {
        return read(Collections.singletonList(pipe), byteOrder);
    }

}
