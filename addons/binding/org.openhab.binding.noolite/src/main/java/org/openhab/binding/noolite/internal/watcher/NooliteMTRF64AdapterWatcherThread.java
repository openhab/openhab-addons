package org.openhab.binding.noolite.internal.watcher;

import java.io.DataInputStream;
import java.io.IOException;

import javax.xml.bind.DatatypeConverter;

import org.openhab.binding.noolite.handler.NooliteMTRF64BridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NooliteMTRF64AdapterWatcherThread} is listener for accepting and transfer signals from usb stick
 *
 * @author Petr Shatsillo - Initial contribution
 */
public class NooliteMTRF64AdapterWatcherThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(NooliteMTRF64AdapterWatcherThread.class);

    private boolean stopped = false;
    private NooliteMTRF64Adapter base;
    DataInputStream in;

    public NooliteMTRF64AdapterWatcherThread(NooliteMTRF64Adapter nooliteMTRF64Adapter, DataInputStream in) {
        base = nooliteMTRF64Adapter;
        this.in = in;
    }

    @Override
    public void interrupt() {
        stopped = true;
        super.interrupt();
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }

    }

    @Override
    public void run() {
        byte[] data = new byte[17];

        try {
            logger.debug("Starting data listener");
            while (stopped != true) {
                if (in.read(data) > 0) {
                    logger.debug("Received data: {}", DatatypeConverter.printHexBinary(data));
                    short count = 0;
                    byte sum = 0;
                    for (int i = 0; i <= 14; i++) {
                        count += (data[i] & 0xFF);
                    }
                    sum = (byte) (count & 0xFF);
                    if (((data[0] & 0xFF) == 0b10101101) && ((data[16] & 0xFF) == 0b10101110)) {
                        logger.debug("sum is {} CRC must be {} receive {}", count, sum, data[15]);
                        if (sum == data[15]) {
                            logger.debug("CRC is OK");

                            logger.debug("Updating values...");
                            NooliteMTRF64BridgeHandler.updateValues(data);
                        } else {
                            logger.debug("CRC is WRONG");
                        }
                    } else {
                        logger.debug("Start/stop bits is wrong");

                    }
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e1) {
                    }
                }
            }
        } catch (IOException e) {
            logger.warn("{}", e.getLocalizedMessage());
        }
    }
}
