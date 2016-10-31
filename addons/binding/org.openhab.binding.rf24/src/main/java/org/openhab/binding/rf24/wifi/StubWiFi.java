package org.openhab.binding.rf24.wifi;

import java.nio.ByteOrder;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

import pl.grzeslowski.smarthome.proto.common.Basic.BasicMessage;
import pl.grzeslowski.smarthome.proto.sensor.Sensor;
import pl.grzeslowski.smarthome.proto.sensor.Sensor.Dht11Response;
import pl.grzeslowski.smarthome.proto.sensor.Sensor.OnOff;
import pl.grzeslowski.smarthome.proto.sensor.Sensor.OnOffRequest;
import pl.grzeslowski.smarthome.proto.sensor.Sensor.OnOffResponse;
import pl.grzeslowski.smarthome.proto.sensor.Sensor.SensorResponse.Builder;
import pl.grzeslowski.smarthome.rpi.wifi.help.Pipe;

public class StubWiFi implements WiFi {
    private static final Logger logger = LoggerFactory.getLogger(StubWiFi.class);
    private final Map<Pipe, Queue<Sensor.SensorRequest>> messages = new HashMap<>();
    private final Map<Pipe, OnOffRequest> onOffStates = new HashMap<>();
    private boolean initialized;
    private int messageId = 1;

    @Override
    public synchronized void init() {
        logger.info("StubWiFi.init()");
        if (initialized) {
            throw new RuntimeException("Already initialized!");
        }
        initialized = true;
    }

    @Override
    public synchronized void close() {
        logger.info("StubWiFi.close()");
        if (!initialized) {
            throw new RuntimeException("Not initialized - cannot close!");
        }
        initialized = false;
    }

    @Override
    public synchronized boolean write(Pipe pipe, Sensor.SensorRequest cmd) {
        checkInitialized();
        logger.info("StubWiFi.write({}, {})", pipe, cmd);
        if (!messages.containsKey(pipe)) {
            messages.put(pipe, new LinkedList<>());
        }
        messages.get(pipe).add(cmd);
        if (cmd.hasOnOffRequest()) {
            // Set on off states
            onOffStates.put(pipe, cmd.getOnOffRequest());
        }
        return true;
    }

    @Override
    public synchronized Optional<Sensor.SensorResponse> read(List<Pipe> pipes, ByteOrder byteOrder) {
        checkInitialized();
        logger.info("StubWiFi.read({}, {})", Joiner.on(",").join(pipes), byteOrder);
        for (Pipe pipe : pipes) {
            if (messages.containsKey(pipe)) {
                Sensor.SensorRequest cmd = messages.get(pipe).poll();
                if (cmd != null) {
                    BasicMessage basic = BasicMessage.newBuilder().setDeviceId(1).setMessageId(messageId++)
                            .setLinuxTimestamp(new Date().getTime()).build();
                    Builder ack = Sensor.SensorResponse.newBuilder().setBasic(basic)
                            .setMessageId(cmd.getBasic().getMessageId());

                    // If has request for OnOff alse return OnOff state
                    if (cmd.hasOnOffRequest()) {
                        OnOffRequest lastOnOffState = onOffStates.get(pipe);
                        OnOff state;
                        if (lastOnOffState == null || lastOnOffState.getOnOff() == OnOff.ON) {
                            state = OnOff.OFF;
                        } else {
                            state = OnOff.ON;
                        }
                        OnOffResponse onOff = OnOffResponse.newBuilder().setOnOff(state).build();
                        ack.setOnOffResponse(onOff);
                    }

                    // If has request for DHT11 state, return DHT11 state
                    if (cmd.hasRefreshDht11Request()) {
                        Random r = new Random();
                        Dht11Response dht11Response = Dht11Response.newBuilder().setTemperature(randomTemperature())
                                .setHumidity(randomHumidity()).build();
                        ack.setDht11Response(dht11Response);
                    }

                    return Optional.of(ack.build());
                }
            }
        }
        return Optional.empty();
    }

    private float randomTemperature() {
        Random r = new Random();
        float temp = r.nextFloat() * 100;
        if (temp >= 0 && temp <= 50) {
            return temp;
        } else {
            return randomTemperature();
        }
    }

    private float randomHumidity() {
        Random r = new Random();
        float hum = r.nextFloat() * 100;
        if (hum >= 20 && hum <= 90) {
            return hum;
        } else {
            return randomHumidity();
        }
    }

    @Override
    public synchronized Optional<Sensor.SensorResponse> read(Pipe pipe) {
        return read(Collections.singletonList(pipe), ByteOrder.LITTLE_ENDIAN);
    }

    @Override
    public synchronized Optional<Sensor.SensorResponse> read(Pipe pipe, ByteOrder byteOrder) {
        return read(Collections.singletonList(pipe), byteOrder);
    }

    private void checkInitialized() {
        if (!initialized) {
            throw new RuntimeException("Not initialized!");
        }
    }
}
