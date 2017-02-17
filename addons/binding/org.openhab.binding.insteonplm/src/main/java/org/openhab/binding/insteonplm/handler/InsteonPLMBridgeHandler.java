/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.insteonplm.handler;

import static org.openhab.binding.insteonplm.InsteonPLMBindingConstants.CHANNEL_1;

import java.io.IOException;
import java.util.PriorityQueue;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.insteonplm.config.InsteonPLMBridgeConfiguration;
import org.openhab.binding.insteonplm.internal.device.DeviceFeatureFactory;
import org.openhab.binding.insteonplm.internal.device.InsteonAddress;
import org.openhab.binding.insteonplm.internal.driver.IOStream;
import org.openhab.binding.insteonplm.internal.driver.MessageListener;
import org.openhab.binding.insteonplm.internal.driver.Port;
import org.openhab.binding.insteonplm.internal.driver.SerialIOStream;
import org.openhab.binding.insteonplm.internal.driver.TcpIOStream;
import org.openhab.binding.insteonplm.internal.driver.hub.HubIOStream;
import org.openhab.binding.insteonplm.internal.message.FieldException;
import org.openhab.binding.insteonplm.internal.message.Message;
import org.openhab.binding.insteonplm.internal.message.MessageFactory;
import org.openhab.binding.insteonplm.internal.utils.Utils.ParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link InsteonPLMBridgeHandler} is responsible for dealing with talking to the serial
 * port, finding the insteon devices and co-ordinating calls with the internal things.
 *
 * @author David Bennett - Initial contribution
 */
public class InsteonPLMBridgeHandler extends BaseBridgeHandler implements MessageListener {
    private Logger logger = LoggerFactory.getLogger(InsteonPLMBridgeHandler.class);
    private DeviceFeatureFactory deviceFeatureFactory;
    private MessageFactory messageFactory;
    private IOStream ioStream;
    private Port port;
    private PriorityQueue<InsteonBridgeThingQEntry> messagesToSend = new PriorityQueue<>();
    private Thread messageQueueThread;
    private InsteonAddress modemAddress;

    public InsteonPLMBridgeHandler(Bridge thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_1)) {
            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    @Override
    public void initialize() {
        InsteonPLMBridgeConfiguration config = getConfigAs(InsteonPLMBridgeConfiguration.class);
        // Connect to the port.
        try {
            deviceFeatureFactory = new DeviceFeatureFactory();
            messageFactory = new MessageFactory();
        } catch (IOException e) {
            logger.error("Exception loading xml", e);
        } catch (FieldException f) {
            logger.error("Exception with the field {}", f);
        } catch (ParsingException p) {
            logger.error("Exception with the parsing xml {}", p);
        }

        switch (config.portType) {
            case Hub:
                ioStream = new HubIOStream(config);
                break;
            case SerialPort:
                ioStream = new SerialIOStream(config);
                break;
            case Tcp:
                ioStream = new TcpIOStream(config);
                break;
            default:
                logger.error("Invalid type of port for insteon plm.");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Invalid type of port");
                return;
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE);
        port = new Port(ioStream, messageFactory);
        port.addListener(this);
        modemAddress = null;
    }

    @Override
    public void processMessage(Message message) {
        // Got a message, go online. Yay!
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
            messageQueueThread = new Thread(new RequestQueueReader());
            messageQueueThread.start();
        }
        Bridge bridge = getThing();
        try {
            if (message.getByte("Cmd") == 0x60) {
                // add the modem to the device list
                modemAddress = new InsteonAddress(message.getAddress("IMAddress"));
                logger.info("Found the modem address", modemAddress);
            }
        } catch (FieldException e) {
            logger.error("Unable to parse modem message", e);
        }

        try {
            InsteonAddress fromAddress = message.getAddress("fromAddress");
            InsteonAddress toAddress = message.getAddress("toAddress");

            // Send the message to all the handlers. This is a little inefficent, leave it for now.
            for (Thing thing : bridge.getThings()) {
                if (thing.getHandler() instanceof InsteonThingHandler) {
                    InsteonThingHandler handler = (InsteonThingHandler) thing.getHandler();
                    // Only send on messages to that specific thing.
                    if (fromAddress.equals(handler.getAddress()) || toAddress.equals(handler.getAddress())) {
                        handler.handleMessage(message);
                    }
                }
            }
        } catch (FieldException e) {
            logger.error("Unable to get the address from the message {}", message, e);
        }

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

    /** The message factory to use when making messages. */
    public MessageFactory getMessageFactory() {
        return messageFactory;
    }

    /**
     * Add device to global request queue.
     *
     * @param dev the device to add
     * @param time the time when the queue should be processed
     */
    public void addThingToSendingQueue(InsteonThingHandler handler, long time) {
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
                    logger.trace("updating request for device {} in {} msec", handler.getAddress(),
                            time - System.currentTimeMillis());
                    return;
                }
            }
            logger.trace("scheduling request for device {} in {} msec", handler.getAddress(),
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
            while (getBridge().getStatus() == ThingStatus.ONLINE) {
                InsteonBridgeThingQEntry entry = messagesToSend.peek();
                try {
                    if (messagesToSend.size() > 0) {
                        long now = System.currentTimeMillis();
                        long expTime = entry.getExpirationTime();
                        if (expTime > now) {
                            //
                            // The head of the queue is not up for processing yet, wait().
                            //
                            logger.trace("request queue head: {} must wait for {} msec",
                                    entry.getThingHandler().getAddress(), expTime - now);
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
                        long nextExp = entry.getThingHandler().processRequestQueue(now);
                        if (nextExp > 0) {
                            InsteonBridgeThingQEntry newEntry = new InsteonBridgeThingQEntry(entry.getThingHandler(),
                                    nextExp);
                            messagesToSend.add(newEntry);
                            logger.trace("device queue for {} rescheduled in {} msec",
                                    entry.getThingHandler().getAddress(), nextExp - now);
                        } else {
                            // remove from hash since queue is no longer scheduled
                            logger.debug("device queue for {} is empty!", entry.getThingHandler().getAddress());
                        }
                    }
                    logger.trace("waiting for request queues to fill");
                    messagesToSend.wait();
                } catch (InterruptedException e) {
                    logger.error("request queue thread got interrupted, breaking..", e);
                    break;
                }
            }
        }
    }
}