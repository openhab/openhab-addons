package org.openhab.binding.mysensors.protocol.ip;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import org.openhab.binding.mysensors.protocol.MySensorsWriter;

public class MySensorsIpWriter extends MySensorsWriter {

    public MySensorsIpWriter(Socket sock, MySensorsIpConnection mysCon, int sendDelay) {
        this.mysCon = mysCon;
        try {
            this.outStream = sock.getOutputStream();
            outs = new PrintWriter(outStream);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.sendDelay = sendDelay;
    }
}
