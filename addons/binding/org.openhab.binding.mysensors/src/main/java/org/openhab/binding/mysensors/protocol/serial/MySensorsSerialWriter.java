package org.openhab.binding.mysensors.protocol.serial;

import java.io.OutputStream;
import java.io.PrintWriter;

import org.openhab.binding.mysensors.protocol.MySensorsWriter;

public class MySensorsSerialWriter extends MySensorsWriter {

    public MySensorsSerialWriter(OutputStream outStream, MySensorsSerialConnection mysCon, int sendDelay) {
        this.mysCon = mysCon;
        this.outStream = outStream;
        outs = new PrintWriter(outStream);
        this.sendDelay = sendDelay;
    }
}
