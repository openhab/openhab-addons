package org.openhab.binding.lgtvserial.internal.protocol.serial;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerialCommunicatorFactory {

    /**
     * Logger.
     */
    private Logger logger = LoggerFactory.getLogger(SerialCommunicatorFactory.class);

    private Map<String, LGSerialCommunicator> instances = new HashMap<String, LGSerialCommunicator>();

    public synchronized LGSerialCommunicator getInstance(String port) {
        LGSerialCommunicator comm = instances.get(port);
        if (comm == null) {
            comm = createCommunicator(port);
            if (comm != null) {
                instances.put(port, comm);
            }
        }
        return comm;
    }

    private LGSerialCommunicator createCommunicator(final String portName) {

        return new LGSerialCommunicator(portName, new RegistrationCallback() {

            @Override
            public void onUnregister() {
                logger.debug("Unregistered last handler, closing");
                deleteInstance(portName);
            }

        });

    }

    protected synchronized void deleteInstance(String port) {
        instances.remove(port).close();
    }

}
