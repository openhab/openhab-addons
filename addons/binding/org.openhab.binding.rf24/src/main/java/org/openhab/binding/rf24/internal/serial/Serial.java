package org.openhab.binding.rf24.internal.serial;

import java.util.concurrent.ExecutorService;

import pl.grzeslowski.smarthome.common.io.id.TransmitterId;
import pl.grzeslowski.smarthome.proto.sensor.Sensor.SensorRequest;
import pl.grzeslowski.smarthome.proto.sensor.Sensor.SensorResponse;
import pl.grzeslowski.smarthome.rpi.serial.DataRate;
import pl.grzeslowski.smarthome.rpi.serial.Port;

public class Serial extends pl.grzeslowski.smarthome.rpi.serial.Serial implements ArduinoSerial {
    private TransmitterId id;

    public Serial(DataRate dataRate, Port port, ExecutorService executorService, TransmitterId id) {
        super(dataRate, port, executorService);
        this.id = id;
    }

    @Override
    public TransmitterId getTransmitterId() {
        return id;
    }

    @Override
    public void addListener(org.openhab.binding.rf24.internal.serial.ArduinoSerial.OnSerialMessageListener listener) {
        addListener(new pl.grzeslowski.smarthome.rpi.serial.Serial.OnSerialMessageListener() {

            @Override
            public void onMessage(pl.grzeslowski.smarthome.rpi.serial.Serial serial, SensorResponse response) {
                listener.onMessage(Serial.this, response);
            }

            @Override
            public void onMessage(pl.grzeslowski.smarthome.rpi.serial.Serial serial, SensorRequest request) {
                listener.onMessage(Serial.this, request);
            }
        });
    }

    @Override
    public boolean write(SensorRequest request) {
        return write(request);
    }
}
