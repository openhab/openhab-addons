package org.openhab.binding.ivtheatpump.internal.protocol;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class IpIVRConnection implements IVRConnection {
    /** Connection timeout in milliseconds **/
    private static final int CONNECTION_TIMEOUT = 3000;

    /** Socket read timeout in milliseconds **/
    private static final int SOCKET_READ_TIMEOUT = 1000;

    private final String address;
    private final int port;
    private final Socket clientSocket;

    public IpIVRConnection(String address, int port) {
        this.address = address;
        this.port = port;

        clientSocket = new Socket();
    }

    @Override
    public void connect() throws IOException {
        clientSocket.setSoTimeout(SOCKET_READ_TIMEOUT);
        clientSocket.connect(new InetSocketAddress(address, port), CONNECTION_TIMEOUT);
    }

    @Override
    public boolean isConnected() {
        return clientSocket.isConnected();
    }

    @Override
    public void close() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void write(byte[] data) throws IOException {
        final OutputStream stream = clientSocket.getOutputStream();
        stream.write(data);
        stream.flush();
    }

    @Override
    public int read() throws IOException {
        return clientSocket.getInputStream().read();
    }
}
