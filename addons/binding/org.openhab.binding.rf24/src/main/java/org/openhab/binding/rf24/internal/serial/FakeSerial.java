package org.openhab.binding.rf24.internal.serial;

import com.google.common.base.Preconditions;

import pl.grzeslowski.smarthome.common.io.id.TransmitterId;
import pl.grzeslowski.smarthome.proto.sensor.Sensor.SensorRequest;

public class FakeSerial implements ArduinoSerial {

    private final TransmitterId id;

    public FakeSerial(TransmitterId id) {
        this.id = Preconditions.checkNotNull(id);
    }

    @Override
    public void init() {

    }

    @Override
    public void close() {

    }

    @Override
    public TransmitterId getTransmitterId() {
        return id;
    }

    @Override
    public void addListener(OnSerialMessageListener listener) {

    }

    @Override
    public boolean write(SensorRequest request) {
        return false;
    }

}
