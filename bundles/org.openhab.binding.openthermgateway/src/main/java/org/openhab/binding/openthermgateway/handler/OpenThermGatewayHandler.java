/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.openthermgateway.internal.ConnectionState;
import org.openhab.binding.openthermgateway.internal.DataItemGroup;
import org.openhab.binding.openthermgateway.internal.GatewayCommand;
import org.openhab.binding.openthermgateway.internal.GatewayCommandCode;
import org.openhab.binding.openthermgateway.internal.Message;
import org.openhab.binding.openthermgateway.internal.OpenThermGatewayBindingConstants;
import org.openhab.binding.openthermgateway.internal.OpenThermGatewayCallback;
import org.openhab.binding.openthermgateway.internal.OpenThermGatewayConfiguration;
import org.openhab.binding.openthermgateway.internal.OpenThermGatewayConnector;
import org.openhab.binding.openthermgateway.internal.OpenThermGatewaySocketConnector;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenThermGatewayHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Arjen Korevaar - Initial contribution
 */
@NonNullByDefault
public class OpenThermGatewayHandler extends BaseBridgeHandler implements OpenThermGatewayCallback {
    private static final String PROPERTY_GATEWAY_ID_NAME = "gatewayId";
    private static final String PROPERTY_GATEWAY_ID_TAG = "PR: A=";

    private final Logger logger = LoggerFactory.getLogger(OpenThermGatewayHandler.class);

    private @Nullable OpenThermGatewayConfiguration configuration;
    private @Nullable OpenThermGatewayConnector connector;
    private @Nullable ScheduledFuture<?> reconnectTask;

    private @Nullable ConnectionState state;
    private boolean autoReconnect = true;
    private boolean disposing = false;

    public OpenThermGatewayHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing OpenThermGateway handler for uid {}", getThing().getUID());

        configuration = getConfigAs(OpenThermGatewayConfiguration.class);
        logger.debug("Using configuration: {}", configuration);

        disposing = false;
        updateStatus(ThingStatus.UNKNOWN);
        connect();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command {} for channel {}", command, channelUID);

