package org.smslib.driver;

import java.io.IOException;
import java.net.Socket;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.CommunicationException;
import org.smslib.Modem;

/**
 * Extracted from SMSLib
 * Manage communication with ser2net (or equivalent)
 *
 * Extracted from SMSLib
 */
@NonNullByDefault
public class IPModemDriver extends AbstractModemDriver {
    static Logger logger = LoggerFactory.getLogger(IPModemDriver.class);

    String address;

    int port;

    @Nullable
    Socket socket;

    public IPModemDriver(Modem modem, String address, int port) {
        super(modem);
        this.address = address;
        this.port = port;
    }

    @Override
    public void openPort() throws CommunicationException {
        logger.debug("Opening IP port: {}", getPortInfo());
        try {
            Socket openSocket = new Socket(this.address, this.port);
            openSocket.setReceiveBufferSize(Integer.valueOf(getModemSettings("port_buffer")));
            openSocket.setSendBufferSize(Integer.valueOf(getModemSettings("port_buffer")));
            openSocket.setSoTimeout(30000);
            openSocket.setTcpNoDelay(true);
            this.in = openSocket.getInputStream();
            this.out = openSocket.getOutputStream();
            this.socket = openSocket;
        } catch (IOException e) {
            throw new CommunicationException("Cannot open port", e);
        }
        countSheeps(Integer.valueOf(getModemSettings("after_ip_connect_wait_unit")));
        this.pollReader = new PollReader(this, getPortInfo());
        this.pollReader.setDaemon(true);
        this.pollReader.start();
    }

    @Override
    public void closePort() {
        logger.debug("Closing IP port: {}", getPortInfo());
        try {
            this.pollReader.cancel();
            this.pollReader.join();
            if (in != null) {
                this.in.close();
                this.in = null;
            }
            if (out != null) {
                this.out.close();
                this.out = null;
            }
            Socket finalSocket = socket;
            if (finalSocket != null) {
                finalSocket.close();
            }
        } catch (InterruptedException | IOException e) {
            logger.debug("Cannot close port");
        }
        countSheeps(Integer.valueOf(getModemSettings("after_ip_connect_wait_unit")));
    }

    @Override
    public String getPortInfo() {
        return this.address + ":" + this.port;
    }
}
