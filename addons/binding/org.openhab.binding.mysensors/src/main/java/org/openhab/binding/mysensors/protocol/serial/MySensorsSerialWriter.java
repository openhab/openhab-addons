package org.openhab.binding.mysensors.protocol.serial;

import java.io.PrintWriter;

import org.openhab.binding.mysensors.protocol.MySensorsWriter;

import gnu.io.NRSerialPort;

public class MySensorsSerialWriter extends MySensorsWriter {

    private NRSerialPort serialConnection = null;

    public MySensorsSerialWriter(NRSerialPort serialConnection, MySensorsSerialConnection mysCon, int sendDelay) {
        this.mysCon = mysCon;
        this.serialConnection = serialConnection;
        this.sendDelay = sendDelay;

        outs = new PrintWriter(serialConnection.getOutputStream());
    }
}
