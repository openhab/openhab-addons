package org.openhab.binding.mysensors.protocol.serial;

import java.io.IOException;
import java.io.PrintWriter;

import org.openhab.binding.mysensors.protocol.MySensorsWriter;

import gnu.io.SerialPort;

public class MySensorsSerialWriter extends MySensorsWriter {

    private SerialPort serialConnection = null;

    public MySensorsSerialWriter(SerialPort serialConnection, MySensorsSerialConnection mysCon, int sendDelay) {
        this.mysCon = mysCon;
        this.serialConnection = serialConnection;
        this.sendDelay = sendDelay;

        try {
            outs = new PrintWriter(serialConnection.getOutputStream());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
