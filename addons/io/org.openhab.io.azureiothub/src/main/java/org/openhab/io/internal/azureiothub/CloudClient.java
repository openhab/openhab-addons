/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.internal.azureiothub;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.function.Consumer;

import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.library.types.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.servicebus.ServiceBusException;

/**
 *
 * This class provides communication between openHAB and Azure IoT Hub.
 * implementation is taken from https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-java-java-getstarted
 *
 * @author Niko Tanghe - Initial contribution
 * @author Kai Kreuzer - Code cleanup
 *
 */

public class CloudClient {

    private static final String DATAPOINT_VALUE = "value";
    private static final String DATAPOINT_DEVICE_ID = "deviceId";

    private final Logger logger = LoggerFactory.getLogger(CloudClient.class);

    private String connectionstring;
    private boolean commandEnabled;
    private EventPublisher eventPublisher;

    private AzureDevices azureDeviceStore;
    private EventHubClient azureClient;
    private Object lockobj = new Object();
    private Gson gson = new Gson();

    /**
     * Constructor of CloudClient
     *
     * @param connectionstring the connectionstring to the Azure IoT Hub
     * @param eventPublisher
     * @throws IOException
     * @throws ServiceBusException
     *
     */
    public CloudClient(String connectionstring, boolean commandEnabled, EventPublisher eventPublisher)
            throws IOException, ServiceBusException {
        this.connectionstring = connectionstring;
        this.commandEnabled = commandEnabled;
        this.eventPublisher = eventPublisher;

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

    private void setItemState(AzureDevice azureDevice, String state) {
        AzureEventCallback callback = new AzureEventCallback();

        Device device = azureDevice.getDevice();
        if (device == null) {
            logger.error("Invalid device connection for device, can not send item state update");
            return;
        }

        JsonObject datapoint = new JsonObject();
        datapoint.addProperty(DATAPOINT_DEVICE_ID, device.getDeviceId());
        datapoint.addProperty(DATAPOINT_VALUE, state);

        Message msg = new Message(gson.toJson(datapoint));

        azureDevice.sendMessage(msg, callback, lockobj);

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
            } catch (ServiceBusException e) {
                logger.warn("failed to close IoT client", e);
            }
        }
    }

    private EventHubClient receiveMessages(final String partitionId) throws ServiceBusException, IOException {
        EventHubClient client = null;
        client = EventHubClient.createFromConnectionStringSync(this.connectionstring);
        client.createReceiver(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, partitionId, Instant.now())
                .thenAccept(new Consumer<PartitionReceiver>() {
                    @Override
                    public void accept(PartitionReceiver receiver) {
                        logger.debug("Created IOT Hub receiver on partition {}", partitionId);
                        try {
                            while (true) {
                                Iterable<EventData> receivedEvents = receiver.receive(100).get();
                                processReceivedEvents(partitionId, receivedEvents);
                            }
                        } catch (Exception e) {
                            logger.debug("Failed to receive messages ", e);
                        }
                    }

                });
        return client;
    }

    private void processReceivedEvents(final String partitionId, Iterable<EventData> receivedEvents) {
        int batchSize = 0;
        if (receivedEvents != null) {
            for (EventData receivedEvent : receivedEvents) {
                logger.debug("Offset: {}, SeqNo: {}, EnqueueTime: {}", receivedEvent.getSystemProperties().getOffset(),
                        receivedEvent.getSystemProperties().getSequenceNumber(),
                        receivedEvent.getSystemProperties().getEnqueuedTime());
                String itemName = receivedEvent.getSystemProperties().get("iothub-connection-device-id").toString();
                logger.debug("| device ID: {}", itemName);
                String payload = new String(receivedEvent.getBytes(), Charset.defaultCharset());
                logger.debug("| Message Payload: {}", payload);
                eventPublisher.post(ItemEventFactory.createCommandEvent(itemName, new StringType(payload)));
                batchSize++;
            }
        }
        logger.debug("Partition: {}, ReceivedBatch Size: {}", partitionId, batchSize);
    }
}
