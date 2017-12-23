package org.openhab.binding.knx.internal.client;

import java.util.Enumeration;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.knx.handler.StatusUpdateCallback;
import org.openhab.binding.knx.handler.TypeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPortIdentifier;
import gnu.io.RXTXVersion;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.link.KNXNetworkLink;
import tuwien.auto.calimero.link.KNXNetworkLinkFT12;
import tuwien.auto.calimero.link.medium.TPSettings;

public class SerialClient extends KNXClient {

    private final Logger logger = LoggerFactory.getLogger(SerialClient.class);

    private final String serialPort;

    public SerialClient(int autoReconnectPeriod, ThingUID thingUID, int responseTimeout, int readingPause,
            int readRetriesLimit, ScheduledExecutorService knxScheduler, String serialPort,
            StatusUpdateCallback statusUpdateCallback, TypeHelper typeHelper) {
        super(autoReconnectPeriod, thingUID, responseTimeout, readingPause, readRetriesLimit, knxScheduler,
                statusUpdateCallback, typeHelper);
        this.serialPort = serialPort;
    }

    @Override
    protected @NonNull KNXNetworkLink establishConnection() throws KNXException, InterruptedException {
        try {
            RXTXVersion.getVersion();
            logger.debug("Establishing connection to KNX bus through FT1.2 on serial port {}.", serialPort);
            return new KNXNetworkLinkFT12(serialPort, new TPSettings());

        } catch (NoClassDefFoundError e) {
            throw new KNXException(
                    "The serial FT1.2 KNX connection requires the RXTX libraries to be available, but they could not be found!",
                    e);
        } catch (KNXException e) {
            if (e.getMessage().startsWith("can not open serial port")) {
                StringBuilder sb = new StringBuilder("Available ports are:\n");
                Enumeration<?> portList = CommPortIdentifier.getPortIdentifiers();
                while (portList.hasMoreElements()) {
                    CommPortIdentifier id = (CommPortIdentifier) portList.nextElement();
                    if (id.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                        sb.append(id.getName());
                        sb.append("\n");
                    }
                }
                sb.deleteCharAt(sb.length() - 1);
                throw new KNXException("Serial port '" + serialPort + "' could not be opened. " + sb.toString());
            } else {
                throw e;
            }
        }
    }

}