        if (!(command instanceof RefreshType)) {
            String channel = channelUID.getId();
            String code = getGatewayCodeFromChannel(channel);

            GatewayCommand gatewayCommand = null;

            if (command instanceof OnOffType) {
                OnOffType onOff = (OnOffType) command;
                gatewayCommand = GatewayCommand.parse(code, onOff == OnOffType.ON ? "1" : "0");
            }
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

            sendCommand(gatewayCommand);

            if (GatewayCommandCode.CONTROLSETPOINT.equals(code)) {
                if (gatewayCommand.getMessage().equals("0.0")) {
                    updateState(OpenThermGatewayBindingConstants.CHANNEL_OVERRIDE_CENTRAL_HEATING_WATER_SETPOINT,
                            UnDefType.UNDEF);
                }
                updateState(OpenThermGatewayBindingConstants.CHANNEL_OVERRIDE_CENTRAL_HEATING_ENABLED,
                        OnOffType.from(!gatewayCommand.getMessage().equals("0.0")));
            } else if (GatewayCommandCode.CONTROLSETPOINT2.equals(code)) {
                if (gatewayCommand.getMessage().equals("0.0")) {
                    updateState(OpenThermGatewayBindingConstants.CHANNEL_OVERRIDE_CENTRAL_HEATING2_WATER_SETPOINT,
                            UnDefType.UNDEF);
                }
                updateState(OpenThermGatewayBindingConstants.CHANNEL_OVERRIDE_CENTRAL_HEATING2_ENABLED,
                        OnOffType.from(!gatewayCommand.getMessage().equals("0.0")));
            }
        }
    }

    public void sendCommand(GatewayCommand gatewayCommand) {
        @Nullable
        OpenThermGatewayConnector conn = connector;

        if (conn != null && conn.isConnected()) {
            conn.sendCommand(gatewayCommand);
        } else {
            logger.debug("Unable to send command {}: connector not connected", gatewayCommand.toFullString());
        }
    }

    @Override
    public void receiveMessage(Message message) {
        scheduler.submit(() -> receiveMessageTask(message));
    }

    private void receiveMessageTask(Message message) {
        int msgId = message.getID();

        if (!DataItemGroup.DATAITEMGROUPS.containsKey(msgId)) {
            logger.debug("Unsupported message id {}", msgId);
            return;
        }

        for (Thing thing : getThing().getThings()) {
            BaseDeviceHandler handler = (BaseDeviceHandler) thing.getHandler();

            if (handler != null) {
                handler.receiveMessage(message);
            }
        }
    }

    @Override
    public void connectionStateChanged(ConnectionState state) {
        scheduler.submit(() -> connectionStateChangedTask(state));
    }

    private void connectionStateChangedTask(ConnectionState state) {
        if (this.state != state) {
            this.state = state;

            switch (state) {
                case CONNECTED:
                    updateStatus(ThingStatus.ONLINE);
                    cancelAutoReconnect();
                    break;
                case DISCONNECTED:
                    if (!disposing) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                        autoReconnect();
                    }
                default:
            }
        }
    }

    @Override
    public void receiveAcknowledgement(String message) {
        scheduler.submit(() -> receiveAcknowledgementTask(message));
    }

    private void receiveAcknowledgementTask(String message) {
        if (message.startsWith(PROPERTY_GATEWAY_ID_TAG)) {
            getThing().setProperty(PROPERTY_GATEWAY_ID_NAME,
                    message.substring(PROPERTY_GATEWAY_ID_TAG.length()).strip());
        }
    }

    @Override
    public void handleRemoval() {
        logger.debug("Removing OpenThermGateway handler");
        disconnect();
        super.handleRemoval();
    }

    @Override
    public void dispose() {
        logger.debug("Disposing OpenThermGateway handler");
        disposing = true;
        disconnect();
        super.dispose();
    }

    private void connect() {
        @Nullable
        OpenThermGatewayConfiguration config = configuration;

        if (this.state == ConnectionState.CONNECTING) {
            logger.debug("OpenThermGateway connector is already connecting");
            return;
        }

        // Make sure everything is cleaned up before creating a new connection
        disconnect();

        if (config != null) {
            connectionStateChanged(ConnectionState.INITIALIZING);

            logger.debug("Starting OpenThermGateway connector");

            autoReconnect = true;

            OpenThermGatewayConnector conn = connector = new OpenThermGatewaySocketConnector(this, config);
            conn.start();

            logger.debug("OpenThermGateway connector started");
        }
    }

    private void disconnect() {
        updateStatus(ThingStatus.OFFLINE);

        autoReconnect = false;

        cancelAutoReconnect();

        @Nullable
        OpenThermGatewayConnector conn = connector;
        if (conn != null) {
            conn.stop();
            connector = null;
        }
    }

    private void autoReconnect() {
        @Nullable
        OpenThermGatewayConfiguration config = configuration;

        if (autoReconnect && config != null && config.connectionRetryInterval > 0) {
            logger.debug("Scheduling to auto reconnect in {} seconds", config.connectionRetryInterval);
            reconnectTask = scheduler.schedule(this::connect, config.connectionRetryInterval, TimeUnit.SECONDS);
        }
    }

    private void cancelAutoReconnect() {
        ScheduledFuture<?> localReconnectTask = reconnectTask;

        if (localReconnectTask != null) {
            if (!localReconnectTask.isDone()) {
                logger.debug("Cancelling auto reconnect task");
                localReconnectTask.cancel(true);
            }

            reconnectTask = null;
        }
    }

    private @Nullable String getGatewayCodeFromChannel(String channel) throws IllegalArgumentException {
        switch (channel) {
            case OpenThermGatewayBindingConstants.CHANNEL_OVERRIDE_SETPOINT_TEMPORARY:
                return GatewayCommandCode.TEMPERATURETEMPORARY;
            case OpenThermGatewayBindingConstants.CHANNEL_OVERRIDE_SETPOINT_CONSTANT:
                return GatewayCommandCode.TEMPERATURECONSTANT;
            case OpenThermGatewayBindingConstants.CHANNEL_OUTSIDE_TEMPERATURE:
                return GatewayCommandCode.TEMPERATUREOUTSIDE;
            case OpenThermGatewayBindingConstants.CHANNEL_OVERRIDE_DHW_SETPOINT:
                return GatewayCommandCode.SETPOINTWATER;
            case OpenThermGatewayBindingConstants.CHANNEL_OVERRIDE_CENTRAL_HEATING_WATER_SETPOINT:
                return GatewayCommandCode.CONTROLSETPOINT;
            case OpenThermGatewayBindingConstants.CHANNEL_OVERRIDE_CENTRAL_HEATING_ENABLED:
                return GatewayCommandCode.CENTRALHEATING;
            case OpenThermGatewayBindingConstants.CHANNEL_OVERRIDE_CENTRAL_HEATING2_WATER_SETPOINT:
                return GatewayCommandCode.CONTROLSETPOINT2;
            case OpenThermGatewayBindingConstants.CHANNEL_OVERRIDE_CENTRAL_HEATING2_ENABLED:
                return GatewayCommandCode.CENTRALHEATING2;
            case OpenThermGatewayBindingConstants.CHANNEL_OVERRIDE_VENTILATION_SETPOINT:
                return GatewayCommandCode.VENTILATIONSETPOINT;
            case OpenThermGatewayBindingConstants.CHANNEL_SEND_COMMAND:
                return null;
            default:
                throw new IllegalArgumentException(String.format("Unknown channel %s", channel));
        }
    }
}
