/**
 * Copyright (c) 2018,2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.openthermgateway.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.openthermgateway.OpenThermGatewayBindingConstants;
import org.openhab.binding.openthermgateway.internal.CommandType;
import org.openhab.binding.openthermgateway.internal.DataItem;
import org.openhab.binding.openthermgateway.internal.DataItemGroup;
import org.openhab.binding.openthermgateway.internal.Message;
import org.openhab.binding.openthermgateway.internal.OpenThermGatewayCallback;
import org.openhab.binding.openthermgateway.internal.OpenThermGatewayConfiguration;
import org.openhab.binding.openthermgateway.internal.OpenThermGatewayConnector;
import org.openhab.binding.openthermgateway.internal.OpenThermGatewaySocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenThermGatewayHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Arjen Korevaar - Updated channels
 */
@NonNullByDefault
public class OpenThermGatewayHandler extends BaseThingHandler implements OpenThermGatewayCallback {

    private final Logger logger = LoggerFactory.getLogger(OpenThermGatewayHandler.class);

    @Nullable
    private OpenThermGatewayConfiguration config;

    @Nullable
    private OpenThermGatewayConnector connector;

    @Nullable
    private Thread connectorThread;

    public OpenThermGatewayHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received channel: {}, command: {}", channelUID, command);

        String channel = channelUID.getId();

        if (channel.equals(OpenThermGatewayBindingConstants.CHANNEL_ROOM_SETPOINT)) {
            connector.sendCommand(CommandType.TemperatureTemporary, command.toFullString());
        }

        if (channel.equals(OpenThermGatewayBindingConstants.CHANNEL_OUTSIDE_TEMPERATURE)) {
            connector.sendCommand(CommandType.TemperatureOutside, command.toFullString());
        }

        // updateState(OpenThermGatewayBindingConstants.CHANNEL_ROOM_TEMPERATURE, TypeConverter.toDecimalType(10));

        // switch (channelUID.getId()) {
        // case OpenThermGatewayBindingConstants.CHANNEL_CENTRAL_HEATING_ENABLE:
        // // updateState(channelUID, getTemperature());
        // break;
        // }

        // if (channelUID.getId().equals(OpenThermGatewayBindingConstants.CHANNEL_CENTRAL_HEATING_ENABLE)) {
        // TODO: handle command

        // Note: if communication with thing fails for some reason,
        // indicate that by setting the status with detail information
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
        // "Could not control device at IP address x.x.x.x");

        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "");
        // }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing OpenTherm Gateway handler for uid '{}'", getThing().getUID());

        updateStatus(ThingStatus.OFFLINE);

        config = getConfigAs(OpenThermGatewayConfiguration.class);

        // TODO: support different kinds of connectors, such as USB, serial port

        connector = new OpenThermGatewaySocketConnector(this, config.ipaddress, config.port);

        connectorThread = new Thread(connector);
        connectorThread.start();
    }

    @Override
    public void connecting() {
        updateStatus(ThingStatus.OFFLINE);
    }

    @Override
    public void connected() {
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void disconnected() {
        updateStatus(ThingStatus.OFFLINE);
    }

    @Override
    public void receiveMessage(@Nullable Message message) {
        if (message == null) {
            return;
        }

        if (DataItemGroup.dataItemGroups.containsKey(message.getID())) {
            DataItem[] dataItems = DataItemGroup.dataItemGroups.get(message.getID());

            for (int i = 0; i < dataItems.length; i++) {
                DataItem dataItem = dataItems[i];

                String channelId = dataItem.getSubject();

                logger.debug("Received update for channel '{}'", channelId);

                if (!OpenThermGatewayBindingConstants.SUPPORTED_CHANNEL_IDS.contains(channelId)) {
                    continue;
                }

                State state = null;

                switch (dataItem.getDataType()) {
                    case Flags:
                        state = TypeConverter.toOnOffType(message.getBit(dataItem.getByteType(), dataItem.getBitPos()));
                        break;
                    case Uint8:
                    case Uint16:
                        state = TypeConverter.toDecimalType(message.getUInt(dataItem.getByteType()));
                        break;
                    case Int8:
                    case Int16:
                        state = TypeConverter.toDecimalType(message.getInt(dataItem.getByteType()));
                        break;
                    case Float:
                        state = TypeConverter.toDecimalType(message.getFloat());
                        break;
                    case DoWToD:
                        break;
                }

                if (state != null) {
                    updateState(channelId, state);
                }
            }
        }
    }
}
