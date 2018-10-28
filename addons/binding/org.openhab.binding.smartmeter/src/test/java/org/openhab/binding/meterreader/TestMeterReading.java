package org.openhab.binding.smartmeter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.Executors;

import javax.measure.Quantity;

import org.junit.Test;
import org.openhab.binding.smartmeter.connectors.ConnectorBase;
import org.openhab.binding.smartmeter.connectors.IMeterReaderConnector;
import org.openhab.binding.smartmeter.internal.MeterDevice;
import org.openhab.binding.smartmeter.internal.helper.ProtocolMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.disposables.Disposable;

public class TestMeterReading {

    @Test
    public void testContinousReading() throws Exception {
        MeterDevice<Object> meterDevice = new MeterDevice<Object>("id", "port", null, 9600, 0, ProtocolMode.SML) {
            private Logger logger = LoggerFactory.getLogger(TestMeterReading.class);

            @Override
            protected IMeterReaderConnector<Object> createConnector(String serialPort, int baudrate,
                    int baudrateChangeDelay, ProtocolMode protocolMode) {

                return new MockMeterReaderConnector(serialPort);
            }

            @Override
            protected <Q extends Quantity<Q>> void populateValueCache(Object smlFile) {
                logger.info("successfully read: {}", smlFile);
            }
        };
        Disposable readValues = meterDevice.readValues(Executors.newScheduledThreadPool(1), Duration.ofSeconds(1));
        Thread.sleep(15000);
        readValues.dispose();
    }
}

class MockMeterReaderConnector extends ConnectorBase<Object> {

    private int count = 0;

    protected MockMeterReaderConnector(String portName) {
        super(portName);
    }

    @Override
    public void openConnection() throws IOException {
        count = 0;
    }

    @Override
    public void closeConnection() {
        // TODO Auto-generated method stub

    }

    @Override
    protected Object readNext(byte[] initMessage) throws IOException {
        if (count == 2) {
            count = 0;
            throw new IllegalArgumentException("test");
        }
        count++;
        return count + "";
    }

    @Override
    protected boolean applyRetryHandling() {
        return true;
    }
}
