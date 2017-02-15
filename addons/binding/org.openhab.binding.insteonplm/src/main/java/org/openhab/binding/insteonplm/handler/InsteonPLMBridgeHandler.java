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

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.insteonplm.InsteonPLMBindingConstants;
import org.openhab.binding.insteonplm.config.InsteonPLMBridgeConfiguration;
import org.openhab.binding.insteonplm.internal.device.DeviceFeatureFactory;
import org.openhab.binding.insteonplm.internal.device.InsteonAddress;
import org.openhab.binding.insteonplm.internal.device.RequestQueueManager;
import org.openhab.binding.insteonplm.internal.driver.IOStream;
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
public class InsteonPLMBridgeHandler extends BaseBridgeHandler {
    private Logger logger = LoggerFactory.getLogger(InsteonPLMBridgeHandler.class);
    private RequestQueueManager requestQueueManager;
    private DeviceFeatureFactory deviceFeatureFactory;
    private MessageFactory messageFactory;
    private IOStream ioStream;
    private Port port;
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
            requestQueueManager = new RequestQueueManager();
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
        modemAddress = null;
    }

    public void handleMessage(Message message, InsteonAddress address) {
        if (address != null && address.equals(modemAddress)) {
            // To the modem, then the bridge itself will process this packet.
        } else if (address != null) {
            Bridge bridge = getThing();
            // Find the device based on the address.
            for (Thing thing : bridge.getThings()) {
                if (thing.getProperties().get(InsteonPLMBindingConstants.PROPERTY_INSTEON_ADDRESS)
                        .equals(address.toString())) {
                    // Yay!
                    InsteonThingHandler handler = (InsteonThingHandler) thing.getHandler();
                    handler.handleMessage(message);
                }
            }
        }
    }

    /** Gets the thing associated with this address. */
    public Thing getDevice(InsteonAddress a) {
        return null;
    }

    public void startScan() {
        // Create a message to query the modem for devices.
    }

    /** The factory to make device features. */
    public DeviceFeatureFactory getDeviceFeatureFactory() {
        return deviceFeatureFactory;
    }

    /** The queue to handle talking to the devices. */
    public RequestQueueManager getRequestQueueManager() {
        return requestQueueManager;
    }

    /** The message factory to use when making messages. */
    public MessageFactory getMessageFactory() {
        return messageFactory;
    }
}
