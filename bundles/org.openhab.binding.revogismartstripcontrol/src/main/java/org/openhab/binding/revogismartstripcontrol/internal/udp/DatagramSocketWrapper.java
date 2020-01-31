package org.openhab.binding.revogismartstripcontrol.internal.udp;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class DatagramSocketWrapper {

    DatagramSocket datagramSocket;

    public void initSocket() throws SocketException {
        if (datagramSocket != null && !datagramSocket.isClosed()) {
            datagramSocket.close();
        }
        datagramSocket = new DatagramSocket();
        datagramSocket.setBroadcast(true);
        datagramSocket.setSoTimeout(3);
    }

    public void closeSocket() {
        datagramSocket.close();
    }

    public void sendPacket(DatagramPacket datagramPacket) throws IOException {
        datagramSocket.send(datagramPacket);
    }

    public void receiveAnswer(DatagramPacket datagramPacket) throws IOException {
        datagramSocket.receive(datagramPacket);
    }
}