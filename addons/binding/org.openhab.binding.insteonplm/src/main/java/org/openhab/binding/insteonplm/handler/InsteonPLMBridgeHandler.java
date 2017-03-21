/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.insteonplm.handler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.joda.time.DateTime;
import org.openhab.binding.insteonplm.internal.config.InsteonConfigProvider;
import org.openhab.binding.insteonplm.internal.config.InsteonPLMBridgeConfiguration;
import org.openhab.binding.insteonplm.internal.config.InsteonProduct;
import org.openhab.binding.insteonplm.internal.device.DeviceFeatureFactory;
import org.openhab.binding.insteonplm.internal.device.InsteonAddress;
import org.openhab.binding.insteonplm.internal.driver.IOStream;
import org.openhab.binding.insteonplm.internal.driver.MessageListener;
import org.openhab.binding.insteonplm.internal.driver.Port;
import org.openhab.binding.insteonplm.internal.driver.SerialIOStream;
import org.openhab.binding.insteonplm.internal.driver.TcpIOStream;
import org.openhab.binding.insteonplm.internal.driver.hub.HubIOStream;
import org.openhab.binding.insteonplm.internal.message.FieldException;
import org.openhab.binding.insteonplm.internal.message.InsteonFlags;
import org.openhab.binding.insteonplm.internal.message.StandardInsteonMessages;
import org.openhab.binding.insteonplm.internal.message.modem.AllLinkRecordResponse;
import org.openhab.binding.insteonplm.internal.message.modem.BaseModemMessage;
import org.openhab.binding.insteonplm.internal.message.modem.GetFirstAllLinkingRecord;
import org.openhab.binding.insteonplm.internal.message.modem.GetIMInfo;
import org.openhab.binding.insteonplm.internal.message.modem.GetNextAllLinkingRecord;
import org.openhab.binding.insteonplm.internal.message.modem.SendInsteonMessage;
import org.openhab.binding.insteonplm.internal.message.modem.StandardMessageReceived;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * The {@link InsteonPLMBridgeHandler} is responsible for dealing with talking to the serial
 * port, finding the insteon devices and co-ordinating calls with the internal things.
 *
 * @author David Bennett - Initial contribution
 */
public class InsteonPLMBridgeHandler extends BaseBridgeHandler implements MessageListener {
    private Logger logger = LoggerFactory.getLogger(InsteonPLMBridgeHandler.class);
    private DeviceFeatureFactory deviceFeatureFactory;
    private IOStream ioStream;
    private Port port;
    private PriorityQueue<InsteonBridgeThingQEntry> messagesToSend = new PriorityQueue<>();
    private Thread messageQueueThread;
    private InsteonAddress modemAddress;
    private Map<InsteonAddress, InsteonNodeDetails> foundDevices = Maps.newHashMap();
    private Map<Integer, List<InsteonAddress>> groups = Maps.newHashMap();
    private DateTime lastRequested = DateTime.now().minusDays(2);

    public InsteonPLMBridgeHandler(Bridge thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        // Connect to the port.
        deviceFeatureFactory = new DeviceFeatureFactory();

        startupPort();
    }

    @Override
    public void dispose() {
        logger.error("Disposing the bridge");
        super.dispose();
        if (this.ioStream != null) {
            this.ioStream.close();
        }
        this.ioStream = null;
        if (this.port != null) {
            this.port.stop();
        }
        this.port = null;
        this.deviceFeatureFactory = null;

        this.messagesToSend.clear();
        this.messagesToSend = null;
        if (this.messageQueueThread != null) {
            this.messageQueueThread.interrupt();
        }
        this.messageQueueThread = null;
    }

    private void startupPort() {
        InsteonPLMBridgeConfiguration config = getConfigAs(InsteonPLMBridgeConfiguration.class);
        logger.error("config {}", config);

        switch (config.getPortType()) {
            case Hub:
                ioStream = new HubIOStream(config);
                break;
            case SerialPort:
                ioStream = new SerialIOStream(config);
                if (!ioStream.open()) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Unable to open port " + config.getSerialPort());
                    return;
                }
                break;
            case Tcp:
                ioStream = new TcpIOStream(config);
                break;
            default:
                logger.error("Invalid type of port for insteon plm.");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Invalid type of port");
                return;
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Connecting to port");
        port = new Port(ioStream);
        port.addListener(this);
        modemAddress = null;
        if (port.start()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Port Started");
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Unable to start communicating on " + config.getSerialPort());
        }

        // Start downloading the link db.
        try {
            port.writeMessage(new GetIMInfo());
        } catch (IOException e) {
            logger.error("error sending link record query ", e);
        }

    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        super.handleConfigurationUpdate(configurationParameters);
        // Stop the port then restart it to pick up the new configuration.
        if (this.port != null) {
            this.port.stop();
        }
        startupPort();
    }

