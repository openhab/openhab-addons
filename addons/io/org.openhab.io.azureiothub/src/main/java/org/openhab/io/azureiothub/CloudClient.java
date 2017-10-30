/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.io.azureiothub;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.servicebus.ServiceBusException;

/**
 *
 * This class provides communication between openHAB and azure IOT Hub.
 * implementation is taken from https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-java-java-getstarted
 *
 * @author Niko Tanghe - Initial contribution
 *
 */

public class CloudClient {
    /*
     * Logger for this class
     */
    private Logger logger = LoggerFactory.getLogger(CloudClient.class);

    private String connectionstring;
    private boolean commandEnabled;

    private AzureDevices azureDeviceStore;
    private EventHubClient azureClient;

    private Object lockobj = new Object();

    /**
     * Constructor of CloudClient
     *
     * @param connectionstring the connectionstring to the azure IOT hub
     * @throws IOException
     *
     */
    public CloudClient(String connectionstring, boolean commandEnabled) throws Exception {
        this.connectionstring = connectionstring;
        this.commandEnabled = commandEnabled;

        azureDeviceStore = new AzureDevices(this.connectionstring);

        if (this.commandEnabled) {
            azureClient = receiveMessages("0");
        }
    }

    public void sendItemUpdate(String deviceId, String state) {

        AzureDevice device;
        try {
            device = azureDeviceStore.getDevice(deviceId);
        } catch (Exception e) {
            logger.error("Failed to obtain azure device", e);
            return;
        }

        setItemState(device, state);
    }

    private void setItemState(AzureDevice device, String state) {
        AzureEventCallback callback = new AzureEventCallback();

        AzureDatapoint datapoint = new AzureDatapoint();

        if (device.Device == null) {
            logger.error("Invalid device connection for device, can not send item state update");
            return;
        }

        datapoint.deviceId = device.Device.getDeviceId();
        datapoint.value = state;

        Message msg = new Message(datapoint.serialize());

        device.SendMessage(msg, callback, lockobj);

        synchronized (lockobj) {
            try {
                lockobj.wait();
            } catch (InterruptedException e) {
                // done
            }
        }
    }

    public void shutdown() {
        if (azureClient != null) {
            try {
                azureClient.closeSync();
            } catch (ServiceBusException sbe) {
                logger.error("failed to close IOT client", sbe);
            }
        }
    }

    private EventHubClient receiveMessages(final String partitionId) {
        EventHubClient client = null;
        try {
            client = EventHubClient.createFromConnectionStringSync(this.connectionstring);
        } catch (Exception e) {
            logger.error("Failed to create IOT EventHub client", e);
            return null;
        }
        try {
            client.createReceiver(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, partitionId, Instant.now())
                    .thenAccept(new Consumer<PartitionReceiver>() {
                        @Override
                        public void accept(PartitionReceiver receiver) {
                            logger.debug("Created IOT Hub receiver on partition {}", partitionId);
                            try {
                                while (true) {
                                    Iterable<EventData> receivedEvents = receiver.receive(100).get();
                                    int batchSize = 0;
                                    if (receivedEvents != null) {
                                        for (EventData receivedEvent : receivedEvents) {
                                            // we should do something with this received command...
                                            // maybe execute the command for the openhab device ?

                                            logger.debug("Offset: {}, SeqNo: {}, EnqueueTime: {}",
                                                    receivedEvent.getSystemProperties().getOffset(),
                                                    receivedEvent.getSystemProperties().getSequenceNumber(),
                                                    receivedEvent.getSystemProperties().getEnqueuedTime());
                                            logger.debug("| Device ID: {}", receivedEvent.getSystemProperties()
                                                    .get("iothub-connection-device-id"));
                                            logger.debug("| Message Payload: {}",
                                                    new String(receivedEvent.getBytes(), Charset.defaultCharset()));
                                            batchSize++;
                                        }
                                    }
                                    logger.debug("Partition: {}, ReceivedBatch Size: {}", partitionId, batchSize);
                                }
                            } catch (Exception e) {
                                logger.debug("Failed to receive messages ", e);
                            }
                        }
                    });
        } catch (Exception e) {
            logger.debug("Failed to create receiver ", e);
        }
        return client;
    }
}
