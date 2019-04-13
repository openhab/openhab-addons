package org.openhab.binding.noolite.internal.watcher;

import java.io.DataInputStream;
import java.io.IOException;

import javax.xml.bind.DatatypeConverter;

import org.openhab.binding.noolite.handler.NooliteMTRF64BridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NooliteFakeAdapterWatcherThread} is for testing signals from usb stick
 *
 * @author Petr Shatsillo - Initial contribution
 */
public class NooliteFakeAdapterWatcherThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(NooliteFakeAdapterWatcherThread.class);

    private boolean stopped = false;
    private NooliteMTRF64Adapter base;
    DataInputStream in;

    public NooliteFakeAdapterWatcherThread(NooliteMTRF64Adapter nooliteMTRF64Adapter, DataInputStream in) {
        base = nooliteMTRF64Adapter;
        this.in = in;
    }

    public NooliteFakeAdapterWatcherThread(String string) {

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
            logger.warn("{}", e.getLocalizedMessage());
        }
    }

    @Override
    public void run() {

        /*
         * byte[] data = { (byte) 0b10101101, (byte) 0b00000001, (byte) 0b00000000, (byte) 0b00101101, (byte)
         * 0b00000000,
         * (byte) 0b00001111, (byte) 0b00000001, (byte) 0b00000010, (byte) 0b00100001, (byte) 0b00010011,
         * (byte) 0b11111111, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000,
         * (byte) 0b00100000, (byte) 0b10101110 };
         */

        byte[] data = { (byte) 0xAD, (byte) 0x02, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000, (byte) 0x82,
                (byte) 0b00000000, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0xFF, (byte) 0b00000000,
                (byte) 0b00000000, (byte) 0x09, (byte) 0x37, (byte) 0x73, (byte) 0xAE };
        short count = 0;
        byte sum = 0;
        for (int i = 0; i <= 14; i++) {
            count += (data[i] & 0xFF);
        }
        sum = (byte) (count & 0xFF);
        logger.debug("crc is {}", sum);

        logger.debug("Starting data listener");
        while (stopped != true) {
            if (data.length == 17) {
                logger.debug("Received data: {}", DatatypeConverter.printHexBinary(data));
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
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e1) {
            }
        }
    }
}
