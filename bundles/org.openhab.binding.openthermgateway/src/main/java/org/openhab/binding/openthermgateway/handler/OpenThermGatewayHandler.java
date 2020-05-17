/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.openthermgateway.handler;

import java.util.concurrent.TimeUnit;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.openthermgateway.OpenThermGatewayBindingConstants;
import org.openhab.binding.openthermgateway.internal.DataItem;
import org.openhab.binding.openthermgateway.internal.DataItemGroup;
import org.openhab.binding.openthermgateway.internal.GatewayCommand;
import org.openhab.binding.openthermgateway.internal.GatewayCommandCode;
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
 * @author Arjen Korevaar - Initial contribution
 */
@NonNullByDefault
public class OpenThermGatewayHandler extends BaseThingHandler implements OpenThermGatewayCallback {

    private final Logger logger = LoggerFactory.getLogger(OpenThermGatewayHandler.class);

    private @Nullable OpenThermGatewayConfiguration config;

    private @Nullable OpenThermGatewayConnector connector;

    private boolean connecting = false;

    private boolean explicitDisconnect = false;

    public OpenThermGatewayHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing OpenTherm Gateway handler for uid '{}'", getThing().getUID());

        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Initializing");

        config = getConfigAs(OpenThermGatewayConfiguration.class);

        connect();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received channel: {}, command: {}", channelUID, command);

        if (!(command instanceof RefreshType)) {
            String channel = channelUID.getId();
            String code = getGatewayCodeFromChannel(channel);

            GatewayCommand gatewayCommand = null;

            if (command instanceof QuantityType<?>) {
                QuantityType<?> quantityType = ((QuantityType<?>) command).toUnit(SIUnits.CELSIUS);

                if (quantityType != null) {
                    double value = quantityType.doubleValue();
                    gatewayCommand = GatewayCommand.parse(code, Double.toString(value));
                }
            }

            if (gatewayCommand == null) {
                gatewayCommand = GatewayCommand.parse(code, command.toFullString());
            }

            if (checkConnection()) {
                @Nullable
                OpenThermGatewayConnector conn = connector;

                if (conn != null) {
                    conn.sendCommand(gatewayCommand);
                }
            }
        }
    }

    @Override
    public void connecting() {
        connecting = true;
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Connecting");
    }

    @Override
    public void connected() {
        connecting = false;
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void disconnected() {
        @Nullable
        OpenThermGatewayConnector conn = connector;

        @Nullable
        OpenThermGatewayConfiguration conf = config;

        connecting = false;

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Disconnected");

        // retry connection if disconnect is not explicitly requested
        if (conf != null && !explicitDisconnect && conf.connectionRetryInterval > 0) {
            scheduler.schedule(() -> {
                if (conn != null && !connecting && !conn.isConnected()) {
                    connect();
                }
            }, conf.connectionRetryInterval, TimeUnit.SECONDS);
        }
    }

    @Override
    public void receiveMessage(Message message) {
        if (DataItemGroup.dataItemGroups.containsKey(message.getID())) {
            DataItem[] dataItems = DataItemGroup.dataItemGroups.get(message.getID());

            for (DataItem dataItem : dataItems) {
                String channelId = dataItem.getSubject();

                if (!OpenThermGatewayBindingConstants.SUPPORTED_CHANNEL_IDS.contains(channelId)) {
                    continue;
                }

                State state = null;

                switch (dataItem.getDataType()) {
                    case FLAGS:
                        state = OnOffType.from(message.getBit(dataItem.getByteType(), dataItem.getBitPos()));
                        break;
                    case UINT8:
                    case UINT16:
                        state = new DecimalType(message.getUInt(dataItem.getByteType()));
                        break;
                    case INT8:
                    case INT16:
                        state = new DecimalType(message.getInt(dataItem.getByteType()));
                        break;
                    case FLOAT:
                        float value = message.getFloat();
                        @Nullable
                        Unit<?> unit = dataItem.getUnit();
                        state = (unit == null) ? new DecimalType(value) : new QuantityType<>(value, unit);
                        break;
                    case DOWTOD:
                        break;
                }

                if (state != null) {
                    logger.debug("Received update for channel '{}': {}", channelId, state);
                    updateState(channelId, state);
                }
            }
        }
    }

    @Override
    public void handleRemoval() {
        logger.debug("Removing OpenTherm Gateway handler");
        disconnect();
        super.handleRemoval();
    }

    @Override
    public void dispose() {
        disconnect();
        super.dispose();
    }

    private boolean checkConnection() {
        @Nullable
        OpenThermGatewayConnector conn = connector;

        if (conn != null && conn.isConnected()) {
            return true;
        }

        return connect();
    }

    private boolean connect() {
        @Nullable
        OpenThermGatewayConfiguration conf = config;

        disconnect();

        if (conf != null) {
            logger.debug("Starting OpenTherm Gateway connector");

            explicitDisconnect = false;

            connector = new OpenThermGatewaySocketConnector(this, conf.ipaddress, conf.port);

            Thread thread = new Thread(connector, "OpenTherm Gateway Binding - socket listener thread");
            thread.setDaemon(true);
            thread.start();

            logger.debug("OpenTherm Gateway connector started");

            return true;
        }

        return false;
    }

    private void disconnect() {
        @Nullable
        OpenThermGatewayConnector conn = connector;

        if (conn != null) {
            if (conn.isConnected()) {
                logger.debug("Stopping OpenTherm Gateway connector");

                explicitDisconnect = true;
                conn.stop();
            }

            connector = null;
        }
    }

    private @Nullable String getGatewayCodeFromChannel(String channel) throws IllegalArgumentException {
        switch (channel) {
            case OpenThermGatewayBindingConstants.CHANNEL_OVERRIDE_SETPOINT_TEMPORARY:
                return GatewayCommandCode.TemperatureTemporary;
            case OpenThermGatewayBindingConstants.CHANNEL_OVERRIDE_SETPOINT_CONSTANT:
                return GatewayCommandCode.TemperatureConstant;
            case OpenThermGatewayBindingConstants.CHANNEL_OUTSIDE_TEMPERATURE:
                return GatewayCommandCode.TemperatureOutside;
            case OpenThermGatewayBindingConstants.CHANNEL_OVERRIDE_DHW_SETPOINT:
                return GatewayCommandCode.SetpointWater;
            case OpenThermGatewayBindingConstants.CHANNEL_SEND_COMMAND:
                return null;
            default:
                throw new IllegalArgumentException(String.format("Unknown channel %s", channel));
        }
    }
}
