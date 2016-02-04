package org.openhab.binding.mysensors.protocol.ip;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.openhab.binding.mysensors.protocol.MySensorsReader;

public class MySensorIpReader extends MySensorsReader {
    public MySensorIpReader(InputStream inStream, MySensorsIpConnection mysCon) {
        this.mysCon = mysCon;
        this.inStream = inStream;
        reads = new BufferedReader(new InputStreamReader(inStream));
    }
}
