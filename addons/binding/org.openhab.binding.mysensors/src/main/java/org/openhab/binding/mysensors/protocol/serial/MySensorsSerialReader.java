package org.openhab.binding.mysensors.protocol.serial;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.openhab.binding.mysensors.protocol.MySensorsReader;

public class MySensorsSerialReader extends MySensorsReader {

    public MySensorsSerialReader(InputStream inStream, MySensorsSerialConnection mysCon) {
        this.mysCon = mysCon;

        reads = new BufferedReader(new InputStreamReader(inStream));
    }
}