    @Override
    public void processMessage(BaseModemMessage message) {
        // Got a message, go online. Yay!
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
            messageQueueThread = new Thread(new RequestQueueReader());
            messageQueueThread.start();
        }
        Bridge bridge = getThing();
        switch (message.getMessageType()) {
            case StandardMessageReceived:
                StandardMessageReceived messReceived = (StandardMessageReceived) message;
                if (!foundDevices.containsKey(messReceived.getFromAddress())) {
                    InsteonNodeDetails details = new InsteonNodeDetails();
                    foundDevices.put(messReceived.getFromAddress(), details);
                    // Send a product request.
                    try {
                        port.writeMessage(new SendInsteonMessage(messReceived.getFromAddress(), new InsteonFlags(),
                                StandardInsteonMessages.ProductDataRequest, (byte) 0x00));
                    } catch (IOException e) {
                        logger.error("Error sending message to get data for {}", messReceived.getFromAddress());
                    }
                }
                if (messReceived.getCmd1() == StandardInsteonMessages.ExtendedProductDataRequest) {
                    byte productKeyMsb = messReceived.getData()[2];
                    byte productKeyMsb2 = messReceived.getData()[3];
                    byte productKeyLsb = messReceived.getData()[4];
                    byte deviceCategory = messReceived.getData()[5];
                    byte deviceSubcategory = messReceived.getData()[6];
                    InsteonNodeDetails details;
                    synchronized (foundDevices) {
                        details = foundDevices.get(messReceived.getFromAddress());
                        details.setQueried(true);
                        details.setDeviceCategory(deviceCategory);
                        details.setDeviceSubcategory(deviceSubcategory);
                        details.setProductKey((productKeyMsb << 16) | (productKeyMsb2 << 8) | productKeyLsb);
                    }
                    // Query the string details, product data request with a 0x02 is a string request.
                    try {
                        port.writeMessage(new SendInsteonMessage(messReceived.getFromAddress(), new InsteonFlags(),
                                StandardInsteonMessages.ProductDataRequest, (byte) 0x02));
                    } catch (IOException e) {
                        logger.error("Unable to write request for device text string", e);
                    }

                    logger.error("Details {}", details);
                    // Lets see if we can find this device.
                    InsteonProduct product = InsteonConfigProvider.getInsteonProduct(details.getDeviceCategory(),
                            details.getDeviceSubcategory());
                    if (product != null) {
                        logger.error("Found insteon product {} {}", product.getModel(), product.getThingTypeUID());
                    }
                    return;
                } else if (messReceived.getCmd1() == StandardInsteonMessages.ExtendedDeviceTextStringResponse) {
                    StringBuilder builder = new StringBuilder();

                    for (int i = 1; i < 15; i++) {
                        builder.append((char) messReceived.getData()[i]);
                    }
                    logger.error("Received device desc {}", builder.toString());
                    sendNextInfoRequest();
                    return;
                }

                // Go and find the device to send this message to.
                // Send the message to all the handlers. This is a little inefficent, leave it for now.
                for (Thing thing : bridge.getThings()) {
                    if (thing.getHandler() instanceof InsteonThingHandler) {
                        InsteonThingHandler handler = (InsteonThingHandler) thing.getHandler();
                        // Only send on messages to that specific thing.
                        if (messReceived.getFromAddress().equals(handler.getAddress())
                                || messReceived.getToAddress().equals(handler.getAddress())) {
                            try {
                                handler.handleMessage(messReceived);
                            } catch (FieldException e) {
                                logger.error("Error handling message {}", message, e);
                            }
                        }
                    }
                }

                break;

            case GetImInfo:
                GetIMInfo info = (GetIMInfo) message;
                // add the modem to the device list
                modemAddress = info.getModemAddress();
                logger.info("Found the modem address {}", modemAddress);
                try {
                    port.writeMessage(new GetFirstAllLinkingRecord());
                } catch (IOException e) {
                    logger.error("Unable to send first linking message");
                }
                return;

            case AllLinkRecordResponse:
                AllLinkRecordResponse response = (AllLinkRecordResponse) message;
                // Found a device msg.getAddress("LinkAddr")
                InsteonNodeDetails details = new InsteonNodeDetails();
                synchronized (foundDevices) {
                    if (!foundDevices.containsKey(response.getAddress())) {
                        foundDevices.put(response.getAddress(), details);
                        logger.error("Found device {}", response.getAddress());
                    }
                }
                // Group 1 is the default group everything is in.
                if (response.getGroup() != 1) {
                    synchronized (groups) {
                        if (!groups.containsKey(response.getGroup())) {
                            groups.put(Integer.valueOf(response.getGroup()), Lists.<InsteonAddress> newArrayList());
                            logger.error("Found group {}", response.getGroup());
                        }
                        groups.get(response.getGroup()).add(response.getAddress());
                    }
                }
                details.setQueried(false);
                // Send a request for the next one.
                try {
                    port.writeMessage(new GetNextAllLinkingRecord());
                } catch (IOException e) {
                    logger.error("Unable to send next all link record");
                }
                return;

            case PureNack:
                // Explicit nack.
                logger.info("Pure nack recieved.");
                break;

            case GetFirstAllLinkRecord:
            case GetNextAllLinkRecord:
                if (message.isNack()) {
                    logger.debug("got all link records.");
                }
                lastRequested = DateTime.now();
                // Now request the data for all the devices. Yay.
                sendNextInfoRequest();
                return;

            default:
                logger.warn("Unhandled insteon message {}", message.getMessageType());
                break;
        }

    }

    private void sendNextInfoRequest() {
        synchronized (foundDevices) {
            for (InsteonAddress address : foundDevices.keySet()) {
                InsteonNodeDetails detail = foundDevices.get(address);
                if (!detail.isQueried()) {
                    try {
                        logger.error("Sending info request for {}", address);
                        port.writeMessage(new SendInsteonMessage(address, new InsteonFlags(),
                                StandardInsteonMessages.ExtendedProductDataRequest));
                    } catch (IOException e) {
                        logger.error("Unable to send the request for data", e);
                    }
                    detail.setQueried(true);
                    return;
                }
            }
        }
        // Don't request anything again for a while...
        lastRequested = DateTime.now().plusHours(6);
    }

    /** Gets the thing associated with this address. */
    public InsteonThingHandler getDevice(InsteonAddress a) {
        for (Thing thing : getBridge().getThings()) {
            if (thing.getHandler() instanceof InsteonThingHandler) {
                InsteonThingHandler handler = (InsteonThingHandler) thing.getHandler();
                if (handler.getAddress().equals(a)) {
                    return handler;
                }
            }
        }
        return null;
    }

    public void startScan() {
        // Create a message to query the modem for devices.
    }

    /** The factory to make device features. */
    public DeviceFeatureFactory getDeviceFeatureFactory() {
        return deviceFeatureFactory;
    }

    /**
     * Add device to global request queue.
     *
     * @param dev the device to add
     * @param time the time when the queue should be processed
     */
    public void addThingToSendingQueue(InsteonPlmBaseThing handler, long time) {
        synchronized (messagesToSend) {
            // See if we can find the entry first.
            for (InsteonBridgeThingQEntry entry : messagesToSend) {
                if (entry.getThingHandler() == handler) {
                    long expTime = entry.getExpirationTime();
                    if (expTime > time) {
                        entry.setExpirationTime(time);
                    }
                    messagesToSend.remove(entry);
                    messagesToSend.add(entry);
                    messagesToSend.notify();
                    logger.trace("updating request for device {} in {} msec", handler.toString(),
                            time - System.currentTimeMillis());
                    return;
                }
            }
            logger.trace("scheduling request for device {} in {} msec", handler.toString(),
                    time - System.currentTimeMillis());
            InsteonBridgeThingQEntry entry = new InsteonBridgeThingQEntry(handler, time);
            // add the queue back in after (maybe) having modified
            // the expiration time
            messagesToSend.add(entry);
            messagesToSend.notify();
        }
    }

    class RequestQueueReader implements Runnable {
        @Override
        public void run() {
            logger.debug("starting request queue thread");
            // Run while we are online.
            while (getThing().getStatus() == ThingStatus.ONLINE) {
                InsteonBridgeThingQEntry entry = messagesToSend.peek();
                try {
                    // See if we have any info requests to send.
                    if (lastRequested.isBefore(DateTime.now().minusSeconds(2))) {
                        sendNextInfoRequest();
                    }
                    if (messagesToSend.size() > 0) {
                        long now = System.currentTimeMillis();
                        long expTime = entry.getExpirationTime();
                        if (expTime > now) {
                            //
                            // The head of the queue is not up for processing yet, wait().
                            //
                            logger.trace("request queue head: {} must wait for {} msec",
                                    entry.getThingHandler().toString(), expTime - now);
                            //
                            // note that the wait() can also return because of changes to
                            // the queue, not just because the time expired!
                            //
                            continue;
                        }
                        //
                        // The head of the queue has expired and can be processed!
                        //
                        entry = messagesToSend.poll(); // remove front element
                        long nextExp;
                        try {
                            nextExp = entry.getThingHandler().processRequestQueue(port, now);
                            if (nextExp > 0) {
                                InsteonBridgeThingQEntry newEntry = new InsteonBridgeThingQEntry(
                                        entry.getThingHandler(), nextExp);
                                messagesToSend.add(newEntry);
                                logger.trace("device queue for {} rescheduled in {} msec",
                                        entry.getThingHandler().toString(), nextExp - now);
                            } else {
                                // remove from hash since queue is no longer scheduled
                                logger.debug("device queue for {} is empty!", entry.getThingHandler().toString());
                            }
                        } catch (IOException e) {
                            logger.error("request queue thread unable to write to port..", e);
                        }
                    }
                    logger.trace("waiting for request queues to fill");
                    messagesToSend.wait();
                } catch (InterruptedException e) {
                    logger.error("request queue thread got interrupted, breaking..", e);
                    break;
                }
            }
            logger.error("exiting request queue thread");
        }
    }
}