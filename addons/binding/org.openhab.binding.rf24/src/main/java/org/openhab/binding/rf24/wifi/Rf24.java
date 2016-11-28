package org.openhab.binding.rf24.wifi;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;

import pl.grzeslowski.smarthome.proto.sensor.Sensor;
import pl.grzeslowski.smarthome.proto.sensor.Sensor.SensorResponse;
import pl.grzeslowski.smarthome.rf24.BasicRf24;
import pl.grzeslowski.smarthome.rf24.Rf24Adapter;
import pl.grzeslowski.smarthome.rf24.helpers.Payload;
import pl.grzeslowski.smarthome.rf24.helpers.Pins;
import pl.grzeslowski.smarthome.rf24.helpers.Pipe;
import pl.grzeslowski.smarthome.rf24.helpers.Retry;

public class Rf24 implements WiFi {
    private Logger logger = LoggerFactory.getLogger(Rf24.class);
    private static final ByteOrder ARDUINO_BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;
    private static final long WRTITE_RETRIES_MAX_TIME = TimeUnit.MILLISECONDS.toMillis(500);

    private final BasicRf24 wifi;
    private final Payload payload;

    public Rf24(Pins pins, Retry retry, Payload payload) {
        this.payload = payload;
        wifi = new Rf24Adapter(pins, retry, payload);
    }

    @Override
    public void init() {
        logger.info("Init RF24");
        wifi.init();

        if (wifi instanceof Rf24Adapter) {
            Rf24Adapter x = (Rf24Adapter) wifi;
            x.printDetails();
        }
    }

    @Override
    public void close() {
        logger.info("Close RF24");
        wifi.close();
    }

    @Override
    public boolean write(Pipe pipe, Sensor.SensorRequest cmd) {
        final byte[] toSend = cmd.toByteArray();
        logger.debug("Write RF24, size: {}.", toSend.length);
        boolean wrote = false;
        final long startTime = new Date().getTime();
        int times = 0;
        while (!wrote && startTime + WRTITE_RETRIES_MAX_TIME >= new Date().getTime()) {
            wrote = wifi.write(pipe, toSend);
            if (!wrote) {
                logger.trace("Didn't send command {}", cmd.getBasic().getMessageId());
            }
            times++;
        }

        if (wrote) {
            logger.info("Msg was sent ID {} (times {})!", cmd.getBasic().getMessageId(), times);
        }

        return wrote;
    }

    @Override
    public Optional<Sensor.SensorResponse> read(Collection<Pipe> pipes) {
        // logger.info("Read RF24");
        ByteBuffer buffer = ByteBuffer.allocate(payload.getSize());
        buffer.order(ARDUINO_BYTE_ORDER);
        final boolean read = wifi.read(new ArrayList<>(pipes), buffer);
        if (read) {
            logger.info("read on...");
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            String arr = "";
            for (int i = 0; i < data.length; i++) {

                arr += data[i] + ", ";
            }
            logger.info("data = [{}]", arr);

            int howManyBytes = 0;
            byte reading;
            do {
                reading = data[howManyBytes];
                howManyBytes++;
            } while (reading != (byte) 0);

            byte[] protobuf = new byte[howManyBytes - 1];
            for (int i = 0; i < howManyBytes - 1; i++) {
                protobuf[i] = data[i];
            }

            arr = "";
            for (int i = 0; i < protobuf.length; i++) {

                arr += protobuf[i] + ", ";
            }
            logger.info("protobuf = [{}]", arr);

            final Sensor.SensorResponse ackMessage = parseSensorResponse(protobuf);
            logger.info("Read smt from pipe {}!", ackMessage.getBasic().getMessageId());
            return Optional.of(ackMessage);
        } else {
            return Optional.empty();
        }
    }

    private SensorResponse parseSensorResponse(byte[] data) {
        try {
            return Sensor.SensorResponse.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Could not parse data into Sensor.SensorResponse!", e);
        }
    }

    @Override
    public Optional<Sensor.SensorResponse> read(Pipe pipe) {
        return read(Collections.singletonList(pipe));
    }

    @Override
    public String toString() {
        return wifi.toString();
    }
}
