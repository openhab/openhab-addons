package org.openhab.binding.rf24.wifi;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;

import pl.grzeslowski.smarthome.proto.sensor.Sensor;
import pl.grzeslowski.smarthome.proto.sensor.Sensor.SensorResponse;
import pl.grzeslowski.smarthome.rf24.BasicRf24;
import pl.grzeslowski.smarthome.rf24.Rf24Adapter;
import pl.grzeslowski.smarthome.rf24.helpers.ClockSpeed;
import pl.grzeslowski.smarthome.rf24.helpers.Payload;
import pl.grzeslowski.smarthome.rf24.helpers.Pins;
import pl.grzeslowski.smarthome.rf24.helpers.Pipe;
import pl.grzeslowski.smarthome.rf24.helpers.Retry;

public class Rf24 implements WiFi {
    private Logger logger = LoggerFactory.getLogger(Rf24.class);
    private static final ByteOrder ARDUINO_BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;

    private final BasicRf24 wifi;
    private final Payload payload;

    public Rf24(short ce, short cs, int spi, short delay, short number, short size) {
        payload = new Payload(size);
        wifi = new Rf24Adapter(new Pins(ce, cs, ClockSpeed.findClockSpeed(spi).get()), new Retry(delay, number),
                payload);
    }

    @Override
    public void init() {
        logger.info("Init RF24");
        wifi.init();
    }

    @Override
    public void close() {
        logger.info("Close RF24");
        wifi.close();
    }

    @Override
    public boolean write(Pipe pipe, Sensor.SensorRequest cmd) {
        logger.info("Write RF24");
        return wifi.write(pipe, cmd.toByteArray());
    }

    @Override
    public Optional<Sensor.SensorResponse> read(Collection<Pipe> pipes) {
        logger.info("Read RF24");
        ByteBuffer buffer = ByteBuffer.allocate(payload.getSize());
        buffer.order(ARDUINO_BYTE_ORDER);
        final boolean read = wifi.read(new ArrayList<>(pipes), buffer);
        if (read) {
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            final Sensor.SensorResponse ackMessage = parseSensorResponse(data);
            return Optional.of(ackMessage);
        } else {
            return Optional.empty();
        }
    }

    private SensorResponse parseSensorResponse(byte[] data) {
        try {
            return Sensor.SensorResponse.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Could not parse data into Sensor.SensorResponse!");
        }
    }

    @Override
    public Optional<Sensor.SensorResponse> read(Pipe pipe) {
        return read(Collections.singletonList(pipe));
    }
}
