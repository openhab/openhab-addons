package org.openhab.binding.mysensors.protocol.ip;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import org.openhab.binding.mysensors.protocol.MySensorsWriter;

public class MySensorsIpWriter extends MySensorsWriter {

    private Socket sock = null;

    public MySensorsIpWriter(Socket sock, MySensorsIpConnection mysCon, int sendDelay) {
        this.mysCon = mysCon;
        this.sock = sock;
        this.sendDelay = sendDelay;
        try {
            outs = new PrintWriter(sock.getOutputStream());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public MySensorsIpWriter(Socket sock, MySensorsIpConnection mysCon) {
        this.mysCon = mysCon;
        this.sock = sock;
        try {
            outs = new PrintWriter(sock.getOutputStream());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
