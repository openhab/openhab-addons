package org.openhab.binding.ownet.internal;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.adapter.NetAdapter;
import com.dalsemi.onewire.adapter.OneWireIOException;

public class OWNetAdapter extends NetAdapter {

    static final byte CMD_ISPRESENT = 0x40;
    static final byte CMD_SELECT = 0x41;
    private String portName = null;

    @Override
    public boolean selectPort(Socket sock) throws OneWireIOException, OneWireException {
        boolean bSuccess = true;
        synchronized (conn) {
            Connection tmpConn = new Connection();
            tmpConn.sock = sock;

            try {
                tmpConn.input = new DataInputStream(sock.getInputStream());
                if (BUFFERED_OUTPUT) {
                    tmpConn.output = new DataOutputStream(new BufferedOutputStream(sock.getOutputStream()));
                } else {
                    tmpConn.output = new DataOutputStream(sock.getOutputStream());
                }
                tmpConn.sock.setSoTimeout(5000);

            } catch (IOException e) {
                bSuccess = false;
                tmpConn = null;
                System.out.println(e.getMessage());
            }

            if (bSuccess) {
                portName = sock.getInetAddress().getHostName() + ":" + sock.getPort();
                conn = tmpConn;
            }
        }

        // invalid response or version number
        return bSuccess;
    }

    @Override
    public boolean isPresent(byte[] address) throws OneWireIOException, OneWireException {
        try {
            synchronized (conn) {
                // send getBit command
                conn.output.writeByte(CMD_ISPRESENT);
                conn.output.write(address);
                conn.output.flush();

                // check return value for success
                checkReturnValue(conn);

                // next parameter should be the return from getBit
                return conn.input.readBoolean();
            }
        } catch (IOException ioe) {
            throw new OneWireException(COMM_FAILED + ioe.getMessage());
        }
    }

    @Override
    public boolean select(byte[] address) throws OneWireIOException, OneWireException {
        try {
            synchronized (conn) {
                // send getBit command
                conn.output.writeByte(CMD_SELECT);
                conn.output.write(address);
                conn.output.flush();

                // check return value for success
                checkReturnValue(conn);

                // next parameter should be the return from getBit
                return conn.input.readBoolean();
            }
        } catch (IOException ioe) {
            throw new OneWireException(COMM_FAILED + ioe.getMessage());
        }
    }

    public void reconnect() {
        try {
            // this.freePort();
            conn = EMPTY_CONNECTION;
            this.selectPort(portName);
        } catch (OneWireException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setPort(String hostString) {
        portName = hostString;
    }

}
