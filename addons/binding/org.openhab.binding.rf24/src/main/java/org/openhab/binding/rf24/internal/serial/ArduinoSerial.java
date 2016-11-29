package org.openhab.binding.rf24.internal.serial;

import pl.grzeslowski.smarthome.common.io.id.TransmitterId;
import pl.grzeslowski.smarthome.proto.sensor.Sensor;
import pl.grzeslowski.smarthome.proto.sensor.Sensor.SensorRequest;

public interface ArduinoSerial extends AutoCloseable {

    void init();

    @Override
    void close();

    TransmitterId getTransmitterId();

    void addListener(OnSerialMessageListener listener);

    boolean write(SensorRequest request);

    public interface OnSerialMessageListener {
        void onMessage(ArduinoSerial serial, Sensor.SensorRequest request);

        void onMessage(ArduinoSerial serial, Sensor.SensorResponse response);
    }
}
