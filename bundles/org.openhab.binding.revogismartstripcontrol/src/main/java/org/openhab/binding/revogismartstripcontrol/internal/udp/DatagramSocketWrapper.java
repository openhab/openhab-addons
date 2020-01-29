package org.openhab.binding.revogismartstripcontrol.internal.udp;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

class DatagramSocketWrapper {

    private DatagramSocket datagramSocket = new DatagramSocket();

    DatagramSocketWrapper() throws SocketException {
    }


    public void initSocket() throws SocketException {
        if (!datagramSocket.isClosed()) {
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