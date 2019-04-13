package org.openhab.binding.noolite.internal.watcher;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.IOUtils;
import org.openhab.binding.noolite.internal.config.NooliteBridgeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.NRSerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

/**
 * The {@link NooliteMTRF64Adapter} is for init usb stick
 *
 * @author Petr Shatsillo - Initial contribution
 */
public class NooliteMTRF64Adapter implements SerialPortEventListener {

    private static final Logger logger = LoggerFactory.getLogger(NooliteMTRF64Adapter.class);
    DataInputStream in = null;
    DataOutputStream out = null;
    Thread watcherThread = null;
    NRSerialPort serial;

    public void connect(NooliteBridgeConfiguration config) throws Exception {

        serial = new NRSerialPort(config.serial, 9600);
        serial.connect();

        in = new DataInputStream(serial.getInputStream());
        out = new DataOutputStream(serial.getOutputStream());
        out.flush();

        serial.addEventListener(this);
        serial.notifyOnDataAvailable(true);

        watcherThread = new NooliteMTRF64AdapterWatcherThread(this, in);
        watcherThread.start();
    }

    public void connect(String string) {

        watcherThread = new NooliteFakeAdapterWatcherThread("fake");
        watcherThread.start();

    }

    @Override
    public void serialEvent(SerialPortEvent arg0) {
        // TODO Auto-generated method stub

    }

    public void disconnect() {
        if (serial != null) {
            serial.removeEventListener();
        }

        if (watcherThread != null) {
            watcherThread.interrupt();
        }

        if (out != null) {
            logger.debug("Close serial out stream");
            IOUtils.closeQuietly(out);
        }
        if (in != null) {
            logger.debug("Close serial in stream");
            IOUtils.closeQuietly(in);
        }

        if (serial != null) {
            logger.debug("Close serial port");
            serial.disconnect();
        }

        in = null;
        out = null;
        watcherThread = null;

    }

    public void sendData(byte[] data) throws IOException {
        logger.debug("Sending {} bytes: {}", data.length, DatatypeConverter.printHexBinary(data));
        out.write(data);
        out.flush();
    }

}
